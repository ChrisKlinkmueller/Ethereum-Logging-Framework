
// TYPES

grammar Types;

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