// (outdated) based on
//    ANTLR4 grammar for SQLite by Bart Kiers (https://github.com/antlr/grammars-v4/blob/master/sqlite/SQLite.g4), and
//    ANTLR4 grammar for Java9 by Terence Parr & Sam Harwell (https://github.com/antlr/grammars-v4/blob/master/java9/Java9.g4)
//    Tutorial for expression evaluation by Sebastien Ros (https://www.codeproject.com/articles/18880/state-of-the-art-expression-evaluation)
//
// 01/12/2020 helpful links
//    ANTLR4 documentation (https://github.com/antlr/antlr4/blob/4.9/doc/index.md)
//    Tutorial https://docs.google.com/document/d/1gQ2lsidvN2cDUUsHEkT05L-wGbX5mROB7d70Aaj3R64/edit#heading=h.xr0jj8vcdsgc
//

grammar BcqlUtil;

import BcqlLexemes;

//      FILTER UTIL

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
    // This is needed to be used in Hyperledger
    | STRING_LITERAL (',' STRING_LITERAL)*
    ;


/** A logEntrySignature represents the structure of an existing log/event entry in the specified contract of the respective
    logEntryFilter. Starts with defining the Identifier of the log/event entry and subsequently sets an enumeration of
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


/** A smartContractQuery is parsed to a publicVariableQuery or a publicFunctionQuery. With the publicVariableQuery
 *  a variable on the respective Blockchain contract is queried and with a publicFunctionQuery a function on the
 *  respective Blockchain contract is queried. */

smartContractQuery
    : publicVariableQuery
    | publicFunctionQuery
    ;


/** A publicVariableQuery is parsed to a single smartContractParameter, representing a queried variable. */

publicVariableQuery
    : smartContractParameter
    ;


/** A publicFunctionQuery consists of
 *  - at least one smartContractParameter (devided by ','), defining the output parameters of the respective function
 *  - the methodName (stated after an '='), which represents the function name the user wants query on the contract
 *  - at least one smartContractQueryParameter (inside () braces and devided by ','), the corresponding input parameters
 *  of the respective function. In contrast to the smartContractParameter, this one can also be parsed into an variableName.
 */

publicFunctionQuery
    : smartContractParameter (',' smartContractParameter)* '=' methodName=Identifier '(' (smartContractQueryParameter (',' smartContractQueryParameter)* )? ')'
    ;


/** A smartContractParameter consists of a parameter type (solType) and name (variableName). In contrast to the
 *  smartContractQueryParameter the name of the parameter is declared and saved in a variable, as it the output parameter
 *  which is queried for and therefore made usable inside the scope of the smartContractFilter.
 */

smartContractParameter
    : solType variableName
    ;


/** A smartContractQueryParameter consists of a parameter type (solType) and name (literal). Alternativly a variableName
 *  can be used, in which both parameter type and name has to be previously stored.
 */

smartContractQueryParameter
    : variableName
    | solType literal
    ;




//      EMIT STATEMENTS UTIL

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


//      EXPRESSION STATEMENTS: Add additional functionality to the grammar

expressionStatement
    : methodStatement
    | variableDeclarationStatement
    | variableAssignmentStatement
    ;

methodStatement
    : methodInvocation ';'
    ;

methodInvocation
    : methodName=Identifier '(' (valueExpression (',' valueExpression)* )? ')'
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

variableName
    : Identifier
    | Identifier ':' Identifier
    | Identifier '.' Identifier
    ;



//      LITERALS

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

//      TYPES

solTypeRule : solType EOF;

solType
    : SOL_ADDRESS_TYPE
    | SOL_BOOL_TYPE
    | SOL_BYTE_TYPE
    | SOL_INT_TYPE
    | SOL_STRING_TYPE
    | solType '[' ']'
    ;
