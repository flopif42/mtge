package mtgengine.gui;

import javax.swing.JDialog;

public class OptionsDialog extends JDialog {
	private static final long serialVersionUID = 1221359041303543262L;

	public OptionsDialog(Board board) {
		//dialog box attributes
		setTitle("Hold priority settings");
		setSize(270, 320);
		setResizable(false);
		setModal(true);
		setLocationRelativeTo(null);

		// components
		OptionsPanel panel = new OptionsPanel(board.getHoldPrioritiesSettings());
		panel.setDialog(this);
		panel.setBoard(board);
		add(panel);
	}
}
