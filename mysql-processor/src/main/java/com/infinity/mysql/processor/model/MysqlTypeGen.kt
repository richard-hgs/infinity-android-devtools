package com.infinity.mysql.processor.model

import java.sql.ResultSet

/**
 * Created by richard on 16/02/2023 21:23
 *
 * Holds the string types used to generate the [ResultSet.getString] equivalent to the resolved data type
 * @param type The typename in text format to be used in generation to hold the [getType] parameter returned from the [ResultSet]
 * @param getType The typename in text format to be used in generation to get value from [ResultSet] in the correct mysql type.
 */
data class MysqlTypeGen(
    val type: String,
    val getType: String
)
