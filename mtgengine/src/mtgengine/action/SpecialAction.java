package mtgengine.action;

import mtgengine.MtgObject;
import mtgengine.card.Card;

public class SpecialAction implements PerformableAction {
	public enum Option {
		PLAY_LAND,
		TURN_FACEUP,
		SUSPEND
	}
	
	private Option _option;
	private Card _source;
	private int _id;
	
	public SpecialAction(Card source, Option option/*, String name*/) {
		_source = source;
		_option = option;
		_id = MtgObject.generateID();
	}
	
	public Option getOption() {
		return _option;
	}
	
	public String getPrintActionsText() {
		switch (_option)
		{
		case PLAY_LAND:
			return "Play land";

		case TURN_FACEUP:
			return "Turn face up for its morph/megamorph cost.";
			
		case SUSPEND:
			return "Suspend <i>(exile with N time counters)</i>";
		default:
			return _option.toString();
		}
	}
	
	public String getSystemName() {
		String ret = _source.getSystemName() + "#" + _id + "~" + getPrintActionsText();
		return ret;
	}

	@Override
	public int getID() {
		return _id;
	}
	@Override
	public Type getActionType() {
		return Type.TAKE_SPECIAL_ACTION;
	}

}
