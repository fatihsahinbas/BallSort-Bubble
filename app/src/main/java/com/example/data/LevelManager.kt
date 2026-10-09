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
 * LevelManager handles procedural level generation, tracking current level progress,
 * and saving/loading the player's level status using Jetpack DataStore Preferences.
 */
class LevelManager(private val context: Context) {

    companion object {
        private val KEY_CURRENT_LEVEL = intPreferencesKey("current_level")
        private val KEY_MAX_UNLOCKED_LEVEL = intPreferencesKey("max_unlocked_level")
        private val KEY_COMPLETED_LEVELS_COUNT = intPreferencesKey("completed_levels_count")
    }

    /**
     * Flow observing the highest level unlocked by the player (defaults to 1).
     */
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

    /**
     * Flow observing the last played level (defaults to 1).
     */
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

    /**
     * Flow observing total completed levels count.
     */
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

    /**
     * Generates a guaranteed-solvable level procedurally based on deterministic seed.
     */
    fun generateLevel(level: Int): List<Tube> {
        return BallSortGenerator.generateLevel(level)
    }

    /**
     * Checks if the tubes configuration is solved.
     */
    fun isLevelSolved(tubes: List<Tube>): Boolean {
        return BallSortGenerator.isBoardSolved(tubes)
    }

    /**
     * Retrieves the number of distinct ball colors for a level.
     */
    fun getColorsCount(level: Int): Int {
        return BallSortGenerator.getColorsCountForLevel(level)
    }

    /**
     * Updates and saves the current level the player is playing.
     */
    suspend fun saveCurrentLevel(level: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_CURRENT_LEVEL] = level
        }
    }

    /**
     * Unlocks a new level if it exceeds the current highest unlocked level.
     */
    suspend fun unlockLevel(level: Int) {
        context.dataStore.edit { preferences ->
            val currentMax = preferences[KEY_MAX_UNLOCKED_LEVEL] ?: 1
            if (level > currentMax) {
                preferences[KEY_MAX_UNLOCKED_LEVEL] = level
            }
        }
    }

    /**
     * Increments the total completed levels count and returns the updated count.
     */
    suspend fun incrementCompletedLevels(): Int {
        var updatedCount = 1
        context.dataStore.edit { preferences ->
            val current = preferences[KEY_COMPLETED_LEVELS_COUNT] ?: 0
            updatedCount = current + 1
            preferences[KEY_COMPLETED_LEVELS_COUNT] = updatedCount
        }
        return updatedCount
    }

    /**
     * Synchronously/suspendingly fetches current max unlocked level.
     */
    suspend fun getMaxUnlockedLevel(): Int {
        return maxUnlockedLevel.first()
    }

    /**
     * Synchronously/suspendingly fetches current active level.
     */
    suspend fun getCurrentLevel(): Int {
        return currentLevel.first()
    }

    /**
     * Resets level progress (e.g. for debug or full data reset).
     */
    suspend fun resetProgress() {
        context.dataStore.edit { preferences ->
            preferences[KEY_CURRENT_LEVEL] = 1
            preferences[KEY_MAX_UNLOCKED_LEVEL] = 1
            preferences[KEY_COMPLETED_LEVELS_COUNT] = 0
        }
    }
}
