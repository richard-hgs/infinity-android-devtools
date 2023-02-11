package com.infinity.mysql.management

import com.infinity.mysql.MysqlDatabase
import java.sql.ResultSet

/**
 * Created by richard on 10/02/2023 22:30
 *
 * Database Utilities
 */

/**
 * Performs the SQLiteQuery on the given database.
 *
 * This util method encapsulates copying the cursor if the `maybeCopy` parameter is
 * `true` and either the api level is below a certain threshold or the full result of the
 * query does not fit in a single window.
 *
 * @param db          The database to perform the query on.
 * @param mysqlQuery  The query to perform.
 * @return Result of the query.
 */
fun query(
    db: MysqlDatabase,
    mysqlQuery: MysqlQuery
) : ResultSet {
    return db.query(mysqlQuery)
}