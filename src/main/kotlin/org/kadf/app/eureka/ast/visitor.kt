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

//    fun visit(node: AstProgramNode): Any = Unit
//    fun visit(node: AstVarDeclNode): Any = Unit
//    fun visit(node: AstClassDeclNode): Any = Unit
//    fun visit(node: AstFuncDeclNode): Any = Unit
//    fun visit(node: AstConstrNode): Any = Unit
//    fun visit(node: AstEmptyStmtNode): Any = Unit
//    fun visit(node: AstBlockStmtNode): Any = Unit
//    fun visit(node: AstExprStmtNode): Any = Unit
//    fun visit(node: AstVarDeclStmtNode): Any = Unit
//    fun visit(node: AstBranchStmtNode): Any = Unit
//    fun visit(node: AstForLoopStmtNode): Any = Unit
//    fun visit(node: AstWhileLoopStmtNode): Any = Unit
//    fun visit(node: AstControlStmtNode): Any = Unit
//    fun visit(node: AstReturnStmtNode): Any = Unit
//    fun visit(node: AstLiteralNode): Any = Unit
//    fun visit(node: AstVariableExprNode): Any = Unit
//    fun visit(node: AstInvocationExprNode): Any = Unit
//    fun visit(node: AstPropertyExprNode): Any = Unit
//    fun visit(node: AstMethodExprNode): Any = Unit
//    fun visit(node: AstIndexExprNode): Any = Unit
//    fun visit(node: AstLambdaExprNode): Any = Unit
//    fun visit(node: AstAssignExprNode): Any = Unit
//    fun visit(node: AstCompoundExprNode): Any = Unit
//    fun visit(node: AstUnaryExprNode): Any = Unit
//    fun visit(node: AstBinaryExprNode): Any = Unit
//    fun visit(node: AstNewArrayNode): Any = Unit
//    fun visit(node: AstNewObjectNode): Any = Unit

//fun visit(node: AstNode) = when (node) {
//    is AstProgramNode -> {
//        println("here prog"); node.accept(this)
//    }
//
//    is AstVarDeclNode -> {
//        println("here var decl"); node.accept(this)
//    }
//
//    is AstClassDeclNode -> {
//        println("here class decl");node.accept(this)
//    }
//
//    is AstConstrNode -> {
//        println("here constructor decl");node.accept(this)
//    }
//
//    is AstFuncDeclNode -> {
//        println("here func decl");node.accept(this)
//    }
//
//    is AstEmptyStmtNode -> {
//        println("here empty stmt");node.accept(this)
//    }
//
//    is AstBlockStmtNode -> {
//        println("here block stmt");node.accept(this)
//    }
//
//    is AstExprStmtNode -> {
//        println("here expr stmt");node.accept(this)
//    }
//
//    is AstVarDeclStmtNode -> {
//        println("here var decl stmt");node.accept(this)
//    }
//
//    is AstLiteralNode -> {
//        println("here literal");node.accept(this)
//    }
//
//    is AstAssignExprNode -> {
//        println("here assign");node.accept(this)
//    }
//
//    is AstUnaryExprNode -> {
//        println("here unary");node.accept(this)
//    }
//
//    is AstBinaryExprNode -> {
//        println("here binary");node.accept(this)
//    }
//
//    is AstBranchStmtNode -> {
//        println("here branch");node.accept(this)
//    }
//
//    is AstForLoopStmtNode -> {
//        println("here for loop");node.accept(this)
//    }
//
//    is AstWhileLoopStmtNode -> {
//        println("here while loop");node.accept(this)
//    }
//
//    is AstReturnStmtNode -> {
//        println("here return stmt");node.accept(this)
//    }
//
//    is AstControlStmtNode -> {
//        println("here control");node.accept(this)
//    }
//
//    is AstNewArrayNode -> {
//        println("here new array");node.accept(this)
//    }
//
//    is AstNewObjectNode -> {
//        println("here new obj");node.accept(this)
//    }
//
//    is AstVariableExprNode -> {
//        println("here var");node.accept(this)
//    }
//
//    is AstInvocationExprNode -> {
//        println("here invoke");node.accept(this)
//    }
//
//    is AstPropertyExprNode -> {
//        println("here prop");node.accept(this)
//    }
//
//    is AstMethodExprNode -> {
//        println("here method");node.accept(this)
//    }
//
//    is AstIndexExprNode -> {
//        println("here indexing"); node.accept(this)
//    }
//
//    is AstLambdaExprNode -> {
//        println("here lambda");node.accept(this)
//    }
//
//    is AstCompoundExprNode -> {
//        println("here comp");node.accept(this)
//    }
//}
//
}