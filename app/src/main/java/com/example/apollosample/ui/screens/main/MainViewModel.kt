package com.example.apollosample.ui.screens.main

import androidx.lifecycle.ViewModel
import com.example.apollosample.WikiApp
import com.example.apollosample.dagger.AppComponent
import com.example.apollosample.state.Session
import javax.inject.Inject

class MainViewModel(appComponent: AppComponent = WikiApp.appComponent) : ViewModel() {

    @Inject
    lateinit var session: Session

    val token by lazy { session.token }

    init {
        appComponent.inject(this)
    }
}