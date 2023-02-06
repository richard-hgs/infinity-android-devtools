package com.infinity.mysql

/**
 * Created by richard on 05/02/2023 19:23
 *
 * Mysql Database Management
 */
open class MysqlDatabase {

    /**
     * Builder for MysqlDatabase.
     *
     * @param T The type of the abstract database class.
     */
    open class Builder<T : MysqlDatabase> internal constructor(
        private val klass: Class<T>,
        private val name: String,
        private val host: String,
        private val port: Int,
        private val user: String,
        private val pass: String
    ) {
        open fun build(): T {
            val db = Mysql.getGeneratedImplementation<T, T>(klass, "_Impl")
            return db
        }
    }
}