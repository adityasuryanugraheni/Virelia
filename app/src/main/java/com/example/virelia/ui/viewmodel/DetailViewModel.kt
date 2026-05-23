package com.example.virelia.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.example.virelia.data.Comment

class DetailViewModel : ViewModel() {

    var comment by mutableStateOf("")

    var isLiked by mutableStateOf(false)

    var likeCount by mutableStateOf(0)

    var isLoading by mutableStateOf(false)

    var message by mutableStateOf("")

    var commentList = mutableStateListOf<Comment>()

    private val db = FirebaseFirestore.getInstance()

    fun postComment(title: String) {

        if (comment.isEmpty()) {

            message = "Comment tidak boleh kosong"

            return
        }

        isLoading = true

        val user = com.google.firebase.auth.FirebaseAuth
            .getInstance()
            .currentUser

        val username =
            user?.email ?: "Unknown"

        val data = hashMapOf(
            "comment" to comment,
            "noteId" to title,
            "username" to username
        )

        db.collection("comments")
            .add(data)

            .addOnSuccessListener {

                isLoading = false

                message = "Comment berhasil dikirim"

                comment = ""

                getComments(title)
            }

            .addOnFailureListener {

                isLoading = false

                message = "Gagal mengirim comment"
            }
    }

    fun getComments(title: String) {

        db.collection("comments")
            .whereEqualTo("noteId", title)
            .get()
            .addOnSuccessListener { result ->

                commentList.clear()

                for (document in result) {

                    val data = document.toObject(Comment::class.java)

                    commentList.add(data)
                }
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