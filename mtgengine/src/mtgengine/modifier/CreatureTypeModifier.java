package mtgengine.modifier;

import java.util.Vector;

import mtgengine.StackObject;
import mtgengine.type.CreatureType;

public class CreatureTypeModifier extends Modifier {
	private Vector<CreatureType> _creatureTypes = new Vector<CreatureType>();
	
	public CreatureTypeModifier(StackObject source, Modifier.Duration duration, CreatureType... types) {
		_source = source;
		for (CreatureType t : types)
			_creatureTypes.add(t);	
		_duration = duration;
	}

	public Vector<CreatureType> getCreatureTypes() {
		return _creatureTypes;
	}
}
