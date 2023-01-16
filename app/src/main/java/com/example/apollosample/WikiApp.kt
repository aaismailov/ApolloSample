package com.example.apollosample

import android.app.Application
import com.example.apollosample.dagger.AppComponent
import com.example.apollosample.dagger.DaggerAppComponent

class WikiApp : Application() {

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent
            .builder()
            .context(this)
            .build()
    }

    companion object {
        lateinit var appComponent: AppComponent
    }
}