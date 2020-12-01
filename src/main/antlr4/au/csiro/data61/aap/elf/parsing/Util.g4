
// UTIL

grammar Util;

import Identifier, Fragments, Keywords, Literals, Types;

//      FILTER

/** A blockNumber is either one of the keywords CURRENT, EARLIEST, CONTINUOUS or should be an integer number
    representing a certain block of the current blockchain or a keyword. */

blockNumber
    : KEY_CURRENT
    | KEY_EARLIEST
    | KEY_CONTINUOUS
    | valueExpression
    ;


/** An addressList is parsed either to an enumeration of at least one hexadecimal BYTES_LITERAL, separated by a comma,
    the keyword ANY or a variableName. */

addressList
    : BYTES_LITERAL (',' BYTES_LITERAL)*
    | KEY_ANY
    | variableName
    ;


/** A logEntrySignature represents the structure of an existing log/event entry in the specified contract of the respective
    logEntryFilter. It starts with defining the Identifier of the log/event entry and subsequently sets an enumeration of
    at least one logEntryParameter inside of () braces. */

logEntrySignature
    : methodName=Identifier '(' (logEntryParameter (',' logEntryParameter)* )? ')'
    ;


/** A logEntryParameter corresponds to the single parameters of the log/event entry. It starts with defining the Solidity
    type of parameter, continous with the optional INDEXED specification and finishes with the parameter name. */

logEntryParameter
    : solType (KEY_INDEXED)? variableName
    ;


/** (unused) */

skippableLogEntryParameter
    : logEntryParameter
    | KEY_SKIP_INDEXED
    | KEY_SKIP_DATA
    ;


/** A smartContractQuery is parsed to a publicVariableQuery or a publicFunctionQuery. */

smartContractQuery
    : publicVariableQuery
    | publicFunctionQuery
    ;


/** A publicVariableQuery is parsed to a smartContractParameter. */

publicVariableQuery
    : smartContractParameter
    ;


/** A publicFunctionQuery consists of .... */

publicFunctionQuery
    : smartContractParameter (',' smartContractParameter)* '=' methodName=Identifier '(' (smartContractQueryParameter (',' smartContractQueryParameter)* )? ')'
    ;


/** A smartContractQuery consists of the Solidity type and the name of the parameter. */

smartContractParameter
    : solType variableName
    ;


/** A smartContractQuery consists of the Solidity type and the name of the parameter. */

smartContractQueryParameter
    : variableName
    | solType literal
    ;



//      EMIT STATEMENTS


namedEmitVariable
    : valueExpression (KEY_AS variableName)?
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

//      EXPRESSION STATEMENT

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

WS
    : [ \u000B\t\r\n]+ -> skip
    ;

COMMENT
    : '/*' .*? '*/' -> channel(HIDDEN)
    ;

LINE_COMMENT
    : '//' ~[\r\n]* -> channel(HIDDEN)
    ;