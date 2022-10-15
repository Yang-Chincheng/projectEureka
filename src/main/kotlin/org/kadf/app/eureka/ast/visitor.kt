package org.kadf.app.eureka.ast

import org.kadf.app.eureka.ast.nodes.*

sealed interface ASTVisitor {
    fun visit(node: ProgramNode) {}
    fun visit(node: VarDeclNode) {}
    fun visit(node: ClassDeclNode) {}
    fun visit(node: FuncDeclNode) {}
    fun visit(node: ConstrNode) {}

    fun visit(node: EmptyStmtNode) {}
    fun visit(node: BlockStmtNode) {}
    fun visit(node: ExprStmtNode) {}
    fun visit(node: BranchStmtNode) {}
    fun visit(node: ForLoopStmtNode) {}
    fun visit(node: WhileLoopStmtNode) {}
    fun visit(node: BreakStmtNode) {}
    fun visit(node: ContinueStmtNode) {}
    fun visit(node: ReturnStmtNode) {}

    fun visit(node: IAtomic) {}
    fun visit(node: IndexExprNode) {}
    fun visit(node: AccessExprNode) {}
    fun visit(node: InvokeExprNode) {}
    fun visit(node: LambdaExprNode) {}
    fun visit(node: NewExprNode) {}
    fun visit(node: AssignExprNode) {}
    fun visit(node: CompoundExprNode) {}
    fun visit(node: UnaryExprNode) {}
    fun visit(node: BinaryExprNode) {}
}