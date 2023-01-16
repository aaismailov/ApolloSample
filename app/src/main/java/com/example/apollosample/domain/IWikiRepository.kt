package com.example.apollosample.domain

import com.apollographql.apollo3.api.ApolloResponse
import com.example.apollosample.*

interface IWikiRepository {

    suspend fun getPageList(): ApolloResponse<PagesListQuery.Data>

    suspend fun getSinglePage(id: Int): ApolloResponse<SinglePageQuery.Data>

    suspend fun authLogin(): ApolloResponse<AuthLoginMutation.Data>
}