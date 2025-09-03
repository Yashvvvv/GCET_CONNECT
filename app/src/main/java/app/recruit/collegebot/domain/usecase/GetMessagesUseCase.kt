package app.recruit.collegebot.domain.usecase

import app.recruit.collegebot.data.local.entities.MessageEntity
import app.recruit.collegebot.domain.repository.ChatRepository

class GetMessagesUseCase(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(): List<MessageEntity> {
        return repository.getAllMessages()
    }
}
