package gal.sendeiro.rutas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

class AdaptadorRutas(
    private val abrirDetalle: (Ruta) -> Unit,
    private val borrarRuta: (Ruta) -> Unit
) : RecyclerView.Adapter<AdaptadorRutas.Fila>() {
    private val rutas = mutableListOf<Ruta>()
    class Fila(v: View) : RecyclerView.ViewHolder(v) {
        val nombre: TextView = v.findViewById(R.id.nombreRuta)
        val etiqueta: TextView = v.findViewById(R.id.etiquetaCerrada)
        val datos: TextView = v.findViewById(R.id.datosRuta)
        val detalle: Button = v.findViewById(R.id.botonDetalle)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Fila {
        android.util.Log.d("RECICLADO", "onCreateViewHolder")
        return Fila(LayoutInflater.from(parent.context).inflate(R.layout.fila_ruta, parent, false))
    }
    override fun getItemCount() = rutas.size
    override fun onBindViewHolder(holder: Fila, position: Int) {
        android.util.Log.d("RECICLADO", "onBindViewHolder posición=$position")
        val ruta = rutas[position]
        holder.nombre.text = ruta.nombre
        holder.datos.text = "${ruta.km} km · ${ruta.dificultad} · ${if (ruta.circular) "Circular" else "Lineal"}"
        // Es imprescindible asignar ambos estados: una vista reciclada puede venir de una ruta cerrada.
        holder.etiqueta.visibility = if (ruta.cerrada) View.VISIBLE else View.GONE
        holder.detalle.setOnClickListener { abrirDetalle(ruta) }
        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(holder.itemView.context).setMessage(R.string.confirmar_borrado)
                .setNegativeButton(R.string.cancelar, null).setPositiveButton(R.string.borrar) { _, _ -> borrarRuta(ruta) }.show()
            true
        }
    }
    fun enviarNuevas(nuevas: List<Ruta>) { rutas.clear(); rutas.addAll(nuevas); notifyDataSetChanged() }
}
