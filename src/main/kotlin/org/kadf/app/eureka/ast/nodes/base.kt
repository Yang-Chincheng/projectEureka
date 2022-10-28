package org.kadf.app.eureka.ast.nodes

import org.kadf.app.eureka.utils.CodeContext
import org.kadf.app.eureka.ast.ASTVisitor

sealed class ASTNode(val ctx: CodeContext?) {
    abstract fun accept(visitor: ASTVisitor): Any
}

sealed interface IDeclaration

class ProgramNode(
    ctx: CodeContext,
    val decls: List<IDeclaration>
): ASTNode(ctx) {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}
