package com.example.studypal.di

import android.app.Application
import com.example.studypal.data.repository.AuthRepository
import com.example.studypal.data.repository.AuthRepositoryImpl
import com.example.studypal.data.repository.SubjectRepository
import com.example.studypal.data.repository.SubjectRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        application: Application
    ): AuthRepository = AuthRepositoryImpl(auth, firestore, application)

    @Provides
    @Singleton
    fun provideSubjectRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): SubjectRepository = SubjectRepositoryImpl(auth, firestore)
} 