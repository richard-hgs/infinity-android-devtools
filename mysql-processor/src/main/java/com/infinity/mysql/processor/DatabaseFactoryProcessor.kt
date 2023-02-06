package com.infinity.mysql.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.validate
import com.infinity.mysql.annotation.Database
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
class DatabaseFactoryProcessor(
    private val logger: KSPLogger,
    codeGenerator: CodeGenerator
) : SymbolProcessor {

    private val visitor = DatabaseVisitor(logger, codeGenerator)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("DatabaseFactoryProcessor was invoked.")

        // Filter through all application symbols only the symbols of our annotations
        var unresolvedSymbols: List<KSAnnotated> = emptyList()
        val annotationName = Database::class.qualifiedName

        if (annotationName != null) {
            // getSymbolsWithAnnotation Fetch all the symbols annotated with FragmentFactory annotation
            // ou can also use getClassDeclarationByName, getDeclarationsFromPackage when your processor
            // relies on logic outside annotation targets.
            val resolved = resolver
                .getSymbolsWithAnnotation(annotationName)
                .toList()

            // Here you use the default validate function offered by KSP to filter symbols in the
            // scope that can be resolved. This is done internally using a KSValidateVisitor that
            // visits each declaration and resolves all type parameters.
            val validatedSymbols = resolved.filter { it.validate() }.toList()
            validatedSymbols
                .filter {
                    // Add more validations
                    true
                }
                .forEach {
                    // Visit and process this symbol
                    it.accept(visitor, Unit)
                }

            // Finally, you return all the unresolved symbols that would need more rounds. In the current example,
            // this would be an empty list because all the symbols should resolve in the first round.
            unresolvedSymbols = resolved - validatedSymbols
        }
        return unresolvedSymbols
    }

}