package app.recruit.collegebot.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import app.recruit.collegebot.data.local.entities.MessageEntity

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    suspend fun getAllMessages(): List<MessageEntity>

    @Insert
    suspend fun insertMessage(message: MessageEntity)

    @Query("DELETE FROM messages")
    suspend fun clearAllMessages()
}