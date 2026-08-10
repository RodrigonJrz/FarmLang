package com.farmlang.semantic;

import com.farmlang.FarmLangBaseVisitor;
import com.farmlang.FarmLangParser.*;
public class SemanticVisitor extends FarmLangBaseVisitor<Object> {
    private final Scope scope;

    public SemanticVisitor() {
        scope = new Scope();
    }

    @Override
    public Object visitProgram(ProgramContext ctx) {
        scope.enterScope();
        for (var stmt : ctx.statement()) {
            visit(stmt);
        }
        scope.exitScope();
        return null;
    }
}