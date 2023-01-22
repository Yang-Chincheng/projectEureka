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
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class AstInvocationExprNode(
    ctx: CodeContext,
    val funcId: String,
    val args: List<AstNode>
): AstNode(ctx) {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class AstPropertyExprNode(
    ctx: CodeContext,
    val obj: AstNode,
    val id: String
): AstNode(ctx) {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
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
}

class AstLambdaExprNode(
    ctx: CodeContext,
    val cap: Boolean,
    val type: AstFuncType,
    val ids: List<String>,
    val body: List<AstNode>,
    val args: List<AstNode>
) : AstNode(ctx) {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class AstAssignExprNode(
    ctx: CodeContext,
    val lhs: AstNode,
    val rhs: AstNode
) : AstNode(ctx) {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
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
    val op: Operator
): AstNode(ctx) {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class AstBinaryExprNode(
    ctx: CodeContext,
    val lhs: AstNode,
    val rhs: AstNode,
    val op: Operator
): AstNode(ctx) {
    private fun isLast() = rhs !is AstBinaryExprNode || rhs.op != op
    fun collect(): List<AstNode> {
        var cur = this
        val ret = mutableListOf<AstNode>()
        while(!cur.isLast()) {
            ret.add(cur.lhs)
            cur = cur.rhs as AstBinaryExprNode
        }
        ret.add(cur.lhs)
        ret.add(cur.rhs)
        return ret
    }
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}