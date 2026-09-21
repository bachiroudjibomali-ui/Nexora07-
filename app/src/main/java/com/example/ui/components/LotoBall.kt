package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LotoBackground
import com.example.ui.theme.LotoBlue
import com.example.ui.theme.LotoGold
import com.example.ui.theme.LotoGreen
import com.example.ui.theme.LotoOrange
import com.example.ui.theme.LotoText

enum class BallHighlight {
    DEFAULT,
    ORANGE,
    GREEN,
    BLUE,
    GOLD
}

@Composable
fun LotoBall(
    number: Int,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    highlight: BallHighlight = BallHighlight.DEFAULT
) {
    val (bgColor1, bgColor2, borderColor, textColor) = when (highlight) {
        BallHighlight.ORANGE -> listOf(
            LotoOrange,
            Color(0xFFB5541A),
            LotoOrange.copy(alpha = 0.9f),
            Color.White
        )
        BallHighlight.GREEN -> listOf(
            LotoGreen,
            Color(0xFF137042),
            LotoGreen.copy(alpha = 0.9f),
            Color.White
        )
        BallHighlight.BLUE -> listOf(
            LotoBlue,
            Color(0xFF285F96),
            LotoBlue.copy(alpha = 0.9f),
            Color.White
        )
        BallHighlight.GOLD -> listOf(
            LotoGold,
            Color(0xFF9E6E18),
            LotoGold.copy(alpha = 0.9f),
            Color.Black
        )
        BallHighlight.DEFAULT -> listOf(
            Color(0xFF1F292E),
            Color(0xFF131A1E),
            Color(0xFF334047),
            LotoText
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .testTag("loto_ball_$number")
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(bgColor1, bgColor2),
                    radius = 50f
                )
            )
            .border(1.5.dp, borderColor, CircleShape)
    ) {
        val fontSize = if (size < 36.dp) 12.sp else if (size < 48.dp) 14.sp else 18.sp
        Text(
            text = String.format("%02d", number),
            color = textColor,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
