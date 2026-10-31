package com.despensa.solidaria.ui.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
fun PantallaProductos(
    onIr: (String) -> Unit,
    onAbrirProducto: (Int) -> Unit,
    onNuevoProducto: () -> Unit,
) {
    val vm: ProductosViewModel = viewModel(factory = vmFactory { ProductosViewModel(Graph.repo) })
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.cargar() }
    RecargarAlReconectar { vm.cargar() }

    Scaffold(
        topBar = { PantallaTopBar("Productos") },
        bottomBar = { BarraInferior("productos", onIr) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNuevoProducto,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.White,
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("Nuevo producto") },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(Modifier.padding(padding)) {
            OutlinedTextField(
                value = vm.busqueda,
                onValueChange = { vm.busqueda = it },
                placeholder = { Text("Buscar producto…") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = {
                    if (vm.busqueda.isNotEmpty()) {
                        IconButton(onClick = { vm.busqueda = "" }) { Icon(Icons.Filled.Close, "Borrar búsqueda") }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp),
            )

            ContenidoEstado(vm.estado, vm::cargar) { productos ->
                val categorias = remember(productos) { productos.map { it.categoria }.distinct().sorted() }
                val filtrados = productos.filter { p ->
                    (vm.categoria == null || p.categoria == vm.categoria) &&
                        (vm.busqueda.isBlank() || p.nombre.contains(vm.busqueda.trim(), ignoreCase = true))
                }
                Column {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        item {
                            FilterChip(
                                selected = vm.verDesactivados,
                                onClick = { vm.alternarDesactivados() },
                                label = { Text("Desactivados") },
                            )
                        }
                        item {
                            FilterChip(selected = vm.categoria == null, onClick = { vm.categoria = null }, label = { Text("Todas") })
                        }
                        items(categorias) { cat ->
                            FilterChip(
                                selected = vm.categoria == cat,
                                onClick = { vm.categoria = if (vm.categoria == cat) null else cat },
                                label = { Text(cat) },
                            )
                        }
                    }
                    if (filtrados.isEmpty()) {
                        PantallaVacia(
                            Icons.Filled.Inventory2,
                            if (productos.isEmpty()) "Aún no hay productos" else "Sin resultados",
                            if (productos.isEmpty()) "Toca \"Nuevo producto\" para registrar el primero." else "Prueba con otra búsqueda o categoría.",
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(filtrados, key = { it.id }) { p -> ProductoCard(p) { onAbrirProducto(p.id) } }
                        }
                    }
                }
            }
        }
    }
}
