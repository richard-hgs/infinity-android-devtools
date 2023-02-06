package com.infinity.mysql.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * Created by richard on 05/02/2023 19:59
 *
 * The create function is invoked whenever KSP needs to create an instance of your SymbolProcessor.
 * This gives you access to the environment which provides the default logger.
 * The codeGenerator provides methods for creating and managing files.
 * Furthermore, only the files that are created from it are available to KSP for incremental
 * processing and compilations.
 */
class DatabaseFactoryProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return DatabaseFactoryProcessor(
            logger = environment.logger,
            codeGenerator = environment.codeGenerator
        )
    }
}