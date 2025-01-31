package com.example.apk.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.apk.Model.FireBaseAutenticacion
import com.example.apk.Model.User
import com.example.apk.databinding.ActivityAuthBinding


class AuthViewModel : ViewModel() {
    private lateinit var binding: ActivityAuthBinding
    private val authRepository = FireBaseAutenticacion()

    val authStatus = MutableLiveData<Pair<Boolean, String?>>()
    val fieldErrors = MutableLiveData<String>()

    fun register(email: String, password: String){
        val user = User(email, password)

        if (!user.isValidEmail()) {
            fieldErrors.postValue("El correo no es válido")
            return
        }
        if (!user.isValidPassword()) {
            fieldErrors.postValue("La contraseña debe tener al menos 6 caracteres")
            return
        }

        authRepository.registerUser(user.email, user.password) { isSuccess, message ->
            authStatus.postValue(Pair(isSuccess, message))
        }
    }

    fun login(email: String, password: String){
        val user = User(email, password)

        if (!user.isValidEmail()) {
            fieldErrors.postValue("El correo no es válido")
            return
        }
        if (!user.isValidPassword()) {
            fieldErrors.postValue("La contraseña debe tener al menos 6 caracteres")
            return
        }

        authRepository.loginUser(user.email, user.password) { isSuccess, message ->
            authStatus.postValue(Pair(isSuccess, message))
        }
    }
}