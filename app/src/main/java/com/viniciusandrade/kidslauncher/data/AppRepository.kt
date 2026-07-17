package com.viniciusandrade.kidslauncher.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.viniciusandrade.kidslauncher.data.model.LauncherApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads the set of launchable apps from the system and launches them.
 *
 * We intentionally rely on the [PackageManager] MAIN/LAUNCHER query (declared in
 * the manifest's <queries> block) instead of QUERY_ALL_PACKAGES: it returns
 * exactly the apps a launcher is allowed to surface and keeps us Play-compliant.
 */
class AppRepository(private val context: Context) {

    private val packageManager: PackageManager get() = context.packageManager

    /**
     * Query every app that exposes a MAIN/LAUNCHER activity, sorted alphabetically.
     * Runs on [Dispatchers.Default] because resolving + sorting can touch hundreds
     * of packages on a loaded device.
     */
    suspend fun loadInstalledApps(): List<LauncherApp> = withContext(Dispatchers.Default) {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val ownPackage = context.packageName

        packageManager.queryIntentActivities(intent, 0)
            .asSequence()
            // Never let the child open the launcher's own icon from inside itself.
            .filter { it.activityInfo.packageName != ownPackage }
            .map { resolveInfo ->
                LauncherApp(
                    packageName = resolveInfo.activityInfo.packageName,
                    activityName = resolveInfo.activityInfo.name,
                    label = resolveInfo.loadLabel(packageManager).toString(),
                    icon = resolveInfo.loadIcon(packageManager),
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    /**
     * Launch an app in a new task. Any failure (app uninstalled between the query
     * and the tap, disabled component, etc.) is reported via [onError] so the UI
     * can show a gentle message rather than crashing the child's home screen.
     */
    fun launch(app: LauncherApp, onError: (Throwable) -> Unit = {}) {
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
                ?.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                ?: error("Sem intent de abertura para ${app.packageName}")
            context.startActivity(launchIntent)
        } catch (t: Throwable) {
            onError(t)
        }
    }

    /**
     * Open a web link (e.g. a YouTube video) in whatever app handles it — the
     * YouTube app if installed, otherwise a browser. Reports failures via [onError].
     */
    fun openUrl(url: String, onError: (Throwable) -> Unit = {}) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (t: Throwable) {
            onError(t)
        }
    }
}
