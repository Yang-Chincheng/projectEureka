package org.kadf.app.eureka.ir

class IrComment(private val msg: String) {
    override fun toString(): String = "; $msg"
}

class IrModule {
    private val func = mutableMapOf<IrIdent, IrFunction>()
    private val alias = mutableMapOf<IrIdent, IrType>()
    private val str = mutableMapOf<IrIdent, IrValue>()
    private val glb = mutableMapOf<IrIdent, IrValue>()

    fun addFunc(vararg functions: IrFunction) {
        functions.forEach { func[it.funcId] = it }
    }

    fun addAlias(id: IrTypeIdent, type: IrType) {
        alias[id] = type
    }

    fun addGlobal(id: IrVarIdent, value: IrValue) {
        glb[id] = value
    }

    fun addString(id: IrVarIdent, value: IrValue) {
        str[id] = value
    }

    val ir: String
        get() {
            val builtin =
                "declare void @__mx_builtin_printInt(i32)\n" +
                "declare void @__mx_builtin_printlnInt(i32)\n" +
                "declare void @__mx_builtin_print(i8*)\n" +
                "declare void @__mx_builtin_println(i8*)\n" +
                "declare i32 @__mx_builtin_getInt()\n" +
                "declare i8* @__mx_builtin_getString()\n" +
                "declare i8* @__mx_builtin_toString(i32)\n" +
                "declare i8* @__mx_builtin_malloc(i32)\n" +
                "declare i32 @__mx_builtin_str_len(i8*)\n" +
                "declare i8* @__mx_builtin_str_cat(i8*, i8*)\n" +
                "declare i1 @__mx_builtin_str_eq(i8*, i8*)\n" +
                "declare i1 @__mx_builtin_str_ne(i8*, i8*)\n" +
                "declare i1 @__mx_builtin_str_lt(i8*, i8*)\n" +
                "declare i1 @__mx_builtin_str_le(i8*, i8*)\n" +
                "declare i1 @__mx_builtin_str_gt(i8*, i8*)\n" +
                "declare i1 @__mx_builtin_str_ge(i8*, i8*)\n" +
                "declare i8* @__mx_builtin_str_sub(i8*, i32, i32)\n" +
                "declare i32 @__mx_builtin_str_parse(i8*)\n" +
                "declare i32 @__mx_builtin_str_ord(i8*, i32)\n" +
                "declare i32 @__mx_builtin_arr_size(i32*)\n"
            val alias = alias
                .map { "${it.key} = type ${it.value}" }
                .let {
                    if (it.isEmpty()) ""
                    else it.reduce { acc, s -> "$acc\n$s" }
                }
            val str = str
                .map { it.value.ir }
                .let {
                    if (it.isEmpty()) ""
                    else it.reduce { acc, s -> "$acc\n$s" }
                }
            val glb = glb
                .map { it.value.ir }
                .let {
                    if (it.isEmpty()) ""
                    else it.reduce { acc, s -> "$acc\n$s\n" }
                }
            val func = func
                .map { it.value.ir }
                .let {
                    if (it.isEmpty()) ""
                    else it.reduce { acc, s -> "$acc\n$s" }
                }
            return "$builtin\n$alias\n$str\n$glb\n$func\n"
        }

}

class IrFunction(
    module: IrModule,
    val funcId: IrFuncIdent,
    private val retType: IrType,
    val para: List<IrValue>
) : IrUser {

    val paraTypes = para.map { it.type }
    val funcType = IrFunctionType(retType, paraTypes)

    val isMember = funcId.isMember

    private val block = mutableListOf<IrBasicBlock>()

    init {
        module.addFunc(this)
        para.forEach { it.addUser(this) }
    }

    fun addBlock(vararg basicBlock: IrBasicBlock) {
        block.addAll(basicBlock)
    }

    val ir: String
        get() {
            val paras = para
                .map { "${it.type} $it" }
                .let {
                    if (it.isEmpty()) ""
                    else it.reduce { acc, s -> "$acc, $s" }
                }
            assert(block.isNotEmpty())
            val blocks = block
                .map { it.ir }
                .reduce { acc, s -> "$acc\n$s" }
            return "define $retType $funcId($paras) {\n$blocks}\n"
        }
}


class IrBasicBlock(
    function: IrFunction,
    val id: IrBasicBlockIdent
) {
    private val pred = mutableListOf<IrBasicBlock>()
    private val succ = mutableListOf<IrBasicBlock>()
    private val inst = mutableListOf<IrValue>()
    val terminated: Boolean get() = inst.isNotEmpty() && (inst.last() is IrTermination)

    init {
        function.addBlock(this)
    }

    private fun addPred(vararg basicBlocks: IrBasicBlock) {
        pred.addAll(basicBlocks)
    }

    private fun addSucc(vararg basicBlocks: IrBasicBlock) {
        succ.addAll(basicBlocks)
    }

    fun addInst(vararg instructions: IrValue) {
        instructions
            .filterIsInstance<IrBrInst>()
            .forEach { br ->
                addSucc(br.thenBlock)
                br.thenBlock.addPred(this)
                br.elseBlock?.let {
                    addSucc(it)
                    it.addPred(this)
                }

            }
        inst.addAll(instructions)
    }

    val ir: String
        get() {
            assert(inst.isNotEmpty())
            val body = inst
                .map { "  ${it.ir}" }
                .reduce { acc, s -> "$acc\n$s" }
            return "${id.asBlock}\n$body\n"
        }
}



