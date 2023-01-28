package org.kadf.app.eureka.ast.nodes

import org.kadf.app.eureka.utils.CodeContext
import org.kadf.app.eureka.ast.*

class AstVarDeclNode(
    ctx: CodeContext,
    val type: AstType,
    val ids: List<String>,
    val inits: List<AstNode?>
): AstNode(ctx) {
    val declInfo = ids.zip(inits)
    var isGlobal: Boolean = false
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

class AstConstrNode(
    ctx: CodeContext,
    val id: String,
    val body: List<AstNode>
): AstNode(ctx) {
    var memberOf: AstType? = null
    val isGloabl: Boolean = memberOf == null
    var hasReturned: Boolean = false
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class AstFuncDeclNode(
    ctx: CodeContext,
    val funcId: String,
    val funcType: AstFuncType,
    val paraIds: List<String>,
    val body: List<AstNode>
): AstNode(ctx) {
    val retType = funcType.retType
    val paraTypes = funcType.paraTypes
    val paraInfo = paraIds.zip(funcType.paraTypes)
    var memberOf: AstType? = null
    val isGlobal: Boolean get() = memberOf == null
    val isMainFunc: Boolean = funcId == "main"
    var hasReturn: Boolean = false
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

