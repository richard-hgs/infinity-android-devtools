package com.infinity.mysql.annotation

/**
 * Created by richard on 07/02/2023 21:53
 * Mysql entity that will hold mysql table information, like columns
 *
 * @param tableName Table entity name in database.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Entity(
    val tableName: String
)
