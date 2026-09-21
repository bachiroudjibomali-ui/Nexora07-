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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Draw
import com.example.data.db.CustomDrawEntity
import com.example.engine.NumberStats
import com.example.ui.components.BallHighlight
import com.example.ui.components.DisclaimerCard
import com.example.ui.components.DrawRowCard
import com.example.ui.components.LotoBall
import com.example.ui.components.StatCard
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
fun HomeScreen(
    draws: List<Draw>,
    customDraws: List<CustomDrawEntity>,
    numberStats: List<NumberStats>,
    onAddDraw: (String, List<Int>) -> Unit,
    onMassImport: (String) -> Unit,
    onDeleteCustomDraw: (Long) -> Unit,
    onClearCustomDraws: () -> Unit,
    onNavigateToAnalysis: () -> Unit,
    onNavigateToPremium: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showMassImportDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0: 10 derniers, 1: Mes ajouts

    val lastDraw = draws.lastOrNull()
    val totalDraws = draws.size

    val mostFrequent = numberStats.maxByOrNull { it.frequency }
    val mostDelayed = numberStats.maxByOrNull { it.currentGap }
    val topScore = numberStats.maxByOrNull { it.score }

    Box(modifier = Modifier.fillMaxSize().background(LotoBackground)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Titre et Sous-titre
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Loto Niger Analytics",
                            color = LotoOrange,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Moteur statistique officiel 5 sur 90",
                            color = LotoTextMuted,
                            fontSize = 13.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(LotoSurfaceVariant)
                            .border(1.dp, LotoBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$totalDraws tirages",
                            color = LotoGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Avertissement permanent
            item {
                DisclaimerCard(compact = true)
            }

            // Carte du Dernier Tirage
            if (lastDraw != null) {
                item {
                    LastDrawCard(
                        draw = lastDraw,
                        totalCount = totalDraws,
                        onAddClick = { showAddDialog = true }
                    )
                }
            }

            // Grille des Indicateurs Clés
            item {
                Text(
                    text = "INDICATEURS CLÉS",
                    color = LotoTextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatCard(
                        title = "Top Fréquence",
                        value = mostFrequent?.let { "N°${it.number} (${it.frequency})" } ?: "-",
                        subtitle = mostFrequent?.let { "${String.format("%.1f", it.rate * 100)}%" },
                        accentColor = LotoGreen,
                        icon = Icons.Default.Analytics,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Plus en retard",
                        value = mostDelayed?.let { "N°${it.number} (${it.currentGap})" } ?: "-",
                        subtitle = "écart actuel",
                        accentColor = LotoOrange,
                        icon = Icons.Default.Timer,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatCard(
                        title = "Top Momentum",
                        value = topScore?.let { "N°${it.number}" } ?: "-",
                        subtitle = topScore?.let { "Indice: ${String.format("%.1f", it.score)}" },
                        accentColor = LotoGold,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Mes ajouts",
                        value = "${customDraws.size} tirages",
                        subtitle = "Persistance Room",
                        accentColor = LotoBlue,
                        icon = Icons.Default.History,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Actions rapides : Saisie & Import en masse
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LotoOrange,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("home_add_draw_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Saisir un tirage", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showMassImportDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = LotoText
                        ),
                        border = ButtonDefaults.outlinedButtonBorder(true).copy(
                            brush = androidx.compose.ui.graphics.SolidColor(LotoBorder)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("home_mass_import_button")
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, tint = LotoBlue, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Import en masse", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Onglets : 10 Derniers Tirages vs Mes Ajouts
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = LotoSurface,
                    contentColor = LotoOrange,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = LotoOrange
                        )
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, LotoBorder, RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "10 derniers tirages",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) LotoOrange else LotoTextMuted
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Mes ajouts (${customDraws.size})",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) LotoOrange else LotoTextMuted
                            )
                        }
                    )
                }
            }

            // Contenu de l'onglet sélectionné
            if (selectedTab == 0) {
                val recent10 = draws.takeLast(10).reversed()
                items(recent10) { draw ->
                    DrawRowCard(draw = draw)
                }
            } else {
                if (customDraws.isEmpty()) {
                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(LotoSurface)
                                .border(1.dp, LotoBorder, RoundedCornerShape(12.dp))
                                .padding(28.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = LotoTextDim,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Aucun tirage personnel ajouté",
                                    color = LotoTextMuted,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Utilisez « Saisir un tirage » ou « Import en masse » pour enrichir vos données.",
                                    color = LotoTextDim,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                } else {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${customDraws.size} tirage(s) personnalisé(s)",
                                color = LotoTextMuted,
                                fontSize = 12.sp
                            )
                            TextButton(onClick = onClearCustomDraws) {
                                Text("Tout effacer", color = LotoRed, fontSize = 12.sp)
                            }
                        }
                    }
                    items(customDraws.reversed()) { entity ->
                        val baseSize = draws.size - customDraws.size
                        val draw = entity.toDraw(baseSize)
                        DrawRowCard(
                            draw = draw,
                            onDelete = { onDeleteCustomDraw(entity.id) }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp)) // Espace pour le FAB et la barre de navigation
            }
        }

        // Bouton flottant FAB "Nouveau tirage"
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = LotoOrange,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
                .testTag("fab_new_draw")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nouveau tirage")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Nouveau tirage", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Dialogue de saisie manuelle d'un nouveau tirage
    if (showAddDialog) {
        AddDrawDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { date, nums ->
                onAddDraw(date, nums)
                showAddDialog = false
            }
        )
    }

    // Dialogue d'import en masse
    if (showMassImportDialog) {
        MassImportDialog(
            onDismiss = { showMassImportDialog = false },
            onConfirm = { text ->
                onMassImport(text)
                showMassImportDialog = false
            }
        )
    }
}

@Composable
private fun LastDrawCard(
    draw: Draw,
    totalCount: Int,
    onAddClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(LotoSurfaceElevated)
            .border(1.dp, LotoOrange.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .padding(16.dp)
            .testTag("last_draw_card")
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(LotoOrange)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "DERNIER TIRAGE",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "#${draw.id}",
                        color = LotoText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = draw.date,
                    color = LotoTextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5 Boules du tirage
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                for (num in draw.numbers) {
                    LotoBall(
                        number = num,
                        size = 52.dp,
                        highlight = BallHighlight.ORANGE
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Somme des numéros : ${draw.sum}",
                    color = LotoTextMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Parité : ${draw.evenCount} Pairs / ${draw.oddCount} Impairs",
                    color = LotoTextMuted,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun AddDrawDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, List<Int>) -> Unit
) {
    val today = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }
    var dateText by remember { mutableStateOf(today) }
    var numbersText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LotoSurface,
        title = {
            Text("Saisir un nouveau tirage", color = LotoText, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "Entrez la date et les 5 numéros tirés (entre 1 et 90). Les séparateurs sont libres (espaces, virgules, tirets).",
                    color = LotoTextMuted,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Date du tirage (JJ/MM/AAAA)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LotoText,
                        unfocusedTextColor = LotoText,
                        focusedBorderColor = LotoOrange,
                        unfocusedBorderColor = LotoBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_draw_date")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = numbersText,
                    onValueChange = {
                        numbersText = it
                        errorMessage = null
                    },
                    label = { Text("5 numéros (ex: 12 45 67 89 23)") },
                    placeholder = { Text("12, 45, 67, 89, 23") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LotoText,
                        unfocusedTextColor = LotoText,
                        focusedBorderColor = LotoOrange,
                        unfocusedBorderColor = LotoBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_draw_numbers")
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage!!, color = LotoRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val nums = Regex("\\d+").findAll(numbersText)
                        .map { it.value.toInt() }
                        .toList()
                    if (nums.size != 5) {
                        errorMessage = "Veuillez saisir exactement 5 numéros (actuellement ${nums.size})."
                        return@Button
                    }
                    if (nums.toSet().size != 5) {
                        errorMessage = "Les 5 numéros doivent être tous distincts."
                        return@Button
                    }
                    val outOfRange = nums.filter { it !in 1..90 }
                    if (outOfRange.isNotEmpty()) {
                        errorMessage = "Les numéros doivent être compris entre 1 et 90."
                        return@Button
                    }
                    onConfirm(dateText.trim(), nums.sorted())
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = LotoOrange,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("confirm_add_draw_button")
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = LotoTextMuted)
            }
        }
    )
}

@Composable
private fun MassImportDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var rawText by remember {
        mutableStateOf(
            "46-84-67-60-48 (01/01/26)\n12-34-56-78-90 (02/01/26)"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LotoSurface,
        title = {
            Text("Import en masse", color = LotoText, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "Collez vos tirages (une ligne par tirage).\nFormat conseillé : « 46-84-67-60-48 (01/01/26) » ou séparateurs libres.",
                    color = LotoTextMuted,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    minLines = 6,
                    maxLines = 10,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LotoText,
                        unfocusedTextColor = LotoText,
                        focusedBorderColor = LotoBlue,
                        unfocusedBorderColor = LotoBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("mass_import_textarea")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(rawText) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = LotoBlue,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("confirm_mass_import_button")
            ) {
                Text("Importer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = LotoTextMuted)
            }
        }
    )
}
