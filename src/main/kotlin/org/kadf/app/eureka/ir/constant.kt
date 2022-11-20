package org.kadf.app.eureka.ir

sealed class IRConst(val type: IRType): IRNode

class BoolConst(val value: Boolean): IRConst(BoolType) {
    override val text: String get() = "$value"
}

sealed class IntegerConst(len: Int, val value: Int): IRConst(IntegerType(len)) {
    override val text: String get() = "${type.text} $value"
}
class IntConst(value: Int): IntegerConst(32, value)
class CharConst(value: Int): IntegerConst(8, value)

object NullConst: IRConst(PointerType) {
    override val text: String get() = "null"
}

class ArrayConst(val elements: List<IRConst>):
    IRConst(ArrayType(elements.first().type as SingleValueType, elements.size)) {
    override val text: String
        get() = "[ ${elements.map { it.text }.reduce { acc, s -> "$acc, $s" }} ]"
}

class StructConst(val elements: List<IRConst>):
    IRConst(StructType(elements.map { it.type as FirstClassType})) {
    override val text: String
        get() = "{ ${elements.map { it.text }.reduce { acc, s -> "$acc, $s" }} }"
}

