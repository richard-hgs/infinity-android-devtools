package com.infinity.devtools.domain.repository

import com.infinity.devtools.domain.database.MysqlConnDao
import com.infinity.devtools.model.sqlite.MysqlConn

/**
 * Local database connections repository
 *
 * @property connDao [MysqlConnDao] instance used to run the queries
 */
class MysqlConnRepoImpl(
    private val connDao : MysqlConnDao
) : MysqlConnRepo {
    /**
     * Get all connections from database
     *
     * @return A [MysqlConnsFlow] holding all [MysqlConn]
     */
    override fun getMysqlConnsFromRoom() = connDao.getConns()

    /**
     * Get a single database connection by its id
     *
     * @param id Id of database connection
     * @return  [MysqlConn] or Null if connection not found
     */
    override fun getMysqlConnFromRoom(id: Int) = connDao.getConn(id)

    /**
     * Get a single database connection by its host, port and user name
     *
     * @param host  Connection host
     * @param port  Connection port
     * @param user  Connection user
     * @return [MysqlConn] or Null if connection not found
     */
    override fun getMysqlConnFromRoom(host: String, port: Int, user: String) = connDao.getConn(host, port, user)

    /**
     * Add a new database connection to database
     *
     * @param conn [MysqlConn] to be added
     */
    override fun addMysqlConnToRoom(conn: MysqlConn) = connDao.addConn(conn)

    /**
     * Update a exiting database connection
     *
     * @param conn [MysqlConn] to be updated
     */
    override fun updateMysqlConnInRoom(conn: MysqlConn) = connDao.updateConn(conn)

    /**
     * Delete a exiting database connection
     *
     * @param conn [MysqlConn] to be deleted
     */
    override fun deleteMysqlConnFromRoom(conn: MysqlConn) = connDao.deleteConn(conn)
}