package com.infinity.devtools.domain.resources

import android.content.Context
import androidx.annotation.StringRes

/**
 * Resources provider implementation
 */
class ResourcesProviderImpl constructor(
    private val context: Context
) : ResourcesProvider {

    /**
     * Provide string resource by it's resource id
     *
     * @param stringResId String id name
     * @return String received
     */
    override fun getString(@StringRes stringResId: Int): String {
        return context.getString(stringResId)
    }
}