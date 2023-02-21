package com.infinity.devtools.constants

import com.infinity.devtools.domain.database.AppDatabase

/**
 * Local database version, name, tables and columns names
 */
object ConstantsDb {

    /**
     * Database version used in [AppDatabase]
     */
    const val DB_VERSION = 1
    /**
     * Database name used in [AppDatabase]
     */
    const val DB_NAME = "database"

    const val TABLE_MYSQL_CONN = "mysql_connection"
    const val MYSQL_CONN_ID = "id"
    const val MYSQL_CONN_NAME = "name"
    const val MYSQL_CONN_HOST = "host"
    const val MYSQL_CONN_PORT = "port"
    const val MYSQL_CONN_USER = "user"
    const val MYSQL_CONN_PASS = "pass"
    const val MYSQL_CONN_DBNAME = "dbname"
}