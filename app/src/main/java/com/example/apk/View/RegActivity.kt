package com.example.apk.View

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.apk.ViewModel.AuthViewModel
import com.example.apk.databinding.ActivityRegBinding
import com.google.firebase.auth.FirebaseAuth


class RegActivity : AppCompatActivity() {
    lateinit var binding: ActivityRegBinding
    lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //inicializar ViewBinding
        binding = ActivityRegBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        binding.btnRegistro.setOnClickListener {

            val email = binding.editTxtEmail.text.toString().trim()
            val password = binding.editTextContra.text.toString().trim()

            if (email.isEmpty()) {
                binding.editTxtEmail.error = "El correo es obligatorio"
                binding.editTxtEmail.requestFocus()
                return@setOnClickListener

            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.editTxtEmail.error = "Introduzca otro correo valido"
                binding.editTxtEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.editTextContra.error = "La contraseña es obligatoria"
                binding.editTextContra.requestFocus()
                return@setOnClickListener
            }

            viewModel.register(email, password)
        }

        viewModel.authStatus.observe(this) { status ->
            val (isSuccess, message) = status
            if (isSuccess) {
                Toast.makeText(this, "Cuenta creada con éxito", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, AuthActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.fieldErrors.observe(this) { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }
}