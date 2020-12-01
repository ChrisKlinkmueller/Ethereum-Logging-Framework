// based on
//    ANTLR4 grammar for SQLite by Bart Kiers (https://github.com/antlr/grammars-v4/blob/master/sqlite/SQLite.g4), and
//    ANTLR4 grammar for Java9 by Terence Parr & Sam Harwell (https://github.com/antlr/grammars-v4/blob/master/java9/Java9.g4)
//    Tutorial for expression evaluation by Sebastien Ros (https://www.codeproject.com/articles/18880/state-of-the-art-expression-evaluation)

grammar Ethql;

/** The entry parser rule defines the manifest file, a source and destination directory have to be specified.
*   It can include an arbitrary number of statements and the explicit EOF defines that the entire file is parsed. */
document
    : (connection outputFolder)? statement* EOF
    ;

// TODO: add in the above statement
//blockchainType
//    : KEY_SET KEY_BLOCKCHAIN STRING_LITERAL
//    ;

connection
    : KEY_SET (KEY_IPC)? KEY_CONNECTION literal
    ;

outputFolder
    : KEY_SET KEY_OUTPUT_FOLDER literal
    ;

/** A statement is either a scope, an expressionStatement or an emitStatement. */
statement
    : scope
    | expressionStatement
    | emitStatement
    ;

/** A scope is composed of a filter, which is applied to an arbitrary number of statements inside of {} braces. */
scope
    : filter '{' statement* '}'
    ;

/** A filter is either a blockFilter, a transactionFilter, a logEntryFilter, a genericFilter or a smartContractFilter. */
filter
    : blockFilter
    | transactionFilter
    | logEntryFilter
    | genericFilter
    | smartContractFilter
    ;

/** A blockFilter is setting a block range and filters for the qualifying blocks in the respective scope. It starts with
 *  the keyword BLOCKS and subsequently sets a starting and an ending blocknumber, each inside of () braces. */
blockFilter
    : KEY_BLOCK_RANGE '(' from=blockNumber ')' '(' to=blockNumber ')'
    ;

/** A blockNumber is either one of the keywords CURRENT, EARLIEST, CONTINUOUS or should be an integer number
 *  representing a certain block of the current blockchain or a keyword. */
blockNumber
    : KEY_CURRENT
    | KEY_EARLIEST
    | KEY_CONTINUOUS
    | valueExpression
    ;

/** A transactionFilter is taking sender and recipient addresses as input and filters for the qualifying transactions
 *  in the respective scope. It starts with the keyword TRANSACTIONS and subsequently sets a mandatory sending addressList
 *  and an optional recipients addressList. */
transactionFilter
    : KEY_TRANSACTIONS '(' (senders=addressList)? ')' '(' recipients=addressList ')'
    ;

/** An addressList is either an enumeration of at least one hexadecimal BYTES_LITERAL, separated by a comma, the keyword
 *  ANY or a variableName. */
addressList
    : BYTES_LITERAL (',' BYTES_LITERAL)*
    | KEY_ANY
    | variableName
    ;

/** A logEntryFilter is taking addresses and a specific logEntrySignature as input and filters for the qualifying logs in
 *  the respective scope. It starts with the keyword LOG ENTRIES and subsequently sets an addressList and an logEntrySignature,
 *  each inside of () braces. */
logEntryFilter
    : KEY_LOG_ENTRIES '(' addressList ')' '(' logEntrySignature ')'
    ;

/** A logEntrySignature is an enumeration of at least one logEntryParameter and filters for the qualifying logs in
 *  the respective scope. It starts with the keyword LOG ENTRIES and subsequently sets an addressList and an logEntrySignature,
 *  each inside of () braces. */
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
    : KEY_IF '(' conditionalExpression ')'
    ;

smartContractFilter
    : KEY_SMART_CONTRACT '(' contractAddress=valueExpression ')' ('(' smartContractQuery ')')+
    ;

smartContractQuery
    : publicVariableQuery
    | publicFunctionQuery
    ;

publicVariableQuery
    : smartContractParameter
    ;

publicFunctionQuery
    : smartContractParameter (',' smartContractParameter)* '=' methodName=Identifier '(' (smartContractQueryParameter (',' smartContractQueryParameter)* )? ')'
    ;

smartContractParameter
    : solType variableName
    ;

smartContractQueryParameter
    : variableName
    | solType literal
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
    : KEY_EMIT KEY_LOG_LINE '(' valueExpression (',' valueExpression)* ')' ';'
    ;

emitStatementXesTrace
    : KEY_EMIT KEY_XES_TRACE '(' (pid=valueExpression)? ')' '(' (piid=valueExpression)? ')' '(' xesEmitVariable (',' xesEmitVariable)* ')' ';'
    ;

emitStatementXesEvent
    : KEY_EMIT KEY_XES_EVENT '(' (pid=valueExpression)? ')' '(' (piid=valueExpression)? ')' '(' (eid=valueExpression)? ')' '(' xesEmitVariable (',' xesEmitVariable)* ')' ';'
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

conditionalExpression
    : conditionalOrExpression
    ;

conditionalOrExpression
    : conditionalAndExpression
    | conditionalOrExpression KEY_OR conditionalAndExpression
    ;

conditionalAndExpression
    : conditionalComparisonExpression
    | conditionalAndExpression KEY_AND conditionalComparisonExpression
    ;

conditionalComparisonExpression
    : conditionalNotExpression (comparators conditionalNotExpression)?
    ;

conditionalNotExpression
    : KEY_NOT? conditionalPrimaryExpression
    ;

conditionalPrimaryExpression 
    : valueExpression 
    | '(' conditionalOrExpression ')'
    ;

valueExpression
    : literal
    | variableName
    ;

comparators
    : '=='
    | '!='
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

KEY_BLOCK_RANGE : B L O C K S;                          // initiates a blockFilter
KEY_EARLIEST : E A R L I E S T;                         // pick the earliest block the program can access in the source file
KEY_CURRENT : C U R R E N T;                            // pick the latest block the program can access in the source file
KEY_CONTINUOUS : C O N T I N U O U S;                   // set the program to a continous mode of extraction instead of an ending block
KEY_ANY : A N Y;                                        // set no address restriction in an addressList in the transactionFilter
KEY_TRANSACTIONS : T R A N S A C T I O N S;             // initiates a transactionFilter
KEY_SMART_CONTRACT : S M A R T ' ' C O N T R A C T;
KEY_LOG_ENTRIES : L O G ' ' E N T R I E S ;             // initiates a logEntryFilter
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
KEY_SET: S E T;
KEY_BLOCKCHAIN: B L O C K C H A I N;
KEY_OUTPUT_FOLDER: O U T P U T ' ' F O L D E R;
KEY_CONNECTION: C O N N E C T I O N;
KEY_IPC: I P C;


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
    : '{' STRING_LITERAL (',' STRING_LITERAL)* '}'
    ;

intArrayLiteral
    : '{' (INT_LITERAL) (',' INT_LITERAL)* '}'
    ;

booleanArrayLiteral
    : '{' BOOLEAN_LITERAL (',' BOOLEAN_LITERAL)* '}'
    ;

bytesArrayLiteral
    : '{' BYTES_LITERAL (',' BYTES_LITERAL)* '}'
    ;

STRING_LITERAL : '"' StringCharacters? '"'; //'"' ('\\"' | ~["\r\n])* '"';

INT_LITERAL : '-'? ([0]|[1-9][0-9]*);

BOOLEAN_LITERAL 
  : T R U E
  | F A L S E
  ;

BYTES_LITERAL : '0x' [0-9a-fA-F]+; // A hexadecimal number, initiated with a '0x' and at least one hexadecimal digit



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

fragment StringCharacters
    : StringCharacter+
    ;
fragment StringCharacter
    : ~["\\\r\n]
	| EscapeSequence
    ;

fragment
EscapeSequence
	:	'\\' [btnfr"'\\]
	|	OctalEscape
    |   UnicodeEscape // This is not in the spec but prevents having to preprocess the input
	;

fragment
OctalEscape
	:	'\\' OctalDigit
	|	'\\' OctalDigit OctalDigit
	|	'\\' ZeroToThree OctalDigit OctalDigit
	;

fragment
ZeroToThree
	:	[0-3]
	;

fragment
UnicodeEscape
    :   '\\' 'u'+ HexDigit HexDigit HexDigit HexDigit
    ;

fragment
HexDigit
	:	[0-9a-fA-F]
	;

fragment
OctalDigit
	:	[0-7]
	;

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
    : '/*' .*? '*/' -> channel(HIDDEN)
    ;

LINE_COMMENT
    : '//' ~[\r\n]* -> channel(HIDDEN)
    ;