package com.despensa.solidaria.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.despensa.solidaria.data.Tipos
import com.despensa.solidaria.ui.pantallas.PantallaAjustes
import com.despensa.solidaria.ui.pantallas.PantallaAlertas
import com.despensa.solidaria.ui.pantallas.PantallaInicio
import com.despensa.solidaria.ui.pantallas.PantallaMovimientoDetalle
import com.despensa.solidaria.ui.pantallas.PantallaMovimientoForm
import com.despensa.solidaria.ui.pantallas.PantallaMovimientos
import com.despensa.solidaria.ui.pantallas.PantallaProductoDetalle
import com.despensa.solidaria.ui.pantallas.PantallaProductoForm
import com.despensa.solidaria.ui.pantallas.PantallaProductos

object Rutas {
    const val INICIO = "inicio"
    const val PRODUCTOS = "productos"
    const val MOVIMIENTOS = "movimientos"
    const val ALERTAS = "alertas"
    const val AJUSTES = "ajustes"
    const val PRODUCTO = "producto/{id}"
    const val PRODUCTO_NUEVO = "producto_nuevo"
    const val PRODUCTO_EDITAR = "producto_editar/{id}"
    const val MOVIMIENTO_NUEVO = "movimiento_nuevo/{productoId}/{tipo}"
    const val MOVIMIENTO = "movimiento/{id}"
}

@Composable
fun AppNav() {
    val nav: NavHostController = rememberNavController()

    // Navegación de la barra inferior: no apila pantallas, conserva el estado de cada pestaña.
    fun irAPestana(ruta: String) {
        nav.navigate(ruta) {
            popUpTo(Rutas.INICIO) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val argId = listOf(navArgument("id") { type = NavType.IntType })

    NavHost(navController = nav, startDestination = Rutas.INICIO) {
        composable(Rutas.INICIO) {
            PantallaInicio(
                onIr = ::irAPestana,
                onAbrirMovimiento = { nav.navigate("movimiento/$it") },
                onNuevoMovimiento = { tipo -> nav.navigate("movimiento_nuevo/0/$tipo") },
                onNuevoProducto = { nav.navigate(Rutas.PRODUCTO_NUEVO) },
                onAjustes = { nav.navigate(Rutas.AJUSTES) },
            )
        }
        composable(Rutas.PRODUCTOS) {
            PantallaProductos(
                onIr = ::irAPestana,
                onAbrirProducto = { nav.navigate("producto/$it") },
                onNuevoProducto = { nav.navigate(Rutas.PRODUCTO_NUEVO) },
            )
        }
        composable(Rutas.MOVIMIENTOS) {
            PantallaMovimientos(
                onIr = ::irAPestana,
                onAbrirMovimiento = { nav.navigate("movimiento/$it") },
                onNuevoMovimiento = { nav.navigate("movimiento_nuevo/0/${Tipos.ENTRADA}") },
            )
        }
        composable(Rutas.ALERTAS) {
            PantallaAlertas(
                onIr = ::irAPestana,
                onAbrirProducto = { nav.navigate("producto/$it") },
            )
        }
        composable(Rutas.AJUSTES) {
            PantallaAjustes(onBack = { nav.popBackStack() })
        }
        composable(Rutas.PRODUCTO, arguments = argId) { entrada ->
            val id = entrada.arguments?.getInt("id") ?: 0
            PantallaProductoDetalle(
                id = id,
                onBack = { nav.popBackStack() },
                onEditar = { nav.navigate("producto_editar/$it") },
                onMovimiento = { productoId, tipo -> nav.navigate("movimiento_nuevo/$productoId/$tipo") },
                onAbrirMovimiento = { nav.navigate("movimiento/$it") },
            )
        }
        composable(Rutas.PRODUCTO_NUEVO) {
            PantallaProductoForm(id = 0, onBack = { nav.popBackStack() }, onGuardado = { nav.popBackStack() })
        }
        composable(Rutas.PRODUCTO_EDITAR, arguments = argId) { entrada ->
            val id = entrada.arguments?.getInt("id") ?: 0
            PantallaProductoForm(id = id, onBack = { nav.popBackStack() }, onGuardado = { nav.popBackStack() })
        }
        composable(
            Rutas.MOVIMIENTO_NUEVO,
            arguments = listOf(
                navArgument("productoId") { type = NavType.IntType },
                navArgument("tipo") { type = NavType.StringType },
            ),
        ) { entrada ->
            val productoId = entrada.arguments?.getInt("productoId") ?: 0
            val tipo = entrada.arguments?.getString("tipo") ?: Tipos.ENTRADA
            PantallaMovimientoForm(
                productoId = productoId,
                tipoInicial = tipo,
                onBack = { nav.popBackStack() },
                onGuardado = { nav.popBackStack() },
            )
        }
        composable(Rutas.MOVIMIENTO, arguments = argId) { entrada ->
            val id = entrada.arguments?.getInt("id") ?: 0
            PantallaMovimientoDetalle(
                id = id,
                onBack = { nav.popBackStack() },
                onAbrirProducto = { nav.navigate("producto/$it") },
            )
        }
    }
}
