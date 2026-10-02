package ru.jone501.myfefu.networking.di

import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.jone501.myfefu.networking.ApiEndpoints
import ru.jone501.myfefu.networking.api.ApiService
import ru.jone501.myfefu.networking.token.NetworkRequestInterceptor
import ru.jone501.myfefu.networking.token.TokenService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

fun providesLoggingInterceptor(): HttpLoggingInterceptor {
    return HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
}

fun providesTokenService(retrofit: Retrofit): TokenService =
    retrofit.create(TokenService::class.java)

fun providesApiService(retrofit: Retrofit): ApiService =
    retrofit.create(ApiService::class.java)

fun provideHttpClient(
    loggingInterceptor: HttpLoggingInterceptor,
    networkRequestInterceptor: NetworkRequestInterceptor,
): OkHttpClient {
    return OkHttpClient
        .Builder()
        .callTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(networkRequestInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()
}

fun provideConverterFactory(): GsonConverterFactory =
    GsonConverterFactory.create(GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, object :
            TypeAdapter<LocalDateTime>() {
            private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            override fun write(
                out: JsonWriter?,
                value: LocalDateTime?
            ) {
                out?.value(value?.format(formatter))
            }

            override fun read(input: JsonReader?): LocalDateTime? {
                return LocalDateTime.parse(input?.nextString(), formatter)
            }
        })
        .registerTypeAdapter(LocalDate::class.java, object :
            TypeAdapter<LocalDate>() {
            private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            override fun write(
                out: JsonWriter?,
                value: LocalDate?
            ) {
                out?.value(value?.format(formatter))
            }

            override fun read(input: JsonReader?): LocalDate? {
                return LocalDate.parse(input?.nextString(), formatter)
            }
        })
        .create())

fun provideRetrofit(
    okHttpClient: OkHttpClient,
    gsonConverterFactory: GsonConverterFactory,
): Retrofit {
    return Retrofit.Builder()
        .baseUrl(ApiEndpoints.API_HOST)
        .client(okHttpClient)
        .addConverterFactory(gsonConverterFactory)
        .build()
}

val networkModule = module {
    singleOf(::providesTokenService)
    singleOf(::providesApiService)
    singleOf(::NetworkRequestInterceptor)
    singleOf(::providesLoggingInterceptor)
    singleOf(::provideHttpClient)
    singleOf(::provideConverterFactory)
    singleOf(::provideRetrofit)
}