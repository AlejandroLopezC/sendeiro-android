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
import android.widget.AutoCompleteTextView
import kotlinx.coroutines.delay
import android.location.LocationManager
import android.annotation.SuppressLint

class MainActivity : AppCompatActivity() {
    private val tag = "CICLO"
    private lateinit var campoNombre: EditText
    private lateinit var campoDistancia: EditText
    private lateinit var campoDificultad: Spinner
    private lateinit var campoCircular: CheckBox
    private lateinit var campoDescripcion: EditText
    private lateinit var campoTramo: AutoCompleteTextView
    private lateinit var campoMillas: CheckBox
    private lateinit var estado: TextView
    private lateinit var estadoLista: TextView
    private lateinit var progreso: ProgressBar
    private lateinit var adaptador: AdaptadorRutas
    private val db by lazy { SendeiroDatabase.get(this) }
    private var ultimoErrorPermiso = false
    private var limpiandoFormulario = false

    private val permisoUbicacion = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { resultados ->
        if (resultados.values.any { it }) {
            registrarUbicacion()
        } else {
            val puedeExplicar =
                shouldShowRequestPermissionRationale(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) || shouldShowRequestPermissionRationale(
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
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
        campoTramo = findViewById(R.id.campoTramo)

        val tramos = (1..140).map { "Tramo $it" }

        val adaptadorTramos = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            tramos
        )
        campoTramo.setAdapter(adaptadorTramos)
        campoTramo.threshold = 1
        campoTramo.setOnFocusChangeListener { _, tieneFoco ->
            if (!tieneFoco) {
                val tramo = campoTramo.text.toString().trim()

                campoTramo.error = if (tramo !in tramos) {
                    getString(R.string.error_tramo)
                } else {
                    null
                }
            }
        }
        estado = findViewById(R.id.estado); estadoLista = findViewById(R.id.estadoLista); progreso = findViewById(R.id.progreso)
        savedInstanceState?.let {
            campoNombre.setText(it.getString("nombre", ""))
            campoDistancia.setText(it.getString("distancia", ""))
            campoDescripcion.setText(it.getString("descripcion", ""))
            campoCircular.isChecked = it.getBoolean("circular", false)
            campoTramo.setText(it.getString("tramo", ""), false)
            campoDificultad.setSelection(it.getInt("dificultad", 0))
        }
        val prefs = getSharedPreferences("ajustes", MODE_PRIVATE)
        campoMillas.isChecked = prefs.getBoolean("millas", false)
        campoMillas.setOnCheckedChangeListener { _, checked -> prefs.edit().putBoolean("millas", checked).apply(); cargarRutas() }
        if (savedInstanceState == null) {
            restaurarBorrador()
        }
        campoNombre.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!limpiandoFormulario) validarNombre(false)
            }
            override fun afterTextChanged(s: Editable?) {
                if (!limpiandoFormulario) guardarBorradorValidado()
            }
        })
        campoDistancia.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!limpiandoFormulario) validarDistancia(false)
            }
            override fun afterTextChanged(s: Editable?) {
                if (!limpiandoFormulario) guardarBorradorValidado()
            }
        })
        campoDistancia.setOnFocusChangeListener { _, tieneFoco -> if (!tieneFoco) validarDistancia(true) }
        campoNombre.setOnFocusChangeListener { _, tieneFoco -> if (!tieneFoco) validarNombre(true) }
        findViewById<Button>(R.id.botonGuardar).setOnClickListener { guardarRuta() }
        findViewById<Button>(R.id.botonLimpiar).setOnClickListener { limpiarFormulario() }
        findViewById<Button>(R.id.botonPermiso).setOnClickListener { pedirUbicacionEnContexto() }
        findViewById<Button>(R.id.botonCompartir).setOnClickListener { compartirParte() }
        adaptador = AdaptadorRutas(::abrirDetalle, ::borrarRuta)
        adaptador.cambiarUnidades(campoMillas.isChecked)
        findViewById<RecyclerView>(R.id.listaRutas).apply { layoutManager = LinearLayoutManager(this@MainActivity); adapter = adaptador }
        observarRutas()
        insertarDatosDemostracionSiVacio()
    }
    override fun onStart() { super.onStart(); android.util.Log.d(tag, "onStart") }
    override fun onResume() { super.onResume(); android.util.Log.d(tag, "onResume") }
    override fun onPause() {
        guardarBorradorValidado()
        android.util.Log.d(tag, "onPause")
        super.onPause()
    }
    override fun onStop() { android.util.Log.d(tag, "onStop"); super.onStop() }
    override fun onRestart() { super.onRestart(); android.util.Log.d(tag, "onRestart") }
    override fun onDestroy() { android.util.Log.d(tag, "onDestroy"); super.onDestroy() }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("nombre", campoNombre.text.toString()); outState.putString("distancia", campoDistancia.text.toString())
        outState.putString("descripcion", campoDescripcion.text.toString()); outState.putBoolean("circular", campoCircular.isChecked)
        outState.putString("tramo", campoTramo.text.toString())
        outState.putInt("dificultad", campoDificultad.selectedItemPosition)
        android.util.Log.d(tag, "onSaveInstanceState"); super.onSaveInstanceState(outState)
    }
    private fun validarNombre(mostrar: Boolean): Boolean {
        val ok = campoNombre.text.toString().trim().isNotEmpty()

        campoNombre.error = when {
            ok -> null
            mostrar -> getString(R.string.error_nombre)
            else -> getString(R.string.pista_nombre)
        }

        return ok
    }

    private fun validarDistancia(mostrar: Boolean): Boolean {
        val ok = distanciaValida() != null

        campoDistancia.error = when {
            ok -> null
            mostrar -> getString(R.string.error_distancia)
            else -> getString(R.string.pista_distancia)
        }

        return ok
    }

    private fun distanciaValida(): Double? =
        campoDistancia.text.toString()
            .trim()
            .replace(',', '.')
            .toDoubleOrNull()
            ?.takeIf {
                it > 0 && it.isFinite() && it < 100000.0
            }
    private fun guardarBorradorValidado() {
        val nombre = campoNombre.text.toString().take(80)
        val distancia = distanciaValida()

        getSharedPreferences("borrador", MODE_PRIVATE)
            .edit()
            .putString("nombre", nombre)
            .putString("distancia", distancia?.let {
                campoDistancia.text.toString()
            } ?: "")
            .putString("descripcion", campoDescripcion.text.toString().take(1000))
            .putBoolean("circular", campoCircular.isChecked)
            .putInt("dificultad", campoDificultad.selectedItemPosition)
            .putString("tramo", campoTramo.text.toString().trim()
                .takeIf { it in (1..140).map { n -> "Tramo $n" } } ?: "")
            .apply()
    }
    private fun restaurarBorrador() {
        val p = getSharedPreferences("borrador", MODE_PRIVATE)

        campoNombre.setText(p.getString("nombre", ""))
        campoDistancia.setText(p.getString("distancia", ""))
        campoDescripcion.setText(p.getString("descripcion", ""))
        campoCircular.isChecked = p.getBoolean("circular", false)
        campoDificultad.setSelection(p.getInt("dificultad", 0))
        campoTramo.setText(p.getString("tramo", ""), false)
    }
    private fun guardarRuta() {
        val tramo = campoTramo.text.toString().trim()
        val tramosValidos = (1..140).map { "Tramo $it" }

        if (tramo !in tramosValidos) {
            campoTramo.error = getString(R.string.error_tramo)
            campoTramo.requestFocus()
            return
        }

        campoTramo.error = null
        val nombreOk = validarNombre(true); val distanciaOk = validarDistancia(true)
        if (!nombreOk || !distanciaOk) {
            estado.text = getString(R.string.borrador_invalido)
            if (!nombreOk) campoNombre.requestFocus() else campoDistancia.requestFocus()
            return
        }
        val ruta = Ruta(
            nombre = campoNombre.text.toString().trim(),
            km = distanciaValida()!!,
            cerrada = false,
            dificultad = campoDificultad.selectedItem.toString(),
            circular = campoCircular.isChecked,
            descripcion = campoDescripcion.text.toString().trim(),
            tramo = tramo
        )
        lifecycleScope.launch {
            withContext(Dispatchers.IO) { db.rutaDao().insertar(ruta) }
            estado.text = getString(R.string.guardado); Snackbar.make(findViewById(R.id.botonGuardar), R.string.guardado, Snackbar.LENGTH_SHORT).show()
            getSharedPreferences("borrador", MODE_PRIVATE).edit().clear().apply(); limpiarFormulario()
        }
    }
    private fun limpiarFormulario() {
        limpiandoFormulario = true
        campoNombre.text.clear()
        campoDistancia.text.clear()
        campoDescripcion.text.clear()
        campoTramo.setText("", false)
        campoCircular.isChecked = false
        campoDificultad.setSelection(0)

        campoNombre.error = null
        campoDistancia.error = null
        campoTramo.error = null

        estado.text = ""

        getSharedPreferences("borrador", MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
        limpiandoFormulario = false
    }
    private fun observarRutas() {
        progreso.visibility = View.VISIBLE
        estadoLista.text = getString(R.string.cargando)

        lifecycleScope.launch {
            db.rutaDao().observarTodas()
                .catch {
                    progreso.visibility = View.GONE
                    adaptador.enviarNuevas(emptyList())
                    estadoLista.text = getString(R.string.error_carga)
                }
                .collectLatest { rutas ->
                    progreso.visibility = View.GONE
                    adaptador.enviarNuevas(rutas)

                    estadoLista.text = if (rutas.isEmpty()) {
                        getString(R.string.vacio_rutas)
                    } else {
                        "${rutas.size} rutas disponibles"
                    }
                }
        }
    }
    private fun cargarRutas() {
        adaptador.cambiarUnidades(campoMillas.isChecked)
    }
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
        val tramo = ruta.tramo.ifBlank { "Sin asignar" }
        val millas = campoMillas.isChecked
        val distancia = if (millas) ruta.km * 0.621371 else ruta.km
        val unidad = if (millas) "mi" else "km"
        val distanciaTexto = String.format("%.1f", distancia)

        startActivity(
            Intent(this, DetalleRutaActivity::class.java)
                .putExtra("nombre", ruta.nombre)
                .putExtra(
                    "descripcion",
                    "${ruta.descripcion}\n" +
                            "Distancia: $distanciaTexto $unidad\n" +
                            "Dificultad: ${ruta.dificultad}\n" +
                            "Tramo: $tramo"
                )
        )
    }
    private fun borrarRuta(ruta: Ruta) { lifecycleScope.launch(Dispatchers.IO) { db.rutaDao().borrar(ruta) } }
    private fun compartirParte() {
        val texto = "Parte de Sendeiro: ${campoNombre.text}; distancia: ${campoDistancia.text} km; ${campoDescripcion.text}"
        val intent = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, texto)
        if (intent.resolveActivity(packageManager) != null) startActivity(Intent.createChooser(intent, getString(R.string.compartir)))
        else Snackbar.make(findViewById(R.id.botonCompartir), R.string.sin_manejador, Snackbar.LENGTH_LONG).show()
    }
    @SuppressLint("MissingPermission")
    private fun registrarUbicacion() {
        val fine = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fine && !coarse) return

        val gestor = getSystemService(LOCATION_SERVICE) as LocationManager

        val ubicacion = gestor.getProviders(true)
            .mapNotNull { proveedor ->
                try {
                    gestor.getLastKnownLocation(proveedor)
                } catch (e: SecurityException) {
                    null
                }
            }
            .maxByOrNull { it.time }

        val mensaje = if (ubicacion != null) {
            "Ubicación disponible correctamente."
        } else {
            "No hay una ubicación disponible todavía."
        }

        Snackbar.make(
            findViewById(R.id.botonPermiso),
            mensaje,
            Snackbar.LENGTH_LONG
        ).show()
    }
    private fun pedirUbicacionEnContexto() {
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (fine || coarse) {
            registrarUbicacion()
            return
        }
        AlertDialog.Builder(this).setMessage(R.string.permiso_ubicacion).setPositiveButton(R.string.aceptar) { _, _ ->
            permisoUbicacion.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }.setNegativeButton(R.string.cancelar, null).show()
    }
}
