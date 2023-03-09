@file:Suppress("FunctionName")

package com.infinity.devtools.ui.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.infinity.devtools.ui.components.*
import com.infinity.devtools.ui.components.sharedelement.*
import com.infinity.devtools.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

var curMillis: Long = System.currentTimeMillis()

@OptIn(ExperimentalMaterialApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                RootScreen()
//                NavGraph(
//                    navController = rememberAnimatedNavController()
//                )
            }
        }
    }

    @Preview
    @Composable
    fun Preview() {
        RootScreen()
    }

    // ============== CREATING IMPLEMENTATION OF THE SHARED ELEMENTS TRANSITION =================
    @Composable
    fun RootScreen() {
        var currentScreen by remember { mutableStateOf("Screen1") }

        SharedElRoot(
            screenKey = currentScreen
        ) {
            curMillis = System.currentTimeMillis()
            DelayExit(visible = currentScreen == "Screen1") {
                Screen1 {
                    currentScreen = "Screen2"
                }
            }
            DelayExit(visible = currentScreen == "Screen2") {
                Screen2 {
                    currentScreen = "Screen1"
                }
            }
        }
    }

    @Composable
    fun Screen1(
        navigateScreen2: () -> Unit
    ) {
        Column {
            Text(
                modifier = Modifier.clickable { navigateScreen2() },
                text = "Screen 1"
            )
            SharedEl(
                key = "el1",
                screenKey = "Screen1"
            ) {
                Text(
                    text = "Some Text"
                )
            }
            SharedEl(
                key = "el2",
                screenKey = "Screen1"
            ) {
                Text(
                    text = "Some Text 2"
                )
            }
        }
    }

    @Composable
    fun Screen2(
        navigateScreen1: () -> Unit
    ) {
        var contentText by remember { mutableStateOf("Some Text") }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                modifier = Modifier.clickable { navigateScreen1() },
                text = "Screen 2"
            )
            SharedEl(
                key = "el1",
                screenKey = "Screen2"
            ) {
                Text(
                    modifier = Modifier.clickable { contentText = "Some new Text" },
                    text = contentText
                )
            }
            Divider(modifier = Modifier.height(50.dp))
            SharedEl(
                key = "el2",
                screenKey = "Screen2"
            ) {
                Text(
                    text = "Some Text 2"
                )
            }
        }
    }
}

@Composable
fun SharedElRoot(
    screenKey: String,
    content: @Composable () -> Unit
) {
    val rootState = remember { SharedElRootState() }

    if (rootState.curScreen == null) {
        rootState.prevScreen = screenKey
    }

    rootState.curScreen = screenKey

    if (rootState.prevScreen == null) {
        rootState.prevScreen = screenKey
    }

    if (rootState.curScreen != rootState.prevScreen) {
        rootState.transitionRunning = true
    }

    CompositionLocalProvider(
        LocalSharedElsRootState provides rootState
    ) {
        // Wrap screen content on a box to fill max size to allow transition of shared elements
        // start in any position of entire screen
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }

        rootState.transitionElements.forEach {
            SharedElTransition(it.id)
        }
    }
}

@Composable
private fun SharedElTransition(
    id: SharedElId
) {
    val rootState = LocalSharedElsRootState.current

    val transAlpha = if(rootState.transitionRunning) 1f else 0f
    var offsetAnim: Animatable<Offset, AnimationVector2D>? by remember { mutableStateOf(null) }

    LaunchedEffect(rootState.transitionElements) {
        val elInfo = rootState.getSharedElement(id)
        if (elInfo?.offset != null) {
            offsetAnim = Animatable(Offset(
                elInfo.offset?.x ?: 0f,
                elInfo.offset?.y ?: 0f,
            ), Offset.VectorConverter)
        }
    }

    LaunchedEffect(
        rootState.transitionRunning,
        rootState.transitionElements
    ) {
        if (offsetAnim != null && !offsetAnim!!.isRunning) {
            val elInfo = rootState.getSharedElement(id)
            if (elInfo?.endElement != null) {
                var targetOffset = elInfo.offset
                var destOffset = elInfo.endElement!!.offset
                if (rootState.curScreen == elInfo.screenKey) {
                    targetOffset = elInfo.endElement!!.offset
                    destOffset = elInfo.offset
                }
                if (targetOffset != null && destOffset != null && elInfo.content != null && rootState.transitionRunning) {
                    // offsetAnim.snapTo(Offset(targetOffset.x, targetOffset.y))
                    offsetAnim!!.animateTo(
                        targetValue = Offset(
                            destOffset.x,
                            destOffset.y
                        ),
                        animationSpec = tween(1000),
                    )
                    rootState.prevScreen = rootState.curScreen
                    rootState.transitionRunning = false
                }
            }
        }
    }

    Box(modifier = Modifier.alpha(transAlpha).offset {
        IntOffset(offsetAnim?.value?.x?.toInt() ?: 0, offsetAnim?.value?.y?.toInt() ?: 0)
    }) {
        val elInfo = rootState.getSharedElement(id)
        if (elInfo != null) {
            elInfo.content?.let { it() }
        }
    }
}

@Composable
fun SharedEl(
    key: String,
    screenKey: String,
    content: @Composable () -> Unit
) {
    val rootState = LocalSharedElsRootState.current
    val alpha = if(!rootState.transitionRunning && rootState.curScreen == screenKey) 1f else 0f
    val id = SharedElId(key = key, screenKey = screenKey)
    val elInfo = rootState.getSharedElement(id)

    if (elInfo == null) {
        rootState.registerSharedElement(SharedElInfo(key = key, screenKey = screenKey, content = content))
    }

    LaunchedEffect(Unit) {
        val mElInfo = rootState.getSharedElement(id)
        if (mElInfo != null) {
            rootState.registerSharedElement(mElInfo.copy(content = content))
        }
    }

    Box(modifier = Modifier.alpha(alpha) /*.background(if (screenKey == "Screen1") Color.Blue else Color.Red)*/ .onPlaced { coordinates ->
        val mElInfo = rootState.getSharedElement(id)
        if (mElInfo != null) {
            rootState.registerSharedElement(mElInfo.copy(offset = coordinates.positionInRoot()))
        }
    }) {
        content()
    }
}

@Composable
fun DelayExit(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    val rootState = LocalSharedElsRootState.current

    var state by remember { mutableStateOf(DelayExitState.Invisible) }

    when (state) {
        DelayExitState.Invisible -> {
            if (visible) state = DelayExitState.Visible
        }
        DelayExitState.Visible -> {
            if (!visible) {
                state = if (rootState.transitionRunning) DelayExitState.ExitDelayed else DelayExitState.Invisible
            }
        }
        DelayExitState.ExitDelayed -> {
            if (!rootState.transitionRunning) state = DelayExitState.Invisible
        }
    }

    if (state != DelayExitState.Invisible) content()
}


private class SharedElRootState {
    var prevScreen: String? by mutableStateOf(null)
    var curScreen: String? by mutableStateOf(null)
    var transitionRunning: Boolean by mutableStateOf(false)
    var transitionElements = mutableStateListOf<SharedElInfo>()

    /**
     * Register all information of a shared element.
     * The information is needed to animate the shared element
     * Don't forget to unregister the shared element will not be used anymore
     *
     * @param sharedElInfo  Shared element info. Can be a start or a end element information.
     */
    fun registerSharedElement(sharedElInfo : SharedElInfo) {
        // NotNull=Shared element is already registered, Null=New shared element being registered
        var elExists : SharedElInfo? = null
        var elExistsPos = 0
        // Null=Shared element start, NotNull=Parent of the Shared element end
        var startEl : SharedElInfo? = null
        var startElPos = 0
        transitionElements.forEachIndexed { index, it ->
            if (sharedElInfo.key == it.key && sharedElInfo.screenKey == it.screenKey) {
                // Element is already registered
                elExists = it
                elExistsPos = index
            }
            if (sharedElInfo.key == it.key && sharedElInfo.screenKey != it.screenKey) {
                // Start element of the current end element found
                startEl = it
                startElPos = index
            }
        }

        if (startEl == null) {
            // No start element found, register current element as the start element
            if (elExists == null) {
                // Element for key doesn't exists add for the first time as a start element
                transitionElements.add(sharedElInfo)
            } else {
                // Start element already exists update it's information
                transitionElements[elExistsPos] = sharedElInfo
            }
        } else {
            // Start element found, register current element inside the start element as a end element
            startEl!!.endElement = sharedElInfo
            transitionElements[startElPos] = startEl!!
        }
    }

    /**
     * Search for a shared element information using it's identifier [id]
     *
     * @param id    Identifier of the shared element information
     * @return      Element information or NULL if not found
     */
    fun getSharedElement(id: SharedElId) : SharedElInfo? {
        for(i in transitionElements.indices) {
            val sharedElAt = transitionElements[i]
            if (sharedElAt.id == id) {
                // Check if element being searched is the start element
                return sharedElAt
            } else if (sharedElAt.endElement != null && sharedElAt.endElement!!.id == id) {
                // If not start element, checks if it's the end element
                return sharedElAt.endElement!!
            }
        }
        // Element not found return null
        return null
    }
}

private val LocalSharedElsRootState = staticCompositionLocalOf<SharedElRootState> {
    error("SharedElementsRoot not found. SharedElement must be hosted in SharedElementsRoot.")
}

private data class SharedElInfo(
    var key: String,
    var screenKey: String,
    var content: (@Composable () -> Unit)?,
    var offset: Offset? = null,
    var endElement: SharedElInfo? = null,
    var id: SharedElId = SharedElId(key, screenKey)
)

private class SharedElId(
    val key : String,
    val screenKey : String
) {
    override fun toString(): String {
        return "SharedElId(key='$key', screenKey='$screenKey')"
    }

    /**
     * Overriding equals allow us to compare [SharedElId] == [SharedElId] with different hash codes,
     * but with same attributes.
     */
    override fun equals(other: Any?): Boolean {
        other as SharedElId
        if (key != other.key) return false
        if (screenKey != other.screenKey) return false
        return true
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + screenKey.hashCode()
        return result
    }
}

private enum class DelayExitState {
    Invisible, Visible, ExitDelayed
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