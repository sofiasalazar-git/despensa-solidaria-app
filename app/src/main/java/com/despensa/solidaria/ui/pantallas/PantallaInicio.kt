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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.despensa.solidaria.data.Graph
import com.despensa.solidaria.data.Tipos
import com.despensa.solidaria.ui.componentes.BarraInferior
import com.despensa.solidaria.ui.componentes.ContenidoEstado
import com.despensa.solidaria.ui.componentes.MovimientoItem
import com.despensa.solidaria.ui.componentes.PantallaTopBar
import com.despensa.solidaria.ui.componentes.RecargarAlReconectar
import com.despensa.solidaria.ui.theme.Naranja
import com.despensa.solidaria.ui.theme.NaranjaClaro
import com.despensa.solidaria.ui.theme.Rojo
import com.despensa.solidaria.ui.theme.RojoClaro
import com.despensa.solidaria.ui.theme.Verde
import com.despensa.solidaria.ui.theme.VerdeClaro
import com.despensa.solidaria.ui.theme.VerdeOscuro

@Composable
fun PantallaInicio(
    onIr: (String) -> Unit,
    onAbrirMovimiento: (Int) -> Unit,
    onNuevoMovimiento: (String) -> Unit,
    onNuevoProducto: () -> Unit,
    onAjustes: () -> Unit,
) {
    val vm: InicioViewModel = viewModel(factory = vmFactory { InicioViewModel(Graph.repo) })
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.cargar() }
    RecargarAlReconectar { vm.cargar() }

    Scaffold(
        topBar = {
            PantallaTopBar("Despensa Solidaria", acciones = {
                IconButton(onClick = onAjustes) { Icon(Icons.Filled.Settings, "Ajustes") }
            })
        },
        bottomBar = { BarraInferior("inicio", onIr) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        ContenidoEstado(vm.estado, vm::cargar, Modifier.padding(padding)) { resumen ->
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                // Encabezado con degradado + tarjetas de resumen que "flotan" encima
                Box(Modifier.fillMaxWidth()) {
                    Box(
                        Modifier.fillMaxWidth().height(170.dp).background(
                            Brush.verticalGradient(listOf(Verde, VerdeOscuro)),
                            RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                        ).padding(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Column {
                            Text("Comedor comunitario", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyMedium)
                            Text("Control de inventario", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text("Cada alimento cuenta", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 112.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        TarjetaResumen(Icons.Filled.Inventory2, resumen.totalProductos.toString(), "Productos", VerdeClaro, Verde, Modifier.weight(1f)) { onIr("productos") }
                        TarjetaResumen(Icons.Filled.Timer, resumen.porVencer.size.toString(), "Por vencer", NaranjaClaro, Naranja, Modifier.weight(1f)) { onIr("alertas") }
                        TarjetaResumen(Icons.Filled.Warning, resumen.bajoStock.size.toString(), "Bajo stock", RojoClaro, Rojo, Modifier.weight(1f)) { onIr("alertas") }
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (resumen.porVencer.isNotEmpty() || resumen.bajoStock.isNotEmpty()) {
                    Card(
                        onClick = { onIr("alertas") },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = NaranjaClaro),
                        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.NotificationsActive, null, tint = Naranja)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Atención", fontWeight = FontWeight.Bold, color = Color(0xFF5A2A00))
                                Text(
                                    "${resumen.porVencer.size} por vencer esta semana y ${resumen.bajoStock.size} con bajo stock",
                                    style = MaterialTheme.typography.bodySmall, color = Color(0xFF5A2A00),
                                )
                            }
                            Icon(Icons.Filled.ChevronRight, null, tint = Naranja)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                Text("Acciones rápidas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(8.dp))
                Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BotonAccion("Entrada", Icons.Filled.ArrowDownward, Verde, Modifier.weight(1f)) { onNuevoMovimiento(Tipos.ENTRADA) }
                    BotonAccion("Salida", Icons.Filled.ArrowUpward, Naranja, Modifier.weight(1f)) { onNuevoMovimiento(Tipos.SALIDA) }
                    BotonAccion("Producto", Icons.Filled.AddCircle, VerdeOscuro, Modifier.weight(1f)) { onNuevoProducto() }
                }

                Spacer(Modifier.height(20.dp))
                Row(Modifier.padding(start = 16.dp, end = 8.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Últimos movimientos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    TextButton(onClick = { onIr("movimientos") }) { Text("Ver todo") }
                }
                if (resumen.ultimos.isEmpty()) {
                    Text(
                        "Aún no hay movimientos registrados.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                } else {
                    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        resumen.ultimos.forEach { item ->
                            MovimientoItem(item.mov, item.producto) { onAbrirMovimiento(item.mov.id) }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun TarjetaResumen(
    icono: ImageVector,
    valor: String,
    etiqueta: String,
    fondo: Color,
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier,
    ) {
        Column(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(fondo), contentAlignment = Alignment.Center) {
                Icon(icono, null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text(valor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(etiqueta, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun BotonAccion(texto: String, icono: ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = modifier,
    ) {
        Column(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icono, null, tint = Color.White, modifier = Modifier.size(26.dp))
            Spacer(Modifier.height(4.dp))
            Text(texto, color = Color.White, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}
