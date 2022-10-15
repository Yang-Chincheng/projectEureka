package org.kadf.app.eureka.ast.nodes

import org.kadf.app.eureka.CodeContext
import org.kadf.app.eureka.ast.ASTVisitor
import org.kadf.app.eureka.ast.INewableType
import org.kadf.app.eureka.ast.Operator

sealed interface IExpression
sealed interface ILValueExpression : IExpression
sealed interface IRValueExpression : IExpression

sealed interface IAtomic

sealed interface ICallable
sealed interface INewable
sealed interface IAccessible
sealed interface IIndexable

class IdentifierNode(
    ctx: CodeContext,
    val id: String
) : ASTNode(ctx), IExpression, IAtomic {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class IndexExprNode(
    ctx: CodeContext,
    val arr: IExpression,
    val idx: IExpression
) : ASTNode(ctx), IExpression {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class AccessExprNode(
    ctx: CodeContext,
    val obj: IExpression,
    val prop: IExpression
) : ASTNode(ctx), IExpression {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class InvokeExprNode(
    ctx: CodeContext,
    val callee: IExpression,
    val args: List<IExpression>
) : ASTNode(ctx), IExpression {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class LambdaExprNode(
    ctx: CodeContext,
    val cap: Boolean,
    val params: List<FuncParamEntry>,
    val body: BlockStmtNode
) : ASTNode(ctx), IExpression {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class AssignExprNode(
    ctx: CodeContext,
    val variable: IExpression,
    val value: IExpression
) : ASTNode(ctx), IExpression {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class CompoundExprNode(
    ctx: CodeContext,
    val rest: List<IExpression>,
    val last: IExpression
): ASTNode(ctx), IExpression {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class NewExprNode(
    ctx: CodeContext,
    val type: INewableType
): ASTNode(ctx), IExpression {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class UnaryExprNode(
    ctx: CodeContext,
    val expr: IExpression,
    val op: Operator
): ASTNode(ctx), IExpression {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class BinaryExprNode(
    ctx: CodeContext,
    val lexpr: IExpression,
    val rexpr: IExpression,
    val op: Operator
): ASTNode(ctx), IExpression {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}