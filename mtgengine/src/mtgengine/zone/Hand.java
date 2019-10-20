package mtgengine.zone;

import java.util.Vector;

import mtgengine.Game;
import mtgengine.MtgObject;
import mtgengine.card.Card;
import mtgengine.player.Player;

public class Hand extends Zone {
	public Hand(Game g, Player owner) {
		super(g, Name.Hand, owner);
	}

	public Vector<Card> getCreatureCards() {
		Vector<Card> ret = new Vector<Card>();
		
		for (MtgObject obj : _objects) {
			Card card = (Card) obj;
			
			if (card.isCreatureCard())
				ret.add(card);
		}
		return ret;
	}

	public Vector<Card> getLandCards() {
		Vector<Card> ret = new Vector<Card>();
		
		for (MtgObject obj : _objects) {
			Card card = (Card) obj;
			
			if (card.isLandCard())
				ret.add(card);
		}
		return ret;
	}

	public Vector<Card> getCards() {
		Vector<Card> ret = new Vector<Card>();
		for (MtgObject obj : _objects)
			ret.add((Card) obj);
		return ret;
	}

	public void shuffleIntoLibrary() {
		while (_objects.size() > 0)
			_g.move_HND_to_TOPLIB((Card) _objects.get(0));
		_owner.shuffle();
	}
};
