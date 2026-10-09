package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

private val Context.leaderboardDataStore: DataStore<Preferences> by preferencesDataStore(name = "leaderboard_datastore")

/**
 * Represents a player's best completion record for a specific level.
 */
data class LevelRecord(
    val level: Int,
    val moves: Int,
    val timeSeconds: Int,
    val timestamp: Long
)

/**
 * LeaderboardManager handles local-only persistence of completed level performance
 * using Jetpack DataStore Preferences. Tracks minimum moves and fastest completion time.
 */
class LeaderboardManager(private val context: Context) {

    companion object {
        private val KEY_LEADERBOARD_JSON = stringPreferencesKey("leaderboard_records_json")
    }

    /**
     * Flow emitting the top 5 records across completed levels.
     * Ranked by highest level, then minimum move count, then fastest completion time.
     */
    val topRecords: Flow<List<LevelRecord>> = context.leaderboardDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val jsonString = preferences[KEY_LEADERBOARD_JSON] ?: "[]"
            parseRecords(jsonString)
                .sortedWith(
                    compareByDescending<LevelRecord> { it.level }
                        .thenBy { it.moves }
                        .thenBy { it.timeSeconds }
                )
                .take(5)
        }

    /**
     * Flow emitting all saved level records.
     */
    val allRecords: Flow<List<LevelRecord>> = context.leaderboardDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val jsonString = preferences[KEY_LEADERBOARD_JSON] ?: "[]"
            parseRecords(jsonString)
        }

    /**
     * Records level completion. Updates the level's record if it's the first completion,
     * or if the player achieved fewer moves, or equal moves in less time.
     */
    suspend fun recordLevelCompletion(level: Int, moves: Int, timeSeconds: Int) {
        context.leaderboardDataStore.edit { preferences ->
            val jsonString = preferences[KEY_LEADERBOARD_JSON] ?: "[]"
            val records = parseRecords(jsonString).toMutableList()

            val existingIndex = records.indexOfFirst { it.level == level }
            val now = System.currentTimeMillis()

            if (existingIndex >= 0) {
                val existing = records[existingIndex]
                val isBetter = moves < existing.moves ||
                        (moves == existing.moves && timeSeconds < existing.timeSeconds)

                if (isBetter) {
                    records[existingIndex] = LevelRecord(
                        level = level,
                        moves = moves,
                        timeSeconds = timeSeconds,
                        timestamp = now
                    )
                }
            } else {
                records.add(
                    LevelRecord(
                        level = level,
                        moves = moves,
                        timeSeconds = timeSeconds,
                        timestamp = now
                    )
                )
            }

            preferences[KEY_LEADERBOARD_JSON] = serializeRecords(records)
        }
    }

    private fun parseRecords(jsonString: String): List<LevelRecord> {
        return try {
            val array = JSONArray(jsonString)
            val list = mutableListOf<LevelRecord>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    LevelRecord(
                        level = obj.getInt("level"),
                        moves = obj.getInt("moves"),
                        timeSeconds = obj.optInt("timeSeconds", 0),
                        timestamp = obj.optLong("timestamp", 0L)
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun serializeRecords(records: List<LevelRecord>): String {
        val array = JSONArray()
        for (record in records) {
            val obj = JSONObject().apply {
                put("level", record.level)
                put("moves", record.moves)
                put("timeSeconds", record.timeSeconds)
                put("timestamp", record.timestamp)
            }
            array.put(obj)
        }
        return array.toString()
    }
}
