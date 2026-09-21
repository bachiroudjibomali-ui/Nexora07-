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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.config.AccessControl
import com.example.config.UserTier
import com.example.engine.StrategyBacktestResult
import com.example.ui.components.DisclaimerCard
import com.example.ui.components.LockedFeatureCard
import com.example.ui.theme.LotoBackground
import com.example.ui.theme.LotoBlue
import com.example.ui.theme.LotoBorder
import com.example.ui.theme.LotoGold
import com.example.ui.theme.LotoGreen
import com.example.ui.theme.LotoOrange
import com.example.ui.theme.LotoPurple
import com.example.ui.theme.LotoRed
import com.example.ui.theme.LotoSurface
import com.example.ui.theme.LotoSurfaceElevated
import com.example.ui.theme.LotoSurfaceVariant
import com.example.ui.theme.LotoText
import com.example.ui.theme.LotoTextDim
import com.example.ui.theme.LotoTextMuted

@Composable
fun BacktestScreen(
    backtestResults: List<StrategyBacktestResult>,
    isCalculating: Boolean,
    userTier: UserTier,
    onRunBacktest: (Int) -> Unit,
    onNavigateToPremium: () -> Unit
) {
    val canAccess = AccessControl.LIMITS.canAccessBacktest(userTier)
    var selectedWindow by remember { mutableIntStateOf(40) }

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
                text = "Backtest Walk-Forward",
                color = LotoOrange,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Simulation rigoureuse sans fuite de données futures sur fenêtre glissante W",
                color = LotoTextMuted,
                fontSize = 13.sp
            )
        }

        item {
            DisclaimerCard(compact = true)
        }

        if (!canAccess) {
            item {
                LockedFeatureCard(
                    requiredTier = UserTier.PREMIUM_PLUS,
                    featureName = "Moteur de Backtest Walk-Forward complet",
                    description = "Le comparateur de stratégies teste les prédictions sur les tirages récents passés pour évaluer objectivement la pertinence des approches (fréquence, retard, momentum et relations A→B). Réservé aux membres Premium+.",
                    onNavigateToPremium = onNavigateToPremium
                )
            }
        } else {
            // Configuration de la fenêtre d'évaluation
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
                            text = "FENÊTRE D'ENTRAÎNEMENT GLISSANTE (W)",
                            color = LotoTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(30, 40, 50, 60).forEach { window ->
                                val isSelected = selectedWindow == window
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) LotoOrange else LotoSurfaceVariant)
                                        .border(1.dp, if (isSelected) LotoOrange else LotoBorder, RoundedCornerShape(8.dp))
                                        .testTag("window_selector_$window")
                                        .padding(horizontal = 4.dp)
                                ) {
                                    androidx.compose.material3.TextButton(
                                        onClick = {
                                            selectedWindow = window
                                            onRunBacktest(window)
                                        }
                                    ) {
                                        Text(
                                            text = "W=$window",
                                            color = if (isSelected) Color.White else LotoText,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bouton de relance du calcul
            item {
                Button(
                    onClick = { onRunBacktest(selectedWindow) },
                    enabled = !isCalculating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LotoOrange,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("run_backtest_button")
                ) {
                    if (isCalculating) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Calcul de simulation en cours...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Exécuter le Backtest Walk-Forward", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Résultats comparatifs
            if (backtestResults.isNotEmpty()) {
                item {
                    val testedDraws = backtestResults.first().totalDrawsTested
                    Text(
                        text = "RÉSULTATS DE SIMULATION SUR $testedDraws TIRAGES TESTÉS",
                        color = LotoTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp
                    )
                }

                items(backtestResults) { res ->
                    StrategyCard(res = res)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StrategyCard(res: StrategyBacktestResult) {
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
                    text = res.strategyName,
                    color = LotoText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(LotoGreen.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Moy: ${String.format("%.2f", res.avgMatchesPerDraw)} bons",
                        color = LotoGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Taux de réussite
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Taux ≥ 1 bon : ${String.format("%.1f", res.winRateAtLeast1)}%",
                    color = LotoTextMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Taux ≥ 2 bons : ${String.format("%.1f", res.winRateAtLeast2)}%",
                    color = LotoOrange,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Graphique de distribution des correspondances (0, 1, 2, 3, 4, 5)
            Text(
                text = "Distribution des correspondances :",
                color = LotoTextDim,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))

            val maxHits = res.matchDistribution.values.maxOrNull() ?: 1
            for (m in 0..5) {
                val count = res.matchDistribution[m] ?: 0
                val ratio = if (maxHits > 0) count.toFloat() / maxHits else 0f
                val pct = if (res.totalDrawsTested > 0) (count.toDouble() / res.totalDrawsTested) * 100.0 else 0.0

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Text(
                        text = "$m bon(s)",
                        color = if (m >= 2) LotoOrange else LotoTextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.width(60.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(LotoSurfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(ratio)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when (m) {
                                        0 -> Color(0xFF4A555C)
                                        1 -> LotoBlue
                                        2 -> LotoGreen
                                        else -> LotoOrange
                                    }
                                )
                        )
                    }
                    Text(
                        text = "$count (${String.format("%.1f", pct)}%)",
                        color = LotoTextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .width(85.dp)
                            .padding(start = 8.dp)
                    )
                }
            }
        }
    }
}
