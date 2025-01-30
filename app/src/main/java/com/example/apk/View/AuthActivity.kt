package com.example.apk.View

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.apk.Model.FireBaseAutenticacion
import com.example.apk.ViewModel.AuthViewModel
import com.example.apk.databinding.ActivityAuthBinding
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

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




        //Analitycs events
        val analytics:FirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("message","Integracion de firebase completa")
        analytics.logEvent("InitScreen", bundle)


       //Instanciacion de FireBaseAuth y AuthViewModel
       firebaseAuth = Firebase.auth
        viewModel=ViewModelProvider(this)[AuthViewModel::class.java]


        binding.txtRegistro.setOnClickListener{
            try {
                val intent = Intent(this, RegActivity::class.java)
                startActivity(intent)
            }catch(e: Exception) {
                Log.e("AuthActivity", "Error al iniciar RegActivity", e)
            }

        }

        binding.btnInicioSesion.setOnClickListener{
            val email = binding.editTextTextEmailAddress.text.toString().trim()
            val pass = binding.editTextTextPassword.text.toString().trim()



            if (email.isNotEmpty() && pass.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener{ task->
                    if (task.isSuccessful) {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Log.e("LoginError", " ${task.exception?.message}")
                        Toast.makeText(this,  "Error desconocido ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "NO SE ACEPTAN CAMPOS VACIOS", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.authStatus.observe(this) { status ->
            val (isSuccess, message) = status
            if (isSuccess) {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
            }
        }

     /*    fun onStart() {
            super.onStart()

            if(firebaseAuth.currentUser != null){
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }*/
    }
}