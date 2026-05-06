package com.example.builddaily

import android.app.Application

class BuildDailyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize any global dependencies here
        // Supabase client is initialized lazily in the object
    }
}
