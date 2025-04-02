package com.example.apk.View

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apk.R
import com.google.firebase.auth.FirebaseAuth

class RecuperarContraActivity : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var btnRecuperarContra: Button
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recuperar_contra)

        editTextEmail = findViewById(R.id.editTextEmail)
        btnRecuperarContra = findViewById(R.id.btnRecuperarContra)
        firebaseAuth = FirebaseAuth.getInstance()

        btnRecuperarContra.setOnClickListener {
            val email = editTextEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Por favor ingrese su correo electrónico", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Enviar el correo de recuperación
            firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Correo de recuperación enviado", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}