@file:Suppress("unused")

package com.infinity.mysql.processor.generator

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.infinity.mysql.annotation.Dao
import com.infinity.mysql.processor.extensions.toDecapitalize
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.jvm.volatile
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

/**
 * Created by richard on 05/02/2023 22:01
 *
 * Generates the MysqlDatabase_Impl class
 */
@OptIn(KspExperimental::class)
class DatabaseGenerator(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator
) {
    fun generate(
        annotatedClass: KSClassDeclaration
    ) {
        val packageName = annotatedClass.packageName.asString()
        val implName = "${annotatedClass.simpleName.asString()}_Impl"
        val superType = annotatedClass.asType(emptyList()).toTypeName(TypeParameterResolver.EMPTY)
        val superFunctions = annotatedClass.getDeclaredFunctions()

        // Dao functions to be implemented
        val daoFunctions = superFunctions.filter { ksFunc ->
            ksFunc.returnType?.let { ksReturn ->
                // Implements only the interface functions that are annotated with [Dao]
                val returnResolved = ksReturn.resolve()
                val returnClass = returnResolved.declaration as? KSClassDeclaration
                val returnClassAnnotated = returnClass?.isAnnotationPresent(Dao::class)

                returnClassAnnotated
            } == true
        }

        // Dao variables
        val daoVariables = daoFunctions.map { ksFunc ->
            ksFunc.returnType!!.let { ksReturn ->
                // Implements only the interface functions that are annotated with [Dao]
                val returnResolved = ksReturn.resolve()
                val returnClass = returnResolved.declaration as KSClassDeclaration

                returnClass
            }
        }

        // code generation logic
        val fileSpec = FileSpec.builder(
            packageName = packageName, fileName = implName
        ).apply {
            addType(
                TypeSpec.classBuilder(implName)
                    .superclass(superType)
                    // DAO global variables declaration
                    .addProperties(daoVariables.asIterable().map { ksClass ->
                        PropertySpec.builder("_${ksClass.simpleName.asString().toDecapitalize()}", ksClass.toClassName().copy(nullable = true))
                            .mutable(true)
                            .volatile()
                            .addModifiers(KModifier.PRIVATE)
                            .initializer("null")
                            .build()
                    })
                    // DAO functions generation
                    .addFunctions(daoFunctions.asIterable().map { ksFunc ->
                        val funcName = ksFunc.simpleName.asString()
                        val returnResolved = ksFunc.returnType!!.resolve()
                        val returnClass = returnResolved.declaration as KSClassDeclaration
                        val daoReturnVarName = "_${returnClass.simpleName.asString().toDecapitalize()}"

                        FunSpec.builder(funcName)
                            .apply {
                                addModifiers(KModifier.OVERRIDE)
                            }
                            .addStatement("""
                                if ($daoReturnVarName != null) {
                                    return $daoReturnVarName!!
                                } else {
                                    synchronized(this) {
                                        if ($daoReturnVarName == null) {
                                            $daoReturnVarName = ${returnClass.simpleName.asString()}_Impl(this)
                                        }
                                        return $daoReturnVarName!!
                                    }
                                }
                            """.trimIndent())
                            .returns(ksFunc.returnType!!.resolve().toTypeName(TypeParameterResolver.EMPTY))
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