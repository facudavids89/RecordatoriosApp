
package com.example.recordatoriosapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [RecordatorioEntity::class],
    version = 2,
    exportSchema = false
)
abstract class RecordatoriosDatabase : RoomDatabase() {

    abstract fun recordatorioDao(): RecordatorioDao

    companion object {

        private val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    ALTER TABLE recordatorios
                    ADD COLUMN userId TEXT NOT NULL DEFAULT ''
                    """.trimIndent()
                )
            }
        }

        @Volatile
        private var INSTANCE: RecordatoriosDatabase? = null

        fun obtenerDatabase(context: Context): RecordatoriosDatabase {
            return INSTANCE ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    RecordatoriosDatabase::class.java,
                    "recordatorios_database"
                )
                    .addMigrations(MIGRACION_1_2)
                    .build()

                INSTANCE = instancia
                instancia
            }
        }
    }
}
