package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.config.UserTier
import com.example.data.Draw
import com.example.data.DrawRepository
import com.example.data.ImportResult
import com.example.data.PreferencesManager
import com.example.data.db.AppDatabase
import com.example.data.db.CustomDrawEntity
import com.example.engine.CombinationStat
import com.example.engine.LotoEngine
import com.example.engine.NumberStats
import com.example.engine.RandomnessTestResult
import com.example.engine.RelationAB
import com.example.engine.StrategyBacktestResult
import com.example.service.AdminCodeItem
import com.example.service.CreatedCodeResult
import com.example.service.FirebaseAccessService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = DrawRepository(db.customDrawDao())
    private val preferencesManager = PreferencesManager(application)
    private val firebaseService = FirebaseAccessService(application)

    // État utilisateur & code d'activation
    val userTier: StateFlow<UserTier> = preferencesManager.userTier
    val activationCode: StateFlow<String> = preferencesManager.activationCode
    val expirationTimestamp: StateFlow<Long?> = preferencesManager.expirationTimestamp
    val deviceId: String = preferencesManager.deviceId

    // État du processus d'activation serveur
    private val _isActivating = MutableStateFlow(false)
    val isActivating: StateFlow<Boolean> = _isActivating.asStateFlow()

    private val _activationError = MutableStateFlow<String?>(null)
    val activationError: StateFlow<String?> = _activationError.asStateFlow()

    // État du mode hors ligne & grâce 72h
    private val _isOfflineGraceActive = MutableStateFlow(!preferencesManager.isOfflineGracePeriodValid() && preferencesManager.userTier.value != UserTier.FREE)
    val isOfflineGraceActive: StateFlow<Boolean> = _isOfflineGraceActive.asStateFlow()

    private val _remainingOfflineHours = MutableStateFlow(preferencesManager.getRemainingOfflineGraceHours())
    val remainingOfflineHours: StateFlow<Int> = _remainingOfflineHours.asStateFlow()

    // Flux des tirages
    val allDraws: StateFlow<List<Draw>> = repository.allDrawsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customDraws: StateFlow<List<CustomDrawEntity>> = repository.customDrawsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // États de calcul
    private val _isCalculating = MutableStateFlow(false)
    val isCalculating: StateFlow<Boolean> = _isCalculating.asStateFlow()

    // Statistiques des 90 numéros
    private val _numberStats = MutableStateFlow<List<NumberStats>>(emptyList())
    val numberStats: StateFlow<List<NumberStats>> = _numberStats.asStateFlow()

    // Relations A -> B
    private val _selectedNumA = MutableStateFlow(46)
    val selectedNumA: StateFlow<Int> = _selectedNumA.asStateFlow()

    private val _relationsAB = MutableStateFlow<List<RelationAB>>(emptyList())
    val relationsAB: StateFlow<List<RelationAB>> = _relationsAB.asStateFlow()

    // Backtest
    private val _backtestResults = MutableStateFlow<List<StrategyBacktestResult>>(emptyList())
    val backtestResults: StateFlow<List<StrategyBacktestResult>> = _backtestResults.asStateFlow()

    // Combinaisons
    private val _selectedComboSize = MutableStateFlow(2)
    val selectedComboSize: StateFlow<Int> = _selectedComboSize.asStateFlow()

    private val _combinations = MutableStateFlow<List<CombinationStat>>(emptyList())
    val combinations: StateFlow<List<CombinationStat>> = _combinations.asStateFlow()

    // Tests d'aléa
    private val _randomnessResult = MutableStateFlow<RandomnessTestResult?>(null)
    val randomnessResult: StateFlow<RandomnessTestResult?> = _randomnessResult.asStateFlow()

    // Message d'opération (succès / erreur)
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Administration (Espace Propriétaire)
    private val _isAdminLoggedIn = MutableStateFlow(firebaseService.isAdminLoggedIn())
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _adminEmail = MutableStateFlow(firebaseService.getAdminEmail())
    val adminEmail: StateFlow<String?> = _adminEmail.asStateFlow()

    private val _adminCodes = MutableStateFlow<List<AdminCodeItem>>(emptyList())
    val adminCodes: StateFlow<List<AdminCodeItem>> = _adminCodes.asStateFlow()

    private val _isLoadingAdmin = MutableStateFlow(false)
    val isLoadingAdmin: StateFlow<Boolean> = _isLoadingAdmin.asStateFlow()

    private val _createdCodeResult = MutableStateFlow<CreatedCodeResult?>(null)
    val createdCodeResult: StateFlow<CreatedCodeResult?> = _createdCodeResult.asStateFlow()

    init {
        // Recalculer les analyses à chaque mise à jour de tirages
        viewModelScope.launch {
            allDraws.collect { draws ->
                if (draws.isNotEmpty()) {
                    recomputeAllAnalytics(draws)
                }
            }
        }

        // Vérification de l'accès serveur au démarrage
        verifierAccesOnStartup()
    }

    /**
     * Vérifie la validité du code auprès du serveur au démarrage de l'application.
     * En cas d'absence de réseau, applique la règle de grâce hors-ligne de 72 heures.
     */
    fun verifierAccesOnStartup() {
        viewModelScope.launch {
            val codeId = preferencesManager.getCodeId()
            if (codeId.isNullOrBlank()) {
                // Pas de code actif : compte gratuit
                return@launch
            }

            val result = firebaseService.verifierAcces(deviceId, codeId)
            if (result.isSuccess) {
                val data = result.getOrNull()!!
                if (data.status == "expiré" || data.status == "révoqué" || data.tier == UserTier.FREE) {
                    preferencesManager.clearAccess()
                    _snackbarMessage.value = "Votre abonnement est arrivé à expiration ou a été révoqué."
                } else {
                    preferencesManager.recordSuccessfulVerification(data.tier, data.expirationMs)
                    _isOfflineGraceActive.value = false
                }
            } else {
                // Hors-ligne : vérifier le délai de grâce de 72h
                if (preferencesManager.isOfflineGracePeriodValid()) {
                    _isOfflineGraceActive.value = true
                    _remainingOfflineHours.value = preferencesManager.getRemainingOfflineGraceHours()
                } else {
                    // Délai de grâce expiré sans contact serveur : rétrogradation au niveau gratuit
                    preferencesManager.clearAccess()
                    _snackbarMessage.value = "Mode hors-ligne expiré (limite de 72h atteinte). Connectez-vous à Internet pour réactiver votre accès."
                }
            }
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun clearActivationError() {
        _activationError.value = null
    }

    /**
     * Active un code d'accès par vérification serveur distante.
     */
    fun saveActivationCode(code: String) {
        val clean = code.trim().uppercase()
        if (clean.isBlank()) {
            _activationError.value = "Veuillez renseigner un code."
            return
        }

        viewModelScope.launch {
            _isActivating.value = true
            _activationError.value = null

            val result = firebaseService.activerCode(clean, deviceId)
            if (result.isSuccess) {
                val act = result.getOrNull()!!
                preferencesManager.saveServerVerifiedAccess(
                    tier = act.tier,
                    code = clean,
                    expirationMs = act.expirationMs,
                    codeId = act.codeId
                )
                _isOfflineGraceActive.value = false
                _snackbarMessage.value = "Code validé par le serveur ! Niveau : ${act.tier.displayName}"
            } else {
                val err = result.exceptionOrNull()?.message ?: "Erreur d'activation."
                _activationError.value = err
                _snackbarMessage.value = err
            }

            _isActivating.value = false
        }
    }

    /**
     * Déconnexion / réinitialisation locale.
     */
    fun clearActivationCode() {
        preferencesManager.clearAccess()
        _isOfflineGraceActive.value = false
        _snackbarMessage.value = "Déconnexion effectuée. Compte Gratuit."
    }

    // =========================================================================
    // Gestion Espace Propriétaire / Administration
    // =========================================================================

    fun loginAdmin(email: String, pass: String) {
        viewModelScope.launch {
            _isLoadingAdmin.value = true
            val result = firebaseService.loginAdmin(email, pass)
            _isLoadingAdmin.value = false

            if (result.isSuccess) {
                _isAdminLoggedIn.value = true
                _adminEmail.value = firebaseService.getAdminEmail()
                _snackbarMessage.value = "Connexion Administrateur réussie."
                loadAdminCodes()
            } else {
                _snackbarMessage.value = result.exceptionOrNull()?.message ?: "Échec de connexion administrateur."
            }
        }
    }

    fun logoutAdmin() {
        firebaseService.logoutAdmin()
        _isAdminLoggedIn.value = false
        _adminEmail.value = null
        _adminCodes.value = emptyList()
        _createdCodeResult.value = null
        _snackbarMessage.value = "Déconnexion de l'espace administration."
    }

    fun loadAdminCodes() {
        viewModelScope.launch {
            _isLoadingAdmin.value = true
            val res = firebaseService.listerCodes()
            _isLoadingAdmin.value = false

            if (res.isSuccess) {
                _adminCodes.value = res.getOrNull() ?: emptyList()
            } else {
                _snackbarMessage.value = res.exceptionOrNull()?.message ?: "Erreur de chargement des codes."
            }
        }
    }

    fun creerCode(tier: String, note: String) {
        viewModelScope.launch {
            _isLoadingAdmin.value = true
            val res = firebaseService.creerCode(tier, note)
            _isLoadingAdmin.value = false

            if (res.isSuccess) {
                val created = res.getOrNull()
                _createdCodeResult.value = created
                _snackbarMessage.value = "Nouveau code généré : ${created?.code}"
                loadAdminCodes()
            } else {
                _snackbarMessage.value = res.exceptionOrNull()?.message ?: "Erreur de génération du code."
            }
        }
    }

    fun dismissCreatedCode() {
        _createdCodeResult.value = null
    }

    fun revoquerCode(codeId: String) {
        viewModelScope.launch {
            _isLoadingAdmin.value = true
            val res = firebaseService.revoquerCode(codeId)
            _isLoadingAdmin.value = false
            if (res.isSuccess) {
                _snackbarMessage.value = "Code révoqué avec succès."
                loadAdminCodes()
            } else {
                _snackbarMessage.value = res.exceptionOrNull()?.message ?: "Erreur lors de la révocation."
            }
        }
    }

    fun prolongerCode(codeId: String, jours: Int = 30) {
        viewModelScope.launch {
            _isLoadingAdmin.value = true
            val res = firebaseService.prolongerCode(codeId, jours)
            _isLoadingAdmin.value = false
            if (res.isSuccess) {
                _snackbarMessage.value = "Code prolongé de $jours jours."
                loadAdminCodes()
            } else {
                _snackbarMessage.value = res.exceptionOrNull()?.message ?: "Erreur lors de la prolongation."
            }
        }
    }

    fun reinitialiserAppareils(codeId: String) {
        viewModelScope.launch {
            _isLoadingAdmin.value = true
            val res = firebaseService.reinitialiserAppareils(codeId)
            _isLoadingAdmin.value = false
            if (res.isSuccess) {
                _snackbarMessage.value = "Appareils réinitialisés (0/2)."
                loadAdminCodes()
            } else {
                _snackbarMessage.value = res.exceptionOrNull()?.message ?: "Erreur de réinitialisation."
            }
        }
    }

    fun refreshAdminCodes() = loadAdminCodes()
    fun createCode(tier: String, note: String) = creerCode(tier, note)
    fun revokeCode(codeId: String) = revoquerCode(codeId)
    fun resetCodeDevices(codeId: String) = reinitialiserAppareils(codeId)

    // =========================================================================
    // Données des tirages et calculs
    // =========================================================================

    fun setSelectedNumA(num: Int) {
        if (num in 1..90) {
            _selectedNumA.value = num
            recomputeRelations(num)
        }
    }

    fun setSelectedComboSize(size: Int) {
        if (size in 2..5) {
            _selectedComboSize.value = size
            recomputeCombinations(size)
        }
    }

    fun addSingleDraw(date: String, numbers: List<Int>) {
        viewModelScope.launch {
            val result = repository.addSingleDraw(date, numbers)
            if (result.isSuccess) {
                _snackbarMessage.value = "Nouveau tirage ajouté avec succès (#${result.getOrNull()}) !"
            } else {
                _snackbarMessage.value = "Erreur : ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun importMassDraws(text: String, onComplete: ((ImportResult) -> Unit)? = null) {
        viewModelScope.launch {
            _isCalculating.value = true
            val res = withContext(Dispatchers.Default) {
                repository.importMassDraws(text)
            }
            _isCalculating.value = false
            if (res.successCount > 0) {
                _snackbarMessage.value = "${res.successCount} tirages importés avec succès !"
            } else if (res.errorCount > 0) {
                _snackbarMessage.value = "Erreur d'import : ${res.errors.firstOrNull()}"
            }
            onComplete?.invoke(res)
        }
    }

    fun deleteCustomDraw(id: Long) {
        viewModelScope.launch {
            repository.deleteCustomDraw(id)
            _snackbarMessage.value = "Tirage supprimé."
        }
    }

    fun clearAllCustomDraws() {
        viewModelScope.launch {
            repository.clearCustomDraws()
            _snackbarMessage.value = "Tous les ajouts ont été réinitialisés."
        }
    }

    fun triggerWalkForwardBacktest(windowSize: Int = 40) {
        val currentDraws = allDraws.value
        if (currentDraws.isEmpty()) return

        viewModelScope.launch {
            _isCalculating.value = true
            val results = withContext(Dispatchers.Default) {
                LotoEngine.runWalkForwardBacktest(currentDraws, windowSize)
            }
            _backtestResults.value = results
            _isCalculating.value = false
        }
    }

    private fun recomputeRelations(numA: Int) {
        val currentDraws = allDraws.value
        if (currentDraws.isEmpty()) return
        viewModelScope.launch {
            val rels = withContext(Dispatchers.Default) {
                LotoEngine.computeRelationsAB(currentDraws, numA)
            }
            _relationsAB.value = rels
        }
    }

    private fun recomputeCombinations(size: Int) {
        val currentDraws = allDraws.value
        if (currentDraws.isEmpty()) return
        viewModelScope.launch {
            _isCalculating.value = true
            val combos = withContext(Dispatchers.Default) {
                LotoEngine.computeTopCombinations(currentDraws, size, limit = 50)
            }
            _combinations.value = combos
            _isCalculating.value = false
        }
    }

    private fun recomputeAllAnalytics(draws: List<Draw>) {
        viewModelScope.launch {
            _isCalculating.value = true
            withContext(Dispatchers.Default) {
                val stats = LotoEngine.computeNumberStats(draws)
                val rels = LotoEngine.computeRelationsAB(draws, _selectedNumA.value)
                val combos = LotoEngine.computeTopCombinations(draws, _selectedComboSize.value, limit = 50)
                val rnd = LotoEngine.computeRandomnessTests(draws)
                val btest = LotoEngine.runWalkForwardBacktest(draws, 40)

                _numberStats.value = stats
                _relationsAB.value = rels
                _combinations.value = combos
                _randomnessResult.value = rnd
                _backtestResults.value = btest
            }
            _isCalculating.value = false
        }
    }
}
