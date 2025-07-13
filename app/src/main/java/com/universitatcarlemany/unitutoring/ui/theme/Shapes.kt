package com.universitatcarlemany.unitutoring.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Define las formas personalizadas para la aplicación.
 *
 * Se establece un radio de esquina de 12.dp para componentes medianos como
 * botones y tarjetas, creando una apariencia visual coherente y moderna.
 */
val AppShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp)
)
