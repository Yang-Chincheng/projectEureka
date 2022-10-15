lexer grammar MxStarLexer;

channels {
	COMMENT_CHANNEL
}

WhiteSpace: [ \r\n\t]+ -> skip;
SingleLineComment: '//' ~[\r\n]* -> channel(COMMENT_CHANNEL);
MultiLineComment: '/*' .*? '*/' -> channel(COMMENT_CHANNEL);

// Reserved Keywords
Void: 'void';
Bool: 'bool';
Int: 'int';
String: 'string';
New: 'new';
Class: 'class';
Null: 'null';
True: 'ture';
False: 'false';
This: 'this';
If: 'if';
Else: 'else';
For: 'for';
While: 'while';
Break: 'break';
Continue: 'continue';
Return: 'return';

// Operators
Arrow: '->';
Inc: '++';
Dec: '--';
Plus: '+';
Minus: '-';
Mul: '*';
Div: '/';
Mod: '%';
Ge: '>=';
Le: '<=';
Eq: '==';
Ne: '!=';
Lsh: '<<';
Rsh: '>>';
Gt: '>';
Lt: '<';
LogicAnd: '&&';
LogicOr: '||';
LogicNot: '!';
BitAnd: '&';
BitOr: '|';
BitNot: '~';
BitXor: '^';
Assign: '=';
Dot: '.';
Comma: ',';
SemiColon: ';';
Colon: ':';
LParen: '(';
RParen: ')';
LBracket: '[';
RBracket: ']';
LBrace: '{';
RBrace: '}';

// Identifers
fragment Chars: [a-zA-Z];
fragment UpperCaseChars: [A-Z];
fragment LowerCaseChars: [a-z];
fragment NonzereDigits: [1-9];
fragment Digits: [0-9];

fragment IdentifierHead: [a-zA-Z];
fragment IdentifierBody: [a-zA-Z0-9_];
Identifier: IdentifierHead IdentifierBody*;
// CammelCaseIdentier: (UpperCaseCharacters LowerCaseCharacters*)+; MixCammelCaseIdentifer:
// LowerCaseCharacters+ (UpperCaseCharacters LowerCaseCharacters*)*;

// Literals
fragment Sign: [-+];
IntLiteral: '0' | [1-9] [0-9]*;

fragment CharLiteral: '\\' . | ~["\\];
StrLiteral: '"' CharLiteral* '"';

Boolean: True | False;

