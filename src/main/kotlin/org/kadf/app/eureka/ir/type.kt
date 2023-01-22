package org.kadf.app.eureka.ir

sealed interface Type

//sealed interface FirstClassType: IRNode
//sealed interface SingleValueType: IRNode, FirstClassType
//sealed interface AggregateType: IRNode, FirstClassType, ReturnableType
//sealed interface ReturnableType: IRNode

// IRType / VoidType
object VoidType: Type {
    override fun toString(): String = "void"
}

// IRType / FunctionType
class FunctionType(
    val retType: Type,
    val paraType: List<Type>
): Type

// IRType / FirstClassTypes / SingleValueTypes / IntegerType
open class IntegerType(
    val len: Int
): Type {
    override fun toString(): String = "i$len"
}
object IntType: IntegerType(32)
object CharType: IntegerType(8)
object BoolType: IntegerType(1)

// IRType / FirstClassTypes / SingleValueTypes / PointerType
object PointerType: Type {
    override fun toString(): String = "ptr"
}

// IRType / FirstClassTypes / LabelType
object LabelType: Type {
    override fun toString(): String = "label"
}

// IRType / FirstClassTypes / AggregateTypes / ArrayType
class ArrayType(
    val len: Int,
    val subtype: Type
): Type {

    constructor(
        type: Type,
        scale: List<Int>
    ) : this(
        scale.first(),
        when (scale.size) {
            1 -> type
            else -> ArrayType(type, scale.subList(1, scale.size))
        }
    )

    override fun toString(): String = "[$len x $subtype]"
}

// IRType / FirstClassTypes / AggregateTypes / StructureType
class StructType(
    val subtype: List<Type>
): Type {
    override fun toString(): String {
        val seq = subtype.map { "$it" }.reduce { acc, s -> "$acc, $s" }
        return "{ $seq }"
    }
}