package com.infinity.mysql

import com.mysql.cj.exceptions.ConnectionIsClosedException
import java.sql.SQLException

/**
 * Created by richard on 04/02/2023 22:50
 *
 * Responsible for Mysql ODBC connection management.
 */
interface MysqlOdbc {

    /**
     * Connects to a mysql database
     *
     * @param host Host name of database server
     * @param port Port of database server
     * @param user User registered in database
     * @param pass User password
     *
     * @throws java.sql.SQLException
     */
    @kotlin.jvm.Throws(SQLException::class)
    fun connect(host: String, port: Int, user: String, pass: String)

    /**
     * Disconnect from a mysql database
     *
     * @param validTimeout Timeout millis to wait when checking connection validity
     *
     * @throws java.sql.SQLException
     */
    @kotlin.jvm.Throws(SQLException::class)
    fun disconnect(validTimeout: Int)

    /**
     * Check if a database connection is open
     *
     * @return True=Connection Open, False=Connection Closed
     *
     * @throws java.sql.SQLException
     */
    @kotlin.jvm.Throws(SQLException::class)
    fun isConnected() : Boolean

    /**
     * Executes a sql in current connection
     *
     * @throws java.sql.SQLException
     * @throws ConnectionIsClosedException if connection is closed or not opened using [connect]
     */
    @kotlin.jvm.Throws(SQLException::class)
    fun execSQL(query: String, bindArgs: Array<Any>) : OdbcResult
}