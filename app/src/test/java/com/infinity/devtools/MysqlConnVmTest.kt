package com.infinity.devtools

import android.text.TextUtils
import com.infinity.devtools.data.model.MysqlConn
import com.infinity.devtools.domain.repository.MysqlConnRepoImpl
import com.infinity.devtools.domain.resource.ResourcesProvider
import com.infinity.devtools.providers.UTContextProvider
import com.infinity.devtools.ui.vm.MysqlConnVm
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any
import org.mockito.kotlin.mock


class MysqlConnVmTest {
    /**
     * Basic context mock
     */
    private val context = UTContextProvider.context

    /**
     * MysqlConn repository mock
     */
    private val mysqlConnRepo: MysqlConnRepoImpl = mock {
        on { getMysqlConnsFromRoom() }.thenAnswer {
            emptyFlow<MysqlConn>()
        }
        on { getMysqlConnFromRoom(any()) }.thenAnswer {
            null
        }
        // on { addMysqlConnToRoom(any()) } then { /* callback executed for func call */ }
    }

    /**
     * Resources provider mock
     */
    private val resProvider: ResourcesProvider = mock {
        on { getString(anyInt()) }.thenAnswer {
            context.getString(it.arguments.first() as Int)
        }
    }

    /**
     * MysqlConnVm mock
     */
    private val mysqlConnVm: MysqlConnVm = MysqlConnVm(
        repo = mysqlConnRepo,
        resProv = resProvider
    )

    @Before
    fun beforeTest() {
        val myMock = mockStatic(TextUtils::class.java)
        myMock.`when`<Any> { TextUtils::getTrimmedLength }.thenAnswer {
            0
        }
    }

    @Test
    fun testFieldValidator() {
        val length = TextUtils.getTrimmedLength("abc ")
        println("L: $length")

//        val conns = mysqlConnRepo.getMysqlConnsFromRoom()

//        runBlocking(Dispatchers.IO) {
//            // FAILURE - None field informed so name field presents error dialog message
//            val job = mysqlConnVm.updateConn(conn = mysqlConnVm.mysqlConn)
//            job.join()
//            println("dialogOpen: ${mysqlConnVm.errDialogOpen.value}")
//            assertTrue(mysqlConnVm.errDialogOpen.value)
//            assertEquals(
//                mysqlConnVm.errDialogMsg,
//                resProvider.getString(stringResId = R.string.err_conn_name_field_required)
//            )
//        }


    }
}