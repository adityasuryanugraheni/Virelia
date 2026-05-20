package com.example.virelia.viewmodel

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileViewModel : ViewModel() {
    //PROFIL
    var profileImageUrl = mutableStateOf("")
        private set

    // USERNAME
    var username = mutableStateOf("")
        private set

    // EMAIL
    var email = mutableStateOf("")
        private set

    // FOTO PROFILE
    var imageUri = mutableStateOf<Uri?>(null)
        private set

    // TOTAL LIKE
    var totalLikes = mutableStateOf(0)
        private set

    // FIREBASE
    private val auth = FirebaseAuth.getInstance()

    private val db = FirebaseFirestore.getInstance()

    init {

        getUserData()

        loadTotalLikes()
    }

    // AMBIL DATA USER
    private fun getUserData() {

        val currentUser = auth.currentUser

        currentUser?.uid?.let { uid ->

            println("UID LOGIN = $uid")

            db.collection("users")
                .document(uid)
                .get()

                .addOnSuccessListener { document ->

                    username.value =
                        document.getString("username")
                            ?: "No Username"

                    email.value =
                        document.getString("email")
                            ?: "No Email"

                    profileImageUrl.value =
                        document.getString("profileImage")
                            ?: ""

                    println("USERNAME = ${username.value}")
                }
        }
    }

    // HITUNG TOTAL LIKE
    fun loadTotalLikes() {

        val uid =
            FirebaseAuth.getInstance()
                .currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("stories")
            .whereEqualTo("userId", uid)
            .get()

            .addOnSuccessListener { result ->

                var total = 0

                for (document in result) {

                    total +=
                        document.getLong("likeCount")
                            ?.toInt() ?: 0
                }

                totalLikes.value = total
            }
    }

    // UPDATE FOTO
    fun updateImage(uri: Uri?) {

        imageUri.value = uri

        val uid =
            auth.currentUser?.uid ?: return

        db.collection("users")
            .document(uid)
            .update(

                "profileImage",
                uri.toString()
            )

            .addOnSuccessListener {

                profileImageUrl.value =
                    uri.toString()
            }
    }

    // LOGOUT
    fun logout(
        onLogoutSuccess: () -> Unit
    ) {

        auth.signOut()

        onLogoutSuccess()
    }
}