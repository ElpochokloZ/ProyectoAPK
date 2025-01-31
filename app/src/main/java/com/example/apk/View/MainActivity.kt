package com.example.apk.View

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.apk.R
import com.example.apk.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}