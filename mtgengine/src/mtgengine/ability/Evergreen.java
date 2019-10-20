package mtgengine.ability;

public enum Evergreen {
	FLASH,
	FLYING,
	FEAR,
	MENACE,
	SHADOW,
	FIRSTSTRIKE,
	DOUBLESTRIKE,
	VIGILANCE,
	TRAMPLE,
	HASTE,
	DEATHTOUCH,
	DEFENDER,
	HEXPROOF,
	INDESTRUCTIBLE,
	INFECT,
	LIFELINK,
	REACH,
	SHROUD,
	CHANGELING,
	UNBLOCKABLE,
	CANTBLOCK,
	UNCOUNTERABLE,
	UNDAMAGEABLE, // Prevent all damage that would be dealt to it (i.e. When Gideon turns into a creature or under Solitary Confinement)
	
	// Landwalk
	PLAINSWALK,
	ISLANDWALK,
	SWAMPWALK,
	MOUNTAINWALK,
	FORESTWALK,
	
	// Used by the game engine only
	IGNORE_DEFENDER,  // when a creature has this it can attack as though it didnt have defender
	NOUNTAP           // Used for permanents that do not untap during their controller's untap steps
;

	public String getFullName() {
		String fullname = null;
		
		switch(this) {
		case DEATHTOUCH:
			fullname = "Deathtouch";
			break;
			
		case DEFENDER:
			fullname = "Defender";
			break;
			
		case DOUBLESTRIKE:
			fullname = "Double strike";
			break;
			
		case FIRSTSTRIKE:
			fullname = "First strike";
			break;
			
		case FLASH:
			fullname = "Flash";
			break;
			
		case FLYING:
			fullname = "Flying";
			break;
		
		case FEAR:
			fullname = "Fear <i>(This creature can't be blocked except by artifact creatures and/or black creatures.)</i>";
			break;
		
		case MENACE:
			fullname = "Menace <i>(This creature can't be blocked except by two or more creatures.)</i>";
			break;
			
		case SHADOW:
			fullname = "Shadow";
			break;
			
		case HASTE:
			fullname = "Haste";
			break;
			
		case HEXPROOF:
			fullname = "Hexproof";
			break;
			
		case INDESTRUCTIBLE:
			fullname = "Indestructible";
			break;
			
		case INFECT:
			fullname = "Infect";
			break;
			
		case LIFELINK:
			fullname = "Lifelink";
			break;
			
		case REACH:
			fullname = "Reach";
			break;
			
		case SHROUD:
			fullname = "Shroud";
			break;
			
		case TRAMPLE:
			fullname = "Trample";
			break;
			
		case VIGILANCE:
			fullname = "Vigilance";
			break;

		case CHANGELING:
			fullname = "Changeling";
			break;
			
		case PLAINSWALK:
			fullname = "Plainswalk <i>(This creature can't be blocked as long as defending player controls a Plains.)</i>";
			break;
			
		case ISLANDWALK:
			fullname = "Islandwalk <i>(This creature can't be blocked as long as defending player controls a Island.)</i>";
			break;
			
		case SWAMPWALK:
			fullname = "Swampwalk <i>(This creature can't be blocked as long as defending player controls a Swamp.)</i>";
			break;
			
		case MOUNTAINWALK:
			fullname = "Mountainwalk <i>(This creature can't be blocked as long as defending player controls a Mountain.)</i>";
			break;
			
		case FORESTWALK:
			fullname = "Forestwalk <i>(This creature can't be blocked as long as defending player controls a Forest.)</i>";
			break;

		default:
			fullname = "Error in Evergreen.getFullName() : " + this.name();
			break;
		}
		
		return fullname;
	}
}
