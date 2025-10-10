package com.spendwiz.app.Screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.spendwiz.app.Ads.CommonNativeAd
import com.spendwiz.app.Ads.InterstitialAdManager
import com.spendwiz.app.AppStyle.AppColors.customButtonColors
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
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf<(() -> Unit)?>(null) }
    var importedCount by remember { mutableStateOf<Int?>(null) }
    var isImporting by remember { mutableStateOf(false) }
    var showPermissionSettingsDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as Activity

    val configuration = LocalConfiguration.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) {
                CommonNativeAd(Modifier ,
                    stringResource(id = R.string.ad_unit_id_backup_screen)
                )
            }
        }
    ) { innerPadding ->
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                BackupRestoreContent(
                    modifier = Modifier.weight(1f),
                    addScreenViewModel = addScreenViewModel,
                    viewModel = viewModel,
                    state = state,
                    googleUser = googleUser,
                    showLogoutDialog = showLogoutDialog,
                    onShowLogoutDialogChange = { showLogoutDialog = it },
                    showRestoreDialog = showRestoreDialog,
                    onShowRestoreDialogChange = { showRestoreDialog = it },
                    importedCount = importedCount,
                    onImportedCountChange = { importedCount = it },
                    isImporting = isImporting,
                    onIsImportingChange = { isImporting = it },
                    showPermissionSettingsDialog = showPermissionSettingsDialog,
                    onShowPermissionSettingsDialogChange = { showPermissionSettingsDialog = it }
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CommonNativeAd(
                        Modifier,
                        stringResource(id = R.string.ad_unit_id_backup_screen)
                    )
                }
            }
        } else {
            BackupRestoreContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                        end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                        bottom = innerPadding.calculateBottomPadding()
                    ),
                addScreenViewModel = addScreenViewModel,
                viewModel = viewModel,
                state = state,
                googleUser = googleUser,
                showLogoutDialog = showLogoutDialog,
                onShowLogoutDialogChange = { showLogoutDialog = it },
                showRestoreDialog = showRestoreDialog,
                onShowRestoreDialogChange = { showRestoreDialog = it },
                importedCount = importedCount,
                onImportedCountChange = { importedCount = it },
                isImporting = isImporting,
                onIsImportingChange = { isImporting = it },
                showPermissionSettingsDialog = showPermissionSettingsDialog,
                onShowPermissionSettingsDialogChange = { showPermissionSettingsDialog = it }
            )
        }
    }

    // Dialogs remain outside the main content layout
    val myStableButtonColor = colorResource(id = R.color.button_color)
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
                        InterstitialAdManager.showAd(activity){}
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
                        InterstitialAdManager.showAd(activity){}
                    },
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

    if (showPermissionSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionSettingsDialog = false },
            title = { Text("Permission Required") },
            text = { Text("You have permanently denied the SMS permission. To use this feature, please enable it manually in the app settings.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionSettingsDialog = false
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", context.packageName, null)
                        intent.data = uri
                        context.startActivity(intent)
                    }
                ) {
                    Text("Go to Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionSettingsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun BackupRestoreContent(
    modifier: Modifier = Modifier,
    addScreenViewModel: AddScreenViewModel,
    viewModel: BackupRestoreViewModel,
    state: BackupRestoreState,
    googleUser: com.google.android.gms.auth.api.signin.GoogleSignInAccount?,
    showLogoutDialog: Boolean,
    onShowLogoutDialogChange: (Boolean) -> Unit,
    showRestoreDialog: (() -> Unit)?,
    onShowRestoreDialogChange: ((() -> Unit)?) -> Unit,
    importedCount: Int?,
    onImportedCountChange: (Int?) -> Unit,
    isImporting: Boolean,
    onIsImportingChange: (Boolean) -> Unit,
    showPermissionSettingsDialog: Boolean,
    onShowPermissionSettingsDialogChange: (Boolean) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activity = context as Activity

    val myStableButtonColor = colorResource(id = R.color.button_color)

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
        onResult = { uri: Uri? -> uri?.let { onShowRestoreDialogChange { viewModel.restoreData(it) } } }
    )

    val requestSmsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            onIsImportingChange(true)
            onImportedCountChange(null)
            coroutineScope.launch {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val count = addScreenViewModel.insertTransactionsFromSms(context)
                    onImportedCountChange(count)
                }
                onIsImportingChange(false)
                InterstitialAdManager.showAd(activity){}
            }
        } else {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_SMS)) {
                onShowPermissionSettingsDialogChange(true)
            } else {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("SMS permission is required to import transactions.")
                }
            }
        }
    }

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
        modifier = modifier
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Backup & Restore",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // --- Google Drive Section ---
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Google Drive Backup",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (googleUser != null) {
                TextButton(
                    colors = customButtonColors(),
                    shape = RoundedCornerShape(12.dp),
                    onClick = { onShowLogoutDialogChange(true) }
                ) {
                    Text("Sign Out")
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
                shape = RoundedCornerShape(8.dp),
                colors = customButtonColors()
            ) {
                Text("Sign in with Google")
            }
        } else {
            Text("Signed in as: ${googleUser.displayName}", fontWeight = FontWeight.Bold)
            Text(
                googleUser.email ?: "",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onShowRestoreDialogChange { viewModel.backupToDrive() } },
                    modifier = Modifier.weight(1f),
                    enabled = state !is BackupRestoreState.InProgress,
                    shape = RoundedCornerShape(8.dp),
                    colors = customButtonColors()
                ) {
                    Text("Backup")
                }
                OutlinedButton(
                    onClick = { onShowRestoreDialogChange { viewModel.restoreFromDrive() } },
                    modifier = Modifier.weight(1f),
                    enabled = state !is BackupRestoreState.InProgress,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, myStableButtonColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = myStableButtonColor)
                ) {
                    Text("Restore")
                }
            }
        }

        if (state is BackupRestoreState.InProgress) {
            Spacer(Modifier.height(12.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = myStableButtonColor
            )
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

        val suggestedFileName = remember {
            val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            "spendwiz_backup_$dateStr.json"
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { createDocumentLauncher.launch(suggestedFileName) },
                modifier = Modifier.weight(1f),
                enabled = state !is BackupRestoreState.InProgress,
                shape = RoundedCornerShape(8.dp),
                colors = customButtonColors()
            ) {
                Text("Backup")
            }
            OutlinedButton(
                onClick = { openDocumentLauncher.launch(arrayOf("application/json")) },
                modifier = Modifier.weight(1f),
                enabled = state !is BackupRestoreState.InProgress,
                shape = RoundedCornerShape(8.dp),
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
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_SMS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    onIsImportingChange(true)
                    onImportedCountChange(null)
                    coroutineScope.launch {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val count = addScreenViewModel.insertTransactionsFromSms(context)
                            onImportedCountChange(count)
                        }
                        onIsImportingChange(false)
                    }
                } else {
                    requestSmsPermissionLauncher.launch(Manifest.permission.READ_SMS)
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
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = myStableButtonColor
                )
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
}

