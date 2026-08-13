package com.farmlang.semantic;

public class Symbol {
    private final String name;
    private final String type;
    private Object value;

    public Symbol(String name, String type, Object value){
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("symbol{name='%s', type='%s', value='%s'}", 
        name, type, value);
    }

}
