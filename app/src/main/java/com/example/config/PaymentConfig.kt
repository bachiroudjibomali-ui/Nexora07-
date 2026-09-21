package com.example.config

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Configuration des paiements et coordonnées de Roa Vision numérique.
 * Facilement modifiable pour ajuster les numéros, opérateurs et tarifs.
 */
object PaymentConfig {
    const val PHONE_NUMBER = "+227 89 17 55 74"
    const val PHONE_NUMBER_RAW = "22789175574"
    const val BENEFICIARY_NAME = "Roa Vision numérique — Niamey"
    
    val OPERATORS = listOf(
        PaymentOperator(
            name = "Airtel Money",
            code = "airtel",
            colorHex = 0xFFE50000,
            description = "Transfert direct vers $PHONE_NUMBER"
        ),
        PaymentOperator(
            name = "MyNita",
            code = "mynita",
            colorHex = 0xFF0083CA,
            description = "Envoi via application ou agence MyNita"
        ),
        PaymentOperator(
            name = "Amanata",
            code = "amanata",
            colorHex = 0xFF2E7D32,
            description = "Transfert rapide Amanata Niger"
        ),
        PaymentOperator(
            name = "Wave",
            code = "wave",
            colorHex = 0xFF1DA1F2,
            description = "Paiement instantané Wave"
        )
    )

    val STEPS = listOf(
        PaymentStep(
            stepNumber = 1,
            title = "Choisissez votre formule",
            description = "Premium (2 000 FCFA/mois) ou Premium+ (5 000 FCFA/mois)."
        ),
        PaymentStep(
            stepNumber = 2,
            title = "Effectuez le transfert",
            description = "Envoyez le montant exact via Airtel Money, MyNita, Amanata ou Wave au $PHONE_NUMBER ($BENEFICIARY_NAME)."
        ),
        PaymentStep(
            stepNumber = 3,
            title = "Confirmez sur WhatsApp",
            description = "Envoyez la capture d'écran ou la référence du reçu au service client par WhatsApp."
        ),
        PaymentStep(
            stepNumber = 4,
            title = "Activez votre code d'accès",
            description = "Entrez votre code reçu (format ESS-XXXXX ou PRO-XXXXX) ci-dessous pour débloquer immédiatement vos outils."
        )
    )

    fun openWhatsAppPayment(context: Context, planName: String, amount: String) {
        val message = "Bonjour Roa Vision numérique, je souhaite activer mon abonnement Loto Niger Analytics ($planName - $amount) via transfert au $PHONE_NUMBER. Voici ma référence de paiement :"
        val encodedMessage = Uri.encode(message)
        val url = "https://wa.me/$PHONE_NUMBER_RAW?text=$encodedMessage"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            // Fallback: standard browser
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$PHONE_NUMBER_RAW&text=$encodedMessage")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
        }
    }
}

data class PaymentOperator(
    val name: String,
    val code: String,
    val colorHex: Long,
    val description: String
)

data class PaymentStep(
    val stepNumber: Int,
    val title: String,
    val description: String
)
