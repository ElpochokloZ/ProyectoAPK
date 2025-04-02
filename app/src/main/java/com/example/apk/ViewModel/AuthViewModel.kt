package com.example.apk.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.apk.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Date

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore
    private val TAG = "AuthViewModel"
    private val _sharedLocations = MutableLiveData<List<SharedLocation>>()
    val sharedLocations: LiveData<List<SharedLocation>> = _sharedLocations

    private val _userData = MutableLiveData<Map<String, Any>?>()
    val userData: LiveData<Map<String, Any>?> = _userData

    private val _operationStatus = MutableLiveData<Pair<Boolean, String?>>()
    val operationStatus: LiveData<Pair<Boolean, String?>> = _operationStatus
    private val userCache = HashMap<String, Map<String, Any>>()
    val authStatus = MutableLiveData<Pair<Boolean, String?>>()
    val fieldErrors = MutableLiveData<String>()

    data class SharedLocation(
        val id: String = "",
        val userId: String = "",
        val userEmail: String = "",
        val latitude: Double = 0.0,
        val longitude: Double = 0.0,
        val title: String = "",
        val description: String = "",
        @ServerTimestamp val createdAt: Date? = null
    )

    fun register(email: String, password: String, nombre: String, fechaNacimiento: String) {
        val user = User(email, password)

        if (!user.isValidEmail()) {
            fieldErrors.postValue("El correo no es válido")
            return
        }
        if (!user.isValidPassword()) {
            fieldErrors.postValue("La contraseña debe tener al menos 6 caracteres")
            return
        }

        // Registro en Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Guardar datos en Firestore
                val userData: MutableMap<String, Any> = HashMap()
                userData["email"] = email
                userData["nombre"] = nombre
                userData["fecha de nacimiento"] = fechaNacimiento

                db.collection("usuarios").document(email).set(userData)
                    .addOnSuccessListener {
                        authStatus.postValue(Pair(true, "Registro exitoso"))
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error al guardar datos: ${e.message}")
                        authStatus.postValue(Pair(false, "Error al guardar datos: ${e.message}"))
                    }
            } else {
                Log.e(TAG, "Error al crear usuario: ${task.exception?.message}")
                authStatus.postValue(Pair(false, "Error al crear usuario: ${task.exception?.message}"))
            }
        }
    }

    fun login(email: String, password: String) {
        val user = User(email, password)

        if (!user.isValidEmail()) {
            fieldErrors.postValue("El correo no es válido")
            return
        }
        if (!user.isValidPassword()) {
            fieldErrors.postValue("La contraseña debe tener al menos 6 caracteres")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    authStatus.postValue(Pair(true, "Inicio de sesión exitoso"))
                } else {
                    Log.e(TAG, "Error al iniciar sesión: ${task.exception?.message}")
                    authStatus.postValue(Pair(false, "Error al iniciar sesión: ${task.exception?.message}"))
                }
            }
    }

    fun getUserData(email: String) = userData.apply {

        if (userCache.containsKey(email)) {
            _userData.postValue(userCache[email])
        }else{
            db.collection("usuarios").document(email)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _operationStatus.postValue(Pair(false, "Error al cargar datos"))
                        return@addSnapshotListener
                    }
                    _userData.postValue(snapshot?.data)
                }

        }

    }

    fun updateUserDescription(email: String, description: String) {
        when {
            description.isEmpty() -> _operationStatus.postValue(Pair(false, "La descripción no puede estar vacía"))
            description.length > 500 -> _operationStatus.postValue(Pair(false, "Máximo 500 caracteres"))
            else -> {
                db.collection("usuarios").document(email)
                    .update("descripcion", description)
                    .addOnSuccessListener {
                        _operationStatus.postValue(Pair(true, "Descripción actualizada"))
                    }
                    .addOnFailureListener { e ->
                        _operationStatus.postValue(Pair(false, "Error al guardar: ${e.message}"))
                    }
                    }

            }
    }

    fun loadSharedLocations() {
        db.collection("shared_locations")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _operationStatus.postValue(Pair(false, "Error al cargar ubicaciones"))
                    return@addSnapshotListener
                }

                val locations = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(SharedLocation::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                _sharedLocations.postValue(locations)
            }
    }

    fun saveSharedLocation(location: SharedLocation) {
        db.collection("shared_locations")
            .add(location)
            .addOnSuccessListener {
                _operationStatus.postValue(Pair(true, "Ubicación compartida"))
            }
            .addOnFailureListener { e ->
                _operationStatus.postValue(Pair(false, "Error al compartir: ${e.message}"))
            }
    }
    fun logout() {
        auth.signOut()
        _operationStatus.postValue(Pair(true, "Sesión cerrada"))
    }
}