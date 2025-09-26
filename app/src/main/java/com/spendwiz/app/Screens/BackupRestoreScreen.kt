package com.spendwiz.app.Screens

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spendwiz.app.BackUp.BackupRestoreState
import com.spendwiz.app.BackUp.BackupRestoreViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    viewModel: BackupRestoreViewModel = viewModel(factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory(LocalContext.current.applicationContext as android.app.Application))
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Build suggested filename spendwiz_backup_YYYYMMDD.json
    val suggestedFileName = remember {
        val dateStr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        } else {
            SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(Date())
        }
        "spendwiz_backup_$dateStr.json"
    }

    // CreateDocument launcher for backup
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri: Uri? ->
            uri?.let { viewModel.backupData(it) }
        }
    )

    // State for restore confirmation dialog
    var showRestoreDialog by remember { mutableStateOf(false) }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }

    // OpenDocument launcher for restore
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                pendingRestoreUri = it
                showRestoreDialog = true
            }
        }
    )

    // Show Snackbar on state changes
    LaunchedEffect(state) {
        when (val currentState = state) {
            is BackupRestoreState.Success -> snackbarHostState.showSnackbar(currentState.message)
            is BackupRestoreState.Error -> snackbarHostState.showSnackbar(currentState.error)
            else -> Unit // Do nothing for Idle or InProgress
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Backup & Restore") })
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { createDocumentLauncher.launch(suggestedFileName) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state !is BackupRestoreState.InProgress
            ) {
                Text("Backup Data")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { openDocumentLauncher.launch(arrayOf("application/json")) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state !is BackupRestoreState.InProgress
            ) {
                Text("Restore Data")
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (state is BackupRestoreState.InProgress) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Processing… Please wait.")
                }
            }
        }

        // Restore confirmation dialog
        if (showRestoreDialog) {
            AlertDialog(
                onDismissRequest = { showRestoreDialog = false },
                title = { Text("Confirm Restore") },
                text = {
                    Text("This will delete all current data and replace it with the data from the backup file. This action cannot be undone. Are you sure?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            pendingRestoreUri?.let { viewModel.restoreData(it) }
                            showRestoreDialog = false
                            pendingRestoreUri = null
                        }
                    ) {
                        Text("Yes, Restore")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRestoreDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
