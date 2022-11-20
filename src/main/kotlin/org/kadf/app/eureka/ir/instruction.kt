package org.kadf.app.eureka.ir


sealed interface IInstruction

sealed interface ITerminator : IInstruction

class RetInst(
    val retType: Type,
    val retVal: Value
) : Value(VoidType), IInstruction, IUser {
    init {
        retVal.addUser(this)
    }

    override val ir: String
        get() = "ret $retType $retVal"
}

class BrInst(
    val cond: BoolConst?,
    val thenDest: Label,
    val elseDest: Label? = null
) : Value(VoidType), IInstruction, IUser {
    val isConditional = cond != null

    init {
        cond?.addUser(this)
    }

    override val ir: String
        get() = cond?.let { "br $it, $thenDest, $elseDest" }
            ?: run { "br $thenDest" }
}

sealed interface IBinary : IInstruction

enum class BinaryOpt(val opt: String) {
    ADD("add"),
    SUB("sub"),
    MUL("mul"),
    UDIV("udiv"),
    SDIV("sdiv"),
    UREM("urem"),
    SREM("srem"),

    SHL("shl"),
    LSHR("lshr"),
    ASHL("ashl"),
    AND("and"),
    OR("or"),
    XOR("xor");

    override fun toString(): String = opt
}

class BinaryInst(
    val opt: BinaryOpt,
    id: String,
    type: Type,
    val opd1: Value,
    val opd2: Value
) : Value(type, id), IInstruction, IUser {
    init {
        opd1.addUser(this)
        opd2.addUser(this)
    }

    override val ir: String
        get() = "$id = $opt $type $opd1, $opd2"

}

class AllocaInst(
    id: String,
    val allocType: Type
) : Value(PointerType, id), IInstruction {
    override val ir: String
        get() = "$id = alloca $allocType"
}

class LoadInst(
    id: String,
    val loadType: Type,
    val loadAddr: Value
) : Value(loadType, id), IInstruction, IUser {
    init {
        loadAddr.addUser(this)
    }

    override val ir: String
        get() = "$id = load $loadType, ptr $loadAddr"
}

class StoreInst(
    val storeValue: Value,
    val storeAddr: Value
) : Value(VoidType), IInstruction, IUser {
    init {
        storeValue.addUser(this)
        storeAddr.addUser(this)
    }

    override val ir: String
        get() = "store ${storeValue.type} $storeValue, ptr $storeAddr"
}

class GetElementPtrInst(
    id: String,
    type: Type,
    val ptr: Value,
    val idx: Value
) : Value(type, id), IInstruction, IUser {
    init {
        ptr.addUser(this)
        idx.addUser(this)
    }

    override val ir: String
        get() = "$id = getelementptr $type, ptr $ptr, ${idx.type} $idx"
}

enum class ICmpCond(val cond: String) {
    EQ("eq"),
    NE("ne"),
    UGT("ugt"),
    UGE("uge"),
    ULT("ult"),
    ULE("ule"),
    SGT("sgt"),
    SLT("slt"),
    SLE("sle");

    override fun toString(): String = cond
}

class ICmpInst(
    val cond: ICmpCond,
    id: String,
    val cmpType: Type,
    val opd1: Value,
    val opd2: Value
) : Value(BoolType, id), IInstruction, IUser {
    init {
        opd1.addUser(this)
        opd2.addUser(this)
    }

    override val ir: String
        get() = "$id = icmp $cond $cmpType $opd1, $opd2"
}

class CallInst(
    id: String?,
    val func: Function,
    val argList: List<Value>
) : Value(func.funcType.retType, id), IInstruction, IUser {
    init {
        func.addUser(this)
        argList.forEach { it.addUser(this) }
    }

    override val ir: String
        get() {
            val seq = argList
                .map { "${it.type} $it" }
                .reduce { acc, s -> "$acc, $s" }
            return if (id != null) "$id = call $type $func($seq)"
            else "call $type $func($seq)"
        }
}
//sealed interface IUnary : IInstruction

//sealed interface IBitwiseBinary : IInstruction

//sealed interface IMemory : IInstruction

//sealed interface IAggregate : IInstruction
