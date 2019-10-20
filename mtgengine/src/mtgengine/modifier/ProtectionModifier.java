package mtgengine.modifier;

import mtgengine.StackObject;

public class ProtectionModifier extends Modifier {
	private Object _protection;
	
	public ProtectionModifier(StackObject source, Object protection, Modifier.Duration duration) {
		_source = source;
		_protection = protection;
		_duration = duration;
	}

	public Object getProtectionFrom() {
		return _protection;
	}
}
