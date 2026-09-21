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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.config.UserTier
import com.example.ui.theme.LotoBackground
import com.example.ui.theme.LotoBorder
import com.example.ui.theme.LotoGold
import com.example.ui.theme.LotoOrange
import com.example.ui.theme.LotoSurface
import com.example.ui.theme.LotoText
import com.example.ui.theme.LotoTextMuted

@Composable
fun LockedFeatureCard(
    requiredTier: UserTier,
    featureName: String,
    description: String,
    onNavigateToPremium: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tierColor = if (requiredTier == UserTier.PREMIUM_PLUS) Color(0xFF1FA363) else LotoOrange

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1B2328),
                        LotoSurface
                    )
                )
            )
            .border(1.dp, tierColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(20.dp)
            .testTag("locked_card_${requiredTier.name}")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(tierColor.copy(alpha = 0.15f))
                    .border(1.dp, tierColor, RoundedCornerShape(26.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Module verrouillé",
                    tint = tierColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "🔒 Fonctionnalité réservée aux membres ${requiredTier.displayName}",
                color = LotoText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "« $featureName » nécessite un abonnement ${requiredTier.displayName}.",
                color = tierColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                color = LotoTextMuted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNavigateToPremium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = tierColor,
                    contentColor = LotoBackground
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(44.dp)
                    .testTag("unlock_premium_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Voir les formules & Activer",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
