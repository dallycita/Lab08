package com.tuusuario.lab6.pantallas

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tuusuario.lab6.database.AppDatabase
import com.tuusuario.lab6.datos.Foto
import com.tuusuario.lab6.internet.ClientePexels
import com.tuusuario.lab6.internet.ServicioPexels
import com.tuusuario.lab6.repository.PhotoRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Detalles(
    fotoId: String,
    volver: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val api = remember { ClientePexels.crear(ServicioPexels::class.java) }
    val repository = remember { PhotoRepository(api, db.photoDao(), db.recentSearchDao()) }

    val scope = rememberCoroutineScope()

    var foto by remember { mutableStateOf<Foto?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var resolucion by remember { mutableStateOf<String?>(null) }
    var esFavorito by remember { mutableStateOf(false) }

    LaunchedEffect(fotoId) {
        try {
            cargando = true
            error = null

            val result = repository.getPhotoById(fotoId)

            if (result.isSuccess) {
                foto = result.getOrNull()
                try {
                    val json = api.fotoPorId(fotoId)
                    resolucion = "${json.width}×${json.height}"
                } catch (_: Exception) {
                }
            } else {
                error = "No se pudo cargar la foto"
            }
        } catch (e: Exception) {
            error = "Error: ${e.message}"
        } finally {
            cargando = false
        }
    }

    LaunchedEffect(fotoId) {
        db.photoDao().getPhotoById(fotoId)?.let {
            esFavorito = it.isFavorite
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles") },
                navigationIcon = {
                    IconButton(onClick = volver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                esFavorito = !esFavorito
                                repository.toggleFavorite(fotoId, esFavorito)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (esFavorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (esFavorito) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = {
                            foto?.let { f ->
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "Mira esta foto de ${f.nombreAutor}: ${f.urlGrande}")
                                }
                                context.startActivity(Intent.createChooser(intent, "Compartir foto"))
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Compartir"
                        )
                    }
                }
            )
        }
    ) { inner ->
        when {
            cargando -> {
                Box(
                    modifier = Modifier
                        .padding(inner)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier
                        .padding(inner)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(error ?: "Ocurrió un problema")
                }
            }
            foto == null -> {
                Box(
                    modifier = Modifier
                        .padding(inner)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No se encontró la foto")
                }
            }
            else -> {
                val f = foto!!
                Column(
                    modifier = Modifier
                        .padding(inner)
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = f.urlGrande,
                        contentDescription = "Foto de ${f.nombreAutor}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )
                    Text(
                        text = "Autor: ${f.nombreAutor}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(text = "ID: ${f.id}")
                    resolucion?.let { Text(text = "Resolución: $it") }

                    if (esFavorito) {
                        AssistChip(
                            onClick = { },
                            label = { Text("⭐ Marcado como favorito") },
                            leadingIcon = {
                                Icon(Icons.Filled.Favorite, contentDescription = null)
                            }
                        )
                    }
                }
            }
        }
    }
}