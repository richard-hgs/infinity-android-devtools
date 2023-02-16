package com.infinity.mysql.processor.generator

import com.google.devtools.ksp.*
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.infinity.mysql.annotation.ColumnInfo
import com.infinity.mysql.annotation.Query
import com.infinity.mysql.processor.extensions._camelCase
import com.infinity.mysql.processor.utils.BindUtils
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
                            val bindResult = BindUtils.bindQuery(funcAnnotArgQuery, ksFunc)

                            logger.warn("DAO queryParamMap: ${bindResult.bindMap} - Query: $funcAnnotArgQuery - newQuery: ${bindResult.query}")

                            funcCode = """
                                 |val _sql = %S
                                 |val _stmt = MysqlQuery.acquire(_sql, 1)
                                 |var _argIndex = 1
                            """.trimIndent()

                            // Append the query param string to be formatted
                            funcCodeParams += bindResult.query

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

                            // RESULT_SET(START) of the - try finally - Create a try finally to be able to close resultSet even if an error occurs
                            funcCode += "\n|try {"

                            // Get the map [String(colName)] => [ColumnInfo]
                            funcCode += "\n|  val _colInfoMap = ResultSetUtil.mapResultColumns(_resultSet)"

                            // Get the indexes of the bind columns of the query

                            if (funcRetClass.toClassName() == List::class.asClassName()) {
                                var strListItemInstance = ""
                                var strCursorIndexes = ""
                                var strReadCursorRows = "\n|  while(_resultSet.next()) {"
                                if (funcRetTypeParam == null) {
                                    // If list Type parameter not resolved notify user through an exception.
                                    throw Exception("List<T> typeParamClass is null.")
                                }
                                val funcRetTypeParamClass = (funcRetTypeParam.resolve().declaration as KSClassDeclaration)
                                val funcRetListClassProps = funcRetTypeParamClass.getDeclaredProperties().iterator()

                                strReadCursorRows += "\n|    val _item : ${funcRetTypeParamClass.simpleName.asString()}"
                                strListItemInstance += "\n|    _item = ${funcRetTypeParamClass.simpleName.asString()}("

                                while(funcRetListClassProps.hasNext()) {
                                    val propAt = funcRetListClassProps.next()
                                    var propAtColName = propAt.simpleName.asString()
                                    val propAtAnnotColInfo = propAt.getAnnotationsByType(ColumnInfo::class).firstOrNull()
                                    val propAtAttrTypeStr : String
                                    val propAtAttrTypeGetStr : String
                                    if (propAtAnnotColInfo != null && propAtAnnotColInfo.name != ColumnInfo.INHERIT_FIELD_NAME) {
                                        propAtColName = propAtAnnotColInfo.name
                                    }

                                    if (propAt.type.toTypeName() == String::class.asTypeName() || propAt.type.toTypeName() == String::class.asTypeName().copy(true)) {
                                        val optional = propAt.type.toTypeName() == String::class.asTypeName().copy(true)
                                        propAtAttrTypeStr = "String" + if (optional) "?" else ""
                                        propAtAttrTypeGetStr = "String"
                                    } else if (propAt.type.toTypeName() == Int::class.asTypeName() || propAt.type.toTypeName() == Int::class.asTypeName().copy(true)) {
                                        val optional = propAt.type.toTypeName() == Int::class.asTypeName().copy(true)
                                        propAtAttrTypeStr = "Int" + if (optional) "?" else ""
                                        propAtAttrTypeGetStr = "Int"
                                    } else if (propAt.type.toTypeName() == Long::class.asTypeName() || propAt.type.toTypeName() == Long::class.asTypeName().copy(true)) {
                                        val optional = propAt.type.toTypeName() == Long::class.asTypeName().copy(true)
                                        propAtAttrTypeStr = "Long" + if (optional) "?" else ""
                                        propAtAttrTypeGetStr = "Long"
                                    } else if (propAt.type.toTypeName() == Float::class.asTypeName() || propAt.type.toTypeName() == Float::class.asTypeName().copy(true)) {
                                        val optional = propAt.type.toTypeName() == Float::class.asTypeName().copy(true)
                                        propAtAttrTypeStr = "Float" + if (optional) "?" else ""
                                        propAtAttrTypeGetStr = "Float"
                                    } else if (propAt.type.toTypeName() == Double::class.asTypeName() || propAt.type.toTypeName() == Double::class.asTypeName().copy(true)) {
                                        val optional = propAt.type.toTypeName() == Double::class.asTypeName().copy(true)
                                        propAtAttrTypeStr = "Double" + if (optional) "?" else ""
                                        propAtAttrTypeGetStr = "Double"
                                    } else {
                                        propAtAttrTypeStr = "Any"
                                        propAtAttrTypeGetStr = "Object"
                                    }

                                    strCursorIndexes += "\n"
                                    strCursorIndexes += """|  val _cursorIndexOf${propAtColName._camelCase()} = ResultSetUtil.getColumnIndexOrThrow(_colInfoMap, %S)""".trimIndent()

                                    // Append the column name to string format params
                                    funcCodeParams += propAtColName

                                    strReadCursorRows += "\n"
                                    strReadCursorRows += """
                                        |    val _tmp${propAtColName._camelCase()} : $propAtAttrTypeStr = _resultSet.get$propAtAttrTypeGetStr(_cursorIndexOf${propAtColName._camelCase()})
                                    """.trimIndent()

                                    strListItemInstance += "_tmp${propAtColName._camelCase()}" + if (funcRetListClassProps.hasNext()) ","  else ""
                                }
                                strListItemInstance += ")"
                                strReadCursorRows += strListItemInstance
                                strReadCursorRows += "\n|    _result += _item"
                                strReadCursorRows += "\n|  }"

                                funcCode += strCursorIndexes
                                funcCode += "\n|  val _result = ArrayList<${funcRetTypeParamClass.simpleName.asString()}>(_resultSet.fetchSize)"
                                funcCode += strReadCursorRows
                                funcCode += "\n|  return _result"
                            }

                            funcCode += "\n|} finally {" // RESULT_SET(END) of the - try finally
                            funcCode += "\n|  _resultSet.close()"
                            funcCode += "\n|}"
                        }

                        FunSpec.builder(funcName)
                            .addModifiers(KModifier.OVERRIDE)
                            .addParameters(funcParams.asIterable().map { ksParam ->
                                val paramName = ksParam.name!!.asString()
                                val paramType = ksParam.type.toTypeName()
                                // val paramHasDef = ksParam.getDefaultValueExpression(logger)

                                ParameterSpec.builder(paramName, paramType)
                                    .build()
                            })
                            .addCode(
                                """
                                    $funcCode
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