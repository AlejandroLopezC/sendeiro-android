package gal.sendeiro.rutas

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Ruta::class],
    version = 2,
    exportSchema = false
)
abstract class SendeiroDatabase : RoomDatabase() {

    abstract fun rutaDao(): RutaDao

    companion object {

        @Volatile
        private var INSTANCE: SendeiroDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE rutas ADD COLUMN tramo TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        fun get(context: Context): SendeiroDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SendeiroDatabase::class.java,
                    "sendeiro.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}