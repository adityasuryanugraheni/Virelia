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

    // =========================
    // ROOM → UI
    // =========================
    val notes =
        db.noteDao()
            .getNotesByUser(currentUserId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // =========================
    // DELETE NOTE
    // =========================
    fun deleteNote(note: NoteEntity) {

        viewModelScope.launch {

            // DELETE ROOM
            db.noteDao().deleteNote(note)

            // DELETE FIRESTORE
            if (
                note.isShared &&
                note.firestoreId.isNotEmpty()
            ) {

                firestore
                    .collection("stories")
                    .document(note.firestoreId)
                    .delete()
            }
        }
    }

    // =========================
    // SHARE NOTE
    // =========================
    fun shareNote(
        note: NoteEntity,
        onSuccess: () -> Unit,
        onFailed: () -> Unit
    ) {

        // AMBIL USERNAME DARI COLLECTION USERS
        firestore
            .collection("users")
            .document(currentUserId)
            .get()

            .addOnSuccessListener { userDoc ->

                val username =
                    userDoc.getString("username")
                        ?: "Unknown"

                val noteData = hashMapOf(

                    "title" to note.title,

                    "desc" to note.desc,

                    "time" to note.time,

                    "userId" to currentUserId,

                    "username" to username,

                    "likeCount" to note.likeCount,

                    "commentCount" to note.commentCount
                )

                // =========================
                // JIKA BELUM PERNAH DISHARE
                // =========================
                if (note.firestoreId.isEmpty()) {

                    firestore
                        .collection("stories")
                        .add(noteData)

                        .addOnSuccessListener { document ->

                            viewModelScope.launch {

                                db.noteDao().updateNote(

                                    note.copy(

                                        isShared = true,

                                        firestoreId =
                                            document.id
                                    )
                                )
                            }

                            onSuccess()
                        }

                        .addOnFailureListener {

                            onFailed()
                        }

                } else {

                    // =========================
                    // UPDATE STORY
                    // =========================
                    firestore
                        .collection("stories")
                        .document(note.firestoreId)
                        .set(noteData)

                        .addOnSuccessListener {

                            viewModelScope.launch {

                                db.noteDao().updateNote(

                                    note.copy(
                                        isShared = true
                                    )
                                )
                            }

                            onSuccess()
                        }

                        .addOnFailureListener {

                            onFailed()
                        }
                }
            }

            .addOnFailureListener {

                onFailed()
            }
    }

    // =========================
    // SYNC FIRESTORE → ROOM
    // =========================
    fun syncStories() {

        firestore
            .collection("stories")
            .whereEqualTo("userId", currentUserId)
            .get()

            .addOnSuccessListener { result ->

                viewModelScope.launch {

                    val currentNotes =
                        notes.value

                    for (document in result) {

                        val firestoreId =
                            document.id

                        val title =
                            document.getString("title")
                                ?: ""

                        val desc =
                            document.getString("desc")
                                ?: ""

                        // =========================
                        // CEK DUPLIKAT
                        // =========================
                        val alreadyExists =
                            currentNotes.any {

                                // SUDAH ADA FIRESTORE ID
                                it.firestoreId == firestoreId ||

                                        // ATAU TITLE & DESC SAMA
                                        (
                                                it.title == title &&
                                                        it.desc == desc
                                                )
                            }

                        val note = NoteEntity(

                            title = title,

                            desc = desc,

                            time =
                                document.getString("time")
                                    ?: "",

                            userId =
                                document.getString("userId")
                                    ?: "",

                            username =
                                document.getString("username")
                                    ?: "",

                            firestoreId =
                                firestoreId,

                            isShared = true,

                            likeCount = document.getLong("likeCount")?.toInt() ?: 0,
                            commentCount = document.getLong("commentCount")?.toInt() ?: 0
                        )

                        // =========================
                        // INSERT / UPDATE
                        // =========================
                        if (!alreadyExists) {

                            db.noteDao().insertNote(note)

                        } else {

                            val existingNote = currentNotes.find {
                                it.firestoreId == firestoreId
                            }

                            if (existingNote != null) {

                                db.noteDao().updateNote(

                                    existingNote.copy(

                                        likeCount = note.likeCount,

                                        commentCount = note.commentCount
                                    )
                                )
                            }
                        }
                    }
                }
            }
    }
}