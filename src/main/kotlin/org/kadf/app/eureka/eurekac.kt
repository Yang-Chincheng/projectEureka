package org.kadf.app.eureka

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.kadf.app.eureka.ast.ASTBuilder
import org.kadf.app.eureka.semantic.SemanticChecker
import org.kadf.app.eureka.utils.EurekaErrorListener
import java.io.InputStream

class EurekaCompiler(private val input: InputStream) {

    fun eureka() {
        val lexer = MxStarLexer(CharStreams.fromStream(input))
        lexer.removeErrorListeners()
        lexer.addErrorListener(EurekaErrorListener())

        val parser = MxStarParser(CommonTokenStream(lexer))
        parser.removeErrorListeners()
        parser.addErrorListener(EurekaErrorListener())

        val cstRoot = parser.program()

        val astRoot = ASTBuilder(null).visitProgram(cstRoot)

        SemanticChecker().visit(astRoot)
    }

}