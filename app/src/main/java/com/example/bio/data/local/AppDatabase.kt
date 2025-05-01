package com.example.bio.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.bio.data.local.dao.ApiTokenDao
import com.example.bio.data.local.dao.MessageDao
import com.example.bio.data.local.dao.UserDao
import com.example.bio.data.local.dao.UserPreferenceDao
import com.example.bio.data.local.entity.ApiToken
import com.example.bio.data.local.entity.Message
import com.example.bio.data.local.entity.User
import com.example.bio.data.local.entity.UserPreferences

@Database(entities = [
    User::class,
    UserPreferences::class,
    ApiToken::class,
    Message::class
],
    version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun userPreferencesDao(): UserPreferenceDao
    abstract fun apiTokenDao(): ApiTokenDao
    abstract fun messageDao(): MessageDao
}

