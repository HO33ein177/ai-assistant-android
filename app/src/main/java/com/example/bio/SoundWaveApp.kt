package com.example.bio

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SoundWaveApp : Application() {
//    lateinit var database: AppDatabase
//        private set
    override fun onCreate(){
        super.onCreate()
//        database = Room.databaseBuilder(
//            applicationContext,
//            AppDatabase::class.java,
//            "soundwave_database"
//        ).build()
    }
}