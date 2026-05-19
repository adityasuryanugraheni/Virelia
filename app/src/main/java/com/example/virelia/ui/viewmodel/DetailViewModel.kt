package com.example.virelia.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class DetailViewModel : ViewModel() {

    var comment by mutableStateOf("")

    var isLiked by mutableStateOf(false)

    var likeCount by mutableStateOf(0)

    var isLoading by mutableStateOf(false)

    var message by mutableStateOf("")

    private val db = FirebaseFirestore.getInstance()

    fun postComment() {

        if (comment.isEmpty()) {

            message = "Comment tidak boleh kosong"

            return
        }

        isLoading = true

        val data = hashMapOf(
            "comment" to comment
        )

        db.collection("comments")
            .add(data)

            .addOnSuccessListener {

                isLoading = false

                message = "Comment berhasil dikirim"

                comment = ""
            }

            .addOnFailureListener {

                isLoading = false

                message = "Gagal mengirim comment"
            }
    }

    fun toggleLike() {

        if (isLiked) {
            isLiked = false
            likeCount--
        } else {
            isLiked = true
            likeCount++
        }
    }
}