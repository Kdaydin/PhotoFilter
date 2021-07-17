package com.kdaydin.photofilter.application

import android.app.Application
import com.kdaydin.photofilter.module.networkModule
import com.kdaydin.photofilter.module.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin


class PhotoFilterApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@PhotoFilterApplication)
            modules(networkModule, viewModelModule)
        }
    }
}