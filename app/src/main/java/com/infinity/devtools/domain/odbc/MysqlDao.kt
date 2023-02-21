package com.infinity.devtools.domain.odbc

import com.infinity.devtools.BuildConfig
import com.infinity.devtools.model.mysql.TableInfo
import com.infinity.mysql.annotation.Dao
import com.infinity.mysql.annotation.Query

typealias TableInfoList = List<TableInfo>

/**
 * Online MySQL data access object.
 * Used to access data from a online remote database
 */
@Dao
interface MysqlDao {
    /**
     * Get all tables for a database schema
     *
     * @param schema Schema to get tables from
     * @return
     */
    @Query("SELECT TABLE_NAME, CREATE_TIME, UPDATE_TIME, TABLE_COLLATION, ENGINE, TABLE_COMMENT, " +
            "AUTO_INCREMENT, TABLE_ROWS, " +
            "ROUND((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024, 5) AS SIZE_MB " +
            "FROM information_schema.tables WHERE table_schema = :schema")
    fun getTables(schema : String = BuildConfig.DB_NAME) : TableInfoList

    /**
     * Get the used disk size of a database schema in megabytes
     *
     * @param schema Schema to get size from
     * @return  Size in megabytes
     */
    @Query("SELECT ROUND(SUM(DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024, 5) AS SIZE_MB FROM information_schema.tables WHERE table_schema = :schema")
    fun getDbDiskSize(schema : String = BuildConfig.DB_NAME) : Double?
}