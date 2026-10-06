package com.example.charadas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val ROUND_SECONDS = 60
const val COUNTDOWN_SECONDS = 3

enum class Phase { MENU, COUNTDOWN, PLAYING, RESULTS }

enum class Feedback { NONE, CORRECT, PASS }

data class WordResult(val word: String, val correct: Boolean)

data class GameUiState(
    val phase: Phase = Phase.MENU,
    val category: Category = Categories.all.first(),
    val countdown: Int = COUNTDOWN_SECONDS,
    val timeLeft: Int = ROUND_SECONDS,
    val currentWord: String = "",
    val score: Int = 0,
    val results: List<WordResult> = emptyList(),
    val feedback: Feedback = Feedback.NONE
)

class GameViewModel : ViewModel() {

    private val _state = MutableStateFlow(GameUiState())
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    private var deck = ArrayDeque<String>()
    private var timerJob: Job? = null
    private var feedbackJob: Job? = null

    fun startGame(category: Category) {
        timerJob?.cancel()
        feedbackJob?.cancel()
        deck = ArrayDeque(category.words.shuffled())
        _state.value = GameUiState(
            phase = Phase.COUNTDOWN,
            category = category,
            countdown = COUNTDOWN_SECONDS
        )

        timerJob = viewModelScope.launch {
            for (i in COUNTDOWN_SECONDS downTo 1) {
                _state.update { it.copy(countdown = i) }
                delay(1000)
            }
            _state.update {
                it.copy(
                    phase = Phase.PLAYING,
                    timeLeft = ROUND_SECONDS,
                    currentWord = nextWord(category)
                )
            }
            for (t in ROUND_SECONDS - 1 downTo 0) {
                delay(1000)
                _state.update { it.copy(timeLeft = t) }
            }
            finishRound()
        }
    }

    /** Marca la palabra actual como acertada (true) o pasada (false) y avanza a la siguiente. */
    fun answer(correct: Boolean) {
        val current = _state.value
        if (current.phase != Phase.PLAYING || current.timeLeft <= 0) return

        _state.update {
            it.copy(
                score = if (correct) it.score + 1 else it.score,
                results = it.results + WordResult(it.currentWord, correct),
                currentWord = nextWord(it.category),
                feedback = if (correct) Feedback.CORRECT else Feedback.PASS
            )
        }

        feedbackJob?.cancel()
        feedbackJob = viewModelScope.launch {
            delay(450)
            _state.update { it.copy(feedback = Feedback.NONE) }
        }
    }

    fun backToMenu() {
        timerJob?.cancel()
        feedbackJob?.cancel()
        _state.value = GameUiState()
    }

    private fun finishRound() {
        _state.update { it.copy(phase = Phase.RESULTS, feedback = Feedback.NONE) }
    }

    private fun nextWord(category: Category): String {
        if (deck.isEmpty()) deck = ArrayDeque(category.words.shuffled())
        return deck.removeFirst()
    }

    override fun onCleared() {
        timerJob?.cancel()
        feedbackJob?.cancel()
    }
}
