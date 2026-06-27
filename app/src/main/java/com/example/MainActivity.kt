package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.ui.AppNavigation
import com.example.ui.NoteViewModel
import com.example.ui.NoteViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Instantiate ViewModel
        val factory = NoteViewModelFactory(application)
        val viewModel = ViewModelProvider(this, factory)[NoteViewModel::class.java]
        
        setContent {
            AppNavigation(viewModel = viewModel)
        }
    }
}
