package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ad.AdConfig
import com.example.audio.SoundManager
import com.example.data.GamePreferences
import com.example.data.LeaderboardManager
import com.example.model.BallSortGenerator
import com.example.model.MoveHistory
import com.example.model.Tube
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * GameViewModel manages the complete game state:
 * - Current tube arrangements (tubes StateFlow)
 * - Selected tube index for player interactions (selectedTubeIndex StateFlow)
 * - Move validation logic adhering to Ball Sort rules
 * - Undo history tracking and state restoration
 * - Win condition detection and level progress synchronization
 * - Moves counter and elapsed time tracking recorded into local LeaderboardManager
 */
class GameViewModel(
    private val preferences: GamePreferences,
    private val soundManager: SoundManager,
    private val leaderboardManager: LeaderboardManager? = null
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

    private val _movesCount = MutableStateFlow(0)
    val movesCount: StateFlow<Int> = _movesCount.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds.asStateFlow()

    private val _isLevelWon = MutableStateFlow(false)
    val isLevelWon: StateFlow<Boolean> = _isLevelWon.asStateFlow()

    private val _shouldShowInterstitial = MutableStateFlow(false)
    val shouldShowInterstitial: StateFlow<Boolean> = _shouldShowInterstitial.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Undo move history stack
    private val undoStack = mutableListOf<MoveHistory>()
    private var initialTubesForLevel: List<Tube> = emptyList()
    private var timerJob: Job? = null

    val canUndo: Boolean
        get() = _remainingUndos.value > 0 && undoStack.isNotEmpty()

    init {
        loadLevel(_currentLevel.value)
    }

    /**
     * Loads and initializes a level board.
     */
    fun loadLevel(level: Int) {
        _currentLevel.value = level
        val generated = BallSortGenerator.generateLevel(level)
        initialTubesForLevel = generated
        _tubes.value = generated
        _selectedTubeIndex.value = null
        _remainingUndos.value = 5
        _bonusTubesCount.value = 0
        _movesCount.value = 0
        _elapsedSeconds.value = 0
        _isLevelWon.value = false
        _shouldShowInterstitial.value = false
        undoStack.clear()

        startTimer()
    }

    /**
     * Restarts the current level to its starting arrangement.
     */
    fun restartLevel() {
        _tubes.value = initialTubesForLevel
        _selectedTubeIndex.value = null
        _remainingUndos.value = 5
        _bonusTubesCount.value = 0
        _movesCount.value = 0
        _elapsedSeconds.value = 0
        _isLevelWon.value = false
        undoStack.clear()

        startTimer()
        soundManager.playButtonClick()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (!_isLevelWon.value) {
                delay(1000)
                _elapsedSeconds.value += 1
            }
        }
    }

    /**
     * Checks if moving a ball from fromIndex to toIndex is legal according to game rules:
     * - Destination tube must not be full
     * - Destination tube must be either empty OR its top ball must have the same color
     */
    fun isValidMove(fromIndex: Int, toIndex: Int): Boolean {
        val currentTubes = _tubes.value
        if (fromIndex !in currentTubes.indices || toIndex !in currentTubes.indices) return false
        if (fromIndex == toIndex) return false
        val src = currentTubes[fromIndex]
        val dst = currentTubes[toIndex]
        val ball = src.topBall ?: return false
        return dst.canAccept(ball)
    }

    /**
     * Handles tube click events with selection and transfer validation.
     */
    fun onTubeClicked(
        index: Int,
        onHapticFeedback: () -> Unit = {},
        onInvalidHaptic: () -> Unit = {}
    ) {
        if (_isLevelWon.value) return
        val currentTubes = _tubes.value
        if (index !in currentTubes.indices) return

        val selectedIdx = _selectedTubeIndex.value

        if (selectedIdx == null) {
            // First tap: Select source tube
            val tube = currentTubes[index]
            if (tube.isEmpty || tube.isCompleted) return

            _selectedTubeIndex.value = index
            soundManager.playBallSelect()
            onHapticFeedback()
        } else if (selectedIdx == index) {
            // Tapped same tube: deselect and drop ball back
            _selectedTubeIndex.value = null
            soundManager.playBallDrop()
            onHapticFeedback()
        } else {
            // Second tap on different tube: Attempt move from selectedIdx to index
            val srcTube = currentTubes[selectedIdx]
            val dstTube = currentTubes[index]
            val ballToMove = srcTube.topBall

            if (ballToMove != null && dstTube.canAccept(ballToMove)) {
                // Save undo snapshot before executing move
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
                _movesCount.value += 1
                soundManager.playBallDrop()
                onHapticFeedback()

                // Check win condition
                if (BallSortGenerator.isBoardSolved(newTubes)) {
                    handleLevelWin()
                }
            } else {
                // Invalid move: buzz feedback and keep/clear selection
                soundManager.playInvalidMove()
                onInvalidHaptic()
                _selectedTubeIndex.value = null
            }
        }
    }

    private fun handleLevelWin() {
        _isLevelWon.value = true
        timerJob?.cancel()
        soundManager.playWin()

        val completedLevel = _currentLevel.value
        val moves = _movesCount.value
        val timeSec = _elapsedSeconds.value

        val nextLevel = completedLevel + 1
        preferences.unlockLevel(nextLevel)

        val totalCompleted = preferences.incrementCompletedLevels()
        if (totalCompleted % AdConfig.INTERSTITIAL_LEVEL_INTERVAL == 0) {
            _shouldShowInterstitial.value = true
        }

        viewModelScope.launch {
            leaderboardManager?.recordLevelCompletion(
                level = completedLevel,
                moves = moves,
                timeSeconds = timeSec
            )
        }
    }

    fun onInterstitialShown() {
        _shouldShowInterstitial.value = false
    }

    /**
     * Undoes the last move, restoring previous tube board state.
     */
    fun undoMove(onHapticFeedback: () -> Unit = {}): Boolean {
        if (_isLevelWon.value) return false
        if (_remainingUndos.value <= 0 || undoStack.isEmpty()) {
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

    /**
     * Grants bonus undos (e.g. from watching a rewarded video ad).
     */
    fun addBonusUndos(count: Int = 3) {
        _remainingUndos.value += count
        soundManager.playButtonClick()
    }

    /**
     * Adds an extra empty tube to assist with difficult levels.
     */
    fun addBonusTube(): Boolean {
        if (_bonusTubesCount.value >= 2) return false
        val currentTubes = _tubes.value
        val newTubeId = currentTubes.size
        val newTube = Tube(id = newTubeId, capacity = 4, balls = emptyList())
        _tubes.value = currentTubes + newTube
        // Keep undo snapshots in sync so undoing an earlier move does not drop the bonus tube
        for (i in undoStack.indices) {
            val entry = undoStack[i]
            undoStack[i] = entry.copy(tubes = entry.tubes + newTube.copy(id = entry.tubes.size))
        }
        _bonusTubesCount.value += 1
        soundManager.playButtonClick()
        return true
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
