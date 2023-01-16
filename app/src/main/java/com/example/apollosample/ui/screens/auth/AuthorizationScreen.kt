package com.example.apollosample.ui.screens.auth

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.apollosample.R
import com.example.apollosample.ui.components.loader.CircularLoader
import com.example.apollosample.ui.components.webview.CustomWebView
import com.example.apollosample.utils.ErrorResult
import com.example.apollosample.utils.LoadingResult
import kotlinx.coroutines.launch

@Composable
fun AuthorizationScreen() {

    val viewModel: AuthViewModel = viewModel()

    val location by viewModel.location.collectAsState()
    val authLoginResult by viewModel.authLoginResult.collectAsState()

    var showWebView by remember { mutableStateOf(false) }

    authLoginResult.also { result ->
        if (result is LoadingResult) {
            CircularLoader()
        } else if (result is ErrorResult) {
            Toast.makeText(
                LocalContext.current,
                result.message ?: R.string.on_error_def,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    if (showWebView) {
        CustomWebView(
            location = location,
            getToken = viewModel::changeToken,
            doOnFinished = { showWebView = false }
        )
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            val scope = LocalLifecycleOwner.current
            Button(onClick = {
                scope.lifecycleScope.launch {
                    viewModel.changeLocation("")
                    viewModel.authLogin()
                    showWebView = true
                }
            }) {
                Text(text = "ВОЙТИ")
            }
        }
    }
}