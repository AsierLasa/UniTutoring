package com.universitatcarlemany.unitutoring.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reservations")
data class Reservation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val teacherName: String,
    val subject: String,
    val date: String,
    val time: String
)
