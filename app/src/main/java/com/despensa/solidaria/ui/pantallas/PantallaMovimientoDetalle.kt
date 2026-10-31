package com.despensa.solidaria.ui.pantallas

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.despensa.solidaria.data.Graph
import com.despensa.solidaria.data.Tipos
import com.despensa.solidaria.ui.componentes.ContenidoEstado
import com.despensa.solidaria.ui.componentes.PantallaTopBar
import com.despensa.solidaria.ui.componentes.RecargarAlReconectar
import com.despensa.solidaria.ui.theme.Naranja
import com.despensa.solidaria.ui.theme.NaranjaClaro
import com.despensa.solidaria.ui.theme.Verde
import com.despensa.solidaria.ui.theme.VerdeClaro
import com.despensa.solidaria.util.cantidadConUnidad
import com.despensa.solidaria.util.formatoCantidad
import com.despensa.solidaria.util.formatoFechaHora

@Composable
fun PantallaMovimientoDetalle(id: Int, onBack: () -> Unit, onAbrirProducto: (Int) -> Unit) {
    val vm: MovimientoDetalleViewModel = viewModel(factory = vmFactory { MovimientoDetalleViewModel(Graph.repo, id) })
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.cargar() }
    RecargarAlReconectar { vm.cargar() }

    Scaffold(
        topBar = { PantallaTopBar("Detalle del movimiento", onBack = onBack) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        ContenidoEstado(vm.estado, vm::cargar, Modifier.padding(padding)) { detalle ->
            val m = detalle.movimiento
            val unidad = detalle.producto?.unidad ?: ""
            val esEntrada = m.tipo == Tipos.ENTRADA
            val color = if (esEntrada) Verde else Naranja
            val fondo = if (esEntrada) VerdeClaro else NaranjaClaro

            Column(
                Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = fondo),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(if (esEntrada) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward, null, tint = color, modifier = Modifier.size(36.dp))
                        Text(if (esEntrada) "ENTRADA" else "SALIDA", color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                        Text(
                            (if (esEntrada) "+" else "−") + cantidadConUnidad(m.cantidad, unidad).trim(),
                            color = color,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(formatoFechaHora(m.fecha), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Fila("Producto", detalle.producto?.nombre ?: "Producto #${m.productoId}")
                        HorizontalDivider()
                        if (!vm.editando) {
                            Fila("Responsable", m.responsable)
                            HorizontalDivider()
                            Fila("Observación", m.observacion?.ifBlank { null } ?: "Sin observación")
                        } else {
                            OutlinedTextField(
                                value = vm.responsable,
                                onValueChange = { vm.responsable = it },
                                label = { Text("Responsable") },
                                singleLine = true,
                                isError = vm.errorResponsable != null,
                                supportingText = vm.errorResponsable?.let { { Text(it) } },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth(),
                            )
                            OutlinedTextField(
                                value = vm.observacion,
                                onValueChange = { vm.observacion = it },
                                label = { Text("Observación") },
                                minLines = 2,
                                maxLines = 4,
                                supportingText = { Text("${vm.observacion.length}/200") },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                if (m.stockResultante != null) {
                    Text("Existencia resultante: ${formatoCantidad(m.stockResultante)} $unidad", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                vm.errorServidor?.let { MensajeError(it) }

                if (vm.editando) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { vm.cancelarEdicion() }, shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f).height(52.dp)) { Text("Cancelar") }
                        Button(onClick = { vm.guardar() }, enabled = !vm.guardando, shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f).height(52.dp)) {
                            if (vm.guardando) CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                            else {
                                Icon(Icons.Filled.Check, null)
                                Spacer(Modifier.width(6.dp))
                                Text("Guardar")
                            }
                        }
                    }
                } else {
                    Button(onClick = { vm.empezarEdicion(m) }, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Icon(Icons.Filled.Edit, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Corregir responsable / observación")
                    }
                    OutlinedButton(onClick = { onAbrirProducto(m.productoId) }, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Text("Ver producto")
                    }
                    Text(
                        "Por seguridad del historial, solo se pueden corregir el responsable y la observación. La cantidad, el tipo y el producto no cambian.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun Fila(etiqueta: String, valor: String) {
    Column {
        Text(etiqueta, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}
