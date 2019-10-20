package mtgengine.card;

public class UntapOptional {
	public enum State { Untap, LeaveTapped };
	
	private State _state = null;
	private boolean _bAnswered = false;
	
	public void setOptionalUntapState(State answer) {
		_state = answer;
		_bAnswered = true;
	}
	
	public State getChosenState() {
		_bAnswered = false; // we need to reset the answered variable for next turn
		return _state;
	}
	
	public boolean isAnswered() {
		return _bAnswered;
	}
}
