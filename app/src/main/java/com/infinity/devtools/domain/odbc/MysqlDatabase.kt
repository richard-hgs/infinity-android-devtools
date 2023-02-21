package com.infinity.devtools.domain.odbc

import com.infinity.devtools.BuildConfig
import com.infinity.mysql.Mysql
import com.infinity.mysql.annotation.Database
import com.infinity.mysql.management.MysqlConnInfo

/**
 * Online Mysql Database Connection
 */
@Database
abstract class MysqlDatabase : com.infinity.mysql.MysqlDatabase() {
    companion object {
        var INSTANCE : MysqlDatabase? = null

        /**
         * Create a singleton instance of our database.
         * Called internally by the [com.infinity.mysql.MysqlDatabase] when a connection with database is requested.
         *
         * @return [MysqlDatabase] instance
         */
        fun getDatabase(): MysqlDatabase {
            if (INSTANCE == null) {
                // NOTE: You can change to your test server the information inside project root file: "local.properties"
                // by creating variables below:
                // db_name=MyDbName
                // db_host=my.host.com
                // db_port=3306
                // db_user=my_db_user_name
                // db_pass=my_db_pass
                INSTANCE = Mysql.databaseBuilder(
                    MysqlDatabase::class.java,
                    MysqlConnInfo(
                        BuildConfig.DB_NAME,
                        BuildConfig.DB_HOST,
                        BuildConfig.DB_PORT,
                        BuildConfig.DB_USER,
                        BuildConfig.DB_PASS
                    )
                ).build()
            }

            return INSTANCE as MysqlDatabase
        }
    }

    abstract fun getMysqlDao() : MysqlDao
}