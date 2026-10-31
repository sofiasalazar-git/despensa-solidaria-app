package com.despensa.solidaria.ui.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.despensa.solidaria.data.Graph
import com.despensa.solidaria.ui.componentes.BarraInferior
import com.despensa.solidaria.ui.componentes.ContenidoEstado
import com.despensa.solidaria.ui.componentes.PantallaTopBar
import com.despensa.solidaria.ui.componentes.PantallaVacia
import com.despensa.solidaria.ui.componentes.ProductoCard
import com.despensa.solidaria.ui.componentes.RecargarAlReconectar

@Composable
fun PantallaAlertas(onIr: (String) -> Unit, onAbrirProducto: (Int) -> Unit) {
    val vm: AlertasViewModel = viewModel(factory = vmFactory { AlertasViewModel(Graph.repo) })
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.cargar() }
    RecargarAlReconectar { vm.cargar() }
    var pestana by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = { PantallaTopBar("Alertas") },
        bottomBar = { BarraInferior("alertas", onIr) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        ContenidoEstado(vm.estado, vm::cargar, Modifier.padding(padding)) { alertas ->
            Column {
                TabRow(selectedTabIndex = pestana, containerColor = Color.White, contentColor = MaterialTheme.colorScheme.primary) {
                    Tab(selected = pestana == 0, onClick = { pestana = 0 }, text = { Text("Por vencer (${alertas.porVencer.size})") })
                    Tab(selected = pestana == 1, onClick = { pestana = 1 }, text = { Text("Bajo stock (${alertas.bajoStock.size})") })
                }
                if (pestana == 0) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(3, 7, 15, 30).forEach { d ->
                            FilterChip(selected = vm.dias == d, onClick = { vm.cambiarDias(d) }, label = { Text("$d días") })
                        }
                    }
                }
                val lista = if (pestana == 0) alertas.porVencer else alertas.bajoStock
                if (lista.isEmpty()) {
                    PantallaVacia(
                        Icons.Filled.CheckCircle,
                        "Todo en orden",
                        if (pestana == 0) "No hay productos que venzan en los próximos ${vm.dias} días." else "Ningún producto está por debajo de su stock mínimo.",
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = if (pestana == 0) 0.dp else 12.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(lista, key = { it.id }) { p -> ProductoCard(p) { onAbrirProducto(p.id) } }
                    }
                }
            }
        }
    }
}
