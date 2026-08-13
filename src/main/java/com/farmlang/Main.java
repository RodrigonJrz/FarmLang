package com.farmlang;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.Trees;

import com.farmlang.interpreter.InterpreterVisitor;
import com.farmlang.semantic.SemanticVisitor;

import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws Exception {

        Path file = Path.of("examples/valid.farm");
        boolean showTree = true;

        // ── Análise Léxica ──────────────────────────────────────
        CharStream input = CharStreams.fromPath(file);
        FarmLangLexer lexer = new FarmLangLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        
        // ── Análise Sintática ───────────────────────────────────
        FarmLangParser parser = new FarmLangParser(tokens);
        ParseTree tree = parser.program();

        //── Árvore Abstrata Sintática ────────────────────────────
        if (showTree) System.out.println(tree.toStringTree(parser));

        // ── Análise Semântica ───────────────────────────────────
        SemanticVisitor semantic = new SemanticVisitor();
        semantic.visit(tree);
        
        // ── Interpretador ───────────────────────────────────────
        InterpreterVisitor interpreter = new InterpreterVisitor();
        interpreter.visit(tree);

        System.out.println("Programa analisado com sucesso!");
    }
}