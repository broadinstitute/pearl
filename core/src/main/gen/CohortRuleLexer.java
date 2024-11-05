// Generated from /Users/barker/broad/juniper/core/src/main/antlr/bio/terra/pearl/core/antlr/CohortRule.g4 by ANTLR 4.13.2
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class CohortRuleLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, NUMBER=3, STRING=4, VAR_STUDY=5, VAR_MODEL=6, VAR_ARGUMENTS=7, 
		VAR_OPEN=8, VAR_CLOSE=9, BOOLEAN=10, NULL=11, WS=12, OPERATOR=13, AND=14, 
		OR=15, PAR_OPEN=16, PAR_CLOSE=17, NOT=18, INCLUDE=19, FUNCTION_NAME=20;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "NUMBER", "STRING", "VAR_STUDY", "VAR_MODEL", "VAR_ARGUMENTS", 
			"VAR_OPEN", "VAR_CLOSE", "BOOLEAN", "NULL", "WS", "OPERATOR", "AND", 
			"OR", "PAR_OPEN", "PAR_CLOSE", "NOT", "INCLUDE", "FUNCTION_NAME"
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


	public CohortRuleLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "CohortRule.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\u0004\u0000\u0014\u009f\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002"+
		"\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002"+
		"\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002"+
		"\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002"+
		"\u000b\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e"+
		"\u0002\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011"+
		"\u0002\u0012\u0007\u0012\u0002\u0013\u0007\u0013\u0001\u0000\u0001\u0000"+
		"\u0001\u0001\u0001\u0001\u0001\u0002\u0004\u0002/\b\u0002\u000b\u0002"+
		"\f\u00020\u0001\u0002\u0001\u0002\u0004\u00025\b\u0002\u000b\u0002\f\u0002"+
		"6\u0003\u00029\b\u0002\u0001\u0003\u0001\u0003\u0005\u0003=\b\u0003\n"+
		"\u0003\f\u0003@\t\u0003\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0004\u0004H\b\u0004\u000b\u0004\f\u0004I\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0004\u0005P\b\u0005\u000b"+
		"\u0005\f\u0005Q\u0001\u0006\u0004\u0006U\b\u0006\u000b\u0006\f\u0006V"+
		"\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\tf\b\t\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\u000b\u0004\u000bn\b\u000b\u000b\u000b"+
		"\f\u000bo\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0003\f\u0084\b\f\u0001\r\u0001\r\u0001\r\u0001\r\u0001"+
		"\u000e\u0001\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001\u0010\u0001"+
		"\u0010\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001"+
		"\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0013\u0004"+
		"\u0013\u009c\b\u0013\u000b\u0013\f\u0013\u009d\u0000\u0000\u0014\u0001"+
		"\u0001\u0003\u0002\u0005\u0003\u0007\u0004\t\u0005\u000b\u0006\r\u0007"+
		"\u000f\b\u0011\t\u0013\n\u0015\u000b\u0017\f\u0019\r\u001b\u000e\u001d"+
		"\u000f\u001f\u0010!\u0011#\u0012%\u0013\'\u0014\u0001\u0000\u0007\u0001"+
		"\u000009\u0004\u0000\n\n\r\r\'\'\\\\\u0004\u000009AZ__az\u0005\u0000."+
		".09AZ__az\u0003\u0000\t\n\r\r  \u0002\u0000<<>>\u0003\u0000AZ__az\u00ad"+
		"\u0000\u0001\u0001\u0000\u0000\u0000\u0000\u0003\u0001\u0000\u0000\u0000"+
		"\u0000\u0005\u0001\u0000\u0000\u0000\u0000\u0007\u0001\u0000\u0000\u0000"+
		"\u0000\t\u0001\u0000\u0000\u0000\u0000\u000b\u0001\u0000\u0000\u0000\u0000"+
		"\r\u0001\u0000\u0000\u0000\u0000\u000f\u0001\u0000\u0000\u0000\u0000\u0011"+
		"\u0001\u0000\u0000\u0000\u0000\u0013\u0001\u0000\u0000\u0000\u0000\u0015"+
		"\u0001\u0000\u0000\u0000\u0000\u0017\u0001\u0000\u0000\u0000\u0000\u0019"+
		"\u0001\u0000\u0000\u0000\u0000\u001b\u0001\u0000\u0000\u0000\u0000\u001d"+
		"\u0001\u0000\u0000\u0000\u0000\u001f\u0001\u0000\u0000\u0000\u0000!\u0001"+
		"\u0000\u0000\u0000\u0000#\u0001\u0000\u0000\u0000\u0000%\u0001\u0000\u0000"+
		"\u0000\u0000\'\u0001\u0000\u0000\u0000\u0001)\u0001\u0000\u0000\u0000"+
		"\u0003+\u0001\u0000\u0000\u0000\u0005.\u0001\u0000\u0000\u0000\u0007:"+
		"\u0001\u0000\u0000\u0000\tC\u0001\u0000\u0000\u0000\u000bO\u0001\u0000"+
		"\u0000\u0000\rT\u0001\u0000\u0000\u0000\u000fX\u0001\u0000\u0000\u0000"+
		"\u0011Z\u0001\u0000\u0000\u0000\u0013e\u0001\u0000\u0000\u0000\u0015g"+
		"\u0001\u0000\u0000\u0000\u0017m\u0001\u0000\u0000\u0000\u0019\u0083\u0001"+
		"\u0000\u0000\u0000\u001b\u0085\u0001\u0000\u0000\u0000\u001d\u0089\u0001"+
		"\u0000\u0000\u0000\u001f\u008c\u0001\u0000\u0000\u0000!\u008e\u0001\u0000"+
		"\u0000\u0000#\u0090\u0001\u0000\u0000\u0000%\u0092\u0001\u0000\u0000\u0000"+
		"\'\u009b\u0001\u0000\u0000\u0000)*\u0005.\u0000\u0000*\u0002\u0001\u0000"+
		"\u0000\u0000+,\u0005,\u0000\u0000,\u0004\u0001\u0000\u0000\u0000-/\u0007"+
		"\u0000\u0000\u0000.-\u0001\u0000\u0000\u0000/0\u0001\u0000\u0000\u0000"+
		"0.\u0001\u0000\u0000\u000001\u0001\u0000\u0000\u000018\u0001\u0000\u0000"+
		"\u000024\u0005.\u0000\u000035\u0007\u0000\u0000\u000043\u0001\u0000\u0000"+
		"\u000056\u0001\u0000\u0000\u000064\u0001\u0000\u0000\u000067\u0001\u0000"+
		"\u0000\u000079\u0001\u0000\u0000\u000082\u0001\u0000\u0000\u000089\u0001"+
		"\u0000\u0000\u00009\u0006\u0001\u0000\u0000\u0000:>\u0005\'\u0000\u0000"+
		";=\b\u0001\u0000\u0000<;\u0001\u0000\u0000\u0000=@\u0001\u0000\u0000\u0000"+
		"><\u0001\u0000\u0000\u0000>?\u0001\u0000\u0000\u0000?A\u0001\u0000\u0000"+
		"\u0000@>\u0001\u0000\u0000\u0000AB\u0005\'\u0000\u0000B\b\u0001\u0000"+
		"\u0000\u0000CD\u0005[\u0000\u0000DE\u0005\"\u0000\u0000EG\u0001\u0000"+
		"\u0000\u0000FH\u0007\u0002\u0000\u0000GF\u0001\u0000\u0000\u0000HI\u0001"+
		"\u0000\u0000\u0000IG\u0001\u0000\u0000\u0000IJ\u0001\u0000\u0000\u0000"+
		"JK\u0001\u0000\u0000\u0000KL\u0005\"\u0000\u0000LM\u0005]\u0000\u0000"+
		"M\n\u0001\u0000\u0000\u0000NP\u0007\u0002\u0000\u0000ON\u0001\u0000\u0000"+
		"\u0000PQ\u0001\u0000\u0000\u0000QO\u0001\u0000\u0000\u0000QR\u0001\u0000"+
		"\u0000\u0000R\f\u0001\u0000\u0000\u0000SU\u0007\u0003\u0000\u0000TS\u0001"+
		"\u0000\u0000\u0000UV\u0001\u0000\u0000\u0000VT\u0001\u0000\u0000\u0000"+
		"VW\u0001\u0000\u0000\u0000W\u000e\u0001\u0000\u0000\u0000XY\u0005{\u0000"+
		"\u0000Y\u0010\u0001\u0000\u0000\u0000Z[\u0005}\u0000\u0000[\u0012\u0001"+
		"\u0000\u0000\u0000\\]\u0005t\u0000\u0000]^\u0005r\u0000\u0000^_\u0005"+
		"u\u0000\u0000_f\u0005e\u0000\u0000`a\u0005f\u0000\u0000ab\u0005a\u0000"+
		"\u0000bc\u0005l\u0000\u0000cd\u0005s\u0000\u0000df\u0005e\u0000\u0000"+
		"e\\\u0001\u0000\u0000\u0000e`\u0001\u0000\u0000\u0000f\u0014\u0001\u0000"+
		"\u0000\u0000gh\u0005n\u0000\u0000hi\u0005u\u0000\u0000ij\u0005l\u0000"+
		"\u0000jk\u0005l\u0000\u0000k\u0016\u0001\u0000\u0000\u0000ln\u0007\u0004"+
		"\u0000\u0000ml\u0001\u0000\u0000\u0000no\u0001\u0000\u0000\u0000om\u0001"+
		"\u0000\u0000\u0000op\u0001\u0000\u0000\u0000pq\u0001\u0000\u0000\u0000"+
		"qr\u0006\u000b\u0000\u0000r\u0018\u0001\u0000\u0000\u0000s\u0084\u0005"+
		"=\u0000\u0000tu\u0005!\u0000\u0000u\u0084\u0005=\u0000\u0000v\u0084\u0007"+
		"\u0005\u0000\u0000wx\u0005>\u0000\u0000x\u0084\u0005=\u0000\u0000yz\u0005"+
		"<\u0000\u0000z\u0084\u0005=\u0000\u0000{|\u0005c\u0000\u0000|}\u0005o"+
		"\u0000\u0000}~\u0005n\u0000\u0000~\u007f\u0005t\u0000\u0000\u007f\u0080"+
		"\u0005a\u0000\u0000\u0080\u0081\u0005i\u0000\u0000\u0081\u0082\u0005n"+
		"\u0000\u0000\u0082\u0084\u0005s\u0000\u0000\u0083s\u0001\u0000\u0000\u0000"+
		"\u0083t\u0001\u0000\u0000\u0000\u0083v\u0001\u0000\u0000\u0000\u0083w"+
		"\u0001\u0000\u0000\u0000\u0083y\u0001\u0000\u0000\u0000\u0083{\u0001\u0000"+
		"\u0000\u0000\u0084\u001a\u0001\u0000\u0000\u0000\u0085\u0086\u0005a\u0000"+
		"\u0000\u0086\u0087\u0005n\u0000\u0000\u0087\u0088\u0005d\u0000\u0000\u0088"+
		"\u001c\u0001\u0000\u0000\u0000\u0089\u008a\u0005o\u0000\u0000\u008a\u008b"+
		"\u0005r\u0000\u0000\u008b\u001e\u0001\u0000\u0000\u0000\u008c\u008d\u0005"+
		"(\u0000\u0000\u008d \u0001\u0000\u0000\u0000\u008e\u008f\u0005)\u0000"+
		"\u0000\u008f\"\u0001\u0000\u0000\u0000\u0090\u0091\u0005!\u0000\u0000"+
		"\u0091$\u0001\u0000\u0000\u0000\u0092\u0093\u0005i\u0000\u0000\u0093\u0094"+
		"\u0005n\u0000\u0000\u0094\u0095\u0005c\u0000\u0000\u0095\u0096\u0005l"+
		"\u0000\u0000\u0096\u0097\u0005u\u0000\u0000\u0097\u0098\u0005d\u0000\u0000"+
		"\u0098\u0099\u0005e\u0000\u0000\u0099&\u0001\u0000\u0000\u0000\u009a\u009c"+
		"\u0007\u0006\u0000\u0000\u009b\u009a\u0001\u0000\u0000\u0000\u009c\u009d"+
		"\u0001\u0000\u0000\u0000\u009d\u009b\u0001\u0000\u0000\u0000\u009d\u009e"+
		"\u0001\u0000\u0000\u0000\u009e(\u0001\u0000\u0000\u0000\r\u0000068>IQ"+
		"TVeo\u0083\u009d\u0001\u0006\u0000\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}