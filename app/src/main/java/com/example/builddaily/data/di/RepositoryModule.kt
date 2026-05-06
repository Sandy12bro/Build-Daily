package com.example.builddaily.data.di

import com.example.builddaily.data.repository.AuthRepository
import com.example.builddaily.data.repository.AuthRepositoryImpl
import com.example.builddaily.data.repository.DayRepository
import com.example.builddaily.data.repository.DayRepositoryImpl
import com.example.builddaily.data.repository.DiaryRepository
import com.example.builddaily.data.repository.DiaryRepositoryImpl
import com.example.builddaily.data.repository.TaskRepository
import com.example.builddaily.data.repository.TaskRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    single { AuthRepositoryImpl() as AuthRepository }
    single { DayRepositoryImpl() as DayRepository }
    single { TaskRepositoryImpl() as TaskRepository }
    single { DiaryRepositoryImpl() as DiaryRepository }
}

object RepositoryModule {
    fun provideAuthRepository(): AuthRepository {
        return AuthRepositoryImpl()
    }
    
    fun provideDayRepository(): DayRepository {
        return DayRepositoryImpl()
    }
    
    fun provideTaskRepository(): TaskRepository {
        return TaskRepositoryImpl()
    }
    
    fun provideDiaryRepository(): DiaryRepository {
        return DiaryRepositoryImpl()
    }
}
