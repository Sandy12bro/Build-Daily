package com.example.builddaily.di

import com.example.builddaily.viewmodel.AuthViewModel
import com.example.builddaily.viewmodel.TodayViewModel
import com.example.builddaily.viewmodel.PlanDayViewModel
import com.example.builddaily.viewmodel.DiaryViewModel
import com.example.builddaily.viewmodel.CalendarViewModel
import com.example.builddaily.viewmodel.InsightsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { AuthViewModel() }
    viewModel { TodayViewModel() }
    viewModel { PlanDayViewModel() }
    viewModel { DiaryViewModel() }
    viewModel { CalendarViewModel() }
    viewModel { InsightsViewModel() }
}
