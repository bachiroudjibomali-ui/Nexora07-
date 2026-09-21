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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.config.UserTier
import com.example.ui.components.DisclaimerCard
import com.example.ui.theme.LotoBackground
import com.example.ui.theme.LotoBlue
import com.example.ui.theme.LotoBorder
import com.example.ui.theme.LotoGold
import com.example.ui.theme.LotoGreen
import com.example.ui.theme.LotoOrange
import com.example.ui.theme.LotoSurface
import com.example.ui.theme.LotoSurfaceElevated
import com.example.ui.theme.LotoSurfaceVariant
import com.example.ui.theme.LotoText
import com.example.ui.theme.LotoTextDim
import com.example.ui.theme.LotoTextMuted

@Composable
fun PlusScreen(
    userTier: UserTier,
    onNavigateToFrequencies: () -> Unit,
    onNavigateToCombinations: () -> Unit,
    onNavigateToReportsGrids: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToRandomness: () -> Unit,
    onNavigateToAdmin: (() -> Unit)? = null
) {
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
                text = "Outils & Modules Complémentaires",
                color = LotoOrange,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Fonctionnalités avancées de calcul, exports et historique",
                color = LotoTextMuted,
                fontSize = 13.sp
            )
        }

        item {
            DisclaimerCard(compact = true)
        }

        item {
            PlusMenuItemCard(
                title = "Fréquences & Périodes",
                subtitle = "Top numéros chauds/froids et filtrage temporel (30, 50, 100 tirages)",
                badge = if (userTier.level >= UserTier.PREMIUM.level) "Actif" else "Premium",
                badgeColor = if (userTier.level >= UserTier.PREMIUM.level) LotoGreen else LotoOrange,
                icon = Icons.Default.BarChart,
                iconColor = LotoOrange,
                onClick = onNavigateToFrequencies,
                testTag = "menu_frequencies"
            )
        }

        item {
            PlusMenuItemCard(
                title = "Combinaisons Récurrentes",
                subtitle = "Paires, triplets, quartets et quintets les plus fréquents de l'histoire",
                badge = if (userTier.level >= UserTier.PREMIUM_PLUS.level) "Complet" else if (userTier.level >= UserTier.PREMIUM.level) "Paires/Triplets" else "Gratuit limité",
                badgeColor = if (userTier.level >= UserTier.PREMIUM.level) LotoGreen else LotoTextDim,
                icon = Icons.Default.DynamicFeed,
                iconColor = LotoGreen,
                onClick = onNavigateToCombinations,
                testTag = "menu_combinations"
            )
        }

        item {
            PlusMenuItemCard(
                title = "Rapports & Grilles",
                subtitle = "Générateur d'analyses + export PDF, générateur de grilles + export CSV",
                badge = if (userTier.level >= UserTier.PREMIUM_PLUS.level) "Complet" else "Premium+",
                badgeColor = if (userTier.level >= UserTier.PREMIUM_PLUS.level) LotoGreen else Color(0xFF1FA363),
                icon = Icons.Default.PictureAsPdf,
                iconColor = LotoGold,
                onClick = onNavigateToReportsGrids,
                testTag = "menu_reports_grids"
            )
        }

        item {
            PlusMenuItemCard(
                title = "Historique Complet des Tirages",
                subtitle = "Recherche par numéro, filtres de date et tirages personnalisés ajoutés",
                badge = if (userTier.level >= UserTier.PREMIUM.level) "261 tirages" else "10 récents",
                badgeColor = if (userTier.level >= UserTier.PREMIUM.level) LotoGreen else LotoTextDim,
                icon = Icons.Default.History,
                iconColor = LotoBlue,
                onClick = onNavigateToHistory,
                testTag = "menu_history"
            )
        }

        item {
            PlusMenuItemCard(
                title = "Graine & Tests d'Aléa",
                subtitle = "Chi-deux (χ²), test des séries de Wald-Wolfowitz et conformité loi uniforme",
                badge = if (userTier.level >= UserTier.PREMIUM_PLUS.level) "Complet" else "Premium+",
                badgeColor = if (userTier.level >= UserTier.PREMIUM_PLUS.level) LotoGreen else Color(0xFF1FA363),
                icon = Icons.Default.Casino,
                iconColor = Color(0xFF9E6E18),
                onClick = onNavigateToRandomness,
                testTag = "menu_randomness"
            )
        }

        if (onNavigateToAdmin != null) {
            item {
                PlusMenuItemCard(
                    title = "Espace Propriétaire (Administration)",
                    subtitle = "Gestion et émission des codes abonnés sécurisés (Accès réservé)",
                    badge = "Propriétaire",
                    badgeColor = LotoOrange,
                    icon = Icons.Default.DynamicFeed,
                    iconColor = LotoOrange,
                    onClick = onNavigateToAdmin,
                    testTag = "menu_admin"
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PlusMenuItemCard(
    title: String,
    subtitle: String,
    badge: String,
    badgeColor: Color,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LotoSurface)
            .border(1.dp, LotoBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconColor.copy(alpha = 0.15f))
                        .border(1.dp, iconColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            color = LotoText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(badgeColor.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badge,
                                color = badgeColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = subtitle,
                        color = LotoTextMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = LotoTextDim,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
