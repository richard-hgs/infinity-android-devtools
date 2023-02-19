package com.infinity.devtools.model.mysql

import com.infinity.mysql.annotation.ColumnInfo
import com.infinity.mysql.annotation.Entity

/**
 * Created by richard on 07/02/2023 21:57
 * Entity that will hold Mysql database table information
 */
@Entity(
    tableName = "information_schema.tables"
)
class TableInfo(
    @ColumnInfo(name = "TABLE_NAME")
    val tableName: String?,
    @ColumnInfo(name = "CREATE_TIME")
    val createTime: String,
    @ColumnInfo(name = "UPDATE_TIME")
    val updateTime: String?,
    @ColumnInfo(name = "TABLE_COLLATION")
    val collation: String?,
    @ColumnInfo(name = "ENGINE")
    val engine: String?,
    @ColumnInfo(name = "TABLE_COMMENT")
    val comment: String?,
    @ColumnInfo(name = "AUTO_INCREMENT")
    val autoIncrement: Long?,
    @ColumnInfo(name = "TABLE_ROWS")
    val rowCount: Long?,
    @ColumnInfo(name = "SIZE_MB")
    val diskSizeMb: Double?,
)