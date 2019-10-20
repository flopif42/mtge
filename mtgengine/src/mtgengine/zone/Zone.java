package mtgengine.zone;
import java.util.Random;
import java.util.Vector;

import mtgengine.Game;
import mtgengine.MtgObject;
import mtgengine.StackObject;
import mtgengine.card.Card;
import mtgengine.card.CardFactory;
import mtgengine.player.Player;

public class Zone {
	public enum Name { Battlefield, Hand, Library, Stack, Graveyard, Exile, Command, Revealed };
	protected Vector<MtgObject> _objects;
	protected Game _g;
	private Name _zoneName;
	protected Player _owner = null;
	
	public Zone(Game g, Name zoneName, Player owner) {
		_g = g;
		_zoneName = zoneName;
		_objects = new Vector<MtgObject>();
		_owner = owner;
	}
	
	public Name getName() {
		return _zoneName;
	}
	
	public Vector<MtgObject> getObjects() {
		return _objects;
	}
	
	public Vector<Card> getPermanents() {
		Vector<Card> ret = new Vector<Card>();
		for (MtgObject obj : _objects)
			ret.add((Card) obj);
		return ret;
	}
	
	public Vector<MtgObject> getTargetableObjects(StackObject so) {
		Vector<MtgObject> ret = new Vector<MtgObject>();
		for (MtgObject o : _objects)
		{
			if ((_zoneName != Name.Battlefield) || o.isTargetableBy(_g, so))
				ret.add(o);				
		}
		return ret;
	}
	
	public final MtgObject getObjectByID(int cardID) {
		for (MtgObject obj : _objects) {
			if (obj.getID() == cardID)
				return obj;
		}
		return null;
	}
	
	public void add(String... cardnames) {
		Card card;
		for (String cardname : cardnames) {
			card = CardFactory.create(cardname);
			if (card == null)
				return;
			card.setOwner(_owner);
			card.setController(_g, _owner);
			addObject(card);
		}
	}
	
	public int indexOf(MtgObject obj) {
		return _objects.indexOf(obj);
	}
	
	public MtgObject removeObject(int i) {
		return _objects.remove(i);
	}

	public MtgObject getObjectAt(int i) {
		return _objects.get(i);
	}
	
	public boolean removeObject(MtgObject c) {
		return _objects.remove(c);
	}

	public void addObject(MtgObject obj) {
		addObject(obj, 0);
	}
	
	public void addObject(MtgObject obj, int index) {
		if (obj != null)
		{
			if (index == -1)
				index = _objects.size();
			_objects.add(index, obj);
		}
	}

	public boolean isEmpty() {
		return (_objects.size() == 0);
	}

	public int size() {
		return _objects.size();
	}
	
	public static Vector<Card> shuffle(Vector<Card> cards) {
		Random rnd = new Random();
		int n;
		Vector<Card> tmpStack = null;

		for (int i = 0; i < 100; i++) {
			tmpStack = new Vector<Card>();
			do {
				n = rnd.nextInt(cards.size());
				tmpStack.add(cards.remove(n));
			} while (cards.size() > 0);
			cards = tmpStack;
		}
		return tmpStack;
	}
	
	public String toString() {
		String ret = _zoneName.toString() + "=";
		for (int i = 0; i < _objects.size(); i++)
			ret += (_objects.get(i).getSystemName() + ";");
		ret += "|";
		return ret;
	}
	
	public Vector<String> toStringVector() {
		Vector<String> ret = new Vector<String>();
		
		for (int i = 0; i < _objects.size(); i++)
			ret.add(0, _objects.get(i).getSystemName());
		return ret;
	}

	public MtgObject findCardByName(String search) {
		Card c;
		for (MtgObject obj : _objects) {
			c = (Card) obj;
			if (c.isNamed(search))
				return c;
		}
		return null;
	}
	
	public boolean contains(MtgObject object) {
		return _objects.contains(object);
	}

};
