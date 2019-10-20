package mtgengine.ability;

import java.util.Vector;

import mtgengine.CounterType;
import mtgengine.Game;
import mtgengine.MtgObject;
import mtgengine.card.Card;
import mtgengine.card.Color;
import mtgengine.player.Player;
import mtgengine.type.CreatureType;
import mtgengine.type.Supertype;
import mtgengine.type.Subtype;
import mtgengine.zone.Battlefield;
import mtgengine.zone.Graveyard;
import mtgengine.zone.Hand;
import mtgengine.zone.Library;

public class Choice {
	private Player _player;
	private Vector<Integer> _choices = null;
	private Vector<MtgObject> _cardChoices = null;
	
	public Choice(Player player, Vector<Integer> choices) {
		_player = player;
		_choices = choices;
		_cardChoices = new Vector<MtgObject>();
	}

	public void clear() {
		_choices = null;
		_cardChoices.clear();
	}

	public Vector<MtgObject> get() {
		return _cardChoices;
	}
	
	public boolean validate(Game g) {
		MtgObject object = null;
		Card card;
		int choiceId;
		MtgObject chosenSource;
		Battlefield battlefield = g.getBattlefield();
		Library lib = _player.getLibrary();
		Hand hand = _player.getHand();
		Graveyard graveyard = _player.getGraveyard();
		Game.State state = _player.getState();
		
		switch (state)
		{
		/* Ichorid (exile another black creature card from graveyard) */
		case WaitExileForIchorid :
			// Waiting for exaclty one entry (a card or DONE when no card in hand)
			if (_choices.size() != 1)
				return false;
			
			// the player clicked "DONE" : the choice is valid but do not reanimate Ichorid
			if (_choices.get(0) == -1)
				return true;
			
			// Make sure returned object is not null
			object = graveyard.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// Make sure it's a creature card, it's black, and in its owner's graveyard
			if (!card.isCreatureCard() || !card.hasColor(Color.BLACK) || !card.isIGY(g))
				return false;
			break;
		
		/* Scrapheap Scrounger */
		case WaitPayCostExileAnotherCreatureCardFromGyd:
			// Waiting for exaclty one entry
			if (_choices.size() != 1)
				return false;
			
			// Make sure returned object is not null
			object = graveyard.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// Make sure it's a creature card, and in its owner's graveyard, and it's not the scrounger
			if (!card.isCreatureCard() || !card.isIGY(g) || (card == g.getTopStackObject().getSource()))
				return false;
			break;
			
		case PromptDoYouWantToDrawACard:
		case PromptDoYouWantPutTheCardInYourHand:
		case PromptDoYouWantPutInGraveyard:
			// player must answer either yes or no
			if (_choices.size() != 1)
				return false;
			break;
			
		case WaitChoiceLookTop:
		case LookPlayersHand:
			// no cards can be chosen, just waiting for the player to click 'Done'
			if (_choices.size() > 0)
				return false;
			break;
			
		case WaitChoiceScryPutBottom:
			// player has the choice to not pick a card
			if (_choices.size() == 0)
				return true;
			
			// Make sure returned object is not null
			object = (Card) lib.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			break;
			
		case WaitChoiceCollectedCompany:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;
			
			// player has the choice to not pick a card
			if (_choices.size() == 0)
				return true;

			// Make sure returned object is not null
			object = lib.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;

			// Check card type and CMC
			if (!card.isCreatureCard() || (card.getConvertedManaCost(g) > 3))
				return false;
			break;
			
		case WaitchoiceCardInGraveyard:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;

			// player has the choice to not pick a card
			if (_choices.size() == 0)
				return true;

			// Make sure returned object is not null
			object = (Card) graveyard.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			break;
			
		case WaitChoicePutCreatureCardInHand:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;

			// player has the choice to not pick a card
			if (_choices.size() == 0)
				return true;

			// Make sure returned object is not null
			object = (Card) lib.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// Check card type
			if (!card.isCreatureCard())
				return false;
			break;
			
		case WaitChoiceOathOfNissa:  //creature, land or planeswalker card
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;

			// player has the choice to not pick a card
			if (_choices.size() == 0)
				return true;

			// Make sure returned object is not null
			object = (Card) lib.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// Check card type
			if (!card.isCreatureCard() && !card.isLandCard() && !card.isPlaneswalkerCard())
				return false;

			break;
			
		case WaitChoicePutInHand:
		case WaitChoicePutBottomLib:
		case WaitChoicePutTopLib:
			// Waiting for exaclty one entry
			if (_choices.size() != 1)
				return false;
			
			// Make sure returned object is not null
			object = (Card) lib.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			break;
			
		/* Tap un untapped creature you control */
		case WaitPayCostCrew:
		case WaitPayCostTapUntappedCreature:
			// Make sure exactly 1 entry
			if (_choices.size() != 1)
				return false;
			// 1. Make sure returned object is not null
			object = (Card) battlefield.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// 2. Check card type
			if (!card.isCreature(g))
				return false;
			// 3. Check creature controller
			if (card.getController(g) != _player)
				return false;
			// 4. Check creature is untapped
			if (card.isTapped())
				return false;
			break;
		
		/* Enchantment Alteration */
		case WaitChoiceEnchantmentAlteration:
			// Make sure exactly 1 entry
			if (_choices.size() != 1)
				return false;
			// 1. Make sure returned object is not null
			object = (Card) battlefield.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// 2. Check card type
			if (!(card.isCreature(g) || card.isLand(g)))
				return false;
			// 3. Check that new host is different than current host
			if (card == ((Card) g.getTopStackObject().getTargetObject(0)).getHost())
				return false;
			break;
		
		case WaitChoicePurgingScythe:
			// Make sure exactly 1 entry
			if (_choices.size() != 1)
				return false;
			// 1. Make sure returned object is not null
			object = battlefield.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// 2. Check card type
			if (!card.isCreature(g))
				return false;
			// 3. Check creature has least toughness
			if (!battlefield.getCreaturesWithLeastToughness().contains(card))
				return false;
			break;
			
		/* A creature on the battlefield controlled by the player */
		case WaitPayCostSacrificeCreature:
		case WaitSacrificeCreature:
		case WaitChoiceCreature:
			// Make sure exactly 1 entry
			if (_choices.size() != 1)
				return false;
			// 1. Make sure returned object is not null
			object = (Card) battlefield.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// 2. Check card type
			if (!card.isCreature(g))
				return false;
			// 3. Check creature controller
			if (card.getController(g) != _player)
				return false;
			break;
			
		case WaitSacrificeCreatureOrLand:
			// Make sure exactly 1 entry
			if (_choices.size() != 1)
				return false;
			// 1. Make sure returned object is not null
			object = (Card) battlefield.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// 2. Check card type
			if (!(card.isCreature(g) || card.isLand(g)))
				return false;
			// 3. Check creature controller
			if (card.getController(g) != _player)
				return false;
			break;
			
		case WaitSacrificeCreatureOrPlaneswalker:
			// Make sure exactly 1 entry
			if (_choices.size() != 1)
				return false;
			// 1. Make sure returned object is not null
			object = (Card) battlefield.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// 2. Check card type
			if (!(card.isCreature(g) || card.isPlaneswalkerCard()))
				return false;
			// 3. Check creature/planeswalker controller
			if (card.getController(g) != _player)
				return false;
			break;
			
		/* Sacrifice an enchantment */
		case WaitPayCostSacrificeEnchantment:
		case WaitSacrificeEnchantment:
			// Make sure exactly 1 entry
			if (_choices.size() != 1)
				return false;
			// 1. Make sure returned object is not null
			object = (Card) battlefield.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// 2. Check card type
			if (!card.isEnchantment(g))
				return false;
			// 3. Check creature controller
			if (card.getController(g) != _player)
				return false;
			break;

		/* Sacrifice another Vampire or Zombie (Kalitas) */
		case WaitPayCostSacrificeAnotherVampireOrZombie:
			// Make sure exactly 1 entry
			if (_choices.size() != 1)
				return false;
			// 1. Make sure returned object is not null
			object = (Card) battlefield.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// 2. Check creature controller
			if (card.getController(g) != _player)
				return false;
			// 3. Check card subtype
			if (!(card.hasCreatureType(g, CreatureType.Vampire) || card.hasCreatureType(g, CreatureType.Zombie)))
				return false;
			// 4. Check the 'another' clause
			if (object == g.getTopStackObject().getSource())
				return false;
			break;

		/* Sacrifice a Goblin */
		case WaitPayCostSacrificeGoblin:
			// Make sure exactly 1 entry
			if (_choices.size() != 1)
				return false;
			// 1. Make sure returned object is not null
			object = (Card) battlefield.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// 2. Check creature controller
			if (card.getController(g) != _player)
				return false;
			// 3. Check card subtype
			if (!card.hasCreatureType(g, CreatureType.Goblin))
				return false;
			break;

		/* Return an Elf */
		case WaitPayCostReturnElf:
			// Make sure exactly 1 entry
			if (_choices.size() != 1)
				return false;
			// 1. Make sure returned object is not null
			object = (Card) battlefield.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// 2. Check creature controller
			if (card.getController(g) != _player)
				return false;
			// 3. Check card subtype
			if (!card.hasCreatureType(g, CreatureType.Elf))
				return false;
			break;
			
		/* Heart of Kiran */
		case WaitHeartOfKiran:
			// Make sure exactly 1 entry
			if (_choices.size() != 1)
				return false;
			// 1. Make sure returned object is not null
			object = (Card) battlefield.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// 2. Check creature controller
			if (card.getController(g) != _player)
				return false;
			// 3. Check card subtype
			if (!card.isPlaneswalkerCard())
				return false;
			break;
			
		/* Sacrifice an artifact */
		case WaitPayCostSacrificeArtifact:
			// Make sure exactly 1 entry
			if (_choices.size() != 1)
				return false;
			// 1. Make sure returned object is not null
			object = (Card) battlefield.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// 2. Check card type
			if (!card.isArtifact(g))
				return false;
			// 3. Check artifact controller
			if (card.getController(g) != _player)
				return false;

			break;
			
		/* Reprocess (sacrifice an artifact, creature or land) */
		case WaitChoiceReprocess:
			// Make sure no more than one entry
			if (_choices.size() > 1)
				return false;
			
			// player has the choice to not pick a card
			if (_choices.size() == 0)
				return true;
			
			// 1. Make sure returned object is not null
			object = (Card) battlefield.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// 2. Check controller
			if (card.getController(g) != _player)
				return false;
			// 3. Check card type
			if (!(card.isArtifact(g) || card.isCreature(g) || card.isLand(g)))
				return false;
			break;

		/* Sacrifice a permanent */
		case WaitPayCostSacrificePermanent:
		case WaitSacrificePermanent:
		case WaitReturnPermanent:
			// Make sure exactly 1 entry
			if (_choices.size() != 1)
				return false;
			// 1. Make sure returned object is not null
			object = (Card) battlefield.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// 2. Check controller
			if (card.getController(g) != _player)
				return false;
			break;
			
		/* Sacrifice/Return a land */
		case WaitSacrificeLand:
		case WaitPayCostSacrificeLand:
		case WaitPayCostReturnLand:
			// Make sure exactly 1 entry
			if (_choices.size() != 1)
				return false;
			// 1. Make sure returned object is not null
			object = (Card) battlefield.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// 2. Check card type
			if (!card.isLand(g))
				return false;
			// 3. Check land controller
			if (card.getController(g) != _player)
				return false;
			break;

		/* Untap a land */
		case WaitUntapLand:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;
			
			// player has the choice to not pick a card
			if (_choices.size() == 0)
				return true;
			
			// 1. Make sure returned object is not null
			object = (Card) battlefield.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// 2. Check card type
			if (!card.isLand(g))
				return false;
			break;
			
		/* Sacrifice a Forest or Plains */
		case WaitPayCostSacrificeForestOrPlains:
			// Make sure exactly 1 entry
			if (_choices.size() != 1)
				return false;
			// 1. Make sure returned object is not null
			object = (Card) battlefield.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// 2. Check card type and subtype
			if (! (card.isLand(g) && (card.hasSubtypeGlobal(g, Subtype.FOREST) || card.hasSubtypeGlobal(g, Subtype.PLAINS))))
				return false;
			// 3. Check land controller
			if (card.getController(g) != _player)
				return false;

			break;

		/* Sacrifice a Forest */
		case WaitPayCostSacrificeForest:
			// Make sure exactly 1 entry
			if (_choices.size() != 1)
				return false;
			// 1. Make sure returned object is not null
			object = (Card) battlefield.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// 2. Check card type and subtype
			if (! (card.isLand(g) && (card.hasSubtypeGlobal(g, Subtype.FOREST))))
				return false;
			// 3. Check land controller
			if (card.getController(g) != _player)
				return false;

			break;

		case WaitChoiceDuress:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;

			// player has the choice to not pick a card if there are no valid choices
			if (_choices.size() == 0) {
				Vector<Card> opponentHand = _player.getOpponent().getHand().getCards();
				for (Card c : opponentHand) {
					if (! (c.isCreatureCard() || c.isLandCard()))
						return false;
				}
				return true;
			}
			
			// Make sure returned object is not null
			object = _player.getOpponent().getHand().getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			
			card = (Card) object;
			// Check card type
			if (card.isCreatureCard() || card.isLandCard())
				return false;
			
			break;
			
		case WaitChoiceThoughtseize:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;

			// player has the choice to not pick a card if there are no valid choices
			if (_choices.size() == 0) {
				Vector<Card> opponentHand = _player.getOpponent().getHand().getCards();
				for (Card c : opponentHand) {
					if (!c.isLandCard())
						return false;
				}
				return true;
			}
			
			// Make sure returned object is not null
			object = _player.getOpponent().getHand().getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			
			card = (Card) object;
			// Check card type
			if (card.isLandCard())
				return false;
			
			break;
			
		case WaitChoiceShowAndTell:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;

			// player has the choice to not choose a card
			if (_choices.size() == 0)
				return true;
			
			// Make sure returned object is not null
			object = _player.getHand().getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			
			card = (Card) object;
			// Check card type
			if (!(card.isCreatureCard() || card.isLandCard() || card.isArtifact(g) || card.isEnchantment(g)))
				return false;
			
			break;
			
		case WaitChoiceCoercion:
			// Make sure exactly 1 entry
			if (_choices.size() != 1)
				return false;

			// Make sure returned object is not null
			object = _player.getOpponent().getHand().getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			break;
			
		/* Demonic Tutor */
		case WaitChoiceTutor: 
			// Make sure exactly 1 entry
			if (_choices.size() != 1)
				return false;

			// Make sure returned object is not null
			object = (Card) lib.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			break;
			
		/* Exhume */
		case WaitChoiceExhume: 
			// Make sure exactly 1 entry
			if (_choices.size() != 1)
				return false;

			// Make sure returned object is not null
			object = (Card) graveyard.getObjectByID(_choices.get(0));
			if (object == null)
				return false;card = (Card) object;
			
			// Check card type
			if (!card.isCreatureCard())
				return false;
			break;

		/* Daxe */
		case WaitChoiceDaze:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;
			
			// Make sure returned object is not null
			object = (Card) battlefield.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			
			card = (Card) object;

			// Check card subtype
			if (!card.hasSubtypeGlobal(g, Subtype.ISLAND))
				return false;
			break;
			
		/* Force of Will */
		case WaitChoiceForceOfWill:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;
			
			// Make sure returned object is not null
			object = (Card) hand.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			
			card = (Card) object;

			// check that the chosen card is NOT force of will
			if (card == g.getTopStackObject())
				return false;
			
			// Check card color
			if (!card.hasColor(Color.BLUE))
				return false;
			
			break;
			
		/* Stoneforge Mystic */
		case WaitChoiceStoneforgeMystic_search:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;
			
			// player has the choice to not pick a card
			if (_choices.size() == 0)
				return true;
			
			// Make sure returned object is not null
			object = (Card) lib.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// Check card type
			if (!card.isArtifactCard())
				return false;
			
			// Check card subtype
			if (!card.hasSubtypeGlobal(g, Subtype.EQUIPMENT))
				return false;
			break;
				
		case WaitChoiceStoneforgeMystic_put:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;
			
			// player has the choice to not pick a card
			if (_choices.size() == 0)
				return true;
			
			// 1. Make sure returned object is not null
			object = (Card) hand.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// Check card type and subtype
			if (!card.isArtifactCard() || !card.hasSubtypeGlobal(g, Subtype.EQUIPMENT))
				return false;
			break;
			
		case WaitChoiceSneakAttack:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;
			
			// player has the choice to not pick a card
			if (_choices.size() == 0)
				return true;
			
			// 1. Make sure returned object is not null
			object = (Card) hand.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// Check card type and subtype
			if (!card.isCreatureCard())
				return false;
			break;

		case WaitChoiceGoblinLackey:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;
			
			// player has the choice to not pick a card
			if (_choices.size() == 0)
				return true;
			
			// 1. Make sure returned object is not null
			object = (Card) hand.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// Check card type and subtype
			if (!(card.isPermanentCard() && card.hasCreatureType(g, CreatureType.Goblin)))
				return false;
			break;

			
		case WaitChoiceCopperGnomes:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;
			
			// player has the choice to not pick a card
			if (_choices.size() == 0)
				return true;
			
			// 1. Make sure returned object is not null
			object = (Card) hand.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// Check card type and subtype
			if (!card.isArtifactCard())
				return false;
			break;

			
		case WaitChoiceAcademyResearchers:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;
			
			// player has the choice to not pick a card
			if (_choices.size() == 0)
				return true;
			
			// 1. Make sure returned object is not null
			object = (Card) hand.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// Check card type and subtype
			if (!(card.isEnchantmentCard() && card.hasSubtypeGlobal(g, Subtype.AURA) && card.hasStaticAbility("enchantCreature")))
				return false;
			break;

			
		case WaitChoiceAetherVial_put:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;
			
			// player has the choice to not pick a card
			if (_choices.size() == 0)
				return true;
			
			// 1. Make sure returned object is not null
			object = (Card) hand.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// Check card type and CMC
			int X = g.getTopStackObject().getSource().getNbCountersOfType(g, CounterType.CHARGE);
			if (! (card.isCreatureCard() && (card.getConvertedManaCost(g) == X)))
				return false;
			break;

			
		case WaitChoiceMoxDiamond:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;
			
			// player has the choice to not pick a card
			if (_choices.size() == 0)
				return true;
			
			// 1. Make sure returned object is not null
			object = (Card) hand.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// Check card type
			if (!card.isLandCard())
				return false;
			break;
			
		case PromptRevealPlainsOrIsland:
		case PromptRevealIslandOrSwamp:
		case PromptRevealSwampOrMountain:
		case PromptRevealMountainOrForest:
		case PromptRevealForestOrPlains:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;
			
			// player has the choice to not pick a card
			if (_choices.size() == 0)
				return true;
			
			// 1. Make sure returned object is not null
			object = (Card) hand.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// 2. Check type
			if (!card.isLandCard())
				return false;
			
			// 3. Check subtypes
			StaticAbility reveal = g.getRevealLand().getStaticAbility("revealland");
			Subtype basic1 = reveal.getAssociatedBasicLandType(0);
			Subtype basic2 = reveal.getAssociatedBasicLandType(1);
			if (!(card.hasSubtypeGlobal(g, basic1) || card.hasSubtypeGlobal(g, basic2)))
				return false;
			break;
			
		case PromptDredge:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;
			
			// player has the choice to not pick a card
			if (_choices.size() == 0)
				return true;
			
			// 1. Make sure returned object is not null
			object = (Card) graveyard.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// Check card has Dredge
			if (!card.hasStaticAbility("dredge"))
				return false;
			break;
				
		/* Sylvan Scrying */
		case WaitChoiceSylvanScrying:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;
			
			// player has the choice to not pick a card
			if (_choices.size() == 0)
				return true;
			
			// Make sure returned object is not null
			object = (Card) lib.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// Check card type
			if (!card.isLandCard())
				return false;
			break;
			
		/* Basic forest card */
		case WaitChoiceBasicForestCard:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;
			
			// player has the choice to not pick a card
			if (_choices.size() == 0)
				return true;
			
			// Make sure returned object is not null
			object = (Card) lib.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// Check card type and name
			if (!(card.isLandCard() && card.hasSupertype(Supertype.BASIC) && card.getName().equals("Forest")))
				return false;
			break;
			
		/* Forest card */
		case WaitChoiceForestCard:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;
			
			// player has the choice to not pick a card
			if (_choices.size() == 0)
				return true;
			
			// Make sure returned object is not null
			object = (Card) lib.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// Check card type and name
			if (!card.hasSubtypeGlobal(g, Subtype.FOREST))
				return false;
			break;
			
			/* Basic land card */
		case WaitChoiceBasicLand:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;
			
			// player has the choice to not pick a card
			if (_choices.size() == 0)
				return true;
			
			// Make sure returned object is not null
			object = (Card) lib.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// Check card type and name
			if (!card.isLandCard() || !card.hasSupertype(Supertype.BASIC))
				return false;
			break;
			
		case WaitChoicePlainsIsland:
		case WaitChoiceIslandSwamp:
		case WaitChoiceSwampMountain:
		case WaitChoiceMountainForest:
		case WaitChoiceForestPlains:
		case WaitChoicePlainsSwamp:
		case WaitChoiceSwampForest:
		case WaitChoiceForestIsland:
		case WaitChoiceIslandMountain:
		case WaitChoiceMountainPlains:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;
			
			// player has the choice to not pick a card
			if (_choices.size() == 0)
				return true;
			
			// Make sure returned object is not null
			object = (Card) lib.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// Check card type and subtype
			if (!fetchLandTypeHelperFunction(card, _player.getState()))
				return false;
			break;
				
		/* Chord of Calling */
		case WaitChoiceChordOfCalling:
		case WaitChoiceCitanulFlute:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;
			
			// player has the choice to not pick a card
			if (_choices.size() == 0)
				return true;

			// Make sure returned object is not null
			object = (Card) lib.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// Check card type
			if (!card.isCreatureCard())
				return false;
			
			// Check converted mana cost (X value)
			if (card.getConvertedManaCost(g) > g.getTopStackObject().getXValue())
				return false;
			break;
			
		/* Green Sun Zenith */
		case WaitChoiceGreenSunZenith:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;
			
			// player has the choice to not pick a card
			if (_choices.size() == 0)
				return true;

			// Make sure returned object is not null
			object = (Card) lib.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// Check card type and color
			if (!(card.isCreatureCard() && card.hasColor(Color.GREEN)))
				return false;
			
			// Check converted mana cost (X value)
			if (card.getConvertedManaCost(g) > g.getTopStackObject().getXValue())
				return false;
			break;
		
		case WaitChoiceCreatureCard:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;
			
			// player has the choice to not pick a card
			if (_choices.size() == 0)
				return true;

			// Make sure returned object is not null
			object = (Card) lib.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// Check card type
			if (!card.isCreatureCard())
				return false;
			break;

		case WaitChoiceRemembrance:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;
			
			// player has the choice to not pick a card
			if (_choices.size() == 0)
				return true;

			// Make sure returned object is not null
			object = (Card) lib.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// Check card name
			String dyingCreatureName = ((Card) g.getTopStackObject().getAdditionalData()).getName();
			if (!card.getName().equals(dyingCreatureName))
				return false;
			break;

			
		case WaitChoiceArtifactOrCreatureCard:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;
			
			// player has the choice to not pick a card
			if (_choices.size() == 0)
				return true;

			// Make sure returned object is not null
			object = (Card) lib.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// Check card type
			if (!(card.isCreatureCard() || card.isArtifactCard()))
				return false;
			break;
			
		case WaitChoiceLandOrCreatureCard:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;
			
			// player has the choice to not pick a card
			if (_choices.size() == 0)
				return true;

			// Make sure returned object is not null
			object = (Card) lib.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// Check card type
			if (!(card.isCreatureCard() || card.isLandCard()))
				return false;
			break;
			
		case WaitChoiceGoblinCard:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;
			
			// player has the choice to not pick a card
			if (_choices.size() == 0)
				return true;

			// Make sure returned object is not null
			object = (Card) lib.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// Check card type
			if (!(card.hasCreatureType(g, CreatureType.Goblin)))
				return false;
			break;

		case WaitChoiceCreatureCardWithToughness2orLess:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;
			
			// player has the choice to not pick a card
			if (_choices.size() == 0)
				return true;

			// Make sure returned object is not null
			object = (Card) lib.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			
			card = (Card) object;
			// Check card type
			if (!card.isCreatureCard())
				return false;
			
			// Check toughness
			if (card.getToughness(g) > 2)
				return false;
			break;

			
		case WaitChoiceArtifactOrEnchantmentCard:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;
			
			// player has the choice to not pick a card
			if (_choices.size() == 0)
				return true;

			// Make sure returned object is not null
			object = (Card) lib.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// Check card type
			if (!(card.isEnchantmentCard() || card.isArtifactCard()))
				return false;
			break;
			
		case WaitChoiceEnchantment:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;
			
			// player has the choice to not pick a card
			if (_choices.size() == 0)
				return true;

			// Make sure returned object is not null
			object = (Card) lib.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// Check card type
			if (!card.isEnchantmentCard())
				return false;
			break;
			
		case WaitChoiceDamageSource:
			// Make sure exactly 1 entry
			if (_choices.size() != 1)
				return false;
			
			choiceId = _choices.get(0);
			chosenSource = g.findObjectById(choiceId);
			if (chosenSource == null)
				return false;
			object = chosenSource;
			break;			

		case WaitChoiceDamageArtifactSource:
			// Make sure exactly 1 entry
			if (_choices.size() != 1)
				return false;
			
			choiceId = _choices.get(0);
			chosenSource = g.findObjectById(choiceId);
			if (chosenSource == null)
				return false;
			
			if (chosenSource instanceof Card && !((Card)chosenSource).isArtifactCard())
				return false;
			
			object = chosenSource;
			break;	
			
		case WaitChoiceDamageLandSource:
			// Make sure exactly 1 entry
			if (_choices.size() != 1)
				return false;
			
			choiceId = _choices.get(0);
			chosenSource = g.findObjectById(choiceId);
			if (chosenSource == null)
				return false;
			
			if (chosenSource instanceof Card && !((Card)chosenSource).isLandCard())
				return false;
			
			object = chosenSource;
			break;		

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
				return false;
			}
			
			// Make sure exactly 1 entry
			if (_choices.size() != 1)
				return false;
			
			choiceId = _choices.get(0);
			chosenSource = g.findObjectById(choiceId);
			if (chosenSource == null)
				return false;
			
			if (chosenSource instanceof Card && !((Card)chosenSource).hasColor(col))
				return false;
			
			object = chosenSource;
			break;		
		}
			
		case WaitChoiceEnchantmentCardWithCCM3orLess:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;
			
			// player has the choice to not pick a card
			if (_choices.size() == 0)
				return true;

			// Make sure returned object is not null
			object = (Card) lib.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// Check card type and CMC
			if (!(card.isEnchantmentCard() && (card.getConvertedManaCost(g) <= 3)))
				return false;
			break;
			
		case WaitChoiceInstantOrSorceryCard:
			// Make sure no more than 1 entry
			if (_choices.size() > 1)
				return false;
			
			// player has the choice to not pick a card
			if (_choices.size() == 0)
				return true;

			// Make sure returned object is not null
			object = (Card) lib.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			// Check card type
			if (!(card.isInstantCard() || card.isSorceryCard()))
				return false;
			break;
		
		case WaitDiscard:     /* Discard a card */
		case WaitBrainstorm:  /* Put a card from hand on top of library */
			// Waiting for exaclty one entry (a card or DONE when no card in hand)
			if (_choices.size() != 1)
				return false;
			
			// in case the player has no card in hand and clicked "DONE" button
			if ((_choices.get(0) == -1) && hand.isEmpty())
				return true;
			
			// Make sure returned object is not null
			object = (Card) hand.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			break;

		/* Discard a creature card */
		case WaitPayCostDiscardACreatureCard:
			// Waiting for exaclty one entry (a card or DONE when no card in hand)
			if (_choices.size() != 1)
				return false;
			
			// in case the player has no card in hand and clicked "DONE" button
			if ((_choices.get(0) == -1) && hand.isEmpty())
				return true;
			
			// Make sure returned object is not null
			object = (Card) hand.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			card = (Card) object;
			if (!card.isCreatureCard())
				return false;
			break;
			
		/*  */
		case WaitChooseReplaceDraw:
			object = (Card) graveyard.getObjectByID(_choices.get(0));
			if (object == null)
				object = (Card) battlefield.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			break;
			
		/* Discard a ard */
		case WaitPayCostDiscardACard:
			// Waiting for exaclty one entry (a card or DONE when no card in hand)
			if (_choices.size() != 1)
				return false;
			
			// in case the player has no card in hand and clicked "DONE" button
			if ((_choices.get(0) == -1) && hand.isEmpty())
				return true;
			
			// Make sure returned object is not null
			object = (Card) hand.getObjectByID(_choices.get(0));
			if (object == null)
				return false;
			break;
				
		// Unimplemented yet
		default:
			return false;
		}
		
		// Add this card to our cardChoices object
		if (!_cardChoices.contains(object))
			_cardChoices.add(object);
		return true;
	}
	
	private boolean fetchLandTypeHelperFunction(Card c, Game.State state) {
		// Check card type
		if (!c.isLandCard())
			return false;
		
		// Check land subtype
		switch (state) {
		case WaitChoicePlainsIsland:
			if (!c.hasSubtypePrinted(Subtype.PLAINS) && !c.hasSubtypePrinted(Subtype.ISLAND))
				return false;
			break;
			
		case WaitChoiceIslandSwamp:
			if (!c.hasSubtypePrinted(Subtype.ISLAND) && !c.hasSubtypePrinted(Subtype.SWAMP))
				return false;
			break;
			
		case WaitChoiceSwampMountain:
			if (!c.hasSubtypePrinted(Subtype.SWAMP) && !c.hasSubtypePrinted(Subtype.MOUNTAIN))
				return false;
			break;
			
		case WaitChoiceMountainForest:
			if (!c.hasSubtypePrinted(Subtype.MOUNTAIN) && !c.hasSubtypePrinted(Subtype.FOREST))
				return false;
			break;
			
		case WaitChoiceForestPlains:		
			if (!c.hasSubtypePrinted(Subtype.FOREST) && !c.hasSubtypePrinted(Subtype.PLAINS))
				return false;
			break;
			
		case WaitChoicePlainsSwamp:
			if (!c.hasSubtypePrinted(Subtype.PLAINS) && !c.hasSubtypePrinted(Subtype.SWAMP))
				return false;
			break;
			
		case WaitChoiceSwampForest:
			if (!c.hasSubtypePrinted(Subtype.SWAMP) && !c.hasSubtypePrinted(Subtype.FOREST))
				return false;
			break;
			
		case WaitChoiceForestIsland:
			if (!c.hasSubtypePrinted(Subtype.FOREST) && !c.hasSubtypePrinted(Subtype.ISLAND))
				return false;
			break;
			
		case WaitChoiceIslandMountain:
			if (!c.hasSubtypePrinted(Subtype.ISLAND) && !c.hasSubtypePrinted(Subtype.MOUNTAIN))
				return false;
			break;
			
		case WaitChoiceMountainPlains:		
			if (!c.hasSubtypePrinted(Subtype.MOUNTAIN) && !c.hasSubtypePrinted(Subtype.PLAINS))
				return false;
			break;
		
		default:
			return false;
		}
		return true;
	}
}
