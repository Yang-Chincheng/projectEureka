package org.kadf.app.eureka.ast

import org.kadf.app.eureka.ast.nodes.*

interface ASTVisitor {
    fun visit(node: ProgramNode): Any = node.decls.forEach { visit(it) }
    fun visit(node: IDeclaration): Any = when(node) {
        is VarDeclNode -> node.accept(this)
        is FuncDeclNode -> node.accept(this)
        is ClassDeclNode -> node.accept(this)
    }
    fun visit(node: VarDeclNode) : Any = node.inits.filterNotNull().forEach { visit(it) }
    fun visit(node: ClassDeclNode) : Any = node.member.forEach { visit(it) }
    fun visit(node: FuncDeclNode): Any = node.body.forEach { visit(it) }
    fun visit(node: IClassMember): Any = when(node) {
        is VarDeclNode -> node.accept(this)
        is FuncDeclNode -> node.accept(this)
        is ConstrNode -> node.accept(this)
    }
    fun visit(node: ConstrNode): Any = node.body.forEach { visit(it) }
    fun visit(node: IStatement): Any = when(node) {
        is VarDeclStmtNode -> node.accept(this)
        is BranchStmtNode -> node.accept(this)
        is ForLoopStmtNode -> node.accept(this)
        is WhileLoopStmtNode -> node.accept(this)
        is ControlStmtNode -> node.accept(this)
        is ReturnStmtNode -> node.accept(this)
        is ExprStmtNode -> node.accept(this)
        is BlockStmtNode -> node.accept(this)
        else -> {}
    }
    fun visit(node: BlockStmtNode): Any = node.stmts.forEach { visit(it) }
    fun visit(node: ExprStmtNode): Any = visit(node.expr)
    fun visit(node: VarDeclStmtNode): Any = node.decl.accept(this)
    fun visit(node: BranchStmtNode): Any = Unit.also {
        visit(node.cond)
        visit(node.thenBody)
        node.elseBody?.let { visit(it) }
    }
    fun visit(node: ForLoopStmtNode): Any = Unit.also {
        node.init?.let { visit(it) }
        node.cond?.let { visit(it) }
        node.iter?.let { visit(it) }
        visit(node.body)
    }
    fun visit(node: IForLoopInit): Any = when(node) {
        is VarDeclNode -> node.accept(this)
        is IExpression -> visit(node)
    }
    fun visit(node: WhileLoopStmtNode): Any = visit(node.body)
    fun visit(node: ControlStmtNode): Any = Unit
    fun visit(node: ReturnStmtNode): Any = node.value?.let { visit(it) } ?: Unit
    fun visit(node: IExpression) = when(node) {
        is LiteralNode -> node.accept(this)
        is VariableExprNode -> node.accept(this)
        is InvocationExprNode -> node.accept(this)
        is PropertyExprNode -> node.accept(this)
        is MethodExprNode -> node.accept(this)
        is IndexExprNode -> node.accept(this)
        is LambdaExprNode -> node.accept(this)
        is NewObjectNode -> node.accept(this)
        is NewArrayNode -> node.accept(this)
        is AssignExprNode -> node.accept(this)
        is CompoundExprNode -> node.accept(this)
        is UnaryExprNode -> node.accept(this)
        is BinaryExprNode -> node.accept(this)
    }
    fun visit(node: LiteralNode): Any = Unit
    fun visit(node: VariableExprNode): Any = Unit
    fun visit(node: InvocationExprNode): Any = Unit
    fun visit(node: PropertyExprNode): Any = visit(node.obj)
    fun visit(node: MethodExprNode): Any = visit(node.obj)
    fun visit(node: IndexExprNode): Any = Unit
    fun visit(node: LambdaExprNode): Any = node.body.forEach { visit(it) }
    fun visit(node: AssignExprNode): Any = Unit
    fun visit(node: CompoundExprNode): Any = visit(node.last).also { node.rest.forEach { visit(it) } }
    fun visit(node: UnaryExprNode): Any = visit(node.expr)
    fun visit(node: BinaryExprNode): Any = visit(node.lhs).also{ visit(node.rhs) }
    fun visit(node: NewArrayNode): Any = Unit
    fun visit(node: NewObjectNode): Any = Unit
}