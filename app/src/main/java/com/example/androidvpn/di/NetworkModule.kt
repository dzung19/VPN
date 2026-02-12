package com.example.androidvpn.di

import com.example.androidvpn.data.CloudflareApi
import com.example.androidvpn.data.VpnApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("User-Agent", "okhttp/3.12.1")
                    .addHeader("CF-Client-Version", "a-6.10-2158")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val gson = GsonBuilder().setLenient().create()
        return Retrofit.Builder()
            .baseUrl("https://api.cloudflareclient.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideCloudflareApi(retrofit: Retrofit): CloudflareApi {
        return retrofit.create(CloudflareApi::class.java)
    }

    @Provides
    @Singleton
    @javax.inject.Named("VpnRetrofit")
    fun provideVpnRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val gson = GsonBuilder().setLenient().create()
        // Replace with your actual Worker URL when deployed
        val baseUrl = "https://vpn-api.daumo-ringtones.workers.dev/"
        
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideVpnApiService(@javax.inject.Named("VpnRetrofit") retrofit: Retrofit): VpnApiService {
        return retrofit.create(VpnApiService::class.java)
    }
}
