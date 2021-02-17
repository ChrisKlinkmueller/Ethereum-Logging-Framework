// based on
//    (outdated) ANTLR4 grammar for SQLite by Bart Kiers (https://github.com/antlr/grammars-v4/blob/master/sqlite/SQLite.g4), and
//    (outdated) ANTLR4 grammar for Java9 by Terence Parr & Sam Harwell (https://github.com/antlr/grammars-v4/blob/master/java9/Java9.g4)
//    Tutorial for expression evaluation by Sebastien Ros (https://www.codeproject.com/articles/18880/state-of-the-art-expression-evaluation)
//
// additional helpful links
//    ANTLR4 documentation (https://github.com/antlr/antlr4/blob/4.9/doc/index.md)
//    ANTLR4 presentation by Terence Parr (https://vimeo.com/59285751)
//    ANTLR4 introduction by TU Darmstadt in german (https://www.esa.informatik.tu-darmstadt.de/archive/twiki/pub/Lectures/Compiler113De/antlr-v4-handout.pdf)


grammar Bcql;

import BcqlUtil, BcqlLexemes;



//      GENERAL: Entry rules which are initiating the parser tree

/** The document parser rule is the root parser rule of a manifest file, all other grammar rules are nested inside it
 *  and must be satisfied.
 *
 *  A blockchain, a connection and an outputfolder must be specified. After that, tt can include an arbitrary number of
 *  statements and the explicit EOF defines that the entire file is parsed. */

document
    : blockchain outputFolder optionalParams* connection statement* EOF
    ;


blockchain
    : KEY_SET KEY_BLOCKCHAIN literal ';'
    ;


connection
    : KEY_SET (KEY_IPC)? KEY_CONNECTION literal ';'
    ;


outputFolder
    : KEY_SET KEY_OUTPUT_FOLDER literal ';'
    ;


optionalParams
    : emissionMode
    | abortOnException
    | errorOutput
    ;

errorOutput
    : KEY_SET KEY_ERROR_OUTPUT STRING_LITERAL STRING_LITERAL? ';'
    ;

emissionMode
    : KEY_SET KEY_EMISSION_MODE literal ';'
    ;

abortOnException
    : KEY_SET KEY_ABORT_ON_EXCEPTION BOOLEAN_LITERAL ';'
    ;

/** A statement is parsed to a scope, an expressionStatement or an emitStatement. */

statement
    : scope
    | expressionStatement
    | emitStatement
    ;


/** A scope is composed of a filter, which is applied to an arbitrary number of statements inside of {} braces. */

scope
    : filter '{' statement* '}'
    ;



//      FILTERS: Primary grammar rules to narrow down the logging data in scopes

/** A filter is parsed to a blockFilter, a transactionFilter, a logEntryFilter, a genericFilter or a smartContractFilter. */

filter
    : blockFilter
    | transactionFilter
    | logEntryFilter
    | smartContractFilter
    | genericFilter
    ;


/** A blockFilter allows for selecting blocks whose block number is in the interval [from,to].

    Provides access to the attributes of those blocks like mining difficulty or consumed gas. Starts with the keyword
    BLOCKS and subsequently sets a starting and an ending blockNumber, each inside of () braces. */

blockFilter
    : KEY_BLOCK_RANGE '(' from=blockNumber ')' '(' to=blockNumber ')'
    ;


/** A transactionFilter is used to narrow down the set of transactions within the selected blocks based on account addresses
    of senders and recipients.

    The transactionFilter must be nested within a blockFilter. Within the scope of such a filter are transaction attributes,
    such as gas price or transferred value, are accessible. Starts with the keyword TRANSACTIONS and subsequently sets a
    optional sending addressList and an mandatory recipients addressList, each inside of () braces. */

transactionFilter
    : KEY_TRANSACTIONS '(' (senders=addressList)? ')' '(' recipients=addressList ')'
    ;


/** A logEntryFilter enables users to select log entries that were emitted by smart contracts during transaction execution.

    Users need to specify the relevant smart contract addresses and the event signature. Must be nested within a blockFilter
    or a transactionFilter. Provides access to log entry attributes and the event signature parameters. Starts with the
    keyword LOG ENTRIES and subsequently sets an addressList and an logEntrySignature, each inside of () braces. */

logEntryFilter
    : KEY_LOG_ENTRIES '(' addressList ')' '(' logEntrySignature ')'
    ;


/** A smartContractFilter allows for querying state information of smart contracts.

    Users must specify the contract address and the member variables or functions. Note that these variables and functions
    must be part of the contract’s public API. Must be nested within a block filter. Starts with the keyword SMART CONTRACT
    and subsequently sets a valueExpression and at least one smartContractQuery, each inside of () braces. */

smartContractFilter
    : KEY_SMART_CONTRACT '(' contractAddress=valueExpression ')' ('(' smartContractQuery ')')+
    ;


/** A genericFilter allows users to introduce arbitrary criteria which can rely on entity or user-defined variables.

    Can be nested into any other filter, but does not provide access to new variables. Starts with the keyword IF
    and subsequently sets a conditionalExpression inside of () braces. */

genericFilter
    : KEY_IF '(' conditionalExpression ')'
    ;




//      EMIT STATEMENTS: Defines the target data and the output format in a specific scope

/** An emitStatement is parsed to an emitStatementCsv, an emitStatementLog, an emitStatementXesEvent or an emitStatementXesTrace */

emitStatement
    : emitStatementCsv
    | emitStatementLog
    | emitStatementXesEvent
    | emitStatementXesTrace
    ;


/** An emitStatementCsv sets the CSV format as data output and allows for further target data and output format specification  */

emitStatementCsv
    : KEY_EMIT KEY_CSV_ROW '(' tableName=valueExpression ')' '(' namedEmitVariable (',' namedEmitVariable)* ')'	';'
    ;


/** An emitStatementLog sets the Log format as data output and allows for further target data and output format specification  */

emitStatementLog
    : KEY_EMIT KEY_LOG_LINE '(' valueExpression (',' valueExpression)* ')' ';'
    ;


/** An emitStatementXesTrace sets the XES event format as data output and allows for further target data and output format specification  */

emitStatementXesEvent
    : KEY_EMIT KEY_XES_EVENT '(' (pid=valueExpression)? ')' '(' (piid=valueExpression)? ')' '(' (eid=valueExpression)? ')' '(' xesEmitVariable (',' xesEmitVariable)* ')' ';'
    ;


/** An emitStatementXesTrace sets the XES trace format as data output and allows for further target data and output format specification  */

emitStatementXesTrace
    : KEY_EMIT KEY_XES_TRACE '(' (pid=valueExpression)? ')' '(' (piid=valueExpression)? ')' '(' xesEmitVariable (',' xesEmitVariable)* ')' ';'
    ;
