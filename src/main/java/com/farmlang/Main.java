package com.farmlang;

import java.nio.file.Path;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import com.farmlang.interpreter.InterpreterVisitor;
import com.farmlang.semantic.SemanticVisitor;

public class Main {

    public static void main(String[] args) throws Exception {

        Path file = (args.length > 0) ? Path.of(args[0]) : Path.of("examples/valid.farm");
        boolean showTree = false;

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