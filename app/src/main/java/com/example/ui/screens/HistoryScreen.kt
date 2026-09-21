package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.config.AccessControl
import com.example.config.UserTier
import com.example.data.Draw
import com.example.ui.components.DisclaimerCard
import com.example.ui.components.DrawRowCard
import com.example.ui.components.LockedFeatureCard
import com.example.ui.theme.LotoBackground
import com.example.ui.theme.LotoBorder
import com.example.ui.theme.LotoOrange
import com.example.ui.theme.LotoSurface
import com.example.ui.theme.LotoText
import com.example.ui.theme.LotoTextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    draws: List<Draw>,
    userTier: UserTier,
    onBack: () -> Unit,
    onNavigateToPremium: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val canAccessFull = AccessControl.LIMITS.canAccessHistoryFull(userTier)

    val reversedDraws = draws.reversed()

    val filteredDraws = reversedDraws.filter { draw ->
        if (searchQuery.isBlank()) true
        else {
            val q = searchQuery.trim()
            val numQuery = q.toIntOrNull()
            if (numQuery != null) {
                draw.numbers.contains(numQuery)
            } else {
                draw.date.contains(q, ignoreCase = true) || draw.id.toString() == q
            }
        }
    }

    val displayDraws = if (canAccessFull) filteredDraws else filteredDraws.take(AccessControl.LIMITS.FREE_HISTORY_COUNT)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historique des Tirages", color = LotoText, fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                DisclaimerCard(compact = true)
            }

            // Recherche
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = LotoTextMuted)
                    },
                    placeholder = { Text("Rechercher un numéro (ex: 46) ou date...", color = LotoTextMuted) },
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
                        .testTag("history_search_input")
                )
            }

            item {
                Text(
                    text = "${filteredDraws.size} tirage(s) trouvé(s) sur ${draws.size} au total",
                    color = LotoTextMuted,
                    fontSize = 12.sp
                )
            }

            // Liste des tirages
            items(displayDraws) { draw ->
                val numQuery = searchQuery.trim().toIntOrNull()
                val highlight = if (numQuery != null && draw.numbers.contains(numQuery)) setOf(numQuery) else emptySet()
                DrawRowCard(draw = draw, highlightNumbers = highlight)
            }

            if (!canAccessFull) {
                item {
                    LockedFeatureCard(
                        requiredTier = UserTier.PREMIUM,
                        featureName = "Historique complet des 261 tirages",
                        description = "La version gratuite est limitée aux 10 derniers tirages. Activez l'abonnement Premium pour consulter l'intégralité des tirages du 01/01/2026 au 18/09/2026 avec recherche instantanée.",
                        onNavigateToPremium = onNavigateToPremium
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
