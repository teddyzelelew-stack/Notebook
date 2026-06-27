package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE ethiopianYear = :year AND ethiopianMonth = :month ORDER BY ethiopianDay ASC")
    fun getNotesForMonth(year: Int, month: Int): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE ethiopianYear = :year AND ethiopianMonth = :month AND ethiopianDay = :day LIMIT 1")
    fun getNoteForDateDirect(year: Int, month: Int, day: Int): Note?

    @Query("SELECT * FROM notes WHERE ethiopianYear = :year AND ethiopianMonth = :month AND ethiopianDay = :day")
    fun getNotesForDate(year: Int, month: Int, day: Int): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Int): Note?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()

    @Query("SELECT * FROM notes WHERE isSynced = 0")
    suspend fun getUnsyncedNotes(): List<Note>

    @Query("UPDATE notes SET isSynced = :isSynced, lastSynced = :lastSynced, cloudId = :cloudId WHERE id = :id")
    suspend fun updateSyncStatus(id: Int, isSynced: Boolean, lastSynced: Long, cloudId: String)
}
