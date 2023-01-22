package org.kadf.app.eureka.ast.nodes

import org.kadf.app.eureka.utils.CodeContext
import org.kadf.app.eureka.ast.*

class AstVarDeclNode(
    ctx: CodeContext,
    val type: AstType,
    val ids: List<String>,
    val inits: List<AstNode?>
): AstNode(ctx) {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class AstClassDeclNode(
    ctx: CodeContext,
    val id: String,
    val type: AstUserDefType,
    val member: List<AstNode>
): AstNode(ctx) {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

//sealed interface AstClassMember

//sealed interface AstFunction {
//    val body: List<AstNode>
//}

class AstConstrNode(
    ctx: CodeContext,
    val id: String,
    val body: List<AstNode>
): AstNode(ctx) {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class AstFuncDeclNode(
    ctx: CodeContext,
    val funcId: String,
    val type: AstFuncType,
    val paraId: List<String>,
    val body: List<AstNode>
): AstNode(ctx) {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

