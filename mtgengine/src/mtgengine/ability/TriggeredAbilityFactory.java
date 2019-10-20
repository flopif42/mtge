package mtgengine.ability;

import mtgengine.ability.TriggeredAbility.Event;
import mtgengine.ability.TriggeredAbility.YouMayChoiceMaker;
import mtgengine.ability.TriggeredAbility.Origin;
import mtgengine.card.Card;
import mtgengine.cost.Cost;
import mtgengine.effect.Effect;
import mtgengine.Target.Category;
import mtgengine.TargetRequirement.Cardinality;

public class TriggeredAbilityFactory {
	public static TriggeredAbility create(String name, Card source) {
		return create(name, source, null, null);
	}
	
	public static TriggeredAbility create(String name, Card source, Cost cost, String parameter) {
		TriggeredAbility ability = new TriggeredAbility(name, source, cost, parameter);
		String manaCost = "";
		if (cost != null)
			manaCost = cost.getManaCost().toString();
		
		/* Echo */
		if (name.equals("echo")) {
			ability.initialize(name, "Echo " + manaCost + " <i>(At the beginning of your upkeep, if this came under your control since the beginning of your last upkeep, sacrifice it unless you pay its echo cost.)</i>", Event.BegOfYourUpkeep);
			ability.setInterveningIfClause();
		}
		
		/* Cumulative upkeep */
		else if (name.equals("cumulativeUpkeep")) {
			ability.initialize(name, "Cumulative upkeep " + manaCost + " <i>(At the beginning of your upkeep, put an age counter on this permanent, then sacrifice it unless you pay its upkeep cost for each age counter on it.)</i>", Event.BegOfYourUpkeep);
		}
		
		/* necropotence_exile */
		else if (name.equals("necropotence_exile"))
			ability.initialize(name, "Whenever you discard a card, exile that card from your graveyard.", Event.YouDiscard);
		
		/* greenbeltRampager */
		else if (name.equals("greenbeltRampager"))
			ability.initialize(name, "When Greenbelt Rampager enters the battlefield, pay {E}{E} <i>(two energy counters)</i>. If you can't, return Greenbelt Rampager to its owner's hand and you get {E}.", Event.EntersTheBattlefield);
		
		/* parasiticBond */
		else if (name.equals("parasiticBond"))
			ability.initialize(name, "At the beginning of the upkeep of enchanted creature's controller, Parasitic Bond deals 2 damage to that player.", Event.BegOfEnchantedCreatureControllerUpkeep);
		
		/* powerTaint */
		else if (name.equals("powerTaint"))
			ability.initialize(name, "At the beginning of the upkeep of enchanted enchantment's controller, that player loses 2 life unless he or she pays {2}.", Event.BegOfEnchantedCreatureControllerUpkeep);
		
		/* throneWarden */
		else if (name.equals("throneWarden")) {
			ability.initialize(Effect.PUT_PLUS_ONE_COUNTER_ON_THIS, "At the beginning of your end step, if you're the monarch, put a +1/+1 counter on Throne Warden.", Event.BegOfYourEndStep);
			ability.setInterveningIfClause();
		}
		
		/* hiddenSpider */
		else if (name.equals("hiddenSpider")) {
			ability.initialize(name, "When an opponent casts a creature spell with flying, if Hidden Spider is an enchantment, Hidden Spider becomes a 3/5 Spider creature with reach. <i>(It can block creatures with flying.)</i>", Event.AnOpponentCastCreatureSpellFlying);
			ability.setInterveningIfClause();
		}
		
		/* hiddenGuerrillas */
		else if (name.equals("hiddenGuerrillas")) {
			ability.initialize(name, "When an opponent casts an artifact spell, if Hidden Guerrillas is an enchantment, Hidden Guerrillas becomes a 5/3 Soldier creature with trample.", Event.AnOpponentCastArtifactSpell);
			ability.setInterveningIfClause();
		}
		
		/* hiddenAncients */
		else if (name.equals("hiddenAncients")) {
			ability.initialize(name, "When an opponent casts an enchantment spell, if Hidden Ancients is an enchantment, Hidden Ancients becomes a 5/5 Treefolk creature.", Event.AnOpponentCastEnchantSpell);
			ability.setInterveningIfClause();
		}
		
		/* hiddenHerd */
		else if (name.equals("hiddenHerd")) {
			ability.initialize(name, "When an opponent plays a nonbasic land, if Hidden Herd is an enchantment, Hidden Herd becomes a 3/3 Beast creature.", Event.AnOpponentPlaysNonbasicLand);
			ability.setInterveningIfClause();
		}
		
		/* hiddenStag */
		else if (name.equals("hiddenStag")) {
			ability.initialize(name, "Whenever an opponent plays a land, if Hidden Stag is an enchantment, Hidden Stag becomes a 3/2 Elk Beast creature.", Event.AnOpponentPlaysLand);
			ability.setInterveningIfClause();
		}
		
		/* greenerPastures */
		else if (name.equals("greenerPastures")) {
			ability.initialize(name, "At the beginning of each player's upkeep, if that player controls more lands than each other player, the player creates a 1/1 green Saproling creature token.", Event.BegOfEachUpkeep);
			ability.setInterveningIfClause();
		}
		
		/* veiledApparition_upkeep */
		else if (name.equals("veiledApparition_upkeep"))
			ability.initialize("driftingDjinn_upkeep", "At the beginning of your upkeep, sacrifice Veiled Apparition unless you pay {1}{U}.", Event.BegOfYourUpkeep);
		
		/* driftingDjinn_upkeep */
		else if (name.equals("driftingDjinn_upkeep"))
			ability.initialize(name, "At the beginning of your upkeep, sacrifice Drifting Djinn unless you pay {1}{U}.", Event.BegOfYourUpkeep);
		
		/* yawgmothsEdict */
		else if (name.equals("yawgmothsEdict"))
			ability.initialize(name, "Whenever an opponent casts a white spell, that player loses 1 life and you gain 1 life.", Event.AnOpponentCastWhiteSpell);
		
		/* endlessWurm */
		else if (name.equals("endlessWurm"))
			ability.initialize(name, "At the beginning of your upkeep, sacrifice Endless Wurm unless you sacrifice an enchantment.", Event.BegOfYourUpkeep);
		
		/* childOfGaea_upkeep */
		else if (name.equals("childOfGaea_upkeep"))
			ability.initialize(name, "At the beginning of your upkeep, sacrifice Child of Gaea unless you pay {G}{G}.", Event.BegOfYourUpkeep);
		
		/* annihilator */
		else if (name.equals("annihilator"))
			ability.initialize(name, "Annihilator " + parameter + " <i>(Whenever this creature attacks, defending player sacrifices " + parameter +" permanents.)</i>", Event.Attacks);
		
		/* cascade */
		else if (name.equals("cascade")) {
			ability.initialize(name, "Cascade <i>(When you cast this spell, exile cards from the top of your library until you exile a nonland card that costs less. You may cast it without paying its mana cost. Put the exiled cards on the bottom in a random order.)</i>", Event.YouCastThisSpell);
			ability.setOrigin(Origin.STACK);
		}
		
		/* mirari */
		else if (name.equals("mirari"))
			ability.initialize(name, "Whenever you cast an instant or sorcery spell, you may pay 3. If you do, copy that spell. You may choose new targets for the copy.", Event.YouCastInstantOrSorcerySpell);
		
		/* templeScry */
		else if (name.equals("templeScry"))
			ability.initialize(name, "When " + source.getName() + " enters the battlefield, scry 1.", Event.EntersTheBattlefield);
		
		/* veteranMotorist_scry */
		else if (name.equals("veteranMotorist_scry"))
			ability.initialize(name, "When Veteran Motorist enters the battlefield, scry 2.", Event.EntersTheBattlefield);
		
		/* veteranMotorist_pump */
		else if (name.equals("veteranMotorist_pump"))
			ability.initialize(name, "Whenever Veteran Motorist crews a Vehicle, that Vehicle gets +1/+1 until end of turn.", Event.CrewsAVehicle);
		
		/* toolcraftExemplar */
		else if (name.equals("toolcraftExemplar")) {
			ability.initialize(name, "At the beginning of combat on your turn, if you control an artifact, Toolcraft Exemplar gets +2/+1 until end of turn. If you control three or more artifacts, it also gains first strike until end of turn.", Event.BegOfYourCombatStep);
			ability.setInterveningIfClause();
		}
		
		/* battlecry */
		else if (name.equals("battlecry"))
			ability.initialize(name, "Battle cry <i>(Whenever this creature attacks, each other attacking creature gets +1/+0 until end of turn.)</i>", Event.Attacks);
		
		/* tidespoutTyrant */
		else if (name.equals("tidespoutTyrant")) {
			ability.initialize(name, "Whenever you cast a spell, return target permanent to its owner's hand.", Event.YouCastASpell);
			ability.addTargetRequirement(Category.Permanent);
		}
		
		/* chandraTOD_ultimate_trigger */
		else if (name.equals("chandraTOD_ultimate_trigger")) {
			ability.initialize(name, "Whenever you cast a spell, this emblem deals 5 damage to any target.", Event.YouCastASpell);
			ability.addTargetRequirement(Category.AnyTarget);
		}
		
		/* delverOfSecrets */
		else if (name.equals("delverOfSecrets")) {
			ability.initialize(name, "At the beginning of your upkeep, look at the top card of your library. You may reveal that card. If an instant or sorcery card is revealed this way, transform Delver of Secrets.", Event.BegOfYourUpkeep);
		}
		
		/*  */
		else if (name.equals("nissaVF_ultimate_trigger")) {
			ability.initialize(Effect.DRAW_A_CARD, "Whenever a land enters the battlefield under your control, you may draw a card.", Event.ALandEntersBattlefieldUnderYourControl);
			ability.setOptional();
		}
		
		/* verdurousGearhulk */
		else if (name.equals("verdurousGearhulk")) {
			ability.initialize(name, "When Verdurous Gearhulk enters the battlefield, distribute four +1/+1 counters among any number of target creatures you control.", Event.EntersTheBattlefield);
			ability.addTargetRequirement(Category.CreatureYouControl, Cardinality.UP_TO_FOUR);
		}
		
		/* Rebound */
		else if (name.equals("rebound_cast")) {
			ability.initialize(name, "At the beginning of your next upkeep, you may cast this card from exile without paying its mana cost.", Event.BegOfYourUpkeep);
			ability.setOptional();
		}
		
		/* narset_rebound */
		else if (name.equals("narset_rebound"))
			ability.initialize(name, "When you cast your next instant or sorcery spell from your hand this turn, it gains rebound.", Event.YouCastYourNextInstantOrSorcerySpellThisTurn);
		
		/* living_weapon */
		else if (name.equals("living_weapon"))
			ability.initialize(name, "Living weapon <i>(When this Equipment enters the battlefield, create a 0/0 black Germ creature token, then attach this to it.)</i>", Event.EntersTheBattlefield);
		
		/* avacynPurifier_damage */
		else if (name.equals("avacynPurifier_damage"))
			ability.initialize(name, "When this creature transforms into Avacyn, the Purifier, it deals 3 damage to each other creature and each opponent.", Event.TransformsInto);
		
		/* werewolf_transform_night (day face to night face (turning into a werewolf)) */
		else if (name.equals("werewolf_transform_night")) {
			ability.initialize("werewolf_transform", "At the beginning of each upkeep, if no spells were cast last turn, transform this permanent.", Event.BegOfEachUpkeep);
			ability.setInterveningIfClause();
		}
		
		/* werewolf_transform_day (night face to day face (turning into a human)) */
		else if (name.equals("werewolf_transform_day")) {
			ability.initialize("werewolf_transform", "At the beginning of each upkeep, if a player cast two or more spells last turn, transform this permanent.", Event.BegOfEachUpkeep);
			ability.setInterveningIfClause();
		}
		
		/* archangelAvacyn_protect */
		else if (name.equals("archangelAvacyn_protect"))
			ability.initialize(name, "When Archangel Avacyn enters the battlefield, creatures you control gain indestructible until end of turn.", Event.EntersTheBattlefield);
		
		/* tabernacle_upkeep */
		else if (name.equals("tabernacle_upkeep"))
			ability.initialize(name, "At the beginning of your upkeep, destroy this creature unless you pay {1}.", Event.BegOfYourUpkeep);
		
		/* retaliation_pump */
		else if (name.equals("retaliation_pump"))
			ability.initialize(Effect.PUMP, "Whenever this creature becomes blocked by a creature, this creature gets +1/+1 until end of turn.", Event.BecomesBlockedByACreature);
		
		/* carnophage */
		else if (name.equals("carnophage"))
			ability.initialize(name, "At the beginning of your upkeep, tap Carnophage unless you pay 1 life.", Event.BegOfYourUpkeep);
		
		/* restorationAngel */
		else if (name.equals("restorationAngel")) {
			ability.initialize(name, "When Restoration Angel enters the battlefield, you may exile target non-Angel creature you control, then return that card to the battlefield under your control.", Event.EntersTheBattlefield);
			ability.setOptional();
			ability.addTargetRequirement(Category.NonAngelCreatureYouControl);
		}
		
		/* felidarGuardian */
		else if (name.equals("felidarGuardian")) {
			ability.initialize(name, "When Felidar Guardian enters the battlefield, you may exile another target permanent you control, then return that card to the battlefield under its owner's control.", Event.EntersTheBattlefield);
			ability.setOptional();
			ability.addTargetRequirement(Category.AnotherPermanentYouControl);
		}
		
		/* tirelessTracker_investigate */
		else if (name.equals("tirelessTracker_investigate"))
			ability.initialize(name, "Whenever a land enters the battlefield under your control, investigate.", Event.ALandEntersBattlefieldUnderYourControl);
		
		/* tirelessTracker_counter */
		else if (name.equals("tirelessTracker_counter"))
			ability.initialize(name, "Whenever you sacrifice a Clue, put a +1/+1 counter on Tireless Tracker.", Event.SacrificeClue);
		
		/* thrabenInspector */
		else if (name.equals("thrabenInspector"))
			ability.initialize(name, "When Thraben Inspector enters the battlefield, investigate.", Event.EntersTheBattlefield);
		
		/* karmicGuide */
		else if (name.equals("karmicGuide")) {
			ability.initialize(name, "When Karmic Guide enters the battlefield, return target creature card from your graveyard to the battlefield.", Event.EntersTheBattlefield);
			ability.addTargetRequirement(Category.CreatureCardInYourGraveyard);
		}		

		/* kitchenFinks_gainLife */
		else if (name.equals("kitchenFinks_gainLife"))
			ability.initialize(name, "When Kitchen Finks enters the battlefield, you gain 2 life.", Event.EntersTheBattlefield);
		
		/* loxodonHierarch_gainlife */
		else if (name.equals("loxodonHierarch_gainlife"))
			ability.initialize(name, "When Loxodon Hierarch enters the battlefield, you gain 4 life.", Event.EntersTheBattlefield);
		
		/* linvalaPreserver_gainLife */
		else if (name.equals("linvalaPreserver_gainLife")) {
			ability.initialize(name, "When Linvala, the Preserver enters the battlefield, if an opponent has more life than you, you gain 5 life.", Event.EntersTheBattlefield);
			ability.setInterveningIfClause();
		}
		
		/* linvalaPreserver_putToken */
		else if (name.equals("linvalaPreserver_putToken")) {
			ability.initialize(name, "When Linvala enters the battlefield, if an opponent controls more creatures than you, create a 3/3 white Angel creature token with flying.", Event.EntersTheBattlefield);
			ability.setInterveningIfClause();
		}
		
		/* thragtusk_gainLife */
		else if (name.equals("thragtusk_gainLife"))
			ability.initialize(name, "When Thragtusk enters the battlefield, you gain 5 life.", Event.EntersTheBattlefield);
		
		/* thragtusk_putToken */
		else if (name.equals("thragtusk_putToken"))
			ability.initialize(name, "When Thragtusk leaves the battlefield, create a 3/3 green Beast creature token.", Event.LeavesTheBattlefield);
		
		/* saprolingBurst_destroy */
		else if (name.equals("saprolingBurst_destroy"))
			ability.initialize(name, "When Saproling Burst leaves the battlefield, destroy all tokens created with Saproling Burst. They can't be regenerated.", Event.LeavesTheBattlefield);
		
		/* parallaxWave_leaves */
		else if (name.equals("parallaxWave_leaves"))
			ability.initialize(name, "When Parallax Wave leaves the battlefield, each player returns to the battlefield all cards he or she owns exiled with Parallax Wave.", Event.LeavesTheBattlefield);
		
		/* sporogenesis_removeCounters */
		else if (name.equals("sporogenesis_removeCounters"))
			ability.initialize(name, "When Sporogenesis leaves the battlefield, remove all fungus counters from all creatures.", Event.LeavesTheBattlefield);
		
		/* parallaxTide_leaves */
		else if (name.equals("parallaxTide_leaves"))
			ability.initialize(name, "When Parallax Tide leaves the battlefield, each player returns to the battlefield all cards he or she owns exiled with Parallax Tide.", Event.LeavesTheBattlefield);
		
		/* hangarbackWalker */
		else if (name.equals("hangarbackWalker"))
			ability.initialize("hangarbackWalker_dies", "When Hangarback Walker dies, create a 1/1 colorless Thopter artifact creature token with flying for each +1/+1 counter on Hangarback Walker.", Event.Dies);
		
		/* yavimayaElder_fetch */
		else if (name.equals("yavimayaElder_fetch")) {
			ability.initialize(name, "When Yavimaya Elder dies, you may search your library for up to two basic land cards, reveal them, and put them into your hand. If you do, shuffle your library.", Event.Dies);
			ability.setOptional();
		}
		
		/* catacombSifter_ETB */
		else if (name.equals("catacombSifter_ETB"))
			ability.initialize(name, "When Catacomb Sifter enters the battlefield, create a 1/1 colorless Eldrazi Scion creature token. It has \"Sacrifice this creature: Add {C}.\"", Event.EntersTheBattlefield);
		
		/* glacialChasm_upkeep */
		else if (name.equals("glacialChasm_upkeep"))
			ability.initialize(name, "Cumulative upkeep-Pay 2 life.", Event.BegOfYourUpkeep);
		
		/* glacialChasm_sacAland */
		else if (name.equals("glacialChasm_sacAland"))
			ability.initialize(name, "When Glacial Chasm enters the battlefield, sacrifice a land.", Event.EntersTheBattlefield);
		
		/* blisterpod */
		else if (name.equals("blisterpod"))
			ability.initialize(name, "When Blisterpod dies, create a 1/1 colorless Eldrazi Scion creature token. It has \"Sacrifice this creature: Add {C}.\"", Event.Dies);
		
		/* darkDepths_putToken */
		else if (name.equals("darkDepths_putToken"))
			ability.initialize(name, "When Dark Depths has no ice counters on it, sacrifice it. If you do, create a legendary 20/20 black Avatar creature token with flying and indestructible named Marit Lage.", Event.NoIceCounters);
		
		/* hiddenPredators */
		else if (name.equals("hiddenPredators")) {
			ability.initialize(name, "When an opponent controls a creature with power 4 or greater, if Hidden Predators is an enchantment, Hidden Predators becomes a 4/4 Beast creature.", Event.AnOpponentControlsCreaturePower4);
			ability.setInterveningIfClause();
		}
		
		/* phyrexianNegator */
		else if (name.equals("phyrexianNegator"))
			ability.initialize(name, "Whenever Phyrexian Negator is dealt damage, sacrifice that many permanents.", Event.IsDealtDamage);
		
		/* jackalPup */
		else if (name.equals("jackalPup"))
			ability.initialize(name, "Whenever Jackal Pup is dealt damage, it deals that much damage to you.", Event.IsDealtDamage);
		
		/* wildgrowthWalker */
		else if (name.endsWith("wildgrowthWalker"))
			ability.initialize(name, "Whenever a creature you control explores, put a +1/+1 counter on Wildgrowth Walker and you gain 3 life.", Event.ACreatureYouControlExplores);
		
		/* hellrider */
		else if (name.equals("hellrider")) {
			ability.initialize(name, "Whenever a creature you control attacks, Hellrider deals 1 damage to defending player.", Event.ACreatureYouControlAttacks);
		}
		
		/* zulaportCutthroat_drain */
		else if (name.equals("zulaportCutthroat_drain"))
			ability.initialize(name, "Whenever Zulaport Cutthroat or another creature you control dies, each opponent loses 1 life and you gain 1 life.", Event.ACreatureYouControlDies);
		
		/* thundermawHellkite */
		else if (name.equals("thundermawHellkite"))
			ability.initialize(name, "When Thundermaw Hellkite enters the battlefield, it deals 1 damage to each creature with flying your opponents control. Tap those creatures.", Event.EntersTheBattlefield);
		
		/* archangelOfThune */
		else if (name.equals("archangelOfThune"))
			ability.initialize(name, "Whenever you gain life, put a +1/+1 counter on each creature you control.", Event.YouGainLife);
		
		/* punishingFire_return */
		else if (name.equals("punishingFire_return")) {
			ability.initialize(name, "Whenever an opponent gains life, you may pay {R}. If you do, return Punishing Fire from your graveyard to your hand.", Event.AnOpponentGainsLife);
			ability.setOrigin(Origin.GRVYRD);
		}
		
		/* glitterfang */
		else if (name.equals("glitterfang"))
			ability.initialize(name, "At the beginning of the end step, return Glitterfang to its owner's hand.", Event.BegOfEachEndStep);
		
		/* pestilence_sac */
		else if (name.equals("pestilence_sac")) {
			ability.initialize(name, "At the beginning of the end step, if no creatures are on the battlefield, sacrifice Pestilence.", Event.BegOfEachEndStep);
			ability.setInterveningIfClause();
		}
		
		/* desolationAngel */
		else if (name.equals("desolationAngel"))
			ability.initialize(name, "When Desolation Angel enters the battlefield, destroy all lands you control. If it was kicked, destroy all lands instead.", Event.EntersTheBattlefield);
		
		/* skizzik_sacrifice */
		else if (name.equals("skizzik_sacrifice")) {
			ability.initialize(name, "At the beginning of the end step, sacrifice Skizzik unless it was kicked.", Event.BegOfEachEndStep);
			ability.setInterveningIfClause();
		}
		
		/* thranQuarry_sacrifice */
		else if (name.equals("thranQuarry_sacrifice")) {
			ability.initialize(name, "At the beginning of the end step, if you control no creatures, sacrifice Thran Quarry.", Event.BegOfEachEndStep);
			ability.setInterveningIfClause();
		}
		
		/* glimmervoid_sacrifice */
		else if (name.equals("glimmervoid_sacrifice")) {
			ability.initialize(name, "At the beginning of the end step, if you control no artifacts, sacrifice Glimmervoid.", Event.BegOfEachEndStep);
			ability.setInterveningIfClause();
		}
		
		/* ballLightning_sacrifice */
		else if (name.equals("ballLightning_sacrifice"))
			ability.initialize(name, "At the beginning of the end step, sacrifice Ball Lightning.", Event.BegOfEachEndStep);
		
		/* ichorid_sacrifice */
		else if (name.equals("ichorid_sacrifice"))
			ability.initialize(name, "At the beginning of the end step, sacrifice Ichorid.", Event.BegOfEachEndStep);
		
		/* ichorid_reanimate */
		else if (name.equals("ichorid_reanimate")) {
			ability.initialize(name, "At the beginning of your upkeep, if Ichorid is in your graveyard, you may exile a black creature card other than Ichorid from your graveyard. If you do, return Ichorid to the battlefield.", Event.BegOfYourUpkeep);
			ability.setOptional();
			ability.setOrigin(Origin.GRVYRD);
			ability.setInterveningIfClause();
		}
		
		/* orzhovPontiff */
		else if (name.equals("orzhovPontiff")) {
			ability.initialize(name, "When Orzhov Pontiff enters the battlefield or the creature it haunts dies, choose one —§"
					+ "• Creatures you control get +1/+1 until end of turn.§"
					+ "• Creatures you don't control get -1/-1 until end of turn.",
					Event.EntersTheBattlefield);
			ability.setModal(2);
		}
		
		/* tormentorExarch */
		else if (name.equals("tormentorExarch")) {
			ability.initialize(name, "When Tormentor Exarch enters the battlefield, choose one —§"
					+ "• Target creature gets +2/+0 until end of turn.§"
					+ "• Target creature gets -0/-2 until end of turn.",
					Event.EntersTheBattlefield);
			ability.setModal(2);
			ability.addTargetRequirementMode(1, Category.Creature, Cardinality.ONE);
			ability.addTargetRequirementMode(2, Category.Creature, Cardinality.ONE);
		}
		
		/* cityOfBrass_Damage */
		else if (name.equals("cityOfBrass_Damage"))
			ability.initialize(name, "Whenever City of Brass becomes tapped, it deals 1 damage to you.", Event.BecomesTapped);
		
		/* monasteryMentor */
		else if (name.equals("monasteryMentor"))
			ability.initialize(name, "Whenever you cast a noncreature spell, create a 1/1 white Monk creature token with prowess.", Event.YouCastNonCreatureSpell);

		/* jeskaiAscendancy_pump */
		else if (name.equals("jeskaiAscendancy_pump"))
			ability.initialize(name, "Whenever you cast a noncreature spell, creatures you control get +1/+1 until end of turn. Untap those creatures.", Event.YouCastNonCreatureSpell);
	
		/* jeskaiAscendancy_draw */
		else if (name.equals("jeskaiAscendancy_draw")) {
			ability.initialize(name, "Whenever you cast a noncreature spell, you may draw a card. If you do, discard a card.", Event.YouCastNonCreatureSpell);
			ability.setOptional();
		}
		
		/* Prowess */
		else if (name.equals("prowess"))
			ability.initialize(Effect.PUMP, "Prowess <i>(Whenever you cast a noncreature spell, this creature gets +1/+1 until end of turn.)</i>", Event.YouCastNonCreatureSpell);
		
		/* modular_moveCounters */
		else if (name.equals("modular_moveCounters")) {
			ability.initialize(name, "When this creature dies, you may put its +1/+1 counters on target artifact creature.", Event.Dies, true);
			ability.addTargetRequirement(Category.ArtifactCreature);
			ability.setOptional();
		}
		
		/* purgingScythe */
		else if (name.equals("purgingScythe"))
			ability.initialize(name, "At the beginning of your upkeep, Purging Scythe deals 2 damage to the creature with the least toughness. If two or more creatures are tied for least toughness, you choose one of them.", Event.BegOfYourUpkeep);
		
		/* haunt */
		else if (name.equals("haunt")) {
			ability.initialize(name, "Haunt <i>(When this creature dies, exile it haunting target creature.)</i>", Event.Dies);
			ability.addTargetRequirement(Category.Creature);
		}
		
		/* persist */
		else if (name.equals("persist")) {
			ability.initialize(name, "Persist <i>(When this creature dies, if it had no -1/-1 counters on it, return it to the battlefield under its owner's control with a -1/-1 counter on it.)</i>", Event.Dies);
			ability.setInterveningIfClause();
		}
		
		/* Fading */
		else if (name.equals("fading_remove"))
			ability.initialize("fadingRemoveCounter", "At the beginning of your upkeep, remove a fade counter from this permanent. If you can't, sacrifice it.", Event.BegOfYourUpkeep, true);
		
		/* vanishing_remove */
		else if (name.equals("vanishing_remove"))
			ability.initialize("removeTimeCounter", "At the beginning of your upkeep, remove a time counter from this permanent.", Event.BegOfYourUpkeep, true);
		
		else if (name.equals("vanishing_sacrifice"))
			ability.initialize("vanishingSacrifice", "When the last time counter is removed, sacrifice this permanent.", Event.LastTimeCounterRemoved, true);
		
		/* spellQueller_enters */
		else if (name.equals("spellQueller_enters")) {
			ability.initialize(name, "When Spell Queller enters the battlefield, exile target spell with converted mana cost 4 or less.", Event.EntersTheBattlefield);
			ability.addTargetRequirement(Category.SpellWithCMC4orLess);
		}
		
		/* spellQueller_leaves */
		else if (name.equals("spellQueller_leaves"))
			ability.initialize(name, "When Spell Queller leaves the battlefield, the exiled card's owner may cast that card without paying its mana cost.", Event.LeavesTheBattlefield);
		
		/* fiendHunter_enters */
		else if (name.equals("fiendHunter_enters")) {
			ability.initialize(name, "When Fiend Hunter enters the battlefield, you may exile another target creature.", Event.EntersTheBattlefield);
			ability.addTargetRequirement(Category.AnotherCreature);
			ability.setOptional();
		}
		/* fiendHunter_leaves */
		else if (name.equals("fiendHunter_leaves"))
			ability.initialize(name, "When Fiend Hunter leaves the battlefield, return the exiled card to the battlefield under its owner's " + "control.", Event.LeavesTheBattlefield);
		
		/* darkHatchling */
		else if (name.equals("darkHatchling")) {
			ability.initialize(Effect.DESTROY_TARGET_CANNOT_REGEN, "When Dark Hatchling enters the battlefield, destroy target nonblack creature. It can't be regenerated.", Event.EntersTheBattlefield);
			ability.addTargetRequirement(Category.NonBlackCreature);
		}
		
		/* boneShredder */
		else if (name.equals("boneShredder")) {
			ability.initialize(Effect.DESTROY_TARGET_CAN_REGEN, "When Bone Shredder enters the battlefield, destroy target nonartifact, nonblack creature.", Event.EntersTheBattlefield);
			ability.addTargetRequirement(Category.NonArtifactNonBlackCreature);
		}
		
		/* ravenousChupacabra */
		else if (name.equals("ravenousChupacabra")) {
			ability.initialize(Effect.DESTROY_TARGET_CAN_REGEN, "When Ravenous Chupacabra enters the battlefield, destroy target creature an opponent controls.", Event.EntersTheBattlefield);
			ability.addTargetRequirement(Category.CreatureAnOpponentControls);
		}
		
		/* reclamationSage */
		else if (name.equals("reclamationSage")) {
			ability.initialize(Effect.DESTROY_TARGET_CAN_REGEN, "When Reclamation Sage enters the battlefield, you may destroy target artifact or enchantment.", Event.EntersTheBattlefield);
			ability.addTargetRequirement(Category.ArtifactOrEnchantment);
			ability.setOptional();
		}
		
		/* monkRealist */
		else if (name.equals("monkRealist")) {
			ability.initialize(Effect.DESTROY_TARGET_CAN_REGEN, "When Monk Realist enters the battlefield, destroy target enchantment.", Event.EntersTheBattlefield);
			ability.addTargetRequirement(Category.Enchantment);
		}
		
		/* Avalanche Riders */
		else if (name.equals("avalancheRiders")) {
			ability.initialize(Effect.DESTROY_TARGET_CAN_REGEN, "When Avalanche Riders enters the battlefield, destroy target land.", Event.EntersTheBattlefield);
			ability.addTargetRequirement(Category.Land);
		}
		
		/* emrakulTAT_timeWalk */
		else if (name.equals("emrakulTAT_timeWalk")) {
			ability.initialize(name, "When you cast Emrakul, take an extra turn after this one.", Event.YouCastThisSpell);
			ability.setOrigin(Origin.STACK);
		}
		
		/* emrakulTAT_shuffle */
		else if (name.equals("emrakulTAT_shuffle")) {
			ability.initialize(name, "When Emrakul is put into a graveyard from anywhere, its owner shuffles his or her graveyard into his or her library.", Event.PutIntoAGraveyardFromAnywhere);
			ability.setOrigin(Origin.ANYWHERE);
		}
		
		/* serraAvatar_shuffle */
		else if (name.equals("serraAvatar_shuffle")) {
			ability.initialize(name, "When Serra Avatar is put into a graveyard from anywhere, shuffle it into its owner's library.", Event.PutIntoAGraveyardFromAnywhere);
			ability.setOrigin(Origin.ANYWHERE);
		}
		
		/* scoriaWurm */
		else if (name.equals("scoriaWurm"))
			ability.initialize(name, "At the beginning of your upkeep, flip a coin. If you lose the flip, return Scoria Wurm to its owner's hand.", Event.BegOfYourUpkeep);
		
		/* venserSS */
		else if (name.equals("venserSS")) {
			ability.initialize("venserSS", "When Venser, Shaper Savant enters the battlefield, return target spell or permanent to its owner's hand.", Event.EntersTheBattlefield);
			ability.addTargetRequirement(Category.SpellOrPermanent);
		}
		
		/* craterHellion */
		else if (name.equals("craterHellion"))
			ability.initialize(name, "When Crater Hellion enters the battlefield, it deals 4 damage to each other creature.", Event.EntersTheBattlefield);
		
		/* stinkweedImp */
		else if (name.equals("stinkweedImp"))
			ability.initialize(name, "Whenever Stinkweed Imp deals combat damage to a creature, destroy that creature.", Event.ThisDealsCbtDmgToACreature);
		
		/* bulwark */
		else if (name.equals("bulwark")) {
			ability.initialize(name, "At the beginning of your upkeep, Bulwark deals X damage to target opponent, where X is the number of cards in your hand minus the number of cards in that player's hand.", Event.BegOfYourUpkeep);
			ability.addTargetRequirement(Category.Opponent);
		}
			
		
		/* electryte */
		else if (name.equals("electryte"))
			ability.initialize(name, "Whenever Electryte deals combat damage to defending player, it deals damage equal to its power to each blocking creature.", Event.ThisDealsCbtDmgToAPlayer);
		
		/* orderOfYawgmoth */
		else if (name.equals("orderOfYawgmoth"))
			ability.initialize(name, "Whenever Order of Yawgmoth deals damage to a player, that player discards a card.", Event.ThisDealsDmgToAPlayer);
		
		/* Scroll Thief */
		else if (name.equals("scrollThief"))
			ability.initialize(Effect.DRAW_A_CARD, "Whenever Scroll Thief deals combat damage to a player, draw a card.", Event.ThisDealsCbtDmgToAPlayer);
		
		/* longtuskCub_damage */
		else if (name.equals("longtuskCub_damage"))
			ability.initialize(Effect.GAIN_2_ENERGY, "Whenever Longtusk Cub deals combat damage to a player, you get {E}{E} <i>(two energy counters)</i>.", Event.ThisDealsCbtDmgToAPlayer);
		
		/* dynavoltTower_charge */
		else if (name.equals("dynavoltTower_charge"))
			ability.initialize(Effect.GAIN_2_ENERGY, "Whenever you cast an instant or sorcery spell, you get {E}{E} <i>(two energy counters)</i>.", Event.YouCastInstantOrSorcerySpell);
		
		/* torrentialGearhulk */
		else if (name.equals("torrentialGearhulk")) {
			ability.initialize(name, "When Torrential Gearhulk enters the battlefield, you may cast target instant card from your graveyard without paying its mana cost. If that card would be put into your graveyard this turn, exile it instead.", Event.EntersTheBattlefield);
			ability.addTargetRequirement(Category.InstantCardInYourGraveyard);
		}
		
		/* shieldedAetherThief_blocks */
		else if (name.equals("shieldedAetherThief_blocks"))
			ability.initialize(Effect.GAIN_1_ENERGY, "Whenever Shielded Aether Thief blocks, you get {E} <i>(an energy counter)</i>.", Event.Blocks);
		
		/* denProtector_regrowth */
		else if (name.equals("denProtector_regrowth")) {
			ability.initialize(Effect.REGROWTH, "When Den Protector is turned face up, return target card from your graveyard to your hand.", Event.IsTurnedFaceUp);
			ability.addTargetRequirement(Category.CardInYourGraveyard);
		}
		
		/* academyResearchers */
		else if (name.equals("academyResearchers")) {
			ability.initialize(name, "When Academy Researchers enters the battlefield, you may put an Aura card from your hand onto the battlefield attached to Academy Researchers.", Event.EntersTheBattlefield);
			ability.setOptional();
		}
		
		/* argothianWurm */
		else if (name.equals("argothianWurm"))
			ability.initialize(name, "When Argothian Wurm enters the battlefield, any player may sacrifice a land. If a player does, put Argothian Wurm on top of its owner's library.", Event.EntersTheBattlefield);
		
		/* abyssalHorror */
		else if (name.equals("abyssalHorror")) {
			ability.initialize(name, "When Abyssal Horror enters the battlefield, target player discards two cards.", Event.EntersTheBattlefield);
			ability.addTargetRequirement(Category.Player);
		}
		
		/* manabond */
		else if (name.equals("manabond")) {
			ability.initialize(name, "At the beginning of your end step, you may reveal your hand and put all land cards from it onto the battlefield. If you do, discard your hand.", Event.BegOfYourEndStep);
			ability.setOptional();
		}
		
		/* carpetOfFlowers */
		else if (name.equals("carpetOfFlowers")) {
			ability.initialize(name, "At the beginning of each of your main phases, if you haven't added mana with this ability this turn, you may add up to X mana of any one color, where X is the number of Islands target opponent controls.", Event.BegOfYourMainPhase);
			ability.setOptional();
			ability.addTargetRequirement(Category.Opponent);
		}
		
		/* hystrodon */
		else if (name.equals("hystrodon")) {
			ability.initialize(Effect.DRAW_A_CARD, "Whenever Hystrodon deals combat damage to a player, you may draw a card.", Event.ThisDealsCbtDmgToAPlayer);
			ability.setOptional();
		}
		
		/* jitte_dealsDamage */
		else if (name.equals("jitte_dealsDamage"))
			ability.initialize(name, "Whenever equipped creature deals combat damage, put two charge counters on Umezawa's Jitte.", Event.EquippedCreatureDealsCbtDmg);
		
		/* Sword of Fire and Ice */
		else if (name.equals("swordFireAndIce")) {
			ability.initialize(name, "Whenever equipped creature deals combat damage to a player, Sword of Fire and Ice deals 2 damage to any target and you draw a card.", Event.EquippedCreatureDealsCbtDmgToAPlayer);
			ability.addTargetRequirement(Category.AnyTarget);
		}
		
		/* Sword of Feast and Famine */
		else if (name.equals("swordFeastAndFamine"))
			ability.initialize(name, "Whenever equipped creature deals combat damage to a player, that player discards a card and you untap all lands you control.", Event.EquippedCreatureDealsCbtDmgToAPlayer);
		
		/* swordBodyAndMind */
		else if (name.equals("swordBodyAndMind"))
			ability.initialize(name, "Whenever equipped creature deals combat damage to a player, you create a 2/2 green Wolf creature token and that player puts the top ten cards of his or her library into his or her graveyard.", Event.EquippedCreatureDealsCbtDmgToAPlayer);

		/* diabolicServitude_reanimate */
		else if (name.equals("diabolicServitude_reanimate")) {
			ability.initialize(name, "When Diabolic Servitude enters the battlefield, return target creature card from your graveyard to the battlefield.", Event.EntersTheBattlefield);
			ability.addTargetRequirement(Category.CreatureCardInYourGraveyard);
		}
		
		/* destructiveUrge */
		else if (name.equals("destructiveUrge"))
			ability.initialize(name, "Whenever enchanted creature deals combat damage to a player, that player sacrifices a land.", Event.EnchantedCreatureDealsCbtDmgToAPlayer);
		
		/* venomousFangs */
		else if (name.equals("venomousFangs"))
			ability.initialize(name, "Whenever enchanted creature deals damage to a creature, destroy the other creature.", Event.EnchantedCreatureDealsDmgToACreature);
		
		/* vampiricEmbrace_addCounter */
		else if (name.equals("vampiricEmbrace_addCounter")) {
			ability.initialize(name, "Whenever a creature dealt damage by enchanted creature this turn dies, put a +1/+1 counter on that creature.", Event.ACreatureDies);
			ability.setInterveningIfClause();
		}
		
		/* vernalBloom */
		else if (name.equals("vernalBloom"))
			ability.initialize(name, "Whenever a Forest is tapped for mana, its controller adds {G} <i>(in addition to the mana the land produces).</i>", Event.APlayerTapsForestForMana);
		
		/* spreadingAlgae_destroy */
		else if (name.equals("spreadingAlgae_destroy"))
			ability.initialize(name, "When enchanted land becomes tapped, destroy it.", Event.EnchantedLandBecomesTapped);
		
		/* craterhoofBehemoth */
		else if (name.equals("craterhoofBehemoth"))
			ability.initialize(name, "When Craterhoof Behemoth enters the battlefield, creatures you control gain trample and get +X/+X until end of turn, where X is the number of creatures you control.", Event.EntersTheBattlefield);
		
		/* bondBeetle */
		else if (name.equals("bondBeetle")) {
			ability.initialize(name, "When Bond Beetle enters the battlefield, put a +1/+1 counter on target creature.", Event.EntersTheBattlefield);
			ability.addTargetRequirement(Category.Creature);
		}
		
		/* sleeperAgent_give */
		else if (name.equals("sleeperAgent_give")) {
			ability.initialize(name, "When Sleeper Agent enters the battlefield, target opponent gains control of it.", Event.EntersTheBattlefield);
			ability.addTargetRequirement(Category.Opponent);
		}
		
		/* sleeperAgent_damage */
		else if (name.equals("sleeperAgent_damage"))
			ability.initialize(name, "At the beginning of your upkeep, Sleeper Agent deals 2 damage to you.", Event.BegOfYourUpkeep);
		
		/* gildedDrake */
		else if (name.equals("gildedDrake")) {
			ability.initialize(name, "When Gilded Drake enters the battlefield, exchange control of Gilded Drake and up to one target creature an opponent controls. If you don't make an exchange, sacrifice Gilded Drake. This ability can't be countered except by spells and abilities. <i>(This effect lasts indefinitely.)</i>", Event.EntersTheBattlefield);
			ability.addTargetRequirement(Category.CreatureAnOpponentControls, Cardinality.UP_TO_ONE);
		}
		
		/* goblinLackey */
		else if (name.equals("goblinLackey")) {
			ability.initialize(name, "Whenever Goblin Lackey deals damage to a player, you may put a Goblin permanent card from your hand onto the battlefield.", Event.ThisDealsDmgToAPlayer);
			ability.setOptional();
		}
		
		/* goblinCadets */
		else if (name.equals("goblinCadets")) {
			ability.initialize(name, "Whenever Goblin Cadets blocks or becomes blocked, target opponent gains control of it. <i>(This removes Goblin Cadets from combat.)</i>", Event.BlocksOrBecomesBlocked);
			ability.addTargetRequirement(Category.Opponent);			
		}

		/* somnophore */
		else if (name.equals("somnophore")) {
			ability.initialize(name, "Whenever Somnophore deals damage to a player, tap target creature that player controls. That creature doesn't untap during its controller's untap step for as long as Somnophore remains on the battlefield.", Event.ThisDealsDmgToAPlayer);
			ability.addTargetRequirement(Category.CreatureAnOpponentControls);
		}
		
		/* fleshReaver */
		else if (name.equals("fleshReaver"))
			ability.initialize(name, "Whenever Flesh Reaver deals damage to a creature or opponent, Flesh Reaver deals that much damage to you.", Event.ThisDealsDamageToCreatureOrOpponent);
		
		/* contamination_sac */
		else if (name.equals("contamination_sac"))
			ability.initialize(name, "At the beginning of your upkeep, sacrifice Contamination unless you sacrifice a creature.", Event.BegOfYourUpkeep);
		
		/* solitaryConfinement_discard */
		else if (name.equals("solitaryConfinement_discard"))
			ability.initialize("masticore_discard", "At the beginning of your upkeep, sacrifice Solitary Confinement unless you discard a card.", Event.BegOfYourUpkeep);
		
		/* Mystic Snake */
		else if (name.equals("mysticSnake")) {
			ability.initialize(Effect.COUNTER_TARGET_STACKOBJECT, "When Mystic Snake enters the battlefield, counter target spell.", Event.EntersTheBattlefield);
			ability.addTargetRequirement(Category.Spell);
		}
		
		/* catacombSifter_scry */
		else if (name.equals("catacombSifter_scry"))
			ability.initialize(name, "Whenever another creature you control dies, scry 1.", Event.AnotherCreatureYouControlDies);
		
		/* wildGrowth */
		else if (name.equals("wildGrowth"))
			ability.initialize(name, "Whenever enchanted land is tapped for mana, its controller adds {G} <i>(in addition to the mana the land produces)</i>.", Event.EnchantedLandIsTappedForMana);
		
		/* Overgrowth */
		else if (name.equals("overgrowth"))
			ability.initialize(name, "Whenever enchanted land is tapped for mana, its controller adds an additional {G}{G}.", Event.EnchantedLandIsTappedForMana);
		
		/* fertileGround */
		else if (name.equals("fertileGround"))
			ability.initialize(name, "Whenever enchanted land is tapped for mana, its controller adds an additional one mana of any color.", Event.EnchantedLandIsTappedForMana);
		
		/* utopiaSprawl_additionalMana */
		else if (name.equals("utopiaSprawl_additionalMana"))
			ability.initialize(name, "Whenever enchanted Forest is tapped for mana, its controller adds an additional one mana of the chosen color.", Event.EnchantedLandIsTappedForMana);
		
		/* mirrisGuile */
		else if (name.equals("mirrisGuile")) {
			ability.initialize(name, "At the beginning of your upkeep, you may look at the top three cards of your library, then put them back in any order.", Event.BegOfYourUpkeep);
			ability.setOptional();
		}
		
		/* exaltedAngel */
		else if (name.equals("exaltedAngel"))
			ability.initialize(name, "Whenever Exalted Angel deals damage, you gain that much life.", Event.ThisDealsDamage);
		
		/* archangelAvacyn_creatureDies */
		else if (name.equals("archangelAvacyn_creatureDies"))
			ability.initialize(name, "When a non-Angel creature you control dies, transform Archangel Avacyn at the beginning of the next upkeep.", Event.AnonAngelCreatureYouControlDies);
		
		/* lilianaHeretical_transform */
		else if (name.equals("lilianaHeretical_transform"))
			ability.initialize(name, "Whenever another nontoken creature you control dies, exile Liliana, Heretical Healer, then return her to the battlefield transformed under her owner's control. If you do, create a 2/2 black Zombie creature token.", Event.AnotherNonTokenCreatureYouControlDies);

		/* wildDogs */
		else if (name.equals("wildDogs")) {
			ability.initialize(name, "At the beginning of your upkeep, if a player has more life than each other player, the player with the most life gains control of Wild Dogs.", Event.BegOfYourUpkeep);
			ability.setInterveningIfClause();
		}
		
		/* thranTurbine */
		else if (name.equals("thranTurbine")) {
			ability.initialize(name, "At the beginning of your upkeep, you may add {C} or {C}{C}. You can't spend this mana to cast spells.", Event.BegOfYourUpkeep);
			ability.setOptional();
		}
		
		/* presenceOfTheMaster */
		else if (name.equals("presenceOfTheMaster"))
			ability.initialize(name, "Whenever a player casts an enchantment spell, counter it.", Event.AnyPlayerCastAnEnchantSpell);
		
		/* titaniasChosen */
		else if (name.equals("titaniasChosen"))
			ability.initialize(name, "Whenever a player casts a green spell, put a +1/+1 counter on Titania's Chosen.", Event.AnyPlayerCastAGreenSpell);
		
		/* wallOfJunk */
		else if (name.equals("wallOfJunk_blocks"))
			ability.initialize(name, "When Wall of Junk blocks, return it to its owner's hand at end of combat. <i>(Return it only if it's on the battlefield.)</i>", Event.Blocks);
		
		/* wallOfJunk_return (delayed trigger) */
		else if (name.equals("wallOfJunk_return"))
			ability.initialize(name, "Return Wall of Junk to its owner's hand at end of combat.", Event.EndOfCombat);
		
		/* recantation_charge */
		else if (name.equals("recantation_charge")) {
			ability.initialize(Effect.ADD_VERSE_COUNTER, "At the beginning of your upkeep, you may put a verse counter on Recantation.", Event.BegOfYourUpkeep);
			ability.setOptional();
		}

		/* vileRequiem_charge */
		else if (name.equals("vileRequiem_charge")) {
			ability.initialize(Effect.ADD_VERSE_COUNTER, "At the beginning of your upkeep, you may put a verse counter on Vile Requiem.", Event.BegOfYourUpkeep);
			ability.setOptional();
		}
		
		/* warDance_charge */
		else if (name.equals("warDance_charge")) {
			ability.initialize(Effect.ADD_VERSE_COUNTER, "At the beginning of your upkeep, you may put a verse counter on War Dance.", Event.BegOfYourUpkeep);
			ability.setOptional();
		}
		
		/* rumblingCrescendo_charge */
		else if (name.equals("rumblingCrescendo_charge")) {
			ability.initialize(Effect.ADD_VERSE_COUNTER, "At the beginning of your upkeep, you may put a verse counter on Rumbling Crescendo.", Event.BegOfYourUpkeep);
			ability.setOptional();
		}
		
		/* barrinsCodex_charge */
		else if (name.equals("barrinsCodex_charge")) {
			ability.initialize(name, "At the beginning of your upkeep, you may put a page counter on Barrin's Codex.", Event.BegOfYourUpkeep);
			ability.setOptional();
		}
		
		/* serrasHymn_charge */
		else if (name.equals("serrasHymn_charge")) {
			ability.initialize(Effect.ADD_VERSE_COUNTER, "At the beginning of your upkeep, you may put a verse counter on Serra's Hymn.", Event.BegOfYourUpkeep);
			ability.setOptional();
		}
		
		/* serrasLiturgy_charge */
		else if (name.equals("serrasLiturgy_charge")) {
			ability.initialize(Effect.ADD_VERSE_COUNTER, "At the beginning of your upkeep, you may put a verse counter on Serra's Liturgy.", Event.BegOfYourUpkeep);
			ability.setOptional();
		}

		/* veilOfBirds */
		else if (name.equals("veilOfBirds")) {
			ability.initialize(name, "When an opponent casts a spell, if Veil of Birds is an enchantment, Veil of Birds becomes a 1/1 Bird creature with flying.", Event.AnOpponentCastSpell);
			ability.setInterveningIfClause();
		}
		
		/* veiledSentry */
		else if (name.equals("veiledSentry")) {
			ability.initialize(name, "When an opponent casts a spell, if Veiled Sentry is an enchantment, Veiled Sentry becomes an Illusion creature with power and toughness each equal to that spell's converted mana cost.", Event.AnOpponentCastSpell);
			ability.setInterveningIfClause();
		}
		
		/* veiledApparition */
		else if (name.equals("veiledApparition")) {
			ability.initialize(name, "When an opponent casts a spell, if Veiled Apparition is an enchantment, Veiled Apparition becomes a 3/3 Illusion creature with flying and \"At the beginning of your upkeep, sacrifice Veiled Apparition unless you pay {1}{U}.\"", Event.AnOpponentCastSpell);
			ability.setInterveningIfClause();
		}
		
		/* veiledCrocodile */
		else if (name.equals("veiledCrocodile")) {
			ability.initialize(name, "When a player has no cards in hand, if Veiled Crocodile is an enchantment, Veiled Crocodile becomes a 4/4 Crocodile creature.", Event.APlayerHasNoCardInHand);
			ability.setInterveningIfClause();
		}
		
		/* veiledSerpent */
		else if (name.equals("veiledSerpent")) {
			ability.initialize(name, "When an opponent casts a spell, if Veiled Serpent is an enchantment, Veiled Serpent becomes a 4/4 Serpent creature with \"This creature can't attack unless defending player controls an Island.\"", Event.AnOpponentCastSpell);
			ability.setInterveningIfClause();
		}
		
		/* opalAcrolith_animate */
		else if (name.equals("opalAcrolith_animate")) {
			ability.initialize(name, "Whenever an opponent casts a creature spell, if Opal Acrolith is an enchantment, Opal Acrolith becomes a 2/4 Soldier creature.", Event.AnOpponentCastCreatureSpell);
			ability.setInterveningIfClause();
		}
		
		/* opalArchangel */
		else if (name.equals("opalArchangel")) {
			ability.initialize(name, "When an opponent casts a creature spell, if Opal Archangel is an enchantment, Opal Archangel becomes a 5/5 Angel creature with flying and vigilance.", Event.AnOpponentCastCreatureSpell);
			ability.setInterveningIfClause();
		}
		
		/* opalCaryatid */
		else if (name.equals("opalCaryatid")) {
			ability.initialize(name, "When an opponent casts a creature spell, if Opal Caryatid is an enchantment, Opal Caryatid becomes a 2/2 Soldier creature.", Event.AnOpponentCastCreatureSpell);
			ability.setInterveningIfClause();
		}
		
		/* opalGargoyle */
		else if (name.equals("opalGargoyle")) {
			ability.initialize(name, "When an opponent casts a creature spell, if Opal Gargoyle is an enchantment, Opal Gargoyle becomes a 2/2 Gargoyle creature with flying.", Event.AnOpponentCastCreatureSpell);
			ability.setInterveningIfClause();
		}
		
		/* opalTitan */
		else if (name.equals("opalTitan")) {
			ability.initialize(name, "When an opponent casts a creature spell, if Opal Titan is an enchantment, Opal Titan becomes a 4/4 Giant creature with protection from each of that spell's colors.", Event.AnOpponentCastCreatureSpell);
			ability.setInterveningIfClause();
		}

		/* liltingRefrain_charge */
		else if (name.equals("liltingRefrain_charge")) {
			ability.initialize(Effect.ADD_VERSE_COUNTER, "At the beginning of your upkeep, you may put a verse counter on Lilting Refrain.", Event.BegOfYourUpkeep);
			ability.setOptional();
		}
		
		/* midsummerRevel_charge */
		else if (name.equals("midsummerRevel_charge")) {
			ability.initialize(Effect.ADD_VERSE_COUNTER, "At the beginning of your upkeep, you may put a verse counter on Midsummer Revel.", Event.BegOfYourUpkeep);
			ability.setOptional();
		}
		
		/* discordantDirge_charge */
		else if (name.equals("discordantDirge_charge")) {
			ability.initialize(Effect.ADD_VERSE_COUNTER, "At the beginning of your upkeep, you may put a verse counter on Discordant Dirge.", Event.BegOfYourUpkeep);
			ability.setOptional();
		}
		
		/* torchSong_charge */
		else if (name.equals("torchSong_charge")) {
			ability.initialize(Effect.ADD_VERSE_COUNTER, "At the beginning of your upkeep, you may put a verse counter on Torch Song.", Event.BegOfYourUpkeep);
			ability.setOptional();
		}
		
		/* smokestack_charge */
		else if (name.equals("smokestack_charge")) {
			ability.initialize(name, "At the beginning of your upkeep, you may put a soot counter on Smokestack.", Event.BegOfYourUpkeep);
			ability.setOptional();
		}
		
		/* smokestack_sac */
		else if (name.equals("smokestack_sac"))
			ability.initialize(name, "At the beginning of each player's upkeep, that player sacrifices a permanent for each soot counter on Smokestack.", Event.BegOfEachUpkeep);
		
		/* umbilicus */
		else if (name.equals("umbilicus"))
			ability.initialize(name, "At the beginning of each player's upkeep, that player returns a permanent he or she controls to its owner's hand unless he or she pays 2 life.", Event.BegOfEachUpkeep);
		
		/* noeticScales */
		else if (name.equals("noeticScales"))
			ability.initialize(name, "At the beginning of each player's upkeep, return to its owner's hand each creature that player controls with power greater than the number of cards in his or her hand.", Event.BegOfEachUpkeep);
		
		/* metrognome_discarded */
		else if (name.equals("metrognome_discarded")) {
			ability.initialize(name, "When a spell or ability an opponent controls causes you to discard Metrognome, create four 1/1 colorless Gnome artifact creature tokens.", Event.DiscardedByOpponent);
			ability.setOrigin(Origin.HAND);
		}
		
		/* lotusBlossom_charge */
		else if (name.equals("lotusBlossom_charge")) {
			ability.initialize(name, "At the beginning of your upkeep, you may put a petal counter on Lotus Blossom.", Event.BegOfYourUpkeep);
			ability.setOptional();
		}
		
		/* oppression */
		else if (name.equals("oppression"))
			ability.initialize(name, "Whenever a player casts a spell, that player discards a card.", Event.AnyPlayerCastASpell);
		
		/* graftedSkullcap_draw */
		else if (name.equals("graftedSkullcap_draw"))
			ability.initialize(Effect.DRAW_A_CARD, "At the beginning of your draw step, draw an additional card.", Event.BegOfYourDrawStep);
		
		/* graftedSkullcap_discard */
		else if (name.equals("graftedSkullcap_discard"))
			ability.initialize(name, "At the beginning of your end step, discard your hand.", Event.BegOfYourEndStep);
		
		/* vebulid_upkeep */
		else if (name.equals("vebulid_upkeep")) {
			ability.initialize(name, "At the beginning of your upkeep, you may put a +1/+1 counter on Vebulid.", Event.BegOfYourUpkeep);
			ability.setOptional();
		}
		
		/* vebulid_attaksOrBlocks */
		else if (name.equals("vebulid_attaksOrBlocks"))
			ability.initialize(name, "When Vebulid attacks or blocks, destroy it at end of combat.", Event.AttacksOrBlocks);
		
		/* vebulid_destroy (delayed trigger) */
		else if (name.equals("vebulid_destroy"))
			ability.initialize(name, "Destroy Vebulid at end of combat.", Event.EndOfCombat);
		
		/* sternProctor */
		else if (name.equals("sternProctor")) {
			ability.initialize("boomerang", "When Stern Proctor enters the battlefield, return target artifact or enchantment to its owner's hand.", Event.EntersTheBattlefield);
			ability.addTargetRequirement(Category.ArtifactOrEnchantment);
		}
		
		/* spireOwl */
		else if (name.equals("spireOwl"))
			ability.initialize(name, "When Spire Owl enters the battlefield, look at the top four cards of your library, then put them back in any order.", Event.EntersTheBattlefield);
		
		/* spinedFluke_sac */
		else if (name.equals("spinedFluke_sac"))
			ability.initialize(name, "When Spined Fluke enters the battlefield, sacrifice a creature.", Event.EntersTheBattlefield);
		
		/* oathOfLiliana_sac */
		else if (name.equals("oathOfLiliana_sac"))
			ability.initialize(name, "When Oath of Liliana enters the battlefield, each opponent sacrifices a creature.", Event.EntersTheBattlefield);
		
		/* oathOfLiliana_putToken */
		else if (name.equals("oathOfLiliana_putToken")) {
			ability.initialize(name, "At the beginning of each end step, if a planeswalker entered the battlefield under your control this turn, create a 2/2 black Zombie creature token.", Event.BegOfEachEndStep);
			ability.setInterveningIfClause();
		}
		
		/* oathOfNissa */
		else if (name.equals("oathOfNissa")) {
			ability.initialize(name, "When Oath of Nissa enters the battlefield, look at the top three cards of your library. "
					+ "You may reveal a creature, land, or planeswalker card from among them and put it into your hand. "
					+ "Put the rest on the bottom of your library in any order.", Event.EntersTheBattlefield);
		}
		
		/* grimHaruspex_draw */
		else if (name.equals("grimHaruspex_draw")) {
			ability.initialize(Effect.DRAW_A_CARD, "Whenever another nontoken creature you control dies, draw a card.", Event.AnotherNonTokenCreatureYouControlDies);
		}
		
		/* chaliceVoid_counterSpell */
		else if (name.equals("chaliceVoid_counterSpell")) {
			ability.initialize(name, "Whenever a player casts a spell with converted mana cost equal to the number of charge counters on Chalice of the Void, counter that spell.", Event.AnyPlayerCastASpell);
			ability.setInterveningIfClause();
		}
		
		/* lifeline_trigger */
		else if (name.equals("lifeline_trigger")) {
			ability.initialize(name, "Whenever a creature dies, if another creature is on the battlefield, return the first card to the battlefield under its owner's control at the beginning of the next end step.", Event.ACreatureDies);
			ability.setInterveningIfClause();
		}
		
		/* lifeline_reanimate */
		else if (name.equals("lifeline_reanimate"))
			ability.initialize(name, "Return the card to the battlefield under its owner's control.", Event.BegOfEachEndStep);
		
		/* angelicChorus_gainLife */
		else if (name.equals("angelicChorus_gainLife"))
			ability.initialize(name, "Whenever a creature enters the battlefield under your control, you gain life equal to its toughness.", Event.ACreatureEntersBattlefieldUnderYourControl);

		/* taintedAEther_sac */
		else if (name.equals("taintedAEther_sac"))
			ability.initialize(name, "Whenever a creature enters the battlefield, its controller sacrifices a creature or land.", Event.ACreatureEntersBattlefield);
		
		/* scald_damage */
		else if (name.equals("scald_damage"))
			ability.initialize(name, "Whenever a player taps an Island for mana, Scald deals 1 damage to that player.", Event.APlayerTapsIslandForMana);
		
		/* antagonism */
		else if (name.equals("antagonism"))
			ability.initialize(name, "At the beginning of each player's end step, Antagonism deals 2 damage to that player unless one of his or her opponents was dealt damage this turn.", Event.BegOfEachEndStep);
		
		/* bereavement_discard */
		else if (name.equals("bereavement_discard"))
			ability.initialize(name, "Whenever a green creature dies, its controller discards a card.", Event.AGreenCreatureDies);
		
		/* brilliantHalo_return */
		else if (name.equals("brilliantHalo_return"))
			ability.initialize(Effect.RETURN_TO_HAND, "When Brilliant Halo is put into a graveyard from the battlefield, return Brilliant Halo to its owner's hand.", Event.Dies);
		
		/* fortitude_return */
		else if (name.equals("fortitude_return"))
			ability.initialize(Effect.RETURN_TO_HAND, "When Fortitude is put into a graveyard from the battlefield, return Fortitude to its owner's hand.", Event.Dies);
		
		/* spreadingAlgae_return */
		else if (name.equals("spreadingAlgae_return"))
			ability.initialize(Effect.RETURN_TO_HAND, "When Spreading Algae is put into a graveyard from the battlefield, return Spreading Algae to its owner's hand.", Event.Dies);
		
		/* fieryMantle_return */
		else if (name.equals("fieryMantle_return"))
			ability.initialize(Effect.RETURN_TO_HAND, "When Fiery Mantle is put into a graveyard from the battlefield, return Fiery Mantle to its owner's hand.", Event.Dies);
		
		/* despondency_return */
		else if (name.equals("despondency_return"))
			ability.initialize(Effect.RETURN_TO_HAND, "When Despondency is put into a graveyard from the battlefield, return Despondency to its owner's hand.", Event.Dies);

		/* launch_return */
		else if (name.equals("launch_return"))
			ability.initialize(Effect.RETURN_TO_HAND, "When Launch is put into a graveyard from the battlefield, return Launch to its owner's hand.", Event.Dies);
		
		/* diabolicServitude_return */
		else if (name.equals("diabolicServitude_return")) {
			ability.initialize(name, "When the creature put onto the battlefield with Diabolic Servitude dies, exile it and return Diabolic Servitude to its owner's hand.", Event.ACreatureDies);
			ability.setInterveningIfClause();
		}
		
		/* diabolicServitude_exile */
		else if (name.equals("diabolicServitude_exile"))
			ability.initialize(name, "When Diabolic Servitude leaves the battlefield, exile the creature put onto the battlefield with Diabolic Servitude.", Event.LeavesTheBattlefield);
		
		/* remembrance_tutor */
		else if (name.equals("remembrance_tutor")) {
			ability.initialize(name, "Whenever a nontoken creature you control dies, you may search your library for a card with the same name as that creature, reveal it, and put it into your hand. If you do, shuffle your library.", Event.ANonTokenCreatureYouControlDies);
			ability.setOptional();
		}
		
		/* sporogenesis_putCounter */
		else if (name.equals("sporogenesis_putCounter")) {
			ability.initialize(name, "At the beginning of your upkeep, you may put a fungus counter on target nontoken creature.", Event.BegOfYourUpkeep);
			ability.addTargetRequirement(Category.NontokenCreature);
			ability.setOptional();
		}
		
		/* fecundity_draw */
		else if (name.equals("fecundity_draw")) {
			ability.initialize(name, "Whenever a creature dies, that creature's controller may draw a card.", Event.ACreatureDies);
			ability.setOptional();
			ability.setYouMayChoiceMaker(YouMayChoiceMaker.AdditionalDataController);
		}
		
		/* sporogenesis_makeToken */
		else if (name.equals("sporogenesis_makeToken"))
			ability.initialize(name, "Whenever a creature with a fungus counter on it dies, create a 1/1 green Saproling creature token for each fungus counter on that creature.", Event.ACreatureWithFungusDies);
		
		/* geistOfSaintTraft */
		else if (name.equals("geistOfSaintTraft"))
			ability.initialize(name, "Whenever Geist of Saint Traft attacks, create a 4/4 white Angel creature token with flying tapped and attacking. Exile that token at end of combat.", Event.Attacks);
		
		/* geistOfSaintTraft_exile */
		else if (name.equals("geistOfSaintTraft_exile"))
			ability.initialize(name, "Exile that token at end of combat.", Event.EndOfCombat);
		
		/* mishrasBauble_draw */
		else if (name.equals("mishrasBauble_draw"))
			ability.initialize(Effect.DRAW_A_CARD, "Draw a card.", Event.BegOfEachUpkeep);
		
		/* flickerwisp_return */
		else if (name.equals("flickerwisp_return"))
			ability.initialize(name, "Return that card to the battlefield under its owner's control at the beginning of the next end step.", Event.BegOfEachEndStep);
		
		/* necropotence_putCardInHand */
		else if (name.equals("necropotence_putCardInHand"))
			ability.initialize(name, "Put that card into your hand at the beginning of your next end step.", Event.BegOfYourEndStep);
		
		/* waylay_exile */
		else if (name.equals("waylay_exile"))
			ability.initialize(name, "Exile the tokens at the beginning of the next cleanup step.", Event.BegOfNextCleanupStep);
		
		/* chandraF_exileTokens */
		else if (name.equals("chandraF_exileTokens"))
			ability.initialize(name, "Exile the tokens at the beginning of the next end step.", Event.BegOfEachEndStep);
		
		/* sneakAttack_sacCreature */
		else if (name.equals("sneakAttack_sacCreature"))
			ability.initialize(name, "Sacrifice the creature at the beginning of the next end step.", Event.BegOfEachEndStep);
		
		/* nahiriUltimate_bounce */
		else if (name.equals("nahiriUltimate_bounce"))
			ability.initialize(name, "Return that artifact or creature card to your hand at the beginning of the next end step.", Event.BegOfEachEndStep);
		
		/* explores */
		else if (name.equals("explores"))
			ability.initialize(name, "When " + source.getName() + " enters the battlefield, it explores. <i>(Reveal the top card of your library. Put that card into your hand if it's a land. Otherwise, put a +1/+1 counter on this creature, then put the card back or put it into your graveyard.)</i>", Event.EntersTheBattlefield);
		
		/* jadelightRanger */
		else if (name.equals("jadelightRanger"))
			ability.initialize(name, "When Jadelight Ranger enters the battlefield, it explores, then it explores again. <i>(Reveal the top card of your library. Put that card into your hand if it's a land. Otherwise, put a +1/+1 counter on this creature, then put the card back or put it into your graveyard. Then repeat this process.)</i>", Event.EntersTheBattlefield);
		
		/* boundingKrasis */
		else if (name.equals("boundingKrasis")) {
			ability.initialize(name, "When Bounding Krasis enters the battlefield, you may tap or untap target creature.", Event.EntersTheBattlefield);
			ability.addTargetRequirement(Category.Creature);
			ability.setOptional();
		}
		
		/* rallyTheAncestors */
		else if (name.equals("rallyTheAncestors_exile"))
			ability.initialize(name, "Exile those creatures at the beginning of your next upkeep.", Event.BegOfYourUpkeep);
		
		/* archangelAvacyn_transform */
		else if (name.equals("archangelAvacyn_transform"))
			ability.initialize(name, "Transform Archangel Avacyn at the beginning of the next upkeep.", Event.BegOfEachUpkeep);
		
		/* Liability */
		else if (name.equals("liability_loseLife"))
			ability.initialize(name, "Whenever a nontoken permanent is put into a player's graveyard from the battlefield, that player loses 1 life.", Event.ANonTokenPermanentDies);
		
		/* planarVoid_exile */
		else if (name.equals("planarVoid_exile"))
			ability.initialize(name, "Whenever another card is put into a graveyard from anywhere, exile that card.", Event.ACardPutInAnyGydFromAnywhere);
		
		/* energyField_sac */
		else if (name.equals("energyField_sac"))
			ability.initialize(name, "When a card is put into your graveyard from anywhere, sacrifice Energy Field.", Event.ACardPutInYourGydFromAnywhere);
		
		/* eldraziSkyspawner */
		else if (name.equals("eldraziSkyspawner"))
			ability.initialize(name, "When Eldrazi Skyspawner enters the battlefield, put a 1/1 colorless Eldrazi Scion creature token onto the battlefield. It has \"Sacrifice this creature: Add {C}.\"", Event.EntersTheBattlefield);

		/* broodMonitor */
		else if (name.equals("broodMonitor"))
			ability.initialize(name, "When Brood Monitor enters the battlefield, put three 1/1 colorless Eldrazi Scion creature tokens onto the battlefield. They have \"Sacrifice this creature: Add {C}.\"", Event.EntersTheBattlefield);
		
		/* maokai_1 */
		else if (name.equals("maokai_1"))
			ability.initialize(name, "At the beginning of your upkeep, create a 1/1 green Sapling creature token.", Event.BegOfYourUpkeep);
		
		/* Deranged Hermit */
		else if (name.equals("derangedHermit_tokens"))
			ability.initialize(name, "When Deranged Hermit enters the battlefield, create four 1/1 green Squirrel creature tokens.", Event.EntersTheBattlefield);
		
		/* ashenRider */
		else if (name.equals("ashenRider")) {
			ability.initialize(name, "When Ashen Rider enters the battlefield or dies, exile target permanent.", Event.EntersTheBattleFieldOrDies);
			ability.addTargetRequirement(Category.Permanent);
		}
		
		/* exalted */
		else if (name.equals("exalted"))
			ability.initialize(name, "Exalted <i>(Whenever a creature you control attacks alone, that creature gets +1/+1 until end of turn.)</i>", Event.ACreatureYouControlAttacksAlone);
		
		/* zurTheEnchanter */
		else if (name.equals("zurTheEnchanter")) {
			ability.initialize(name, "Whenever Zur the Enchanter attacks, you may search your library for an enchantment card with converted mana cost 3 or less and put it onto the battlefield. If you do, shuffle your library.", Event.Attacks);
			ability.setOptional();
		}
		
		/* sunTitan */
		else if (name.equals("sunTitan")) {
			ability.initialize(name, "Whenever Sun Titan enters the battlefield or attacks, you may return target permanent card with converted mana cost 3 or less from your graveyard to the battlefield.", Event.EntersTheBattleFieldOrAttacks);
			ability.addTargetRequirement(Category.PermanentCardWithCdMC3OrLessInYourGraveyard);
			ability.setOptional();
		}
		
		/* graveTitan */
		else if (name.equals("graveTitan"))
			ability.initialize(name, "Whenever Grave Titan enters the battlefield or attacks, put two 2/2 black Zombie creature tokens onto the battlefield.", Event.EntersTheBattleFieldOrAttacks);
		
		/* siegeGang_putTokens */
		else if (name.equals("siegeGang_putTokens"))
			ability.initialize(name, "When Siege-Gang Commander enters the battlefield, create three 1/1 red Goblin creature tokens.", Event.EntersTheBattlefield);
		
		/* piaNalaar_makeToken */
		else if (name.equals("piaNalaar_makeToken"))
			ability.initialize(name, "When Pia Nalaar enters the battlefield, create a 1/1 colorless Thopter artifact creature token with flying.", Event.EntersTheBattlefield);
		
		/* Goblin Marshal */
		else if (name.equals("goblinMarshal"))
			ability.initialize(name, "When Goblin Marshal enters the battlefield or dies, put two 1/1 red Goblin creature tokens onto the battlefield.", Event.EntersTheBattleFieldOrDies);
		
		/* thornscapeBattlemage_1 */
		else if (name.equals("thornscapeBattlemage_1")) {
			ability.initialize(name, "When Thornscape Battlemage enters the battlefield, if it was kicked with its {R} kicker, it deals 2 damage to any target.", Event.EntersTheBattlefield);
			ability.addTargetRequirement(Category.AnyTarget);
			ability.setInterveningIfClause();
		}
		/* thornscapeBattlemage_2 */
		else if (name.equals("thornscapeBattlemage_2")) {
			ability.initialize(Effect.DESTROY_TARGET_CAN_REGEN, "When Thornscape Battlemage enters the battlefield, if it was kicked with its {W} kicker, destroy target artifact.", Event.EntersTheBattlefield);
			ability.addTargetRequirement(Category.Artifact);
			ability.setInterveningIfClause();
		}
		
		/* karn_pump */
		else if (name.equals("karn_pump"))
			ability.initialize(name, "Whenever Karn, Silver Golem blocks or becomes blocked, it gets -4/+4 until end of turn.", Event.BlocksOrBecomesBlocked);
		
		/* murderousRedcap_damage */
		else if (name.equals("murderousRedcap_damage")) {
			ability.initialize(name, "When Murderous Redcap enters the battlefield, it deals damage equal to its power to any target.", Event.EntersTheBattlefield);
			ability.addTargetRequirement(Category.AnyTarget);
		}
		
		/* flametongueKavu */
		else if (name.equals("flametongueKavu")) {
			ability.initialize(name, "When Flametongue Kavu enters the battlefield, it deals 4 damage to target creature.", Event.EntersTheBattlefield);
			ability.addTargetRequirement(Category.Creature);
		}
		
		/* Man-o'-War */
		else if (name.equals("manOwar")) {
			ability.initialize("unsummon", "When Man-o'-War enters the battlefield, return target creature to its owner's hand.", Event.EntersTheBattlefield);
			ability.addTargetRequirement(Category.Creature);
		}
		
		/* palaceJailer_monarch */
		else if (name.equals("palaceJailer_monarch"))
			ability.initialize(name, "When Palace Jailer enters the battlefield, you become the monarch.", Event.EntersTheBattlefield);
		
		/* palaceJailer_exile */
		else if (name.equals("palaceJailer_exile")) {
			ability.initialize(name, "When Palace Jailer enters the battlefield, exile target creature an opponent controls until an opponent becomes the monarch. <i>(That creature returns under its owner's control.)</i>", Event.EntersTheBattlefield);
			ability.addTargetRequirement(Category.CreatureAnOpponentControls);
		}
		
		/* cacklingFiend */
		else if (name.equals("cacklingFiend"))
			ability.initialize(name, "When Cackling Fiend enters the battlefield, each opponent discards a card.", Event.EntersTheBattlefield);
		
		/* reflectorMage */
		else if (name.equals("reflectorMage")) {
			ability.initialize(name, "When Reflector Mage enters the battlefield, return target creature an opponent controls to its owner's hand. That creature's owner can't cast spells with the same name as that creature until your next turn.", Event.EntersTheBattlefield);
			ability.addTargetRequirement(Category.CreatureAnOpponentControls);
		}
		
		/* siegeRhino */
		else if (name.equals("siegeRhino"))
			ability.initialize(name, "When Siege Rhino enters the battlefield, each opponent loses 3 life and you gain 3 life.", Event.EntersTheBattlefield);
		
		/* illusions_gainLife */
		else if (name.equals("illusions_gainLife"))
			ability.initialize(name, "When Illusions of Grandeur enters the battlefield, you gain 20 life.", Event.EntersTheBattlefield);
		
		/* illusions_loseLife */
		else if (name.equals("illusions_loseLife"))
			ability.initialize(name, "When Illusions of Grandeur leaves the battlefield, you lose 20 life.", Event.LeavesTheBattlefield);
		
		/* flickerwisp */
		else if (name.equals("flickerwisp")) {
			ability.initialize(name, "When Flickerwisp enters the battlefield, exile another target permanent. Return that card to the battlefield under its owner's control at the beginning of the next end step.", Event.EntersTheBattlefield);
			ability.addTargetRequirement(Category.AnotherPermanent);
		}
		
		/* Phyrexian Rager */
		else if (name.equals("phyrexianRager"))
			ability.initialize(name, "When Phyrexian Rager enters the battlefield, you draw a card and lose 1 life.", Event.EntersTheBattlefield);
		
		/* aetherVial_charge */
		else if (name.equals("aetherVial_charge")) {
			ability.initialize(name, "At the beginning of your upkeep, you may put a charge counter on Aether Vial.", Event.BegOfYourUpkeep);
			ability.setOptional();
		}

		/* assembleTheLegion */
		else if (name.equals("assembleTheLegion"))
			ability.initialize(name, "At the beginning of your upkeep, put a muster counter on Assemble the Legion. Then create a 1/1 red and white Soldier creature token with haste for each muster counter on Assemble the Legion.", Event.BegOfYourUpkeep);
		
		/* phyrexianArena */
		else if (name.equals("phyrexianArena")) {
			ability.initialize(name, "At the beginning of your upkeep, you draw a card and you lose 1 life.", Event.BegOfYourUpkeep);
		}
		
		/* sarcomancy_token */
		else if (name.equals("sarcomancy_token"))
			ability.initialize(name, "When Sarcomancy enters the battlefield, create a 2/2 black Zombie creature token.", Event.EntersTheBattlefield);
		
		/* sarcomancy_damage */
		else if (name.equals("sarcomancy_damage")) {
			ability.initialize(name, "At the beginning of your upkeep, if there are no Zombies on the battlefield, Sarcomancy deals 1 damage to you.", Event.BegOfYourUpkeep);
			ability.setInterveningIfClause();
		}
		
		/* darkConfidant */
		else if (name.equals("darkConfidant"))
			ability.initialize(name, "At the beginning of your upkeep, reveal the top card of your library and put that card into your hand. You lose life equal to its converted mana cost.", Event.BegOfYourUpkeep);
		
		/* bitterblossom */
		else if (name.equals("bitterblossom"))
			ability.initialize(name, "At the beginning of your upkeep, you lose 1 life and create a 1/1 black Faerie Rogue creature token with flying.", Event.BegOfYourUpkeep);
		
		/* pendrellFlux_upkeep */
		else if (name.equals("pendrellFlux_upkeep"))
			ability.initialize(name, "At the beginning of your upkeep, sacrifice this creature unless you pay its mana cost.", Event.BegOfYourUpkeep);
		
		/* manaVault_untap */
		else if (name.equals("manaVault_untap"))
			ability.initialize(name, "At the beginning of your upkeep, you may pay {4}. If you do, untap Mana Vault.", Event.BegOfYourUpkeep);
		
		/* manaVault_damage */
		else if (name.equals("manaVault_damage")) {
			ability.initialize(name, "At the beginning of your draw step, if Mana Vault is tapped, it deals 1 damage to you.", Event.BegOfYourDrawStep);
			ability.setInterveningIfClause();
		}
		
		/* aethersphereHarvester_energy */
		else if (name.equals("aethersphereHarvester_energy"))
			ability.initialize(Effect.GAIN_2_ENERGY, "When Aethersphere Harvester enters the battlefield, you get {E}{E} <i>(two energy counters)</i>.", Event.EntersTheBattlefield);
		
		/* rogueRefiner */
		else if (name.equals("rogueRefiner"))
			ability.initialize(name, "When Rogue Refiner enters the battlefield, draw a card and you get {E}{E} <i>(two energy counters)</i>.", Event.EntersTheBattlefield);
		
		/* voltaicBrawler_energy */
		else if (name.equals("voltaicBrawler_energy"))
			ability.initialize(Effect.GAIN_2_ENERGY, "When Voltaic Brawler enters the battlefield, you get {E}{E} <i>(two energy counters)</i>.", Event.EntersTheBattlefield);

		/* voltaicBrawler_pump */
		else if (name.equals("voltaicBrawler_pump")) {
			ability.initialize(name, "Whenever Voltaic Brawler attacks, you may pay {E}. If you do, it gets +1/+1 and gains trample until end of turn.", Event.Attacks);
			ability.setOptional();
		}
		
		/* fleetwheelCruiser */
		else if (name.equals("fleetwheelCruiser"))
			ability.initialize(Effect.CREW, "When Fleetwheel Cruiser enters the battlefield, it becomes an artifact creature until end of turn.", Event.EntersTheBattlefield);
		
		/* aetherHub_energy */
		else if (name.equals("aetherHub_energy"))
			ability.initialize(name, "When Aether Hub enters the battlefield, you get {E} <i>(an energy counter)</i>.", Event.EntersTheBattlefield);
		
		/* masticore_discard */
		else if (name.equals("masticore_discard"))
			ability.initialize(name, "At the beginning of your upkeep, sacrifice Masticore unless you discard a card.", Event.BegOfYourUpkeep);
		
		/* rishkarPeemaRenegade_counters */
		else if (name.equals("rishkarPeemaRenegade_counters")) {
			ability.initialize(name, "When Rishkar, Peema Renegade enters the battlefield, put a +1/+1 counter on each of up to two target creatures.", Event.EntersTheBattlefield);
			ability.addTargetRequirement(Category.Creature, Cardinality.UP_TO_TWO);
		}
		
		/* monkIdealist */
		else if (name.equals("monkIdealist")) {
			ability.initialize(Effect.REGROWTH, "When Monk Idealist enters the battlefield, return target enchantment card from your graveyard to your hand.", Event.EntersTheBattlefield);
			ability.addTargetRequirement(Category.EnchantmentCardInYourGraveyard);
		}
		
		/* Eternal Witness */
		else if (name.equals("eternalWitness")) {
			ability.initialize(Effect.REGROWTH, "When Eternal Witness enters the battlefield, return target card from your graveyard to your hand.", Event.EntersTheBattlefield);
			ability.addTargetRequirement(Category.CardInYourGraveyard);
		}

		/* imaginaryPet */
		else if (name.equals("imaginaryPet")) {
			ability.initialize(name, "At the beginning of your upkeep, if you have a card in hand, return Imaginary Pet to its owner's hand.", Event.BegOfYourUpkeep);
			ability.setInterveningIfClause();
		}
		
		/* seasonedMarshal */
		else if (name.equals("seasonedMarshal")) {
			ability.initialize(name, "Whenever Seasoned Marshal attacks, you may tap target creature.", Event.Attacks);
			ability.addTargetRequirement(Category.Creature);
			ability.setOptional();
		}
		
		/* ravenousSkirge */
		else if (name.equals("ravenousSkirge"))
			ability.initialize("hollowDogs", "Whenever Ravenous Skirge attacks, it gets +2/+0 until end of turn.", Event.Attacks);
		
		/* hollowDogs */
		else if (name.equals("hollowDogs"))
			ability.initialize(name, "Whenever Hollow Dogs attacks, it gets +2/+0 until end of turn.", Event.Attacks);
		
		/* retromancer */
		else if (name.equals("retromancer"))
			ability.initialize(name, "Whenever Retromancer becomes the target of a spell or ability, Retromancer deals 3 damage to that spell or ability's controller.", Event.BecomesTheTargetOfSpellOrAb);
		
		/* dromosaur */
		else if (name.equals("dromosaur"))
			ability.initialize(name, "Whenever Dromosaur blocks or becomes blocked, it gets +2/-2 until end of turn.", Event.BlocksOrBecomesBlocked);
		
		/* viashinoWeaponsmith */
		else if (name.equals("viashinoWeaponsmith"))
			ability.initialize(name, "Whenever Viashino Weaponsmith becomes blocked by a creature, Viashino Weaponsmith gets +2/+2 until end of turn.", Event.BecomesBlockedByACreature);
		
		/* caveTiger */
		else if (name.equals("caveTiger"))
			ability.initialize(Effect.PUMP, "Whenever Cave Tiger becomes blocked by a creature, Cave Tiger gets +1/+1 until end of turn.", Event.BecomesBlockedByACreature);
		
		/* cathodion */
		else if (name.equals("cathodion"))
			ability.initialize(name, "When Cathodion dies, add {C}{C}{C}.", Event.Dies);
		
		/* priestGix */
		else if (name.equals("priestGix"))
			ability.initialize(name, "When Priest of Gix enters the battlefield, add {B}{B}{B}.", Event.EntersTheBattlefield);
		
		/* coilingOracle */
		else if (name.equals("coilingOracle"))
			ability.initialize(name, "When Coiling Oracle enters the battlefield, reveal the top card of your library. If it's a land card, put it onto the battlefield. Otherwise, put that card into your hand.", Event.EntersTheBattlefield);
		
		/* elvishVisionary */
		else if (name.equals("elvishVisionary"))
			ability.initialize(Effect.DRAW_A_CARD, "When Elvish Visionary enters the battlefield, draw a card.", Event.EntersTheBattlefield);
		
		/* wallOfBlossoms */
		else if (name.equals("wallOfBlossoms"))
			ability.initialize(Effect.DRAW_A_CARD, "When Wall of Blossoms enters the battlefield, draw a card.", Event.EntersTheBattlefield);
		
		/* Baleful Strix */
		else if (name.equals("balefulStrix"))
			ability.initialize(Effect.DRAW_A_CARD, "When Baleful Strix enters the battlefield, draw a card.", Event.EntersTheBattlefield);
		
		/* goblinMatron */
		else if (name.equals("goblinMatron")) {
			ability.initialize(name, "When Goblin Matron enters the battlefield, you may search your library for a Goblin card, reveal that card, and put it into your hand. If you do, shuffle your library.", Event.EntersTheBattlefield);
			ability.setOptional();
		}

		/* recruiterOfTheGuard */
		else if (name.equals("recruiterOfTheGuard")) {
			ability.initialize(name, "When Recruiter of the Guard enters the battlefield, you may search your library for a creature card with toughness 2 or less, reveal it, put it into your hand, then shuffle your library.", Event.EntersTheBattlefield);
			ability.setOptional();
		}
		
		/* peregrineDrake */
		else if (name.equals("peregrineDrake"))
			ability.initialize(name, "When Peregrine Drake enters the battlefield, untap up to five lands.", Event.EntersTheBattlefield);
		
		/* greatWhale */
		else if (name.equals("greatWhale"))
			ability.initialize(name, "When Great Whale enters the battlefield, untap up to seven lands.", Event.EntersTheBattlefield);
		
		/* runeScarredDemon */
		else if (name.equals("runeScarredDemon"))
			ability.initialize("demonicTutor", "When Rune-Scarred Demon enters the battlefield, search your library for a card, put it into your hand, then shuffle your library.", Event.EntersTheBattlefield);

		/* stoneforgeMystic_fetch */
		else if (name.equals("stoneforgeMystic_tutor")) {
			ability.initialize(name, "When Stoneforge Mystic enters the battlefield, you may search your library for an Equipment card, reveal it, put it into your hand, then shuffle your library.", Event.EntersTheBattlefield);
			ability.setOptional();
		}
		
		/* nissaVS_tutorForest (tutor a basic forest) */
		else if (name.equals("nissaVS_tutorForest")) {
			ability.initialize(name, "When Nissa, Vastwood Seer enters the battlefield, you may search your library for a basic Forest card, reveal it, put it into your hand, then shuffle your library.", Event.EntersTheBattlefield);
			ability.setOptional();
		}
		/* nissaVS_transform (landfall and tranform if nb lands >= 7) */
		else if (name.equals("nissaVS_transform")) {
			ability.initialize(name, "Whenever a land enters the battlefield under your control, if you control seven or more lands, exile Nissa, then return her to the battlefield transformed under her owner's control.", Event.ALandEntersBattlefieldUnderYourControl);
			ability.setInterveningIfClause();
		}
		
		/* vengevine */
		else if (name.equals("vengevine")) {
			ability.initialize(name, "Whenever you cast a spell, if it's the second creature spell you cast this turn, you may return Vengevine from your graveyard to the battlefield.", Event.YouCastCreatureSpell);
			ability.setInterveningIfClause();
			ability.setOrigin(Origin.GRVYRD);
			ability.setOptional();
		}
		
		/* squeeGoblinNabob */
		else if (name.equals("squeeGoblinNabob")) {
			ability.initialize(Effect.RETURN_TO_HAND, "At the beginning of your upkeep, you may return Squee, Goblin Nabob from your graveyard to your hand.", Event.BegOfYourUpkeep);
			ability.setOrigin(Origin.GRVYRD);
			ability.setOptional();
		}		
		
		/* suspend_remove_counter */
		else if (name.equals("suspend_remove_counter")) {
			ability.initialize("removeTimeCounter", "At the beginning of your upkeep, if this card is suspended, remove a time counter from it.", Event.BegOfYourUpkeep, true);
			ability.setOrigin(Origin.EXILE);
			ability.setInterveningIfClause();
		}
		
		/* suspend_cast_from_exile */
		else if (name.equals("suspend_cast_from_exile")) {
			ability.initialize(name, "When the last time counter is removed from this card, if it’s exiled, play it without paying its mana cost if able. If you can’t, it remains exiled. If you cast a creature spell this way, it gains haste until you lose control of the spell or the permanent it becomes.", Event.LastTimeCounterRemoved, true);
			ability.setOrigin(Origin.EXILE);
			ability.setInterveningIfClause();
		}
		
		/* reclusiveWight */
		else if (name.equals("reclusiveWight")) {
			ability.initialize(name, "At the beginning of your upkeep, if you control another nonland permanent, sacrifice Reclusive Wight.", Event.BegOfYourUpkeep);
			ability.setInterveningIfClause();
		}
		
		/* netherSpirit */
		else if (name.equals("netherSpirit")) {
			ability.initialize(name, "At the beginning of your upkeep, if Nether Spirit is the only creature card in your graveyard, you may return Nether Spirit to the battlefield.", Event.BegOfYourUpkeep);
			ability.setOrigin(Origin.GRVYRD);
			ability.setInterveningIfClause();
			ability.setOptional();
		}
		
		/* skitteringSkirge */
		else if (name.equals("skitteringSkirge"))
			ability.initialize(name, "When you cast a creature spell, sacrifice Skittering Skirge.", Event.YouCastCreatureSpell);
		
		/* primordialSage */
		else if (name.equals("primordialSage")) {
			ability.initialize(Effect.DRAW_A_CARD, "Whenever you cast a creature spell, you may draw a card.", Event.YouCastCreatureSpell);
			ability.setOptional();
		}
		
		/* monarchExtraDraw */
		else if (name.equals("monarchExtraDraw")) {
			ability.initialize(Effect.DRAW_A_CARD, "At the beginning of the monarch’s end step, that player draws a card.", Event.BegOfYourEndStep);
			ability.setOrigin(Origin.COMMAND);
		}
		
		/* monarchCombatDamaged */
		else if (name.equals("monarchCombatDamaged")) {
			ability.initialize(name, "Whenever a creature deals combat damage to the monarch, its controller becomes the monarch.", Event.MonarchWasDamaged);
			ability.setOrigin(Origin.COMMAND);
		}
		
		/* youngPyromancer */
		else if (name.equals("youngPyromancer"))
			ability.initialize(name, "Whenever you cast an instant or sorcery spell, create a 1/1 red Elemental creature token.", Event.YouCastInstantOrSorcerySpell);
		
		/* eidolonOfBlossoms */
		else if (name.equals("eidolonOfBlossoms"))
			ability.initialize(Effect.DRAW_A_CARD, "<i>Constellation</i> — Whenever Eidolon of Blossoms or another enchantment enters the battlefield under your control, draw a card.", Event.ThisOrAnotherEnchantETB);
		
		/* argothianEnchantress, enchantressPresence */
		else if (name.equals("argothianEnchantress") || name.equals("enchantressPresence"))
			ability.initialize(Effect.DRAW_A_CARD, "Whenever you cast an enchantment spell, draw a card.", Event.YouCastEnchantmentSpell);
		
		/* ragingRavine_pump */
		else if (name.equals("ragingRavine_pump"))
			ability.initialize(Effect.PUT_PLUS_ONE_COUNTER_ON_THIS, "Whenever this creature attacks, put a +1/+1 counter on it.", Event.Attacks);
		
		/* ------------------ SAGAS --------------------- */
		
		/* History of Benalia */
		else if (name.equals("historyOfBenalia_1"))
			ability.initialize("historyOfBenalia_1_2", "I - Create a 2/2 white Knight creature token with vigilance.", Event.LoreCountersAdded);
		else if (name.equals("historyOfBenalia_2"))
			ability.initialize("historyOfBenalia_1_2", "II - Create a 2/2 white Knight creature token with vigilance.", Event.LoreCountersAdded);
		else if (name.equals("historyOfBenalia_3"))
			ability.initialize(name, "III - Knights you control get +2/+1 until end of turn.", Event.LoreCountersAdded);
		
		/* The Eldest Reborn */
		else if (name.equals("theEldestReborn_1"))
			ability.initialize(name, "I - Each opponent sacrifices a creature or planeswalker.", Event.LoreCountersAdded);
		else if (name.equals("theEldestReborn_2"))
			ability.initialize("cacklingFiend", "II - Each opponent discards a card.", Event.LoreCountersAdded);
		else if (name.equals("theEldestReborn_3")) {
			ability.initialize("zombify", "III - Put target creature or planeswalker card from a graveyard onto the battlefield under your control.", Event.LoreCountersAdded);
			ability.addTargetRequirement(Category.CreatureOrPlaneswalkerCardinAnyGraveyard);
		}
		
		/* Should not get here */
		else {
			ability = null;
			System.err.println("Error : unknown triggered ability : " + name);
			System.exit(0);
		}
		
		return ability;
	}
}
