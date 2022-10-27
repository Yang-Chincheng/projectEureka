package org.kadf.app.eureka.semantic

import org.kadf.app.eureka.ast.*
import org.kadf.app.eureka.ast.nodes.*
import kotlin.Exception


class SemanticChecker : ASTVisitor {

    class TypeInfo(val type: IType, val isLeftValue: Boolean)

    private fun IType.match(it: IExpression) = match((visit(it) as TypeInfo).type)

    private val tEnv = TypeEnv()
    private val vEnv = EnvManager(tEnv)

    override fun visit(node: ProgramNode) {
        // type registry
        node.decls.filterIsInstance<ClassDeclNode>().forEach {
            tEnv.registerType(it.type, TypeBinding(vEnv.global).apply { isClass = true })
        }
        // global variable & function registry
        node.decls.filterIsInstance<VarDeclNode>().forEach { decl ->
            if (!tEnv.containType(decl.type)) throw Exception()
            decl.ids.forEach { vEnv.global.registerVar(it, decl.type) }
        }
        node.decls.filterIsInstance<FuncDeclNode>().forEach { decl ->
            if (!tEnv.containType(decl.type)) throw Exception()
            vEnv.global.registerFun(decl.id, decl.type)
        }
        // class member variable & function registry
        node.decls.filterIsInstance<ClassDeclNode>().forEach { decl ->
            val venv = tEnv.lookupType(decl.type) ?: throw Exception()
            decl.member.filterIsInstance<VarDeclNode>().forEach { mVar ->
                if (!tEnv.containType(mVar.type)) throw Exception()
                mVar.ids.forEach { venv.registerVar(it, mVar.type) }
            }
            decl.member.filterIsInstance<FuncDeclNode>().forEach { mFun ->
                if (!tEnv.containType(mFun.type)) throw Exception()
                venv.registerFun(mFun.id, mFun.type)
            }
        }
        // start semantic check
        node.decls.forEach { visit(it) }
    }

    override fun visit(node: VarDeclNode) {
        if (!vEnv.current.isGlobal && !vEnv.current.isClass) {
            // type registry check
            if (!tEnv.containType(node.type)) throw Exception()
            // local variable registry
            node.ids.forEach { vEnv.current.registerVar(it, node.type) }
        }
        // initial expression type check
        if (!node.inits.filterNotNull().all { node.type.match(it) } ) throw Exception()
    }

    override fun visit(node: FuncDeclNode) {
        // main function check
        if (node.id == "main" && node.type.retType !is IntegerType) throw Exception()
        // create environment
        val local = vEnv.enter(node).apply { isFunction = true }
        // parameter registry
        node.ids.zip(node.type.paraTypes).forEach {
            local.registerVar(it.first, it.second)
        }
        // traverse
        node.body.forEach { visit(it) }
        // return check
        local.inferredReturnType?.let {
            if (!it.match(node.type.retType)) throw Exception()
        } ?: run {
            if (node.id != "main") throw Exception()
        }
        // remove environment
        vEnv.leave()
    }

    override fun visit(node: ClassDeclNode) {
        // enter environment
        vEnv.current = tEnv.lookupType(node.type)!!
        // constructor check
        val constr = node.member.filterIsInstance<ConstrNode>()
        if (constr.size > 1) throw Exception()
        if (constr.isNotEmpty() && constr.first().id != node.id) throw Exception()
        // traverse
        node.member.forEach { visit(it) }
        // remove environment
        vEnv.leave()
    }

    override fun visit(node: ConstrNode) {
        vEnv.enter(node).apply {
            isFunction = true
            inferredReturnType = NothingType
        }
        // traverse
        node.body.forEach { visit(it) }
        // remove environment
        vEnv.leave()
    }

    override fun visit(node: BranchStmtNode) {
        // condition type check
        if (!BooleanType.match(node.cond)) throw Exception()
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
            if (!BooleanType.match(it)) throw Exception()
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
        if(!BooleanType.match(node.cond)) throw Exception()
        // create environment
        vEnv.enter(node).apply { isLoop = true }
        // traverse
        visit(node.body)
        // remove environment
        vEnv.leave()
    }

    override fun visit(node: ControlStmtNode) {
        if (vEnv.current.outerLoop == null) throw Exception()
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
                    if (!retType.match(outerLambda.inferredReturnType!!)) throw Exception()
                }
                else outerLambda.inferredReturnType = retType
            }
            outerFunc != null -> {
                if (outerFunc.inferredReturnType != null) {
                    if (!retType.match(outerFunc.inferredReturnType!!)) throw Exception()
                }
                else outerFunc.inferredReturnType = retType
            }
            else -> throw Exception()
        }
    }

    override fun visit(node: LiteralNode) = when(node.type) {
        is AnyType -> {
            vEnv.current.outerClass?.let {
                TypeInfo((it.node as ClassDeclNode).type, true)
            } ?: throw Exception()
        }
        else -> TypeInfo(node.type, false)
    }

    override fun visit(node: VariableExprNode) = TypeInfo(
        // variable registry check
        vEnv.current.lookupVar(node.id) ?: throw Exception(),
        true
    )

    override fun visit(node: InvocationExprNode): TypeInfo {
        vEnv.current.lookupFun(node.func)?.let { type ->
            if (node.args.size != type.paraTypes.size) throw Exception()
            if (type.paraTypes.zip(node.args).any { !it.first.match(it.second) }) throw Exception()
            return TypeInfo(type.retType, false)
        } ?: throw Exception()
    }

    override fun visit(node: PropertyExprNode): TypeInfo {
        val objTypeInfo = visit(node.obj) as TypeInfo
        if (!objTypeInfo.isLeftValue) throw Exception()
        tEnv.lookupType(objTypeInfo.type)?.let { venv ->
            return venv.lookupVar(node.id)?.let { TypeInfo(it, true) } ?: throw Exception()
        } ?: throw Exception()
    }

    override fun visit(node: MethodExprNode): TypeInfo {
        val objTypeInfo = visit(node.obj) as TypeInfo
        if (!objTypeInfo.isLeftValue) throw Exception()
        tEnv.lookupType(objTypeInfo.type)?.let { venv ->
            venv.lookupFun(node.func)?.let { type ->
                if (type.paraTypes.size != node.args.size) throw Exception()
                if (type.paraTypes.zip(node.args).any { !it.first.match(it.second) } ) throw Exception()
                return TypeInfo(type.retType, false)
            } ?: throw Exception()
        } ?: throw Exception()
    }

    override fun visit(node: IndexExprNode) : TypeInfo {
        val arrTypeInfo = visit(node.arr) as TypeInfo
        if (arrTypeInfo.type !is ArrayType) throw Exception()
        if (!arrTypeInfo.isLeftValue) throw Exception()
        if (!IntegerType.match(node.idx)) throw Exception()
        return with(arrTypeInfo.type) { when {
            dim > 1 -> TypeInfo(ArrayType(type, dim - 1), true)
            else -> TypeInfo(type, true)
        } }
    }

    override fun visit(node: LambdaExprNode): TypeInfo {
        // type check
        if (!tEnv.containType(node.type)) throw Exception()
        // new environment
        val local = vEnv.enter().apply { isLambda = true; capture = node.cap }
        // parameter registry
        node.ids.zip(node.type.paraTypes).forEach { local.registerVar(it.first, it.second) }
        // body traverse
        node.body.forEach { visit(it) }
        // return type check
        val retType = (local.inferredReturnType as? IReturnableType) ?: UnitType
        return TypeInfo(
            FunctionType(retType, node.type.paraTypes), false
        ).also { vEnv.leave() }
    }

    override fun visit(node: AssignExprNode): TypeInfo {
        val lhsTypeInfo = visit(node.lhs) as TypeInfo
        val rhsTypeInfo = visit(node.rhs) as TypeInfo
        if (!lhsTypeInfo.isLeftValue) throw Exception()
        if (!lhsTypeInfo.type.match(rhsTypeInfo.type)) throw Exception()
        return TypeInfo(lhsTypeInfo.type, true)
    }

    override fun visit(node: CompoundExprNode) = visit(node.last).also {
        node.rest.forEach { visit(it) }
    }

    override fun visit(node: UnaryExprNode): TypeInfo {
        val info = visit(node.expr) as TypeInfo
        when(node.op) {
            Operator.PRE_INC, Operator.PRE_DEC,
            Operator.POST_INC, Operator.POST_DEC -> {
                if (!info.isLeftValue) throw Exception()
                if (!info.type.match(IntegerType)) throw Exception()
                return TypeInfo(IntegerType, true)
            }
            Operator.POSIT, Operator.NEG, Operator.NOT -> {
                if (!info.type.match(IntegerType)) throw Exception()
                return TypeInfo(IntegerType, false)
            }
            Operator.LOGIC_NOT -> {
                if (!info.type.match(BooleanType)) throw Exception()
                return TypeInfo(BooleanType, false)
            }
            else -> throw Exception()
        }
    }

    override fun visit(node: BinaryExprNode): TypeInfo {
        val lhsType = (visit(node.lhs) as TypeInfo).type
        val rhsType = (visit(node.rhs) as TypeInfo).type
        if (!lhsType.match(rhsType)) throw Exception()
        when(node.op) {
            Operator.PLUS -> {
                if (!lhsType.match(IntegerType) && !lhsType.match(StringType)) throw Exception()
                return TypeInfo(lhsType, false)
            }
            Operator.LT, Operator.GT, Operator.LE, Operator.GE -> {
                if (!lhsType.match(IntegerType) && !lhsType.match(StringType)) throw Exception()
                return TypeInfo(BooleanType, false)
            }
            Operator.MINUS, Operator.MUL, Operator.DIV, Operator.MOD,
            Operator.AND, Operator.OR, Operator.XOR, Operator.LSH, Operator.RSH -> {
                if (!lhsType.match(IntegerType)) throw Exception()
                return TypeInfo(IntegerType, false)
            }
            Operator.LOGIC_OR, Operator.LOGIC_AND -> {
                if (!lhsType.match(BooleanType)) throw Exception()
                return TypeInfo(BooleanType, false)
            }
            Operator.EQ, Operator.NE -> {
                return TypeInfo(BooleanType, false)
            }
            else -> throw Exception()
        }
    }

    override fun visit(node: NewArrayNode): TypeInfo {
        // type check
        if (!tEnv.containType(node.type)) throw Exception()
        // scale check
        if (node.scales.any { !IntegerType.match(it) }) throw Exception()
        return TypeInfo(node.type, false)
    }

    override fun visit(node: NewObjectNode): TypeInfo {
        // type check
        if (tEnv.containType(node.type)) throw Exception()
        return TypeInfo(node.type, false)
    }
}