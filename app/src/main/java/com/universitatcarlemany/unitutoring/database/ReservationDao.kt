package com.universitatcarlemany.unitutoring.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.universitatcarlemany.unitutoring.model.Reservation

@Dao
interface ReservationDao {
    @Insert
    suspend fun insertReservation(reservation: Reservation)

    @Query("SELECT * FROM reservations")
    suspend fun getAllReservations(): List<Reservation>
}
