import javafx.util.Pair;

import java.util.*;

public class Automaton {
	private static final boolean DEBUG_MODE = true;
	private boolean deterministic;

	public boolean isDeterministic() {
		return deterministic;
	}

	public void setDeterministic(boolean deterministic) {
		this.deterministic = deterministic;
	}

	private Node start, finish;

	public Node getStart() {
		return start;
	}

	public void setFinish(Node finish) {
		this.finish = finish;
	}

	public Node getFinish() {
		return finish;
	}

	public Automaton() {
		start = new Node(false);
		finish = new Node(true);
		deterministic = false;
	}

	public Automaton(char c) {
		this();
		start.addTransition(finish, c);
	}

	@Override
	public String toString() {
		Map<Node, Integer> ids = new HashMap<>();
		Queue<Node> q = new LinkedList<>();
		Set<Character> alphabet = new HashSet<>();
		StringBuilder ans = new StringBuilder("#transitions\n"), acc = new StringBuilder("#accepting\n");
		int id = 1;
		q.add(start);
		ids.put(start, 0);
		while (!q.isEmpty()) {
			Node temp = q.remove();
			if (temp.isAccepting()) acc.append("q").append(ids.get(temp)).append("\n");
			Set<Pair<Node, Character>> trns = temp.getTransitions();
			for (Pair<Node, Character> trn : trns) {
				alphabet.add(trn.getValue());
				if (!ids.containsKey(trn.getKey())) {
					ids.put(trn.getKey(), id);
					id++;
					q.add(trn.getKey());
				}
				//ans += ids.get(temp) + ":" + trn.getValue() + ">" + ids.get(trn.getKey()) + "\n"; //String
				ans.append("q").append(ids.get(temp)).append(":").append(trn.getValue()).append(">").append("q").append(ids.get(trn.getKey())).append("\n");
			}
		}
		if (DEBUG_MODE) {
			ans.append("#states\n");
			for (int i = 0; i < id; i++) ans.append("q").append(i).append("\n");
			ans.append("#alphabet\n");
			alphabet.remove('$');
			for (char c : alphabet) ans.append(c).append("\n");
		}
		return "#initial\nq0\n" + acc + ans;
	}
}