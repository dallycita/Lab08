package com.tuusuario.lab6.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tuusuario.lab6.datos.Foto
import com.tuusuario.lab6.internet.ClientePexels
import com.tuusuario.lab6.internet.ServicioPexels
import com.tuusuario.lab6.internet.aFoto
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.debounce
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Inicio(
    irADetalle: (String) -> Unit,
    irAPerfil: () -> Unit
) {
    val api = remember { ClientePexels.crear(ServicioPexels::class.java) }

    // estados de UI
    var textoBuscado by remember { mutableStateOf("") }
    var visibles by remember { mutableStateOf(listOf<Foto>()) }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val gridState = rememberLazyGridState()

    // estados para paginación
    var pagina by remember { mutableStateOf(1) }
    var cargandoMas by remember { mutableStateOf(false) }

    // cuando cambia el texto, pedimos nuevas fotos (con debounce)
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
                    pagina = 1             // reiniciar página
                    val res = api.buscarFotos(
                        tema = q,
                        pagina = pagina,
                        porPagina = 20
                    )
                    visibles = res.photos.map { it.aFoto() } // reemplazar lista
                } catch (_: Exception) {
                    error = "No se pudieron cargar los resultados"
                } finally {
                    cargando = false
                }
            }
    }

    // cargar más cuando nos acercamos al final
    LaunchedEffect(gridState, textoBuscado, visibles) {
        snapshotFlow {
            val info = gridState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            Pair(lastVisible, visibles.size)
        }
            .distinctUntilChanged()
            .collectLatest { (lastVisible, total) ->
                val cercaDelFinal = lastVisible >= total - 6
                if (!cargando && !cargandoMas && error == null && textoBuscado.isNotBlank() && cercaDelFinal) {
                    try {
                        cargandoMas = true
                        val siguiente = pagina + 1
                        val res = api.buscarFotos(
                            tema = textoBuscado.trim(),
                            pagina = siguiente,
                            porPagina = 20
                        )
                        val nuevas = res.photos.map { it.aFoto() }
                        if (nuevas.isNotEmpty()) {
                            pagina = siguiente
                            visibles = visibles + nuevas
                        }
                    } catch (_: Exception) {
                        // silencioso
                    } finally {
                        cargandoMas = false
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fotos") },
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
                            Card(
                                onClick = { irADetalle(foto.id) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
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
                        }
                    }
                }
            }
        }
    }
}
