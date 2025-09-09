package com.tuusuario.lab6.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp

// pantalla de perfil con opción de cambiar tema
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Perfil(
    esOscuro: Boolean,
    cambiarTema: () -> Unit,
    volver: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
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
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // avatar simple
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "Avatar",
                modifier = Modifier.size(64.dp)
            )
            // datos mock
            Text("Nombre: Estudiante UVG")
            Text("Email: estudiante@uvg.edu.gt")

            // switch de tema
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(if (esOscuro) "Tema actual: oscuro" else "Tema actual: claro")
                Switch(
                    checked = esOscuro,
                    onCheckedChange = { cambiarTema() }
                )
            }
        }
    }
}
