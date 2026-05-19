package com.example.virelia.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var passwordVisible by mutableStateOf(false)

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")

    private val auth = FirebaseAuth.getInstance()

    fun login(
        onLoginSuccess: () -> Unit
    ) {

        errorMessage = ""

        if (
            email.isEmpty() ||
            password.isEmpty()
        ) {

            errorMessage =
                "Email dan password wajib diisi"

            return
        }

        isLoading = true

        auth.signInWithEmailAndPassword(
            email,
            password
        )
            .addOnSuccessListener {

                isLoading = false
                onLoginSuccess()
            }

            .addOnFailureListener { exception ->

                isLoading = false

                errorMessage =
                    exception.message ?: "Login gagal"
            }
    }

    fun togglePasswordVisibility() {

        passwordVisible =
            !passwordVisible
    }
}