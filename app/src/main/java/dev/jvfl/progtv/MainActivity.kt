package dev.jvfl.progtv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import dev.jvfl.progtv.ui.AppRoot
import dev.jvfl.progtv.ui.theme.ProgTvTheme

/** Single-activity host. All navigation happens inside Compose. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            ProgTvTheme {
                AppRoot()
            }
        }
    }

    /**
     * Close the app when the user leaves it (HOME / Recents) instead of keeping it in
     * the background. Reopening then starts fresh (splash) and resumes the last channel.
     */
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        finishAndRemoveTask()
    }
}
