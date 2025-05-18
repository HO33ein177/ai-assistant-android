package com.example.bio.di

import android.content.Context
import androidx.room.Room
import com.example.bio.data.local.dao.ApiTokenDao
import com.example.bio.data.local.AppDatabase
import com.example.bio.data.local.dao.MessageDao
import com.example.bio.data.local.dao.UserDao
import com.example.bio.data.local.dao.UserPreferenceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "soundwave_database" //  database name
        )
            .fallbackToDestructiveMigration() // Consider proper migrations for production
            .build()
    }

    // Provide ApiTokenDao
    @Provides
    @Singleton
    fun provideApiTokenDao(appDatabase: AppDatabase): ApiTokenDao {
        return appDatabase.apiTokenDao()
    }

    // Provide MessageDao
    @Provides
    @Singleton
    fun provideMessageDao(appDatabase: AppDatabase): MessageDao {
        return appDatabase.messageDao()
    }

    // Provide UserDao
    @Provides
    @Singleton
    fun provideUserDao(appDatabase: AppDatabase): UserDao { // <-- Return UserDao type
        return appDatabase.userDao() // <-- Call userDao() method
    }

    // Provide UserPreferenceDao
    @Provides
    @Singleton
    fun provideUserPreferenceDao(appDatabase: AppDatabase): UserPreferenceDao {
        return appDatabase.userPreferencesDao() // <-- Call userPreferencesDao() method
    }


}
