package com.example.apk.View

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.apk.R
import com.example.apk.databinding.FragmentHomeBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class Home : Fragment(), OnMapReadyCallback,
    GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener,
    GoogleMap.OnMapLongClickListener,
    GoogleMap.OnMarkerClickListener {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var map: GoogleMap
    private lateinit var firestore: FirebaseFirestore
    private val markers = mutableListOf<Marker>()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        const val REQUEST_CODE_LOCATION = 0
        private const val MARKERS_COLLECTION = "map_markers"
    }

    data class MapMarker(
        val id: String = "",
        val latitude: Double = 0.0,
        val longitude: Double = 0.0,
        val title: String = "Nuevo marcador",
        val snippet: String = "",
        val userId: String = "", // Para asociar marcadores a usuarios
        @ServerTimestamp val createdAt: Date? = null
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar Firestore
        firestore = Firebase.firestore

        binding.fabMenu.setOnClickListener {
            val mainActivity = activity as? MainActivity
            if (mainActivity != null) {
                mainActivity.openDrawer()
            } else {
                Toast.makeText(requireContext(), "Error al abrir el menú", Toast.LENGTH_SHORT).show()
            }
        }

        // Configurar el mapa
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Configurar listeners del mapa
        with(map) {
            setOnMyLocationButtonClickListener(this@Home)
            setOnMyLocationClickListener(this@Home)
            setOnMapLongClickListener(this@Home)
            setOnMarkerClickListener(this@Home)
        }

        enableLocation()
        loadUserMarkers()
    }

    // Métodos de ubicación
    private fun isLocationPermissionGranted() = ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun enableLocation() {
        if (!::map.isInitialized) return
        if (isLocationPermissionGranted()) {
            try {
                map.isMyLocationEnabled = true
            } catch (e: SecurityException) {
                showToast("Error al habilitar la ubicación")
            }
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            showToast("Necesitas aceptar los permisos de ubicación")
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (::map.isInitialized) {
                        enableLocation()
                    }
                } else {
                    showToast("Para usar esta función necesitas activar los permisos de ubicación")
                }
            }
        }
    }

    // Métodos para manejar marcadores
    override fun onMapLongClick(latLng: LatLng) {
        showAddMarkerDialog(latLng)
    }

    private fun showAddMarkerDialog(latLng: LatLng) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_marker, null)
        val editTitle = dialogView.findViewById<EditText>(R.id.editTitle)
        val editSnippet = dialogView.findViewById<EditText>(R.id.editSnippet)

        AlertDialog.Builder(requireContext())
            .setTitle("Agregar marcador")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val title = editTitle.text.toString()
                val snippet = editSnippet.text.toString()

                auth.currentUser?.uid?.let { userId ->
                    val marker = MapMarker(
                        latitude = latLng.latitude,
                        longitude = latLng.longitude,
                        title = title.ifEmpty { "Marcador ${markers.size + 1}" },
                        snippet = snippet,
                        userId = userId
                    )
                    saveMarker(marker)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun saveMarker(marker: MapMarker) {
        firestore.collection(MARKERS_COLLECTION)
            .add(marker)
            .addOnSuccessListener { docRef ->
                val markerWithId = marker.copy(id = docRef.id)
                docRef.set(markerWithId)
                    .addOnSuccessListener {
                        addMarkerToMap(markerWithId)
                        showToast("Marcador guardado")
                    }
            }
            .addOnFailureListener { e ->
                showToast("Error al guardar: ${e.message}")
            }
    }

    private fun loadUserMarkers() {
        auth.currentUser?.uid?.let { userId ->
            firestore.collection(MARKERS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { result ->
                    result.forEach { doc ->
                        doc.toObject(MapMarker::class.java).let { addMarkerToMap(it) }
                    }
                }
                .addOnFailureListener { e ->
                    showToast("Error al cargar marcadores: ${e.message}")
                }
        }
    }

    private fun addMarkerToMap(mapMarker: MapMarker) {
        val latLng = LatLng(mapMarker.latitude, mapMarker.longitude)
        map.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(mapMarker.title)
                .snippet(mapMarker.snippet)
        )?.let { marker ->
            marker.tag = mapMarker.id
            markers.add(marker)

            // Centrar en el primer marcador
            if (markers.size == 1) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            }
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        AlertDialog.Builder(requireContext())
            .setTitle(marker.title)
            .setMessage(marker.snippet ?: "¿Qué deseas hacer con este marcador?")
            .setPositiveButton("Eliminar") { _, _ -> deleteMarker(marker) }
            .setNegativeButton("Cerrar", null)
            .show()
        return true
    }

    private fun deleteMarker(marker: Marker) {
        (marker.tag as? String)?.let { markerId ->
            firestore.collection(MARKERS_COLLECTION).document(markerId)
                .delete()
                .addOnSuccessListener {
                    marker.remove()
                    markers.remove(marker)
                    showToast("Marcador eliminado")
                }
                .addOnFailureListener { e ->
                    showToast("Error al eliminar: ${e.message}")
                }
        }
    }

    // Métodos de la interfaz del mapa
    override fun onMyLocationButtonClick(): Boolean {
        showToast("Centrando en tu ubicación...")
        return false
    }

    override fun onMyLocationClick(location: Location) {
        showToast("Estás en ${location.latitude}, ${location.longitude}")
    }

    // Método de ayuda
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        if (::map.isInitialized && isLocationPermissionGranted()) {
            enableLocation()
        }
    }
}