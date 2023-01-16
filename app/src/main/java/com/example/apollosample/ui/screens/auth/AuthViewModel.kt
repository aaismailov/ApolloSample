package com.example.apollosample.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.api.ApolloResponse
import com.example.apollosample.AuthLoginMutation
import com.example.apollosample.WikiApp
import com.example.apollosample.dagger.AppComponent
import com.example.apollosample.domain.IWikiRepository
import com.example.apollosample.state.Session
import com.example.apollosample.utils.MutableResultFlow
import com.example.apollosample.utils.loadOrError
import kotlinx.coroutines.launch
import javax.inject.Inject

class AuthViewModel(appComponent: AppComponent = WikiApp.appComponent) : ViewModel() {

    @Inject
    lateinit var session: Session

    @Inject
    lateinit var repository: IWikiRepository

    val location by lazy { session.location }
    val authLoginResult = MutableResultFlow<ApolloResponse<AuthLoginMutation.Data>>()

    init {
        appComponent.inject(this)
    }

    fun changeToken(newToken: String) = session.changeToken(newToken)

    fun changeLocation(newLocation: String) = session.changeLocation(newLocation)

    fun authLogin() = viewModelScope.launch {
        authLoginResult.loadOrError {
            changeLocation("")
            repository.authLogin()
        }
    }
}