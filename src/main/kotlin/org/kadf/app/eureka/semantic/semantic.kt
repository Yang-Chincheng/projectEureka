package org.kadf.app.eureka.semantic

import org.kadf.app.eureka.ast.*
import org.kadf.app.eureka.ast.nodes.*
import org.kadf.app.eureka.utils.CodeContext
import kotlin.Exception

class EurekaException(val ctx: CodeContext?, msg: String?): Exception(msg)

class SemanticChecker : ASTVisitor {

    private fun Exception.with(ctx: CodeContext?) = EurekaException(ctx, this.message)

    class TypeInfo(val type: IType, val isLeftValue: Boolean)

    private fun IType.match(it: IExpression) = match((visit(it) as TypeInfo).type)

    private val tEnv = TypeEnv()
    private val vEnv = EnvManager(tEnv)

    override fun visit(node: ProgramNode) {
        // type registry
        node.decls.filterIsInstance<ClassDeclNode>().forEach {
            tEnv.registerType(it.type, TypeBinding(vEnv.global, it).apply { isClass = true })
        }

        // global variable & function registry
        node.decls.filterIsInstance<FuncDeclNode>().forEach { decl ->
            if (!tEnv.containType(decl.type))
                throw Exception("global fun registry illegal type").with(node.ctx)
            vEnv.global.registerFun(tEnv, decl.id, decl.type)
        }
        if (!vEnv.global.containFun("main"))
            throw Exception("do not have main function").with(node.ctx)

        // class member variable & function registry
        node.decls.filterIsInstance<ClassDeclNode>().forEach { decl ->
            val venv = tEnv.lookupType(decl.type) ?:
                throw Exception("class not found when class member registry").with(node.ctx)

            decl.member.filterIsInstance<VarDeclNode>().forEach { mVar ->
                if (!tEnv.containType(mVar.type))
                    throw Exception("class member var registry illegal type").with(node.ctx)
                mVar.ids.forEach {
                    venv.registerVar(tEnv, it, mVar.type)
                }
            }
            decl.member.filterIsInstance<FuncDeclNode>().forEach { mFun ->
                if (!tEnv.containType(mFun.type))
                    throw Exception("class member fun registry illegal type").with(node.ctx)
                venv.registerFun(tEnv, mFun.id, mFun.type)
            }
        }

        // start semantic check
        node.decls.forEach { visit(it) }
    }

    override fun visit(node: VarDeclNode) {
        // initial expression type check
        if (!node.inits.filterNotNull().all { node.type.match(it) } )
            throw Exception("var decl node: init type not mismatch").with(node.ctx)
        // type registry check
        if (!tEnv.containType(node.type))
            throw Exception("var decl node: type").with(node.ctx)
        if (!vEnv.current.isClass) {
            // local variable registry
            node.ids.forEach {
//                if (tEnv.containID(it)) throw Exception("var check failed").with(node.ctx)
                vEnv.current.registerVar(tEnv, it, node.type)
            }
        }
    }

    override fun visit(node: FuncDeclNode) {
        // main function check
        if (node.id == "main") {
            if (node.type.retType !is IntegerType)
                throw Exception("main function return type").with(node.ctx)
            if (node.ids.isNotEmpty())
                throw Exception("main function args").with(node.ctx)
        }
        // create environment
        val local = vEnv.enter(node).apply { isFunction = true }
        // parameter registry
        node.ids.zip(node.type.paraTypes).forEach {
//            if (tEnv.containID(it.first)) throw Exception("")
            local.registerVar(tEnv, it.first, it.second)
        }
        // traverse
        node.body.forEach { visit(it) }
        // return check
        local.inferredReturnType?.let {
            if (!it.match(node.type.retType))
                throw Exception("return check failed").with(node.ctx)
        } ?: run {
            if (node.id != "main" && !UnitType.match(node.type.retType))
                throw Exception("${node.id} main infer return type").with(node.ctx)
        }
        // remove environment
        vEnv.leave()
    }

    override fun visit(node: ClassDeclNode) {
        // enter environment
        vEnv.current = tEnv.lookupType(node.type)!!
        // constructor check
        val constr = node.member.filterIsInstance<ConstrNode>()
        if (constr.size > 1)
            throw Exception("multiple constructor").with(node.ctx)
        if (constr.isNotEmpty() && constr.first().id != node.id)
            throw Exception("constructor id mismatch").with(node.ctx)
        // traverse
        node.member.forEach { visit(it) }
        // remove environment
        vEnv.leave()
    }

    override fun visit(node: ConstrNode) {
        vEnv.enter(node).apply {
            isFunction = true
//            inferredReturnType = NothingType
        }
        // traverse
        node.body.forEach { visit(it) }
        // return type check
        with(vEnv.current.inferredReturnType) {
            if (this != null && !UnitType.match(this))
                throw Exception("constructor return type").with(node.ctx)
        }
        // remove environment
        vEnv.leave()
    }

    override fun visit(node: BlockStmtNode) {
        vEnv.enter(node)
        node.stmts.forEach { visit(it) }
        vEnv.leave()
    }

    override fun visit(node: BranchStmtNode) {
        // condition type check
        if (!BooleanType.match(node.cond))
            throw Exception("branch condition not bool").with(node.ctx)
        // environment for then block
        vEnv.enter()
        visit(node.thenBody)
        vEnv.leave()
        // environment for else block
        node.elseBody?.let {
            vEnv.enter()
            visit(it)
            vEnv.leave()
        }
    }

    override fun visit(node: ForLoopStmtNode) {
        // condition type check
        node.cond?.let {
            if (!BooleanType.match(it))
                throw Exception("for condition not bool").with(node.ctx)
        }
        // create environment
        vEnv.enter(node).apply { isLoop = true }
        // traverse
        node.init?.let { visit(it) }
        node.iter?.let { visit(it) }
        visit(node.body)
        // remove environment
        vEnv.leave()
    }

    override fun visit(node: WhileLoopStmtNode) {
        // condition type check
        if(!BooleanType.match(node.cond))
            throw Exception("while condition not bool").with(node.ctx)
        // create environment
        vEnv.enter(node).apply { isLoop = true }
        // traverse
        visit(node.body)
        // remove environment
        vEnv.leave()
    }

    override fun visit(node: ControlStmtNode) {
        if (vEnv.current.outerLoop == null)
            throw Exception("break/continue outside loop").with(node.ctx)
    }

    override fun visit(node: ReturnStmtNode) {
        // get return value type
        val retType = node.value?.let { (visit(it) as TypeInfo).type }?: UnitType
        // priority: lambda > function
        val outerLambda = vEnv.current.outerLambda
        val outerFunc = vEnv.current.outerFunc
        when {
            outerLambda != null -> {
                if (outerLambda.inferredReturnType != null) {
                    if (!retType.match(outerLambda.inferredReturnType!!))
                        throw Exception("lambda return type mismatch while inferring").with(node.ctx)
                }
                else outerLambda.inferredReturnType = retType

            }
            outerFunc != null -> {
                if (outerFunc.inferredReturnType != null) {
                    if (!retType.match(outerFunc.inferredReturnType!!))
                        throw Exception("function return type conflict while inferring").with(node.ctx)
                }
                else outerFunc.inferredReturnType = retType
            }
            else -> throw Exception("return outside function")
        }
    }

    override fun visit(node: LiteralNode) = when(node.type) {
        is AnyType -> {
            vEnv.current.outerClass?.let {
                TypeInfo((it.node as ClassDeclNode).type, true)
            } ?: throw Exception("this outside class").with(node.ctx)
        }
        else -> TypeInfo(node.type, false)
    }

    override fun visit(node: VariableExprNode) = TypeInfo(
        // variable registry check
        vEnv.current.lookupVar(node.id)
            ?: throw Exception("${node.id} use unregisted var").with(node.ctx),
        true
    )

    override fun visit(node: InvocationExprNode): TypeInfo {
        vEnv.current.lookupFun(node.func)?.let { type ->
            if (node.args.size != type.paraTypes.size)
                throw Exception("function invocation arguments size mismatch").with(node.ctx)
            if (type.paraTypes.zip(node.args).any { !it.first.match(it.second) })
                throw Exception("function invocation arguments type mismatch").with(node.ctx)
            return TypeInfo(type.retType, false)
        } ?: throw Exception("use unreg func").with(node.ctx)
    }

    override fun visit(node: PropertyExprNode): TypeInfo {
        val objTypeInfo = visit(node.obj) as TypeInfo
//        if (!objTypeInfo.isLeftValue) throw Exception("property node not lval").with(node.ctx)
        tEnv.lookupType(objTypeInfo.type)?.let { venv ->
            return venv.lookupVar(node.id)?.let { TypeInfo(it, true) }
                ?: throw Exception("illegal property").with(node.ctx)
        } ?: throw Exception("property node illegal type").with(node.ctx)
    }

    override fun visit(node: MethodExprNode): TypeInfo {
        val objTypeInfo = visit(node.obj) as TypeInfo
        tEnv.lookupType(objTypeInfo.type)?.let { venv ->
            venv.lookupFun(node.func)?.let { type ->
                if (type.paraTypes.size != node.args.size)
                    throw Exception("method invocation size").with(node.ctx)
                if (type.paraTypes.zip(node.args).any { !it.first.match(it.second) } )
                    throw Exception("method invocation type").with(node.ctx)
                return TypeInfo(type.retType, false)
            } ?: throw Exception("method invocation dont have this func").with(node.ctx)
        } ?: throw Exception("method invocation dont have this type").with(node.ctx)
    }

    override fun visit(node: IndexExprNode) : TypeInfo {
        val arrTypeInfo = visit(node.arr) as TypeInfo
        if (arrTypeInfo.type !is ArrayType) throw Exception("index not array").with(node.ctx)
        if (!IntegerType.match(node.idx)) throw Exception("idx not int").with(node.ctx)
        return with(arrTypeInfo.type) { when {
            dim > 1 -> TypeInfo(ArrayType(type, dim - 1), true)
            else -> TypeInfo(type, true)
        } }
    }

    override fun visit(node: LambdaExprNode): TypeInfo {
        // type check
        if (!tEnv.containType(node.type))
            throw Exception("lambda type check").with(node.ctx)
        // new environment
        val local = vEnv.enter().apply { isLambda = true; capture = node.cap }
        // parameter registry
        node.ids.zip(node.type.paraTypes).forEach {
            local.registerVar(tEnv, it.first, it.second)
        }
        // body traverse
        node.body.forEach { visit(it) }
        // arguments check
        if (node.args.size != node.type.paraTypes.size) throw Exception()
        node.type.paraTypes.zip(node.args).forEach {
            if (!it.first.match(it.second)) throw Exception()
        }
        // return type check
        val retType = (local.inferredReturnType as? IReturnableType) ?: UnitType
        return TypeInfo(retType, false).also { vEnv.leave() }
    }

    override fun visit(node: AssignExprNode): TypeInfo {
        val lhsTypeInfo = visit(node.lhs) as TypeInfo
        val rhsTypeInfo = visit(node.rhs) as TypeInfo
        if (!lhsTypeInfo.isLeftValue)
            throw Exception("assign not lval").with(node.ctx)
        if (!lhsTypeInfo.type.match(rhsTypeInfo.type))
            throw Exception("assign lhs and rhs type mismatch").with(node.ctx)
        return TypeInfo(lhsTypeInfo.type, true)
    }

    override fun visit(node: CompoundExprNode) = visit(node.last).also {
        node.rest.forEach { visit(it) }
    }

    override fun visit(node: UnaryExprNode): TypeInfo {
        val info = visit(node.expr) as TypeInfo
        when(node.op) {
            Operator.PRE_INC, Operator.PRE_DEC -> {
                if (!info.isLeftValue)
                    throw Exception("unary not lval").with(node.ctx)
                if (!info.type.match(IntegerType))
                    throw Exception("unary inc/dec not int").with(node.ctx)
                return TypeInfo(IntegerType, true)
            }
            Operator.POST_INC, Operator.POST_DEC -> {
                if (!info.isLeftValue)
                    throw Exception("unary not lval").with(node.ctx)
                if (!info.type.match(IntegerType))
                    throw Exception("unary inc/dec not int").with(node.ctx)
                return TypeInfo(IntegerType, false)
            }
            Operator.POSIT, Operator.NEG, Operator.NOT -> {
                if (!info.type.match(IntegerType))
                    throw Exception("unary neg/not/posi not int").with(node.ctx)
                return TypeInfo(IntegerType, false)
            }
            Operator.LOGIC_NOT -> {
                if (!info.type.match(BooleanType))
                    throw Exception("unary logic_not not bool").with(node.ctx)
                return TypeInfo(BooleanType, false)
            }
            else -> throw Exception()
        }
    }

    override fun visit(node: BinaryExprNode): TypeInfo {
        val lhsType = (visit(node.lhs) as TypeInfo).type
        val rhsType = (visit(node.rhs) as TypeInfo).type
        if (!lhsType.match(rhsType))
            throw Exception("binary lhs and rhs type dismatch").with(node.ctx)
        when(node.op) {
            Operator.PLUS -> {
                if (!lhsType.match(IntegerType) && !lhsType.match(StringType))
                    throw Exception("binary plus").with(node.ctx)
                return TypeInfo(lhsType, false)
            }
            Operator.LT, Operator.GT, Operator.LE, Operator.GE -> {
                if (!lhsType.match(IntegerType) && !lhsType.match(StringType))
                    throw Exception("binary lt gt le ge").with(node.ctx)
                return TypeInfo(BooleanType, false)
            }
            Operator.MINUS, Operator.MUL, Operator.DIV, Operator.MOD,
            Operator.AND, Operator.OR, Operator.XOR, Operator.LSH, Operator.RSH -> {
                if (!lhsType.match(IntegerType))
                    throw Exception("binary minus and so on").with(node.ctx)
                return TypeInfo(IntegerType, false)
            }
            Operator.LOGIC_OR, Operator.LOGIC_AND -> {
                if (!lhsType.match(BooleanType))
                    throw Exception("binary logic or/and").with(node.ctx)
                return TypeInfo(BooleanType, false)
            }
            Operator.EQ, Operator.NE -> {
                return TypeInfo(BooleanType, false)
            }
            else -> throw Exception("binary illegal operator").with(node.ctx)
        }
    }

    override fun visit(node: NewArrayNode): TypeInfo {
        // type check
        if (!tEnv.containType(node.type))
            throw Exception("new array with unknown type").with(node.ctx)
        // scale check
        val firstNull = node.scales.indexOfFirst { it == null }
        val lastNotNull = node.scales.indexOfLast { it != null }
        if (firstNull != -1 && lastNotNull != -1 && firstNull < lastNotNull)
            throw Exception("illegal scale").with(node.ctx)
        if (node.scales.filterNotNull().any { !IntegerType.match(it) })
            throw Exception("new array not int as scale").with(node.ctx)
        return TypeInfo(node.type, false)
    }

    override fun visit(node: NewObjectNode): TypeInfo {
        // type check
        if (!tEnv.containType(node.type))
            throw Exception("new object illegal type").with(node.ctx)
        return TypeInfo(node.type, false)
    }
}