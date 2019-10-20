package mtgengine.modifier;

import mtgengine.StackObject;

/**
 * This modifier is used when a card is granted an ability. For example, when spawning pool is animated, it gains the ability to regenerate.
 * The ability can either be an activated ability or a triggered ability (i.e. Raging Ravine)
 * @author nguyenf
 *
 */
public class AbilityModifier extends Modifier {
	private StackObject _ability;
	
	public AbilityModifier(StackObject source, StackObject ability, Modifier.Duration duration) {
		_source = source;
		_ability = ability;
		_duration = duration;
	}

	public StackObject getAbility() {
		return _ability;
	}
}
