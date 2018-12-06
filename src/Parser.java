import java.util.*;

import static java.util.Arrays.asList;

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

	public static List<Object> checkRegExp(String regExp) {
		Deque<Integer> bracketStack = new ArrayDeque<>();
		if (regExp.length() == 0) {
			return asList("Empty string", 0, 0);
		}
		for (int i = 0; i < regExp.length(); i++) {
			if (i + 1 < regExp.length() && regExp.charAt(i) == '*' && regExp.charAt(i + 1) == '*')
				return asList("Empty subexpression with Kleene Star", i, i + 2);
			if (i + 1 < regExp.length() && (regExp.charAt(i) == '(' || regExp.charAt(i) == '|') && regExp.charAt(i + 1) == '*')
				return asList("Empty subexpression with Kleene Star", i, i + 2);
			if (i + 1 < regExp.length() && (regExp.charAt(i) == '(' || regExp.charAt(i) == '|') && regExp.charAt(i + 1) == '|')
				return asList("Empty subexpression with Alternation", i, i + 2);
			if (i + 1 < regExp.length() && regExp.charAt(i) == '|' && regExp.charAt(i + 1) == ')')
				return asList("Empty subexpression with Alternation", i, i + 2);
			if (i + 1 < regExp.length() && regExp.charAt(i) == '(' && regExp.charAt(i + 1) == ')')
				return asList("Empty subexpression with parentheses", i, i + 2);
			if (regExp.charAt(i) == '(') bracketStack.addLast(i);
			else if (regExp.charAt(i) == ')') {
				if (bracketStack.size() == 0) return asList("Bracket imbalance", i, i + 1);
				bracketStack.removeLast();
			}
		}
		if (bracketStack.size() > 0) {
			return asList("Bracket imbalance with unclosed opening bracket", bracketStack.getLast(), bracketStack.getLast() + 1);
		}
		if (regExp.charAt(0) == '|') return asList("Empty subexpression with Alternation", 0, 1);
		if (regExp.charAt(regExp.length() - 1) == '|')
			return asList("Empty subexpression with Alternation", regExp.length() - 1, regExp.length());
		return null;
	}
}
