package com.spendwiz.app.Screens

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.Coil
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
// ✅ ADDED: New imports for the unbundled model manager
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.spendwiz.app.Ads.CommonNativeAd
import com.spendwiz.app.AppStyle.AppColors.customButtonColors
import com.spendwiz.app.AppStyle.AppColors.customCardColors
import com.spendwiz.app.Database.money.Money
import com.spendwiz.app.Database.money.TransactionType
import com.spendwiz.app.R
import com.spendwiz.app.ViewModels.AddScreenViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

data class ParsedReceiptData(
    val merchant: String?,
    val date: String?,
    val total: Double?,
    val time: String? = null
)

// ✅ UPDATED: A more descriptive state than a simple boolean
enum class ProcessingState {
    IDLE,
    DOWNLOADING_MODEL,
    SCANNING_IMAGE
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ReceiptScanScreen(
    viewModel: AddScreenViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    // ✅ CHANGED: Replaced 'isProcessing' with a more detailed state
    var processingState by remember { mutableStateOf(ProcessingState.IDLE) }
    var showMissingDataDialog by remember { mutableStateOf(false) }
    var parsedData by remember { mutableStateOf<ParsedReceiptData?>(null) }

    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = context.externalCacheDir
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    val tempImageFile by remember { lazy { createImageFile() } }
    val tempImageUri = remember {
        FileProvider.getUriForFile(context, "${context.packageName}.provider", tempImageFile)
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success -> if (success) imageUri = tempImageUri }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            CommonNativeAd(Modifier, stringResource(id = R.string.ad_unit_id_receipt_scan_screen))
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                    bottom = innerPadding.calculateBottomPadding()
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (imageUri == null) {
                BillScannerStillInBetaCard()
                Column(
                    Modifier.padding(top = 20.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        "Scan a Receipt",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    Button(
                        onClick = {
                            if (cameraPermissionState.status.isGranted) takePictureLauncher.launch(
                                tempImageUri
                            )
                            else cameraPermissionState.launchPermissionRequest()
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = customButtonColors()
                    ) {
                        Icon(painter = painterResource(R.drawable.camera), "Open Camera")
                        Text("Open Camera", Modifier.padding(5.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { pickImageLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = customButtonColors()
                    ) {
                        Icon(painter = painterResource(R.drawable.gallery), "Pick from Gallery")
                        Text("Pick from Gallery", Modifier.padding(5.dp))
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Selected Receipt",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                // ✅ UPDATED: Show progress indicator for both downloading and scanning
                if (processingState != ProcessingState.IDLE) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (processingState == ProcessingState.DOWNLOADING_MODEL)
                                "Downloading recognition model..."
                            else
                                "Scanning receipt...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                imageUri = null
                                Coil.imageLoader(context).memoryCache?.clear()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = customButtonColors(),
                            modifier = Modifier.weight(1f)
                        ) { Text("Clear") }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = {
                                // ✅ CHANGED: Call the new wrapper function
                                downloadModelAndProcess(
                                    context = context,
                                    imageUri = imageUri!!,
                                    onStateChange = { newState -> processingState = newState },
                                    onResult = { text ->
                                        parsedData = parseReceiptText(text)
                                        processingState = ProcessingState.IDLE
                                        showMissingDataDialog = true
                                    }
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = customButtonColors(),
                            modifier = Modifier.weight(1f)
                        ) { Text("Scan & Save") }
                    }
                }
            }

            if (showMissingDataDialog && parsedData != null) {
                MissingDataDialog(
                    parsedData = parsedData!!,
                    onDismiss = { showMissingDataDialog = false },
                    onSave = { updatedData ->
                        showMissingDataDialog = false
                        saveTransaction(viewModel, updatedData, navController, context)
                    }
                )
            }
        }
    }
}

// This function wraps the entire download and process logic
fun downloadModelAndProcess(
    context: Context,
    imageUri: Uri,
    onStateChange: (ProcessingState) -> Unit,
    onResult: (String) -> Unit
) {
    val moduleInstallClient = ModuleInstall.getClient(context)
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val TAG = "ReceiptScan" // Define a tag for easy filtering in Logcat

    moduleInstallClient
        .areModulesAvailable(recognizer)
        .addOnSuccessListener {
            if (it.areModulesAvailable()) {
                // ✅ ADDED: Log for when the model is already present
                Log.d(TAG, "ML Kit OCR Model is already available.")
                onStateChange(ProcessingState.SCANNING_IMAGE)
                processImageForText(context, imageUri, onResult)
            } else {
                // ✅ ADDED: Log for when a download is needed
                Log.d(TAG, "ML Kit OCR Model not found. Starting download.")
                onStateChange(ProcessingState.DOWNLOADING_MODEL)
                val installRequest = ModuleInstallRequest.newBuilder().addApi(recognizer).build()
                moduleInstallClient
                    .installModules(installRequest)
                    .addOnSuccessListener {
                        // ✅ ADDED: Log for a successful download
                        Log.d(TAG, "Model downloaded successfully.")
                        onStateChange(ProcessingState.SCANNING_IMAGE)
                        processImageForText(context, imageUri, onResult)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Model download failed: ${e.message}", Toast.LENGTH_LONG).show()
                        Log.e(TAG, "Model download failed", e)
                        onStateChange(ProcessingState.IDLE)
                    }
            }
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Model check failed: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Model check failed", e)
            onStateChange(ProcessingState.IDLE)
        }
}

// This function now only contains the recognition logic and is called when the model is ready.
fun processImageForText(context: Context, imageUri: Uri, onResult: (String) -> Unit) {
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    try {
        val image = InputImage.fromFilePath(context, imageUri)
        recognizer.process(image)
            .addOnSuccessListener { visionText -> onResult(visionText.text) }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Check your internet connection. First-time use of this feature requires an ML model to be downloaded", Toast.LENGTH_LONG).show()
                onResult("")
            }
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to load image: ${e.message}", Toast.LENGTH_LONG).show()
        onResult("")
    }
}


// --- Your other functions (parseReceiptText, saveTransaction, dialogs, etc.) remain unchanged ---
// ... (rest of your file) ...
fun parseReceiptText(text: String): ParsedReceiptData {
    val lines = text.lines().map { it.trim().replace("\\s+".toRegex(), " ") }.filter { it.isNotEmpty() }

    // Merchant
    val skipWords = listOf("invoice", "receipt", "bill", "statement", "tax", "gst", "no", "total", "amount")
    val merchant = lines.firstOrNull { line -> skipWords.none { line.lowercase().contains(it) } }

    // Total
    val totalKeywords = listOf("grand total", "net total", "amount due", "amount payable", "balance due",
        "total amount", "total amt", "total payable", "total", "net amt", "net amount",
        "total inr", "gross total", "bill total", "total bill", "total incl", "total including gst")
    val currencySymbols = listOf("₹", "rs", "rs.", "inr", "$", "€", "£")
    val numberRegex = """(\d{1,3}(?:[,\s]\d{2,3})*(?:\.\d{1,2})?|\d+\.\d{1,2})"""
    val totalLinePattern = Pattern.compile("""(?i)(${totalKeywords.joinToString("|")})[:\-]?\s*(?:${currencySymbols.joinToString("|")})?\s*($numberRegex)""")
    val currencyAmountPattern = Pattern.compile("""(?i)(?:${currencySymbols.joinToString("|")})\s*($numberRegex)""")
    val totalsFound = mutableListOf<Double>()
    for (i in lines.indices.reversed()) {
        val line = lines[i]
        val m = totalLinePattern.matcher(line)
        if (m.find()) {
            totalsFound.add(m.group(2)!!.replace("[,\\s]".toRegex(), "").toDouble())
            continue
        }
        val nm = currencyAmountPattern.matcher(line)
        if (nm.find() && i > 0 && totalKeywords.any { lines[i - 1].lowercase().contains(it) }) {
            totalsFound.add(nm.group(1)!!.replace("[,\\s]".toRegex(), "").toDouble())
        }
    }
    val total = totalsFound.maxOrNull()

    // Date
    var date: String? = null
    val datePattern = Pattern.compile(
        """((?:\d{1,2}(?:st|nd|rd|th)?[\s.,/-]\w{3,9}[\s.,/-]\d{2,4})|(?:\w{3,9}[\s.,/-]\d{1,2}(?:st|nd|rd|th)?(?:,)?[\s.,/-]\d{2,4})|(?:\d{4}[\s./-]\d{1,2}[\s./-]\d{1,2})|(?:\d{1,2}[\s./-]\d{1,2}[\s./-]\d{2,4}))""",
        Pattern.CASE_INSENSITIVE
    )
    val inputFormats = listOf(
        "dd MMMM yyyy", "dd-MMMM-yyyy", "dd MMM yyyy", "dd-MMM-yyyy", "MMM dd, yyyy", "MMMM dd, yyyy",
        "dd/MM/yyyy", "dd-MM-yyyy", "dd.MM.yyyy", "dd/MM/yy", "dd-MM-yy", "dd.MM.yy",
        "MM/dd/yyyy", "MM-dd-yyyy", "MM.dd.yyyy", "MM/dd/yy", "MM-dd-yy", "MM.dd.yy",
        "yyyy-MM-dd", "yyyy/MM/dd", "yyyy.MM.dd", "yyyy dd MM"
    ).map { SimpleDateFormat(it, Locale.US).apply { isLenient = false } }
    val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
    run {
        for (line in lines) {
            val matcher = datePattern.matcher(line)
            if (matcher.find()) {
                var found = matcher.group(1) ?: continue
                found = found.replace("(?<=\\d)(st|nd|rd|th)".toRegex(), "")
                for (format in inputFormats) {
                    try {
                        val parsed = format.parse(found)
                        if (parsed != null) {
                            date = outputFormat.format(parsed)
                            return@run
                        }
                    } catch (_: Exception) {}
                }
            }
        }
    }

    // ✅ Time detection
    var time: String? = null
    val timePattern = Pattern.compile("""\b(1[0-2]|0?[1-9]):([0-5]\d)(?:\s?[APap][Mm])?\b|\b([01]?\d|2[0-3]):([0-5]\d)\b""")
    for (line in lines) {
        val matcher = timePattern.matcher(line)
        if (matcher.find()) {
            time = matcher.group().trim()
            break
        }
    }

    return ParsedReceiptData(merchant, date, total, time)
}

@RequiresApi(Build.VERSION_CODES.O)
private fun saveTransaction(
    viewModel: AddScreenViewModel,
    data: ParsedReceiptData,
    navController: NavController,
    context: Context
) {
    if (data.total == null || data.total <= 0) {
        Toast.makeText(context, "Invalid or zero amount.", Toast.LENGTH_SHORT).show(); return
    }
    if (data.date.isNullOrBlank()) {
        Toast.makeText(context, "Date is missing.", Toast.LENGTH_SHORT).show(); return
    }
    val finalTime = data.time ?: Money.getCurrentTime()

    viewModel.addMoney1(
        amount = data.total,
        description = data.merchant ?: "Scanned Receipt",
        type = TransactionType.EXPENSE,
        date = data.date,
        time = finalTime,
        category = "Receipt",
        subCategory = "General"
    )
    Toast.makeText(context, "Transaction Saved!", Toast.LENGTH_SHORT).show()
    navController.popBackStack()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissingDataDialog(
    parsedData: ParsedReceiptData,
    onDismiss: () -> Unit,
    onSave: (ParsedReceiptData) -> Unit
) {
    var merchant by remember { mutableStateOf(parsedData.merchant ?: "") }
    var date by remember { mutableStateOf(parsedData.date ?: "") }
    var total by remember { mutableStateOf(parsedData.total?.toString() ?: "") }
    var time by remember { mutableStateOf(parsedData.time ?: "") }
    var showTimePicker by remember { mutableStateOf(parsedData.time.isNullOrBlank()) }

    if (showTimePicker) {
        TimePickerDialog(
            onTimeSelected = { selected ->
                time = selected
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Details") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Some details were missing. Please confirm or enter manually.")
                OutlinedTextField(value = merchant, onValueChange = { merchant = it }, label = { Text("Merchant") })
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (dd/MM/yyyy)") })
                OutlinedTextField(value = total, onValueChange = { total = it }, label = { Text("Total Amount") })
                OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Time (HH:mm)") })
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(
                    ParsedReceiptData(
                        merchant = merchant.ifBlank { "Scanned Receipt" },
                        date = date,
                        total = total.toDoubleOrNull(),
                        time = time.ifBlank { null }
                    )
                )
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(onTimeSelected: (String) -> Unit, onDismiss: () -> Unit) {
    val state = rememberTimePickerState()
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val hour = state.hour.toString().padStart(2, '0')
                val minute = state.minute.toString().padStart(2, '0')
                onTimeSelected("$hour:$minute")
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        text = { TimePicker(state = state) }
    )
}


@Composable
fun BillScannerStillInBetaCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = customCardColors()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Information",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Our new Bill Scanning feature is currently in its beta phase. This means you may encounter some inaccuracies in the scanned data. We are continuously working on improving this feature and appreciate your patience." ,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Justify
            )
        }
    }
}