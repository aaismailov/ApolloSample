package com.example.apollosample.dagger

import android.content.Context
import android.util.Log
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.okHttpClient
import com.example.apollosample.data.repository.WikiRepository
import com.example.apollosample.domain.IWikiRepository
import com.example.apollosample.state.Session
import dagger.Binds
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import javax.inject.Singleton

@Module
class DataModules {

    @Provides
    @Singleton
    fun provideSession(context: Context) = Session(context)

    @Provides
    @Singleton
    fun provideHttpClient(session: Session): OkHttpClient {
        val interceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

        val jsonObject = JSONObject()
        jsonObject.put("code", 200)
        jsonObject.put("status", "OK")
        jsonObject.put("message", "Successful")

        val httpClient = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .addNetworkInterceptor { chain ->
                val request = chain.request()
                val response = chain.proceed(request)
                if (response.code == 302) {
                    session.changeLocation(response.headers["Location"].toString())
                    response
                } else if (response.code == 200 && request.url.host == "profile.miem.hse.ru") {
                    val contentType = response.body?.contentType()
                    val body = jsonObject.toString().toResponseBody(contentType)
                    response.newBuilder().body(body).build()
                } else {
                    response
                }
            }
            return httpClient.build()
    }

    @Provides
    @Singleton
    fun provideApolloClient(httpClient: OkHttpClient, session: Session) = ApolloClient.Builder()
        .serverUrl("https://wiki.miem.hse.ru/graphql/")
        .addHttpHeader("Accept-Encoding", "identity")
        .addHttpHeader("Authorization", "Bearer ${session.token.value}")
        .okHttpClient(httpClient)
        .build()
}

@Module
interface RepositoriesModule {

    @Singleton
    @Binds
    fun bindIWikiRepository(wikiRepository: WikiRepository): IWikiRepository
}