package com.example.collegebot

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.collegebot.data.ChatDatabase
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val database = ChatDatabase.getDatabase(application)
    private val messageDao = database.messageDao()
    
    private val _messageList = mutableStateListOf<MessageModel>()
    val messageList: List<MessageModel> = _messageList

    // Add a state to track if messages are being shown
    private val _showMessages = mutableStateOf(false)
    val showMessages: State<Boolean> = _showMessages

    init {
        loadMessages()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            val savedMessages = messageDao.getAllMessages()
            _messageList.clear()
            _messageList.addAll(savedMessages.map { entity ->
                MessageModel(entity.content, entity.sender)
            })
        }
    }

    val generativeModel: GenerativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = Constants.apiKey
    )

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
                    min(dp[i-1][j] + 1, dp[i][j-1] + 1),
                    dp[i-1][j-1] + if (s1[i-1].equals(s2[j-1], ignoreCase = true)) 0 else 1
                )
            }
        }
        
        val maxLength = max(s1.length, s2.length)
        return 1 - (dp[s1.length][s2.length].toDouble() / maxLength)
    }

    private fun findBestMatchingQuery(userQuery: String): Pair<String, Double>? {
        val normalizedUserQuery = userQuery.lowercase().trim()
        var bestMatch: String? = null
        var bestSimilarity = 0.0

        // Check for common keywords first
        val containsCommonKeyword = commonKeywords.any { 
            normalizedUserQuery.contains(it) 
        }

        val userKeywords = normalizedUserQuery.split(" ")
            .filter { it.length > 3 }
            .toSet()

        for (customQuery in customQueries.keys) {
            val normalizedCustomQuery = customQuery.lowercase()
            val fullStringSimilarity = calculateSimilarity(normalizedUserQuery, normalizedCustomQuery)
            
            val customKeywords = normalizedCustomQuery.split(" ")
                .filter { it.length > 3 }
                .toSet()
            val keywordOverlap = userKeywords.intersect(customKeywords).size.toDouble() / 
                max(userKeywords.size, customKeywords.size)

            // Give bonus similarity for common keywords
            val keywordBonus = if (containsCommonKeyword) 0.1 else 0.0
            val combinedSimilarity = (fullStringSimilarity * 0.4) + 
                                   (keywordOverlap * 0.6) + 
                                   keywordBonus

            if (combinedSimilarity > bestSimilarity) {
                bestSimilarity = combinedSimilarity
                bestMatch = customQuery
            }
        }

        return bestMatch?.let { Pair(it, bestSimilarity) }
    }

    fun sendMessage(question: String) {
        viewModelScope.launch {
            try {
                _messageList.add(MessageModel(question, "user"))
                _messageList.add(MessageModel("Typing...", "model", isTyping = true))

                // Handle other queries
                val bestMatch = findBestMatchingQuery(question)
                when {
                    bestMatch != null && bestMatch.second > 0.7 -> {
                        _messageList.removeLast()
                        val customAnswer = customQueries[bestMatch.first]
                        _messageList.add(MessageModel(customAnswer ?: "I don't understand the question.", "model"))
                    }

                    else -> {
                        val chat = generativeModel.startChat(
                            history = _messageList.map {
                                content(it.role) { text(it.message) }
                            }.toList()
                        )
                        
                        // Prepare the question for the generative model
                        val constrainedQuestion = """
                            Context: You are GCET Connect, a chatbot for Galgotias College of Engineering and Technology, Greater Noida.
                            
                            Rules:
                            1. For college-specific questions, provide accurate, helpful information
                            2. For general academic queries, provide constructive guidance
                            3. For non-academic but relevant queries, give appropriate responses
                            4. For inappropriate, explicit, or harmful content, respond with "I cannot assist with such queries"
                            5. Keep responses professional and educational
                            6. If unsure, suggest visiting relevant department/website
                            7. For personal issues, recommend student counseling center
                            
                            User Question: $question
                            
                            Please provide a helpful response following these rules.
                        """.trimIndent()
                        
                        _messageList.removeLast()
                        val response = chat.sendMessage(constrainedQuestion)
                        _messageList.add(MessageModel(response.text.toString(), "model"))
                    }
                }

            } catch (e: Exception) {
                _messageList.removeLast()
                _messageList.add(MessageModel("Error: ${e.message}", "model"))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        saveMessages()
    }

    private fun saveMessages() {
        viewModelScope.launch {
            // Optional: Implement local storage
            // This is a placeholder for persistence
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            messageDao.clearAllMessages()
            _messageList.clear()
        }
    }

    fun toggleMessageVisibility(show: Boolean) {
        _showMessages.value = show
    }
}
