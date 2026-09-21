package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.config.AccessControl
import com.example.config.UserTier
import com.example.engine.RandomnessTestResult
import com.example.ui.components.DisclaimerCard
import com.example.ui.components.LockedFeatureCard
import com.example.ui.theme.LotoBackground
import com.example.ui.theme.LotoBlue
import com.example.ui.theme.LotoBorder
import com.example.ui.theme.LotoGreen
import com.example.ui.theme.LotoOrange
import com.example.ui.theme.LotoRed
import com.example.ui.theme.LotoSurface
import com.example.ui.theme.LotoSurfaceElevated
import com.example.ui.theme.LotoSurfaceVariant
import com.example.ui.theme.LotoText
import com.example.ui.theme.LotoTextDim
import com.example.ui.theme.LotoTextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomnessScreen(
    result: RandomnessTestResult?,
    userTier: UserTier,
    onBack: () -> Unit,
    onNavigateToPremium: () -> Unit
) {
    val canAccess = AccessControl.LIMITS.canAccessAdvancedRandomness(userTier)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tests d'Aléa & Graine", color = LotoText, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = LotoText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LotoSurface)
            )
        },
        containerColor = LotoBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                DisclaimerCard(compact = true)
            }

            if (!canAccess) {
                item {
                    LockedFeatureCard(
                        requiredTier = UserTier.PREMIUM_PLUS,
                        featureName = "Tests avancés d'aléa, Chi-deux & Wald-Wolfowitz",
                        description = "Audit mathématique de l'indépendance des tirages et de l'adéquation à la loi uniforme (évaluation du générateur pseudo-aléatoire et graine). Réservé aux membres Premium+.",
                        onNavigateToPremium = onNavigateToPremium
                    )
                }
            } else if (result != null) {
                // Test du Chi-deux
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(LotoSurface)
                            .border(1.dp, LotoBorder, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "TEST D'ADÉQUATION DU CHI-DEUX (χ²)",
                                    color = LotoOrange,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                val isConform = result.isUniformConform
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isConform) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (isConform) LotoGreen else LotoRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isConform) "Conforme" else "Non conforme",
                                        color = if (isConform) LotoGreen else LotoRed,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Valeur calculée χ² : ${String.format("%.2f", result.chiSquare)} (Seuil critique à 95% : ${result.criticalValue95}, df = ${result.degreesOfFreedom})",
                                color = LotoText,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Espérance théorique par numéro : ${String.format("%.1f", result.expectedPerNumber)} sorties sur ${result.totalNumbersDrawn} numéros tirés au total.",
                                color = LotoTextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Test des Séries de Wald-Wolfowitz (Runs Test)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(LotoSurface)
                            .border(1.dp, LotoBorder, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "TEST DES SÉRIES (WALD-WOLFOWITZ)",
                                    color = LotoBlue,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                val isConform = result.runsTestConform
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isConform) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (isConform) LotoGreen else LotoOrange,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isConform) "Aléatoire" else "Non-aléatoire",
                                        color = if (isConform) LotoGreen else LotoOrange,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Score Z : ${String.format("%.2f", result.runsZScore)} (|Z| ≤ 1.96 pour conservation de l'hypothèse nulle d'indépendance)",
                                color = LotoText,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Analyse sur la séquence temporelle de parité (dominance paire vs impaire).",
                                color = LotoTextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Répartition Pairs / Impairs
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(LotoSurface)
                            .border(1.dp, LotoBorder, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = "ÉQUILIBRE DE LA PARITÉ GLOBALE",
                                color = LotoTextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Pairs : ${String.format("%.1f", result.evenPercentage)}%",
                                    color = LotoGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Impairs : ${String.format("%.1f", result.oddPercentage)}%",
                                    color = LotoOrange,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // Répartition par dizaines (1-10, ..., 81-90)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(LotoSurface)
                            .border(1.dp, LotoBorder, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = "RÉPARTITION PAR DIZAINES (1-90)",
                                color = LotoTextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            val maxDecile = result.decileDistribution.values.maxOrNull() ?: 1
                            for ((range, count) in result.decileDistribution) {
                                val ratio = if (maxDecile > 0) count.toFloat() / maxDecile else 0f
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                ) {
                                    Text(
                                        text = range,
                                        color = LotoTextMuted,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.width(60.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(12.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(LotoSurfaceVariant)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(ratio)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(LotoOrange)
                                        )
                                    }
                                    Text(
                                        text = "$count",
                                        color = LotoText,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier
                                            .width(45.dp)
                                            .padding(start = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
