@file:Suppress("unused")

package com.infinity.mysql.processor.generator

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.infinity.mysql.annotation.Dao
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
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

        for (type in superFunctions) {
            type.returnType?.let {
                val returnClass = it.resolve().declaration as? KSClassDeclaration
                returnClass?.isAnnotationPresent(Dao::class)
            }
        }

        // Functions to be implemented
        val daoFunctions = superFunctions.filter { ksFunc ->
            ksFunc.returnType?.let { ksReturn ->
                val returnClass = ksReturn.resolve().declaration as? KSClassDeclaration
                returnClass?.isAnnotationPresent(Dao::class)
            } ?: false
        }

        // code generation logic
        val fileSpec = FileSpec.builder(
            packageName = packageName, fileName = implName
        ).apply {
            addType(
                TypeSpec.classBuilder(implName)
                    .superclass(superType)
                    .addFunctions(daoFunctions.asIterable().map { ksFunc ->
                        val funcName = ksFunc.simpleName.asString()
                        FunSpec.builder(funcName)
                            .apply {
                                addModifiers(KModifier.OVERRIDE)
                            }
                            .addStatement("return MysqlDao_Impl()")
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