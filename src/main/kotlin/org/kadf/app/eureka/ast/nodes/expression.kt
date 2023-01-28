package org.kadf.app.eureka.ast.nodes

import org.kadf.app.eureka.utils.CodeContext
import org.kadf.app.eureka.ast.*

class AstLiteralNode(
    ctx: CodeContext,
    val type: AstType,
    val literal: String
) : AstNode(ctx) {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class AstVariableExprNode(
    ctx: CodeContext,
    val id: String
) : AstNode(ctx) {
    var memberOf: AstType? = null
    val isMember: Boolean get() = memberOf != null
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class AstInvocationExprNode(
    ctx: CodeContext,
    val funcId: String,
    val args: List<AstNode>
): AstNode(ctx) {
    var memberOf: AstType? = null
    val isMember: Boolean get() = memberOf != null
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class AstPropertyExprNode(
    ctx: CodeContext,
    val obj: AstNode,
    val id: String
): AstNode(ctx) {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
    fun collect(): List<AstPropertyExprNode> {
        return if (obj !is AstPropertyExprNode) listOf(this)
        else obj.collect() + listOf(this)
    }
}

class AstMethodExprNode(
    ctx: CodeContext,
    val obj: AstNode,
    val funcId: String,
    val args: List<AstNode>
): AstNode(ctx) {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class AstIndexExprNode(
    ctx: CodeContext,
    val arr: AstNode,
    val idx: AstNode
) : AstNode(ctx) {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
    fun collect(): List<AstIndexExprNode> {
        return if (arr !is AstIndexExprNode) listOf(this)
        else arr.collect() + listOf(this)
    }
}

class AstLambdaExprNode(
    ctx: CodeContext,
    val cap: Boolean,
    val type: AstFuncType,
    val ids: List<String>,
    val body: List<AstNode>,
    val args: List<AstNode>
) : AstNode(ctx) {
    val paraTypes = type.paraTypes
    val retType = type.retType
    val paraInfo = ids.zip(type.paraTypes)
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class AstAssignExprNode(
    ctx: CodeContext,
    val lhs: AstNode,
    val rhs: AstNode
) : AstNode(ctx) {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
    fun collectRightAssoc(): List<AstNode> {
        return if (rhs !is AstAssignExprNode) listOf(lhs, rhs)
        else listOf(lhs) + rhs.collectRightAssoc()
    }
}

class AstCompoundExprNode(
    ctx: CodeContext,
    val rest: List<AstNode>,
    val last: AstNode
): AstNode(ctx) {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class AstNewArrayNode(
    ctx: CodeContext,
    val type: AstArrayType,
    val scales: List<AstNode?>
): AstNode(ctx) {
    init {
        if (scales.size > type.dim) throw Exception()
    }
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class AstNewObjectNode(
    ctx: CodeContext,
    val type: AstType
): AstNode(ctx) {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class AstUnaryExprNode(
    ctx: CodeContext,
    val expr: AstNode,
    val op: AstOperator
): AstNode(ctx) {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class AstBinaryExprNode(
    ctx: CodeContext,
    val lhs: AstNode,
    val rhs: AstNode,
    val op: AstOperator
): AstNode(ctx) {
    fun collectRightAssoc(): List<AstNode> {
        return if (rhs !is AstBinaryExprNode || rhs.op != op) listOf(lhs, rhs)
        else listOf(lhs) + rhs.collectRightAssoc()
    }
    fun collectLeftAssoc(): List<AstNode> {
        return if (lhs !is AstBinaryExprNode || lhs.op != op) listOf(lhs, rhs)
        else lhs.collectLeftAssoc() + listOf(rhs)
    }
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}