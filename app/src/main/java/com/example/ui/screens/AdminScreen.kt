package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.AdminCodeItem
import com.example.service.CreatedCodeResult
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
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminScreen(
    isAdminLoggedIn: Boolean,
    adminEmail: String?,
    codesList: List<AdminCodeItem>,
    isLoading: Boolean,
    createdCode: CreatedCodeResult?,
    onLogin: (String, String) -> Unit,
    onLogout: () -> Unit,
    onRefresh: () -> Unit,
    onCreateCode: (String, String) -> Unit,
    onDismissCreatedCode: () -> Unit,
    onRevokeCode: (String) -> Unit,
    onProlongerCode: (String, Int) -> Unit,
    onResetDevices: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var inputEmail by remember { mutableStateOf(adminEmail ?: "bachiroudjibomali@gmail.com") }
    var inputPassword by remember { mutableStateOf("") }

    var selectedNewTier by remember { mutableStateOf("plus") }
    var clientNote by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("TOUS") }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LotoBackground)
    ) {
        // En-tête Navigation
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(LotoSurface)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint = LotoOrange
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Espace Propriétaire",
                    color = LotoText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isAdminLoggedIn) "Connecté : ${adminEmail ?: "Admin"}" else "Administration sécurisée",
                    color = LotoTextMuted,
                    fontSize = 11.sp
                )
            }

            if (isAdminLoggedIn) {
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Actualiser", tint = LotoBlue)
                }
                TextButton(onClick = onLogout) {
                    Text("Quitter", color = LotoRed, fontSize = 12.sp)
                }
            }
        }

        if (!isAdminLoggedIn) {
            // Vue de connexion Administrateur
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(LotoSurface)
                        .border(1.dp, LotoBorder, RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .background(LotoOrange.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = LotoOrange,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Accès Administrateur",
                            color = LotoOrange,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Réservé au propriétaire pour générer, prolonger et révoquer les codes d'accès abonnés.",
                            color = LotoTextMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        OutlinedTextField(
                            value = inputEmail,
                            onValueChange = { inputEmail = it },
                            label = { Text("Email Propriétaire") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = LotoText,
                                unfocusedTextColor = LotoText,
                                focusedBorderColor = LotoOrange,
                                unfocusedBorderColor = LotoBorder
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = inputPassword,
                            onValueChange = { inputPassword = it },
                            label = { Text("Mot de passe") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = LotoText,
                                unfocusedTextColor = LotoText,
                                focusedBorderColor = LotoOrange,
                                unfocusedBorderColor = LotoBorder
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = { onLogin(inputEmail, inputPassword) },
                            enabled = !isLoading && inputEmail.isNotBlank() && inputPassword.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LotoOrange,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Se connecter", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            // Vue connectée : Gestion des codes
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Modal / Affichage du code nouvellement créé
                if (createdCode != null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(LotoGreen.copy(alpha = 0.15f))
                                .border(1.5.dp, LotoGreen, RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LotoGreen)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "CODE GÉNÉRÉ AVEC SUCCÈS",
                                            color = LotoGreen,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    TextButton(onClick = onDismissCreatedCode) {
                                        Text("Fermer", color = LotoTextMuted, fontSize = 11.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = createdCode.code,
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Formule : ${if (createdCode.tier == "plus") "Premium+ (5 000 FCFA)" else "Premium (2 000 FCFA)"} • Client : ${createdCode.note.ifBlank { "Non renseigné" }}",
                                    color = LotoText,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Valable 30 jours dès la 1ère activation • 2 appareils maximum",
                                    color = LotoTextMuted,
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(createdCode.code))
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp), tint = LotoGreen)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Copier", color = LotoGreen, fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = {
                                            val formula = if (createdCode.tier == "plus") "Premium+" else "Premium"
                                            val msg = "Bonjour ${createdCode.note.ifBlank { "cher abonné" }},\n\nVoici votre code d'accès pour l'application Loto Niger Analytics ($formula) :\n\n🔑 *${createdCode.code}*\n\nValidité : 30 jours à partir de l'activation (2 appareils max).\nMerci pour votre confiance !"
                                            val url = "https://wa.me/?text=${URLEncoder.encode(msg, "UTF-8")}"
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            context.startActivity(intent)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF25D366),
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1.3f)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("WhatsApp", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 1 : Créer un nouveau code
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(LotoSurface)
                            .border(1.dp, LotoBorder, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = LotoOrange)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "CRÉER UN NOUVEAU CODE D'ACCÈS",
                                    color = LotoOrange,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Sélecteur de formule
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                FilterChip(
                                    selected = selectedNewTier == "prem",
                                    onClick = { selectedNewTier = "prem" },
                                    label = { Text("Premium (2 000 F)") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = LotoOrange.copy(alpha = 0.25f),
                                        selectedLabelColor = LotoOrange
                                    )
                                )
                                FilterChip(
                                    selected = selectedNewTier == "plus",
                                    onClick = { selectedNewTier = "plus" },
                                    label = { Text("Premium+ (5 000 F)") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = LotoGreen.copy(alpha = 0.25f),
                                        selectedLabelColor = LotoGreen
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = clientNote,
                                onValueChange = { clientNote = it },
                                label = { Text("Nom du client ou note (ex: Amadou Niamey)") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = LotoText,
                                    unfocusedTextColor = LotoText,
                                    focusedBorderColor = LotoOrange,
                                    unfocusedBorderColor = LotoBorder
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    onCreateCode(selectedNewTier, clientNote)
                                    clientNote = ""
                                },
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LotoOrange,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Text("Générer le code sécurisé", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Section 2 : Liste des codes émis
                item {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "CODES ÉMIS (${codesList.size})",
                                color = LotoTextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp
                            )
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = LotoOrange, strokeWidth = 2.dp)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Filtres chips
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("TOUS", "ACTIFS", "INACTIFS", "EXPIRÉS").forEach { f ->
                                FilterChip(
                                    selected = selectedFilter == f,
                                    onClick = { selectedFilter = f },
                                    label = { Text(f, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = LotoBlue.copy(alpha = 0.3f),
                                        selectedLabelColor = LotoBlue
                                    )
                                )
                            }
                        }
                    }
                }

                val filteredList = codesList.filter { item ->
                    val matchesQuery = searchQuery.isBlank() ||
                            item.note.contains(searchQuery, ignoreCase = true) ||
                            item.tier.contains(searchQuery, ignoreCase = true)

                    val matchesFilter = when (selectedFilter) {
                        "ACTIFS" -> item.status == "actif"
                        "INACTIFS" -> item.status == "inactif"
                        "EXPIRÉS" -> item.status == "expiré" || item.status == "révoqué"
                        else -> true
                    }
                    matchesQuery && matchesFilter
                }

                if (filteredList.isEmpty()) {
                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp)
                        ) {
                            Text(
                                text = "Aucun code trouvé pour ce filtre.",
                                color = LotoTextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    items(filteredList) { codeItem ->
                        AdminCodeCard(
                            item = codeItem,
                            dateFormat = dateFormat,
                            onRevoke = { onRevokeCode(codeItem.id) },
                            onProlonger = { onProlongerCode(codeItem.id, 30) },
                            onResetDevices = { onResetDevices(codeItem.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun AdminCodeCard(
    item: AdminCodeItem,
    dateFormat: SimpleDateFormat,
    onRevoke: () -> Unit,
    onProlonger: () -> Unit,
    onResetDevices: () -> Unit
) {
    val tierColor = if (item.tier == "plus") LotoGreen else LotoOrange
    val statusColor = when (item.status) {
        "actif" -> LotoGreen
        "inactif" -> LotoTextMuted
        else -> LotoRed
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(LotoSurface)
            .border(1.dp, LotoBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
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
                            .clip(RoundedCornerShape(4.dp))
                            .background(tierColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (item.tier == "plus") "PREMIUM+" else "PREMIUM",
                            color = tierColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.status.uppercase(),
                            color = statusColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Compteur d'appareils
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DeviceHub,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = if (item.appareilsCount >= 2) LotoOrange else LotoTextMuted
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${item.appareilsCount}/2 appareils",
                        color = if (item.appareilsCount >= 2) LotoOrange else LotoTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (item.note.isNotBlank()) {
                Text(
                    text = "Client : ${item.note}",
                    color = LotoText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (item.expireAt != null) {
                val now = System.currentTimeMillis()
                val daysLeft = ((item.expireAt - now) / (24 * 3600 * 1000L)).toInt()
                val expireStr = dateFormat.format(Date(item.expireAt))
                Text(
                    text = "Expire le : $expireStr ($daysLeft j restants)",
                    color = if (daysLeft <= 3) LotoRed else LotoTextDim,
                    fontSize = 11.sp
                )
            } else {
                Text(
                    text = "Non encore activé (30 jours démarrent à l'activation)",
                    color = LotoTextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Actions d'administration
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onResetDevices) {
                    Text("Réinit. app. (0/2)", color = LotoBlue, fontSize = 11.sp)
                }
                TextButton(onClick = onProlonger) {
                    Text("+30 jours", color = LotoGreen, fontSize = 11.sp)
                }
                if (item.status != "révoqué") {
                    TextButton(onClick = onRevoke) {
                        Text("Révoquer", color = LotoRed, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
