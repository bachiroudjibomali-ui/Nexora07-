package com.example.config

enum class UserTier(val level: Int, val displayName: String, val badgeColorHex: Long) {
    FREE(0, "Gratuit", 0xFF9BA7AD),
    PREMIUM(1, "Premium", 0xFFE07B39),
    PREMIUM_PLUS(2, "Premium+", 0xFF1FA363)
}

data class PlanInfo(
    val tier: UserTier,
    val name: String,
    val price: String,
    val duration: String,
    val features: List<String>,
    val recommended: Boolean = false
)

object AccessControl {

    val PLANS = listOf(
        PlanInfo(
            tier = UserTier.FREE,
            name = "Gratuit",
            price = "0 FCFA",
            duration = "À vie",
            features = listOf(
                "Dernier tirage & indicateurs clés",
                "10 derniers tirages de l'historique",
                "Ajout de tirages personnels",
                "Aperçu de l'analyse statistique (top numéros)",
                "Relations A → B limitées à 5 résultats",
                "Fréquences globales de base"
            )
        ),
        PlanInfo(
            tier = UserTier.PREMIUM,
            name = "Premium",
            price = "2 000 FCFA",
            duration = "par mois",
            features = listOf(
                "Tous les avantages Gratuit",
                "Analyse complète des 90 numéros avec Wilson 95%",
                "Relations A → B complètes sans limite",
                "Fréquences & retards avec filtrage par période (30, 50, 100 tirages)",
                "Combinaisons en paires et triplets",
                "Historique complet des 261 tirages avec recherche avancée",
                "Génération de grilles de jeu optimisées"
            ),
            recommended = true
        ),
        PlanInfo(
            tier = UserTier.PREMIUM_PLUS,
            name = "Premium+",
            price = "5 000 FCFA",
            duration = "par mois",
            features = listOf(
                "Tous les avantages Premium",
                "Moteur de Backtest Walk-Forward complet (4 stratégies)",
                "Combinaisons en quartets et quintets",
                "Tests d'aléa avancés (Chi-deux & Runs test de Wald-Wolfowitz)",
                "Générateur de rapports d'analyse complets avec export PDF",
                "Export CSV des grilles de tirage",
                "Comparateur de stratégies & simulation de rentabilité",
                "Support VIP direct WhatsApp Roa Vision"
            )
        )
    )

    /**
     * Convertit le niveau retourné et certifié par le serveur Firebase
     * en énumération UserTier.
     */
    fun fromServerTier(tierStr: String?): UserTier {
        if (tierStr.isNullOrBlank()) return UserTier.FREE
        return when (tierStr.trim().lowercase()) {
            "plus", "premium+", "premium_plus" -> UserTier.PREMIUM_PLUS
            "prem", "premium" -> UserTier.PREMIUM
            else -> UserTier.FREE
        }
    }

    /**
     * Règles de limites par fonctionnalité
     */
    object LIMITS {
        const val FREE_HISTORY_COUNT = 10
        const val FREE_RELATIONS_COUNT = 5
        const val FREE_ANALYSIS_COUNT = 10

        fun canAccessAnalysisFull(tier: UserTier): Boolean = tier.level >= UserTier.PREMIUM.level
        fun canAccessRelationsFull(tier: UserTier): Boolean = tier.level >= UserTier.PREMIUM.level
        fun canAccessHistoryFull(tier: UserTier): Boolean = tier.level >= UserTier.PREMIUM.level
        fun canAccessPeriods(tier: UserTier): Boolean = tier.level >= UserTier.PREMIUM.level
        fun canAccessPairsAndTriplets(tier: UserTier): Boolean = tier.level >= UserTier.PREMIUM.level

        fun canAccessBacktest(tier: UserTier): Boolean = tier.level >= UserTier.PREMIUM_PLUS.level
        fun canAccessQuartetsAndQuintets(tier: UserTier): Boolean = tier.level >= UserTier.PREMIUM_PLUS.level
        fun canAccessAdvancedRandomness(tier: UserTier): Boolean = tier.level >= UserTier.PREMIUM_PLUS.level
        fun canExportPdf(tier: UserTier): Boolean = tier.level >= UserTier.PREMIUM_PLUS.level
        fun canExportCsv(tier: UserTier): Boolean = tier.level >= UserTier.PREMIUM_PLUS.level
    }
}
