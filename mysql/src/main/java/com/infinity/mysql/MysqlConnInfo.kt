package com.infinity.mysql

/**
 * Created by richard on 09/02/2023 01:00
 *
 * Connection information that will be used to connect to Mysql database and run queries
 *
 * @param host Host name of database server
 * @param port Port of database server
 * @param user User registered in database
 * @param pass User password
 */
data class MysqlConnInfo(
    val name: String,
    val host: String,
    val port: Int,
    val user: String,
    val pass: String
)
