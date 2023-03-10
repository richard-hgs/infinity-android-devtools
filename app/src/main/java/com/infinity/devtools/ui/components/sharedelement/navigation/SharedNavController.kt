@file:Suppress("FunctionName")

package com.infinity.devtools.ui.components.sharedelement.navigation

import android.content.res.Resources.NotFoundException
import android.net.http.SslCertificate.restoreState
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import androidx.annotation.RestrictTo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.infinity.devtools.ui.components.sharedelementold.SharedElementsRootScope

class SharedNavController {
    val currentDestination = mutableStateOf(SharedNavStackDest.EMPTY)

    private val backQueue: ArrayDeque<SharedNavStackDest> = ArrayDeque()

    var graph: SharedNavGraph? = null
        set(value) {
            field = value
            initParams()
        }

    private var scope: SharedElementsRootScope? = null
    private var lifecycleOwner: LifecycleOwner? = null
    private var onBackPressedDispatcher: OnBackPressedDispatcher? = null

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                popBackStack()
            }
        }
    private var enableOnBackPressedCallback = true

    private val lifecycleObserver: LifecycleObserver = LifecycleEventObserver { _, _ ->
//        hostLifecycleState = event.targetState
//        if (_graph != null) {
//            for (entry in backQueue) {
//                entry.handleLifecycleEvent(event)
//            }
//        }
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
                    backQueue.addLast(SharedNavStackDest(startDestination.route, null, null))
                } else {
                    throw NotFoundException("Start destination route \"${mGraph.startDestinationRoute}\" not found.")
                }
            }

            // Setup current destination to last destination from the back queue
            currentDestination.value = backQueue.last()

            // Update the onBackPressed to set enabled or not.
            updateOnBackPressedCallbackEnabled()
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
     * Prepare transition before starting the cross fade transition between two screens
     * to fix scope.isRunningTransition state
     */
    private fun prepareTransition(animatedKeys : Array<Any>) {
        if (scope != null) {
            Log.d("TAG", "prepare transition")
            scope!!.prepareTransition(*animatedKeys)
        }
    }

    /**
     * Set the shared elements root scope
     *
     * @param scope Root scope of the shared elements used to animate the shared elements while navigating
     */
    fun setSharedElementsRootScope(scope: SharedElementsRootScope) {
        this.scope = scope
    }

    /**
     * Return if transition is running when shared elements are navigating
     *
     * @return
     */
    fun getSharedElementsRootScope() : SharedElementsRootScope {
        return this.scope!!
    }

    /**
     * Set the navController lifecycle owner to handle lifecycle events such as onBackPressed
     *
     * @param owner The lifecycle owner
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun setLifecycleOwner(owner: LifecycleOwner) {
        if (owner == lifecycleOwner) {
            return
        }
        lifecycleOwner?.lifecycle?.removeObserver(lifecycleObserver)
        lifecycleOwner = owner
        owner.lifecycle.addObserver(lifecycleObserver)
    }

    /**
     * Setup the navController backPressed dispatcher to handle onBackPressed events
     *
     * @param dispatcher  The dispatcher to add the backPressed callback
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun setOnBackPressedDispatcher(dispatcher: OnBackPressedDispatcher) {
        if (dispatcher == onBackPressedDispatcher) {
            return
        }
        val lifecycleOwner = checkNotNull(lifecycleOwner) {
            "You must call setLifecycleOwner() before calling setOnBackPressedDispatcher()"
        }
        // Remove the callback from any previous dispatcher
        onBackPressedCallback.remove()
        // Then add it to the new dispatcher
        onBackPressedDispatcher = dispatcher
        dispatcher.addCallback(lifecycleOwner, onBackPressedCallback)

        // Make sure that listener for updating the SharedNavStackDest lifecycles comes after
        // the dispatcher
        lifecycleOwner.lifecycle.apply {
            removeObserver(lifecycleObserver)
            addObserver(lifecycleObserver)
        }
    }

    /**
     * Enable or disable the onBackPressed handler globally
     * @param enabled   true=Enabled, false=Disabled
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun enableOnBackPressed(enabled: Boolean) {
        enableOnBackPressedCallback = enabled
        updateOnBackPressedCallbackEnabled()
    }

    /**
     * Update the onBackPressedCallback to disable/enable the onBackPressed handler in the callback
     */
    private fun updateOnBackPressedCallbackEnabled() {
        onBackPressedCallback.isEnabled = (
            enableOnBackPressedCallback && backQueue.count() > 1
        )
    }

    /**
     * Navigate to the given route destination
     *
     * @param route Route name
     */
    fun navigate(route: String, arguments: Bundle? = null, vararg animatedKeys: Any) {
        val destination = findDestination(route)
        if (destination != null) {
            if (scope == null || !scope!!.isRunningTransition) {
                val animatedKeysArr = arrayOf(*animatedKeys)
                prepareTransition(animatedKeysArr)
                backQueue.addLast(SharedNavStackDest(destination.route, arguments, animatedKeysArr))
                currentDestination.value = backQueue.last()
            } else {
                Log.d("SharedNavController", "Transition in progress navigation is blocked.")
            }
        } else {
            throw NotFoundException("The route name \"$route\" could not be found.")
        }
        // Update the onBackPressed to set enabled or not.
        updateOnBackPressedCallbackEnabled()
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
            if (scope == null || !scope!!.isRunningTransition) {
                // Pop backQueue and set the current destination to the previous one
                val removedDest = backQueue.removeLast()
                if (removedDest.animatedKeys != null) {
                    prepareTransition(removedDest.animatedKeys)
                }
                currentDestination.value = backQueue.last()
                // Update the onBackPressed to set enabled or not.
                updateOnBackPressedCallbackEnabled()
                true
            } else {
                Log.d("SharedNavController", "Transition in progress navigation is blocked.")
                false
            }
        }
    }

    /**
     * Get content for destination
     *
     * @param route Route to get composable content
     * @return      Composable content to be shown
     */
    fun getContentForDestination(route: String) : @Composable (SharedNavStackDest) -> Unit {
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

/**
 * Create a remember of the [SharedNavController] using [SharedNavControllerSaver]
 * to allow sharedNavController state to be saved and restored.
 *
 * @return [SharedNavController] remembered instance
 */
@Composable
fun rememberSharedNavController() : SharedNavController {
    val savedNavController = rememberSaveable(saver = SharedNavControllerSaver()) {
        createSharedNavController()
    }
    return savedNavController
}

/**
 * Create a new instance of the [SharedNavController] and return
 * @return [SharedNavController] instance
 */
private fun createSharedNavController() = SharedNavController()

/**
 * The [SharedNavController] saver used to save and restore the [SharedNavController] across config change and process death.
 */
private fun SharedNavControllerSaver(

): Saver<SharedNavController, *> = Saver(
    save = { it.saveState() },
    restore = { createSharedNavController().apply { restoreState(it) } }
)
