package org.kadf.app.eureka.ast

import org.antlr.v4.runtime.ParserRuleContext
import org.kadf.app.eureka.*
import org.kadf.app.eureka.ast.nodes.*

class ASTBuilder(private val src: CodeSource?) : MxStarParserBaseVisitor<ASTNode>() {

    private val ParserRuleContext.ctx: CodeContext
        get() = CodeContext(src, this)

    override fun visitProgram(node: MxStarParser.ProgramContext?) = ProgramNode(
        node!!.ctx,
        node.progDecl()?.map { visit(it) as IDeclaration } ?: listOf()
    )

    override fun visitProgVar(node: MxStarParser.ProgVarContext?) = visit(node!!.varDecl()) as VarDeclNode

    override fun visitProgFunc(node: MxStarParser.ProgFuncContext?) = visit(node!!.funcDecl()) as FuncDeclNode

    override fun visitProgClass(node: MxStarParser.ProgClassContext?) = visit(node!!.classDecl()) as ClassDeclNode

    override fun visitVarDecl(node: MxStarParser.VarDeclContext?) = VarDeclNode(
        node!!.ctx,
        node.declarableType().type,
        node.entries.map { it.Identifier().text },
        node.entries.map { init -> init.expression()?.let { visit(it) as IExpression} }
    )

    override fun visitClassDecl(node: MxStarParser.ClassDeclContext?) = ClassDeclNode(
        node!!.ctx,
        node.Identifier().text,
        UserDefinedType(node.Identifier().text),
        node.classBlock().classMember()?.map { visit(it) as IClassMember } ?: listOf()
    )

    override fun visitClassConstr(node: MxStarParser.ClassConstrContext?) = ConstrNode(
        node!!.ctx,
        node.Identifier().text,
        node.stmtBlock().statement()?.map { visit(it) as IStatement } ?: listOf()
    )

    override fun visitClassMethod(node: MxStarParser.ClassMethodContext?) = visit(node!!.funcDecl()) as FuncDeclNode

    override fun visitClassProper(node: MxStarParser.ClassProperContext?) = visit(node!!.varDecl()) as VarDeclNode

    override fun visitFuncDecl(node: MxStarParser.FuncDeclContext?) = FuncDeclNode(
        node!!.ctx,
        node.Identifier().text,
        FunctionType(
            node.returnableType().type,
            node.parameterList().entrys.map { it.declarableType().type }
        ),
        node.parameterList().entrys.map { it.Identifier().text },
        node.stmtBlock().statement()?.map { visit(it) as IStatement } ?: listOf()
    )

    override fun visitStmtBlock(node: MxStarParser.StmtBlockContext?) = BlockStmtNode(
        node!!.ctx,
        node.statement()?.map { visit(it) as IStatement } ?: listOf()
    )

    override fun visitExprStmt(node: MxStarParser.ExprStmtContext?) = ExprStmtNode(
        node!!.ctx,
        visit(node.expression()) as IExpression
    )

    override fun visitDeclStmt(node: MxStarParser.DeclStmtContext?) = VarDeclStmtNode(
        node!!.ctx,
        visit(node.varDecl()) as VarDeclNode
    )

    override fun visitBranchStmt(node: MxStarParser.BranchStmtContext?) = BranchStmtNode(
        node!!.ctx,
        visit(node.condi) as IExpression,
        visit(node.thenblk) as IStatement,
        node.elseblk?.let { visit(node.elseblk) as IStatement }
    )

    override fun visitForLoopStmt(node: MxStarParser.ForLoopStmtContext?) = ForLoopStmtNode(
        node!!.ctx,
        node.init?.let {visit(node.init) as IForLoopInit},
        node.condi?.let {visit(node.condi) as IExpression},
        node.iter?.let {visit(node.iter) as IExpression},
        visit(node.statement()) as IStatement
    )

    override fun visitWhileLoopStmt(node: MxStarParser.WhileLoopStmtContext?) = WhileLoopStmtNode(
        node!!.ctx,
        node.condi?.let { visit(node.condi) as IExpression } ?: throw Exception(),
        visit(node.statement()) as IStatement
    )

    override fun visitCtrlStmt(node: MxStarParser.CtrlStmtContext?) = ControlStmtNode(
        node!!.ctx, node.ctrl.text
    )

    override fun visitReturnStmt(node: MxStarParser.ReturnStmtContext?) = ReturnStmtNode(
        node!!.ctx,
        node.ret?.let { visit(node.ret) as IExpression}
    )

    override fun visitBlockStmt(node: MxStarParser.BlockStmtContext?) = BlockStmtNode(
        node!!.ctx,
        node.stmtBlock().statement()?.map { visit(it) as IStatement } ?: listOf()
    )

    override fun visitEmptyStmt(node: MxStarParser.EmptyStmtContext?) = EmptyStmtNode(node!!.ctx)

    override fun visitLiteralExpr(node: MxStarParser.LiteralExprContext?) = LiteralNode(
        node!!.ctx,
        with(node.literal()) {
            when {
                This() != null -> AnyType
                Null() != null -> NullType
                True() != null -> BooleanType
                False() != null -> BooleanType
                IntLiteral() != null -> IntegerType
                StrLiteral() != null -> StringType
                else -> NothingType
            }
        },
        node.literal().text
    )

    override fun visitAtomicVarExpr(node: MxStarParser.AtomicVarExprContext?) = VariableExprNode(
        node!!.ctx, node.Identifier().text
    )

    override fun visitAtomicFuncInvkExpr(node: MxStarParser.AtomicFuncInvkExprContext?) = InvocationExprNode(
        node!!.ctx,
        node.func.text,
        node.call.args?.filterNotNull()?.map { visit(it) as IExpression } ?: listOf()
    )

    override fun visitMemberVarExpr(node: MxStarParser.MemberVarExprContext?) = PropertyExprNode(
        node!!.ctx,
        visit(node.obj) as IExpression,
        node.Identifier().text
    )

    override fun visitMemberFuncInvkExpr(node: MxStarParser.MemberFuncInvkExprContext?) = MethodExprNode(
        node!!.ctx,
        visit(node.obj) as IExpression,
        node.Identifier().text,
        node.call.args?.filterNotNull()?.map { visit(it) as IExpression } ?: listOf()
    )

    override fun visitIndexExpr(node: MxStarParser.IndexExprContext?) = IndexExprNode(
        node!!.ctx,
        visit(node.arr) as IExpression,
        visit(node.idx) as IExpression
    )

    override fun visitLambdaInvkExpr(node: MxStarParser.LambdaInvkExprContext?) = LambdaExprNode(
        node!!.ctx,
        node.cap != null,
        FunctionType(
            UnitType, // will be inferred afterwards
            node.parameterList().entrys?.map { it.declarableType().type } ?: listOf()
        ),
        node.parameterList().entrys?.map { it.Identifier().text } ?: listOf(),
        node.stmtBlock().statement()?.map { visit(it) as IStatement } ?: listOf(),
        node.argumentList().args?.filterNotNull()?.map { visit(it) as IExpression } ?: listOf()
    )

    override fun visitNewUnscaledArray(node: MxStarParser.NewUnscaledArrayContext?) = NewArrayNode(
        node!!.ctx,
        ArrayType(node.nonVoidType().type, node.total.size),
        listOf()
    )

    override fun visitNewScaledArray(node: MxStarParser.NewScaledArrayContext?) = NewArrayNode(
        node!!.ctx,
        ArrayType(node.nonVoidType().type, node.total.size),
        node.scales.map { visit(it) as IExpression }
    )

    override fun visitNewObject(node: MxStarParser.NewObjectContext?) = NewObjectNode(
        node!!.ctx,
        node.nonVoidType().type
    )

    override fun visitPreUpdExpr(node: MxStarParser.PreUpdExprContext?) = UnaryExprNode(
        node!!.ctx,
        visit(node.obj) as IExpression,
        when(node.op.text) {
            "++" -> Operator.PRE_INC
            "--" -> Operator.PRE_DEC
            else -> throw Exception()
        }
    )

    override fun visitPostUpdExpr(node: MxStarParser.PostUpdExprContext?) = UnaryExprNode(
        node!!.ctx,
        visit(node.obj) as IExpression,
        when(node.op.text) {
            "++" -> Operator.POST_INC
            "--" -> Operator.POST_DEC
            else -> throw Exception()
        }
    )

    override fun visitUnaryExpr(node: MxStarParser.UnaryExprContext?) = UnaryExprNode(
        node!!.ctx,
        visit(node.rhs) as IExpression,
        when(node.op.text) {
            "!" -> Operator.LOGIC_NOT
            "~" -> Operator.NOT
            "+" -> Operator.POSIT
            "-" -> Operator.NEG
            else -> throw Exception()
        }
    )

    override fun visitBinaryExpr(node: MxStarParser.BinaryExprContext?) = BinaryExprNode(
        node!!.ctx,
        visit(node.lhs) as IExpression,
        visit(node.rhs) as IExpression,
        when(node.op.text) {
            "+" -> Operator.PLUS
            "-" -> Operator.MINUS
            "*" -> Operator.MUL
            "/" -> Operator.DIV
            "%" -> Operator.MOD
            "<" -> Operator.LT
            ">" -> Operator.GT
            "<=" -> Operator.LE
            ">=" -> Operator.GE
            "==" -> Operator.EQ
            "!=" -> Operator.NE
            "||" -> Operator.LOGIC_OR
            "&&" -> Operator.LOGIC_AND
            "&" -> Operator.AND
            "|" -> Operator.OR
            "^" -> Operator.XOR
            "<<" -> Operator.LSH
            ">>" -> Operator.RSH
            else -> throw Exception()
        }
    )

    override fun visitAssignExpr(node: MxStarParser.AssignExprContext?) = AssignExprNode(
        node!!.ctx,
        visit(node.lhs) as IExpression,
        visit(node.rhs) as IExpression
    )

    override fun visitCompndExpr(node: MxStarParser.CompndExprContext?) = CompoundExprNode(
        node!!.ctx,
        node.rest?.map { visit(it) as IExpression } ?: listOf(),
        visit(node.last) as IExpression
    )
}
