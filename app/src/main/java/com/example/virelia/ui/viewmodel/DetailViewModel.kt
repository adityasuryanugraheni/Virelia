package com.example.virelia.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class DetailViewModel : ViewModel() {

    var comment by mutableStateOf("")

    fun postComment() {

        if (comment.isNotEmpty()) {

            // nanti bisa simpan ke firebase

            comment = ""
        }
    }
}