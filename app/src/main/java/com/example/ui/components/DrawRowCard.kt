package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import com.example.data.Draw
import com.example.ui.theme.LotoBlue
import com.example.ui.theme.LotoBorder
import com.example.ui.theme.LotoGreen
import com.example.ui.theme.LotoOrange
import com.example.ui.theme.LotoRed
import com.example.ui.theme.LotoSurface
import com.example.ui.theme.LotoText
import com.example.ui.theme.LotoTextDim
import com.example.ui.theme.LotoTextMuted

@Composable
fun DrawRowCard(
    draw: Draw,
    modifier: Modifier = Modifier,
    highlightNumbers: Set<Int> = emptySet(),
    onDelete: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LotoSurface)
            .border(1.dp, LotoBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
            .testTag("draw_card_${draw.id}")
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Tirage #${draw.id}",
                        color = LotoOrange,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = draw.date,
                        color = LotoTextMuted,
                        fontSize = 12.sp
                    )
                    if (draw.isCustom) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(LotoBlue.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Ajouté",
                                color = LotoBlue,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Somme: ${draw.sum} | ${draw.evenCount}P/${draw.oddCount}I",
                        color = LotoTextDim,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    if (onDelete != null && draw.isCustom) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(28.dp)
                                .padding(start = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Supprimer ce tirage",
                                tint = LotoRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Boules de tirage
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (num in draw.numbers) {
                    val highlight = if (highlightNumbers.contains(num)) {
                        BallHighlight.ORANGE
                    } else if (num <= 30) {
                        BallHighlight.GREEN
                    } else if (num <= 60) {
                        BallHighlight.BLUE
                    } else {
                        BallHighlight.DEFAULT
                    }
                    LotoBall(
                        number = num,
                        size = 38.dp,
                        highlight = highlight
                    )
                }
            }
        }
    }
}
