package gal.sendeiro.rutas

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "rutas")
data class Ruta(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val km: Double,
    val cerrada: Boolean,
    val dificultad: String = "Moderada",
    val circular: Boolean = false,
    val descripcion: String = "",
    @ColumnInfo(defaultValue = "''")
    val tramo: String = ""
)