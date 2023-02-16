package com.infinity.devtools.ui.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infinity.devtools.domain.odbc.MysqlDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by richard on 13/02/2023 20:29
 *
 * Model for server connection
 */
@HiltViewModel
class ServerConnVm @Inject constructor(
    private val mysqlDao: MysqlDao
) : ViewModel() {

    private val coroutineExceptionHandler = CoroutineExceptionHandler{ _, throwable ->
        throwable.printStackTrace()
    }

    fun getTables() = viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
        val tableInfoList = mysqlDao.getTables()
        tableInfoList.forEach {
            Log.d("ServerConnVm", "tableInfo: ${it.tableName}")
        }
    }
}