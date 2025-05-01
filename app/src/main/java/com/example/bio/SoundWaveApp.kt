package com.example.bio

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SoundWaveApp : Application() {
//    lateinit var database: AppDatabase
//        private set
    override fun onCreate(){
        super.onCreate()
        FirebaseApp.initializeApp(this) // Initialize Firebase
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )
//        database = Room.databaseBuilder(
//            applicationContext,
//            AppDatabase::class.java,
//            "soundwave_database"
//        ).build()
    }
}