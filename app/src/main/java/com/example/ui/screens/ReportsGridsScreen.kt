package com.example.ui.screens

import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.config.AccessControl
import com.example.config.UserTier
import com.example.data.Draw
import com.example.engine.LotoEngine
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsGridsScreen(
    draws: List<Draw>,
    numberStats: List<NumberStats>,
    userTier: UserTier,
    onBack: () -> Unit,
    onNavigateToPremium: () -> Unit
) {
    val context = LocalContext.current
    var selectedSection by remember { mutableIntStateOf(0) } // 0: Générateur de Grilles, 1: Rapport d'Analyse
    var selectedStrategy by remember { mutableStateOf("Équilibrée") }
    var generatedGrids by remember {
        mutableStateOf(LotoEngine.generateSmartGrids(draws, 5, "Équilibrée"))
    }

    val canExportPdf = AccessControl.LIMITS.canExportPdf(userTier)
    val canExportCsv = AccessControl.LIMITS.canExportCsv(userTier)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rapports & Grilles", color = LotoText, fontWeight = FontWeight.Bold) },
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

            // Onglets : Grilles vs Rapport
            item {
                TabRow(
                    selectedTabIndex = selectedSection,
                    containerColor = LotoSurface,
                    contentColor = LotoOrange,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedSection]),
                            color = LotoOrange
                        )
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, LotoBorder, RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedSection == 0,
                        onClick = { selectedSection = 0 },
                        text = {
                            Text(
                                "Générateur de Grilles",
                                fontWeight = if (selectedSection == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedSection == 0) LotoOrange else LotoTextMuted
                            )
                        }
                    )
                    Tab(
                        selected = selectedSection == 1,
                        onClick = { selectedSection = 1 },
                        text = {
                            Text(
                                "Rapport d'Analyse",
                                fontWeight = if (selectedSection == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedSection == 1) LotoOrange else LotoTextMuted
                            )
                        }
                    )
                }
            }

            if (selectedSection == 0) {
                // Section Générateur de Grilles
                item {
                    Text(
                        text = "STRATÉGIE D'ASSEMBLAGE DES GRILLES :",
                        color = LotoTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Équilibrée", "Plein Momentum", "Chasseurs de Retard").forEach { strat ->
                            val isSel = selectedStrategy == strat
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) LotoOrange else LotoSurface)
                                    .border(1.dp, if (isSel) LotoOrange else LotoBorder, RoundedCornerShape(8.dp))
                            ) {
                                androidx.compose.material3.TextButton(
                                    onClick = {
                                        selectedStrategy = strat
                                        generatedGrids = LotoEngine.generateSmartGrids(draws, 5, strat)
                                    }
                                ) {
                                    Text(
                                        text = strat,
                                        color = if (isSel) Color.White else LotoText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Bouton Régénérer & Exporter CSV
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                generatedGrids = LotoEngine.generateSmartGrids(draws, 5, selectedStrategy)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LotoOrange, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Régénérer 5 grilles", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                if (canExportCsv) {
                                    exportGridsCsv(context, generatedGrids)
                                } else {
                                    onNavigateToPremium()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = LotoGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (canExportCsv) "Export CSV" else "Export CSV 🔒", fontSize = 12.sp, color = LotoText)
                        }
                    }
                }

                // Affichage des grilles
                items(generatedGrids.withIndex().toList()) { (index, grid) ->
                    GridDisplayCard(index = index + 1, numbers = grid)
                }

            } else {
                // Section Rapport d'Analyse
                item {
                    val topMomentum = numberStats.sortedByDescending { it.score }.take(5).map { it.number }
                    val topFreq = numberStats.sortedByDescending { it.frequency }.take(5).map { it.number }
                    val topDelay = numberStats.sortedByDescending { it.currentGap }.take(5).map { it.number }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(LotoSurface)
                            .border(1.dp, LotoBorder, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "RAPPORT STATISTIQUE GLOBAL",
                                color = LotoOrange,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Édition du ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())} - Base de ${draws.size} tirages",
                                color = LotoTextMuted,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "• Top 5 Momentum : ${topMomentum.joinToString(", ")}",
                                color = LotoText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• Top 5 Sorties : ${topFreq.joinToString(", ")}",
                                color = LotoText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• Top 5 Retards : ${topDelay.joinToString(", ")}",
                                color = LotoText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (canExportPdf) {
                                        shareAnalysisReport(context, draws, numberStats)
                                    } else {
                                        onNavigateToPremium()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (canExportPdf) LotoOrange else Color(0xFF2A363E),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(46.dp)
                            ) {
                                Icon(
                                    imageVector = if (canExportPdf) Icons.Default.Share else Icons.Default.PictureAsPdf,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (canExportPdf) "Partager le rapport complet (PDF / Texte)" else "Export PDF & Partage 🔒 (Premium+)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                if (!canExportPdf) {
                    item {
                        LockedFeatureCard(
                            requiredTier = UserTier.PREMIUM_PLUS,
                            featureName = "Générateur et Exportateur de Rapports PDF & Grilles CSV",
                            description = "Exportez vos analyses et grilles de combinaisons au format professionnel PDF et fichier tableur CSV prêt pour l'impression ou le partage WhatsApp.",
                            onNavigateToPremium = onNavigateToPremium
                        )
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
private fun GridDisplayCard(index: Int, numbers: List<Int>) {
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
            Text(
                text = "Grille #$index",
                color = LotoOrange,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(70.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (num in numbers) {
                    LotoBall(
                        number = num,
                        size = 38.dp,
                        highlight = BallHighlight.ORANGE
                    )
                }
            }
        }
    }
}

private fun exportGridsCsv(context: Context, grids: List<List<Int>>) {
    val csvContent = buildString {
        appendLine("Grille,N1,N2,N3,N4,N5")
        grids.forEachIndexed { idx, nums ->
            appendLine("Grille ${idx + 1},${nums.joinToString(",")}")
        }
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_SUBJECT, "Grilles Loto Niger")
        putExtra(Intent.EXTRA_TEXT, csvContent)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(Intent.createChooser(intent, "Partager le fichier CSV"))
}

private fun shareAnalysisReport(
    context: Context,
    draws: List<Draw>,
    numberStats: List<NumberStats>
) {
    val topMomentum = numberStats.sortedByDescending { it.score }.take(5)
    val topFreq = numberStats.sortedByDescending { it.frequency }.take(5)
    val topDelay = numberStats.sortedByDescending { it.currentGap }.take(5)

    val reportText = buildString {
        appendLine("📊 RAPPORT OFFICIEL LOTO NIGER ANALYTICS")
        appendLine("Date : ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}")
        appendLine("Base totale : ${draws.size} tirages analysés")
        appendLine("----------------------------------------")
        appendLine("🚀 TOP 5 INDICE STATISTIQUE (MOMENTUM) :")
        topMomentum.forEach {
            appendLine("  • N°${it.number} : Score ${String.format("%.1f", it.score)} (Retard ${it.currentGap}, ${it.frequency} sorties)")
        }
        appendLine("")
        appendLine("🔥 TOP 5 NUMÉROS LES PLUS FRÉQUENTS :")
        topFreq.forEach {
            appendLine("  • N°${it.number} : ${it.frequency} sorties (${String.format("%.1f", it.rate * 100)}%)")
        }
        appendLine("")
        appendLine("⏳ TOP 5 NUMÉROS LES PLUS EN RETARD :")
        topDelay.forEach {
            appendLine("  • N°${it.number} : écart de ${it.currentGap} tirages (Écart max : ${it.maxGap})")
        }
        appendLine("----------------------------------------")
        appendLine("⚠️ Avertissement : Les statistiques historiques ne garantissent jamais le résultat d'un prochain tirage. Moteur développé par Roa Vision numérique — Niamey.")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Rapport d'analyse Loto Niger Analytics")
        putExtra(Intent.EXTRA_TEXT, reportText)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(Intent.createChooser(intent, "Partager le rapport d'analyse"))
}
