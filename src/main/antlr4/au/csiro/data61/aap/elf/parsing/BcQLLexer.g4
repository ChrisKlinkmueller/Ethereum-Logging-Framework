
lexer grammar BcQLLexer;

//      KEYWORDS: Used to set certain grammar specifications and provide easily legible grammar vocabular.
//      Are defined in single big letters to make them case insensitive (compare CASE FRAGMENTS).

KEY_SET: S E T;                                         // helper keyword for settings
KEY_BLOCKCHAIN: B L O C K C H A I N;                    // initiates the blockchain parameter setting
KEY_OUTPUT_FOLDER: O U T P U T ' ' F O L D E R;         // initiates the output parameter setting
KEY_CONNECTION: C O N N E C T I O N;                    // initiates the connection setting
KEY_IPC: I P C;                                         // initiates the inter process communication setting

KEY_BLOCK_RANGE : B L O C K S;                          // initiates a blockFilter
KEY_TRANSACTIONS : T R A N S A C T I O N S;             // initiates a transactionFilter
KEY_SMART_CONTRACT : S M A R T ' ' C O N T R A C T;     // initiates a smartContractFilter
KEY_LOG_ENTRIES : L O G ' ' E N T R I E S ;             // initiates a logEntryFilter
KEY_IF : I F;                                           // initiates a smartContractFilter

KEY_EMIT: E M I T;                                      // initiates an emit statements
KEY_CSV_ROW: C S V ' ' R O W;                           // csv output specification for the emit statement
KEY_LOG_LINE: L O G ' ' L I N E;                        // log output specification for the emit statement
KEY_XES_EVENT: X E S ' ' E V E N T;                     // xes event output specification for the emit statement
KEY_XES_TRACE: X E S ' ' T R A C E;                     // xes trace output specification for the emit statement

KEY_EARLIEST : E A R L I E S T;                         // pick the earliest block the program can access in the source file
KEY_CURRENT : C U R R E N T;                            // pick the latest block the program can access in the source file
KEY_CONTINUOUS : C O N T I N U O U S;                   // set the program to a continous mode of extraction instead of an ending block
KEY_ANY : A N Y;                                        // set no address restriction in an addressList in the transactionFilter
KEY_INDEXED : 'indexed';                                // corresponds to the 'indexed' addition for indexed parameters in log entries

KEY_NOT: '!';                                           // logical NOT operater can be expressed as ! or NOT
KEY_AND: '&&';                                          // logical AND operater can be expressed as && or AND
KEY_OR: '||';                                           // logical OR operater can be expressed as || or OR
KEY_IN: I N;                                            // comparator addition for expression statements
KEY_AS: A S;                                            // helper keyword to assign valueExpressions to variableNames in emit statements

KEY_SKIP_INDEXED : '_indexed_';                         // unused
KEY_SKIP_DATA : '_';                                    // unused

//      CASE FRAGMENTS: Provide case insensitivite letters

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


BOOLEAN_LITERAL
  : T R U E
  | F A L S E
  ;

STRING_LITERAL : '"' StringCharacters? '"';             // A string literal needs to be surrounded by "" and may include StringCharacters

INT_LITERAL : '-'? ([0]|[1-9][0-9]*);                   //

BYTES_LITERAL : '0x' [0-9a-fA-F]+;                      // A hexadecimal number, initiated with a '0x' and at least one hexadecimal digit

SOL_BYTE_TYPE : 'byte' ('s' (SOL_BYTES_LENGTH)?)?;      //

SOL_INT_TYPE: (SOL_UNSIGNED)? 'int' SOL_NUMBER_LENGTH?; //

SOL_ADDRESS_TYPE: 'address';                            //

SOL_BOOL_TYPE: 'bool';                                  //

SOL_BYTES_LENGTH : [1-9]|[1-2][0-9]|[3][0-2];           //

SOL_UNSIGNED : 'u';                                     //

//
SOL_NUMBER_LENGTH : '8'|'16'|'24'|'32'|'40'|'48'|'56'|'64'|'72'|'80'|'88'|'96'|'104'|'112'|'120'|'128'|'136'|'144'|'152'|'160'|'168'|'176'|'184'|'192'|'200'|'208'|'216'|'224'|'232'|'240'|'248'|'256';

SOL_FIXED_N : [1-7]?[0-9]|[8][0-1];                     //

SOL_STRING_TYPE: 'string';                              //

WS : [ \u000B\t\r\n]+ -> skip;                          // grammar recognizes empty lines and skip them

COMMENT: '/*' .*? '*/' -> channel(HIDDEN);              // for arbitrary comment lenght use /* as start and */ as end

LINE_COMMENT: '//' ~[\r\n]* -> channel(HIDDEN);         // for a line comment use //

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


