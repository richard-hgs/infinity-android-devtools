package com.infinity.mysql.management

import androidx.annotation.RestrictTo
import com.infinity.mysql.MysqlDatabase
import java.sql.PreparedStatement

/**
 * Created by richard on 09/02/2023 20:46
 *
 * Implementations of this class knows how to delete or update a particular entity.
 *
 * This is an internal library class and all of its implementations are auto-generated.
 *
 * @constructor Creates a DeletionOrUpdateAdapter that can delete or update the entity type T on the
 * given database.
 *
 * @param T The type parameter of the entity to be deleted
 * @suppress
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
abstract class EntityDeletionOrUpdateAdapter<T>(
    database: MysqlDatabase
) : SharedMysqlStatement(database) {
    /**
     * Create the deletion or update query
     *
     * @return An SQL query that can delete or update instances of T.
     */
    abstract override fun createQuery(): String

    /**
     * Binds the entity into the given statement.
     *
     * @param statement The SQLite statement that prepared for the query returned from
     * createQuery.
     * @param entity    The entity of type T.
     */
    protected abstract fun bind(statement: PreparedStatement, entity: T)

    /**
     * Deletes or updates the given entities in the database and returns the affected row count.
     *
     * @param entity The entity to delete or update
     * @return The number of affected rows
     */
    fun handle(entity: T): Int {
        val stmt: PreparedStatement = acquire()
        return try {
            bind(stmt, entity)
            stmt.execute()
            stmt.updateCount
        } finally {
            release(stmt)
        }
    }

    /**
     * Deletes or updates the given entities in the database and returns the affected row count.
     *
     * @param entities Entities to delete or update
     * @return The number of affected rows
     */
    fun handleMultiple(entities: Iterable<T>): Int {
        val stmt: PreparedStatement = acquire()
        return try {
            var total = 0
            entities.forEach { entity ->
                bind(stmt, entity)
                stmt.execute()
                total += stmt.updateCount
            }
            total
        } finally {
            release(stmt)
        }
    }

    /**
     * Deletes or updates the given entities in the database and returns the affected row count.
     *
     * @param entities Entities to delete or update
     * @return The number of affected rows
     */
    fun handleMultiple(entities: Array<out T>): Int {
        val stmt: PreparedStatement = acquire()
        return try {
            var total = 0
            entities.forEach { entity ->
                bind(stmt, entity)
                stmt.execute()
                total += stmt.updateCount
            }
            total
        } finally {
            release(stmt)
        }
    }
}