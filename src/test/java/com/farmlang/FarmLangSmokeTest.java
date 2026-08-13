package com.farmlang;

import com.farmlang.error.FarmLangErrorListener;
import com.farmlang.interpreter.InterpreterVisitor;
import com.farmlang.semantic.SemanticVisitor;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FarmLangSmokeTest {

    @Test
    @DisplayName("Teste Smoke: Executa o pipeline completo sem erros")
    public void testSmokePipeline() {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║        SMOKE TEST — Pipeline Completo            ║");
        System.out.println("╚══════════════════════════════════════════════════╝");

        String code = "int x = 10; plant \"Corn\";";
        System.out.println("  Codigo: " + code);

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

        assertFalse(errorListener.hasErrors(), "Pipeline não deve conter erros sintáticos/léxicos no smoke test");
        System.out.println("  [1/3] Analise Lexica + Sintatica ........... OK");

        SemanticVisitor semanticVisitor = new SemanticVisitor();
        assertDoesNotThrow(() -> semanticVisitor.visit(tree), "SemanticVisitor não deve lançar exceção");
        System.out.println("  [2/3] Analise Semantica .................... OK");

        InterpreterVisitor interpreterVisitor = new InterpreterVisitor();
        assertDoesNotThrow(() -> interpreterVisitor.visit(tree), "InterpreterVisitor não deve lançar exceção");
        System.out.println("  [3/3] Interpretador ........................ OK");

        System.out.println("  ──────────────────────────────────────────────");
        System.out.println("  RESULTADO: PIPELINE COMPLETO FUNCIONANDO!");
        System.out.println();
    }
}
