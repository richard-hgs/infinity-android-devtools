package com.infinity.mysql.processor.visitor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.validate
import com.google.devtools.ksp.visitor.KSEmptyVisitor
import com.infinity.mysql.annotation.Dao
import com.infinity.mysql.annotation.Database
import com.infinity.mysql.processor.BaseVisitor
import com.infinity.mysql.processor.generator.DaoGenerator

/**
 * Created by richard on 06/02/2023 22:59
 *
 */
class DaoVisitor(
    private val logger: KSPLogger,
    codeGenerator: CodeGenerator
) : KSEmptyVisitor<KSClassDeclaration, Unit>(), BaseVisitor {

    private val generator = DaoGenerator(logger, codeGenerator)

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: KSClassDeclaration) {
        super.visitClassDeclaration(classDeclaration, data)

        generator.generate(
            annotatedClass = classDeclaration,
            dbAnnotatedClass = data
        )
    }

    override fun defaultHandler(node: KSNode, data: KSClassDeclaration) {
        // Required unused handler
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        var unresolvedSymbols: List<KSAnnotated> = emptyList()

        val symbols = resolver
            .getSymbolsWithAnnotation(Dao::class.qualifiedName!!)
            .toList()

        val dbSymbol = resolver
            .getSymbolsWithAnnotation(Database::class.qualifiedName!!)
            .toList()
            .firstOrNull()

        val validatedSymbols = symbols.filter {
            dbSymbol?.validate()
            it.validate()
        }.toList()

        validatedSymbols.forEach {
            // Visit and process this symbol
            it.accept(this, dbSymbol as KSClassDeclaration)
        }

        // Return unresolved symbols to be processed in next round
        unresolvedSymbols = symbols - validatedSymbols.toSet()

        logger.info("MysqlProcessor - DaoVisitor - unresolvedSymbols: ${unresolvedSymbols.count()}")

        return unresolvedSymbols
    }


}