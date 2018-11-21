import javafx.util.Pair;

import java.util.HashSet;
import java.util.Set;

public class Node {
	private boolean accepted;
	private Set<Pair<Node, Character>> transitions;

	public Node() {
		transitions = new HashSet<>();
		accepted = false;
	}

	public Node(boolean accepted) {
		this();
		this.accepted = accepted;
	}

	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}

	public boolean isAccepted() {
		return accepted;
	}

	public void addTransition(Node finish, char c) {
		transitions.add(new Pair<>(finish, c));
	}

	public Set<Pair<Node, Character>> getTransitions() {
		return transitions;
	}
}
