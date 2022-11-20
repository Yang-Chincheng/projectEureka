package org.kadf.app.eureka.ir

sealed interface IConstant
sealed interface ISimpleConstant

class BoolConst(
    val value: Boolean
): Value(BoolType), IConstant, ISimpleConstant {
    override fun toString(): String = "$value"
}

class IntegerConst(
    len: Int,
    val value: Int
): Value(IntegerType(len)), IConstant, ISimpleConstant {
    override fun toString(): String = "$value"
}

object NullConst: Value(PointerType), IConstant, ISimpleConstant {
    override fun toString(): String = "null"
}

class ArrayConst(
    val value: List<Value>
): Value(ArrayType(value.size, value.first().type)), IConstant {

    constructor(
        value: Value,
        scale: List<Int>
    ): this(
        List(scale.first()) {
            when (scale.size) {
                1 -> value
                else -> ArrayConst(value, scale.subList(1, scale.size))
            }
        }
    )

    override fun toString(): String {
        val seq = value
            .map {
                when(it) {
                    is ISimpleConstant -> "${it.type} $it"
                    else -> "$it"
                }
            }
            .reduce { acc, s -> "$acc, $s" }
        return "[ $seq ]"
    }
}

class StructConstant(
    val value: List<Value>
): Value(StructType(value.map { it.type })), IConstant {
    override fun toString(): String {
        val seq = value
            .map {
                when(it) {
                    is ISimpleConstant -> "${it.type} $it"
                    else -> "$it"
                }
            }
            .reduce { acc, s -> "$acc, $s" }
        return "{ $seq }"
    }
}

class Empty(id: String): Value(VoidType) {
    override fun toString(): String = ""
}
class Label(id: String): Value(LabelType, id) {
    override fun toString(): String = "label %$id"
    override val ir: String = "$id:"
}


