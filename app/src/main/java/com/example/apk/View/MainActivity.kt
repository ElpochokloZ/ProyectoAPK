package com.example.apk.View

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.apk.R
import com.example.apk.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(){
    private lateinit var binding: ActivityMainBinding

    companion object{
        lateinit var firebaseAuth: FirebaseAuth
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



    }
}