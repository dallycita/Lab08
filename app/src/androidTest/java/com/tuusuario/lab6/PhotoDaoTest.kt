package com.tuusuario.lab6

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tuusuario.lab6.database.AppDatabase
import com.tuusuario.lab6.database.PhotoDao
import com.tuusuario.lab6.database.PhotoEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhotoDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var photoDao: PhotoDao

    @Before
    fun setup() {
        // Base de datos en memoria (se borra después de cada test)
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        photoDao = database.photoDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndRetrievePhoto() = runBlocking {
        // Crear una foto de prueba
        val photo = PhotoEntity(
            id = "123",
            photographer = "Test Author",
            urlMedium = "https://example.com/medium.jpg",
            urlLarge = "https://example.com/large.jpg",
            width = 1920,
            height = 1080,
            queryKey = "cats",
            pageIndex = 1,
            isFavorite = false,
            likes = 42
        )

        // Insertar
        photoDao.insertPhotos(listOf(photo))

        // Recuperar
        val retrieved = photoDao.getPhotoById("123")

        // Verificar
        assertNotNull(retrieved)
        assertEquals("Test Author", retrieved?.photographer)
        assertEquals("cats", retrieved?.queryKey)
        assertEquals(42, retrieved?.likes)
    }

    @Test
    fun getPhotosByQueryAndPage() = runBlocking {
        // Insertar múltiples fotos de la misma query pero diferentes páginas
        val photos = listOf(
            PhotoEntity("1", "Author 1", "url1", "url1", 800, 600, "dogs", 1, false, likes = 10),
            PhotoEntity("2", "Author 2", "url2", "url2", 800, 600, "dogs", 1, false, likes = 20),
            PhotoEntity("3", "Author 3", "url3", "url3", 800, 600, "dogs", 2, false, likes = 30),
            PhotoEntity("4", "Author 4", "url4", "url4", 800, 600, "cats", 1, false, likes = 40)
        )
        photoDao.insertPhotos(photos)

        // Obtener solo página 1 de "dogs"
        val page1 = photoDao.getPhotosByQueryAndPage("dogs", 1)

        // Verificar
        assertEquals(2, page1.size)
        assertTrue(page1.all { it.queryKey == "dogs" && it.pageIndex == 1 })
    }

    @Test
    fun toggleFavorite() = runBlocking {
        // Insertar foto no favorita
        val photo = PhotoEntity(
            id = "fav1",
            photographer = "Fav Author",
            urlMedium = "url",
            urlLarge = "url",
            width = 800,
            height = 600,
            queryKey = "test",
            pageIndex = 1,
            isFavorite = false,
            likes = 5
        )
        photoDao.insertPhotos(listOf(photo))

        // Marcar como favorita
        photoDao.updateFavorite("fav1", true)
        var retrieved = photoDao.getPhotoById("fav1")
        assertTrue(retrieved?.isFavorite == true)

        // Desmarcar
        photoDao.updateFavorite("fav1", false)
        retrieved = photoDao.getPhotoById("fav1")
        assertFalse(retrieved?.isFavorite == true)
    }

    @Test
    fun getFavoritesFlow() = runBlocking {
        // Insertar fotos mixtas (favoritas y no favoritas)
        val photos = listOf(
            PhotoEntity("f1", "Auth1", "url", "url", 800, 600, "q1", 1, isFavorite = true, likes = 10),
            PhotoEntity("f2", "Auth2", "url", "url", 800, 600, "q1", 1, isFavorite = false, likes = 20),
            PhotoEntity("f3", "Auth3", "url", "url", 800, 600, "q2", 1, isFavorite = true, likes = 30)
        )
        photoDao.insertPhotos(photos)

        // Obtener solo favoritas
        val favorites = photoDao.getFavorites().first()

        // Verificar
        assertEquals(2, favorites.size)
        assertTrue(favorites.all { it.isFavorite })
    }

    @Test
    fun deleteOldCache() = runBlocking {
        val now = System.currentTimeMillis()
        val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000L)
        val eightDaysAgo = now - (8 * 24 * 60 * 60 * 1000L)

        // Insertar fotos con diferentes timestamps
        val photos = listOf(
            PhotoEntity("old1", "Auth", "url", "url", 800, 600, "q1", 1, false, eightDaysAgo, likes = 1),
            PhotoEntity("recent1", "Auth", "url", "url", 800, 600, "q1", 1, false, sevenDaysAgo, likes = 2),
            PhotoEntity("fav1", "Auth", "url", "url", 800, 600, "q1", 1, true, eightDaysAgo, likes = 3) // Favorita antigua
        )
        photoDao.insertPhotos(photos)

        // Eliminar cache antiguo (más de 7 días) pero NO favoritos
        photoDao.deleteOldCache(sevenDaysAgo)

        // Verificar
        val old = photoDao.getPhotoById("old1")
        val recent = photoDao.getPhotoById("recent1")
        val fav = photoDao.getPhotoById("fav1")

        assertNull(old) // Se eliminó (antigua y no favorita)
        assertNotNull(recent) // Se mantiene (reciente)
        assertNotNull(fav) // Se mantiene (favorita, aunque sea antigua)
    }

    @Test
    fun getAllPhotosByQuery() = runBlocking {
        // Insertar fotos de múltiples páginas
        val photos = listOf(
            PhotoEntity("1", "A1", "url", "url", 800, 600, "nature", 1, false, likes = 1),
            PhotoEntity("2", "A2", "url", "url", 800, 600, "nature", 2, false, likes = 2),
            PhotoEntity("3", "A3", "url", "url", 800, 600, "nature", 3, false, likes = 3),
            PhotoEntity("4", "A4", "url", "url", 800, 600, "city", 1, false, likes = 4)
        )
        photoDao.insertPhotos(photos)

        // Obtener todas las fotos de "nature" (todas las páginas)
        val allNature = photoDao.getAllPhotosByQuery("nature")

        // Verificar
        assertEquals(3, allNature.size)
        assertTrue(allNature.all { it.queryKey == "nature" })
        // Verificar que están ordenadas por página
        assertEquals(1, allNature[0].pageIndex)
        assertEquals(2, allNature[1].pageIndex)
        assertEquals(3, allNature[2].pageIndex)
    }
}