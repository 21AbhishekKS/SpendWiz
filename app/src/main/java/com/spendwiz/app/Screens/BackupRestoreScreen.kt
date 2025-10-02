package com.spendwiz.app.Screens

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.spendwiz.app.BackUp.BackupRestoreState
import com.spendwiz.app.BackUp.BackupRestoreViewModel
import com.spendwiz.app.R
import com.spendwiz.app.ViewModels.AddScreenViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BackupRestoreScreen(
    addScreenViewModel: AddScreenViewModel,
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
    val coroutineScope = rememberCoroutineScope()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf<(() -> Unit)?>(null) }
    var importedCount by remember { mutableStateOf<Int?>(null) }
    var isImporting by remember { mutableStateOf(false) }

    val myStableButtonColor = colorResource(id = R.color.button_color)

    val authResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleSignInResult(result.data)
    }

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
        onResult = { uri: Uri? -> uri?.let { showRestoreDialog = { viewModel.restoreData(it) } } }
    )

    LaunchedEffect(state) {
        when (val currentState = state) {
            is BackupRestoreState.Success -> snackbarHostState.showSnackbar(currentState.message)
            is BackupRestoreState.Error -> snackbarHostState.showSnackbar(currentState.error)
            else -> Unit
        }
        if (state !is BackupRestoreState.InProgress) {
            viewModel.resetState()
        }
    }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            // Screen Title
            Text(
                text = "Backup & Restore",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // --- Google Drive Section ---
            Row(Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween){
                Text(
                    text = "Google Drive Backup",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (googleUser != null) {
                    TextButton(
                        onClick = { showLogoutDialog = true }
                    ) {
                        Text("Sign Out" , color = colorResource(id = R.color.button_text_color),)
                    }
                }
            }
            Text(
                text = "Sync your data securely with your Google Drive. This ensures your data is safe and accessible across devices.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (googleUser == null) {
                Button(
                    onClick = {
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken("903991082659-uauv6af5179j70ijr2sn42ufop96ibi2.apps.googleusercontent.com")
                            .requestEmail()
                            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
                            .build()
                        val googleSignInClient = GoogleSignIn.getClient(context, gso)
                        authResultLauncher.launch(googleSignInClient.signInIntent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp), // less rounded
                    colors = ButtonDefaults.buttonColors(containerColor = myStableButtonColor)
                ) {
                    Text("Sign in with Google", color = colorResource(id = R.color.button_text_color))
                }
            } else {
                Text("Signed in as: ${googleUser?.displayName}", fontWeight = FontWeight.Bold)
                Text(googleUser?.email ?: "", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showRestoreDialog = { viewModel.backupToDrive() } },
                        modifier = Modifier.weight(1f),
                        enabled = state !is BackupRestoreState.InProgress,
                        shape = RoundedCornerShape(8.dp),
                        // --- THE FIX: Apply the stable color here as well ---
                        colors = ButtonDefaults.buttonColors(containerColor = myStableButtonColor)
                    ) {
                        Text("Backup",
                            color =colorResource(id = R.color.button_text_color))
                    }
                    OutlinedButton(
                        onClick = { showRestoreDialog = { viewModel.restoreFromDrive() } },
                        modifier = Modifier.weight(1f),
                        enabled = state !is BackupRestoreState.InProgress,
                        shape = RoundedCornerShape(8.dp),
                        // --- THE FIX: For OutlinedButton, set the border and content color ---
                        border = BorderStroke(1.dp, myStableButtonColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = myStableButtonColor)
                    ) {
                        Text("Restore")
                    }
                }
            }

            if (state is BackupRestoreState.InProgress) {
                Spacer(Modifier.height(12.dp))
                CircularProgressIndicator(modifier = Modifier.size(28.dp), color = myStableButtonColor)
                Text("Processing… Please wait.", fontSize = 13.sp)
            }

            // --- Local Backup Section ---
            Divider(modifier = Modifier.padding(vertical = 20.dp))

            Text(
                "Local Backup",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Save a backup file on your device storage and restore it anytime manually.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { createDocumentLauncher.launch(suggestedFileName) },
                    modifier = Modifier.weight(1f),
                    enabled = state !is BackupRestoreState.InProgress,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = myStableButtonColor)
                ) {
                    Text("Backup", color = colorResource(id = R.color.button_text_color))
                }
                OutlinedButton(
                    onClick = { openDocumentLauncher.launch(arrayOf("application/json")) },
                    modifier = Modifier.weight(1f),
                    enabled = state !is BackupRestoreState.InProgress,
                    shape = RoundedCornerShape(8.dp),
                    // --- THE FIX: Apply the stable color to the border and text ---
                    border = BorderStroke(1.dp, myStableButtonColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = myStableButtonColor)
                ) {
                    Text("Restore")
                }
            }

            // --- Import from SMS ---
            Divider(modifier = Modifier.padding(vertical = 20.dp))

            Text(
                text = "Import Transactions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Quickly fetch transactions from your SMS inbox.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Button(
                onClick = {
                    isImporting = true
                    importedCount = null
                    coroutineScope.launch {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val count = addScreenViewModel.insertTransactionsFromSms(context)
                            importedCount = count
                        }
                        isImporting = false
                    }
                },
                enabled = !isImporting,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = myStableButtonColor)
            ) {
                Text("Import from SMS", color = colorResource(id = R.color.button_text_color))
            }
            when {
                isImporting -> {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), color = myStableButtonColor)
                    Text("Importing SMS…", fontSize = 13.sp)
                }
                importedCount != null -> {
                    Text(
                        "Imported $importedCount transactions.",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
            Divider(modifier = Modifier.padding(vertical = 20.dp))

        }

    // Restore Confirmation Dialog
    if (showRestoreDialog != null) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = null },
            title = { Text("Confirm Action") },
            text = { Text("This action may overwrite your current data. Are you sure?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestoreDialog?.invoke()
                        showRestoreDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = myStableButtonColor)
                ) { Text("Yes, Continue") }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Confirm Logout") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.signOut(context)
                        showLogoutDialog = false
                    },
                    // --- Optional: Apply color to dialog buttons too ---
                    colors = ButtonDefaults.textButtonColors(contentColor = myStableButtonColor)
                ) { Text("Yes, Sign Out") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

