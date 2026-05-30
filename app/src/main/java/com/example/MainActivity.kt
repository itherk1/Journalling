package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.JournalRepository
import com.example.data.UserPreferences
import com.example.ui.JournalApp
import com.example.ui.JournalViewModel
import com.example.ui.JournalViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : FragmentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val database = AppDatabase.getDatabase(this)
    val prefs = UserPreferences(this)
    val repository = JournalRepository(database.journalDao())
    val factory = JournalViewModelFactory(repository, prefs)
    val viewModel = ViewModelProvider(this, factory)[JournalViewModel::class.java]

    val openNewEntry = intent.getBooleanExtra("OPEN_NEW_ENTRY", false)

    setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          // Passing modifier implies avoiding edge-to-edge clash, but we'll 
          // let the internal screens handle padding. 
          // Note: our Navigation compose wraps with Scaffold. 
          // So we don't necessarily need a Scaffold here.
          JournalApp(activity = this@MainActivity, viewModel = viewModel, openNewEntry = openNewEntry)
        }
      }
    }
  }
}
