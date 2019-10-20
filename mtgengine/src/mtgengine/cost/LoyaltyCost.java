package mtgengine.cost;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoyaltyCost {
	public enum Type { Put, Remove }
	
	private Type _type;
	private int _number;
	private boolean _xValue = false;
	
	public static boolean isValid(String loyaltyCost) {
		Pattern p = Pattern.compile("^([+-]?)([0-9]+|X)$");
		Matcher m = p.matcher(loyaltyCost);
		
		if (m.matches())
			return true;
		else
			return false;
	}
	
	public LoyaltyCost() { }
	
	public LoyaltyCost(String loyaltyCost) {
		Pattern p = Pattern.compile("^([+-]?)([0-9]+|X)$");
		Matcher m = p.matcher(loyaltyCost);
		String type;
		String number;

		if (m.matches()) {
			type = m.group(1);
			number = m.group(2);
			if (type.equals("+"))
				_type = Type.Put;
			else
				_type = Type.Remove;

			if (number.equals("X"))
				_xValue = true;
			else {
				_number = Integer.parseInt(number);
				_xValue = false;
			}
		}
	}
	
	public Type getType() {
		return _type;
	}
	
	public int getNumber() {
		return _number;
	}
	
	public boolean isXvalue() {
		return _xValue;
	}
	
	public LoyaltyCost clone() {
		LoyaltyCost lc = new LoyaltyCost();
		lc._number = this._number;
		lc._type = this._type;
		lc._xValue = this._xValue;
		return lc;
	}
}
