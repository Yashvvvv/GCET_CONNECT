package app.recruit.collegebot.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.recruit.collegebot.domain.model.QuizQuestion
import com.google.genai.Client
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val client: Client
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Idle)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    // ── Quiz generation ───────────────────────────────────────────────

    fun generateQuiz(topic: String) {
        if (topic.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = QuizUiState.Loading
            try {
                val response = client.models.generateContent("gemini-2.5-flash", buildPrompt(topic), null)
                val raw = response.text() ?: error("Empty response from Gemini")
                val questions = parseQuizJson(raw)
                _uiState.value = QuizUiState.Active(
                    topic     = topic,
                    questions = questions
                )
            } catch (e: Exception) {
                _uiState.value = QuizUiState.Error(
                    when {
                        e.message?.contains("API key", ignoreCase = true) == true ->
                            "API key invalid. Check your Gemini key."
                        e.message?.contains("network", ignoreCase = true) == true ->
                            "No internet. Check your connection."
                        else ->
                            "Could not generate quiz. Try a different topic."
                    }
                )
            }
        }
    }

    // ── Quiz interaction ──────────────────────────────────────────────

    fun selectAnswer(optionIndex: Int) {
        val state = _uiState.value as? QuizUiState.Active ?: return
        if (state.isAnswered) return
        _uiState.value = state.copy(
            selectedIndex   = optionIndex,
            showExplanation = true
        )
    }

    fun nextQuestion() {
        val state = _uiState.value as? QuizUiState.Active ?: return
        val wasCorrect = state.selectedIndex == state.currentQuestion.correctIndex
        val newHistory = state.answeredCorrectly + wasCorrect

        if (state.isLastQuestion) {
            _uiState.value = QuizUiState.Completed(
                topic = state.topic,
                score = newHistory.count { it },
                total = state.questions.size
            )
        } else {
            _uiState.value = state.copy(
                currentIndex      = state.currentIndex + 1,
                selectedIndex     = null,
                showExplanation   = false,
                answeredCorrectly = newHistory
            )
        }
    }

    fun retryTopic() {
        val topic = when (val s = _uiState.value) {
            is QuizUiState.Active    -> s.topic
            is QuizUiState.Completed -> s.topic
            else -> return
        }
        generateQuiz(topic)
    }

    fun reset() { _uiState.value = QuizUiState.Idle }

    // ── Prompt + parsing ──────────────────────────────────────────────

    private fun buildPrompt(topic: String) = """
        Generate exactly 5 multiple-choice questions about: $topic

        Respond ONLY with valid JSON — no markdown, no text outside the JSON object:
        {
          "questions": [
            {
              "question": "Question text?",
              "options": ["Option A", "Option B", "Option C", "Option D"],
              "correctIndex": 0,
              "explanation": "1-2 sentence explanation of why this is correct."
            }
          ]
        }

        Requirements:
        - Relevant to B.Tech engineering curriculum at GCET
        - correctIndex is 0-based (0 = first option, 3 = last option)
        - All 4 options must be plausible, clearly distinct
        - Questions must test conceptual understanding, not just definitions
        - Explanations must be concise and educational
    """.trimIndent()

    private fun parseQuizJson(raw: String): List<QuizQuestion> {
        // Strip markdown code fences Gemini sometimes adds
        val cleaned = raw.trim()
            .let { if (it.startsWith("```")) it.substringAfter("\n") else it }
            .let { if (it.trimEnd().endsWith("```")) it.trimEnd().dropLast(3) else it }
            .trim()

        val root = JSONObject(cleaned)
        val arr  = root.getJSONArray("questions")

        return (0 until arr.length()).map { i ->
            val q    = arr.getJSONObject(i)
            val opts = q.getJSONArray("options")
            QuizQuestion(
                id           = i + 1,
                question     = q.getString("question"),
                options      = (0 until opts.length()).map { opts.getString(it) },
                correctIndex = q.getInt("correctIndex"),
                explanation  = q.getString("explanation")
            )
        }
    }
}
