@file:Suppress("FunctionName")

package com.infinity.devtools.ui.presentation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.infinity.devtools.ui.vm.MysqlConnVm

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MysqlConnsScreen(
    viewModel: MysqlConnVm = hiltViewModel(),
    navigateToUpdateMysqlConnScreen: (connId: Int) -> Unit,
    navigateToInsertMysqlConnScreen: () -> Unit
) {
    val connections by viewModel.connections.collectAsState(
        initial = emptyList()
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = navigateToInsertMysqlConnScreen,
                backgroundColor = Color.Blue,
                content = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            )
        },
        content = {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(connections) {
                    Row {
                        Column {
                            Text(text = it.dbname, style = typography.h6)
                            Text(text = "VIEW DETAIL", style = typography.caption)
                        }
                    }
                }
            }
        }
    )
}