package app.recruit.collegebot.domain.repository

import app.recruit.collegebot.data.local.entities.MessageEntity

interface ChatRepository {
    suspend fun getAllMessages(): List<MessageEntity>
    suspend fun insertMessage(message: MessageEntity)
    suspend fun clearAllMessages()
}
