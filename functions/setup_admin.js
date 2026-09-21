/**
 * Script utilitaire pour attribuer le rôle administrateur au propriétaire.
 * Utilisation : node setup_admin.js bachiroudjibomali@gmail.com
 */

const admin = require("firebase-admin");
admin.initializeApp();

const email = process.argv[2] || "bachiroudjibomali@gmail.com";

async function makeAdmin(targetEmail) {
    try {
        const user = await admin.auth().getUserByEmail(targetEmail);
        await admin.auth().setCustomUserClaims(user.uid, {
            admin: true,
            tier: "plus"
        });
        console.log(`✅ Succès ! L'utilisateur ${targetEmail} (UID: ${user.uid}) est maintenant Administrateur Propriétaire.`);
    } catch (error) {
        console.error("❌ Erreur lors de l'attribution des droits administrateur :", error.message);
        process.exit(1);
    }
}

makeAdmin(email);
