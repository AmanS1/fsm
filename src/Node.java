import javafx.util.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Node {
	private boolean accepting;
	private Set<Pair<Node, Character>> transitions;
	private Map<Character, Set<Node>> revTransitions;

	public Node() {
		transitions = new HashSet<>();
		revTransitions = new HashMap<>();
		accepting = false;
	}

	public Node(boolean accepting) {
		this();
		this.accepting = accepting;
	}

	public void setAccepting(boolean accepting) {
		this.accepting = accepting;
	}

	public boolean isAccepting() {
		return accepting;
	}

	public void addTransition(Node finish, char c) {
		transitions.add(new Pair<>(finish, c));
		finish.addRevTransition(this, c);
	}

	public void removeTransition(Node finish, char c) {
		transitions.remove(new Pair<>(finish, c));
		finish.addRevTransition(this, c);
		finish.removeRevTransition(this, c);
	}

	private void addRevTransition(Node start, char c) {
		revTransitions.computeIfAbsent(c, k -> new HashSet<>());
		revTransitions.get(c).add(start);
	}

	private void removeRevTransition(Node start, char c) {
		revTransitions.computeIfAbsent(c, k -> new HashSet<>());
		revTransitions.get(c).remove(start);
	}

	public Set<Pair<Node, Character>> getTransitions() {
		return transitions;
	}

	public Map<Character, Set<Node>> getRevTransitions() {
		return revTransitions;
	}
}
