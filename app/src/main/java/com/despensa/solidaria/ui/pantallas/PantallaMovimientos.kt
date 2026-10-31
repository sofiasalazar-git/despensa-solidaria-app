package com.despensa.solidaria.ui.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.despensa.solidaria.ui.componentes.PantallaVacia
import com.despensa.solidaria.ui.componentes.RecargarAlReconectar

@Composable
fun PantallaMovimientos(
    onIr: (String) -> Unit,
    onAbrirMovimiento: (Int) -> Unit,
    onNuevoMovimiento: () -> Unit,
) {
    val vm: MovimientosViewModel = viewModel(factory = vmFactory { MovimientosViewModel(Graph.repo) })
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.cargar() }
    RecargarAlReconectar { vm.cargar() }

    Scaffold(
        topBar = { PantallaTopBar("Movimientos") },
        bottomBar = { BarraInferior("movimientos", onIr) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNuevoMovimiento,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.White,
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("Registrar") },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(Modifier.padding(padding)) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = vm.filtroTipo == null, onClick = { vm.filtrar(null) }, label = { Text("Todos") })
                FilterChip(selected = vm.filtroTipo == Tipos.ENTRADA, onClick = { vm.filtrar(Tipos.ENTRADA) }, label = { Text("Entradas") })
                FilterChip(selected = vm.filtroTipo == Tipos.SALIDA, onClick = { vm.filtrar(Tipos.SALIDA) }, label = { Text("Salidas") })
            }
            ContenidoEstado(vm.estado, vm::cargar) { datos ->
                if (datos.movimientos.isEmpty()) {
                    PantallaVacia(Icons.Filled.SwapVert, "Sin movimientos", "Aquí aparecerá el historial de entradas y salidas.")
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(datos.movimientos, key = { it.id }) { m ->
                            MovimientoItem(m, datos.nombres[m.productoId] ?: "Producto #${m.productoId}") { onAbrirMovimiento(m.id) }
                        }
                    }
                }
            }
        }
    }
}
