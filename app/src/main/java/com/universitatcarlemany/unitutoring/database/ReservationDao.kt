package com.universitatcarlemany.unitutoring.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.Query
import com.universitatcarlemany.unitutoring.model.Reservation

@Dao
interface ReservationDao {
    @Insert
    suspend fun insertReservation(reservation: Reservation)

    @Query("SELECT * FROM reservations ORDER BY date ASC, time ASC")
    suspend fun getAllReservations(): List<Reservation>

    /**
     * Obtiene la próxima reserva pendiente a partir de la fecha actual.
     *
     * @param currentDate La fecha de hoy en formato "YYYY-MM-DD".
     * @return La reserva más próxima, o null si no hay ninguna.
     */
    @Query("SELECT * FROM reservations WHERE date >= :currentDate ORDER BY date ASC, time ASC LIMIT 1")
    suspend fun getNextReservation(currentDate: String): Reservation?

    /**
     * Obtiene todas las reservas para un profesor específico en una fecha concreta.
     *
     * Esta función es crucial para saber qué horarios ya no están disponibles.
     *
     * @param teacherName El nombre del profesor a consultar.
     * @param date La fecha de la consulta en formato "YYYY-MM-DD".
     * @return Una lista de las reservas existentes para ese día y profesor.
     */
    @Query("SELECT * FROM reservations WHERE teacherName = :teacherName AND date = :date")
    suspend fun getReservationsForTeacherOnDate(teacherName: String, date: String): List<Reservation>

    /**
     * Elimina una reserva específica de la base de datos.
     *
     * @param reservation La reserva que se va a eliminar.
     */
    @Delete
    suspend fun deleteReservation(reservation: Reservation)
}

