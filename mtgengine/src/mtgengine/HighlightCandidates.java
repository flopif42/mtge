package mtgengine;

import java.util.Vector;

import mtgengine.Game.State;
import mtgengine.Target.Category;
import mtgengine.ability.StaticAbility;
import mtgengine.card.Card;
import mtgengine.card.Color;
import mtgengine.effect.ContinuousEffect;
import mtgengine.player.Player;
import mtgengine.type.CreatureType;
import mtgengine.type.Supertype;
import mtgengine.type.Subtype;
import mtgengine.zone.Battlefield;
import mtgengine.zone.Graveyard;
import mtgengine.zone.Hand;
import mtgengine.zone.Library;
import mtgengine.zone.Stack;

public class HighlightCandidates {

	public static Vector<String> dump(Game g, int idPlayer, State state) {
		Vector<String> ret = new Vector<String>();
		Player p = g.findPlayerByID(idPlayer);
		Library lib = p.getLibrary();
		Hand hand = p.getHand();
		Graveyard graveyard = p.getGraveyard();
		Battlefield battlefield = g.getBattlefield();
		Stack stack = g.getStack();
		Card card;
		
		switch(state)
		{
		/* Force of Will */
		case WaitChoiceForceOfWill:
			for (Card c : p.getHand().getCards()) {
				if (c.hasColor(Color.BLUE) && (c != g.getTopStackObject()))
					ret.add(Integer.toString(c.getID()));
			}
			break;
		
		/* Daze */
		case WaitChoiceDaze:
			for (MtgObject obj : battlefield.getLandsControlledBy(p)) {
				card = (Card) obj; 
				if (card.hasSubtypeGlobal(g, Subtype.ISLAND))
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* Coercicion */
		case WaitChoiceCoercion:
			for (MtgObject obj : p.getOpponent().getHand().getObjects()) {
				card = (Card) obj; 
				ret.add(Integer.toString(card.getID()));
			}
			break;

		/* Reprocess */
		case WaitChoiceReprocess:
			for (MtgObject obj : battlefield.getObjectsControlledBy(idPlayer)) {
				card = (Card) obj;
				if (card.isCreature(g) || card.isArtifact(g) || card.isLand(g))
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		case WaitChoiceShowAndTell:
			for (MtgObject obj : p.getHand().getObjects()) {
				card = (Card) obj;
				if (card.isArtifact(g) || card.isEnchantment(g) || card.isLandCard() || card.isCreatureCard())
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* Duress */
		case WaitChoiceDuress:
			for (MtgObject obj : p.getOpponent().getHand().getObjects()) {
				card = (Card) obj;
				if (!(card.isLandCard() || card.isCreatureCard()))
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* Thoughtseize */
		case WaitChoiceThoughtseize:
			for (MtgObject obj : p.getOpponent().getHand().getObjects()) {
				card = (Card) obj;
				if (!card.isLandCard())
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* Card with untap optional */
		case PromptDoYouWantToUntap:
			card = g._untapOptional_card;
			ret.add(Integer.toString(card.getID()));
			break;
		
		/* Land cards in the library */
		case WaitChoiceSylvanScrying:
			for (MtgObject obj : lib.getObjects()) {
				card = (Card) obj; 
				if (card.isLandCard())
					ret.add(Integer.toString(card.getID()));
			}
			break;
		
		/* Basic Land cards in the library */
		case WaitChoiceBasicLand:
			for (MtgObject obj : lib.getObjects()) {
				card = (Card) obj; 
				if (card.isLandCard() && card.hasSupertype(Supertype.BASIC))
					ret.add(Integer.toString(card.getID()));
			}
			break;
		
		/* Basic Forest cards in the library */
		case WaitChoiceBasicForestCard:
			for (MtgObject obj : lib.getObjects()) {
				card = (Card) obj; 
				if (card.isLandCard() && card.hasSupertype(Supertype.BASIC) && card.hasSubtypeGlobal(g, Subtype.FOREST))
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* Forest cards in the library */
		case WaitChoiceForestCard:
			for (MtgObject obj : lib.getObjects()) {
				card = (Card) obj; 
				if (card.hasSubtypeGlobal(g, Subtype.FOREST))
					ret.add(Integer.toString(card.getID()));
			}
			break;

		/* Ally color fetchlands */
		/* Plains or Island cards in the library */
		case WaitChoicePlainsIsland:
			for (MtgObject obj : lib.getObjects()) {
				card = (Card) obj; 
				if (card.isLandCard() && (card.hasSubtypeGlobal(g, Subtype.PLAINS) || card.hasSubtypeGlobal(g, Subtype.ISLAND)))
					ret.add(Integer.toString(card.getID()));
			}	
			break;

		/* Island or Swamp cards in the library */
		case WaitChoiceIslandSwamp:
			for (MtgObject obj : lib.getObjects()) {
				card = (Card) obj; 
				if (card.isLandCard() && (card.hasSubtypeGlobal(g, Subtype.ISLAND) || card.hasSubtypeGlobal(g, Subtype.SWAMP)))
					ret.add(Integer.toString(card.getID()));
			}	
			break;
		
		/* Swamp or Mountain cards in the library */
		case WaitChoiceSwampMountain:
			for (MtgObject obj : lib.getObjects()) {
				card = (Card) obj; 
				if (card.isLandCard() && (card.hasSubtypeGlobal(g, Subtype.SWAMP) || card.hasSubtypeGlobal(g, Subtype.MOUNTAIN)))
					ret.add(Integer.toString(card.getID()));
			}	
			break;

		/* Mountain or Forest cards in the library */
		case WaitChoiceMountainForest:
			for (MtgObject obj : lib.getObjects()) {
				card = (Card) obj; 
				if (card.isLandCard() && (card.hasSubtypeGlobal(g, Subtype.MOUNTAIN) || card.hasSubtypeGlobal(g, Subtype.FOREST)))
					ret.add(Integer.toString(card.getID()));
			}	
			break;

		/* Forest or Plains cards in the library */
		case WaitChoiceForestPlains:
			for (MtgObject obj : lib.getObjects()) {
				card = (Card) obj; 
				if (card.isLandCard() && (card.hasSubtypeGlobal(g, Subtype.FOREST) || card.hasSubtypeGlobal(g, Subtype.PLAINS)))
					ret.add(Integer.toString(card.getID()));
			}
			break;
		
		/* Ennemy color fetchlands */
		/* Plains or Swamp cards in the library */
		case WaitChoicePlainsSwamp:
			for (MtgObject obj : lib.getObjects()) {
				card = (Card) obj; 
				if (card.isLandCard() && (card.hasSubtypeGlobal(g, Subtype.PLAINS) || card.hasSubtypeGlobal(g, Subtype.SWAMP)))
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* Swamp or Forest cards in the library */
		case WaitChoiceSwampForest:
			for (MtgObject obj : lib.getObjects()) {
				card = (Card) obj; 
				if (card.isLandCard() && (card.hasSubtypeGlobal(g, Subtype.SWAMP) || card.hasSubtypeGlobal(g, Subtype.FOREST)))
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* Forest or Island cards in the library */
		case WaitChoiceForestIsland:
			for (MtgObject obj : lib.getObjects()) {
				card = (Card) obj; 
				if (card.isLandCard() && (card.hasSubtypeGlobal(g, Subtype.FOREST) || card.hasSubtypeGlobal(g, Subtype.ISLAND)))
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* Island or Mountain cards in the library */
		case WaitChoiceIslandMountain:
			for (MtgObject obj : lib.getObjects()) {
				card = (Card) obj; 
				if (card.isLandCard() && (card.hasSubtypeGlobal(g, Subtype.ISLAND) || card.hasSubtypeGlobal(g, Subtype.MOUNTAIN)))
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* Mountain or Plains cards in the library */
		case WaitChoiceMountainPlains:
			for (MtgObject obj : lib.getObjects()) {
				card = (Card) obj; 
				if (card.isLandCard() && (card.hasSubtypeGlobal(g, Subtype.MOUNTAIN) || card.hasSubtypeGlobal(g, Subtype.PLAINS)))
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* An Equipment card in the library */
		case WaitChoiceStoneforgeMystic_search:
			for (MtgObject obj : lib.getObjects()) {
				card = (Card) obj; 
				if (card.isArtifactCard() && card.hasSubtypeGlobal(g, Subtype.EQUIPMENT))
					ret.add(Integer.toString(card.getID()));
			}
			break;

		/* An Equipment card in hand */
		case WaitChoiceStoneforgeMystic_put:
			for (MtgObject obj : hand.getObjects()) {
				card = (Card) obj; 
				if (card.isArtifactCard() && card.hasSubtypeGlobal(g, Subtype.EQUIPMENT))
					ret.add(Integer.toString(card.getID()));
			}
			break;			
			
		/* A creature card in hand */
		case WaitChoiceSneakAttack:
			for (MtgObject obj : hand.getObjects()) {
				card = (Card) obj; 
				if (card.isCreatureCard())
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* A Goblin permanent card in hand */
		case WaitChoiceGoblinLackey:
			for (MtgObject obj : hand.getObjects()) {
				card = (Card) obj; 
				if (card.isPermanentCard() && card.hasCreatureType(g, CreatureType.Goblin))
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* An Artifact card in hand */
		case WaitChoiceCopperGnomes:
			for (MtgObject obj : hand.getObjects()) {
				card = (Card) obj; 
				if (card.isArtifactCard())
					ret.add(Integer.toString(card.getID()));
			}
			break;

		/* An Enchant creature card in hand */
		case WaitChoiceAcademyResearchers:
			for (MtgObject obj : hand.getObjects()) {
				card = (Card) obj; 
				if (card.isEnchantmentCard() && card.hasSubtypeGlobal(g, Subtype.AURA) && card.hasStaticAbility("enchantCreature"))
					ret.add(Integer.toString(card.getID()));
			}
			break;			

			
		/* A Creature card in hand with converted mana cost = X */
		case WaitChoiceAetherVial_put:
			int X = g.getTopStackObject().getSource().getNbCountersOfType(g, CounterType.CHARGE);
			for (MtgObject obj : hand.getObjects()) {
				card = (Card) obj; 
				if (card.isCreatureCard() && (card.getConvertedManaCost(g) == X))
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* A basic land for RevealLand cards */
		case PromptRevealPlainsOrIsland:
		case PromptRevealIslandOrSwamp:
		case PromptRevealSwampOrMountain:
		case PromptRevealMountainOrForest:
		case PromptRevealForestOrPlains:
			StaticAbility reveal = g.getRevealLand().getStaticAbility("revealland");
			Subtype basic1 = reveal.getAssociatedBasicLandType(0);
			Subtype basic2 = reveal.getAssociatedBasicLandType(1);
			for (MtgObject obj : hand.getObjects()) {
				card = (Card) obj;
				if (card.hasSubtypeGlobal(g, basic1) || card.hasSubtypeGlobal(g, basic2))
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		case WaitChooseReplaceDraw:
			for (MtgObject obj : graveyard.getObjects()) {
				card = (Card) obj; 
				if (card.hasStaticAbility("dredge"))
					ret.add(Integer.toString(card.getID()));
			}
			for (ContinuousEffect ce : g._replaceDrawEffects)
				ret.add(Integer.toString(((Card) ce.getSource()).getID()));

			break;
			
		/* A card with Dredge in the graveyard */
		case PromptDredge:
			for (MtgObject obj : graveyard.getObjects()) {
				card = (Card) obj; 
				if (card.hasStaticAbility("dredge"))
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* A land card in hand */
		case WaitChoiceMoxDiamond:
			for (MtgObject obj : hand.getObjects()) {
				card = (Card) obj; 
				if (card.isLandCard())
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* A Creature card in hand */
		case WaitPayCostDiscardACreatureCard:
			for (MtgObject obj : hand.getObjects()) {
				card = (Card) obj; 
				if (card.isCreatureCard())
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* A card in hand */
		case WaitPayCostDiscardACard:
			for (MtgObject obj : hand.getObjects())
				ret.add(Integer.toString(((Card) obj).getID()));
			break;
			
		/* Creature cards with converted mana cost X or less in the library */
		case WaitChoiceCitanulFlute:
		case WaitChoiceChordOfCalling:
			for (MtgObject obj : lib.getObjects()) {
				card = (Card) obj;
				int xValue = g.getTopStackObject().getXValue();
				if (card.isCreatureCard() && (card.getConvertedManaCost(g) <= xValue))
					ret.add(Integer.toString(card.getID()));
			}
			break;
		
		/* Green creature cards with converted mana cost X or less in the library */
		case WaitChoiceGreenSunZenith:
			for (MtgObject obj : lib.getObjects()) {
				card = (Card) obj;
				int xValue = g.getTopStackObject().getXValue();
				if (card.isCreatureCard() && card.hasColor(Color.GREEN) && (card.getConvertedManaCost(g) <= xValue))
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* Creature cards in the library */
		case WaitChoiceCreatureCard:
			for (MtgObject obj : lib.getObjects()) {
				card = (Card) obj; 
				if (card.isCreatureCard())
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* Cards with the same name than the creature who died */
		case WaitChoiceRemembrance:
			String dyingCreatureName = ((Card) g.getTopStackObject().getAdditionalData()).getName();
			for (Card c : lib.getCards()) {
				if (c.getName().equals(dyingCreatureName))
					ret.add(Integer.toString(c.getID()));
			}
			break;
			
		/* Creature cards in the graveyard */
		case WaitChoiceExhume:
			for (MtgObject obj : graveyard.getObjects()) {
				card = (Card) obj; 
				if (card.isCreatureCard())
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* Artifact or Creature cards in the library */
		case WaitChoiceArtifactOrCreatureCard:
			for (MtgObject obj : lib.getObjects()) {
				card = (Card) obj; 
				if (card.isCreatureCard() || card.isArtifactCard())
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* Land or Creature cards in the library */
		case WaitChoiceLandOrCreatureCard:
			for (MtgObject obj : lib.getObjects()) {
				card = (Card) obj; 
				if (card.isCreatureCard() || card.isLandCard())
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* Goblin cards in the library */
		case WaitChoiceGoblinCard:
			for (MtgObject obj : lib.getObjects()) {
				card = (Card) obj; 
				if (card.hasCreatureType(g, CreatureType.Goblin))
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* Recruiter of the Guard */
		case WaitChoiceCreatureCardWithToughness2orLess:
			for (MtgObject obj : lib.getObjects()) {
				card = (Card) obj; 
				if (card.isCreatureCard() && (card.getToughness(g) <= 2))
					ret.add(Integer.toString(card.getID()));
			}
			break;

		/* Artifact or Enchantment cards in the library */
		case WaitChoiceArtifactOrEnchantmentCard:
			for (MtgObject obj : lib.getObjects()) {
				card = (Card) obj; 
				if (card.isEnchantmentCard() || card.isArtifactCard())
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* Enchantment cards in the library */
		case WaitChoiceEnchantment:
			for (MtgObject obj : lib.getObjects()) {
				card = (Card) obj; 
				if (card.isEnchantmentCard())
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* Enchantment cards with CMC <= 3 in the library (Zur) */
		case WaitChoiceEnchantmentCardWithCCM3orLess:
			for (MtgObject obj : lib.getObjects()) {
				card = (Card) obj; 
				if (card.isEnchantmentCard() && (card.getConvertedManaCost(g) <= 3))
					ret.add(Integer.toString(card.getID()));
			}
			break;

		/* Instant or Sorcery cards in the library */
		case WaitChoiceInstantOrSorceryCard:
			for (MtgObject obj : lib.getObjects()) {
				card = (Card) obj; 
				if (card.isInstantCard() || card.isSorceryCard())
					ret.add(Integer.toString(card.getID()));
			}
			break;

			
		/* Collected Company : creature cards with converted mana cost 3 or less among the top 6 cards of the library */
		case WaitChoiceCollectedCompany:
			for (Card c : lib.getXTopCards(6)) {
				if (c.isCreatureCard() && (c.getConvertedManaCost(g) <= 3))
					ret.add(Integer.toString(c.getID()));
			}	
			break;

		/* Duskwatch Recruiter : creature cards among the top 3 cards of the library */
		case WaitChoicePutCreatureCardInHand:
			for (Card c : lib.getXTopCards(3)) {
				if (c.isCreatureCard())
					ret.add(Integer.toString(c.getID()));
			}
			break;
			
		case WaitchoiceCardInGraveyard:
			for (MtgObject obj : p.getGraveyard().getObjects()) {
				card = (Card) obj;
				ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* Oath of Nissa : creature, land or planeswalker cards among the top 3 cards of the library*/
		case WaitChoiceOathOfNissa:
			for (Card c : lib.getXTopCards(3)) {
				if (c.isCreatureCard() || c.isLandCard() || c.isPlaneswalkerCard())
					ret.add(Integer.toString(c.getID()));
			}
			break;
		
		/* Scrapheap Scrounger : other creature cards in the graveyard */
		case WaitPayCostExileAnotherCreatureCardFromGyd:
			for (MtgObject obj : p.getGraveyard().getObjects()) {
				card = (Card) obj;
				if (card.isCreatureCard() && (card != g.getTopStackObject().getSource()))
					ret.add(Integer.toString(card.getID()));
			}
			break;
		
		/* Ichorid effect : other black creature cards in the graveyard */
		case WaitExileForIchorid:
			for (MtgObject obj : p.getGraveyard().getObjects()) {
				card = (Card) obj;
				if (card.isCreatureCard() && card.hasColor(Color.BLACK) && (card != g.getTopStackObject().getSource()))
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* Untapped creatures controlled by that player */
		case WaitPayCostCrew:
		case WaitPayCostTapUntappedCreature:
			for (MtgObject obj : battlefield.getObjectsControlledBy(idPlayer)) {
				card = (Card) obj;
				if (card.isCreature(g) && !card.isTapped())
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* Creatures controlled by that player */
		case WaitPayCostSacrificeCreature:
		case WaitSacrificeCreature:
		case WaitChoiceCreature:
			for (MtgObject obj : battlefield.getObjectsControlledBy(idPlayer)) {
				card = (Card) obj;
				if (card.isCreature(g))
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* Creatures and lands controlled by that player */
		case WaitSacrificeCreatureOrLand:
			for (MtgObject obj : battlefield.getObjectsControlledBy(idPlayer)) {
				card = (Card) obj;
				if (card.isCreature(g) || card.isLand(g))
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* Creatures and planeswalkers controlled by that player */
		case WaitSacrificeCreatureOrPlaneswalker:
			for (MtgObject obj : battlefield.getObjectsControlledBy(idPlayer)) {
				card = (Card) obj;
				if (card.isCreature(g) || card.isPlaneswalkerCard())
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* Creatures tied for least toughness */
		case WaitChoicePurgingScythe:
			for (MtgObject obj : battlefield.getCreaturesWithLeastToughness()) {
				card = (Card) obj;
				ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* Enchantment Alteration */
		case WaitChoiceEnchantmentAlteration:
			Card spell = (Card) g.getTopStackObject();
			Card targetAura = (Card) spell.getTargetObject(0);
			Category enchantWhat = targetAura.getTargetRequirements().get(0).getCategory();
			
			switch (enchantWhat) {
			case Creature:
				for (MtgObject obj : battlefield.getPermanents()) {
					card = (Card) obj;
					if (card.isCreature(g) && (card != targetAura.getTargetObject(0)))
						ret.add(Integer.toString(card.getID()));
				}
				break;
				
			case Land:
				for (MtgObject obj : battlefield.getPermanents()) {
					card = (Card) obj;
					if (card.isLand(g) && (card != targetAura.getTargetObject(0)))
						ret.add(Integer.toString(card.getID()));
				}
				break;
				
			case Permanent:
				for (MtgObject obj : battlefield.getPermanents()) {
					card = (Card) obj;
					if (card != targetAura.getTargetObject(0))
						ret.add(Integer.toString(card.getID()));
				}
				break;
			
			default:
				// should never get here
				break;
			}
			
			break;

		/* Enchantments controlled by that player */
		case WaitSacrificeEnchantment:
		case WaitPayCostSacrificeEnchantment:
			for (MtgObject obj : battlefield.getObjectsControlledBy(idPlayer)) {
				card = (Card) obj;
				if (card.isEnchantment(g))
					ret.add(Integer.toString(card.getID()));
			}
			break;

		/* Vampires and Zombies controlled by that player */
		case WaitPayCostSacrificeAnotherVampireOrZombie:
			for (MtgObject obj : battlefield.getObjectsControlledBy(idPlayer)) {
				card = (Card) obj;
				if ((card.hasCreatureType(g, CreatureType.Vampire) || card.hasCreatureType(g, CreatureType.Zombie)) &&
						(card != g.getTopStackObject().getSource()))
					ret.add(Integer.toString(card.getID()));
			}
			break;

		/* Goblins controlled by that player */
		case WaitPayCostSacrificeGoblin:
			for (MtgObject obj : battlefield.getObjectsControlledBy(idPlayer)) {
				card = (Card) obj;
				if (card.hasCreatureType(g, CreatureType.Goblin))
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* Planeswalkers controlled by that player */
		case WaitHeartOfKiran:
			for (MtgObject obj : battlefield.getObjectsControlledBy(idPlayer)) {
				card = (Card) obj;
				if (card.isPlaneswalkerCard())
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* Elf creatures controlled by that player */
		case WaitPayCostReturnElf:
			for (MtgObject obj : battlefield.getObjectsControlledBy(idPlayer)) {
				card = (Card) obj;
				if (card.hasCreatureType(g, CreatureType.Elf))
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* Artifacts controlled by that player */
		case WaitPayCostSacrificeArtifact:
			for (MtgObject obj : battlefield.getObjectsControlledBy(idPlayer)) {
				card = (Card) obj;
				if (card.isArtifact(g))
					ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* Permanents controlled by that player */
		case WaitPayCostSacrificePermanent:
		case WaitSacrificePermanent:
		case WaitReturnPermanent:
			for (MtgObject obj : battlefield.getObjectsControlledBy(idPlayer)) {
				card = (Card) obj;
				ret.add(Integer.toString(card.getID()));
			}
			break;
		
		/* Lands controlled by that player */
		case WaitPayCostReturnLand:
		case WaitSacrificeLand:
		case WaitPayCostSacrificeLand:
			for (MtgObject obj : battlefield.getObjectsControlledBy(idPlayer)) {
				card = (Card) obj;
				if (card.isLand(g))
					ret.add(Integer.toString(card.getID()));
			}
			break;

		/* Lands controlled by either player */
		case WaitUntapLand:
			for (MtgObject obj : battlefield.getLands()) {
				card = (Card) obj;
				ret.add(Integer.toString(card.getID()));
			}
			break;
			
		/* Forest and Plains controlled by that player */
		case WaitPayCostSacrificeForestOrPlains:
			for (MtgObject obj : battlefield.getObjectsControlledBy(idPlayer)) {
				card = (Card) obj;
				if (card.isLand(g) && (card.hasSubtypeGlobal(g, Subtype.FOREST) || card.hasSubtypeGlobal(g, Subtype.PLAINS)))
					ret.add(Integer.toString(card.getID()));
			}
			break;

		/* Forest controlled by that player */
		case WaitPayCostSacrificeForest:
			for (MtgObject obj : battlefield.getObjectsControlledBy(idPlayer)) {
				card = (Card) obj;
				if (card.isLand(g) && (card.hasSubtypeGlobal(g, Subtype.FOREST)))
					ret.add(Integer.toString(card.getID()));
			}
			break;

			
		/* A damage source */
		case WaitChoiceDamageSource:
			// Permanents
			for (MtgObject obj : battlefield.getPermanents())
				ret.add(Integer.toString(obj.getID()));
			
			// Spells on the stack
			for (MtgObject obj : stack.getObjects())
				ret.add(Integer.toString(obj.getID()));
			
			// Objects in any command zone
			for (Player pl : g.getPlayers()) {
				for (MtgObject obj : pl.getCommand().getObjects()) {
					ret.add(Integer.toString(obj.getID()));
				}
			}
			
			break;
			
		/* An artifact damage source */
		case WaitChoiceDamageArtifactSource:
			// Permanents
			for (Card c : battlefield.getPermanents()) {
				if (c.isArtifact(g))
					ret.add(Integer.toString(c.getID()));
			}
			
			// Spells on the stack
			for (MtgObject obj : stack.getObjects()) {
				if (obj instanceof Card && ((Card)obj).isArtifactCard())
					ret.add(Integer.toString(obj.getID()));
			}
			
			break;

		/* A colored damage source */
		case WaitChoiceDamageWhiteSource:
		case WaitChoiceDamageBlueSource:
		case WaitChoiceDamageBlackSource:
		case WaitChoiceDamageRedSource:
		case WaitChoiceDamageGreenSource:
		{
			Color col;
			
			switch (state) {
			case WaitChoiceDamageWhiteSource:
				col = Color.WHITE;
				break;
				
			case WaitChoiceDamageBlueSource:
				col = Color.BLUE;
				break;
				
			case WaitChoiceDamageBlackSource:
				col = Color.BLACK;
				break;
				
			case WaitChoiceDamageRedSource:
				col = Color.RED;
				break;
				
			case WaitChoiceDamageGreenSource:
				col = Color.GREEN;
				break;

			default:
				return ret;
			}
			
			// Permanents
			for (Card c : battlefield.getPermanents()) {
				if (c.hasColor(col))
					ret.add(Integer.toString(c.getID()));
			}
			
			// Spells and abilities on the stack
			for (MtgObject obj : stack.getObjects()) {
				if (obj instanceof Card && ((Card)obj).hasColor(col))
					ret.add(Integer.toString(obj.getID()));
			}
			
			break;
		}
			
				
		/* A land damage source */
		case WaitChoiceDamageLandSource:
			// Permanents
			for (Card c : battlefield.getPermanents()) {
				if (c.isLand(g))
					ret.add(Integer.toString(c.getID()));
			}
			
			break;
			
		default:
			break;
		}
		return ret;
	}
}


