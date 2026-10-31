package com.despensa.solidaria.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.despensa.solidaria.data.EstadoConexion
import com.despensa.solidaria.data.Graph
import com.despensa.solidaria.data.ServerSettings
import com.despensa.solidaria.ui.componentes.PantallaTopBar
import com.despensa.solidaria.ui.theme.Ambar
import com.despensa.solidaria.ui.theme.Rojo
import com.despensa.solidaria.ui.theme.Verde
import kotlinx.coroutines.launch

@Composable
fun PantallaAjustes(onBack: () -> Unit) {
    val estado by Graph.monitor.estado.collectAsState()
    val urlGuardada by Graph.settings.url.collectAsState()
    var texto by remember { mutableStateOf(urlGuardada) }
    val alcance = rememberCoroutineScope()

    val color = when (estado) {
        EstadoConexion.CONECTADO -> Verde
        EstadoConexion.DESCONECTADO -> Rojo
        EstadoConexion.VERIFICANDO -> Ambar
    }
    val icono = when (estado) {
        EstadoConexion.CONECTADO -> Icons.Filled.Wifi
        EstadoConexion.DESCONECTADO -> Icons.Filled.WifiOff
        EstadoConexion.VERIFICANDO -> Icons.Filled.Sync
    }
    val titulo = when (estado) {
        EstadoConexion.CONECTADO -> "Conectado al servidor"
        EstadoConexion.DESCONECTADO -> "Sin conexión con el servidor"
        EstadoConexion.VERIFICANDO -> "Verificando…"
    }

    Scaffold(
        topBar = { PantallaTopBar("Ajustes del servidor", onBack = onBack) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(78.dp).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
                        Icon(icono, null, tint = Color.White, modifier = Modifier.size(40.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
                    Text(
                        ServerSettings.legible(urlGuardada),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = texto,
                        onValueChange = { texto = it },
                        label = { Text("Dirección de la API") },
                        placeholder = { Text("http://10.0.2.2:3000/api/") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        onClick = {
                            Graph.settings.guardarUrl(texto)
                            texto = Graph.settings.url.value
                            Graph.monitor.marcarVerificando()
                            alcance.launch { Graph.monitor.verificar() }
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                    ) { Text("Guardar y probar conexión", fontWeight = FontWeight.Bold) }
                    OutlinedButton(
                        onClick = {
                            Graph.settings.restaurarUrl()
                            texto = Graph.settings.url.value
                            Graph.monitor.marcarVerificando()
                            alcance.launch { Graph.monitor.verificar() }
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                    ) { Text("Restaurar valor por defecto") }
                }
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("¿Qué dirección uso?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("• Emulador de Android Studio: http://10.0.2.2:3000/api/", style = MaterialTheme.typography.bodySmall)
                    Text("• Celular físico: la IP del computador en la misma red WiFi, por ejemplo http://192.168.1.15:3000/api/", style = MaterialTheme.typography.bodySmall)
                    Text("• El puerto es el PORT del archivo .env del backend (3000 en tu proyecto).", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
