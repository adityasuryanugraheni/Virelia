package com.example.virelia.viewmodel

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileViewModel : ViewModel() {

    var username = mutableStateOf("")
        private set

    var email = mutableStateOf("")
        private set

    var imageUri = mutableStateOf<Uri?>(null)
        private set

    private val auth = FirebaseAuth.getInstance()

    private val db = FirebaseFirestore.getInstance()

    init {

        getUserData()
    }

    private fun getUserData() {

        val currentUser = auth.currentUser

        currentUser?.uid?.let { uid ->

            db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->

                    username.value =
                        document.getString("username") ?: ""

                    email.value =
                        document.getString("email") ?: ""
                }
        }
    }

    fun updateImage(uri: Uri?) {

        imageUri.value = uri
    }

    fun logout(
        onLogoutSuccess: () -> Unit
    ) {

        auth.signOut()

        onLogoutSuccess()
    }
}