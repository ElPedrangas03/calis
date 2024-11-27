package com.example.proyectofinal.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Tarea::class, Nota::class], version = 1, exportSchema = false)
abstract class ProyectoFinalDatabase : RoomDatabase() {

    abstract fun tareaDao(): TareaDao
    abstract fun notaDao(): NotaDao

    companion object {
        @Volatile
        private var INSTANCE: ProyectoFinalDatabase? = null

        fun getDatabase(context: Context): ProyectoFinalDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    ProyectoFinalDatabase::class.java,
                    "proyecto_final_database"
                ).fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}