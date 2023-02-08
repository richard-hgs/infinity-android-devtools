package com.infinity.mysql.processor.visitor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import com.infinity.mysql.annotation.Database
import com.infinity.mysql.processor.BaseVisitor
import com.infinity.mysql.processor.generator.DatabaseGenerator

/**
 * Created by richard on 05/02/2023 21:58
 *
 * Generates the MysqlDatabase_Impl class
 */
class DatabaseVisitor(
    private val logger: KSPLogger,
    codeGenerator: CodeGenerator
) : KSVisitorVoid(), BaseVisitor {

    private val generator = DatabaseGenerator(logger, codeGenerator)

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        super.visitClassDeclaration(classDeclaration, data)

        generator.generate(classDeclaration)
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        var unresolvedSymbols: List<KSAnnotated> = emptyList()

        val symbols = resolver
            .getSymbolsWithAnnotation(Database::class.qualifiedName!!)
            .toList()

        val validatedSymbols = symbols.filter {
            it.validate()
        }.toList()

        validatedSymbols.forEach {
            // Visit and process this symbol
            it.accept(this, Unit)
        }

        // Return unresolved symbols to be processed in next round
        unresolvedSymbols = symbols - validatedSymbols.toSet()

        logger.info("MysqlProcessor - DatabaseVisitor - unresolvedSymbols: ${unresolvedSymbols.count()}")

        return unresolvedSymbols
    }
}