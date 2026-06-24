package com.nightdial.radio

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.launch

private val BG = Color(0xFF141110)
private val PANEL = Color(0xFF1E1916)
private val INK = Color(0xFFF2E7DB)
private val MUTED = Color(0xFF9C8B7B)
private val AMBER = Color(0xFFE8A13A)
private val AMBER_SOFT = Color(0xFFF4C87E)
private val LINE = Color(0xFF3A302A)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) {
            val perm = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                registerForActivityResult(ActivityResultContracts.RequestPermission()) {}.launch(perm)
            }
        }
        setContent { RadioApp() }
    }
}

@Composable
fun RadioApp() {
    val context = LocalContext.current
    var controller by remember { mutableStateOf<MediaController?>(null) }

    DisposableEffect(Unit) {
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener({ controller = future.get() }, MoreExecutors.directExecutor())
        onDispose {
            controller?.release()
            controller = null
        }
    }

    var currentIndex by remember { mutableStateOf(-1) }
    var isPlaying by remember { mutableStateOf(false) }
    var song by remember { mutableStateOf("") }
    var lyrics by remember { mutableStateOf("") }
    var lyricsSource by remember { mutableStateOf("") }
    var lyricsBusy by remember { mutableStateOf(false) }
    var manualHold by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    DisposableEffect(controller) {
        val c = controller ?: return@DisposableEffect onDispose {}
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
            override fun onMediaMetadataChanged(md: MediaMetadata) {
                val t = md.title?.toString()?.trim() ?: ""
                if (t.isNotEmpty() && t != song) { song = t; manualHold = false }
            }
        }
        c.addListener(listener)
        isPlaying = c.isPlaying
        onDispose { c.removeListener(listener) }
    }

    LaunchedEffect(song, manualHold) {
        if (song.isNotBlank() && !manualHold) {
            lyricsBusy = true; lyrics = ""; lyricsSource = ""
            val r = fetchLyrics(song)
            lyricsBusy = false
            if (r != null) { lyrics = r.text; lyricsSource = r.source } else lyrics = ""
        }
    }

    fun tune(i: Int) {
        val c = controller ?: return
        currentIndex = i; song = ""; lyrics = ""; lyricsSource = ""; manualHold = false
        c.setMediaItem(MediaItem.fromUri(STATIONS[i].url))
        c.prepare()
        c.play()
    }

    fun toggle() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else { c.prepare(); c.play() }
    }

    Surface(color = BG, modifier = Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
                .padding(top = 24.dp)
        ) {
            Text(
                "NIGHT·DIAL",
                color = AMBER,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                itemsIndexed(STATIONS) { i, st ->
                    val tuned = i == currentIndex
                    Column(
                        Modifier
                            .width(180.dp)
                            .height(92.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(PANEL)
                            .border(1.dp, if (tuned) AMBER else LINE, RoundedCornerShape(16.dp))
                            .clickable { tune(i) }
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            st.name,
                            color = if (tuned) AMBER_SOFT else INK,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            st.host,
                            color = MUTED,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(PANEL)
                    .padding(14.dp)
            ) {
                Box(
                    Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(AMBER)
                        .clickable { if (currentIndex < 0) tune(0) else toggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (isPlaying) "❚❚" else "▶", color = BG, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "TUNED TO",
                        color = MUTED,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        letterSpacing = 2.sp
                    )
                    Text(
                        if (currentIndex >= 0) STATIONS[currentIndex].name else "— pick a station —",
                        color = INK,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        if (song.isNotBlank()) song else if (isPlaying) "Live" else "Paused",
                        color = MUTED,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "LYRICS",
                    color = MUTED,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp,
                    modifier = Modifier.weight(1f)
                )
                if (lyricsSource.isNotBlank()) {
                    Text("via $lyricsSource", color = AMBER, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            }

            Spacer(Modifier.height(8.dp))

            var query by remember { mutableStateOf("") }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Type Artist – Title", color = MUTED) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = INK,
                        unfocusedTextColor = INK,
                        focusedBorderColor = AMBER,
                        unfocusedBorderColor = LINE,
                        cursorColor = AMBER
                    )
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        val v = query.trim()
                        if (v.isNotEmpty()) {
                            manualHold = true
                            scope.launch {
                                lyricsBusy = true; lyrics = ""; lyricsSource = ""
                                val r = fetchLyrics(v)
                                lyricsBusy = false
                                if (r != null) { lyrics = r.text; lyricsSource = r.source; song = v } else lyrics = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AMBER, contentColor = BG)
                ) { Text("Find") }
            }

            Spacer(Modifier.height(12.dp))

            val scroll = rememberScrollState()
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(scroll)
            ) {
                if (song.isNotBlank()) {
                    Text(
                        song,
                        color = AMBER_SOFT,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Serif
                    )
                }
                Spacer(Modifier.height(8.dp))
                when {
                    lyricsBusy ->
                        Text("Looking up lyrics…", color = MUTED, fontFamily = FontFamily.Serif, fontSize = 18.sp)
                    lyrics.isNotBlank() ->
                        Text(lyrics, color = INK, fontFamily = FontFamily.Serif, fontSize = 20.sp, lineHeight = 30.sp)
                    currentIndex < 0 ->
                        Text("Tap a station — lyrics appear automatically when the song is known.", color = MUTED, fontFamily = FontFamily.Serif, fontSize = 18.sp)
                    song.isNotBlank() ->
                        Text("No lyrics found for this one — or type it above.", color = MUTED, fontFamily = FontFamily.Serif, fontSize = 18.sp)
                    else ->
                        Text("Waiting for song info…", color = MUTED, fontFamily = FontFamily.Serif, fontSize = 18.sp)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
