package com.tuusuario.lab6.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<PhotoEntity>)

    @Query("SELECT * FROM photos WHERE queryKey = :queryKey AND pageIndex = :page ORDER BY updatedAt DESC")
    suspend fun getPhotosByQueryAndPage(queryKey: String, page: Int): List<PhotoEntity>

    @Query("SELECT * FROM photos WHERE queryKey = :queryKey ORDER BY pageIndex, updatedAt DESC")
    suspend fun getAllPhotosByQuery(queryKey: String): List<PhotoEntity>

    @Query("SELECT * FROM photos WHERE id = :photoId LIMIT 1")
    suspend fun getPhotoById(photoId: String): PhotoEntity?

    @Query("SELECT * FROM photos WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavorites(): Flow<List<PhotoEntity>>

    @Query("UPDATE photos SET isFavorite = :isFavorite WHERE id = :photoId")
    suspend fun updateFavorite(photoId: String, isFavorite: Boolean)

    @Query("DELETE FROM photos WHERE queryKey = :queryKey")
    suspend fun deleteByQuery(queryKey: String)

    @Query("DELETE FROM photos WHERE updatedAt < :timestamp AND isFavorite = 0")
    suspend fun deleteOldCache(timestamp: Long)
}