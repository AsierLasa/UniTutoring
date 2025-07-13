package com.universitatcarlemany.unitutoring.model

import com.google.gson.annotations.SerializedName

/**
 * Representa un rango de disponibilidad para un día específico.
 * @property day El día de la semana (ej. "Lunes").
 * @property startTime La hora de inicio del rango (ej. "09:00").
 * @property endTime La hora de fin del rango (ej. "12:00").
 */
data class Availability(
    val day: String,
    val startTime: String,
    val endTime: String
)

/**
 * Representa a un profesor.
 *
 * @property availability La lista de rangos de disponibilidad del profesor.
 * Se mapea desde la clave "availability" en el JSON.
 */
data class Teacher(
    val id: Int,
    @SerializedName("nombre") val name: String,
    @SerializedName("asignatura") val subject: String,
    @SerializedName("email") val email: String,
    @SerializedName("availability") val availability: List<Availability>
)
