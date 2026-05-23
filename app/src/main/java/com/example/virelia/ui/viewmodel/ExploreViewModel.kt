package com.example.virelia.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.virelia.Database.NoteEntity
import com.example.virelia.data.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ExploreViewModel(application: Application)
    : AndroidViewModel(application) {

    private val _publicStories =
        MutableStateFlow<List<NoteEntity>>(emptyList())
    val publicStories: StateFlow<List<NoteEntity>> = _publicStories

    private val _comments =
        MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    private val _commentCounts =
        MutableStateFlow<Map<String, Int>>(emptyMap())
    val commentCounts: StateFlow<Map<String, Int>> = _commentCounts

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        loadPublicStories()
    }

    fun loadPublicStories() {
        val currentUserId = auth.currentUser?.uid ?: ""

        firestore.collection("stories")
            .get()
            .addOnSuccessListener { result ->
                viewModelScope.launch {

                    val stories = mutableListOf<NoteEntity>()
                    val counts = mutableMapOf<String, Int>()

                    for (document in result) {
                        val firestoreId = document.id

                        val likeDoc = firestore
                            .collection("stories")
                            .document(firestoreId)
                            .collection("likes")
                            .document(currentUserId)
                            .get()
                            .await()

                        val isLikedByMe = likeDoc.exists()

                        val likesSnapshot = firestore
                            .collection("stories")
                            .document(firestoreId)
                            .collection("likes")
                            .get()
                            .await()

                        val likeCount = likesSnapshot.size()

                        // HITUNG LANGSUNG DARI SUBCOLLECTION
                        val commentsSnapshot = firestore
                            .collection("stories")
                            .document(firestoreId)
                            .collection("comments")
                            .get()
                            .await()

                        val commentCount = commentsSnapshot.size()
                        counts[firestoreId] = commentCount

                        val note = NoteEntity(
                            title = document.getString("title") ?: "",
                            desc = document.getString("desc") ?: "",
                            time = document.getString("time") ?: "",
                            userId = document.getString("userId") ?: "",
                            username = document.getString("username") ?: "Unknown",
                            firestoreId = firestoreId,
                            isShared = true,
                            likeCount = likeCount,
                            commentCount = commentCount,
                            isLiked = isLikedByMe
                        )

                        stories.add(note)
                    }

                    _publicStories.value = stories
                    _commentCounts.value = counts
                }
            }
    }

    fun loadComments(firestoreId: String) {
        viewModelScope.launch {
            val snapshot = firestore
                .collection("stories")
                .document(firestoreId)
                .collection("comments")
                .orderBy("time")
                .get()
                .await()

            val list = snapshot.documents.map { doc ->
                Comment(
                    docId = doc.id,
                    userId = doc.getString("userId") ?: "",
                    username = doc.getString("username") ?: "Unknown",
                    comment = doc.getString("text") ?: "",  // ← field "text"
                    time = doc.getString("time") ?: ""
                )
            }

            _comments.value = list
        }
    }

    fun addComment(
        firestoreId: String,
        text: String,
        onDone: () -> Unit
    ) {
        val currentUser = auth.currentUser ?: return
        val uid = currentUser.uid

        viewModelScope.launch {

            val userDoc = firestore
                .collection("users")
                .document(uid)
                .get()
                .await()

            val username = userDoc.getString("username") ?: "Unknown"

            val commentData = hashMapOf(
                "userId" to uid,
                "username" to username,
                "text" to text,
                "time" to System.currentTimeMillis().toString()
            )

            firestore
                .collection("stories")
                .document(firestoreId)
                .collection("comments")
                .add(commentData)
                .await()

            // RELOAD KOMENTAR
            loadComments(firestoreId)

            // HITUNG ULANG DAN UPDATE FIELD commentCount DI FIRESTORE
            val commentsSnapshot = firestore
                .collection("stories")
                .document(firestoreId)
                .collection("comments")
                .get()
                .await()

            val newCount = commentsSnapshot.size()

            firestore
                .collection("stories")
                .document(firestoreId)
                .update("commentCount", newCount)
                .await()

            // UPDATE COUNT LOKAL
            val currentCounts = _commentCounts.value.toMutableMap()
            currentCounts[firestoreId] = newCount
            _commentCounts.value = currentCounts

            // UPDATE publicStories lokal
            val updatedList = _publicStories.value.toMutableList()
            val index = updatedList.indexOfFirst { it.firestoreId == firestoreId }
            if (index != -1) {
                updatedList[index] = updatedList[index].copy(commentCount = newCount)
                _publicStories.value = updatedList
            }

            onDone()
        }
    }

    fun toggleLike(note: NoteEntity) {
        val currentUserId = auth.currentUser?.uid ?: return

        val updatedList = _publicStories.value.toMutableList()
        val index = updatedList.indexOf(note)
        if (index == -1) return

        val currentNote = updatedList[index]
        val updatedNote = currentNote.copy(
            isLiked = !currentNote.isLiked,
            likeCount = if (currentNote.isLiked)
                currentNote.likeCount - 1
            else
                currentNote.likeCount + 1
        )

        updatedList[index] = updatedNote
        _publicStories.value = updatedList

        val storyRef = firestore
            .collection("stories")
            .document(note.firestoreId)

        val likesRef = storyRef
            .collection("likes")
            .document(currentUserId)

        if (updatedNote.isLiked) {
            likesRef.set(mapOf("liked" to true))
            storyRef.update(
                "likeCount",
                com.google.firebase.firestore.FieldValue.increment(1)
            )
        } else {
            likesRef.delete()
            storyRef.update(
                "likeCount",
                com.google.firebase.firestore.FieldValue.increment(-1)
            )
        }
    }
}