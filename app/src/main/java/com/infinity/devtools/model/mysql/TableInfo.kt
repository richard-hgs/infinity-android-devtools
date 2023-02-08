package com.infinity.devtools.model.mysql

import com.infinity.mysql.annotation.Entity

/**
 * Created by richard on 07/02/2023 21:57
 * Entity that will hold Mysql database table information
 */
@Entity(
    tableName = "information_schema.tables"
)
class TableInfo(
    private val tableName: String
) {

}