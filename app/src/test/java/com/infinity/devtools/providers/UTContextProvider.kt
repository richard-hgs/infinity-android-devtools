package com.infinity.devtools.providers

import android.content.Context
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.kotlin.any
import org.mockito.kotlin.mock

object UTContextProvider {

    /**
     * Provides some basic functionalities of the Android Context
     * INCLUDES:
     * - getString(R.string.some_str_id)
     */
    val context : Context = mock {
        on { getString(anyInt()) }.thenAnswer {
            if (UTResourcesProvider.stringsMap.containsKey(it.arguments.first())) {
                UTResourcesProvider.stringsMap.get(key = it.arguments.first())
            } else {
                null
            }
        }
    }
}