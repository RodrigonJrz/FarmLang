grammar FarmLang;

program
    : statement* EOF
    ;

statement
    : 'plant' STRING ';'
    ;

STRING
    : '"' .*? '"'
    ;

WS
    : [ \t\r\n]+ -> skip
    ;