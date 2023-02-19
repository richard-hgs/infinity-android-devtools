package com.infinity.devtools.domain.odbc

import com.infinity.devtools.BuildConfig
import com.infinity.devtools.model.mysql.TableInfo
import com.infinity.mysql.annotation.Dao
import com.infinity.mysql.annotation.Query

/**
 * Created by richard on 05/02/2023 19:15
 * Mysql DAO
 */
@Dao
interface MysqlDao {
    @Query("SELECT TABLE_NAME, CREATE_TIME, UPDATE_TIME, TABLE_COLLATION, ENGINE, TABLE_COMMENT, " +
            "AUTO_INCREMENT, TABLE_ROWS, " +
            "ROUND((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024, 5) AS SIZE_MB " +
            "FROM information_schema.tables WHERE table_schema = :schema")
    fun getTables(schema : String = BuildConfig.DB_NAME) : List<TableInfo>

    @Query("SELECT ROUND(SUM(DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024, 5) AS SIZE_MB FROM information_schema.tables WHERE table_schema = :schema")
    fun getDbDiskSize(schema : String = BuildConfig.DB_NAME) : Double?
}