package com.infinity.devtools.domain.resources

import androidx.annotation.StringRes

interface ResourcesProvider {

    fun getString(@StringRes stringResId: Int): String
}