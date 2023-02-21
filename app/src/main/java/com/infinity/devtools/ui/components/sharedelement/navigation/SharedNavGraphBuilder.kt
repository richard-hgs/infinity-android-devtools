package com.infinity.devtools.ui.components.sharedelement.navigation

/**
 * Shared navigation graph builder
 */
open class SharedNavGraphBuilder(
    private var startDestinationRoute: String
) {
    private val destinations = mutableListOf<SharedNavDestination>()

    /**
     * Add the destination to the [SharedNavGraphBuilder]
     */
    fun addDestination(destination: SharedNavDestination) {
        destinations += destination
    }

    fun build(): SharedNavGraph {
        return SharedNavGraph(
            startDestinationRoute,
            destinations
        )
    }
}