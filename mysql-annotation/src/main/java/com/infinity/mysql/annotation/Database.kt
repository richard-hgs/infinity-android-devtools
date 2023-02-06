package com.infinity.mysql.annotation

/**
 * Created by richard on 05/02/2023 15:39
 *
 * Mysql database annotation to mysql-processor be able to locate the database in symbols
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Database
