
// LITERALS

grammar Literals;
import Fragments;

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