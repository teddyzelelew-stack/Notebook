package com.example.ui

import com.example.ui.theme.MyApplicationTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Note
import com.example.util.EthiopianDateHelper
import com.example.util.GeminiHelper

sealed class Screen {
    object Dashboard : Screen()
    data class NoteEditor(val noteId: Int, val year: Int, val month: Int, val day: Int) : Screen()
    object SyncSettings : Screen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(viewModel: NoteViewModel) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    
    val language by viewModel.language.collectAsState()
    
    // Simple helper to localize strings based on active language setting
    fun getString(am: String, en: String): String {
        return if (language == "am") am else en
    }

    MyApplicationTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (val screen = currentScreen) {
                is Screen.Dashboard -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        getString = ::getString,
                        onNavigateToEditor = { id, y, m, d ->
                            currentScreen = Screen.NoteEditor(id, y, m, d)
                        },
                        onNavigateToSync = {
                            currentScreen = Screen.SyncSettings
                        }
                    )
                }
                is Screen.NoteEditor -> {
                    NoteEditorScreen(
                        viewModel = viewModel,
                        screen = screen,
                        getString = ::getString,
                        onNavigateBack = {
                            currentScreen = Screen.Dashboard
                        }
                    )
                }
                is Screen.SyncSettings -> {
                    SyncSettingsScreen(
                        viewModel = viewModel,
                        getString = ::getString,
                        onNavigateBack = {
                            currentScreen = Screen.Dashboard
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: NoteViewModel,
    getString: (String, String) -> String,
    onNavigateToEditor: (Int, Int, Int, Int) -> Unit,
    onNavigateToSync: () -> Unit
) {
    val selectedYear by viewModel.selectedYear.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredNotes by viewModel.filteredNotes.collectAsState()
    val notesInMonth by viewModel.notesForSelectedMonth.collectAsState()
    val language by viewModel.language.collectAsState()

    val currentEthDate = remember { EthiopianDateHelper.currentEthDate() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onNavigateToEditor(0, selectedYear, selectedMonth, selectedDay)
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_note_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = getString("አዲስ ማስታወሻ", "New Note"))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search & Title Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = getString("የዕለት ማስታወሻ", "EthioNote"),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Row {
                    // Language Switcher Button
                    IconButton(
                        onClick = { viewModel.toggleLanguage() },
                        modifier = Modifier.testTag("language_toggle_button")
                    ) {
                        Text(
                            text = if (language == "am") "EN" else "አማ",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 14.sp
                        )
                    }
                    // Sync Button
                    IconButton(
                        onClick = { onNavigateToSync() },
                        modifier = Modifier.testTag("sync_settings_button")
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = getString("ደመና ማመሳሰያ", "Cloud Sync"),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Hero banner displaying today's Ethiopian date
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Gray)
            ) {
                // Background Image
                Image(
                    painter = painterResource(id = R.drawable.img_hero_banner_1782560638993),
                    contentDescription = "Tibeb fabric banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Overlay Gradient for text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                            )
                        )
                )

                // Date Display inside Banner
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = getString("ዛሬ፡ ", "Today: ") + currentEthDate.formatFullAmharic(),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = getString("የዕለት ማስታወሻዎን እዚህ ይመዝግቡ", "Journal your daily life on the Ethiopian Calendar"),
                        color = Color.LightGray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Search Bar
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text(getString("ፈልግ... (ርዕስ፣ ይዘት ወይም ቀን)", "Search notes by title, body or date...")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_input"),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Month Browser (Browsing Calendar Months)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        val prevMonth = if (selectedMonth == 0) 12 else selectedMonth - 1
                        val prevYear = if (selectedMonth == 0) selectedYear - 1 else selectedYear
                        viewModel.selectYearAndMonth(prevYear, prevMonth)
                    },
                    modifier = Modifier.testTag("prev_month_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Month")
                }

                Text(
                    text = "${EthiopianDateHelper.MONTH_NAMES_AMHARIC[selectedMonth]} $selectedYear",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("month_title")
                )

                IconButton(
                    onClick = {
                        val nextMonth = if (selectedMonth == 12) 0 else selectedMonth + 1
                        val nextYear = if (selectedMonth == 12) selectedYear + 1 else selectedYear
                        viewModel.selectYearAndMonth(nextYear, nextMonth)
                    },
                    modifier = Modifier.testTag("next_month_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month")
                }
            }

            // Calendar Grid Calendar
            EthiopianMonthCalendarGrid(
                year = selectedYear,
                month = selectedMonth,
                selectedDay = selectedDay,
                currentEthDate = currentEthDate,
                notesInMonth = notesInMonth,
                onDaySelected = { viewModel.selectDay(it) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Section Header
            Text(
                text = getString("የዕለቱ ማስታወሻዎች", "Notes on Selected Day") + " (${EthiopianDateHelper.MONTH_NAMES_AMHARIC[selectedMonth]} $selectedDay)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Notes List Section
            val notesOnSelectedDay = filteredNotes.filter {
                it.ethiopianYear == selectedYear && 
                it.ethiopianMonth == selectedMonth && 
                it.ethiopianDay == selectedDay
            }

            if (notesOnSelectedDay.isEmpty() && searchQuery.isBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Edit, 
                            contentDescription = "Empty", 
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = getString("ለዚህ ቀን የተመዘገበ ማስታወሻ የለም።", "No notes recorded for this day."),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = getString("ለመጻፍ የታችኛውን + ምልክት ይጫኑ።", "Tap the + button to record a note!"),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                val displayList = if (searchQuery.isNotBlank()) filteredNotes else notesOnSelectedDay
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(displayList, key = { it.id }) { note ->
                        NoteCardItem(
                            note = note,
                            getString = getString,
                            onClick = {
                                onNavigateToEditor(note.id, note.ethiopianYear, note.ethiopianMonth, note.ethiopianDay)
                            },
                            onDelete = {
                                viewModel.deleteNote(note)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EthiopianMonthCalendarGrid(
    year: Int,
    month: Int,
    selectedDay: Int,
    currentEthDate: EthiopianDateHelper.EthDate,
    notesInMonth: List<Note>,
    onDaySelected: (Int) -> Unit
) {
    val totalDays = EthiopianDateHelper.getDaysInMonth(year, month)
    val startDayOfWeek = EthiopianDateHelper.getStartDayOfWeek(year, month) // 1 = Sun, 7 = Sat
    
    // We render grid rows. Weekday offset: startDayOfWeek - 1
    val offset = startDayOfWeek - 1
    val totalCells = totalDays + offset
    val rowsCount = (totalCells + 6) / 7

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        // Weekday Names Row
        Row(modifier = Modifier.fillMaxWidth()) {
            EthiopianDateHelper.WEEKDAY_SHORT_NAMES_AMHARIC.forEach { dayName ->
                Text(
                    text = dayName,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Grid of dates
        for (row in 0 until rowsCount) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                for (col in 0..6) {
                    val cellIndex = row * 7 + col
                    val dayNum = cellIndex - offset + 1

                    if (dayNum in 1..totalDays) {
                        val hasNote = notesInMonth.any { it.ethiopianDay == dayNum }
                        val isToday = currentEthDate.year == year && currentEthDate.month == month && currentEthDate.day == dayNum
                        val isSelected = selectedDay == dayNum

                        val cellBg by animateColorAsState(
                            targetValue = when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                isToday -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
                                else -> Color.Transparent
                            }
                        )

                        val textColor = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            isToday -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.onSurface
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1.1f)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(cellBg)
                                .border(
                                    width = if (isToday && !isSelected) 1.5.dp else 0.dp,
                                    color = if (isToday && !isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onDaySelected(dayNum) }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = dayNum.toString(),
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = textColor,
                                    fontSize = 14.sp
                                )
                                
                                // Golden / Crimson Dot if has note
                                if (hasNote) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                            )
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun NoteCardItem(
    note: Note,
    getString: (String, String) -> String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("note_item_card_${note.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                // Sync status indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (note.isSynced) {
                        Icon(
                            Icons.Default.CheckCircle, 
                            contentDescription = "Synced", 
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = getString("የተመሳሰለ", "Synced"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    } else {
                        Icon(
                            Icons.Default.Warning, 
                            contentDescription = "Unsynced", 
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = getString("ያልተመሳሰለ", "Local only"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.ethiopianDateString,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                IconButton(
                    onClick = { onDelete() },
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("delete_note_button_${note.id}")
                ) {
                    Icon(
                        Icons.Default.Delete, 
                        contentDescription = "Delete", 
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    viewModel: NoteViewModel,
    screen: Screen.NoteEditor,
    getString: (String, String) -> String,
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    val aiLoading by viewModel.aiLoading.collectAsState()
    
    val fullDateAmharic = remember { 
        EthiopianDateHelper.EthDate(screen.year, screen.month, screen.day).formatFullAmharic() 
    }

    LaunchedEffect(screen.noteId) {
        if (screen.noteId != 0) {
            val note = viewModel.allNotes.value.find { it.id == screen.noteId }
            if (note != null) {
                title = note.title
                content = note.content
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (screen.noteId == 0) getString("አዲስ ማስታወሻ", "Add Note") else getString("ማስታወሻ ማረም", "Edit Note")) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("editor_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            viewModel.addOrUpdateNote(
                                id = screen.noteId,
                                title = title,
                                content = content,
                                year = screen.year,
                                month = screen.month,
                                day = screen.day
                            )
                            onNavigateBack()
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("save_note_button")
                    ) {
                        Text(getString("አስቀምጥ", "Save"))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Calendar Badge Display
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.DateRange, 
                        contentDescription = "Calendar", 
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = fullDateAmharic,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Text Inputs
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text(getString("ርዕስ ያስገቡ...", "Note Title...")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_title_input"),
                textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text(getString("ዛሬ ምን ተፈጠረ? ማስታወሻዎን እዚህ ይጻፉ...", "What happened today? Write your thoughts here...")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("note_content_input"),
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )

            // Pulsing progress bar while Gemini AI is thinking
            AnimatedVisibility(visible = aiLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = getString("ጌሚኒ ረዳት በማሰብ ላይ ነው... (AI Assistant is writing...)", "Gemini Assistant is thinking..."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Gemini AI Smart Helpers Panel
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Star, 
                            contentDescription = "AI", 
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = getString("የጌሚኒ ስማርት ረዳት (Gemini AI)", "Gemini Smart AI Assistant"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Title generator button
                        Button(
                            onClick = {
                                viewModel.askAiToSuggestTitleAndSummary(content) { suggestedTitle, _ ->
                                    title = suggestedTitle
                                }
                            },
                            enabled = content.isNotBlank() && !aiLoading,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_suggest_title_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text(getString("ርዕስ ጠቁም", "Suggest Title"), fontSize = 11.sp)
                        }

                        // Writing refiner button
                        Button(
                            onClick = {
                                viewModel.askAiToRefineAmharic(content) { refined ->
                                    content = refined
                                }
                            },
                            enabled = content.isNotBlank() && !aiLoading,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_refine_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text(getString("አጻጻፍ አሻሽል", "Polish Amharic"), fontSize = 11.sp)
                        }

                        // Translation button
                        Button(
                            onClick = {
                                viewModel.askAiToTranslateToAmharic(content) { translated ->
                                    content = translated
                                }
                            },
                            enabled = content.isNotBlank() && !aiLoading,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_translate_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text(getString("ተርጉም", "Translate to Amharic"), fontSize = 11.sp)
                        }
                    }
                    
                    if (!GeminiHelper.isApiKeyAvailable()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = getString("የጌሚኒ ቁልፍ አልተዘጋጀም (AI functions are running on simulation fallback mode)", "API key missing: Running on simulation mock mode."),
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncSettingsScreen(
    viewModel: NoteViewModel,
    getString: (String, String) -> String,
    onNavigateBack: () -> Unit
) {
    val encryptionPassword by viewModel.encryptionPassword.collectAsState()
    val syncLog by viewModel.syncProgressLog.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()

    val unsyncedCount = allNotes.count { !it.isSynced }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(getString("ደመና ማመሳሰያ (Secure Cloud Sync)", "Secure Cloud Sync")) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("sync_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Security information Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Lock, 
                        contentDescription = "Secure Lock", 
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = getString("የደንበኛ-ወገን ምስጠራ (Client-Side AES Encryption)", "End-to-End Client AES Encryption"),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = getString(
                                "የእርስዎ ማስታወሻዎች ከስልክዎ ከመውጣታቸው በፊት በይለፍ ቃልዎ ሙሉ በሙሉ ይመሰረታሉ። በደመና ላይ ማንም ሊያነበው አይችልም።",
                                "Your notes are fully encrypted on your device using AES before they leave to the cloud. Total privacy is guaranteed."
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Encryption Password Configuration
            Text(
                text = getString("የማመስጠሪያ የይለፍ ቃል (AES Secret Key)", "Encryption Secret Password (AES Key)"),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            TextField(
                value = encryptionPassword,
                onValueChange = { viewModel.updateEncryptionPassword(it) },
                placeholder = { Text("Secret Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("encryption_password_input"),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Key") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sync Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = getString("አጠቃላይ ማስታወሻዎች፡ ${allNotes.size}", "Total Notes on device: ${allNotes.size}"),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = getString("ያልተመሳሰሉ ማስታወሻዎች፡ $unsyncedCount", "Unsynced Notes: $unsyncedCount"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (unsyncedCount > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary,
                        fontWeight = if (unsyncedCount > 0) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Button(
                    onClick = { viewModel.performCloudSync() },
                    enabled = !isSyncing,
                    modifier = Modifier.testTag("trigger_sync_button")
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(getString("አመሳስል (Sync Now)", "Sync Now"))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sync logs title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getString("የማመሳሰያ ሂደት መዝገብ (Sync Log Terminal)", "Sync Log Terminal"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                TextButton(onClick = { viewModel.clearLogs() }) {
                    Text(getString("አጽዳ", "Clear"), style = MaterialTheme.typography.bodySmall)
                }
            }

            // Monospace Retro Log Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0D0D11))
                    .border(1.dp, Color(0xFF333344), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                if (syncLog.isEmpty()) {
                    Text(
                        text = getString("> ዝግጁ። ማመሳሰል ለመጀመር 'አመሳስል' የሚለውን ይጫኑ።", "> Idle. Click 'Sync Now' to initiate secure connection."),
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF00FF00),
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.TopStart)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(syncLog) { logLine ->
                            Text(
                                text = "> $logLine",
                                fontFamily = FontFamily.Monospace,
                                color = if (logLine.contains("ተሳካ") || logLine.contains("completed") || logLine.contains("ሁሉም")) Color(0xFF00FF00) else if (logLine.contains("ስህተት") || logLine.contains("error")) Color.Red else Color(0xFFE0E0FF),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
