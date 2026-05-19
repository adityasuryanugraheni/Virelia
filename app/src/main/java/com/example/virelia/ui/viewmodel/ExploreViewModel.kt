package com.example.virelia.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.virelia.Database.NoteEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExploreViewModel(application: Application)
    : AndroidViewModel(application) {

    private val _publicStories =
        MutableStateFlow<List<NoteEntity>>(emptyList())

    val publicStories:
            StateFlow<List<NoteEntity>>
            = _publicStories

    init {

        loadPublicStories()
    }

    fun loadPublicStories() {

        FirebaseFirestore.getInstance()
            .collection("stories")
            .get()

            .addOnSuccessListener { result ->

                viewModelScope.launch {

                    val stories =
                        mutableListOf<NoteEntity>()

                    for (document in result) {

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
                                document.id,

                            isShared = true
                        )

                        stories.add(note)
                    }

                    _publicStories.value = stories
                }
            }
    }
}