package mtgengine.mana;

import mtgengine.card.Color;

public class Mana {
	public enum ManaType {
		WHITE, BLUE, BLACK, RED, GREEN, COLORLESS;
	}

	public static ManaType parse(String str) {
		if (str.equals("{w}"))
			return ManaType.WHITE;
		if (str.equals("{u}"))
			return ManaType.BLUE;
		if (str.equals("{b}"))
			return ManaType.BLACK;
		if (str.equals("{r}"))
			return ManaType.RED;
		if (str.equals("{g}"))
			return ManaType.GREEN;
		if (str.equals("{c}"))
			return ManaType.COLORLESS;
		return null;
	}
	
	public static ManaType colorToMana(Color col) {
		switch(col) {
		case BLACK:
			return ManaType.BLACK;
			
		case BLUE:
			return ManaType.BLUE;
			
		case GREEN:
			return ManaType.GREEN;
			
		case RED:
			return ManaType.RED;
			
		case WHITE:
			return ManaType.WHITE;
		}
		return null;
	}
	
	// Used when a mana type (color) is chosen trough the GUI for example : Fertile Ground
	public static ManaType intToManaType(int iColor) {
		switch (iColor)
		{
		case 1:
			return ManaType.WHITE;

		case 2:
			return ManaType.BLUE;

		case 3:
			return ManaType.BLACK;

		case 4:
			return ManaType.RED;

		case 5:
			return ManaType.GREEN;

		default: // should never get here
			return null;
		}
	}
}
