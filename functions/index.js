const functions = require("firebase-functions");
const admin = require("firebase-admin");
const crypto = require("crypto");
const engine = require("./engine");

admin.initializeApp();
const db = admin.firestore();

const SERVER_SALT = process.env.CODE_SALT || "loto_niger_secure_salt_2026_@roa_vision";
const OWNER_EMAIL = process.env.OWNER_EMAIL || "bachiroudjibomali@gmail.com";
const CODE_CHARSET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"; // 32 caractères sans 0, O, 1, I

function hashCode(rawCode) {
    const clean = rawCode.trim().toUpperCase();
    return crypto.createHash("sha256").update(clean + SERVER_SALT).digest("hex");
}

function generateRandomCode(tier) {
    const prefix = tier === "plus" ? "PRO-" : "ESS-";
    let randomPart = "";
    for (let i = 0; i < 8; i++) {
        const randIndex = crypto.randomInt(0, CODE_CHARSET.length);
        randomPart += CODE_CHARSET[randIndex];
    }
    return `${prefix}${randomPart}`;
}

async function checkRateLimit(idAppareil) {
    if (!idAppareil) return;
    const docRef = db.collection("tentatives").doc(idAppareil);
    const snap = await docRef.get();
    if (snap.exists) {
        const data = snap.data();
        const now = Date.now();
        if (data.bloque_jusqu_a && data.bloque_jusqu_a > now) {
            const minutesRestantes = Math.ceil((data.bloque_jusqu_a - now) / 60000);
            throw new functions.https.HttpsError(
                "resource-exhausted",
                `Trop de tentatives infructueuses. Cet appareil est temporairement bloqué pendant ${minutesRestantes} minute(s).`
            );
        }
    }
}

async function recordFailedAttempt(idAppareil, codePrefix) {
    if (!idAppareil) return;
    const docRef = db.collection("tentatives").doc(idAppareil);
    const snap = await docRef.get();
    const now = Date.now();
    let count = 1;

    if (snap.exists) {
        const data = snap.data();
        // Si le dernier échec date de plus de 15 minutes, réinitialiser le compteur
        if (data.dernier_echec && (now - data.dernier_echec > 15 * 60 * 1000)) {
            count = 1;
        } else {
            count = (data.count || 0) + 1;
        }
    }

    const updates = {
        count,
        dernier_echec: now,
        bloque_jusqu_a: count >= 5 ? now + (15 * 60 * 1000) : null
    };

    await docRef.set(updates, { merge: true });

    // Journal d'activation serveur
    await db.collection("logs_activations").add({
        date: admin.firestore.FieldValue.serverTimestamp(),
        idAppareil,
        code_prefix: codePrefix || "UNKNOWN",
        resultat: count >= 5 ? "ECHEC_BLOQUE_15MIN" : "ECHEC_CODE_INVALIDE"
    });
}

async function resetFailedAttempts(idAppareil) {
    if (!idAppareil) return;
    await db.collection("tentatives").doc(idAppareil).delete();
}

function verifyAdmin(context) {
    if (!context.auth) {
        throw new functions.https.HttpsError("unauthenticated", "Connexion requise pour cette action.");
    }
    const token = context.auth.token || {};
    const isAdmin = token.admin === true || token.email === OWNER_EMAIL;
    if (!isAdmin) {
        throw new functions.https.HttpsError("permission-denied", "Action réservée exclusivement au propriétaire administrateur.");
    }
    return true;
}

// ============================================================================
// 1. activerCode(code, idAppareil)
// ============================================================================
exports.activerCode = functions.https.onCall(async (data, context) => {
    const code = (data && data.code ? String(data.code) : "").trim().toUpperCase();
    const idAppareil = (data && data.idAppareil ? String(data.idAppareil) : "").trim();

    if (!code || !idAppareil) {
        throw new functions.https.HttpsError("invalid-argument", "Le code et l'identifiant de l'appareil sont requis.");
    }

    // Contrôle du taux d'échecs (5 tentatives -> blocage 15 min)
    await checkRateLimit(idAppareil);

    const hash = hashCode(code);
    const codePrefix = code.length >= 4 ? code.substring(0, 4) : "???";

    const query = await db.collection("codes").where("code_hash", "==", hash).limit(1).get();

    if (query.empty) {
        await recordFailedAttempt(idAppareil, codePrefix);
        throw new functions.https.HttpsError("invalid-argument", "Code d'accès invalide, expiré ou révoqué.");
    }

    const codeDoc = query.docs[0];
    const codeData = codeDoc.data();
    const now = Date.now();

    // Vérifier statut et expiration
    const isRevoked = codeData.statut === "révoqué";
    const isExplicitlyExpired = codeData.statut === "expiré";
    const isTimeExpired = codeData.expire_le && (codeData.expire_le.toMillis ? codeData.expire_le.toMillis() : codeData.expire_le) < now;

    if (isRevoked || isExplicitlyExpired || isTimeExpired) {
        await recordFailedAttempt(idAppareil, codePrefix);
        throw new functions.https.HttpsError("invalid-argument", "Code d'accès invalide, expiré ou révoqué.");
    }

    // Gestion du nombre d'appareils (max 2)
    const appareils = Array.isArray(codeData.appareils) ? [...codeData.appareils] : [];
    if (!appareils.includes(idAppareil)) {
        if (appareils.length >= 2) {
            await db.collection("logs_activations").add({
                date: admin.firestore.FieldValue.serverTimestamp(),
                idAppareil,
                code_prefix: codePrefix,
                resultat: "ECHEC_MAX_APPAREILS_ATTEINT"
            });
            throw new functions.https.HttpsError(
                "failed-precondition",
                "Ce code est déjà utilisé sur le nombre maximum d'appareils autorisés (2 appareils max)."
            );
        }
        appareils.push(idAppareil);
    }

    // Première activation : fixer expiration à +30 jours
    let expireTimestamp = codeData.expire_le ? (codeData.expire_le.toMillis ? codeData.expire_le.toMillis() : codeData.expire_le) : null;
    let activatedTimestamp = codeData.activé_le;

    const updates = {
        appareils
    };

    if (codeData.statut === "inactif" || !expireTimestamp) {
        const thirtyDaysLater = now + (30 * 24 * 60 * 60 * 1000);
        updates.statut = "actif";
        updates.activé_le = admin.firestore.Timestamp.fromMillis(now);
        updates.expire_le = admin.firestore.Timestamp.fromMillis(thirtyDaysLater);
        expireTimestamp = thirtyDaysLater;
        activatedTimestamp = updates.activé_le;
    }

    await codeDoc.ref.update(updates);
    await resetFailedAttempts(idAppareil);

    // Si l'utilisateur est authentifié avec Firebase Auth (anonyme ou email)
    if (context.auth && context.auth.uid) {
        try {
            await admin.auth().setCustomUserClaims(context.auth.uid, {
                tier: codeData.tier,
                expire_le: expireTimestamp,
                code_id: codeDoc.id
            });
        } catch (e) {
            console.error("Erreur setCustomUserClaims:", e);
        }
    }

    // Log succès
    await db.collection("logs_activations").add({
        date: admin.firestore.FieldValue.serverTimestamp(),
        idAppareil,
        code_prefix: codePrefix,
        tier: codeData.tier,
        resultat: "SUCCES_ACTIVATION"
    });

    return {
        success: true,
        tier: codeData.tier,
        expire_le: expireTimestamp,
        code_id: codeDoc.id,
        message: "Code activé avec succès !"
    };
});

// ============================================================================
// 2. verifierAcces(idAppareil, codeId)
// ============================================================================
exports.verifierAcces = functions.https.onCall(async (data, context) => {
    const idAppareil = (data && data.idAppareil) || null;
    const codeId = (data && data.codeId) || null;

    let targetCodeId = codeId;
    if (!targetCodeId && context.auth && context.auth.token && context.auth.token.code_id) {
        targetCodeId = context.auth.token.code_id;
    }

    if (!targetCodeId) {
        return { tier: "gratuit", expire_le: null, statut: "inactif" };
    }

    const codeDoc = await db.collection("codes").doc(targetCodeId).get();
    if (!codeDoc.exists) {
        if (context.auth && context.auth.uid) {
            await admin.auth().setCustomUserClaims(context.auth.uid, { tier: "gratuit" });
        }
        return { tier: "gratuit", expire_le: null, statut: "introuvable" };
    }

    const docData = codeDoc.data();
    const now = Date.now();
    const expireTimestamp = docData.expire_le ? (docData.expire_le.toMillis ? docData.expire_le.toMillis() : docData.expire_le) : null;

    if (docData.statut === "révoqué" || (expireTimestamp && now > expireTimestamp)) {
        if (docData.statut !== "révoqué") {
            await codeDoc.ref.update({ statut: "expiré" });
        }
        if (context.auth && context.auth.uid) {
            await admin.auth().setCustomUserClaims(context.auth.uid, { tier: "gratuit" });
        }
        return { tier: "gratuit", expire_le: expireTimestamp, statut: docData.statut === "révoqué" ? "révoqué" : "expiré" };
    }

    return {
        tier: docData.tier,
        expire_le: expireTimestamp,
        statut: docData.statut
    };
});

// ============================================================================
// 3. Fonctions protégées pour les modules payants
// ============================================================================
async function assertUserTier(context, requiredTier) {
    let tier = "gratuit";
    if (context.auth && context.auth.token && context.auth.token.tier) {
        tier = context.auth.token.tier;
    }
    const levels = { "gratuit": 0, "prem": 1, "plus": 2 };
    const userLevel = levels[tier] || 0;
    const requiredLevel = levels[requiredTier] || 1;

    if (userLevel < requiredLevel) {
        throw new functions.https.HttpsError(
            "permission-denied",
            `Accès restreint. Cette fonctionnalité requiert l'abonnement ${requiredTier === "plus" ? "Premium+" : "Premium"}.`
        );
    }
}

exports.calculerBacktestServer = functions.https.onCall(async (data, context) => {
    await assertUserTier(context, "plus");
    const draws = data.draws || [];
    const windowSize = data.windowSize || 40;
    return engine.runWalkForwardBacktest(draws, windowSize);
});

exports.calculerCombinaisonsServer = functions.https.onCall(async (data, context) => {
    const size = data.size || 2;
    if (size >= 4) {
        await assertUserTier(context, "plus");
    } else {
        await assertUserTier(context, "prem");
    }
    const draws = data.draws || [];
    const limit = data.limit || 50;
    return engine.computeTopCombinations(draws, size, limit);
});

exports.genererGrillesServer = functions.https.onCall(async (data, context) => {
    await assertUserTier(context, "prem");
    const draws = data.draws || [];
    const count = data.count || 5;
    const strategy = data.strategy || "MOMENTUM";
    return engine.generateGrids(draws, count, strategy);
});

// ============================================================================
// 4. Administration (réservée au compte propriétaire / claim admin)
// ============================================================================
exports.creerCode = functions.https.onCall(async (data, context) => {
    verifyAdmin(context);
    const tier = data.tier === "plus" ? "plus" : "prem";
    const note = (data.note ? String(data.note) : "").trim();

    const clearCode = generateRandomCode(tier);
    const hash = hashCode(clearCode);

    const docRef = await db.collection("codes").add({
        code_hash: hash,
        tier,
        statut: "inactif",
        créé_le: admin.firestore.FieldValue.serverTimestamp(),
        activé_le: null,
        expire_le: null,
        appareils: [],
        note
    });

    return {
        id: docRef.id,
        code: clearCode,
        tier,
        note
    };
});

exports.listerCodes = functions.https.onCall(async (data, context) => {
    verifyAdmin(context);
    const snapshot = await db.collection("codes").orderBy("créé_le", "desc").limit(100).get();
    const codes = [];

    const now = Date.now();
    for (const doc of snapshot.docs) {
        const d = doc.data();
        let expireMs = d.expire_le ? (d.expire_le.toMillis ? d.expire_le.toMillis() : d.expire_le) : null;
        let createdMs = d.créé_le ? (d.créé_le.toMillis ? d.créé_le.toMillis() : d.créé_le) : null;
        let activatedMs = d.activé_le ? (d.activé_le.toMillis ? d.activé_le.toMillis() : d.activé_le) : null;

        let statut = d.statut;
        if (statut === "actif" && expireMs && now > expireMs) {
            statut = "expiré";
        }

        codes.push({
            id: doc.id,
            tier: d.tier,
            statut,
            note: d.note || "",
            appareils: d.appareils || [],
            expire_le: expireMs,
            créé_le: createdMs,
            activé_le: activatedMs
        });
    }

    return { codes };
});

exports.revoquerCode = functions.https.onCall(async (data, context) => {
    verifyAdmin(context);
    const codeId = data.codeId;
    if (!codeId) throw new functions.https.HttpsError("invalid-argument", "codeId requis.");

    await db.collection("codes").doc(codeId).update({
        statut: "révoqué"
    });

    return { success: true, message: "Code révoqué avec succès." };
});

exports.prolongerCode = functions.https.onCall(async (data, context) => {
    verifyAdmin(context);
    const codeId = data.codeId;
    const jours = data.jours || 30;
    if (!codeId) throw new functions.https.HttpsError("invalid-argument", "codeId requis.");

    const doc = await db.collection("codes").doc(codeId).get();
    if (!doc.exists) throw new functions.https.HttpsError("not-found", "Code introuvable.");

    const d = doc.data();
    const now = Date.now();
    let currentExpire = d.expire_le ? (d.expire_le.toMillis ? d.expire_le.toMillis() : d.expire_le) : now;
    if (currentExpire < now) currentExpire = now;

    const newExpire = currentExpire + (jours * 24 * 60 * 60 * 1000);

    await doc.ref.update({
        statut: "actif",
        expire_le: admin.firestore.Timestamp.fromMillis(newExpire)
    });

    return { success: true, new_expire: newExpire, message: `Code prolongé de ${jours} jours.` };
});

exports.reinitialiserAppareils = functions.https.onCall(async (data, context) => {
    verifyAdmin(context);
    const codeId = data.codeId;
    if (!codeId) throw new functions.https.HttpsError("invalid-argument", "codeId requis.");

    await db.collection("codes").doc(codeId).update({
        appareils: []
    });

    return { success: true, message: "Appareils réinitialisés avec succès (0/2)." };
});
