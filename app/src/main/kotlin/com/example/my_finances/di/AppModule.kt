package com.example.my_finances.di

import android.content.Context
import com.example.my_finances.data.repository.BudgetRepository
import com.example.my_finances.data.repository.CategoryRepository
import com.example.my_finances.data.repository.FirebaseAuthDataSource
import com.example.my_finances.data.repository.TransactionRepository
import com.example.my_finances.data.repository.impl.BudgetRepositoryImpl
import com.example.my_finances.data.repository.impl.CategoryRepositoryImpl
import com.example.my_finances.data.repository.impl.TransactionRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseAuthDataSource(
        auth: FirebaseAuth,
        @ApplicationContext context: Context
    ): FirebaseAuthDataSource {
        return FirebaseAuthDataSource(auth, context)
    }

    @Provides
    @Singleton
    fun provideTransactionRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): TransactionRepository {
        return TransactionRepositoryImpl(firestore, auth)
    }

    @Provides
    @Singleton
    fun provideCategoryRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): CategoryRepository {
        return CategoryRepositoryImpl(firestore, auth)
    }

    @Provides
    @Singleton
    fun provideBudgetRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): BudgetRepository {
        return BudgetRepositoryImpl(firestore, auth)
    }
}
