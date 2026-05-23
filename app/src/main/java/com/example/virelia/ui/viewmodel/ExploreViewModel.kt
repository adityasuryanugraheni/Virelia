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
import com.google.firebase.firestore.FieldValue
import com.example.virelia.data.Comment
import android.util.Log

class ExploreViewModel(application: Application)
    : AndroidViewModel(application) {

    private val _publicStories =
        MutableStateFlow<List<NoteEntity>>(emptyList())

    val publicStories: StateFlow<List<NoteEntity>> = _publicStories

    // KOMENTAR PER STORY
    private val _comments =
        MutableStateFlow<List<Comment>>(emptyList())

    val comments: StateFlow<List<Comment>> = _comments

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
                            commentCount =
                                document.getLong("commentCount")
                                    ?.toInt() ?: 0,
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
                Comment(
                    userId = doc.getString("userId") ?: "",
                    username = doc.getString("username") ?: "Unknown",
                    comment = doc.getString("comment") ?: "",
                    noteId = firestoreId,
                    time = doc.getString("time") ?: ""
                )
            }

            _comments.value = list
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

        val storyRef = firestore
            .collection("stories")
            .document(note.firestoreId)

        val likesRef = storyRef
            .collection("likes")
            .document(currentUserId)

        if (updatedNote.isLiked) {

            // LIKE
            likesRef.set(mapOf("liked" to true))

            // UPDATE TOTAL LIKE
            storyRef.update(
                "likeCount",
                com.google.firebase.firestore.FieldValue.increment(1)
            )

        } else {

            // UNLIKE
            likesRef.delete()

            // UPDATE TOTAL LIKE
            storyRef.update(
                "likeCount",
                com.google.firebase.firestore.FieldValue.increment(-1)
            )
        }
    }
}