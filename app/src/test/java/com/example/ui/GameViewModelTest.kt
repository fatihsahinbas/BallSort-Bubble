package com.example.ui

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.SoundManager
import com.example.data.GamePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GameViewModelTest {

    private lateinit var viewModel: GameViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        val context = ApplicationProvider.getApplicationContext<Context>()
        viewModel = GameViewModel(GamePreferences(context), SoundManager().apply { isSoundEnabled = false })
        viewModel.loadLevel(1)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeAnyValidMove() {
        val tubes = viewModel.tubes.value
        for (from in tubes.indices) for (to in tubes.indices) {
            if (!tubes[from].isCompleted && viewModel.isValidMove(from, to)) {
                viewModel.onTubeClicked(from)
                viewModel.onTubeClicked(to)
                return
            }
        }
        error("no valid move")
    }

    @Test
    fun `undo after adding bonus tube keeps the bonus tube`() {
        val baseSize = viewModel.tubes.value.size
        makeAnyValidMove()
        assertTrue(viewModel.addBonusTube())
        assertEquals(baseSize + 1, viewModel.tubes.value.size)

        assertTrue(viewModel.undoMove())
        assertEquals(baseSize + 1, viewModel.tubes.value.size)
        assertEquals(viewModel.tubes.value.indices.toList(), viewModel.tubes.value.map { it.id })
        assertEquals(1, viewModel.bonusTubesCount.value)
    }

    @Test
    fun `undo restores previous board and consumes one undo`() {
        val before = viewModel.tubes.value
        makeAnyValidMove()
        assertEquals(1, viewModel.movesCount.value)
        assertTrue(viewModel.undoMove())
        assertEquals(before, viewModel.tubes.value)
        assertEquals(4, viewModel.remainingUndos.value)
    }
}
