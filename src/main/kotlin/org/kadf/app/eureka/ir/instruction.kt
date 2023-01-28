package org.kadf.app.eureka.ir

sealed interface IrInstruction
sealed interface IrTermination

class IrRetInst(
    block: IrBasicBlock,
    private val retValue: IrValue? = null
) : IrValue(IrVoidType), IrInstruction, IrTermination, IrUser {
    private val retType = retValue?.type ?: IrVoidType
    init {
        block.addInst(this)
        retValue?.addUser(this)
    }

    override val ir: String
        get() = retValue?.let { "ret $retType $retValue" }
            ?: run { "ret $retType" }
}

class IrBrInst(
    block: IrBasicBlock,
    private val cond: IrValue?,
    val thenBlock: IrBasicBlock,
    val elseBlock: IrBasicBlock? = null
) : IrValue(IrVoidType), IrInstruction, IrTermination, IrUser {
    val isConditional = cond != null

    init {
        block.addInst(this)
        cond?.addUser(this)
    }

    override val ir: String
        get() = cond?.let { "br i1 $it, label ${thenBlock.id.asLabel}, label ${elseBlock!!.id.asLabel}" }
            ?: run { "br label ${thenBlock.id.asLabel}" }
}

enum class IrBinaryOpt {
    ADD, SUB, MUL, SDIV, SREM,
    SHL, ASHR, AND, OR, XOR;

    override fun toString(): String = name.lowercase()
}

class IrBinaryInst(
    block: IrBasicBlock,
    id: IrVarIdent,
    private val opt: IrBinaryOpt,
    private val opd1: IrValue,
    private val opd2: IrValue
) : IrValue(opd1.type, id), IrInstruction, IrUser {
    init {
        block.addInst(this)
        opd1.addUser(this)
        opd2.addUser(this)
    }

    override val ir: String
        get() = "$id = $opt $type $opd1, $opd2"

}

class IrAllocaInst(
    block: IrBasicBlock,
    id: IrVarIdent,
    private val allocType: IrType
) : IrValue(allocType.asRef, id), IrInstruction {
    init {
        block.addInst(this)
    }

    override val ir: String
        get() {
            return if (type.isBoolPtr) "$id = alloca i8"
            else "$id = alloca $allocType"
        }
}

class IrLoadInst(
    block: IrBasicBlock,
    id: IrVarIdent,
    private val loadType: IrType,
    private val loadAddr: IrValue
) : IrValue(loadType, id), IrInstruction, IrUser {
    init {
        block.addInst(this)
        loadAddr.addUser(this)
    }

    override val ir: String
        get() = "$id = load $loadType, ${loadAddr.type} $loadAddr"
}

class IrStoreInst(
    block: IrBasicBlock,
    storeValue: IrValue,
    private val storeAddr: IrValue
) : IrValue(IrVoidType), IrInstruction, IrUser {
    private val storeType = storeValue.type
    init {
        block.addInst(this)
        storeValue.addUser(this)
        storeAddr.addUser(this)
    }

    private val storeValue: IrValue = when(storeValue) {
        is IrNullConst -> IrNullConst(storeAddr.deRefType)
        else -> storeValue
    }

    override val ir: String
        get() = "store ${storeValue.type} $storeValue, ${storeAddr.type} $storeAddr"
}

class IrGepInst(
    block: IrBasicBlock,
    id: IrVarIdent,
    type: IrType,
    private val ptr: IrValue,
    private val idx: List<IrValue>
) : IrValue(type.asRef, id), IrInstruction, IrUser {

    init {
        block.addInst(this)
        ptr.addUser(this)
        idx.forEach { it.addUser(this) }
    }

    constructor(
        block: IrBasicBlock,
        id: IrVarIdent,
        type: IrType,
        ptr: IrValue,
        vararg idx: IrValue
    ): this(
        block, id, type, ptr, listOf(*idx)
    )

    override val ir: String get(): String{
        return if (ptr.type.isBoolPtr) {
            "$id = getelementptr i8, ${ptr.type} $ptr" +
                    idx.fold("") { acc, it -> "$acc, i32 $it" }
        } else {
            "$id = getelementptr ${ptr.deRefType}, ${ptr.type} $ptr" +
                    idx.fold("") { acc, it -> "$acc, i32 $it" }
        }
    }
}

enum class IrICmpCond {
    EQ, NE, SGE, SGT, SLT, SLE;
    override fun toString(): String = name.lowercase()
}

class IrICmpInst(
    block: IrBasicBlock,
    id: IrVarIdent,
    private val cond: IrICmpCond,
    lhs: IrValue,
    rhs: IrValue
) : IrValue(IrBoolType, id), IrInstruction, IrUser {
    init {
        block.addInst(this)
        lhs.addUser(this)
        rhs.addUser(this)
    }

    private val lhs: IrValue = when {
        lhs is IrNullConst && rhs is IrNullConst -> IrNullConst(IrPointerType(IrIntType))
        lhs is IrNullConst -> IrNullConst(rhs.type)
        else -> lhs
    }
    private val rhs: IrValue = when {
        lhs is IrNullConst && rhs is IrNullConst -> IrNullConst(IrPointerType(IrIntType))
        rhs is IrNullConst -> IrNullConst(lhs.type)
        else -> rhs
    }

    override val ir: String
        get() = "$id = icmp $cond ${lhs.type} $lhs, $rhs"
}

class IrCallInst(
    block: IrBasicBlock,
    private val callId: IrVarIdent? = null,
    private val funcId: IrFuncIdent,
    private val retType: IrType,
    private val args: List<IrValue>
) : IrValue(retType, callId ?: IrAnonymousIdent), IrInstruction, IrUser {
    init {
        block.addInst(this)
        args.forEach { it.addUser(this) }
    }

    constructor(
        block: IrBasicBlock,
        id: IrVarIdent?,
        funcId: IrFuncIdent,
        retType: IrType,
        vararg args: IrValue
    ): this(
        block, id, funcId, retType, listOf(*args)
    )

    override val ir: String
        get() {
            val seq = args
                .map { "${it.type} $it" }
                .let {
                    if (it.isEmpty()) ""
                    else it.reduce { acc, s -> "$acc, $s" }
                }
            return callId?.let { "$callId = call $retType $funcId($seq)" }
                ?: run { "call void $funcId($seq)" }
        }
}

enum class IrConvOpt {
    TRUNC, ZEXT, BITCAST;
    override fun toString(): String = name.lowercase()
}
class IrConvInst(
    block: IrBasicBlock,
    id: IrVarIdent,
    private val opt: IrConvOpt,
    private val fromValue: IrValue,
    private val toType: IrType
): IrValue(toType, id), IrUser, IrInstruction {
    init {
        block.addInst(this)
        fromValue.addUser(this)
    }

    override val ir: String
        get() = "$id = $opt ${fromValue.type} $fromValue to $toType"
}

class IrPhiInst(
    block: IrBasicBlock,
    id: IrVarIdent,
    type: IrType
): IrValue(type, id), IrUser, IrInstruction {
    init {
        block.addInst(this)
    }
    private val pred = mutableMapOf<IrBasicBlock, IrValue>()
    fun addPred(value: IrValue, block: IrBasicBlock) {
        pred[block] = value
    }

    override val ir: String
        get() = "$id = phi $type " + pred
            .map { "[ ${it.value}, ${it.key.id.asLabel} ]" }
            .reduce { acc, s -> "$acc, $s" }
}
