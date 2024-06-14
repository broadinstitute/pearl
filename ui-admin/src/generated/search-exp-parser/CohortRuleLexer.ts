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
	public static readonly FUNCTION_NAME = 13;
	public static readonly EOF = Token.EOF;

	public static readonly channelNames: string[] = [ "DEFAULT_TOKEN_CHANNEL", "HIDDEN" ];
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
	public static readonly modeNames: string[] = [ "DEFAULT_MODE", ];

	public static readonly ruleNames: string[] = [
		"T__0", "NUMBER", "STRING", "VARIABLE", "BOOLEAN", "NULL", "WS", "OPERATOR", 
		"AND", "OR", "PAR_OPEN", "PAR_CLOSE", "FUNCTION_NAME",
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

	public static readonly _serializedATN: number[] = [4,0,13,116,6,-1,2,0,
	7,0,2,1,7,1,2,2,7,2,2,3,7,3,2,4,7,4,2,5,7,5,2,6,7,6,2,7,7,7,2,8,7,8,2,9,
	7,9,2,10,7,10,2,11,7,11,2,12,7,12,1,0,1,0,1,1,4,1,31,8,1,11,1,12,1,32,1,
	1,1,1,4,1,37,8,1,11,1,12,1,38,3,1,41,8,1,1,2,1,2,5,2,45,8,2,10,2,12,2,48,
	9,2,1,2,1,2,1,3,1,3,4,3,54,8,3,11,3,12,3,55,1,3,1,3,1,4,1,4,1,4,1,4,1,4,
	1,4,1,4,1,4,1,4,3,4,69,8,4,1,5,1,5,1,5,1,5,1,5,1,6,4,6,77,8,6,11,6,12,6,
	78,1,6,1,6,1,7,1,7,1,7,1,7,1,7,1,7,1,7,1,7,1,7,1,7,1,7,1,7,1,7,1,7,1,7,
	1,7,3,7,99,8,7,1,8,1,8,1,8,1,8,1,9,1,9,1,9,1,10,1,10,1,11,1,11,1,12,4,12,
	113,8,12,11,12,12,12,114,0,0,13,1,1,3,2,5,3,7,4,9,5,11,6,13,7,15,8,17,9,
	19,10,21,11,23,12,25,13,1,0,6,1,0,48,57,4,0,10,10,13,13,39,39,92,92,5,0,
	46,46,48,57,65,90,95,95,97,122,3,0,9,10,13,13,32,32,2,0,60,60,62,62,3,0,
	65,90,95,95,97,122,128,0,1,1,0,0,0,0,3,1,0,0,0,0,5,1,0,0,0,0,7,1,0,0,0,
	0,9,1,0,0,0,0,11,1,0,0,0,0,13,1,0,0,0,0,15,1,0,0,0,0,17,1,0,0,0,0,19,1,
	0,0,0,0,21,1,0,0,0,0,23,1,0,0,0,0,25,1,0,0,0,1,27,1,0,0,0,3,30,1,0,0,0,
	5,42,1,0,0,0,7,51,1,0,0,0,9,68,1,0,0,0,11,70,1,0,0,0,13,76,1,0,0,0,15,98,
	1,0,0,0,17,100,1,0,0,0,19,104,1,0,0,0,21,107,1,0,0,0,23,109,1,0,0,0,25,
	112,1,0,0,0,27,28,5,44,0,0,28,2,1,0,0,0,29,31,7,0,0,0,30,29,1,0,0,0,31,
	32,1,0,0,0,32,30,1,0,0,0,32,33,1,0,0,0,33,40,1,0,0,0,34,36,5,46,0,0,35,
	37,7,0,0,0,36,35,1,0,0,0,37,38,1,0,0,0,38,36,1,0,0,0,38,39,1,0,0,0,39,41,
	1,0,0,0,40,34,1,0,0,0,40,41,1,0,0,0,41,4,1,0,0,0,42,46,5,39,0,0,43,45,8,
	1,0,0,44,43,1,0,0,0,45,48,1,0,0,0,46,44,1,0,0,0,46,47,1,0,0,0,47,49,1,0,
	0,0,48,46,1,0,0,0,49,50,5,39,0,0,50,6,1,0,0,0,51,53,5,123,0,0,52,54,7,2,
	0,0,53,52,1,0,0,0,54,55,1,0,0,0,55,53,1,0,0,0,55,56,1,0,0,0,56,57,1,0,0,
	0,57,58,5,125,0,0,58,8,1,0,0,0,59,60,5,116,0,0,60,61,5,114,0,0,61,62,5,
	117,0,0,62,69,5,101,0,0,63,64,5,102,0,0,64,65,5,97,0,0,65,66,5,108,0,0,
	66,67,5,115,0,0,67,69,5,101,0,0,68,59,1,0,0,0,68,63,1,0,0,0,69,10,1,0,0,
	0,70,71,5,110,0,0,71,72,5,117,0,0,72,73,5,108,0,0,73,74,5,108,0,0,74,12,
	1,0,0,0,75,77,7,3,0,0,76,75,1,0,0,0,77,78,1,0,0,0,78,76,1,0,0,0,78,79,1,
	0,0,0,79,80,1,0,0,0,80,81,6,6,0,0,81,14,1,0,0,0,82,99,5,61,0,0,83,84,5,
	33,0,0,84,99,5,61,0,0,85,99,7,4,0,0,86,87,5,62,0,0,87,99,5,61,0,0,88,89,
	5,60,0,0,89,99,5,61,0,0,90,91,5,99,0,0,91,92,5,111,0,0,92,93,5,110,0,0,
	93,94,5,116,0,0,94,95,5,97,0,0,95,96,5,105,0,0,96,97,5,110,0,0,97,99,5,
	115,0,0,98,82,1,0,0,0,98,83,1,0,0,0,98,85,1,0,0,0,98,86,1,0,0,0,98,88,1,
	0,0,0,98,90,1,0,0,0,99,16,1,0,0,0,100,101,5,97,0,0,101,102,5,110,0,0,102,
	103,5,100,0,0,103,18,1,0,0,0,104,105,5,111,0,0,105,106,5,114,0,0,106,20,
	1,0,0,0,107,108,5,40,0,0,108,22,1,0,0,0,109,110,5,41,0,0,110,24,1,0,0,0,
	111,113,7,5,0,0,112,111,1,0,0,0,113,114,1,0,0,0,114,112,1,0,0,0,114,115,
	1,0,0,0,115,26,1,0,0,0,11,0,32,38,40,46,53,55,68,78,98,114,1,6,0,0];

	private static __ATN: ATN;
	public static get _ATN(): ATN {
		if (!CohortRuleLexer.__ATN) {
			CohortRuleLexer.__ATN = new ATNDeserializer().deserialize(CohortRuleLexer._serializedATN);
		}

		return CohortRuleLexer.__ATN;
	}


	static DecisionsToDFA = CohortRuleLexer._ATN.decisionToState.map( (ds: DecisionState, index: number) => new DFA(ds, index) );
}