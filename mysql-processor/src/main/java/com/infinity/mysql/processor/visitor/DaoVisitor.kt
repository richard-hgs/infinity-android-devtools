package com.infinity.mysql.processor.visitor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.infinity.mysql.processor.generator.DaoGenerator

/**
 * Created by richard on 06/02/2023 22:59
 *
 */
class DaoVisitor(
    private val logger: KSPLogger,
    codeGenerator: CodeGenerator
) : KSVisitorVoid() {

    private val generator = DaoGenerator(logger, codeGenerator)

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        super.visitClassDeclaration(classDeclaration, data)

        generator.generate(classDeclaration)
    }
}