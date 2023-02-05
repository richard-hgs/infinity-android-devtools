package com.infinity.devtools.domain.odbc

import com.infinity.devtools.data.model.OdbcResult
import com.mysql.cj.exceptions.ConnectionIsClosedException
import java.io.InputStream
import java.math.BigDecimal
import java.sql.*
import java.util.*

/**
 * Created by richard on 04/02/2023 22:28
 *
 * Responsible for Mysql ODBC connection management.
 */
class MysqlOdbcImpl : MysqlOdbc {

    var conn: Connection? = null

    /**
     * Constructor that loads jdbc driver and creates a new instance
     *
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @kotlin.jvm.Throws(ClassNotFoundException::class, IllegalAccessException::class, InstantiationException::class)
    constructor() {
        Class.forName("com.mysql.jdbc.Driver").newInstance()
    }

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
    override fun connect(host: String, port: Int, user: String, pass: String) {
        val connectionProps = Properties()
        connectionProps["user"] = user
        connectionProps["password"] = pass

        conn = DriverManager.getConnection(
            "jdbc:mysql://$host:$port/", connectionProps
        )
    }

    /**
     * Disconnect from a mysql database
     *
     * @param validTimeout Timeout millis to wait when checking connection validity
     *
     * @throws java.sql.SQLException
     */
    @kotlin.jvm.Throws(SQLException::class)
    override fun disconnect(validTimeout: Int) {
        conn?.let {
            if (it.isValid(validTimeout)) {
                it.close()
                conn = null
            }
        }
    }

    /**
     * Check if a database connection is open
     *
     * @return True=Connection Open, False=Connection Closed
     *
     * @throws java.sql.SQLException
     */
    @kotlin.jvm.Throws(SQLException::class)
    override fun isConnected(): Boolean {
        return conn?.let {
            !it.isClosed
        } ?: false
    }

    /**
     * Executes a sql in current connection
     *
     * @throws java.sql.SQLException
     * @throws ConnectionIsClosedException if connection is closed or not opened using [connect]
     */
    @kotlin.jvm.Throws(SQLException::class)
    override fun execSQL(query: String, bindArgs: Array<Any>) : OdbcResult {
        if (isConnected()) {
            return conn?.let { mConn ->
                val stmt : PreparedStatement = mConn.prepareStatement(query)

                bindArgs.forEachIndexed { index, value ->
                    when(value) {
                        is Boolean -> stmt.setBoolean(index, value)
                        is Byte -> stmt.setByte(index, value)
                        is Short -> stmt.setShort(index, value)
                        is Int -> stmt.setInt(index, value)
                        is Long -> stmt.setLong(index, value)
                        is Float -> stmt.setFloat(index, value)
                        is Double -> stmt.setDouble(index, value)
                        is BigDecimal -> stmt.setBigDecimal(index, value)
                        is String -> stmt.setString(index, value)
                        is ByteArray -> stmt.setBytes(index, value)
                        is java.sql.Date -> stmt.setDate(index, value)
                        is Time -> stmt.setTime(index, value)
                        is Timestamp -> stmt.setTimestamp(index, value)
                        is InputStream -> stmt.setBinaryStream(index, value)
                    }
                }

                val execResult = stmt.execute()

                if (execResult) {
                    // Result Set
                    return OdbcResult(
                        result = true,
                        resultSet = stmt.resultSet,
                        updateCount = 0,
                        stmt = stmt
                    )
                } else {
                    // Update Count
                    return OdbcResult(
                        result = true,
                        resultSet = stmt.resultSet,
                        updateCount = stmt.updateCount,
                        stmt = stmt
                    )
                }
            } ?: throw SQLException("OdbcResult is missing.")

        } else {
            throw ConnectionIsClosedException("Connection is closed, did you forget to call MysqlOdbc fun connect(host: String, port: Int, user: String, pass: String)")
        }
    }
}