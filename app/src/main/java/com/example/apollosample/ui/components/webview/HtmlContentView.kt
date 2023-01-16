package com.example.apollosample.ui.components.webview

import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.apollosample.PagesListQuery
import com.example.apollosample.ui.screens.home.HomeViewModel
import com.example.apollosample.utils.SuccessResult
import com.example.apollosample.utils.getIdFromUrl


@Composable
fun HtmlContentView(
    content: String,
    doOnFinished: () -> Unit
) {
    val viewModel: HomeViewModel = viewModel()
    val getPageListResult by viewModel.getPageListResult.collectAsState()
    val pageLists = remember { mutableStateOf(emptyList<PagesListQuery.List>()) }

    val uriHandler = LocalUriHandler.current
    var enableUriHandler by remember { mutableStateOf(false) }

    AndroidView(
        factory = {
            WebView(it).apply {
                settings.javaScriptEnabled = true
                settings.userAgentString = settings.userAgentString.replace("; wv)", ")")
                webViewClient = object : WebViewClient() {

                    override fun shouldInterceptRequest(
                        view: WebView,
                        request: WebResourceRequest
                    ): WebResourceResponse? {
                        val url = request.url
                        if (url.host?.contains("wiki.miem.hse.ru") == true) {
                            getPageListResult.also { result ->
                                if (result is SuccessResult) {
                                    if (url.toString() == "data:text/HTML;charset=utf-8;base64,") {
                                        enableUriHandler = true
                                    }
                                    pageLists.value = getPageListResult.data?.data?.pages?.list!!
                                    val id = getIdFromUrl(url.toString(), pageLists.value)
                                    doOnFinished()
                                    if (id != 0) {
                                        viewModel.getSinglePage(id)
                                    }
                                }
                            }
                        } else if (enableUriHandler) { // Open in browser if it's an external link
                            uriHandler.openUri(url.toString())
                        }
                        return super.shouldInterceptRequest(view, request)
                    }
                }
                loadDataWithBaseURL(
                    null, content,
                    "text/HTML", "UTF-8", null
                )
            }
        }, update = {
            it.loadDataWithBaseURL(
                null, content,
                "text/HTML", "UTF-8", null
            )
        }
    )
}