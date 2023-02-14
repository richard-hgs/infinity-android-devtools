package com.infinity.mysql.processor.extensions

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSValueParameter
import java.io.File
import java.io.FileReader

/**
 * Created by richard on 13/02/2023 22:30
 *
 * KSValueParameter extensions
 */
fun KSValueParameter.getDefaultValueExpression(logger: KSPLogger) {
    val fileLocation : FileLocation = this.location as FileLocation
    val file = File(fileLocation.filePath)
    val fileR = FileReader(file)
    val linesSequence = fileR.buffered().lineSequence().iterator()
    var funcText = ""
    var lineIdx = 0

    while(linesSequence.hasNext()) {
        val lineText = linesSequence.next()

        lineIdx++
        if (lineIdx == fileLocation.lineNumber) {
            funcText += lineText
            if (funcText.contains("\\)\$".toRegex())) {
                // Function end line reach
                break
            }
        }
    }
    fileR.close()

    val regexMatchFunc = "(\\s*(\\w*)\\s*:\\s*(\\w*)\\s*=?\\s*([a-zA-Z_.\"\\d]*),?\\s*)".toRegex()
    // val matchGroups = funcText.(regexMatchFunc)
}