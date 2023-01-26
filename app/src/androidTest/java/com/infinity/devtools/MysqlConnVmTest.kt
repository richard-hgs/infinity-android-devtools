package com.infinity.devtools

import android.content.Context
import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.FailureHandler
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.infinity.devtools.di.validators.MysqlValidator
import com.infinity.devtools.domain.database.AppDatabase
import com.infinity.devtools.domain.repository.MysqlConnRepoImpl
import com.infinity.devtools.domain.resources.ResourcesProviderImpl
import com.infinity.devtools.ui.presentation.MainActivity
import com.infinity.devtools.ui.vm.MysqlConnVm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matcher
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.model.MultipleFailureException.assertEmpty


@RunWith(AndroidJUnit4::class)
class MysqlConnVmTest {

    @Before
    fun deleteConns() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val appDatabase = AppDatabase.getDatabase(
            context = context
        )
        val mysqlConnVm = MysqlConnVm(
            repo = MysqlConnRepoImpl(
                connDao = appDatabase.getMysqlConnDao()
            ),
            resProv = ResourcesProviderImpl(context),
            validator = MysqlValidator()
        )
    }

    @Test
    fun testValidators() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val appDatabase = AppDatabase.getDatabase(
            context = context
        )
        val mysqlConnVm = MysqlConnVm(
            repo = MysqlConnRepoImpl(
                connDao = appDatabase.getMysqlConnDao()
            ),
            resProv = ResourcesProviderImpl(context),
            validator = MysqlValidator()
        )
        runBlocking(Dispatchers.IO) {
            // Invalid Conn Name
            var job = mysqlConnVm.addConn(mysqlConnVm.mysqlConn)
            job.join()
            assertTrue(mysqlConnVm.errDialogOpen.value)
            assertEquals(mysqlConnVm.errDialogMsg, context.getString(R.string.err_conn_name_field_required))
            mysqlConnVm.errDialogOpen.value = false // Close dialog

            // Add host name to current connection being created
            mysqlConnVm.updateName("Connection name")

            // Invalid Host
            job = mysqlConnVm.addConn(mysqlConnVm.mysqlConn)
            job.join()
            assertTrue(mysqlConnVm.errDialogOpen.value)
            assertEquals(mysqlConnVm.errDialogMsg, context.getString(R.string.err_conn_host_field_required))
            mysqlConnVm.errDialogOpen.value = false // Close dialog

            // Add host name to current connection being created
            mysqlConnVm.updateHost("hostname")

            // Add port to current connection being created
            mysqlConnVm.updatePort("65537")

            // Invalid Port
            job = mysqlConnVm.addConn(mysqlConnVm.mysqlConn)
            job.join()
            assertTrue(mysqlConnVm.errDialogOpen.value)
            assertEquals(mysqlConnVm.errDialogMsg, context.getString(R.string.err_conn_port_field_required))
            mysqlConnVm.errDialogOpen.value = false // Close dialog

            // Add port to current connection being created
            mysqlConnVm.updatePort("-1")

            // Invalid Port
            job = mysqlConnVm.addConn(mysqlConnVm.mysqlConn)
            job.join()
            assertTrue(mysqlConnVm.errDialogOpen.value)
            assertEquals(mysqlConnVm.errDialogMsg, context.getString(R.string.err_conn_port_field_required))
            mysqlConnVm.errDialogOpen.value = false // Close dialog

            // Add port to current connection being created
            mysqlConnVm.updatePort("35")

            // Invalid User
            job = mysqlConnVm.addConn(mysqlConnVm.mysqlConn)
            job.join()
            assertTrue(mysqlConnVm.errDialogOpen.value)
            assertEquals(mysqlConnVm.errDialogMsg, context.getString(R.string.err_conn_user_field_required))
            mysqlConnVm.errDialogOpen.value = false // Close dialog

            // Add user name to current connection being created
            mysqlConnVm.updateUser("username")

            // Invalid Pass
            job = mysqlConnVm.addConn(mysqlConnVm.mysqlConn)
            job.join()
            assertTrue(mysqlConnVm.errDialogOpen.value)
            assertEquals(mysqlConnVm.errDialogMsg, context.getString(R.string.err_conn_pass_field_required))
            mysqlConnVm.errDialogOpen.value = false // Close dialog

            // Add user name to current connection being created
            mysqlConnVm.updatePass("pass")

            // Invalid dbname
            job = mysqlConnVm.addConn(mysqlConnVm.mysqlConn)
            job.join()
            assertTrue(mysqlConnVm.errDialogOpen.value)
            assertEquals(mysqlConnVm.errDialogMsg, context.getString(R.string.err_conn_dbname_field_required))
            mysqlConnVm.errDialogOpen.value = false // Close dialog

            // Add dbname to current connection being created
            mysqlConnVm.updateDbName("dbname")

            // Valid connection
            job = mysqlConnVm.addConn(mysqlConnVm.mysqlConn)
            job.join()
            assertFalse(mysqlConnVm.errDialogOpen.value)
        }
    }
}