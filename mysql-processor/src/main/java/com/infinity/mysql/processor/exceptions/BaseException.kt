package com.infinity.mysql.processor.exceptions

import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.Location

/**
 * Created by richard on 11/02/2023 15:22
 *
 * Base query exception
 */
open class BaseException(
    message: String,
    location: Location,
    qualifiedName: String?,
    fileName: String?
) : Exception("$message \n\tat ${String.format("%s(%s:%s)", qualifiedName, fileName, (location as FileLocation).lineNumber)}") {
}