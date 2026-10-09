package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.BallSortGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Ball Sort Puzzle", appName)
  }

  @Test
  fun `test level generation determinism and rules`() {
    val level1A = BallSortGenerator.generateLevel(1)
    val level1B = BallSortGenerator.generateLevel(1)

    assertEquals(level1A.size, level1B.size)
    assertEquals(5, level1A.size) // 3 colors + 2 tubes = 5 tubes

    // Total balls in level 1 must be 3 colors * 4 balls = 12 balls
    val totalBalls = level1A.sumOf { it.balls.size }
    assertEquals(12, totalBalls)

    // Level 10 should have 6 colors -> 8 tubes
    val level10 = BallSortGenerator.generateLevel(10)
    assertEquals(8, level10.size)
    assertEquals(24, level10.sumOf { it.balls.size })
  }

  @Test
  fun `test LevelManager procedural generation delegation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val levelManager = com.example.data.LevelManager(context)
    val level5 = levelManager.generateLevel(5)

    assertEquals(6, level5.size) // 4 colors + 2 tubes = 6 tubes
    assertEquals(4, levelManager.getColorsCount(5))
    assertTrue(levelManager.isLevelSolved(emptyList()))
  }
}

