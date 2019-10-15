// based on the grammars for 
//    SQLite by Bart Kiers (https://github.com/antlr/grammars-v4/blob/master/sqlite/SQLite.g4), and
//    Java8 by Terence Parr & Sam Harwell (https://github.com/antlr/grammars-v4/blob/master/java8/Java8.g4)

grammar Xbel;

document 
    : blockBody EOF
    ;

blockStartRule
    : block EOF
    ;

block 
    : blockHead '{' blockBody '}'
    ;

// block header

blockHead
    : blocksRange
    | transactionsRange
    | smartContractsRange
    | logEntriesRange
    ;

blocksRange
    : KEY_BLOCK_RANGE '(' from=blockRangeNumber ',' to=blockRangeNumber ')'
    ;

blockRangeNumber
    : INT_VALUE
    | KEY_CURRENT
    | KEY_EARLIEST
    | KEY_PENDING
    | variableName
    | methodCall
    ;

transactionsRange
    : KEY_TRANSACTIONS '(' senders=addressList ')' '(' recipients=addressList ')'
    ;

smartContractsRange
    : KEY_SMART_CONTRACTS '(' contracts=addressList ')'
    ;

addressList
    : BYTE_AND_ADDRESS_VALUE (',' BYTE_AND_ADDRESS_VALUE)*
    | KEY_ANY
    | variableName
    | methodCall
    ;

logEntriesRange
    : KEY_LOG_ENTRIES '(' eventSignatureSpecification ')'
    | KEY_LOG_ENTRIES '(' varArgsSpecification ')'
    ;

eventSignatureSpecification
    : methodName=Identifier '(' (solVariable (',' solVariable)* )? ')' KEY_ANONYMOUS?
    ;

varArgsSpecification
    : (solSkipVariable (',' solSkipVariable)* )? (',' KEY_VAR_ARGS)?
    ;

solVariable
    : solType (KEY_INDEXED)? variableName
    ;

solSkipVariable
    : solVariable
    | KEY_SKIP_INDEXED
    | KEY_SKIP_DATA
    ;

boolExpr
    : variableName
    | methodCall
    | arrayValue
    | BOOLEAN_VALUE
    | BYTE_AND_ADDRESS_VALUE
    | FIXED_VALUE
    | INT_VALUE
    | STRING_VALUE
    | variableName KEY_IN '[' INT_VALUE ',' INT_VALUE ']'
    | variableName KEY_IN '[' FIXED_VALUE ',' FIXED_VALUE ']'
    | variableName KEY_IN arrayValue
    | '(' boolExpr ')'
    | KEY_NOT boolExpr 
    | boolExpr KEY_AND boolExpr 
    | boolExpr KEY_OR boolExpr 
    | boolExpr '==' boolExpr 
    | boolExpr '!=' boolExpr 
    | boolExpr '<=' boolExpr 
    | boolExpr '<' boolExpr 
    | boolExpr '>=' boolExpr 
    | boolExpr '>' boolExpr
    ;


// BLOCK BODY

blockBody
    : blockBodyElements*
    ;

blockBodyElements
    : block
    | statement ';'
    | emitBlock
    ;

statement
    : (leftStatementSide '=')? rightStatementSide 
    ;

leftStatementSide
    : variableDefinition
    | variableName
    ;

variableDefinition
    : solType variableName
    ;

variableDefinitionStartRule
    : variableDefinition EOF
    ;

rightStatementSide
    : variableName
    | methodCall
    | value
    ;

emitBlock
    : emitHead '{' emitCall* '}'
    ;

emitHead
    : KEY_EMIT emitCondition?
    ;

emitCondition
    : KEY_IF '(' boolExpr ')'
    ;

emitCall
    : type=(KEY_EVENT|KEY_TRACE) '(' emitVariable (',' emitVariable )* ')' ';'
    ;

emitVariable
    : variableName (KEY_AS xesVariable)?
    | BOOLEAN_VALUE KEY_AS (xesType)? variableName
    | BYTE_AND_ADDRESS_VALUE (xesType)? variableName
    | STRING_VALUE KEY_AS (xesType)? variableName
    | FIXED_VALUE KEY_AS (xesType)? variableName
    | INT_VALUE KEY_AS (xesType)? variableName
    | arrayValue KEY_AS (xesType)? variableName
    ;

xesVariable
    : xesType variableName
    | xesType
    | variableName
    ;

xesType
    : KEY_XBOOLEAN
    | KEY_XDATE
    | KEY_XFLOAT
    | KEY_XID
    | KEY_XINT
    | KEY_XSTRING
    ;

// KEY WORD SECTION

KEY_BLOCK_RANGE : B L O C K ' ' R A N G E;
KEY_EARLIEST : E A R L I E S T;
KEY_CURRENT : C U R R E N T;
KEY_PENDING : P E N D I N G;
KEY_ANY : A N Y;
KEY_TRANSACTIONS : T R A N S A C T I O N S;
KEY_SMART_CONTRACTS : S M A R T ' ' C O N T R A C T S;
KEY_LOG_ENTRIES : L O G ' ' E N T R I E S ;
KEY_ANONYMOUS : 'anonymous';
KEY_VAR_ARGS : '...';
KEY_INDEXED : 'indexed';
KEY_SKIP_INDEXED : '_indexed_';
KEY_SKIP_DATA : '_';
KEY_EMIT : E M I T;
KEY_IF : I F;
KEY_IN : I N;
KEY_NOT : N O T;
KEY_AND : A N D;
KEY_OR : O R;
KEY_EVENT : E V E N T;
KEY_TRACE : T R A C E;
KEY_AS : A S;
KEY_XDATE : X D A T E;
KEY_XINT : X I N T;
KEY_XSTRING : X S T R I N G;
KEY_XFLOAT : X F L O A T;
KEY_XID : X I D;
KEY_XBOOLEAN : X B O O L E A N;

// GENERAL VARIABLE DEFINITIONS

variableName
    : Identifier
    | Identifier ':' Identifier
    | Identifier '.' Identifier
    ; 

methodCall
    : methodName=Identifier '(' (methodParameter (',' methodParameter)* )? ')'
    ;

methodParameter
    : variableName
    | STRING_VALUE
    | BOOLEAN_VALUE
    | BYTE_AND_ADDRESS_VALUE
    | FIXED_VALUE
    | INT_VALUE
    | arrayValue
    ;

KEY_CONFIGURATION : C O N F I G U R A T I O N;
KEY_EXPORT_TO : E X P O R T ' ' T O;
KEY_XES : X E S;
KEY_XES_EXTENSION : X E S E X T E N S I O N;
KEY_XES_GLOBAL : X E S G L O B A L;
KEY_XES_CLASSIFIER : X E S C L A S S I F I E R;
KEY_CSV : C S V;
KEY_EXCEPTION_HANDLING : E X C E P T I O N ' ' H A N D L I N G;
KEY_EXCEPTION_IGNORE : I G N O R E;
KEY_EXCEPTION_DETERMINE : D E T E R M I N E;
KEY_EXCEPTION_PRINT : P R I N T;
KEY_NULL : N U L L;






// TYPES AND VALUES

value
    : STRING_VALUE
    | FIXED_VALUE
    | INT_VALUE
    | BOOLEAN_VALUE
    | BYTE_AND_ADDRESS_VALUE
    ;

arrayValue
    : stringArrayValue
    | intArrayValue
    | booleanArrayValue
    | fixedArrayValue
    | byteAndAddressArrayValue
    ;

stringArrayValue
    : '{' (STRING_VALUE (',' STRING_VALUE)*)? '}'
    ;

intArrayValue
    : '{' ((INT_VALUE|FIXED_VALUE) (',' (INT_VALUE|FIXED_VALUE))*)? '}'
    ;

fixedArrayValue
    : '{' (FIXED_VALUE (',' FIXED_VALUE)*)? '}'
    ;

booleanArrayValue
    : '{' (BOOLEAN_VALUE (',' BOOLEAN_VALUE)*)? '}'
    ;

byteAndAddressArrayValue
    : '{' (BYTE_AND_ADDRESS_VALUE (',' BYTE_AND_ADDRESS_VALUE)*)? '}'
    ;

STRING_VALUE : '"' ('\\"' | ~["\r\n])* '"';

FIXED_VALUE : [0-9]* '.' [0-9]+ ;

INT_VALUE : [0-9]+;

BOOLEAN_VALUE 
  : T R U E
  | F A L S E
  ;

BYTE_AND_ADDRESS_VALUE : ('0x')? [0-9a-fA-F]+;



solTypeStartRule
    : solType EOF
    ;
    
solType 
    :
    | SOL_ADDRESS_TYPE
    | SOL_ADDRESS_ARRAY_TYPE
    | SOL_BOOL_ARRAY_TYPE
    | SOL_BOOL_TYPE
    | SOL_BYTE_ARRAY_TYPE
    | SOL_BYTE_TYPE
    | SOL_FIXED_ARRAY_TYPE
    | SOL_FIXED_TYPE
    | SOL_INT_ARRAY_TYPE
    | SOL_INT_TYPE
    | SOL_STRING_TYPE
    ;

SOL_FIXED_ARRAY_TYPE
    : SOL_FIXED_TYPE '[' ']'
    ;

SOL_FIXED_TYPE
    : (SOL_UNSIGNED)? 'fixed'(SOL_NUMBER_LENGTH'x'SOL_FIXED_N)?
    ;

SOL_BYTE_ARRAY_TYPE
    : SOL_BYTE_TYPE '[' ']'
    ;

SOL_BYTE_TYPE 
    : 'byte' ('s' SOL_BYTES_LENGTH)?
    | 'bytes'
    ;

SOL_INT_ARRAY_TYPE
    : SOL_INT_TYPE '[' ']'
    ;

SOL_INT_TYPE
    : (SOL_UNSIGNED)? 'int' SOL_NUMBER_LENGTH?
    ;

SOL_ADDRESS_ARRAY_TYPE 
    : SOL_ADDRESS_TYPE '[' ']'
    ;

SOL_ADDRESS_TYPE
    : 'address'
    ;

SOL_BOOL_ARRAY_TYPE
    : SOL_BOOL_TYPE '[' ']'
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

WS : [ \u000B\t\r\n]+ -> skip;

COMMENT
    :   '/*' .*? '*/' -> skip
    ;

LINE_COMMENT
    :   '//' ~[\r\n]* -> skip
    ;