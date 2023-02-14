package com.infinity.mysql

import android.os.Looper
import androidx.annotation.RestrictTo
import com.infinity.mysql.management.MysqlConnInfo
import com.infinity.mysql.management.MysqlQuery
import java.sql.*
import java.util.*
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * Created by richard on 05/02/2023 19:23
 *
 * Mysql Database Management
 */
open class MysqlDatabase {

    /**
     * JDBC connection info
     */
    private lateinit var connInfo: MysqlConnInfo

    /**
     * JDBC connection instance for current [connInfo]
     */
    private var conn: Connection? = null

    /**
     * If true allow queries to run in main thread.
     * Should be avoided to prevent blocking the UI Thread.
     */
    private var allowMainThreadQueries = false

    /**
     * Suspending transaction id of the current thread.
     *
     * This id is only set on threads that are used to dispatch coroutines within a suspending
     * database transaction.
     */
    @get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    val suspendingTransactionId = ThreadLocal<Int>()

    /**
     * Saves the status of current connection transaction status
     */
    private var transactionInProgress = false

    /**
     * Locker for read and write of database
     */
    private val readWriteLock = ReentrantReadWriteLock()

    /**
     * Constructor that loads jdbc driver and creates a new instance.
     * Called internally by the [Mysql.getGeneratedImplementation]
     *
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @Suppress("ConvertSecondaryConstructorToPrimary")
    @kotlin.jvm.Throws(ClassNotFoundException::class, IllegalAccessException::class, InstantiationException::class)
    constructor() {
         Class.forName("com.mysql.cj.jdbc.Driver").newInstance()
    }

    /**
     * Builder for MysqlDatabase.
     *
     * @param T The type of the abstract database class.
     * @param klass Database class
     * @param connInfo Connection information used to connect to mysql server
     */
    open class Builder<T : MysqlDatabase> internal constructor(
        private val klass: Class<T>,
        private val connInfo: MysqlConnInfo
    ) {
        open fun build(): T {
            // Get a instance of the generated implementation of this database connector
            val db = Mysql.getGeneratedImplementation<T, T>(klass, "_Impl")
            // Instantiate the JDBC [MysqlOdbc]
            db.connInfo = connInfo
            return db
        }
    }

    /** True if the calling thread is the main thread.  */
    internal val isMainThread: Boolean
        get() = Looper.getMainLooper().thread === Thread.currentThread()

    /**
     * Uses this lock to prevent the database from closing while it is
     * querying database updates.
     *
     * The returned lock is reentrant and will allow multiple threads to acquire the lock
     * simultaneously until close is invoked in which the lock becomes exclusive as
     * a way to let the InvalidationTracker finish its work before closing the database.
     *
     * @return The lock for close.
     */
    internal fun getCloseLock(): Lock {
        return readWriteLock.readLock()
    }

    /**
     * Asserts that we are not on a suspending transaction.
     *
     * @hide
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP) // used in generated code
    open fun assertNotSuspendingTransaction() {
        check(transactionInProgress || suspendingTransactionId.get() == null) {
            "Cannot access database on a different coroutine" +
                    " context inherited from a suspending transaction."
        }
    }

    /**
     * Asserts that we are not on the main thread.
     *
     * @hide
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX) // used in generated code
    open fun assertNotMainThread() {
        if (allowMainThreadQueries) {
            return
        }
        check(!isMainThread) {
            "Cannot access database on the main thread since" +
                    " it may potentially lock the UI for a long period of time."
        }
    }

    /**
     * Connects to mysql database
     *
     * @throws java.sql.SQLException
     */
    @kotlin.jvm.Throws(SQLException::class)
    private fun connect() {
        val connectionProps = Properties()
        connectionProps["user"] = connInfo.user
        connectionProps["password"] = connInfo.pass

        conn = DriverManager.getConnection(
            "jdbc:mysql://${connInfo.host}:${connInfo.port}/", connectionProps
        )
    }

    /**
     * Disconnect from a mysql database
     *
     * @param validTimeout Timeout millis to wait when checking connection validity
     *
     * @throws java.sql.SQLException
     */
    @kotlin.jvm.Throws(SQLException::class)
    private fun disconnect(validTimeout: Int) {
        conn?.let {
            // Check if connection is valid
            if (it.isValid(validTimeout)) {
                // Acquire close lock
                val closeLock: Lock = readWriteLock.writeLock()
                closeLock.lock()

                try {
                    it.close()
                    conn = null
                } finally {
                    // Release close lock
                    closeLock.unlock()
                }
            }
        }
    }

    /**
     * Check if a database connection is open
     *
     * @return True=Connection Open, False=Connection Closed
     *
     * @throws java.sql.SQLException
     */
    @kotlin.jvm.Throws(SQLException::class)
    private fun isConnected(): Boolean {
        return conn?.let {
            !it.isClosed
        } ?: false
    }

    /**
     * Used to check if connection is active to use connection functions
     *
     * @return
     */
    private fun getConn() : Connection {
        if (!isConnected()) {
            // Since we are disconnected from database. Create a new connection.
            connect()
        }
        return conn!!
    }

    /**
     * Wrapper for [Connection.prepareStatement].
     *
     * @param sql The query to compile.
     * @return The compiled query.
     */
    open fun compileStatement(sql: String): PreparedStatement {
        assertNotMainThread()
        assertNotSuspendingTransaction()
        return getConn().prepareStatement(sql)
    }

    private fun beginTransaction() {
        assertNotMainThread()
        internalBeginTransaction()
    }

    private fun internalBeginTransaction() {
        assertNotMainThread()
        val conn = getConn()
        conn.autoCommit = false
        transactionInProgress = true
    }

    private fun endTransaction() {
        internalEndTransaction()
    }

    private fun internalEndTransaction() {
        try {
            val conn = getConn()
            if (!conn.autoCommit) {
                conn.autoCommit = true
            }
        } finally {
            transactionInProgress = false
        }
    }

    private fun setTransactionSuccessful() {
        try {
            val conn = getConn()
            if (!conn.autoCommit) {
                conn.commit()
            }
        } finally {
            transactionInProgress = false
        }
    }

    /**
     * Executes the specified [Runnable] in a database transaction. The transaction will be
     * marked as successful unless an exception is thrown in the [Runnable].
     *
     * Room will only perform at most one transaction at a time.
     *
     * @param body The piece of code to execute.
     */
    @Suppress("DEPRECATION")
    open fun runInTransaction(body: Runnable) {
        beginTransaction()
        try {
            body.run()
            setTransactionSuccessful()
        } finally {
            endTransaction()
        }
    }

    open fun query(query: MysqlQuery): ResultSet {
        assertNotMainThread()
        assertNotSuspendingTransaction()
        val conn = getConn()
        val prepStmt = conn.prepareStatement(query.sql)
        query.bindTo(prepStmt)
        return prepStmt.executeQuery()
    }

//    /**
//     * Executes a sql in current connection
//     *
//     * @throws java.sql.SQLException
//     * @throws ConnectionIsClosedException if connection is closed or not opened using [connect]
//     */
//    @kotlin.jvm.Throws(SQLException::class)
//    fun execSQL(query: String, bindArgs: Array<Any>) : OdbcResult {
//        if (!isConnected()) {
//            // Open a new connection
//            connect()
//        }
//
//        // Check if connection still open
//        if (isConnected()) {
//            return conn?.let { mConn ->
//                val stmt : PreparedStatement = mConn.prepareStatement(query)
//
//                stmt.setString(0, 1)
//
//                bindArgs.forEachIndexed { index, value ->
//                    when(value) {
//                        is Boolean -> stmt.setBoolean(index, value)
//                        is Byte -> stmt.setByte(index, value)
//                        is Short -> stmt.setShort(index, value)
//                        is Int -> stmt.setInt(index, value)
//                        is Long -> stmt.setLong(index, value)
//                        is Float -> stmt.setFloat(index, value)
//                        is Double -> stmt.setDouble(index, value)
//                        is BigDecimal -> stmt.setBigDecimal(index, value)
//                        is String -> stmt.setString(index, value)
//                        is ByteArray -> stmt.setBytes(index, value)
//                        is java.sql.Date -> stmt.setDate(index, value)
//                        is Time -> stmt.setTime(index, value)
//                        is Timestamp -> stmt.setTimestamp(index, value)
//                        is InputStream -> stmt.setBinaryStream(index, value)
//                    }
//                }
//
//                val execResult = stmt.execute()
//
//                if (execResult) {
//                    // Result Set
//                    return OdbcResult(
//                        result = true,
//                        resultSet = stmt.resultSet,
//                        updateCount = 0,
//                        stmt = stmt
//                    )
//                } else {
//                    // Update Count
//                    return OdbcResult(
//                        result = true,
//                        resultSet = stmt.resultSet,
//                        updateCount = stmt.updateCount,
//                        stmt = stmt
//                    )
//                }
//            } ?: throw SQLException("OdbcResult is missing.")
//
//        } else {
//            throw ConnectionIsClosedException("Connection is closed, did you forget to call MysqlOdbc fun connect(host: String, port: Int, user: String, pass: String)")
//        }
//    }
}