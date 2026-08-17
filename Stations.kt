package com.nightdial.radio

data class Station(val name: String, val url: String) {
    val host: String
        get() = try { java.net.URI(url).host ?: url } catch (e: Exception) { url }
}

// OUI FM migrated off ice.infomaniak.ch to *.stream.ouifm.fr (HTTPS) in 2026.
// If a OUI FM stream ever fails, the old infomaniak URLs are in git history.
val STATIONS = listOf(
    Station("OUI FM · Acoustic", "https://acoustic.stream.ouifm.fr/ouifmacoustic.mp3"),
    Station("OUI FM 2", "https://alternatif.stream.ouifm.fr/ouifm2.mp3"),
    Station("OUI FM · Blues'n'Rock", "https://bluesnrock.stream.ouifm.fr/ouifmbluesnrock-128.mp3"),
    Station("OUI FM · Bring the Noise", "https://bringthenoise.stream.ouifm.fr/ouifmbringthenoise.mp3"),
    Station("OUI FM · Noël", "http://christmasrock.stream.ouifm.fr/ouifmnoel-128.mp3"),
    Station("OUI FM 3", "https://classicrock.stream.ouifm.fr/ouifm3.mp3"),
    // NOTE: garagerock slug is unconfirmed — verify by playing before trusting it.
    Station("OUI FM · Garage Rock", "https://garagerock.stream.ouifm.fr/ouifmgaragerock-128.mp3"),
    Station("OUI FM · Woodstock", "https://woodstock.stream.ouifm.fr/ouifmwoodstock.mp3"),
    Station("OUI FM · Girls Rock", "https://girlsrock.stream.ouifm.fr/ouifmgirlsrock.mp3"),
    Station("OUI FM · Slow Rock", "https://slowrock.stream.ouifm.fr/ouifmslowrock.mp3"),
    Station("OUI FM · Ganja", "https://ganja.stream.ouifm.fr/ouifmganja-128.mp3"),
    Station("OUI FM · Sixties", "https://rock60s.stream.ouifm.fr/ouifmsixties.mp3"),
    Station("OUI FM · Seventies", "https://rock70s.stream.ouifm.fr/ouifmseventies.mp3"),
    Station("OUI FM · Eighties", "https://rock80s.stream.ouifm.fr/ouifmeighties.mp3"),
    Station("OUI FM · Nineties", "https://rock90s.stream.ouifm.fr/ouifmnineties.mp3"),
    Station("OUI FM · Rock 2000", "https://rock2000.stream.ouifm.fr/ouifmrock2000.mp3"),
    Station("OUI FM · Rock Français", "https://rockfrancais.stream.ouifm.fr/ouifmrockfrancais.mp3"),
    Station("OUI FM 5", "https://rockinde.stream.ouifm.fr/ouifm5.mp3"),
    Station("OUI FM · Summertime", "https://summertime.stream.ouifm.fr/ouifmsummertime.mp3"),
    Station("OUI FM · Top Week", "https://topweek.stream.ouifm.fr/ouifmtopweek.mp3"),
    Station("Europe 1", "http://europe1.lmn.fm/europe1.mp3"),
    Station("Europe 2 · Hits", "https://europe2.lmn.fm/vr-wr12.mp3"),
    Station("Europe 2 · Lounge", "https://europe2.lmn.fm/vr-wr3.mp3"),
    Station("Europe 2 · Rock", "http://europe2.lmn.fm/vr-wr4.mp3"),
    Station("Europe 2 · Dance", "https://europe2.lmn.fm/vr-wr7.mp3"),
    Station("Europe 2 · Party", "https://europe2.lmn.fm/vr-wr15.mp3"),
    Station("Europe 2 · 80s", "http://europe2.lmn.fm/vr-wr5.mp3"),
    Station("Europe 2 · 90s", "https://europe2.lmn.fm/vr-wr10.mp3"),
    Station("France Info", "http://icecast.radiofrance.fr/franceinfo-hifi.aac"),
    Station("France Inter", "http://icecast.radiofrance.fr/franceinter-hifi.aac"),
    Station("RTL", "http://icecast.rtl.fr/rtl-1-44-128?listen=webCwsBCggNCQgLDQUGBAcGBg"),
    Station("Sud Radio", "http://ice.creacast.com/sudradio"),
    Station("Classic 21", "https://radio.rtbf.be/c21/mp3-160/me"),
    Station("Classic 21 60s", "https://radio.rtbf.be/c21-60s/mp3-128/me"),
    Station("Classic 21 70s", "https://radio.rtbf.be/c21-70s/mp3-128/me"),
    Station("Classic 21 80s", "https://radio.rtbf.be/c21-80s/mp3-128/me"),
    Station("Classic 21 90s", "https://radio.rtbf.be/c21-90s/mp3-128/me"),
    Station("Classic 21 Live", "https://radio.rtbf.be/c21-live/mp3-128/me"),
    Station("Classic 21 Baroque", "https://radio.rtbf.be/m3-bar/mp3-192/me")
)
