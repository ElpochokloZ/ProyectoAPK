package com.example.apk.View

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.apk.ViewModel.AuthViewModel
import com.example.apk.databinding.ActivityAuthBinding
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Analytics events
        val analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("message", "Integración de Firebase completa")
        analytics.logEvent("InitScreen", bundle)

        // Inicialización de Firebase Auth y ViewModel
        firebaseAuth = Firebase.auth
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        // Click para ir a la actividad de registro
        binding.txtRegistro.setOnClickListener {
            try {
                val intent = Intent(this, RegActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("AuthActivity", "Error al iniciar RegActivity", e)
            }
        }

        // Click para iniciar sesión
        binding.btnInicioSesion.setOnClickListener {
            val email = binding.editTextTextEmailAddress.text.toString().trim()
            val pass = binding.editTextTextPassword.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Email y contraseña son requeridos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.login(email, pass)
        }

        // Observador para el estado de autenticación
        viewModel.authStatus.observe(this) { status ->
            val (isSuccess, message) = status
            if (isSuccess) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // Cierra AuthActivity para no volver atrás
            } else {
                Toast.makeText(this, "Error en el inicio de sesión: $message", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.fieldErrors.observe(this) { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }

        // Click para recuperar contraseña
        binding.txtRecuperarContra.setOnClickListener {
            val intent = Intent(this, RecuperarContraActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser  != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}