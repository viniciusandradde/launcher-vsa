package com.viniciusandrade.kidslauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.viniciusandrade.kidslauncher.ui.KidsLauncherRoot
import com.viniciusandrade.kidslauncher.ui.KidsLauncherViewModel

/**
 * The single Activity. Registered as CATEGORY_HOME so it can be selected as the
 * device's default launcher. When it resumes we refresh the app list so newly
 * installed/removed apps show up without a restart.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: KidsLauncherViewModel by viewModels { KidsLauncherViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KidsLauncherRoot(viewModel)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshApps()
        // Credit time spent in a launched app towards the daily screen-time budget.
        viewModel.onLauncherResumed()
    }
}
