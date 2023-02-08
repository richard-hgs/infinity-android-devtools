package com.infinity.mysql.processor.generator

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.infinity.mysql.annotation.Query
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
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
        annotatedClass: KSClassDeclaration
    ) {
        val packageName = annotatedClass.packageName.asString()
        val implName = "${annotatedClass.simpleName.asString()}_Impl"
        val superType = annotatedClass.asType(emptyList()).toTypeName(TypeParameterResolver.EMPTY)
        val superFunctions = annotatedClass.getDeclaredFunctions()

        // Functions to be implemented
        val queryFunctions = superFunctions.filter { ksFunc ->
            ksFunc.isAnnotationPresent(Query::class)
        }

        // code generation logic
        val fileSpec = FileSpec.builder(
            packageName = packageName, fileName = implName
        ).apply {
            addType(
                TypeSpec.classBuilder(implName)
                    .addSuperinterface(superType)
                    .primaryConstructor(FunSpec.constructorBuilder().build())
                    .addFunctions(queryFunctions.asIterable().map { ksFunc ->
                        val funcName = ksFunc.simpleName.asString()
                        val funcRetResolved = ksFunc.returnType!!.resolve()
                        val funcRetType = funcRetResolved.toTypeName(TypeParameterResolver.EMPTY)
                        // val funcRetTypeParam = ksFunc.returnType!!.resolve().arguments.firstOrNull()?.toTypeName(TypeParameterResolver.EMPTY)
                        // val funcRetClassName = (ksFunc.returnType!!.resolve().declaration as KSClassDeclaration).simpleName.asString()
                        // val funcRetTypeParamClassName = ksFunc.returnType!!.resolve().arguments.firstOrNull()?.type?.toString()

                        // logger.warn("funcRetType: ${funcRetClassName}<${funcRetTypeParamClassName}>")

                        FunSpec.builder(funcName)
                            .apply {
                                addModifiers(KModifier.OVERRIDE)
                            }
                            .addStatement("return emptyList()")
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