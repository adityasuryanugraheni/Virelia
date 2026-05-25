package com.example.virelia.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.virelia.Database.DatabaseProvider
import com.example.virelia.Database.NoteEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel(application: Application)
    : AndroidViewModel(application) {

    private val db =
        DatabaseProvider.getDatabase(application)

    private val firestore =
        FirebaseFirestore.getInstance()

    private val currentUserId =
        FirebaseAuth.getInstance()
            .currentUser
            ?.uid ?: ""

    val notes =
        db.noteDao()
            .getNotesByUser(currentUserId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            db.noteDao().deleteNote(note)
            if (note.isShared && note.firestoreId.isNotEmpty()) {
                firestore
                    .collection("stories")
                    .document(note.firestoreId)
                    .delete()
            }
        }
    }

    fun shareNote(
        note: NoteEntity,
        onSuccess: () -> Unit,
        onFailed: () -> Unit
    ) {
        firestore
            .collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { userDoc ->

                val username = userDoc.getString("username") ?: "Unknown"

                val noteData = hashMapOf(
                    "title" to note.title,
                    "desc" to note.desc,
                    "time" to note.time,
                    "userId" to currentUserId,
                    "username" to username,
                    "imageUrl" to note.imageUrl,
                    "likeCount" to note.likeCount,
                    "commentCount" to note.commentCount
                )

                if (note.firestoreId.isEmpty()) {
                    firestore
                        .collection("stories")
                        .add(noteData)
                        .addOnSuccessListener { document ->
                            viewModelScope.launch {
                                db.noteDao().updateNote(
                                    note.copy(
                                        isShared = true,
                                        firestoreId = document.id
                                    )
                                )
                            }
                            onSuccess()
                        }
                        .addOnFailureListener { onFailed() }

                } else {
                    firestore
                        .collection("stories")
                        .document(note.firestoreId)
                        .set(noteData)
                        .addOnSuccessListener {
                            viewModelScope.launch {
                                db.noteDao().updateNote(
                                    note.copy(isShared = true)
                                )
                            }
                            onSuccess()
                        }
                        .addOnFailureListener { onFailed() }
                }
            }
            .addOnFailureListener { onFailed() }
    }

    fun syncStories() {
        viewModelScope.launch {
            try {
                val result = firestore
                    .collection("stories")
                    .whereEqualTo("userId", currentUserId)
                    .get()
                    .await()

                val currentNotes = notes.value

                for (document in result) {
                    val firestoreId = document.id
                    val title = document.getString("title") ?: ""
                    val desc = document.getString("desc") ?: ""

                    // HITUNG LANGSUNG DARI SUBCOLLECTION
                    val commentsSnapshot = firestore
                        .collection("stories")
                        .document(firestoreId)
                        .collection("comments")
                        .get()
                        .await()

                    val commentCount = commentsSnapshot.size()

                    // HITUNG LIKES DARI SUBCOLLECTION
                    val likesSnapshot = firestore
                        .collection("stories")
                        .document(firestoreId)
                        .collection("likes")
                        .get()
                        .await()

                    val likeCount = likesSnapshot.size()

                    val alreadyExists = currentNotes.any {
                        it.firestoreId == firestoreId ||
                                (it.title == title && it.desc == desc)
                    }

                    val note = NoteEntity(
                        title = title,
                        desc = desc,
                        time = document.getString("time") ?: "",
                        userId = document.getString("userId") ?: "",
                        username = document.getString("username") ?: "",
                        imageUrl = document.getString("imageUrl") ?: "",
                        firestoreId = firestoreId,
                        isShared = true,
                        likeCount = likeCount,
                        commentCount = commentCount
                    )

                    if (!alreadyExists) {
                        db.noteDao().insertNote(note)
                    } else {
                        val existingNote = currentNotes.find {
                            it.firestoreId == firestoreId
                        }
                        if (existingNote != null) {
                            db.noteDao().updateNote(
                                existingNote.copy(
                                    likeCount = likeCount,
                                    commentCount = commentCount
                                )
                            )
                        }
                    }
                }

            } catch (e: Exception) {
                // silent fail
            }
        }
    }
}