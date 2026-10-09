package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ad.AdManager
import com.example.ad.BillingManager
import com.example.ad.UMPManager
import com.example.audio.SoundManager
import com.example.audio.VibrationManager
import com.example.data.GamePreferences
import com.example.data.LeaderboardManager
import com.example.data.LevelManager
import com.example.ui.GameViewModel
import com.example.ui.screens.GameScreen
import com.example.ui.screens.LevelSelectScreen
import com.example.ui.screens.MenuScreen
import com.example.ui.screens.SettingsDialog
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen {
    MENU,
    GAME,
    LEVEL_SELECT
}

class MainActivity : ComponentActivity() {

    private lateinit var preferences: GamePreferences
    private lateinit var levelManager: LevelManager
    private lateinit var leaderboardManager: LeaderboardManager
    private lateinit var soundManager: SoundManager
    private lateinit var vibrationManager: VibrationManager
    private lateinit var umpManager: UMPManager
    private lateinit var billingManager: BillingManager
    private lateinit var adManager: AdManager
    private lateinit var viewModel: GameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferences = GamePreferences(this)
        levelManager = LevelManager(this)
        leaderboardManager = LeaderboardManager(this)
        soundManager = SoundManager().apply {
            isSoundEnabled = preferences.soundEnabled.value
        }
        vibrationManager = VibrationManager(this).apply {
            isVibrationEnabled = preferences.vibrationEnabled.value
        }
        umpManager = UMPManager(this)
        billingManager = BillingManager(this, preferences).apply {
            startConnection()
        }
        adManager = AdManager(this, umpManager, billingManager)
        viewModel = GameViewModel(preferences, soundManager, levelManager, leaderboardManager)

        // Request UMP consent before initializing ads
        umpManager.gatherConsent(this) { canRequestAds ->
            if (canRequestAds) {
                adManager.initializeIfConsentGranted()
            }
        }

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppContent()
                }
            }
        }
    }

    @Composable
    private fun AppContent() {
        val maxUnlockedLevel by preferences.maxUnlockedLevel.collectAsStateWithLifecycle()
        val soundEnabled by preferences.soundEnabled.collectAsStateWithLifecycle()
        val vibrationEnabled by preferences.vibrationEnabled.collectAsStateWithLifecycle()
        val symbolsEnabled by preferences.colorblindSymbolsEnabled.collectAsStateWithLifecycle()
        val adsRemoved by billingManager.isAdsRemoved.collectAsStateWithLifecycle()
        val canRequestAds by umpManager.canRequestAdsState.collectAsStateWithLifecycle()
        val topRecords by leaderboardManager.topRecords.collectAsStateWithLifecycle(emptyList())

        // Sync sound & vibration settings
        LaunchedEffect(soundEnabled) {
            soundManager.isSoundEnabled = soundEnabled
        }
        LaunchedEffect(vibrationEnabled) {
            vibrationManager.isVibrationEnabled = vibrationEnabled
        }

        val currentLevel by viewModel.currentLevel.collectAsStateWithLifecycle()
        val tubes by viewModel.tubes.collectAsStateWithLifecycle()
        val selectedTubeIndex by viewModel.selectedTubeIndex.collectAsStateWithLifecycle()
        val remainingUndos by viewModel.remainingUndos.collectAsStateWithLifecycle()
        val bonusTubesCount by viewModel.bonusTubesCount.collectAsStateWithLifecycle()
        val movesCount by viewModel.movesCount.collectAsStateWithLifecycle()
        val elapsedSeconds by viewModel.elapsedSeconds.collectAsStateWithLifecycle()
        val isLevelWon by viewModel.isLevelWon.collectAsStateWithLifecycle()
        val shouldShowInterstitial by viewModel.shouldShowInterstitial.collectAsStateWithLifecycle()

        var currentScreen by remember { mutableStateOf(AppScreen.MENU) }
        var showSettingsDialog by remember { mutableStateOf(false) }

        // Trigger interstitial ad after every 4 completed levels
        LaunchedEffect(shouldShowInterstitial) {
            if (shouldShowInterstitial) {
                adManager.showInterstitialIfAllowed(this@MainActivity) {
                    viewModel.onInterstitialShown()
                }
            }
        }

        if (showSettingsDialog) {
            SettingsDialog(
                preferences = preferences,
                billingManager = billingManager,
                umpManager = umpManager,
                isSoundEnabled = soundEnabled,
                isVibrationEnabled = vibrationEnabled,
                isSymbolsEnabled = symbolsEnabled,
                isAdsRemoved = adsRemoved,
                onDismiss = { showSettingsDialog = false }
            )
        }

        when (currentScreen) {
            AppScreen.MENU -> {
                MenuScreen(
                    maxUnlockedLevel = maxUnlockedLevel,
                    isAdsRemoved = adsRemoved,
                    canRequestAds = canRequestAds,
                    topRecords = topRecords,
                    onPlayClicked = {
                        viewModel.loadLevel(maxUnlockedLevel)
                        currentScreen = AppScreen.GAME
                    },
                    onLevelsClicked = {
                        currentScreen = AppScreen.LEVEL_SELECT
                    },
                    onSettingsClicked = {
                        showSettingsDialog = true
                    }
                )
            }

            AppScreen.LEVEL_SELECT -> {
                LevelSelectScreen(
                    maxUnlockedLevel = maxUnlockedLevel,
                    onLevelSelected = { selectedLevel ->
                        viewModel.loadLevel(selectedLevel)
                        currentScreen = AppScreen.GAME
                    },
                    onBack = {
                        currentScreen = AppScreen.MENU
                    }
                )
            }

            AppScreen.GAME -> {
                GameScreen(
                    viewModel = viewModel,
                    vibrationManager = vibrationManager,
                    currentLevel = currentLevel,
                    tubes = tubes,
                    selectedTubeIndex = selectedTubeIndex,
                    remainingUndos = remainingUndos,
                    bonusTubesCount = bonusTubesCount,
                    movesCount = movesCount,
                    elapsedSeconds = elapsedSeconds,
                    isLevelWon = isLevelWon,
                    isSymbolsEnabled = symbolsEnabled,
                    onBack = {
                        currentScreen = AppScreen.MENU
                    },
                    onOpenSettings = {
                        showSettingsDialog = true
                    },
                    onRequestRewardedAd = { rewardType ->
                        adManager.showRewarded(
                            activity = this@MainActivity,
                            onRewardEarned = {
                                if (rewardType == "TUBE") {
                                    val added = viewModel.addBonusTube()
                                    if (added) {
                                        Toast.makeText(
                                            this@MainActivity,
                                            getString(R.string.tube_added_success),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    viewModel.addBonusUndos()
                                    Toast.makeText(
                                        this@MainActivity,
                                        getString(R.string.undos_added_success),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            onDismissed = {},
                            onFailedToLoad = {
                                Toast.makeText(
                                    this@MainActivity,
                                    getString(R.string.ad_not_ready),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    },
                    onOpenLevels = {
                        currentScreen = AppScreen.LEVEL_SELECT
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        billingManager.destroy()
    }
}
