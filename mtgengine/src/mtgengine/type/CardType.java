package mtgengine.type;

public enum CardType { TRIBAL, ARTIFACT, CREATURE, ENCHANTMENT, INSTANT, LAND, PLANESWALKER, SORCERY;

	public static CardType parse(String str) {
		str = str.toLowerCase();
		if (str.equals("artifact"))
			return ARTIFACT;
		
		if (str.equals("creature"))
			return CREATURE;
		
		if (str.equals("enchantment"))
			return ENCHANTMENT;
		
		if (str.equals("instant"))
			return INSTANT;
		
		if (str.equals("land"))
			return LAND;
		
		if (str.equals("planeswalker"))
			return PLANESWALKER;
		
		if (str.equals("sorcery"))
			return SORCERY;
		
		if (str.equals("tribal"))
			return TRIBAL;
		
		return null;
	}

	public String toString() {
		String fullname = null;
		
		switch(this) {
		case ARTIFACT:
			fullname = "Artifact";
			break;
			
		case CREATURE:
			fullname = "Creature";
			break;
			
		case ENCHANTMENT:
			fullname = "Enchantment";
			break;
			
		case INSTANT:
			fullname = "Instant";
			break;
			
		case LAND:
			fullname = "Land";
			break;
			
		case PLANESWALKER:
			fullname = "Planeswalker";
			break;
			
		case SORCERY:
			fullname = "Sorcery";
			break;
		
		case TRIBAL:
			fullname = "Tribal";
			break;
			
		default:
			break;
		
		}
		return fullname;
	}
}
