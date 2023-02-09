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
    @Query("SELECT * FROM information_schema.tables WHERE table_schema = '"+ BuildConfig.DB_NAME+"'")
    fun getTables() : List<TableInfo>
}