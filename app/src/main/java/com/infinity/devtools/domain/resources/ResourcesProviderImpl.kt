package com.infinity.devtools.domain.resources

import android.content.Context
import androidx.annotation.StringRes

class ResourcesProviderImpl constructor(
    private val context: Context
) : ResourcesProvider {
    override fun getString(@StringRes stringResId: Int): String {
        return context.getString(stringResId)
    }
}