package com.infinity.devtools.di.validators

import androidx.core.text.trimmedLength
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Mysql validations
 */
@Singleton
class MysqlValidator @Inject constructor() {
    /**
     * Validates if a connection name is valid
     *
     * @param name Connection name
     * @return  true=Valid, false=Invalid
     */
    fun connNameIsValid(name: String): Boolean {
        return name.trimmedLength() > 0
    }

    /**
     * Validates if a connection host is valid
     *
     * @param host Connection host
     * @return  true=Valid, false=Invalid
     */
    fun connHostIsValid(host: String): Boolean {
        return host.trimmedLength() > 0
    }

    /**
     * Validates if a connection port is valid
     *
     * @param port Connection port
     * @return  true=Valid, false=Invalid
     */
    fun connPortIsValid(port: Int): Boolean {
        return port in 0..65535
    }

    /**
     * Validates if a connection user is valid
     *
     * @param user Connection user
     * @return  true=Valid, false=Invalid
     */
    fun connUserIsValid(user: String): Boolean {
        return user.trimmedLength() > 0
    }

    /**
     * Validates if a connection pass is valid
     *
     * @param pass Connection pass
     * @return  true=Valid, false=Invalid
     */
    fun connPassIsValid(pass: String): Boolean {
        return pass.trimmedLength() > 0
    }

    /**
     * Validates if a connection database name is valid
     *
     * @param dbname Connection database name
     * @return  true=Valid, false=Invalid
     */
    fun connDbnameIsValid(dbname: String): Boolean {
        return dbname.trimmedLength() > 0
    }
}