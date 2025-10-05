package com.example.a122mm.helper

import okhttp3.HttpUrl
import java.text.Normalizer
import android.net.Uri

//fun String.fixEncoding(): String {
//    return this
//        .toByteArray(Charsets.ISO_8859_1).toString(Charsets.UTF_8)
//        .replace("\\\"", "\"") // replace \" with "
//        .replace("~", ": ")     // replace ~ with :
//        .replace("`", "'")     // keep your earlier replacement
//        .replace("[\u2012\u2013\u2014\u2015\u2212]".toRegex(), "-")
//}

private fun looksLikeMojibake(s: String): Boolean {
    // Heuristics: common mojibake glyphs from UTF-8 read as Latin-1/Windows-1252
    return s.indexOf('\u00C3') >= 0 ||        // 'Ã'
            s.contains("â") ||                // â€™, â€œ, etc.
            s.contains('\uFFFD')              // replacement char �
}

fun String.fixEncoding(): String {
    // 1) Only apply Latin-1→UTF-8 rescue if it *looks* broken
    val rescued = if (looksLikeMojibake(this)) {
        try { String(this.toByteArray(Charsets.ISO_8859_1), Charsets.UTF_8) } catch (_: Throwable) { this }
    } else this

    // 2) Normalize to NFC (compose accents properly)
    val nfc = Normalizer.normalize(rescued, Normalizer.Form.NFC)

    // 3) Clean punctuation you want unified
    return nfc
        // If your JSON is parsed by Moshi/Gson/kotlinx, you usually DON'T need this unescape:
        .replace("\\\"", "\"")        // only keep if you truly receive literal backslash+quote
        .replace("~", ": ")           // your custom rule
        .replace("`", "'")
        // unify various dashes (incl. soft hyphen & figure dash) to ASCII hyphen
        .replace("[\\u00AD\\u2010\\u2011\\u2012\\u2013\\u2014\\u2015\\u2212]".toRegex(), "-")
        // collapse non-breaking & zero-width spaces
        .replace('\u00A0', ' ')
        .replace("\u200B", "")
        // unify curly quotes
        .replace("[\u2018\u2019\u2032]".toRegex(), "'")
        .replace("[\u201C\u201D\u2033]".toRegex(), "\"")
}


fun encodeUrlSegments(full: String): String {
    return try {
        val u = android.net.Uri.parse(full)
        val scheme = u.scheme ?: return full
        val host = u.host ?: return full
        val builder = HttpUrl.Builder()
            .scheme(scheme)
            .host(host)

        if (u.port != -1) builder.port(u.port)

        // Encode each path segment
        u.pathSegments
            ?.filter { it.isNotEmpty() }
            ?.forEach { builder.addPathSegment(it) }

        // Preserve query params (properly encoded)
        for (name in u.queryParameterNames) {
            for (value in u.getQueryParameters(name)) {
                builder.addQueryParameter(name, value)
            }
        }

        builder.build().toString()
    } catch (_: Throwable) {
        full // fallback unchanged
    }
}

private fun encodeStrictPathSegment(seg: String): String {
    val bytes = seg.toByteArray(Charsets.UTF_8)
    val out = StringBuilder(bytes.size * 3)
    for (b in bytes) {
        val c = b.toInt() and 0xFF
        val isUnreserved =
            (c in 0x30..0x39) || // 0-9
                    (c in 0x41..0x5A) || // A-Z
                    (c in 0x61..0x7A) || // a-z
                    c == 0x2D || c == 0x2E || c == 0x5F || c == 0x7E // - . _ ~
        if (isUnreserved) {
            out.append(c.toChar())
        } else {
            out.append('%')
            val hi = "0123456789ABCDEF"[c ushr 4]
            val lo = "0123456789ABCDEF"[c and 0x0F]
            out.append(hi).append(lo)
        }
    }
    return out.toString()
}

fun encodeUrlSegmentsStrict(full: String): String {
    return try {
        val u = Uri.parse(full)
        val scheme = u.scheme ?: return full
        val host = u.host ?: return full

        val builder = HttpUrl.Builder()
            .scheme(scheme)
            .host(host)

        if (u.port != -1) builder.port(u.port)

        // Strict-encode each decoded segment, then add as already-encoded
        u.pathSegments
            ?.filter { it.isNotEmpty() }
            ?.forEach { seg ->
                builder.addEncodedPathSegment(encodeStrictPathSegment(seg))
            }

        // Query params: OkHttp encodes them properly already; keep as-is
        for (name in u.queryParameterNames) {
            for (value in u.getQueryParameters(name)) {
                builder.addQueryParameter(name, value)
            }
        }

        builder.build().toString()
    } catch (_: Throwable) {
        full
    }
}


