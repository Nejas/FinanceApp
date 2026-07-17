package com.example.financeapp.data.network.di

import com.example.financeapp.BuildConfig
import com.example.financeapp.data.network.api.FinanceApiService
import com.example.financeapp.data.network.auth.AuthTokenProvider
import com.example.financeapp.data.network.auth.BearerAuthInterceptor
import com.example.financeapp.data.network.auth.BuildConfigAuthTokenProvider
import com.example.financeapp.data.network.executor.NetworkCallExecutor
import com.example.financeapp.data.network.executor.NetworkRequestExecutor
import com.example.financeapp.data.network.provider.FinanceNetworkDataSource
import com.example.financeapp.data.network.provider.FinanceRemoteDataSource
import com.example.financeapp.data.network.result.RetryPolicy
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import kotlinx.coroutines.sync.Semaphore
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkBindingsModule {

    @Binds
    @Singleton
    abstract fun bindAuthTokenProvider(
        provider: BuildConfigAuthTokenProvider
    ): AuthTokenProvider

    @Binds
    @Singleton
    abstract fun bindNetworkCallExecutor(
        executor: NetworkRequestExecutor
    ): NetworkCallExecutor

    @Binds
    @Singleton
    abstract fun bindFinanceRemoteDataSource(
        dataSource: FinanceNetworkDataSource
    ): FinanceRemoteDataSource
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetryPolicy(): RetryPolicy {
        return RetryPolicy()
    }

    @Provides
    @Singleton
    fun provideNetworkSemaphore(): Semaphore {
        return Semaphore(permits = MAX_CONCURRENT_NETWORK_REQUESTS)
    }

    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        bearerAuthInterceptor: BearerAuthInterceptor,
        retryPolicy: RetryPolicy
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(retryPolicy.requestTimeoutMillis, TimeUnit.MILLISECONDS)
            .readTimeout(retryPolicy.requestTimeoutMillis, TimeUnit.MILLISECONDS)
            .writeTimeout(retryPolicy.requestTimeoutMillis, TimeUnit.MILLISECONDS)
            .callTimeout(retryPolicy.requestTimeoutMillis, TimeUnit.MILLISECONDS)
            .addInterceptor(bearerAuthInterceptor)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
                    )
                }
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        json: Json,
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(CONTENT_TYPE.toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideFinanceApiService(retrofit: Retrofit): FinanceApiService {
        return retrofit.create(FinanceApiService::class.java)
    }

    private const val BASE_URL = "https://shmr-finance.ru/api/v1/"
    private const val CONTENT_TYPE = "application/json"
    private const val MAX_CONCURRENT_NETWORK_REQUESTS = 5
}
