package org.kadf.app.eureka.ast.nodes

import org.kadf.app.eureka.CodeContext
import org.kadf.app.eureka.ast.ASTVisitor

sealed interface ILiteral: IAtomic

class StringLiteralNode(
    ctx: CodeContext,
    val value: String
): ASTNode(ctx), IExpression, ILiteral {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class IntegerLiteralNode(
    ctx: CodeContext,
    val value: Int
): ASTNode(ctx), IExpression, ILiteral {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class NullLiteralNode(
    ctx: CodeContext
): ASTNode(ctx), IExpression, ILiteral {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class BooleanLiteralNode(
    ctx: CodeContext,
    val value: Boolean
): ASTNode(ctx), IExpression, ILiteral {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class ThisLiteralNode(
    ctx: CodeContext
): ASTNode(ctx), IExpression, ILiteral {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}