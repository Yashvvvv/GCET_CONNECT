package app.recruit.collegebot.presentation.viewmodel

import app.recruit.collegebot.domain.model.MessageModel

data class ChatUiState(
    val messages: List<MessageModel> = emptyList(),
    val showWelcome: Boolean = true,
    val isLoadingHistory: Boolean = true,
    val isStreaming: Boolean = false,
    val error: String? = null
)
