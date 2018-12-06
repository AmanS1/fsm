import java.util.*;

import static java.util.Arrays.asList;

/**
 * @author Aman Sariyev
 */
public class Parser {
	public static Automaton convertRegExpTokensToAutomaton(List<String> tokens) {
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
				aut1 = convertRegExpTokensToAutomaton(tokens.subList(2, tokens.size()));
				aut.getFinish().addTransition(aut.getStart(), '$');
				aut.getFinish().setAccepting(false);
				aut.getStart().setAccepting(true);
				aut.setFinish(aut.getStart());
			} else {
				aut1 = convertRegExpTokensToAutomaton(tokens.subList(1, tokens.size()));
			}
			if (aut1 != null) {
				aut.getFinish().addTransition(aut1.getStart(), '$');
				aut.getFinish().setAccepting(false);
				aut.setFinish(aut1.getFinish());
			}
		} else {
			aut1 = convertRegExpTokensToAutomaton(tokens.subList(0, posAlt));
			aut2 = convertRegExpTokensToAutomaton(tokens.subList(posAlt + 1, tokens.size()));
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

	public static Automaton convertDescTokensToAutomaton(List<String> tokens) {
		Automaton aut = new Automaton();
		Map<String, Node> nodes = new HashMap<>();
		Set<String> inits = new HashSet<>(), fins = new HashSet<>();
		int cat = -1;
		for (int i = 0; i < tokens.size(); i++) {
			if (tokens.get(i).equals("#")) {
				if (tokens.get(i + 1).equals("initial")) cat = 0;
				else if (tokens.get(i + 1).equals("accepting")) cat = 1;
				else if (tokens.get(i + 1).equals("transitions")) cat = 2;
				i++;
			} else if (cat == 0) {
				if (!nodes.containsKey(tokens.get(i))) nodes.put(tokens.get(i), new Node());
				inits.add(tokens.get(i));
			} else if (cat == 1) {
				if (!nodes.containsKey(tokens.get(i))) nodes.put(tokens.get(i), new Node());
				nodes.get(tokens.get(i)).setAccepting(true);
				fins.add(tokens.get(i));
			} else if (cat == 2) {
				System.out.println(tokens.subList(i, i + 5));
				if (!nodes.containsKey(tokens.get(i))) nodes.put(tokens.get(i), new Node());
				if (!nodes.containsKey(tokens.get(i + 4))) nodes.put(tokens.get(i + 4), new Node());
				System.out.println(nodes.get(tokens.get(i)).getTransitions());
				nodes.get(tokens.get(i)).addTransition(nodes.get(tokens.get(i + 4)), tokens.get(i + 2).charAt(0));
				i += 4;
			}
		}
		if (inits.size() == 1) aut.setStart(nodes.get(inits.iterator().next()));
		else for (String s : inits) aut.getStart().addTransition(nodes.get(s), '$');
		if (fins.size() == 1) aut.setFinish(nodes.get(inits.iterator().next()));
		else for (String s : inits) nodes.get(s).addTransition(aut.getFinish(), '$');
		System.out.println(aut.getStart());
		return aut;
	}

	public static List<Object> checkDesc(List<String> tokens) {
		if (tokens.isEmpty()) {
			return asList("Empty description", 0, 0);
		}
		int cat = -1;
		for (int i = 0; i < tokens.size(); i++) {
			if (i > 0 && tokens.get(i - 1).equals("#")) {
				if (tokens.get(i).equals("initial")) cat = 0;
				else if (tokens.get(i).equals("accepting")) cat = 1;
				else if (tokens.get(i).equals("transitions")) cat = 2;
				continue;
			}
			if (cat < 0 && !tokens.get(i).equals("#")) return asList("Undefined category", i, i + 1);
			if (i + 1 < tokens.size() && tokens.get(i).equals("#") && !tokens.get(i + 1).equals("initial") &&
					!tokens.get(i + 1).equals("accepting") && !tokens.get(i + 1).equals("transitions"))
				return asList("Unrecognized category after #", i, i + 2);
			if ((cat == 0 || cat == 1) && (tokens.get(i).equals(":") || tokens.get(i).equals(">")))
				return asList("Inappropriate symbol for this category", i, i + 1);
			if (i + 1 < tokens.size() && tokens.get(i).equals("#") && !tokens.get(i + 1).equals("initial") &&
					!tokens.get(i + 1).equals("accepting") && !tokens.get(i + 1).equals("transitions"))
				return asList("Unrecognized category after #", i, i + 2);
			if ((cat == 0 || cat == 1) && (tokens.get(i).equals(":") || tokens.get(i).equals(">")))
				return asList("Inappropriate symbol for this category", i, i + 1);
			if (cat == 2) {
				if (canBeSt(tokens.get(i))) {
					if (!(i + 4 < tokens.size() && canBeTrans(tokens.subList(i, i + 5)) ||
							i - 2 >= 0 && i + 2 < tokens.size() && canBeTrans(tokens.subList(i - 2, i + 3)) ||
							i - 4 >= 0 && canBeTrans(tokens.subList(i - 4, i + 1))))
						return asList("Unidentifiable token", i, i + 1);
					if (i - 2 >= 0 && i + 2 < tokens.size() && canBeTrans(tokens.subList(i - 2, i + 3)) && tokens.get(i).length() > 1)
						return asList("Transition has to be 1 symbol only", i, i + 1);
				}
				if (tokens.get(i).equals(":") && (i - 1 < 0 || i + 3 >= tokens.size() || !canBeTrans(tokens.subList(i - 1, i + 4))))
					return asList("Unidentifiable token", i, i + 1);
				if (tokens.get(i).equals(">") && (i - 3 < 0 || i + 1 >= tokens.size() || !canBeTrans(tokens.subList(i - 3, i + 2)))) {
					return asList("Unidentifiable token", i, i + 1);
				}
			}
		}
		return null;
	}

	private static boolean canBeTrans(List<String> strings) {
		return canBeSt(strings.get(0)) && strings.get(1).equals(":") &&
				canBeSt(strings.get(2)) && strings.get(3).equals(">") && canBeSt(strings.get(4));
	}

	private static boolean canBeSt(String s) {
		return !s.equals("#") && !s.equals(":") && !s.equals(">");
	}
}
