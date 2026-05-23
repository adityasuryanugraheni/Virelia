package com.example.virelia.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.virelia.Database.NoteEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class CommentItem(
    val userId: String = "",
    val username: String = "",
    val text: String = "",
    val time: String = ""
)

class ExploreViewModel(application: Application)
    : AndroidViewModel(application) {

    private val _publicStories =
        MutableStateFlow<List<NoteEntity>>(emptyList())

    val publicStories: StateFlow<List<NoteEntity>> = _publicStories

    // KOMENTAR PER STORY
    private val _comments =
        MutableStateFlow<List<CommentItem>>(emptyList())

    val comments: StateFlow<List<CommentItem>> = _comments

    // JUMLAH KOMENTAR PER STORY (firestoreId -> count)
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

                        // CEK APAKAH USER INI SUDAH LIKE
                        val likeDoc = firestore
                            .collection("stories")
                            .document(firestoreId)
                            .collection("likes")
                            .document(currentUserId)
                            .get()
                            .await()

                        val isLikedByMe = likeDoc.exists()

                        // HITUNG TOTAL LIKE DARI SUBCOLLECTION
                        val likesSnapshot = firestore
                            .collection("stories")
                            .document(firestoreId)
                            .collection("likes")
                            .get()
                            .await()

                        val likeCount = likesSnapshot.size()

                        // HITUNG TOTAL KOMENTAR
                        val commentsSnapshot = firestore
                            .collection("stories")
                            .document(firestoreId)
                            .collection("comments")
                            .get()
                            .await()

                        counts[firestoreId] = commentsSnapshot.size()

                        val note = NoteEntity(
                            title = document.getString("title") ?: "",
                            desc = document.getString("desc") ?: "",
                            time = document.getString("time") ?: "",
                            userId = document.getString("userId") ?: "",
                            username = document.getString("username") ?: "Unknown",
                            firestoreId = firestoreId,
                            isShared = true,
                            likeCount = likeCount,
                            isLiked = isLikedByMe
                        )

                        stories.add(note)
                    }

                    _publicStories.value = stories
                    _commentCounts.value = counts
                }
            }
    }

    // LOAD KOMENTAR UNTUK STORY TERTENTU
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
                CommentItem(
                    userId = doc.getString("userId") ?: "",
                    username = doc.getString("username") ?: "Unknown",
                    text = doc.getString("text") ?: "",
                    time = doc.getString("time") ?: ""
                )
            }

            _comments.value = list
        }
    }

    // KIRIM KOMENTAR
    fun addComment(
        firestoreId: String,
        text: String,
        onDone: () -> Unit
    ) {
        val currentUser = auth.currentUser ?: return
        val uid = currentUser.uid

        viewModelScope.launch {

            // AMBIL USERNAME DARI FIRESTORE
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

            // SIMPAN KE SUBCOLLECTION COMMENTS
            firestore
                .collection("stories")
                .document(firestoreId)
                .collection("comments")
                .add(commentData)
                .await()

            // RELOAD KOMENTAR
            loadComments(firestoreId)

            // UPDATE COUNT LOKAL
            val currentCounts = _commentCounts.value.toMutableMap()
            currentCounts[firestoreId] = (currentCounts[firestoreId] ?: 0) + 1
            _commentCounts.value = currentCounts

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
            likeCount =
                if (currentNote.isLiked)
                    currentNote.likeCount - 1
                else
                    currentNote.likeCount + 1
        )

        updatedList[index] = updatedNote
        _publicStories.value = updatedList

        val likesRef = firestore
            .collection("stories")
            .document(note.firestoreId)
            .collection("likes")
            .document(currentUserId)

        if (updatedNote.isLiked) {
            likesRef.set(mapOf("liked" to true))
        } else {
            likesRef.delete()
        }
    }
}