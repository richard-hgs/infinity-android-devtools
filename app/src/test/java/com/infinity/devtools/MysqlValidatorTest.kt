package com.infinity.devtools

import com.infinity.devtools.di.validators.MysqlValidator
import com.infinity.devtools.providers.UTTextUtilsProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


class MysqlValidatorTest {

    @Before
    fun beforeTest() {
    }

    @Test
    fun testFieldValidator() {
        @Suppress("UNUSED_VARIABLE")
        val textUtilsMock = UTTextUtilsProvider.mockInst
        val validator = MysqlValidator()

        assertFalse(validator.connNameIsValid(""))
        assertTrue(validator.connNameIsValid("nome conexão"))

        assertFalse(validator.connHostIsValid(""))
        assertTrue(validator.connHostIsValid("host name whatever"))

        assertFalse(validator.connPortIsValid(-1))
        assertFalse(validator.connPortIsValid(65536))
        assertTrue(validator.connPortIsValid(0))

        assertFalse(validator.connUserIsValid(""))
        assertTrue(validator.connUserIsValid("user name"))

        assertFalse(validator.connPassIsValid(""))
        assertTrue(validator.connPassIsValid("user pass"))

        assertFalse(validator.connDbnameIsValid(""))
        assertTrue(validator.connDbnameIsValid("dbname"))
    }
}