package org.kadf.app.eureka

import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.kadf.app.eureka.ast.ASTBuilder
import org.kadf.app.eureka.ast.nodes.AstNode
import org.kadf.app.eureka.semantic.SemanticChecker
import org.kadf.app.eureka.utils.AntlrErrorListener
import java.io.InputStream

object EurekaCompiler {

    private val CharStream.cst: ParserRuleContext
        get() {
            val lexer = MxStarLexer(this)
            lexer.removeErrorListeners()
            lexer.addErrorListener(AntlrErrorListener())
            val parser = MxStarParser(CommonTokenStream(lexer))
            parser.removeErrorListeners()
            parser.addErrorListener(AntlrErrorListener())
            return parser.program()
        }

    private val ParserRuleContext.ast: AstNode
        get() {
            return ASTBuilder(null).build(this)
        }

    private val AstNode.semantic: AstNode
        get() {
            SemanticChecker(this).check(this)
            return this
        }

    fun eureka(input: InputStream) {
        CharStreams.fromStream(input)
            .cst
            .ast
            .semantic
    }

}