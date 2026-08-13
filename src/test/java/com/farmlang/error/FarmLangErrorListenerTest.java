package com.farmlang.error;

import com.farmlang.FarmLangLexer;
import com.farmlang.FarmLangParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FarmLangErrorListenerTest {

    private FarmLangErrorListener parse(String inputString) {
        CharStream input = CharStreams.fromString(inputString);
        FarmLangLexer lexer = new FarmLangLexer(input);
        lexer.removeErrorListeners();
        FarmLangErrorListener errorListener = new FarmLangErrorListener();
        lexer.addErrorListener(errorListener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        FarmLangParser parser = new FarmLangParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        parser.program();
        return errorListener;
    }

    @Test
    @DisplayName("Deverá detectar erro léxico em caractere inválido")
    public void testLexicalError() {
        FarmLangErrorListener listener = parse("int x = 5@;");

        assertTrue(listener.hasErrors(), "Deveria ter erros registrados");
        List<String> errors = listener.getErrors();
        assertEquals(1, errors.size(), "Deveria conter exatamente 1 erro");

        String error = errors.get(0);
        assertTrue(error.contains("[ERRO LEXICO]"), "Erro deveria ser classificado como [ERRO LEXICO]");
        assertTrue(error.contains("Linha 1"), "Erro deveria indicar a Linha 1");
        assertTrue(error.contains("Caractere invalido '@'"), "Erro deveria conter o caractere inválido '@'");
    }

    @Test
    @DisplayName("Deverá formatar erro léxico com aspa simples corretamente")
    public void testLexicalErrorSingleQuote() {
        FarmLangErrorListener listener = new FarmLangErrorListener();
        org.antlr.v4.runtime.Lexer dummyLexer = new com.farmlang.FarmLangLexer(CharStreams.fromString(""));
        listener.syntaxError(dummyLexer, null, 1, 0, "'", null);

        assertTrue(listener.hasErrors());
        assertEquals("[ERRO LEXICO] Linha 1: Caractere invalido '''", listener.getErrors().get(0));
    }

    @Test
    @DisplayName("Deverá detectar erro sintático ao omitir ponto e vírgula")
    public void testSyntacticErrorMissingSemicolon() {
        FarmLangErrorListener listener = parse("int x = 5");

        assertTrue(listener.hasErrors(), "Deveria ter erros registrados");
        List<String> errors = listener.getErrors();
        assertEquals(1, errors.size(), "Deveria conter exatamente 1 erro");

        String error = errors.get(0);
        assertTrue(error.contains("[ERRO SINTATICO]"), "Erro deveria ser classificado como [ERRO SINTATICO]");
        assertTrue(error.contains("Linha 1"), "Erro deveria indicar a Linha 1");
    }

    @Test
    @DisplayName("Deverá detectar erro sintático em chave não fechada")
    public void testSyntacticErrorUnclosedBrace() {
        String code = "if (x == 5) {\n" +
                      "    int y = 10;\n";
        FarmLangErrorListener listener = parse(code);

        assertTrue(listener.hasErrors(), "Deveria ter erros registrados para chave não fechada");
        List<String> errors = listener.getErrors();
        assertFalse(errors.isEmpty(), "Lista de erros não deveria estar vazia");

        boolean hasSyntacticError = false;
        for (String err : errors) {
            if (err.contains("[ERRO SINTATICO]")) {
                hasSyntacticError = true;
                break;
            }
        }
        assertTrue(hasSyntacticError, "Deveria conter erro sintático referente à chave não fechada");
    }

    @Test
    @DisplayName("Deverá aceitar programa válido sem registrar erros")
    public void testValidProgram() {
        FarmLangErrorListener listener = parse("int x = 5;");

        assertFalse(listener.hasErrors(), "Programa válido não deveria registrar erros");
        assertTrue(listener.getErrors().isEmpty(), "Lista de erros deveria estar vazia");
    }

    @Test
    @DisplayName("Deverá acumular múltiplos erros no mesmo arquivo")
    public void testMultipleErrors() {
        String code = "int x = 5@;\n" +
                      "int y = 10";

        FarmLangErrorListener listener = parse(code);

        assertTrue(listener.hasErrors(), "Deveria ter erros registrados");
        List<String> errors = listener.getErrors();
        assertTrue(errors.size() >= 2, "Deveria acumular múltiplos erros");

        assertTrue(errors.get(0).contains("[ERRO LEXICO]"), "Primeiro erro deveria ser léxico");
        assertTrue(errors.get(0).contains("Linha 1"), "Primeiro erro deveria ser na Linha 1");

        assertTrue(errors.get(1).contains("[ERRO SINTATICO]"), "Segundo erro deveria ser sintático");
        assertTrue(errors.get(1).contains("Linha 2"), "Segundo erro deveria ser na Linha 2");
    }

    @Test
    @DisplayName("Deverá imprimir mensagens em System.err e acionar o exitHandler ao chamar reportAndExit quando houver erros")
    public void testReportAndExitWithErrors() {
        FarmLangErrorListener listener = new FarmLangErrorListener();
        listener.syntaxError(null, null, 3, 0, "mismatched input", null);

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        boolean[] exitCalled = new boolean[]{false};
        int[] exitCode = new int[]{-1};

        try {
            System.setErr(new PrintStream(errContent));
            listener.reportAndExit(code -> {
                exitCalled[0] = true;
                exitCode[0] = code;
            });
        } finally {
            System.setErr(originalErr);
        }

        assertTrue(exitCalled[0], "reportAndExit deveria ter chamado o exitHandler");
        assertEquals(1, exitCode[0], "Código de saída deveria ser 1");
        String output = errContent.toString();
        assertTrue(output.contains("[ERRO SINTATICO] Linha 3: mismatched input"));
    }

    @Test
    @DisplayName("Não deverá imprimir mensagens nem acionar exitHandler ao chamar reportAndExit sem erros")
    public void testReportAndExitWithoutErrors() {
        FarmLangErrorListener listener = new FarmLangErrorListener();
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        boolean[] exitCalled = new boolean[]{false};

        try {
            System.setErr(new PrintStream(errContent));
            listener.reportAndExit(code -> exitCalled[0] = true);
        } finally {
            System.setErr(originalErr);
        }

        assertFalse(exitCalled[0], "exitHandler não deveria ser chamado quando não há erros");
        assertTrue(errContent.toString().isEmpty(), "Nenhuma mensagem deveria ser impressa em System.err");
    }
}
