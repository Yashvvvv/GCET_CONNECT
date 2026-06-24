package app.recruit.collegebot.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.recruit.collegebot.data.local.entities.MessageEntity
import app.recruit.collegebot.domain.model.MessageModel
import app.recruit.collegebot.domain.repository.ChatRepository
import app.recruit.collegebot.utils.customQueries
import com.google.genai.Client
import com.google.genai.types.Content
import com.google.genai.types.Part
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val client: Client
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadMessages()
    }

    // ── History ──────────────────────────────────────────────────────

    private fun loadMessages() {
        viewModelScope.launch {
            val saved = chatRepository.getAllMessages()
            val messages = saved.map { MessageModel(it.content, it.sender) }
            _uiState.update {
                it.copy(
                    messages        = messages,
                    showWelcome     = messages.isEmpty(),
                    isLoadingHistory = false
                )
            }
        }
    }

    // ── Send + streaming ─────────────────────────────────────────────

    fun sendMessage(question: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val userMsg   = MessageModel(question, "user")
            val typingMsg = MessageModel("", "model", isTyping = true)

            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        messages    = it.messages + userMsg + typingMsg,
                        showWelcome = false,
                        isStreaming = true,
                        error       = null
                    )
                }
            }

            try {
                val bestMatch = findBestMatchingQuery(question)

                if (bestMatch != null && bestMatch.second > 0.7) {
                    // Instant custom answer
                    val answer = customQueries[bestMatch.first] ?: "I don't have that info yet."
                    withContext(Dispatchers.Main) {
                        _uiState.update { state ->
                            state.copy(
                                messages    = state.messages.dropLast(1) + MessageModel(answer, "model"),
                                isStreaming = false
                            )
                        }
                    }
                    persistMessage(userMsg)
                    persistMessage(MessageModel(answer, "model"))

                } else {
                    // Streaming Gemini response
                    val historyForChat = _uiState.value.messages
                        .dropLast(2) // drop: new user msg + typing indicator
                        .filter { it.role in listOf("user", "model") && !it.isTyping && it.message.isNotBlank() }
                        .map {
                            Content.builder().role(it.role).parts(listOf(Part.fromText(it.message))).build()
                        }

                    val prompt = buildPrompt(question)
                    val promptContent = Content.builder().role("user").parts(listOf(Part.fromText(prompt))).build()
                    val contents = historyForChat + promptContent

                    val stream = client.models.generateContentStream("gemini-2.5-flash", contents, null)

                    // Replace typing indicator with an empty bot message to stream into
                    withContext(Dispatchers.Main) {
                        _uiState.update { state ->
                            state.copy(
                                messages = state.messages.dropLast(1) + MessageModel("", "model")
                            )
                        }
                    }

                    var accumulated = ""

                    for (chunk in stream) {
                        accumulated += chunk.text() ?: ""
                        withContext(Dispatchers.Main) {
                            _uiState.update { state ->
                                val msgs = state.messages.toMutableList()
                                if (msgs.isNotEmpty()) {
                                    msgs[msgs.lastIndex] = MessageModel(accumulated, "model")
                                }
                                state.copy(messages = msgs)
                            }
                        }
                    }

                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(isStreaming = false) }
                    }
                    persistMessage(userMsg)
                    persistMessage(MessageModel(accumulated, "model"))
                }

            } catch (e: Exception) {
                Log.e("ChatViewModel", "sendMessage failed: ${e.javaClass.simpleName}: ${e.message}", e)
                val msg = e.message.orEmpty()
                val errorMsg = when {
                    msg.contains("API key", ignoreCase = true) ||
                    msg.contains("API_KEY", ignoreCase = true) ||
                    msg.contains("invalid", ignoreCase = true) ||
                    msg.contains("403", ignoreCase = true) ->
                        "API key invalid. Get a new key from aistudio.google.com."
                    msg.contains("network", ignoreCase = true) ||
                    msg.contains("Unable to resolve", ignoreCase = true) ||
                    msg.contains("timeout", ignoreCase = true) ||
                    msg.contains("connect", ignoreCase = true) ->
                        "No internet connection. Check your network."
                    msg.contains("not found", ignoreCase = true) ||
                    msg.contains("404", ignoreCase = true) ->
                        "Model not found. Check model name in AppModule."
                    else -> "Error: ${msg.take(120)}"
                }
                withContext(Dispatchers.Main) {
                    _uiState.update { state ->
                        state.copy(
                            messages    = state.messages.dropLast(1),
                            isStreaming = false,
                            error       = errorMsg
                        )
                    }
                }
            }
        }
    }

    fun retryLast() {
        val msgs = _uiState.value.messages
        val lastUser = msgs.lastOrNull { it.role == "user" } ?: return
        _uiState.update { it.copy(error = null) }
        sendMessage(lastUser.message)
    }

    fun dismissError() = _uiState.update { it.copy(error = null) }

    fun clearChat() {
        viewModelScope.launch {
            chatRepository.clearAllMessages()
            _uiState.update { ChatUiState(isLoadingHistory = false) }
        }
    }

    // ── Persistence ───────────────────────────────────────────────────

    private suspend fun persistMessage(msg: MessageModel) {
        chatRepository.insertMessage(
            MessageEntity(content = msg.message, sender = msg.role)
        )
    }

    // ── Query matching ────────────────────────────────────────────────

    private val commonKeywords = setOf(
        "assignment", "practical", "submit", "stationery", "lab", "manual",
        "project", "deadline", "format", "shop", "print", "bind"
    )

    private fun calculateSimilarity(s1: String, s2: String): Double {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j
        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                dp[i][j] = min(
                    min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + if (s1[i - 1].equals(s2[j - 1], ignoreCase = true)) 0 else 1
                )
            }
        }
        return 1.0 - dp[s1.length][s2.length].toDouble() / max(s1.length, s2.length)
    }

    private fun findBestMatchingQuery(userQuery: String): Pair<String, Double>? {
        val normalized = userQuery.lowercase().trim()
        val userKeywords = normalized.split(" ").filter { it.length > 3 }.toSet()
        val hasCommonKeyword = commonKeywords.any { normalized.contains(it) }
        var bestMatch: String? = null
        var bestScore = 0.0

        for (key in customQueries.keys) {
            val normKey = key.lowercase()
            val strSim = calculateSimilarity(normalized, normKey)
            val keyOverlap = userKeywords
                .intersect(normKey.split(" ").filter { it.length > 3 }.toSet())
                .size.toDouble() / max(userKeywords.size, 1)
            val bonus = if (hasCommonKeyword) 0.1 else 0.0
            val score = strSim * 0.4 + keyOverlap * 0.6 + bonus

            if (score > bestScore) {
                bestScore = score
                bestMatch = key
            }
        }
        return bestMatch?.let { Pair(it, bestScore) }
    }

    private fun buildPrompt(question: String) = """
        Context: You are GCET Connect, an AI assistant for Galgotias College of Engineering and Technology, Greater Noida.

        Rules:
        1. Answer college-specific questions accurately and helpfully.
        2. For general academic queries, give constructive guidance.
        3. For inappropriate or harmful content, respond: "I cannot assist with that."
        4. Keep responses concise, professional, and educational.
        5. If unsure, suggest the relevant department or college website.
        6. For personal issues, recommend the student counseling center.

        Question: $question
    """.trimIndent()
}
