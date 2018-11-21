import javafx.util.Pair;

import java.util.*;

public class Main {
	public static final boolean DEBUG_MODE = true;

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

	private static Automata convertRegExpToAutomata(String regExp) {
		/*
		Brackets		First Priority
		Kleene Star		Second Priority
		Concatenation	Third Priority
		Alternative		Fourth Priority
		*/
		Automata aut;
		List<String> tokens = new ArrayList<>();
		int left = 0, bracketStack = 0;
		if (regExp.length() == 1) {
			return new Automata(regExp.charAt(0));
		}
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
		aut = convertTokensToAutomata(tokens);
		return aut;
	}

	private static Automata convertTokensToAutomata(List<String> tokens) {
		Automata aut, aut1, aut2;
		int posAlt = -1;
		if (tokens.size() == 0) return null;
		if (tokens.size() == 1) {
			return convertRegExpToAutomata(tokens.get(0));
		}
		for (int i = 0; i < tokens.size(); i++) {
			if (tokens.get(i).equals("|")) {
				posAlt = i;
				break;
			}
		}
		if (posAlt < 0) {
			aut = convertRegExpToAutomata(tokens.get(0));
			if (tokens.size() > 1 && tokens.get(1).equals("*")) {
				aut1 = convertTokensToAutomata(tokens.subList(2, tokens.size()));
				aut.getFinish().addTransition(aut.getStart(), '$');
				aut.getFinish().setAccepted(false);
				aut.getStart().setAccepted(true);
				aut.setFinish(aut.getStart());
			} else {
				aut1 = convertTokensToAutomata(tokens.subList(1, tokens.size()));
			}
			if (aut1 != null) {
				aut.getFinish().addTransition(aut1.getStart(), '$');
				aut.getFinish().setAccepted(false);
				aut.setFinish(aut1.getFinish());
			}
		} else {
			aut1 = convertTokensToAutomata(tokens.subList(0, posAlt));
			aut2 = convertTokensToAutomata(tokens.subList(posAlt + 1, tokens.size()));
			aut = new Automata();
			aut.getStart().addTransition(aut1.getStart(), '$');
			aut.getStart().addTransition(aut2.getStart(), '$');
			aut1.getFinish().addTransition(aut.getFinish(), '$');
			aut2.getFinish().addTransition(aut.getFinish(), '$');
			aut1.getFinish().setAccepted(false);
			aut2.getFinish().setAccepted(false);
		}
		return aut;
	}

	private static Automata determineAutomata(Automata aut) {
		Automata ans = new Automata();
		Map<Node, Integer> ids = new HashMap<>();
		Queue<Node> q = new LinkedList<>();
		Set<Pair<Node, Character>> trns;
		List<Node> nodeById = new ArrayList<>();
		Set<Character> alphabet = new HashSet<>();
		int id = 1;
		q.add(aut.getStart());
		ids.put(aut.getStart(), 0);
		nodeById.add(aut.getStart());
		while (!q.isEmpty()) {
			Node temp = q.remove();
			trns = temp.getTransitions();
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
		Set<Integer> genSt = new HashSet<>();
		Map<Set<Integer>, Integer> stIds = new HashMap<>();
		Queue<Set<Integer>> q2 = new LinkedList<>();
		Map<Set<Integer>, Node> nodeBySet = new HashMap<>();
		Node tempNode;
		boolean acc = aut.getStart().isAccepted();
		genSt.add(0);
		trns = aut.getStart().getTransitions();
		for (Pair<Node, Character> trn : trns) {
			if (trn.getValue() == '$') {
				acc |= trn.getKey().isAccepted();
				genSt.add(ids.get(trn.getKey()));
			}
		}
		id = 1;
		q2.add(genSt);
		stIds.put(genSt, 0);
		nodeBySet.put(genSt, ans.getStart());
		if (acc) ans.getStart().setAccepted(true);
		while (!q2.isEmpty()) {
			Set<Integer> temp = q2.remove();
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
				boolean expand;
				acc = false;
				do {
					expand = false;
					Set<Integer> genStClone = new HashSet<>(genSt);
					for (Integer it : genSt) {
						acc |= nodeById.get(it).isAccepted();
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
				if (!genSt.isEmpty()) {
					if (!stIds.containsKey(genSt)) {
						stIds.put(genSt, id);
						id++;
						q2.add(genSt);
						tempNode = new Node();
						nodeBySet.put(genSt, tempNode);
					} else tempNode = nodeBySet.get(genSt);
					if (DEBUG_MODE) {
						System.out.println("Letter: " + c + "\n" + genSt);
					}
					nodeBySet.get(temp).addTransition(tempNode, c);
					if (acc) tempNode.setAccepted(true);
				}
			}
		}
		return ans;
	}

	private static Automata simplifyAutomata(Automata aut) {
		return determineAutomata(aut);
		//return deRedoundAutomata(determineAutomata(aut));
	}

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		String regExp = in.nextLine();
		try {
			checkRegExp(regExp);
			Automata aut = convertRegExpToAutomata(regExp);
			System.out.println(aut.toString());
			aut = simplifyAutomata(aut);
			System.out.println(aut.toString());
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}