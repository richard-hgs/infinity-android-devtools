package com.infinity.mysql.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated

/**
 * Created by richard on 07/02/2023 21:11
 *
 * Interface to access processor functions inside visitors and simplify code generation
 */
interface BaseVisitor {

    /**
     * Called by Kotlin Symbol Processing to run the processing task.
     *
     * @param resolver provides [SymbolProcessor] with access to compiler details such as Symbols.
     * @return A list of deferred symbols that the processor can't process. Only symbols that can't be processed at this round should be returned. Symbols in compiled code (libraries) are always valid and are ignored if returned in the deferral list.
     */
    fun process(resolver: Resolver): List<KSAnnotated>
}