package com.example.builddaily

import android.app.Application
import com.example.builddaily.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BuildDailyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidContext(this@BuildDailyApplication)
            modules(appModule)
        }
    }
}
