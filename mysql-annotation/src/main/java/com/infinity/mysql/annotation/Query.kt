package com.infinity.mysql.annotation

/**
 * Created by richard on 07/02/2023 22:06
 * Annotation that will be used inside [Dao] annotated interfaces to run queries
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Query(
    val query: String
)
