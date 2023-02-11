package com.infinity.devtools

import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        // assertEquals(4, 2 + 2)
        val word = "SELECT * FROM information_schema.tables WHERE table_schema = :schema AND code = :code AND table_schema = :schema"
        val split = word.split("(?<=.)(?=:\\w+)|(?<=:\\w{1,100}\\s)".toRegex())
        split.forEach {
            println("split: ${it}")
        }
    }
}