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
import com.infinity.devtools.ui.presentation.EditMysqlConnScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel that handles local database connections
 *
 * @property repo       Repository
 * @property resProv    Resources provider used to get resources
 * @property validator  Validator used to validate mysql fields
 */
@HiltViewModel
class MysqlConnVm @Inject constructor(
    private val repo: MysqlConnRepo,
    private val resProv: ResourcesProvider,
    private val validator: MysqlValidator
): ViewModel() {
    var mysqlConn by mutableStateOf(EMPTY_CONN)

    var errDialogOpen = mutableStateOf(false)
    var errDialogMsg by mutableStateOf("")

    val connections = repo.getMysqlConnsFromRoom()

    /**
     * Get a connection by it's id to be edited in [EditMysqlConnScreen]
     *
     * @param id    Connection id being edited
     */
    @Suppress("unused")
    fun getConn(id: Int) = viewModelScope.launch(Dispatchers.IO) {
        mysqlConn = repo.getMysqlConnFromRoom(id) ?: mysqlConn
    }

    /**
     * Set a connection info to be edited in [EditMysqlConnScreen]
     *
     * @param conn  Connection info being edited
     */
    fun setConn(conn: MysqlConn?) {
        mysqlConn = conn ?: EMPTY_CONN
    }

    /**
     * Save a new connection information in database. Uses [errDialogMsg] and [errDialogOpen] to show
     * warnings if some is found.
     *
     * @param conn  Connection information
     */
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

    /**
     * Update a existing connection information in database. Uses [errDialogMsg] and [errDialogOpen] to show
     * warnings if some is found.
     *
     * @param conn
     */
    fun updateConn(conn: MysqlConn) = viewModelScope.launch(Dispatchers.IO) {
        try {
            if (validateFields(conn)) {
                repo.updateMysqlConnInRoom(conn)
            }
        } catch (e: SQLiteConstraintException) {
            showErrorDialog(e.message ?: "")
        }
    }

    /**
     * Deletes an existing connection information in database.
     *
     * @param conn
     */
    fun deleteConn(conn: MysqlConn) = viewModelScope.launch(Dispatchers.IO) {
        repo.deleteMysqlConnFromRoom(conn)
    }

    // ================================ VALIDATORS ===================================
    /**
     * Validate a connection information if it's valid. Uses [errDialogMsg] and [errDialogOpen] to show
     * warnings if some is found.
     *
     * @param conn Connection information to be validated
     * @return true=Valid, false=Invalid
     */
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

    /**
     * Shows a error dialog with a given message
     *
     * @param errMsg  Error message to be shown
     */
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

    companion object {
        val EMPTY_CONN = MysqlConn(0, NO_VALUE, NO_VALUE, -1, NO_VALUE, NO_VALUE, NO_VALUE)
    }
}