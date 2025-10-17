package com.tuusuario.lab6.database

import com.tuusuario.lab6.datos.Foto
import com.tuusuario.lab6.internet.FotoJson

fun FotoJson.toEntity(queryKey: String, pageIndex: Int): PhotoEntity {
    return PhotoEntity(
        id = id.toString(),
        photographer = photographer,
        urlMedium = src.medium,
        urlLarge = src.large,
        width = width,
        height = height,
        queryKey = queryKey,
        pageIndex = pageIndex,
        isFavorite = false,
        updatedAt = System.currentTimeMillis()
    )
}

fun PhotoEntity.toFoto(): Foto {
    return Foto(
        id = id,
        nombreAutor = photographer,
        urlPequena = urlMedium,
        urlGrande = urlLarge
    )
}

fun String.normalizeQuery(): String {
    return this.trim().lowercase()
}