package com.tuusuario.lab6.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Favoritos(
    irADetalle: (String) -> Unit,
    volver: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val api = remember { ClientePexels.crear(ServicioPexels::class.java) }
    val repository = remember { PhotoRepository(api, db.photoDao(), db.recentSearchDao()) }

    val scope = rememberCoroutineScope()

    val todosFavoritos by repository.getFavorites().collectAsState(initial = emptyList())

    var filtroAutor by remember { mutableStateOf<String?>(null) }
    var mostrarMenuAutores by remember { mutableStateOf(false) }

    // Lista de autores únicos
    val autores = remember(todosFavoritos) {
        todosFavoritos.map { it.nombreAutor }.distinct().sorted()
    }

    // Fotos filtradas
    val fotosFiltradas = remember(todosFavoritos, filtroAutor) {
        if (filtroAutor == null) {
            todosFavoritos
        } else {
            todosFavoritos.filter { it.nombreAutor == filtroAutor }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Favoritos")
                        if (filtroAutor != null) {
                            Text(
                                "Autor: $filtroAutor",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = volver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás"
                        )
                    }
                },
                actions = {
                    if (autores.isNotEmpty()) {
                        IconButton(onClick = { mostrarMenuAutores = true }) {
                            Icon(Icons.Filled.FilterList, contentDescription = "Filtrar por autor")
                        }

                        DropdownMenu(
                            expanded = mostrarMenuAutores,
                            onDismissRequest = { mostrarMenuAutores = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Todos") },
                                onClick = {
                                    filtroAutor = null
                                    mostrarMenuAutores = false
                                }
                            )
                            Divider()
                            autores.forEach { autor ->
                                DropdownMenuItem(
                                    text = { Text(autor) },
                                    onClick = {
                                        filtroAutor = autor
                                        mostrarMenuAutores = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { inner ->
        when {
            todosFavoritos.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .padding(inner)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text("No hay fotos favoritas aún")
                        Text(
                            "Marca fotos con ♥ para verlas aquí",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
            fotosFiltradas.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .padding(inner)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("No hay fotos de este autor")
                        TextButton(onClick = { filtroAutor = null }) {
                            Text("Ver todos")
                        }
                    }
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .padding(inner)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(fotosFiltradas.size) { index ->
                        val foto = fotosFiltradas[index]

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

                                // Indicador de favorito (siempre visible en esta pantalla)
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            repository.toggleFavorite(foto.id, false)
                                        }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Favorite,
                                        contentDescription = "Quitar de favoritos",
                                        tint = MaterialTheme.colorScheme.error
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