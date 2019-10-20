package mtgengine.cost;

import java.util.HashMap;

import mtgengine.cost.AdditionalCost.Requirement;

public class Cost {
	private ManaCost _manaCost;
	private HashMap<AdditionalCost.Requirement, Boolean> _additionalCosts = new HashMap<Requirement, Boolean>();

	/* This is used only if it's a loyalty ability */
	private LoyaltyCost _loyaltyCost = null;
	
	public Cost(ManaCost manaCost) {
		_manaCost = manaCost;
		for (Requirement req : Requirement.values())
			_additionalCosts.put(req, Boolean.FALSE);
	}

	public void addAdditionalCost(Requirement req) {
		if (req != null)
			_additionalCosts.put(req, Boolean.TRUE);
	}
	
	public boolean requiresAdditionalCost(Requirement req) {
		return _additionalCosts.get(req);
	}

	public void setLoyaltyCost(LoyaltyCost lc) {
		_loyaltyCost = lc;
	}

	public LoyaltyCost getLoyaltyCost() {
		return _loyaltyCost;
	}
	
	@SuppressWarnings("unchecked")
	public Cost clone() {
		Cost clone = new Cost(_manaCost);
		clone._additionalCosts = (HashMap<Requirement, Boolean>) _additionalCosts.clone();
		if (_loyaltyCost != null)
			clone._loyaltyCost = _loyaltyCost.clone();
		return clone;
	}

	public void setManaCost(ManaCost manaCost) {
		_manaCost = manaCost;
	}
	
	public ManaCost getManaCost() {
		return _manaCost;
	}
	
	public String toString() {
		String ret = "";
		if (_manaCost != null) {
			ret += _manaCost.toString() + ", ";
		}
		for (Requirement ac : _additionalCosts.keySet()) {
			if (_additionalCosts.get(ac))
				ret += ac.toString() + ", ";
		}

		// remove the ending ", "
		if (ret.length() >=2 )
			ret = ret.substring(0, ret.length()-2);
		return ret;
	}
}
