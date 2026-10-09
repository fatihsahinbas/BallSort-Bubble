package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Ball
import com.example.model.Tube
import kotlin.math.roundToInt

@Composable
fun TubeView(
    tube: Tube,
    isSelected: Boolean,
    showSymbols: Boolean,
    tubeWidth: Dp,
    tubeHeight: Dp,
    ballSize: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Completed pulse scale
    val completedScale by animateFloatAsState(
        targetValue = if (tube.isCompleted) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "completedScale"
    )

    // Glow and border colors
    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        tube.isCompleted -> Color(0xFFFFD700)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
    }

    val borderWidth = if (isSelected || tube.isCompleted) 2.5.dp else 1.5.dp

    Box(
        modifier = modifier
            .width(tubeWidth)
            .height(tubeHeight + ballSize + 12.dp) // extra headroom for lifted ball
            .testTag("tube_${tube.id}")
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Tube Rim (top lip)
        Box(
            modifier = Modifier
                .width(tubeWidth + 8.dp)
                .height(6.dp)
                .offset(y = (-tubeHeight))
                .clip(RoundedCornerShape(3.dp))
                .background(
                    when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        tube.isCompleted -> Color(0xFFFFD700)
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                    }
                )
        )

        // Glass Tube Body
        Box(
            modifier = Modifier
                .width(tubeWidth)
                .height(tubeHeight)
                .clip(RoundedCornerShape(bottomStart = tubeWidth / 2, bottomEnd = tubeWidth / 2))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                        )
                    )
                )
                .border(
                    width = borderWidth,
                    color = borderColor,
                    shape = RoundedCornerShape(bottomStart = tubeWidth / 2, bottomEnd = tubeWidth / 2)
                )
        ) {
            // Glass Reflection Line
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawLine(
                    color = Color.White.copy(alpha = 0.25f),
                    start = Offset(x = size.width * 0.18f, y = 8f),
                    end = Offset(x = size.width * 0.18f, y = size.height * 0.85f),
                    strokeWidth = 3f
                )
            }

            // Completed checkmark indicator at tube base
            if (tube.isCompleted) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50).copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Balls stack column
        Column(
            modifier = Modifier
                .width(tubeWidth)
                .height(tubeHeight)
                .padding(bottom = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Balls are ordered: bottom to top
            // Render remaining empty slots, then balls
            val balls = tube.balls
            val topIndex = balls.lastIndex

            for (i in (tube.capacity - 1) downTo 0) {
                if (i <= topIndex) {
                    val ball = balls[i]
                    val isTopBall = (i == topIndex)

                    // Animate top ball lifting up if tube is selected
                    val liftOffsetFraction by animateFloatAsState(
                        targetValue = if (isTopBall && isSelected) 1f else 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "ballLift"
                    )

                    val liftY = -(liftOffsetFraction * (tubeHeight.value * 0.45f + 18f))

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(0, liftY.roundToInt()) }
                            .padding(vertical = 1.dp)
                    ) {
                        BallView(
                            ball = ball,
                            size = ballSize,
                            showSymbol = showSymbols
                        )
                    }
                } else {
                    // Empty slot spacer
                    Spacer(modifier = Modifier.size(ballSize + 2.dp))
                }
            }
        }
    }
}

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
                elevation = 3.dp,
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
                    center = Offset(0.35f, 0.35f)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Specular highlight gleam
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.45f),
                radius = this.size.minDimension * 0.16f,
                center = Offset(this.size.width * 0.32f, this.size.height * 0.28f)
            )
        }

        // Colorblind symbol
        if (showSymbol) {
            Text(
                text = ballColor.symbol,
                color = ballColor.symbolColor,
                fontSize = (size.value * 0.4f).sp,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
