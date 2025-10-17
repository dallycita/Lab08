package com.tuusuario.lab6.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "photos",
    indices = [
        Index(value = ["queryKey", "pageIndex"]),
        Index(value = ["isFavorite"])
    ]
)
data class PhotoEntity(
    @PrimaryKey
    val id: String,
    val photographer: String,
    val urlMedium: String,
    val urlLarge: String,
    val width: Int,
    val height: Int,
    val queryKey: String,
    val pageIndex: Int,
    val isFavorite: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
    val likes: Int = 0
)