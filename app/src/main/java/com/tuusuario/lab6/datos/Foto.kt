package com.tuusuario.lab6.datos

// clase simple para guardar datos de una foto
data class Foto(
    val id: String,
    val nombreAutor: String,
    val urlPequena: String, // para miniatura / grid
    val urlGrande: String   // para pantalla de detalles
)
