package com.infinity.devtools.domain.repository

import com.infinity.devtools.model.sqlite.MysqlConn
import kotlinx.coroutines.flow.Flow

typealias MysqlConns = List<MysqlConn>
typealias MysqlConnsFlow = Flow<MysqlConns>

/**
 * Local database connections repository
 */
interface MysqlConnRepo {
    /**
     * Get all connections from database
     *
     * @return A [MysqlConnsFlow] holding all [MysqlConn]
     */
    fun getMysqlConnsFromRoom() : MysqlConnsFlow

    /**
     * Get a single database connection by its id
     *
     * @param id Id of database connection
     * @return  [MysqlConn] or Null if connection not found
     */
    fun getMysqlConnFromRoom(id: Int) : MysqlConn?

    /**
     * Get a single database connection by its host, port and user name
     *
     * @param host  Connection host
     * @param port  Connection port
     * @param user  Connection user
     * @return [MysqlConn] or Null if connection not found
     */
    fun getMysqlConnFromRoom(host: String, port: Int, user: String) : MysqlConn?

    /**
     * Add a new database connection to database
     *
     * @param conn [MysqlConn] to be added
     */
    fun addMysqlConnToRoom(conn: MysqlConn)

    /**
     * Update a exiting database connection
     *
     * @param conn [MysqlConn] to be updated
     */
    fun updateMysqlConnInRoom(conn: MysqlConn)

    /**
     * Delete a exiting database connection
     *
     * @param conn [MysqlConn] to be deleted
     */
    fun deleteMysqlConnFromRoom(conn: MysqlConn)
}