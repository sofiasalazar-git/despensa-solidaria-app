package com.despensa.solidaria.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Los 11 endpoints del contrato REST del backend. */
interface DespensaApi {

    // ---- Productos ----
    @GET("productos")
    suspend fun productos(
        @Query("buscar") buscar: String?,
        @Query("categoria") categoria: String?,
        @Query("activo") activo: Boolean?,
    ): List<Producto>

    @GET("productos/{id}")
    suspend fun producto(@Path("id") id: Int): Producto

    @POST("productos")
    suspend fun crearProducto(@Body cuerpo: ProductoRequest): Producto

    @PUT("productos/{id}")
    suspend fun actualizarProducto(@Path("id") id: Int, @Body cuerpo: ProductoRequest): Producto

    @DELETE("productos/{id}")
    suspend fun desactivarProducto(@Path("id") id: Int): Response<Unit>

    @GET("productos/por-vencer")
    suspend fun porVencer(@Query("dias") dias: Int): List<Producto>

    @GET("productos/bajo-stock")
    suspend fun bajoStock(): List<Producto>

    // ---- Movimientos ----
    @GET("movimientos")
    suspend fun movimientos(
        @Query("productoId") productoId: Int?,
        @Query("tipo") tipo: String?,
    ): List<Movimiento>

    @GET("movimientos/{id}")
    suspend fun movimiento(@Path("id") id: Int): Movimiento

    @POST("movimientos")
    suspend fun registrarMovimiento(@Body cuerpo: MovimientoRequest): Movimiento

    @PATCH("movimientos/{id}")
    suspend fun corregirMovimiento(@Path("id") id: Int, @Body cuerpo: CorreccionRequest): Movimiento
}
