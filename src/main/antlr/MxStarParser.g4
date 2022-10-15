parser grammar MxStarParser;

options {
	tokenVocab = MxStarLexer;
}

program: progDecl* EOF;

/* Declaration Parse Rules */
progDecl:
	varDecl ';'		# ProgVar
	| classDecl ';'	# ProgClass
	| funcDecl		# ProgFunc;

// variable
varDecl:
	declarableType (vars += varDeclEntry ',')* vars += varDeclEntry;

varDeclEntry: Identifier ('=' expression)?;

// class
classDecl: Class Identifier classBlock;

classBlock: '{' classMember* '}';

classMember:
	constr		    # ClassConstr
	| funcDecl	    # ClassMethod
	| varDecl ';'	# ClassProper;

constr: Identifier '(' ')' stmtBlock;

// function
funcDecl: returnableType Identifier funcDeclParas stmtBlock;

funcDeclParas:
	'(' (args += funcDeclParasEntry ',')* args += funcDeclParasEntry? ')';

funcDeclParasEntry: declarableType Identifier;

/* Type Parser Rules */
nonVoidType: (Int | String | Bool) | Identifier;

declarableType: nonVoidType | arrayType;

arrayType: nonVoidType ('[' ']')+;

newableArrayType: nonVoidType ('[' lens += expression? ']')+;

returnableType: Void | nonVoidType | arrayType;

/* Statement Parse Rules */
stmtBlock: '{' statement* '}';

statement:
	expression ';'					# ExprStmt
	| varDecl ';'					# DeclStmt
	| branch						# BranchStmt
	| loop							# LoopStmt
	| ctrl = (Continue | Break) ';'	# CtrlStmt
	| Return ret = expression? ';'	# ReturnStmt
	| stmtBlock						# BlockStmt
	| ';'							# EmptyStmt;

branchBlock: stmtBlock | statement;

branch:
	If '(' condi = expression ')' ifblk = branchBlock Else elseblk = branchBlock	# IfElseStmt
	| If '(' condi = expression ')' ifblk = branchBlock								# IfStmt;

loopBlock: stmtBlock | statement;

loop: forLoop # ForLoopStmt | whileLoop # WhlieLoopStmt;

forInit: varDecl | expression;

forLoop:
	For '(' init = forInit? ';' condi = expression? ';' iter = expression? ')' loopBlock;

whileLoop: While '(' condi = expression ')' loopBlock;


/* Expression Parse Rules */
literalExpr:
    This
    | Null
    | Boolean
    | IntLiteral
    | StrLiteral;

atomicExpr:
    Identifier
    | literalExpr;

expression:
	atomicExpr															# LValueExpr
	| arr = expression '[' idx = expression ']'							# IndexLExpr
    | obj = expression '.' prop = Identifier								# AccessLExpr
    | callee = expression funcCallArgs									# FuncInvkLExpr
    | callee = lambdaExpr funcCallArgs									# LambdaInvkLExpr
	| 'new' newableSyntax													    # NewExpr
    | op = ('++' | '--') obj = expression								# PrefixUpdLExpr
	| obj = expression op = ('++' | '--')									# PostfixUpdExpr
	| <assoc = right> op = ('!' | '~' | '+' | '-') rexpr = expression		# UnaryExpr
	| lexpr = expression op = ('*' | '/' | '%') rexpr = expression			# BinaryExpr
	| lexpr = expression op = ('+' | '-') rexpr = expression				# BinaryExpr
	| lexpr = expression op = ('<<' | '>>') rexpr = expression				# BinaryExpr
	| lexpr = expression op = ('<' | '<=' | '>' | '>=') rexpr = expression	# BinaryExpr
	| lexpr = expression op = ('==' | '!=') rexpr = expression				# BinaryExpr
	| lexpr = expression op = '&' rexpr = expression						# BinaryExpr
	| lexpr = expression op = '^' rexpr = expression						# BinaryExpr
	| lexpr = expression op = '|' rexpr = expression						# BinaryExpr
	| lexpr = expression op = '&&' rexpr = expression						# BinaryExpr
	| lexpr = expression op = '||' rexpr = expression						# BinaryExpr
    | <assoc = right> lexpr = expression op = '=' rexpr = expression	# AssignLExpr
	| '(' (rest += expression ',')* last = expression ')'                   # CompndExpr;

newableSyntax:
	newableArrayType		# NewArray
	| Identifier ('(' ')')?	# NewObject;

lambdaExpr: '[' cap = '&'? ']' funcDeclParas '->' stmtBlock;

funcCallArgs:
	'(' (args += expression ',')* args += expression? ')';
