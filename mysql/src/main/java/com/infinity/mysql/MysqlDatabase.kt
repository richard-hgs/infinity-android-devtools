package com.infinity.mysql

/**
 * Created by richard on 05/02/2023 19:23
 *
 * Mysql Database Management
 */
open class MysqlDatabase {

    /**
     * JDBC instance of current mysql connection
     */
    private lateinit var odbc: MysqlOdbc

    /**
     * Builder for MysqlDatabase.
     *
     * @param T The type of the abstract database class.
     * @param klass Database class
     * @param connInfo Connection information used to connect to mysql server
     */
    open class Builder<T : MysqlDatabase> internal constructor(
        private val klass: Class<T>,
        private val connInfo: MysqlConnInfo
    ) {
        open fun build(): T {
            // Get a instance of the generated implementation of this database connector
            val db = Mysql.getGeneratedImplementation<T, T>(klass, "_Impl")
            // Instantiate the JDBC [MysqlOdbc]
            db.odbc = MysqlOdbcImpl()
            return db
        }
    }
}