package com.example.bio.di // Or your preferred DI package

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
        // The standard way to get the FirebaseAuth instance
        return FirebaseAuth.getInstance()
    }

    // Add providers for other Firebase services here if needed (e.g., Firestore)
    // @Provides
    // @Singleton
    // fun provideFirestore(): FirebaseFirestore {
    //     return FirebaseFirestore.getInstance()
    // }
}
