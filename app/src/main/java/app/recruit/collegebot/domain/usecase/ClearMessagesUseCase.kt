package app.recruit.collegebot.domain.usecase

import app.recruit.collegebot.domain.repository.ChatRepository

class ClearMessagesUseCase(
    private val repository: ChatRepository
) {
    suspend operator fun invoke() {
        repository.clearAllMessages()
    }
}
