package com.example.virelia.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.virelia.Database.DatabaseProvider
import com.example.virelia.Database.NoteEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application)
    : AndroidViewModel(application) {

    private val db =
        DatabaseProvider.getDatabase(application)

    // AMBIL SEMUA NOTE
    val notes =
        db.noteDao()
            .getAllNotes()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // DELETE NOTE
    fun deleteNote(note: NoteEntity) {

        viewModelScope.launch {

            db.noteDao().deleteNote(note)

            if (note.firestoreId.isNotEmpty()) {

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
            "time" to note.time
        )

        FirebaseFirestore.getInstance()
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

            .addOnFailureListener {

                onFailed()
            }
    }
}