package com.example.virelia.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.virelia.Database.DatabaseProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)

    var showLogoutDialog = mutableStateOf(false)
        private set

    var profileImageUrl = mutableStateOf("")
        private set

    var username = mutableStateOf("")
        private set

    var email = mutableStateOf("")
        private set

    var imageUri = mutableStateOf<Uri?>(null)
        private set

    var totalLikes = mutableStateOf(0)
        private set

    var totalNotes = mutableStateOf(0)
        private set

    var totalPublic = mutableStateOf(0)
        private set

    var totalComments = mutableStateOf(0)
        private set

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    init {
        getUserData()
        loadTotalLikes()
        loadTotalNotes()
        loadTotalPublic()
        loadTotalComments()
    }

    // TAMBAH FUNGSI INI
    fun refresh() {
        loadTotalLikes()
        loadTotalNotes()
        loadTotalPublic()
        loadTotalComments()
    }

    private fun getUserData() {
        val currentUser = auth.currentUser
        currentUser?.uid?.let { uid ->
            firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    username.value = document.getString("username") ?: "No Username"
                    email.value = document.getString("email") ?: "No Email"
                    profileImageUrl.value = document.getString("profileImage") ?: ""
                }
        }
    }

    fun loadTotalLikes() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            var total = 0
            val stories = firestore
                .collection("stories")
                .whereEqualTo("userId", uid)
                .get()
                .await()

            for (story in stories) {
                val likesSnapshot = firestore
                    .collection("stories")
                    .document(story.id)
                    .collection("likes")
                    .get()
                    .await()
                total += likesSnapshot.size()
            }

            totalLikes.value = total
        }
    }

    fun loadTotalNotes() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            totalNotes.value = db.noteDao().countNotesByUser(uid)
        }
    }

    fun loadTotalPublic() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            totalPublic.value = db.noteDao().countPublicNotesByUser(uid)
        }
    }

    fun loadTotalComments() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            var total = 0
            val stories = firestore
                .collection("stories")
                .whereEqualTo("userId", uid)
                .get()
                .await()

            for (story in stories) {
                val commentsSnapshot = firestore
                    .collection("stories")
                    .document(story.id)
                    .collection("comments")
                    .get()
                    .await()
                total += commentsSnapshot.size()
            }

            totalComments.value = total
        }
    }

    fun updateImage(uri: Uri?) {
        imageUri.value = uri
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(uid)
            .update("profileImage", uri.toString())
            .addOnSuccessListener {
                profileImageUrl.value = uri.toString()
            }
    }

    fun onLogoutClick() { showLogoutDialog.value = true }
    fun onLogoutDismiss() { showLogoutDialog.value = false }
    fun onLogoutConfirm(onLogoutSuccess: () -> Unit) {
        auth.signOut()
        showLogoutDialog.value = false
        onLogoutSuccess()
    }
}