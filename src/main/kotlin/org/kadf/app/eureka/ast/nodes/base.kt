package org.kadf.app.eureka.ast.nodes

import org.kadf.app.eureka.utils.CodeContext
import org.kadf.app.eureka.ast.ASTVisitor
import org.kadf.app.eureka.ast.AstNothingType
import org.kadf.app.eureka.ast.AstType
import org.kadf.app.eureka.ast.AstUnitType
import org.kadf.app.eureka.semantic.TypeEnv
import org.kadf.app.eureka.semantic.VarEnv

sealed class AstNode(val ctx: CodeContext) {
    abstract fun accept(visitor: ASTVisitor): Any
    lateinit var env: VarEnv
    var astType: AstType = AstNothingType
    var inferType: AstType = AstNothingType
}

class AstProgramNode(
    ctx: CodeContext,
    val decls: List<AstNode>
): AstNode(ctx) {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}
