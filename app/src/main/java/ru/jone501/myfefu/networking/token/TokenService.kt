package ru.jone501.myfefu.networking.token

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import ru.jone501.myfefu.networking.ApiEndpoints
import ru.jone501.myfefu.networking.ApiEndpoints.USER_AGENT
import ru.jone501.myfefu.networking.token.request.CreateTokenRequest
import ru.jone501.myfefu.networking.token.request.RefreshTokenRequest
import ru.jone501.myfefu.networking.token.response.TokenPairResponse

interface TokenService {
    @POST(ApiEndpoints.CREATE_TOKEN_URL)
    @Headers(
        "Cache-Control: no-cache",
        "Authorization: Basic YnJpY3M6MTIzMzIx",
        USER_AGENT
    )
    suspend fun createToken(@Body body: CreateTokenRequest): Response<TokenPairResponse>

    @POST(ApiEndpoints.REFRESH_TOKEN_URL)
    @Headers(
        "Cache-Control: no-cache",
        "Authorization: Basic YnJpY3M6MTIzMzIx",
        USER_AGENT
    )
    suspend fun refreshToken(@Body body: RefreshTokenRequest): Response<TokenPairResponse>
}