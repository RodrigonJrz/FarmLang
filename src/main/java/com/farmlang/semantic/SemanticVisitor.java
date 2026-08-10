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
        value = checkAndConvertType(type, value, line);
        scope.declare(name, type, value, line);

        return null;
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatementContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        int    line = ctx.IDENTIFIER().getSymbol().getLine();

        Symbol symbol = scope.lookup(name, line);
        Object value = visit(ctx.expression());
        checkAndConvertType(symbol.getType(), value, line);
        scope.assign(name, value, line);

        return null;
    }

    @Override
    public Object visitIfStatement(IfStatementContext ctx) {
        visit(ctx.expression());
        //Bloco then
        visit(ctx.block(0));
        //Bloco else se existir
        if (ctx.block().size() > 1) {
            visit(ctx.block(1));
        }
        return null;
    }

    @Override
    public Object visitWhileStatement(WhileStatementContext ctx) {
        visit(ctx.expression());
        visit(ctx.block());
        return null;
    }

    @Override
    public Object visitPlantStatement(PlantStatementContext ctx) {
        int    line  = ctx.start.getLine();
        Object value = visit(ctx.value());

        if (!(value instanceof String)) {
            semanticError(line,
                "'plant' espera um valor do tipo string, recebeu "
                + typeName(value) + ".");
        }
        return null;
    }

    @Override
    public Object visitWaterStatement(WaterStatementContext ctx) {
        int    line  = ctx.start.getLine();
        Object value = visit(ctx.value());

        if (!(value instanceof String)) {
            semanticError(line,
                "'water' espera um valor do tipo string, recebeu "
                + typeName(value) + ".");
        }
        return null;
    }

    @Override
    public Object visitFertilizeStatement(FertilizeStatementContext ctx) {
        int    line  = ctx.start.getLine();
        Object value = visit(ctx.value());

        if (!(value instanceof String)) {
            semanticError(line,
                "'fertilize' espera um valor do tipo string, recebeu "
                + typeName(value) + ".");
        }
        return null;
    }

    @Override
    public Object visitHarvestStatement(HarvestStatementContext ctx) {
        int    line  = ctx.start.getLine();
        Object value = visit(ctx.value());

        if (!(value instanceof String)) {
            semanticError(line,
                "'harvest' espera um valor do tipo string, recebeu "
                + typeName(value) + ".");
        }
        return null;
    }

    @Override
    public Object visitWaitStatement(WaitStatementContext ctx) {
        int    line  = ctx.start.getLine();
        Object value = visit(ctx.expression());

        if (!(value instanceof Number)) {
            semanticError(line,
                "'wait' espera um valor numérico (int ou float), recebeu "
                + typeName(value) + ".");
        }
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
        int    line  = ctx.REL_OP().getSymbol().getLine();

        if (left instanceof String || right instanceof String) {
            String op = ctx.REL_OP().getText();
            if (!op.equals("==") && !op.equals("!=")) {
                semanticError(line,
                    "Operador '" + op + "' não pode ser usado com string. "
                    + "Apenas '==' e '!=' são permitidos.");
            }
            if (left instanceof String && !(right instanceof String)) {
                semanticError(line,
                    "Não é possível comparar string com " + typeName(right) + ".");
            }
            if (right instanceof String && !(left instanceof String)) {
                semanticError(line,
                    "Não é possível comparar " + typeName(left) + " com string.");
            }
        }

        return true;
    }

    @Override
    public Object visitAdditiveExpression(AdditiveExpressionContext ctx) {
        Object result = visit(ctx.multiplicativeExpression(0));

        for (int i = 1; i < ctx.multiplicativeExpression().size(); i++) {
            Object right = visit(ctx.multiplicativeExpression(i));
            String op    = ctx.getChild(2 * i - 1).getText(); // '+' ou '-'
            int    line  = ctx.start.getLine();

            if (result instanceof String || right instanceof String) {
                if (!op.equals("+")) {
                    semanticError(line,
                        "Operador '-' não pode ser usado com string.");
                }
                if (result instanceof String && !(right instanceof String)) {
                    semanticError(line,
                        "Não é possível usar '+' entre string e "
                        + typeName(right) + ".");
                }
                if (right instanceof String && !(result instanceof String)) {
                    semanticError(line,
                        "Não é possível usar '+' entre "
                        + typeName(result) + " e string.");
                }
                result = "";
                continue;
            }

            checkNumeric(result, op, line);
            checkNumeric(right,  op, line);

            result = (result instanceof Double || right instanceof Double)
                ? 0.0 : 0;
        }

        return result;
    }

    @Override
    public Object visitMultiplicativeExpression(MultiplicativeExpressionContext ctx) {
        Object result = visit(ctx.primaryExpression(0));

        for (int i = 1; i < ctx.primaryExpression().size(); i++) {
            Object right = visit(ctx.primaryExpression(i));
            String op    = ctx.getChild(2 * i - 1).getText(); // '*' ou '/'
            int    line  = ctx.start.getLine();

            checkNumeric(result, op, line);
            checkNumeric(right,  op, line);

            result = (result instanceof Double || right instanceof Double)
                ? 0.0 : 0;
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
            int    line = ctx.IDENTIFIER().getSymbol().getLine();

            // Lança erro se não declarada
            Symbol symbol = scope.lookup(name, line);

            // Retorna valor simbólico do tipo correto para checagens
            return switch (symbol.getType()) {
                case "int"    -> 0;
                case "float"  -> 0.0;
                case "string" -> "";
                default       -> null;
            };
        }
        return visit(ctx.literal());
    }

    @Override
    public Object visitLiteral(LiteralContext ctx) {
        if (ctx.INT()    != null) return Integer.parseInt(ctx.INT().getText());
        if (ctx.FLOAT()  != null) return Double.parseDouble(ctx.FLOAT().getText());
        if (ctx.STRING() != null) {
            String raw = ctx.STRING().getText();
            return raw.substring(1, raw.length() - 1);
        }
        return null;
    }

    private Object checkAndConvertType(String type, Object value, int line) {
        return switch (type) {
            case "int" -> {
                if (value instanceof Integer i) yield i;
                typeError(line, type, value);
                yield null;
            }
            case "float" -> {
                if (value instanceof Double  d) yield d;
                if (value instanceof Integer i) yield i.doubleValue(); // promoção
                typeError(line, type, value);
                yield null;
            }
            case "string" -> {
                if (value instanceof String s) yield s;
                typeError(line, type, value);
                yield null;
            }
            default -> value;
        };
    }

    private void checkNumeric(Object value, String op, int line) {
        if (!(value instanceof Number)) {
            semanticError(line,
                "Operador '" + op + "' requer operandos numéricos, "
                + "recebeu " + typeName(value) + ".");
        }
    }

    private String typeName(Object value) {
        if (value instanceof Integer) return "int";
        if (value instanceof Double)  return "float";
        if (value instanceof String)  return "string";
        return "desconhecido";
    }

    private void typeError(int line, String expected, Object received) {
        semanticError(line,
            "Tipo incompatível. Esperado '" + expected
            + "', recebeu '" + typeName(received) + "'.");
    }

    private void semanticError(int line, String message) {
        System.err.println("[ERRO SEMÂNTICO] Linha " + line + ": " + message);
        System.exit(1);
    }
}