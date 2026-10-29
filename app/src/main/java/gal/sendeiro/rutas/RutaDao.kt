package gal.sendeiro.rutas

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RutaDao {
    @Query("SELECT * FROM rutas ORDER BY nombre") fun observarTodas(): Flow<List<Ruta>>
    @Query("SELECT * FROM rutas ORDER BY nombre") suspend fun todas(): List<Ruta>
    @Insert suspend fun insertar(ruta: Ruta): Long
    @Delete suspend fun borrar(ruta: Ruta)
    @Query("SELECT COUNT(*) FROM rutas") suspend fun contar(): Int
}
