package com.despensa.solidaria.ui.componentes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight

/** Barra superior verde + franja de conexión. La usan todas las pantallas. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTopBar(
    titulo: String,
    onBack: (() -> Unit)? = null,
    acciones: @Composable RowScope.() -> Unit = {},
) {
    Column {
        TopAppBar(
            title = { Text(titulo, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            },
            actions = acciones,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White,
                actionIconContentColor = Color.White,
            ),
        )
        BarraConexion()
    }
}

data class Destino(val ruta: String, val etiqueta: String, val icono: ImageVector)

val destinosPrincipales = listOf(
    Destino("inicio", "Inicio", Icons.Filled.Home),
    Destino("productos", "Productos", Icons.Filled.Inventory2),
    Destino("movimientos", "Movimientos", Icons.Filled.SwapVert),
    Destino("alertas", "Alertas", Icons.Filled.NotificationsActive),
)

@Composable
fun BarraInferior(actual: String, onIr: (String) -> Unit) {
    NavigationBar(containerColor = Color.White) {
        destinosPrincipales.forEach { destino ->
            NavigationBarItem(
                selected = destino.ruta == actual,
                onClick = { onIr(destino.ruta) },
                icon = { Icon(destino.icono, contentDescription = destino.etiqueta) },
                label = { Text(destino.etiqueta) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        }
    }
}
