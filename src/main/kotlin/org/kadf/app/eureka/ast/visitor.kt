package org.kadf.app.eureka.ast

import org.kadf.app.eureka.ast.nodes.*

interface ASTVisitor {

    fun visit(node: AstNode) = when (node) {
        is AstProgramNode -> node.accept(this)
        is AstVarDeclNode -> node.accept(this)
        is AstClassDeclNode -> node.accept(this)
        is AstConstrNode -> node.accept(this)
        is AstFuncDeclNode -> node.accept(this)
        is AstEmptyStmtNode -> node.accept(this)
        is AstBlockStmtNode -> node.accept(this)
        is AstExprStmtNode -> node.accept(this)
        is AstVarDeclStmtNode -> node.accept(this)
        is AstLiteralNode -> node.accept(this)
        is AstAssignExprNode -> node.accept(this)
        is AstUnaryExprNode -> node.accept(this)
        is AstBinaryExprNode -> node.accept(this)
        is AstBranchStmtNode -> node.accept(this)
        is AstForLoopStmtNode -> node.accept(this)
        is AstWhileLoopStmtNode -> node.accept(this)
        is AstReturnStmtNode -> node.accept(this)
        is AstControlStmtNode -> node.accept(this)
        is AstNewArrayNode -> node.accept(this)
        is AstNewObjectNode -> node.accept(this)
        is AstVariableExprNode -> node.accept(this)
        is AstInvocationExprNode -> node.accept(this)
        is AstPropertyExprNode -> node.accept(this)
        is AstMethodExprNode -> node.accept(this)
        is AstIndexExprNode -> node.accept(this)
        is AstLambdaExprNode -> node.accept(this)
        is AstCompoundExprNode -> node.accept(this)
    }

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
