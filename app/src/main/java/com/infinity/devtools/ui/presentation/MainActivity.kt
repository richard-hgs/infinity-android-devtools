@file:Suppress("FunctionName")

package com.infinity.devtools.ui.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import com.airbnb.lottie.compose.*
import com.infinity.devtools.ui.components.*
import com.infinity.devtools.ui.components.sharedelement.*
import com.infinity.devtools.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.max
import kotlin.math.min

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

    // ============== CREATING IMPLEMENTATION OF THE SHARED ELEMENTS TRANSITION =================
    @Composable
    fun RootScreen() {
        var currentScreen by remember { mutableStateOf("Screen1") }

        SharedElRoot(
            screenKey = currentScreen
        ) {
            val rootState = LocalSharedElsRootState.current
            curMillis = System.currentTimeMillis()
            DelayExit(visible = (rootState.previousScreenKey != rootState.currentScreenKey && rootState.previousScreenKey == "Screen1") || rootState.currentScreenKey == "Screen1") {
                Screen1 {
                    currentScreen = "Screen2"
                }
            }
            DelayExit(visible = (rootState.previousScreenKey != rootState.currentScreenKey && rootState.previousScreenKey == "Screen2") || rootState.currentScreenKey == "Screen2") {
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
    var sharedElements: List<SharedElInfo> by remember { mutableStateOf(emptyList()) }

    if (rootState.currentScreenKey == null) {
        // Initialize for the first time the initial screen key
        rootState.currentScreenKey = screenKey
        // Initialize for the first time the previous screen key to the initial screen key
        rootState.previousScreenKey = screenKey
    }

    rootState.setSharedElementsRootObserver {
        // Triggered every time a shared element changes
        val sharedElState = rootState.sharedElements.firstOrNull {
            it.isAnimationRunning
        }

        if (sharedElState == null && rootState.transitionLock) {
            // Remove transition lock since all animations finished
            rootState.transitionLock = false
            // All shared elements animation finished. Notify that transition is not running
            rootState.transitionRunning = false
            // Assign current screen as the previous screen.
            // When they are equal no transition direction can be guessed, that means we are in idle state.
            rootState.previousScreenKey = screenKey
        }

        sharedElements = rootState.sharedElements
    }

    // Detect screen changed
    LaunchedEffect(screenKey) {
        if (screenKey != rootState.previousScreenKey) {
            // Screen change detected, start all shared elements transition
            rootState.currentScreenKey = screenKey
        }
    }

    CompositionLocalProvider(
        LocalSharedElsRootState provides rootState
    ) {
        // Wrap screen content on a box to fill max size to allow transition of shared elements
        // start in any position of entire screen
        Box(modifier = Modifier.fillMaxSize()) {
            content()
            for (i in sharedElements.indices) {
                val mSharedElInfoAt = sharedElements[i]
                mSharedElInfoAt.transitionContent()
            }
        }
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

    // Shared el info id
    val id = SharedElId(key, screenKey)

    // Element info that will be animated
    var sharedElInfo : SharedElInfo? by remember { mutableStateOf(rootState.getSharedElement(id)) }

    var visible : Boolean by remember { mutableStateOf(false) }

    val alpha: Float by animateFloatAsState(if (visible) 1f else .0f)

    if (sharedElInfo == null) {
        // Add shared element for the first time only
        sharedElInfo = SharedElInfo(
            key = key,
            screenKey = screenKey,
            transitionContent = @Composable { SharedElTransition(id = id, content = content) },
            isAnimationRunning = false
        )
        // Register the current element that will be used to create the transition
        rootState.registerSharedElement(sharedElInfo!!)
    }

    // Set shared element observer at position 0
    rootState.setSharedElementObserver(id = id, 0, object : SharedElObserver {
        override fun changed(mIsStartEl: Boolean, mElInfo: SharedElInfo) {
            sharedElInfo = mElInfo
        }
    })

    val modifier = Modifier.onPlaced { coordinates ->
        // Update the current element bounds
        val mSharedElInfo = rootState.getSharedElement(id = id)
        if (mSharedElInfo != null) {
            rootState.registerSharedElement(mSharedElInfo.copy(boundsInRoot = coordinates.positionInRoot(), isDisposed = false))
        }
    }.alpha(alpha)

    LaunchedEffect(sharedElInfo) {
        visible = sharedElInfo != null && !sharedElInfo!!.isAnimationRunning
    }

    DisposableEffect(Unit) {
        onDispose {
            // Update the current element as disposed
            val mSharedElInfo = rootState.getSharedElement(id = id)
            // Log.d("TAG", "onDispose($key - $screenKey): ${mSharedElInfo}")
            if (mSharedElInfo != null) {
                rootState.registerSharedElement(mSharedElInfo.copy(isDisposed = true))
            }
        }
    }

    // Show content only when animation is not running, if not the start element keeps on screen,
    // when this same start element is transitioning to end position element.
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

        val width =
            min(max(constraints.minWidth, placeable?.width ?: 0), constraints.maxWidth)
        val height =
            min(max(constraints.minHeight, placeable?.height ?: 0), constraints.maxHeight)

        layout(width, height) {
            placeable?.place(0, 0)
        }
    }
}

@Composable
private fun SharedElTransition(
    id: SharedElId,
    content: @Composable () -> Unit
) {
    // State of the SharedElRoot composable
    val rootState = LocalSharedElsRootState.current

    // var startSharedElInfo : SharedElInfo? by remember { mutableStateOf(rootState.getSharedElement()) }
    var sharedElInfo : SharedElInfo? by remember { mutableStateOf(rootState.getSharedElement(id)) }

    // Register a observer that listen when a startElement bounds are set and endElement is present and its bounds is set to begin the transition
    rootState.setSharedElementObserver(id, 1, object : SharedElObserver {
        override fun changed(mIsStartEl: Boolean, mElInfo: SharedElInfo) {
            Log.d("TAG", "changeds: ${mElInfo}")
            sharedElInfo = mElInfo
        }
    })

    var offset by remember { mutableStateOf(
        Animatable(Offset(
            sharedElInfo?.boundsInRoot?.x ?: 0f,
            sharedElInfo?.boundsInRoot?.y ?: 0f
        ), Offset.VectorConverter)
    )}

    LaunchedEffect(sharedElInfo, rootState.transitionRunning, rootState.currentScreenKey, rootState.previousScreenKey) {
        if (
            rootState.currentScreenKey != null &&
            rootState.previousScreenKey != null &&
            rootState.currentScreenKey != rootState.previousScreenKey &&
            sharedElInfo?.boundsInRoot != null &&
            sharedElInfo?.endElement?.boundsInRoot != null
        ) {
            // Screen transition is in progress
            if (
                sharedElInfo?.endElement?.screenKey == rootState.currentScreenKey
            ) {
                // Is start element
                // Target element of current start/end shared element is found. Perform animation transition to target position
                if (sharedElInfo?.isAnimationRunning == false) {
                    // Set offset only for the first time
                    offset = Animatable(
                        Offset(
                            sharedElInfo?.boundsInRoot?.x ?: 0f,
                            sharedElInfo?.boundsInRoot?.y ?: 0f
                        ), Offset.VectorConverter
                    )
                }
                val mSharedElInfo = rootState.getSharedElement(id)
                if (mSharedElInfo != null) {
                    // Begin start element transition
                    rootState.registerSharedElement(mSharedElInfo.copy(isAnimationRunning = true))
                    rootState.registerSharedElement(mSharedElInfo.endElement!!.copy(isAnimationRunning = true))
                    offset.animateTo(
                        targetValue = Offset(sharedElInfo!!.endElement!!.boundsInRoot!!.x, sharedElInfo!!.endElement!!.boundsInRoot!!.y),
                        tween(1000)
                    )
                    // Element transition finished
                    rootState.registerSharedElement(mSharedElInfo.copy(isAnimationRunning = false))
                    rootState.registerSharedElement(mSharedElInfo.endElement!!.copy(isAnimationRunning = false))
                }
            } else if (
                sharedElInfo?.screenKey == rootState.currentScreenKey
            ) {
                // Is end element
                // Target element of current start/end shared element is found. Perform animation transition to target position
                if (sharedElInfo?.isAnimationRunning == false) {
                    // Set offset only for the first time
                    offset = Animatable(
                        Offset(
                            sharedElInfo?.endElement?.boundsInRoot?.x ?: 0f,
                            sharedElInfo?.endElement?.boundsInRoot?.y ?: 0f
                        ), Offset.VectorConverter
                    )
                }
                val mSharedElInfo = rootState.getSharedElement(id)
                if (mSharedElInfo != null) {
                    // Begin start element transition
                    rootState.registerSharedElement(mSharedElInfo.copy(isAnimationRunning = true))
                    rootState.registerSharedElement(mSharedElInfo.endElement!!.copy(isAnimationRunning = true))
                    offset.animateTo(
                        targetValue = Offset(sharedElInfo?.boundsInRoot!!.x, sharedElInfo?.boundsInRoot!!.y),
                        tween(1000)
                    )
                    // Element transition finished
                    rootState.registerSharedElement(mSharedElInfo.copy(isAnimationRunning = false))
                    rootState.registerSharedElement(mSharedElInfo.endElement!!.copy(isAnimationRunning = false))
                }
            }
        }
    }

    if (
        // Transition is in progress
        sharedElInfo?.isAnimationRunning == true
    ) {
        Box(modifier = Modifier.offset {
            IntOffset(offset.value.x.toInt(), offset.value.y.toInt())
        }) {
            content()
            // Notify transition later to fix blink in composables when transition starts
            if (!rootState.transitionRunning) rootState.transitionRunning = true
        }
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

private enum class DelayExitState {
    Invisible, Visible, ExitDelayed
}

private class SharedElRootState {
    val sharedElements = mutableListOf<SharedElInfo>()
    var previousScreenKey by mutableStateOf<String?>(null)
    var currentScreenKey by mutableStateOf<String?>(null)
    private val sharedElementsObservers = mutableMapOf<SharedElId, MutableMap<Int, SharedElObserver>>()
    private var sharedElementsRootObserver : (() -> Unit)? = null
    private var screenChangeObserver : ((screen: String) -> Unit)? = null
    var transitionRunning by mutableStateOf(false)
    // Lock some events when it's false, this means we are waiting for at least one shared element transition to begin,
    // before checking some logics. The lock is released only when all shared elements transitions finishes
    var transitionLock by mutableStateOf(false)

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

        if (sharedElInfo.isAnimationRunning) {
            // Acquire animation lock
            transitionLock = true
        }

        if (startEl == null) {
            // No start element found, register current element as the start element
            if (elExists == null) {
                // Element is not present in sharedElements registered list add it for the first time
                sharedElements.add(sharedElInfo)
                // Notify observer
                notifyObserver(sharedElInfo.id, sharedElInfo)
                // Notify root observer
                notifyRootObserver()
            } else {
                // Start element already exists update it's information
                sharedElements[elExistsPos] = sharedElInfo
                // Notify start observer
                notifyObserver(sharedElInfo.id, sharedElInfo)
                if (sharedElInfo.endElement != null) {
                    // Notify end observer
                    notifyObserver(sharedElInfo.endElement!!.id, sharedElInfo.endElement!!)
                }
                // Notify root observer
                notifyRootObserver()
            }
        } else {
            // Start element found, register current element inside the start element as a end element
            startEl!!.endElement = sharedElInfo
            sharedElements[startElPos] = startEl!!
            // Notify start observer
            notifyObserver(startEl!!.id, startEl!!)
            // Notify end observer
            notifyObserver(sharedElInfo.id, sharedElInfo)
            // Notify root observer
            notifyRootObserver()
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
     * @param observerId Observer id for a [SharedElId] since one element can be observed in multiple locations, and only once
     * this [observerId] must be unique for a given [id]
     * @param observer   Observer that will listen for the shared element changes
     */
    fun setSharedElementObserver(id: SharedElId, observerId: Int, observer : SharedElObserver?) {
        if (observer != null) {
            // Put or update observer
            if (sharedElementsObservers[id] == null) {
                sharedElementsObservers[id] = mutableMapOf()
                Log.d("TAG", "resetObservers")
            }
            sharedElementsObservers[id]?.set(observerId, observer)
        } else {
            // Remove observer
            sharedElementsObservers[id]?.remove(observerId)
        }
    }

    /**
     * Used by [SharedElRoot] to observe all changes of all shared elements
     *
     * @param observer  Observer of all shared elements changes
     */
    fun setSharedElementsRootObserver(observer: (() -> Unit)?) {
        sharedElementsRootObserver = observer
    }

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
        sharedElementsObservers[id]?.forEach {
            it.value.changed(elInfo.endElement != null, elInfo)
        }
        // sharedElementsObservers[id]?.changed(elInfo.endElement != null, elInfo)
    }

    private fun notifyRootObserver() {
        if (sharedElementsRootObserver != null) {
            sharedElementsRootObserver!!()
        }
    }

    override fun toString(): String {
        return "SharedElRootState(sharedElements=$sharedElements, isAnimationRunning=${isAnimationRunning()})"
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
    val transitionContent : @Composable () -> Unit,
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

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + screenKey.hashCode()
        return result
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