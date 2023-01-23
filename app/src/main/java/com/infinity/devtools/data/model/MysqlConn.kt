package com.infinity.devtools.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.infinity.devtools.constants.Constants.NO_VALUE
import com.infinity.devtools.constants.ConstantsDb.TABLE_MYSQL_CONN

@Entity(
    tableName = TABLE_MYSQL_CONN,
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["host", "port", "user"], unique = true)
    ]
)
data class MysqlConn(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val host: String,
    val port: Int,
    val user: String,
    val pass: String,
    val dbname: String
) {
    constructor() : this(0, NO_VALUE, NO_VALUE, 0, NO_VALUE, NO_VALUE, NO_VALUE)
}