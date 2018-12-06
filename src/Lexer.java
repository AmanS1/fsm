import java.util.ArrayList;
import java.util.List;

/**
 * @author Aman Sariyev
 */
public class Lexer {
	public static List<String> tokenizeRegExp(String regExp) {
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
					tokens.add(regExp.substring(left, i));
				}
			} else if (bracketStack == 0) {
				tokens.add("" + regExp.charAt(i));
			}
		}
		return tokens;
	}
}
