import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * A Jetpack Compose component that handles the in-app update check.
 *
 * This composable should be placed at the top level of your UI hierarchy,
 * for example, at the start destination of your NavHost or in MainActivity's setContent.
 *
 * It uses LaunchedEffect to trigger the check once and DisposableEffect to
 * listen for lifecycle events to resume an update if necessary.
 */
@Composable
fun AppUpdateChecker() {
    // Get the current context and cast it to an Activity.
    // The update flow requires an Activity context.
    val context = LocalContext.current
    val activity = context as? Activity ?: return // Early return if not in an Activity context

    // Remember an instance of the InAppUpdateManager across recompositions.
    val updateManager = remember { InAppUpdateManager(context) }

    // Use a lifecycle observer to resume the update flow when the app comes to the foreground.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            // When the app is resumed, check for an update in progress.
            if (event == Lifecycle.Event.ON_RESUME) {
                updateManager.checkAndResumeUpdate(activity)
            }
        }

        // Add the observer to the lifecycle.
        lifecycleOwner.lifecycle.addObserver(observer)

        // Clean up the observer when the composable is disposed.
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Trigger the initial update check when this composable is first launched.
    // LaunchedEffect with a Unit key runs only once.
    LaunchedEffect(Unit) {
        updateManager.checkForUpdate(activity)
    }

    // This composable does not render any UI of its own. It's a logical component.
    // Your main app UI can be placed after this call.
}
