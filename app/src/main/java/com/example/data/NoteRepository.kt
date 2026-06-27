package com.example.data

import com.example.util.CryptographyHelper
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class NoteRepository(private val noteDao: NoteDao) {

    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()

    fun getNotesForMonth(year: Int, month: Int): Flow<List<Note>> {
        return noteDao.getNotesForMonth(year, month)
    }

    fun getNotesForDate(year: Int, month: Int, day: Int): Flow<List<Note>> {
        return noteDao.getNotesForDate(year, month, day)
    }

    suspend fun getNoteById(id: Int): Note? {
        return noteDao.getNoteById(id)
    }

    suspend fun insertNote(note: Note): Long {
        return noteDao.insertNote(note)
    }

    suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note)
    }

    suspend fun deleteAllNotes() {
        noteDao.deleteAllNotes()
    }

    /**
     * Perform secure synchronization to simulated cloud storage.
     * Generates AES ciphers of notes, showing actual secure upload details.
     */
    suspend fun syncNotesWithCloud(encryptionPassword: String, onProgress: (String) -> Unit): Boolean {
        try {
            onProgress("ማመሳሰል በመጀመር ላይ... (Initializing sync...)")
            val unsynced = noteDao.getUnsyncedNotes()
            if (unsynced.isEmpty()) {
                onProgress("ሁሉም ማስታወሻዎች ቀድሞውኑ ተመሳስለዋል። (All notes are up to date!)")
                return true
            }

            onProgress("የማመሳሰል ዝግጅት፡ ${unsynced.size} ያልተመሳሰሉ ማስታወሻዎች ተገኝተዋል። (Preparing ${unsynced.size} notes...)")
            
            for ((index, note) in unsynced.withIndex()) {
                val progressPercent = ((index + 1) * 100) / unsynced.size
                onProgress("ማስፈንጠር እና መመስጠር [${note.title}]... (Encrypting...)")
                
                // Client-side AES encryption
                val encryptedTitle = CryptographyHelper.encrypt(note.title, encryptionPassword)
                val encryptedContent = CryptographyHelper.encrypt(note.content, encryptionPassword)
                
                onProgress("ክላውድ ሰቀላ በመገናኘት ላይ... (Uploading payload to secure cloud...)")
                onProgress("የምስጠራ ኮድ (Cipher): ${encryptedContent.take(15)}...")

                // Simulate secure cloud API latency
                kotlinx.coroutines.delay(600)

                val cloudId = UUID.randomUUID().toString()
                noteDao.updateSyncStatus(
                    id = note.id,
                    isSynced = true,
                    lastSynced = System.currentTimeMillis(),
                    cloudId = cloudId
                )
                onProgress("የተሳካ ሰቀላ ($progressPercent%): ${note.title}")
            }
            onProgress("የማመሳሰል ሂደት በተሳካ ሁኔታ ተጠናቋል! (Cloud synchronization successfully completed!)")
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            onProgress("የማመሳሰል ስህተት ተከስቷል: ${e.message}")
            return false
        }
    }
}
