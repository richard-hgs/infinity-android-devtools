package com.infinity.devtools.domain.resources

import androidx.annotation.StringRes

/**
 * Resources provider interface
 */
interface ResourcesProvider {

    /**
     * Provide string resource by it's resource id
     *
     * @param stringResId String id name
     * @return String received
     */
    fun getString(@StringRes stringResId: Int): String
}