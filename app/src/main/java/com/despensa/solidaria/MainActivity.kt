package com.despensa.solidaria

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.despensa.solidaria.data.EstadoConexion
import com.despensa.solidaria.data.Graph
import com.despensa.solidaria.ui.AppNav
import com.despensa.solidaria.ui.theme.DespensaTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )

        // Mientras la app está visible, pregunta al servidor cada pocos segundos si sigue conectado.
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    Graph.monitor.verificar()
                    delay(if (Graph.monitor.estado.value == EstadoConexion.CONECTADO) 10_000L else 4_000L)
                }
            }
        }

        setContent {
            DespensaTheme {
                AppNav()
            }
        }
    }
}
