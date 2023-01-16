package com.example.apollosample.dagger

import android.content.Context
import com.example.apollosample.ui.screens.auth.AuthViewModel
import com.example.apollosample.ui.screens.home.HomeViewModel
import com.example.apollosample.ui.screens.main.MainViewModel
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [DataModules::class, RepositoriesModule::class])
interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun context(context: Context): Builder
        fun build(): AppComponent
    }

    fun inject(authViewModel: AuthViewModel)
    fun inject(homeViewModel: HomeViewModel)
    fun inject(mainViewModel: MainViewModel)
}