package com.farmlang;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws Exception {

        Path file = Path.of("examples/valid.farm");

        CharStream input = CharStreams.fromPath(file);

        FarmLangLexer lexer = new FarmLangLexer(input);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        FarmLangParser parser = new FarmLangParser(tokens);

        ParseTree tree = parser.program();

        System.out.println("Programa analisado com sucesso!");
        System.out.println(tree.toStringTree(parser));
    }
}