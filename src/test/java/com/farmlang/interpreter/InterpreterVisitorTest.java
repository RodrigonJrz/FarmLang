package com.farmlang.interpreter;

import com.farmlang.FarmLangLexer;
import com.farmlang.FarmLangParser;
import com.farmlang.error.FarmLangErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class InterpreterVisitorTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private static final PrintStream originalOut = System.out;

    @BeforeAll
    public static void printHeader() {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║     TESTES DE SUCESSO — Interpretador            ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
    }

    @BeforeEach
    public void setUpStreams() {
        outContent.reset();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    private String runCode(String code) {
        CharStream input = CharStreams.fromString(code);
        FarmLangLexer lexer = new FarmLangLexer(input);
        lexer.removeErrorListeners();
        FarmLangErrorListener errorListener = new FarmLangErrorListener();
        lexer.addErrorListener(errorListener);
        
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        FarmLangParser parser = new FarmLangParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);
        
        ParseTree tree = parser.program();
        
        assertFalse(errorListener.hasErrors(), "Código não deveria conter erros sintáticos/léxicos");
        
        InterpreterVisitor interpreter = new InterpreterVisitor();
        interpreter.visit(tree);

        return outContent.toString().trim();
    }

    private void logResult(String testName, String code, String output) {
        originalOut.println("  Teste: " + testName);
        originalOut.println("    Codigo:  " + code);
        originalOut.println("    Saida:   " + output.replace("\n", " | ").replace("\r", ""));
        originalOut.println("    Status:  PASSOU!");
        originalOut.println();
    }

    @Test
    @DisplayName("Deverá interpretar declaração de variável int e uso em expressão")
    public void testIntDeclarationAndExpression() {
        String code = "int x = 10; plant x;";
        String output = runCode(code);
        assertTrue(output.contains("Plantando 10"), "Saída incorreta para int");
        logResult("Declaracao INT", code, output);
    }

    @Test
    @DisplayName("Deverá interpretar declaração de variável float e promoção int -> float")
    public void testFloatDeclarationAndPromotion() {
        String code = "float f = 5; plant f;";
        String output = runCode(code);
        assertTrue(output.contains("Plantando 5.0"), "Saída incorreta para float/promoção");
        logResult("Declaracao FLOAT (promocao int->float)", code, output);
    }

    @Test
    @DisplayName("Deverá interpretar declaração de variável string")
    public void testStringDeclaration() {
        String code = "string s = \"Tomato\"; harvest s;";
        String output = runCode(code);
        assertTrue(output.contains("Colhendo Tomato"), "Saída incorreta para string");
        logResult("Declaracao STRING", code, output);
    }

    @Test
    @DisplayName("Deverá interpretar expressões aritméticas (+, -, *, /)")
    public void testArithmeticExpressions() {
        String code = "int a = 10 + 5; int b = 20 - a; int c = b * 2; int d = c / 2; plant d;";
        String output = runCode(code);
        assertTrue(output.contains("Plantando 5"), "Saída incorreta para aritmética");
        logResult("Expressoes aritmeticas (+, -, *, /)", code, output);
    }

    @Test
    @DisplayName("Deverá interpretar expressões relacionais (==, !=, >, <)")
    public void testRelationalExpressions() {
        String code = "if (10 > 5) { plant \"Maior\"; } if (5 < 10) { plant \"Menor\"; } if (5 == 5) { plant \"Igual\"; } if (5 != 10) { plant \"Diferente\"; }";
        String output = runCode(code);
        assertTrue(output.contains("Plantando Maior"));
        assertTrue(output.contains("Plantando Menor"));
        assertTrue(output.contains("Plantando Igual"));
        assertTrue(output.contains("Plantando Diferente"));
        logResult("Expressoes relacionais (==, !=, >, <)", code, output);
    }

    @Test
    @DisplayName("Deverá interpretar if/else executando o branch correto")
    public void testIfElseBranch() {
        String code = "int x = 10; if (x > 20) { plant \"Erro\"; } else { plant \"Sucesso\"; }";
        String output = runCode(code);
        assertFalse(output.contains("Erro"));
        assertTrue(output.contains("Sucesso"));
        logResult("IF/ELSE (branch correto)", code, output);
    }

    @Test
    @DisplayName("Deverá iterar while o número correto de vezes")
    public void testWhileIteration() {
        String code = "int i = 0; while (i < 3) { plant i; i = i + 1; }";
        String output = runCode(code);
        assertTrue(output.contains("Plantando 0"));
        assertTrue(output.contains("Plantando 1"));
        assertTrue(output.contains("Plantando 2"));
        assertFalse(output.contains("Plantando 3"));
        logResult("WHILE (3 iteracoes)", code, output);
    }

    @Test
    @DisplayName("Deverá interpretar comandos plant, water, fertilize, harvest corretamente")
    public void testFarmCommands() {
        String code = "string crop = \"Corn\"; plant crop; water crop; fertilize crop; harvest crop;";
        String output = runCode(code);
        assertTrue(output.contains("Plantando Corn"));
        assertTrue(output.contains("Irrigando Corn"));
        assertTrue(output.contains("Fertilizando Corn"));
        assertTrue(output.contains("Colhendo Corn"));
        logResult("Comandos de dominio (plant/water/fertilize/harvest)", code, output);
    }

    @Test
    @DisplayName("Deverá interpretar comando wait corretamente")
    public void testWaitCommand() {
        String code = "wait 5; wait 2.5;";
        String output = runCode(code);
        assertTrue(output.contains("Aguardando 5 dias..."));
        assertTrue(output.contains("Aguardando 2.5 dias..."));
        logResult("Comando WAIT", code, output);
    }
}
