package mtgengine.modifier;

import mtgengine.StackObject;
import mtgengine.card.Card;

public class PTModifier extends Modifier {
	private String _definition;
	
	public PTModifier(StackObject source, String definition, Modifier.Operation operation, Modifier.Duration duration) {
		_source = source;
		_operation = operation;
		_definition = definition;
		_duration = duration;
	}
	
	public String set() {
		return _definition;
	}
	
	public String add(String oldValue) {
		return Card.addPT(oldValue, _definition);
	}
	
	public String switchPT(String oldValue) {
		int power;
		int toughness;
		
		// Decompose old value
		String[] ptArray = oldValue.split("/");
		power = Integer.parseInt(ptArray[0]);
		toughness = Integer.parseInt(ptArray[1]);
		
		// Compose and return new value
		return String.format("%d/%d", toughness, power);
	}
}
