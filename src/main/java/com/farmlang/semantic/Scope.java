package com.farmlang.semantic;

import java.util.LinkedList;
import java.util.Optional;

public class Scope {
    private final LinkedList<SymbolTable> stack;

    public Scope() {
        stack = new LinkedList<>(); 
    }

    public void enterScope() {
        stack.push(new SymbolTable());
    }

    public void exitScope() {
        if(!stack.isEmpty()) stack.pop();
    }

    public void declare(String name, String type, Object value, int line) {
        SymbolTable current = stack.peek();

        if (current.contains(name)) {
            semanticError(line, 
                "Variável '" + name + "' já foi declarada neste escopo.");
        }

        current.add(name, type, value);
    }

    public Symbol lookup(String name, int line) {
        Optional<Symbol> found = 
        stack.stream()
        .map(table -> table.get(name))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();

        if (found.isEmpty()) {
            semanticError(line,
                "Variável '" + name + "' não foi declarada.");
        }

        return found.get();
    }

    public void assign(String name, Object newValue, int line) {
        for (SymbolTable table : stack) {
            if (table.update(name, newValue)) return;
        }
        semanticError(line,
            "Variável '" + name + "' não foi declarada.");
    }

    public boolean exists(String name) {
        return stack.stream().anyMatch(table -> table.contains(name));
    }

    private void semanticError(int line, String message) {
        System.err.println("[ERRO SEMÂNTICO] Linha " + line + ": " + message);
        System.exit(1);
    }
}
