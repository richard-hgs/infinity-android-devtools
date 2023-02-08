package com.infinity.mysql.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.infinity.mysql.processor.visitor.DaoVisitor
import com.infinity.mysql.processor.visitor.DatabaseVisitor

/**
 * Created by richard on 05/02/2023 15:50
 *
 * Processor that will create MysqlDatabase_Impl that will manage all mysql connection queries
 *
 * To debug ksp processors run in a terminal the following command:
 * ./gradlew clean installDebug --info
 *
 * If failed to clean run the following command:
 * ./gradlew --stop
 *
 * You’ll also see a ksp directory inside the build folder of your :app module.
 * PATH: build/generated/ksp/debug
 */
class Processor(
    private val logger: KSPLogger,
    codeGenerator: CodeGenerator
) : SymbolProcessor {

    private val daoVisitor = DaoVisitor(logger, codeGenerator)
    private val databaseVisitor = DatabaseVisitor(logger, codeGenerator)

    private var rounds = 0

    override fun process(resolver: Resolver): List<KSAnnotated> {
        rounds++
        logger.info("MysqlProcessor was invoked $rounds times.")

        val unresolvedSymbols: MutableList<KSAnnotated> = emptyList<KSAnnotated>().toMutableList()

        // Process all symbols for all visitors
        unresolvedSymbols += daoVisitor.process(resolver)
        unresolvedSymbols += databaseVisitor.process(resolver)

        return unresolvedSymbols
    }

}