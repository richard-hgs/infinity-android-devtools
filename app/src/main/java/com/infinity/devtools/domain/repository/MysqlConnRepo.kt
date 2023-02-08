package com.infinity.devtools.domain.repository

import com.infinity.devtools.model.sqlite.MysqlConn
import kotlinx.coroutines.flow.Flow

typealias MysqlConns = List<MysqlConn>

interface MysqlConnRepo {

    fun getMysqlConnsFromRoom() : Flow<MysqlConns>

    fun getMysqlConnFromRoom(id: Int) : MysqlConn?

    fun getMysqlConnFromRoom(host: String, port: Int, user: String) : MysqlConn?

    fun addMysqlConnToRoom(conn: MysqlConn)

    fun updateMysqlConnInRoom(conn: MysqlConn)

    fun deleteMysqlConnFromRoom(conn: MysqlConn)
}