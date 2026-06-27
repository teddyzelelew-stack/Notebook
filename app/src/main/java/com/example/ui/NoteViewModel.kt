package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Note
import com.example.data.NoteRepository
import com.example.util.EthiopianDateHelper
import com.example.util.GeminiHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NoteRepository

    // Current calendar browsing state (Year, Month)
    private val _selectedYear = MutableStateFlow(EthiopianDateHelper.currentEthDate().year)
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedMonth = MutableStateFlow(EthiopianDateHelper.currentEthDate().month)
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    // Current selected day for detailed view or adding a note
    private val _selectedDay = MutableStateFlow(EthiopianDateHelper.currentEthDate().day)
    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()

    // Active search term
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Sync parameters
    private val _encryptionPassword = MutableStateFlow("EthioSecurePass123")
    val encryptionPassword: StateFlow<String> = _encryptionPassword.asStateFlow()

    private val _syncProgressLog = MutableStateFlow<List<String>>(emptyList())
    val syncProgressLog: StateFlow<List<String>> = _syncProgressLog.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // AI Assist loading state
    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    private val _aiResponse = MutableStateFlow("")
    val aiResponse: StateFlow<String> = _aiResponse.asStateFlow()

    // Language setting: "am" for Amharic, "en" for English
    private val _language = MutableStateFlow("am")
    val language: StateFlow<String> = _language.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = NoteRepository(database.noteDao())
    }

    // List of all notes
    val allNotes: StateFlow<List<Note>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // List of notes filtered by selected year and month
    val notesForSelectedMonth: StateFlow<List<Note>> = combine(
        _selectedYear, _selectedMonth
    ) { year, month ->
        Pair(year, month)
    }.flatMapLatest { (year, month) ->
        repository.getNotesForMonth(year, month)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // List of notes matching search query or selected day
    val filteredNotes: StateFlow<List<Note>> = combine(
        allNotes, _searchQuery
    ) { notes, query ->
        if (query.isBlank()) {
            notes
        } else {
            notes.filter {
                it.title.contains(query, ignoreCase = true) || 
                it.content.contains(query, ignoreCase = true) ||
                it.ethiopianDateString.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectYearAndMonth(year: Int, month: Int) {
        _selectedYear.value = year
        _selectedMonth.value = month
    }

    fun selectDay(day: Int) {
        _selectedDay.value = day
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateEncryptionPassword(password: String) {
        if (password.isNotBlank()) {
            _encryptionPassword.value = password
        }
    }

    fun toggleLanguage() {
        _language.value = if (_language.value == "am") "en" else "am"
    }

    // CRUD note operations
    fun addOrUpdateNote(id: Int = 0, title: String, content: String, year: Int, month: Int, day: Int) {
        viewModelScope.launch {
            val formattedDate = EthiopianDateHelper.EthDate(year, month, day).formatFullAmharic()
            val note = Note(
                id = if (id == 0) 0 else id,
                title = title.ifBlank { "ያለ ርዕስ ማስታወሻ" }, // "Untitled Note" in Amharic
                content = content,
                ethiopianYear = year,
                ethiopianMonth = month,
                ethiopianDay = day,
                ethiopianDateString = formattedDate,
                isSynced = false // Mark as unsynced on modifications
            )
            repository.insertNote(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun clearLogs() {
        _syncProgressLog.value = emptyList()
    }

    /**
     * Trigger secure AES database synchronization to cloud
     */
    fun performCloudSync() {
        if (_isSyncing.value) return
        _isSyncing.value = true
        _syncProgressLog.value = emptyList()

        viewModelScope.launch {
            repository.syncNotesWithCloud(_encryptionPassword.value) { log ->
                _syncProgressLog.value = _syncProgressLog.value + log
            }
            _isSyncing.value = false
        }
    }

    /**
     * AI Assistant Functions
     */
    fun askAiToSuggestTitleAndSummary(content: String, onResult: (title: String, summary: String) -> Unit) {
        if (content.isBlank() || _aiLoading.value) return
        _aiLoading.value = true
        viewModelScope.launch {
            val result = GeminiHelper.suggestTitleAndSummary(content)
            onResult(result.first, result.second)
            _aiLoading.value = false
        }
    }

    fun askAiToRefineAmharic(content: String, onResult: (refined: String) -> Unit) {
        if (content.isBlank() || _aiLoading.value) return
        _aiLoading.value = true
        viewModelScope.launch {
            val refined = GeminiHelper.improveAmharicWriting(content)
            onResult(refined)
            _aiLoading.value = false
        }
    }

    fun askAiToTranslateToAmharic(content: String, onResult: (translation: String) -> Unit) {
        if (content.isBlank() || _aiLoading.value) return
        _aiLoading.value = true
        viewModelScope.launch {
            val translation = GeminiHelper.translateToAmharic(content)
            onResult(translation)
            _aiLoading.value = false
        }
    }
}

class NoteViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
