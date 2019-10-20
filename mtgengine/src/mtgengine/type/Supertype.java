package mtgengine.type;

public enum Supertype { BASIC, LEGENDARY, ONGOING, SNOW, WORLD;

	public String toString() {
		String fullname = null;
		
		switch(this) {
		case BASIC:
			fullname = "Basic";
			break;
			
		case LEGENDARY:
			fullname = "Legendary";
			break;
			
		case ONGOING:
			fullname = "Ongoing";
			break;
			
		case SNOW:
			fullname = "Snow";
			break;
			
		case WORLD:
			fullname = "World";
			break;
			
		default:
			break;
		
		}
		
		return fullname;
	}

}
