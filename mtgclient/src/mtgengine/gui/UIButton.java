package mtgengine.gui;

public class UIButton {
	public int width;
	public int height;
	public int x;
	public int y;
	public int ID; // used for player
	private String _label;
	private boolean _bEnabled;
	
	// the two following fields are only used for activated abilities
	private int _sourceId = 0;
	private int _actionId = 0;

	public UIButton(String label) {
		_label = label;
		_bEnabled = true;
	}

	public void setLabel(String label) {
		_label = label;
	}
	
	public String getLabel() {
		return _label;
	}

	public void setDimensions(int w, int h) {
		width = w;
		height = h;
	}

	public void setLocation(int lx, int ly) {
		x = lx;
		y = ly;
	}

	public boolean isEnabled() {
		return _bEnabled;
	}

	public void setEnabled(boolean b) {
		_bEnabled = b;
	}

	public void setActionData(int sourceId, int actionId) {
		_sourceId = sourceId;
		_actionId = actionId;
	}
	
	public int getSourceId() {
		return _sourceId;
	}
	
	public int getActionId() {
		return _actionId;
	}
}
