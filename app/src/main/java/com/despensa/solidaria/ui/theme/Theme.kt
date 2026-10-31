package com.despensa.solidaria.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val Verde = Color(0xFF2E7D32)
val VerdeOscuro = Color(0xFF1B5E20)
val VerdeClaro = Color(0xFFDCEFD9)
val Naranja = Color(0xFFEF6C00)
val NaranjaClaro = Color(0xFFFFE8CC)
val Rojo = Color(0xFFC62828)
val RojoClaro = Color(0xFFFFE0DE)
val Ambar = Color(0xFFF9A825)
val Crema = Color(0xFFFFFBF2)
val TextoOscuro = Color(0xFF1F2A1F)
val Gris = Color(0xFF6B6B63)
val GrisClaro = Color(0xFFECEBE4)

private val Colores = lightColorScheme(
    primary = Verde,
    onPrimary = Color.White,
    primaryContainer = VerdeClaro,
    onPrimaryContainer = VerdeOscuro,
    secondary = Naranja,
    onSecondary = Color.White,
    secondaryContainer = NaranjaClaro,
    onSecondaryContainer = Color(0xFF5A2A00),
    background = Crema,
    onBackground = TextoOscuro,
    surface = Color.White,
    onSurface = TextoOscuro,
    surfaceVariant = Color(0xFFEFF3EA),
    onSurfaceVariant = Gris,
    error = Rojo,
    onError = Color.White,
    errorContainer = RojoClaro,
    onErrorContainer = Color(0xFF7A1010),
    outline = Color(0xFFB9BEB3),
)

private val Formas = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

@Composable
fun DespensaTheme(contenido: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colores, shapes = Formas, content = contenido)
}
