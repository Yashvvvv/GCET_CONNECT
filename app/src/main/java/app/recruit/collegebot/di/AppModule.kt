package app.recruit.collegebot.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import app.recruit.collegebot.data.local.dao.MessageDao
import app.recruit.collegebot.data.local.database.ChatDatabase
import app.recruit.collegebot.data.repository.ChatRepositoryImpl
import app.recruit.collegebot.domain.repository.ChatRepository
import app.recruit.collegebot.utils.Constants

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideChatDatabase(@ApplicationContext context: Context): ChatDatabase =
        Room.databaseBuilder(
            context,
            ChatDatabase::class.java,
            "chat_database"
        ).build()

    @Provides
    fun provideMessageDao(db: ChatDatabase): MessageDao = db.messageDao()

    @Provides
    @Singleton
    fun provideChatRepository(messageDao: MessageDao): ChatRepository =
        ChatRepositoryImpl(messageDao)

    @Provides
    @Singleton
    fun provideClient(@ApplicationContext context: Context): com.google.genai.Client {
        val key = Constants.getApiKey(context)
        Log.d("AppModule", "Gemini key loaded: length=${key.length}, prefix=${key.take(8)}")
        return com.google.genai.Client.builder().apiKey(key).build()
    }
}
