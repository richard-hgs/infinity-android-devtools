package com.infinity.devtools.di.validators

import androidx.core.text.trimmedLength
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MysqlValidator @Inject constructor() {

    fun connNameIsValid(name: String): Boolean {
        return name.trimmedLength() > 0
    }

    fun connHostIsValid(host: String): Boolean {
        return host.trimmedLength() > 0
    }

    fun connPortIsValid(port: Int): Boolean {
        return port in 0..65535
    }

    fun connUserIsValid(user: String): Boolean {
        return user.trimmedLength() > 0
    }

    fun connPassIsValid(pass: String): Boolean {
        return pass.trimmedLength() > 0
    }

    fun connDbnameIsValid(dbname: String): Boolean {
        return dbname.trimmedLength() > 0
    }
}