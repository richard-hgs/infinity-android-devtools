package com.infinity.mysql

import androidx.annotation.RestrictTo
import com.infinity.mysql.annotation.Database
import com.infinity.mysql.management.MysqlConnInfo

/**
 * Created by richard on 05/02/2023 19:34
 *
 * Utility functions for mysql
 */
object Mysql {
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    @JvmStatic
    fun <T, C> getGeneratedImplementation(
        klass: Class<C>,
        suffix: String
    ): T {
        val fullPackage = klass.getPackage()!!.name
        val name: String = klass.canonicalName!!
        val postPackageName =
            if (fullPackage.isEmpty()) name else name.substring(fullPackage.length + 1)
        val implName = postPackageName.replace('.', '_') + suffix
        return try {
            val fullClassName = if (fullPackage.isEmpty()) {
                implName
            } else {
                "$fullPackage.$implName"
            }
            val aClass = Class.forName(
                fullClassName, true, klass.classLoader
            ) as Class<T>
            aClass.newInstance()
        } catch (e: ClassNotFoundException) {
            throw RuntimeException(
                "Cannot find implementation for ${klass.canonicalName}. $implName does not " +
                        "exist"
            )
        } catch (e: IllegalAccessException) {
            throw RuntimeException(
                "Cannot access the constructor $klass.canonicalName"
            )
        } catch (e: InstantiationException) {
            throw RuntimeException(
                "Failed to create an instance of $klass.canonicalName"
            )
        }
    }

    /**
     * Creates a MysqlDatabase.Builder for a persistent database. Once a database is built, you
     * should keep a reference to it and re-use it.
     *
     * @param klass   The abstract class which is annotated with [Database] and extends
     * [MysqlDatabase].
     * @param connInfo The connection information of the database
     * @param T        The type of the database class.
     * @return A `MysqlDatabaseBuilder<T>` which you can use to create the database connection.
     */
    @JvmStatic
    fun <T : MysqlDatabase> databaseBuilder(
        klass: Class<T>,
        connInfo: MysqlConnInfo
    ): MysqlDatabase.Builder<T> {
        return MysqlDatabase.Builder(klass, connInfo)
    }
}