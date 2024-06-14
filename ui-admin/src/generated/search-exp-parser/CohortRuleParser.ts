// Generated from bio/terra/pearl/core/antlr/CohortRule.g4 by ANTLR 4.13.1
// noinspection ES6UnusedImports,JSUnusedGlobalSymbols,JSUnusedLocalSymbols

import {
	ATN,
	ATNDeserializer, DecisionState, DFA, FailedPredicateException,
	RecognitionException, NoViableAltException, BailErrorStrategy,
	Parser, ParserATNSimulator,
	RuleContext, ParserRuleContext, PredictionMode, PredictionContextCache,
	TerminalNode, RuleNode,
	Token, TokenStream,
	Interval, IntervalSet
} from 'antlr4';
import CohortRuleVisitor from "./CohortRuleVisitor.js";

// for running tests with parameters, TODO: discuss strategy for typed parameters in CI
// eslint-disable-next-line no-unused-vars
type int = number;

export default class CohortRuleParser extends Parser {
	public static readonly T__0 = 1;
	public static readonly NUMBER = 2;
	public static readonly STRING = 3;
	public static readonly VARIABLE = 4;
	public static readonly BOOLEAN = 5;
	public static readonly NULL = 6;
	public static readonly WS = 7;
	public static readonly OPERATOR = 8;
	public static readonly AND = 9;
	public static readonly OR = 10;
	public static readonly PAR_OPEN = 11;
	public static readonly PAR_CLOSE = 12;
	public static readonly FUNCTION_NAME = 13;
	public static readonly EOF = Token.EOF;
	public static readonly RULE_expr = 0;
	public static readonly RULE_term = 1;
	public static readonly literalNames: (string | null)[] = [ null, "','", 
                                                            null, null, 
                                                            null, null, 
                                                            "'null'", null, 
                                                            null, "'and'", 
                                                            "'or'", "'('", 
                                                            "')'" ];
	public static readonly symbolicNames: (string | null)[] = [ null, null, 
                                                             "NUMBER", "STRING", 
                                                             "VARIABLE", 
                                                             "BOOLEAN", 
                                                             "NULL", "WS", 
                                                             "OPERATOR", 
                                                             "AND", "OR", 
                                                             "PAR_OPEN", 
                                                             "PAR_CLOSE", 
                                                             "FUNCTION_NAME" ];
	// tslint:disable:no-trailing-whitespace
	public static readonly ruleNames: string[] = [
		"expr", "term",
	];
	public get grammarFileName(): string { return "CohortRule.g4"; }
	public get literalNames(): (string | null)[] { return CohortRuleParser.literalNames; }
	public get symbolicNames(): (string | null)[] { return CohortRuleParser.symbolicNames; }
	public get ruleNames(): string[] { return CohortRuleParser.ruleNames; }
	public get serializedATN(): number[] { return CohortRuleParser._serializedATN; }

	protected createFailedPredicateException(predicate?: string, message?: string): FailedPredicateException {
		return new FailedPredicateException(this, predicate, message);
	}

	constructor(input: TokenStream) {
		super(input);
		this._interp = new ParserATNSimulator(this, CohortRuleParser._ATN, CohortRuleParser.DecisionsToDFA, new PredictionContextCache());
	}

	public expr(): ExprContext;
	public expr(_p: number): ExprContext;
	// @RuleVersion(0)
	public expr(_p?: number): ExprContext {
		if (_p === undefined) {
			_p = 0;
		}

		let _parentctx: ParserRuleContext = this._ctx;
		let _parentState: number = this.state;
		let localctx: ExprContext = new ExprContext(this, this._ctx, _parentState);
		let _prevctx: ExprContext = localctx;
		let _startState: number = 0;
		this.enterRecursionRule(localctx, 0, CohortRuleParser.RULE_expr, _p);
		try {
			let _alt: number;
			this.enterOuterAlt(localctx, 1);
			{
			this.state = 13;
			this._errHandler.sync(this);
			switch (this._input.LA(1)) {
			case 11:
				{
				this.state = 5;
				this.match(CohortRuleParser.PAR_OPEN);
				this.state = 6;
				this.expr(0);
				this.state = 7;
				this.match(CohortRuleParser.PAR_CLOSE);
				}
				break;
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 13:
				{
				this.state = 9;
				this.term();
				this.state = 10;
				this.match(CohortRuleParser.OPERATOR);
				this.state = 11;
				this.term();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			this._ctx.stop = this._input.LT(-1);
			this.state = 23;
			this._errHandler.sync(this);
			_alt = this._interp.adaptivePredict(this._input, 2, this._ctx);
			while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER) {
				if (_alt === 1) {
					if (this._parseListeners != null) {
						this.triggerExitRuleEvent();
					}
					_prevctx = localctx;
					{
					this.state = 21;
					this._errHandler.sync(this);
					switch ( this._interp.adaptivePredict(this._input, 1, this._ctx) ) {
					case 1:
						{
						localctx = new ExprContext(this, _parentctx, _parentState);
						this.pushNewRecursionContext(localctx, _startState, CohortRuleParser.RULE_expr);
						this.state = 15;
						if (!(this.precpred(this._ctx, 2))) {
							throw this.createFailedPredicateException("this.precpred(this._ctx, 2)");
						}
						this.state = 16;
						this.match(CohortRuleParser.AND);
						this.state = 17;
						this.expr(3);
						}
						break;
					case 2:
						{
						localctx = new ExprContext(this, _parentctx, _parentState);
						this.pushNewRecursionContext(localctx, _startState, CohortRuleParser.RULE_expr);
						this.state = 18;
						if (!(this.precpred(this._ctx, 1))) {
							throw this.createFailedPredicateException("this.precpred(this._ctx, 1)");
						}
						this.state = 19;
						this.match(CohortRuleParser.OR);
						this.state = 20;
						this.expr(2);
						}
						break;
					}
					}
				}
				this.state = 25;
				this._errHandler.sync(this);
				_alt = this._interp.adaptivePredict(this._input, 2, this._ctx);
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.unrollRecursionContexts(_parentctx);
		}
		return localctx;
	}
	// @RuleVersion(0)
	public term(): TermContext {
		let localctx: TermContext = new TermContext(this, this._ctx, this.state);
		this.enterRule(localctx, 2, CohortRuleParser.RULE_term);
		let _la: number;
		try {
			this.state = 43;
			this._errHandler.sync(this);
			switch (this._input.LA(1)) {
			case 2:
				this.enterOuterAlt(localctx, 1);
				{
				this.state = 26;
				this.match(CohortRuleParser.NUMBER);
				}
				break;
			case 3:
				this.enterOuterAlt(localctx, 2);
				{
				this.state = 27;
				this.match(CohortRuleParser.STRING);
				}
				break;
			case 4:
				this.enterOuterAlt(localctx, 3);
				{
				this.state = 28;
				this.match(CohortRuleParser.VARIABLE);
				}
				break;
			case 5:
				this.enterOuterAlt(localctx, 4);
				{
				this.state = 29;
				this.match(CohortRuleParser.BOOLEAN);
				}
				break;
			case 6:
				this.enterOuterAlt(localctx, 5);
				{
				this.state = 30;
				this.match(CohortRuleParser.NULL);
				}
				break;
			case 13:
				this.enterOuterAlt(localctx, 6);
				{
				this.state = 31;
				this.match(CohortRuleParser.FUNCTION_NAME);
				this.state = 32;
				this.match(CohortRuleParser.PAR_OPEN);
				this.state = 33;
				this.term();
				this.state = 38;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
				while (_la===1) {
					{
					{
					this.state = 34;
					this.match(CohortRuleParser.T__0);
					this.state = 35;
					this.term();
					}
					}
					this.state = 40;
					this._errHandler.sync(this);
					_la = this._input.LA(1);
				}
				this.state = 41;
				this.match(CohortRuleParser.PAR_CLOSE);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return localctx;
	}

	public sempred(localctx: RuleContext, ruleIndex: number, predIndex: number): boolean {
		switch (ruleIndex) {
		case 0:
			return this.expr_sempred(localctx as ExprContext, predIndex);
		}
		return true;
	}
	private expr_sempred(localctx: ExprContext, predIndex: number): boolean {
		switch (predIndex) {
		case 0:
			return this.precpred(this._ctx, 2);
		case 1:
			return this.precpred(this._ctx, 1);
		}
		return true;
	}

	public static readonly _serializedATN: number[] = [4,1,13,46,2,0,7,0,2,
	1,7,1,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,3,0,14,8,0,1,0,1,0,1,0,1,0,1,
	0,1,0,5,0,22,8,0,10,0,12,0,25,9,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
	1,5,1,37,8,1,10,1,12,1,40,9,1,1,1,1,1,3,1,44,8,1,1,1,0,1,0,2,0,2,0,0,52,
	0,13,1,0,0,0,2,43,1,0,0,0,4,5,6,0,-1,0,5,6,5,11,0,0,6,7,3,0,0,0,7,8,5,12,
	0,0,8,14,1,0,0,0,9,10,3,2,1,0,10,11,5,8,0,0,11,12,3,2,1,0,12,14,1,0,0,0,
	13,4,1,0,0,0,13,9,1,0,0,0,14,23,1,0,0,0,15,16,10,2,0,0,16,17,5,9,0,0,17,
	22,3,0,0,3,18,19,10,1,0,0,19,20,5,10,0,0,20,22,3,0,0,2,21,15,1,0,0,0,21,
	18,1,0,0,0,22,25,1,0,0,0,23,21,1,0,0,0,23,24,1,0,0,0,24,1,1,0,0,0,25,23,
	1,0,0,0,26,44,5,2,0,0,27,44,5,3,0,0,28,44,5,4,0,0,29,44,5,5,0,0,30,44,5,
	6,0,0,31,32,5,13,0,0,32,33,5,11,0,0,33,38,3,2,1,0,34,35,5,1,0,0,35,37,3,
	2,1,0,36,34,1,0,0,0,37,40,1,0,0,0,38,36,1,0,0,0,38,39,1,0,0,0,39,41,1,0,
	0,0,40,38,1,0,0,0,41,42,5,12,0,0,42,44,1,0,0,0,43,26,1,0,0,0,43,27,1,0,
	0,0,43,28,1,0,0,0,43,29,1,0,0,0,43,30,1,0,0,0,43,31,1,0,0,0,44,3,1,0,0,
	0,5,13,21,23,38,43];

	private static __ATN: ATN;
	public static get _ATN(): ATN {
		if (!CohortRuleParser.__ATN) {
			CohortRuleParser.__ATN = new ATNDeserializer().deserialize(CohortRuleParser._serializedATN);
		}

		return CohortRuleParser.__ATN;
	}


	static DecisionsToDFA = CohortRuleParser._ATN.decisionToState.map( (ds: DecisionState, index: number) => new DFA(ds, index) );

}

export class ExprContext extends ParserRuleContext {
	constructor(parser?: CohortRuleParser, parent?: ParserRuleContext, invokingState?: number) {
		super(parent, invokingState);
    	this.parser = parser;
	}
	public PAR_OPEN(): TerminalNode {
		return this.getToken(CohortRuleParser.PAR_OPEN, 0);
	}
	public expr_list(): ExprContext[] {
		return this.getTypedRuleContexts(ExprContext) as ExprContext[];
	}
	public expr(i: number): ExprContext {
		return this.getTypedRuleContext(ExprContext, i) as ExprContext;
	}
	public PAR_CLOSE(): TerminalNode {
		return this.getToken(CohortRuleParser.PAR_CLOSE, 0);
	}
	public term_list(): TermContext[] {
		return this.getTypedRuleContexts(TermContext) as TermContext[];
	}
	public term(i: number): TermContext {
		return this.getTypedRuleContext(TermContext, i) as TermContext;
	}
	public OPERATOR(): TerminalNode {
		return this.getToken(CohortRuleParser.OPERATOR, 0);
	}
	public AND(): TerminalNode {
		return this.getToken(CohortRuleParser.AND, 0);
	}
	public OR(): TerminalNode {
		return this.getToken(CohortRuleParser.OR, 0);
	}
    public get ruleIndex(): number {
    	return CohortRuleParser.RULE_expr;
	}
	// @Override
	public accept<Result>(visitor: CohortRuleVisitor<Result>): Result {
		if (visitor.visitExpr) {
			return visitor.visitExpr(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class TermContext extends ParserRuleContext {
	constructor(parser?: CohortRuleParser, parent?: ParserRuleContext, invokingState?: number) {
		super(parent, invokingState);
    	this.parser = parser;
	}
	public NUMBER(): TerminalNode {
		return this.getToken(CohortRuleParser.NUMBER, 0);
	}
	public STRING(): TerminalNode {
		return this.getToken(CohortRuleParser.STRING, 0);
	}
	public VARIABLE(): TerminalNode {
		return this.getToken(CohortRuleParser.VARIABLE, 0);
	}
	public BOOLEAN(): TerminalNode {
		return this.getToken(CohortRuleParser.BOOLEAN, 0);
	}
	public NULL(): TerminalNode {
		return this.getToken(CohortRuleParser.NULL, 0);
	}
	public FUNCTION_NAME(): TerminalNode {
		return this.getToken(CohortRuleParser.FUNCTION_NAME, 0);
	}
	public PAR_OPEN(): TerminalNode {
		return this.getToken(CohortRuleParser.PAR_OPEN, 0);
	}
	public term_list(): TermContext[] {
		return this.getTypedRuleContexts(TermContext) as TermContext[];
	}
	public term(i: number): TermContext {
		return this.getTypedRuleContext(TermContext, i) as TermContext;
	}
	public PAR_CLOSE(): TerminalNode {
		return this.getToken(CohortRuleParser.PAR_CLOSE, 0);
	}
    public get ruleIndex(): number {
    	return CohortRuleParser.RULE_term;
	}
	// @Override
	public accept<Result>(visitor: CohortRuleVisitor<Result>): Result {
		if (visitor.visitTerm) {
			return visitor.visitTerm(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}
