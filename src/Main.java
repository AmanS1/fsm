import java.util.*;

public class Main {

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		String regExp = in.nextLine();
		System.out.println(AutomatonUtils.removeEpsilonAlternation(regExp));
		String testString;
		//testString = in.nextLine();
		try {
			Automaton aut = AutomatonUtils.convertRegExpToAutomaton(regExp);
			System.out.println(aut.toString());
			aut = AutomatonUtils.simplifyAutomaton(aut);
			System.out.println(aut.toString());
			//System.out.println(checkAcceptability(aut, testString));
			testString = AutomatonUtils.convertAutomatonToRegExp(aut);
			System.out.println("RegExp: " + testString);
		} catch (Exception e) {
			System.err.println(e);
		}
	}
}