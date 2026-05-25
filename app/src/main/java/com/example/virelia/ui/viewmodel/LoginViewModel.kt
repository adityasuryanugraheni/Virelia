package com.example.virelia.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

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
                saveFcmToken()

                isLoading = false
                onLoginSuccess()
            }

            .addOnFailureListener { exception ->

                isLoading = false

                errorMessage =
                    exception.message ?: "Login gagal"
            }
    }
    private fun saveFcmToken() {

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->

                android.util.Log.d("FCM_TOKEN", token)

                val uid = FirebaseAuth
                    .getInstance()
                    .currentUser
                    ?.uid
                    ?: return@addOnSuccessListener

                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .update("fcmToken", token)
                    .addOnSuccessListener {

                        android.util.Log.d(
                            "FCM_TOKEN",
                            "Token berhasil disimpan"
                        )
                    }
                    .addOnFailureListener {

                        android.util.Log.e(
                            "FCM_TOKEN",
                            "Gagal simpan token"
                        )
                    }
            }
            .addOnFailureListener {

                android.util.Log.e(
                    "FCM_TOKEN",
                    "Gagal mengambil token"
                )
            }
    }

    fun togglePasswordVisibility() {

        passwordVisible =
            !passwordVisible
    }
}