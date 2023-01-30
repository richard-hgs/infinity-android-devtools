package com.infinity.devtools.domain.database

import androidx.room.*
import com.infinity.devtools.constants.ConstantsDb.TABLE_MYSQL_CONN
import com.infinity.devtools.data.model.MysqlConn
import kotlinx.coroutines.flow.Flow

typealias MysqlConns = List<MysqlConn>

@Dao
interface MysqlConnDao {
    @Query("SELECT * FROM $TABLE_MYSQL_CONN ORDER BY id ASC")
    fun getConns(): Flow<MysqlConns>

    @Query("SELECT * FROM $TABLE_MYSQL_CONN WHERE id = :id")
    fun getConn(id: Int): MysqlConn?

    @Query("SELECT * FROM $TABLE_MYSQL_CONN WHERE host = :host AND port = :port AND user = :user")
    fun getConn(host: String, port: Int, user: String): MysqlConn?

    @Insert
    fun addConn(entity: MysqlConn)

    @Update
    fun updateConn(entity: MysqlConn)

    @Delete
    fun deleteConn(entity: MysqlConn)

    @Query("DELETE FROM $TABLE_MYSQL_CONN")
    fun deleteAllConns()
}