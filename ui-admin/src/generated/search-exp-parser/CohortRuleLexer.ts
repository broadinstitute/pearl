// Generated from bio/terra/pearl/core/antlr/CohortRule.g4 by ANTLR 4.13.1
// noinspection ES6UnusedImports,JSUnusedGlobalSymbols,JSUnusedLocalSymbols
import {
	ATN,
	ATNDeserializer,
	CharStream,
	DecisionState, DFA,
	Lexer,
	LexerATNSimulator,
	RuleContext,
	PredictionContextCache,
	Token
} from "antlr4";
export default class CohortRuleLexer extends Lexer {
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
	public static readonly NOT = 13;
	public static readonly INCLUDE = 14;
	public static readonly FUNCTION_NAME = 15;
	public static readonly EOF = Token.EOF;

	public static readonly channelNames: string[] = [ "DEFAULT_TOKEN_CHANNEL", "HIDDEN" ];
	public static readonly literalNames: (string | null)[] = [ null, "','", 
                                                            null, null, 
                                                            null, null, 
                                                            "'null'", null, 
                                                            null, "'and'", 
                                                            "'or'", "'('", 
                                                            "')'", "'!'", 
                                                            "'include'" ];
	public static readonly symbolicNames: (string | null)[] = [ null, null, 
                                                             "NUMBER", "STRING", 
                                                             "VARIABLE", 
                                                             "BOOLEAN", 
                                                             "NULL", "WS", 
                                                             "OPERATOR", 
                                                             "AND", "OR", 
                                                             "PAR_OPEN", 
                                                             "PAR_CLOSE", 
                                                             "NOT", "INCLUDE", 
                                                             "FUNCTION_NAME" ];
	public static readonly modeNames: string[] = [ "DEFAULT_MODE", ];

	public static readonly ruleNames: string[] = [
		"T__0", "NUMBER", "STRING", "VARIABLE", "BOOLEAN", "NULL", "WS", "OPERATOR", 
		"AND", "OR", "PAR_OPEN", "PAR_CLOSE", "NOT", "INCLUDE", "FUNCTION_NAME",
	];


	constructor(input: CharStream) {
		super(input);
		this._interp = new LexerATNSimulator(this, CohortRuleLexer._ATN, CohortRuleLexer.DecisionsToDFA, new PredictionContextCache());
	}

	public get grammarFileName(): string { return "CohortRule.g4"; }

	public get literalNames(): (string | null)[] { return CohortRuleLexer.literalNames; }
	public get symbolicNames(): (string | null)[] { return CohortRuleLexer.symbolicNames; }
	public get ruleNames(): string[] { return CohortRuleLexer.ruleNames; }

	public get serializedATN(): number[] { return CohortRuleLexer._serializedATN; }

	public get channelNames(): string[] { return CohortRuleLexer.channelNames; }

	public get modeNames(): string[] { return CohortRuleLexer.modeNames; }

	public static readonly _serializedATN: number[] = [4,0,15,148,6,-1,2,0,
	7,0,2,1,7,1,2,2,7,2,2,3,7,3,2,4,7,4,2,5,7,5,2,6,7,6,2,7,7,7,2,8,7,8,2,9,
	7,9,2,10,7,10,2,11,7,11,2,12,7,12,2,13,7,13,2,14,7,14,1,0,1,0,1,1,4,1,35,
	8,1,11,1,12,1,36,1,1,1,1,4,1,41,8,1,11,1,12,1,42,3,1,45,8,1,1,2,1,2,5,2,
	49,8,2,10,2,12,2,52,9,2,1,2,1,2,1,3,1,3,4,3,58,8,3,11,3,12,3,59,1,3,1,3,
	1,3,1,3,4,3,66,8,3,11,3,12,3,67,1,3,1,3,1,3,3,3,73,8,3,1,3,4,3,76,8,3,11,
	3,12,3,77,1,3,1,3,1,4,1,4,1,4,1,4,1,4,1,4,1,4,1,4,1,4,3,4,91,8,4,1,5,1,
	5,1,5,1,5,1,5,1,6,4,6,99,8,6,11,6,12,6,100,1,6,1,6,1,7,1,7,1,7,1,7,1,7,
	1,7,1,7,1,7,1,7,1,7,1,7,1,7,1,7,1,7,1,7,1,7,3,7,121,8,7,1,8,1,8,1,8,1,8,
	1,9,1,9,1,9,1,10,1,10,1,11,1,11,1,12,1,12,1,13,1,13,1,13,1,13,1,13,1,13,
	1,13,1,13,1,14,4,14,145,8,14,11,14,12,14,146,0,0,15,1,1,3,2,5,3,7,4,9,5,
	11,6,13,7,15,8,17,9,19,10,21,11,23,12,25,13,27,14,29,15,1,0,7,1,0,48,57,
	4,0,10,10,13,13,39,39,92,92,4,0,48,57,65,90,95,95,97,122,5,0,46,46,48,57,
	65,90,95,95,97,122,3,0,9,10,13,13,32,32,2,0,60,60,62,62,3,0,65,90,95,95,
	97,122,163,0,1,1,0,0,0,0,3,1,0,0,0,0,5,1,0,0,0,0,7,1,0,0,0,0,9,1,0,0,0,
	0,11,1,0,0,0,0,13,1,0,0,0,0,15,1,0,0,0,0,17,1,0,0,0,0,19,1,0,0,0,0,21,1,
	0,0,0,0,23,1,0,0,0,0,25,1,0,0,0,0,27,1,0,0,0,0,29,1,0,0,0,1,31,1,0,0,0,
	3,34,1,0,0,0,5,46,1,0,0,0,7,55,1,0,0,0,9,90,1,0,0,0,11,92,1,0,0,0,13,98,
	1,0,0,0,15,120,1,0,0,0,17,122,1,0,0,0,19,126,1,0,0,0,21,129,1,0,0,0,23,
	131,1,0,0,0,25,133,1,0,0,0,27,135,1,0,0,0,29,144,1,0,0,0,31,32,5,44,0,0,
	32,2,1,0,0,0,33,35,7,0,0,0,34,33,1,0,0,0,35,36,1,0,0,0,36,34,1,0,0,0,36,
	37,1,0,0,0,37,44,1,0,0,0,38,40,5,46,0,0,39,41,7,0,0,0,40,39,1,0,0,0,41,
	42,1,0,0,0,42,40,1,0,0,0,42,43,1,0,0,0,43,45,1,0,0,0,44,38,1,0,0,0,44,45,
	1,0,0,0,45,4,1,0,0,0,46,50,5,39,0,0,47,49,8,1,0,0,48,47,1,0,0,0,49,52,1,
	0,0,0,50,48,1,0,0,0,50,51,1,0,0,0,51,53,1,0,0,0,52,50,1,0,0,0,53,54,5,39,
	0,0,54,6,1,0,0,0,55,72,5,123,0,0,56,58,7,2,0,0,57,56,1,0,0,0,58,59,1,0,
	0,0,59,57,1,0,0,0,59,60,1,0,0,0,60,61,1,0,0,0,61,62,5,91,0,0,62,63,5,34,
	0,0,63,65,1,0,0,0,64,66,7,2,0,0,65,64,1,0,0,0,66,67,1,0,0,0,67,65,1,0,0,
	0,67,68,1,0,0,0,68,69,1,0,0,0,69,70,5,34,0,0,70,71,5,93,0,0,71,73,5,46,
	0,0,72,57,1,0,0,0,72,73,1,0,0,0,73,75,1,0,0,0,74,76,7,3,0,0,75,74,1,0,0,
	0,76,77,1,0,0,0,77,75,1,0,0,0,77,78,1,0,0,0,78,79,1,0,0,0,79,80,5,125,0,
	0,80,8,1,0,0,0,81,82,5,116,0,0,82,83,5,114,0,0,83,84,5,117,0,0,84,91,5,
	101,0,0,85,86,5,102,0,0,86,87,5,97,0,0,87,88,5,108,0,0,88,89,5,115,0,0,
	89,91,5,101,0,0,90,81,1,0,0,0,90,85,1,0,0,0,91,10,1,0,0,0,92,93,5,110,0,
	0,93,94,5,117,0,0,94,95,5,108,0,0,95,96,5,108,0,0,96,12,1,0,0,0,97,99,7,
	4,0,0,98,97,1,0,0,0,99,100,1,0,0,0,100,98,1,0,0,0,100,101,1,0,0,0,101,102,
	1,0,0,0,102,103,6,6,0,0,103,14,1,0,0,0,104,121,5,61,0,0,105,106,5,33,0,
	0,106,121,5,61,0,0,107,121,7,5,0,0,108,109,5,62,0,0,109,121,5,61,0,0,110,
	111,5,60,0,0,111,121,5,61,0,0,112,113,5,99,0,0,113,114,5,111,0,0,114,115,
	5,110,0,0,115,116,5,116,0,0,116,117,5,97,0,0,117,118,5,105,0,0,118,119,
	5,110,0,0,119,121,5,115,0,0,120,104,1,0,0,0,120,105,1,0,0,0,120,107,1,0,
	0,0,120,108,1,0,0,0,120,110,1,0,0,0,120,112,1,0,0,0,121,16,1,0,0,0,122,
	123,5,97,0,0,123,124,5,110,0,0,124,125,5,100,0,0,125,18,1,0,0,0,126,127,
	5,111,0,0,127,128,5,114,0,0,128,20,1,0,0,0,129,130,5,40,0,0,130,22,1,0,
	0,0,131,132,5,41,0,0,132,24,1,0,0,0,133,134,5,33,0,0,134,26,1,0,0,0,135,
	136,5,105,0,0,136,137,5,110,0,0,137,138,5,99,0,0,138,139,5,108,0,0,139,
	140,5,117,0,0,140,141,5,100,0,0,141,142,5,101,0,0,142,28,1,0,0,0,143,145,
	7,6,0,0,144,143,1,0,0,0,145,146,1,0,0,0,146,144,1,0,0,0,146,147,1,0,0,0,
	147,30,1,0,0,0,14,0,36,42,44,50,59,67,72,75,77,90,100,120,146,1,6,0,0];

	private static __ATN: ATN;
	public static get _ATN(): ATN {
		if (!CohortRuleLexer.__ATN) {
			CohortRuleLexer.__ATN = new ATNDeserializer().deserialize(CohortRuleLexer._serializedATN);
		}

		return CohortRuleLexer.__ATN;
	}


	static DecisionsToDFA = CohortRuleLexer._ATN.decisionToState.map( (ds: DecisionState, index: number) => new DFA(ds, index) );
}