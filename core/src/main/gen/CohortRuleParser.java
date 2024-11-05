// Generated from /Users/barker/broad/juniper/core/src/main/antlr/bio/terra/pearl/core/antlr/CohortRule.g4 by ANTLR 4.13.2
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class CohortRuleParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, NUMBER=3, STRING=4, VAR_STUDY=5, VAR_MODEL=6, VAR_ARGUMENTS=7, 
		VAR_OPEN=8, VAR_CLOSE=9, BOOLEAN=10, NULL=11, WS=12, OPERATOR=13, AND=14, 
		OR=15, PAR_OPEN=16, PAR_CLOSE=17, NOT=18, INCLUDE=19, FUNCTION_NAME=20;
	public static final int
		RULE_expr = 0, RULE_term = 1;
	private static String[] makeRuleNames() {
		return new String[] {
			"expr", "term"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'.'", "','", null, null, null, null, null, "'{'", "'}'", null, 
			"'null'", null, null, "'and'", "'or'", "'('", "')'", "'!'", "'include'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, "NUMBER", "STRING", "VAR_STUDY", "VAR_MODEL", "VAR_ARGUMENTS", 
			"VAR_OPEN", "VAR_CLOSE", "BOOLEAN", "NULL", "WS", "OPERATOR", "AND", 
			"OR", "PAR_OPEN", "PAR_CLOSE", "NOT", "INCLUDE", "FUNCTION_NAME"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "CohortRule.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public CohortRuleParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExprContext extends ParserRuleContext {
		public TerminalNode PAR_OPEN() { return getToken(CohortRuleParser.PAR_OPEN, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode PAR_CLOSE() { return getToken(CohortRuleParser.PAR_CLOSE, 0); }
		public List<TermContext> term() {
			return getRuleContexts(TermContext.class);
		}
		public TermContext term(int i) {
			return getRuleContext(TermContext.class,i);
		}
		public TerminalNode OPERATOR() { return getToken(CohortRuleParser.OPERATOR, 0); }
		public TerminalNode NOT() { return getToken(CohortRuleParser.NOT, 0); }
		public TerminalNode INCLUDE() { return getToken(CohortRuleParser.INCLUDE, 0); }
		public TerminalNode AND() { return getToken(CohortRuleParser.AND, 0); }
		public TerminalNode OR() { return getToken(CohortRuleParser.OR, 0); }
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CohortRuleListener ) ((CohortRuleListener)listener).enterExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CohortRuleListener ) ((CohortRuleListener)listener).exitExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CohortRuleVisitor ) return ((CohortRuleVisitor<? extends T>)visitor).visitExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExprContext expr() throws RecognitionException {
		return expr(0);
	}

	private ExprContext expr(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExprContext _localctx = new ExprContext(_ctx, _parentState);
		ExprContext _prevctx = _localctx;
		int _startState = 0;
		enterRecursionRule(_localctx, 0, RULE_expr, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(20);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PAR_OPEN:
				{
				setState(5);
				match(PAR_OPEN);
				setState(6);
				expr(0);
				setState(7);
				match(PAR_CLOSE);
				}
				break;
			case NUMBER:
			case STRING:
			case VAR_OPEN:
			case BOOLEAN:
			case NULL:
			case FUNCTION_NAME:
				{
				setState(9);
				term();
				setState(10);
				match(OPERATOR);
				setState(11);
				term();
				}
				break;
			case NOT:
				{
				setState(13);
				match(NOT);
				setState(14);
				expr(2);
				}
				break;
			case INCLUDE:
				{
				setState(15);
				match(INCLUDE);
				setState(16);
				match(PAR_OPEN);
				setState(17);
				term();
				setState(18);
				match(PAR_CLOSE);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(30);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(28);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
					case 1:
						{
						_localctx = new ExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(22);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(23);
						match(AND);
						setState(24);
						expr(5);
						}
						break;
					case 2:
						{
						_localctx = new ExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(25);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(26);
						match(OR);
						setState(27);
						expr(4);
						}
						break;
					}
					} 
				}
				setState(32);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TermContext extends ParserRuleContext {
		public TerminalNode NUMBER() { return getToken(CohortRuleParser.NUMBER, 0); }
		public TerminalNode STRING() { return getToken(CohortRuleParser.STRING, 0); }
		public TerminalNode VAR_OPEN() { return getToken(CohortRuleParser.VAR_OPEN, 0); }
		public TerminalNode VAR_MODEL() { return getToken(CohortRuleParser.VAR_MODEL, 0); }
		public TerminalNode VAR_CLOSE() { return getToken(CohortRuleParser.VAR_CLOSE, 0); }
		public TerminalNode VAR_ARGUMENTS() { return getToken(CohortRuleParser.VAR_ARGUMENTS, 0); }
		public TerminalNode BOOLEAN() { return getToken(CohortRuleParser.BOOLEAN, 0); }
		public TerminalNode NULL() { return getToken(CohortRuleParser.NULL, 0); }
		public TerminalNode FUNCTION_NAME() { return getToken(CohortRuleParser.FUNCTION_NAME, 0); }
		public TerminalNode PAR_OPEN() { return getToken(CohortRuleParser.PAR_OPEN, 0); }
		public List<TermContext> term() {
			return getRuleContexts(TermContext.class);
		}
		public TermContext term(int i) {
			return getRuleContext(TermContext.class,i);
		}
		public TerminalNode PAR_CLOSE() { return getToken(CohortRuleParser.PAR_CLOSE, 0); }
		public TermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_term; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CohortRuleListener ) ((CohortRuleListener)listener).enterTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CohortRuleListener ) ((CohortRuleListener)listener).exitTerm(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CohortRuleVisitor ) return ((CohortRuleVisitor<? extends T>)visitor).visitTerm(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TermContext term() throws RecognitionException {
		TermContext _localctx = new TermContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_term);
		int _la;
		try {
			setState(56);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NUMBER:
				enterOuterAlt(_localctx, 1);
				{
				setState(33);
				match(NUMBER);
				}
				break;
			case STRING:
				enterOuterAlt(_localctx, 2);
				{
				setState(34);
				match(STRING);
				}
				break;
			case VAR_OPEN:
				enterOuterAlt(_localctx, 3);
				{
				setState(35);
				match(VAR_OPEN);
				setState(36);
				match(VAR_MODEL);
				setState(39);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__0) {
					{
					setState(37);
					match(T__0);
					setState(38);
					match(VAR_ARGUMENTS);
					}
				}

				setState(41);
				match(VAR_CLOSE);
				}
				break;
			case BOOLEAN:
				enterOuterAlt(_localctx, 4);
				{
				setState(42);
				match(BOOLEAN);
				}
				break;
			case NULL:
				enterOuterAlt(_localctx, 5);
				{
				setState(43);
				match(NULL);
				}
				break;
			case FUNCTION_NAME:
				enterOuterAlt(_localctx, 6);
				{
				setState(44);
				match(FUNCTION_NAME);
				setState(45);
				match(PAR_OPEN);
				setState(46);
				term();
				setState(51);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__1) {
					{
					{
					setState(47);
					match(T__1);
					setState(48);
					term();
					}
					}
					setState(53);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(54);
				match(PAR_CLOSE);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 0:
			return expr_sempred((ExprContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expr_sempred(ExprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 4);
		case 1:
			return precpred(_ctx, 3);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001\u0014;\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0001"+
		"\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0003\u0000\u0015\b\u0000\u0001"+
		"\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0005"+
		"\u0000\u001d\b\u0000\n\u0000\f\u0000 \t\u0000\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0003\u0001(\b\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0005\u00012\b\u0001\n\u0001\f\u00015\t\u0001"+
		"\u0001\u0001\u0001\u0001\u0003\u00019\b\u0001\u0001\u0001\u0000\u0001"+
		"\u0000\u0002\u0000\u0002\u0000\u0000D\u0000\u0014\u0001\u0000\u0000\u0000"+
		"\u00028\u0001\u0000\u0000\u0000\u0004\u0005\u0006\u0000\uffff\uffff\u0000"+
		"\u0005\u0006\u0005\u0010\u0000\u0000\u0006\u0007\u0003\u0000\u0000\u0000"+
		"\u0007\b\u0005\u0011\u0000\u0000\b\u0015\u0001\u0000\u0000\u0000\t\n\u0003"+
		"\u0002\u0001\u0000\n\u000b\u0005\r\u0000\u0000\u000b\f\u0003\u0002\u0001"+
		"\u0000\f\u0015\u0001\u0000\u0000\u0000\r\u000e\u0005\u0012\u0000\u0000"+
		"\u000e\u0015\u0003\u0000\u0000\u0002\u000f\u0010\u0005\u0013\u0000\u0000"+
		"\u0010\u0011\u0005\u0010\u0000\u0000\u0011\u0012\u0003\u0002\u0001\u0000"+
		"\u0012\u0013\u0005\u0011\u0000\u0000\u0013\u0015\u0001\u0000\u0000\u0000"+
		"\u0014\u0004\u0001\u0000\u0000\u0000\u0014\t\u0001\u0000\u0000\u0000\u0014"+
		"\r\u0001\u0000\u0000\u0000\u0014\u000f\u0001\u0000\u0000\u0000\u0015\u001e"+
		"\u0001\u0000\u0000\u0000\u0016\u0017\n\u0004\u0000\u0000\u0017\u0018\u0005"+
		"\u000e\u0000\u0000\u0018\u001d\u0003\u0000\u0000\u0005\u0019\u001a\n\u0003"+
		"\u0000\u0000\u001a\u001b\u0005\u000f\u0000\u0000\u001b\u001d\u0003\u0000"+
		"\u0000\u0004\u001c\u0016\u0001\u0000\u0000\u0000\u001c\u0019\u0001\u0000"+
		"\u0000\u0000\u001d \u0001\u0000\u0000\u0000\u001e\u001c\u0001\u0000\u0000"+
		"\u0000\u001e\u001f\u0001\u0000\u0000\u0000\u001f\u0001\u0001\u0000\u0000"+
		"\u0000 \u001e\u0001\u0000\u0000\u0000!9\u0005\u0003\u0000\u0000\"9\u0005"+
		"\u0004\u0000\u0000#$\u0005\b\u0000\u0000$\'\u0005\u0006\u0000\u0000%&"+
		"\u0005\u0001\u0000\u0000&(\u0005\u0007\u0000\u0000\'%\u0001\u0000\u0000"+
		"\u0000\'(\u0001\u0000\u0000\u0000()\u0001\u0000\u0000\u0000)9\u0005\t"+
		"\u0000\u0000*9\u0005\n\u0000\u0000+9\u0005\u000b\u0000\u0000,-\u0005\u0014"+
		"\u0000\u0000-.\u0005\u0010\u0000\u0000.3\u0003\u0002\u0001\u0000/0\u0005"+
		"\u0002\u0000\u000002\u0003\u0002\u0001\u00001/\u0001\u0000\u0000\u0000"+
		"25\u0001\u0000\u0000\u000031\u0001\u0000\u0000\u000034\u0001\u0000\u0000"+
		"\u000046\u0001\u0000\u0000\u000053\u0001\u0000\u0000\u000067\u0005\u0011"+
		"\u0000\u000079\u0001\u0000\u0000\u00008!\u0001\u0000\u0000\u00008\"\u0001"+
		"\u0000\u0000\u00008#\u0001\u0000\u0000\u00008*\u0001\u0000\u0000\u0000"+
		"8+\u0001\u0000\u0000\u00008,\u0001\u0000\u0000\u00009\u0003\u0001\u0000"+
		"\u0000\u0000\u0006\u0014\u001c\u001e\'38";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}