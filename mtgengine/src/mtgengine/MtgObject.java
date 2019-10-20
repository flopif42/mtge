package mtgengine;

import java.util.Vector;

import mtgengine.ability.Evergreen;
import mtgengine.ability.Protection;
import mtgengine.card.Color;
import mtgengine.effect.ContinuousEffect;
import mtgengine.modifier.Modifier;
import mtgengine.modifier.ProtectionModifier;

public abstract class MtgObject {
	protected String _name;
	protected int _id = 0;
	private static int currentID = 1;
	private static int currentMorphID = 1;
	
	// Modifiers
	protected Vector<Modifier> _modifiers = new Vector<Modifier>();
	protected Vector<Object> _protections = new Vector<Object>();
	/* Evergreen abilities */
	protected Vector<Evergreen> _evergreenAbilities = new Vector<Evergreen>();

	// Abstract methods
	public abstract String getSystemName();
	public abstract boolean isTargetableBy(Game g, StackObject so);
	
	public MtgObject(String name) {
		_name = name;
		_id = generateID();
	}
	
	public int getID() {
		return _id;
	}

	public final void addModifiers(Modifier... mods) {
		for (Modifier mod : mods)
			if (!_modifiers.contains(mod))
				_modifiers.add(mod);
	}

	public Vector<Modifier> getModifiers() {
		return _modifiers;
	}
	
	public static int generateID() {
		currentID++;
		return currentID;
	}
	
	public static int generateMorphID() {
		currentMorphID++;
		return currentMorphID;
	}

	public static void resetID() {
		currentID = 1;
		currentMorphID = 1;
	}

	public void setProtectionFrom(Color... colors) {
		for (Color c : colors)
			_protections.addElement(c);
	}
	
	public void setProtectionFrom(Protection prot) {
		_protections.addElement(prot);
	}
	
	public boolean hasProtections() {
		return !_protections.isEmpty();
	}
	
	public Vector<Object> getProtections(Game g) {
		Vector<Object> ret = new Vector<Object>();
		
		// add protection printed on the card itself
		ret.addAll(_protections);
		
		// add protection granted from mofiers (ex: Mother of Runes)
		for (Modifier mod : _modifiers) {
			if (mod.getClass() == ProtectionModifier.class) {
				ProtectionModifier protMod = (ProtectionModifier) mod;
				ret.add(protMod.getProtectionFrom());
			}
		}
		
		// add protection granted from continuous effects coming from other cards
		for (ContinuousEffect ce : g.getContinuousEffects()) {
			Vector<Object> prots = ce.getProtections(g, this);
			for (Object prot : prots) {
				if (!ret.contains(prot))   // do this to manage redundancy
					ret.add(prot);
			}
		}
		return ret;
	}
	
	
}
