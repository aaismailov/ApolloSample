package com.example.apollosample.ui.components.webview

import android.graphics.Bitmap
import android.util.Log
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun CustomWebView(
    location: String,
    getToken: (token: String) -> Unit = {},
    doOnFinished: () -> Unit
) = Box(
    modifier = Modifier
        .fillMaxSize()
        .background(Color.White)
) {
    var webViewCanGoBack by remember { mutableStateOf(false) } // Checking if the WebView can go back
    var isBack by remember { mutableStateOf(false) } // Checking if the page is open when going back or is it a new page
    var historyStackPointer by remember { mutableStateOf(0) } // Number of open pages in WebView
    var backEnabled by remember { mutableStateOf(true) }
    var webView: WebView? = null

    AndroidView(
        factory = {
            WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                settings.userAgentString = settings.userAgentString.replace("; wv)", ")")
                webViewClient = object : WebViewClient() {

                    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                        webViewCanGoBack = view.canGoBack()
                        if (!isBack) {
                            historyStackPointer++
                            isBack = false
                        }
                    }

                    override fun shouldInterceptRequest(
                        view: WebView,
                        request: WebResourceRequest
                    ): WebResourceResponse? {
                        val url = request.url
                        Log.i("WEBVIEW INTERCEPT+++++", url.toString())
                        if (url.host == "wiki.miem.hse.ru") {
                            // At the end of authorization we get a jwt token from the cookie and close the WebView
                            getToken(getCookie(url.toString(), "jwt"))
                            doOnFinished()
                            backEnabled = false
                        }
                        return super.shouldInterceptRequest(view, request)
                    }
                }
                loadUrl(location)
                webView = this
            }
        }, update = {
            it.loadUrl(location)
            webView = it
        }
    )

    BackHandler(enabled = backEnabled) {
        if (webViewCanGoBack && historyStackPointer > 3) { // 3 pages are required to open the service
            historyStackPointer -= 1
            isBack = true
            webView?.goBack()
        } else {
            doOnFinished()
            backEnabled = false
        }
    }
}

fun getCookie(siteName: String?, cookieName: String?): String {
    var cookieValue = ""
    val cookies: String = CookieManager.getInstance().getCookie(siteName)
    val temp = cookies.split(";").toTypedArray()
    for (attr in temp) {
        if (attr.contains(cookieName!!)) {
            cookieValue = attr.split("=").toTypedArray()[1]
            break
        }
    }
    return cookieValue
}