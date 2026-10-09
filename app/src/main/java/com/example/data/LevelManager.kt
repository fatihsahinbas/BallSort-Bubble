package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.BallSortGenerator
import com.example.model.Tube
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "level_progress_datastore")

/**
 * Legacy wrapper. Level generation lives in [BallSortGenerator] (single source of truth) and
 * progress is persisted by [GamePreferences]; this DataStore copy is no longer written by the
 * game and can be deleted.
 */
class LevelManager(private val context: Context) {

    companion object {
        private val KEY_CURRENT_LEVEL = intPreferencesKey("current_level")
        private val KEY_MAX_UNLOCKED_LEVEL = intPreferencesKey("max_unlocked_level")
        private val KEY_COMPLETED_LEVELS_COUNT = intPreferencesKey("completed_levels_count")
    }

    // ========================================================================
    // 1. DETERMINISTIC PROCEDURAL GENERATION (REVERSE-MOVES FROM SOLVED STATE)
    // ========================================================================

    fun getColorsCount(level: Int): Int = BallSortGenerator.getColorsCountForLevel(level)

    fun generateLevel(level: Int): List<Tube> = BallSortGenerator.generateLevel(level)

    fun isLevelSolved(tubes: List<Tube>): Boolean = BallSortGenerator.isBoardSolved(tubes)

    // ========================================================================
    // 2. DATASTORE PLAYER PROGRESS TRACKING
    // ========================================================================

    val maxUnlockedLevel: Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_MAX_UNLOCKED_LEVEL] ?: 1
        }

    val currentLevel: Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_CURRENT_LEVEL] ?: 1
        }

    val completedLevelsCount: Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_COMPLETED_LEVELS_COUNT] ?: 0
        }

    suspend fun saveCurrentLevel(level: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_CURRENT_LEVEL] = level
        }
    }

    suspend fun unlockLevel(level: Int) {
        context.dataStore.edit { preferences ->
            val currentMax = preferences[KEY_MAX_UNLOCKED_LEVEL] ?: 1
            if (level > currentMax) {
                preferences[KEY_MAX_UNLOCKED_LEVEL] = level
            }
        }
    }

    suspend fun incrementCompletedLevels(): Int {
        var updatedCount = 1
        context.dataStore.edit { preferences ->
            val current = preferences[KEY_COMPLETED_LEVELS_COUNT] ?: 0
            updatedCount = current + 1
            preferences[KEY_COMPLETED_LEVELS_COUNT] = updatedCount
        }
        return updatedCount
    }

    suspend fun getMaxUnlockedLevel(): Int = maxUnlockedLevel.first()

    suspend fun getCurrentLevel(): Int = currentLevel.first()

    suspend fun resetProgress() {
        context.dataStore.edit { preferences ->
            preferences[KEY_CURRENT_LEVEL] = 1
            preferences[KEY_MAX_UNLOCKED_LEVEL] = 1
            preferences[KEY_COMPLETED_LEVELS_COUNT] = 0
        }
    }
}
