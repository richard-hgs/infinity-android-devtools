package com.infinity.mysql.processor.extensions

/**
 * Created by richard on 08/02/2023 23:03
 * Extensions for kotlin strings
 */
fun String._camelCase() = lowercase().split("_|\\s".toRegex()).joinToString("") { word -> word.replaceFirstChar { it.uppercase() }}

fun String._snakeCase() = replace("\\s".toRegex(), "_").lowercase()

fun String._decapitalize() = split(" ").joinToString(" ") { word -> word.replaceFirstChar { it.lowercase() } }

fun String._capitalizeFirst() = replaceFirstChar { it.uppercase() }