package mtgengine.zone;
import java.util.Vector;

import mtgengine.Game;
import mtgengine.MtgObject;
import mtgengine.StackObject;
import mtgengine.card.Card;
import mtgengine.player.Player;
import mtgengine.type.CardType;
import mtgengine.type.CreatureType;
import mtgengine.type.Subtype;

public class Battlefield extends Zone {
	public Battlefield(Game g) {
		super(g, Name.Battlefield, null);
	}
	
	public void endUntilEOTEffects() {
		for (MtgObject obj : _objects)
			((Card)obj).endUntilEOTEffects();
	}
	
	public void endUntilYNTEffects(Player p) {
		for (MtgObject obj : _objects)
			((Card)obj).endUntilYNTEffects(_g, p);
	}

	public void cleanDamage() {
		for (MtgObject obj : _objects) {
			((Card) obj).cleanDamage();
		}
	}

	public Vector<MtgObject> getTargetableObjectsOfType(CardType type, StackObject so) {
		Vector<MtgObject> ret = new Vector<MtgObject>();
		
		for (MtgObject obj : _objects) {
			if (obj.getClass() == Card.class) {
				Card permanent = (Card) obj;
				
				if (permanent.hasCardTypeGlobal(_g, type) && permanent.isTargetableBy(_g, so))
					ret.add(obj);
			}
		}
		return ret; 
	}

	public Vector<Card> getObjectsControlledBy(int idPlayer) {
		Vector<Card> ret = new Vector<Card>();
		for (MtgObject o : _objects) {
			Card c = (Card) o;
			if (c.getController(_g) == _g.findPlayerByID(idPlayer))
				ret.add(c);				
		}
		return ret;
	}	
	
	/**
	 * Return all creatures controlled by player p
	 * @param p
	 * @return
	 */
	public Vector<Card> getCreaturesControlledBy(Player p) {
		Vector<Card> ret = new Vector<Card>();
		for (MtgObject o : _objects) {
			Card c = (Card) o;
			if ((c.getController(_g) == p) && (c.isCreature(_g)))
				ret.add(c);				
		}
		return ret;
	}

	/**
	 * Return all enchantments controlled by player p
	 * @param p
	 * @return
	 */
	public Vector<Card> getEnchantmentsControlledBy(Player p) {
		Vector<Card> ret = new Vector<Card>();
		for (MtgObject o : _objects) {
			Card c = (Card) o;
			if ((c.getController(_g) == p) && (c.isEnchantment(_g)))
				ret.add(c);				
		}
		return ret;
	}

	
	/**
	 * Return all untapped creatures controlled by player p
	 * @param p
	 * @return
	 */
	public Vector<Card> getUntappedCreaturesControlledBy(Player p) {
		Vector<Card> ret = new Vector<Card>();
		for (MtgObject o : _objects) {
			Card c = (Card) o;
			if ((c.getController(_g) == p) && c.isCreature(_g) && !c.isTapped())
				ret.add(c);				
		}
		return ret;
	}
	
	/**
	 * Return all Artifacts controlled by player p
	 * @param p
	 * @return
	 */
	public Vector<Card> getArtifactsControlledBy(Player p) {
		Vector<Card> ret = new Vector<Card>();
		for (MtgObject o : _objects) {
			Card c = (Card) o;
			if ((c.getController(_g) == p) && (c.isArtifact(_g)))
				ret.add(c);				
		}
		return ret;
	}

	/**
	 * Return all Artifacts
	 * @param p
	 * @return
	 */
	public Vector<Card> getArtifacts() {
		Vector<Card> ret = new Vector<Card>();
		for (MtgObject o : _objects) {
			Card c = (Card) o;
			ret.add(c);				
		}
		return ret;
	}

	
	/**
	 * Return all Planeswalkers controlled by player p
	 * @param p
	 * @return
	 */
	public Vector<Card> getPlaneswalkersControlledBy(Player p) {
		Vector<Card> ret = new Vector<Card>();
		for (MtgObject o : _objects) {
			Card c = (Card) o;
			if ((c.getController(_g) == p) && (c.isPlaneswalkerCard()))
				ret.add(c);				
		}
		return ret;
	}

	
	/**
	 * Return all Planeswalkers
	 * @param p
	 * @return
	 */
	public Vector<Card> getPlaneswalkers() {
		Vector<Card> ret = new Vector<Card>();
		for (MtgObject o : _objects) {
			Card c = (Card) o;
			ret.add(c);				
		}
		return ret;
	}

	
	/**
	 * Return all Lands controlled by player p
	 * @param p
	 * @return
	 */
	public Vector<Card> getLandsControlledBy(Player p) {
		Vector<Card> ret = new Vector<Card>();
		for (MtgObject o : _objects) {
			Card c = (Card) o;
			if ((c.getController(_g) == p) && (c.isLand(_g)))
				ret.add(c);				
		}
		return ret;
	}
	
	/**
	 * Return all permanents controlled by player p
	 * @param p
	 * @return
	 */
	public Vector<Card> getPermanentsControlledBy(Player p) {
		Vector<Card> ret = new Vector<Card>();
		for (MtgObject o : _objects) {
			Card c = (Card) o;
			if (c.getController(_g) == p)
				ret.add(c);				
		}
		return ret;
	}

	public Vector<MtgObject> getTargetableCreaturesControlledBy(Player p, StackObject so) {
		Vector<MtgObject> ret = new Vector<MtgObject>();
		for (MtgObject o : _objects) {
			Card c = (Card) o;
			if ((c.getController(_g) == p) && c.isCreature(_g) && c.isOTB(_g) && c.isTargetableBy(_g, so))
				ret.add(c);				
		}
		return ret;
	}
	
	public Vector<MtgObject> getTargetableLandsControlledBy(Player p, StackObject so) {
		Vector<MtgObject> ret = new Vector<MtgObject>();
		for (MtgObject o : _objects) {
			Card c = (Card) o;
			if ((c.getController(_g) == p) && c.isLand(_g) && c.isOTB(_g) && c.isTargetableBy(_g, so))
				ret.add(c);				
		}
		return ret;
	}

	/**
	 * Returns a vector with creatures that have the least toughness.
	 * @return
	 */
	public Vector<Card> getCreaturesWithLeastToughness() {
		int smallestToughness = 10000;
		int toughness;
		Vector<Card> ret = new Vector<Card>();
		
		for (Card creature : this.getCreatures()) {
			toughness = creature.getToughness(_g);
			
			// The toughness equals le smallest : add creature to flagged creature
			if (toughness == smallestToughness)
				ret.add(creature);
			
			// The toughness is smaller than the smallest : reset and add creature to flagged creature
			if (toughness < smallestToughness) {
				smallestToughness = toughness;
				ret.clear();
				ret.add(creature);
			}
		}
		return ret;
	}
	
	public Vector<Card> getCreatures() {
		Vector<Card> ret = new Vector<Card>();
		for (MtgObject o : _objects) {
			Card c = (Card) o;
			if (c.isCreature(_g))
				ret.add(c);				
		}
		return ret;
	}
	
	public Vector<Card> getLands() {
		Vector<Card> ret = new Vector<Card>();
		for (MtgObject o : _objects) {
			Card c = (Card) o;
			if (c.isLand(_g))
				ret.add(c);				
		}
		return ret;
	}
	
	public Vector<Card> getEnchantments() {
		Vector<Card> ret = new Vector<Card>();
		for (MtgObject o : _objects) {
			Card c = (Card) o;
			if (c.isEnchantment(_g))
				ret.add(c);				
		}
		return ret;
	}

	public Vector<Card> getSagas() {
		Vector<Card> ret = new Vector<Card>();
		for (MtgObject o : _objects) {
			Card c = (Card) o;
			if (c.isEnchantment(_g) && c.hasSubtypePrinted(Subtype.SAGA))
				ret.add(c);				
		}
		return ret;
	}

	
	public Vector<MtgObject> getTargetablePermanentsControlledBy(Player p, StackObject so) {
		Vector<MtgObject> ret = new Vector<MtgObject>();
		for (MtgObject o : _objects) {
			Card c = (Card) o;
			if ((c.getController(_g) == p) && c.isOTB(_g) && c.isTargetableBy(_g, so))
				ret.add(c);				
		}
		return ret;
	}
	
	public int getNumberOfControlledBy(Subtype st, Player p) {
		int nb = 0;
		
		for (MtgObject obj : _objects) {
			Card permanent = (Card) obj;
			if (permanent.hasSubtypeGlobal(_g, st))
				nb++;
		}
		return nb;
	}
	
	/**
	 * Returns the number of permanents of a certain quality (card types or creature types) controlled by player p
	 * if p is null, counts all objects on the battlefield
	 * @param elem
	 * @param p
	 * @return
	 */
	public int getNumberOfControlledBy(CardType ct, Player p) {
		switch (ct) {
		case ARTIFACT:
			if (p != null)
				return this.getArtifactsControlledBy(p).size();
			else
				return this.getArtifacts().size();
			
		case CREATURE:
			if (p != null)
				return this.getCreaturesControlledBy(p).size();
			else
				return this.getCreatures().size();

		case ENCHANTMENT:
			if (p != null)
				return this.getEnchantmentsControlledBy(p).size();
			else
				return this.getEnchantments().size();

		case LAND:
			if (p != null)
				return this.getLandsControlledBy(p).size();
			else
				return this.getLands().size();

		case PLANESWALKER:
			if (p != null)
				return this.getPlaneswalkersControlledBy(p).size();
			else
				return this.getPlaneswalkers().size();

		default:
			break;
		}
		return 0;
	}

	/**
	 * if pl is null, returns all on battlefield
	 * @param elf
	 * @param pl
	 * @return
	 */
	public int getNumberOfCreatureTypesControlledBy(CreatureType creaType, Player pl) {
		int number = 0;
		
		for (MtgObject o : _objects) {
			Card c = (Card) o;
			if (c.isCreature(_g) && c.hasCreatureType(_g, creaType)) {
				if ((pl != null) && (c.getController(_g) == pl))
					number++;
				else
					number++;
			}
		}
		
		return number;
	}
};

