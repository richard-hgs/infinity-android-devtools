package com.infinity.devtools

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Required [Application] used by hilt dependency injection
 *
 */
@HiltAndroidApp
class MyApplication : Application()