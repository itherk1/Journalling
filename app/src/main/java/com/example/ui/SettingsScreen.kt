package com.example.ui

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.workers.NotificationWorker
import com.example.ui.JournalViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SettingsScreen(viewModel: JournalViewModel) {
    val context = LocalContext.current
    val name by viewModel.name.collectAsState()
    val age by viewModel.age.collectAsState()
    val gender by viewModel.gender.collectAsState()
    val focusArea by viewModel.focusArea.collectAsState()
    val userGoals by viewModel.userGoals.collectAsState()
    val appLockEnabled by viewModel.appLockEnabled.collectAsState()

    var editName by remember { mutableStateOf(name) }
    var editAge by remember { mutableStateOf(age) }
    var editGender by remember { mutableStateOf(gender) }
    var editFocusArea by remember { mutableStateOf(focusArea) }
    var editGoals by remember { mutableStateOf(userGoals) }
    var showSave by remember { mutableStateOf(false) }

    // Request permission if Android 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionState = rememberPermissionState(android.Manifest.permission.POST_NOTIFICATIONS)
        LaunchedEffect(Unit) {
            if (!permissionState.status.isGranted) {
                permissionState.launchPermissionRequest()
            }
        }
    }

    LaunchedEffect(Unit) {
        NotificationWorker.scheduleWork(context)
    }

    LaunchedEffect(editName, editAge, editGender, editFocusArea, editGoals) {
        showSave = editName != name || editAge != age || editGender != gender || editFocusArea != focusArea || editGoals != userGoals
    }

    // Scrollable Column to make room for new fields
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Profile", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editAge,
                        onValueChange = { editAge = it },
                        label = { Text("Age") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = editGender,
                        onValueChange = { editGender = it },
                        label = { Text("Gender") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = editFocusArea,
                    onValueChange = { editFocusArea = it },
                    label = { Text("Focus Area") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editGoals,
                    onValueChange = { editGoals = it },
                    label = { Text("User Goals") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (showSave) {
                    Button(
                        onClick = {
                            viewModel.updateProfile(editName, editAge, editGender, editFocusArea, editGoals)
                            showSave = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Profile")
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Security", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text("Enable Biometric App Lock", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = appLockEnabled,
                        onCheckedChange = { viewModel.setAppLockEnabled(it) }
                    )
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Notifications Active", style = MaterialTheme.typography.titleMedium)
                Text(
                    "You will receive mood check-ins (morning, afternoon, evening, night) and periodic motivation quotes automatically.", 
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Notifications are handled automatically by the system in the background.", 
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
