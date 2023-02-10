package com.infinity.mysql.management

import com.infinity.mysql.MysqlDatabase
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by richard on 09/02/2023 20:49
 *
 * Represents a prepared Mysql state that can be re-used multiple times.
 *
 * This class is used by generated code. After it is used, `release` must be called so that
 * it can be used by other threads.
 *
 * To avoid re-entry even within the same thread, this class allows only 1 time access to the shared
 * statement until it is released.
 *
 * @constructor Creates an Mysql prepared statement that can be re-used across threads. If it is
 * in use, it automatically creates a new one.
 *
 * @suppress
 */
abstract class SharedMysqlStatement(private val database: MysqlDatabase) {
    /**
     * Statement lock
     */
    private val lock = AtomicBoolean(false)

    /**
     * Statement for running queries
     */
    private val stmt: PreparedStatement by lazy {
        createNewStatement()
    }

    /**
     * Create the query.
     *
     * @return The SQL query to prepare.
     */
    protected abstract fun createQuery(): String

    /**
     * Asserts that we are not on the main thread.
     *
     * @hide
     */
    protected open fun assertNotMainThread() {
        database.assertNotMainThread()
    }

    /**
     * Create a new statement when a statement is already in use
     *
     * @return The new prepared statement [Connection.prepareStatement]
     */
    private fun createNewStatement(): PreparedStatement {
        val query = createQuery()
        return database.compileStatement(query)
    }

    private fun getStmt(canUseCached: Boolean): PreparedStatement {
        val stmt = if (canUseCached) {
            stmt
        } else {
            // it is in use, create a one off statement
            createNewStatement()
        }
        return stmt
    }

    /**
     * Call this to get the statement. Must call [.release] once done.
     */
    open fun acquire(): PreparedStatement {
        assertNotMainThread()
        return getStmt(lock.compareAndSet(false, true))
    }

    /**
     * Must call this when statement will not be used anymore.
     *
     * @param statement The statement that was returned from acquire.
     */
    open fun release(statement: PreparedStatement) {
        if (statement === stmt) {
            lock.set(false)
        }
    }
}