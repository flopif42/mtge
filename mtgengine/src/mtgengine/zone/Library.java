package mtgengine.zone;
import java.util.Random;
import java.util.Vector;

import mtgengine.Game;
import mtgengine.MtgObject;
import mtgengine.card.Card;
import mtgengine.player.Player;

public class Library extends Zone {
	private boolean _bSearchable = false;
	
	public Library(Game g, Player owner) {
		super(g, Name.Library, owner);
	}
	
	public void shuffle() {
		Random rnd = new Random();
		int n;
		Vector<MtgObject> tmpStack;

		for (int i = 0; i < 200; i++) {
			tmpStack = new Vector<MtgObject>();
			do {
				n = rnd.nextInt(_objects.size());
				tmpStack.add(_objects.remove(n));
			} while (_objects.size() > 0);
			_objects = tmpStack;
		}
	}
	
	public Card getObjectAt(int i) {
		return (Card) _objects.get(i);
	}
	
	public String toString() {
		if (!isSearchable())
			return "Library unavailable";
		return super.toString();
	}

	public boolean isSearchable() {
		return _bSearchable;
	}

	public Card getTopCard() {
		if (_objects.size() == 0)
			return null;
		return (Card) _objects.get(0);
	}

	public Vector<Card> getXTopCards(int number) {
		Vector<Card> ret = new Vector<Card>();
		if (number > _objects.size())
			number = _objects.size();
		while (number-- > 0) {
			ret.add((Card) _objects.get(number));
		}
		return ret;
	}
	
	public void setSearchable(boolean value) {
		_bSearchable = value;
	}

	public Vector<Card> getCards() {
		Vector<Card> ret = new Vector<Card>();
		for (MtgObject obj : _objects)
			ret.add((Card) obj);
		return ret;
	}
};
