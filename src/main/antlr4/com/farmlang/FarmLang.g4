grammar FarmLang;


program
    : statement* EOF
    ;

statement
    : variableDeclaration
    | assignmentStatement
    | plantStatement
    | waterStatement
    | fertilizeStatement
    | harvestStatement
    | waitStatement
    | ifStatement
    | whileStatement
    ;
    
block
    : '{' statement* '}'
    ;

ifStatement
    : 'if' '(' expression ')' block ('else' block)?
    ;

whileStatement
    : 'while' '(' expression ')' block
    ;

variableDeclaration
    : type IDENTIFIER '=' expression ';'
    ;

plantStatement
    : 'plant' value ';'
    ;

waterStatement
    : 'water' value ';'
    ;

fertilizeStatement
    : 'fertilize' value ';'
    ;

harvestStatement
    : 'harvest' value ';'
    ;

waitStatement
    : 'wait' expression ';'
    ;

type
    : INT_TYPE
    | FLOAT_TYPE
    | STRING_TYPE
    ;


value
    : literal
    | IDENTIFIER
    ;

expression
    : relationalExpression
    ;

relationalExpression
    : additiveExpression (REL_OP additiveExpression)?
    ;

additiveExpression
    : multiplicativeExpression (('+' | '-') multiplicativeExpression)*
    ;

multiplicativeExpression
    : primaryExpression (('*' | '/') primaryExpression)*
    ;

primaryExpression
    : value
    | '(' expression ')'
    ;

literal
    : STRING
    | FLOAT
    | INT
    ;

STRING
    : '"' .*? '"'
    ;

FLOAT
    : [0-9]+ '.' [0-9]+
    ;

INT
    : [0-9]+
    ;

INT_TYPE
    : 'int'
    ;

FLOAT_TYPE
    : 'float'
    ;

STRING_TYPE
    : 'string'
    ;

REL_OP
    : '=='
    | '!='
    | '>='
    | '<='
    | '>'
    | '<'
    ;

IDENTIFIER
    : [a-zA-Z_] [a-zA-Z0-9_]*
    ;

assignmentStatement
    : IDENTIFIER '=' expression ';'
    ;

COMMENT
    : '//' ~[\r\n]* -> skip
    ;

WS
    : [ \t\r\n]+ -> skip
    ;