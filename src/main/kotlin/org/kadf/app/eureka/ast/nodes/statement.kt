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

class ExprStmtNode(
    ctx: CodeContext,
    val expr: IExpression
): ASTNode(ctx), IStatement {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class BranchStmtNode(
    ctx: CodeContext,
    val cond: IExpression,
    val ifBody: BlockStmtNode,
    val elseBody: BlockStmtNode?
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

class BreakStmtNode(
    ctx: CodeContext
): ASTNode(ctx), IStatement {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class ContinueStmtNode(
    ctx: CodeContext
): ASTNode(ctx), IStatement {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class ReturnStmtNode(
    ctx: CodeContext,
    val value: IExpression?
): ASTNode(ctx), IStatement {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

