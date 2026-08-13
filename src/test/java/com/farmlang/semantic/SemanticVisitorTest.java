package com.farmlang.semantic;

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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("removal")
public class SemanticVisitorTest {

    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private static final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private SecurityManager originalSecurityManager;

    /** Exceção lançada para interceptar System.exit() sem matar a JVM. */
    private static class ExitException extends SecurityException {
        public final int status;
        public ExitException(int status) {
            super("System.exit interceptado com status: " + status);
            this.status = status;
        }
    }

    @BeforeAll
    public static void printHeader() {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║     TESTES DE FALHA — Deteccao de Erros          ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
    }

    @BeforeEach
    public void setUpStreams() {
        errContent.reset();
        System.setErr(new PrintStream(errContent));

        originalSecurityManager = System.getSecurityManager();
        System.setSecurityManager(new SecurityManager() {
            @Override
            public void checkExit(int status) {
                throw new ExitException(status);
            }
            @Override
            public void checkPermission(java.security.Permission perm) {
                // Permitir tudo
            }
        });
    }

    @AfterEach
    public void restoreStreams() {
        System.setErr(originalErr);
        System.setSecurityManager(originalSecurityManager);
    }

    private FarmLangErrorListener parseAndGetListener(String code) {
        CharStream input = CharStreams.fromString(code);
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

    private void runSemantic(String code) {
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
        
        SemanticVisitor semantic = new SemanticVisitor();
        semantic.visit(tree);
    }

    private void logErrorResult(String testName, String code, String errorMsg) {
        originalOut.println("  Teste: " + testName);
        originalOut.println("    Codigo:  " + code);
        originalOut.println("    Erro:    " + errorMsg.trim().replace("\n", " | ").replace("\r", ""));
        originalOut.println("    Status:  ERRO DETECTADO CORRETAMENTE!");
        originalOut.println();
    }

    // ══════════════════════════════════════════════════
    //  ERROS LEXICOS
    // ══════════════════════════════════════════════════

    @Test
    @DisplayName("Deverá identificar erro léxico com caractere invalido")
    public void testLexicalError() {
        String code = "int x = 10@;";
        FarmLangErrorListener listener = parseAndGetListener(code);
        assertTrue(listener.hasErrors());
        List<String> errors = listener.getErrors();
        assertTrue(errors.get(0).contains("[ERRO LEXICO]"));
        logErrorResult("[LEXICO] Caractere invalido '@'", code, errors.get(0));
    }

    // ══════════════════════════════════════════════════
    //  ERROS SINTATICOS
    // ══════════════════════════════════════════════════

    @Test
    @DisplayName("Deverá identificar erro sintático com ponto e virgula ausente")
    public void testSyntaxErrorMissingSemicolon() {
        String code = "int x = 10";
        FarmLangErrorListener listener = parseAndGetListener(code);
        assertTrue(listener.hasErrors());
        List<String> errors = listener.getErrors();
        String err = errors.stream().filter(e -> e.contains("[ERRO SINTATICO]")).findFirst().orElse("");
        logErrorResult("[SINTATICO] Ponto-e-virgula ausente", code, err);
    }
    
    @Test
    @DisplayName("Deverá identificar erro sintático com if sem parenteses")
    public void testSyntaxErrorIfWithoutParentheses() {
        String code = "if x == 10 { plant \"Corn\"; }";
        FarmLangErrorListener listener = parseAndGetListener(code);
        assertTrue(listener.hasErrors());
        List<String> errors = listener.getErrors();
        String err = errors.stream().filter(e -> e.contains("[ERRO SINTATICO]")).findFirst().orElse("");
        logErrorResult("[SINTATICO] if sem parenteses", code, err);
    }

    // ══════════════════════════════════════════════════
    //  ERROS SEMANTICOS
    // ══════════════════════════════════════════════════

    @Test
    @DisplayName("Deverá identificar erro semântico de variável não declarada")
    public void testUndeclaredVariable() {
        String code = "plant x;";
        ExitException ex = assertThrows(ExitException.class, () -> {
            runSemantic(code);
        });
        assertEquals(1, ex.status, "Deveria sair com status 1");
        String stderr = errContent.toString();
        assertTrue(stderr.contains("ERRO SEM"), "Deveria conter mensagem de erro semântico: " + stderr);
        logErrorResult("[SEMANTICO] Variavel nao declarada", code, stderr);
    }

    @Test
    @DisplayName("Deverá identificar erro semântico de tipo incompatível")
    public void testIncompatibleType() {
        String code = "int x = \"texto\";";
        ExitException ex = assertThrows(ExitException.class, () -> {
            runSemantic(code);
        });
        assertEquals(1, ex.status, "Deveria sair com status 1");
        String stderr = errContent.toString();
        assertTrue(stderr.contains("ERRO SEM"), "Deveria conter mensagem de erro semântico: " + stderr);
        logErrorResult("[SEMANTICO] Tipo incompativel (int = string)", code, stderr);
    }

    @Test
    @DisplayName("Deverá identificar erro semântico de variável duplicada")
    public void testDuplicateVariable() {
        String code = "int x = 1; int x = 2;";
        ExitException ex = assertThrows(ExitException.class, () -> {
            runSemantic(code);
        });
        assertEquals(1, ex.status, "Deveria sair com status 1");
        String stderr = errContent.toString();
        assertTrue(stderr.contains("ERRO SEM"), "Deveria conter mensagem de erro semântico: " + stderr);
        logErrorResult("[SEMANTICO] Variavel duplicada", code, stderr);
    }

    @Test
    @DisplayName("Deverá identificar erro semântico de operador inválido com string")
    public void testInvalidOperatorWithString() {
        String code = "string a = \"x\"; string b = \"y\"; string c = a - b;";
        ExitException ex = assertThrows(ExitException.class, () -> {
            runSemantic(code);
        });
        assertEquals(1, ex.status, "Deveria sair com status 1");
        String stderr = errContent.toString();
        assertTrue(stderr.contains("ERRO SEM"), "Deveria conter mensagem de erro semântico: " + stderr);
        logErrorResult("[SEMANTICO] Operador '-' com string", code, stderr);
    }

    @Test
    @DisplayName("Deverá identificar erro semântico com wait usando string")
    public void testWaitWithString() {
        String code = "wait \"trinta\";";
        ExitException ex = assertThrows(ExitException.class, () -> {
            runSemantic(code);
        });
        assertEquals(1, ex.status, "Deveria sair com status 1");
        String stderr = errContent.toString();
        assertTrue(stderr.contains("ERRO SEM"), "Deveria conter mensagem de erro semântico: " + stderr);
        logErrorResult("[SEMANTICO] wait com string", code, stderr);
    }
}
