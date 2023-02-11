package com.infinity.mysql.processor.exceptions

import com.google.devtools.ksp.symbol.Location

/**
 * Created by richard on 11/02/2023 14:42
 *
 * Exception for query bindings
 */
class QueryBindException(
    message: String,
    location: Location,
    qualifiedName: String?,
    fileName: String?
) : BaseException(message, location, qualifiedName, fileName)