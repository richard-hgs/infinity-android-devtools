package com.infinity.mysql.processor

import com.infinity.mysql.processor.extensions._camelCase
import com.infinity.mysql.processor.extensions._capitalizeFirst
import com.infinity.mysql.processor.extensions._decapitalize
import com.infinity.mysql.processor.extensions._snakeCase
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by richard on 16/02/2023 20:26
 *
 * String extensions test
 */
class StringExtTest {
    @Test
    fun test_camelCase() {
        val text = "some string to camel case"

        assertEquals("SomeStringToCamelCase", text._camelCase())
    }

    @Test
    fun test_snakeCase() {
        val text = "some string to snake Case"

        assertEquals("some_string_to_snake_case", text._snakeCase())
    }

    @Test
    fun test_decapitalize() {
        val text = "SomE string to Decapitalize"

        assertEquals("somE string to decapitalize", text._decapitalize())
    }

    @Test
    fun test_capitalizeFirst() {
        val text = "some string to capitalize first"

        assertEquals("Some string to capitalize first", text._capitalizeFirst())
    }
}