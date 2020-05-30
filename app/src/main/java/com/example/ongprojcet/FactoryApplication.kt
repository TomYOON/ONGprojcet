package com.example.ongprojcet

import android.app.Application

class FactoryApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        BLE.init(applicationContext)
    }
}