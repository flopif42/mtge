package mtgengine.gui;

import java.util.HashMap;
import java.util.Vector;

public class UIPlayer {
	private int _id;
	private String _name;
	private int _life;
	private boolean _bMonarch;
	private int _libSize;
	private int _handSize;
	private HashMap<String, Integer> _manaPool;
	
	/* Zones */
	private UIZone _myHand;
	private UIZone _myGraveyard;
	private UIZone _myExile;
	private UIZone _myCommand;
	private UIZone _myLands;
	private UIZone _myCreatures;
	private UIZone _myOtherPermanents;
	
	/* Buttons */
	private UIButton _nameButton;
	private UIButton _graveyardBtn;
	private UIButton _exileBtn;
	private UIButton _commandBtn;
	private Vector<UIButton> _buttons = new Vector<UIButton>();
	
	/* Panels */
	private UIZonePanel _graveyardPanel = null;
	private UIZonePanel _exilePanel = null;
	private UIZonePanel _commandPanel = null;
	
	public UIPlayer(int id, String name) {
		_id = id;
		_name = name;
		_nameButton = new UIButton(name);
		_myGraveyard = new UIZone(name + "'s graveyard");
		_myExile = new UIZone(name + "'s exile");
		_myCommand = new UIZone(name + "'s command");
		_myHand = new UIZone(name + "'s hand");
		_myLands = new UIZone(name + "'s lands");
		_myCreatures = new UIZone(name + "'s creatures");
		_myOtherPermanents = new UIZone(name + "'s other permanents");
		
		_manaPool = new HashMap<String, Integer>();
		_manaPool.put("W", 0);
		_manaPool.put("U", 0);
		_manaPool.put("B", 0);
		_manaPool.put("R", 0);
		_manaPool.put("G", 0);
		_manaPool.put("C", 0);
	}
	
	public HashMap<String, Integer> getManaPool() {
		return _manaPool;
	}
	
	public int getId() {
		return _id;
	}

	public String getName() {
		return _name;
	}
	
	public int getLife() {
		return _life;
	}
	
	public void setLife(int life) {
		_life = life;
	}

	public int setMana(String type, int amount) {
		if (!_manaPool.containsKey(type))
			return -1;
		_manaPool.put(type, amount);
		return 0;
	}
	
	public int getManaAmount(String type) {
		if (!_manaPool.containsKey(type))
			return -1;
		return _manaPool.get(type);
	}
	
	public UIButton getNameButton() {
		return _nameButton;
	}

	public UIZone getGraveyard() {
		return _myGraveyard;
	}
	
	public UIZone getExile() {
		return _myExile;
	}
	
	public UIZone getCommand() {
		return _myCommand;
	}
	
	public UIZone getHand() {
		return _myHand;
	}
	
	public UIZone getLands() {
		return _myLands;
	}
	
	public UIZone getCreatures() {
		return _myCreatures;
	}
	
	public UIZone getOtherPermanents() {
		return _myOtherPermanents;
	}
	
	public Vector<UIZone> getZones() {
		Vector<UIZone> ret = new Vector<UIZone>();
		ret.add(_myHand);
		ret.add(_myGraveyard);
		ret.add(_myExile);
		ret.add(_myCreatures);
		ret.add(_myLands);
		ret.add(_myOtherPermanents);
		return ret;
	}
	
	public void setButtons(UIButton graveyard, UIButton exile, UIButton command) {
		_graveyardBtn = graveyard;
		_exileBtn = exile;
		_commandBtn = command;
		_buttons.clear();
		_buttons.add(_nameButton);
		_buttons.add(_graveyardBtn);
		_buttons.add(_exileBtn);
		_buttons.add(_commandBtn);
	}
	
	public Vector<UIButton> getButtons() {
		return _buttons;
	}
	
	public UIButton getGraveyardButton() {
		return _graveyardBtn;
	}
	
	public UIButton getExileButton() {
		return _exileBtn;
	}
	
	public UIButton getCommandButton() {
		return _commandBtn;
	}

	// Graveyard Panel
	public void setGraveyardPanel(UIZonePanel panel) {
		_graveyardPanel = panel;
	}
	
	public UIZonePanel getGraveyardPanel() {
		return _graveyardPanel;
	}
	
	// Exile Panel
	public void setExilePanel(UIZonePanel panel) {
		_exilePanel = panel;
	}
	
	public UIZonePanel getExilePanel() {
		return _exilePanel;
	}
	
	// Command Panel
	public void setCommandPanel(UIZonePanel panel) {
		_commandPanel = panel;
	}
	
	public UIZonePanel getCommandPanel() {
		return _commandPanel;
	}

	public int getLibSize() {
		return _libSize;
	}

	public void setLibSize(int _libSize) {
		this._libSize = _libSize;
	}

	public int getHandSize() {
		return _handSize;
	}

	public void setHandSize(int _handSize) {
		this._handSize = _handSize;
	}
	
	public void setMonarch(boolean status) {
		_bMonarch = status;
	}
	
	public boolean isMonarch() {
		return _bMonarch;
	}
}
