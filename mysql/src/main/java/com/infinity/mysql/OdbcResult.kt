package com.infinity.mysql

import java.sql.ResultSet
import java.sql.Statement

/**
 * Created by richard on 04/02/2023 23:20
 *
 * Holds odbc query results
 */
data class OdbcResult(
    val result: Boolean,
    val resultSet: ResultSet?,
    val updateCount: Int,
    val stmt: Statement
)