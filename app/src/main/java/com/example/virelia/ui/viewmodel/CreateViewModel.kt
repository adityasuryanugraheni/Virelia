// CREATEVIEWMODEL.KT

package com.example.virelia.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.virelia.Database.DatabaseProvider
import com.example.virelia.Database.NoteEntity
import com.example.virelia.utils.isInternetAvailable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CreateViewModel(application: Application)
    : AndroidViewModel(application) {

    private val db =
        DatabaseProvider.getDatabase(application)

    private val _note =
        MutableStateFlow<NoteEntity?>(null)

    val note: StateFlow<NoteEntity?> = _note

    // AMBIL NOTE
    fun getNote(noteId: Int) {

        viewModelScope.launch {

            val data =
                db.noteDao().getNoteById(noteId)

            _note.value = data
        }
    }

    // SAVE NOTE
    fun saveNote(
        note: NoteEntity?,
        title: String,
        content: String,
        onSuccess: () -> Unit
    ) {

        viewModelScope.launch {

            val userId =
                FirebaseAuth
                    .getInstance()
                    .currentUser
                    ?.uid ?: ""

            // CREATE
            if (note == null) {

                val firestoreId =
                    FirebaseFirestore
                        .getInstance()
                        .collection("stories")
                        .document()
                        .id

                val newNote = NoteEntity(

                    title = title,
                    desc = content,
                    time = "Today",

                    userId = userId,

                    firestoreId = firestoreId,

                    isShared = false
                )

                // ROOM
                db.noteDao().insertNote(newNote)

                // FIRESTORE
                if (isInternetAvailable(getApplication())) {

                    val noteData = hashMapOf(

                        "title" to title,

                        "desc" to content,

                        "time" to "Today",

                        "userId" to userId,

                        "likeCount" to 0,

                        "isLiked" to false
                    )

                    FirebaseFirestore
                        .getInstance()
                        .collection("stories")
                        .document(firestoreId)
                        .set(noteData)
                }

            } else {

                val updatedNote = note.copy(

                    title = title,
                    desc = content
                )

                // UPDATE ROOM
                db.noteDao().updateNote(updatedNote)

                // UPDATE FIRESTORE
                if (
                    updatedNote.isShared
                    &&
                    isInternetAvailable(getApplication())
                ) {

                    val noteData = hashMapOf(

                        "title" to title,

                        "desc" to content,

                        "time" to updatedNote.time,

                        "userId" to updatedNote.userId,

                        "likeCount" to updatedNote.likeCount,

                        "isLiked" to updatedNote.isLiked
                    )

                    FirebaseFirestore
                        .getInstance()
                        .collection("stories")
                        .document(updatedNote.firestoreId)
                        .set(noteData)
                }
            }

            onSuccess()
        }
    }
}