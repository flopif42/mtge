package mtgengine.ability;

public enum Protection {
	COLOREDSPELLS, EVERYTHING;
	
	public String toString() {
		switch (this) {
		case COLOREDSPELLS:
			return "colored spells";

		case EVERYTHING:
			return "everything";
			
		default:
			return null;
		}
	}
}

