package com.universitatcarlemany.unitutoring.model

import com.google.gson.annotations.SerializedName

data class Teacher(
    val id: Int,
    @SerializedName("nombre") val name: String,
    @SerializedName("asignatura") val subject: String,
    @SerializedName("email") val email: String,
    @SerializedName("horarios") val schedule: List<String>
)
