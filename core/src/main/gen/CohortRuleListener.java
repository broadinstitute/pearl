// Generated from /Users/barker/broad/juniper/core/src/main/antlr/bio/terra/pearl/core/antlr/CohortRule.g4 by ANTLR 4.13.2
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link CohortRuleParser}.
 */
public interface CohortRuleListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link CohortRuleParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(CohortRuleParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link CohortRuleParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(CohortRuleParser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link CohortRuleParser#term}.
	 * @param ctx the parse tree
	 */
	void enterTerm(CohortRuleParser.TermContext ctx);
	/**
	 * Exit a parse tree produced by {@link CohortRuleParser#term}.
	 * @param ctx the parse tree
	 */
	void exitTerm(CohortRuleParser.TermContext ctx);
}