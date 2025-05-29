package com.example.bio.di

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Provide FirebaseAuth as a singleton for the whole app
object FirebaseModule {

    @Provides
    @Singleton // Ensures only one instance of FirebaseAuth is created
    fun provideFirebaseAuth(): FirebaseAuth {
        // get the FirebaseAuth instance
        return FirebaseAuth.getInstance()
    }

}
