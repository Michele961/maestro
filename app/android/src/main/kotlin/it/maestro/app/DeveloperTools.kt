package it.maestro.app

import android.content.Context
import android.content.pm.ApplicationInfo

/**
 * Strumenti di sviluppo (crash di prova, partita all'ultima mano, dialog del crash precedente):
 * disponibili solo nelle build debuggabili, mai in quella di rilascio (task F1R-05).
 */
public object DeveloperTools {
    /** `true` se l'app è stata compilata come debuggabile (variante debug). */
    public fun isEnabled(context: Context): Boolean = isDebuggable(context.applicationInfo.flags)

    internal fun isDebuggable(applicationFlags: Int): Boolean =
        applicationFlags and ApplicationInfo.FLAG_DEBUGGABLE != 0
}
