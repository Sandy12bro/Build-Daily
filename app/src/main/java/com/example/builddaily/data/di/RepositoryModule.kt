package com.example.builddaily.data.di

import com.example.builddaily.data.repository.DayRepository
import com.example.builddaily.data.repository.DayRepositoryImpl
import com.example.builddaily.data.repository.DiaryRepository
import com.example.builddaily.data.repository.DiaryRepositoryImpl
import com.example.builddaily.data.repository.TaskRepository
import com.example.builddaily.data.repository.TaskRepositoryImpl

object RepositoryModule {
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
