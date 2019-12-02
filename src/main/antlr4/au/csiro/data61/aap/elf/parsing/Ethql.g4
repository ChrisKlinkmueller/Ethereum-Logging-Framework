// based on the grammars for 
//    SQLite by Bart Kiers (https://github.com/antlr/grammars-v4/blob/master/sqlite/SQLite.g4), and
//    Java9 by Terence Parr & Sam Harwell (https://github.com/antlr/grammars-v4/blob/master/java9/Java9.g4)

grammar Ethql;

document
    : statement* EOF
    ;

statement
    : scope
    | expressionStatement
    | emitStatement
    ;

scope 
    : filter '{' statement* '}'
    ;

filter
    : blockFilter
    | transactionFilter
    | logEntryFilter
    | genericFilter
    ;

blockFilter
    : KEY_BLOCK_RANGE '(' from=blockNumber ')' '(' to=blockNumber ')'
    ;

blockNumber
    : KEY_CURRENT
    | KEY_EARLIEST
    | KEY_CONTINUOUS
    | valueExpression
    ;

transactionFilter
    : KEY_TRANSACTIONS '(' (senders=addressList)? ')' '(' recipients=addressList ')'
    ;

addressList
    : BYTES_LITERAL (',' BYTES_LITERAL)*
    | KEY_ANY
    | variableName
    ;

logEntryFilter
    : KEY_LOG_ENTRIES '(' addressList ')' '(' logEntrySignature ')'
    ;

logEntrySignature
    : methodName=Identifier '(' (logEntryParameter (',' logEntryParameter)* )? ')'
    ;

logEntryParameter
    : solType (KEY_INDEXED)? variableName
    ;

skippableLogEntryParameter
    : logEntryParameter
    | KEY_SKIP_INDEXED
    | KEY_SKIP_DATA
    ;

genericFilter
    : KEY_IF '(' booleanExpression ')'
    ;



// emitStatements

emitStatement
    : emitStatementCsv
    | emitStatementLog
    | emitStatementXesEvent
    | emitStatementXesTrace
    ;

emitStatementCsv
    : KEY_EMIT KEY_CSV_ROW '(' tableName=valueExpression ')' '(' namedEmitVariable (',' namedEmitVariable)* ')'	';'
    ;

namedEmitVariable
    : valueExpression (KEY_AS variableName)?
    ;

emitStatementLog
    : KEY_EMIT KEY_LOG_LINE '(' valueExpression+ ')' ';'
    ;

emitStatementXesTrace
    : KEY_EMIT KEY_XES_TRACE '(' (pid=valueExpression)? ')' '(' (piid=valueExpression)?')' '(' xesEmitVariable+ ')' ';'
    ;

emitStatementXesEvent
    : KEY_EMIT KEY_XES_EVENT '(' (pid=valueExpression)? ')' '(' (piid=valueExpression)?')' '(' (eid=valueExpression)? ')' '(' xesEmitVariable+ ')' ';'
    ;

xesEmitVariable
    : valueExpression (KEY_AS (xesTypes)? variableName)?
    ;

xesTypes
    : 'xs:string'
    | 'xs:date'
    | 'xs:int'
    | 'xs:float'
    | 'xs:boolean'
    ;



// expressionStatements

expressionStatement 
    : methodStatement
    | variableDeclarationStatement
    | variableAssignmentStatement
    ; 

variableDeclarationStatement
    : solType variableName '=' statementExpression ';' 
    ; 

variableAssignmentStatement
    : variableName '=' statementExpression ';' 
    ;

statementExpression
    : valueExpression
    | methodInvocation
    ;

booleanExpression
    : notExpression
    | KEY_NOT notExpression
    ;

notExpression
    : orExpression
    | '(' orExpression ')'
    ;

orExpression
    : andExpression
    | orExpression KEY_OR andExpression
    ;

andExpression
    : comparisonExpression
    | andExpression KEY_AND comparisonExpression
    ;

comparisonExpression
    : value=valueExpression
    | leftHandSide=valueExpression comparators rightHandSide=valueExpression
    ;

valueExpression
    : literal
    | variableName
    ;

comparators
    : '=='
    | '>='
    | '>'
    | '<'
    | '<='
    | KEY_IN
    ;

methodStatement
    : methodInvocation ';'
    ;

methodInvocation
    : methodName=Identifier '(' (valueExpression (',' valueExpression)* )? ')'
    ;

variableName
    : Identifier
    | Identifier ':' Identifier
    | Identifier '.' Identifier
    ;



// Keywords

KEY_BLOCK_RANGE : B L O C K S;
KEY_EARLIEST : E A R L I E S T;
KEY_CURRENT : C U R R E N T;
KEY_CONTINUOUS : C O N T I N U O U S;
KEY_ANY : A N Y;
KEY_TRANSACTIONS : T R A N S A C T I O N S;
KEY_SMART_CONTRACT : S M A R T ' ' C O N T R A C T;
KEY_LOG_ENTRIES : L O G ' ' E N T R I E S ;
KEY_INDEXED : 'indexed';
KEY_SKIP_INDEXED : '_indexed_';
KEY_SKIP_DATA : '_';
KEY_IF : I F;
KEY_NOT: '!';
KEY_AND: '||';
KEY_OR: '&&';
KEY_IN: I N;
KEY_AS: A S;
KEY_EMIT: E M I T;
KEY_CSV_ROW: C S V ' ' R O W;
KEY_LOG_LINE: L O G ' ' L I N E;
KEY_XES_EVENT: X E S ' ' E V E N T;
KEY_XES_TRACE: X E S ' ' T R A C E;



// Literals

literal 
    : STRING_LITERAL
    | BOOLEAN_LITERAL
    | BYTES_LITERAL
    | INT_LITERAL
    | arrayLiteral
    ;

arrayLiteral
    : stringArrayLiteral
    | intArrayLiteral
    | booleanArrayLiteral
    | bytesArrayLiteral
    ;

stringArrayLiteral
    : '[' (STRING_LITERAL (',' STRING_LITERAL)*)? ']'
    ;

intArrayLiteral
    : '[' ((INT_LITERAL) (',' INT_LITERAL)*)? ']'
    ;

booleanArrayLiteral
    : '[' (BOOLEAN_LITERAL (',' BOOLEAN_LITERAL)*)? ']'
    ;

bytesArrayLiteral
    : '[' (BYTES_LITERAL (',' BYTES_LITERAL)*)? ']'
    ;

STRING_LITERAL : '"' ('\\"' | ~["\r\n])* '"';

INT_LITERAL : '-'? ([0]|[1-9][0-9]+);

BOOLEAN_LITERAL 
  : T R U E
  | F A L S E
  ;

BYTES_LITERAL : '0x' [0-9a-fA-F]+;



// TYPES

solTypeRule : solType EOF;

solType 
    : SOL_ADDRESS_TYPE
    | SOL_BOOL_TYPE
    | SOL_BYTE_TYPE
    | SOL_INT_TYPE
    | SOL_STRING_TYPE
    | solType '[' ']'
    ;

SOL_BYTE_TYPE 
    : 'byte' ('s' (SOL_BYTES_LENGTH)?)?
    ;

SOL_INT_TYPE
    : (SOL_UNSIGNED)? 'int' SOL_NUMBER_LENGTH?
    ;

SOL_ADDRESS_TYPE
    : 'address'
    ;

SOL_BOOL_TYPE
    : 'bool'
    ; 

SOL_BYTES_LENGTH
    : [1-9]|[1-2][0-9]|[3][0-2]
    ;

SOL_UNSIGNED 
    : 'u'
    ;

SOL_NUMBER_LENGTH
    : '8'|'16'|'24'|'32'|'40'|'48'|'56'|'64'|'72'|'80'|'88'|'96'|'104'|'112'|'120'|'128'|'136'|'144'|'152'|'160'|'168'|'176'|'184'|'192'|'200'|'208'|'216'|'224'|'232'|'240'|'248'|'256'
    ;

SOL_FIXED_N
    : [1-7]?[0-9]|[8][0-1]
    ;

SOL_STRING_TYPE
    : 'string'
    ;



// FRAGMENTS

fragment A : [aA];
fragment B : [bB];
fragment C : [cC];
fragment D : [dD];
fragment E : [eE];
fragment F : [fF];
fragment G : [gG];
fragment H : [hH];
fragment I : [iI];
fragment J : [jJ];
fragment K : [kK];
fragment L : [lL];
fragment M : [mM];
fragment N : [nN];
fragment O : [oO];
fragment P : [pP];
fragment Q : [qQ];
fragment R : [rR];
fragment S : [sS];
fragment T : [tT];
fragment U : [uU];
fragment V : [vV];
fragment W : [wW];
fragment X : [xX];
fragment Y : [yY];
fragment Z : [zZ];



// Identifier


Identifier
	:	Letter LetterOrDigit*
    | '_' LetterOrDigit+
	;

fragment Letter
	:	[a-zA-Z] // these are the "java letters" below 0x7F
	|	// covers all characters above 0x7F which are not a surrogate
		~[\u0000-\u007F\uD800-\uDBFF]
		{Character.isJavaIdentifierStart(_input.LA(-1))}?
	|	// covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
		[\uD800-\uDBFF] [\uDC00-\uDFFF]
		{Character.isJavaIdentifierStart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
	;

fragment LetterOrDigit
	:	[a-zA-Z0-9$_] // these are the "java letters or digits" below 0x7F
	|	// covers all characters above 0x7F which are not a surrogate
		~[\u0000-\u007F\uD800-\uDBFF]
		{Character.isJavaIdentifierPart(_input.LA(-1))}?
	|	// covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
		[\uD800-\uDBFF] [\uDC00-\uDFFF]
		{Character.isJavaIdentifierPart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
	;

WS 
    : [ \u000B\t\r\n]+ -> skip
    ;

COMMENT
    : '/*' .*? '*/' -> skip
    ;

LINE_COMMENT
    : '//' ~[\r\n]* -> skip
    ;