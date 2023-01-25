package com.infinity.devtools.utils

import java.io.File
import java.io.FileInputStream
import java.io.IOException

object FileUtils {

    /**
     * Read a file content as String
     *
     * @param file File being read
     * @return String file content
     */
    fun readFile(file: File) : String {
        val strBuilder = StringBuilder()
        try {
            FileInputStream(file).use { fis ->
                var content: Int
                // reads a byte at a time, if it reached end of the file, returns -1
                while (fis.read().also { content = it } != -1) {
                    strBuilder.append(content.toChar())
                    // println(content.toChar())
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return strBuilder.toString()
    }
}