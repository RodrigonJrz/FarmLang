package com.farmlang.interpreter;

import com.farmlang.FarmLangBaseVisitor;
import com.farmlang.FarmLangParser.*;
import com.farmlang.semantic.Scope;
import com.farmlang.semantic.Symbol;

public class InterpreterVisitor extends FarmLangBaseVisitor<Object>{
    private final Scope scope;
    
    public InterpreterVisitor() {
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

    @Override
    public Object visitBlock(BlockContext ctx) {
        scope.enterScope();
        for (var stmt : ctx.statement()) {
            visit(stmt);
        }
        scope.exitScope();
        return null;
    }

    @Override
    public Object visitVariableDeclaration(VariableDeclarationContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        String type = ctx.type().getText();
        int line = ctx.IDENTIFIER().getSymbol().getLine();

        Object value = visit(ctx.expression());
        if (type.equals("float") && value instanceof Integer i) {
            value = i.doubleValue();
        }
        scope.declare(name, type, value, line);

        return null;
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatementContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        int line = ctx.IDENTIFIER().getSymbol().getLine();
        Object value = visit(ctx.expression());
        Symbol symbol = scope.lookup(name, line);
        if (symbol.getType().equals("float") && value instanceof Integer i) {
            value = i.doubleValue();
        }
        scope.assign(name, value, line);

        return null;
    }

    @Override
    public Object visitIfStatement(IfStatementContext ctx) {
        boolean condition = (boolean) visit(ctx.expression());
        
        if (condition) {
            //Bloco then
            visit(ctx.block(0));
        } 
        //Bloco else se existir
        else if (ctx.block().size() > 1) {
            visit(ctx.block(1));
        }
        
        return null;
    }

    @Override
    public Object visitWhileStatement(WhileStatementContext ctx) {
        while ((boolean) visit(ctx.expression())) {
            visit(ctx.block());
        }
        
        return null;
    }

    @Override
    public Object visitPlantStatement(PlantStatementContext ctx) {
        Object value = visit(ctx.value());
        System.out.println("Plantando " + value);
        return null;
    }

    @Override
    public Object visitWaterStatement(WaterStatementContext ctx) {
        Object value = visit(ctx.value());
        System.out.println("Irrigando " + value);
        return null;
    }

    @Override
    public Object visitFertilizeStatement(FertilizeStatementContext ctx) {
        Object value = visit(ctx.value());
        System.out.println("Fertilizando " + value);
        return null;
    }

    @Override
    public Object visitHarvestStatement(HarvestStatementContext ctx) {
        Object value = visit(ctx.value());
        System.out.println("Colhendo " + value);
        return null;
    }

    @Override
    public Object visitWaitStatement(WaitStatementContext ctx) {
        Object value = visit(ctx.expression());
        System.out.println("Aguardando " + formatValue(value) + " dias...");
        return null;
    }

    @Override
    public Object visitExpression(ExpressionContext ctx) {
        return visit(ctx.relationalExpression());
    }

    @Override
    public Object visitRelationalExpression(RelationalExpressionContext ctx) {
        Object left = visit(ctx.additiveExpression(0));

        if (ctx.REL_OP() == null) return left;

        Object right = visit(ctx.additiveExpression(1));
        String op = ctx.REL_OP().getText();


        if (left instanceof String ls && right instanceof String rs) {
           return switch (op) {
                case "==" -> ls.equals(rs);
                case "!=" -> !ls.equals(rs);
                default -> false;
            };
        }

        double l = toDouble(left);
        double r = toDouble(right);

        return switch (op) {
            case "==" -> l == r;
            case "!=" -> l != r;
            case ">" -> l > r;
            case "<" -> l < r;
            case ">=" -> l >= r;
            case "<=" -> l <= r;
            default -> false;
        };
    }

    @Override
    public Object visitAdditiveExpression(AdditiveExpressionContext ctx) {
        Object result = visit(ctx.multiplicativeExpression(0));

        for (int i = 1; i < ctx.multiplicativeExpression().size(); i++) {
            Object right = visit(ctx.multiplicativeExpression(i));
            String op = ctx.getChild(2 * i - 1).getText(); // '+' ou '-'

            if (result instanceof String || right instanceof String) {
                result = formatValue(result) + formatValue(right);
                continue;
            }

            if (result instanceof Double || right instanceof Double) {
                double l = toDouble(result);
                double r = toDouble(right);
                result = op.equals("+") ? l + r : l - r;
            } else {
                int l = (Integer) result;
                int r = (Integer) right;
                result = op.equals("+") ? l + r : l - r;
            }   
        }

        return result;
    }

    @Override
    public Object visitMultiplicativeExpression(MultiplicativeExpressionContext ctx) {
        Object result = visit(ctx.primaryExpression(0));

        for (int i = 1; i < ctx.primaryExpression().size(); i++) {
            Object right = visit(ctx.primaryExpression(i));
            String op = ctx.getChild(2 * i - 1).getText(); // '*' ou '/'
            
            if (result instanceof Double || right instanceof Double) {
                double l = toDouble(result);
                double r = toDouble(right);
                result = op.equals("*") ? l * r : l / r;
            } else {
                int l = (Integer) result;
                int r = (Integer) right;
                result = op.equals("*") ? l * r : l / r;
            }
        }

        return result;
    }

    @Override
    public Object visitPrimaryExpression(PrimaryExpressionContext ctx) {
        if (ctx.expression() != null) {
            return visit(ctx.expression());
        }
        return visit(ctx.value());
    }

    @Override
    public Object visitValue(ValueContext ctx) {
        if (ctx.IDENTIFIER() != null) {
            String name = ctx.IDENTIFIER().getText();
            int line = ctx.IDENTIFIER().getSymbol().getLine();
            return scope.lookup(name, line).getValue();
        }
        return visit(ctx.literal());
    }

    @Override
    public Object visitLiteral(LiteralContext ctx) {
        if (ctx.INT() != null) return Integer.parseInt(ctx.INT().getText());
        if (ctx.FLOAT() != null) return Double.parseDouble(ctx.FLOAT().getText());
        if (ctx.STRING() != null) {
            String raw = ctx.STRING().getText();
            return raw.substring(1, raw.length() - 1);
        }
        return null;
    }

    /* COMANDOS AUXILIARES  */

    private double toDouble(Object value) {
        if (value instanceof Double  d) return d;
        if (value instanceof Integer i) return i.doubleValue();
        return 0;
    }

    private String formatValue(Object value) {
        if (value instanceof Double d) {
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                return String.valueOf(d.intValue());
            }
            return String.valueOf(d);
        }
        return String.valueOf(value);
    }

}
