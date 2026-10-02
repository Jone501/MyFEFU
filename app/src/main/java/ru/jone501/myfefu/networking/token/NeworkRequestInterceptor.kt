package ru.jone501.myfefu.networking.token

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.jone501.myfefu.data.repository.EncryptedSessionManager
import ru.jone501.myfefu.domain.model.AuthToken
import ru.jone501.myfefu.networking.ApiEndpoints
import ru.jone501.myfefu.networking.ApiEndpoints.AUTH_HOST
import ru.jone501.myfefu.networking.token.request.RefreshTokenRequest
import java.net.URL

class NetworkRequestInterceptor(
    private val sessionManager: EncryptedSessionManager,
) : Interceptor, KoinComponent {
    private val tokenService: TokenService by inject()

    companion object {
        const val HEADER_AUTHORIZATION = "Authorization"
        const val TOKEN_TYPE = "Bearer"
        const val UNAUTHORIZED_CODE = 401
        const val FORBIDDEN_CODE = 403
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if (!request.url.toUrl().path.equals(ApiEndpoints.CREATE_TOKEN_URL)
            && !request.url.toUrl().path.equals(ApiEndpoints.REFRESH_TOKEN_URL)
        ) {
            val authToken = runBlocking {
                return@runBlocking sessionManager.get().first()
            }
            if (authToken != null) {
                request = attachTokenToRequest(request, authToken.accessToken)
            }
            val response = chain.proceed(request)
            if (response.code == UNAUTHORIZED_CODE) {
                val originalResponseBody = response.peekBody(Long.MAX_VALUE)
                response.close() // Close previous request
                synchronized(this) {
                    val newAccessToken = refreshAccessToken(authToken)
                    if (newAccessToken != null) {
                        val newRequest = attachTokenToRequest(request, newAccessToken)
                        return chain.proceed(newRequest)
                    } else {
//                        Timber.tag("JT").i("Forcefully logout users")
                        return response.newBuilder().code(FORBIDDEN_CODE)
                            .body(originalResponseBody)
                            .build()
                    }
                }
            }
            return response
        } else {
            val url = request.url.toUrl()
            request = request.newBuilder()
                .url(URL("$AUTH_HOST${url.path}"))
                .build()
        }
        return chain.proceed(request)
    }

    private fun refreshAccessToken(authToken: AuthToken?): String? {
        return if (authToken == null) null else runBlocking {
            val tokenResponse = tokenService.refreshToken(RefreshTokenRequest(authToken.refreshToken))
            if (tokenResponse.isSuccessful) {
                tokenResponse.body()?.let {
                    sessionManager.set(AuthToken(it.access_token, it.refresh_token))
                    it.access_token
                }
            } else null
        }
    }

    private fun attachTokenToRequest(request: Request, token: String): Request {
        return request.newBuilder()
            .header(HEADER_AUTHORIZATION, "$TOKEN_TYPE $token")
            .build()
    }
}