package com.spendwiz.app.Screens

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
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
    viewModel: BackupRestoreViewModel = viewModel(factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory(LocalContext.current.applicationContext as android.app.Application))
) {
    val state by viewModel.state.collectAsState()
    val googleUser by viewModel.googleUser.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // State for restore confirmation dialog
    var showRestoreDialog by remember { mutableStateOf<(() -> Unit)?>(null) }


    // --- GOOGLE SIGN-IN LAUNCHER ---
    val authResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleSignInResult(result.data)
    }

    // --- LOCAL BACKUP/RESTORE LAUNCHERS ---
    val suggestedFileName = remember {
        val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        "spendwiz_backup_$dateStr.json"
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri: Uri? -> uri?.let { viewModel.backupData(it) } }
    )

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                showRestoreDialog = { viewModel.restoreData(it) }
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
        if (state !is BackupRestoreState.InProgress) {
            viewModel.resetState() // Reset state after showing snackbar
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Backup & Restore") }) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- Google Drive Section ---
            if (googleUser == null) {
                // Not Signed In View
                Text(
                    text = "Sign in to back up your data to Google Drive. Your backup file will be stored privately and will not be visible in your main Drive folder.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    // **IMPORTANT**: Replace with your Web Client ID from Google Cloud Console
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken("903991082659-uauv6af5179j70ijr2sn42ufop96ibi2.apps.googleusercontent.com")
                        .requestEmail()
                        .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    authResultLauncher.launch(googleSignInClient.signInIntent)
                }) {
                    Text("Sign in with Google")
                }
            } else {
                // Signed In View
                Text("Signed in as: ${googleUser?.displayName}", fontWeight = FontWeight.Bold)
                Text(googleUser?.email ?: "", fontSize = 14.sp)
                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { showRestoreDialog = { viewModel.backupToDrive() } },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state !is BackupRestoreState.InProgress
                ) {
                    Text("Backup to Google Drive")
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { showRestoreDialog = { viewModel.restoreFromDrive() } },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state !is BackupRestoreState.InProgress
                ) {
                    Text("Restore from Google Drive")
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { viewModel.signOut() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign Out")
                }
            }

            // --- Processing Indicator ---
            if (state is BackupRestoreState.InProgress) {
                Spacer(Modifier.height(24.dp))
                CircularProgressIndicator()
                Spacer(Modifier.height(8.dp))
                Text("Processing… Please wait.")
            }


            // --- Local Backup Section ---
            Divider(modifier = Modifier.padding(vertical = 24.dp))

            Text(
                "Local Backup",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
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
            ) {
                Text("Backup to Device")
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { openDocumentLauncher.launch(arrayOf("application/json")) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state !is BackupRestoreState.InProgress
            ) {
                Text("Restore from Device")
            }
        }
    }

    // Restore confirmation dialog
    if (showRestoreDialog != null) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = null },
            title = { Text("Confirm Action") },
            text = {
                Text(
                    "This action may overwrite your current data. This cannot be undone. Are you sure you want to continue?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestoreDialog?.invoke()
                        showRestoreDialog = null
                    }
                ) {
                    Text("Yes, Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}