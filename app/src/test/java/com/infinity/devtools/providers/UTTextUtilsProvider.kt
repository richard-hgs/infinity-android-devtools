package com.infinity.devtools.providers

import android.text.TextUtils
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.kotlin.any

object UTTextUtilsProvider {

    val mockInst: MockedStatic<TextUtils> = Mockito.mockStatic(TextUtils::class.java)

    init {
        mockInst.`when`<TextUtils> { TextUtils.getTrimmedLength(any()) }.thenAnswer {
            val s = it.arguments.first() as CharSequence
            val len: Int = s.length

            var start = 0
            while (start < len && s.get(start) <= ' ') {
                start++
            }

            var end = len
            while (end > start && s.get(end - 1) <= ' ') {
                end--
            }

            end - start
        }
    }
}