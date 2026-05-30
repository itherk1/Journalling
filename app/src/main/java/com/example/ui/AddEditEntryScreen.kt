package com.example.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.util.Locale
import java.io.File
import java.io.FileOutputStream
import android.content.Context
import androidx.compose.ui.platform.LocalContext

fun copyUriToInternalStorage(context: Context, uri: Uri): Uri? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val fileName = "journal_img_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        Uri.fromFile(file)
    } catch (e: Exception) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEntryScreen(
    viewModel: JournalViewModel,
    entryId: Int? = null,
    initialPrompt: String = "",
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf("NEUTRAL") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var timestamp by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(entryId) {
        if (entryId != null) {
            val entry = viewModel.entries.value.find { it.id == entryId }
            if (entry != null) {
                title = entry.title
                content = entry.content
                selectedMood = entry.mood
                photoUri = entry.photoUri?.let { Uri.parse(it) }
                timestamp = entry.timestamp
            }
        } else if (initialPrompt.isNotBlank()) {
            title = initialPrompt
        }
    }

    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { 
            val localUri = copyUriToInternalStorage(context, it)
            if (localUri != null) photoUri = localUri
        } 
    }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = data?.get(0) ?: ""
            if (spokenText.isNotEmpty()) {
                content = if (content.isEmpty()) spokenText else "$content $spokenText"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (entryId == null) "New Entry" else "Edit Entry") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { photoPickerLauncher.launch("image/*") }) {
                        Icon(Icons.Rounded.Image, contentDescription = "Add Photo")
                    }
                    IconButton(onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                        }
                        try {
                            speechRecognizerLauncher.launch(intent)
                        } catch (e: Exception) {
                            // Ignored if device doesn't support speech recognition
                        }
                    }) {
                        Icon(Icons.Rounded.Mic, contentDescription = "Voice to Text")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (entryId == null) {
                    viewModel.addEntry(title, content, selectedMood, photoUri?.toString())
                    if (initialPrompt.isNotBlank() && title == initialPrompt) {
                        viewModel.refreshPrompt()
                    }
                } else {
                    viewModel.updateEntry(entryId, title, content, timestamp, selectedMood, photoUri?.toString())
                }
                onNavigateBack()
            }) {
                Icon(Icons.Rounded.Check, contentDescription = "Save")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Title", style = MaterialTheme.typography.headlineMedium) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.headlineMedium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            LazyRow(
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(
                    items = listOf(
                        "☁️ NEUTRAL", "☀️ HAPPY", "🌧️ SAD", 
                        "🙏 GRATEFUL", "🌟 HOPEFUL", "🤩 EXCITED", 
                        "💖 GENEROUS", "😌 CALM", "😤 ANGRY"
                    )
                ) { mood ->
                    FilterChip(
                        selected = selectedMood == mood,
                        onClick = { selectedMood = mood },
                        label = { Text(mood) }
                    )
                }
            }

            if (photoUri != null) {
                Box(modifier = Modifier.padding(vertical = 12.dp)) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "Attached photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text("Start writing your thoughts...", style = MaterialTheme.typography.bodyLarge) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
        }
    }
}
