package com.infinity.devtools.ui.vm

import android.database.sqlite.SQLiteConstraintException
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infinity.devtools.R
import com.infinity.devtools.constants.Constants.NO_VALUE
import com.infinity.devtools.di.validators.MysqlValidator
import com.infinity.devtools.domain.repository.MysqlConnRepo
import com.infinity.devtools.domain.resources.ResourcesProvider
import com.infinity.devtools.model.sqlite.MysqlConn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MysqlConnVm @Inject constructor(
    private val repo: MysqlConnRepo,
    private val resProv: ResourcesProvider,
    private val validator: MysqlValidator
): ViewModel() {
    var mysqlConn by mutableStateOf(MysqlConn(0, NO_VALUE, NO_VALUE, -1, NO_VALUE, NO_VALUE, NO_VALUE))

    var errDialogOpen = mutableStateOf(false)
    var errDialogMsg by mutableStateOf("")

    val connections = repo.getMysqlConnsFromRoom()

    @Suppress("unused")
    fun getConn(id: Int) = viewModelScope.launch(Dispatchers.IO) {
        mysqlConn = repo.getMysqlConnFromRoom(id) ?: mysqlConn
    }

    fun setConn(conn: MysqlConn) {
        mysqlConn = conn
    }

    fun addConn(conn: MysqlConn) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val exists = repo.getMysqlConnFromRoom(conn.host, conn.port, conn.user)
            if (exists == null) {
                if (validateFields(conn)) {
                    repo.addMysqlConnToRoom(conn)
                }
            } else {
                showErrorDialog(resProv.getString(R.string.err_host_port_and_user_exists))
            }
        } catch (e: SQLiteConstraintException) {
            showErrorDialog(e.message ?: "")
        }
    }

    fun updateConn(conn: MysqlConn) = viewModelScope.launch(Dispatchers.IO) {
        try {
            if (validateFields(conn)) {
                repo.updateMysqlConnInRoom(conn)
            }
        } catch (e: SQLiteConstraintException) {
            showErrorDialog(e.message ?: "")
        }
    }

    fun deleteConn(conn: MysqlConn) = viewModelScope.launch(Dispatchers.IO) {
        repo.deleteMysqlConnFromRoom(conn)
    }

    // ================================ VALIDATORS ===================================
    private fun validateFields(conn: MysqlConn) : Boolean {
        if (!validator.connNameIsValid(conn.name)) {
            showErrorDialog(resProv.getString(R.string.err_conn_name_field_required))
            return false
        }
        if (!validator.connHostIsValid(conn.host)) {
            showErrorDialog(resProv.getString(R.string.err_conn_host_field_required))
            return false
        }
        if (!validator.connPortIsValid(conn.port)) {
            showErrorDialog(resProv.getString(R.string.err_conn_port_field_required))
            return false
        }
        if (!validator.connUserIsValid(conn.user)) {
            showErrorDialog(resProv.getString(R.string.err_conn_user_field_required))
            return false
        }
        if (!validator.connPassIsValid(conn.pass)) {
            showErrorDialog(resProv.getString(R.string.err_conn_pass_field_required))
            return false
        }
        if (!validator.connDbnameIsValid(conn.dbname)) {
            showErrorDialog(resProv.getString(R.string.err_conn_dbname_field_required))
            return false
        }
        return true
    }

    private fun showErrorDialog(errMsg: String) {
        errDialogMsg = errMsg
        errDialogOpen.value = true
    }

    // ================================ GETTERS AND SETTERS ===================================

    fun updateName(name: String) {
        mysqlConn = mysqlConn.copy(
            name = name
        )
    }

    fun updateHost(host: String) {
        mysqlConn = mysqlConn.copy(
            host = host
        )
    }

    @kotlin.jvm.Throws(NumberFormatException::class)
    fun updatePort(port: String) {
        try {
            mysqlConn = mysqlConn.copy(
                port = port.ifEmpty {
                    "-1"
                }.toInt()
            )
        } catch (e: NumberFormatException) {
            // Unhandled
        }
    }

    fun updateUser(user: String) {
        mysqlConn = mysqlConn.copy(
            user = user
        )
    }

    fun updatePass(pass: String) {
        mysqlConn = mysqlConn.copy(
            pass = pass
        )
    }

    fun updateDbName(dbname: String) {
        mysqlConn = mysqlConn.copy(
            dbname = dbname
        )
    }

    fun undoDeletion(conn: MysqlConn) {
        addConn(conn)
    }
}