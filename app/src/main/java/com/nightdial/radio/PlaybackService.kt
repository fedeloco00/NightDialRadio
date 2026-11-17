package com.nightdial.radio

import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService() {
    private var session: MediaSession? = null
    private val handler = Handler(Looper.getMainLooper())
    private var retryDelayMs = RETRY_MIN_MS

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        // Don't hang for long on a dead socket; follow http<->https redirects.
        val http = DefaultHttpDataSource.Factory()
            .setConnectTimeoutMs(10_000)
            .setReadTimeoutMs(10_000)
            .setAllowCrossProtocolRedirects(true)
        val sources = DefaultMediaSourceFactory(DefaultDataSource.Factory(this, http))
            .setLoadErrorHandlingPolicy(DefaultLoadErrorHandlingPolicy(6))

        // Live streams: build a 5 s cushion before playing and after a stall.
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(20_000, 30_000, 5_000, 5_000)
            .build()

        val player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(sources)
            .setLoadControl(loadControl)
            .setWakeMode(C.WAKE_MODE_NETWORK)   // keep CPU + Wi-Fi awake with screen off
            .build()

        // Watchdog: on a network error or the server closing the stream, reconnect.
        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) = scheduleReconnect(player)
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) scheduleReconnect(player)
            }
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) retryDelayMs = RETRY_MIN_MS
            }
        })

        session = MediaSession.Builder(this, player).build()
    }

    private fun scheduleReconnect(player: ExoPlayer) {
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({
            val stuck = player.playerError != null || player.playbackState == Player.STATE_ENDED
            val item = player.currentMediaItem
            if (stuck && player.playWhenReady && item != null) {   // not if the user paused
                player.setMediaItem(item)
                player.prepare()
            }
        }, retryDelayMs)
        retryDelayMs = (retryDelayMs * 2).coerceAtMost(RETRY_MAX_MS)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        session?.run {
            player.release()
            release()
        }
        session = null
        super.onDestroy()
    }

    private companion object {
        const val RETRY_MIN_MS = 2_000L
        const val RETRY_MAX_MS = 30_000L
    }
}
