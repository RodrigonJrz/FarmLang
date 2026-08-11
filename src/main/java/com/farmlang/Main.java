package com.farmlang;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import com.farmlang.semantic.SemanticVisitor;

import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws Exception {

        Path file = Path.of("examples/error_tests.farm");

        // ── Análise Léxica ─────────────────────────────────────
        CharStream input = CharStreams.fromPath(file);
        FarmLangLexer lexer = new FarmLangLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        
        // ── Análise Sintática ───────────────────────────────────
        FarmLangParser parser = new FarmLangParser(tokens);
        ParseTree tree = parser.program();

        // ── Análise Semântica ───────────────────────────────────
        SemanticVisitor semantic = new SemanticVisitor();
        semantic.visit(tree);
        
        System.out.println("Programa analisado com sucesso!");
        //System.out.println(tree.toStringTree(parser));
    }
}