package com.tuusuario.lab6.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tuusuario.lab6.datos.Foto
import com.tuusuario.lab6.internet.ClientePexels
import com.tuusuario.lab6.internet.ServicioPexels
import com.tuusuario.lab6.internet.aFoto
import androidx.compose.ui.layout.ContentScale

// pantalla de detalles: trae la foto por id y muestra la imagen grande y el autor
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Detalles(
    fotoId: String,
    volver: () -> Unit
) {
    val api = remember { ClientePexels.crear(ServicioPexels::class.java) }
    var foto by remember { mutableStateOf<Foto?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var resolucion by remember { mutableStateOf<String?>(null) } //

    // pedir la foto real desde Pexels
    LaunchedEffect(fotoId) {
        try {
            cargando = true
            error = null
            val json = api.fotoPorId(fotoId)
            foto = json.aFoto()
            resolucion = "${json.width}×${json.height}" //
        } catch (e: Exception) {
            error = "No se pudo cargar la foto"
        } finally {
            cargando = false
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
                        contentScale = ContentScale.Crop, // opcional
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )
                    Text(text = "Autor: ${f.nombreAutor}")
                    Text(text = "ID: ${f.id}")
                    resolucion?.let { Text(text = "Resolución: $it") }
                }
            }
        }
    }
}
