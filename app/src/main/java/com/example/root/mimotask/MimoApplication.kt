package com.example.root.mimotask

import android.app.Application
import android.content.Context

class MimoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }

    companion object {
        lateinit var appContext: Context
    }
}