// based on
//    ANTLR4 grammar for SQLite by Bart Kiers (https://github.com/antlr/grammars-v4/blob/master/sqlite/SQLite.g4), and
//    ANTLR4 grammar for Java9 by Terence Parr & Sam Harwell (https://github.com/antlr/grammars-v4/blob/master/java9/Java9.g4)
//    Tutorial for expression evaluation by Sebastien Ros (https://www.codeproject.com/articles/18880/state-of-the-art-expression-evaluation)

grammar Ethql;

import EthqlCore;

document
    : statement* EOF
    ;

statement
    : scopedStatement
    | expressionStatement
    | emitStatement
    ;

scopedStatement
    : filter '{' statement* '}'
    ;

filter
    : genericFilter
    ;

genericFilter
    : KEY_IF '(' conditionalExpression ')'
    ;


// emitStatements

emitStatement
    : KEY_EMIT Identifier ';'
    ;



// expressionStatements

expressionStatement 
    : methodStatement
    | variableDeclarationStatement
    | variableAssignmentStatement
    ; 

variableDeclarationStatement
    : type variableName '=' statementExpression ';' 
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

type
    : TYPE_BOOLEAN
    | TYPE_INT
    | TYPE_FLOAT
    | TYPE_STRING
    ;

// Keywords
TYPE_INT : I N T;
TYPE_FLOAT : F L O A T;
TYPE_BOOLEAN : B O O L E A N;
TYPE_STRING : S T R I N G;


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