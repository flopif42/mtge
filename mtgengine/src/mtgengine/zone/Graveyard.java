package mtgengine.zone;

import java.util.Vector;

import mtgengine.Game;
import mtgengine.MtgObject;
import mtgengine.card.Card;
import mtgengine.player.Player;

public class Graveyard extends Zone {
	public Graveyard(Game g, Player owner) {
		super(g, Name.Graveyard, owner);
	}

	public Vector<Card> getCards() {
		Vector<Card> ret = new Vector<Card>();
		for (MtgObject obj : _objects)
			ret.add((Card) obj);
		return ret;
	}
	
	/**
	 * Returns the number of creature cards in the graveyard.
	 * @return
	 */
	public int getNbCreatures() {
		int number = 0;
		Card c;
		
		for (MtgObject obj : _objects) {
			c = (Card) obj;
			if (c.isCreatureCard())
				number++;
		}
		return number;
	}
	
	/**
	 * Returns the number of land cards in the graveyard
	 * @return
	 */
	public int getNbLands() {
		int number = 0;
		Card c;
		
		for (MtgObject obj : _objects) {
			c = (Card) obj;
			if (c.isLandCard())
				number++;
		}
		return number;
	}
	
	/**
	 * Returns a vector with only creature cards.
	 * @return
	 */
	public Vector<Card> getCreatureCards() {
		Vector<Card> ret = new Vector<Card>();
		Card card;
		for (MtgObject obj : _objects) {
			card = (Card) obj; 
			if (card.isCreatureCard())
				ret.add(card);
		}
		return ret;
	}
	
	/**
	 * Returns a vector with only planeswalker cards.
	 * @return
	 */
	public Vector<Card> getPlaneswalkerCards() {
		Vector<Card> ret = new Vector<Card>();
		Card card;
		for (MtgObject obj : _objects) {
			card = (Card) obj; 
			if (card.isPlaneswalkerCard())
				ret.add(card);
		}
		return ret;
	}

	
	/**
	 * Returns a vector with only land cards.
	 * @return
	 */
	public Vector<Card> getLandCards() {
		Vector<Card> ret = new Vector<Card>();
		Card card;
		for (MtgObject obj : _objects) {
			card = (Card) obj; 
			if (card.isLandCard())
				ret.add(card);
		}
		return ret;
	}

	/**
	 * Returns a vector with only land cards.
	 * @return
	 */
	public Vector<MtgObject> getEnchantmentCards() {
		Vector<MtgObject> ret = new Vector<MtgObject>();
		Card card;
		for (MtgObject obj : _objects) {
			card = (Card) obj; 
			if (card.isEnchantmentCard())
				ret.add(card);
		}
		return ret;
	}

	
	/**
	 * Returns a vector with only sorcery cards.
	 * @return
	 */
	public Vector<MtgObject> getSorceryCards() {
		Vector<MtgObject> ret = new Vector<MtgObject>();
		Card card;
		for (MtgObject obj : _objects) {
			card = (Card) obj; 
			if (card.isSorceryCard())
				ret.add(card);
		}
		return ret;
	}
	
	/**
	 * Returns a vector with only instant cards.
	 * @return
	 */
	public Vector<MtgObject> getInstantCards() {
		Vector<MtgObject> ret = new Vector<MtgObject>();
		Card card;
		for (MtgObject obj : _objects) {
			card = (Card) obj; 
			if (card.isInstantCard())
				ret.add(card);
		}
		return ret;
	}

	
	/**
	 * Returns a vector with only enchantment cards.
	 * @return
	 */
	public Vector<Card> getEnchantments() {
		Vector<Card> ret = new Vector<Card>();
		Card card;
		for (MtgObject obj : _objects) {
			card = (Card) obj; 
			if (card.isEnchantmentCard())
				ret.add(card);
		}
		return ret;
	}
	
	public boolean containsDredgeCards() {
		for (Card c : getCards()) {
			if (c.hasStaticAbility("dredge") && (Integer.parseInt(c.getStaticAbility("dredge").getParameter()) > 0))
				return true;
		}
		return false;
	}
	
	public void shuffleIntoLibrary() {
		while (_objects.size() > 0)
			_g.move_GYD_to_TOPLIB((Card) _objects.get(0));
		_owner.shuffle();
	}
	
	public void endUntilEOTEffects() {
		for (MtgObject obj : _objects) {
			((Card)obj).setPutIntoGraveyardThisTurn(false);
		}
	}
	
	
	// This function returns the number of card types among cards in this graveyard (for Delirium)
	public int getNbCardtypes() {
		int nbTypes = 0;
		boolean bArti = false;
		boolean bCrea = false;
		boolean bEnch = false;
		boolean bInst = false;
		boolean bLand = false;
		boolean bPlan = false;
		boolean bSorc = false;
		boolean bTrib = false;

		for (MtgObject o : _objects) {
			Card card = (Card) o;
			
			if (card.isArtifact(_g) && !bArti) {
				nbTypes++;
				bArti = true;
			}
			if (card.isCreatureCard() && !bCrea) {
				nbTypes++;
				bCrea = true;
			}
			if (card.isEnchantmentCard() && !bEnch) {
				nbTypes++;
				bEnch = true;
			}
			if (card.isInstantCard() && !bInst) {
				nbTypes++;
				bInst = true;
			}
			if (card.isLandCard() && !bLand) {
				nbTypes++;
				bLand = true;
			}
			if (card.isPlaneswalkerCard() && !bPlan) {
				nbTypes++;
				bPlan = true;
			}
			if (card.isSorceryCard() && !bSorc) {
				nbTypes++;
				bSorc = true;
			}

			if (card.isTribalCard() && !bTrib) {
				nbTypes++;
				bTrib = true;
			}

		}
		
		return nbTypes;
	}
};
