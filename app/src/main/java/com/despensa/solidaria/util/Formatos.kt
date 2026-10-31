package com.despensa.solidaria.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val LOCALE_ES = Locale.forLanguageTag("es-CO")
private val FECHA_HORA = DateTimeFormatter.ofPattern("d MMM yyyy · HH:mm", LOCALE_ES)
private val FECHA_LARGA = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", LOCALE_ES)

/** 21.0 -> "21"; 2.5 -> "2.5"; 2.25 -> "2.25" */
fun formatoCantidad(valor: Double): String {
    if (valor % 1.0 == 0.0) return valor.toLong().toString()
    return String.format(Locale.US, "%.2f", valor).trimEnd('0').trimEnd('.')
}

fun cantidadConUnidad(valor: Double, unidad: String) = "${formatoCantidad(valor)} $unidad"

/** Días que faltan para vencer (negativo = ya venció). null si no tiene fecha. */
fun diasParaVencer(fecha: String?): Long? = try {
    if (fecha.isNullOrBlank()) null
    else ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(fecha.take(10)))
} catch (e: Exception) {
    null
}

fun textoVencimiento(dias: Long?): String? = when {
    dias == null -> null
    dias < 0L -> "Vencido"
    dias == 0L -> "Vence hoy"
    dias == 1L -> "Vence mañana"
    else -> "Vence en $dias días"
}

fun formatoFechaHora(iso: String?): String = try {
    if (iso.isNullOrBlank()) "" else LocalDateTime.parse(iso).format(FECHA_HORA)
} catch (e: Exception) {
    iso ?: ""
}

fun formatoFecha(iso: String?): String = try {
    if (iso.isNullOrBlank()) "Sin fecha" else LocalDate.parse(iso.take(10)).format(FECHA_LARGA)
} catch (e: Exception) {
    iso ?: ""
}

/** Acepta coma o punto decimal. */
fun leerDecimal(texto: String): Double? = texto.trim().replace(',', '.').toDoubleOrNull()

fun tieneMaximoDosDecimales(texto: String): Boolean {
    val limpio = texto.trim().replace(',', '.')
    val punto = limpio.indexOf('.')
    return punto < 0 || limpio.length - punto - 1 <= 2
}
