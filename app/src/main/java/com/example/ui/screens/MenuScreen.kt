package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Ball
import com.example.model.BallColor
import com.example.ui.components.BallView
import com.example.ui.components.BannerAdView

@Composable
fun MenuScreen(
    maxUnlockedLevel: Int,
    isAdsRemoved: Boolean,
    canRequestAds: Boolean,
    onPlayClicked: () -> Unit,
    onLevelsClicked: () -> Unit,
    onSettingsClicked: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "menuBalls")
    val bounceY1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce1"
    )
    val bounceY2 by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce2"
    )
    val bounceY3 by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = -14f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce3"
    )

    Scaffold(
        bottomBar = {
            // Adaptive banner on menu screen only!
            BannerAdView(
                isAdsRemoved = isAdsRemoved,
                canRequestAds = canRequestAds,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Animated decorative balls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.offset(y = bounceY1.dp)) {
                        BallView(
                            ball = Ball(1L, BallColor.RUBY_RED),
                            size = 46.dp,
                            showSymbol = true
                        )
                    }
                    Box(modifier = Modifier.offset(y = bounceY2.dp)) {
                        BallView(
                            ball = Ball(2L, BallColor.SKY_BLUE),
                            size = 54.dp,
                            showSymbol = true
                        )
                    }
                    Box(modifier = Modifier.offset(y = bounceY3.dp)) {
                        BallView(
                            ball = Ball(3L, BallColor.EMERALD_GREEN),
                            size = 46.dp,
                            showSymbol = true
                        )
                    }
                    Box(modifier = Modifier.offset(y = bounceY1.dp)) {
                        BallView(
                            ball = Ball(4L, BallColor.SOLAR_GOLD),
                            size = 42.dp,
                            showSymbol = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // App Title
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.level_label, maxUnlockedLevel),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(44.dp))

                // Play Button
                Button(
                    onClick = onPlayClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("play_button"),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.play),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Levels Button
                OutlinedButton(
                    onClick = onLevelsClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("levels_button"),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GridOn,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.levels),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Settings Button
                OutlinedButton(
                    onClick = onSettingsClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("settings_button"),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.settings),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
