package com.example.service

import android.content.Context
import android.util.Log
import com.example.config.AccessControl
import com.example.config.UserTier
import com.example.data.Draw
import com.example.engine.CombinationStat
import com.example.engine.LotoEngine
import com.example.engine.StrategyBacktestResult
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import kotlinx.coroutines.tasks.await
import java.util.Date

data class ActivationResult(
    val success: Boolean,
    val tier: UserTier,
    val expirationMs: Long?,
    val codeId: String?,
    val message: String
)

data class VerificationResult(
    val tier: UserTier,
    val expirationMs: Long?,
    val status: String,
    val isOnline: Boolean
)

data class AdminCodeItem(
    val id: String,
    val tier: String,
    val status: String,
    val note: String,
    val appareilsCount: Int,
    val appareils: List<String>,
    val expireAt: Long?,
    val createdAt: Long?,
    val activatedAt: Long?
)

data class CreatedCodeResult(
    val id: String,
    val code: String,
    val tier: String,
    val note: String
)

class FirebaseAccessService(private val context: Context) {

    private val tag = "FirebaseAccessService"

    /**
     * Vérifie si Firebase est initialisé dans le runtime.
     */
    fun isFirebaseInitialized(): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    private fun getAuth(): FirebaseAuth? {
        return if (isFirebaseInitialized()) {
            try {
                FirebaseAuth.getInstance()
            } catch (e: Exception) {
                null
            }
        } else null
    }

    private fun getFunctions(): FirebaseFunctions? {
        return if (isFirebaseInitialized()) {
            try {
                FirebaseFunctions.getInstance()
            } catch (e: Exception) {
                null
            }
        } else null
    }

    /**
     * Authentification anonyme transparente pour les clients
     */
    suspend fun ensureAnonymousAuth(): Boolean {
        val auth = getAuth() ?: return false
        return try {
            if (auth.currentUser == null) {
                auth.signInAnonymously().await()
            }
            true
        } catch (e: Exception) {
            Log.w(tag, "Échec authentification anonyme Firebase : ${e.message}")
            false
        }
    }

    /**
     * Active un code d'accès auprès du serveur Firebase Cloud Functions.
     */
    suspend fun activerCode(code: String, deviceId: String): Result<ActivationResult> {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.isBlank()) {
            return Result.failure(IllegalArgumentException("Veuillez saisir un code d'accès."))
        }

        val functions = getFunctions()
        if (functions == null) {
            // Si Firebase n'est pas encore configuré (ex: absence de google-services.json),
            // on informe l'utilisateur de configurer le backend.
            return Result.failure(
                IllegalStateException("Le service Firebase n'est pas initialisé. Vérifiez votre configuration google-services.json.")
            )
        }

        return try {
            ensureAnonymousAuth()

            val data = hashMapOf(
                "code" to cleanCode,
                "idAppareil" to deviceId
            )

            val task = functions.getHttpsCallable("activerCode").call(data).await()
            val resMap = task.data as? Map<*, *>

            val success = (resMap?.get("success") as? Boolean) ?: true
            val rawTier = resMap?.get("tier") as? String
            val tier = AccessControl.fromServerTier(rawTier)
            val expireMs = (resMap?.get("expire_le") as? Number)?.toLong()
            val codeId = resMap?.get("code_id") as? String
            val msg = (resMap?.get("message") as? String) ?: "Code activé avec succès !"

            Result.success(
                ActivationResult(
                    success = success,
                    tier = tier,
                    expirationMs = expireMs,
                    codeId = codeId,
                    message = msg
                )
            )
        } catch (e: FirebaseFunctionsException) {
            val message = when (e.code) {
                FirebaseFunctionsException.Code.RESOURCE_EXHAUSTED ->
                    e.message ?: "Trop de tentatives infructueuses. Cet appareil est temporairement bloqué pendant 15 minutes."
                FirebaseFunctionsException.Code.FAILED_PRECONDITION ->
                    e.message ?: "Ce code est déjà utilisé sur le nombre maximum d'appareils autorisés (2 appareils max)."
                FirebaseFunctionsException.Code.INVALID_ARGUMENT ->
                    "Code d'accès invalide, expiré ou révoqué."
                else -> e.message ?: "Erreur lors de l'activation du code."
            }
            Result.failure(Exception(message))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Impossible de joindre le serveur. Vérifiez votre connexion internet."))
        }
    }

    /**
     * Vérifie le statut du code auprès du serveur Firebase.
     */
    suspend fun verifierAcces(deviceId: String, codeId: String?): Result<VerificationResult> {
        val functions = getFunctions() ?: return Result.failure(Exception("Firebase non initialisé"))

        return try {
            ensureAnonymousAuth()

            val data = hashMapOf(
                "idAppareil" to deviceId,
                "codeId" to (codeId ?: "")
            )

            val task = functions.getHttpsCallable("verifierAcces").call(data).await()
            val resMap = task.data as? Map<*, *>

            val rawTier = resMap?.get("tier") as? String
            val tier = AccessControl.fromServerTier(rawTier)
            val expireMs = (resMap?.get("expire_le") as? Number)?.toLong()
            val status = (resMap?.get("statut") as? String) ?: "inactif"

            Result.success(
                VerificationResult(
                    tier = tier,
                    expirationMs = expireMs,
                    status = status,
                    isOnline = true
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Authentification administrateur par email / mot de passe.
     */
    suspend fun loginAdmin(email: String, password: String): Result<Unit> {
        val auth = getAuth() ?: return Result.failure(Exception("Firebase Auth non disponible."))
        return try {
            auth.signInWithEmailAndPassword(email.trim(), password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Identifiants administrateur incorrects."))
        }
    }

    fun logoutAdmin() {
        try {
            getAuth()?.signOut()
        } catch (e: Exception) {
            Log.w(tag, "Erreur déconnexion admin : ${e.message}")
        }
    }

    fun isAdminLoggedIn(): Boolean {
        val auth = getAuth() ?: return false
        val user = auth.currentUser ?: return false
        return !user.isAnonymous
    }

    fun getAdminEmail(): String? {
        val user = getAuth()?.currentUser ?: return null
        return if (!user.isAnonymous) user.email else null
    }

    // =========================================================================
    // Actions Administrateur Propriétaire
    // =========================================================================

    suspend fun creerCode(tier: String, note: String): Result<CreatedCodeResult> {
        val functions = getFunctions() ?: return Result.failure(Exception("Firebase non disponible."))
        return try {
            val data = hashMapOf("tier" to tier, "note" to note)
            val task = functions.getHttpsCallable("creerCode").call(data).await()
            val res = task.data as? Map<*, *>

            val id = (res?.get("id") as? String) ?: ""
            val code = (res?.get("code") as? String) ?: ""
            val resTier = (res?.get("tier") as? String) ?: tier
            val resNote = (res?.get("note") as? String) ?: note

            Result.success(CreatedCodeResult(id, code, resTier, resNote))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listerCodes(): Result<List<AdminCodeItem>> {
        val functions = getFunctions() ?: return Result.failure(Exception("Firebase non disponible."))
        return try {
            val task = functions.getHttpsCallable("listerCodes").call().await()
            val res = task.data as? Map<*, *>
            val list = res?.get("codes") as? List<*> ?: emptyList<Any>()

            val codeItems = list.mapNotNull { item ->
                val map = item as? Map<*, *> ?: return@mapNotNull null
                val id = (map["id"] as? String) ?: return@mapNotNull null
                val tier = (map["tier"] as? String) ?: "prem"
                val statut = (map["statut"] as? String) ?: "inactif"
                val note = (map["note"] as? String) ?: ""
                val appareils = (map["appareils"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                val expireAt = (map["expire_le"] as? Number)?.toLong()
                val createdAt = (map["créé_le"] as? Number)?.toLong()
                val activatedAt = (map["activé_le"] as? Number)?.toLong()

                AdminCodeItem(
                    id = id,
                    tier = tier,
                    status = statut,
                    note = note,
                    appareilsCount = appareils.size,
                    appareils = appareils,
                    expireAt = expireAt,
                    createdAt = createdAt,
                    activatedAt = activatedAt
                )
            }
            Result.success(codeItems)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun revoquerCode(codeId: String): Result<Unit> {
        val functions = getFunctions() ?: return Result.failure(Exception("Firebase non disponible."))
        return try {
            functions.getHttpsCallable("revoquerCode").call(hashMapOf("codeId" to codeId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun prolongerCode(codeId: String, jours: Int = 30): Result<Unit> {
        val functions = getFunctions() ?: return Result.failure(Exception("Firebase non disponible."))
        return try {
            functions.getHttpsCallable("prolongerCode").call(hashMapOf("codeId" to codeId, "jours" to jours)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reinitialiserAppareils(codeId: String): Result<Unit> {
        val functions = getFunctions() ?: return Result.failure(Exception("Firebase non disponible."))
        return try {
            functions.getHttpsCallable("reinitialiserAppareils").call(hashMapOf("codeId" to codeId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
