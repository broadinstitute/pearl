// Generated from /Users/barker/broad/juniper/core/src/main/antlr/bio/terra/pearl/core/antlr/CohortRule.g4 by ANTLR 4.13.2
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link CohortRuleParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface CohortRuleVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link CohortRuleParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(CohortRuleParser.ExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link CohortRuleParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm(CohortRuleParser.TermContext ctx);
}