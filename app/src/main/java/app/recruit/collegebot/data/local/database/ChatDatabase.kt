package app.recruit.collegebot.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import app.recruit.collegebot.data.local.dao.MessageDao
import app.recruit.collegebot.data.local.entities.MessageEntity

@Database(entities = [MessageEntity::class], version = 1, exportSchema = false)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
}
