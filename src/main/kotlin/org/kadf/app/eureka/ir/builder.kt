package org.kadf.app.eureka.ir

import org.kadf.app.eureka.ast.*
import org.kadf.app.eureka.ast.nodes.*

object SSA {
    private val namingTable = mutableMapOf<String, Int>()
    private fun rename(id: String = ""): String {
        val counter = namingTable[id] ?: 0
        namingTable[id] = counter + 1
        return "${id}.${counter}"
    }

    fun localIdent(id: String) = IrVarIdent(rename(id), false)
    fun globalIdent(id: String) = IrVarIdent(id, true)
    val localIdent get() = IrVarIdent(rename(), false)
    val globalIdent get() = IrVarIdent(rename(), true)

    fun constrIdent(type: AstType) = IrFuncIdent("constr.", type)
    val constrRetType get() = IrVoidType
    fun constrParaList(type: IrType) = listOf(IrParameter(thisIdent, type))

    val gInitFuncIdent get() = IrFuncIdent("__mx_glb.init")
    val gInitFuncRetType get() = IrVoidType
    val gInitFuncParaList get() = listOf<IrParameter>()

    val funcEntryIdent get() = IrBasicBlockIdent(rename("fn.entry"))
    val funcExitIdent get() = IrBasicBlockIdent(rename("fn.exit"))
    val funcBodyIdent get() = IrBasicBlockIdent(rename("fn.body"))

    val thisIdent get() = IrVarIdent(".this", false)
    val retIdent get() = IrVarIdent(".ret", false)

    val branCondIdent get() = IrBasicBlockIdent(rename("br.cond"))
    val branThenIdent get() = IrBasicBlockIdent(rename("br.then"))
    val branElseIdent get() = IrBasicBlockIdent(rename("br.else"))
    val branExitIdent get() = IrBasicBlockIdent(rename("br.exit"))

    val loopCondIdent get() = IrBasicBlockIdent(rename("lp.cond"))
    val loopIterIdent get() = IrBasicBlockIdent(rename("lp.iter"))
    val loopBodyIdent get() = IrBasicBlockIdent(rename("lp.body"))
    val loopExitIdent get() = IrBasicBlockIdent(rename("lp.exit"))

    val logicCondIdent get() = IrBasicBlockIdent(rename("lg.cond"))
    val logicExitIdent get() = IrBasicBlockIdent(rename("lg.exit"))

    val strLiteralIdent get() = globalIdent(rename("__mx_str"))

    val mallocFuncIdent get() = IrFuncIdent("malloc")
    val mallocFuncRetType get() = IrPointerType(IrCharType)

    val newArrayFuncIdent get() = IrFuncIdent(rename("arr.new"))

    fun strBinIdent(opt: AstOperator) = IrFuncIdent(
        when(opt) {
            AstOperator.PLUS -> "str.cat"
            AstOperator.EQ -> "str.eq"
            AstOperator.NE -> "str.neq"
            AstOperator.LT -> "str.lt"
            AstOperator.LE -> "str.le"
            AstOperator.GT -> "str.gt"
            AstOperator.GE -> "str.ge"
            else -> throw Exception()
        }
    )
    fun strBinRetType(opt: AstOperator) = when(opt) {
        AstOperator.PLUS -> IrPointerType(IrCharType)
        else -> IrBoolType
    }

}

class IrBuilder(root: AstNode) : ASTVisitor {

    fun build(node: AstNode): IrModule {
        (node as AstProgramNode).accept(this)
        return ctx.module
    }

    private val ctx = object {
        val tEnv = root.env.tEnv
        val module: IrModule = IrModule()

        lateinit var retAddr: IrValue
        lateinit var thisAddr: IrValue

        lateinit var func: IrFunction
        lateinit var block: IrBasicBlock
        lateinit var gFunc: IrFunction
        lateinit var gBody: IrBasicBlock
        lateinit var gBlock: IrBasicBlock
        lateinit var gEntry: IrBasicBlock

        lateinit var entry: IrBasicBlock
        lateinit var body: IrBasicBlock
        lateinit var exit: IrBasicBlock
        lateinit var continueTarget: IrBasicBlock
        lateinit var breakTarget: IrBasicBlock
    }

    private fun loadFrom(addr: IrValue, block: IrBasicBlock = ctx.block): IrValue {
        return if (addr.type.isBoolPtr) {
            IrConvInst(
                block, SSA.localIdent, IrConvOpt.TRUNC,
                IrLoadInst(block, SSA.localIdent, IrCharType, addr), IrBoolType
            )
        } else {
            IrLoadInst(block, SSA.localIdent, addr.deRefType, addr)
        }
    }

    private fun IrValue.storeInto(addr: IrValue, block: IrBasicBlock = ctx.block): IrValue {
        return if (addr.type.isBoolPtr) {
            val conv = IrConvInst(block, SSA.localIdent, IrConvOpt.ZEXT, this, IrCharType)
            IrStoreInst(block, conv, addr)
        } else {
            IrStoreInst(block, this, addr)
        }
    }
    private fun ValueInfo.storeInto(addr: IrValue, block: IrBasicBlock = ctx.block): IrValue {
        return extract.storeInto(addr, block)
    }
    private fun Any.storeInto(addr: IrValue, block: IrBasicBlock = ctx.block): IrValue {
        return extract.storeInto(addr, block)
    }

    class ValueInfo(
        val value: IrValue,
        val isRef: Boolean
    ) {
        val type = value.type
        val deRefType = value.deRefType
        val asRefType = value.asRefType
    }

    private val ValueInfo.extract get() = if (isRef) loadFrom(value) else value
    private val Any.extract get() = (this as ValueInfo).extract
    private val Any.value get() = (this as ValueInfo).value

    private fun convertType(type: AstType): IrType = when(type) {
        is AstUnitType -> IrVoidType
        is AstIntegerType -> IrIntType
        is AstBooleanType -> IrBoolType
        is AstNullType -> IrPointerType(IrVoidType)
        is AstStringType -> IrPointerType(IrCharType)
        is AstUserDefType -> IrPointerType(IrTypeAlias(type.typeId))
        is AstArrayType -> IrPointerType(convertType(type.type), type.dim)
        is AstFuncType -> IrFunctionType(convertType(type.retType), type.paraTypes.map { convertType(it) })
        else -> IrVoidType
    }

    override fun visit(node: AstProgramNode) {
        // setup global init environment
        ctx.gFunc = IrFunction(
            ctx.module,
            SSA.gInitFuncIdent,
            SSA.gInitFuncRetType,
            SSA.gInitFuncParaList
        )
        ctx.gEntry = IrBasicBlock(ctx.gFunc, SSA.funcEntryIdent)
        ctx.gBody = IrBasicBlock(ctx.gFunc, SSA.funcBodyIdent)
        ctx.gBlock = ctx.gBody
        // traverse
        node.decls.forEach { it.accept(this) }
        // close global init environment
        IrBrInst(ctx.gEntry, null, ctx.gBody)
        IrRetInst(ctx.gBlock)
    }

    override fun visit(node: AstClassDeclNode) {
        val typeId = node.type.typeId
        val tBinding = ctx.tEnv.lookupType(node.type)!!
        // type aliasing
        ctx.module.addAlias(
            IrTypeIdent(typeId),
            IrStructType(tBinding.propTypes.map { convertType(it) })
        )
        // building member functions
        node.member
            .filterIsInstance<AstConstrNode>()
            .forEach { it.accept(this) }
        node.member
            .filterIsInstance<AstFuncDeclNode>()
            .forEach { it.accept(this) }
    }

    override fun visit(node: AstConstrNode) {
        val type = convertType(node.memberOf!!)
        ctx.func = IrFunction(
            ctx.module,
            SSA.constrIdent(node.memberOf!!),
            SSA.constrRetType,
            SSA.constrParaList(type),
        )
        ctx.entry = IrBasicBlock(ctx.func, SSA.funcEntryIdent)
        ctx.thisAddr = IrAllocaInst(ctx.entry, SSA.localIdent, type)
        ctx.func.para.first().storeInto(ctx.thisAddr, ctx.entry)
        ctx.block = IrBasicBlock(ctx.func, SSA.funcBodyIdent)
        IrBrInst(ctx.entry, null, ctx.block)
        node.body.forEach { it.accept(this) }
        if (!ctx.block.terminated) IrRetInst(ctx.block)
    }

    override fun visit(node: AstFuncDeclNode) {
        val retType = convertType(node.retType)
        val paraList = node.paraTypes
            .let { if (node.isGlobal) it else listOf(node.memberOf!!) + it }
            .map { IrParameter(SSA.localIdent, convertType(it)) }
        val paraIds = node.paraIds
            .map { SSA.localIdent(it) }
            .let { if (node.isGlobal) it else listOf(SSA.thisIdent) + it }
        // 1. construct Function
        ctx.func = IrFunction(
            ctx.module,
            IrFuncIdent(node.funcId, node.memberOf),
            retType, paraList
        )
        // 2. function entry block
        // 2.1 construct entry block
        ctx.entry = IrBasicBlock(ctx.func, SSA.funcEntryIdent)
        // 2.2 allocate reg for return value
        if (node.retType !is AstUnitType) {
            ctx.retAddr = IrAllocaInst(ctx.entry, SSA.retIdent, retType)
            if (node.isMainFunc) {
                IrStoreInst(ctx.entry, IrIntConst(0), ctx.retAddr)
            }
        }
        // 2.3 allocate regs for arguments and store their value
        paraIds.zip(paraList)
            .map {
                // allocate regs and store their value
                val alloca = IrAllocaInst(ctx.entry, it.first, it.second.type)
                it.second.storeInto(alloca, ctx.entry); alloca
            }
            .let {
                // save reg for <this> ptr
                if (node.isGlobal) it
                else { ctx.thisAddr = it.first(); it.drop(1) }
            }
            .zip(node.paraIds)
            .forEach {
                // address registry
                node.env.setVarValue(it.second, it.first)
            }
        // 3. function exit block
        ctx.exit = IrBasicBlock(ctx.func, SSA.funcExitIdent)
        if (node.retType is AstUnitType) {
            IrRetInst(ctx.exit)
        } else {
            IrRetInst(ctx.exit, loadFrom(ctx.retAddr, ctx.exit))
        }
        // 4. function body blocks
        ctx.body = IrBasicBlock(ctx.func, SSA.funcBodyIdent)
        if (node.isMainFunc) {
            IrCallInst(ctx.body, null, SSA.gInitFuncIdent, SSA.gInitFuncRetType)
        }
        ctx.block = ctx.body
        node.body.forEach { it.accept(this) }
        // 5. terminating all blocks
        IrBrInst(ctx.entry, null, ctx.body)
        if (!ctx.block.terminated) IrBrInst(ctx.block, null, ctx.exit)
    }


    override fun visit(node: AstVarDeclNode) {
        val declType = convertType(node.type)
        if (node.isGlobal) {
            node.declInfo
                .forEach {
                    val ident = SSA.globalIdent(it.first)
                    val assign = IrGlobalAssign(ident, IrZeroInit(declType))
                    // value registry
                    node.env.setVarValue(it.first, assign)
                    // add global variable into module
                    ctx.module.addGlobal(ident, assign)
                    // initialization
                    it.second?.let { init ->
                        ctx.func = ctx.gFunc
                        ctx.block = ctx.gBlock
                        ctx.entry = ctx.gEntry
                        init.accept(this).storeInto(assign)
                        ctx.gBlock = ctx.block
                    }
                }
        } else {
            node.declInfo
                .forEach {
                    val alloca = IrAllocaInst(
                        ctx.entry, SSA.localIdent(it.first), declType
                    )
                    // value registry
                    node.env.setVarValue(it.first, alloca)
                    // initialization
                    it.second?.accept(this)?.storeInto(alloca)
                }
        }
    }

    override fun visit(node: AstEmptyStmtNode) = Unit

    override fun visit(node: AstLambdaExprNode) = Unit

    override fun visit(node: AstBlockStmtNode) { node.stmts.forEach { it.accept(this) } }

    override fun visit(node: AstVarDeclStmtNode) { node.decl.accept(this) }

    override fun visit(node: AstExprStmtNode) { node.expr.accept(this) }

    override fun visit(node: AstBranchStmtNode) {
        // current block -> cond block
        val condBlock = IrBasicBlock(ctx.func, SSA.branCondIdent)
        IrBrInst(ctx.block, null, condBlock)
        ctx.block = condBlock
        val cond = node.cond.accept(this).extract
        // cond block -> then/else block -> exit block
        val thenBlock = IrBasicBlock(ctx.func, SSA.branThenIdent)
        val elseBlock = IrBasicBlock(ctx.func, SSA.branElseIdent)
        val exitBlock = IrBasicBlock(ctx.func, SSA.branExitIdent)
        IrBrInst(ctx.block, cond, thenBlock, elseBlock)
        ctx.block = thenBlock
        node.thenBody.accept(this)
        if (!ctx.block.terminated) IrBrInst(ctx.block, null, exitBlock)
        ctx.block = elseBlock
        node.elseBody?.accept(this)
        if (!ctx.block.terminated) IrBrInst(ctx.block, null, exitBlock)
        ctx.block = exitBlock
    }

    override fun visit(node: AstForLoopStmtNode) {
        node.init?.accept(this)
        val condBlock = IrBasicBlock(ctx.func, SSA.loopCondIdent)
        val bodyBlock = IrBasicBlock(ctx.func, SSA.loopBodyIdent)
        val iterBlock = IrBasicBlock(ctx.func, SSA.loopIterIdent)
        val exitBlock = IrBasicBlock(ctx.func, SSA.loopExitIdent)
        IrBrInst(ctx.block, null, condBlock)
        ctx.block = condBlock
        val cond = node.cond?.accept(this)?.extract ?: IrBoolConst(true)
        IrBrInst(ctx.block, cond, bodyBlock, exitBlock)
        ctx.breakTarget = exitBlock
        ctx.continueTarget = iterBlock
        ctx.block = bodyBlock
        node.body.accept(this)
        if (!ctx.block.terminated) IrBrInst(ctx.block, null, iterBlock)
        ctx.block = iterBlock
        node.iter?.accept(this)
        if (!ctx.block.terminated) IrBrInst(ctx.block, null, condBlock)
        ctx.block = exitBlock
    }

    override fun visit(node: AstWhileLoopStmtNode) {
        val condBlock = IrBasicBlock(ctx.func, SSA.loopCondIdent)
        val bodyBlock = IrBasicBlock(ctx.func, SSA.loopBodyIdent)
        val exitBlock = IrBasicBlock(ctx.func, SSA.loopExitIdent)
        IrBrInst(ctx.block, null, condBlock)
        ctx.block = condBlock
        val cond = node.cond.accept(this).extract
        IrBrInst(ctx.block, cond, bodyBlock, exitBlock)
        ctx.breakTarget = exitBlock
        ctx.continueTarget = condBlock
        ctx.block = bodyBlock
        node.body.accept(this)
        if (!ctx.block.terminated) IrBrInst(ctx.block, null, condBlock)
        ctx.block = exitBlock
    }

    override fun visit(node: AstControlStmtNode) = when (node.ctrl) {
        "continue" -> IrBrInst(ctx.block, null, ctx.continueTarget)
        "break" -> IrBrInst(ctx.block, null, ctx.breakTarget)
        else -> throw Exception()
    }

    override fun visit(node: AstReturnStmtNode) {
        node.value?.accept(this)?.storeInto(ctx.retAddr)
        IrBrInst(ctx.block, null, ctx.exit)
    }

    private val IrValue.asRefInfo get() = ValueInfo(this, true)
    private val IrValue.asValInfo get() = ValueInfo(this, false)

    private fun String.parse(): List<Int> {
        return when {
            isEmpty() -> listOf()
            this[0] == '\\' -> when(this[1]) {
                't' -> listOf('\t'.code) + drop(2).parse()
                'n' -> listOf('\n'.code) + drop(2).parse()
                '\\' -> listOf('\\'.code) + drop(2).parse()
                else -> throw Exception()
            }
            else -> listOf(this[0].code) + drop(1).parse()
        }
    }

    override fun visit(node: AstLiteralNode): ValueInfo = when (node.type) {
        is AstIntegerType -> IrIntConst(node.literal.toInt()).asValInfo

        is AstBooleanType -> IrBoolConst(node.literal.toBoolean()).asValInfo

        is AstStringType -> {
            val value = node.literal
                .let { it.substring(1, it.lastIndex) } // trim " at start & end
//                .encodeToByteArray()
                .parse() // convert string to byte array
                .let { it + listOf(0) } // append \0
                .map { IrCharConst(it) }
                .let { IrArrayConst(it) }
            val assign = IrConstAssign(SSA.strLiteralIdent, value)
            ctx.module.addString(SSA.strLiteralIdent, assign)
            IrGepInst(
                ctx.block, SSA.localIdent,
                IrCharType, assign,
                IrIntConst(0), IrIntConst(0)
            ).asValInfo
        }

        is AstAnyType -> ctx.thisAddr.asRefInfo
        is AstNullType -> IrNullConst().asValInfo
        else -> throw Exception()
    }

    override fun visit(node: AstVariableExprNode): ValueInfo {
        return if (node.isMember) {
            val idx = ctx.tEnv.getPropIdx(node.memberOf!!, node.id)!!
            val obj = loadFrom(ctx.thisAddr)
            IrGepInst(
                ctx.block, SSA.localIdent, convertType(node.astType), obj,
                IrIntConst(0), IrIntConst(idx)
            ).asRefInfo
        } else {
            node.env.getVarValue(node.id)!!.asRefInfo
        }
    }

    override fun visit(node: AstInvocationExprNode): ValueInfo {
        val retType = convertType(node.astType)
        val paraType = node.env
            .getFunType(node.funcId)!!
            .paraTypes
            .map { convertType(it) }
        return IrCallInst(
            ctx.block,
            if (retType is IrVoidType) null else SSA.localIdent,
            IrFuncIdent(node.funcId, node.memberOf),
            retType,
            node.args
                .map { it.accept(this).extract }
                .mapIndexed { idx, it ->
                    if (it is IrNullConst) IrNullConst(paraType[idx]) else it
                }
                .let { if (node.isMember) listOf(loadFrom(ctx.thisAddr)) + it else it }
        ).asValInfo
    }

    override fun visit(node: AstPropertyExprNode): ValueInfo {
//        val nodes = node.collect()
//        val obj = nodes.first().obj.accept(this).extract
//        val prop = nodes.map {
//            IrIntConst(ctx.tEnv.getPropIdx(it.obj.astType, it.id)!!)
//        }
        val obj = node.obj.accept(this).extract
        val prop = IrIntConst(ctx.tEnv.getPropIdx(node.obj.astType, node.id)!!)
        return IrGepInst(
            ctx.block, SSA.localIdent, convertType(node.astType), obj,
            IrIntConst(0), prop
        ).asRefInfo
    }

    override fun visit(node: AstMethodExprNode): ValueInfo {
        val retType = convertType(node.astType)
        return when {
            node.obj.astType is AstStringType && node.funcId == "length" -> {
                IrCallInst(
                    ctx.block, SSA.localIdent,
                    IrFuncIdent("str.len", node.obj.astType),
                    IrIntType, node.obj.accept(this).extract
                ).asValInfo
            }
            node.obj.astType is AstStringType && node.funcId == "substring" -> {
                IrCallInst(
                    ctx.block, SSA.localIdent,
                    IrFuncIdent("str.sub", node.obj.astType),
                    IrStringType,
                    listOf(node.obj.accept(this).extract) +
                            node.args.map { it.accept(this).extract }
                ).asValInfo
            }
            node.obj.astType is AstStringType && node.funcId == "parseInt" -> {
                IrCallInst(
                    ctx.block, SSA.localIdent,
                    IrFuncIdent("str.parse", node.obj.astType),
                    IrIntType, node.obj.accept(this).extract
                ).asValInfo
            }
            node.obj.astType is AstStringType && node.funcId == "ord" -> {
                IrCallInst(
                    ctx.block, SSA.localIdent,
                    IrFuncIdent("str.ord", node.obj.astType),
                    IrIntType,
                    listOf(node.obj.accept(this).extract) +
                            node.args.map { it.accept(this).extract }
                ).asValInfo
            }
            node.obj.astType is AstArrayType && node.funcId == "size" -> {
                val conv = IrConvInst(
                    ctx.block, SSA.localIdent,
                    IrConvOpt.BITCAST, node.obj.accept(this).extract,
                    IrPointerType(IrIntType)
                )
                IrCallInst(
                    ctx.block, SSA.localIdent,
                    IrFuncIdent("arr.size", node.obj.astType),
                    IrIntType, conv
                ).asValInfo
            }
            else -> {
                val paraType = ctx.tEnv.lookupMeth(node.obj.astType, node.funcId)!!
                    .paraTypes
                    .map { convertType(it) }
                val paraList = node.args
                    .map { it.accept(this).extract }
                    .mapIndexed { idx, it ->
                        if (it is IrNullConst) IrNullConst(paraType[idx]) else it
                    }
                IrCallInst(
                    ctx.block,
                    if (retType is IrVoidType) null else SSA.localIdent,
                    IrFuncIdent(node.funcId, node.obj.astType),
                    convertType(node.astType),
                    listOf(node.obj.accept(this).extract) + paraList

                ).asValInfo
            }
        }
    }

    override fun visit(node: AstIndexExprNode): ValueInfo {
//        val nodes = node.collect()
//        val obj = nodes.first().arr.accept(this).extract
//        val idx = nodes.map {
//            it.idx.accept(this).extract
//        }
        val obj = node.arr.accept(this).extract
        val idx = node.idx.accept(this).extract
        return IrGepInst(
            ctx.block, SSA.localIdent,
            convertType(node.astType), obj, idx
        ).asRefInfo
    }

    override fun visit(node: AstAssignExprNode): ValueInfo {
        val rhs = node.rhs.accept(this) as ValueInfo
        val lhs = node.lhs.accept(this) as ValueInfo
        rhs.extract.storeInto(lhs.value)
        return lhs
    }

    override fun visit(node: AstCompoundExprNode): ValueInfo {
        node.rest.forEach { it.accept(this) }
        return node.last.accept(this) as ValueInfo
    }

    fun newArray(type: IrType, scale: List<IrValue?>): IrValue {
        val len = scale.first()!!
        val size = IrBinaryInst(
            ctx.block, SSA.localIdent,
            IrBinaryOpt.ADD, IrIntConst(IrIntType.bytes),
            IrBinaryInst(
                ctx.block, SSA.localIdent, IrBinaryOpt.MUL, len,
                IrIntConst(type.deRef.bytes)
            )
        )
        val malloc = IrCallInst(
            ctx.block, SSA.localIdent,
            SSA.mallocFuncIdent, SSA.mallocFuncRetType, size
        )
        val conv2int = IrConvInst(
            ctx.block, SSA.localIdent,
            IrConvOpt.BITCAST, malloc, IrPointerType(IrIntType)
        )
        len.storeInto(conv2int)
        val move = IrGepInst(
            ctx.block, SSA.localIdent,
            IrIntType, conv2int, IrIntConst(1)
        )
        val head: IrValue = if (type.isIntPtr) move
        else IrConvInst(
            ctx.block, SSA.localIdent,
            IrConvOpt.BITCAST, move, type
        )
        val updScale = scale.drop(1)
        if (updScale.isNotEmpty() && updScale.first() != null) {
            val counter = IrAllocaInst(ctx.entry, SSA.localIdent, IrIntType)
            IrIntConst(0).storeInto(counter, ctx.entry)
            val condBlk = IrBasicBlock(ctx.func, SSA.loopCondIdent)
            val iterBlk = IrBasicBlock(ctx.func, SSA.loopIterIdent)
            val bodyBlk = IrBasicBlock(ctx.func, SSA.loopBodyIdent)
            val exitBlk = IrBasicBlock(ctx.func, SSA.loopExitIdent)
            IrBrInst(ctx.block, null, condBlk)
            // cond block
            val cond = IrICmpInst(
                condBlk, SSA.localIdent,
                IrICmpCond.SLT, loadFrom(counter, condBlk), len
            )
            IrBrInst(condBlk, cond, bodyBlk, exitBlk)
            // iter block
            IrBinaryInst(
                iterBlk, SSA.localIdent,
                IrBinaryOpt.ADD, loadFrom(counter, iterBlk), IrIntConst(1)
            ).storeInto(counter, iterBlk)
            IrBrInst(iterBlk, null, condBlk)
            // body block
            ctx.block = bodyBlk
            val ptr = newArray(type.deRef, updScale)
            val addr = IrGepInst(
                ctx.block, SSA.localIdent,
                ptr.type, head, loadFrom(counter)
            )
            ptr.storeInto(addr)
            IrBrInst(ctx.block, null, iterBlk)
            ctx.block = exitBlk
        }
        return head
    }

    override fun visit(node: AstNewArrayNode): ValueInfo {
        val idx = node.scales.map { it?.accept(this)?.extract }
        return if (idx.isEmpty() || idx.first() == null) {
            IrNullConst(convertType(node.type)).asValInfo
        } else {
            newArray(convertType(node.type), idx).asValInfo
        }
    }

    override fun visit(node: AstNewObjectNode): ValueInfo {
        return when(node.type) {
            is AstUserDefType -> {
                val type = convertType(node.type)
                val malloc = IrCallInst(
                    ctx.block, SSA.localIdent,
                    SSA.mallocFuncIdent,
                    SSA.mallocFuncRetType,
                    IrIntConst(type.bytes)
                )
                val conv = IrConvInst(
                    ctx.block, SSA.localIdent,
                    IrConvOpt.BITCAST, malloc, type
                )
                if (ctx.tEnv.lookupType(node.type)!!.hasConstr) {
                    IrCallInst(
                        ctx.block, null,
                        SSA.constrIdent(node.type),
                        SSA.constrRetType, conv
                    )
                }
                conv.asValInfo
            }
            else -> throw Exception()
        }
    }

    private fun convertOpt(opt: AstOperator): IrBinaryOpt = when(opt) {
        AstOperator.PRE_DEC, AstOperator.POST_DEC, AstOperator.MINUS, AstOperator.NEG -> IrBinaryOpt.SUB
        AstOperator.PRE_INC, AstOperator.POST_INC, AstOperator.PLUS -> IrBinaryOpt.ADD
        AstOperator.MUL -> IrBinaryOpt.MUL
        AstOperator.DIV -> IrBinaryOpt.SDIV
        AstOperator.MOD -> IrBinaryOpt.SREM
        AstOperator.XOR, AstOperator.NOT, AstOperator.LOGIC_NOT -> IrBinaryOpt.XOR
        AstOperator.LSH -> IrBinaryOpt.SHL
        AstOperator.RSH -> IrBinaryOpt.ASHR
        AstOperator.AND -> IrBinaryOpt.AND
        AstOperator.OR -> IrBinaryOpt.OR
        else -> throw Exception()
    }

    private fun convertCond(opt: AstOperator): IrICmpCond = when(opt) {
        AstOperator.EQ -> IrICmpCond.EQ
        AstOperator.NE -> IrICmpCond.NE
        AstOperator.LT -> IrICmpCond.SLT
        AstOperator.LE -> IrICmpCond.SLE
        AstOperator.GT -> IrICmpCond.SGT
        AstOperator.GE -> IrICmpCond.SGE
        else -> throw Exception()
    }

    override fun visit(node: AstUnaryExprNode): ValueInfo  {
        val obj = node.expr.accept(this) as ValueInfo
        return when (node.op) {
            AstOperator.PRE_INC, AstOperator.PRE_DEC -> {
                IrBinaryInst(
                    ctx.block, SSA.localIdent,
                    convertOpt(node.op), obj.extract, IrIntConst(1)
                ).storeInto(obj.value)
                obj
            }

            AstOperator.POST_INC, AstOperator.POST_DEC -> {
                val data = obj.extract
                IrBinaryInst(
                    ctx.block, SSA.localIdent,
                    convertOpt(node.op),
                    data, IrIntConst(1)
                ).storeInto(obj.value)
                data.asValInfo
            }

            AstOperator.POSIT -> obj.extract.asValInfo

            AstOperator.NEG -> IrBinaryInst(
                ctx.block, SSA.localIdent,
                IrBinaryOpt.SUB,
                IrIntConst(0), obj.extract
            ).asValInfo

            AstOperator.NOT -> IrBinaryInst(
                ctx.block, SSA.localIdent,
                IrBinaryOpt.XOR,
                obj.extract, IrIntConst.maxValue
            ).asValInfo

            AstOperator.LOGIC_NOT -> IrBinaryInst(
                ctx.block, SSA.localIdent,
                IrBinaryOpt.XOR,
                obj.extract, IrBoolConst.maxValue
            ).asValInfo

            else -> throw Exception()
        }
    }

    override fun visit(node: AstBinaryExprNode): ValueInfo {
        return when (node.lhs.astType) {
            is AstStringType -> IrCallInst(
                ctx.block, SSA.localIdent,
                SSA.strBinIdent(node.op),
                SSA.strBinRetType(node.op),
                node.lhs.accept(this).extract,
                node.rhs.accept(this).extract
            ).asValInfo

            is AstIntegerType -> when (node.op) {
                AstOperator.LT, AstOperator.GT, AstOperator.LE,
                AstOperator.GE, AstOperator.EQ, AstOperator.NE -> IrICmpInst(
                    ctx.block, SSA.localIdent,
                    convertCond(node.op),
                    node.lhs.accept(this).extract,
                    node.rhs.accept(this).extract
                ).asValInfo

                else -> IrBinaryInst(
                    ctx.block, SSA.localIdent,
                    convertOpt(node.op),
                    node.lhs.accept(this).extract,
                    node.rhs.accept(this).extract
                ).asValInfo
            }

            is AstBooleanType -> when (node.op) {
                AstOperator.EQ, AstOperator.NE -> IrICmpInst(
                    ctx.block, SSA.localIdent,
                    convertCond(node.op),
                    node.lhs.accept(this).extract,
                    node.rhs.accept(this).extract
                ).asValInfo

                else -> {
                    val branches = node.collectLeftAssoc()
                    val condBlocks = List(branches.size) {
                        IrBasicBlock(ctx.func, SSA.logicCondIdent)
                    }
                    val exit = IrBasicBlock(ctx.func, SSA.logicExitIdent)
                    val phi = IrPhiInst(exit, SSA.localIdent, IrBoolType)
                    IrBrInst(ctx.block, null, condBlocks.first())
                    branches.forEachIndexed { idx, br ->
                        val block = condBlocks[idx]
                        ctx.block = block
                        val cond = br.accept(this).extract
                        phi.addPred(cond, ctx.block)
                        if (idx == condBlocks.lastIndex) {
                            IrBrInst(ctx.block, null, exit)
                        } else {
                            if (node.op == AstOperator.LOGIC_AND) {
                                IrBrInst(ctx.block, cond, condBlocks[idx + 1], exit)
                            }
                            else {
                                IrBrInst(ctx.block, cond, exit, condBlocks[idx + 1])
                            }
                        }
                    }
                    ctx.block = exit
                    phi.asValInfo
                }
            }

            is AstNullType, is AstArrayType, is AstUserDefType -> IrICmpInst(
                ctx.block, SSA.localIdent,
                convertCond(node.op),
                node.lhs.accept(this).extract,
                node.rhs.accept(this).extract
            ).asValInfo

            else -> throw Exception()
        }
    }

}
