package org.kadf.app.eureka.utils

import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer

class AntlrErrorListener: BaseErrorListener() {
    override fun syntaxError(
        recognizer: Recognizer<*, *>?,
        offendingSymbol: Any?,
        line: Int,
        charPositionInLine: Int,
        msg: String?,
        e: RecognitionException?
    ) {
        DefaultErrorHandler.report("$line:$charPositionInLine, incurs error: $msg")
    }
}

fun interface ErrorHandler {
    fun report(msg: String): Nothing
}

object DefaultErrorHandler: ErrorHandler {
    override fun report(msg: String): Nothing = throw Exception(msg)
}

class SemanticError(val ctx: CodeContext?, msg: String?) : Exception(msg)