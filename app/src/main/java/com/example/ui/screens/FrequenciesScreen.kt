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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SevereCold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.data.Draw
import com.example.engine.LotoEngine
import com.example.ui.components.BallHighlight
import com.example.ui.components.DisclaimerCard
import com.example.ui.components.LockedFeatureCard
import com.example.ui.components.LotoBall
import com.example.ui.theme.LotoBackground
import com.example.ui.theme.LotoBlue
import com.example.ui.theme.LotoBorder
import com.example.ui.theme.LotoGreen
import com.example.ui.theme.LotoOrange
import com.example.ui.theme.LotoSurface
import com.example.ui.theme.LotoSurfaceElevated
import com.example.ui.theme.LotoSurfaceVariant
import com.example.ui.theme.LotoText
import com.example.ui.theme.LotoTextDim
import com.example.ui.theme.LotoTextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrequenciesScreen(
    draws: List<Draw>,
    userTier: UserTier,
    onBack: () -> Unit,
    onNavigateToPremium: () -> Unit
) {
    var selectedPeriodIndex by remember { mutableIntStateOf(0) } // 0: Tous, 1: 100, 2: 50, 3: 30
    val canAccessPeriods = AccessControl.LIMITS.canAccessPeriods(userTier)

    val effectivePeriod = when (selectedPeriodIndex) {
        1 -> if (canAccessPeriods) 100 else draws.size
        2 -> if (canAccessPeriods) 50 else draws.size
        3 -> if (canAccessPeriods) 30 else draws.size
        else -> draws.size
    }

    val slicedDraws = draws.takeLast(effectivePeriod)
    val periodStats = remember(slicedDraws) {
        LotoEngine.computeNumberStats(slicedDraws)
    }

    val hot10 = periodStats.sortedByDescending { it.frequency }.take(10)
    val cold10 = periodStats.sortedBy { it.frequency }.take(10)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fréquences & Périodes", color = LotoText, fontWeight = FontWeight.Bold) },
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

            // Sélecteur de période
            item {
                Column {
                    Text(
                        text = "PÉRIODE D'ANALYSE :",
                        color = LotoTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val periods = listOf("Tous (${draws.size})", "100 tirages", "50 tirages", "30 tirages")
                        periods.forEachIndexed { index, label ->
                            val isSelected = selectedPeriodIndex == index
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) LotoOrange else LotoSurface)
                                    .border(1.dp, if (isSelected) LotoOrange else LotoBorder, RoundedCornerShape(8.dp))
                                    .testTag("period_tab_$index")
                            ) {
                                androidx.compose.material3.TextButton(
                                    onClick = { selectedPeriodIndex = index }
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) Color.White else LotoText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (selectedPeriodIndex > 0 && !canAccessPeriods) {
                item {
                    LockedFeatureCard(
                        requiredTier = UserTier.PREMIUM,
                        featureName = "Filtrage temporel par période (30, 50, 100 tirages)",
                        description = "L'analyse des fréquences sur périodes restreintes permet de détecter le dynamisme récent et les variations de momentum. Réservé aux abonnés Premium.",
                        onNavigateToPremium = onNavigateToPremium
                    )
                }
            } else {
                // Top 10 numéros chauds
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = LotoOrange)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "TOP 10 NUMÉROS CHAUDS (LES PLUS FRÉQUENTS)",
                                    color = LotoOrange,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                hot10.take(5).forEach { stat ->
                                    NumberRankItem(stat = stat, isHot = true)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                hot10.drop(5).take(5).forEach { stat ->
                                    NumberRankItem(stat = stat, isHot = true)
                                }
                            }
                        }
                    }
                }

                // Top 10 numéros froids
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.SevereCold, contentDescription = null, tint = LotoBlue)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "TOP 10 NUMÉROS FROIDS (LES PLUS RARES)",
                                    color = LotoBlue,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                cold10.take(5).forEach { stat ->
                                    NumberRankItem(stat = stat, isHot = false)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                cold10.drop(5).take(5).forEach { stat ->
                                    NumberRankItem(stat = stat, isHot = false)
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

@Composable
private fun NumberRankItem(stat: com.example.engine.NumberStats, isHot: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        LotoBall(
            number = stat.number,
            size = 38.dp,
            highlight = if (isHot) BallHighlight.ORANGE else BallHighlight.BLUE
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${stat.frequency} fois",
            color = LotoText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "${String.format("%.1f", stat.rate * 100)}%",
            color = LotoTextDim,
            fontSize = 10.sp
        )
    }
}
