package com.tuusuario.lab6.internet

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ServicioPexels {
    @GET("search")
    suspend fun buscarFotos(
        @Query("query") tema: String,
        @Query("page") pagina: Int,
        @Query("per_page") porPagina: Int
    ): RespuestaBusqueda

    @GET("photos/{id}")
    suspend fun fotoPorId(
        @Path("id") id: String
    ): FotoJson
}

// Modelos de la API
data class RespuestaBusqueda(
    val page: Int,
    val per_page: Int,
    val total_results: Int,
    val photos: List<FotoJson>
)

data class FotoJson(
    val id: Long,
    val photographer: String,
    val width: Int,
    val height: Int,
    val src: LinksFoto
)

data class LinksFoto(
    val medium: String,
    val large: String
)
