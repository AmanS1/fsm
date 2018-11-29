import javafx.util.Pair;

import java.util.*;

public class Main {
	private static final boolean DEBUG_MODE = true;

	private static void checkRegExp(String regExp) throws Exception {
		int bracketStack = 0;
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
	}

	private static List<String> tokenize(String regExp) {
		/*
		Brackets		First Priority
		Kleene Star		Second Priority
		Concatenation	Third Priority
		Alternative		Fourth Priority
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

	private static Automaton convertRegExpToAutomaton(String regExp) {
		if (regExp.length() == 1) {
			return new Automaton(regExp.charAt(0));
		}
		List<String> tokens = tokenize(regExp);
		return convertTokensToAutomaton(tokens);
	}

	private static Automaton convertTokensToAutomaton(List<String> tokens) {
		Automaton aut, aut1, aut2;
		int posAlt = -1;
		if (tokens.size() == 0) return null;
		if (tokens.size() == 1) {
			return convertRegExpToAutomaton(tokens.get(0));
		}
		for (int i = 0; i < tokens.size(); i++) {
			if (tokens.get(i).equals("|")) {
				posAlt = i;
				break;
			}
		}
		if (posAlt < 0) {
			aut = convertRegExpToAutomaton(tokens.get(0));
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

	private static void enumerateAutomaton(Automaton aut, Map<Node, Integer> ids, List<Node> nodeById, Set<Character> alphabet) {
		Queue<Node> q = new LinkedList<>();
		int id = 1;
		q.add(aut.getStart());
		ids.put(aut.getStart(), 0);
		nodeById.add(aut.getStart());
		while (!q.isEmpty()) {
			Node temp = q.remove();
			Set<Pair<Node, Character>> trns = temp.getTransitions();
			for (Pair<Node, Character> trn : trns) {
				if (trn.getValue() != '$') alphabet.add(trn.getValue());
				if (!ids.containsKey(trn.getKey())) {
					ids.put(trn.getKey(), id);
					nodeById.add(trn.getKey());
					id++;
					q.add(trn.getKey());
				}
			}
		}
	}

	private static boolean stateEpsilonExpand(Set<Integer> genSt, Map<Node, Integer> ids, List<Node> nodeById) {
		Set<Pair<Node, Character>> trns;
		Set<Integer> origGenSt = genSt;
		boolean expand, acc = false;
		do {
			expand = false;
			Set<Integer> genStClone = new HashSet<>(genSt);
			for (Integer it : genSt) {
				acc |= nodeById.get(it).isAccepting();
				trns = nodeById.get(it).getTransitions();
				for (Pair<Node, Character> trn : trns) {
					if (trn.getValue() == '$' && !genSt.contains(ids.get(trn.getKey()))) {
						genStClone.add(ids.get(trn.getKey()));
						expand = true;
					}
				}
			}
			genSt = genStClone;
		} while (expand);
		origGenSt.clear();
		origGenSt.addAll(genSt);
		return acc;
	}

	private static Automaton determineAutomaton(Automaton aut) {
		Map<Node, Integer> ids = new HashMap<>();
		List<Node> nodeById = new ArrayList<>();
		Set<Character> alphabet = new HashSet<>();
		enumerateAutomaton(aut, ids, nodeById, alphabet);
		Automaton ans = new Automaton();
		Set<Pair<Node, Character>> trns;
		Set<Integer> genSt = new HashSet<>();
		Map<Set<Integer>, Integer> stIds = new HashMap<>();
		Queue<Set<Integer>> q = new LinkedList<>();
		Map<Set<Integer>, Node> nodeBySet = new HashMap<>();
		Node tempNode;
		boolean acc;
		genSt.add(0);
		acc = stateEpsilonExpand(genSt, ids, nodeById);
		int id = 1;
		q.add(genSt);
		stIds.put(genSt, 0);
		nodeBySet.put(genSt, ans.getStart());
		if (acc) ans.getStart().setAccepting(true);
		while (!q.isEmpty()) {
			Set<Integer> temp = q.remove();
			if (DEBUG_MODE) System.out.println("New State: " + temp);
			for (Character c : alphabet) {
				genSt = new HashSet<>();
				for (Integer it : temp) {
					trns = nodeById.get(it).getTransitions();
					for (Pair<Node, Character> trn : trns) {
						if (trn.getValue() == c) {
							genSt.add(ids.get(trn.getKey()));
						}
					}
				}
				acc = stateEpsilonExpand(genSt, ids, nodeById);
				if (!genSt.isEmpty()) {
					if (!stIds.containsKey(genSt)) {
						stIds.put(genSt, id);
						id++;
						q.add(genSt);
						tempNode = new Node();
						nodeBySet.put(genSt, tempNode);
					} else tempNode = nodeBySet.get(genSt);
					if (DEBUG_MODE) System.out.println("Letter: " + c + "\n" + genSt);
					nodeBySet.get(temp).addTransition(tempNode, c);
					if (acc) tempNode.setAccepting(true);
				}
			}
		}
		ans.setDeterministic(true);
		return ans;
	}

	private static Automaton removeRedundancyAutomaton(Automaton aut) {
		// http://pages.cs.wisc.edu/~shuchi/courses/520-S08/handouts/Lec7.pdf
		Map<Node, Integer> ids = new HashMap<>();
		List<Node> nodeById = new ArrayList<>();
		Set<Character> alphabet = new HashSet<>();

		enumerateAutomaton(aut, ids, nodeById, alphabet);
		Queue<Pair<Integer, Integer>> q = new LinkedList<>();
		Set<Pair<Integer, Integer>> distinguishable = new HashSet<>();
		Set<Integer> acc = new HashSet<>(), nonAcc = new HashSet<>(), visited = new HashSet<>();
		Map<Character, Set<Node>> rev;
		int x, y;
		for (Node nd : nodeById) (nd.isAccepting() ? acc : nonAcc).add(ids.get(nd));
		for (int i : acc) {
			for (int j : nonAcc) {
				distinguishable.add(new Pair<>(i, j));
				q.add(new Pair<>(i, j));
			}
		}
		while (!q.isEmpty()) {
			x = q.peek().getKey();
			y = q.remove().getValue();
			for (char c : alphabet) {
				if (nodeById.get(x).getRevTransitions().get(c) == null || nodeById.get(y).getRevTransitions().get(c) == null)
					continue;
				for (Node i : nodeById.get(x).getRevTransitions().get(c)) {
					for (Node j : nodeById.get(y).getRevTransitions().get(c)) {
						if (!distinguishable.contains(new Pair<>(ids.get(i), ids.get(j)))) {
							distinguishable.add(new Pair<>(ids.get(i), ids.get(j)));
							q.add(new Pair<>(ids.get(i), ids.get(j)));
						}
					}
				}
			}
		}
		for (int i = 0; i < ids.size(); i++) {
			if (!visited.contains(i)) {
				visited.add(i);
				for (int j = i + 1; j < ids.size(); j++) {
					if (!distinguishable.contains(new Pair<>(i, j)) && !distinguishable.contains(new Pair<>(j, i))) {
						visited.add(j);
						rev = nodeById.get(j).getRevTransitions();
						if (DEBUG_MODE) System.out.println(i + " = " + j + " Concatenation");
						for (char c : alphabet) {
							if (rev.get(c) == null) continue;
							List<Node> temp = new ArrayList<>(rev.get(c));
							for (Node nd : temp) {
								nd.removeTransition(nodeById.get(j), c);
								nd.addTransition(nodeById.get(i), c);
							}
						}
						Set<Pair<Node, Character>> temp = new HashSet<>(nodeById.get(j).getTransitions());
						for (Pair<Node, Character> it : temp) {
							nodeById.get(i).addTransition(it.getKey(), it.getValue());
							nodeById.get(j).removeTransition(it.getKey(), it.getValue());
						}
					}
				}
			}
		}
		if (DEBUG_MODE) System.out.println("DIFF: {" + distinguishable + "} :END");
		return aut;
	}

	private static void addDummyState(Automaton aut) {
		Node dummy = new Node();
		Map<Node, Integer> ids = new HashMap<>();
		List<Node> nodeById = new ArrayList<>();
		Set<Character> alphabet = new HashSet<>();
		enumerateAutomaton(aut, ids, nodeById, alphabet);
		for (char c : alphabet) {
			for (Node nd : nodeById) {
				nd.addTransition(dummy, c);
			}
			dummy.addTransition(dummy, c);
		}
	}

	private static Automaton simplifyAutomaton(Automaton aut) {
		addDummyState(aut);
		if (DEBUG_MODE) {
			aut = determineAutomaton(aut);
			System.out.println(aut.toString());
			return removeRedundancyAutomaton(aut);
		}
		return removeRedundancyAutomaton(determineAutomaton(aut));
	}

	private static boolean checkAcceptability(Automaton aut, String input) throws Exception {
		if (!aut.isDeterministic()) throw new Exception("This automaton is not deterministic");
		boolean trans;
		Node nd = aut.getStart();
		for (int i = 0; i < input.length(); i++) {
			trans = false;
			for (Pair<Node, Character> j : nd.getTransitions()) {
				if (j.getValue() == input.charAt(i)) {
					nd = j.getKey();
					trans = true;
					break;
				}
			}
			if (!trans) return false;
		}
		return nd.isAccepting();
	}

	private static String removeEpsilonAlternation(String s) {
		StringBuilder ans = new StringBuilder();
		List<String> tokens = tokenize(s);
		for (int i = 0; i < tokens.size(); i++) {
			if (tokens.get(i).equals("$") && (i == 0 || tokens.get(i - 1).equals("|")) && (i + 1 >= tokens.size() || tokens.get(i + 1).equals("|"))) {
				if (i + 1 < tokens.size() && tokens.get(i + 1).equals("|")) {
					tokens.remove(i + 1);
					tokens.remove(i);
				} else {
					tokens.remove(i);
					if (i > 0 && tokens.get(i - 1).equals("|")) tokens.remove(i - 1);
				}
			}
		}
		for (String token : tokens) ans.append(token);
		return ans.toString();
	}

	private static boolean hasAlternation(String s) {
		List<String> tokens = tokenize(s);
		for (String token : tokens) {
			if (token.equals("|")) return true;
		}
		return false;
	}

	private static String convertAutomatonToRegExp(Automaton aut) {
		Map<Node, Integer> ids = new HashMap<>();
		List<Node> nodeById = new ArrayList<>();
		Set<Character> alphabet = new HashSet<>();
		enumerateAutomaton(aut, ids, nodeById, alphabet);
		Set<String> temp;
		int n = ids.size();
		enumerateAutomaton(aut, ids, nodeById, alphabet);
		List<List<List<String>>> substrings = new ArrayList<>();
		List<List<List<Boolean>>> alternation = new ArrayList<>();
		for (int k = 0; k < n + 1; k++) {
			substrings.add(new ArrayList<>());
			alternation.add(new ArrayList<>());
			for (int i = 0; i < n; i++) {
				substrings.get(k).add(new ArrayList<>());
				alternation.get(k).add(new ArrayList<>());
			}
		}
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				temp = new HashSet<>();
				Set<Pair<Node, Character>> trns = nodeById.get(i).getTransitions();
				for (Pair<Node, Character> trn : trns) {
					if (ids.get(trn.getKey()) == j) {
						temp.add(String.valueOf(trn.getValue()));
					}
				}
				if (i == j) temp.add("$");
				substrings.get(0).get(i).add(String.join("|", temp));
				alternation.get(0).get(i).add(temp.size() > 1);
				if (DEBUG_MODE) {
					System.out.println("temp: " + i + " " + j + " " + temp);
					System.out.println("{" + String.join("|", temp) + "} " + alternation.get(0).get(i).get(j));
				}
			}
		}
		for (int k = 0; k < n; k++) {
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					temp = new HashSet<>();
					boolean b1 = false, b2 = false;
					if (substrings.get(k).get(i).get(j).length() != 0) {
						temp.add(substrings.get(k).get(i).get(j));
						b1 = alternation.get(k).get(i).get(j);
					}
					if (substrings.get(k).get(i).get(k).length() != 0 && substrings.get(k).get(k).get(j).length() != 0 &&
							substrings.get(k).get(k).get(k).length() != 0) {
						String s1 = substrings.get(k).get(i).get(k), s2 = substrings.get(k).get(k).get(j),
								s = substrings.get(k).get(k).get(k);
						if (DEBUG_MODE)
							System.out.println(k + " " + i + " " + j + "\n"
									+ alternation.get(k).get(k).get(k) + " " + s1.equals("$") + " " + s2.equals("$"));
						if (alternation.get(k).get(k).get(k) && !s.equals("$")) {
							s = removeEpsilonAlternation(s);
							if (hasAlternation(s)) s = String.format("(%s)", s);
						}
						if (alternation.get(k).get(i).get(k) && !(s.equals("$") && s2.equals("$")))
							s1 = String.format("(%s)", s1);
						if (alternation.get(k).get(k).get(j) && !(s.equals("$") && s1.equals("$")))
							s2 = String.format("(%s)", s2);
						if (s1.equals("$") && !(s.equals("$") && s2.equals("$")) || !s1.equals("$") && s.equals(s1))
							s1 = "";
						if (s2.equals("$") || !s2.equals("$") && s.equals(s2)) s2 = "";
						if (s.equals("$")) {
							temp.add(String.format("%s%s", s1, s2));
							b2 = s1.equals("") && alternation.get(k).get(k).get(j) || s2.equals("") && alternation.get(k).get(i).get(k);
						} else {
							temp.add(String.format("%s%s*%s", s1, s, s2));
						}
					}
					if (temp.size() > 1) {
						temp.remove(substrings.get(k).get(i).get(j));
						if (b2) {
							String s = temp.iterator().next();
							temp.add(String.format("(%s)", s));
						}
						temp.add(b1 ? String.format("(%s)", substrings.get(k).get(i).get(j)) : substrings.get(k).get(i).get(j));
					}
					substrings.get(k + 1).get(i).add(String.join("|", temp));
					if (temp.size() > 1) alternation.get(k + 1).get(i).add(true);
					else if (temp.size() == 1) {
						alternation.get(k + 1).get(i).add(temp.iterator().next().equals(substrings.get(k).get(i).get(j)) ? b1 : b2);
					} else alternation.get(k + 1).get(i).add(true);
					if (DEBUG_MODE) {
						System.out.printf("temp: %d %d %d %s%n", k, i, j, temp);
						System.out.printf("{%s} %s\n%n", String.join("|", temp), alternation.get(k + 1).get(i).get(j));
					}
				}
			}
		}
		Set<Integer> acc = new HashSet<>();
		temp = new HashSet<>();
		for (Node nd : nodeById) if (nd.isAccepting()) acc.add(ids.get(nd));
		for (int i : acc) {
			temp.add(substrings.get(n).get(0).get(i));
		}
		return String.join("|", temp);
	}

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		String regExp = in.nextLine();
		System.out.println(removeEpsilonAlternation(regExp));
		String testString;
		//testString = in.nextLine();
		try {
			checkRegExp(regExp);
			Automaton aut = convertRegExpToAutomaton(regExp);
			System.out.println(aut.toString());
			aut = simplifyAutomaton(aut);
			System.out.println(aut.toString());
			//System.out.println(checkAcceptability(aut, testString));
			testString = convertAutomatonToRegExp(aut);
			System.out.println("RegExp: " + testString);
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}