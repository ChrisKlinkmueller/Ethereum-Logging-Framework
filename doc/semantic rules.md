## SolidityType
- (Done) must be valid type

## Variable
- (Done) Definition: variable name must not already be introduced 
  - (Done) implicitly by scope, or
  - (Done) explicitly by user
- (Done) Reference: variable name must exist

## Scope Definition
- (Done) if scopes introduce user defined variables (signatures in smart contract and log entry scopes), the variable must not be introduced
- (Open) literals and referenced variables must be type compatible 

## MethodCall
- (Open) Method name must be registered
- (Done) Existence of referenced variables in parameter list is checked by variable rule
- (Open) Types of parameters (variables or literals) must be compatible with one of the signatures for the methodname

## Statement
- (Open) Type of left and right side must be compatible