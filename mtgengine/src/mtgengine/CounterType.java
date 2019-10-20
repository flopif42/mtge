package mtgengine;

public enum CounterType {
	// counters on creatures
	PLUS_ONE,  				// +1/+1
	MINUS_ONE, 				// -1/-1
	MINUS_ZERO_MINUS_ONE,	// -0/-1

	// counters on planeswalkers
	LOYALTY,
	
	// counters on other permanents
	CHARGE,
	AGE,
	MUSTER,
	ICE,
	LORE,
	MINING,
	PAGE,
	PETAL,
	SOOT,
	VERSE,
	FUNGUS,
	
	// vanishing and fading
	TIME,
	FADE,
	
	// counters assigned to players
	POISON,
	ENERGY;

	public static CounterType parse(String counterType) {
		if (counterType.equals("+1/+1"))
			return PLUS_ONE;
		else if (counterType.equals("fade"))
			return FADE;
		else if (counterType.equals("time"))
			return TIME;
		else if (counterType.equals("charge"))
			return CHARGE;
		else if (counterType.equals("mining"))
			return MINING;
		else if (counterType.equals("ice"))
			return ICE;
		else {
			System.err.println("Error, unknown counter type : " + counterType);
			System.exit(1);
			return null;
		}
	}
};
