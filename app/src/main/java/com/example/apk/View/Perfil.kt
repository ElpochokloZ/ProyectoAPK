package com.example.apk.View

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.apk.R
import com.example.apk.databinding.FragmentPerfilBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

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

        // Inicializar FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()
        user = firebaseAuth.currentUser

        // Mostrar el correo del usuario en el TextView
        user?.let {
            binding.txtViewMail.text = it.email // Asignar el correo al TextView
        } ?: run {
            binding.txtViewMail.text = "No hay usuario conectado" // Mensaje alternativo
        }

        // Configurar el listener para el botón de cerrar sesión
        binding.btnlogout.setOnClickListener {
            SalirAplicacion()
        }

        return binding.root // Devolver la vista inflada
    }

    private fun SalirAplicacion() {
        firebaseAuth.signOut() // Cerrar sesión
        val intent = Intent(activity, AuthActivity::class.java) // Crear un Intent para AuthActivity
        Toast.makeText(activity, "Cerraste sesión", Toast.LENGTH_SHORT).show() // Mostrar un mensaje
        startActivity(intent) // Iniciar AuthActivity
        activity?.finish() // Opcional: cerrar la actividad actual debido al user null
    }
}