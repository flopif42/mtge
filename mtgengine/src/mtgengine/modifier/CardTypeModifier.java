package mtgengine.modifier;

import java.util.Vector;

import mtgengine.StackObject;
import mtgengine.type.CardType;

/**
 * This modifier adds a new card type to the card. For example, when Treetop Village is animated, it gains the Creature card type.
 * @author nguyenf
 *
 */
public class CardTypeModifier extends Modifier {
	private Vector<CardType> _addedTypes = new Vector<CardType>();
	private Vector<CardType> _setTypes = new Vector<CardType>();
	
	public CardTypeModifier(StackObject source, Modifier.Operation operation, Modifier.Duration duration, CardType... types) {
		_source = source;
		_duration = duration;
		
		if (operation == Modifier.Operation.ADD) {
			for (CardType type : types)
				_addedTypes.add(type);	
		}
		
		else if (operation == Modifier.Operation.SET) {
			for (CardType type : types)
				_setTypes.add(type);
		}
	}

	public Vector<CardType> getAddedTypes() {
		return _addedTypes;
	}
	
	public Vector<CardType> getSetTypes() {
		return _setTypes;
	}
}
