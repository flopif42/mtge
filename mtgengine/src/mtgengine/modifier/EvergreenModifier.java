package mtgengine.modifier;

import java.util.Vector;

import mtgengine.StackObject;
import mtgengine.ability.Evergreen;

public class EvergreenModifier extends Modifier {
	private Vector<Evergreen> _abilities = new Vector<Evergreen>();
	
	public EvergreenModifier(StackObject source, Modifier.Duration duration, Evergreen... abilities) {
		_source = source;
		for (Evergreen ability : abilities)
			_abilities.add(ability);
		_duration = duration;
	}


	public Vector<Evergreen> getEvergreens() {
		return _abilities;
	}
}
