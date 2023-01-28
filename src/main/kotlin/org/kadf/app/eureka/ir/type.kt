package org.kadf.app.eureka.ir

sealed interface IrType {
    val bytes: Int get() = 0
    val bits: Int get() = 0
}

val IrType.asRef get() = IrPointerType(this)
val IrType.deRef get() = if (this is IrPointerType) refType else this
val IrType.isBoolPtr get() = this is IrPointerType && refType is IrBoolType
val IrType.isIntPtr get() = this is IrPointerType && refType is IrIntType

// IRType / VoidType
object IrVoidType : IrType {
    override fun toString(): String = "void"
}

// IRType / FunctionType
class IrFunctionType(
    val retType: IrType,
    val paraTypes: List<IrType>
) : IrType {
    constructor(vararg types: IrType): this(types.first(), types.drop(1))
}

// IRType / FirstClassTypes / SingleValueTypes / IntegerType
open class IrIntegerType(
    private val len: Int
) : IrType {
    override fun toString(): String = "i$len"
    override val bits: Int = len
    override val bytes: Int = len / 8
}

object IrIntType : IrIntegerType(32)

object IrCharType : IrIntegerType(8)

object IrBoolType : IrIntegerType(1) {
    override val bits: Int = 8
    override val bytes: Int = 1
}


// IRType / FirstClassTypes / SingleValueTypes / PointerType
open class IrPointerType(val refType: IrType) : IrType {
    constructor(basicType: IrType, dim: Int): this(
        when(dim) {
            1 -> basicType
            else -> IrPointerType(basicType, dim - 1)
        }
    )
    override fun toString(): String = if (isBoolPtr) "i8*" else "$refType*"
    override val bytes = 8
    override val bits = 64

}

// IRType / FirstClassTypes / AggregateTypes / ArrayType
open class IrArrayType(
    private val len: Int,
    private val subtype: IrType
) : IrType {

    constructor(type: IrType, scale: List<Int>) : this(
        scale.first(),
        when (scale.size) {
            1 -> type
            else -> IrArrayType(type, scale.subList(1, scale.size))
        }
    )

    override fun toString(): String = "[$len x $subtype]"
    override val bits = subtype.bits * len
    override val bytes = subtype.bytes * len
}

val IrStringType = IrPointerType(IrCharType)

// IRType / FirstClassTypes / AggregateTypes / StructureType
class IrStructType(
    private val subtype: List<IrType>
) : IrType {

    private val offsets = mutableListOf<Int>()
    private var size = 0
    init {
        subtype.forEach {
            while(size % it.bytes != 0) size++
            offsets.add(size)
            size += it.bytes
        }
    }

    override fun toString(): String {
        assert(subtype.isNotEmpty())
        val seq = subtype
            .map { if (it is IrBoolType) "i8" else "$it" }
            .reduce { acc, s -> "$acc, $s" }
        return "{ $seq }"
    }

    override val bytes = size
    override val bits = size * 8
    fun offsetOf(idx: Int) = offsets[idx]
}

class IrTypeAlias(
    val typeId: IrTypeIdent
): IrType {
    constructor(id: String): this(IrTypeIdent(id))

    override fun toString(): String = "$typeId"
}
