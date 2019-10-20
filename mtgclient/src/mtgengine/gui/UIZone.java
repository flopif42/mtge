package mtgengine.gui;

import java.util.Vector;

public class UIZone {
	private Vector<UICard> _cards;
	private String _name;
	public int x, y, width, height;
	
	public UIZone(String name) {
		_cards = new Vector<UICard>();
		_name = name;
	}
	
	public String getName() {
		return _name;
	}
	
	public int size() {
		return _cards.size();
	}

	public UICard get(int i) {
		try {
			if (i >= _cards.size() )
				return null;
			return _cards.get(i);
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("--------------------------------- _cards.size() = " + _cards.size());
			System.err.println("--------------------------------- i = " + i);
		}
		return null;
	}
	
	public void load(String zoneData) {
		int index = zoneData.indexOf("=");
		
		_cards.clear();
		String[] cards = (zoneData.substring(index + 1)).split(";"); // cards are separated by semicolon
		for (int i = 0; i < cards.length; i++) {
			if (cards[i].length() > 0)
				_cards.add(new UICard(this, cards[i]));
		}
	}

	public Vector<UICard> getCards() {
		return _cards;
	}
	
	public void clear() {
		_cards.clear();
	}
	
	public int getNbAuras() {
		int ret = 0;
		
		for (UICard card : _cards) {
			if (card.getHostID() != 0)
				ret++;
		}
		
		return ret;
	}
}
