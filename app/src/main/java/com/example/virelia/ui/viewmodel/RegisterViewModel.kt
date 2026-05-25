package com.example.virelia.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.virelia.Database.DatabaseProvider
import com.example.virelia.Database.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import com.google.firebase.auth.userProfileChangeRequest

class RegisterViewModel(
    application: Application
) : AndroidViewModel(application) {

    // Loading State
    var isLoading = mutableStateOf(false)
        private set

    // Error Message
    var errorMessage = mutableStateOf("")
        private set

    // Firebase Auth
    private val auth = FirebaseAuth.getInstance()

    // Firestore
    private val firestore = FirebaseFirestore.getInstance()

    // Room Database
    private val userDao =
        DatabaseProvider
            .getDatabase(application)
            .userDao()

    fun registerUser(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit
    ) {

        // Reset error
        errorMessage.value = ""

        // Validasi field kosong
        if (
            name.isEmpty() ||
            email.isEmpty() ||
            password.isEmpty() ||
            confirmPassword.isEmpty()
        ) {

            errorMessage.value =
                "Semua field harus diisi"

            return
        }

        // Validasi password sama
        if (password != confirmPassword) {

            errorMessage.value =
                "Password tidak sama"

            return
        }

        // Validasi password minimal
        if (password.length < 6) {

            errorMessage.value =
                "Password minimum 6 characters"

            return
        }

        // Mulai loading
        isLoading.value = true

        // Register Firebase Auth
        auth.createUserWithEmailAndPassword(
            email,
            password
        )

            .addOnSuccessListener {

                val profileUpdates = userProfileChangeRequest {
                    displayName = name
                }

                auth.currentUser?.updateProfile(profileUpdates)

                // UID Firebase
                val uid =
                    auth.currentUser?.uid ?: ""

                // Data user untuk Firestore
                val user = hashMapOf(

                    "uid" to uid,

                    "username" to name,

                    "email" to email,

                    "profileImage" to "",

                    "isLoggedIn" to true
                )

                // Simpan ke Firestore
                firestore.collection("users")
                    .document(uid)
                    .set(user)

                    .addOnSuccessListener {

                        // Simpan ke Room Database
                        viewModelScope.launch {

                            userDao.insertUser(

                                UserEntity(

                                    uid = uid,

                                    username = name,

                                    email = email,

                                    password = password,

                                    profileImage = "",

                                    isLoggedIn = false
                                )
                            )
                        }

                        // Logout Firebase
                        FirebaseAuth.getInstance().signOut()

                        isLoading.value = false

                        onSuccess()
                    }

                    .addOnFailureListener {

                        isLoading.value = false

                        errorMessage.value =
                            it.message.toString()
                    }
            }

            .addOnFailureListener {

                isLoading.value = false

                errorMessage.value =
                    it.message.toString()
            }
    }
}