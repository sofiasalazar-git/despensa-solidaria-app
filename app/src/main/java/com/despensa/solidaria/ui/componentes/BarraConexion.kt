package com.despensa.solidaria.ui.componentes

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.despensa.solidaria.data.EstadoConexion
import com.despensa.solidaria.data.Graph
import com.despensa.solidaria.data.ServerSettings
import com.despensa.solidaria.ui.theme.Ambar
import com.despensa.solidaria.ui.theme.Rojo
import com.despensa.solidaria.ui.theme.Verde

/**
 * Franja que aparece bajo la barra superior de TODAS las pantallas:
 *  - Verde + icono de WiFi  -> conectado al servidor
 *  - Rojo  + icono sin WiFi -> sin conexión (toca "Reintentar")
 *  - Ámbar + icono de sync  -> verificando
 */
@Composable
fun BarraConexion() {
    val estado by Graph.monitor.estado.collectAsState()
    val url by Graph.settings.url.collectAsState()

    val fondo by animateColorAsState(
        targetValue = when (estado) {
            EstadoConexion.CONECTADO -> Verde
            EstadoConexion.DESCONECTADO -> Rojo
            EstadoConexion.VERIFICANDO -> Ambar
        },
        label = "colorConexion",
    )
    val icono = when (estado) {
        EstadoConexion.CONECTADO -> Icons.Filled.Wifi
        EstadoConexion.DESCONECTADO -> Icons.Filled.WifiOff
        EstadoConexion.VERIFICANDO -> Icons.Filled.Sync
    }
    val texto = when (estado) {
        EstadoConexion.CONECTADO -> "Conectado al servidor"
        EstadoConexion.DESCONECTADO -> "Sin conexión con el servidor"
        EstadoConexion.VERIFICANDO -> "Verificando conexión…"
    }

    Row(
        Modifier.fillMaxWidth().background(fondo).padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icono, contentDescription = texto, tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            texto,
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        if (estado == EstadoConexion.DESCONECTADO) {
            Text(
                "Reintentar",
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    Graph.monitor.marcarVerificando()
                    Graph.monitor.verificarYa()
                },
            )
        } else {
            Text(
                ServerSettings.legible(url),
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}
