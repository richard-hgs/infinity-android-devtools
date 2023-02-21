package com.infinity.devtools.model.mysql

import com.infinity.mysql.annotation.ColumnInfo
import com.infinity.mysql.annotation.Entity

/**
 * Entity that will hold Online Mysql database table information
 *
 * @param tableName Name of the table
 * @param createTime Timestamp when table was created
 * @param updateTime Timestamp last time table was updated
 * @param collation Collation encoding of the table
 * @param engine Engine used in this table, like innodb or myisam
 * @param comment Table commentaries
 * @param autoIncrement The last auto-increment number from the table
 * @param rowCount The total amount of rows this table has
 * @param diskSizeMb The total disk size this table is using from the server
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