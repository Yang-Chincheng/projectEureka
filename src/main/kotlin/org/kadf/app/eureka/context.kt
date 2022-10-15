package org.kadf.app.eureka

class SourceFile(private val filename: String) {
    override fun toString(): String = filename
}

class CodePosition(private val line: Int, private val column: Int) {
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

class CodeContext(val source: SourceFile?, val begin: CodePosition, val end: CodePosition) {
    init {
        if (begin > end) throw Exception("invalid code segment");
    }
}