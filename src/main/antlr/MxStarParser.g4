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
	type = declarableType (entries += variableListEntry ',')* entries += variableListEntry;

variableListEntry: Identifier ('=' expression)?;

// class
classDecl: Class Identifier classBlock;


classBlock: '{' classMember* '}';

classMember:
	Identifier '(' ')' stmtBlock	# ClassConstr
	| funcDecl						# ClassMethod
	| varDecl ';'					# ClassProper;

// function
funcDecl: returnableType Identifier parameterList stmtBlock;

parameterList:
	'(' (entrys += parameterListEntry ',')* entrys += parameterListEntry? ')';

parameterListEntry: declarableType Identifier;

/* Type Parser Rules */
nonVoidType: Int | String | Bool | Identifier;

arrayType: nonVoidType (total += '[' ']')+;

declarableType: nonVoidType | arrayType;

returnableType: Void | nonVoidType | arrayType;

/* Statement Parse Rules */
stmtBlock: '{' statement* '}';

statement:
	expression ';'	# ExprStmt
	| varDecl ';'	# DeclStmt
	| If '(' condi = expression ')' thenblk = statement (Else elseblk = statement)?	         # BranchStmt
	| For '(' init = forLoopInit? ';' condi = expression? ';' iter = expression? ')' statement	 # ForLoopStmt
	| While '(' condi = expression ')' statement	# WhileLoopStmt
	| ctrl = (Continue | Break) ';'					# CtrlStmt
	| Return ret = expression? ';'					# ReturnStmt
	| stmtBlock										# BlockStmt
	| ';'											# EmptyStmt;

forLoopInit: varDecl | expression;

/* Expression Parse Rules */
literal: This | Null | True | False | IntLiteral | StrLiteral;

expression:
	literal					# LiteralExpr
	| var = Identifier  # AtomicVarExpr
	| arr = expression '[' idx = expression ']'								# IndexExpr
	| obj = expression '.' (Identifier | '(' Identifier ')') # MemberVarExpr
	| func = Identifier call = argumentList # AtomicFuncInvkExpr
	| obj = expression '.' func = Identifier call = argumentList # MemberFuncInvkExpr
	| '[' cap = '&'? ']' parameterList '->' stmtBlock argumentList								# LambdaInvkExpr
	| 'new' newableSyntax													# NewExpr
	| op = ('++' | '--') obj = expression									# PreUpdExpr
	| obj = expression op = ('++' | '--')									# PostUpdExpr
	| <assoc = right> op = ('!' | '~' | '+' | '-') rhs = expression		# UnaryExpr
	| lhs = expression op = ('*' | '/' | '%') rhs = expression			# BinaryExpr
	| lhs = expression op = ('+' | '-') rhs = expression				# BinaryExpr
	| lhs = expression op = ('<<' | '>>') rhs = expression				# BinaryExpr
	| lhs = expression op = ('<' | '<=' | '>' | '>=') rhs = expression	# BinaryExpr
	| lhs = expression op = ('==' | '!=') rhs = expression				# BinaryExpr
	| lhs = expression op = '&' rhs = expression						# BinaryExpr
	| lhs = expression op = '^' rhs = expression						# BinaryExpr
	| lhs = expression op = '|' rhs = expression						# BinaryExpr
	| lhs = expression op = '&&' rhs = expression						# BinaryExpr
	| lhs = expression op = '||' rhs = expression						# BinaryExpr
	| <assoc = right> lhs = expression op = '=' rhs = expression		# AssignExpr
	| '(' (rest += expression ',')* last = expression ')'					# CompndExpr;

newableSyntax:
    nonVoidType arrayDim+ # NewArray
	| nonVoidType ('(' ')')?						# NewObject;

arrayDim: '[' expression? ']';

argumentList:
	'(' (args += expression ',')* args += expression? ')';
