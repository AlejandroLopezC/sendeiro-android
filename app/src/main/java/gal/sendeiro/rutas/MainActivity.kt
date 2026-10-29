/*
 * Sendeiro · PMDM-UD2 · paquete gal.sendeiro.rutas
 * Requisitos: JDK 17, Android SDK instalado y ANDROID_HOME/local.properties configurado.
 * Compilar: ./gradlew assembleDebug (Windows: gradlew.bat assembleDebug).
 * Instalar: ./gradlew installDebug, con un AVD arrancado o un móvil con depuración USB.
 * Release: configurar SENDEIRO_STORE, SENDEIRO_PASS, SENDEIRO_ALIAS y SENDEIRO_KEY_PASS
 * en ~/.gradle/gradle.properties (fuera del repositorio); después ./gradlew assembleRelease.
 * No se incluye ni se debe versionar el almacén de claves. Las pruebas de dispositivo deben registrarse
 * únicamente después de ejecutarlas; este proyecto no afirma haber sido instalado en un móvil real.
 */
package gal.sendeiro.rutas

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private val tag = "CICLO"
    private lateinit var campoNombre: EditText
    private lateinit var campoDistancia: EditText
    private lateinit var campoDificultad: Spinner
    private lateinit var campoCircular: CheckBox
    private lateinit var campoDescripcion: EditText
    private lateinit var campoMillas: CheckBox
    private lateinit var estado: TextView
    private lateinit var estadoLista: TextView
    private lateinit var progreso: ProgressBar
    private lateinit var adaptador: AdaptadorRutas
    private val db by lazy { SendeiroDatabase.get(this) }
    private var ultimoErrorPermiso = false

    private val permisoUbicacion = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { resultados ->
        if (resultados.values.any { it }) {
            Snackbar.make(findViewById(R.id.botonPermiso), "Permiso concedido: ya se puede usar la ubicación.", Snackbar.LENGTH_LONG).show()
        } else {
            val puedeExplicar = shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
            if (puedeExplicar) {
                AlertDialog.Builder(this).setMessage(R.string.permiso_denegado)
                    .setPositiveButton(R.string.aceptar, null).show()
            } else {
                AlertDialog.Builder(this).setMessage(R.string.permiso_denegado)
                    .setPositiveButton(R.string.abrir_ajustes) { _, _ ->
                        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")))
                    }.setNegativeButton(R.string.cancelar, null).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); android.util.Log.d(tag, "onCreate")
        setContentView(R.layout.activity_main)
        campoNombre = findViewById(R.id.campoNombre); campoDistancia = findViewById(R.id.campoDistancia)
        campoDificultad = findViewById(R.id.campoDificultad); campoCircular = findViewById(R.id.campoCircular)
        ArrayAdapter.createFromResource(this, R.array.dificultades, android.R.layout.simple_spinner_item).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            campoDificultad.adapter = adapter
        }
        campoDescripcion = findViewById(R.id.campoDescripcion); campoMillas = findViewById(R.id.campoMillas)
        estado = findViewById(R.id.estado); estadoLista = findViewById(R.id.estadoLista); progreso = findViewById(R.id.progreso)
        savedInstanceState?.let {
            campoNombre.setText(it.getString("nombre", "")); campoDistancia.setText(it.getString("distancia", ""))
            campoDescripcion.setText(it.getString("descripcion", "")); campoCircular.isChecked = it.getBoolean("circular", false)
        }
        val prefs = getSharedPreferences("ajustes", MODE_PRIVATE)
        campoMillas.isChecked = prefs.getBoolean("millas", false)
        campoMillas.setOnCheckedChangeListener { _, checked -> prefs.edit().putBoolean("millas", checked).apply(); cargarRutas() }
        campoNombre.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { validarNombre(false) }
            override fun afterTextChanged(s: Editable?) = guardarBorradorValidado()
        })
        campoDistancia.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { validarDistancia(false) }
            override fun afterTextChanged(s: Editable?) = guardarBorradorValidado()
        })
        campoDistancia.setOnFocusChangeListener { _, tieneFoco -> if (!tieneFoco) validarDistancia(true) }
        campoNombre.setOnFocusChangeListener { _, tieneFoco -> if (!tieneFoco) validarNombre(true) }
        findViewById<Button>(R.id.botonGuardar).setOnClickListener { guardarRuta() }
        findViewById<Button>(R.id.botonLimpiar).setOnClickListener { limpiarFormulario() }
        findViewById<Button>(R.id.botonPermiso).setOnClickListener { pedirUbicacionEnContexto() }
        findViewById<Button>(R.id.botonCompartir).setOnClickListener { compartirParte() }
        adaptador = AdaptadorRutas(::abrirDetalle, ::borrarRuta)
        findViewById<RecyclerView>(R.id.listaRutas).apply { layoutManager = LinearLayoutManager(this@MainActivity); adapter = adaptador }
        restaurarBorrador(); observarRutas(); insertarDatosDemostracionSiVacio()
    }
    override fun onStart() { super.onStart(); android.util.Log.d(tag, "onStart") }
    override fun onResume() { super.onResume(); android.util.Log.d(tag, "onResume") }
    override fun onPause() { android.util.Log.d(tag, "onPause"); super.onPause() }
    override fun onStop() { android.util.Log.d(tag, "onStop"); super.onStop() }
    override fun onRestart() { super.onRestart(); android.util.Log.d(tag, "onRestart") }
    override fun onDestroy() { android.util.Log.d(tag, "onDestroy"); super.onDestroy() }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("nombre", campoNombre.text.toString()); outState.putString("distancia", campoDistancia.text.toString())
        outState.putString("descripcion", campoDescripcion.text.toString()); outState.putBoolean("circular", campoCircular.isChecked)
        android.util.Log.d(tag, "onSaveInstanceState"); super.onSaveInstanceState(outState)
    }
    private fun validarNombre(mostrar: Boolean): Boolean {
        val ok = campoNombre.text.toString().trim().isNotEmpty()
        campoNombre.error = if (!ok && mostrar) getString(R.string.error_nombre) else null
        return ok
    }
    private fun distanciaValida(): Double? = campoDistancia.text.toString().trim().replace(',', '.').toDoubleOrNull()?.takeIf { it > 0 && it.isFinite() && it < 100000.0 }
    private fun validarDistancia(mostrar: Boolean): Boolean {
        val ok = distanciaValida() != null
        campoDistancia.error = if (!ok && mostrar) getString(R.string.error_distancia) else null
        return ok
    }
    private fun guardarBorradorValidado() {
        // No se persisten campos numéricos inválidos como si fueran datos válidos.
        val nombre = campoNombre.text.toString().take(80)
        val distancia = campoDistancia.text.toString()
        getSharedPreferences("borrador", MODE_PRIVATE).edit().putString("nombre", nombre)
            .putString("distancia", distancia.take(12)).putString("descripcion", campoDescripcion.text.toString().take(1000))
            .putBoolean("circular", campoCircular.isChecked).apply()
    }
    private fun restaurarBorrador() {
        val p = getSharedPreferences("borrador", MODE_PRIVATE)
        if (campoNombre.text.isNullOrEmpty()) campoNombre.setText(p.getString("nombre", ""))
        if (campoDistancia.text.isNullOrEmpty()) campoDistancia.setText(p.getString("distancia", ""))
        if (campoDescripcion.text.isNullOrEmpty()) campoDescripcion.setText(p.getString("descripcion", ""))
        campoCircular.isChecked = p.getBoolean("circular", campoCircular.isChecked)
    }
    private fun guardarRuta() {
        val nombreOk = validarNombre(true); val distanciaOk = validarDistancia(true)
        if (!nombreOk || !distanciaOk) {
            estado.text = getString(R.string.borrador_invalido)
            if (!nombreOk) campoNombre.requestFocus() else campoDistancia.requestFocus()
            return
        }
        val ruta = Ruta(nombre = campoNombre.text.toString().trim(), km = distanciaValida()!!,
            cerrada = false, dificultad = campoDificultad.selectedItem.toString(), circular = campoCircular.isChecked,
            descripcion = campoDescripcion.text.toString().trim())
        lifecycleScope.launch {
            withContext(Dispatchers.IO) { db.rutaDao().insertar(ruta) }
            estado.text = getString(R.string.guardado); Snackbar.make(findViewById(R.id.botonGuardar), R.string.guardado, Snackbar.LENGTH_SHORT).show()
            getSharedPreferences("borrador", MODE_PRIVATE).edit().clear().apply(); limpiarFormulario()
        }
    }
    private fun limpiarFormulario() {
        campoNombre.text.clear(); campoDistancia.text.clear(); campoDescripcion.text.clear(); campoCircular.isChecked = false
        estado.text = ""; getSharedPreferences("borrador", MODE_PRIVATE).edit().clear().apply()
    }
    private fun observarRutas() {
        progreso.visibility = View.VISIBLE; estadoLista.text = getString(R.string.cargando)
        lifecycleScope.launch {
            db.rutaDao().observarTodas().catch {
                progreso.visibility = View.GONE; estadoLista.text = getString(R.string.error_carga)
            }.collectLatest { rutas ->
                progreso.visibility = View.GONE; adaptador.enviarNuevas(rutas)
                estadoLista.text = if (rutas.isEmpty()) getString(R.string.vacio_rutas) else "${rutas.size} rutas disponibles"
            }
        }
    }
    private fun cargarRutas() { /* El Flow de Room actualiza automáticamente el listado. */ }
    private fun insertarDatosDemostracionSiVacio() {
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = db.rutaDao()
            if (dao.contar() == 0) {
                val nombres = listOf("Fragas do Eume", "Monte Aloia", "Ruta do río Xesta", "Costa da Vela", "Serra do Suído", "Camiño da Auga", "Miradoiro do Castro", "Senda do Lérez", "Ruta do Miño", "Bosque de Seivane", "Fervenza do Toxa", "Costa de Arousa", "Camiño dos Faros", "Ruta da Moura", "Monte Pindo", "Senda do Tea", "Val do Deza", "Pontevedra verde", "Ruta da Pedra", "Souto da Retorta", "Serra da Capelada", "Fraga de Catasós", "Ruta do Xurés", "Camiño de San Pedro", "Costa de Dexo", "Ribeira Sacra", "Ruta do Tambre", "Monte Galiñeiro", "Senda do Umia", "Camiño da Fervenza")
                for (i in 0 until 140) {
                    val base = nombres[i % nombres.size]
                    val nombreCompleto = if (i < nombres.size) base else "$base · tramo ${i + 1}"
                    dao.insertar(Ruta(nombre = nombreCompleto, km = 3.5 + i * 0.7, cerrada = i % 7 == 0, dificultad = listOf("Fácil", "Moderada", "Difícil")[i % 3], circular = i % 2 == 0, descripcion = "Ruta de demostración ${i + 1}; dato ficticio de muestra."))
                }
            }
        }
    }
    private fun abrirDetalle(ruta: Ruta) {
        startActivity(Intent(this, DetalleRutaActivity::class.java).putExtra("nombre", ruta.nombre).putExtra("descripcion", "${ruta.descripcion}\nDistancia: ${ruta.km} km\nDificultad: ${ruta.dificultad}"))
    }
    private fun borrarRuta(ruta: Ruta) { lifecycleScope.launch(Dispatchers.IO) { db.rutaDao().borrar(ruta) } }
    private fun compartirParte() {
        val texto = "Parte de Sendeiro: ${campoNombre.text}; distancia: ${campoDistancia.text} km; ${campoDescripcion.text}"
        val intent = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, texto)
        if (intent.resolveActivity(packageManager) != null) startActivity(Intent.createChooser(intent, getString(R.string.compartir)))
        else Snackbar.make(findViewById(R.id.botonCompartir), R.string.sin_manejador, Snackbar.LENGTH_LONG).show()
    }
    private fun pedirUbicacionEnContexto() {
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (fine || coarse) { Snackbar.make(findViewById(R.id.botonPermiso), "Ubicación ya autorizada.", Snackbar.LENGTH_SHORT).show(); return }
        AlertDialog.Builder(this).setMessage(R.string.permiso_ubicacion).setPositiveButton(R.string.aceptar) { _, _ ->
            permisoUbicacion.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }.setNegativeButton(R.string.cancelar, null).show()
    }
}
