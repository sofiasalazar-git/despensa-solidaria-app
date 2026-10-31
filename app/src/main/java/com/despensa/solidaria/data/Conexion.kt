package com.despensa.solidaria.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.Gson
import java.io.IOException
import java.util.concurrent.TimeUnit

/** Resultado de una llamada a la API: o trae el dato, o trae un mensaje de error listo para mostrar. */
sealed class Resultado<out T> {
    data class Ok<out T>(val dato: T) : Resultado<T>()
    data class Fallo(
        val mensaje: String,
        val codigo: Int? = null,
        val sinConexion: Boolean = false,
    ) : Resultado<Nothing>()
}

fun <T> Resultado<T>.valorONull(): T? = when (this) {
    is Resultado.Ok -> dato
    is Resultado.Fallo -> null
}

/** Guarda la dirección del servidor (editable desde Ajustes) y el último responsable usado. */
class ServerSettings(context: Context) {
    private val prefs = context.getSharedPreferences("despensa_prefs", Context.MODE_PRIVATE)

    private val _url = MutableStateFlow(prefs.getString(KEY_URL, DEFAULT_URL) ?: DEFAULT_URL)
    val url: StateFlow<String> = _url.asStateFlow()

    fun guardarUrl(nueva: String) {
        val limpia = normalizar(nueva)
        prefs.edit().putString(KEY_URL, limpia).apply()
        _url.value = limpia
    }

    fun restaurarUrl() = guardarUrl(DEFAULT_URL)

    var ultimoResponsable: String
        get() = prefs.getString(KEY_RESPONSABLE, "") ?: ""
        set(valor) { prefs.edit().putString(KEY_RESPONSABLE, valor).apply() }

    companion object {
        private const val KEY_URL = "base_url"
        private const val KEY_RESPONSABLE = "ultimo_responsable"

        /** 10.0.2.2 = el "localhost" del computador visto desde el emulador. Puerto 3000 = el del .env del backend. */
        const val DEFAULT_URL = "http://10.0.2.2:3000/api/"

        fun normalizar(entrada: String): String {
            var u = entrada.trim()
            if (!u.startsWith("http://") && !u.startsWith("https://")) u = "http://$u"
            if (!u.endsWith("/")) u += "/"
            if (!u.contains("/api")) u += "api/"
            return u
        }

        /** "http://10.0.2.2:3000/api/" -> "10.0.2.2:3000" */
        fun legible(url: String): String = Uri.parse(url).authority ?: url
    }
}

enum class EstadoConexion { VERIFICANDO, CONECTADO, DESCONECTADO }

/** Pregunta periódicamente a GET /api si el servidor responde. La interfaz solo observa [estado]. */
class ConnectionMonitor(private val settings: ServerSettings) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _estado = MutableStateFlow(EstadoConexion.VERIFICANDO)
    val estado: StateFlow<EstadoConexion> = _estado.asStateFlow()

    private val cliente = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .build()

    suspend fun verificar() {
        val url = settings.url.value
        val responde = withContext(Dispatchers.IO) {
            try {
                val peticion = Request.Builder().url(url).get().build()
                cliente.newCall(peticion).execute().use { it.isSuccessful }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                false
            }
        }
        _estado.value = if (responde) EstadoConexion.CONECTADO else EstadoConexion.DESCONECTADO
    }

    fun verificarYa() {
        scope.launch { verificar() }
    }

    fun marcarVerificando() {
        _estado.value = EstadoConexion.VERIFICANDO
    }
}

/** Acceso a la API. Reconstruye Retrofit si el usuario cambia la dirección del servidor. */
class Repository(
    private val settings: ServerSettings,
    private val monitor: ConnectionMonitor,
) {
    private val gson = Gson()
    private val http = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    private var apiActual: DespensaApi? = null
    private var urlActual: String? = null

    @Synchronized
    private fun api(): DespensaApi {
        val url = settings.url.value
        val existente = apiActual
        if (existente != null && urlActual == url) return existente
        val nueva = Retrofit.Builder()
            .baseUrl(url)
            .client(http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DespensaApi::class.java)
        apiActual = nueva
        urlActual = url
        return nueva
    }

    private suspend fun <T> llamar(bloque: suspend () -> T): Resultado<T> = withContext(Dispatchers.IO) {
        try {
            Resultado.Ok(bloque())
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            val cuerpo = try { e.response()?.errorBody()?.string() } catch (x: Exception) { null }
            val mensajeServidor = try { gson.fromJson(cuerpo, ApiErrorBody::class.java)?.mensaje } catch (x: Exception) { null }
            val mensaje = mensajeServidor ?: "El servidor respondió con el código ${e.code()}."
            Resultado.Fallo(mensaje, e.code())
        } catch (e: IOException) {
            monitor.verificarYa()
            Resultado.Fallo("No se pudo conectar con el servidor. Revisa que esté encendido y que la dirección sea correcta.", sinConexion = true)
        } catch (e: IllegalArgumentException) {
            Resultado.Fallo("La dirección del servidor no es válida. Corrígela en Ajustes.")
        } catch (e: Exception) {
            Resultado.Fallo("Ocurrió un error inesperado: ${e.message ?: "sin detalle"}")
        }
    }

    // ---- Productos ----
    suspend fun productos(buscar: String? = null, categoria: String? = null, activo: Boolean? = true) =
        llamar { api().productos(buscar, categoria, activo) }

    suspend fun producto(id: Int) = llamar { api().producto(id) }
    suspend fun crearProducto(cuerpo: ProductoRequest) = llamar { api().crearProducto(cuerpo) }
    suspend fun actualizarProducto(id: Int, cuerpo: ProductoRequest) = llamar { api().actualizarProducto(id, cuerpo) }

    suspend fun desactivarProducto(id: Int): Resultado<Unit> = llamar {
        val respuesta = api().desactivarProducto(id)
        if (!respuesta.isSuccessful) throw HttpException(respuesta)
        Unit
    }

    suspend fun porVencer(dias: Int = 7) = llamar { api().porVencer(dias) }
    suspend fun bajoStock() = llamar { api().bajoStock() }

    // ---- Movimientos ----
    suspend fun movimientos(productoId: Int? = null, tipo: String? = null) =
        llamar { api().movimientos(productoId, tipo) }

    suspend fun movimiento(id: Int) = llamar { api().movimiento(id) }
    suspend fun registrarMovimiento(cuerpo: MovimientoRequest) = llamar { api().registrarMovimiento(cuerpo) }
    suspend fun corregirMovimiento(id: Int, cuerpo: CorreccionRequest) = llamar { api().corregirMovimiento(id, cuerpo) }

    /** id -> nombre de TODOS los productos (activos y desactivados), para rotular el historial. */
    suspend fun nombresProductos(): Map<Int, String> {
        val activos = productos(activo = true).valorONull().orEmpty()
        val inactivos = productos(activo = false).valorONull().orEmpty()
        return (activos + inactivos).associate { it.id to it.nombre }
    }
}

/** Contenedor sencillo de dependencias (se inicia en DespensaApp). */
object Graph {
    lateinit var settings: ServerSettings
        private set
    lateinit var monitor: ConnectionMonitor
        private set
    lateinit var repo: Repository
        private set

    fun iniciar(contexto: Context) {
        if (::settings.isInitialized) return
        settings = ServerSettings(contexto.applicationContext)
        monitor = ConnectionMonitor(settings)
        repo = Repository(settings, monitor)
    }
}
