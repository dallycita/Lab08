package com.tuusuario.lab6.repository

import com.tuusuario.lab6.database.*
import com.tuusuario.lab6.datos.Foto
import com.tuusuario.lab6.internet.ServicioPexels
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PhotoRepository(
    private val api: ServicioPexels,
    private val photoDao: PhotoDao,
    private val recentSearchDao: RecentSearchDao
) {

    suspend fun searchPhotos(query: String, page: Int): Result<List<Foto>> {
        val normalizedQuery = query.normalizeQuery()

        return try {
            val response = api.buscarFotos(tema = query, pagina = page, porPagina = 20)

            val entities = response.photos.map { it.toEntity(normalizedQuery, page) }
            photoDao.insertPhotos(entities)

            recentSearchDao.insertSearch(RecentSearchEntity(normalizedQuery))
            recentSearchDao.cleanOldSearches(10)

            Result.success(entities.map { it.toFoto() })

        } catch (e: Exception) {
            val cached = photoDao.getPhotosByQueryAndPage(normalizedQuery, page)
            if (cached.isNotEmpty()) {
                Result.success(cached.map { it.toFoto() })
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun getPhotoById(photoId: String): Result<Foto> {
        return try {
            val cached = photoDao.getPhotoById(photoId)
            if (cached != null) {
                return Result.success(cached.toFoto())
            }

            val json = api.fotoPorId(photoId)
            val entity = PhotoEntity(
                id = json.id.toString(),
                photographer = json.photographer,
                urlMedium = json.src.medium,
                urlLarge = json.src.large,
                width = json.width,
                height = json.height,
                queryKey = "",
                pageIndex = 0,
                isFavorite = false
            )
            photoDao.insertPhotos(listOf(entity))

            Result.success(entity.toFoto())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleFavorite(photoId: String, isFavorite: Boolean) {
        photoDao.updateFavorite(photoId, isFavorite)
    }

    fun getFavorites(): Flow<List<Foto>> {
        return photoDao.getFavorites().map { entities ->
            entities.map { it.toFoto() }
        }
    }

    fun getRecentSearches(): Flow<List<String>> {
        return recentSearchDao.getRecentSearches().map { searches ->
            searches.map { it.query }
        }
    }

    suspend fun getOfflinePhotos(query: String): List<Foto> {
        val normalizedQuery = query.normalizeQuery()
        return photoDao.getAllPhotosByQuery(normalizedQuery).map { it.toFoto() }
    }
}