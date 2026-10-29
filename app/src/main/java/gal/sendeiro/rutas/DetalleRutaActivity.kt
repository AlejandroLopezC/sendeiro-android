package gal.sendeiro.rutas

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetalleRutaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_ruta)
        findViewById<TextView>(R.id.detalleNombre).text = intent.getStringExtra("nombre") ?: "Ruta"
        findViewById<TextView>(R.id.detalleTexto).text = intent.getStringExtra("descripcion") ?: "Sin descripción"
    }
}
