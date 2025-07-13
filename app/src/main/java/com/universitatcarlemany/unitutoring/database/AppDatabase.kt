package com.universitatcarlemany.unitutoring.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.universitatcarlemany.unitutoring.model.Reservation

/**
 * Define la base de datos de la aplicación usando Room.
 *
 * Esta clase abstracta sirve como el punto de acceso principal a la base de datos
 * persistente de la aplicación.
 */
@Database(entities = [Reservation::class], version = 3, exportSchema = false)
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
                )
                    // CORRECCIÓN: Se usa el método no obsoleto para la migración destructiva.
                    // Permite a Room recrear la base de datos si no se proporciona una migración.
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}