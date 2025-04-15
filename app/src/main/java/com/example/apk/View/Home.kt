package com.example.apk.View

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.apk.R
import com.example.apk.ViewModel.AuthViewModel
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
    private lateinit var authViewModel: AuthViewModel

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
        val userEmail: String = "", // Para asociar marcadores a usuarios
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

        // Inicializar AuthViewModel
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        authViewModel.sharedLocations.observe(viewLifecycleOwner) { locations ->
            locations?.let { updateMapWithSharedLocations(it) }
        }

        authViewModel.loadSharedLocations()

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

        // Cargar Marcadores
        fetchMarkers()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Configurar listeners del mapa
        with(map) {
            setOnMyLocationClickListener(this@Home)
            setOnMyLocationButtonClickListener(this@Home)
            setOnMapLongClickListener(this@Home)
            setOnMarkerClickListener(this@Home)
        }

        enableLocation()
        loadUserMarkers()
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

    // Métodos de ubicación
    private fun isLocationPermissionGranted(): Boolean {
        val permissionCheck = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        Log.d("HomeFragment", "Permiso de ubicación concedido: ${permissionCheck == PackageManager.PERMISSION_GRANTED}")
        return permissionCheck == PackageManager.PERMISSION_GRANTED
    }

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

    override fun onMyLocationClick(location: Location) {
        showToast("Estás en ${location.latitude}, ${location.longitude}")
    }

    // Al agregar marcadores desde shared_locations en updateMapWithSharedLocations:
  fun updateMapWithSharedLocations(locations: List<AuthViewModel.SharedLocation>) {
        map.clear() // Limpiar marcadores existentes

        locations.forEach { location ->
            val latLng = LatLng(location.latitude, location.longitude)
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(location.title)
                    .snippet("Compartido por: ${location.userEmail}")
            )?.let { marker ->
                marker.tag = "shared_location_id_${location.id}" // Etiqueta para identificar como compartido
            }
        }

        // Centrar en el primer marcador si existe
        if (locations.isNotEmpty()) {
            val firstLocation = locations.first()
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                LatLng(firstLocation.latitude, firstLocation.longitude),
                25f
            ))
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




    // Modifica tu metodo onMapLongClick para guardar como ubicación compartida
    override fun onMapLongClick(latLng: LatLng) {
        auth.currentUser?.let { user ->
            showAddSharedLocationDialog(latLng, user.email ?: "Anónimo")
        }
    }


    private fun showAddSharedLocationDialog(latLng: LatLng, userEmail: String) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_marker, null)

        AlertDialog.Builder(requireContext())
            .setTitle("Compartir ubicación")
            .setView(dialogView)
            .setPositiveButton("Compartir") { _, _ ->
                val title = dialogView.findViewById<EditText>(R.id.editTitle).text.toString()
                val description = dialogView.findViewById<EditText>(R.id.editSnippet).text.toString()

                val sharedLocation = AuthViewModel.SharedLocation(
                    userId = auth.currentUser ?.uid ?: "",
                    userEmail = userEmail, // Asegúrate de que esto esté correcto
                    latitude = latLng.latitude,
                    longitude = latLng.longitude,
                    title = title.ifEmpty { "Ubicación compartida" },
                    description = description
                )

                authViewModel.saveSharedLocation(sharedLocation)
            }
            .setNegativeButton("Cancelar", null)
            .show()
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

                auth.currentUser?.email?.let { userEmail ->
                    val marker = MapMarker(
                        latitude = latLng.latitude,
                        longitude = latLng.longitude,
                        title = title.ifEmpty { "Marcador ${markers.size + 1}" },
                        snippet = snippet,
                        userEmail = userEmail
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
                // No es necesario volver a establecer el documento, ya que add() ya lo hace
                addMarkerToMap(markerWithId)
                showToast("Marcador guardado")
            }
            .addOnFailureListener { e ->
                showToast("Error al guardar: ${e.message}")
            }
    }

    private fun loadUserMarkers() {
        auth.currentUser ?.email?.let { userEmail -> // Cambiado de userId a userEmail
            firestore.collection(MARKERS_COLLECTION)
                .whereEqualTo("userEmail", userEmail) // Cambiado de userId a userEmail
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
        val markerTag = marker.tag
        val isSharedLocation = markerTag is String && markerTag.startsWith("shared_location_id_") // Ejemplo de cómo identificar

        val builder = AlertDialog.Builder(requireContext())
            .setTitle(marker.title)
            .setMessage(marker.snippet ?: "¿Qué deseas hacer con este marcador?")
            .setNegativeButton("Cerrar", null)

        if (isSharedLocation) {
            builder.setPositiveButton("Eliminar compartido") { _, _ ->
                val sharedLocationId = (markerTag as String).removePrefix("shared_location_id_")
                authViewModel.deleteSharedLocation(sharedLocationId)
                marker.remove() // Eliminar también del mapa local
            }
        } else {
            builder.setPositiveButton("Eliminar mi marcador") { _, _ -> deleteMarker(marker) }
        }

        builder.show()
        return true
    }

    private fun deleteMarker(marker: Marker) {
        (marker.tag as? String)?.let { markerId ->
            firestore.collection(MARKERS_COLLECTION).document(markerId)
                .get()
                .addOnSuccessListener { document ->
                    // Verificar si el documento existe y si el userEmail coincide
                    val mapMarker = document.toObject(MapMarker::class.java)
                    if (mapMarker != null && mapMarker.userEmail == auth.currentUser ?.email) {
                        // Eliminar el marcador
                        firestore.collection(MARKERS_COLLECTION).document(markerId)
                            .delete() // Cambiado de update a delete
                            .addOnSuccessListener {
                                // Eliminar el marcador de la interfaz
                                marker.remove()
                                markers.remove(marker)
                                showToast("Marcador eliminado")
                            }
                            .addOnFailureListener { e ->
                                showToast("Error al eliminar: ${e.message}")
                            }
                    } else {
                        showToast("No tienes permiso para eliminar este marcador.")
                    }
                }
                .addOnFailureListener { e ->
                    showToast("Error al obtener el marcador: ${e.message}")
                }
        } ?: run {
            showToast("Error: ID de marcador no válido")
        }
    }

    private fun fetchMarkers() {
        firestore.collection(MARKERS_COLLECTION)
            .whereEqualTo("isDeleted", false) // Filtrar marcadores eliminados
            .get()
            .addOnSuccessListener { documents ->
                markers.clear()
                for (document in documents) {
                    val marker = document.toObject(Marker::class.java)
                    markers.add(marker)
                }
                loadUserMarkers()
            }
            .addOnFailureListener { e ->
                showToast("Error al recuperar marcadores: ${e.message}")
            }
    }

    // Métodos de la interfaz del mapa
    override fun onMyLocationButtonClick(): Boolean {
        if (isLocationPermissionGranted()) {
            if (::map.isInitialized) {
                // Verifica si la ubicación está habilitada
                if (map.isMyLocationEnabled) {
                    val location = map.myLocation
                    if (location != null) {
                        val latLng = LatLng(location.latitude, location.longitude)
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                        showToast("Centrando en tu ubicación...")
                    } else {
                        showToast("No se pudo obtener la ubicación actual. Asegúrate de que los servicios de ubicación estén habilitados.");
                    }
                } else {
                    showToast("La ubicación no está habilitada en el mapa.");
                }
            } else {
                showToast("El mapa no está inicializado.");
            }
        } else {
            showToast("Permiso de ubicación no concedido.");
        }
        return true
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