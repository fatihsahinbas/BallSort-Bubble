package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.audio.VibrationManager
import com.example.model.Tube
import com.example.ui.GameViewModel
import com.example.ui.components.ConfettiEffect
import com.example.ui.components.TubeView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    vibrationManager: VibrationManager,
    currentLevel: Int,
    tubes: List<Tube>,
    selectedTubeIndex: Int?,
    remainingUndos: Int,
    bonusTubesCount: Int,
    movesCount: Int = 0,
    elapsedSeconds: Int = 0,
    isLevelWon: Boolean,
    isSymbolsEnabled: Boolean,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onRequestRewardedAd: (rewardType: String) -> Unit,
    onOpenLevels: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    var showRestartDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    // Screen shake animatables
    val shakeOffsetX = remember { androidx.compose.animation.core.Animatable(0f) }
    val shakeOffsetY = remember { androidx.compose.animation.core.Animatable(0f) }

    // Celebratory victory haptic pattern
    LaunchedEffect(isLevelWon) {
        if (isLevelWon) {
            vibrationManager.vibrateVictory()
        }
    }

    // Celebratory screen shake animation
    LaunchedEffect(isLevelWon) {
        if (isLevelWon) {
            val shakeSequence = listOf(
                Pair(14f, -8f),
                Pair(-12f, 9f),
                Pair(10f, -6f),
                Pair(-8f, 5f),
                Pair(5f, -3f),
                Pair(-3f, 2f),
                Pair(0f, 0f)
            )
            for ((sx, sy) in shakeSequence) {
                shakeOffsetX.snapTo(sx)
                shakeOffsetY.snapTo(sy)
                kotlinx.coroutines.delay(40)
            }
        } else {
            shakeOffsetX.snapTo(0f)
            shakeOffsetY.snapTo(0f)
        }
    }

    // Intercept back button to return to Menu
    BackHandler {
        onBack()
    }

    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = { Text(stringResource(R.string.restart)) },
            text = { Text(stringResource(R.string.restart_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestartDialog = false
                        viewModel.restartLevel()
                    },
                    modifier = Modifier.testTag("confirm_restart_button")
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRestartDialog = false },
                    modifier = Modifier.testTag("cancel_restart_button")
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showHelpDialog) {
        RewardedOfferDialog(
            canAddTube = bonusTubesCount < 2,
            onWatchForTube = {
                showHelpDialog = false
                onRequestRewardedAd("TUBE")
            },
            onWatchForUndos = {
                showHelpDialog = false
                onRequestRewardedAd("UNDOS")
            },
            onDismiss = { showHelpDialog = false }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.level_label, currentLevel),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.moves_format, movesCount),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            val minutes = elapsedSeconds / 60
                            val seconds = elapsedSeconds % 60
                            Text(
                                text = stringResource(R.string.time_format, minutes, seconds),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("game_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showRestartDialog = true },
                        modifier = Modifier.testTag("restart_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.restart)
                        )
                    }
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("game_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Primary Game Controls Bar (Undo, Restart, Add Tube)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Undo Button
                    FilledTonalButton(
                        onClick = {
                            if (remainingUndos > 0) {
                                viewModel.undoMove(
                                    onHapticFeedback = {
                                        vibrationManager.vibrateBallPlaced()
                                    }
                                )
                            } else {
                                showHelpDialog = true
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("undo_button"),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.undo, remainingUndos),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }

                    // Restart Button
                    OutlinedButton(
                        onClick = { showRestartDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("restart_action_button"),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.restart),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }

                    // Extra Tube Button
                    FilledTonalButton(
                        onClick = { showHelpDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_tube_button"),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.add_tube),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Responsive Adaptive Tube Board with Screen Shake on Win
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .offset(x = shakeOffsetX.value.dp, y = shakeOffsetY.value.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val availableWidth = maxWidth
                    val availableHeight = maxHeight

                    // Determine layout: 1 row vs 2 rows
                    val totalTubes = tubes.size
                    val isTwoRows = totalTubes > 5

                    val row1Count = if (isTwoRows) (totalTubes + 1) / 2 else totalTubes
                    val row2Count = if (isTwoRows) totalTubes - row1Count else 0
                    val maxInRow = maxOf(row1Count, row2Count).coerceAtLeast(1)

                    // Dynamic horizontal spacing based on tube density
                    val spacing = when {
                        maxInRow >= 7 -> 6.dp
                        maxInRow == 6 -> 8.dp
                        else -> 10.dp
                    }

                    // Available horizontal width per tube
                    val totalHorizontalSpacing = spacing * (maxInRow + 1)
                    val widthBasedTubeWidth = ((availableWidth - totalHorizontalSpacing) / maxInRow).coerceIn(36.dp, 72.dp)
                    val widthBasedBallSize = (widthBasedTubeWidth * 0.78f).coerceIn(28.dp, 56.dp)

                    // Available vertical height per row
                    val verticalGap = if (isTwoRows) 24.dp else 0.dp
                    val maxRowHeight = if (isTwoRows) (availableHeight - verticalGap - 16.dp) / 2 else availableHeight - 16.dp
                    // Each tube needs: tubeHeight (ballSize * 4 + 14dp) + lifted ball headroom (ballSize + 12dp)
                    // Total height per tube item = ballSize * 5 + 26dp
                    val heightBasedBallSize = ((maxRowHeight - 26.dp) / 5f).coerceIn(28.dp, 56.dp)

                    // Final unified ball and tube dimensions guaranteeing zero clipping
                    val finalBallSize = minOf(widthBasedBallSize, heightBasedBallSize)
                    val finalTubeWidth = (finalBallSize / 0.78f).coerceIn(36.dp, 72.dp)
                    val finalTubeHeight = finalBallSize * 4f + 14.dp

                    val row1Tubes = tubes.take(row1Count)
                    val row2Tubes = if (isTwoRows) tubes.drop(row1Count) else emptyList()

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // First Row of Tubes
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(spacing),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            for (tube in row1Tubes) {
                                val isSelected = (selectedTubeIndex == tube.id)
                                TubeView(
                                    tube = tube,
                                    isSelected = isSelected,
                                    showSymbols = isSymbolsEnabled,
                                    tubeWidth = finalTubeWidth,
                                    tubeHeight = finalTubeHeight,
                                    ballSize = finalBallSize,
                                    onClick = {
                                        viewModel.onTubeClicked(
                                            index = tube.id,
                                            onHapticFeedback = {
                                                if (selectedTubeIndex == null) {
                                                    vibrationManager.vibrateBallSelected()
                                                } else {
                                                    vibrationManager.vibrateBallPlaced()
                                                }
                                            },
                                            onInvalidHaptic = {
                                                vibrationManager.vibrateInvalidMove()
                                            }
                                        )
                                    }
                                )
                            }
                        }

                        if (isTwoRows) {
                            Spacer(modifier = Modifier.height(verticalGap))

                            // Second Row of Tubes
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(spacing),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                for (tube in row2Tubes) {
                                    val isSelected = (selectedTubeIndex == tube.id)
                                    TubeView(
                                        tube = tube,
                                        isSelected = isSelected,
                                        showSymbols = isSymbolsEnabled,
                                        tubeWidth = finalTubeWidth,
                                        tubeHeight = finalTubeHeight,
                                        ballSize = finalBallSize,
                                        onClick = {
                                            viewModel.onTubeClicked(
                                                index = tube.id,
                                                onHapticFeedback = {
                                                    if (selectedTubeIndex == null) {
                                                        vibrationManager.vibrateBallSelected()
                                                    } else {
                                                        vibrationManager.vibrateBallPlaced()
                                                    }
                                                },
                                                onInvalidHaptic = {
                                                    vibrationManager.vibrateInvalidMove()
                                                }
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Confetti and Win Celebration Overlay
            AnimatedVisibility(
                visible = isLevelWon,
                enter = fadeIn() + scaleIn(initialScale = 0.85f),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center
                ) {
                    ConfettiEffect()

                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .widthIn(max = 380.dp)
                            .shadow(16.dp, RoundedCornerShape(28.dp))
                            .testTag("win_dialog"),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier.size(80.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(78.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(Color(0xFFFFD700).copy(alpha = 0.2f))
                                )
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(66.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = stringResource(R.string.level_completed),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = stringResource(R.string.level_completed_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Score Summary Badge
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.moves_format, movesCount),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                val minutes = elapsedSeconds / 60
                                val seconds = elapsedSeconds % 60
                                Text(
                                    text = stringResource(R.string.time_format, minutes, seconds),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Next Level Button
                            Button(
                                onClick = {
                                    viewModel.loadLevel(currentLevel + 1)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("next_level_button"),
                                shape = RoundedCornerShape(26.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = stringResource(R.string.next_level),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = null)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Levels Selection Button
                            OutlinedButton(
                                onClick = onOpenLevels,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("win_levels_button"),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Icon(Icons.Default.GridView, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.levels),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
