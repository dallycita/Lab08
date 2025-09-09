package com.tuusuario.lab6.navegacion

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tuusuario.lab6.pantallas.Detalles
import com.tuusuario.lab6.pantallas.Inicio
import com.tuusuario.lab6.pantallas.Perfil

// objeto con las rutas principales de la app
object Rutas {
    const val INICIO = "inicio"
    const val DETALLES = "detalles"
    const val PERFIL = "perfil"
}

// función de navegación principal
// aquí se conecta el NavHost con las pantallas: inicio, detalles y perfil
@Composable
fun AppNavegacion(
    esOscuro: Boolean,
    cambiarTema: () -> Unit
) {
    // controlador de navegación
    val nav = rememberNavController()

    // definición del NavHost con la pantalla de inicio como la primera
    NavHost(navController = nav, startDestination = Rutas.INICIO) {

        // pantalla de inicio
        composable(Rutas.INICIO) {
            Inicio(
                irADetalle = { fotoId -> nav.navigate("${Rutas.DETALLES}/$fotoId") },
                irAPerfil = { nav.navigate(Rutas.PERFIL) }
            )
        }

        // pantalla de detalles, recibe el id de la foto como argumento
        composable(
            route = "${Rutas.DETALLES}/{fotoId}",
            arguments = listOf(navArgument("fotoId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("fotoId").orEmpty()
            Detalles(fotoId = id, volver = { nav.popBackStack() })
        }

        // pantalla de perfil con opción para cambiar entre tema claro/oscuro
        composable(Rutas.PERFIL) {
            Perfil(
                esOscuro = esOscuro,
                cambiarTema = cambiarTema,
                volver = { nav.popBackStack() }
            )
        }
    }
}
