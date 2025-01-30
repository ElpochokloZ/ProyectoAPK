package com.example.apk.View

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apk.databinding.ActivityRegBinding
import com.google.firebase.auth.FirebaseAuth


class RegActivity : AppCompatActivity() {
    lateinit var binding: ActivityRegBinding
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //inicializar ViewBinding
        binding = ActivityRegBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

       binding.btnRegistro.setOnClickListener{

           val email = binding.editTxtEmail.text.toString().trim()
           val password = binding.editTextContra.toString().trim()

           if(email.isEmpty()) {
               binding.editTxtEmail.error = "El correo es obligatorio"
               binding.editTxtEmail.requestFocus()
               return@setOnClickListener

           }

           if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
               binding.editTxtEmail.error = "Introduzca otro correo valido"
               binding.editTxtEmail.requestFocus()
               return@setOnClickListener
           }

           if (password.isEmpty()) {
               binding.editTextContra.error = "La contraseña es obligatoria"
               binding.editTextContra.requestFocus()
               return@setOnClickListener
           }

            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{
                task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this, AuthActivity::class.java)
                        startActivity(intent)
                        Toast.makeText(this, "Cuenta creada con éxito", Toast.LENGTH_SHORT).show()
                        finish() // Volver al login
                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT)
                            .show()
                    }

                }
        }
        /*
        binding.loginLink.setOnClickListener {
            finish()
        }*/
    }
}