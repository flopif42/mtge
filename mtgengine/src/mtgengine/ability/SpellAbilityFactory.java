package mtgengine.ability;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mtgengine.Target.Category;
import mtgengine.TargetRequirement.Cardinality;
import mtgengine.card.Card;
import mtgengine.cost.AdditionalCost;
import mtgengine.cost.AdditionalCost.Requirement;
import mtgengine.effect.Effect;

public class SpellAbilityFactory {

	public static SpellAbility create(String name, Card source) {
		SpellAbility spell = new SpellAbility(name);
		
		//  Should never get here
		if (name == null)
			return null;

		/* abruptDecay */
		else if (name.equals("abruptDecay")) {
			spell.setEffect(Effect.DESTROY_TARGET_CAN_REGEN, "Destroy target nonland permanent with converted mana cost 3 or less.");
			source.addTargetRequirement(Category.NonlandPermanentWithCMC3OrLess);
		}
		
		/* traversetheUlvenwald */
		else if (name.equals("traversetheUlvenwald")) {
			spell.setEffect(name, "Search your library for a basic land card, reveal it, put it into your hand, then shuffle your library.§"+
								  "<i>Delirium</i> — If there are four or more card types among cards in your graveyard, instead search your library for a creature or land card, reveal it, put it into your hand, then shuffle your library.");
		}
		
		/* naturalObsolescence */
		else if (name.equals("naturalObsolescence")) {
			spell.setEffect(name, "Put target artifact on the bottom of its owner's library.");
			source.addTargetRequirement(Category.Artifact);
		}
		
		/* absorb */
		else if (name.equals("absorb")) {
			spell.setEffect(name, "Counter target spell. You gain 3 life.");
			source.addTargetRequirement(Category.Spell);
		}
		
		/* accumulatedKnowledge */
		else if (name.equals("accumulatedKnowledge"))
			spell.setEffect(name, "Draw a card, then draw cards equal to the number of cards named Accumulated Knowledge in all graveyards.");
		
		/* animateLand */
		else if (name.equals("animateLand")) {
			spell.setEffect(name, "Until end of turn, target land becomes a 3/3 creature that's still a land.");
			source.addTargetRequirement(Category.Land);
		}
		
		/* anticipate */
		else if (name.equals("anticipate"))
			spell.setEffect(name, "Look at the top three cards of your library. Put one of them into your hand and the rest on the bottom of your library in any order.");
		
		/* corrupt */
		else if (name.equals("corrupt")) {
			spell.setEffect(name, "Corrupt deals damage to any target equal to the number of Swamps you control. You gain life equal to the damage dealt this way.");
			source.addTargetRequirement(Category.AnyTarget);
		}
		
		/* catastrophe */
		else if (name.equals("catastrophe"))
			spell.setEffect(name, "Destroy all lands or all creatures. Creatures destroyed this way can't be regenerated.");
		
		/* illGottenGains */
		else if (name.equals("illGottenGains"))
			spell.setEffect(name, "Exile Ill-Gotten Gains. Each player discards his or her hand, then returns up to three cards from his or her graveyard to his or her hand.");
		
		/* hush */
		else if (name.equals("hush"))
			spell.setEffect(name, "Destroy all enchantments.");
		
		/* armageddon */
		else if (name.equals("armageddon"))
			spell.setEffect(name, "Destroy all lands.");
		
		/* ancestralRecall */
		else if (name.equals("ancestralRecall")) {
			spell.setEffect(name, "Target player draws three cards.");
			source.addTargetRequirement(Category.Player);
		}

		/* Thought Scour */
		else if (name.equals("thoughtScour")) {
			spell.setEffect(name, "Target player puts the top two cards of his or her library into his or her graveyard.<br>Draw a card.");
			source.addTargetRequirement(Category.Player);
		}
		
		/* anguishedUnmaking */
		else if (name.equals("anguishedUnmaking")) {
			spell.setEffect(name, "Exile target nonland permanent. You lose 3 life.");
			source.addTargetRequirement(Category.NonlandPermanent);
		}
		
		/* beastWithin */
		else if (name.equals("beastWithin")) {
			spell.setEffect(name, "Destroy target permanent. Its controller creates a 3/3 green Beast creature token.");
			source.addTargetRequirement(Category.Permanent);
		}
		
		/* headlongRush */
		else if (name.equals("headlongRush"))
			spell.setEffect(name, "Attacking creatures gain first strike until end of turn.");
		
		/* hibernation */
		else if (name.equals("hibernation"))
			spell.setEffect(name, "Return all green permanents to their owners' hands.");
		
		/* timeSpiral */
		else if (name.equals("timeSpiral"))
			spell.setEffect(name, "Exile Time Spiral. Each player shuffles his or her graveyard and hand into his or her library, then draws seven cards. You untap up to six lands.");
		
		/* victimize */
		else if (name.equals("victimize")) {
			spell.setEffect(name, "Choose two target creature cards in your graveyard. Sacrifice a creature. If you do, return the chosen cards to the battlefield tapped.");
			source.addTargetRequirement(Category.CreatureCardInYourGraveyard, Cardinality.TWO);
		}
		
		/* whirlwind */
		else if (name.equals("whirlwind"))
			spell.setEffect(name, "Destroy all creatures with flying.");
		
		/* unnerve */
		else if (name.equals("unnerve"))
			spell.setEffect(name, "Each opponent discards two cards.");
		
		/* titaniasBoon */
		else if (name.equals("titaniasBoon"))
			spell.setEffect(name, "Put a +1/+1 counter on each creature you control.");
		
		/* steamBlast */
		else if (name.equals("steamBlast"))
			spell.setEffect(name, "Steam Blast deals 2 damage to each creature and each player.");
		
		/* faultLine */
		else if (name.equals("faultLine"))
			spell.setEffect(name, "Fault Line deals X damage to each creature without flying and each player.");
		
		/* heatRay */
		else if (name.equals("heatRay")) {
			spell.setEffect(Effect.DEAL_X_DAMAGE_TO_TARGET, "Heat Ray deals X damage to target creature.");
			source.addTargetRequirement(Category.Creature);
		}
		
		/* blaze */
		else if (name.equals("blaze")) {
			spell.setEffect(Effect.DEAL_X_DAMAGE_TO_TARGET, "Blaze deals X damage to any target.");
			source.addTargetRequirement(Category.AnyTarget);
		}
		
		/* lull */
		else if (name.equals("lull"))
			spell.setEffect(name, "Prevent all combat damage that would be dealt this turn.");
		
		/* rescind */
		else if (name.equals("rescind")) {
			spell.setEffect("boomerang", "Return target permanent to its owner's hand.");
			source.addTargetRequirement(Category.Permanent);
		}
		
		/* boomerang */
		else if (name.equals("boomerang")) {
			spell.setEffect(name, "Return target permanent to its owner's hand.");
			source.addTargetRequirement(Category.Permanent);
		}
		
		/* hoodwink */
		else if (name.equals("hoodwink")) {
			spell.setEffect(name, "Return target artifact, enchantment, or land to its owner's hand.");
			source.addTargetRequirement(Category.ArtifactOrEnchantmentOrLand);
		}
		
		/* brainstorm */
		else if (name.equals("brainstorm"))
			spell.setEffect(name, "Draw three cards, then put two cards from your hand on top of your library in any order.");
		
		/* callOfTheHerd */
		else if (name.equals("callOfTheHerd"))
			spell.setEffect(name, "Create a 3/3 green Elephant creature token.");
		
		/* humble */
		else if (name.equals("humble")) {
			spell.setEffect(name, "Until end of turn, target creature loses all abilities and has base power and toughness 0/1.");
			source.addTargetRequirement(Category.Creature);
		}
		
		/* capsize */
		else if (name.equals("capsize")) {
			spell.setEffect("boomerang", "Return target permanent to its owner's hand.");
			source.addTargetRequirement(Category.Permanent);
		}
		
		/* riteOfReplication */
		else if (name.equals("riteOfReplication")) {
			spell.setEffect(name, "Create a token that's a copy of target creature. If Rite of Replication was kicked, create five of those tokens instead.");
			source.addTargetRequirement(Category.Creature);
		}
		
		/* congregate */
		else if (name.equals("congregate")) {
			spell.setEffect(name, "Target player gains 2 life for each creature on the battlefield.");
			source.addTargetRequirement(Category.Player);
		}
		
		/* curfew */
		else if (name.equals("curfew"))
			spell.setEffect(name, "Each player returns a creature he or she controls to its owner's hand.");
		
		/* clear */
		else if (name.equals("clear")) {
			spell.setEffect(Effect.DESTROY_TARGET_CAN_REGEN, "Destroy target enchantment.");
			source.addTargetRequirement(Category.Enchantment);
		}
		
		/* catalog */
		else if (name.equals("catalog"))
			spell.setEffect(name, "Draw two cards, then discard a card.");
		
		/* carefulStudy */
		else if (name.equals("carefulStudy"))
			spell.setEffect(name, "Draw two cards, then discard two cards.");
		
		/* franticSearch */
		else if (name.equals("franticSearch"))
			spell.setEffect(name, "Draw two cards, then discard two cards. Untap up to three lands.");
		
		/* chordOfCalling */
		else if (name.equals("chordOfCalling"))
			spell.setEffect(name, "Search your library for a creature card with converted mana cost X or less and put it onto the battlefield. Then shuffle your library.");
		
		/* collectedCompany */
		else if (name.equals("collectedCompany"))
			spell.setEffect(name, "Look at the top six cards of your library. Put up to two creature cards with converted mana cost 3 or less from among them onto the battlefield. Put the rest on the bottom of your library in any order.");
		
		/* rewind */
		else if (name.equals("rewind")) {
			spell.setEffect(name, "Counter target spell. Untap up to four lands.");
			source.addTargetRequirement(Category.Spell);
		}
		
		/* counterspell */
		else if (name.equals("counterspell")) {
			spell.setEffect(Effect.COUNTER_TARGET_STACKOBJECT, "Counter target spell.");
			source.addTargetRequirement(Category.Spell);
		}
		
		/* raze */
		else if (name.equals("raze")) {
			spell.setEffect(Effect.DESTROY_TARGET_CAN_REGEN, "As an additional cost to cast this spell, sacrifice a land. Destroy target land.");
			source.addTargetRequirement(Category.Land);
		}
		
		/* showAndTell */
		else if (name.equals("showAndTell"))
			spell.setEffect(name, "Each player may put an artifact, creature, enchantment, or land card from his or her hand onto the battlefield.");
		
		/* rejuvenate */
		else if (name.equals("rejuvenate"))
			spell.setEffect(name, "You gain 6 life.");
		
		/* reprocess */
		else if (name.equals("reprocess"))
			spell.setEffect(name, "Sacrifice any number of artifacts, creatures, and/or lands. Draw a card for each permanent sacrificed this way.");
		
		/* cropRotation */
		else if (name.equals("cropRotation"))
			spell.setEffect(name, "As an additional cost to cast this spell, sacrifice a land.§Search your library for a land card and put that card onto the battlefield. Then shuffle your library.");
		
		/* darkPetition */
		else if (name.equals("darkPetition"))
			spell.setEffect(name, "Search your library for a card and put that card into your hand. Then shuffle your library. <i>Spell mastery</i> — If there are two or more instant and/or sorcery cards in your graveyard, add {B}{B}{B}.");
		
		/* darkRitual */
		else if (name.equals("darkRitual"))
			spell.setEffect(name, "Add {B}{B}{B}.");
		
		/* declarationInStone */
		else if (name.equals("declarationInStone")) {
			spell.setEffect(name, "Exile target creature and all other creatures its controller controls with the same name as that creature. That player investigates for each nontoken creature exiled this way.");
			source.addTargetRequirement(Category.Creature);
		}
		
		/* demonicTutor */
		else if (name.equals("demonicTutor"))
			spell.setEffect(name, "Search your library for a card and put that card into your hand. Then shuffle your library.");
		
		/* diabolicEdict */
		else if (name.equals("diabolicEdict")) {
			spell.setEffect(name, "Target player sacrifices a creature.");
			source.addTargetRequirement(Category.Player);
		}
		
		/* diabolicTutor */
		else if (name.equals("diabolicTutor"))
			spell.setEffect("demonicTutor", "Search your library for a card and put that card into your hand. Then shuffle your library.");
		
		/* disenchant */
		else if (name.equals("disenchant")) {
			spell.setEffect(Effect.DESTROY_TARGET_CAN_REGEN, "Destroy target artifact or enchantment.");
			source.addTargetRequirement(Category.ArtifactOrEnchantment);
		}
		
		/* disfigure */
		else if (name.equals("disfigure")) {
			spell.setEffect(name, "Target creature gets -2/-2 until end of turn.");
			source.addTargetRequirement(Category.Creature);
		}
		
		/* dismember */
		else if (name.equals("dismember")) {
			spell.setEffect(name, "Target creature gets -5/-5 until end of turn.");
			source.addTargetRequirement(Category.Creature);
		}
		
		/* gutShot */
		else if (name.equals("gutShot")) {
			spell.setEffect(name, "Gut Shot deals 1 damage to any target.");
			source.addTargetRequirement(Category.AnyTarget);
		}
		
		/* dragonFodder */
		else if (name.equals("dragonFodder"))
			spell.setEffect(name, "Create two 1/1 red Goblin creature tokens.");
		
		/* dreadbore */
		else if (name.equals("dreadbore")) {
			spell.setEffect(Effect.DESTROY_TARGET_CAN_REGEN, "Destroy target creature or planeswalker.");
			source.addTargetRequirement(Category.CreatureOrPlaneswalker);
		}
		
		/* vraskasContempt */
		else if (name.equals("vraskasContempt")) {
			spell.setEffect(name, "Exile target creature or planeswalker. You gain 2 life.");
			source.addTargetRequirement(Category.CreatureOrPlaneswalker);
		}
		
		/*  healingSalve*/
		else if (name.equals("healingSalve")) {
			spell.setEffect(name, "Choose one -" +
							      "• Target player gains 3 life." +
							      "• Prevent the next 3 damage that would be dealt to any target this turn.");
			source.setModal(2);
			source.addTargetRequirementMode(1, Category.Player, Cardinality.ONE);
			source.addTargetRequirementMode(2, Category.AnyTarget, Cardinality.ONE);
		}
		
		/* dromarsCharm */
		else if (name.equals("dromarsCharm")) {
			spell.setEffect(name, "Choose one —§" +
								  "• You gain 5 life.§" +
								  "• Counter target spell.§" +
								  "• Target creature gets -2/-2 until end of turn.");
			source.setModal(3);
			source.addTargetRequirementMode(2, Category.Spell, Cardinality.ONE);
			source.addTargetRequirementMode(3, Category.Creature, Cardinality.ONE);
		}

		/* eladamrisCall */
		else if (name.equals("eladamrisCall"))
			spell.setEffect("faunaShaman", "Search your library for a creature card, reveal that card, and put it into your hand. Then shuffle your library.");
		
		/* entomb */
		else if (name.equals("entomb"))
			spell.setEffect(name, "Search your library for a card and put that card into your graveyard. Then shuffle your library.");
		
		/* erase */
		else if (name.equals("erase")) {
			spell.setEffect(name, "Exile target enchantment.");
			source.addTargetRequirement(Category.Enchantment);
		}

		/* exhume */
		else if (name.equals("exhume"))
			spell.setEffect(name, "Each player puts a creature card from his or her graveyard onto the battlefield.");
		
		/* annul */
		else if (name.equals("annul")) {
			spell.setEffect(Effect.COUNTER_TARGET_STACKOBJECT, "Counter target artifact or enchantment spell.");
			source.addTargetRequirement(Category.ArtifactOrEnchantmentSpell);
		}
		
		/* brand */
		else if (name.equals("brand"))
			spell.setEffect(name, "Gain control of all permanents you own. <i>(This effect lasts indefinitely.)</i>");
		
		/* exclude */
		else if (name.equals("exclude")) {
			spell.setEffect(name, "Counter target creature spell. Draw a card.");
			source.addTargetRequirement(Category.CreatureSpell);
		}
		
		/* urzasRage */
		else if (name.equals("urzasRage")) {
			spell.setEffect(name, "Urza's Rage deals 3 damage to any target. If Urza's Rage was kicked, instead it deals 10 damage to that creature or player and the damage can't be prevented.");
			source.addTargetRequirement(Category.AnyTarget);
		}
		
		/* propheticBolt */
		else if (name.equals("propheticBolt")) {
			spell.setEffect(name, "Prophetic Bolt deals 4 damage to any target. Look at the top four cards of your library. Put one of those cards into your hand and the rest on the bottom of your library in any order.");
			source.addTargetRequirement(Category.AnyTarget);
		}
		
		/* firebolt */
		else if (name.equals("firebolt")) {
			spell.setEffect(Effect.DEAL_2_DAMAGE_TO_TARGET, "Firebolt deals 2 damage to any target.");
			source.addTargetRequirement(Category.AnyTarget);
		}
		
		/* flameSlash */
		else if (name.equals("flameSlash")) {
			spell.setEffect(name, "Flame Slash deals 4 damage to target creature.");
			source.addTargetRequirement(Category.Creature);
		}
		
		/* gamble */
		else if (name.equals("gamble"))
			spell.setEffect(name, "Search your library for a card, put that card into your hand, discard a card at random, then shuffle your library.");
		
		/* galvanicBlast */
		else if (name.equals("galvanicBlast")) {
			spell.setEffect(name, "Galvanic Blast deals 2 damage to any target.§<i>Metalcraft</i> — Galvanic Blast deals 4 damage to that creature or player instead if you control three or more artifacts.");
			source.addTargetRequirement(Category.AnyTarget);
		}

		/* breach */
		else if (name.equals("breach")) {
			spell.setEffect(name, "Target creature gets +2/+0 and gains fear until end of turn. <i>(It can't be blocked except by artifact creatures and/or black creatures.)</i>");
			source.addTargetRequirement(Category.Creature);
		}
		
		/* symbiosis */
		else if (name.equals("symbiosis")) {
			spell.setEffect(name, "Two target creatures each get +2/+2 until end of turn.");
			source.addTargetRequirement(Category.Creature, Cardinality.TWO);
		}
		
		/* rainOfSalt */
		else if (name.equals("rainOfSalt")) {
			spell.setEffect(name, "Destroy two target lands.");
			source.addTargetRequirement(Category.Land, Cardinality.TWO);
		}
		
		/* jaggedLightning */
		else if (name.equals("jaggedLightning")) {
			spell.setEffect(name, "Jagged Lightning deals 3 damage to each of two target creatures.");
			source.addTargetRequirement(Category.Creature, Cardinality.TWO);
		}
		
		/* giantGrowth */
		else if (name.equals("giantGrowth")) {
			spell.setEffect(name, "Target creature gets +3/+3 until end of turn.");
			source.addTargetRequirement(Category.Creature);
		}
		
		/* graspOfDarkness */
		else if (name.equals("graspOfDarkness")) {
			spell.setEffect(name, "Target creature gets -4/-4 until end of turn.");
			source.addTargetRequirement(Category.Creature);
		}
		
		/* greenSunZenith */
		else if (name.equals("greenSunZenith")) {
			spell.setEffect(name, "Search your library for a green creature card with converted mana cost X or less, put it onto the battlefield, then shuffle your library. Shuffle Green Sun's Zenith into its owner's library.");
		}
		
		/* hammerOfBogardan */
		else if (name.equals("hammerOfBogardan")) {
			spell.setEffect(Effect.DEAL_3_DAMAGE_TO_TARGET, "Hammer of Bogardan deals 3 damage to any target.");
			source.addTargetRequirement(Category.AnyTarget);
		}
		
		/* hatred */
		else if (name.equals("hatred")) {
			spell.setEffect(name, "As an additional cost to cast this spell, pay X life. Target creature gets +X/+0 until end of turn.");
			source.addTargetRequirement(Category.Creature);
			source.requiresXValue();
		}
		
		/* impulse */
		else if (name.equals("impulse"))
			spell.setEffect(name, "Look at the top four cards of your library. Put one of them into your hand and the rest on the bottom of your library in any order.");
		
		/* index */
		else if (name.equals("index"))
			spell.setEffect(name, "Look at the top five cards of your library, then put them back in any order.");
		
		/* inspiration */
		else if (name.equals("inspiration")) {
			spell.setEffect(name, "Target player draws two cards.");
			source.addTargetRequirement(Category.Player);
		}
		
		/* languish */
		else if (name.equals("languish"))
			spell.setEffect(name, "All creatures get -4/-4 until end of turn.");
		
		/* redeem */
		else if (name.equals("redeem")) {
			spell.setEffect(name, "Prevent all damage that would be dealt this turn to up to two target creatures.");
			source.addTargetRequirement(Category.Creature, Cardinality.UP_TO_TWO);
		}

		/* befoul */
		else if (name.equals("befoul")) {
			spell.setEffect(Effect.DESTROY_TARGET_CANNOT_REGEN, "Destroy target land or nonblack creature. It can't be regenerated.");
			source.addTargetRequirement(Category.LandOrNonBlackCreature);
		}
		
		/* arcLightning */
		else if (name.equals("arcLightning")) {
			spell.setEffect(name, "Arc Lightning deals 3 damage divided as you choose among one, two, or three target creatures and/or players.<i>(In case of 2 targets, the first target is dealt 1 damage and the second target is dealt 2 damage.)</i>");
			source.addTargetRequirement(Category.AnyTarget, Cardinality.X);
			source.requiresXValue();
		}
		
		/* fatalPush */
		else if (name.equals("fatalPush")) {
			spell.setEffect(name, "Destroy target creature if it has converted mana cost 2 or less.§<i>Revolt</i> — Destroy that creature if it has converted mana cost 4 or less instead if a permanent you controlled left the battlefield this turn.");
			source.addTargetRequirement(Category.Creature);
		}
		
		/* acidicSoil */
		else if (name.equals("acidicSoil"))
			spell.setEffect(name, "Acidic Soil deals damage to each player equal to the number of lands he or she controls.");
		
		/* waylay */
		else if (name.equals("waylay"))
			spell.setEffect(name, "Create three 2/2 white Knight creature tokens. Exile them at the beginning of the next cleanup step.");

		/* spectralProcession */
		else if (name.equals("spectralProcession"))
			spell.setEffect(name, "Create three 1/1 white Spirit creature tokens with flying.");
		
		/* turnabout */
		else if (name.equals("turnabout")) {
			spell.setEffect(name, "Choose artifact, creature, or land. Tap all untapped permanents of the chosen type target player controls, or untap all tapped permanents of that type that player controls.");
			source.addTargetRequirement(Category.Player);
		}
		
		/* tolarianWinds */
		else if (name.equals("tolarianWinds"))
			spell.setEffect(name, "Discard all the cards in your hand, then draw that many cards.");
		
		/* lifeFromTheLoam */
		else if (name.equals("lifeFromTheLoam")) {
			spell.setEffect(name, "Return up to three target land cards from your graveyard to your hand.");
			source.addTargetRequirement(Category.LandCardInYourGraveyard, Cardinality.UP_TO_THREE);
		}
		
		/* ponder */
		else if (name.equals("ponder"))
			spell.setEffect(name, "Look at the top three cards of your library, then put them back in any order. You may shuffle your library.§Draw a card.");
		
		/* lightningBolt */
		else if (name.equals("lightningBolt")) {
			spell.setEffect(Effect.DEAL_3_DAMAGE_TO_TARGET, "Lightning Bolt deals 3 damage to any target.");
			source.addTargetRequirement(Category.AnyTarget);
		}
		
		/* skewerTheCritics */
		else if (name.equals("skewerTheCritics")) {
			spell.setEffect(Effect.DEAL_3_DAMAGE_TO_TARGET, "Skewer the Critics deals 3 damage to any target.");
			source.addTargetRequirement(Category.AnyTarget);
		}
		
		/* riftbolt */
		else if (name.equals("riftbolt")) {
			spell.setEffect(Effect.DEAL_3_DAMAGE_TO_TARGET, "Rift Bolt deals 3 damage to any target.");
			source.addTargetRequirement(Category.AnyTarget);
		}
		
		/* incendiaryFlow */
		else if (name.equals("incendiaryFlow")) {
			spell.setEffect(name, "Incendiary Flow deals 3 damage to any target. If a creature dealt damage this way would die this turn, exile it instead.");
			source.addTargetRequirement(Category.AnyTarget);
		}
		
		/* donate */
		else if (name.equals("donate")) {
			spell.setEffect(name, "Target player gains control of target permanent you control.");
			source.addTargetRequirement(Category.PermanentYouControl);
			source.addTargetRequirement(Category.Player);
		}
		
		/* harnessedLightning */
		else if (name.equals("harnessedLightning")) {
			spell.setEffect(name, "Choose target creature. You get {E}{E}{E} <i>(three energy counters)</i>, then you may pay any amount of {E}. Harnessed Lightning deals that much damage to that creature.");
			source.addTargetRequirement(Category.Creature);
		}
		
		/* lightningHelix */
		else if (name.equals("lightningHelix")) {
			spell.setEffect(name, "Lightning Helix deals 3 damage to any target and you gain 3 life.");
			source.addTargetRequirement(Category.AnyTarget);
		}
		
		/* lightningStrike */
		else if (name.equals("lightningStrike")) {
			spell.setEffect(Effect.DEAL_3_DAMAGE_TO_TARGET, "Lightning Strike deals 3 damage to any target.");
			source.addTargetRequirement(Category.AnyTarget);
		}
		
		/* lingeringSouls */
		else if (name.equals("lingeringSouls"))
			spell.setEffect(name, "Create two 1/1 white Spirit creature tokens with flying.");
		
		/* livingDeath */
		else if (name.equals("livingDeath"))
			spell.setEffect(name, "Each player exiles all creature cards from his or her graveyard, then sacrifices all creatures he or she controls, then puts all cards he or she exiled this way onto the battlefield.");
		
		/* planarBirth */
		else if (name.equals("planarBirth"))
			spell.setEffect(name, "Return all basic land cards from all graveyards to the battlefield tapped under their owners' control.");
		
		/* replenish */
		else if (name.equals("replenish"))
			spell.setEffect(name, "Return all enchantment cards from your graveyard to the battlefield. <i>(Auras with nothing to enchant remain in your graveyard.)</i>");

		/* maelstromPulse */
		else if (name.equals("maelstromPulse")) {
			spell.setEffect(name, "Destroy target nonland permanent and all other permanents with the same name as that permanent.");
			source.addTargetRequirement(Category.NonlandPermanent);
		}
		
		/* magmaJet */
		else if (name.equals("magmaJet")) {
			spell.setEffect(name, "Magma Jet deals 2 damage to any target. Scry 2.");
			source.addTargetRequirement(Category.AnyTarget);
		}
		
		/* rainOfFilth */
		else if (name.equals("rainOfFilth"))
			spell.setEffect(name, "Until end of turn, lands you control gain \"Sacrifice this land: Add {B}.\"");
		
		/* powerSink */
		else if (name.equals("powerSink")) {
			spell.setEffect(name, "Counter target spell unless its controller pays {X}. If that player doesn't, they tap all lands with mana abilities they control and lose all unspent mana.");
			source.addTargetRequirement(Category.Spell);
		}
		
		/* manaLeak */
		else if (name.equals("manaLeak")) {
			spell.setEffect(name, "Counter target spell unless its controller pays {3}.");
			source.addTargetRequirement(Category.Spell);
		}
		
		/* mindPeel */
		else if (name.equals("mindPeel")) {
			spell.setEffect(name, "Target player discards a card.");
			source.addTargetRequirement(Category.Player);
		}
		
		/* forceSpike */
		else if (name.equals("forceSpike")) {
			spell.setEffect(name, "Counter target spell unless its controller pays {1}.");
			source.addTargetRequirement(Category.Spell);
		}
		
		/* meditate */
		else if (name.equals("meditate")) 
			spell.setEffect(name, "Draw four cards. You skip your next turn.");
		
		/* miscalculation */
		else if (name.equals("miscalculation")) {
			spell.setEffect(name, "Counter target spell unless its controller pays {2}.");
			source.addTargetRequirement(Category.Spell);
		}
		
		/* misguidedRage */
		else if (name.equals("misguidedRage")) {
			spell.setEffect(name, "Target player sacrifices a permanent.");
			source.addTargetRequirement(Category.Player);
		}
		
		/* enchantmentAlteration */
		else if (name.equals("enchantmentAlteration")) {
			spell.setEffect(name, "Attach target Aura attached to a creature or land to another permanent of that type.");
			source.addTargetRequirement(Category.AuraAttachedToAcreatureOrLand);
		}
		
		/* nightsWhisper */
		else if (name.equals("nightsWhisper"))
			spell.setEffect(name, "You draw two cards and you lose 2 life.");
		
		/* opportunity */
		else if (name.equals("opportunity")) {
			spell.setEffect(name, "Target player draws four cards.");
			source.addTargetRequirement(Category.Player);
		}
		
		/* orderedMigration */
		else if (name.equals("orderedMigration"))
			spell.setEffect(name, "<i>Domain</i> — Create a 1/1 blue Bird creature token with flying for each basic land type among lands you control.");
		
		/* plagueSpores */
		else if (name.equals("plagueSpores")) {
			spell.setEffect(name, "Destroy target nonblack creature and target land. They can't be regenerated.");
			source.addTargetRequirement(Category.NonBlackCreature);
			source.addTargetRequirement(Category.Land);
		}
		
		/* planarOutburst */
		else if (name.equals("planarOutburst"))
			spell.setEffect(name, "Destroy all nonland creatures.");
		
		/* preordain */
		else if (name.equals("preordain"))
			spell.setEffect(name, "Scry 2, then draw a card.");
		
		/* glimmerOfGenius */
		else if (name.equals("glimmerOfGenius"))
			spell.setEffect(name, "Scry 2, then draw two cards. You get {E}{E} <i>(two energy counters)</i>.");
		
		/* punishingFire */
		else if (name.equals("punishingFire")) {
			spell.setEffect(Effect.DEAL_2_DAMAGE_TO_TARGET, "Punishing Fire deals 2 damage to any target.");
			source.addTargetRequirement(Category.AnyTarget);
		}

		/* shock */
		else if (name.equals("shock")) {
			spell.setEffect(Effect.DEAL_2_DAMAGE_TO_TARGET, "Shock deals 2 damage to any target.");
			source.addTargetRequirement(Category.AnyTarget);
		}
		
		/* exhaustion */
		else if (name.equals("exhaustion")) {
			spell.setEffect(name, "Creatures and lands target opponent controls don't untap during his or her next untap step.");
			source.addTargetRequirement(Category.Opponent);
		}
		
		/* duress */
		else if (name.equals("duress")) {
			spell.setEffect(name, "Target opponent reveals his or her hand. You choose a noncreature, nonland card from it. That player discards that card.");
			source.addTargetRequirement(Category.Opponent);
		}
		
		/* negate */
		else if (name.equals("negate")) {
			spell.setEffect(Effect.COUNTER_TARGET_STACKOBJECT, "Counter target noncreature spell.");
			source.addTargetRequirement(Category.NonCreatureSpell);
		}
		
		/* thoughtseize */
		else if (name.equals("thoughtseize")) {
			spell.setEffect(name, "Target player reveals his or her hand. You choose a nonland card from it. That player discards that card. You lose 2 life.");
			source.addTargetRequirement(Category.Player);
		}
		
		/* Coercion */
		else if (name.equals("coercion")) {
			spell.setEffect(name, "Target opponent reveals his or her hand. You choose a card from it. That player discards that card.");
			source.addTargetRequirement(Category.Opponent);
		}
		
		/* peek */
		else if (name.equals("peek")) {
			spell.setEffect(name, "Look at target player's hand. Draw a card.");
			source.addTargetRequirement(Category.Player);
		}
		
		/* disorder */
		else if (name.equals("disorder"))
			spell.setEffect(name, "Disorder deals 2 damage to each white creature and each player who controls a white creature.");
		
		/* pyroclasm */
		else if (name.equals("pyroclasm"))
			spell.setEffect(name, "Pyroclasm deals 2 damage to each creature.");
		
		/* rallyTheAncestors */
		else if (name.equals("rallyTheAncestors"))
			spell.setEffect(name, "Return each creature card with converted mana cost X or less from your graveyard to the battlefield. Exile those creatures at the beginning of your next upkeep. Exile Rally the Ancestors.");
		
		/* reanimate */
		else if (name.equals("reanimate")) {
			spell.setEffect(name, "Put target creature card from a graveyard onto the battlefield under your control. You lose life equal to its converted mana cost.");
			source.addTargetRequirement(Category.CreatureCardInAnyGraveyard);
		}
		
		/* regrowth */
		else if (name.equals(Effect.REGROWTH)) {
			spell.setEffect(name, "Return target card from your graveyard to your hand.");
			source.addTargetRequirement(Category.CardInYourGraveyard);
		}
		
		/* remand */
		else if (name.equals("remand")) {
			spell.setEffect(name, "Counter target spell. If that spell is countered this way, put it into its owner's hand instead of into that player's graveyard. Draw a card.");
			source.addTargetRequirement(Category.Spell);
		}
		
		/* voidShatter */
		else if (name.equals("voidShatter")) {
			spell.setEffect(name, "Counter target spell. If that spell is countered this way, exile it instead of putting it into its owner's graveyard.");
			source.addTargetRequirement(Category.Spell);
		}
		
		/* repulse */
		else if (name.equals("repulse")) {
			spell.setEffect(name, "Return target creature to its owner's hand. Draw a card.");
			source.addTargetRequirement(Category.Creature);
		}
		
		/* ruinousPath */
		else if (name.equals("ruinousPath")) {
			spell.setEffect(name, "Destroy target creature or planeswalker.");
			source.addTargetRequirement(Category.CreatureOrPlaneswalker);
		}
		
		/* scatterToTheWinds */
		else if (name.equals("scatterToTheWinds")) {
			spell.setEffect(name, "Counter target spell.");
			source.addTargetRequirement(Category.Spell);
		}
		
		/* secureTheWastes */
		else if (name.equals("secureTheWastes"))
			spell.setEffect(name, "Create X 1/1 white Warrior creature tokens.");
		
		/* goblinOffensive */
		else if (name.equals("goblinOffensive"))
			spell.setEffect(name, "Create X 1/1 red Goblin creature tokens.");

		/* scrap */
		else if (name.equals("scrap")) {
			spell.setEffect(Effect.DESTROY_TARGET_CAN_REGEN, "Destroy target artifact.");
			source.addTargetRequirement(Category.Artifact);
		}
		
		/* showerOfSparks */
		else if (name.equals("showerOfSparks")) {
			spell.setEffect(name, "Shower of Sparks deals 1 damage to target creature and 1 damage to target player or planeswalker.");
			source.addTargetRequirement(Category.Creature);
			source.addTargetRequirement(Category.PlayerOrPlaneswalker);
		}
		
		/* shatter */
		else if (name.equals("shatter")) {
			spell.setEffect(Effect.DESTROY_TARGET_CAN_REGEN, "Destroy target artifact.");
			source.addTargetRequirement(Category.Artifact);
		}
		
		/* spellShrivel */
		else if (name.equals("spellShrivel")) {
			spell.setEffect(name, "Counter target spell unless its controller pays {4}. If that spell is countered this way, exile it instead of putting it into its owner's graveyard.");
			source.addTargetRequirement(Category.Spell);
		}
		
		/* sphinxsRevelation */
		else if (name.equals("sphinxsRevelation"))
			spell.setEffect(name, "You gain X life and draw X cards.");
		
		/* staggershock */
		else if (name.equals("staggershock")) {
			spell.setEffect(Effect.DEAL_2_DAMAGE_TO_TARGET, "Staggershock deals 2 damage to any target.");
			source.addTargetRequirement(Category.AnyTarget);
		}
		
		/* disallow */
		else if (name.equals("disallow")) {
			spell.setEffect(Effect.COUNTER_TARGET_STACKOBJECT, "Counter target spell, activated ability, or triggered ability. <i>(Mana abilities can't be targeted.)</i>");
			source.addTargetRequirement(Category.SpellOrActivatedOrTriggeredAbility);
		}
		
		/* stifle */
		else if (name.equals("stifle")) {
			spell.setEffect(Effect.COUNTER_TARGET_STACKOBJECT, "Counter target activated or triggered ability. <i>(Mana abilities can't be targeted.)</i>");
			source.addTargetRequirement(Category.ActivatedOrTriggeredAbility);
		}
		
		/* layWaste */
		else if (name.equals("layWaste")) {
			spell.setEffect(Effect.DESTROY_TARGET_CAN_REGEN, "Destroy target land.");
			source.addTargetRequirement(Category.Land);
		}
		
		/* meltdown */
		else if (name.equals("meltdown"))
			spell.setEffect(name, "Destroy each artifact with converted mana cost X or less.");
		
		/* stoneRain */
		else if (name.equals("stoneRain")) {
			spell.setEffect(Effect.DESTROY_TARGET_CAN_REGEN, "Destroy target land.");
			source.addTargetRequirement(Category.Land);
		}
		
		/* strokeOfGenius */
		else if (name.equals("strokeOfGenius")) {
			spell.setEffect(name, "Target player draws X cards.");
			source.addTargetRequirement(Category.Player);
		}
		
		/* persecute */
		else if (name.equals("persecute")) {
			spell.setEffect(name, "Choose a color. Target player reveals his or her hand and discards all cards of that color.");
			source.addTargetRequirement(Category.Player);
		}
		
		/* pathOfPeace */
		else if (name.equals("pathOfPeace")) {
			spell.setEffect(name, "Destroy target creature. Its owner gains 4 life.");
			source.addTargetRequirement(Category.Creature);
		}
		
		/* swordsToPlowshares */
		else if (name.equals("swordsToPlowshares")) {
			spell.setEffect(name, "Exile target creature. Its controller gains life equal to its power.");
			source.addTargetRequirement(Category.Creature);
		}
		
		/* sylvanScrying */
		else if (name.equals("sylvanScrying"))
			spell.setEffect(name, "Search your library for a land card, reveal it, and put it into your hand. Then shuffle your library.");	
		
		/* gaeasBounty */
		else if (name.equals("gaeasBounty"))
			spell.setEffect(name, "Search your library for up to two Forest cards, reveal those cards, and put them into your hand. Then shuffle your library.");
		
		/* attuneWithAether */
		else if (name.equals("attuneWithAether"))
			spell.setEffect(name, "Search your library for a basic land card, reveal it, put it into your hand, then shuffle your library. You get {E}{E} <i>(two energy counters)</i>.");
		
		/* expunge */
		else if (name.equals("expunge")) {
			spell.setEffect(Effect.DESTROY_TARGET_CANNOT_REGEN, "Destroy target nonartifact, nonblack creature. It can't be regenerated.");
			source.addTargetRequirement(Category.NonArtifactNonBlackCreature);
		}
		
		/* outmaneuver */
		else if (name.equals("outmaneuver")) {
			spell.setEffect(name, "X target blocked creatures assign their combat damage this turn as though they weren't blocked.");
			source.addTargetRequirement(Category.BlockedCreature, Cardinality.X);
		}
		
		/* falter */
		else if (name.equals("falter"))
			spell.setEffect(name, "Creatures without flying can't block this turn.");
		
		/* terminate */
		else if (name.equals("terminate")) {
			spell.setEffect(Effect.DESTROY_TARGET_CANNOT_REGEN, "Destroy target creature. It can't be regenerated.");
			source.addTargetRequirement(Category.Creature);
		}
		
		/* snuffOut */
		else if (name.equals("snuffOut")) {
			spell.setEffect(Effect.DESTROY_TARGET_CANNOT_REGEN, "Destroy target nonblack creature. It can't be regenerated.");
			source.addTargetRequirement(Category.NonBlackCreature);
		}
		
		/* unlicensedDisintegration */
		else if (name.equals("unlicensedDisintegration")) {
			spell.setEffect(name, "Destroy target creature. If you control an artifact, Unlicensed Disintegration deals 3 damage to that creature's controller.");
			source.addTargetRequirement(Category.Creature);
		}
		
		/* thinkTwice */
		else if (name.equals("thinkTwice"))
			spell.setEffect(Effect.DRAW_A_CARD, "Draw a card.");
		
		/* timeTwister */
		else if (name.equals("timeTwister"))
			spell.setEffect(name, "Each player shuffles his or her hand and graveyard into his or her library, then draws seven cards.");
		
		/* tribalFlames */
		else if (name.equals("tribalFlames")) {
			spell.setEffect(name, "<i>Domain</i> — Tribal Flames deals X damage to any target, where X is the number of basic land types among lands you control.");
			source.addTargetRequirement(Category.AnyTarget);
		}
		
		/* twincast */
		else if (name.equals("twincast")) {
			spell.setEffect(name, "Copy target instant or sorcery spell. You may choose new targets for the copy.");
			source.addTargetRequirement(Category.InstantOrSorcery);
		}
		
		/* sunder */
		else if (name.equals("sunder"))
			spell.setEffect(name, "Return all lands to their owners' hands.");
		
		/* upheaval */
		else if (name.equals("upheaval"))
			spell.setEffect(name, "Return all permanents to their owners' hands.");
		
		/* ultimatePrice */
		else if (name.equals("ultimatePrice")) {
			spell.setEffect(Effect.DESTROY_TARGET_CAN_REGEN, "Destroy target monocolored creature.");
			source.addTargetRequirement(Category.MonocoloredCreature);
		}
		
		/* vampiricTutor */
		else if (name.equals("vampiricTutor"))
			spell.setEffect(name, "Search your library for a card, then shuffle your library and put that card on top of it. You lose 2 life.");
		
		/* enlightenedTutor */
		else if (name.equals("enlightenedTutor"))
			spell.setEffect(name, "Search your library for an artifact or enchantment card and reveal that card. Shuffle your library, then put the card on top of it.");

		/* mysticalTutor */
		else if (name.equals("mysticalTutor"))
			spell.setEffect(name, "Search your library for an instant or sorcery card and reveal that card. Shuffle your library, then put the card on top of it.");
		
		/* scourFromExistence */
		else if (name.equals("scourFromExistence")) {
			spell.setEffect(name, "Exile target permanent.");
			source.addTargetRequirement(Category.Permanent);
		}
		
		/* vindicate */
		else if (name.equals("vindicate")) {
			spell.setEffect(Effect.DESTROY_TARGET_CAN_REGEN, "Destroy target permanent.");
			source.addTargetRequirement(Category.Permanent);
		}
		
		/* whispersOfMuse */
		else if (name.equals("whispersOfMuse"))
			spell.setEffect(Effect.DRAW_A_CARD, "Draw a card.");
		
		/* wildfire */
		else if (name.equals("wildfire"))
			spell.setEffect(name, "Each player sacrifices four lands. Wildfire deals 4 damage to each creature.");
		
		/* windfall */
		else if (name.equals("windfall"))
			spell.setEffect(name, "Each player discards his or her hand, then draws cards equal to the greatest number of cards a player discarded this way.");
		
		/* yawgmothsWill */
		else if (name.equals("yawgmothsWill"))
			spell.setEffect(name, "Until end of turn, you may play cards from your graveyard.§If a card would be put into your graveyard from anywhere this turn, exile that card instead.");
		
		/* wrathOfGod */
		else if (name.equals("wrathOfGod"))
			spell.setEffect(name, "Destroy all creatures. They can't be regenerated.");
		
		/* ritualOfSoot */
		else if (name.equals("ritualOfSoot"))
			spell.setEffect(name, "Destroy all creatures with converted mana cost 3 or less.");
		
		/* zap */
		else if (name.equals("zap")) {
			spell.setEffect(name, "Zap deals 1 damage to any target. Draw a card.");
			source.addTargetRequirement(Category.AnyTarget);
		}
		
		/* zombify */
		else if (name.equals("zombify")) {
			spell.setEffect(name, "Return target creature card from your graveyard to the battlefield.");
			source.addTargetRequirement(Category.CreatureCardInYourGraveyard);
		}
		
		/* Should never get here */
		else
		{
			System.err.println("Unknown spell effect : " + name + " from card : " + source.getName());
			System.exit(0);
		}
		
		// set spell additional cost (if any)
		Pattern p = Pattern.compile("^As an additional cost to cast this spell, ([^.]*).*$");
		Matcher m = p.matcher(spell.getDescription());
		if (m.matches()) {
			Requirement ac = AdditionalCost.parse(m.group(1), source.getName());
			if (ac != null)
				source.getCost().addAdditionalCost(ac);
		}
		return spell;
	}
}
