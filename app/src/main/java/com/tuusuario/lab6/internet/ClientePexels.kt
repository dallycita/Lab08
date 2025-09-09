package com.tuusuario.lab6.internet

import com.tuusuario.lab6.CLAVE_PEXELS
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// cliente simple para la API de Pexels
object ClientePexels {

    // Agrega la clave de Pexels a cada request
    private val auth = Interceptor { chain ->
        val req = chain.request().newBuilder()
            .addHeader("Authorization", CLAVE_PEXELS)
            .build()
        chain.proceed(req)
    }

    // Logs en BODY para ver petición y respuesta en Logcat
    private val logs = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // cliente HTTP con auth + logs
    private val clienteHttp = OkHttpClient.Builder()
        .addInterceptor(auth)
        .addInterceptor(logs)
        .build()

    // retrofit con baseUrl y Gson
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.pexels.com/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(clienteHttp)
        .build()

    // función para crear el servicio que le pidamos
    fun <T> crear(servicio: Class<T>): T = retrofit.create(servicio)
}
