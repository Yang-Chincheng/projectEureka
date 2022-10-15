package org.kadf.app.eureka.ast.nodes

import org.kadf.app.eureka.CodeContext
import org.kadf.app.eureka.ast.ASTVisitor
import org.kadf.app.eureka.ast.IDeclarableType
import org.kadf.app.eureka.ast.IReturnableType

class VarDeclNode(
    ctx: CodeContext,
    val type: IDeclarableType,
    val decls: List<VarDeclEntry>
): ASTNode(ctx), IDeclaration, IStatement, IForLoopInit {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

data class VarDeclEntry(
    val ctx: CodeContext,
    val id: String,
    val init: IExpression
)

class ClassDeclNode(
    ctx: CodeContext,
    val id: String,
    val member: List<IClassMember>
): ASTNode(ctx), IDeclaration {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

sealed interface IClassMember

class ConstrNode(
    ctx: CodeContext,
    val body: BlockStmtNode
): ASTNode(ctx), IClassMember {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class FuncDeclNode(
    ctx: CodeContext,
    val retType: IReturnableType,
    val id: String,
    val params: List<FuncParamEntry>,
    val body: BlockStmtNode
): ASTNode(ctx), IDeclaration, IClassMember {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

data class FuncParamEntry(
    val ctx: CodeContext,
    val type: IDeclarableType,
    val id: String
)

