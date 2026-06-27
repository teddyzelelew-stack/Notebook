package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val ethiopianYear: Int,
    val ethiopianMonth: Int, // 0-indexed (0 to 12)
    val ethiopianDay: Int,
    val ethiopianDateString: String, // formatted representation for quick query/display
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val lastSynced: Long = 0,
    val cloudId: String = ""
)
