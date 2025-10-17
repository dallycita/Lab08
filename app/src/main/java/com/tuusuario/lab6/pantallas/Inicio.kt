package com.tuusuario.lab6.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tuusuario.lab6.database.AppDatabase
import com.tuusuario.lab6.datos.Foto
import com.tuusuario.lab6.internet.ClientePexels
import com.tuusuario.lab6.internet.ServicioPexels
import com.tuusuario.lab6.repository.PhotoRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Inicio(
    irADetalle: (String) -> Unit,
    irAPerfil: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val api = remember { ClientePexels.crear(ServicioPexels::class.java) }
    val repository = remember { PhotoRepository(api, db.photoDao(), db.recentSearchDao()) }

    val scope = rememberCoroutineScope()

    var textoBuscado by remember { mutableStateOf("") }
    var visibles by remember { mutableStateOf(listOf<Foto>()) }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var modoOffline by remember { mutableStateOf(false) }

    val busquedasRecientes by repository.getRecentSearches().collectAsState(initial = emptyList())

    val gridState = rememberLazyGridState()

    var pagina by remember { mutableStateOf(1) }
    var cargandoMas by remember { mutableStateOf(false) }

    var favoritos by remember { mutableStateOf(mapOf<String, Boolean>()) }

    // 🔥 CORREGIDO: Cargar el estado de favoritos desde Room cuando cambian las fotos visibles
    LaunchedEffect(visibles) {
        if (visibles.isNotEmpty()) {
            val ids = visibles.map { it.id }
            val favoritosActualizados = mutableMapOf<String, Boolean>()
            ids.forEach { id ->
                val entity = db.photoDao().getPhotoById(id)
                if (entity != null) {
                    favoritosActualizados[id] = entity.isFavorite
                }
            }
            favoritos = favoritosActualizados
        }
    }

    LaunchedEffect(textoBuscado) {
        snapshotFlow { textoBuscado }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctUntilChanged()
            .debounce(500)
            .collectLatest { q ->
                try {
                    cargando = true
                    error = null
                    modoOffline = false
                    pagina = 1

                    val result = repository.searchPhotos(q, pagina)

                    if (result.isSuccess) {
                        visibles = result.getOrNull() ?: emptyList()
                    } else {
                        val cached = repository.getOfflinePhotos(q)
                        if (cached.isNotEmpty()) {
                            visibles = cached
                            modoOffline = true
                        } else {
                            error = "No hay conexión y no hay datos en cache"
                        }
                    }
                } catch (e: Exception) {
                    error = "Error: ${e.message}"
                } finally {
                    cargando = false
                }
            }
    }

    LaunchedEffect(gridState, textoBuscado, visibles) {
        snapshotFlow {
            val info = gridState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            Pair(lastVisible, visibles.size)
        }
            .distinctUntilChanged()
            .collectLatest { (lastVisible, total) ->
                val cercaDelFinal = lastVisible >= total - 6
                if (!cargando && !cargandoMas && !modoOffline && error == null && textoBuscado.isNotBlank() && cercaDelFinal) {
                    try {
                        cargandoMas = true
                        val siguiente = pagina + 1
                        val result = repository.searchPhotos(textoBuscado.trim(), siguiente)

                        if (result.isSuccess) {
                            val nuevas = result.getOrNull() ?: emptyList()
                            if (nuevas.isNotEmpty()) {
                                pagina = siguiente
                                visibles = visibles + nuevas
                            }
                        }
                    } catch (_: Exception) {
                    } finally {
                        cargandoMas = false
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Fotos")
                        if (modoOffline) {
                            Text(
                                "Modo Offline",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = irAPerfil) {
                        Icon(Icons.Filled.AccountCircle, contentDescription = "Perfil")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = textoBuscado,
                onValueChange = { textoBuscado = it },
                label = { Text("Buscar...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            if (busquedasRecientes.isNotEmpty() && textoBuscado.isEmpty()) {
                Text(
                    "Búsquedas recientes:",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    items(busquedasRecientes) { query ->
                        SuggestionChip(
                            onClick = { textoBuscado = query },
                            label = { Text(query) }
                        )
                    }
                }
            }

            when {
                cargando -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { Text(error ?: "Ocurrió un problema") }
                }
                textoBuscado.isBlank() && visibles.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { Text("Escribe un tema para buscar fotos") }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        state = gridState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(visibles.size) { index ->
                            val foto = visibles[index]
                            val esFavorito = favoritos[foto.id] ?: false

                            Card(
                                onClick = { irADetalle(foto.id) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box {
                                    Column {
                                        AsyncImage(
                                            model = foto.urlPequena,
                                            contentDescription = foto.nombreAutor,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(150.dp)
                                        )
                                        Text(
                                            text = foto.nombreAutor,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                val nuevoEstado = !esFavorito
                                                repository.toggleFavorite(foto.id, nuevoEstado)
                                                favoritos = favoritos + (foto.id to nuevoEstado)
                                            }
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (esFavorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                            contentDescription = "Favorito",
                                            tint = if (esFavorito) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}