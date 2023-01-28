package org.kadf.app.eureka.ir

sealed interface IrConstant

sealed class IrIntegerConst(
    len: Int,
    private val value: Int
) : IrValue(IrIntegerType(len)) {
    override fun toString(): String = "$value"
}
class IrIntConst(private val value: Int) : IrValue(IrIntType) {
    companion object {
        val maxValue = IrIntConst(-1)
    }

    override fun toString(): String = "$value"
}
class IrCharConst(private val value: Byte) : IrValue(IrCharType) {
    companion object {
        val maxValue = IrCharConst(255)
    }
    constructor(value: Int): this(value.toByte())

    override fun toString(): String = "${value.toInt()}"
}
class IrBoolConst(private val value: Boolean) : IrValue(IrBoolType) {
    constructor(value: Int): this(value != 0)
    companion object {
        val maxValue = IrBoolConst(1)
    }

    override fun toString(): String = if (value) "1" else "0"
}

val IrNullType = IrPointerType(IrVoidType)

class IrNullConst(type: IrType = IrNullType): IrValue(type), IrConstant {
    override fun toString(): String = "null"
}

class IrArrayConst(
    private val value: List<IrValue>
) : IrValue(IrArrayType(value.size, value.first().type)), IrConstant {

    override fun toString(): String {
        val seq = value
            .map { "${it.type} $it" }
            .reduce { acc, s -> "$acc, $s" }
        return "[ $seq ]"
    }
}

class IrZeroInit(type: IrType): IrValue(type) {
    override fun toString(): String = "zeroinitializer"
}
