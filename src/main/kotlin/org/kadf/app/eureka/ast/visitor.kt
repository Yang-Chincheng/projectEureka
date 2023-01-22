package org.kadf.app.eureka.ast

import org.kadf.app.eureka.ast.nodes.*

interface ASTVisitor {

    fun visit(node: AstProgramNode): Any
    fun visit(node: AstVarDeclNode): Any
    fun visit(node: AstClassDeclNode): Any
    fun visit(node: AstFuncDeclNode): Any
    fun visit(node: AstConstrNode): Any
    fun visit(node: AstEmptyStmtNode): Any
    fun visit(node: AstBlockStmtNode): Any
    fun visit(node: AstExprStmtNode): Any
    fun visit(node: AstVarDeclStmtNode): Any
    fun visit(node: AstBranchStmtNode): Any
    fun visit(node: AstForLoopStmtNode): Any
    fun visit(node: AstWhileLoopStmtNode): Any
    fun visit(node: AstControlStmtNode): Any
    fun visit(node: AstReturnStmtNode): Any
    fun visit(node: AstLiteralNode): Any
    fun visit(node: AstVariableExprNode): Any
    fun visit(node: AstInvocationExprNode): Any
    fun visit(node: AstPropertyExprNode): Any
    fun visit(node: AstMethodExprNode): Any
    fun visit(node: AstIndexExprNode): Any
    fun visit(node: AstLambdaExprNode): Any
    fun visit(node: AstAssignExprNode): Any
    fun visit(node: AstCompoundExprNode): Any
    fun visit(node: AstUnaryExprNode): Any
    fun visit(node: AstBinaryExprNode): Any
    fun visit(node: AstNewArrayNode): Any
    fun visit(node: AstNewObjectNode): Any

}
