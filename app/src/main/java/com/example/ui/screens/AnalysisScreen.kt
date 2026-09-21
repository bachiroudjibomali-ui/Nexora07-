package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.engine.NumberStats
import com.example.ui.components.BallHighlight
import com.example.ui.components.DisclaimerCard
import com.example.ui.components.LockedFeatureCard
import com.example.ui.components.LotoBall
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

enum class SortField {
    NUMBER,
    SCORE,
    FREQUENCY,
    CURRENT_GAP,
    MAX_GAP
}

@Composable
fun AnalysisScreen(
    numberStats: List<NumberStats>,
    userTier: UserTier,
    onNavigateToPremium: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var sortField by remember { mutableStateOf(SortField.SCORE) }
    var sortAscending by remember { mutableStateOf(false) }

    val canAccessFull = AccessControl.LIMITS.canAccessAnalysisFull(userTier)

    // Filtrage et tri
    val filteredStats = numberStats.filter {
        if (searchQuery.isBlank()) true
        else it.number.toString().contains(searchQuery.trim())
    }

    val sortedStats = when (sortField) {
        SortField.NUMBER -> if (sortAscending) filteredStats.sortedBy { it.number } else filteredStats.sortedByDescending { it.number }
        SortField.SCORE -> if (sortAscending) filteredStats.sortedBy { it.score } else filteredStats.sortedByDescending { it.score }
        SortField.FREQUENCY -> if (sortAscending) filteredStats.sortedBy { it.frequency } else filteredStats.sortedByDescending { it.frequency }
        SortField.CURRENT_GAP -> if (sortAscending) filteredStats.sortedBy { it.currentGap } else filteredStats.sortedByDescending { it.currentGap }
        SortField.MAX_GAP -> if (sortAscending) filteredStats.sortedBy { it.maxGap } else filteredStats.sortedByDescending { it.maxGap }
    }

    // Restriction si compte gratuit
    val displayStats = if (canAccessFull) sortedStats else sortedStats.take(AccessControl.LIMITS.FREE_ANALYSIS_COUNT)

    val top5Scores = numberStats.sortedByDescending { it.score }.take(5)

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
                text = "Analyse du Prochain Tirage",
                color = LotoOrange,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Indices statistiques et intervalles de confiance de Wilson (95%)",
                color = LotoTextMuted,
                fontSize = 13.sp
            )
        }

        // Avertissement permanent
        item {
            DisclaimerCard(compact = true)
        }

        // Top 5 indices statistiques
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(LotoSurfaceElevated)
                    .border(1.dp, LotoOrange.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "TOP 5 - INDICES STATISTIQUES",
                            color = LotoOrange,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                        Text(
                            text = "Score calculé",
                            color = LotoTextMuted,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (stat in top5Scores) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                LotoBall(
                                    number = stat.number,
                                    size = 44.dp,
                                    highlight = BallHighlight.ORANGE
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = String.format("%.1f", stat.score),
                                    color = LotoGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Retard: ${stat.currentGap}",
                                    color = LotoTextDim,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Barre de recherche
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = LotoTextMuted)
                },
                placeholder = { Text("Filtrer par numéro (ex: 46)...", color = LotoTextMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = LotoText,
                    unfocusedTextColor = LotoText,
                    focusedBorderColor = LotoOrange,
                    unfocusedBorderColor = LotoBorder
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("analysis_search_input")
            )
        }

        // Tableau défilable horizontalement
        item {
            val horizontalScroll = rememberScrollState()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(LotoSurface)
                    .border(1.dp, LotoBorder, RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier.horizontalScroll(horizontalScroll)
                ) {
                    // En-tête du tableau
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(LotoSurfaceVariant)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        TableHeaderCell("Numéro", 80.dp) {
                            if (sortField == SortField.NUMBER) sortAscending = !sortAscending
                            else { sortField = SortField.NUMBER; sortAscending = true }
                        }
                        TableHeaderCell("Indice Score", 100.dp) {
                            if (sortField == SortField.SCORE) sortAscending = !sortAscending
                            else { sortField = SortField.SCORE; sortAscending = false }
                        }
                        TableHeaderCell("Sorties", 90.dp) {
                            if (sortField == SortField.FREQUENCY) sortAscending = !sortAscending
                            else { sortField = SortField.FREQUENCY; sortAscending = false }
                        }
                        TableHeaderCell("Taux Réel", 90.dp)
                        TableHeaderCell("Retard Actuel", 110.dp) {
                            if (sortField == SortField.CURRENT_GAP) sortAscending = !sortAscending
                            else { sortField = SortField.CURRENT_GAP; sortAscending = false }
                        }
                        TableHeaderCell("Écart Max", 90.dp) {
                            if (sortField == SortField.MAX_GAP) sortAscending = !sortAscending
                            else { sortField = SortField.MAX_GAP; sortAscending = false }
                        }
                        TableHeaderCell("Wilson 95%", 130.dp)
                    }

                    // Lignes du tableau
                    for (stat in displayStats) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .border(0.5.dp, LotoBorder)
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Box(modifier = Modifier.width(80.dp)) {
                                LotoBall(
                                    number = stat.number,
                                    size = 32.dp,
                                    highlight = if (stat.score >= 60.0) BallHighlight.ORANGE else BallHighlight.DEFAULT
                                )
                            }
                            Text(
                                text = String.format("%.1f", stat.score),
                                color = if (stat.score >= 60) LotoGold else LotoText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.width(100.dp)
                            )
                            Text(
                                text = "${stat.frequency}",
                                color = LotoText,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.width(90.dp)
                            )
                            Text(
                                text = "${String.format("%.1f", stat.rate * 100)}%",
                                color = LotoGreen,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.width(90.dp)
                            )
                            Text(
                                text = "${stat.currentGap} tirages",
                                color = if (stat.currentGap > stat.meanGap * 1.5) LotoOrange else LotoTextMuted,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.width(110.dp)
                            )
                            Text(
                                text = "${stat.maxGap}",
                                color = LotoTextMuted,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.width(90.dp)
                            )
                            Text(
                                text = "[${String.format("%.1f", stat.wilsonLow * 100)}% - ${String.format("%.1f", stat.wilsonHigh * 100)}%]",
                                color = LotoBlue,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.width(130.dp)
                            )
                        }
                    }
                }
            }
        }

        // Si gratuit, afficher la bannière verrouillée pour voir l'ensemble des 90 numéros
        if (!canAccessFull) {
            item {
                LockedFeatureCard(
                    requiredTier = UserTier.PREMIUM,
                    featureName = "Tableau d'analyse complet (90 numéros)",
                    description = "La version gratuite affiche les 10 premiers numéros. Passez à la formule Premium pour explorer l'ensemble des 90 numéros avec tris et filtres avancés.",
                    onNavigateToPremium = onNavigateToPremium
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TableHeaderCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = LotoTextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 4.dp)
        )
    }
}
