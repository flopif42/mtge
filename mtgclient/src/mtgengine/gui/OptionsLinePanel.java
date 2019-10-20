package mtgengine.gui;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import mtgengine.Game.Step;

public class OptionsLinePanel extends JPanel {
	private static final long serialVersionUID = -4398924440515439734L;

	private Step _step;
	private JCheckBox _myTurnCheckBox;
	private JCheckBox _hisTurnCheckBox;
	
	public OptionsLinePanel(Step step, boolean my, boolean his) {
		_step = step;
		_myTurnCheckBox = new JCheckBox("My turn", my);
		_hisTurnCheckBox = new JCheckBox("His turn", his);
		
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		add(new JLabel(step.name()));
		add(_myTurnCheckBox);
		add(_hisTurnCheckBox);
		
		setVisible(true);
	}
	
	public HoldPriority getValues() {
		return new HoldPriority(_step, _myTurnCheckBox.isSelected(), _hisTurnCheckBox.isSelected());
	}
}
