package app.recruit.collegebot.domain.usecase

import app.recruit.collegebot.data.local.entities.MessageEntity
import app.recruit.collegebot.domain.repository.ChatRepository

class SendMessageUseCase(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(message: MessageEntity) {
        repository.insertMessage(message)
    }
}
