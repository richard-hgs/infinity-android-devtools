package com.infinity.devtools.domain.repository

import com.infinity.devtools.data.model.MysqlConn
import com.infinity.devtools.domain.database.MysqlConnDao

class MysqlConnRepoImpl(
    private val connDao : MysqlConnDao
) : MysqlConnRepo {
    override fun getMysqlConnsFromRoom() = connDao.getConns()

    override fun getMysqlConnFromRoom(id: Int) = connDao.getConn(id)

    override fun getMysqlConnFromRoom(host: String, port: Int, user: String) = connDao.getConn(host, port, user)

    override fun addMysqlConnToRoom(conn: MysqlConn) = connDao.addConn(conn)

    override fun updateMysqlConnInRoom(conn: MysqlConn) = connDao.updateConn(conn)

    override fun deleteMysqlConnFromRoom(conn: MysqlConn) = connDao.deleteConn(conn)
}