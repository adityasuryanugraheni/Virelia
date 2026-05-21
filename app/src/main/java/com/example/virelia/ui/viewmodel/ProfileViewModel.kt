package com.example.virelia.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.virelia.Database.DatabaseProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)

    // LOGOUT DIALOG
    var showLogoutDialog = mutableStateOf(false)
        private set

    // PROFIL
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

    // TOTAL NOTES
    var totalNotes = mutableStateOf(0)
        private set

    // TOTAL PUBLIC
    var totalPublic = mutableStateOf(0)
        private set

    // FIREBASE
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    init {
        getUserData()
        loadTotalLikes()
        loadTotalNotes()
        loadTotalPublic()
    }

    // AMBIL DATA USER
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

    // HITUNG TOTAL LIKE
    fun loadTotalLikes() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("stories")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { result ->
                var total = 0
                for (document in result) {
                    total += document.getLong("likeCount")?.toInt() ?: 0
                }
                totalLikes.value = total
            }
    }

    // HITUNG TOTAL NOTES DARI ROOM
    fun loadTotalNotes() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            totalNotes.value = db.noteDao().countNotesByUser(uid)
        }
    }

    // HITUNG TOTAL PUBLIC DARI ROOM
    fun loadTotalPublic() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            totalPublic.value = db.noteDao().countPublicNotesByUser(uid)
        }
    }

    // UPDATE FOTO
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

    // LOGOUT DIALOG
    fun onLogoutClick() {
        showLogoutDialog.value = true
    }

    fun onLogoutDismiss() {
        showLogoutDialog.value = false
    }

    fun onLogoutConfirm(onLogoutSuccess: () -> Unit) {
        auth.signOut()
        showLogoutDialog.value = false
        onLogoutSuccess()
    }
}