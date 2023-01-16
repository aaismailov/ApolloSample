package com.example.apollosample.data.repository

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.example.apollosample.*
import com.example.apollosample.domain.IWikiRepository
import javax.inject.Inject

class WikiRepository @Inject constructor() : IWikiRepository {

    @Inject
    lateinit var apolloClient: ApolloClient

    override suspend fun getPageList(): ApolloResponse<PagesListQuery.Data> {
        return apolloClient.query(PagesListQuery()).execute()
    }

    override suspend fun getSinglePage(id: Int): ApolloResponse<SinglePageQuery.Data> {
        return apolloClient.query(SinglePageQuery(id)).execute()
    }

    override suspend fun authLogin(): ApolloResponse<AuthLoginMutation.Data> {
        return apolloClient.mutation(AuthLoginMutation()).execute()
    }
}