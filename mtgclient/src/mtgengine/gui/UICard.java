package mtgengine.gui;

import java.util.Vector;

import mtgengine.CounterType;

public class UICard {
	public int x = -1;
	public int y = -1;
	public int width;
	public int height;
	private UIZone _zone;
	private String _name;
	private String _rulesText = null;
	private int _idCard;
	private int _idSource;
	private int _imageID;
	private String _powerToughness = null;
	private Vector<String> _counters;
	private boolean _bTapped = false;
	private int _blockOrder = 0;
	private int _hostID = 0;
	private Vector<Integer> _aurasID = new Vector<Integer>();

	public UICard(UIZone zone, String cardFullName) {
		int index_AT = cardFullName.indexOf("@");
		int index_POUND = cardFullName.indexOf("#");
		int index_last_POUND = cardFullName.lastIndexOf("#");
		int index_TILDE = cardFullName.lastIndexOf("~");
		
		_zone = zone;
		_name = cardFullName.substring(0, index_AT);
		_imageID = Integer.parseInt(cardFullName.substring(index_AT + 1, index_POUND));
		_counters = new Vector<String>();
		
		// this is a card (not an ability)
		if (index_POUND == index_last_POUND) {
			cardFullName = cardFullName.substring(index_POUND + 1);
			
			// If card is tapped
			int index_TAPPED = cardFullName.indexOf("^");
			if (index_TAPPED != -1) {
				_bTapped = true;
				cardFullName = cardFullName.substring(0, index_TAPPED);
			}
			
			// If card is a creature
			int index_BRACKET = cardFullName.lastIndexOf("[");
			if (index_BRACKET != -1) {
				int index_CLOSE_BRACKET = cardFullName.lastIndexOf("]");
				_powerToughness = cardFullName.substring(index_BRACKET + 1, index_CLOSE_BRACKET);
				cardFullName = cardFullName.substring(0, index_BRACKET);
			}
			
			// If card is attached
			int index_ATTACHED = cardFullName.indexOf("->");
			if (index_ATTACHED != -1) {
				_hostID = Integer.parseInt(cardFullName.substring(index_ATTACHED + 2));
				cardFullName = cardFullName.substring(0, index_ATTACHED);
			}
			
			_idCard = Integer.parseInt(cardFullName);
			if (_hostID != 0)
				Board.addAura(_hostID, _idCard);
		}
		// if this is an ability
		else // if (index_POUND != index_last_POUND)
		{
			if (index_TILDE == -1)
				_idCard = Integer.parseInt(cardFullName.substring(index_last_POUND + 1));
			// if it has an ability description
			else
			{
				_idSource = Integer.parseInt(cardFullName.substring(index_POUND + 1, index_last_POUND));
				_idCard = Integer.parseInt(cardFullName.substring(index_last_POUND + 1, index_TILDE));
				_rulesText = cardFullName.substring(index_TILDE + 1);
			}
		}
	}

	public int getImageID() {
		return _imageID;
	}
	
	public String getName() {
		return _name;
	}
	
	public boolean hit(int lx, int ly) {
		if (x == -1 && y == -1)
			return false;
		if ((lx >= x) && (lx <= x + width) && (ly >= y) && (ly <= y + height))
			return true;
		return false;
	}

	public UIZone getZone() {
		return _zone;
	}

	public int getIdCard() {
		return _idCard;
	}
	
	public String getRulesText() {
		return _rulesText;
	}
	
	public int getSourceId() {
		return _idSource;
	}
	
	public String getPowerToughness() {
		return _powerToughness;
	}
	
	public static int parseForID(String cardFullName) {
		int index_POUND = cardFullName.indexOf("#");
		int index_LAST_POUND = cardFullName.lastIndexOf("#");
		int id;
		
		if (index_POUND == index_LAST_POUND)
			id = Integer.parseInt(cardFullName.substring(index_POUND + 1));
		else
		{
			String omg = cardFullName.substring(index_LAST_POUND + 1);
			id = Integer.parseInt(omg);
		}
		return id;
	}
	
	public void clearCounters() {
		_counters.clear();
	}
	
	public void setCounters(String counterType, int nbCounters) {
		String entry = counterType.replace(CounterType.PLUS_ONE.toString(), "+1/+1");
		entry = entry.replace(CounterType.MINUS_ZERO_MINUS_ONE.toString(), "-0/-1");
		entry = entry.replace(CounterType.MINUS_ONE.toString(), "-1/-1");
		entry = entry.replace(CounterType.CHARGE.toString(), "Charge");
		entry = entry.replace(CounterType.PAGE.toString(), "Page");
		entry = entry.replace(CounterType.PETAL.toString(), "Petal");
		entry = entry.replace(CounterType.LORE.toString(), "Lore");
		entry = entry.replace(CounterType.SOOT.toString(), "Soot");
		entry = entry.replace(CounterType.FUNGUS.toString(), "Fungus");
		entry = entry.replace(CounterType.VERSE.toString(), "Verse");
		entry = entry.replace(CounterType.AGE.toString(), "Age");
		entry = entry.replace(CounterType.ICE.toString(), "Ice");
		entry = entry.replace(CounterType.FADE.toString(), "Fade");
		entry = entry.replace(CounterType.LOYALTY.toString(), "Loyalty");
		entry = entry.replace(CounterType.MUSTER.toString(), "Muster");
		entry = entry.replace(CounterType.TIME.toString(), "Time");
		entry = entry.replace(CounterType.MINING.toString(), "Mining");
		
		if (nbCounters > 1)
			entry += " x" + nbCounters;
		if (nbCounters > 0)
			_counters.add(entry);
	}
	
	public Vector<String> getCounters() {
		return _counters;
	}

	public void tap() {
		_bTapped = true;
	}
	
	public boolean isTapped() {
		return _bTapped ;
	}

	public void setBlockOrder(int iBlocker) {
		_blockOrder  = iBlocker;
	}
	
	public int getBlockOrder() {
		return _blockOrder;
	}
	
	public void addAuraID(int auraID) {
		_aurasID.add(auraID);
	}
	
	public Vector<Integer> getAurasID() {
		return _aurasID;
	}
	
	public int getHostID() {
		return _hostID;
	}
}
