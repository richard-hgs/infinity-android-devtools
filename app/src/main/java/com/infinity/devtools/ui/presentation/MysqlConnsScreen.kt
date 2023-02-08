@file:Suppress("FunctionName")

package com.infinity.devtools.ui.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.room.Room
import com.airbnb.lottie.compose.*
import com.infinity.devtools.R
import com.infinity.devtools.constants.ConstantsDb
import com.infinity.devtools.model.sqlite.MysqlConn
import com.infinity.devtools.di.validators.MysqlValidator
import com.infinity.devtools.domain.database.AppDatabase
import com.infinity.devtools.domain.database.MysqlConnDao_Impl
import com.infinity.devtools.domain.repository.MysqlConnRepoImpl
import com.infinity.devtools.domain.resources.ResourcesProviderImpl
import com.infinity.devtools.ui.components.AppSurface
import com.infinity.devtools.ui.components.AppTopBar
import com.infinity.devtools.ui.components.WarningDialog
import com.infinity.devtools.ui.vm.MysqlConnVm
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ConnListItem(
    conn: MysqlConn,
    navigateToUpdateMysqlConnScreen: (connId: Int) -> Unit,
    navigateToMysqlDbHomeScreen: (connId: Int) -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberDismissState(
        confirmStateChange = {
            var handled = false
            if (it == DismissValue.DismissedToStart) {
                onDelete()
                handled = true
            } else if (it == DismissValue.DismissedToEnd) {
                handled = false
            }
            handled
        }
    )

    Row {
        SwipeToDismiss(
            state = dismissState,
            dismissThresholds = { FractionalThreshold(0.2f) },
            background = {
                val direction = dismissState.dismissDirection ?: return@SwipeToDismiss

                val color by animateColorAsState(
                    targetValue = when (dismissState.targetValue) {
                        DismissValue.Default -> Color.LightGray
                        DismissValue.DismissedToEnd -> Color(0x220000FF)
                        DismissValue.DismissedToStart -> Color(0x44FF0000)
                    }
                )

                val icon = when(direction) {
                    DismissDirection.StartToEnd -> LottieCompositionSpec.RawRes(R.raw.ic_info_blue)
                    DismissDirection.EndToStart -> LottieCompositionSpec.RawRes(R.raw.ic_delete_red_2)
                }

                val scale by animateFloatAsState(
                    targetValue = if (dismissState.targetValue == DismissValue.Default) 0.8f else 1.2f
                )

                val alignment = when(direction) {
                    DismissDirection.EndToStart -> Alignment.CenterEnd
                    DismissDirection.StartToEnd -> Alignment.CenterStart
                }

                Box(
                    modifier = Modifier.fillMaxHeight(.94f)
                        .fillMaxWidth()
                        .background(color)
                        .padding(start = 12.dp, end = 12.dp),
                    contentAlignment = alignment
                ) {
                    val composition by rememberLottieComposition(icon)
                    val progress by animateLottieCompositionAsState(
                        composition = composition,
                        speed = 2f,
                        iterations = LottieConstants.IterateForever
                    )
                    LottieAnimation(
                        modifier = Modifier.size(36.dp)
                            .scale(scale),
                        composition = composition,
                        progress = { progress },
                    )
                }
            }
        ) {
            AppSurface(
                modifier = Modifier.fillMaxWidth()
                    .padding(bottom = 4.dp),
                elevation = 4.dp,
                onClick = {
                    navigateToMysqlDbHomeScreen(conn.id)
                },
                indication = rememberRipple(color = MaterialTheme.colors.primarySurface)
            ) {
                Row(
                    modifier = Modifier.padding(all = 4.dp)
                ) {
                    val composition1 by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.ic_server_on))
                    val progress1 by animateLottieCompositionAsState(
                        composition = composition1,
                        speed = 2f,
                        iterations = LottieConstants.IterateForever
                    )
                    LottieAnimation(
                        modifier = Modifier.size(48.dp),
                        composition = composition1,
                        progress = { progress1 },
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = conn.name, style = typography.h6)
                            Text(text = "${conn.host}:${conn.port}", style = typography.caption)
                        }
                        AppSurface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            indication = rememberRipple(color = MaterialTheme.colors.primarySurface),
                            content = {
                                val composition2 by rememberLottieComposition(
                                    LottieCompositionSpec.RawRes(
                                        R.raw.ic_edit_2
                                    )
                                )
                                val progress2 by animateLottieCompositionAsState(
                                    composition = composition2,
                                    speed = 2f,
                                    iterations = 2
                                )
                                LottieAnimation(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .rotate(90f)
                                        .graphicsLayer(
                                            scaleX = 1.3f,
                                            scaleY = 1.3f,
                                        ),
                                    composition = composition2,
                                    progress = { progress2 },
                                    contentScale = ContentScale.Crop
                                )
                            },
                            onClick = {
                                navigateToUpdateMysqlConnScreen(conn.id)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MysqlConnsScreen(
    viewModel: MysqlConnVm = hiltViewModel(),
    navigateToUpdateMysqlConnScreen: (connId: Int) -> Unit,
    navigateToInsertMysqlConnScreen: () -> Unit,
    navigateToMysqlDbHomeScreen: (connId: Int) -> Unit,
) {
    val coroutine = rememberCoroutineScope()
    val connections by viewModel.connections.collectAsState(
        initial = emptyList()
    )

    val scaffoldState = rememberScaffoldState()

    val strNItemsDeleted = stringResource(R.string.n_items_deleted)
    val strUndo = stringResource(R.string.undo)

    Scaffold(
        scaffoldState = scaffoldState,
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
            ConstraintLayout(
                modifier = Modifier.fillMaxSize().padding(
                    paddingValues = PaddingValues(
                        start = paddingValues.calculateStartPadding(LocalLayoutDirection.current) + 8.dp,
                        top = paddingValues.calculateTopPadding() + 8.dp,
                        end = paddingValues.calculateEndPadding(LocalLayoutDirection.current) + 8.dp,
                        bottom = paddingValues.calculateBottomPadding() + 8.dp
                    )
                ),
            ) {
                val (listRef, imgEmptyRef, tvEmptyRef) = createRefs()
                createVerticalChain(imgEmptyRef, tvEmptyRef, chainStyle = ChainStyle.Packed)

                LazyColumn(
                    modifier = Modifier.constrainAs(listRef) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                        height = Dimension.fillToConstraints
                        width = Dimension.fillToConstraints
                    },
                ) {
                    items(connections) { connAt ->
                        ConnListItem(
                            conn = connAt,
                            navigateToUpdateMysqlConnScreen = navigateToUpdateMysqlConnScreen,
                            navigateToMysqlDbHomeScreen = navigateToMysqlDbHomeScreen,
                            onDelete = {
                                // Delete the connection
                                viewModel.deleteConn(connAt)

                                // Offer a way to undo operation
                                coroutine.launch {
                                    val snackResult = scaffoldState.snackbarHostState.showSnackbar(
                                        message = "1 $strNItemsDeleted",
                                        actionLabel = strUndo,
                                        duration = SnackbarDuration.Long
                                    )

                                    if (snackResult == SnackbarResult.ActionPerformed) {
                                        // Undo deletions
                                        viewModel.undoDeletion(connAt)
                                    }
                                }
                            }
                        )
                    }
                }

                if (connections.isEmpty()) {
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.ic_canada_empty_state))
                    val progress by animateLottieCompositionAsState(
                        composition = composition,
                        speed = 2f,
                        iterations = LottieConstants.IterateForever
                    )
                    LottieAnimation(
                        modifier = Modifier
                            .width(200.dp)
                            .height(150.dp)
                            .constrainAs(imgEmptyRef) {
                                top.linkTo(parent.top)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                                bottom.linkTo(tvEmptyRef.top)
                        },
                        composition = composition,
                        progress = { progress },
                    )
                    Text(
                        modifier = Modifier.constrainAs(tvEmptyRef) {
                            top.linkTo(imgEmptyRef.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            bottom.linkTo(parent.bottom)
                        },
                        text = stringResource(R.string.conn_list_empty),
                        color = Color.Gray
                    )
                }
            }
        }
    )
    WarningDialog(
        open = viewModel.errDialogOpen,
        msg = viewModel.errDialogMsg
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
        navigateToInsertMysqlConnScreen = {},
        navigateToUpdateMysqlConnScreen = {},
        navigateToMysqlDbHomeScreen = {}
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
            navigateToUpdateMysqlConnScreen = {},
            navigateToMysqlDbHomeScreen = {},
            onDelete = {}
        )
    }
}