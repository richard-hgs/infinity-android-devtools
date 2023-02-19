@file:Suppress("FunctionName")

package com.infinity.devtools.ui.presentation

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.*
import com.infinity.devtools.R
import com.infinity.devtools.model.sqlite.MysqlConn
import com.infinity.devtools.ui.components.AppTextField
import com.infinity.devtools.ui.components.AppTopBar
import com.infinity.devtools.ui.components.sharedelement.*
import com.infinity.devtools.ui.vm.MysqlConnVm
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalMaterialApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

//    @OptIn(ExperimentalAnimationApi::class)
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        setContent {
//            AppTheme {
//                NavGraph(
//                    navController = rememberAnimatedNavController()
//                )
//            }
//        }
//    }

    companion object {
        private const val ListScreen = "list"
        private const val DetailsScreen = "details"

        private const val TransitionDurationMillis = 1000

        private val FadeOutTransitionSpec = MaterialContainerTransformSpec(
            durationMillis = TransitionDurationMillis,
            fadeMode = FadeMode.Out
        )
        private val CrossFadeTransitionSpec = SharedElementsTransitionSpec(
            durationMillis = TransitionDurationMillis,
            fadeMode = FadeMode.Cross,
            fadeProgressThresholds = ProgressThresholds(0.10f, 0.40f)
        )
        private val MaterialFadeInTransitionSpec = MaterialContainerTransformSpec(
            pathMotionFactory = MaterialArcMotionFactory,
            durationMillis = TransitionDurationMillis,
            fadeMode = FadeMode.In
        )
        private val MaterialFadeOutTransitionSpec = MaterialContainerTransformSpec(
            pathMotionFactory = MaterialArcMotionFactory,
            durationMillis = TransitionDurationMillis,
            fadeMode = FadeMode.Out
        )
    }

    private var selectedConn: MysqlConn? by mutableStateOf(null)
    private lateinit var scope: SharedElementsRootScope

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent(null) {
            MaterialTheme(
                colors = if (isSystemInDarkTheme()) darkColors() else lightColors()
            ) {
                SharedNavRoot()
            }
        }
    }

    override fun onBackPressed() {
        if (selectedConn != null) {
            changeConn(null)
        } else {
            super.onBackPressed()
        }
    }

    private fun changeConn(conn: MysqlConn?) {
        val currentConn = selectedConn
        if (currentConn != conn) {
            val targetConn = conn ?: currentConn
            if (targetConn != null) {
                scope.prepareTransition("img_${targetConn.id}", "name_${targetConn.id}", "host_${targetConn.id}", "port_${targetConn.id}")
            }
            selectedConn = conn
        }
    }

    @Composable
    private fun SharedNavRoot() {
        SharedElementsRoot {
            scope = this
            val listState = rememberLazyListState()
            val composition by rememberLottieComposition(
                LottieCompositionSpec.RawRes(
                    R.raw.ic_server_on
                )
            )
            val progress by animateLottieCompositionAsState(
                composition = composition,
                speed = 2f,
                iterations = LottieConstants.IterateForever
            )
            Crossfade(
                targetState = selectedConn,
                animationSpec = tween(durationMillis = TransitionDurationMillis)
            ) { conn ->
                when (conn) {
                    null -> MysqlConnScreen(listState, composition, progress)
                    else -> EditMysqlConnScreen(conn, composition, progress)
                }
            }
        }
    }

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @Composable
    private fun MysqlConnScreen(
        listState: LazyListState,
        lottieComp: LottieComposition?,
        lottieProgress: Float,
        viewModel: MysqlConnVm = hiltViewModel()
    ) {
        val connections by viewModel.connections.collectAsState(
            initial = emptyList()
        )

        Scaffold(
            topBar = {
                AppTopBar()
            }
        ) {
            LazyColumn(state = listState) {
                items(connections) { conn ->
                    Row(
                        Modifier.fillMaxWidth()
                            .padding(8.dp)
                            .clickable(enabled = !scope.isRunningTransition) { changeConn(conn) },
                    ) {
                        SharedMaterialContainer(
                            key = "img_${conn.id}",
                            screenKey = ListScreen,
                            shape = MaterialTheme.shapes.medium,
                            color = Color.Transparent,
                            transitionSpec = FadeOutTransitionSpec
                        ) {
                            LottieAnimation(
                                modifier = Modifier.size(48.dp)
                                    .scale(1.49f),
                                composition = lottieComp,
                                progress = { lottieProgress },
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            SharedElement(
                                key = "name_${conn.id}",
                                screenKey = ListScreen,
                                transitionSpec = CrossFadeTransitionSpec
                            ) {
                                Text(text = conn.name)
                            }
                            Row {
                                SharedElement(
                                    key = "host_${conn.id}",
                                    screenKey = ListScreen,
                                    transitionSpec = CrossFadeTransitionSpec
                                ) {
                                    Text(text = conn.host)
                                }
                                SharedElement(
                                    key = "port_${conn.id}",
                                    screenKey = ListScreen,
                                    transitionSpec = CrossFadeTransitionSpec
                                ) {
                                    Text(text = ":${conn.port}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @Composable
    private fun EditMysqlConnScreen(
        conn: MysqlConn,
        lottieComp: LottieComposition?,
        lottieProgress: Float,
        viewModel: MysqlConnVm = hiltViewModel()
    ) {
        val focusRequester = FocusRequester()
        val focusManager = LocalFocusManager.current

        if (viewModel.mysqlConn.id != conn.id) {
            viewModel.setConn(conn)
        }

        Scaffold(
            topBar = {
                AppTopBar(
                    title = stringResource(R.string.title_new_mysql_conn),
                    homeIcon = Icons.Filled.ArrowBack,
                    onHomeClick = {
                        if (!scope.isRunningTransition) {
                            changeConn(null)
                        }
                    }
                )
            }
        ) { paddingVals ->

            Column(
                Modifier.padding(
                    PaddingValues(
                        start = paddingVals.calculateStartPadding(LocalLayoutDirection.current) + 8.dp,
                        top = paddingVals.calculateTopPadding() + 8.dp,
                        end = paddingVals.calculateEndPadding(LocalLayoutDirection.current) + 8.dp,
                        bottom = paddingVals.calculateBottomPadding()
                    )
                ).fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SharedMaterialContainer(
                    key = "img_${conn.id}",
                    screenKey = DetailsScreen,
                    shape = MaterialTheme.shapes.medium,
                    color = Color.Transparent,
                    elevation = 0.dp,
                    transitionSpec = FadeOutTransitionSpec
                ) {
                    LottieAnimation(
                        modifier = Modifier.size(150.dp)
                            .align(Alignment.CenterHorizontally)
                            .clickable(enabled = !scope.isRunningTransition) { changeConn(null) }
                            .scale(1.49f),
                        composition = lottieComp,
                        progress = { lottieProgress },
                    )
                }
                AppTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    text = viewModel.mysqlConn.name,
                    placeholder = stringResource(R.string.conn_name),
                    onChange = { str ->
                        viewModel.updateName(str)
                    },
                    imeAction = ImeAction.Next, // Show next as IME button
                    keyboardType = KeyboardType.Text, // Plain text keyboard
                    keyBoardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        }
                    ),
                    maxLength = 50,
                    sharedElTransitionKey = "name_${conn.id}",
                    sharedElTransitionScreenKey = DetailsScreen,
                    sharedElTransitionSpec = CrossFadeTransitionSpec,
                    sharedElTransitionEnd = !scope.isRunningTransition
                )
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
                    maxLength = 255,
                    sharedElTransitionKey = "host_${conn.id}",
                    sharedElTransitionScreenKey = DetailsScreen,
                    sharedElTransitionSpec = CrossFadeTransitionSpec,
                    sharedElTransitionEnd = !scope.isRunningTransition
                )
                AppTextField(
                    modifier = Modifier.fillMaxWidth(),
                    text = if (viewModel.mysqlConn.port != -1) viewModel.mysqlConn.port.toString() else "",
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
                    maxLength = 5,
                    sharedElTransitionKey = "port_${conn.id}",
                    sharedElTransitionScreenKey = DetailsScreen,
                    sharedElTransitionSpec = CrossFadeTransitionSpec,
                    sharedElTransitionEnd = !scope.isRunningTransition
                )
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
                AppTextField(
                    modifier = Modifier.fillMaxWidth(),
                    text = viewModel.mysqlConn.dbname,
                    placeholder = stringResource(R.string.conn_dbname),
                    onChange = {
                        viewModel.updateDbName(it)
                    },
                    imeAction = ImeAction.Done, // Show next as IME button
                    keyboardType = KeyboardType.Text, // Plain text keyboard
                    keyBoardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                        }
                    ),
                    maxLength = 255
                )
            }
        }
    }
}


//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//    AppTheme {
//        MotionLayoutTest()
//    }
//}

//@Preview(showBackground = true)
//@Composable
//fun DrawerPreview() {
//    ArchitectureProjectTheme {
//        NavigationDrawerContent(
//            navController = rememberNavController(),
//            onCloseDrawer = {}
//        )
//    }
//}
//
//@Composable
//fun Content(preview: Boolean = false) {
//    val navController = rememberNavController()
//
//    val appBarIcon by remember { mutableStateOf(Icons.Filled.Menu) }
//    val scaffoldState = rememberScaffoldState()
//    val coroutineScope = rememberCoroutineScope()
//
//    Scaffold(
//        scaffoldState = scaffoldState,
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(text = stringResource(R.string.app_name))
//                },
//                navigationIcon = {
//                    Button(
//                        content = {
//                            Icon(
//                                imageVector = appBarIcon,
//                                contentDescription = stringResource(R.string.ic_menu)
//                            )
//                        },
//                        onClick = {
//                            coroutineScope.launch {
//                                if (scaffoldState.drawerState.isOpen) {
//                                    scaffoldState.drawerState.close()
//                                } else {
//                                    scaffoldState.drawerState.open()
//                                }
//                            }
//                        },
//                        colors = ButtonDefaults.buttonColors(
//                            backgroundColor = Color.Transparent,
//                            contentColor = Color.White
//                        ),
//                        elevation = ButtonDefaults.elevation(
//                            defaultElevation = 0.dp,
//                            pressedElevation = 0.dp,
//                            hoveredElevation = 0.dp,
//                            focusedElevation = 0.dp
//                        ),
//                    )
//                }
//            )
//        },
//        drawerContent = {
//            NavigationDrawerContent(
//                navController = navController,
//                onCloseDrawer = {
//                    coroutineScope.launch {
//                        // delay for the ripple effect
//                        delay(timeMillis = 250)
//                        scaffoldState.drawerState.close()
//                    }
//                }
//            )
//        },
//        content = {
//            Column(
//                modifier = Modifier.fillMaxSize()
//            ) {
//                if (preview) {
//                    HomeScreen(navController = navController)
//                } else {
//                    NavHost(
//                        navController = navController,
//                        startDestination = ConstantsScreen.MYSQL_CONNS_SCREEN
//                    ) {
//                        composable(ConstantsScreen.MYSQL_CONNS_SCREEN) { HomeScreen(navController = navController) }
//                        composable(ConstantsScreen.SETTINGS_SCREEN) { SettingsScreen(navController = navController) }
//                    }
//                }
//            }
//        }
//    )
//}
//
//@Composable
//private fun NavigationDrawerContent(
//    navController: NavController,
//    onCloseDrawer: () -> Unit
//) {
//    val itemsList = prepareNavigationDrawerItems()
//
//    LazyColumn(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(
//                brush = Brush.verticalGradient(
//                    colors = listOf(
//                        Color(0xFF5D28FF),
//                        Color(0xFFBA94FF)
//                    )
//                )
//            ),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 16.dp)
//    ) {
//        item {
//            // settings icon
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//            ) {
//                TransparentButton(
//                    modifier = Modifier
//                        .align(Alignment.TopEnd),
//                    onClick = {
//                        navController.navigate(ConstantsScreen.SETTINGS_SCREEN)
//                        onCloseDrawer()
//                    }
//                ) {
//                    Icon(
//                        modifier = Modifier
//                            .size(28.dp),
//                        imageVector = Icons.Filled.Settings,
//                        contentDescription = stringResource(R.string.ic_settings),
//                        tint = Color.White
//                    )
//                }
//            }
//
//            // user's image
//            Icon(
//                modifier = Modifier
//                    .size(size = 120.dp)
//                    .clip(shape = CircleShape),
//                imageVector = Icons.Filled.Person,
//                contentDescription = stringResource(R.string.ic_profile),
//                tint = Color.White
//            )
//
//            // user's name
//            Text(
//                modifier = Modifier
//                    .padding(top = 12.dp),
//                text = stringResource(R.string.app_name),
//                fontSize = 26.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color.White
//            )
//
//            // user's email
//            Text(
//                modifier = Modifier.padding(top = 8.dp, bottom = 30.dp),
//                text = stringResource(R.string.app_email),
//                fontWeight = FontWeight.Normal,
//                fontSize = 16.sp,
//                color = Color.White
//            )
//        }
//
//        items(itemsList) { item ->
//            NavigationListItem(item = item) {
//                // itemClick(item.label)
//            }
//        }
//    }
//}
//
//@Composable
//private fun NavigationListItem(
//    item: NavigationDrawerItem,
//    unreadBubbleColor: Color = Color(0xFF0FFF93),
//    itemClick: () -> Unit
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable {
//                itemClick()
//            }
//            .padding(horizontal = 24.dp, vertical = 10.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        // icon and unread bubble
//        Box {
//            Icon(
//                modifier = Modifier
//                    .padding(all = if (item.showUnreadBubble && item.label == "Messages") 5.dp else 2.dp)
//                    .size(size = if (item.showUnreadBubble && item.label == "Messages") 24.dp else 28.dp),
//                painter = item.image,
//                contentDescription = null,
//                tint = Color.White
//            )
//
//            // unread bubble
//            if (item.showUnreadBubble) {
//                Box(
//                    modifier = Modifier
//                        .size(size = 8.dp)
//                        .align(alignment = Alignment.TopEnd)
//                        .background(color = unreadBubbleColor, shape = CircleShape)
//                )
//            }
//        }
//
//        // label
//        Text(
//            modifier = Modifier.padding(start = 16.dp),
//            text = item.label,
//            fontSize = 20.sp,
//            fontWeight = FontWeight.Medium,
//            color = Color.White
//        )
//    }
//}
//
//@Composable
//private fun prepareNavigationDrawerItems(): List<NavigationDrawerItem> {
//    val itemsList = arrayListOf<NavigationDrawerItem>()
//
//    itemsList.add(
//        NavigationDrawerItem(
//            image = rememberVectorPainter(Icons.Filled.Home),
//            label = stringResource(R.string.home)
//        )
//    )
//
//    return itemsList
//}
//
//data class NavigationDrawerItem(
//    val image: Painter,
//    val label: String,
//    val showUnreadBubble: Boolean = false
//)