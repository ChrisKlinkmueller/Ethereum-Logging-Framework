grammar EthqlEth;

import EthqlCore;

statement 
    : query EOF
    ;

query
    : blockFilter
    | transactionFilter
    | logEntryFilter 
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