package com.despensa.solidaria.ui.pantallas

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.despensa.solidaria.data.Graph
import com.despensa.solidaria.data.Tipos
import com.despensa.solidaria.ui.componentes.CampoSelector
import com.despensa.solidaria.ui.componentes.PantallaError
import com.despensa.solidaria.ui.componentes.PantallaTopBar
import com.despensa.solidaria.ui.theme.GrisClaro
import com.despensa.solidaria.ui.theme.Naranja
import com.despensa.solidaria.ui.theme.NaranjaClaro
import com.despensa.solidaria.ui.theme.Verde
import com.despensa.solidaria.ui.theme.VerdeClaro
import com.despensa.solidaria.util.cantidadConUnidad
import com.despensa.solidaria.util.formatoCantidad

/** productoId = 0 -> el usuario elige el producto en la lista. */
@Composable
fun PantallaMovimientoForm(productoId: Int, tipoInicial: String, onBack: () -> Unit, onGuardado: () -> Unit) {
    val vm: MovimientoFormViewModel = viewModel(
        factory = vmFactory { MovimientoFormViewModel(Graph.repo, productoId, tipoInicial) }
    )
    LaunchedEffect(Unit) { vm.cargar() }
    val contexto = LocalContext.current

    Scaffold(
        topBar = { PantallaTopBar("Registrar movimiento", onBack = onBack) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when {
            vm.cargando -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            vm.errorCarga != null -> PantallaError(vm.errorCarga ?: "", false, { vm.cargar() }, Modifier.padding(padding))
            else -> Column(
                Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OpcionTipo("Entrada", "Llega mercancía", Icons.Filled.ArrowDownward, Verde, VerdeClaro, vm.tipo == Tipos.ENTRADA, Modifier.weight(1f)) { vm.tipo = Tipos.ENTRADA }
                    OpcionTipo("Salida", "Sale de bodega", Icons.Filled.ArrowUpward, Naranja, NaranjaClaro, vm.tipo == Tipos.SALIDA, Modifier.weight(1f)) { vm.tipo = Tipos.SALIDA }
                }

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        CampoSelector(
                            etiqueta = "Producto",
                            texto = vm.producto?.nombre ?: "",
                            opciones = vm.productos,
                            textoOpcion = { "${it.nombre} (${cantidadConUnidad(it.stockActual, it.unidad)})" },
                            onSeleccion = { vm.producto = it },
                            esError = vm.errorProducto != null,
                            mensajeError = vm.errorProducto,
                        )
                        vm.producto?.let {
                            Text(
                                "Disponible: ${cantidadConUnidad(it.stockActual, it.unidad)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp),
                            )
                        }
                        OutlinedTextField(
                            value = vm.cantidad,
                            onValueChange = { vm.cantidad = it },
                            label = { Text("Cantidad" + (vm.producto?.let { " (${it.unidad})" } ?: "")) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = vm.errorCantidad != null,
                            supportingText = vm.errorCantidad?.let { { Text(it) } },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                        )
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
                            label = { Text("Observación (opcional)") },
                            minLines = 2,
                            maxLines = 4,
                            supportingText = { Text("${vm.observacion.length}/200") },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                if (vm.excedeExistencia()) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.Info, null, tint = Naranja, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Es más de lo que hay en bodega: el servidor rechazará la salida (409).",
                            style = MaterialTheme.typography.bodySmall,
                            color = Naranja,
                        )
                    }
                }

                vm.errorServidor?.let { MensajeError(it) }

                Button(
                    onClick = {
                        vm.guardar { mov ->
                            val resultante = mov.stockResultante?.let { " Existencia: ${formatoCantidad(it)}" } ?: ""
                            Toast.makeText(contexto, "Movimiento registrado.$resultante", Toast.LENGTH_LONG).show()
                            onGuardado()
                        }
                    },
                    enabled = !vm.guardando,
                    colors = ButtonDefaults.buttonColors(containerColor = if (vm.tipo == Tipos.ENTRADA) Verde else Naranja),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                ) {
                    if (vm.guardando) {
                        CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.Check, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (vm.tipo == Tipos.ENTRADA) "Registrar entrada" else "Registrar salida", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun OpcionTipo(
    titulo: String,
    detalle: String,
    icono: ImageVector,
    color: Color,
    fondo: Color,
    seleccionado: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (seleccionado) fondo else Color.White,
        border = BorderStroke(2.dp, if (seleccionado) color else GrisClaro),
        modifier = modifier,
    ) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icono, null, tint = if (seleccionado) color else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(4.dp))
            Text(titulo, fontWeight = FontWeight.Bold, color = if (seleccionado) color else MaterialTheme.colorScheme.onSurface)
            Text(detalle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
