package com.infinity.mysql.management

import com.infinity.mysql.model.ColumnInfo
import com.infinity.mysql.model.ColumnType
import java.sql.ResultSet

/**
 * Created by richard on 14/02/2023 22:05
 * [ResultSet] utilities
 */
object ResultSetUtil {
    /**
     * Map [ResultSet] columns getting column type, index and name
     *
     * @param resultSet That holds current querie information
     * @return Mapped column information where the KEY is the column name
     */
    fun mapResultColumns(resultSet: ResultSet) : MutableMap<String, ColumnInfo> {
        val columnIdxMap = mutableMapOf<String, ColumnInfo>()

        for (idx in 1..resultSet.metaData.columnCount) {
            val colName = resultSet.metaData.getColumnName(idx)
            val colType = resultSet.metaData.getColumnType(idx)
            columnIdxMap[colName] = ColumnInfo(
                idx = idx,
                name = colName,
                type = ColumnType.valueOf(colType)!!
            )
        }

        return columnIdxMap
    }

    fun getColumnIndexOrThrow(colMap: Map<String, ColumnInfo>, colName: String) : Int {
        val colInfo = colMap[colName]
        if (colInfo != null && colInfo.idx >= 0) {
            return colInfo.idx
        }

        val availableColumns = colMap.map {
            it.key
        }.joinToString()

        throw IllegalArgumentException(
            "column '$colName' does not exist. Available columns: $availableColumns"
        )
    }
}