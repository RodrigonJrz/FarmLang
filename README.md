# FarmLang — DSL para Simulação Agrícola

**FarmLang** é uma Linguagem de Domínio Específico (DSL - *Domain Specific Language*) desenvolvida para a simulação e automação de atividades agrícolas. O projeto foi desenvolvido como trabalho acadêmico para a disciplina de **Compiladores**, utilizando **Java 17**, **Apache Maven** e **ANTLR 4.13.1**.

## 👥 Integrantes

- Rodrigo
- Genifrank
- Ricardo

---

## 📋 Pré-requisitos

Para compilar e executar o projeto, certifique-se de ter instalado em sua máquina:
- **Java JDK 17** ou superior
- **Apache Maven 3.8** ou superior

---

## 🚀 Como Compilar e Executar

Abaixo estão os comandos Maven para compilar, executar e testar o compilador da FarmLang.

### 1. Compilar o Projeto
Gera os arquivos do Lexer e Parser do ANTLR e compila as classes Java:
```bash
mvn clean compile
```

### 2. Executar o Compilador
- **Executar com o arquivo de teste padrão (`examples/valid.farm`):**
```bash
mvn exec:java -Dexec.mainClass=com.farmlang.Main
```

- **Executar especificando um arquivo `.farm` via parâmetro:**
```bash
mvn exec:java -Dexec.mainClass=com.farmlang.Main -Dexec.args="examples/valid.farm"
```

### 3. Rodar os Testes Automatizados
Executa a suíte de testes unitários via JUnit 5:
```bash
mvn clean test
```

---

## 📜 Gramática e Sintaxe da Linguagem

A linguagem FarmLang possui uma sintaxe imperativa simples com suporte a variáveis, expressões matemáticas, comparações relacionais e estruturas de controle de fluxo.

### Tipos Primitivos e Declaração de Variáveis
A linguagem oferece suporte aos seguintes tipos primitivos:
- `int`: Números inteiros (ex: `int dias = 30;`)
- `float`: Números decimais de ponto flutuante (ex: `float umidade = 75.5;`)
- `string`: Cadeias de caracteres delimitadas por aspas duplas (ex: `string cultura = "Tomate";`)

### Atribuição de Variáveis
A atribuição permite atualizar valores de variáveis previamente declaradas:
```farmlang
dias = dias + 1;
umidade = 80.0;
```

### Expressões Aritméticas e Relacionais
- **Aritméticas:** Adição (`+`), Subtração (`-`), Multiplicação (`*`), Divisão (`/`)
- **Relacionais:** Igualdade (`==`), Diferença (`!=`), Maior que (`>`), Menor que (`<`), Maior ou igual (`>=`), Menor ou igual (`<=`)

### Estruturas de Controle de Fluxo
- **Condicional (`if` / `else`):**
```farmlang
if (dias == 5) {
    harvest crop;
} else {
    fertilize crop;
}
```

- **Repetição (`while`):**
```farmlang
while (dias < 5) {
    water crop;
    dias = dias + 1;
}
```

### Comentários
Suporte a comentários de linha única iniciados por `//`:
```farmlang
// Este é um comentário de linha em FarmLang
int dias = 0;
```

---

## 🚜 Comandos de Domínio (Nativos)

FarmLang possui 5 comandos nativos específicos para controle e automação de tarefas agrícolas:

| Comando | Propósito | Aceita | Exemplo |
| :--- | :--- | :--- | :--- |
| `plant` | Realiza o plantio de uma cultura | Literal `string` ou variável `string` | `plant "Milho";` ou `plant crop;` |
| `water` | Irriga a cultura especificada | Literal `string` ou variável `string` | `water "Milho";` ou `water crop;` |
| `fertilize` | Aplica fertilizante na cultura | Literal `string` ou variável `string` | `fertilize "Milho";` ou `fertilize crop;` |
| `harvest` | Realiza a colheita da cultura | Literal `string` ou variável `string` | `harvest "Milho";` ou `harvest crop;` |
| `wait` | Simula a espera/passagem de tempo em dias | Valor numérico (literal ou variável `int`/`float`) | `wait 10;` ou `wait dias;` |

---

## 🏗️ Arquitetura do Compilador (Pipeline)

O processo de processamento da FarmLang é estruturado em 4 fases principais:

```mermaid
graph LR
    A[Arquivo .farm] --> B[Análise Léxica<br/>ANTLR Lexer]
    B --> C[Análise Sintática<br/>ANTLR Parser]
    C --> D[Análise Semântica<br/>SemanticVisitor]
    D --> E[Interpretação<br/>InterpreterVisitor]
```

1. **Análise Léxica (ANTLR Lexer - `FarmLangLexer`):**
   - Transforma a sequência de caracteres do código fonte em uma sequência de tokens.
   - Trata espaços em branco e descarta comentários (`//`).
   - Identifica e reporta caracteres inválidos indicando a linha exata.

2. **Análise Sintática (ANTLR Parser - `FarmLangParser`):**
   - Valida se a sequência de tokens respeita a gramática definida em `FarmLang.g4`.
   - Geração da Árvore de Sintaxe Abstrata (AST - *Abstract Syntax Tree*).

3. **Análise Semântica (`SemanticVisitor` + `Scope` / `SymbolTable`):**
   - Validação de escopos e regras de tipagem.
   - Verificação de variáveis não declaradas ou redeclaradas.
   - Checagem de tipos em expressões, atribuições e comandos de domínio (garantindo que `plant`, `water`, `fertilize` e `harvest` recebam `string` e `wait` receba um valor numérico).

4. **Interpretação (`InterpreterVisitor`):**
   - Execução direta dos nós da AST validada semânticamente.

---

## 💡 Exemplos de Código

### Programa Válido Completo (`examples/valid.farm`)

```farmlang
string crop = "Tomato";
int days = 0;
float humidity = 75.5;

plant crop;

while(days < 5){

    water crop;

    days = days + 1;

}

if(days == 5){

    harvest crop;

}else{

    fertilize crop;

}

wait days;
```

### Arquivos de Exemplo
No diretório `examples/`, estão disponíveis:
- `examples/valid.farm`: Programa de exemplo completo exercitando todos os comandos e estruturas da linguagem.
- `examples/error_tests.farm`: Arquivo utilizado para validação e testes das mensagens de erro do compilador.
- `examples/lexical_error.farm`: Programa com caractere inválido (`@`) para testar a detecção de **erros léxicos**.
- `examples/syntax_error.farm`: Programa com `if` sem parênteses para testar a detecção de **erros sintáticos**.
- `examples/semantic_error.farm`: Programa usando variável não declarada para testar a detecção de **erros semânticos**.

---

## ⚠️ Tratamento de Erros

O compilador FarmLang detecta e reporta 3 categorias de erros com mensagens claras contendo o número da linha:

### 1. Erro Léxico
Ocorre ao encontrar um caractere inválido ou não reconhecido no código fonte.
```text
[ERRO LEXICO] Linha 3: Caractere invalido '@'
```

### 2. Erro Sintático
Ocorre quando o código quebra as regras da gramática definida (ex: tokens inesperados ou ausência de delimitadores).
```text
[ERRO SINTATICO] Linha 5: mismatched input 'plant' expecting ';'
```

### 3. Erro Semântico
Ocorre quando há violação de regras semânticas, escopo ou tipos incompatíveis.
```text
[ERRO SEMÂNTICO] Linha 4: Variável 'x' não foi declarada.
```

---

## 🧪 Testes Automatizados

O projeto conta com **27 testes JUnit 5** organizados em 5 classes, cobrindo desde smoke tests até cenários de erro.

Para executar toda a suíte:
```bash
mvn clean test
```

| Classe de Teste | Descrição | Qtd |
| :--- | :--- | :---: |
| `AppTest` | Teste placeholder padrão | 1 |
| `FarmLangErrorListenerTest` | Testes do error listener (erros léxicos e sintáticos) | 8 |
| `FarmLangSmokeTest` | Smoke test do pipeline completo (Lexer → Parser → Semântico → Interpretador) | 1 |
| `InterpreterVisitorTest` | Testes de sucesso: variáveis, expressões, `if`/`else`, `while`, comandos de domínio | 9 |
| `SemanticVisitorTest` | Testes de falha: erros léxicos, sintáticos e semânticos isolados | 8 |
| **Total** | | **27** |

### Cenários de Erro Testados

- **Erro Léxico**: Caractere inválido (`@`) detectado com número da linha.
- **Erro Sintático**: Ponto-e-vírgula ausente e `if` sem parênteses.
- **Erro Semântico**: Variável não declarada, variável duplicada, tipo incompatível (`int x = "texto"`), operador inválido com string (`"a" - "b"`), e `wait` com argumento string.

---

## 📂 Estrutura do Projeto

```text
FarmLang/
├── pom.xml
├── README.md
├── examples/
│   ├── valid.farm
│   ├── error_tests.farm
│   ├── lexical_error.farm
│   ├── syntax_error.farm
│   └── semantic_error.farm
└── src/
    ├── main/
    │   ├── antlr4/com/farmlang/
    │   │   └── FarmLang.g4
    │   └── java/com/farmlang/
    │       ├── Main.java
    │       ├── error/
    │       │   └── FarmLangErrorListener.java
    │       ├── interpreter/
    │       │   └── InterpreterVisitor.java
    │       └── semantic/
    │           ├── Scope.java
    │           ├── SemanticVisitor.java
    │           ├── Symbol.java
    │           └── SymbolTable.java
    └── test/
        └── java/com/farmlang/
            ├── AppTest.java
            ├── FarmLangSmokeTest.java
            ├── error/
            │   └── FarmLangErrorListenerTest.java
            ├── interpreter/
            │   └── InterpreterVisitorTest.java
            └── semantic/
                └── SemanticVisitorTest.java
```
