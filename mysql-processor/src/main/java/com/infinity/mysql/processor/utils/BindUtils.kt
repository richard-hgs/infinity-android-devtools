package com.infinity.mysql.processor.utils

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.infinity.mysql.annotation.Dao
import com.infinity.mysql.annotation.Query
import com.infinity.mysql.processor.exceptions.QueryBindException

/**
 * Created by richard on 11/02/2023 14:34
 *
 * Utilities for query bind generation
 */
object BindUtils {
    /**
     * Bind the query and also validates if the binding parameter exists in function name
     *
     * @param query         The query with binding placeholders parameters :param
     * @param ksFunc        The function annotated with [Query] that is member of [Dao] annotated interface.
     * Used to get file and line position when a exception occurs.
     * @return [BindResult] The result with the query prepared for bind and the parameter map of this query
     */
    fun bindQuery(query: String, ksFunc: KSFunctionDeclaration): BindResult {
        val queryParamMap = hashMapOf<Int, KSValueParameter>()
        val funcParamMap = mutableMapOf<String, KSValueParameter>()

        // Map function parameters to increase speed, since we will be
        // Iterating for each bind param searching for parameter name
        ksFunc.parameters.forEach {
            funcParamMap[it.name?.asString() ?: ""] = it
        }

        // Split all bind arguments from the query
        val newQuery =
            query.split("(?<=.)(?=:\\w+)|(?<=:\\w{1,100}\\s)".toRegex()).joinToString("") { word ->
                if (word.matches(":\\w+\\s*".toRegex())) {
                    // Remove the : and spaces from the binding placeholder to turn into function parameter name
                    val funcParamName = word.replace(":|\\s".toRegex(), "")

                    // Foreach function parameter find the parameter that matches the name
                    ksFunc.parameters.find { ksValParam ->
                        (ksValParam.name ?: "") == funcParamName
                    }

                    // If a parameter with the given bind name exists return its KSValueParameter
                    val funcParamBind = funcParamMap[funcParamName]

                    if (funcParamBind != null) {
                        // If the word is a bind argument, save the bind index and the function parameter to bind to
                        queryParamMap[queryParamMap.size] = funcParamBind
                    } else {
                        // Check if the function has the parameter that needs to be bound
                        throw QueryBindException(
                            "The query bind parameter $word not found in function parameters.",
                            ksFunc.location,
                            ksFunc.qualifiedName?.asString(),
                            ksFunc.containingFile?.fileName
                        )
                    }

                    // Join the query bind question mark
                    "?"
                } else {
                    // The word is a query part. Join to the query
                    word
                }
            }

        return BindResult(
            query = newQuery,
            bindMap = queryParamMap
        )
    }
}