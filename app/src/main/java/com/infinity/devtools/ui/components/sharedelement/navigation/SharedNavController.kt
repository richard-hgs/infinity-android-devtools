package com.infinity.devtools.ui.components.sharedelement.navigation

import android.content.Context
import android.net.http.SslCertificate.restoreState
import android.os.Bundle
import androidx.annotation.CallSuper
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
    val currentDestination = mutableStateOf(SharedNavDestination.EMPTY)

    var graph: SharedNavGraph? = null
        set(value) {
            field = value
            initParams()
        }

    /**
     * Initializes the [currentDestination] to the [SharedNavGraph.startDestinationRoute] for the first time graph is set
     */
    private fun initParams() {
        if (currentDestination.value == SharedNavDestination.EMPTY) {
            val mGraph = safeGetGraph()
            val startDestination = findDestination(mGraph.startDestinationRoute)

            if (startDestination != null) {
                currentDestination.value = startDestination
            }
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

        return bundle
    }

    /**
     * Restores all navigation controller state from a bundle. This should be called before any
     * call to set [graph].
     *
     * State may be saved to a bundle by calling [saveState].
     * Restoring controller state is the responsibility of a [SharedElementsNavHost].
     *
     * @param navState state bundle to restore
     */
    @CallSuper
    fun restoreState(navState: Bundle?) {

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
