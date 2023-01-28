package org.kadf.app.eureka

import com.ibm.icu.util.Output
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.kadf.app.eureka.EurekaCompiler.output
import org.kadf.app.eureka.ast.ASTBuilder
import org.kadf.app.eureka.ast.nodes.AstNode
import org.kadf.app.eureka.ir.IrBuilder
import org.kadf.app.eureka.ir.IrModule
import org.kadf.app.eureka.semantic.SemanticChecker
import org.kadf.app.eureka.utils.AntlrErrorListener
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream

object EurekaCompiler {

    private val InputStream.code: CharStream
        get() {
            return CharStreams.fromStream(this)
        }

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

    private fun AstNode.semantic(): AstNode {
        SemanticChecker(this).check(this)
        return this
    }
    private val AstNode.ir: IrModule
        get() {
            return IrBuilder(this).build(this)
        }

    private fun IrModule.output(os: OutputStream): IrModule {
        PrintStream(os).print(this.ir)
        return this
    }

    fun eureka(input: InputStream, output: OutputStream? = null) {
//        val irFile = File("testspace/code.ll").apply { createNewFile() }
//        val irStream = FileOutputStream(irFile)
        input.code
            .cst
            .ast
            .semantic()
            .ir
            .output(output!!)
    }

}