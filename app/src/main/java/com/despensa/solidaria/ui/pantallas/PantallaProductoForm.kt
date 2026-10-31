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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.viewmodel.compose.viewModel
import com.despensa.solidaria.data.CATEGORIAS_SUGERIDAS
import com.despensa.solidaria.data.Graph
import com.despensa.solidaria.data.UNIDADES
import com.despensa.solidaria.ui.componentes.CampoFecha
import com.despensa.solidaria.ui.componentes.CampoSelector
import com.despensa.solidaria.ui.componentes.PantallaError
import com.despensa.solidaria.ui.componentes.PantallaTopBar
import com.despensa.solidaria.util.cantidadConUnidad

/** id = 0 -> producto nuevo; id > 0 -> editar. */
@Composable
fun PantallaProductoForm(id: Int, onBack: () -> Unit, onGuardado: () -> Unit) {
    val vm: ProductoFormViewModel = viewModel(factory = vmFactory { ProductoFormViewModel(Graph.repo, id) })
    LaunchedEffect(Unit) { vm.cargar() }

    Scaffold(
        topBar = { PantallaTopBar(if (vm.esEdicion) "Editar producto" else "Nuevo producto", onBack = onBack) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when {
            vm.cargando -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            vm.errorCarga != null -> PantallaError(vm.errorCarga ?: "", false, { vm.cargar() }, Modifier.padding(padding))
            else -> Column(
                Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = vm.nombre,
                            onValueChange = { vm.nombre = it },
                            label = { Text("Nombre") },
                            singleLine = true,
                            isError = vm.errorNombre != null,
                            supportingText = vm.errorNombre?.let { { Text(it) } },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = vm.categoria,
                            onValueChange = { vm.categoria = it },
                            label = { Text("Categoría") },
                            singleLine = true,
                            isError = vm.errorCategoria != null,
                            supportingText = vm.errorCategoria?.let { { Text(it) } },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(CATEGORIAS_SUGERIDAS) { cat ->
                                FilterChip(selected = vm.categoria.equals(cat, ignoreCase = true), onClick = { vm.categoria = cat }, label = { Text(cat) })
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        CampoSelector(
                            etiqueta = "Unidad de medida",
                            texto = vm.unidad,
                            opciones = UNIDADES,
                            textoOpcion = { it },
                            onSeleccion = { vm.unidad = it },
                        )
                        OutlinedTextField(
                            value = vm.stockMinimo,
                            onValueChange = { vm.stockMinimo = it },
                            label = { Text("Stock mínimo (${vm.unidad})") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = vm.errorStock != null,
                            supportingText = { Text(vm.errorStock ?: "Se avisará cuando la existencia sea igual o menor.") },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        CampoFecha("Fecha de vencimiento", vm.fechaVencimiento, { vm.fechaVencimiento = it })
                    }
                }

                if (vm.esEdicion) {
                    vm.stockActual?.let {
                        Text(
                            "Existencia actual: ${cantidadConUnidad(it, vm.unidad)}. El stock solo cambia registrando entradas y salidas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    Text(
                        "El producto nuevo empieza con existencia 0. Registra una entrada para cargar stock.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                vm.errorServidor?.let { MensajeError(it) }

                Button(
                    onClick = { vm.guardar(onGuardado) },
                    enabled = !vm.guardando,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                ) {
                    if (vm.guardando) {
                        CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.Check, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (vm.esEdicion) "Guardar cambios" else "Crear producto", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

/** Cuadro rojo para mostrar el mensaje que devuelve el servidor (400, 404, 409…). */
@Composable
fun MensajeError(texto: String) {
    Row(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(14.dp)).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.ErrorOutline, null, tint = MaterialTheme.colorScheme.error)
        Spacer(Modifier.width(10.dp))
        Text(texto, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
    }
}
