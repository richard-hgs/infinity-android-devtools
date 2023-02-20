@file:Suppress("FunctionName")

package com.infinity.devtools.ui.components.sharedelement.navigation

import android.content.Context
import android.content.res.Resources.NotFoundException
import android.net.http.SslCertificate.restoreState
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext

/**
 * Created by richard on 19/02/2023 14:51
 *
 */
class SharedNavController(context: Context) {
    val currentDestination = mutableStateOf(SharedNavStackDest.EMPTY)

    private val backQueue: ArrayDeque<SharedNavStackDest> = ArrayDeque()

    var graph: SharedNavGraph? = null
        set(value) {
            field = value
            initParams()
        }

    /**
     * Initializes the [currentDestination] to the [SharedNavGraph.startDestinationRoute] for the first time graph is set
     */
    private fun initParams() {
        if (currentDestination.value == SharedNavStackDest.EMPTY) {
            val mGraph = safeGetGraph()

            if (backQueue.isEmpty()) {
                // Initialize for the first time the navigation stack, by setting the start destination
                val startDestination = findDestination(mGraph.startDestinationRoute)
                if (startDestination != null) {
                    backQueue.addLast(SharedNavStackDest(startDestination.route))
                } else {
                    throw NotFoundException("Start destination route \"${mGraph.startDestinationRoute}\" not found.")
                }
            }

            // Setup current destination to last destination from the back queue
            currentDestination.value = backQueue.last()
        }
    }

    /**
     * Find a destination inside the current navigation graph
     *
     * @param route The destination route being searched
     * @return  Null or the destination found
     */
    private fun findDestination(route: String) : SharedNavDestination? {
        return safeGetGraph().destinations.find {
            it.route == route
        }
    }

    /**
     * Get graph with null safety
     *
     * @return NonNull Graph
     * @throws IllegalArgumentException if graph is not initialized
     */
    private fun safeGetGraph() : SharedNavGraph {
        if (graph != null) {
            return graph!!
        } else {
            throw IllegalArgumentException("graph is not initialized.")
        }
    }

    /**
     * Navigate to the given route destination
     *
     * @param route Route name
     */
    fun navigate(route: String) {
        val destination = findDestination(route)
        if (destination != null) {
            backQueue.addLast(SharedNavStackDest(destination.route))
            currentDestination.value = backQueue.last()
        } else {
            throw NotFoundException("The route name \"$route\" could not be found.")
        }
    }

    /**
     * Attempts to pop the controller's back stack. Analogous to when the user presses
     * the system [Back][android.view.KeyEvent.KEYCODE_BACK] button when the associated
     * navigation host has focus.
     *
     * @return true if the stack was popped at least once and the user has been navigated to
     * another destination, false otherwise
     */
    @MainThread
    fun popBackStack(): Boolean {
        return if (backQueue.isEmpty()) {
            // Nothing to pop if the back stack is empty
            false
        } else {
            // Pop backQueue and set the current destination to the previous one
            backQueue.removeLast()
            currentDestination.value = backQueue.last()
            true
        }
    }

    /**
     * Get content for destination
     *
     * @param route Route to get composable content
     * @return      Composable content to be shown
     */
    fun getContentForDestination(route: String) : @Composable () -> Unit {
        val destination = findDestination(route)
        return destination!!.content
    }

    /**
     * Saves all navigation controller state to a Bundle.
     *
     * State may be restored from a bundle returned from this method by calling
     * [restoreState]. Saving controller state is the responsibility
     * of a [SharedElementsNavHost].
     *
     * @return saved state for this controller
     */
    @CallSuper
    fun saveState(): Bundle {
        val bundle = Bundle()
        bundle.putParcelableArray("back_queue", backQueue.toTypedArray())
        return bundle
    }

    /**
     * Restores all navigation controller state from a bundle. This should be called before any
     * call to set [graph].
     *
     * State may be saved to a bundle by calling [saveState].
     * Restoring controller state is the responsibility of a [SharedElementsNavHost].
     *
     * @param savedState state bundle to restore
     */
    @CallSuper
    @Suppress("DEPRECATION")
    fun restoreState(savedState: Bundle?) {
        if (savedState != null) {
            val savedQueue = savedState.getParcelableArray("back_queue")
            if (savedQueue != null) {
                for (queue in savedQueue) {
                    backQueue.addLast(queue as SharedNavStackDest)
                }
            }
        }
    }
}

@Composable
fun rememberSharedNavController() : SharedNavController {
    val context = LocalContext.current
    val savedNavController = rememberSaveable(saver = SharedNavControllerSaver(context)) {
        createSharedNavController(context)
    }

    return savedNavController
}

private fun createSharedNavController(context: Context) =
    SharedNavController(context)

/**
 * Saver to save and restore the NavController across config change and process death.
 */
private fun SharedNavControllerSaver(
    context: Context
): Saver<SharedNavController, *> = Saver<SharedNavController, Bundle>(
    save = { it.saveState() },
    restore = { createSharedNavController(context).apply { restoreState(it) } }
)
