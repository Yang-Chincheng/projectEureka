package org.kadf.app.eureka.ast.nodes

import org.kadf.app.eureka.utils.CodeContext
import org.kadf.app.eureka.ast.ASTVisitor

class AstEmptyStmtNode(
    ctx: CodeContext
): AstNode(ctx) {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class AstBlockStmtNode(
    ctx: CodeContext,
    val stmts: List<AstNode>
): AstNode(ctx) {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class AstVarDeclStmtNode(
    ctx: CodeContext,
    val decl: AstVarDeclNode
): AstNode(ctx) {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class AstExprStmtNode(
    ctx: CodeContext,
    val expr: AstNode
): AstNode(ctx) {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class AstBranchStmtNode(
    ctx: CodeContext,
    val cond: AstNode,
    val thenBody: AstNode,
    val elseBody: AstNode?
): AstNode(ctx) {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

//sealed interface AstForLoopInit
//val AstForLoopInit.astType get() = (this as AstNode).astType
//val AstForLoopInit.env get() = (this as AstNode).env

class AstForLoopStmtNode(
    ctx: CodeContext,
    val init: AstNode?,
    val cond: AstNode?,
    val iter: AstNode?,
    val body: AstNode
): AstNode(ctx) {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class AstWhileLoopStmtNode(
    ctx: CodeContext,
    val cond: AstNode,
    val body: AstNode
): AstNode(ctx) {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class AstControlStmtNode(
    ctx: CodeContext,
    val ctrl: String
): AstNode(ctx) {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class AstReturnStmtNode(
    ctx: CodeContext,
    val value: AstNode? = null
): AstNode(ctx) {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

