package com.kw.starter.common.extension

val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()

fun String.toSnakeCase(): String = camelRegex.replace(this) { "_${it.value}" }.lowercase()
