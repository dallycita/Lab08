package com.tuusuario.lab6.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_searches")
data class RecentSearchEntity(
    @PrimaryKey
    val query: String,
    val lastUsedAt: Long = System.currentTimeMillis()
)
