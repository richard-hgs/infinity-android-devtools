package com.infinity.mysql.management

import androidx.annotation.IntDef
import androidx.annotation.VisibleForTesting
import com.infinity.mysql.MysqlDatabase
import java.sql.PreparedStatement
import java.util.*

/**
 * Created by richard on 10/02/2023 21:38
 *
 */
class MysqlQuery private constructor(
    @field:VisibleForTesting val capacity: Int
) {
    @Volatile
    private var query: String? = null

    @JvmField
    @VisibleForTesting
    val longBindings: LongArray

    @JvmField
    @VisibleForTesting
    val doubleBindings: DoubleArray

    @JvmField
    @VisibleForTesting
    val stringBindings: Array<String?>

    @JvmField
    @VisibleForTesting
    val blobBindings: Array<ByteArray?>

    @Binding
    private val bindingTypes: IntArray

    // number of arguments in the query
    var argCount = 0
        private set

    fun init(query: String, initArgCount: Int) {
        this.query = query
        argCount = initArgCount
    }

    init {
        // because, 1 based indices... we don't want to offsets everything with 1 all the time.
        val limit = capacity + 1
        bindingTypes = IntArray(limit)
        longBindings = LongArray(limit)
        doubleBindings = DoubleArray(limit)
        stringBindings = arrayOfNulls(limit)
        blobBindings = arrayOfNulls(limit)
    }

    /**
     * Releases the query back to the pool.
     *
     * After released, the statement might be returned when [.acquire] is called
     * so you should never re-use it after releasing.
     */
    fun release() {
        synchronized(queryPool) {
            queryPool[capacity] = this
            prunePoolLocked()
        }
    }

    /**
     * The sql that [MysqlDatabase] will use to bind the parameters
     */
    val sql: String
        get() = checkNotNull(this.query)

    /**
     * Used by the [MysqlDatabase] to bind the prepared statement
     *
     * @param statement
     */
    fun bindTo(statement: PreparedStatement) {
        for (index in 1..argCount) {
            when (bindingTypes[index]) {
                NULL -> statement.setNull(index, java.sql.Types.NULL)
                LONG -> statement.setLong(index, longBindings[index])
                DOUBLE -> statement.setDouble(index, doubleBindings[index])
                STRING -> statement.setString(index, requireNotNull(stringBindings[index]))
                BLOB -> statement.setBytes(index, requireNotNull(blobBindings[index]))
            }
        }
    }

    fun bindNull(index: Int) {
        bindingTypes[index] = NULL
    }

    fun bindLong(index: Int, value: Long) {
        bindingTypes[index] = LONG
        longBindings[index] = value
    }

    fun bindDouble(index: Int, value: Double) {
        bindingTypes[index] = DOUBLE
        doubleBindings[index] = value
    }

    fun bindString(index: Int, value: String) {
        bindingTypes[index] = STRING
        stringBindings[index] = value
    }

    fun bindBlob(index: Int, value: ByteArray) {
        bindingTypes[index] = BLOB
        blobBindings[index] = value
    }

    /**
     * Copies arguments from another MysqlQuery into this query.
     *
     * @param other The other query, which holds the arguments to be copied.
     */
    fun copyArgumentsFrom(other: MysqlQuery) {
        val argCount = other.argCount + 1 // +1 for the binding offsets
        System.arraycopy(other.bindingTypes, 0, bindingTypes, 0, argCount)
        System.arraycopy(other.longBindings, 0, longBindings, 0, argCount)
        System.arraycopy(other.stringBindings, 0, stringBindings, 0, argCount)
        System.arraycopy(other.blobBindings, 0, blobBindings, 0, argCount)
        System.arraycopy(other.doubleBindings, 0, doubleBindings, 0, argCount)
    }

    fun clearBindings() {
        Arrays.fill(bindingTypes, NULL)
        Arrays.fill(stringBindings, null)
        Arrays.fill(blobBindings, null)
        query = null
        // no need to clear others
    }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(NULL, LONG, DOUBLE, STRING, BLOB)
    internal annotation class Binding

    companion object {
        // Maximum number of queries we'll keep cached.
        @VisibleForTesting
        const val POOL_LIMIT = 15

        // Once we hit POOL_LIMIT, we'll bring the pool size back to the desired number. We always
        // clear the bigger queries (# of arguments).
        @VisibleForTesting
        const val DESIRED_POOL_SIZE = 10

        @JvmField
        @VisibleForTesting
        val queryPool = TreeMap<Int, MysqlQuery>()

        /**
         * Returns a new MysqlQuery that can accept the given number of arguments and holds the
         * given query.
         *
         * @param query         The query to prepare
         * @param argumentCount The number of query arguments
         * @return A MysqlQuery that holds the given query and has space for the given number
         * of arguments.
         */
        @JvmStatic
        fun acquire(query: String, argumentCount: Int): MysqlQuery {
            synchronized(queryPool) {
                val entry = queryPool.ceilingEntry(argumentCount)
                if (entry != null) {
                    queryPool.remove(entry.key)
                    val sqliteQuery = entry.value
                    sqliteQuery.init(query, argumentCount)
                    return sqliteQuery
                }
            }
            val sqLiteQuery = MysqlQuery(argumentCount)
            sqLiteQuery.init(query, argumentCount)
            return sqLiteQuery
        }

        /**
         * Remove queries from [queryPool] until [DESIRED_POOL_SIZE] is achieved
         */
        internal fun prunePoolLocked() {
            if (queryPool.size > POOL_LIMIT) {
                var toBeRemoved = queryPool.size - DESIRED_POOL_SIZE
                val iterator = queryPool.descendingKeySet().iterator()
                while (toBeRemoved-- > 0) {
                    iterator.next()
                    iterator.remove()
                }
            }
        }

        private const val NULL = 1
        private const val LONG = 2
        private const val DOUBLE = 3
        private const val STRING = 4
        private const val BLOB = 5
    }
}