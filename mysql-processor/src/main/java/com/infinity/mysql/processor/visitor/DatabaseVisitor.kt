package com.infinity.mysql.processor.visitor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.infinity.mysql.processor.generator.DatabaseGenerator

/**
 * Created by richard on 05/02/2023 21:58
 *
 * Generates the MysqlDatabase_Impl class
 */
class DatabaseVisitor(
    private val logger: KSPLogger,
    codeGenerator: CodeGenerator
) : KSVisitorVoid() {

    private val generator = DatabaseGenerator(logger, codeGenerator)

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        super.visitClassDeclaration(classDeclaration, data)

        generator.generate(classDeclaration)
    }
}