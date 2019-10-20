package mtgengine.ability;

import mtgengine.Target.Category;
import mtgengine.action.SpecialAction.Option;
import mtgengine.action.SpellCast;
import mtgengine.card.Card;
import mtgengine.cost.AlternateCost;
import mtgengine.cost.AlternateCost.DEFINITION;
import mtgengine.cost.Cost;
import mtgengine.type.Subtype;

public class StaticAbilityFactory {
	/**
	 * This constructor is used when the static ability has no parameter and no cost.
	 * @param name The name of the ability.
	 * @param source The source of the ability.
	 * @return The created ability.
	 */
	public static StaticAbility create(String name, Card source) {
		return create(name, source, null);
	}
	
	/**
	 * This constructor is used when the static ability has one parameter and no cost
	 * @param name The name of the ability.
	 * @param source The source of the ability.
	 * @param parameter The parameter of the ability, for example, the number of Dredge.
	 * @return The created ability.
	 */
	public static StaticAbility create(String name, Card source, String parameter) {
		return create(name, source, null, parameter);
	}

	/**
	 * This constructor is used when the static ability has two associated basic land types, like the "Reveal" lands for example.
	 * @param name The name of the ability.
	 * @param source The source of the ability.
	 * @param st1 The first associated basic land type.
	 * @param st2 The second associated basic land type.
	 * @return The created ability.
	 */
	public static StaticAbility create(String name, Card source, Subtype st1, Subtype st2) {
		StaticAbility sa = new StaticAbility(name, source, null, null);
		sa.addAssociatedSubtype(st1);
		sa.addAssociatedSubtype(st2);
		String cardname = source.getName();
		String land1 = st1.toString();
		String land2 = st2.toString();
		
		/* revealland : i.e. Choked Estuary */
		if (name.equals("revealland"))
			sa.setDescription("As " + cardname + " enters the battlefield, you may reveal a " + land1 + " or " +  land2 + " card from your hand. If you don't, " + cardname + " enters the battlefield tapped.");
		
		/* checkland : i.e. Clifftop Retreat */
		else if (name.equals("checkland"))
			sa.setDescription(cardname + " enters the battlefield tapped unless you control a " + land1 + " or " + land2 + ".");

		return sa;
	}

	/**
	 * This is the main constructor.
	 * @param name The name of the ability.
	 * @param source The source of the ability.
	 * @param parameter The parameter of the ability, for example, the number of Dredge.
	 * @param manaCost The cost of the ability, for example the Kicker cost or Echo cost.
	 * @return The created ability.
	 */
	public static StaticAbility create(String name, Card source, Cost cost, String parameter) {
		StaticAbility ability = new StaticAbility(name, source, cost, parameter);
		String cardname = source.getName();

		// Effects that set a creature's P/T (when * is printed on the card)
		if (name.equals("pt_star")) {
			/*Tarmogoyf */
			if (ability.getParameter().equals("tarmogoyf"))
				ability.setDescription("Tarmogoyf's power is equal to the number of card types among cards in all graveyards and its toughness is equal to that number plus 1.");	
			
			/* saprolingBurst_PT */
			else if (ability.getParameter().equals("saprolingBurst"))
				ability.setDescription("This creature's power and toughness are each equal to the number of fade counters on Saproling Burst.");
			
			/* masterOfEtherium_PT */
			else if (ability.getParameter().equals("masterOfEtherium"))
				ability.setDescription("Master of Etherium's power and toughness are each equal to the number of artifacts you control.");
			
			/* serraAvatar */
			else if (ability.getParameter().equals("serraAvatar"))
				ability.setDescription("Serra Avatar's power and toughness are each equal to your life total.");
			
			/* treefolkSeedlings */
			else if (ability.getParameter().equals("treefolkSeedlings"))
				ability.setDescription("Treefolk Seedlings's toughness is equal to the number of Forests you control.");
		}
		
		/* dredge */
		else if (name.equals("dredge"))
			ability.setDescription("Dredge " + parameter + " <i>(If you would draw a card, instead you may put exactly " + parameter + " cards from the top of your library into your graveyard. If you do, return this card from your graveyard to your hand. Otherwise, draw a card.)</i>");
		
		/* devoid */
		else if (name.equals(StaticAbility.DEVOID))
			ability.setDescription("Devoid <i>(This card has no color.)</i>");
		
		/* spectacle */
		else if (name.equals(StaticAbility.SPECTACLE)) {
			ability.setDescription("Spectacle " + cost.toString() + " <i>(You may cast this spell for its spectacle cost rather than its mana cost if an opponent lost life this turn.)</i>");
			source.addSpellCastOption(SpellCast.Option.CAST_WITH_SPECTACLE, "Spectacle " + cost.toString());
		}
		
		/* morph */
		else if (name.equals(StaticAbility.MORPH)) {
			ability.setDescription("Morph " + cost.toString() + " <i>(You may cast this card face down as a 2/2 creature for {3}. Turn it face up any time for its morph cost.)</i>");
			source.addSpellCastOption(SpellCast.Option.CAST_FACEDOWN);
		}
		
		/* megamorph */
		else if (name.equals(StaticAbility.MEGAMORPH)) {
			ability.setDescription("Megamorph " + cost.toString() + " <i>(You may cast this card face down as a 2/2 creature for {3}. Turn it face up any time for its megamorph cost and put a +1/+1 counter on it.)</i>");
			source.addSpellCastOption(SpellCast.Option.CAST_FACEDOWN);
		}
		
		/* daze */
		else if (name.equals("daze")) {
			ability.setDescription("You may return an Island you control to its owner's hand rather than pay Daze's mana cost.");
			source.addSpellCastOption(SpellCast.Option.CAST_WITH_ALTERNATE_COST, null, new AlternateCost(DEFINITION.Daze, source));
		}
		
		/* forceOfWill */
		else if (name.equals("forceOfWill")) {
			ability.setDescription("You may pay 1 life and exile a blue card from your hand rather than pay Force of Will's mana cost.");
			source.addSpellCastOption(SpellCast.Option.CAST_WITH_ALTERNATE_COST, null, new AlternateCost(DEFINITION.ForceOfWill, source));
		}
		
		/* snuffOut */
		else if (name.equals("snuffOut")) {
			ability.setDescription("If you control a Swamp, you may pay 4 life rather than pay this spell's mana cost.");
			source.addSpellCastOption(SpellCast.Option.CAST_WITH_ALTERNATE_COST, null, new AlternateCost(DEFINITION.SnuffOut, source));
		}
		
		/* flashback */
		else if (name.equals("flashback")) {
			ability.setDescription("Flashback " + cost.toString() + " <i>(You may cast this card from your graveyard for its flashback cost. Then exile it.)</i>");
			source.addSpellCastOption(SpellCast.Option.CAST_WITH_FLASHBACK, "Flashback " + cost.toString());
		}
		
		/* buyback */
		else if (name.equals("buyback")) {
			ability.setDescription("Buyback " + cost.toString() + " <i>(You may pay an additional " + cost.toString() + " as you cast this spell. If you do, put this card into your hand as it resolves.)</i>");
			source.addSpellCastOption(SpellCast.Option.CAST_WITH_BUYBACK, "Buyback " + cost.toString());
		}
		
		/* rebound */
		else if (name.equals("rebound")) {
			ability.setDescription("Rebound <i>(If you cast this spell from your hand, exile it as it resolves. At the beginning of your next upkeep, you may cast this card from exile without paying its mana cost.)</i>");
			source.addSpellCastOption(SpellCast.Option.CAST_FROM_EXILE, "Rebound");
		}
		
		/* kicker */
		else if (name.equals("kicker")) {
			ability.setDescription("Kicker " + cost.toString() + " <i>(You may pay an additional " + cost.toString() + " as you cast this spell.)</i>");
			source.addSpellCastOption(SpellCast.Option.CAST_WITH_KICKER, "Kicker " + cost.toString());
		}
		
		/* awaken */
		else if (name.equals("awaken")) {
			ability.setDescription("Awaken " + parameter + "—" + cost.toString() + " <i>(If you cast this spell for " + cost.toString() + ", also put " + parameter + " +1/+1 counters on target land you control and it becomes a 0/0 Elemental creature with haste. It's still a land.)</i>");
			source.addSpellCastOption(SpellCast.Option.CAST_WITH_AWAKEN, "Awaken " + parameter + " - " + cost.toString());
		}

		/* moxDiamond */
		else if (name.equals("moxDiamond"))
			ability.setDescription("If Mox Diamond would enter the battlefield, you may discard a land card instead. If you do, put Mox Diamond onto the battlefield. If you don't, put it into its owner's graveyard.");
		
		/* fading */
		else if (name.equals("fading")) {
			ability.setDescription("Fading " + parameter + " <i>(This permanent enters the battlefield with " + parameter + " fade counters on it. At the beginning of your upkeep, remove a fade counter from it. If you can't, sacrifice it.)</i>");
			source.addTriggeredAbility(TriggeredAbilityFactory.create("fading_remove", source));
		}
		
		/* vanishing */
		else if (name.equals("vanishing")) {
			ability.setDescription("Vanishing " + parameter + " <i>(This creature enters the battlefield with " + parameter + " time counters on it. At the beginning of your upkeep, remove a time counter from it. When the last is removed, sacrifice it.)</i>");
			source.addTriggeredAbility(TriggeredAbilityFactory.create("vanishing_remove", source),
	   				  				   TriggeredAbilityFactory.create("vanishing_sacrifice", source));
		}
		
		/* suspend */
		else if (name.equals(StaticAbility.SUSPEND)) {
			ability.setDescription("Suspend " + parameter + " — " + cost.toString() + " <i>(Rather than cast this card from your hand, you may pay " + cost.toString() + " and exile it with " + parameter + " time counter(s) on it. "
					+ "At the beginning of your upkeep, remove a time counter. "
					+ "When the last is removed, cast it without paying its mana cost.)</i>");
			source.addSpecialAction(Option.SUSPEND);
			source.addTriggeredAbility(TriggeredAbilityFactory.create("suspend_remove_counter", source),
					                   TriggeredAbilityFactory.create("suspend_cast_from_exile", source));
		}
		
		/* modular */
		else if (name.equals("modular")) {
			ability.setDescription("Modular " + parameter + " <i>(This enters the battlefield with " + parameter + " +1/+1 counter on it. When it dies, you may put its +1/+1 counters on target artifact creature.)</i>");
			source.addTriggeredAbility(TriggeredAbilityFactory.create("modular_moveCounters", source));
		}

		/* sagas */
		else if (name.equals("saga"))
			ability.setDescription("<i>(As this Saga enters and after your draw step, add a lore counter. Sacrifice after III.)</i>");
		
		/* tendoIceBridge_putCounter */
		else if (name.equals("tendoIceBridge_putCounter"))
			ability.setDescription("Tendo Ice Bridge enters the battlefield with a charge counter on it.");
		
		/* gemstoneMine_counters */
		else if (name.equals("gemstoneMine_counters"))
			ability.setDescription("Gemstone Mine enters the battlefield with three mining counters on it.");
		
		/* darkDepths_counters */
		else if (name.equals("darkDepths_counters"))
			ability.setDescription("Dark Depths enters the battlefield with ten ice counters on it.");
		
		/* chaliceVoid_counters */
		else if (name.equals("chaliceVoid_counters"))
			ability.setDescription("Chalice of the Void enters the battlefield with X charge counters on it.");

		/* workhorse_counters */
		else if (name.equals("workhorse_counters"))
			ability.setDescription("Workhorse enters the battlefield with four +1/+1 counters on it.");
		
		/* hangarbackWalker_counters */
		else if (name.equals("hangarbackWalker_counters"))
			ability.setDescription("Hangarback Walker enters the battlefield with X +1/+1 counters on it.");
		
		/* walkingBallista_counters */
		else if (name.equals("walkingBallista_counters"))
			ability.setDescription("Walking Ballista enters the battlefield with X +1/+1 counters on it.");
		
		/* kavuTitan_counters */
		else if (name.equals("kavuTitan_counters"))
			ability.setDescription("If Kavu Titan was kicked, it enters the battlefield with three +1/+1 counters on it and with trample.");
		
		/* spikeFeeder_counters */
		else if (name.equals("spikeFeeder_counters"))
			ability.setDescription("Spike Feeder enters the battlefield with two +1/+1 counters on it.");

		/* vebulid_counters */
		else if (name.equals("vebulid_counters"))
			ability.setDescription("Vebulid enters the battlefield with a +1/+1 counters on it.");
		
		/* ETB_tapped */
		else if (name.equals("ETB_tapped"))
			ability.setDescription(cardname + " enters the battlefield tapped.");
		
		/* shockland */
		else if (name.equals("shockland"))
			ability.setDescription("As " + cardname + " enters the battlefield, you may pay 2 life. If you don't, " + cardname + " enters the battlefield tapped.");
		
		/* phyrexianProcessor_payLife */
		else if (name.equals("phyrexianProcessor_payLife"))
			ability.setDescription("As Phyrexian Processor enters the battlefield, pay any amount of life.");
		
		/* serraAvenger */
		else if (name.equals("serraAvenger"))
			ability.setDescription("You can't cast Serra Avenger during your first, second, or third turns of the game.");
		
		/* tangoland */
		else if (name.equals("tangoland"))
			ability.setDescription(cardname + " enters the battlefield tapped unless you control two or more basic lands.");
		
		/* mirroland */
		else if (name.equals("mirroland"))
			ability.setDescription(cardname + " enters the battlefield tapped unless you control two or fewer other lands.");
		
		/* utopiaSprawl_chooseColor */
		else if (name.equals("utopiaSprawl_chooseColor"))
			ability.setDescription("As Utopia Sprawl enters the battlefield, choose a color.");
		
		/* ionaShieldEmeria */
		else if (name.equals("ionaShieldEmeria"))
			ability.setDescription("As Iona, Shield of Emeria enters the battlefield, choose a color. Your opponents can't cast spells of the chosen color.");
		
		/* voiceOfAll */
		else if (name.equals("voiceOfAll"))
			ability.setDescription("As Voice of All enters the battlefield, choose a color.§Voice of All has protection from the chosen color.");
		
		/* enchantLand */
		else if (name.equals("enchantLand")) {
			ability.setDescription("Enchant Land");
			ability.getSource().addTargetRequirement(Category.Land);
		}
		
		/* enchantForest */
		else if (name.equals("enchantForest")) {
			ability.setDescription("Enchant Forest");
			ability.getSource().addTargetRequirement(Category.Forest);
		}
		
		/* enchantSwamp */
		else if (name.equals("enchantSwamp")) {
			ability.setDescription("Enchant Swamp");
			ability.getSource().addTargetRequirement(Category.Swamp);
		}
		
		/* enchantCreature */
		else if (name.equals("enchantCreature")) {
			ability.setDescription("Enchant creature");
			ability.getSource().addTargetRequirement(Category.Creature);
		}
		
		/* enchantPermanent */
		else if (name.equals("enchantPermanent")) {
			ability.setDescription("Enchant permanent");
			ability.getSource().addTargetRequirement(Category.Permanent);
		}
		
		/* untapOptional */
		else if (name.equals("untapOptional")) {
			ability.setDescription("You may choose not to untap " + ability.getSource().getName() + " during your untap step.");
			ability.getSource().setUntapOptional();
		}
		
		// Should never get here
		else {
			System.err.println("Error : Unknown static ability : " + name);
		}

		return ability;
	}

}


