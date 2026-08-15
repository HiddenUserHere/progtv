package dev.jvfl.progtv.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.TransferListener
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import dev.jvfl.progtv.domain.model.StreamRef
import dev.jvfl.progtv.ui.components.BufferingIndicator
import dev.jvfl.progtv.ui.theme.BgBlack
import dev.jvfl.progtv.ui.theme.TextMuted
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicLong

/** Fallback User-Agent when the backend did not report a working one. */
private const val DEFAULT_UA = "VLC/3.0.20 LibVLC/3.0.20"

/**
 * Fullscreen Media3 player.
 *
 * Buffering UX:
 *  - initial load  -> centered spinner + live KB/s (starts at 0)
 *  - mid-play stall -> compact spinner + live KB/s badge in the TOP-RIGHT corner
 * The KB/s is the REAL bytes/second measured over a 1s window via a TransferListener
 * (ExoPlayer's smoothed bitrateEstimate looks frozen, so we count actual bytes).
 */
@OptIn(UnstableApi::class)
@Composable
fun PlayerSurface(
    stream: StreamRef?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val url = stream?.url

    // Counts network bytes as they are transferred (thread-safe).
    val byteCounter = remember { AtomicLong(0L) }
    val transferListener = remember {
        object : TransferListener {
            override fun onTransferInitializing(source: DataSource, dataSpec: DataSpec, isNetwork: Boolean) {}
            override fun onTransferStart(source: DataSource, dataSpec: DataSpec, isNetwork: Boolean) {}
            override fun onBytesTransferred(
                source: DataSource,
                dataSpec: DataSpec,
                isNetwork: Boolean,
                bytesTransferred: Int,
            ) {
                if (isNetwork) byteCounter.addAndGet(bytesTransferred.toLong())
            }
            override fun onTransferEnd(source: DataSource, dataSpec: DataSpec, isNetwork: Boolean) {}
        }
    }
    // HTTP data source whose User-Agent / Referer we update per stream before each load,
    // so the player sends the exact UA the backend proved works for that stream.
    val httpFactory = remember {
        DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setUserAgent(DEFAULT_UA)
    }
    val exoPlayer = remember {
        val dataSourceFactory = DefaultDataSource.Factory(context, httpFactory)
            .setTransferListener(transferListener)
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .build()
            .apply { playWhenReady = true }
    }

    var buffering by remember { mutableStateOf(false) }
    var everReady by remember { mutableStateOf(false) }
    var bytesPerSec by remember { mutableLongStateOf(0L) }

    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                val isBuffering = playbackState == Player.STATE_BUFFERING
                if (isBuffering && !buffering) bytesPerSec = 0L // reset to 0 when it appears
                buffering = isBuffering
                if (playbackState == Player.STATE_READY) everReady = true
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // (Re)load whenever the selected stream changes.
    LaunchedEffect(stream) {
        everReady = false
        bytesPerSec = 0L
        if (url.isNullOrBlank()) {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
        } else {
            // Apply the stream-specific UA / Referer to the shared HTTP factory so the
            // next data sources ExoPlayer creates (manifest + segments) use them.
            httpFactory.setUserAgent(stream?.userAgent?.takeIf { it.isNotBlank() } ?: DEFAULT_UA)
            httpFactory.setDefaultRequestProperties(
                buildMap { stream?.referrer?.takeIf { it.isNotBlank() }?.let { put("Referer", it) } },
            )
            exoPlayer.setMediaItem(MediaItem.fromUri(url))
            exoPlayer.prepare()
            exoPlayer.play()
        }
    }

    // Real download rate: bytes delta over a 1-second window, updated every second.
    LaunchedEffect(exoPlayer) {
        var last = byteCounter.get()
        while (true) {
            delay(1000)
            val now = byteCounter.get()
            bytesPerSec = (now - last).coerceAtLeast(0L)
            last = now
        }
    }

    Box(modifier = modifier.fillMaxSize().background(BgBlack)) {
        if (!url.isNullOrBlank()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        useController = false
                        setPlayer(exoPlayer)
                        // Never take focus — keys must stay with the Compose overlay
                        // so OK/UP/DOWN keep working after zapping channels.
                        isFocusable = false
                        isFocusableInTouchMode = false
                        descendantFocusability = android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
                    }
                },
            )
        } else {
            Text(
                text = "Selecione um canal",
                style = MaterialTheme.typography.titleMedium,
                color = TextMuted,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        // Initial load -> centered indicator.
        if (buffering && !everReady && !url.isNullOrBlank()) {
            BufferingIndicator(kbps = bytesPerSec, modifier = Modifier.align(Alignment.Center))
        }
        // Mid-play stall -> compact badge, TOP-RIGHT.
        if (buffering && everReady) {
            BufferingIndicator(
                kbps = bytesPerSec,
                compact = true,
                modifier = Modifier.align(Alignment.TopEnd).padding(20.dp),
            )
        }
    }
}
