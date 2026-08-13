package com.farmlang.semantic;

import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
public class SymbolTable {
    private final Map<String, Symbol> table;

    public SymbolTable() {
        table = new HashMap<>();
    }

    public void add(String name, String type, Object value) {
        table.put(name, new Symbol(name, type, value));
    }

    public Optional<Symbol> get(String name) {
        return Optional.ofNullable(table.get(name));
    }

    public boolean update(String name, Object newValue) {
        Symbol symbol = table.get(name);
        if (symbol == null) return false;
        symbol.setValue(newValue);
        return true;
    }

    public boolean contains(String name) {
        return table.containsKey(name);
    }
}