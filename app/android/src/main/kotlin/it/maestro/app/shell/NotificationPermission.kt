package it.maestro.app.shell

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

/**
 * Restituisce l'azione «chiedi il permesso per le notifiche» (task F1G-13). Prima di Android 13 il permesso non
 * esiste e se c'è già non si chiede: [onResult] arriva subito con `true`. Altrimenti si apre la richiesta di sistema e
 * [onResult] riceve l'esito. Chi la chiama decide quando: qui non c'è nessuna insistenza.
 */
@Composable
public fun rememberNotificationPermissionRequest(onResult: (Boolean) -> Unit): () -> Unit {
    val context = LocalContext.current
    val currentOnResult = rememberUpdatedState(onResult)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        currentOnResult.value(it)
    }
    return {
        if (isNotificationPermissionGranted(context)) {
            currentOnResult.value(true)
        } else {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

private fun isNotificationPermissionGranted(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
