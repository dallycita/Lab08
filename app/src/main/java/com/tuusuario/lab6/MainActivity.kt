package com.tuusuario.lab6

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import com.tuusuario.lab6.navegacion.AppNavegacion
import com.tuusuario.lab6.ui.theme.Lab6Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // para tema claro/oscuro
            var oscuro by rememberSaveable { mutableStateOf(false) }

            Lab6Theme(darkTheme = oscuro) {
                AppNavegacion(
                    esOscuro = oscuro,
                    cambiarTema = { oscuro = !oscuro }
                )
            }
        }
    }
}
