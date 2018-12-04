import javax.swing.*;

/**
 * @author Aman Sariyev
 */
public class Fsm extends JFrame {

	static final boolean DEBUG_MODE = false;

	private Fsm() {
		initUI();
	}

	private void initUI() {
		FsmGui mainPane = new FsmGui();
		add(mainPane);
		setResizable(true);
		pack();
		setTitle("FSM Utilities");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
	}

	public static void main(String[] args) {
		Fsm fsm = new Fsm();
		fsm.setVisible(true);
	}
}