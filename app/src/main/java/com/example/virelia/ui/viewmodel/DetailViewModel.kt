package com.example.virelia.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.virelia.data.Comment
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DetailViewModel : ViewModel() {

    var comment by mutableStateOf("")
    var isLiked by mutableStateOf(false)
    var likeCount by mutableStateOf(0)
    var isLoading by mutableStateOf(false)
    var message by mutableStateOf("")
    var commentList = mutableStateListOf<Comment>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    fun postComment(firestoreId: String) {

        if (comment.isEmpty()) {
            message = "Comment tidak boleh kosong"
            return
        }

        isLoading = true
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val userDoc = db.collection("users")
                    .document(uid)
                    .get()
                    .await()

                val username = userDoc.getString("username") ?: "Unknown"

                val data = hashMapOf(
                    "userId" to uid,
                    "username" to username,
                    "text" to comment,
                    "time" to System.currentTimeMillis().toString()
                )

                db.collection("stories")
                    .document(firestoreId)
                    .collection("comments")
                    .add(data)
                    .await()

                db.collection("stories")
                    .document(firestoreId)
                    .update("commentCount", FieldValue.increment(1))
                    .await()

                message = "Comment berhasil dikirim"
                comment = ""
                isLoading = false
                getComments(firestoreId)

            } catch (e: Exception) {
                isLoading = false
                message = "Gagal mengirim comment"
            }
        }
    }

    fun getComments(firestoreId: String) {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("stories")
                    .document(firestoreId)
                    .collection("comments")
                    .orderBy("time")
                    .get()
                    .await()

                commentList.clear()
                for (doc in snapshot.documents) {
                    commentList.add(
                        Comment(
                            docId = doc.id,
                            userId = doc.getString("userId") ?: "",
                            username = doc.getString("username") ?: "Unknown",
                            comment = doc.getString("text") ?: ""
                        )
                    )
                }

            } catch (e: Exception) {
                message = "Gagal memuat komentar"
            }
        }
    }

    fun deleteComment(firestoreId: String, docId: String) {
        viewModelScope.launch {
            try {
                db.collection("stories")
                    .document(firestoreId)
                    .collection("comments")
                    .document(docId)
                    .delete()
                    .await()

                db.collection("stories")
                    .document(firestoreId)
                    .update("commentCount", FieldValue.increment(-1))
                    .await()

                getComments(firestoreId)

            } catch (e: Exception) {
                message = "Gagal menghapus komentar"
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