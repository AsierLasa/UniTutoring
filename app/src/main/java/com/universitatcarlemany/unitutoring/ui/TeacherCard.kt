package com.universitatcarlemany.unitutoring.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.universitatcarlemany.unitutoring.model.Teacher
import com.universitatcarlemany.unitutoring.ui.theme.Typography

/**
 * Tarjeta de presentación para un profesor.
 *
 * Muestra la información esencial del profesor, incluyendo un avatar con sus iniciales,
 * su nombre y asignatura. Incluye un botón de acción claro para ver más detalles.
 *
 * @param teacher El objeto [Teacher] con los datos a mostrar.
 * @param onDetailsClick Callback que se ejecuta al pulsar el botón "Ver Perfil".
 */
@Composable
fun TeacherCard(teacher: Teacher, onDetailsClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar con las iniciales del profesor
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                val initials = teacher.name.split(" ").take(2).map { it.first() }.joinToString("")
                Text(
                    text = initials,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Columna con el nombre y la asignatura
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = teacher.name,
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = teacher.subject,
                    style = Typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Botón de acción para ver detalles
            Button(
                onClick = onDetailsClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Ver Perfil", style = Typography.labelSmall)
            }
        }
    }
}
