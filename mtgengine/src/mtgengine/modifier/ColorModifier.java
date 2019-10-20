package mtgengine.modifier;

import java.util.Vector;

import mtgengine.StackObject;
import mtgengine.card.Color;

public class ColorModifier extends Modifier {
	private Vector<Color> _colors = new Vector<Color>();
	
	public ColorModifier(StackObject source, Modifier.Duration duration, Color... colors) {
		_source = source;
		for (Color color : colors)
			_colors.add(color);	
		_duration = duration;
	}

	public Vector<Color> getColors() {
		return _colors;
	}
}
