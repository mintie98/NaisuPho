package com.example.naisupho.di

import com.example.naisupho.interfaces.RetrofitClient
import com.example.naisupho.repository.StoreRepository
import com.example.naisupho.repository.TravelTimeRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideStoreRepository(firebaseDatabase: FirebaseDatabase): StoreRepository {
        return StoreRepository(firebaseDatabase)
    }

    @Provides
    @Singleton
    fun provideTravelTimeRepository(
        @Named("GoogleClient")retrofitClient: RetrofitClient,
        @Named("GoogleApiKey")apiKey: String
    ): TravelTimeRepository {
        return TravelTimeRepository(retrofitClient, apiKey)
    }

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideDatabaseReference(): DatabaseReference = FirebaseDatabase.getInstance().reference


}