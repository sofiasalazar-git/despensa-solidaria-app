package com.despensa.solidaria.ui.pantallas

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.despensa.solidaria.data.Graph
import com.despensa.solidaria.data.Producto
import com.despensa.solidaria.data.Tipos
import com.despensa.solidaria.ui.componentes.AvatarCategoria
import com.despensa.solidaria.ui.componentes.BarraStock
import com.despensa.solidaria.ui.componentes.ContenidoEstado
import com.despensa.solidaria.ui.componentes.EtiquetasProducto
import com.despensa.solidaria.ui.componentes.MovimientoItem
import com.despensa.solidaria.ui.componentes.PantallaTopBar
import com.despensa.solidaria.ui.componentes.RecargarAlReconectar
import com.despensa.solidaria.ui.theme.Naranja
import com.despensa.solidaria.ui.theme.Rojo
import com.despensa.solidaria.ui.theme.Verde
import com.despensa.solidaria.util.cantidadConUnidad
import com.despensa.solidaria.util.diasParaVencer
import com.despensa.solidaria.util.formatoCantidad
import com.despensa.solidaria.util.formatoFecha
import com.despensa.solidaria.util.textoVencimiento

@Composable
fun PantallaProductoDetalle(
    id: Int,
    onBack: () -> Unit,
    onEditar: (Int) -> Unit,
    onMovimiento: (productoId: Int, tipo: String) -> Unit,
    onAbrirMovimiento: (Int) -> Unit,
) {
    val vm: ProductoDetalleViewModel = viewModel(factory = vmFactory { ProductoDetalleViewModel(Graph.repo, id) })
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.cargar() }
    RecargarAlReconectar { vm.cargar() }

    var confirmar by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(vm.mensaje) {
        vm.mensaje?.let {
            snackbar.showSnackbar(it)
            vm.mensaje = null
        }
    }

    val producto = (vm.estado as? com.despensa.solidaria.ui.componentes.UiState.Exito)?.datos?.producto

    Scaffold(
        topBar = {
            PantallaTopBar("Detalle del producto", onBack = onBack, acciones = {
                if (producto != null && producto.activo) {
                    IconButton(onClick = { onEditar(id) }) { Icon(Icons.Filled.Edit, "Editar") }
                    IconButton(onClick = { confirmar = true }) { Icon(Icons.Filled.Block, "Desactivar") }
                }
            })
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        ContenidoEstado(vm.estado, vm::cargar, Modifier.padding(padding)) { detalle ->
            val p = detalle.producto
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AvatarCategoria(p.categoria, p.activo, tamano = 60)
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text(p.nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                Text(p.categoria, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(Modifier.height(18.dp))
                        Text("Existencia actual", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            cantidadConUnidad(p.stockActual, p.unidad),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (p.stockActual <= p.stockMinimo) Rojo else Verde,
                        )
                        Spacer(Modifier.height(8.dp))
                        BarraStock(p)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Stock mínimo: ${cantidadConUnidad(p.stockMinimo, p.unidad)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(12.dp))
                        EtiquetasProducto(p)
                    }
                }

                Spacer(Modifier.height(12.dp))
                FilaVencimiento(p)

                Spacer(Modifier.height(16.dp))
                if (p.activo) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { onMovimiento(p.id, Tipos.ENTRADA) },
                            colors = ButtonDefaults.buttonColors(containerColor = Verde),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f).height(52.dp),
                        ) {
                            Icon(Icons.Filled.ArrowDownward, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Entrada")
                        }
                        Button(
                            onClick = { onMovimiento(p.id, Tipos.SALIDA) },
                            colors = ButtonDefaults.buttonColors(containerColor = Naranja),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f).height(52.dp),
                        ) {
                            Icon(Icons.Filled.ArrowUpward, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Salida")
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Warning, null, tint = Rojo)
                            Spacer(Modifier.width(10.dp))
                            Text("Este producto está desactivado y no admite nuevos movimientos.", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Spacer(Modifier.height(22.dp))
                Text("Historial del producto", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                if (detalle.movimientos.isEmpty()) {
                    Text("Todavía no tiene movimientos.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        detalle.movimientos.take(15).forEach { m ->
                            MovimientoItem(m, if (m.tipo == Tipos.ENTRADA) "Entrada" else "Salida", p.unidad) { onAbrirMovimiento(m.id) }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (confirmar) {
        AlertDialog(
            onDismissRequest = { confirmar = false },
            title = { Text("¿Desactivar producto?") },
            text = { Text("El producto dejará de aparecer en el inventario activo. Su historial de movimientos se conserva.") },
            confirmButton = {
                TextButton(
                    enabled = !vm.procesando,
                    onClick = {
                        confirmar = false
                        vm.desactivar { onBack() }
                    },
                ) { Text("Desactivar", color = Rojo) }
            },
            dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun FilaVencimiento(p: Producto) {
    val dias = diasParaVencer(p.fechaVencimiento)
    val color = when {
        dias == null -> MaterialTheme.colorScheme.onSurfaceVariant
        dias < 0 -> Rojo
        dias <= 7 -> Naranja
        else -> Verde
    }
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.CalendarMonth, null, tint = color)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Fecha de vencimiento", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    if (p.fechaVencimiento == null) "Sin fecha de vencimiento" else formatoFecha(p.fechaVencimiento),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                textoVencimiento(dias)?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = color, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
