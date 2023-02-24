package com.infinity.mysql.processor.generator

import com.google.devtools.ksp.*
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSTypeReference
import com.infinity.mysql.annotation.ColumnInfo
import com.infinity.mysql.annotation.Query
import com.infinity.mysql.processor.extensions._camelCase
import com.infinity.mysql.processor.model.MysqlTypeGen
import com.infinity.mysql.processor.model.PartialGenResult
import com.infinity.mysql.processor.utils.BindUtils
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import java.sql.ResultSet
import java.util.stream.IntStream.range

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
                        val funcRetClass : KSClassDeclaration
                        val funcRetTypeParam : KSTypeReference?
                        val funcParams = ksFunc.parameters
                        var funcCode = ""
                        var funcCodeParams = emptyArray<Any>()

                        if (funcRetResolved.declaration is KSTypeAlias) {
                            // Function return type is a TypeAlias
                            funcRetClass = (funcRetResolved.declaration as KSTypeAlias).type.resolve().declaration as KSClassDeclaration
                            funcRetTypeParam = (funcRetResolved.declaration as KSTypeAlias).type.resolve().arguments.firstOrNull()?.type

                            // Since type alias argument is not imported automatically, import the List type parameter
                            if (funcRetTypeParam != null) {
                                val funcRetTypeParamClassName = funcRetTypeParam.resolve().toClassName()
                                addImport(funcRetTypeParamClassName.packageName, funcRetTypeParamClassName.simpleName)
                            }
                        } else {
                            // Function return type is not a TypeAlias
                            funcRetClass = funcRetResolved.declaration as KSClassDeclaration
                            funcRetTypeParam = funcRetResolved.arguments.firstOrNull()?.type
                        }

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

                            // Get the indexes of the bind columns of the query
                            if (funcRetClass.toClassName() == List::class.asClassName()) {
                                // Return type is a list generate code for iterating ResultSet and read the column values to the val _result List<T>
                                val genResult = generateResultSetReadForListReturnType(funcRetTypeParam, 1)
                                funcCode += genResult.genCode
                                funcCodeParams = funcCodeParams.plus(elements = genResult.genCodeParams)
                            } else if (isClassNamePrimitive(funcRetClass.toClassName())) {
                                // Return type is a double generate code for reading a single list element and return it
                                val genResult = generateResultSetReadForPrimitiveType(funcRetResolved, 1)
                                funcCode += genResult.genCode
                                funcCodeParams = funcCodeParams.plus(elements = genResult.genCodeParams)
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

    private fun generateResultSetReadForPrimitiveType(primitiveType: KSType, indentLevel: Int) : PartialGenResult {
        var genCode = ""
        var genCodeParams = emptyArray<Any>()
        val indentSpace1 = getIndentSpace(indentLevel)
        val indentSpace2 = getIndentSpace(indentLevel + 1)
        val mysqlTypeResolved = resolveDataTypeToMysqlType(primitiveType.toTypeName())
        genCode += "\n|${indentSpace1}val _cursorIndexOfItem = 1"
        genCode += "\n|${indentSpace1}val _item : ${mysqlTypeResolved.type}"
        genCode += "\n|${indentSpace1}if (_resultSet.next()) {"
        genCode += "\n|${indentSpace2}_item = _resultSet.get${mysqlTypeResolved.getType}(_cursorIndexOfItem)"
        genCode += "\n|${indentSpace1}}"
        if (!mysqlTypeResolved.isOptional) {
            genCode += " else {"
            genCode += "\n|${indentSpace2}throw %T(%S)"
            genCode += "\n|${indentSpace1}}"
            genCodeParams += NullPointerException::class
            genCodeParams += "Could not read the query result, no result received for a non optional DAO return of type ${mysqlTypeResolved.type}"
        } else {
            genCode += " else {"
            genCode += "\n|${indentSpace2}_item = null"
            genCode += "\n|${indentSpace1}}"
        }

        genCode += "\n|${indentSpace1}return _item"

        return PartialGenResult(genCode, genCodeParams)
    }

    /**
     * Generate code for reading all rows of the [ResultSet] to a List<T> of a given T type
     *
     * @param funcRetTypeParam  The DAO function return type
     * @param indentLevel       The indentation level to be used in the generated code, inherit from parent indent
     * @return [PartialGenResult] that contains the generated code and also the parameters used in its string format
     */
    private fun generateResultSetReadForListReturnType(funcRetTypeParam: KSTypeReference?, indentLevel: Int) : PartialGenResult {
        var genCode = ""
        var genCodeParams = emptyArray<Any>()
        val indentSpace1 = getIndentSpace(indentLevel)
        val indentSpace2 = getIndentSpace(indentLevel + 1)
        var strListItemInstance = ""
        var strCursorIndexes = ""
        var strReadCursorRows = "\n|${indentSpace1}while(_resultSet.next()) {"
        if (funcRetTypeParam == null) {
            // If list Type parameter not resolved notify user through an exception.
            throw Exception("List<T> typeParamClass is null.")
        }
        val funcRetTypeParamClass = (funcRetTypeParam.resolve().declaration as KSClassDeclaration)
        val funcRetListClassProps = funcRetTypeParamClass.getDeclaredProperties().iterator()

        strReadCursorRows += "\n|${indentSpace2}val _item : ${funcRetTypeParamClass.simpleName.asString()}"
        strListItemInstance += "\n|${indentSpace2}_item = ${funcRetTypeParamClass.simpleName.asString()}("

        // Get the map [String(colName)] => [ColumnInfo]
        genCode += "\n|${indentSpace1}val _colInfoMap = ResultSetUtil.mapResultColumns(_resultSet)"

        while(funcRetListClassProps.hasNext()) {
            val propAt = funcRetListClassProps.next()
            var propAtColName = propAt.simpleName.asString()
            val propAtAnnotColInfo = propAt.getAnnotationsByType(ColumnInfo::class).firstOrNull()
            val propAtAttrTypeGen = resolveDataTypeToMysqlType(propAt.type.toTypeName())
            if (propAtAnnotColInfo != null && propAtAnnotColInfo.name != ColumnInfo.INHERIT_FIELD_NAME) {
                propAtColName = propAtAnnotColInfo.name
            }

            strCursorIndexes += "\n"
            strCursorIndexes += """|${indentSpace1}val _cursorIndexOf${propAtColName._camelCase()} = ResultSetUtil.getColumnIndexOrThrow(_colInfoMap, %S)""".trimIndent()

            // Append the column name to string format params
            genCodeParams += propAtColName

            strReadCursorRows += "\n"
            strReadCursorRows += """
                |${indentSpace2}val _tmp${propAtColName._camelCase()} : ${propAtAttrTypeGen.type} = _resultSet.get${propAtAttrTypeGen.getType}(_cursorIndexOf${propAtColName._camelCase()})
            """.trimIndent()

            strListItemInstance += "_tmp${propAtColName._camelCase()}" + if (funcRetListClassProps.hasNext()) ","  else ""
        }
        strListItemInstance += ")"
        strReadCursorRows += strListItemInstance
        strReadCursorRows += "\n|${indentSpace2}_result += _item"
        strReadCursorRows += "\n|${indentSpace1}}"

        genCode += strCursorIndexes
        genCode += "\n|${indentSpace1}val _result = ArrayList<${funcRetTypeParamClass.simpleName.asString()}>(_resultSet.fetchSize)"
        genCode += strReadCursorRows
        genCode += "\n|${indentSpace1}return _result"

        return PartialGenResult(genCode, genCodeParams)
    }

    /**
     * Resolves a given data type to the generated code: val myTypeVar : type = ResultSet.getString(?).
     * The String from the get expression is the getType var
     *
     * @param type
     * @return [MysqlTypeGen] Types resolved
     */
    private fun resolveDataTypeToMysqlType(type: TypeName) : MysqlTypeGen {
        val mType: String
        val mGetType : String
        val isOptional : Boolean
        when (type) {
            String::class.asTypeName(), String::class.asTypeName().copy(true) -> {
                isOptional = type == String::class.asTypeName().copy(true)
                mType = "String" + if (isOptional) "?" else ""
                mGetType = "String"
            }
            Boolean::class.asTypeName(), Boolean::class.asTypeName().copy(true) -> {
                isOptional = type == Boolean::class.asTypeName().copy(true)
                mType = "Boolean" + if (isOptional) "?" else ""
                mGetType = "Boolean"
            }
            Int::class.asTypeName(), Int::class.asTypeName().copy(true) -> {
                isOptional = type == Int::class.asTypeName().copy(true)
                mType = "Int" + if (isOptional) "?" else ""
                mGetType = "Int"
            }
            Long::class.asTypeName(), Long::class.asTypeName().copy(true) -> {
                isOptional = type == Long::class.asTypeName().copy(true)
                mType = "Long" + if (isOptional) "?" else ""
                mGetType = "Long"
            }
            Float::class.asTypeName(), Float::class.asTypeName().copy(true) -> {
                isOptional = type == Float::class.asTypeName().copy(true)
                mType = "Float" + if (isOptional) "?" else ""
                mGetType = "Float"
            }
            Double::class.asTypeName(), Double::class.asTypeName().copy(true) -> {
                isOptional = type == Double::class.asTypeName().copy(true)
                mType = "Double" + if (isOptional) "?" else ""
                mGetType = "Double"
            }
            else -> {
                isOptional = false
                mType = "Any"
                mGetType = "Object"
            }
        }

        return MysqlTypeGen(
            mType,
            mGetType,
            isOptional
        )
    }

    /**
     * Check if the given className is a primitive type class or not
     *
     * @param className [ClassName] to check the className against primitive type class
     * @return true=Is a primitive, false=Is nota a primitive
     */
    private fun isClassNamePrimitive(className: ClassName) : Boolean {
        return when(className) {
            String::class.asClassName(), String::class.asClassName().copy(true) -> true
            Boolean::class.asClassName(), Boolean::class.asClassName().copy(true) -> true
            Int::class.asClassName(), Int::class.asClassName().copy(true) -> true
            Long::class.asClassName(), Long::class.asClassName().copy(true) -> true
            Float::class.asClassName(), Float::class.asClassName().copy(true) -> true
            Double::class.asClassName(), Double::class.asClassName().copy(true) -> true
            else -> false
        }
    }

    /**
     * Get indentation space for a level multiple of a space
     *
     * @param level     The level of the indentation.
     * @param spaces    The amount of indentation space used for one level.
     * @return [String] Indentation space
     */
    private fun getIndentSpace(level: Int, spaces : Int = 2) : String {
        var ident = ""
        var strStep = ""
        range(0, spaces).forEach {
            strStep += " "
        }
        range(0, level).forEach {
            ident += strStep
        }
        return ident
    }
}