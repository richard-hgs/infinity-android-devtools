package com.infinity.mysql.processor.extensions

/**
 * Created by richard on 08/02/2023 23:03
 * Extensions for kotlin strings
 */
fun String.toCamelCase() = split('_').joinToString("", transform = String::capitalize)

fun String.toSnakeCase() = replace("(?<=.)(?=\\p{Upper})".toRegex(), "_").lowercase()

fun String.toDecapitalize() = split(" ").joinToString { word -> word.replaceFirstChar { it.lowercase() } }