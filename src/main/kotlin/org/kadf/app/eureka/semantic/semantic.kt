package org.kadf.app.eureka.semantic

import org.kadf.app.eureka.ast.*
import org.kadf.app.eureka.ast.nodes.*
import org.kadf.app.eureka.utils.CodeContext
import org.kadf.app.eureka.utils.DefaultErrorHandler
import org.kadf.app.eureka.utils.SemanticError

class SemanticChecker(root: AstNode) : ASTVisitor {

    private fun reportError(ctx: CodeContext?, msg: String): Nothing {
        throw SemanticError(ctx, msg)
    }

    private val tEnv = TypeEnv()
    private val vEnv = EnvManager(root, tEnv)

    fun check(node: AstNode) { (node as AstProgramNode).accept(this) }

    override fun visit(node: AstProgramNode) {
        node.env = vEnv.current
        // type registry
        node.decls
            .filterIsInstance<AstClassDeclNode>()
            .forEach {
                tEnv.registerType(it.type) { msg -> reportError(node.ctx, msg) }
            }

        // global function registry
        node.decls
            .filterIsInstance<AstFuncDeclNode>()
            .forEach { decl ->
                if (!tEnv.containType(decl.funcType))
                    reportError(node.ctx, "function declaration with undeclared type")
                vEnv.global.registerFun(decl.funcId, decl.funcType) { msg -> reportError(node.ctx, msg) }
            }
        if (!vEnv.global.containFun("main"))
            reportError(node.ctx, "main function is required")

        // class member variable & function registry
        node.decls
            .filterIsInstance<AstClassDeclNode>()
            .forEach { decl ->
                decl.member
                    .filterIsInstance<AstVarDeclNode>()
                    .forEach { mVar ->
                        if (!tEnv.containType(mVar.type))
                            reportError(node.ctx, "property declaration(s) with undeclared type")
                        mVar.ids.forEach {
                            tEnv.registerProp(decl.type, it, mVar.type) { msg -> reportError(node.ctx, msg) }
                        }
                    }
                decl.member
                    .filterIsInstance<AstFuncDeclNode>()
                    .forEach { mFun ->
                        if (!tEnv.containType(mFun.funcType))
                            reportError(node.ctx, "method declaration with undeclared type")
                        tEnv.registerMeth(decl.type, mFun.funcId, mFun.funcType) { msg -> reportError(node.ctx, msg) }
                    }
            }

        // start semantic check
        node.decls.forEach { it.accept(this) }
    }

    override fun visit(node: AstVarDeclNode) {
        node.env = vEnv.current
        // type registry check
        if (!tEnv.containType(node.type))
            reportError(node.ctx, "variable declaration with undeclared type")
        // initialization expression type check
        val initType = node.inits
            .filterNotNull()
            .map { it.accept(this); it.astType }
        if (initType.any { !node.type.match(it) })
            reportError(node.ctx, "type of variable initialization failed to match the declared one")
        if (!vEnv.current.isClass) {
            // local variable registry
            node.ids.forEach {
                node.env.registerVar(it, node.type) { msg -> reportError(node.ctx, msg) }
            }
        }
        node.isGlobal = node.env.isGlobal
    }

    override fun visit(node: AstFuncDeclNode) {
        // main function check
        if (node.isMainFunc) {
            if (node.retType !is AstIntegerType)
                reportError(node.ctx, "main function is required to have <int> as its return type")
            if (node.paraIds.isNotEmpty())
                reportError(node.ctx, "main function is required to have no arguments")
        }
        // create environment
        node.env = vEnv.enter(node).apply { isFunction = true }
        // parameter registry
        node.paraInfo
            .forEach {
                node.env.registerVar(it.first, it.second) { msg -> reportError(node.ctx, msg) }
            }
        // traverse
        node.body.forEach { it.accept(this) }
        // return check
        node.env.returnType?.let {
            node.hasReturn = true
            if (!it.match(node.retType))
                reportError(node.ctx, "function return type mismatching")
        } ?: run {
            node.hasReturn = false
            if (!node.isMainFunc && !AstUnitType.match(node.retType))
                reportError(node.ctx, "function <${node.funcId}> requires at least one return statements")
        }
        node.memberOf = node.env.outerClass?.envType
        // remove environment
        vEnv.leave()
    }

    override fun visit(node: AstClassDeclNode) {
        // enter environment
        node.env = vEnv.enter(node).apply { isClass = true }
        node.member
            .filterIsInstance<AstVarDeclNode>()
            .forEach { decl ->
                decl.ids.forEach {
                    node.env.registerVar(it, decl.type)
                }
            }
        node.member
            .filterIsInstance<AstFuncDeclNode>()
            .forEach {
                node.env.registerFun(it.funcId, it.funcType)
            }
        // constructor check
        val constrList = node.member.filterIsInstance<AstConstrNode>()
        if (constrList.isNotEmpty()) {
            val constr = constrList.first()
            if (constrList.size > 1)
                reportError(node.ctx, "duplicate declaration of class constructor")
            if (constr.id != node.id)
                reportError(node.ctx, "constructor is required to have the same identifier with the class")
            tEnv.lookupType(node.type)!!.hasConstr = true
        }
        // traverse
        node.member.forEach { it.accept(this) }
        // remove environment
        vEnv.leave()
    }

    override fun visit(node: AstConstrNode) {
        node.env = vEnv.enter(node).apply { isFunction = true }
        // traverse
        node.body.forEach { it.accept(this) }
        // return type check
        if (node.env.returnType?.match(AstUnitType) == false)
            reportError(node.ctx, "constructor return type mismatching")
        node.hasReturned = node.env.returnType != null
        node.memberOf = node.env.outerClass?.envType
        // remove environment
        vEnv.leave()
    }

    override fun visit(node: AstBlockStmtNode) {
        node.env = vEnv.enter(node)
        node.stmts.forEach { it.accept(this) }
        vEnv.leave()
    }

    override fun visit(node: AstBranchStmtNode) {
        node.env = vEnv.current
        // condition type check
        node.cond.accept(this)
        if (!AstBooleanType.match(node.cond.astType))
            reportError(node.ctx, "branch condition expression is required to have <bool> type")
        // environment for then block
        vEnv.enter()
        node.thenBody.accept(this)
        vEnv.leave()
        // environment for else block
        node.elseBody?.let {
            vEnv.enter()
            it.accept(this)
            vEnv.leave()
        }
    }

    override fun visit(node: AstForLoopStmtNode) {
        node.env = vEnv.current
        // create environment
        vEnv.enter(node).apply { isLoop = true }
        // traverse
        node.init?.accept(this)
        node.iter?.accept(this)
        node.body.accept(this)
        // condition type check
        node.cond?.let {
            it.accept(this)
            if (!AstBooleanType.match(it.astType))
                reportError(node.ctx, "for loop condition expression is required to have <bool> type")
        }
        // remove environment
        vEnv.leave()
    }

    override fun visit(node: AstWhileLoopStmtNode) {
        node.env = vEnv.current
        // condition type check
        node.cond.accept(this)
        if (!AstBooleanType.match(node.cond.astType))
            reportError(node.ctx, "while loop condition expression is required to have <bool> type")
        // create environment
        vEnv.enter(node).apply { isLoop = true }
        // traverse
        node.body.accept(this)
        // remove environment
        vEnv.leave()
    }

    override fun visit(node: AstControlStmtNode) {
        node.env = vEnv.current
        if (vEnv.current.outerLoop == null)
            reportError(node.ctx, "<break>/<continue> statements are only allowed inside loops")
    }

    override fun visit(node: AstReturnStmtNode) {
        node.env = vEnv.current
        // get return value type
        val retType = node.value?.let { it.accept(this); it.astType } ?: AstUnitType
        // priority: lambda > function
        with(vEnv.current.outerLambda) {
            if (this != null) {
                returnType = AstType.intersect(returnType, retType) { msg -> reportError(node.ctx, msg) }
                return
            }
        }
        with(vEnv.current.outerFunc) {
            if (this != null) {
                returnType = AstType.intersect(returnType, retType) { msg -> reportError(node.ctx, msg) }
                return
            }
        }
    }

    override fun visit(node: AstExprStmtNode) {
        node.env = vEnv.current
        node.expr.accept(this)
        node.astType = node.expr.astType
    }

    override fun visit(node: AstEmptyStmtNode) {
        node.env = vEnv.current
    }

    override fun visit(node: AstVarDeclStmtNode) {
        node.env = vEnv.current
        node.decl.accept(this)
    }

    override fun visit(node: AstLiteralNode): Boolean {
        node.env = vEnv.current
        if (node.type is AstAnyType) {
            vEnv.current.outerClass?.let {
                node.astType = it.envType!!
                return true
            } ?: run {
                reportError(node.ctx, "<this> expression is only allowed inside classed")
            }
        } else {
            node.astType = node.type
            return false
        }
    }

    override fun visit(node: AstVariableExprNode): Boolean {
        // variable registry check
        node.env = vEnv.current
        node.astType = node.env.getVarType(node.id)
            ?: reportError(node.ctx, "usage of undeclared variable ${node.id}")
        node.memberOf = node.env.getVarEnv(node.id)?.envType
        return true
    }

    override fun visit(node: AstInvocationExprNode): Boolean {
        node.env = vEnv.current
        val funcType = node.env.getFunType(node.funcId)
            ?: reportError(node.ctx, "invocation of undeclared function ${node.funcId}")
        val argsType = node.args.map { it.accept(this); it.astType }
        if (!funcType.invocable(argsType))
            reportError(node.ctx, "function invocation argument mismatching")
        node.astType = funcType.retType
        node.memberOf = node.env.getFunEnv(node.funcId)?.envType

        return false
    }

    override fun visit(node: AstPropertyExprNode): Boolean {
        node.env = vEnv.current
        node.obj.accept(this)
        node.astType = tEnv.lookupProp(node.obj.astType, node.id)
            ?: reportError(node.ctx, "missing property named <${node.id}>")
        return true
    }

    override fun visit(node: AstMethodExprNode): Boolean {
        node.env = vEnv.current
        node.obj.accept(this)
        val funcType = tEnv.lookupMeth(node.obj.astType, node.funcId)
            ?: reportError(node.ctx, "missing method named <${node.funcId}>")
        val argsType = node.args.map { it.accept(this); it.astType }
        if (!funcType.invocable(argsType))
            reportError(node.ctx, "method invocation argument mismatching")
        node.astType = funcType.retType
        return false
    }

    override fun visit(node: AstIndexExprNode): Boolean {
        node.env = vEnv.current
        node.arr.accept(this)
        if (node.arr.astType !is AstArrayType)
            reportError(node.ctx, "only arrays are allowed for indexing")
        node.idx.accept(this)
        if (node.idx.astType !is AstIntegerType)
            reportError(node.ctx, "indexing expression is required to have <int> type")
        val arrType = node.arr.astType as AstArrayType
        node.astType = when {
            arrType.dim > 1 -> AstArrayType(arrType.type, arrType.dim - 1)
            else -> arrType.type
        }
        return true
    }

    override fun visit(node: AstLambdaExprNode): Boolean {
        // new environment
        node.env = vEnv.enter()
            .apply {
                isLambda = true
                capture = node.cap
            }
        // type check
        if (!tEnv.containType(node.type))
            reportError(node.ctx, "lambda expression parameter(s) with undeclared type")
        // parameter registry
        node.paraInfo
            .forEach {
                node.env.registerVar(it.first, it.second) { msg -> reportError(node.ctx, msg) }
            }
        // body traverse
        node.body.forEach { it.accept(this) }
        // argument type check
        val argsType = node.args.map { it.accept(this); it.astType }
        if (!node.type.invocable(argsType))
            reportError(node.ctx, "lambda expression invocation argument mismatching")
        // return type check
        node.astType = node.env.returnType ?: AstUnitType
        vEnv.leave()
        return false
    }

    override fun visit(node: AstAssignExprNode): Boolean {
        node.env = vEnv.current
        if (!(node.lhs.accept(this) as Boolean))
            reportError(node.ctx, "assigning to a non-left-handed value")
        node.rhs.accept(this)
        if (!node.lhs.astType.match(node.rhs.astType))
            reportError(node.ctx, "assignment type mismatching")
        node.astType = node.lhs.astType
        return true
    }

    override fun visit(node: AstCompoundExprNode): Boolean {
        node.env = vEnv.current
        node.rest.forEach { it.accept(this) }
        val lastIsLeftValue = node.last.accept(this) as Boolean
        node.astType = node.last.astType
        return lastIsLeftValue
    }

    override fun visit(node: AstUnaryExprNode): Boolean {
        node.env = vEnv.current
        val exprIsLeftValue = node.expr.accept(this) as Boolean
        when (node.op) {
            AstOperator.PRE_INC, AstOperator.PRE_DEC -> {
                if (!exprIsLeftValue)
                    reportError(node.ctx, "this operation applies only to left value")
                if (!AstIntegerType.match(node.expr.astType))
                    reportError(node.ctx, "this operation applies only to expressions of <int> type")
                node.astType = AstIntegerType
                return true
            }

            AstOperator.POST_INC, AstOperator.POST_DEC -> {
                if (!exprIsLeftValue)
                    reportError(node.ctx, "this operator applies only to left values")
                if (!AstIntegerType.match(node.expr.astType))
                    reportError(node.ctx, "this operator applies only to expressions of <int> type")
                node.astType = AstIntegerType
                return false
            }

            AstOperator.POSIT, AstOperator.NEG, AstOperator.NOT -> {
                if (!AstIntegerType.match(node.expr.astType))
                    reportError(node.ctx, "this operator applies only to expressions of <int> type")
                node.astType = AstIntegerType
                return false
            }

            AstOperator.LOGIC_NOT -> {
                if (!AstBooleanType.match(node.expr.astType))
                    reportError(node.ctx, "this operator applies only to expression of <bool> type")
                node.astType = AstBooleanType
                return false
            }

            else -> DefaultErrorHandler.report("")
        }
    }

    override fun visit(node: AstBinaryExprNode): Boolean {
        node.env = vEnv.current
        node.lhs.accept(this)
        node.rhs.accept(this)
        if (!node.lhs.astType.match(node.rhs.astType))
            reportError(node.ctx, "left-hand and right-hand sides type mismatching")
        when (node.op) {
            AstOperator.PLUS -> {
                if (!AstIntegerType.match(node.lhs.astType) && !AstStringType.match(node.lhs.astType))
                    reportError(node.ctx, "this operator applies only to expressions of <int>/<string> type")
                node.astType = node.lhs.astType
                return false
            }

            AstOperator.LT, AstOperator.GT, AstOperator.LE, AstOperator.GE -> {
                if (!AstIntegerType.match(node.lhs.astType) && !AstStringType.match(node.lhs.astType))
                    reportError(node.ctx, "this operator applies only to expressions of <int>/<string> type")
                node.astType = AstBooleanType
                return false
            }

            AstOperator.MINUS, AstOperator.MUL, AstOperator.DIV, AstOperator.MOD,
            AstOperator.AND, AstOperator.OR, AstOperator.XOR, AstOperator.LSH, AstOperator.RSH -> {
                if (!AstIntegerType.match(node.lhs.astType))
                    reportError(node.ctx, "this operator applies only to expressions of <int> type")
                node.astType = AstIntegerType
                return false
            }

            AstOperator.LOGIC_OR, AstOperator.LOGIC_AND -> {
                if (!AstBooleanType.match(node.lhs.astType))
                    reportError(node.ctx, "this operator applies only to expressions of <bool> type")
                node.astType = AstBooleanType
                return false
            }

            AstOperator.EQ, AstOperator.NE -> {
                node.astType = AstBooleanType
                return false
            }

            else -> DefaultErrorHandler.report("")
        }
    }

    override fun visit(node: AstNewArrayNode): Boolean {
        node.env = vEnv.current
        // type check
        if (!tEnv.containType(node.type))
            reportError(node.ctx, "new expression with illegal array type")
        // scale check
        val firstNull = node.scales.indexOfFirst { it == null }
        val lastNotNull = node.scales.indexOfLast { it != null }
        if (firstNull != -1 && lastNotNull != -1 && firstNull < lastNotNull)
            reportError(node.ctx, "new expression with illegal array dimension scales")
        val scaleType = node.scales
            .filterNotNull()
            .map { it.accept(this); it.astType }
        if (scaleType.any { !AstIntegerType.match(it) }) {
            reportError(node.ctx, "new expression with illegal array dimension scales")
        }
        node.astType = node.type
        return false
    }

    override fun visit(node: AstNewObjectNode): Boolean {
        node.env = vEnv.current
        // type check
        if (!tEnv.containType(node.type))
            reportError(node.ctx, "new expression with illegal object type")
        node.astType = node.type
        return false
    }
}