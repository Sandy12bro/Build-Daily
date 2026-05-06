package com.example.builddaily.di

import com.example.builddaily.viewmodel.TodayViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { TodayViewModel() }
}
