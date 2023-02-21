package com.infinity.devtools.domain.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.infinity.devtools.constants.ConstantsDb.DB_NAME
import com.infinity.devtools.constants.ConstantsDb.DB_VERSION
import com.infinity.devtools.model.sqlite.MysqlConn

/**
 * Application local database used to persist data.
 */
@Database(
    entities = [
        MysqlConn::class
    ],
    version = DB_VERSION,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    // val THREADS = 5
    // var databaseWriteExecutor = Executors.newFixedThreadPool(THREADS)


    companion object {
        var INSTANCE : AppDatabase? = null

        /**
         * Create a singleton instance of our database.
         * Called internally by the [RoomDatabase] when a connection with database is requested.
         *
         * @param context Context of the database instance
         * @return [AppDatabase] instance
         */
        fun getDatabase(context: Context): AppDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java, DB_NAME
                )
//          .addMigrations(
//              MIGRATION_2_3,
//              MIGRATION_3_4
//          )
                .build()
            }
            return INSTANCE as AppDatabase
        }
    }

    /**
     * Return database connection DAO interface
     *
     * @return [MysqlConnDao] interface
     */
    abstract fun getMysqlConnDao(): MysqlConnDao
}