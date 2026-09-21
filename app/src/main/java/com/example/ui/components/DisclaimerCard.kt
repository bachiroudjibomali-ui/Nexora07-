package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LotoBorder
import com.example.ui.theme.LotoGold
import com.example.ui.theme.LotoSurface
import com.example.ui.theme.LotoText
import com.example.ui.theme.LotoTextDim
import com.example.ui.theme.LotoTextMuted

@Composable
fun DisclaimerCard(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF14191C))
            .border(1.dp, LotoBorder, RoundedCornerShape(12.dp))
            .padding(if (compact) 10.dp else 14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = LotoGold,
                modifier = Modifier.padding(top = 2.dp, end = 10.dp)
            )
            Column {
                Text(
                    text = "Avertissement permanent de transparence",
                    color = LotoGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "« Les statistiques historiques ne garantissent jamais le résultat d'un prochain tirage. »",
                    color = LotoText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                if (!compact) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Engagement de transparence : aucun abonnement ne donne accès à une prédiction. Cet outil est un moteur d'exploration probabiliste et statistique fondé sur les données passées.",
                        color = LotoTextMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
