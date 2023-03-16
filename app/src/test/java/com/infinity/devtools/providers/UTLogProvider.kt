package com.infinity.devtools.providers

import android.util.Log
import org.mockito.ArgumentMatchers.anyString
import org.mockito.MockedStatic
import org.mockito.Mockito

/**
 * Unit tests "log" provider using mock
 */
object UTLogProvider {

    val mockInst: MockedStatic<Log> = Mockito.mockStatic(Log::class.java)

    init {
        mockInst.`when`<Log> { Log.d(anyString(), anyString()) }.then {
            val tag = it.arguments.first()
            val msg = it.arguments.last()
            println("$tag $msg")
            0
        }
    }
}