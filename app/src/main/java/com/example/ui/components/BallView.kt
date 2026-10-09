package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Ball

/**
 * Renders an individual 3D glossy ball with:
 * - Radial gradient spherical shading
 * - Top-left specular glint
 * - High-contrast colorblind accessibility patterns/symbols
 */
@Composable
fun BallView(
    ball: Ball,
    size: Dp,
    showSymbol: Boolean,
    modifier: Modifier = Modifier
) {
    val ballColor = ball.color

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 4.dp,
                shape = CircleShape,
                spotColor = ballColor.darkColor
            )
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        ballColor.lightColor,
                        ballColor.primaryColor,
                        ballColor.darkColor
                    ),
                    center = Offset(0.32f, 0.32f)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // 3D Specular Light Glint
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = this.size.minDimension
            drawCircle(
                color = Color.White.copy(alpha = 0.55f),
                radius = radius * 0.16f,
                center = Offset(this.size.width * 0.30f, this.size.height * 0.26f)
            )
            // Secondary subtle bottom reflection
            drawCircle(
                color = Color.White.copy(alpha = 0.15f),
                radius = radius * 0.22f,
                center = Offset(this.size.width * 0.65f, this.size.height * 0.72f)
            )
        }

        // Colorblind Pattern Symbol
        if (showSymbol) {
            Text(
                text = ballColor.symbol,
                color = ballColor.symbolColor,
                fontSize = (size.value * 0.42f).sp,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
