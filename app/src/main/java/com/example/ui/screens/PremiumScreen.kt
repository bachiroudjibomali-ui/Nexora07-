package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.config.AccessControl
import com.example.config.PaymentConfig
import com.example.config.PlanInfo
import com.example.config.UserTier
import com.example.ui.components.DisclaimerCard
import com.example.ui.theme.LotoBackground
import com.example.ui.theme.LotoBlue
import com.example.ui.theme.LotoBorder
import com.example.ui.theme.LotoGold
import com.example.ui.theme.LotoGreen
import com.example.ui.theme.LotoOrange
import com.example.ui.theme.LotoRed
import com.example.ui.theme.LotoSurface
import com.example.ui.theme.LotoSurfaceElevated
import com.example.ui.theme.LotoSurfaceVariant
import com.example.ui.theme.LotoText
import com.example.ui.theme.LotoTextDim
import com.example.ui.theme.LotoTextMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PremiumScreen(
    currentTier: UserTier,
    activeCode: String,
    expirationTimestamp: Long? = null,
    deviceId: String = "",
    isActivating: Boolean = false,
    activationError: String? = null,
    isOfflineGraceActive: Boolean = false,
    remainingOfflineHours: Int = 0,
    onActivateCode: (String) -> Unit,
    onResetCode: () -> Unit,
    onOpenAdmin: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var inputCode by remember { mutableStateOf(activeCode) }
    var copyNotice by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy à HH:mm", Locale.FRENCH) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LotoBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Formules & Abonnement",
                color = LotoOrange,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Débloquez la puissance complète du moteur d'analyse statistique",
                color = LotoTextMuted,
                fontSize = 13.sp
            )
        }

        // Avertissement permanent de transparence
        item {
            DisclaimerCard(compact = true)
        }

        // Mode Hors-ligne / Grâce 72h
        if (isOfflineGraceActive) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(LotoGold.copy(alpha = 0.15f))
                        .border(1.dp, LotoGold, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "MODE HORS-LIGNE TEMPORAIRE",
                            color = LotoGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Votre abonnement reste actif sans connexion pendant encore $remainingOfflineHours h. Connectez-vous à Internet avant l'échéance pour renouveler la vérification sécurisée.",
                            color = LotoText,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // État actuel de l'abonnement
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(LotoSurfaceElevated)
                    .border(1.dp, currentTier.badgeColorHex.let { Color(it) }.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "STATUT DU COMPTE",
                                color = LotoTextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentTier.displayName,
                                    color = Color(currentTier.badgeColorHex),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (activeCode.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "($activeCode)",
                                        color = LotoTextMuted,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        if (currentTier != UserTier.FREE) {
                            TextButton(onClick = onResetCode) {
                                Text("Déconnecter", color = LotoRed, fontSize = 12.sp)
                            }
                        }
                    }

                    if (expirationTimestamp != null && currentTier != UserTier.FREE) {
                        val now = System.currentTimeMillis()
                        val daysRemaining = maxOf(0, ((expirationTimestamp - now) / (24 * 3600 * 1000L)).toInt())
                        val expStr = dateFormat.format(Date(expirationTimestamp))

                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(LotoSurfaceVariant)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "📅 Expire le : $expStr ($daysRemaining jours restants)",
                                color = if (daysRemaining <= 3) LotoRed else LotoGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (deviceId.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "📱 Appareil : $deviceId (2 appareils max par code)",
                            color = LotoTextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Formules tarifaires
        item {
            Text(
                text = "NOS FORMULES D'ABONNEMENT",
                color = LotoTextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            )
        }

        items(AccessControl.PLANS) { plan ->
            PlanCard(
                plan = plan,
                isCurrent = plan.tier == currentTier,
                onChoose = {
                    PaymentConfig.openWhatsAppPayment(context, plan.name, plan.price)
                }
            )
        }

        // Instructions et Coordonnées de paiement
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(LotoSurface)
                    .border(1.dp, LotoBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payment, contentDescription = null, tint = LotoOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MODES DE PAIEMENT ACCEPTÉS (NIGER)",
                            color = LotoOrange,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Liste des opérateurs
                    for (op in PaymentConfig.OPERATORS) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(op.colorHex))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${op.name} : ",
                                color = LotoText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = op.description,
                                color = LotoTextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Encadré Numéro bénéficiaire avec bouton Copier
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(LotoSurfaceVariant)
                            .border(1.dp, LotoBorder, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Numéro de transfert officiel :",
                                    color = LotoTextMuted,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = PaymentConfig.PHONE_NUMBER,
                                    color = LotoGreen,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = PaymentConfig.BENEFICIARY_NAME,
                                    color = LotoText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(PaymentConfig.PHONE_NUMBER))
                                    copyNotice = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copier le numéro",
                                    tint = LotoGreen
                                )
                            }
                        }
                    }

                    if (copyNotice) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Numéro copié dans le presse-papiers !",
                            color = LotoGreen,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4 Étapes
                    Text(
                        text = "Étapes simples pour activer votre compte :",
                        color = LotoText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    for (step in PaymentConfig.STEPS) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(vertical = 3.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(LotoOrange.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = "${step.stepNumber}",
                                    color = LotoOrange,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = step.title,
                                    color = LotoText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = step.description,
                                    color = LotoTextMuted,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bouton WhatsApp direct
                    Button(
                        onClick = {
                            PaymentConfig.openWhatsAppPayment(context, "Souscription", "Abonnement")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("whatsapp_payment_button")
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Envoyer mon reçu sur WhatsApp",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Champ de saisie du code d'accès
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(LotoSurface)
                    .border(1.dp, LotoOrange.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = LotoOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ACTIVER MON CODE D'ACCÈS",
                            color = LotoOrange,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Vérification serveur sécurisée • Validité 30 jours • Limité à 2 appareils maximum.",
                        color = LotoTextMuted,
                        fontSize = 12.sp
                    )

                    if (activationError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(LotoRed.copy(alpha = 0.15f))
                                .border(1.dp, LotoRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "⚠️ $activationError",
                                color = LotoRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = inputCode,
                            onValueChange = { inputCode = it },
                            placeholder = { Text("Ex: PRO-78921", color = LotoTextMuted) },
                            singleLine = true,
                            enabled = !isActivating,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = LotoText,
                                unfocusedTextColor = LotoText,
                                focusedBorderColor = LotoOrange,
                                unfocusedBorderColor = LotoBorder
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("access_code_input")
                        )

                        Button(
                            onClick = { onActivateCode(inputCode) },
                            enabled = !isActivating && inputCode.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LotoOrange,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(54.dp)
                                .testTag("activate_code_button")
                        ) {
                            if (isActivating) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Activer", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Accès à l'espace propriétaire / administration
        if (onOpenAdmin != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(LotoSurfaceVariant)
                        .border(1.dp, LotoBorder, RoundedCornerShape(10.dp))
                        .clickable { onOpenAdmin() }
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = LotoGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Espace Propriétaire (Administration)",
                                    color = LotoText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Créer, prolonger et révoquer les codes d'accès",
                                    color = LotoTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Text("Accéder ›", color = LotoOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PlanCard(
    plan: PlanInfo,
    isCurrent: Boolean,
    onChoose: () -> Unit
) {
    val borderColor = if (plan.recommended) LotoOrange else LotoBorder
    val accentColor = Color(plan.tier.badgeColorHex)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LotoSurface)
            .border(if (plan.recommended) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(16.dp)
            .testTag("plan_card_${plan.tier.name}")
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = plan.name,
                        color = accentColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (plan.recommended) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(LotoOrange)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "POPULAIRE",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (isCurrent) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(LotoGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ACTIF",
                                color = LotoGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = plan.price,
                        color = LotoText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = plan.duration,
                        color = LotoTextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Liste des fonctionnalités incluses
            for (feature in plan.features) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = feature,
                        color = LotoText,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            if (plan.tier != UserTier.FREE && !isCurrent) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onChoose,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                ) {
                    Text(
                        text = "Choisir ${plan.name} (${plan.price})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
