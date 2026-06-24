package app.recruit.collegebot.data.repository

import app.recruit.collegebot.data.local.dao.MessageDao
import app.recruit.collegebot.data.local.entities.MessageEntity
import app.recruit.collegebot.domain.repository.ChatRepository
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao
) : ChatRepository {

    override suspend fun getAllMessages(): List<MessageEntity> =
        messageDao.getAllMessages()

    override suspend fun insertMessage(message: MessageEntity) =
        messageDao.insertMessage(message)

    override suspend fun clearAllMessages() =
        messageDao.clearAllMessages()
}
