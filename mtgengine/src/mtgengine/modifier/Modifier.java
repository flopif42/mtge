package mtgengine.modifier;

import mtgengine.StackObject;

public class Modifier {
	public enum Operation { ADD, SET, SWITCH }
	public enum Duration { UNTIL_END_OF_TURN, UNTIL_YOUR_NEXT_TURN, PERMANENTLY }

	protected Modifier.Operation _operation;
	protected Modifier.Duration _duration;
	protected StackObject _source;
	
	public Modifier.Duration getDuration() {
		return _duration;
	}
	
	public final StackObject getSource() {
		return _source;
	}

	public Modifier.Operation getOperation() {
		return _operation;
	}
}
