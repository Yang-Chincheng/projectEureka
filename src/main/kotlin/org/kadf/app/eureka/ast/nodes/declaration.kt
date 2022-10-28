package org.kadf.app.eureka.ast.nodes

import org.kadf.app.eureka.utils.CodeContext
import org.kadf.app.eureka.ast.*

class VarDeclNode(
    ctx: CodeContext,
    val type: IDeclarableType,
    val ids: List<String>,
    val inits: List<IExpression?>
): ASTNode(ctx), IDeclaration, IStatement, IClassMember, IForLoopInit {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class ClassDeclNode(
    ctx: CodeContext,
    val id: String,
    val type: UserDefinedType,
    val member: List<IClassMember>
): ASTNode(ctx), IDeclaration {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

sealed interface IClassMember

class ConstrNode(
    ctx: CodeContext,
    val id: String,
    val body: List<IStatement>
): ASTNode(ctx), IClassMember {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

class FuncDeclNode(
    ctx: CodeContext,
    val id: String,
    val type: FunctionType,
    val ids: List<String>,
    val body: List<IStatement>
): ASTNode(ctx), IDeclaration, IClassMember {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

