package com.example.model

import kotlin.random.Random

object BallSortGenerator {

    /**
     * Determines the number of colors for a given level.
     * Starts at 3 and scales up to 12.
     */
    fun getColorsCountForLevel(level: Int): Int {
        return when {
            level <= 2 -> 3
            level <= 5 -> 4
            level <= 9 -> 5
            level <= 14 -> 6
            level <= 20 -> 7
            level <= 27 -> 8
            level <= 35 -> 9
            level <= 44 -> 10
            level <= 54 -> 11
            else -> 12
        }
    }

    /**
     * Generates a guaranteed-solvable level deterministically using reverse moves from the solved state.
     */
    fun generateLevel(level: Int): List<Tube> {
        val numColors = getColorsCountForLevel(level)
        val numTubes = numColors + 2
        val capacity = 4

        // Seed determinism: reproducible for any level number
        val seed = (level.toLong() * 2147483647L xor 0x5DEECE66DL) + 1234567L
        val random = Random(seed)

        var ballId = 1L
        val availableColors = BallColor.entries.take(numColors)

        // 1. Initialize tubes in SOLVED state
        val initialTubes = MutableList(numTubes) { tubeIndex ->
            if (tubeIndex < numColors) {
                val color = availableColors[tubeIndex]
                val balls = List(capacity) {
                    Ball(id = ballId++, color = color)
                }
                Tube(id = tubeIndex, capacity = capacity, balls = balls)
            } else {
                Tube(id = tubeIndex, capacity = capacity, balls = emptyList())
            }
        }

        // 2. Determine number of scramble steps based on level
        val baseMoves = 16 + (level * 2)
        val scrambleMoves = baseMoves.coerceIn(16, 75)

        var lastFrom = -1
        var lastTo = -1

        for (step in 0 until scrambleMoves) {
            // Find all eligible source tubes for reverse move:
            // A reverse source tube must have top ball that can be legally detached.
            // That means either the tube has only 1 ball, OR the ball right below top has the SAME color.
            val eligibleSources = initialTubes.indices.filter { idx ->
                val tube = initialTubes[idx]
                if (tube.isEmpty) false
                else {
                    tube.balls.size == 1 || tube.balls[tube.balls.size - 2].color == tube.topColor
                }
            }

            if (eligibleSources.isEmpty()) break

            // Shuffle sources for randomness
            val shuffledSources = eligibleSources.shuffled(random)
            var moved = false

            for (srcIdx in shuffledSources) {
                // Find eligible destination tubes (must have room, and not be srcIdx)
                // Also avoid immediately undoing the previous step (e.g., if previous was A -> B, avoid B -> A)
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

            if (!moved) {
                // Fallback: relax the undo restriction
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

        // Return the scrambled board
        return initialTubes.mapIndexed { index, tube -> tube.copy(id = index) }
    }

    /**
     * Checks if the entire board is currently solved.
     */
    fun isBoardSolved(tubes: List<Tube>): Boolean {
        return tubes.all { tube ->
            tube.isEmpty || tube.isCompleted
        }
    }
}
