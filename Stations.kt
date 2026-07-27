package com.nightdial.radio

data class Station(val name: String, val url: String) {
    val host: String
        get() = try { java.net.URI(url).host ?: url } catch (e: Exception) { url }
}

val STATIONS = listOf(
    Station("OUI FM · Acoustic", "http://ouifmacoustic.ice.infomaniak.ch/ouifmacoustic.mp3"),
    Station("OUI FM 2", "http://ouifm2.ice.infomaniak.ch/ouifm2.mp3"),
    Station("OUI FM · Blues'n'Rock", "http://ouifmbluesnrock.ice.infomaniak.ch/ouifmbluesnrock-128.mp3"),
    Station("OUI FM · Bring the Noise", "http://ouifmbringthenoise.ice.infomaniak.ch/ouifmbringthenoise.mp3"),
    Station("OUI FM · Noël", "http://ouifmnoel.ice.infomaniak.ch/ouifmnoel-128.mp3"),
    Station("OUI FM 3", "http://ouifm3.ice.infomaniak.ch/ouifm3.mp3"),
    Station("OUI FM · Garage Rock", "https://ouifmgaragerock.ice.infomaniak.ch/ouifmgaragerock-128.mp3"),
    Station("OUI FM · Woodstock", "http://woodstock.ice.infomaniak.ch/ouifmwoodstock.mp3"),
    Station("OUI FM · Girls Rock", "http://girlsrock.ice.infomaniak.ch/ouifmgirlsrock.mp3"),
    Station("OUI FM · Slow Rock", "http://slowrock.ice.infomaniak.ch/ouifmslowrock.mp3"),
    Station("OUI FM · Ganja", "http://ouifmganja.ice.infomaniak.ch/ouifmganja-128.mp3"),
    Station("OUI FM · Sixties", "http://ouifmrock60s.ice.infomaniak.ch/ouifmsixties.mp3"),
    Station("OUI FM · Seventies", "http://seventies.ice.infomaniak.ch/ouifmseventies.mp3"),
    Station("OUI FM · Eighties", "http://eighties.ice.infomaniak.ch/ouifmeighties.mp3"),
    Station("OUI FM · Nineties", "http://nineties.ice.infomaniak.ch/ouifmnineties.mp3"),
    Station("OUI FM · Rock 2000", "http://ouifmrock2000.ice.infomaniak.ch/ouifmrock2000.mp3"),
    Station("OUI FM · Rock Français", "http://rockfrancais.ice.infomaniak.ch/ouifmrockfrancais.mp3"),
    Station("OUI FM 5", "http://ouifm5.ice.infomaniak.ch/ouifm5.mp3"),
    Station("OUI FM · Summertime", "http://summertime.ice.infomaniak.ch/ouifmsummertime.mp3"),
    Station("OUI FM · Top Week", "http://topweek.ice.infomaniak.ch/ouifmtopweek.mp3"),
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
