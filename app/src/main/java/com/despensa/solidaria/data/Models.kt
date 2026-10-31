package com.despensa.solidaria.data

data class Producto(
    val id: Int,
    val nombre: String,
    val categoria: String,
    val unidad: String,
    val stockActual: Double,
    val stockMinimo: Double,
    val fechaVencimiento: String? = null,
    val activo: Boolean = true,
    val createdAt: String? = null,
)

data class ProductoRequest(
    val nombre: String,
    val categoria: String,
    val unidad: String,
    val stockMinimo: Double,
    val fechaVencimiento: String?,
)

data class Movimiento(
    val id: Int,
    val productoId: Int,
    val tipo: String,
    val cantidad: Double,
    val fecha: String,
    val responsable: String,
    val observacion: String? = null,
    val stockResultante: Double? = null,
)

data class MovimientoRequest(
    val productoId: Int,
    val tipo: String,
    val cantidad: Double,
    val responsable: String,
    val observacion: String?,
)

data class CorreccionRequest(
    val responsable: String,
    val observacion: String,
)

data class ApiErrorBody(
    val status: Int? = null,
    val error: String? = null,
    val mensaje: String? = null,
    val timestamp: String? = null,
)

object Tipos {
    const val ENTRADA = "ENTRADA"
    const val SALIDA = "SALIDA"
}

val UNIDADES = listOf("kg", "litros", "unidades")
val CATEGORIAS_SUGERIDAS = listOf("Granos", "Lácteos", "Carnes", "Verduras", "Enlatados", "Otros")
