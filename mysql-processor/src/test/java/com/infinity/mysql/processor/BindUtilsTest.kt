package com.infinity.mysql.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.infinity.mysql.processor.utils.BindUtils
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by richard on 16/02/2023 22:59
 * Test the bind utilities
 */
class BindUtilsTest {
    @Test
    fun test_bindQuery() {
        val kotlinSourceAnnot = SourceFile.kotlin(
            "test.Query.kt", """
                package test
                @Target(AnnotationTarget.FUNCTION)
                @Retention(AnnotationRetention.SOURCE)
                annotation class Query(val query: String)
            """
        )
        val kotlinSourceDao = SourceFile.kotlin(
            "test.QueryDao.kt", """
                package test
                interface QueryDao {
                    @Query("SELECT * FROM table WHERE table_schema = :schema")
                    fun daoFuncTest(schema : String = "schema_name") : Double
                }
            """
        )

        val compilation = KotlinCompilation().apply {
            sources = listOf(kotlinSourceAnnot, kotlinSourceDao)
            symbolProcessorProviders = listOf(MySymbolProcessorProvider())
        }
        val result = compilation.compile()

        if (result.exitCode != KotlinCompilation.ExitCode.OK) {
            throw Exception("Test failed")
        }
    }

    companion object {
        class MySymbolProcessor(
            private val logger: KSPLogger,
            codeGenerator: CodeGenerator
        ) : SymbolProcessor {
            override fun process(resolver: Resolver): List<KSAnnotated> {
                val ksFunc = resolver
                    .getSymbolsWithAnnotation("test.Query")
                    .toList().first() as KSFunctionDeclaration

                val query = ksFunc.annotations.iterator().next().arguments[0].value as String

                // TEST bindQuery
                val bindResult = BindUtils.bindQuery(query, ksFunc)

                assertEquals(
                    "SELECT * FROM table WHERE table_schema = ?",
                    bindResult.query
                )

                return emptyList()
            }
        }

        class MySymbolProcessorProvider : SymbolProcessorProvider {
            override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
                return MySymbolProcessor(
                    logger = environment.logger,
                    codeGenerator = environment.codeGenerator
                )
            }
        }
    }
}