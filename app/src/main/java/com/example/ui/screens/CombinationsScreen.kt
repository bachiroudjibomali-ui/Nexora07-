package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.engine.CombinationStat
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
import com.example.ui.theme.LotoText
import com.example.ui.theme.LotoTextDim
import com.example.ui.theme.LotoTextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CombinationsScreen(
    selectedComboSize: Int,
    combinations: List<CombinationStat>,
    isCalculating: Boolean,
    userTier: UserTier,
    onSelectComboSize: (Int) -> Unit,
    onBack: () -> Unit,
    onNavigateToPremium: () -> Unit
) {
    val canAccessPairs = AccessControl.LIMITS.canAccessPairsAndTriplets(userTier)
    val canAccessQuartets = AccessControl.LIMITS.canAccessQuartetsAndQuintets(userTier)

    val isCurrentLocked = when (selectedComboSize) {
        2, 3 -> !canAccessPairs
        4, 5 -> !canAccessQuartets
        else -> false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Combinaisons Récurrentes", color = LotoText, fontWeight = FontWeight.Bold) },
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

            // Onglets Paires, Triplets, Quartets, Quintets
            item {
                val sizes = listOf(2, 3, 4, 5)
                val labels = listOf("Paires (2)", "Triplets (3)", "Quartets (4)", "Quintets (5)")
                val currentTabIndex = sizes.indexOf(selectedComboSize).coerceAtLeast(0)

                TabRow(
                    selectedTabIndex = currentTabIndex,
                    containerColor = LotoSurface,
                    contentColor = LotoOrange,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[currentTabIndex]),
                            color = LotoOrange
                        )
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, LotoBorder, RoundedCornerShape(10.dp))
                ) {
                    labels.forEachIndexed { index, label ->
                        val size = sizes[index]
                        Tab(
                            selected = selectedComboSize == size,
                            onClick = { onSelectComboSize(size) },
                            text = {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedComboSize == size) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedComboSize == size) LotoOrange else LotoTextMuted
                                )
                            }
                        )
                    }
                }
            }

            if (isCurrentLocked) {
                item {
                    val requiredTier = if (selectedComboSize in 2..3) UserTier.PREMIUM else UserTier.PREMIUM_PLUS
                    LockedFeatureCard(
                        requiredTier = requiredTier,
                        featureName = "Combinaisons (${labelsForSize(selectedComboSize)})",
                        description = "Découvrez les groupes de numéros qui sont sortis ensemble le plus souvent dans l'histoire des tirages. Accessible avec la formule ${requiredTier.displayName}.",
                        onNavigateToPremium = onNavigateToPremium
                    )
                }
            } else if (isCalculating) {
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp)
                    ) {
                        CircularProgressIndicator(color = LotoOrange)
                    }
                }
            } else if (combinations.isEmpty()) {
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(LotoSurface)
                            .padding(32.dp)
                    ) {
                        Text(
                            text = "Aucune combinaison récurrente de taille $selectedComboSize trouvée.",
                            color = LotoTextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                item {
                    Text(
                        text = "TOP ${combinations.size} COMBINAISONS (${labelsForSize(selectedComboSize)})",
                        color = LotoTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp
                    )
                }

                items(combinations) { combo ->
                    ComboItemCard(combo = combo)
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

private fun labelsForSize(size: Int): String = when (size) {
    2 -> "Paires"
    3 -> "Triplets"
    4 -> "Quartets"
    5 -> "Quintets"
    else -> "$size numéros"
}

@Composable
private fun ComboItemCard(combo: CombinationStat) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LotoSurface)
            .border(1.dp, LotoBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (num in combo.numbers) {
                    LotoBall(
                        number = num,
                        size = 36.dp,
                        highlight = BallHighlight.ORANGE
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${combo.count} sorties",
                    color = LotoGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                if (combo.lastAppearanceDate != null) {
                    Text(
                        text = "Dernier: ${combo.lastAppearanceDate}",
                        color = LotoTextDim,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
