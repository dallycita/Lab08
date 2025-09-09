package com.tuusuario.lab6.internet

import com.tuusuario.lab6.datos.Foto

// convierte la respuesta de la API a modelo simple foto
fun FotoJson.aFoto(): Foto = Foto(
    id = id.toString(),
    nombreAutor = photographer,
    urlPequena = src.medium,
    urlGrande = src.large
)
