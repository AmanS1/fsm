import java.util.ArrayList;
import java.util.List;

/**
 * @author Aman Sariyev
 */
public class Lexer {
	public static List<String> tokenizeRegExp(String regExp, boolean leaveParentheses) {
		/*
		Brackets		First Priority
		Kleene Star		Second Priority
		Concatenation	Third Priority
		Alternation		Fourth Priority
		*/
		List<String> tokens = new ArrayList<>();
		int left = 0, bracketStack = 0;
		for (int i = 0; i < regExp.length(); i++) {
			if (regExp.charAt(i) == '(') {
				if (bracketStack == 0) left = i + 1;
				bracketStack++;
			} else if (regExp.charAt(i) == ')') {
				bracketStack--;
				if (bracketStack == 0) {
					if (leaveParentheses) tokens.add(String.format("(%s)", regExp.substring(left, i)));
					else tokens.add(regExp.substring(left, i));
				}
			} else if (bracketStack == 0) {
				tokens.add("" + regExp.charAt(i));
			}
		}
		return tokens;
	}

	public static List<String> tokenizeDesc(String text) {
		List<String> tokens = new ArrayList<>();
		StringBuilder cur = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == '#' || text.charAt(i) == ':' || text.charAt(i) == '>') {
				if (cur.length() > 0) tokens.add(cur.toString());
				cur = new StringBuilder();
				tokens.add(Character.toString(text.charAt(i)));
			} else if (text.charAt(i) == '\r' || text.charAt(i) == '\n' || text.charAt(i) == ' ') {
				if (cur.length() > 0) tokens.add(cur.toString());
				cur = new StringBuilder();
			} else cur.append(text.charAt(i));
		}
		if (cur.length() > 0) tokens.add(cur.toString());
		return tokens;
	}

	public static List<Integer> tokenizeDescPos(String text) {
		List<Integer> tokensPos = new ArrayList<>();
		StringBuilder cur = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == '#' || text.charAt(i) == ':' || text.charAt(i) == '>') {
				if (cur.length() > 0) tokensPos.add(i - cur.length());
				cur = new StringBuilder();
				tokensPos.add(i);
			} else if (text.charAt(i) == '\r' || text.charAt(i) == '\n' || text.charAt(i) == ' ') {
				if (cur.length() > 0) tokensPos.add(i - cur.length());
				cur = new StringBuilder();
			} else cur.append(text.charAt(i));
		}
		if (cur.length() > 0) tokensPos.add(text.length() - cur.length());
		tokensPos.add(text.length());
		return tokensPos;
	}
}
