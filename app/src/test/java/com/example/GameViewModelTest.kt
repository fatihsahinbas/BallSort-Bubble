package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.SoundManager
import com.example.data.GamePreferences
import com.example.data.LeaderboardManager
import com.example.model.Ball
import com.example.model.BallColor
import com.example.model.Tube
import com.example.ui.GameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GameViewModelTest {

    private lateinit var context: Context
    private lateinit var preferences: GamePreferences
    private lateinit var soundManager: SoundManager
    private lateinit var leaderboardManager: LeaderboardManager
    private lateinit var viewModel: GameViewModel

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        preferences = GamePreferences(context)
        soundManager = SoundManager().apply { isSoundEnabled = false }
        leaderboardManager = LeaderboardManager(context)
        viewModel = GameViewModel(preferences, soundManager, leaderboardManager)
    }

    @Test
    fun testInitialState() {
        assertNotNull(viewModel.tubes.value)
        assertTrue(viewModel.tubes.value.isNotEmpty())
        assertNull(viewModel.selectedTubeIndex.value)
        assertEquals(5, viewModel.remainingUndos.value)
        assertEquals(0, viewModel.movesCount.value)
        assertFalse(viewModel.isLevelWon.value)
    }

    @Test
    fun testSelectAndDeselectTube() {
        // Find a non-empty, non-completed tube to select
        val nonEmptyTubeIndex = viewModel.tubes.value.indexOfFirst { !it.isEmpty && !it.isCompleted }
        assertTrue(nonEmptyTubeIndex >= 0)

        // First click: select
        viewModel.onTubeClicked(nonEmptyTubeIndex)
        assertEquals(nonEmptyTubeIndex, viewModel.selectedTubeIndex.value)

        // Second click on same tube: deselect
        viewModel.onTubeClicked(nonEmptyTubeIndex)
        assertNull(viewModel.selectedTubeIndex.value)
    }

    @Test
    fun testMoveValidationAndUndo() {
        // Find a source tube with balls and an empty target tube
        val srcIndex = viewModel.tubes.value.indexOfFirst { !it.isEmpty && !it.isCompleted }
        val emptyIndex = viewModel.tubes.value.indexOfFirst { it.isEmpty }
        assertTrue(srcIndex >= 0)
        assertTrue(emptyIndex >= 0)

        val initialSrcSize = viewModel.tubes.value[srcIndex].balls.size
        val initialEmptySize = viewModel.tubes.value[emptyIndex].balls.size

        // Select source tube
        viewModel.onTubeClicked(srcIndex)
        assertEquals(srcIndex, viewModel.selectedTubeIndex.value)

        // Click empty destination tube -> execute valid move
        viewModel.onTubeClicked(emptyIndex)
        assertNull(viewModel.selectedTubeIndex.value)
        assertEquals(initialSrcSize - 1, viewModel.tubes.value[srcIndex].balls.size)
        assertEquals(initialEmptySize + 1, viewModel.tubes.value[emptyIndex].balls.size)
        assertEquals(1, viewModel.movesCount.value)
        assertTrue(viewModel.canUndo)

        // Execute Undo
        val undoResult = viewModel.undoMove()
        assertTrue(undoResult)
        assertEquals(4, viewModel.remainingUndos.value)
        assertEquals(initialSrcSize, viewModel.tubes.value[srcIndex].balls.size)
        assertEquals(initialEmptySize, viewModel.tubes.value[emptyIndex].balls.size)
    }

    @Test
    fun testInvalidMoveBetweenMismatchedColors() {
        // Test move validation rule: cannot place on different color ball
        val tube1 = Tube(
            id = 0,
            capacity = 4,
            balls = listOf(Ball(1L, BallColor.RUBY_RED))
        )
        val tube2 = Tube(
            id = 1,
            capacity = 4,
            balls = listOf(Ball(2L, BallColor.SKY_BLUE))
        )
        assertFalse(tube2.canAccept(tube1.topBall!!))
    }
}
