package com.infinity.mysql.model

import java.sql.ResultSet

/**
 * Created by richard on 14/02/2023 21:37
 * Save result set metadata column info like type and other info provided by the [ResultSet.getMetaData]
 * @param name Column name
 * @param type Column data type
 */
data class ColumnInfo(
    val idx: Int,
    val name: String,
    val type: ColumnType
)
