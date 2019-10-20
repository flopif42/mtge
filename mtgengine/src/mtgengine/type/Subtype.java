package mtgengine.type;

public enum Subtype {
	SAGA, AURA, // Enchantment subtypes
	EQUIPMENT, CLUE, VEHICLE, // Artifact subtypes
	PLAINS, ISLAND, SWAMP, MOUNTAIN, FOREST,  // Basic land types
	GATE; // land subtypes

	public static Subtype parse(String subtype) {
		subtype = subtype.toLowerCase();

		if (subtype.equals("aura"))
			return AURA;

		if (subtype.equals("saga"))
			return SAGA;
		
		if (subtype.equals("equipment"))
			return Subtype.EQUIPMENT;
		
		if (subtype.equals("clue"))
			return CLUE;
		
		if (subtype.equals("vehicle"))
			return VEHICLE;
		
		if (subtype.equals("plains"))
			return Subtype.PLAINS;
		
		if (subtype.equals("island"))
			return ISLAND;
		
		if (subtype.equals("swamp"))
			return SWAMP;
		
		if (subtype.equals("mountain"))
			return MOUNTAIN;
		
		if (subtype.equals("forest"))
			return FOREST;
		
		if (subtype.equals("gate"))
			return GATE;
	
		return null;
	}
	
	public String toString() {
		String fullname = null;
		
		switch(this) {
		case AURA:
			fullname = "Aura";
			break;

		case SAGA:
			fullname = "Saga";
			break;
			
		case CLUE:
			fullname = "Clue";
			break;
			
		case GATE:
			fullname = "Gate";
			break;
			
		case EQUIPMENT:
			fullname = "Equipment";
			break;
			
		case VEHICLE:
			fullname = "Vehicle";
			break;
			
		case FOREST:
			fullname = "Forest";
			break;
			
		case ISLAND:
			fullname = "Island";
			break;
			
		case MOUNTAIN:
			fullname = "Mountain";
			break;
			
		case PLAINS:
			fullname = "Plains";
			break;
			
		case SWAMP:
			fullname = "Swamp";
			break;
			
		default:
			break;
			
		}
		return fullname;
	}
}
