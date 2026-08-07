// Generated from FarmLang.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link FarmLangParser}.
 */
public interface FarmLangListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link FarmLangParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(FarmLangParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link FarmLangParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(FarmLangParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link FarmLangParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(FarmLangParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link FarmLangParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(FarmLangParser.StatementContext ctx);
}