package com.infinity.mysql.processor.generator

import com.google.devtools.ksp.*
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.infinity.mysql.annotation.ColumnInfo
import com.infinity.mysql.annotation.Query
import com.infinity.mysql.processor.extensions.getDefaultValueExpression
import com.infinity.mysql.processor.utils.bindQuery
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

/**
 * Created by richard on 06/02/2023 22:59
 *
 */
@OptIn(KspExperimental::class)
class DaoGenerator(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator
) {

    fun generate(
        annotatedClass: KSClassDeclaration,
        dbAnnotatedClass: KSClassDeclaration
    ) {
        val packageName = annotatedClass.packageName.asString()
        val implName = "${annotatedClass.simpleName.asString()}_Impl"
        val superType = annotatedClass.asType(emptyList()).toTypeName(TypeParameterResolver.EMPTY)
        val superFunctions = annotatedClass.getDeclaredFunctions()

        val dbType = dbAnnotatedClass.asType(emptyList()).toTypeName(TypeParameterResolver.EMPTY)

        // Functions to be implemented
        val queryFunctions = superFunctions.filter { ksFunc ->
            ksFunc.isAnnotationPresent(Query::class)
        }

        // code generation logic
        val fileSpec = FileSpec.builder(
            packageName = packageName, fileName = implName
        ).apply {
            addImport("com.infinity.mysql.management", "MysqlQuery")
            addImport("com.infinity.mysql.management", "DBUtil")
            addImport("com.infinity.mysql.management", "ResultSetUtil")
            addImport("android.util", "Log") // Debug Only
            addType(
                TypeSpec.classBuilder(implName)
                    .addSuperinterface(superType)
                    // DAO Primary constructor with Database initialization
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            // __db parameter
                            .addParameter("__db", dbType)
                            .build()
                    )
                    // DAO global database variable
                    .addProperty(
                        PropertySpec.builder("__db", dbType)
                            .addModifiers(KModifier.PRIVATE)
                            .initializer("__db")
                            .build()
                    )
                    // DAO query functions implementation
                    .addFunctions(queryFunctions.asIterable().map { ksFunc ->
                        val funcName = ksFunc.simpleName.asString()
                        val funcRetResolved = ksFunc.returnType!!.resolve()
                        val funcRetType = funcRetResolved.toTypeName()
                        val funcRetClass = funcRetResolved.declaration as KSClassDeclaration
                        val funcRetTypeParam = funcRetResolved.arguments.firstOrNull()?.type
                        val funcParams = ksFunc.parameters
                        var funcCode = ""
                        var funcCodeParams = emptyArray<Any>()

                        logger.warn("funcRetType is a ${funcRetClass.simpleName.asString()}<${funcRetTypeParam.toString()}>")

                        if (ksFunc.isAnnotationPresent(Query::class)) {
                            val funcAnnotArgQuery = ksFunc.annotations.iterator().next().arguments[0].value as String
                            val bindResult = bindQuery(funcAnnotArgQuery, ksFunc)

                            logger.warn("DAO queryParamMap: ${bindResult.bindMap} - Query: $funcAnnotArgQuery - newQuery: ${bindResult.query}")

                            funcCode = """
                                 |val _sql = %S
                                 |val _stmt = MysqlQuery.acquire(_sql, 1)
                                 |var _argIndex = 1
                            """.trimIndent()

                            funcCode += bindResult.bindMap.map { funcMapEntry ->
                                val funcParamValue = funcMapEntry.value
                                var strBind = "\n"
                                if (funcMapEntry.key > 0) {
                                    strBind += "|_argIndex = ${funcMapEntry.key + 1}\n"
                                }

                                var stmtBindIdentSpace = ""

                                // STMT_NULL_CHECK_START - Add a nullability check to be able to bind the null value in the statement
                                if (funcParamValue.type.toTypeName().isNullable) {
                                    stmtBindIdentSpace = "  "
                                    strBind += "|if (${funcParamValue.name?.asString()} == null) {\n"
                                    strBind += "|${stmtBindIdentSpace}_stmt.bindNull(_argIndex)\n"
                                    strBind += "|} else {\n"
                                }

                                if (funcParamValue.type.toTypeName() == Long::class.asTypeName() ||
                                    funcParamValue.type.toTypeName() == Int::class.asTypeName() ||
                                    funcParamValue.type.toTypeName() == Int::class.asTypeName().copy(true)
                                ) {

                                    // The bind long statement
                                    strBind += "|${stmtBindIdentSpace}_stmt.bindLong(_argIndex, ${funcParamValue.name?.asString()}"

                                    // Add the proper cast if function param needs to be cast to the appropriate bind param
                                    if (
                                        funcParamValue.type.toTypeName() == Int::class.asTypeName() ||
                                        funcParamValue.type.toTypeName() == Int::class.asTypeName().copy(true)
                                    ) {
                                        strBind += ".toLong()"
                                    }
                                    strBind += ")"
                                } else if (funcParamValue.type.toTypeName() == String::class.asTypeName()) {
                                    strBind += "|${stmtBindIdentSpace}_stmt.bindString(_argIndex, ${funcParamValue.name?.asString()})"
                                } else if (
                                    funcParamValue.type.toTypeName() == Double::class.asTypeName() ||
                                    funcParamValue.type.toTypeName() == Float::class.asTypeName()
                                ) {
                                    strBind += "|${stmtBindIdentSpace}_stmt.bindDouble(_argIndex, ${funcParamValue.name?.asString()})"
                                }

                                // STMT_NULL_CHECK_END - Add a nullability check to be able to bind the null value in the statement
                                if (funcParamValue.type.toTypeName().isNullable) {
                                    // End of nullable bindParam check
                                    strBind += "\n|}"
                                }

                                strBind
                            }.joinToString("")

                            // Check if some transaction is in progress, usually happens when concurrent coroutines are accessing database at same time
                            funcCode += "\n|__db.assertNotSuspendingTransaction()"

                            // Execute the [MysqlStmt] and get the [ResultSet]
                            funcCode += "\n|val _resultSet = DBUtil.query(__db, _stmt)"

                            // Get the map [String(colName)] => [ColumnInfo]
                            funcCode += "\n|val _colInfoMap = ResultSetUtil.mapResultColumns(_resultSet)"

                            // Get the indexes of the bind columns of the query

                            if (funcRetClass.toClassName() == List::class.asClassName()) {
                                if (funcRetTypeParam == null) {
                                    throw Exception("List<${funcRetTypeParam.toString()}> typeParamClass is null.");
                                }
                                val funcRetTypeParamClass = (funcRetTypeParam.resolve().declaration as KSClassDeclaration)
                                val funcRetListClassProps = funcRetTypeParamClass.getDeclaredProperties().iterator()
                                while(funcRetListClassProps.hasNext()) {
                                    val propAt = funcRetListClassProps.next()
                                    val propAtColName = propAt.simpleName
                                    val propAtAnnotColInfo = propAt.getAnnotationsByType(ColumnInfo::class).firstOrNull()
                                    if (propAtAnnotColInfo != null && propAtAnnotColInfo.name != ColumnInfo.INHERIT_FIELD_NAME) {

                                    }
                                    logger.warn("funcRetProp: ${propAt.simpleName.asString()}")
                                }
                            }

                            // Read the result
                            funcCode += "\n"
                            funcCode += """
                                |while(_resultSet.next()) {
                                |  Log.d("MysqlDao", _resultSet.getString(_resultSet.findColumn("TABLE_NAME")))
                                |}
                                |_resultSet.close()
                            """.trimIndent()

                            funcCodeParams = arrayOf(
                                bindResult.query
                            )
                        }

                        FunSpec.builder(funcName)
                            .addModifiers(KModifier.OVERRIDE)
                            .addParameters(funcParams.asIterable().map { ksParam ->
                                val paramName = ksParam.name!!.asString()
                                val paramType = ksParam.type.toTypeName()
                                val paramHasDef = ksParam.getDefaultValueExpression(logger)

                                ParameterSpec.builder(paramName, paramType)
                                    .build()
                            })
                            .addCode(
                                """
                                    ${funcCode}
                                    |return emptyList()
                                """.trimMargin(),
                                *funcCodeParams
                            )
                            .returns(funcRetType)
                            .build()
                    })
                    .build()
            )
        }.build()

        fileSpec.writeTo(
            codeGenerator = codeGenerator,
            aggregating = false
        )
    }
}