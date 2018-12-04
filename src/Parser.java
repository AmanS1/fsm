import java.util.List;

/**
 * @author Aman Sariyev
 */
public class Parser {
	public static Automaton convertTokensToAutomaton(List<String> tokens) {
		Automaton aut, aut1, aut2;
		int posAlt = -1;
		if (tokens.size() == 0) return null;
		if (tokens.size() == 1) {
			return AutomatonUtils.convertRegExpToAutomaton(tokens.get(0));
		}
		for (int i = 0; i < tokens.size(); i++) {
			if (tokens.get(i).equals("|")) {
				posAlt = i;
				break;
			}
		}
		if (posAlt < 0) {
			aut = AutomatonUtils.convertRegExpToAutomaton(tokens.get(0));
			if (tokens.size() > 1 && tokens.get(1).equals("*")) {
				aut1 = convertTokensToAutomaton(tokens.subList(2, tokens.size()));
				aut.getFinish().addTransition(aut.getStart(), '$');
				aut.getFinish().setAccepting(false);
				aut.getStart().setAccepting(true);
				aut.setFinish(aut.getStart());
			} else {
				aut1 = convertTokensToAutomaton(tokens.subList(1, tokens.size()));
			}
			if (aut1 != null) {
				aut.getFinish().addTransition(aut1.getStart(), '$');
				aut.getFinish().setAccepting(false);
				aut.setFinish(aut1.getFinish());
			}
		} else {
			aut1 = convertTokensToAutomaton(tokens.subList(0, posAlt));
			aut2 = convertTokensToAutomaton(tokens.subList(posAlt + 1, tokens.size()));
			aut = new Automaton();
			aut.getStart().addTransition(aut1.getStart(), '$');
			aut.getStart().addTransition(aut2.getStart(), '$');
			aut1.getFinish().addTransition(aut.getFinish(), '$');
			aut2.getFinish().addTransition(aut.getFinish(), '$');
			aut1.getFinish().setAccepting(false);
			aut2.getFinish().setAccepting(false);
		}
		return aut;
	}

	public static void checkRegExp(String regExp) {
		int bracketStack = 0;
		try {
			for (int i = 0; i < regExp.length(); i++) {
				if (i + 1 < regExp.length() && regExp.charAt(i) == '*' && regExp.charAt(i + 1) == '*')
					throw new Exception("Empty subexpression at index " + (i + 1));
				if (i + 1 < regExp.length() && regExp.charAt(i) == '*' && regExp.charAt(i + 1) == '*')
					throw new Exception("Kleene Star tautology at index " + (i + 1));
				if (i + 1 < regExp.length() && (regExp.charAt(i) == '(' || regExp.charAt(i) == '|') && regExp.charAt(i + 1) == '*')
					throw new Exception("Empty subexpression with Kleene Star at index " + (i + 1));
				if (i + 1 < regExp.length() && (regExp.charAt(i) == '(' || regExp.charAt(i) == '|') && regExp.charAt(i + 1) == '|')
					throw new Exception("Empty subexpression with Alternation at index " + (i + 1));
				if (i + 1 < regExp.length() && regExp.charAt(i + 1) == ')' && regExp.charAt(i) == '|')
					throw new Exception("Empty subexpression with Alternation at index " + (i + 1));
				if (regExp.charAt(i) == '(') bracketStack++;
				else if (regExp.charAt(i) == ')') {
					if (bracketStack == 0) throw new Exception("Bracket imbalance at index " + (i + 1));
					bracketStack--;
				}
			}
			if (bracketStack != 0) throw new Exception("Bracket imbalance with unclosed opening bracket");
			if (regExp.charAt(0) == '|') throw new Exception("Empty subexpression with Alternation at index " + 1);
			if (regExp.charAt(regExp.length() - 1) == '|')
				throw new Exception("Empty subexpression with Alternation at index " + regExp.length());
		} catch (Exception e) {
			System.err.println(e);
		}
	}
}
