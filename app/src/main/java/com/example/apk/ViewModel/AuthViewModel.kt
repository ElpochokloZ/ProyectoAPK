package com.example.apk.ViewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.apk.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore
    private val TAG = "AuthViewModel"

    val authStatus = MutableLiveData<Pair<Boolean, String?>>()
    val fieldErrors = MutableLiveData<String>()

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
}