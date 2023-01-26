@file:Suppress("FunctionName")

package com.infinity.devtools.ui.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.room.Room
import com.infinity.devtools.R
import com.infinity.devtools.constants.ConstantsDb.TABLE_MYSQL_CONN
import com.infinity.devtools.di.validators.MysqlValidator
import com.infinity.devtools.domain.database.AppDatabase
import com.infinity.devtools.domain.database.MysqlConnDao_Impl
import com.infinity.devtools.domain.repository.MysqlConnRepoImpl
import com.infinity.devtools.domain.resources.ResourcesProvider
import com.infinity.devtools.domain.resources.ResourcesProviderImpl
import com.infinity.devtools.ui.components.AppTextField
import com.infinity.devtools.ui.components.ColumnScrollbar
import com.infinity.devtools.ui.components.ProgressButton
import com.infinity.devtools.ui.components.WarningDialog
import com.infinity.devtools.ui.vm.MysqlConnVm
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonState
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NewMysqlConnScreen(
    viewModel: MysqlConnVm = hiltViewModel(),
    navigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val spacing = 4.dp
    val scrollState = rememberScrollState()
    var submitButtonState by remember { mutableStateOf(SSButtonState.IDLE) }

    Column(
        modifier = Modifier.padding(all = 8.dp)
    ) {
        ColumnScrollbar(
            modifier = Modifier.fillMaxWidth()
                .weight(1f),
            state = scrollState
        ) { modifier ->
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_mysql),
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth(fraction = 0.7f)
                        .align(alignment = Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(height = spacing))
                AppTextField(
                    modifier = Modifier.fillMaxWidth(),
                    text = viewModel.mysqlConn.name,
                    placeholder = stringResource(R.string.conn_name),
                    onChange = {
                        viewModel.updateName(it)
                    },
                    imeAction = ImeAction.Next, // Show next as IME button
                    keyboardType = KeyboardType.Text, // Plain text keyboard
                    keyBoardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        }
                    ),
                    maxLength = 50
                )
                Spacer(modifier = Modifier.height(height = spacing))
                AppTextField(
                    modifier = Modifier.fillMaxWidth(),
                    text = viewModel.mysqlConn.host,
                    placeholder = stringResource(R.string.conn_host),
                    onChange = {
                        viewModel.updateHost(it)
                    },
                    imeAction = ImeAction.Next, // Show next as IME button
                    keyboardType = KeyboardType.Text, // Plain text keyboard
                    keyBoardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        }
                    ),
                    maxLength = 255
                )
                Spacer(modifier = Modifier.height(height = spacing))
                AppTextField(
                    modifier = Modifier.fillMaxWidth(),
                    text = viewModel.mysqlConn.port.toString(),
                    placeholder = stringResource(R.string.conn_port),
                    onChange = {
                        viewModel.updatePort(it)
                    },
                    imeAction = ImeAction.Next, // Show next as IME button
                    keyboardType = KeyboardType.Text, // Plain text keyboard
                    keyBoardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        }
                    ),
                    maxLength = 5
                )
                Spacer(modifier = Modifier.height(height = spacing))
                AppTextField(
                    modifier = Modifier.fillMaxWidth(),
                    text = viewModel.mysqlConn.user,
                    placeholder = stringResource(R.string.conn_user),
                    onChange = {
                        viewModel.updateUser(it)
                    },
                    imeAction = ImeAction.Next, // Show next as IME button
                    keyboardType = KeyboardType.Text, // Plain text keyboard
                    keyBoardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        }
                    ),
                    maxLength = 255
                )
                Spacer(modifier = Modifier.height(height = spacing))
                AppTextField(
                    modifier = Modifier.fillMaxWidth(),
                    text = viewModel.mysqlConn.pass,
                    placeholder = stringResource(R.string.conn_pass),
                    onChange = {
                        viewModel.updatePass(it)
                    },
                    imeAction = ImeAction.Next, // Show next as IME button
                    keyboardType = KeyboardType.Password, // Plain text keyboard
                    keyBoardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        }
                    ),
                    maxLength = 255
                )
                Spacer(modifier = Modifier.height(height = spacing))
                AppTextField(
                    modifier = Modifier.fillMaxWidth(),
                    text = viewModel.mysqlConn.dbname,
                    placeholder = stringResource(R.string.conn_dbname),
                    onChange = {
                        viewModel.updateDbName(it)
                    },
                    imeAction = ImeAction.Next, // Show next as IME button
                    keyboardType = KeyboardType.Text, // Plain text keyboard
                    keyBoardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        }
                    ),
                    maxLength = 255
                )
            }
        }
        Spacer(modifier = Modifier.height(height = spacing))
        ProgressButton(
            type = SSButtonType.CIRCLE,
            onClick = {
                coroutineScope.launch {
                    // Perform action on click of button and make it's state to LOADING
                    submitButtonState = SSButtonState.LOADING

                    lateinit var job : Job

                    if (viewModel.mysqlConn.id == 0) {
                        // Insert this new connection
                        job = viewModel.addConn(conn = viewModel.mysqlConn)
                    } else {
                        // Update connection
                        job = viewModel.updateConn(conn = viewModel.mysqlConn)
                    }
                    // Suspend parent coroutine until job is done
                    job.join()

                    // Notify job result
                    val errDialogOpen = viewModel.errDialogOpen.value
                    submitButtonState = if (errDialogOpen) SSButtonState.FAILIURE else SSButtonState.SUCCESS
                    // Wait for job progress show
                    delay(1000)
                    if (!errDialogOpen) {
                        // Navigate back
                        navigateBack()
                    }
                }
            },
            assetColor = Color.White,
            buttonState = submitButtonState,
            setButtonState = { submitButtonState = it },
            text = stringResource(R.string.save_conn),
        )
    }
    WarningDialog(
        open = viewModel.errDialogOpen,
        msg = viewModel.errDialogMsg
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewNewMysqlConnScreen() {
    NewMysqlConnScreen(
        viewModel = MysqlConnVm(
            MysqlConnRepoImpl(
                MysqlConnDao_Impl(
                    Room.databaseBuilder(LocalContext.current, AppDatabase::class.java, TABLE_MYSQL_CONN).build()
                )
            ),
            ResourcesProviderImpl(LocalContext.current),
            MysqlValidator()
        ),
        navigateBack = {

        }
    )
}