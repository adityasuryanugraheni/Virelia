// HOMEVIEWMODEL.KT

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

    private val currentUserId =
        FirebaseAuth.getInstance()
            .currentUser
            ?.uid ?: ""

    // ROOM → UI
    val notes =
        db.noteDao()
            .getNotesByUser(currentUserId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // DELETE NOTE
    fun deleteNote(note: NoteEntity) {

        viewModelScope.launch {

            // DELETE ROOM
            db.noteDao().deleteNote(note)

            // DELETE FIRESTORE
            if (
                note.isShared
                &&
                note.firestoreId.isNotEmpty()
            ) {

                FirebaseFirestore.getInstance()
                    .collection("stories")
                    .document(note.firestoreId)
                    .delete()
            }
        }
    }

    // SHARE NOTE
    fun shareNote(
        note: NoteEntity,
        onSuccess: () -> Unit,
        onFailed: () -> Unit
    ) {

        val noteData = hashMapOf(

            "title" to note.title,
            "desc" to note.desc,
            "time" to note.time,
            "userId" to currentUserId
        )

        FirebaseFirestore.getInstance()
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

    // SYNC FIRESTORE → ROOM
    fun syncStories() {

        FirebaseFirestore.getInstance()
            .collection("stories")
            .whereEqualTo("userId", currentUserId)
            .get()

            .addOnSuccessListener { result ->

                viewModelScope.launch {

                    for (document in result) {

                        val firestoreId =
                            document.id

                        // CEK APAKAH NOTE SUDAH ADA
                        val existingNotes =
                            notes.value

                        val alreadyExists =
                            existingNotes.any {

                                it.firestoreId ==
                                        firestoreId
                            }

                        // JIKA BELUM ADA → INSERT
                        if (!alreadyExists) {

                            val note = NoteEntity(

                                title =
                                    document.getString("title")
                                        ?: "",

                                desc =
                                    document.getString("desc")
                                        ?: "",

                                time =
                                    document.getString("time")
                                        ?: "",

                                userId =
                                    document.getString("userId")
                                        ?: "",

                                firestoreId =
                                    firestoreId,

                                isShared = true
                            )

                            db.noteDao()
                                .insertNote(note)
                        }
                    }
                }
            }
    }
}