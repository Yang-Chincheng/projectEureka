package org.kadf.app.eureka

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token

class CodeSource(private val filename: String) {
    override fun toString(): String = filename
}

class CodePosition(private val line: Int, private val column: Int) {

    constructor(token: Token): this(token.line, token.charPositionInLine) {}

    operator fun compareTo(other: CodePosition): Int {
        return when {
            line < other.line -> -1
            line > other.line -> 1
            else -> when {
                column < other.column -> -1
                column > other.column -> 1
                else -> 0
            }
        }
    }
    override fun toString(): String = "$line:$column"
}

class CodeContext(val source: CodeSource?, val start: CodePosition, val stop: CodePosition) {
    init {
        if (start > stop) throw Exception("invalid code segment");
    }

    constructor(src: CodeSource?, ctx: ParserRuleContext):
            this(src, CodePosition(ctx.start), CodePosition(ctx.stop))
}