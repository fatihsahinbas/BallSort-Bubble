package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ad.AdConfig
import com.example.audio.SoundManager
import com.example.data.GamePreferences
import com.example.data.LevelManager
import com.example.model.BallSortGenerator
import com.example.model.MoveHistory
import com.example.model.Tube
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel(
    private val preferences: GamePreferences,
    private val soundManager: SoundManager,
    private val levelManager: LevelManager? = null
) : ViewModel() {

    private val _currentLevel = MutableStateFlow(preferences.maxUnlockedLevel.value)
    val currentLevel: StateFlow<Int> = _currentLevel.asStateFlow()

    private val _tubes = MutableStateFlow<List<Tube>>(emptyList())
    val tubes: StateFlow<List<Tube>> = _tubes.asStateFlow()

    private val _selectedTubeIndex = MutableStateFlow<Int?>(null)
    val selectedTubeIndex: StateFlow<Int?> = _selectedTubeIndex.asStateFlow()

    private val _remainingUndos = MutableStateFlow(5)
    val remainingUndos: StateFlow<Int> = _remainingUndos.asStateFlow()

    private val _bonusTubesCount = MutableStateFlow(0)
    val bonusTubesCount: StateFlow<Int> = _bonusTubesCount.asStateFlow()

    private val _isLevelWon = MutableStateFlow(false)
    val isLevelWon: StateFlow<Boolean> = _isLevelWon.asStateFlow()

    private val _shouldShowInterstitial = MutableStateFlow(false)
    val shouldShowInterstitial: StateFlow<Boolean> = _shouldShowInterstitial.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val undoStack = mutableListOf<MoveHistory>()
    private var initialTubesForLevel: List<Tube> = emptyList()

    init {
        loadLevel(_currentLevel.value)
    }

    fun loadLevel(level: Int) {
        _currentLevel.value = level
        val generated = levelManager?.generateLevel(level) ?: BallSortGenerator.generateLevel(level)
        initialTubesForLevel = generated
        _tubes.value = generated
        _selectedTubeIndex.value = null
        _remainingUndos.value = 5
        _bonusTubesCount.value = 0
        _isLevelWon.value = false
        _shouldShowInterstitial.value = false
        undoStack.clear()

        viewModelScope.launch {
            levelManager?.saveCurrentLevel(level)
        }
    }

    fun restartLevel() {
        _tubes.value = initialTubesForLevel
        _selectedTubeIndex.value = null
        _remainingUndos.value = 5
        _bonusTubesCount.value = 0
        _isLevelWon.value = false
        undoStack.clear()
        soundManager.playButtonClick()
    }

    fun onTubeClicked(index: Int, onHapticFeedback: () -> Unit, onInvalidHaptic: () -> Unit) {
        if (_isLevelWon.value) return
        val currentTubes = _tubes.value
        if (index !in currentTubes.indices) return

        val selectedIdx = _selectedTubeIndex.value

        if (selectedIdx == null) {
            // First tap: Select source tube
            val tube = currentTubes[index]
            if (tube.isEmpty) {
                // Empty tube cannot be source
                return
            }
            if (tube.isCompleted) {
                // Already completed tube doesn't need to be moved
                return
            }
            _selectedTubeIndex.value = index
            soundManager.playBallSelect()
            onHapticFeedback()
        } else if (selectedIdx == index) {
            // Tapped same tube: deselect
            _selectedTubeIndex.value = null
            soundManager.playBallDrop()
            onHapticFeedback()
        } else {
            // Attempt move from selectedIdx to index
            val srcTube = currentTubes[selectedIdx]
            val dstTube = currentTubes[index]

            val ballToMove = srcTube.topBall
            if (ballToMove != null && dstTube.canAccept(ballToMove)) {
                // Save undo snapshot
                undoStack.add(
                    MoveHistory(
                        tubes = currentTubes,
                        fromTubeIndex = selectedIdx,
                        toTubeIndex = index
                    )
                )

                // Execute move
                val (updatedSrc, poppedBall) = srcTube.popBall()
                val updatedDst = dstTube.pushBall(poppedBall)

                val newTubes = currentTubes.toMutableList()
                newTubes[selectedIdx] = updatedSrc
                newTubes[index] = updatedDst

                _tubes.value = newTubes
                _selectedTubeIndex.value = null
                soundManager.playBallDrop()
                onHapticFeedback()

                // Check win condition
                if (BallSortGenerator.isBoardSolved(newTubes)) {
                    handleLevelWin()
                }
            } else {
                // Invalid move
                soundManager.playInvalidMove()
                onInvalidHaptic()
                _selectedTubeIndex.value = null
            }
        }
    }

    private fun handleLevelWin() {
        _isLevelWon.value = true
        soundManager.playWin()

        val nextLevel = _currentLevel.value + 1
        preferences.unlockLevel(nextLevel)

        val totalCompleted = preferences.incrementCompletedLevels()
        if (totalCompleted % AdConfig.INTERSTITIAL_LEVEL_INTERVAL == 0) {
            _shouldShowInterstitial.value = true
        }

        viewModelScope.launch {
            levelManager?.unlockLevel(nextLevel)
            levelManager?.incrementCompletedLevels()
        }
    }

    fun onInterstitialShown() {
        _shouldShowInterstitial.value = false
    }

    fun undoMove(onHapticFeedback: () -> Unit): Boolean {
        if (_isLevelWon.value) return false
        if (_remainingUndos.value <= 0) {
            soundManager.playInvalidMove()
            return false
        }
        if (undoStack.isEmpty()) {
            soundManager.playInvalidMove()
            return false
        }

        val lastMove = undoStack.removeAt(undoStack.lastIndex)
        _tubes.value = lastMove.tubes
        _selectedTubeIndex.value = null
        _remainingUndos.value -= 1
        soundManager.playBallDrop()
        onHapticFeedback()
        return true
    }

    fun addBonusUndos() {
        _remainingUndos.value += 3
        soundManager.playButtonClick()
    }

    fun addBonusTube(): Boolean {
        if (_bonusTubesCount.value >= 2) return false
        val currentTubes = _tubes.value
        val newTubeId = currentTubes.size
        val newTube = Tube(id = newTubeId, capacity = 4, balls = emptyList())
        _tubes.value = currentTubes + newTube
        _bonusTubesCount.value += 1
        soundManager.playButtonClick()
        return true
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }
}
