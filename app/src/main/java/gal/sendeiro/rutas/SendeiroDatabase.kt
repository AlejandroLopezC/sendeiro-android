package gal.sendeiro.rutas

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Ruta::class], version = 1, exportSchema = false)
abstract class SendeiroDatabase : RoomDatabase() {
    abstract fun rutaDao(): RutaDao
    companion object {
        @Volatile private var INSTANCE: SendeiroDatabase? = null
        fun get(context: Context): SendeiroDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, SendeiroDatabase::class.java, "sendeiro.db").build().also { INSTANCE = it }
        }
    }
}
