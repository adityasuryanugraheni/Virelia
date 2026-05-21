package com.example.virelia.Database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Insert
    suspend fun insertNote(note: NoteEntity)

    @Query("SELECT * FROM notes ORDER BY id DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Int): NoteEntity?

    @Query("SELECT * FROM notes WHERE userId = :userId")
    fun getNotesByUser(userId: String): Flow<List<NoteEntity>>

    // HITUNG SEMUA NOTES USER
    @Query("SELECT COUNT(*) FROM notes WHERE userId = :userId")
    suspend fun countNotesByUser(userId: String): Int

    // HITUNG NOTES PUBLIC USER
    @Query("SELECT COUNT(*) FROM notes WHERE userId = :userId AND isShared = 1")
    suspend fun countPublicNotesByUser(userId: String): Int

    // TAMBAHAN
    @Query("DELETE FROM notes WHERE userId = :userId")
    suspend fun deleteNotesByUser(userId: String)

    @Query("SELECT * FROM notes WHERE firestoreId = :id LIMIT 1")
    suspend fun getNoteByFirestoreId(
        id: String
    ): NoteEntity?
}