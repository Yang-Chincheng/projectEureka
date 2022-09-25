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

varDeclEntry: Identifer (Assign expression)?;

// class
classDecl: Class Identifer classBlock;

classBlock: '{' classMember* '}';

classMember:
	constr		# ClassConstr
	| funcDecl	# ClassMethod
	| varDecl	# ClassProper;

constr: Identifer '(' ')' stmtBlock;

// function
funcDecl: returnableType Identifer funcDeclArgs stmtBlock;

funcDeclArgs:
	'(' (args += funcDeclArgsEntry ',')* args += funcDeclArgsEntry? ')';

funcDeclArgsEntry: declarableType Identifer;

/* Type Parser Rules */
nonVoidType: (Int | String | Bool) | Identifer;

declarableType: nonVoidType | arrayType;

arrayType: nonVoidType ('[' ']')+;

newableArrayType: nonVoidType ('[' len += expression? ']')+;

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

whileLoop: While '(' condi = expression? ')' loopBlock;

/* Expression Parse Rules */
// expression: lvalueExpr # LValueExpr | rvalueExpr # RValueExpr | expression ',' last = expression # Compnd;

lvalueExpr:
	Identifer															# AtomicLExpr
	| This																# AtomicLExpr
	| arr = lvalueExpr '[' idx = expression ']'							# IndexLExpr
	| obj = lvalueExpr '.' prop = Identifer								# AccessLExpr
	| lvalueExpr funcCallArgs											# FuncInvkLExpr
	| lambdaExpr funcCallArgs											# LambdaInvkLExpr
	| op = ('++' | '--') obj = lvalueExpr								# PrefixUpdLExpr
	| <assoc = right> lexpr = lvalueExpr op = '=' rexpr = expression	# AssignLExpr
	| '(' (rest += expression ',')* last = lvalueExpr ')'				# CompndLExpr;

// indexableExpr : lvalueExpr ;

// accessibleExpr : lvalueExpr ;

// callableExpr : lvalueExpr # NormalCallableExpr | lambdaExpr # LambdaCallableExpr ;

lambdaExpr: '[' cap = '&'? ']' funcDeclArgs '->' stmtBlock;

funcCallArgs:
	'(' (args += expression ',')* args += expression? ')';

// rvalueExpr:
expression:
	StrLiteral																# LiteralExpr
	| IntLiteral															# LiteralExpr
	| Boolean																# LiteralExpr
	| Null																	# LiteralExpr
	| lvalueExpr															# LValueExpr
	| 'new' newableSyntax													# NewExpr
	| lexpr = lvalueExpr op = ('++' | '--')									# PostfixUpdExpr
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
	| '(' expr = expression ')'												# CompndExpr
	| <assoc = right> rest = expression ',' last = expression				# CompndExpr;

newableSyntax:
	newableArrayType		# NewArray
	| Identifer ('(' ')')?	# NewObject;
