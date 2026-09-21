package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import com.example.engine.RelationAB
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
import com.example.ui.theme.LotoRed
import com.example.ui.theme.LotoSurface
import com.example.ui.theme.LotoSurfaceElevated
import com.example.ui.theme.LotoSurfaceVariant
import com.example.ui.theme.LotoText
import com.example.ui.theme.LotoTextDim
import com.example.ui.theme.LotoTextMuted

@Composable
fun RelationsScreen(
    selectedNumA: Int,
    relations: List<RelationAB>,
    userTier: UserTier,
    onSelectNumA: (Int) -> Unit,
    onNavigateToPremium: () -> Unit
) {
    val canAccessFull = AccessControl.LIMITS.canAccessRelationsFull(userTier)
    val pageSize = 10
    var currentPage by remember { mutableStateOf(0) }

    val activeList = if (canAccessFull) relations else relations.take(AccessControl.LIMITS.FREE_RELATIONS_COUNT)
    val totalPages = if (activeList.isEmpty()) 1 else (activeList.size + pageSize - 1) / pageSize
    val pagedRelations = activeList.drop(currentPage * pageSize).take(pageSize)

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
                text = "Relations A → B",
                color = LotoOrange,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Probabilités de sortie au tirage suivant (t+1) après la sortie du numéro A",
                color = LotoTextMuted,
                fontSize = 13.sp
            )
        }

        item {
            DisclaimerCard(compact = true)
        }

        // Sélecteur du Numéro A (Barre défilable horizontalement 1..90)
        item {
            Column {
                Text(
                    text = "CHOISIR LE NUMÉRO A :",
                    color = LotoTextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(90) { index ->
                        val num = index + 1
                        val isSelected = num == selectedNumA
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) LotoOrange else LotoSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) LotoOrange else LotoBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    onSelectNumA(num)
                                    currentPage = 0
                                }
                        ) {
                            Text(
                                text = String.format("%02d", num),
                                color = if (isSelected) Color.White else LotoText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Carte récapitulative du numéro A choisi
        item {
            val totalTransitions = relations.firstOrNull()?.totalTransitions ?: 0
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(LotoSurfaceElevated)
                    .border(1.dp, LotoOrange.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LotoBall(
                            number = selectedNumA,
                            size = 50.dp,
                            highlight = BallHighlight.ORANGE
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Numéro A actif : N°$selectedNumA",
                                color = LotoText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$totalTransitions transitions observées (t → t+1)",
                                color = LotoGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(LotoSurfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${relations.size} numéros B",
                            color = LotoTextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Liste paginée des relations A -> B
        item {
            Text(
                text = "SUIVEURS LES PLUS FRÉQUENTS (A → B)",
                color = LotoTextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            )
        }

        items(pagedRelations) { rel ->
            RelationRowCard(rel = rel)
        }

        // Contrôles de pagination
        if (totalPages > 1 && canAccessFull) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    OutlinedButton(
                        onClick = { if (currentPage > 0) currentPage-- },
                        enabled = currentPage > 0,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = null)
                        Text("Précédent", fontSize = 12.sp)
                    }

                    Text(
                        text = "Page ${currentPage + 1} / $totalPages",
                        color = LotoTextMuted,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    OutlinedButton(
                        onClick = { if (currentPage < totalPages - 1) currentPage++ },
                        enabled = currentPage < totalPages - 1,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Suivant", fontSize = 12.sp)
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }
        }

        // Si gratuit, affichage du verrou
        if (!canAccessFull) {
            item {
                LockedFeatureCard(
                    requiredTier = UserTier.PREMIUM,
                    featureName = "Relations A → B complètes et pagination",
                    description = "Le compte Gratuit est limité aux 5 premiers suiveurs. Débloquez la formule Premium pour consulter l'ensemble des 89 relations de suivi conditionnel et le Lift d'attraction pour chaque numéro.",
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
private fun RelationRowCard(rel: RelationAB) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                LotoBall(
                    number = rel.numB,
                    size = 40.dp,
                    highlight = if (rel.lift >= 1.2) BallHighlight.GREEN else BallHighlight.DEFAULT
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Numéro ${rel.numB}",
                        color = LotoText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${rel.count} fois après le ${rel.numA}",
                        color = LotoTextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${String.format("%.1f", rel.probability * 100)}%",
                        color = LotoText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                val isPositiveLift = rel.lift >= 1.0
                Text(
                    text = "Lift: ${String.format("%.2f", rel.lift)}x",
                    color = if (isPositiveLift) LotoGreen else LotoRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
