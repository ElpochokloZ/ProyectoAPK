package com.example.apk.Model

import android.util.Log
import com.google.firebase.auth.FirebaseAuth


class FireBaseAutenticacion {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    fun registerUser(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{ task->
                if (task.isSuccessful){
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message)
                }

            }
    }

    fun loginUser(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }
}