package org.kadf.app.eureka.ast

import org.antlr.v4.runtime.ParserRuleContext
import org.kadf.app.eureka.*
import org.kadf.app.eureka.MxStarParser.*
import org.kadf.app.eureka.ast.nodes.*
import org.kadf.app.eureka.utils.*

class ASTBuilder(private val src: CodeSource?) : MxStarParserBaseVisitor<AstNode>() {

    private val ParserRuleContext.ctx: CodeContext
        get() = CodeContext(src, this)

    fun build(node: ParserRuleContext) = visitProgram(node as ProgramContext)

    override fun visitProgram(node: ProgramContext?) = AstProgramNode(
        node!!.ctx,
        node.progDecl()?.map { visit(it) } ?: listOf()
    )

    override fun visitProgVar(node: ProgVarContext?) = visit(node!!.varDecl()) as AstVarDeclNode

    override fun visitProgFunc(node: ProgFuncContext?) = visit(node!!.funcDecl()) as AstFuncDeclNode

    override fun visitProgClass(node: ProgClassContext?) = visit(node!!.classDecl()) as AstClassDeclNode

    override fun visitVarDecl(node: VarDeclContext?) = AstVarDeclNode(
        ctx = node!!.ctx,
        type = node.declarableType().type,
        ids = node.entries.map { it.Identifier().text },
        inits = node.entries
            .map { init ->
                init.expression()?.let { visit(it) }
            }
    )

    override fun visitClassDecl(node: ClassDeclContext?) = AstClassDeclNode(
        ctx = node!!.ctx,
        id = node.Identifier().text,
        type = AstUserDefType(node.Identifier().text),
        member = node.classBlock().classMember()
            ?.map { visit(it) } ?: listOf()
    )

    override fun visitClassConstr(node: ClassConstrContext?) = AstConstrNode(
        ctx = node!!.ctx,
        id = node.Identifier().text,
        body = node.stmtBlock().statement()
            ?.map { visit(it) } ?: listOf()
    )

    override fun visitClassMethod(node: ClassMethodContext?) = visit(node!!.funcDecl()) as AstFuncDeclNode

    override fun visitClassProper(node: ClassProperContext?) = visit(node!!.varDecl()) as AstVarDeclNode

    override fun visitFuncDecl(node: FuncDeclContext?) = AstFuncDeclNode(
        ctx = node!!.ctx,
        funcId = node.Identifier().text,
        funcType = AstFuncType(
            node.returnableType().type,
            node.parameterList().entrys
                .map { it.declarableType().type }
        ),
        paraIds = node.parameterList().entrys.map { it.Identifier().text },
        body = node.stmtBlock().statement()
            ?.map { visit(it) } ?: listOf()
    )

    override fun visitStmtBlock(node: StmtBlockContext?) = AstBlockStmtNode(
        ctx = node!!.ctx,
        stmts = node.statement()
            ?.map { visit(it) } ?: listOf()
    )

    override fun visitExprStmt(node: ExprStmtContext?) = AstExprStmtNode(
        node!!.ctx,
        visit(node.expression())
    )

    override fun visitDeclStmt(node: DeclStmtContext?) = AstVarDeclStmtNode(
        node!!.ctx,
        visit(node.varDecl()) as AstVarDeclNode
    )

    override fun visitBranchStmt(node: BranchStmtContext?) = AstBranchStmtNode(
        node!!.ctx,
        visit(node.condi),
        visit(node.thenblk),
        node.elseblk?.let { visit(node.elseblk) }
    )

    override fun visitForLoopStmt(node: ForLoopStmtContext?) = AstForLoopStmtNode(
        node!!.ctx,
        node.init?.let { visit(node.init) },
        node.condi?.let { visit(node.condi) },
        node.iter?.let { visit(node.iter) },
        visit(node.statement())
    )

    override fun visitWhileLoopStmt(node: WhileLoopStmtContext?) = AstWhileLoopStmtNode(
        node!!.ctx,
        node.condi?.let { visit(node.condi) } ?: throw Exception(),
        visit(node.statement())
    )

    override fun visitCtrlStmt(node: CtrlStmtContext?) = AstControlStmtNode(
        node!!.ctx, node.ctrl.text
    )

    override fun visitReturnStmt(node: ReturnStmtContext?) = AstReturnStmtNode(
        node!!.ctx,
        node.ret?.let { visit(node.ret) }
    )

    override fun visitBlockStmt(node: BlockStmtContext?) = AstBlockStmtNode(
        node!!.ctx,
        node.stmtBlock().statement()
            ?.map { visit(it) } ?: listOf()
    )

    override fun visitEmptyStmt(node: EmptyStmtContext?) = AstEmptyStmtNode(node!!.ctx)

    override fun visitLiteralExpr(node: LiteralExprContext?) = AstLiteralNode(
        node!!.ctx,
        with(node.literal()) {
            when {
                This() != null -> AstAnyType
                Null() != null -> AstNullType
                True() != null -> AstBooleanType
                False() != null -> AstBooleanType
                IntLiteral() != null -> AstIntegerType
                StrLiteral() != null -> AstStringType
                else -> AstNothingType
            }
        },
        node.literal().text
    )

    override fun visitAtomicVarExpr(node: AtomicVarExprContext?) = AstVariableExprNode(
        node!!.ctx, node.Identifier().text
    )

    override fun visitAtomicFuncInvkExpr(node: AtomicFuncInvkExprContext?) = AstInvocationExprNode(
        node!!.ctx,
        node.func.text,
        node.call.args?.filterNotNull()
            ?.map { visit(it) } ?: listOf()
    )

    override fun visitMemberVarExpr(node: MemberVarExprContext?) = AstPropertyExprNode(
        node!!.ctx,
        visit(node.obj),
        node.Identifier().text
    )

    override fun visitMemberFuncInvkExpr(node: MemberFuncInvkExprContext?) = AstMethodExprNode(
        node!!.ctx,
        visit(node.obj),
        node.Identifier().text,
        node.call.args
            ?.filterNotNull()
            ?.map { visit(it) } ?: listOf()
    )

    override fun visitIndexExpr(node: IndexExprContext?) = AstIndexExprNode(
        node!!.ctx,
        visit(node.arr),
        visit(node.idx)
    )

    override fun visitLambdaInvkExpr(node: LambdaInvkExprContext?) = AstLambdaExprNode(
        node!!.ctx,
        node.cap != null,
        AstFuncType(
            AstAnyType, // will be inferred afterwards
            node.parameterList().entrys?.map { it.declarableType().type } ?: listOf()
        ),
        node.parameterList().entrys
            ?.map { it.Identifier().text } ?: listOf(),
        node.stmtBlock().statement()
            ?.map { visit(it) } ?: listOf(),
        node.argumentList().args
            ?.filterNotNull()
            ?.map { visit(it) } ?: listOf()
    )

    override fun visitNewArray(node: NewArrayContext?) = AstNewArrayNode(
        node!!.ctx,
        AstArrayType(node.nonVoidType().type, node.arrayDim().size),
        node.arrayDim().map { dim ->
            dim.expression()?.let { visit(it) }
        }
    )

    override fun visitNewObject(node: NewObjectContext?) = AstNewObjectNode(
        node!!.ctx,
        node.nonVoidType().type
    )

    override fun visitPreUpdExpr(node: PreUpdExprContext?) = AstUnaryExprNode(
        node!!.ctx,
        visit(node.obj),
        when (node.op.text) {
            "++" -> AstOperator.PRE_INC
            "--" -> AstOperator.PRE_DEC
            else -> throw Exception()
        }
    )

    override fun visitPostUpdExpr(node: PostUpdExprContext?) = AstUnaryExprNode(
        node!!.ctx,
        visit(node.obj),
        when (node.op.text) {
            "++" -> AstOperator.POST_INC
            "--" -> AstOperator.POST_DEC
            else -> throw Exception()
        }
    )

    override fun visitUnaryExpr(node: UnaryExprContext?) = AstUnaryExprNode(
        node!!.ctx,
        visit(node.rhs),
        when (node.op.text) {
            "!" -> AstOperator.LOGIC_NOT
            "~" -> AstOperator.NOT
            "+" -> AstOperator.POSIT
            "-" -> AstOperator.NEG
            else -> throw Exception()
        }
    )

    override fun visitBinaryExpr(node: BinaryExprContext?) = AstBinaryExprNode(
        node!!.ctx,
        visit(node.lhs),
        visit(node.rhs),
        when (node.op.text) {
            "+" -> AstOperator.PLUS
            "-" -> AstOperator.MINUS
            "*" -> AstOperator.MUL
            "/" -> AstOperator.DIV
            "%" -> AstOperator.MOD
            "<" -> AstOperator.LT
            ">" -> AstOperator.GT
            "<=" -> AstOperator.LE
            ">=" -> AstOperator.GE
            "==" -> AstOperator.EQ
            "!=" -> AstOperator.NE
            "||" -> AstOperator.LOGIC_OR
            "&&" -> AstOperator.LOGIC_AND
            "&" -> AstOperator.AND
            "|" -> AstOperator.OR
            "^" -> AstOperator.XOR
            "<<" -> AstOperator.LSH
            ">>" -> AstOperator.RSH
            else -> throw Exception()
        }
    )

    override fun visitAssignExpr(node: AssignExprContext?) = AstAssignExprNode(
        node!!.ctx,
        visit(node.lhs),
        visit(node.rhs)
    )

    override fun visitCompndExpr(node: CompndExprContext?) = AstCompoundExprNode(
        node!!.ctx,
        node.rest
            ?.map { visit(it) } ?: listOf(),
        visit(node.last)
    )
}
