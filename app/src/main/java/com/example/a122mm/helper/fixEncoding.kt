package com.example.a122mm.helper

fun String.fixEncoding(): String {
    return this
        .toByteArray(Charsets.ISO_8859_1).toString(Charsets.UTF_8)
        .replace("\\\"", "\"") // replace \" with "
        .replace("~", ": ")     // replace ~ with :
        .replace("`", "'")     // keep your earlier replacement
}
