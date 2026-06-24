package app.recruit.collegebot.presentation.viewmodel

import app.recruit.collegebot.domain.model.QuizQuestion

sealed class QuizUiState {

    object Idle : QuizUiState()

    object Loading : QuizUiState()

    data class Active(
        val topic: String,
        val questions: List<QuizQuestion>,
        val currentIndex: Int = 0,
        val selectedIndex: Int? = null,     // user's pick for current question
        val showExplanation: Boolean = false,
        val answeredCorrectly: List<Boolean> = emptyList() // history of past answers
    ) : QuizUiState() {
        val currentQuestion: QuizQuestion get() = questions[currentIndex]
        val isLastQuestion: Boolean get() = currentIndex == questions.lastIndex
        val isAnswered: Boolean get() = selectedIndex != null
    }

    data class Completed(
        val topic: String,
        val score: Int,
        val total: Int
    ) : QuizUiState()

    data class Error(val message: String) : QuizUiState()
}
