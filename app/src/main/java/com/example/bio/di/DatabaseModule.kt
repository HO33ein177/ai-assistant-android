package com.example.bio.di // Or your preferred location

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
            "soundwave_database" // Your database name
        )
            .fallbackToDestructiveMigration() // Add options if needed
            .build()
    }

    @Provides
    @Singleton
    fun apiTokenDao(appDatabase: AppDatabase): ApiTokenDao { // CHANGE YourDao and yourDao()
        return appDatabase.apiTokenDao() // CHANGE yourDao() to the method in AppDatabase
    }
    @Provides
    @Singleton
    fun messageDao(appDatabase: AppDatabase): MessageDao { // CHANGE YourDao and yourDao()
        return appDatabase.messageDao() // CHANGE yourDao() to the method in AppDatabase
    }
    @Provides
    @Singleton
    fun userDao(appDatabase: AppDatabase): UserDao { // CHANGE YourDao and yourDao()
        return appDatabase.userDao() // CHANGE yourDao() to the method in AppDatabase
    }

    @Provides
    @Singleton
    fun userPreferences(appDatabase: AppDatabase): UserPreferenceDao { // CHANGE YourDao and yourDao()
        return appDatabase.userPreferencesDao() // CHANGE yourDao() to the method in AppDatabase
    }



    // Add provides methods for other DAOs...
}