package org.kadf.app.eureka.ast.nodes

import org.kadf.app.eureka.CodeContext
import org.kadf.app.eureka.ast.ASTVisitor

sealed interface IStatement

class EmptyStmtNode(
    ctx: CodeContext
): ASTNode(ctx), IStatement {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class BlockStmtNode(
    ctx: CodeContext,
    val stmts: List<IStatement>
): ASTNode(ctx), IStatement {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class VarDeclStmtNode(
    ctx: CodeContext,
    val decl: VarDeclNode
): ASTNode(ctx), IStatement {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class ExprStmtNode(
    ctx: CodeContext,
    val expr: IExpression
): ASTNode(ctx), IStatement {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class BranchStmtNode(
    ctx: CodeContext,
    val cond: IExpression,
    val thenBody: IStatement,
    val elseBody: IStatement?
): ASTNode(ctx), IStatement {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

sealed interface IForLoopInit

class ForLoopStmtNode(
    ctx: CodeContext,
    val init: IForLoopInit?,
    val cond: IExpression?,
    val iter: IExpression?,
    val body: IStatement
): ASTNode(ctx), IStatement {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class WhileLoopStmtNode(
    ctx: CodeContext,
    val cond: IExpression,
    val body: IStatement
): ASTNode(ctx), IStatement {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class ControlStmtNode(
    ctx: CodeContext,
    val ctrl: String
): ASTNode(ctx), IStatement {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class ReturnStmtNode(
    ctx: CodeContext,
    val value: IExpression?
): ASTNode(ctx), IStatement {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

