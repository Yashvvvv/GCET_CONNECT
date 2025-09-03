package app.recruit.collegebot.domain.model

data class MessageModel(
    val message : String,
    val role : String,
    val isTyping: Boolean = false
)