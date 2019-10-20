package mtgengine.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

public class OptionsPanel extends JPanel {
	private static final long serialVersionUID = 2911809583448516332L;
	private JButton _buttonClose;
	private OptionsDialog _parent;
	private Vector<OptionsLinePanel> _optionsLinePanels = new Vector<OptionsLinePanel>();
	
	public OptionsPanel(Vector<HoldPriority> _holdPrioritiesSettings) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		buildUI(_holdPrioritiesSettings);
		setVisible(true);
	}

	public void setDialog(OptionsDialog parent) {
		_parent = parent;
	}
	
	public void buildUI(Vector<HoldPriority> _holdPrioritiesSettings) {
		OptionsLinePanel olp;
		boolean myTurn, hisTurn;
		
		for (HoldPriority hp : _holdPrioritiesSettings) {
			myTurn = hp.getMy();
			hisTurn = hp.getHis();
			olp = new OptionsLinePanel(hp.getStep(), myTurn, hisTurn);
			_optionsLinePanels.addElement(olp);
			add(olp);
		}
		
		// "Close" button
		_buttonClose = new JButton("Close");
		_buttonClose.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				_parent.setVisible(false);
				HoldPriority hp;
				for (OptionsLinePanel olp : _optionsLinePanels) {
					hp = olp.getValues();
					_board.setHoldPrioritySetting(hp.getStep(), hp.getMy(), hp.getHis());
				}
			}
		});
		add(_buttonClose);
	}
	
	private Board _board;
	
	public void setBoard(Board board) {
		_board = board;
	}
}
