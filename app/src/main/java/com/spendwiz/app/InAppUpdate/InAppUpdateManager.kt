import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

/**
 * A helper class to manage the immediate in-app update flow.
 *
 * This class encapsulates the logic for checking for available updates,
 * starting the update flow, and resuming an update in progress.
 *
 * @param context The application context.
 */
class InAppUpdateManager(context: Context) {

    // The AppUpdateManager instance used to manage updates.
    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(context)

    /**
     * Checks for an available immediate update and initiates the update flow if one is found.
     * This should be called when the app starts.
     *
     * @param activity The current activity, required to start the update flow.
     */
    fun checkForUpdate(activity: Activity) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            val isUpdateAvailable = appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            val isImmediateUpdateAllowed = appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)

            if (isUpdateAvailable && isImmediateUpdateAllowed) {
                // An immediate update is available and allowed.
                // Start the update flow.
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    activity,
                    IMMEDIATE_UPDATE_REQUEST_CODE
                )
            } else {
                Log.d("InAppUpdateManager", "No immediate update available or allowed.")
            }
        }.addOnFailureListener { e ->
            Log.e("InAppUpdateManager", "Failed to check for update: ${e.message}", e)
        }
    }

    /**
     * Checks if an update is already in progress and resumes it.
     * This is crucial for handling cases where the user backgrounds the app
     * during the update process. It should be called in the activity's onResume().
     *
     * @param activity The current activity.
     */
    fun checkAndResumeUpdate(activity: Activity) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            // If an immediate update is in progress, resume it.
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    activity,
                    IMMEDIATE_UPDATE_REQUEST_CODE
                )
            }
        }
    }

    companion object {
        // A request code for the update flow. This can be any arbitrary integer.
        const val IMMEDIATE_UPDATE_REQUEST_CODE = 123
    }
}
