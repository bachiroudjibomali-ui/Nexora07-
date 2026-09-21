package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.config.AccessControl
import com.example.config.UserTier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("loto_niger_prefs", Context.MODE_PRIVATE)

    // Identifiant unique d'appareil (persistant)
    val deviceId: String = getOrCreateDeviceId()

    private val _activationCode = MutableStateFlow(getSavedCode())
    val activationCode: StateFlow<String> = _activationCode.asStateFlow()

    private val _userTier = MutableStateFlow(computeCurrentTier())
    val userTier: StateFlow<UserTier> = _userTier.asStateFlow()

    private val _expirationTimestamp = MutableStateFlow(getExpirationTimestamp())
    val expirationTimestamp: StateFlow<Long?> = _expirationTimestamp.asStateFlow()

    private val _codeId = MutableStateFlow(getCodeId())
    val codeId: StateFlow<String?> = _codeId.asStateFlow()

    private fun getOrCreateDeviceId(): String {
        var id = prefs.getString(KEY_DEVICE_ID, null)
        if (id.isNullOrBlank()) {
            id = "DEV-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).uppercase()
            prefs.edit().putString(KEY_DEVICE_ID, id).apply()
        }
        return id
    }

    fun getSavedCode(): String {
        return prefs.getString(KEY_ACTIVATION_CODE, "") ?: ""
    }

    fun getCodeId(): String? {
        return prefs.getString(KEY_CODE_ID, null)
    }

    fun getExpirationTimestamp(): Long? {
        val exp = prefs.getLong(KEY_EXPIRE_TIMESTAMP, -1L)
        return if (exp > 0L) exp else null
    }

    fun getLastVerifiedTimestamp(): Long {
        return prefs.getLong(KEY_LAST_VERIFIED_TIMESTAMP, 0L)
    }

    /**
     * Détermine le niveau actuel en vérifiant la politique hors-ligne (72h max).
     */
    private fun computeCurrentTier(): UserTier {
        val rawTier = prefs.getString(KEY_USER_TIER, UserTier.FREE.name) ?: UserTier.FREE.name
        val tier = try {
            UserTier.valueOf(rawTier)
        } catch (e: Exception) {
            UserTier.FREE
        }

        if (tier == UserTier.FREE) return UserTier.FREE

        // Vérification de l'expiration et du délai de grâce hors-ligne (72h)
        val now = System.currentTimeMillis()
        val exp = getExpirationTimestamp()
        if (exp != null && now > exp) {
            // Le code a expiré
            return UserTier.FREE
        }

        val lastVerified = getLastVerifiedTimestamp()
        if (lastVerified > 0L && (now - lastVerified > OFFLINE_GRACE_PERIOD_MILLIS)) {
            // Plus de 72h sans vérification serveur : retour forcé au niveau gratuit
            return UserTier.FREE
        }

        return tier
    }

    /**
     * Enregistre le résultat certifié par le serveur Firebase.
     */
    fun saveServerVerifiedAccess(
        tier: UserTier,
        code: String,
        expirationMs: Long?,
        codeId: String?
    ) {
        val now = System.currentTimeMillis()
        prefs.edit()
            .putString(KEY_USER_TIER, tier.name)
            .putString(KEY_ACTIVATION_CODE, code.trim().uppercase())
            .putLong(KEY_EXPIRE_TIMESTAMP, expirationMs ?: -1L)
            .putString(KEY_CODE_ID, codeId)
            .putLong(KEY_LAST_VERIFIED_TIMESTAMP, now)
            .apply()

        _activationCode.value = code.trim().uppercase()
        _userTier.value = tier
        _expirationTimestamp.value = expirationMs
        _codeId.value = codeId
    }

    /**
     * Met à jour uniquement l'horodatage de la dernière vérification réussie.
     */
    fun recordSuccessfulVerification(tier: UserTier, expirationMs: Long?) {
        val now = System.currentTimeMillis()
        prefs.edit()
            .putString(KEY_USER_TIER, tier.name)
            .putLong(KEY_EXPIRE_TIMESTAMP, expirationMs ?: -1L)
            .putLong(KEY_LAST_VERIFIED_TIMESTAMP, now)
            .apply()

        _userTier.value = tier
        _expirationTimestamp.value = expirationMs
    }

    /**
     * Réinitialise complètement l'accès au niveau gratuit.
     */
    fun clearAccess() {
        prefs.edit()
            .remove(KEY_ACTIVATION_CODE)
            .remove(KEY_USER_TIER)
            .remove(KEY_EXPIRE_TIMESTAMP)
            .remove(KEY_CODE_ID)
            .remove(KEY_LAST_VERIFIED_TIMESTAMP)
            .apply()

        _activationCode.value = ""
        _userTier.value = UserTier.FREE
        _expirationTimestamp.value = null
        _codeId.value = null
    }

    /**
     * Vérifie la validité du délai de grâce hors ligne (72h).
     */
    fun isOfflineGracePeriodValid(): Boolean {
        val currentTier = _userTier.value
        if (currentTier == UserTier.FREE) return true
        val now = System.currentTimeMillis()
        val exp = getExpirationTimestamp()
        if (exp != null && now > exp) return false

        val lastVerified = getLastVerifiedTimestamp()
        return lastVerified > 0L && (now - lastVerified <= OFFLINE_GRACE_PERIOD_MILLIS)
    }

    fun getRemainingOfflineGraceHours(): Int {
        val lastVerified = getLastVerifiedTimestamp()
        if (lastVerified <= 0L) return 0
        val elapsed = System.currentTimeMillis() - lastVerified
        val remainingMs = OFFLINE_GRACE_PERIOD_MILLIS - elapsed
        return if (remainingMs > 0) (remainingMs / (3600 * 1000L)).toInt() else 0
    }

    companion object {
        private const val KEY_DEVICE_ID = "key_device_id"
        private const val KEY_ACTIVATION_CODE = "key_activation_code"
        private const val KEY_USER_TIER = "key_user_tier"
        private const val KEY_EXPIRE_TIMESTAMP = "key_expire_timestamp"
        private const val KEY_CODE_ID = "key_code_id"
        private const val KEY_LAST_VERIFIED_TIMESTAMP = "key_last_verified_timestamp"

        // 72 heures en millisecondes
        const val OFFLINE_GRACE_PERIOD_MILLIS = 72 * 3600 * 1000L
    }
}
