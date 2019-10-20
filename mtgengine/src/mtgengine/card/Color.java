package mtgengine.card;

public enum Color { WHITE, BLUE, BLACK, RED, GREEN;

	@Override
	public String toString() {
		String fullname = null;
		
		switch(this) {
		case BLACK:
			fullname = "Black";
			break;
			
		case BLUE:
			fullname = "Blue";
			break;
			
		case GREEN:
			fullname = "Green";
			break;
			
		case RED:
			fullname = "Red";
			break;
			
		case WHITE:
			fullname = "White";
			break;
			
		default:
			break;
		
		}
		
		return fullname;
	}
		
	// Used when a color is chosen trough the GUI for example : Mother of Runes
	public static Color intToColor(int iColor) {
		switch (iColor)
		{
		case 1:
			return WHITE;
			
		case 2:
			return BLUE;
			
		case 3:
			return BLACK;
			
		case 4:
			return RED;
			
		case 5:
			return GREEN;
			
		default: // should never get here
			return null;
		}
	}
}
