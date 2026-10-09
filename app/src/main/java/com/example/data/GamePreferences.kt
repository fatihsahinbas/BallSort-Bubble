package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GamePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ball_sort_prefs", Context.MODE_PRIVATE)

    private val _maxUnlockedLevel = MutableStateFlow(prefs.getInt(KEY_MAX_UNLOCKED_LEVEL, 1))
    val maxUnlockedLevel: StateFlow<Int> = _maxUnlockedLevel.asStateFlow()

    private val _soundEnabled = MutableStateFlow(prefs.getBoolean(KEY_SOUND_ENABLED, true))
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(prefs.getBoolean(KEY_VIBRATION_ENABLED, true))
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    private val _colorblindSymbolsEnabled =
        MutableStateFlow(prefs.getBoolean(KEY_COLORBLIND_SYMBOLS, true))
    val colorblindSymbolsEnabled: StateFlow<Boolean> = _colorblindSymbolsEnabled.asStateFlow()

    private val _adsRemoved = MutableStateFlow(prefs.getBoolean(KEY_ADS_REMOVED, false))
    val adsRemoved: StateFlow<Boolean> = _adsRemoved.asStateFlow()

    private val _completedLevelsCount =
        MutableStateFlow(prefs.getInt(KEY_COMPLETED_LEVELS_COUNT, 0))
    val completedLevelsCount: StateFlow<Int> = _completedLevelsCount.asStateFlow()

    fun unlockLevel(level: Int) {
        if (level > _maxUnlockedLevel.value) {
            _maxUnlockedLevel.value = level
            prefs.edit().putInt(KEY_MAX_UNLOCKED_LEVEL, level).apply()
        }
    }

    fun incrementCompletedLevels(): Int {
        val next = _completedLevelsCount.value + 1
        _completedLevelsCount.value = next
        prefs.edit().putInt(KEY_COMPLETED_LEVELS_COUNT, next).apply()
        return next
    }

    fun setSoundEnabled(enabled: Boolean) {
        _soundEnabled.value = enabled
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }

    fun setVibrationEnabled(enabled: Boolean) {
        _vibrationEnabled.value = enabled
        prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply()
    }

    fun setColorblindSymbolsEnabled(enabled: Boolean) {
        _colorblindSymbolsEnabled.value = enabled
        prefs.edit().putBoolean(KEY_COLORBLIND_SYMBOLS, enabled).apply()
    }

    fun setAdsRemoved(removed: Boolean) {
        _adsRemoved.value = removed
        prefs.edit().putBoolean(KEY_ADS_REMOVED, removed).apply()
    }

    companion object {
        private const val KEY_MAX_UNLOCKED_LEVEL = "key_max_unlocked_level"
        private const val KEY_SOUND_ENABLED = "key_sound_enabled"
        private const val KEY_VIBRATION_ENABLED = "key_vibration_enabled"
        private const val KEY_COLORBLIND_SYMBOLS = "key_colorblind_symbols"
        private const val KEY_ADS_REMOVED = "key_ads_removed"
        private const val KEY_COMPLETED_LEVELS_COUNT = "key_completed_levels_count"
    }
}
