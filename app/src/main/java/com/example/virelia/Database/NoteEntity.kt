package com.example.virelia.Database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,

    val desc: String,

    val time: String,

    val isShared: Boolean = false,

    val userId: String = "",

    @get:JvmName("getUsernameValue")  // TAMBAH INI
    val username: String = "",

    val firestoreId: String = "",

    var isLiked: Boolean = false,

    var likeCount: Int = 0
)