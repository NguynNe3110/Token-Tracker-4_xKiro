package com.uzuu.adminitratortoken.data.provider.xkiro

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface XkiroApiService {
    @GET("usage")
    suspend fun getUsage(
        @Header("Authorization") auth: String
    ): UsageResponse

    @GET("usage")
    suspend fun getUsageRaw(
        @Header("Authorization") auth: String
    ): Response<ResponseBody>
}