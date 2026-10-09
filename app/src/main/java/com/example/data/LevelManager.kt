package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.Ball
import com.example.model.BallColor
import com.example.model.Tube
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import kotlin.random.Random

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "level_progress_datastore")

/**
 * LevelManager handles deterministic procedural level generation based on a level seed,
 * guaranteeing each generated puzzle is solvable by performing valid reverse moves
 * from the solved state. Also manages player progression using Jetpack DataStore.
 */
class LevelManager(private val context: Context) {

    companion object {
        private val KEY_CURRENT_LEVEL = intPreferencesKey("current_level")
        private val KEY_MAX_UNLOCKED_LEVEL = intPreferencesKey("max_unlocked_level")
        private val KEY_COMPLETED_LEVELS_COUNT = intPreferencesKey("completed_levels_count")

        private const val TUBE_CAPACITY = 4
    }

    // ========================================================================
    // 1. DETERMINISTIC PROCEDURAL GENERATION (REVERSE-MOVES FROM SOLVED STATE)
    // ========================================================================

    /**
     * Determines the number of unique colors for a given level (scales from 3 to 12).
     */
    fun getColorsCount(level: Int): Int {
        return when {
            level <= 2 -> 3
            level <= 5 -> 4
            level <= 9 -> 5
            level <= 14 -> 6
            level <= 20 -> 7
            level <= 21 -> 8
            level <= 28 -> 9
            level <= 36 -> 10
            level <= 45 -> 11
            else -> 12
        }
    }

    /**
     * Generates a guaranteed-solvable level procedurally and deterministically.
     * Starts from the SOLVED state and performs reverse moves so no solver is required.
     *
     * @param level The level number used as the deterministic generator seed.
     * @return List of tubes representing the scrambled, 100% solvable puzzle board.
     */
    fun generateLevel(level: Int): List<Tube> {
        val numColors = getColorsCount(level)
        val numTubes = numColors + 2

        // Deterministic PRNG seed based on the level number
        val seed = (level.toLong() * 2147483647L xor 0x5DEECE66DL) + 1234567L
        val random = Random(seed)

        var ballIdCounter = 1L
        val availableColors = BallColor.entries.take(numColors)

        // Step 1: Initialize tubes in the SOLVED state
        val initialTubes = MutableList(numTubes) { tubeIndex ->
            if (tubeIndex < numColors) {
                val color = availableColors[tubeIndex]
                val balls = List(TUBE_CAPACITY) {
                    Ball(id = ballIdCounter++, color = color)
                }
                Tube(id = tubeIndex, capacity = TUBE_CAPACITY, balls = balls)
            } else {
                Tube(id = tubeIndex, capacity = TUBE_CAPACITY, balls = emptyList())
            }
        }

        // Step 2: Determine number of reverse scramble steps based on level
        val baseMoves = 16 + (level * 2)
        val scrambleMoves = baseMoves.coerceIn(16, 75)

        var lastFrom = -1
        var lastTo = -1

        // Step 3: Apply valid reverse moves
        for (step in 0 until scrambleMoves) {
            // An eligible source tube for a reverse move has either:
            // - exactly 1 ball (which was placed into an empty tube in forward play)
            // - OR the ball right below top has the SAME color (valid forward stacking)
            val eligibleSources = initialTubes.indices.filter { idx ->
                val tube = initialTubes[idx]
                if (tube.isEmpty) false
                else {
                    tube.balls.size == 1 || tube.balls[tube.balls.size - 2].color == tube.topColor
                }
            }

            if (eligibleSources.isEmpty()) break

            val shuffledSources = eligibleSources.shuffled(random)
            var moved = false

            for (srcIdx in shuffledSources) {
                // Eligible destination tube: has capacity and avoids immediate undo ping-pong
                val eligibleDestinations = initialTubes.indices.filter { dstIdx ->
                    dstIdx != srcIdx &&
                            !initialTubes[dstIdx].isFull &&
                            !(srcIdx == lastTo && dstIdx == lastFrom)
                }

                if (eligibleDestinations.isNotEmpty()) {
                    val dstIdx = eligibleDestinations.random(random)
                    val (updatedSrc, poppedBall) = initialTubes[srcIdx].popBall()
                    val updatedDst = initialTubes[dstIdx].pushBall(poppedBall)

                    initialTubes[srcIdx] = updatedSrc
                    initialTubes[dstIdx] = updatedDst

                    lastFrom = srcIdx
                    lastTo = dstIdx
                    moved = true
                    break
                }
            }

            // Fallback: relax undo ping-pong prevention if stuck
            if (!moved) {
                for (srcIdx in shuffledSources) {
                    val eligibleDestinations = initialTubes.indices.filter { dstIdx ->
                        dstIdx != srcIdx && !initialTubes[dstIdx].isFull
                    }
                    if (eligibleDestinations.isNotEmpty()) {
                        val dstIdx = eligibleDestinations.random(random)
                        val (updatedSrc, poppedBall) = initialTubes[srcIdx].popBall()
                        val updatedDst = initialTubes[dstIdx].pushBall(poppedBall)

                        initialTubes[srcIdx] = updatedSrc
                        initialTubes[dstIdx] = updatedDst

                        lastFrom = srcIdx
                        lastTo = dstIdx
                        break
                    }
                }
            }
        }

        return initialTubes.mapIndexed { index, tube -> tube.copy(id = index) }
    }

    /**
     * Checks if all tubes on the board are solved (either full single-color or empty).
     */
    fun isLevelSolved(tubes: List<Tube>): Boolean {
        return tubes.all { tube ->
            tube.isEmpty || tube.isCompleted
        }
    }

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
