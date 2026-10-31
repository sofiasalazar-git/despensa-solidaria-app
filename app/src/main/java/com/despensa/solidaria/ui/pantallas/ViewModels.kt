package com.despensa.solidaria.ui.pantallas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.despensa.solidaria.data.CorreccionRequest
import com.despensa.solidaria.data.Graph
import com.despensa.solidaria.data.Movimiento
import com.despensa.solidaria.data.MovimientoRequest
import com.despensa.solidaria.data.Producto
import com.despensa.solidaria.data.ProductoRequest
import com.despensa.solidaria.data.Repository
import com.despensa.solidaria.data.Resultado
import com.despensa.solidaria.data.Tipos
import com.despensa.solidaria.data.valorONull
import com.despensa.solidaria.ui.componentes.UiState
import com.despensa.solidaria.util.leerDecimal
import com.despensa.solidaria.util.tieneMaximoDosDecimales
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Suppress("UNCHECKED_CAST")
inline fun <reified VM : ViewModel> vmFactory(crossinline crear: () -> VM): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = crear() as T
    }

private fun <T> Resultado<T>.aUiState(): UiState<T> = when (this) {
    is Resultado.Ok -> UiState.Exito(dato)
    is Resultado.Fallo -> UiState.Error(mensaje, sinConexion)
}

// ------------------------------------------------------------------ Inicio
data class MovimientoConNombre(val mov: Movimiento, val producto: String)

data class ResumenInicio(
    val totalProductos: Int,
    val porVencer: List<Producto>,
    val bajoStock: List<Producto>,
    val ultimos: List<MovimientoConNombre>,
)

class InicioViewModel(private val repo: Repository) : ViewModel() {
    var estado by mutableStateOf<UiState<ResumenInicio>>(UiState.Cargando)
        private set

    fun cargar() {
        viewModelScope.launch {
            if (estado !is UiState.Exito) estado = UiState.Cargando
            val (productos, vencer, bajo, movs, nombres) = coroutineScope {
                val a = async { repo.productos(activo = true) }
                val b = async { repo.porVencer(7) }
                val c = async { repo.bajoStock() }
                val d = async { repo.movimientos() }
                val e = async { repo.nombresProductos() }
                Quintuple(a.await(), b.await(), c.await(), d.await(), e.await())
            }
            val fallo = listOf(productos, vencer, bajo, movs).filterIsInstance<Resultado.Fallo>().firstOrNull()
            if (fallo != null) {
                estado = UiState.Error(fallo.mensaje, fallo.sinConexion)
                return@launch
            }
            estado = UiState.Exito(
                ResumenInicio(
                    totalProductos = productos.valorONull().orEmpty().size,
                    porVencer = vencer.valorONull().orEmpty(),
                    bajoStock = bajo.valorONull().orEmpty(),
                    ultimos = movs.valorONull().orEmpty().take(5).map {
                        MovimientoConNombre(it, nombres[it.productoId] ?: "Producto #${it.productoId}")
                    },
                )
            )
        }
    }
}

data class Quintuple<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)

// --------------------------------------------------------------- Productos
class ProductosViewModel(private val repo: Repository) : ViewModel() {
    var estado by mutableStateOf<UiState<List<Producto>>>(UiState.Cargando)
        private set
    var busqueda by mutableStateOf("")
    var categoria by mutableStateOf<String?>(null)
    var verDesactivados by mutableStateOf(false)
        private set

    fun cargar() {
        viewModelScope.launch {
            if (estado !is UiState.Exito) estado = UiState.Cargando
            estado = repo.productos(activo = !verDesactivados).aUiState()
        }
    }

    fun alternarDesactivados() {
        verDesactivados = !verDesactivados
        categoria = null
        estado = UiState.Cargando
        cargar()
    }
}

// ------------------------------------------------------- Detalle de producto
data class DetalleProducto(val producto: Producto, val movimientos: List<Movimiento>)

class ProductoDetalleViewModel(private val repo: Repository, private val id: Int) : ViewModel() {
    var estado by mutableStateOf<UiState<DetalleProducto>>(UiState.Cargando)
        private set
    var procesando by mutableStateOf(false)
        private set
    var mensaje by mutableStateOf<String?>(null)

    fun cargar() {
        viewModelScope.launch {
            if (estado !is UiState.Exito) estado = UiState.Cargando
            val (p, m) = coroutineScope {
                val a = async { repo.producto(id) }
                val b = async { repo.movimientos(productoId = id) }
                a.await() to b.await()
            }
            estado = when {
                p is Resultado.Fallo -> UiState.Error(p.mensaje, p.sinConexion)
                m is Resultado.Fallo -> UiState.Error(m.mensaje, m.sinConexion)
                else -> UiState.Exito(DetalleProducto(p.valorONull()!!, m.valorONull().orEmpty()))
            }
        }
    }

    fun desactivar(alTerminar: () -> Unit) {
        viewModelScope.launch {
            procesando = true
            when (val r = repo.desactivarProducto(id)) {
                is Resultado.Ok -> alTerminar()
                is Resultado.Fallo -> mensaje = r.mensaje
            }
            procesando = false
        }
    }
}

// ------------------------------------------------------ Formulario producto
class ProductoFormViewModel(private val repo: Repository, private val id: Int) : ViewModel() {
    val esEdicion = id > 0

    var nombre by mutableStateOf("")
    var categoria by mutableStateOf("")
    var unidad by mutableStateOf("kg")
    var stockMinimo by mutableStateOf("0")
    var fechaVencimiento by mutableStateOf<String?>(null)

    var errorNombre by mutableStateOf<String?>(null)
        private set
    var errorCategoria by mutableStateOf<String?>(null)
        private set
    var errorStock by mutableStateOf<String?>(null)
        private set
    var errorServidor by mutableStateOf<String?>(null)
        private set
    var cargando by mutableStateOf(esEdicion)
        private set
    var guardando by mutableStateOf(false)
        private set
    var errorCarga by mutableStateOf<String?>(null)
        private set
    var stockActual by mutableStateOf<Double?>(null)
        private set

    private var yaCargado = false

    fun cargar() {
        if (!esEdicion || yaCargado) return
        viewModelScope.launch {
            cargando = true
            errorCarga = null
            when (val r = repo.producto(id)) {
                is Resultado.Ok -> {
                    val p = r.dato
                    nombre = p.nombre
                    categoria = p.categoria
                    unidad = p.unidad
                    stockMinimo = com.despensa.solidaria.util.formatoCantidad(p.stockMinimo)
                    fechaVencimiento = p.fechaVencimiento
                    stockActual = p.stockActual
                    yaCargado = true
                }
                is Resultado.Fallo -> errorCarga = r.mensaje
            }
            cargando = false
        }
    }

    private fun validar(): Boolean {
        errorNombre = when {
            nombre.trim().length < 2 -> "El nombre debe tener entre 2 y 100 caracteres."
            nombre.trim().length > 100 -> "El nombre no puede superar 100 caracteres."
            else -> null
        }
        errorCategoria = when {
            categoria.trim().isEmpty() -> "La categoría es obligatoria."
            categoria.trim().length > 50 -> "Máximo 50 caracteres."
            else -> null
        }
        val minimo = leerDecimal(stockMinimo)
        errorStock = when {
            minimo == null -> "Escribe un número (puede ser 0)."
            minimo < 0 -> "Debe ser mayor o igual a 0."
            !tieneMaximoDosDecimales(stockMinimo) -> "Máximo 2 decimales."
            else -> null
        }
        return errorNombre == null && errorCategoria == null && errorStock == null
    }

    fun guardar(alTerminar: () -> Unit) {
        errorServidor = null
        if (!validar()) return
        viewModelScope.launch {
            guardando = true
            val cuerpo = ProductoRequest(
                nombre = nombre.trim(),
                categoria = categoria.trim(),
                unidad = unidad,
                stockMinimo = leerDecimal(stockMinimo) ?: 0.0,
                fechaVencimiento = fechaVencimiento,
            )
            val r = if (esEdicion) repo.actualizarProducto(id, cuerpo) else repo.crearProducto(cuerpo)
            when (r) {
                is Resultado.Ok -> alTerminar()
                is Resultado.Fallo -> errorServidor = r.mensaje
            }
            guardando = false
        }
    }
}

// ------------------------------------------------------ Formulario movimiento
class MovimientoFormViewModel(
    private val repo: Repository,
    private val productoInicial: Int,
    tipoInicial: String,
) : ViewModel() {
    var productos by mutableStateOf<List<Producto>>(emptyList())
        private set
    var producto by mutableStateOf<Producto?>(null)
    var tipo by mutableStateOf(tipoInicial)
    var cantidad by mutableStateOf("")
    var responsable by mutableStateOf(Graph.settings.ultimoResponsable)
    var observacion by mutableStateOf("")

    var errorProducto by mutableStateOf<String?>(null)
        private set
    var errorCantidad by mutableStateOf<String?>(null)
        private set
    var errorResponsable by mutableStateOf<String?>(null)
        private set
    var errorServidor by mutableStateOf<String?>(null)
        private set
    var cargando by mutableStateOf(true)
        private set
    var errorCarga by mutableStateOf<String?>(null)
        private set
    var guardando by mutableStateOf(false)
        private set

    fun cargar() {
        viewModelScope.launch {
            cargando = true
            errorCarga = null
            when (val r = repo.productos(activo = true)) {
                is Resultado.Ok -> {
                    productos = r.dato
                    if (producto == null && productoInicial > 0) producto = r.dato.firstOrNull { it.id == productoInicial }
                }
                is Resultado.Fallo -> errorCarga = r.mensaje
            }
            cargando = false
        }
    }

    /** true si se está intentando sacar más de lo que hay (el servidor responderá 409). */
    fun excedeExistencia(): Boolean {
        val p = producto ?: return false
        val c = leerDecimal(cantidad) ?: return false
        return tipo == Tipos.SALIDA && c > p.stockActual
    }

    private fun validar(): Boolean {
        errorProducto = if (producto == null) "Elige un producto." else null
        val c = leerDecimal(cantidad)
        errorCantidad = when {
            c == null -> "Escribe la cantidad."
            c <= 0 -> "La cantidad debe ser mayor que 0."
            !tieneMaximoDosDecimales(cantidad) -> "Máximo 2 decimales."
            else -> null
        }
        errorResponsable = when {
            responsable.trim().isEmpty() -> "El responsable es obligatorio."
            responsable.trim().length > 80 -> "Máximo 80 caracteres."
            else -> null
        }
        return errorProducto == null && errorCantidad == null && errorResponsable == null
    }

    fun guardar(alTerminar: (Movimiento) -> Unit) {
        errorServidor = null
        if (!validar()) return
        viewModelScope.launch {
            guardando = true
            val cuerpo = MovimientoRequest(
                productoId = producto!!.id,
                tipo = tipo,
                cantidad = leerDecimal(cantidad) ?: 0.0,
                responsable = responsable.trim(),
                observacion = observacion.trim().ifEmpty { null },
            )
            when (val r = repo.registrarMovimiento(cuerpo)) {
                is Resultado.Ok -> {
                    Graph.settings.ultimoResponsable = responsable.trim()
                    alTerminar(r.dato)
                }
                is Resultado.Fallo -> errorServidor = r.mensaje
            }
            guardando = false
        }
    }
}

// ---------------------------------------------------------------- Movimientos
data class ListaMovimientos(
    val movimientos: List<Movimiento>,
    val nombres: Map<Int, String>,
)

class MovimientosViewModel(private val repo: Repository) : ViewModel() {
    var estado by mutableStateOf<UiState<ListaMovimientos>>(UiState.Cargando)
        private set
    var filtroTipo by mutableStateOf<String?>(null)
        private set

    fun cargar() {
        viewModelScope.launch {
            if (estado !is UiState.Exito) estado = UiState.Cargando
            val (m, n) = coroutineScope {
                val a = async { repo.movimientos(tipo = filtroTipo) }
                val b = async { repo.nombresProductos() }
                a.await() to b.await()
            }
            estado = when (m) {
                is Resultado.Ok -> UiState.Exito(ListaMovimientos(m.dato, n))
                is Resultado.Fallo -> UiState.Error(m.mensaje, m.sinConexion)
            }
        }
    }

    fun filtrar(tipo: String?) {
        filtroTipo = tipo
        estado = UiState.Cargando
        cargar()
    }
}

// ----------------------------------------------------------- Detalle movimiento
data class DetalleMovimiento(val movimiento: Movimiento, val producto: Producto?)

class MovimientoDetalleViewModel(private val repo: Repository, private val id: Int) : ViewModel() {
    var estado by mutableStateOf<UiState<DetalleMovimiento>>(UiState.Cargando)
        private set
    var editando by mutableStateOf(false)
        private set
    var responsable by mutableStateOf("")
    var observacion by mutableStateOf("")
    var errorResponsable by mutableStateOf<String?>(null)
        private set
    var errorServidor by mutableStateOf<String?>(null)
        private set
    var guardando by mutableStateOf(false)
        private set

    fun cargar() {
        if (editando) return
        viewModelScope.launch {
            if (estado !is UiState.Exito) estado = UiState.Cargando
            when (val m = repo.movimiento(id)) {
                is Resultado.Ok -> {
                    val p = repo.producto(m.dato.productoId).valorONull()
                    estado = UiState.Exito(DetalleMovimiento(m.dato, p))
                }
                is Resultado.Fallo -> estado = UiState.Error(m.mensaje, m.sinConexion)
            }
        }
    }

    fun empezarEdicion(m: Movimiento) {
        responsable = m.responsable
        observacion = m.observacion.orEmpty()
        errorResponsable = null
        errorServidor = null
        editando = true
    }

    fun cancelarEdicion() {
        editando = false
    }

    fun guardar() {
        errorServidor = null
        errorResponsable = when {
            responsable.trim().isEmpty() -> "El responsable es obligatorio."
            responsable.trim().length > 80 -> "Máximo 80 caracteres."
            else -> null
        }
        if (errorResponsable != null) return
        if (observacion.length > 200) {
            errorServidor = "La observación no puede superar 200 caracteres."
            return
        }
        viewModelScope.launch {
            guardando = true
            when (val r = repo.corregirMovimiento(id, CorreccionRequest(responsable.trim(), observacion.trim()))) {
                is Resultado.Ok -> {
                    val actual = (estado as? UiState.Exito)?.datos
                    estado = UiState.Exito(DetalleMovimiento(r.dato, actual?.producto))
                    editando = false
                }
                is Resultado.Fallo -> errorServidor = r.mensaje
            }
            guardando = false
        }
    }
}

// -------------------------------------------------------------------- Alertas
data class Alertas(val porVencer: List<Producto>, val bajoStock: List<Producto>)

class AlertasViewModel(private val repo: Repository) : ViewModel() {
    var estado by mutableStateOf<UiState<Alertas>>(UiState.Cargando)
        private set
    var dias by mutableStateOf(7)
        private set

    fun cargar() {
        viewModelScope.launch {
            if (estado !is UiState.Exito) estado = UiState.Cargando
            val (v, b) = coroutineScope {
                val a = async { repo.porVencer(dias) }
                val c = async { repo.bajoStock() }
                a.await() to c.await()
            }
            estado = when {
                v is Resultado.Fallo -> UiState.Error(v.mensaje, v.sinConexion)
                b is Resultado.Fallo -> UiState.Error(b.mensaje, b.sinConexion)
                else -> UiState.Exito(Alertas(v.valorONull().orEmpty(), b.valorONull().orEmpty()))
            }
        }
    }

    fun cambiarDias(nuevo: Int) {
        dias = nuevo
        cargar()
    }
}
