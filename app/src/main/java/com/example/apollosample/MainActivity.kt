package com.example.apollosample

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.okHttpClient
import com.example.apollosample.ui.theme.ApolloSampleTheme
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ApolloSampleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {

    var loginInput by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }
    var passwordInput by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }

    val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor()
        .setLevel(HttpLoggingInterceptor.Level.BODY)

    val jsonObject = JSONObject()
    jsonObject.put("code",200);
    jsonObject.put("status","OK");
    jsonObject.put("message","Successful");

    val location = remember {
        mutableStateOf("")
    }

    var showWebView by remember { mutableStateOf(false) }
    var token by remember { mutableStateOf("") }

    val httpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .addNetworkInterceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)
            if (response.code == 302) {
                location.value = response.headers["Location"].toString()
                Log.d("LOCATION++++++++", location.value)

                response
            }
            else if (response.code == 200 && (request.url.host == "profile.miem.hse.ru")) {
                val contentType = response.body?.contentType()
                val body = jsonObject.toString().toResponseBody(contentType);
                response.newBuilder().body(body).build()
            }
            else {
                response
            }
        }
        .build()

    val apolloClient = ApolloClient.Builder()
        .serverUrl("https://wiki.miem.hse.ru/graphql/")
        .addHttpHeader("Accept-Encoding", "identity")
        .okHttpClient(httpClient)
        .build()

    val apolloClientWithAuth = apolloClient.newBuilder()
        .addHttpHeader("Authorization", "Bearer $token")
        .build()

    val pageLists = remember { mutableStateOf(emptyList<PagesListQuery.List>()) }

    LaunchedEffect(Unit, token) {
        val responseApiKeys = apolloClientWithAuth.query(AuthApiKeysQuery()).execute()
        val resApiKeys = responseApiKeys.data?.authentication?.apiKeys
        val errApiKeys = responseApiKeys.errors?.get(0)?.message
        Log.d("RESULT++++++++++", "$resApiKeys")
        Log.d("ERROR+++++++++++", "$errApiKeys")
        val responsePageLists = apolloClientWithAuth.query(PagesListQuery()).execute()
        pageLists.value = responsePageLists.data?.pages?.list!!
        val errPageLists = responsePageLists.errors?.get(0)?.message
        Log.d("RESULT++++++++++", "$pageLists")
        Log.d("ERROR+++++++++++", "$errPageLists")
    }

    if (token.isNotEmpty() && pageLists.value.isNotEmpty()) {
        val list = pageLists.value.subList(1, 15)
        LazyColumn(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            items(count = list.size, itemContent = { id ->
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(list[id].id.toString())
                    Text(list[id].path)
                    Text(list[id].title.toString())
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(Color.Gray)
                )
            })
        }
    } else if (showWebView) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            AndroidView(factory = {
                WebView(it).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.javaScriptEnabled = true
                    webViewClient = WebViewClient()
                    webViewClient = object : WebViewClient() {

                        fun getCookie(siteName: String?, cookieName: String?) : String {
                            var cookieValue = ""
                            val cookieManager: CookieManager = CookieManager.getInstance()
                            val cookies: String = cookieManager.getCookie(siteName)
                            val temp = cookies.split(";").toTypedArray()
                            for (ar1 in temp) {
                                if (ar1.contains(cookieName!!)) {
                                    val temp1 = ar1.split("=").toTypedArray()
                                    cookieValue = temp1[1]
                                    break
                                }
                            }
                            return cookieValue
                        }


                        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                            val url = request.url

                            Log.i("WEBVIEW INTERCEPT+++++", url.toString())
                            if (url.host == "wiki.miem.hse.ru") {
                                token = getCookie(url.toString(), "jwt")
                                Log.d("TOKEN++++++++", token)
                                showWebView = false
                            }
                            return super.shouldInterceptRequest(view, request)
                        }
                    }
                    loadUrl(location.value)
                }
            }, update = {
                it.loadUrl(location.value)
            })
        }
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = loginInput,
                onValueChange = {
                    loginInput = it
                },
                label = { Text(text = "username") }
            )

            OutlinedTextField(
                value = passwordInput,
                onValueChange = {
                    passwordInput = it
                },
                label = { Text(text = "password") }
            )

            val scope = LocalLifecycleOwner.current
            Button(onClick = {
                scope.lifecycleScope.launch {
                    val response = apolloClient.mutation(
                        AuthLoginMutation(loginInput.text, passwordInput.text)
                    ).execute()
                    if (location.value.isNotEmpty()) {
                        showWebView = true
                    }
                    Log.d("Login+++++++++++", response.toString())
                }
            }) {
                Text(text = "ВОЙТИ")

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ApolloSampleTheme {
        MainScreen()
    }
}