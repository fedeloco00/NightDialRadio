package com.nightdial.radio

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class LyricsResult(val text: String, val source: String)

private const val UA = "NightDial/1.0 (Android)"

private fun enc(s: String): String = URLEncoder.encode(s, "UTF-8")

private fun httpGet(urlStr: String, headers: Map<String, String> = emptyMap(), timeout: Int = 6000): String? {
    return try {
        val c = URL(urlStr).openConnection() as HttpURLConnection
        c.connectTimeout = timeout
        c.readTimeout = timeout
        c.requestMethod = "GET"
        c.setRequestProperty("User-Agent", UA)
        headers.forEach { (k, v) -> c.setRequestProperty(k, v) }
        if (c.responseCode in 200..299)
            c.inputStream.bufferedReader().use { it.readText() }
        else null
    } catch (e: Exception) {
        null
    }
}

private fun clean(s: String): String {
    var r = s
    r = Regex("\\([^)]*\\)").replace(r, " ")
    r = Regex("\\[[^\\]]*\\]").replace(r, " ")
    r = Regex("(?i)\\b(feat|ft)\\.?\\b.*$").replace(r, " ")
    r = Regex("\\s+").replace(r, " ").trim().trim('-', '\u2013', '\u2014', ' ')
    return r.trim()
}

private fun splitTitle(raw: String): Pair<String, String> {
    val r = raw.trim()
    for (sep in listOf(" - ", " \u2013 ", " \u2014 ", " : ")) {
        val k = r.indexOf(sep)
        if (k > -1) return r.substring(0, k).trim() to r.substring(k + sep.length).trim()
    }
    return "" to r
}

private fun buildPairs(artist: String, title: String): List<Pair<String, String>> {
    val out = ArrayList<Pair<String, String>>()
    val seen = HashSet<String>()
    fun add(a: String, t: String) {
        val aa = a.trim(); val tt = t.trim()
        val key = aa.lowercase() + "|" + tt.lowercase()
        if (aa.isNotEmpty() && tt.isNotEmpty() && seen.add(key)) out.add(aa to tt)
    }
    if (artist.isNotEmpty() && title.isNotEmpty()) {
        add(artist, title); add(title, artist)
        add(clean(artist), clean(title)); add(clean(title), clean(artist))
    }
    return out
}

private fun buildQueries(artist: String, title: String, raw: String): List<String> {
    val out = ArrayList<String>()
    val seen = HashSet<String>()
    fun add(q: String) {
        val qq = Regex("\\s+").replace(q, " ").trim()
        if (qq.isNotEmpty() && seen.add(qq.lowercase())) out.add(qq)
    }
    if (artist.isNotEmpty() && title.isNotEmpty()) {
        add("$artist $title"); add("${clean(artist)} ${clean(title)}")
    }
    add(raw); add(clean(raw))
    if (title.isNotEmpty()) add(title)
    return out
}

private fun lrcToPlain(lrc: String): String {
    val sb = StringBuilder()
    for (line in lrc.lines()) {
        val t = Regex("\\[[^\\]]*\\]").replace(line, "").trim()
        if (t.isNotEmpty()) { sb.append(t); sb.append('\n') }
    }
    return sb.toString().trim()
}

private fun unescapeHtml(s: String): String =
    s.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">")
        .replace("&quot;", "\"").replace("&#x27;", "'").replace("&#39;", "'").replace("&apos;", "'")

private fun provLrclib(pairs: List<Pair<String, String>>, queries: List<String>): String? {
    for ((a, t) in pairs) {
        val resp = httpGet("https://lrclib.net/api/get?artist_name=${enc(a)}&track_name=${enc(t)}") ?: continue
        try {
            val pl = JSONObject(resp).optString("plainLyrics", "")
            if (pl.isNotBlank()) return pl
        } catch (e: Exception) {}
    }
    for (q in queries) {
        val resp = httpGet("https://lrclib.net/api/search?q=${enc(q)}") ?: continue
        try {
            val arr = JSONArray(resp)
            for (i in 0 until arr.length()) {
                val pl = arr.getJSONObject(i).optString("plainLyrics", "")
                if (pl.isNotBlank()) return pl
            }
        } catch (e: Exception) {}
    }
    return null
}

private fun provNetease(pairs: List<Pair<String, String>>, queries: List<String>): String? {
    val hdr = mapOf("Referer" to "https://music.163.com/", "Cookie" to "os=pc; appver=2.0.2")
    for (q in queries) {
        val resp = httpGet("https://music.163.com/api/search/get?s=${enc(q)}&type=1&limit=5", hdr) ?: continue
        try {
            val songs = JSONObject(resp).optJSONObject("result")?.optJSONArray("songs") ?: continue
            for (i in 0 until songs.length()) {
                val id = songs.getJSONObject(i).optLong("id", 0)
                if (id == 0L) continue
                val ly = httpGet("https://music.163.com/api/song/lyric?id=$id&lv=1&kv=1&tv=-1", hdr) ?: continue
                val lrc = JSONObject(ly).optJSONObject("lrc")?.optString("lyric", "") ?: ""
                val plain = lrcToPlain(lrc)
                if (plain.isNotBlank()) return plain
            }
        } catch (e: Exception) {}
    }
    return null
}

private fun provLyrist(pairs: List<Pair<String, String>>, queries: List<String>): String? {
    for ((a, t) in pairs) {
        val resp = httpGet("https://lyrist.vercel.app/api/${enc(t)}/${enc(a)}") ?: continue
        try {
            val l = JSONObject(resp).optString("lyrics", "")
            if (l.isNotBlank()) return l
        } catch (e: Exception) {}
    }
    return null
}

private fun provGenius(pairs: List<Pair<String, String>>, queries: List<String>): String? {
    for (q in queries) {
        val resp = httpGet("https://genius.com/api/search/multi?q=${enc(q)}") ?: continue
        var songUrl: String? = null
        try {
            val sections = JSONObject(resp).optJSONObject("response")?.optJSONArray("sections")
            if (sections != null) {
                outer@ for (i in 0 until sections.length()) {
                    val hits = sections.getJSONObject(i).optJSONArray("hits") ?: continue
                    for (j in 0 until hits.length()) {
                        val hit = hits.getJSONObject(j)
                        if (hit.optString("type") == "song") {
                            songUrl = hit.optJSONObject("result")?.optString("url")
                            if (!songUrl.isNullOrBlank()) break@outer
                        }
                    }
                }
            }
        } catch (e: Exception) {}
        if (songUrl.isNullOrBlank()) continue
        val page = httpGet(songUrl, timeout = 8000) ?: continue
        val parts = Regex("data-lyrics-container=\"true\"[^>]*>(.*?)</div>", RegexOption.DOT_MATCHES_ALL)
            .findAll(page).toList()
        if (parts.isEmpty()) continue
        val sb = StringBuilder()
        for (m in parts) {
            var p = m.groupValues[1]
            p = Regex("<br\\s*/?>").replace(p, "\n")
            p = Regex("<[^>]+>").replace(p, "")
            sb.append(unescapeHtml(p)).append('\n')
        }
        val text = Regex("\n{3,}").replace(sb.toString(), "\n\n").trim()
        if (text.isNotBlank()) return text
    }
    return null
}

private fun provLyricsOvh(pairs: List<Pair<String, String>>, queries: List<String>): String? {
    for ((a, t) in pairs) {
        val resp = httpGet("https://api.lyrics.ovh/v1/${enc(a)}/${enc(t)}", timeout = 5000) ?: continue
        try {
            val l = JSONObject(resp).optString("lyrics", "")
            if (l.isNotBlank()) return l
        } catch (e: Exception) {}
    }
    return null
}

private fun provChartlyrics(pairs: List<Pair<String, String>>, queries: List<String>): String? {
    for ((a, t) in pairs) {
        val xml = httpGet(
            "http://api.chartlyrics.com/apiv1.asmx/SearchLyricDirect?artist=${enc(a)}&song=${enc(t)}",
            timeout = 5000
        ) ?: continue
        val m = Regex("<Lyric>(.*?)</Lyric>", RegexOption.DOT_MATCHES_ALL).find(xml)
        if (m != null) {
            val txt = unescapeHtml(m.groupValues[1]).trim()
            if (txt.isNotBlank()) return txt
        }
    }
    return null
}

private val PROVIDERS: List<Pair<String, (List<Pair<String, String>>, List<String>) -> String?>> = listOf(
    "lrclib" to ::provLrclib,
    "netease" to ::provNetease,
    "lyrist" to ::provLyrist,
    "genius" to ::provGenius,
    "lyrics.ovh" to ::provLyricsOvh,
    "chartlyrics" to ::provChartlyrics
)

suspend fun fetchLyrics(raw: String): LyricsResult? = withContext(Dispatchers.IO) {
    val (artist, title) = splitTitle(raw)
    val pairs = buildPairs(artist, title)
    val queries = buildQueries(artist, title, raw)
    for ((name, fn) in PROVIDERS) {
        val r = try { fn(pairs, queries) } catch (e: Exception) { null }
        if (!r.isNullOrBlank()) return@withContext LyricsResult(r.trim(), name)
    }
    null
}
