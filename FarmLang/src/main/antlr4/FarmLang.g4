grammar FarmLang;


program
    : statement* EOF
    ;


statement
    : 'plant' ID ';'
    ;


ID
    : [a-zA-Z]+
    ;


WS
    : [ \t\r\n]+ -> skip
    ;