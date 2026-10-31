package com.despensa.solidaria.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.despensa.solidaria.data.Movimiento
import com.despensa.solidaria.data.Producto
import com.despensa.solidaria.data.Tipos
import com.despensa.solidaria.ui.theme.GrisClaro
import com.despensa.solidaria.ui.theme.Naranja
import com.despensa.solidaria.ui.theme.NaranjaClaro
import com.despensa.solidaria.ui.theme.Rojo
import com.despensa.solidaria.ui.theme.RojoClaro
import com.despensa.solidaria.ui.theme.Verde
import com.despensa.solidaria.ui.theme.VerdeClaro
import com.despensa.solidaria.util.cantidadConUnidad
import com.despensa.solidaria.util.diasParaVencer
import com.despensa.solidaria.util.formatoCantidad
import com.despensa.solidaria.util.formatoFechaHora
import com.despensa.solidaria.util.textoVencimiento
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

fun iconoCategoria(categoria: String): ImageVector = when (categoria.trim().lowercase()) {
    "granos" -> Icons.Filled.Grain
    "lácteos", "lacteos" -> Icons.Filled.LocalDrink
    "carnes" -> Icons.Filled.SetMeal
    "verduras", "frutas" -> Icons.Filled.Eco
    "enlatados" -> Icons.Filled.Inventory2
    else -> Icons.Filled.Category
}

@Composable
fun AvatarCategoria(categoria: String, activo: Boolean = true, tamano: Int = 48) {
    Box(
        Modifier.size(tamano.dp).clip(CircleShape)
            .background(if (activo) VerdeClaro else GrisClaro),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            iconoCategoria(categoria), null,
            tint = if (activo) Verde else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size((tamano * 0.5).dp),
        )
    }
}

@Composable
fun EtiquetaChip(texto: String, fondo: Color, contenido: Color, icono: ImageVector? = null) {
    Surface(shape = RoundedCornerShape(50), color = fondo) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            if (icono != null) {
                Icon(icono, null, tint = contenido, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(4.dp))
            }
            Text(texto, color = contenido, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Etiquetas de estado de un producto: bajo stock, por vencer, desactivado. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EtiquetasProducto(p: Producto) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (!p.activo) EtiquetaChip("Desactivado", GrisClaro, MaterialTheme.colorScheme.onSurfaceVariant)
        if (p.activo && p.stockActual <= p.stockMinimo) EtiquetaChip("Bajo stock", RojoClaro, Rojo)
        val dias = diasParaVencer(p.fechaVencimiento)
        if (p.activo && dias != null && dias <= 7) {
            val vencido = dias < 0
            EtiquetaChip(
                textoVencimiento(dias) ?: "",
                if (vencido) RojoClaro else NaranjaClaro,
                if (vencido) Rojo else Naranja,
            )
        }
    }
}

@Composable
fun BarraStock(p: Producto, modifier: Modifier = Modifier) {
    val referencia = maxOf(p.stockMinimo * 2, 1.0)
    val progreso = (p.stockActual / referencia).toFloat().coerceIn(0f, 1f)
    val bajo = p.stockActual <= p.stockMinimo
    LinearProgressIndicator(
        progress = { progreso },
        modifier = modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)),
        color = if (bajo) Rojo else Verde,
        trackColor = GrisClaro,
    )
}

@Composable
fun ProductoCard(p: Producto, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarCategoria(p.categoria, p.activo)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(p.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(p.categoria, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(cantidadConUnidad(p.stockActual, p.unidad), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("mín. ${formatoCantidad(p.stockMinimo)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(10.dp))
            BarraStock(p)
            val hayEtiquetas = !p.activo || p.stockActual <= p.stockMinimo || (diasParaVencer(p.fechaVencimiento) ?: 99L) <= 7
            if (hayEtiquetas) {
                Spacer(Modifier.height(10.dp))
                EtiquetasProducto(p)
            }
        }
    }
}

@Composable
fun MovimientoItem(mov: Movimiento, nombreProducto: String, unidad: String? = null, onClick: () -> Unit) {
    val esEntrada = mov.tipo == Tipos.ENTRADA
    val color = if (esEntrada) Verde else Naranja
    val fondo = if (esEntrada) VerdeClaro else NaranjaClaro
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(CircleShape).background(fondo), contentAlignment = Alignment.Center) {
                Icon(if (esEntrada) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward, null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(nombreProducto, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${mov.responsable} · ${formatoFechaHora(mov.fecha)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            val signo = if (esEntrada) "+" else "−"
            val cantidad = formatoCantidad(mov.cantidad) + (if (unidad != null) " $unidad" else "")
            Text("$signo$cantidad", color = color, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
    }
}

/** Campo desplegable: muestra el valor y abre un menú con las opciones. */
@Composable
fun <T> CampoSelector(
    etiqueta: String,
    texto: String,
    opciones: List<T>,
    textoOpcion: (T) -> String,
    onSeleccion: (T) -> Unit,
    modifier: Modifier = Modifier,
    esError: Boolean = false,
    mensajeError: String? = null,
) {
    var abierto by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedTextField(
            value = texto,
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta) },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null) },
            isError = esError,
            supportingText = mensajeError?.let { { Text(it) } },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        Box(Modifier.matchParentSize().padding(bottom = if (mensajeError != null) 20.dp else 0.dp).clickable { abierto = true })
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(textoOpcion(opcion)) },
                    onClick = {
                        onSeleccion(opcion)
                        abierto = false
                    },
                )
            }
        }
    }
}

/** Campo de fecha opcional con selector de calendario. Devuelve AAAA-MM-DD o null. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoFecha(etiqueta: String, valor: String?, onCambio: (String?) -> Unit, mensajeError: String? = null) {
    var mostrar by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = valor ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta) },
            placeholder = { Text("Sin fecha (opcional)") },
            leadingIcon = { Icon(Icons.Filled.CalendarMonth, null) },
            trailingIcon = {
                if (valor != null) {
                    IconButton(onClick = { onCambio(null) }) { Icon(Icons.Filled.Close, "Quitar fecha") }
                }
            },
            isError = mensajeError != null,
            supportingText = mensajeError?.let { { Text(it) } },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            Modifier.matchParentSize()
                .padding(end = if (valor != null) 56.dp else 0.dp, bottom = if (mensajeError != null) 20.dp else 0.dp)
                .clickable { mostrar = true },
        )
    }
    if (mostrar) {
        val inicial = valor?.let {
            runCatching { LocalDate.parse(it).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() }.getOrNull()
        }
        val estado = rememberDatePickerState(initialSelectedDateMillis = inicial)
        DatePickerDialog(
            onDismissRequest = { mostrar = false },
            confirmButton = {
                TextButton(onClick = {
                    estado.selectedDateMillis?.let { ms ->
                        onCambio(Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate().toString())
                    }
                    mostrar = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { mostrar = false }) { Text("Cancelar") } },
        ) {
            DatePicker(state = estado)
        }
    }
}
