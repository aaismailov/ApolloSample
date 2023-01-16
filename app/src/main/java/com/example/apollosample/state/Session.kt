package com.example.apollosample.state

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class Session(context: Context) {

    private val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private val _token = MutableStateFlow(sharedPreferences.getString(PREF_TOKEN, "").orEmpty())
    val token: StateFlow<String> = _token

    private val _location = MutableStateFlow(sharedPreferences.getString(PREF_LOCATION, "").orEmpty())
    val location: StateFlow<String> = _location

    private val _pagesStack = MutableStateFlow(
        sharedPreferences.getString(PREF_STACK, "")?.split(";")
        ?.filter { it.toIntOrNull() != null }?.map { it.toInt() } ?: listOf())
    val pagesStack: StateFlow<List<Int>> = _pagesStack

    fun changeToken(newToken: String) {
        sharedPreferences.edit { putString(PREF_TOKEN, newToken) }
        _token.value = newToken
    }

    fun changeLocation(newLocation: String) {
        sharedPreferences.edit { putString(PREF_LOCATION, newLocation) }
        _location.value = newLocation
    }

    fun addPagesStack(id: Int) {
        val newPagesStack = _pagesStack.value.plus(id)
        sharedPreferences.edit { putString(PREF_STACK, newPagesStack.joinToString(";")) }
        _pagesStack.value = newPagesStack
    }

    fun dropLastPagesStack() {
        val newPagesStack = _pagesStack.value.dropLast(1)
        sharedPreferences.edit { putString(PREF_STACK, newPagesStack.joinToString(";")) }
        _pagesStack.value = newPagesStack
    }

    fun clearPagesStack() {
        sharedPreferences.edit { putString(PREF_STACK, "") }
        _pagesStack.value = listOf()
    }

    companion object {
        const val PREF_TOKEN = "token"
        const val PREF_LOCATION = "location"
        const val PREF_NAME = "session"
        const val PREF_STACK = "pages_stack"
    }
}