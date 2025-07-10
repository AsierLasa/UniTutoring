package com.universitatcarlemany.unitutoring.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.universitatcarlemany.unitutoring.model.Reservation

@Database(entities = [Reservation::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reservationDao(): ReservationDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "unitutoring_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
