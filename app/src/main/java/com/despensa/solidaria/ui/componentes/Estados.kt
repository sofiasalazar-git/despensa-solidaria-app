package com.despensa.solidaria.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.despensa.solidaria.data.EstadoConexion
import com.despensa.solidaria.data.Graph

/** Estado de una pantalla que carga datos del servidor. */
sealed interface UiState<out T> {
    data object Cargando : UiState<Nothing>
    data class Error(val mensaje: String, val sinConexion: Boolean = false) : UiState<Nothing>
    data class Exito<T>(val datos: T) : UiState<T>
}

@Composable
fun <T> ContenidoEstado(
    estado: UiState<T>,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier,
    contenido: @Composable (T) -> Unit,
) {
    when (estado) {
        is UiState.Cargando -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        is UiState.Error -> PantallaError(estado.mensaje, estado.sinConexion, onReintentar, modifier)
        is UiState.Exito -> Box(modifier.fillMaxSize()) { contenido(estado.datos) }
    }
}

@Composable
fun PantallaError(mensaje: String, sinConexion: Boolean, onReintentar: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.size(84.dp).clip(CircleShape).background(MaterialTheme.colorScheme.errorContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.CloudOff, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(42.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(
            if (sinConexion) "Sin conexión con el servidor" else "No se pudo completar la acción",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            mensaje,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onReintentar) { Text("Reintentar") }
    }
}

@Composable
fun PantallaVacia(icono: ImageVector, titulo: String, detalle: String, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.size(84.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icono, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(42.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(
            detalle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/** Vuelve a cargar los datos cuando el servidor pasa de "sin conexión" a "conectado". */
@Composable
fun RecargarAlReconectar(accion: () -> Unit) {
    val estado by Graph.monitor.estado.collectAsState()
    val accionActual by rememberUpdatedState(accion)
    var anterior by remember { mutableStateOf(estado) }
    LaunchedEffect(estado) {
        if (estado == EstadoConexion.CONECTADO && anterior != EstadoConexion.CONECTADO) accionActual()
        anterior = estado
    }
}
