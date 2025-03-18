package com.example.apk.View

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.apk.databinding.FragmentPerfilBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class Perfil : Fragment() {
    private lateinit var binding: FragmentPerfilBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var user: FirebaseUser ? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflar el layout y obtener la instancia de binding
        binding = FragmentPerfilBinding.inflate(inflater, container, false)

        // Inicializar FirebaseAuth y obtener el usuario actual
        firebaseAuth = FirebaseAuth.getInstance()
        user = firebaseAuth.currentUser

        // Mostrar el correo del usuario en el TextView
        user?.let {
            obtenerDatosUser (it.uid) // Llamar a la función para obtener datos del usuario
        } ?: run {
            binding.txtViewMail.text = "No hay usuario conectado" // Mensaje alternativo
        }

        // Configurar el listener para el botón de cerrar sesión
        binding.btnlogout.setOnClickListener {
            salirAplicacion()
        }

        return binding.root // Devolver la vista inflada
    }

    private fun obtenerDatosUser (uid: String) {
        val db = FirebaseFirestore.getInstance()
        val documentoRef = db.collection("usuarios").document(uid)

        documentoRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val nombre = document.getString("nombre") // Asegúrate de que el campo "nombre" existe en tu documento
                    val email = document.getString("email") // Asegúrate de que el campo "email" existe en tu documento

                    // Asignar los datos obtenidos a los TextViews
                    binding.txtNombrePerfil.text = nombre.toString()
                    binding.txtViewMail.text = email.toString()
                    // Si tienes un TextView para la fecha de nacimiento, también puedes asignarlo aquí
                    // binding.txtFechaNacimiento.text = fechaNacimiento ?: "Fecha no disponible"
                } else {
                    println("No se encontró el documento")
                }
            }
            .addOnFailureListener { e ->
                println("Error al obtener el documento: $e")
            }
    }

    private fun salirAplicacion() {
        firebaseAuth.signOut() // Cerrar sesión
        val intent = Intent(activity, AuthActivity::class.java) // Crear un Intent para AuthActivity
        Toast.makeText(activity, "Cerraste sesión", Toast.LENGTH_SHORT).show() // Mostrar un mensaje
        startActivity(intent) // Iniciar AuthActivity
        activity?.finish() // Opcional: cerrar la actividad actual
    }
}