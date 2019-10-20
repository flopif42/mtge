package mtgengine.zone;

import java.util.Vector;

import mtgengine.Game;
import mtgengine.MtgObject;
import mtgengine.StackObject;
import mtgengine.ability.ActivatedAbility;
import mtgengine.card.Card;

public class Stack extends Zone {
	public Stack(Game g) {
		super(g, Name.Stack, null);
	}

	public StackObject getTopObject() {
		return (StackObject) _objects.get(0);
	}

	/**
	 * Returns spells on the stack
	 * @return
	 */
	public Vector<MtgObject> getSpells() {
		Vector<MtgObject> ret = new Vector<MtgObject>();
		for (MtgObject obj : _objects)
			if (obj.getClass() == Card.class)
				ret.add(obj);
		return ret;
	}
	
	/**
	 * Returns Instant and Sorcery spells on the stack
	 * @return
	 */
	public Vector<MtgObject> getInstantAndSorcerySpells() {
		Vector<MtgObject> ret = new Vector<MtgObject>();
		for (MtgObject obj : _objects)
			if (obj.getClass() == Card.class) {
				Card spell = (Card) obj;
				if (spell.isInstantCard() || spell.isSorceryCard())
					ret.add(obj);
			}
		return ret;
	}
	
	/*  */
	public Vector<MtgObject> getArtifactAndEnchantmentSpells() {
		Vector<MtgObject> ret = new Vector<MtgObject>();
		for (MtgObject obj : _objects)
			if (obj.getClass() == Card.class) {
				Card spell = (Card) obj;
				if (spell.isArtifactCard() || spell.isEnchantmentCard())
					ret.add(obj);
			}
		return ret;
	}
	
	/**
	 * Return nonspells on the stack : activated and triggered abilities
	 * @return
	 */
	public Vector<MtgObject> getAbilities() {
		Vector<MtgObject> ret = new Vector<MtgObject>();
		for (MtgObject obj : _objects)
			if (!(obj instanceof Card))
				ret.add(obj);
		return ret;
	}
	
	/**
	 * Return activated abilities on the stack
	 * @return
	 */
	public Vector<MtgObject> getActivatedAbilities() {
		Vector<MtgObject> ret = new Vector<MtgObject>();
		for (MtgObject obj : _objects)
			if (obj instanceof ActivatedAbility)
				ret.add(obj);
		return ret;
	}
	
	/**
	 * Returns creature spells on the stack
	 * @return
	 */
	public Vector<MtgObject> getCreatureSpells() {
		Vector<MtgObject> ret = new Vector<MtgObject>();
		for (MtgObject obj : _objects)
			if (obj.getClass() == Card.class)
			{
				if (((Card) obj).isCreatureCard())
					ret.add(obj);
			}
		return ret;
	}
	
	/**
	 * Returns noncreature spells on the stack
	 * @return
	 */
	public Vector<MtgObject> getNonCreatureSpells() {
		Vector<MtgObject> ret = new Vector<MtgObject>();
		Card c;
		
		for (MtgObject obj : _objects) {
			if (obj instanceof Card) {
				c = (Card) obj;
				if (!c.isCreatureCard())
					ret.add(c);
			}
		}
		return ret;
	}
	
	public Vector<StackObject> getStackObjects() {
		Vector<StackObject> ret = new Vector<StackObject>();
		StackObject so;
		
		for (MtgObject obj : _objects) {
				so = (StackObject) obj;
				ret.add(so);
			}
		return ret;
	}
};
