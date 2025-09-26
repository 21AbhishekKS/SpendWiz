package com.spendwiz.app.Screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.spendwiz.app.BackUp.BackupRestoreState
import com.spendwiz.app.BackUp.BackupRestoreViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    viewModel: BackupRestoreViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory(
            LocalContext.current.applicationContext as android.app.Application
        )
    )
) {
    val state by viewModel.state.collectAsState()
    val googleUser by viewModel.googleUser.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showRestoreDialog by remember { mutableStateOf<(() -> Unit)?>(null) }

    // File name for local backup
    val suggestedFileName = remember {
        val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        "spendwiz_backup_$dateStr.json"
    }

    // Launchers
    val authResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleSignInResult(result.data)
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri: Uri? -> uri?.let { viewModel.backupData(it) } }
    )

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? -> showRestoreDialog = { viewModel.restoreData(uri!!) } }
    )

    // Snackbar handling
    LaunchedEffect(state) {
        when (val currentState = state) {
            is BackupRestoreState.Success -> snackbarHostState.showSnackbar(currentState.message)
            is BackupRestoreState.Error -> snackbarHostState.showSnackbar(currentState.error)
            else -> Unit
        }
        if (state !is BackupRestoreState.InProgress) viewModel.resetState()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ✅ Heading section (instead of TopBar)
            Text(
                text = "Backup & Restore",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )

            Spacer(Modifier.height(8.dp))

            // --- Google Drive Section ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (googleUser == null) {
                        Text(
                            text = "Sign in to back up your data securely to Google Drive.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Button(onClick = {
                            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken("YOUR_WEB_CLIENT_ID")
                                .requestEmail()
                                .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
                                .build()
                            val googleSignInClient = GoogleSignIn.getClient(context, gso)
                            authResultLauncher.launch(googleSignInClient.signInIntent)
                        }) {
                            Text("Sign in with Google")
                        }
                    } else {
                        Text("Signed in as: ${googleUser?.displayName}", fontWeight = FontWeight.Bold)
                        Text(googleUser?.email ?: "", fontSize = 14.sp)
                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = { showRestoreDialog = { viewModel.backupToDrive() } },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = state !is BackupRestoreState.InProgress
                        ) { Text("Backup to Google Drive") }

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = { showRestoreDialog = { viewModel.restoreFromDrive() } },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = state !is BackupRestoreState.InProgress
                        ) { Text("Restore from Google Drive") }

                        Spacer(Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { viewModel.signOut(context) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Sign Out") }
                    }
                }
            }

            if (state is BackupRestoreState.InProgress) {
                Spacer(Modifier.height(24.dp))
                CircularProgressIndicator()
                Spacer(Modifier.height(8.dp))
                Text("Processing… Please wait.")
            }

            // --- Local Backup Section ---
            Spacer(Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Local Backup",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "Save a backup file to your device's storage.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    Button(
                        onClick = { createDocumentLauncher.launch(suggestedFileName) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state !is BackupRestoreState.InProgress
                    ) { Text("Backup to Device") }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = { openDocumentLauncher.launch(arrayOf("application/json")) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state !is BackupRestoreState.InProgress
                    ) { Text("Restore from Device") }
                }
            }
        }
    }

    // Confirmation dialog
    if (showRestoreDialog != null) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = null },
            title = { Text("Confirm Action") },
            text = { Text("This action may overwrite your current data. Are you sure?") },
            confirmButton = {
                TextButton(onClick = {
                    showRestoreDialog?.invoke()
                    showRestoreDialog = null
                }) { Text("Yes, Continue") }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = null }) { Text("Cancel") }
            }
        )
    }
}
