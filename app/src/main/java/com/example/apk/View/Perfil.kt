package com.example.apk.View

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.apk.ViewModel.AuthViewModel
import com.example.apk.databinding.FragmentPerfilBinding
import com.google.firebase.auth.FirebaseAuth

class Perfil : Fragment() {
    private lateinit var binding: FragmentPerfilBinding
    private lateinit var authViewModel: AuthViewModel
    private val currentUserEmail get() = FirebaseAuth.getInstance().currentUser?.email

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPerfilBinding.inflate(inflater, container, false)
        authViewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUserData()
        setupListeners()
        setupObservers()
    }

    private fun setupUserData() {
        currentUserEmail?.let { email ->
            binding.txtViewMail.text = email
            loadUserData(email)
        } ?: run {
            binding.txtViewMail.text = "No autenticado"
            Toast.makeText(context, "Debes iniciar sesión", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserData(email: String) {
        authViewModel.getUserData(email).observe(viewLifecycleOwner) { userData ->
            userData?.let {
                binding.txtNombrePerfil.text = it["nombre"] as? String ?: "Sin nombre"
                binding.editTextDescription.setText(it["descripcion"] as? String ?: "")
                binding.txtUbicacion.text = "Nacimiento: ${it["fecha de nacimiento"] as? String ?: "No especificada"}"
            }
        }
    }

    private fun setupListeners() {
        // Guardar automáticamente al perder foco
        binding.editTextDescription.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                saveDescription()
            }
        }

        // Botón de cerrar sesión
        binding.btnlogout.setOnClickListener {
            authViewModel.logout()
            startActivity(Intent(requireActivity(), AuthActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun setupObservers() {
        authViewModel.operationStatus.observe(viewLifecycleOwner) { (success, message) ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveDescription() {
        currentUserEmail?.let { email ->
            val newDescription = binding.editTextDescription.text.toString()
            authViewModel.updateUserDescription(email, newDescription)
        }
    }
}