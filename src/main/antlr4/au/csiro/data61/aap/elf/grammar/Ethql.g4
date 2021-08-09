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
    | emissionStatement
    ;

scopedStatement
    : scopeDefinition '{' statement* '}'
    ;

scopeDefinition
    : extractScope
    | ifScope
    ;

extractScope
    : KEY_EXTRACT Identifier PLUGIN_DEFINITION
    ;

ifScope
    : KEY_IF '(' conditionalExpression ')'
    ;

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
    : expression
    | methodInvocation
    ;


// emitStatements

configurationStatement
    : KEY_CONFIGURE Identifier PLUGIN_DEFINITION ';'
    ;

emissionStatement
    : KEY_EMIT Identifier PLUGIN_DEFINITION ';'
    ;



// expressionStatements

methodStatement
    : methodInvocation ';'
    ;

PLUGIN_DEFINITION : [0-9a-fA-F ():._'"{}]+;

// Keywords
KEY_EXTRACT : E X T R A C T;
KEY_IF : I F;
KEY_EMIT: E M I T;
KEY_CONFIGURE: C O N F I G U R E;
KEY_CSV_ROW: C S V ' ' R O W;
KEY_LOG_LINE: L O G ' ' L I N E;
KEY_XES_EVENT: X E S ' ' E V E N T;
KEY_XES_TRACE: X E S ' ' T R A C E;