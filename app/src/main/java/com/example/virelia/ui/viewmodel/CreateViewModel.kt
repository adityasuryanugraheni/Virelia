package com.example.virelia.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.virelia.Database.DatabaseProvider
import com.example.virelia.Database.NoteEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateViewModel(application: Application)
    : AndroidViewModel(application) {

    private val db =
        DatabaseProvider.getDatabase(application)

    private val firestore =
        FirebaseFirestore.getInstance()

    private val _note =
        MutableStateFlow<NoteEntity?>(null)

    val note: StateFlow<NoteEntity?> = _note

    // CEGAH DOUBLE SAVE
    var isSaving by mutableStateOf(false)
        private set

    // =========================
    // AMBIL NOTE
    // =========================
    fun getNote(noteId: Int) {

        viewModelScope.launch {

            val data =
                db.noteDao()
                    .getNoteById(noteId)

            _note.value = data
        }
    }

    // =========================
    // SAVE NOTE
    // =========================
    fun saveNote(
        note: NoteEntity?,
        title: String,
        content: String,
        imageUrl: String,
        localImageUri: String,
        onSuccess: () -> Unit
    ) {

        // CEGAH DOUBLE CLICK
        if (isSaving) return

        isSaving = true

        viewModelScope.launch {

            val userId =
                FirebaseAuth
                    .getInstance()
                    .currentUser
                    ?.uid ?: ""

            // =========================
            // CREATE NOTE
            // =========================
            if (note == null) {

                val newNote = NoteEntity(
                    title = title,
                    desc = content,
                    imageUrl = imageUrl,
                    localImageUri = localImageUri,
                    time = SimpleDateFormat(
                        "dd MMM yyyy, HH:mm",
                        Locale.getDefault()
                    ).format(Date()),
                    userId = userId,
                    firestoreId = "",
                    isShared = false
                )

                // ROOM ONLY
                db.noteDao()
                    .insertNote(newNote)

            } else {

                // =========================
                // UPDATE NOTE
                // =========================
                val updatedNote = note.copy(
                    title = title,
                    desc = content,
                    imageUrl = imageUrl,
                    localImageUri = localImageUri
                )

                // UPDATE ROOM
                db.noteDao()
                    .updateNote(updatedNote)

                // UPDATE FIRESTORE
                // HANYA JIKA SUDAH DISHARE
                if (
                    updatedNote.isShared &&
                    updatedNote.firestoreId.isNotEmpty()
                ) {

                    val noteData = hashMapOf(
                        "title" to title,
                        "desc" to content,
                        "imageUrl" to imageUrl, // ✅ TAMBAH INI
                        "time" to updatedNote.time,
                        "userId" to updatedNote.userId,
                        "likeCount" to updatedNote.likeCount,
                        "commentCount" to updatedNote.commentCount
                    )

                    firestore
                        .collection("stories")
                        .document(updatedNote.firestoreId)
                        .set(noteData)
                }
            }

            isSaving = false

            onSuccess()
        }
    }
}