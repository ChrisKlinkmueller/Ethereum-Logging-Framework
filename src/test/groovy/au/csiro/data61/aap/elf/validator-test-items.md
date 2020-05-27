# Validator Test Items

Validator is expected to performance both syntax and semantic check, which
is why test items are divided into the two categories.

The test items can be viewed as expectations of how the validator should behave.
&#x2611; before a test item means the expectation is met. Otherwise
the expectation is not met at the moment. We need to either fix the source
code to meet the expectation or adjust the expectation.

Feel free to edit this file when a test is added, modified, fixed or removed.

## Syntax

- [x] test all the formats of comment
- test identifier
  - [x] allow `_` and non-leading numbers
  - [x] support unicode character
  - [x] don't allow `^`, which can't be part of Java identifier
  - [x] don't allow leading numbers
- test type
  - [x] support type names in variable declaration
  - [x] don't allow upper case characters in type names
- test literal
  - [x] string literal (empty, non-empty, with escape character `\`)
  - [x] int literal (minus sign)
  - [x] boolean literal (allow upper case characters)
  - [x] byte literal
  - [x] empty list literal
  - [x] nonempty list literal
  - [x] don't allow `''` as wrapper of string literal
  - [x] don't allow `{}` as wrapper of list literal 
  
## Semantics

- test variable declaration
  - [x] don't allow double declarations
  - [x] don't allow use before declaration
- test type
  - [ ] support assigning empty list to an int list
  - [x] don't allow variable assignment of different value type
  - [ ] don't allow integer overflow
- [x] method calls must match existing method signature
- test emit
  - [x] happy path of log line, csv row, xes event
  - [x] don't allow undeclared var as csv table name
  - [ ] don't allow undeclared var as piid and xesEmitVariable
- block filter
  - [x] allow int block number
  - [x] allow key word earliest and current
  - [x] don't allow undeclared var as block number
  - [ ] don't allow first argument to be greater than the second
  - [ ] don't allow negative block number
  - [ ] don't allow non-integer block number, such as bytes or string
  - [x] don't allow block filter to be nested in transaction filter
- transaction filter
  - [ ] allow keyword ANY, address literal or variable as sender/recipient address
  - [x] don't allow non-nested transaction filter
  - [x] don't allow byte literal that is not an address
- generic filter
  - [x] allow boolean literal, variable, comparison, AND, OR, keywork IN and parentheses
  - [x] don't allow non-boolean value as operands of AND
  - [ ] don't allow non-list value after keywork IN
- smart contract filter
  - [ ] allow address literal or variable as contract address
  - [ ] smart contract parameters are in the scope of smart contract filter
  - [x] don't allow type mismatch in query parameters
- log entry filter
  - [x] allow variable as address
  - [x] log entry parameters are in the scope of log entry filter
  - [x] don't undeclared var as address
  - [x] don't allow non-nested log entry filter
