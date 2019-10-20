package mtgengine.action;

import mtgengine.MtgObject;
import mtgengine.card.Card;
import mtgengine.cost.AlternateCost;

public class SpellCast implements PerformableAction{
	public enum Option {
		CAST,
		CAST_FACEDOWN,
		CAST_FROM_EXILE, 
		CAST_WITH_BUYBACK,
		CAST_WITH_FLASHBACK,
		CAST_WITH_KICKER,
		CAST_WITH_AWAKEN, 
		CAST_WITH_SPECTACLE,
		CAST_WITH_ALTERNATE_COST,
		CAST_USING_SUSPEND
	}
	
	private Card _source;
	private int _id;
	private Option _option;
	private String _parameter;
	private AlternateCost _alternateCost;
	private boolean _bWithoutPayingManaCost = false;
	
	/**
	 * Default constructor
	 * @param source
	 * @param option
	 */
	public SpellCast(Card source, Option option, String parameter, AlternateCost alternateCost) {
		_id = MtgObject.generateID();
		_option = option;
		_parameter = parameter;
		_source = source;
		_alternateCost = alternateCost;
	}
	
	/**
	 * 
	 * @return
	 */
	public Option getOption() {
		return _option;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getDescription() {
		switch (_option)
		{
		case CAST_FACEDOWN:
			return "Cast face down as a 2/2 creature for {3}";
		
		case CAST_WITH_FLASHBACK:
			return "Cast with Flashback";
			
		case CAST_WITH_BUYBACK:
			return "Cast with Buyback";

		case CAST_WITH_SPECTACLE:
			return "Cast with Spectacle";
			
		case CAST_WITH_KICKER:
			return "Cast with " + _parameter + " kicker";
			
		case CAST:
			return "Cast";
			
		case CAST_FROM_EXILE:
			return "Cast from Exile";
			
		case CAST_WITH_AWAKEN:
			return "Cast with Awaken";
			
		case CAST_WITH_ALTERNATE_COST:
			return "Cast using alternate cost";
			
		default:
			return _option.toString();
		}
	}
	
	public String getSystemName() {
		String ret = _source.getSystemName() + "#" + _id + "~" + getDescription();
		return ret;
	}

	@Override
	public int getID() {
		return _id;
	}

	public AlternateCost getAlternateCost() {
		return _alternateCost;
	}

	public void setWithoutPayingManaCost() {
		_bWithoutPayingManaCost = true;
	}
	
	public boolean getWithoutPayingManaCost() {
		return _bWithoutPayingManaCost;
	}
	
	@Override
	public Type getActionType() {
		return Type.CAST_SPELL;
	}
	
	public String getParameter() {
		return _parameter;
	}
}
