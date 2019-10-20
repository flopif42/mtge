package mtgengine.cost;

public class AdditionalCost {
	public enum Requirement {
		// Tap stuff
		TAP_THIS,
		TAP_AN_UNTAPPED_CREATURE_YOU_CONTROL,
		CREW,

		// Sacrifice stuff
		SACRIFICE_THIS,
		SACRIFICE_A_PERMANENT,
		SACRIFICE_A_LAND,
		SACRIFICE_A_CREATURE,
		SACRIFICE_FIVE_CREATURES,
		SACRIFICE_AN_ARTIFACT,
		SACRIFICE_AN_ENCHANTMENT,
		SACRIFICE_ANOTHER_VAMPIRE_OR_ZOMBIE, // Kalitas
		SACRIFICE_A_FOREST,
		SACRIFICE_A_FOREST_OR_PLAINS, // Knight of Reliquary
		SACRIFICE_A_GOBLIN,
		
		// Exile stuff
		EXILE_ANOTHER_CREATURE_CARD_FROM_GYD,
		
		// Return stuff to their owner's hand
		RETURN_THIS_TO_HAND, // Recurring Nightmare
		RETURN_A_LAND_YOU_CONTROL, // Meloku
		RETURN_AN_ELF_YOU_CONTROL, // Wirewood Symbiote
		
		// Discard stuff
		DISCARD_THIS, // Cycling
		DISCARD_A_CREATURE_CARD, // Fauna Shaman
		DISCARD_A_CARD,
		
		// Pay life
		PAY_1_LIFE,
		PAY_2_LIFE,
		PAY_7_LIFE,
		PAY_8_LIFE,
		PAY_X_LIFE,
		PAY_HALF_LIFE,
		
		// Remove counter(s)
		REMOVE_A_PLUS1_COUNTER,
		REMOVE_A_FADE_COUNTER,
		REMOVE_A_CHARGE_COUNTER,
		REMOVE_A_MINING_COUNTER,
		REMOVE_A_LOYALTY_COUNTER,
		
		// Put counter(s)
		PUT_A_MINUS0_MINUS1_COUNTER,
		
		// Pay Energy
		PAY_1_ENERGY,
		PAY_2_ENERGY,
		PAY_3_ENERGY,
		PAY_5_ENERGY,
	}
	
	public static Requirement parse(String e, String sourceName) {
		e = e.toLowerCase();
		Requirement ac = null;
		
		// Tap stuff
		if (e.startsWith("tap") || e.equalsIgnoreCase("{T}")) {
			if (e.equalsIgnoreCase("{T}"))
				ac = Requirement.TAP_THIS;
			else if (e.equalsIgnoreCase("Tap an untapped creature you control"))
				ac = Requirement.TAP_AN_UNTAPPED_CREATURE_YOU_CONTROL;
			else if (e.startsWith("tap any number of creatures you control with total power "))
				ac = Requirement.CREW;			
		}

		// Sacrifice stuff
		else if (e.startsWith("sacrifice")) {
			if (e.equalsIgnoreCase("Sacrifice " + sourceName) || e.startsWith("sacrifice this"))
				ac = Requirement.SACRIFICE_THIS;
			else if (e.equalsIgnoreCase("Sacrifice a permanent"))
				ac = Requirement.SACRIFICE_A_PERMANENT;
			else if (e.equalsIgnoreCase("Sacrifice a land"))
				ac = Requirement.SACRIFICE_A_LAND;				
			else if (e.equalsIgnoreCase("Sacrifice a creature"))
				ac = Requirement.SACRIFICE_A_CREATURE;
			else if (e.equalsIgnoreCase("Sacrifice five creatures"))
				ac = Requirement.SACRIFICE_FIVE_CREATURES;
			else if (e.equalsIgnoreCase("Sacrifice an artifact"))
				ac = Requirement.SACRIFICE_AN_ARTIFACT;
			else if (e.equalsIgnoreCase("Sacrifice an enchantment"))
				ac = Requirement.SACRIFICE_AN_ENCHANTMENT;
			else if (e.equalsIgnoreCase("Sacrifice another Vampire or Zombie"))
				ac = Requirement.SACRIFICE_ANOTHER_VAMPIRE_OR_ZOMBIE;
			else if (e.equalsIgnoreCase("Sacrifice a Forest"))
				ac = Requirement.SACRIFICE_A_FOREST;				
			else if (e.equalsIgnoreCase("Sacrifice a Forest or Plains"))
				ac = Requirement.SACRIFICE_A_FOREST_OR_PLAINS;
			else if (e.equalsIgnoreCase("Sacrifice a Goblin"))
				ac = Requirement.SACRIFICE_A_GOBLIN;
		}

		// Pay stuff
		else if (e.startsWith("pay")) {
			if (e.equalsIgnoreCase("Pay 1 life"))
				ac = Requirement.PAY_1_LIFE;
			else if (e.equalsIgnoreCase("Pay 2 life"))
				ac = Requirement.PAY_2_LIFE;
			else if (e.equalsIgnoreCase("Pay 7 life"))
				ac = Requirement.PAY_7_LIFE;
			else if (e.equalsIgnoreCase("Pay 8 life"))
				ac = Requirement.PAY_8_LIFE;
			else if (e.equalsIgnoreCase("Pay X life"))
				ac = Requirement.PAY_X_LIFE;
			else if (e.equalsIgnoreCase("Pay half your life rounded up"))
				ac = Requirement.PAY_HALF_LIFE;
			else if (e.equalsIgnoreCase("Pay {E}"))
				ac = Requirement.PAY_1_ENERGY;
			else if (e.equalsIgnoreCase("Pay {E}{E}"))
				ac = Requirement.PAY_2_ENERGY;
			else if (e.equalsIgnoreCase("Pay {E}{E}{E}"))
				ac = Requirement.PAY_3_ENERGY;
			else if (e.equalsIgnoreCase("Pay {E}{E}{E}{E}{E}"))
				ac = Requirement.PAY_5_ENERGY;
		}
		
		// Discard stuff
		else if (e.startsWith("discard")) {
			if (e.equalsIgnoreCase("Discard this card"))
				ac = Requirement.DISCARD_THIS;
			else if (e.equalsIgnoreCase("Discard a creature card"))
				ac = Requirement.DISCARD_A_CREATURE_CARD;
			else if (e.equalsIgnoreCase("Discard a card"))
				ac = Requirement.DISCARD_A_CARD;
		}
		
		// Return stuff to hand
		else if (e.startsWith("return")) {
			if (e.equalsIgnoreCase("Return " + sourceName + " to its owner's hand"))
				ac = Requirement.RETURN_THIS_TO_HAND;
			else if (e.equalsIgnoreCase("Return a land you control to its owner's hand"))
				ac = Requirement.RETURN_A_LAND_YOU_CONTROL;
			else if (e.equalsIgnoreCase("Return an Elf you control to its owner's hand"))
				ac = Requirement.RETURN_AN_ELF_YOU_CONTROL;
		}
		
		// Remove counters from stuff
		else if (e.startsWith("remove")) {
			if (e.equalsIgnoreCase("Remove a +1/+1 counter from this creature") || e.equalsIgnoreCase("Remove a +1/+1 counter from " + sourceName))
				ac = Requirement.REMOVE_A_PLUS1_COUNTER;
			else if (e.equalsIgnoreCase("Remove a fade counter from " + sourceName))
				ac = Requirement.REMOVE_A_FADE_COUNTER;
			else if (e.equalsIgnoreCase("Remove a charge counter from " + sourceName))
				ac = Requirement.REMOVE_A_CHARGE_COUNTER;
			else if (e.equalsIgnoreCase("Remove a mining counter from " + sourceName))
				ac = Requirement.REMOVE_A_MINING_COUNTER;
			else if (e.equalsIgnoreCase("Remove a loyalty counter")) // TODO : this is not correct syntax
				ac = Requirement.REMOVE_A_LOYALTY_COUNTER;	
		}
		
		// Other stuff
		else if (e.equalsIgnoreCase("Exile another creature card from your graveyard"))
			ac = Requirement.EXILE_ANOTHER_CREATURE_CARD_FROM_GYD;				
		else if (e.equalsIgnoreCase("Put a -0/-1 counter on " + sourceName))
			ac = Requirement.PUT_A_MINUS0_MINUS1_COUNTER;

		// Should not get here
		else
			System.err.println("Could not create this Additional Cost Requirement : " + e);
		
		return ac;
	}
}
