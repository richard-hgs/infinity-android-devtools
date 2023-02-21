package com.infinity.devtools.domain.database

import androidx.room.*
import com.infinity.devtools.constants.ConstantsDb.TABLE_MYSQL_CONN
import com.infinity.devtools.model.sqlite.MysqlConn
import kotlinx.coroutines.flow.Flow

typealias MysqlConns = List<MysqlConn>
typealias MysqlConnsFlow = Flow<MysqlConns>

/**
 * Data access object used to manage database connections
 */
@Dao
interface MysqlConnDao {
    /**
     * Get all connections from database
     *
     * @return A [MysqlConnsFlow] holding all [MysqlConn]
     */
    @Query("SELECT * FROM $TABLE_MYSQL_CONN ORDER BY id ASC")
    fun getConns(): MysqlConnsFlow

    /**
     * Get a single database connection by its id
     *
     * @param id Id of database connection
     * @return  [MysqlConn] or Null if connection not found
     */
    @Query("SELECT * FROM $TABLE_MYSQL_CONN WHERE id = :id")
    fun getConn(id: Int): MysqlConn?

    /**
     * Get a single database connection by its host, port and user name
     *
     * @param host  Connection host
     * @param port  Connection port
     * @param user  Connection user
     * @return [MysqlConn] or Null if connection not found
     */
    @Query("SELECT * FROM $TABLE_MYSQL_CONN WHERE host = :host AND port = :port AND user = :user")
    fun getConn(host: String, port: Int, user: String): MysqlConn?

    /**
     * Add a new database connection to database
     *
     * @param entity [MysqlConn] to be added
     */
    @Insert
    fun addConn(entity: MysqlConn)

    /**
     * Update a exiting database connection
     *
     * @param entity [MysqlConn] to be updated
     */
    @Update
    fun updateConn(entity: MysqlConn)

    /**
     * Delete a exiting database connection
     *
     * @param entity [MysqlConn] to be deleted
     */
    @Delete
    fun deleteConn(entity: MysqlConn)

    /**
     * Delete all existing database connections
     */
    @Query("DELETE FROM $TABLE_MYSQL_CONN")
    fun deleteAllConns()
}