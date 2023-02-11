package com.infinity.mysql.processor.utils

import com.google.devtools.ksp.symbol.KSValueParameter

/**
 * Created by richard on 11/02/2023 14:30
 *
 * @param query     Query prepared for prepared statement binding.
 * The query with all :param replaced by ? exclamation marks.
 * @param bindMap   The binding map of the prepared query, that tells:
 * [Int]               Key:   The bind index of the query
 * [KSValueParameter]  Value: The function parameter value that maps to the bind index
 */
data class BindResult(
    val query: String,
    val bindMap: HashMap<Int, KSValueParameter>
)