@file:Suppress("FunctionName")

package com.infinity.devtools.ui.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Constraints
import com.airbnb.lottie.compose.*
import com.infinity.devtools.ui.components.*
import com.infinity.devtools.ui.components.sharedelement.*
import com.infinity.devtools.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterialApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    var someVar : Boolean = false

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

    // ============== CREATING IMPLEMENTATION OF THE SHARED ELEMENTS TRANSITION =================
    @Composable
    fun RootScreen() {
        var currentScreen by remember { mutableStateOf("Screen1") }

        SharedElRoot(
            screenKey = currentScreen
        ) {
            if (currentScreen == "Screen1") {
                Screen1 {
                    currentScreen = "Screen2"
                }
            } else {
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
        SharedEl(
            key = "el1",
            screenKey = "Screen 1"
        ) {
            Text(
                modifier = Modifier.clickable { navigateScreen2() },
                text = "Screen 1"
            )
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
            SharedEl(
                key = "el1",
                screenKey = "Screen 2"
            ) {
                Text(
                    modifier = Modifier.clickable { navigateScreen1() },
                    text = "Screen 2"
                )
            }

            Text(
                modifier = Modifier.clickable { contentText = "Some new Text" },
                text = contentText
            )
        }
    }

}


// ================== SHARED EL ROOT ===================
@Composable
fun SharedElRoot(
    screenKey: String,
    content: @Composable () -> Unit
) {
    val rootState = remember { SharedElRootState() }

    if (rootState.getCurrentScreenKey() == null) {
        // Initialize for the first time the initial screen key
        rootState.setCurrentScreenKey(screenKey)
    }

    CompositionLocalProvider(
        LocalSharedElsRootState provides rootState
    ) {
        content()
    }
}

@Composable
fun SharedEl(
    key: String,
    screenKey: String,
    content: @Composable () -> Unit
) {
    // State of the SharedElRoot composable
    val rootState = LocalSharedElsRootState.current

    var isStartEl by remember { mutableStateOf(false) }

    // val offset = remember { Animatable(Offset(0f, 0f), Offset.VectorConverter) }

    // Element info that will be animated
    var sharedElInfo = rootState.getSharedElement(id = SharedElId(key, screenKey))

    // Log.d("TAG", "before($key - $screenKey): ${sharedElInfo}")

    if (sharedElInfo == null) {
        // Add shared element for the first time only
        sharedElInfo = SharedElInfo(
            key = key,
            screenKey = screenKey,
            isAnimationRunning = false
        )
    }

    // Register a observer that listen when a startElement bounds are set and endElement is present and its bounds is set to begin the transition
    rootState.setSharedElementObserver(sharedElInfo.id, object : SharedElObserver {
        override fun changed(mIsStartEl: Boolean, mElInfo: SharedElInfo) {
            isStartEl = mIsStartEl
            Log.d("TAG", "($key - $screenKey) isStartEl: $mIsStartEl, changed: $mElInfo")
        }
    })

    // Log.d("TAG", "composed($key - $screenKey): ${sharedElInfo}")

    // Register the current element that will be used to create the transition
    rootState.registerSharedElement(sharedElInfo)

    val modifier = Modifier.onGloballyPositioned { coordinates ->
        // Update the current element bounds
        val mSharedElInfo = rootState.getSharedElement(id = sharedElInfo.id)

        // Log.d("TAG", "onGloballyPositioned($key - $screenKey): ${mSharedElInfo}")

        if (mSharedElInfo != null) {
            rootState.registerSharedElement(mSharedElInfo.copy(boundsInRoot = coordinates.positionInRoot()))
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // Update the current element as disposed
            val mSharedElInfo = rootState.getSharedElement(id = sharedElInfo.id)
            // Log.d("TAG", "onDispose($key - $screenKey): ${mSharedElInfo}")
            if (mSharedElInfo != null) {
                rootState.registerSharedElement(mSharedElInfo.copy(isDisposed = true))
            }
        }
    }

    // Content wrapped by this composable SharedEl
    Layout(content, modifier) { measurables, constraints ->
        if (measurables.size > 1) {
            throw IllegalStateException("SharedElement can have only one direct measurable child!")
        }

        val placeable = measurables.firstOrNull()?.measure(
            Constraints(
                minWidth = 0,
                minHeight = 0,
                maxWidth = constraints.maxWidth,
                maxHeight = constraints.maxHeight
            )
        )

        val width = min(max(constraints.minWidth, placeable?.width ?: 0), constraints.maxWidth)
        val height = min(max(constraints.minHeight, placeable?.height ?: 0), constraints.maxHeight)

        layout(width, height) {
            placeable?.place(0, 0)
        }
    }
}

private class SharedElRootState {
    private val sharedElements = mutableListOf<SharedElInfo>()
    private var startScreenKey : String? = null
    private var endScreenKey : String? = null
    private var currentScreenKey : String? = null
    private val sharedElementsObservers = mutableMapOf<SharedElId, SharedElObserver>()
    private var screenChangeObserver : ((screen: String) -> Unit)? = null

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
        sharedElements.forEachIndexed { index, it ->
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
                // Element is not present in sharedElements registered list add it for the first time
                sharedElements.add(sharedElInfo)
                // Notify observer
                notifyObserver(sharedElInfo.id, sharedElInfo)
            } else {
                // Start element already exists update it's information
                sharedElements[elExistsPos] = sharedElInfo
                // Notify start observer
                notifyObserver(sharedElInfo.id, sharedElInfo)
                if (sharedElInfo.endElement != null) {
                    // Notify end observer
                    notifyObserver(sharedElInfo.endElement!!.id, sharedElInfo.endElement!!)
                }
            }
        } else {
            // Start element found, register current element inside the start element as a end element
            startEl!!.endElement = sharedElInfo
            sharedElements[startElPos] = startEl!!
            // Notify start observer
            notifyObserver(startEl!!.id, startEl!!)
            // Notify end observer
            notifyObserver(sharedElInfo.id, sharedElInfo)
        }

        if (!isAnimationRunning()) {
            // If no animation in progress
            if (startScreenKey == null) {
                // Assign start screen key to first shared element added
                startScreenKey = sharedElInfo.screenKey
            } else if (sharedElInfo.screenKey != startScreenKey && endScreenKey == null) {
                // If start screen key already defined, a second screen was opened and it's shared element is being registered
                // assign the screen key to the first shared element of the second screen
                endScreenKey = sharedElInfo.screenKey
            }
        }
    }

//    /**
//     * Unregister the start or end shared element information
//     *
//     * @param sharedElInfo  Shared element info. Can be a start or a end element information.
//     */
//    fun unregisterSharedElement(sharedElInfo : SharedElInfo) {
//        for (i in sharedElements.indices) {
//            if (sharedElements[i].key == sharedElInfo.key && sharedElements[i].screenKey == sharedElInfo.screenKey) {
//                sharedElements.removeAt(i)
//                break
//            }
//        }
//    }

    /**
     * Set or unset a shared element observer
     *
     * @param id         Identification of the shared element, [SharedElId] that contains key and screen key
     * of the shared element being observed
     * @param observer   Observer that will listen for the shared element changes
     */
    fun setSharedElementObserver(id: SharedElId, observer : SharedElObserver?) {
        if (observer != null) {
            // Put or update observer
            sharedElementsObservers[id] = observer
        } else {
            // Remove observer
            sharedElementsObservers.remove(id)
        }
    }

//    /**
//     * Used to set a single observer to the shared elements transition screen state.
//     *
//     * @param observer  Screen change observer
//     */
//    fun setScreenChangeObserver(observer: (screen: String) -> Unit) {
//        screenChangeObserver = observer
//    }

    /**
     * Set current screen key
     *
     * @param screenKey     Current screen key
     */
    fun setCurrentScreenKey(screenKey: String) {
        this.currentScreenKey = screenKey
    }

    /**
     * Get current screen key
     */
    fun getCurrentScreenKey() = currentScreenKey

    /**
     * Search for a shared element information using it's identifier [id]
     *
     * @param id    Identifier of the shared element information
     * @return      Element information or NULL if not found
     */
    fun getSharedElement(id: SharedElId) : SharedElInfo? {
        for(i in sharedElements.indices) {
            val sharedElAt = sharedElements[i]
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

//    /**
//     * Get all shared elements for a given screen key
//     *
//     * @param screenKey Screen key
//     */
//    fun getSharedElements(screenKey: String) : List<SharedElInfo> {
//        return sharedElements.filter {
//            it.screenKey == screenKey
//        }
//    }

    /**
     * Checks if at least one shared element is in transition, since each shared element can have it's own transition
     * running for different times independent of each other.
     *
     * @return True=At least one shared element transition is in progress, False=No shared element transition running
     */
    fun isAnimationRunning() : Boolean {
        val animationRunningFound = sharedElements.firstOrNull {
            it.isAnimationRunning
        }
        return animationRunningFound != null
    }

    /**
     * Notify a shared element observer if it exists, that it's state changed
     *
     * @param id        Id of the observer
     * @param elInfo    New information of the shared element info that changed
     */
    private fun notifyObserver(id: SharedElId, elInfo: SharedElInfo) {
        sharedElementsObservers[id]?.changed(elInfo.endElement != null, elInfo)
    }

    override fun toString(): String {
        return "SharedElRootState(sharedElements=$sharedElements, startScreenKey=$startScreenKey, endScreenKey=$endScreenKey, isAnimationRunning=${isAnimationRunning()})"
    }
}

private val LocalSharedElsRootState = staticCompositionLocalOf<SharedElRootState> {
    error("SharedElementsRoot not found. SharedElement must be hosted in SharedElementsRoot.")
}

// ====================== MODELS =======================
/**
 * Holds all shared element information
 *
 * @property key                    Key that identifies the shared element
 * @property screenKey              Screen key that identifies the shared element start or end transitions
 * @property isAnimationRunning     True=Element is in transition to start/end element state, False=No transition running
 * @property endElement             NotNull=End transition element of the start element, Null=No end element registered or found
 * @property boundsInRoot           Shared element bounds relative to root element
 * @property isDisposed             Track if the screen of this element is open or closed
 * @property id                     Shared element information id, used in observer
 * Changes when a new screen is composed with shared elements on it.
 */
private data class SharedElInfo(
    val key: String,
    val screenKey: String,
    var isAnimationRunning : Boolean,
    var endElement : SharedElInfo? = null,
    var boundsInRoot : Offset? = null,
    var isDisposed: Boolean = false,
    val id : SharedElId = SharedElId(key, screenKey),
) {

    override fun toString(): String {
        return "SharedElInfo(key='$key', screenKey='$screenKey', isAnimationRunning=$isAnimationRunning, endElement=$endElement, boundsInRoot=$boundsInRoot, isDisposed=$isDisposed, id=$id)"
    }
}

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
}

/**
 * Listener used to observe a shared element state changes such as
 * a end element registered for a start element, transition is running.
 */
private interface SharedElObserver {
    /**
     * Fired when:
     * - A end shared element is registered for a start shared element;
     * - The transition state, running and not running changes;
     *
     * @param mIsStartEl True the shared element is the transition start
     * @param mElInfo    The shared element information
     */
    fun changed(mIsStartEl : Boolean, mElInfo : SharedElInfo)
}
// ====================== MODELS =======================

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