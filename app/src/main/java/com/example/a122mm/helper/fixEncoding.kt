package com.example.a122mm.helper

fun String.fixEncoding(): String {
    return this
        .toByteArray(Charsets.ISO_8859_1).toString(Charsets.UTF_8)
        .replace("\\\"", "\"") // replace \" with "
        .replace("~", ": ")     // replace ~ with :
        .replace("`", "'")     // keep your earlier replacement
        .replace("[\u2012\u2013\u2014\u2015\u2212]".toRegex(), "-")
}
