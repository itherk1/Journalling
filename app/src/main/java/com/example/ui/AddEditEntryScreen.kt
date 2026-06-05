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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.rounded.Place
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
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.util.Locale
import java.io.File
import java.io.FileOutputStream
import android.content.Context
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import android.location.Geocoder
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Map
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.platform.LocalView
import android.view.HapticFeedbackConstants

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
    onNavigateBack: () -> Unit,
    onNavigateToEntry: (Int) -> Unit = {}
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf("☁️ NEUTRAL") }
    var photoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var location by remember { mutableStateOf("") }
    var timestamp by remember { mutableStateOf(System.currentTimeMillis()) }
    var backColor by remember { mutableStateOf<String?>(null) }
    var fontFam by remember { mutableStateOf<String?>(null) }
    var linkedEntries by remember { mutableStateOf<List<Int>>(emptyList()) }
    var showLinkDialog by remember { mutableStateOf(false) }

    LaunchedEffect(entryId) {
        if (entryId != null) {
            val entry = viewModel.entries.value.find { it.id == entryId }
            if (entry != null) {
                title = entry.title
                content = entry.content
                selectedMood = entry.mood
                val uris = entry.photoUris.map { Uri.parse(it) }.toMutableList()
                if (entry.photoUri != null && !entry.photoUris.contains(entry.photoUri)) {
                    uris.add(0, Uri.parse(entry.photoUri))
                }
                photoUris = uris
                location = entry.location ?: ""
                timestamp = entry.timestamp
                backColor = entry.backgroundColor
                fontFam = entry.fontFamily
                linkedEntries = entry.linkedEntryIds
            }
        } else if (initialPrompt.isNotBlank()) {
            title = initialPrompt
        }
    }

    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        try {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                            if (!addresses.isNullOrEmpty()) {
                                location = addresses[0].getAddressLine(0)
                            } else {
                                location = "${loc.latitude}, ${loc.longitude}"
                            }
                        } catch (e: Exception) {
                            location = "${loc.latitude}, ${loc.longitude}"
                        }
                    }
                }
            } catch (e: SecurityException) {
                // Ignore
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        val localUris = uris.mapNotNull { copyUriToInternalStorage(context, it) }
        photoUris = photoUris + localUris
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

    val currentBgColor = when (backColor) {
        "primaryContainer" -> MaterialTheme.colorScheme.primaryContainer
        "secondaryContainer" -> MaterialTheme.colorScheme.secondaryContainer
        "tertiaryContainer" -> MaterialTheme.colorScheme.tertiaryContainer
        "errorContainer" -> MaterialTheme.colorScheme.errorContainer
        "surfaceVariant" -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }

    Scaffold(
        containerColor = currentBgColor,
        topBar = {
            TopAppBar(
                title = { Text(if (entryId == null) "New Entry" else "Edit Entry") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showLinkDialog = true }) {
                        Icon(Icons.Rounded.Link, contentDescription = "Link Notes")
                    }
                     IconButton(onClick = { 
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            try {
                                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                                    if (loc != null) {
                                        try {
                                            val geocoder = Geocoder(context, Locale.getDefault())
                                            val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                                            if (!addresses.isNullOrEmpty()) {
                                                location = addresses[0].getAddressLine(0)
                                            } else {
                                                location = "${loc.latitude}, ${loc.longitude}"
                                            }
                                        } catch (e: Exception) {
                                            location = "${loc.latitude}, ${loc.longitude}"
                                        }
                                    }
                                }
                            } catch (e: SecurityException) {}
                        } else {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                        }
                     }) {
                        Icon(Icons.Rounded.Map, contentDescription = "Get GPS Location")
                    }
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
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                val isPrompt = initialPrompt.isNotBlank() && title == initialPrompt
                if (entryId == null) {
                    viewModel.addEntry(
                        title = title,
                        content = content,
                        mood = selectedMood,
                        photoUris = photoUris.map { it.toString() },
                        location = location.takeIf { it.isNotBlank() },
                        isPrompt = isPrompt,
                        backgroundColor = backColor,
                        fontFamily = fontFam,
                        linkedEntryIds = linkedEntries
                    )
                } else {
                    viewModel.updateEntry(
                        id = entryId,
                        title = title,
                        content = content,
                        timestamp = timestamp,
                        mood = selectedMood,
                        photoUris = photoUris.map { it.toString() },
                        location = location.takeIf { it.isNotBlank() },
                        backgroundColor = backColor,
                        fontFamily = fontFam,
                        linkedEntryIds = linkedEntries
                    )
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

            val semanticColors = listOf(
                null, 
                "primaryContainer", 
                "secondaryContainer", 
                "tertiaryContainer", 
                "errorContainer",
                "surfaceVariant"
            )
            LazyRow(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                items(semanticColors) { col ->
                    val colorValue = when (col) {
                        "primaryContainer" -> MaterialTheme.colorScheme.primaryContainer
                        "secondaryContainer" -> MaterialTheme.colorScheme.secondaryContainer
                        "tertiaryContainer" -> MaterialTheme.colorScheme.tertiaryContainer
                        "errorContainer" -> MaterialTheme.colorScheme.errorContainer
                        "surfaceVariant" -> MaterialTheme.colorScheme.surfaceVariant
                        else -> MaterialTheme.colorScheme.surface
                    }
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .height(32.dp)
                            .width(32.dp)
                            .background(colorValue, androidx.compose.foundation.shape.CircleShape)
                            .clickable { backColor = col }
                            .border(
                                width = if (backColor == col) 2.dp else 1.dp,
                                color = if (backColor == col) MaterialTheme.colorScheme.primary else Color.Gray,
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    )
                }
            }

            val fontFamilies = listOf(null, "Serif", "Monospace", "Cursive")
            LazyRow(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                items(fontFamilies) { font ->
                    androidx.compose.material3.FilterChip(
                        selected = fontFam == font,
                        onClick = { fontFam = font },
                        label = { Text(font ?: "Default", fontFamily = when(font) {
                            "Serif" -> androidx.compose.ui.text.font.FontFamily.Serif
                            "Monospace" -> androidx.compose.ui.text.font.FontFamily.Monospace
                            "Cursive" -> androidx.compose.ui.text.font.FontFamily.Cursive
                            else -> androidx.compose.ui.text.font.FontFamily.Default
                        }) }
                    )
                }
            }

            if (linkedEntries.isNotEmpty()) {
                LazyRow(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                    items(linkedEntries) { linkedId ->
                        val linkedEntry = viewModel.entries.value.find { it.id == linkedId }
                        if (linkedEntry != null) {
                            androidx.compose.material3.InputChip(
                                selected = true,
                                onClick = { onNavigateToEntry(linkedId) },
                                label = { Text(linkedEntry.title.ifEmpty { "Untitled" }) },
                                leadingIcon = { Icon(Icons.Rounded.Link, contentDescription = "Link") }
                            )
                        }
                    }
                }
            }

            LazyRow(
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(
                    items = listOf(
                        "☁️ NEUTRAL", "☀️ HAPPY", "🌧️ SAD", 
                        "🙏 GRATEFUL", "🌟 HOPEFUL", "🤩 EXCITED", 
                        "💖 GENEROUS", "😌 CALM", "😤 ANGRY",
                        "🤝 EMPATHY", "😰 STRESSED", "🧩 PUZZLED",
                        "🤔 CONFUSED", "😇 FAITHFUL", "❤️ LOVED", "😭 CRYING"
                    )
                ) { mood ->
                    FilterChip(
                        selected = selectedMood == mood,
                        onClick = { selectedMood = mood },
                        label = { Text(mood) }
                    )
                }
            }

            if (photoUris.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    items(photoUris) { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = "Attached photo",
                            modifier = Modifier
                                .height(200.dp)
                                .fillMaxWidth(0.8f),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            if (location.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val uri = android.net.Uri.parse("geo:0,0?q=${android.net.Uri.encode(location)}")
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Place, contentDescription = "Location", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(location, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                }
            }

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text("Start writing your thoughts...", style = MaterialTheme.typography.bodyLarge) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = when (fontFam) {
                        "Serif" -> androidx.compose.ui.text.font.FontFamily.Serif
                        "Monospace" -> androidx.compose.ui.text.font.FontFamily.Monospace
                        "Cursive" -> androidx.compose.ui.text.font.FontFamily.Cursive
                        else -> androidx.compose.ui.text.font.FontFamily.Default
                    },
                    fontSize = 16.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
            
            if (showLinkDialog) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showLinkDialog = false },
                    title = { Text("Link Entries") },
                    text = {
                        androidx.compose.foundation.lazy.LazyColumn {
                            val allEntries = viewModel.entries.value.filter { it.id != entryId }
                            items(allEntries) { entry ->
                                Row(
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (linkedEntries.contains(entry.id)) {
                                                linkedEntries = linkedEntries - entry.id
                                            } else {
                                                linkedEntries = linkedEntries + entry.id
                                            }
                                        }
                                        .padding(8.dp)
                                ) {
                                    androidx.compose.material3.Checkbox(
                                        checked = linkedEntries.contains(entry.id),
                                        onCheckedChange = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(entry.title.ifEmpty { "Untitled" })
                                }
                            }
                        }
                    },
                    confirmButton = {
                        androidx.compose.material3.TextButton(onClick = { showLinkDialog = false }) {
                            Text("Done")
                        }
                    }
                )
            }
        }
    }
}
