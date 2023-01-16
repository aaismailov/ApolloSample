package com.example.apollosample.ui.screens.home

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.apollosample.PagesListQuery
import com.example.apollosample.R
import com.example.apollosample.ui.MDDocument
import com.example.apollosample.ui.components.loader.CircularLoader
import com.example.apollosample.ui.components.webview.CustomWebView
import com.example.apollosample.ui.components.webview.HtmlContentView
import com.example.apollosample.ui.theme.ApolloSampleTheme
import com.example.apollosample.utils.ErrorResult
import com.example.apollosample.utils.LoadingResult
import com.example.apollosample.utils.SuccessResult
import com.example.apollosample.utils.getIdFromUrl
import kotlinx.coroutines.launch
import org.commonmark.node.Document
import org.commonmark.parser.Parser

@Composable
fun HomeScreen() {

    val viewModel: HomeViewModel = viewModel()

    val location by viewModel.location.collectAsState()
    val getSinglePageResult by viewModel.getSinglePageResult.collectAsState()
    val getPageListResult by viewModel.getPageListResult.collectAsState()

    val pageLists = remember { mutableStateOf(emptyList<PagesListQuery.List>()) }
    var showWebView by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.clearPagesStack()
        viewModel.getPageList()
        viewModel.getSinglePage()
    }

    getPageListResult.also { result ->
        when (result) {
            is SuccessResult -> {
                pageLists.value =
                    getPageListResult.data?.data?.pages?.list!!
            }
            is ErrorResult -> Toast.makeText(
                context,
                result.message ?: R.string.on_error_def,
                Toast.LENGTH_LONG
            ).show()
            is LoadingResult -> CircularLoader()
            else -> {}
        }
    }

    getSinglePageResult.also { result ->
        when (result) {
            is SuccessResult -> {
                val singlePageData = getSinglePageResult.data?.data?.pages?.single
                val errSinglePage = getSinglePageResult.data?.errors?.get(0)?.message
                var content = singlePageData?.content ?: errSinglePage.toString()
                val pagePath = singlePageData?.path?.split("/")

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    val scope = LocalLifecycleOwner.current
                    IconButton(
                        onClick = {
                            scope.lifecycleScope.launch {
                                viewModel.changeLocation("https://wiki.miem.hse.ru/logout")
                                viewModel.changeToken("")
                                showWebView = true
                            }
                        }
                    ) {
                        Icon(
                            Icons.Filled.ExitToApp,
                            contentDescription = "exit",
                            tint = Color.Red
                        )
                    }

                    // Breadcrumb navigation
                    LazyRow(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            IconButton(
                                onClick = { viewModel.getSinglePage() }
                            ) {
                                Icon(
                                    Icons.Filled.Home,
                                    contentDescription = "home",
                                    tint = Color.Gray
                                )
                            }
                        }

                        pagePath?.let {
                            items(it.size) { index ->
                                var path = ""
                                for (i in 0..index) path += "/${pagePath[i]}"

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "/",
                                        modifier = Modifier.padding(horizontal = 10.dp),
                                        color = Color.Gray
                                    )
                                    TextButton(
                                        onClick = {
                                            val id = getIdFromUrl(path, pageLists.value)
                                            if (id != 0) viewModel.getSinglePage(id)
                                        },
                                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Black)
                                    ) {
                                        Text(pagePath[index])
                                    }
                                }
                            }
                        }
                    }

                    // Title with description
                    Text(
                        text = singlePageData?.title.toString(),
                        modifier = Modifier.padding(start = 8.dp, top = 15.dp),
                        style = MaterialTheme.typography.h5
                    )
                    Text(
                        text = singlePageData?.description.toString(),
                        modifier = Modifier.padding(start = 8.dp, bottom = 15.dp),
                        color = Color.Gray
                    )

                    // Content
                    if (singlePageData?.contentType == "markdown") {
                        val parser = Parser.builder().build()
                        val root = parser.parse(content) as Document
                        Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                            Column {
                                MDDocument(root)
                            }
                        }
                    } else {
                        var showHtmlContentView by remember { mutableStateOf(true) }
                        content = content.replace("href=\"/", "href=\"https://wiki.miem.hse.ru/")
                        if (showHtmlContentView) {
                            HtmlContentView(content) {
                                showHtmlContentView = false
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
            is ErrorResult -> Toast.makeText(
                context,
                result.message ?: R.string.on_error_def,
                Toast.LENGTH_LONG
            ).show()
            is LoadingResult -> CircularLoader()
            else -> {}
        }
    }

    if (showWebView) {
        CustomWebView(
            location = location
        ) {
            showWebView = false
        }
    }

    BackHandler(
        enabled = true,
        onBack = viewModel::goBack
    )
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ApolloSampleTheme {
        HomeScreen()
    }
}