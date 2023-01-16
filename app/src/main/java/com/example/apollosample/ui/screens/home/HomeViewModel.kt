package com.example.apollosample.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.api.ApolloResponse
import com.example.apollosample.*
import com.example.apollosample.dagger.AppComponent
import com.example.apollosample.domain.IWikiRepository
import com.example.apollosample.state.Session
import com.example.apollosample.utils.MutableResultFlow
import com.example.apollosample.utils.loadOrError
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeViewModel(appComponent: AppComponent = WikiApp.appComponent) : ViewModel() {

    @Inject
    lateinit var session: Session

    @Inject
    lateinit var repository: IWikiRepository

    val location by lazy { session.location }
    val getPageListResult = MutableResultFlow<ApolloResponse<PagesListQuery.Data>>()
    val getSinglePageResult = MutableResultFlow<ApolloResponse<SinglePageQuery.Data>>()

    init {
        appComponent.inject(this)
    }

    fun changeToken(newToken: String) = session.changeToken(newToken)

    fun changeLocation(newLocation: String) = session.changeLocation(newLocation)

    fun getPageList() = viewModelScope.launch {
        getPageListResult.loadOrError {
            repository.getPageList()
        }
    }

    fun getSinglePage(id: Int = ID, isBack: Boolean = false) = viewModelScope.launch {
        getSinglePageResult.loadOrError {
            val calledId = if (id == 0) ID else id
            if (!isBack) {
                session.addPagesStack(calledId)
            }
            repository.getSinglePage(calledId)
        }
    }

    fun goBack() {
        session.dropLastPagesStack()
        if (session.pagesStack.value.isEmpty()) {
            getSinglePage(isBack = true)
        } else {
            getSinglePage(session.pagesStack.value.last(), true)
        }
    }

    fun clearPagesStack() = session.clearPagesStack()

    companion object {
        // Для тестов id:
        // html:     7827 edu/tracks/networking/students19 Поток 19х специализации Сети
        // markdown: 25   docs/miem-digital                Сервисы МИЭМ
        const val ID = 7827
    }
}