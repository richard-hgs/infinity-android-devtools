@file:Suppress("FunctionName")

package com.infinity.devtools.ui.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.room.Room
import com.airbnb.lottie.compose.*
import com.infinity.devtools.R
import com.infinity.devtools.constants.ConstantsDb
import com.infinity.devtools.data.model.MysqlConn
import com.infinity.devtools.di.validators.MysqlValidator
import com.infinity.devtools.domain.database.AppDatabase
import com.infinity.devtools.domain.database.MysqlConnDao_Impl
import com.infinity.devtools.domain.repository.MysqlConnRepoImpl
import com.infinity.devtools.domain.resources.ResourcesProviderImpl
import com.infinity.devtools.ui.components.AppSurface
import com.infinity.devtools.ui.components.AppTopBar
import com.infinity.devtools.ui.vm.MysqlConnVm

@Composable
fun ConnListItem(
    conn: MysqlConn,
    navigateToUpdateMysqlConnScreen: (connId: Int) -> Unit,
) {
    Row {
        AppSurface(
            modifier = Modifier.fillMaxWidth()
                .padding(bottom = 4.dp),
            elevation = 4.dp,
            onClick = {
                navigateToUpdateMysqlConnScreen(conn.id)
            },
            indication = rememberRipple(color = MaterialTheme.colors.primarySurface)
        ) {
            Row(
                modifier = Modifier.padding(all = 4.dp)
            ) {
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.ic_server_on))
                val progress by animateLottieCompositionAsState(
                    composition = composition,
                    speed = 2f,
                    iterations = LottieConstants.IterateForever
                )
                LottieAnimation(
                    modifier = Modifier.size(48.dp),
                    composition = composition,
                    progress = { progress },
                )
                Column {
                    Text(text = conn.name, style = typography.h6)
                    Text(text = "${conn.host}:${conn.port}", style = typography.caption)
                }
            }
        }
    }
}

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
        topBar = {
             AppTopBar()
        },
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
        content = { paddingValues ->
            LazyColumn(
                    contentPadding = PaddingValues(
                        start = paddingValues.calculateStartPadding(LocalLayoutDirection.current) + 8.dp,
                        top = paddingValues.calculateTopPadding() + 8.dp,
                        end = paddingValues.calculateEndPadding(LocalLayoutDirection.current) + 8.dp,
                        bottom = paddingValues.calculateBottomPadding() + 8.dp
                    )
                ) {
                    items(connections) { connAt ->
                        ConnListItem(
                            conn = connAt,
                            navigateToUpdateMysqlConnScreen = navigateToUpdateMysqlConnScreen
                        )
                    }
                }
            }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewMysqlConnScreen() {
    MysqlConnsScreen(
        viewModel = MysqlConnVm(
            MysqlConnRepoImpl(
                MysqlConnDao_Impl(
                    Room.databaseBuilder(LocalContext.current, AppDatabase::class.java, ConstantsDb.TABLE_MYSQL_CONN).build()
                )
            ),
            ResourcesProviderImpl(LocalContext.current),
            MysqlValidator()
        ),
        navigateToInsertMysqlConnScreen = {

        },
        navigateToUpdateMysqlConnScreen = {

        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewConnListItem() {
    Column {
        ConnListItem(
            conn = MysqlConn(
                id = 1,
                name = "Conn Name",
                host = "hostname.com",
                port = 8080,
                user = "User Name",
                pass = "My Password",
                dbname = "My dbname"
            ),
            navigateToUpdateMysqlConnScreen = {}
        )
    }
}