package org.kadf.app.eureka.ast.nodes

import org.kadf.app.eureka.utils.CodeContext
import org.kadf.app.eureka.ast.*

sealed interface IExpression: IForLoopInit
//sealed interface ILValueExpression : IExpression
//sealed interface IRValueExpression : IExpression

class LiteralNode(
    ctx: CodeContext,
    val type: IType,
    val literal: String
) : ASTNode(ctx), IExpression {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class VariableExprNode(
    ctx: CodeContext,
    val id: String
) : ASTNode(ctx), IExpression {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class InvocationExprNode(
    ctx: CodeContext,
    val func: String,
    val args: List<IExpression>
): ASTNode(ctx), IExpression {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class PropertyExprNode(
    ctx: CodeContext,
    val obj: IExpression,
    val id: String
): ASTNode(ctx), IExpression {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class MethodExprNode(
    ctx: CodeContext,
    val obj: IExpression,
    val func: String,
    val args: List<IExpression>
): ASTNode(ctx), IExpression {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class IndexExprNode(
    ctx: CodeContext,
    val arr: IExpression,
    val idx: IExpression
) : ASTNode(ctx), IExpression {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class LambdaExprNode(
    ctx: CodeContext,
    val cap: Boolean,
    val type: FunctionType,
    val ids: List<String>,
    val body: List<IStatement>,
    val args: List<IExpression>
) : ASTNode(ctx), IExpression {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class AssignExprNode(
    ctx: CodeContext,
    val lhs: IExpression,
    val rhs: IExpression
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

sealed interface INewableSyntax: IExpression

class NewArrayNode(
    ctx: CodeContext,
    val type: ArrayType,
    val scales: List<IExpression?>
): ASTNode(ctx), INewableSyntax {
    init {
        if (scales.size > type.dim) throw Exception()
    }
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class NewObjectNode(
    ctx: CodeContext,
    val type: IDeclarableType
): ASTNode(ctx), INewableSyntax {
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
    val lhs: IExpression,
    val rhs: IExpression,
    val op: Operator
): ASTNode(ctx), IExpression {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}