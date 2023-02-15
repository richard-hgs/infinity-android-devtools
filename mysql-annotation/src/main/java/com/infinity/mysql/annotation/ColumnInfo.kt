package com.infinity.mysql.annotation

/**
 * Created by richard on 14/02/2023 22:33
 * Column info for now we only have column name
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class ColumnInfo(
    val name: String = INHERIT_FIELD_NAME
) {
    companion object {
        /**
         * Constant to let MysqlCompiler inherit the field name as the column name. If used, Compiler will use
         * the field name as the column name.
         */
        const val INHERIT_FIELD_NAME: String = "[field-name]"
    }
}
