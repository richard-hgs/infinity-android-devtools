package com.infinity.devtools.domain.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.infinity.devtools.constants.ConstantsDb.DB_NAME
import com.infinity.devtools.constants.ConstantsDb.DB_VERSION
import com.infinity.devtools.model.sqlite.MysqlConn

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

    abstract fun getMysqlConnDao(): MysqlConnDao
}