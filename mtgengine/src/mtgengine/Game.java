package mtgengine;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import mtgengine.Game.Response;
import mtgengine.Target.Category;
import mtgengine.Target.Timing;
import mtgengine.TargetRequirement.Cardinality;
import mtgengine.ability.ActivatedAbility;
import mtgengine.ability.Choice;
import mtgengine.ability.Emblem;
import mtgengine.ability.Evergreen;
import mtgengine.ability.ManaAbility;
import mtgengine.ability.Protection;
import mtgengine.ability.StaticAbility;
import mtgengine.ability.TriggeredAbility;
import mtgengine.ability.TriggeredAbility.Event;
import mtgengine.ability.TriggeredAbility.Origin;
import mtgengine.action.SpecialAction;
import mtgengine.action.SpellCast;
import mtgengine.action.PerformableAction;
import mtgengine.action.SpellCast.Option;
import mtgengine.card.Card;
import mtgengine.card.CardFactory;
import mtgengine.card.Color;
import mtgengine.card.Token;
import mtgengine.card.UntapOptional;
import mtgengine.cost.AlternateCost;
import mtgengine.cost.Cost;
import mtgengine.cost.AdditionalCost.Requirement;
import mtgengine.cost.LoyaltyCost;
import mtgengine.cost.LoyaltyCost.Type;
import mtgengine.damage.CombatDamage;
import mtgengine.damage.Damageable;
import mtgengine.effect.ContinuousEffect;
import mtgengine.effect.Effect;
import mtgengine.effect.ContinuousEffect.StopWhen;
import mtgengine.effect.ContinuousEffectFactory;
import mtgengine.effect.StaticAbilityEffect;
import mtgengine.mana.Mana.ManaType;
import mtgengine.modifier.EvergreenModifier;
import mtgengine.modifier.Modifier.Duration;
import mtgengine.player.Player;
import mtgengine.type.CardType;
import mtgengine.type.CreatureType;
import mtgengine.type.Subtype;
import mtgengine.type.Supertype;
import mtgengine.zone.Battlefield;
import mtgengine.zone.Exile;
import mtgengine.zone.Library;
import mtgengine.zone.Stack;
import mtgengine.zone.Zone;
import mtgengine.zone.Zone.Name;

public class Game {
	/* Game engine states */
	public enum State { Ready, WaitingForOpponent, // waiting for game command or priority
						PromptDoYouWantToUntap, PromptMulligan, PromptDoYouWantToUseTheAbility, PromptDoYouWantPutTheCardInYourHand, PromptCastWithoutPayingManaCost,
						PromptDoYouWantPutInGraveyard,
						PromptXValue, PromptMode, PromptTargets, PromptTargetsOrDone, PromptHost, PromptTurnabout_doYouWantToTap, PromptCatastrophe_PermanentType,
						WaitDiscardEOT, WaitDiscard, WaitBrainstorm,
						WaitChoiceTutor, WaitChoiceSylvanScrying, WaitChoiceStoneforgeMystic_put, WaitChoiceAcademyResearchers, WaitChoiceCopperGnomes, WaitChoiceGoblinLackey, 
						WaitChoiceRemembrance,
						WaitChoiceForestCard, WaitChoiceBasicForestCard, WaitChoiceCreatureCard, WaitChoiceChordOfCalling, WaitChoiceCitanulFlute, WaitChoiceStoneforgeMystic_search, WaitChoiceAetherVial_put,
						WaitChoiceBasicLand,
						WaitDeclareAttackers, WaitDeclareBlockers, WaitChooseCreatureToBlock, WaitChooseRecipientToAttack,  WaitReorderBlockers,
						WaitSacrificeCreature, WaitSacrificePermanent, WaitSacrificeLand, WaitSacrificeEnchantment, WaitSacrificeCreatureOrLand, WaitSacrificeCreatureOrPlaneswalker,
						WaitPayCostTapUntappedCreature, WaitPayCostSacrificeLand, WaitPayCostReturnLand, WaitPayCostSacrificeCreature, WaitPayCostDiscardACreatureCard, WaitPayCostSacrificeForestOrPlains,
						WaitPayCostCrew,
						WaitPayCostSacrificeForest,
						WaitPayCostSacrificeArtifact, WaitPayCostSacrificeEnchantment, WaitPayCostDiscardACard,
						WaitPayCostSacrificeGoblin, WaitPayCostReturnElf,
						WaitPayCostExileAnotherCreatureCardFromGyd,
						
						WaitChoicePlainsIsland, WaitChoiceIslandSwamp, WaitChoiceSwampMountain, WaitChoiceMountainForest, WaitChoiceForestPlains,
						WaitChoicePlainsSwamp, WaitChoiceSwampForest, WaitChoiceForestIsland, WaitChoiceIslandMountain, WaitChoiceMountainPlains,
						WaitExileForIchorid, WaitChoiceCollectedCompany, WaitChoiceExhume,
						WaitChoiceScryPutBottom, WaitChoicePutInHand, WaitChoicePutCreatureCardInHand, WaitChoicePutBottomLib, WaitChoicePutTopLib, // library manipulation
						WaitChoiceLookTop, WaitChoiceOathOfNissa, WaitChoiceGreenSunZenith, WaitChoiceMoxDiamond, WaitChoiceCreatureCardWithToughness2orLess,
						WaitUntapLand, WaitChoiceReprocess, WaitChoiceShowAndTell,
						PromptDredge, PromptDoYouWantToDrawACard, PromptApplyReplaceDraw, 
						PromptRevealPlainsOrIsland, PromptRevealIslandOrSwamp, PromptRevealSwampOrMountain, PromptRevealMountainOrForest, PromptRevealForestOrPlains,
						PromptPayForShockland, WaitPayCostSacrificeAnotherVampireOrZombie, WaitPayCostSacrificePermanent, PromptPayLifeStaticETB,
						
						PromptPay_1mana, PromptPay_2mana, PromptPay_3mana, PromptPay_4mana, PromptPay_Xmana,
						PromptPay_1life, PromptPay_2life, WaitReturnPermanent, PromptDoYouWantToShuffle,
						PromptPayPunishingFire, PromptPayUpkeepCost, PromptPayEchoCost, WaitChoiceTurnabout, PromptPayCumulativeUpkeep,
						PromptSacrificeEnchantment, PromptDiscardToPay, PromptDoYouWantToSacrificeALand, PromptPayChildOfGaea, PromptPayDriftingDjinn,
						
						WaitchoiceCardInGraveyard,
						WaitChoiceEnchantment, WaitChoiceCreature, WaitChoiceEnchantmentAlteration, 
						WaitChoiceArtifactOrCreatureCard,
						WaitChoiceLandOrCreatureCard,
						WaitChoiceArtifactOrEnchantmentCard,
						WaitChoiceEnchantmentCardWithCCM3orLess,
						WaitChoiceInstantOrSorceryCard,
						WaitChoiceGoblinCard,
						WaitChoiceDamageSource,
						WaitChoiceDamageArtifactSource, WaitChoiceDamageLandSource,
						WaitChoiceDamageWhiteSource, WaitChoiceDamageBlueSource, WaitChoiceDamageBlackSource, WaitChoiceDamageRedSource, WaitChoiceDamageGreenSource,
						WaitChoicePurgingScythe,
						
						WaitChoiceCoercion,
						WaitChoiceDuress, WaitChoiceThoughtseize,
						LookPlayersHand,
						WaitChooseColor, WaitChooseTriggeredManaAbilityColor, PromptChooseColorStaticETB,
						PromptAbundance_CardType, WaitChooseReplaceDraw, WaitChoiceSneakAttack, WaitChooseManaCombination,
						WaitChoiceForceOfWill, WaitChoiceDaze, WaitHeartOfKiran, WaitChoiceHarnessedLightning
					};

	/* Command Responses */
	public enum Response { OK, OKholdPriority,
					Error, ErrorComputeQuery, ErrorWaitingForOpponent, // generic error
                    ErrorNotMainPhase, ErrorStackNotEmpty, ErrorNotALand, ErrorTooManyLandDrops, //play land errors
					ErrorNotASpell, // cast spell errors
					ErrorAlreadyTapped, // tap errors
					ErrorEmptyLibrary, // draw error
					ErrorTargetBecameIllegal, ErrorInvalidTarget, ErrorNoLegalTarget, ErrorIncorrectZone, // targeting errors
					ErrorIndestructible, ErrorNoActivatedAbility,
					MoreStep, // multi-step spell resolving
					ErrorTooManyCards, ErrorInterveningIfNotMet, ErrorNotMyTurn, ErrorIncorrectController, ErrorCannotPayCost,
					ErrorIllegalBlockDeclaration, ErrorIllegalAttackDeclaration,
					ErrorMaxNbPlayerReached, ErrorNotEnoughPlayers,
					SpellCountered, SpellNotCountered, Mulliganing_B, Mulliganing_A, ErrorCardIsTapped,
					PrintActions,
					ErrorInvalidCommand, ErrorInvalidNumberOfArguments, ErrorInvalidCardNumber, ErrorInvalidPlayMode,
					ErrorCardIsToken, ErrorNotDoubleFace, ErrorForbiddenSpell,
					ErrorNotEnoughMana
				}; 

	public static final int COMMAND_DONE              = -1;
	public static final int COMMAND_PASS_PRIORITY     = -2;
	public static final int COMMAND_YES            	  = -3;
	public static final int COMMAND_NO         		  = -4;
	public static final int COMMAND_TOGGLE_DEBUG_MODE = -5;
	public static final int COMMAND_GET_ACTIONS       = -6;
	public static final int COMMAND_PERFORM_ACTION    = -7;
	public static final int COMMAND_GET_CARD_INFO     = -8;
	
	public enum Answer { Yes, No };
	private Answer _answer; 

	private boolean _bDebugMode = false;

	private int _nbCardsToDraw = 0;
	public Card _currentAura = null;
	private StaticAbility _currentSA = null;
	public Card _untapOptional_card = null;
	private ContinuousEffect _replaceDrawEffect;
	
	private static int _internalObjectId = 0;

	private AlternateCost _currentAC = null;
	
	/* Permanent categories, for nice client presentation */
	public enum PermanentCategory { Lands, Creatures, Other };
	
	/* Phases, Steps and Turns */
	public enum Phase { Beginning, PreMain, Combat, PostMain, Ending };
	public enum Step { Untap, Upkeep, Draw, // Beginning Phase
				Main, // Main Phase
	            BegCombat, DeclAttackers, DeclBlockers, FirstStrikeCombatDamage, CombatDamage, EndCombat, // Combat Phase
				End, Cleanup  }; // Ending Phase
				
	/* Zones */
	private Vector<Zone> _zones;
	private Stack _stack;
	private Battlefield _battlefield;
	private Zone _special;
	
	/* Misc data */
	private final static int DEFAULT_MAX_NB_LANDS = 1;
	private final static int DEFAULT_MAX_HAND_SIZE = 7;
	private final static int DEFAULT_STARTING_HAND_SIZE = 7;
	
	private Phase _phase = Phase.PreMain;
	private Step _step = Step.Main;
	private boolean _bDrawEmptyLib = false;
	private boolean _bGameOver = false;
	private Vector<Player> _extraTurns = new Vector<Player>();
	
	/* Spell casting */
	private StackObject _topStackObject;
	
	/* Combat */
	private Vector<Card> _attackers;
	private Vector<Card> _blockers;
	private Card _blockingCreature = null;
	private Card _attackingCreature = null;
	private boolean _bBlockersValidated;
	private boolean _bAttackersValidated;
	private Vector<CombatDamage> _assignedCombatDamage = new Vector<CombatDamage>();
	private int _iTargetReq = 0;
	public int _lastDamageDealt = -1;
	
	/* Choices during resolution */
	private Choice _effectChoice;
	private StackObject _currentManaAbility;
	
	/* Players */
	private Vector<Player> _players = new Vector<Player>();
	private Player _playerWithPriority;
	public Player _activePlayer;
	private Player _monarch = null;
	private static int _nbPlayersWhoPassed = 0;

	/* Performable actions of a card */
	private Vector<PerformableAction> _performableActions = new Vector<PerformableAction>();
	
	/* Triggerered abilities */
	private Vector<TriggeredAbility> _queuedTriggeredAbilities = new Vector<TriggeredAbility>();
	
	/* Triggerered mana abilities (must be handled differently because they do not use the stack) */
	private Vector<TriggeredAbility> _queuedTriggeredManaAbilities = new Vector<TriggeredAbility>();
	
	/* Active continuous effects */
	private Vector<ContinuousEffect> _continuousEffects = new Vector<ContinuousEffect>();
	public Vector<ContinuousEffect> _replaceDrawEffects = new Vector<ContinuousEffect>();
	
	private int _requestCardInfo_IdPlayer;
	private int _requestCardInfo_IdCard;
	private Card _reboundSpell = null;
	private Card _spellCopy = null;
	public Card _revealLand = null;
	public Card _shockland = null;
	
	public Game() {
		MtgObject.resetID();
		initZones();
	}

	/* Initialize players opponents depending on the number of players
	** 1 player (solitaire) game : player = opponent
	** 2 players game : player1 is opponent of player 2 (and vice versa)
	*/
	private void setOpponents() {
		Player p1, p2;
		
		p1 = _players.get(0);
		p2 = _players.get(1);
		p1.setOpponent(p2);
		p2.setOpponent(p1);
	}
	
	private static int generateInternalObjectId() {
		_internalObjectId++;
		return _internalObjectId;
	}
	
	public Response doMulligans() {
		boolean bMulligansOK = true;
		Player opponent = _activePlayer.getOpponent();
		
		switch (_activePlayer.getKeepHand())
		{
		// active player has not yet made a choice
		case Player.KEEP_HAND_UNKNOWN:
			bMulligansOK = false;
			_activePlayer.setState(State.PromptMulligan);
			return Response.Mulliganing_A;
			//break;
			
		// active player has made either choice
		default:
			switch (opponent.getKeepHand())
			{
			// opponent has not yet made a choice
			case Player.KEEP_HAND_UNKNOWN:
				bMulligansOK = false;
				opponent.setState(State.PromptMulligan);
				_activePlayer.setState(State.WaitingForOpponent);
				return Response.Mulliganing_A;
				
			// both players have made their choice
			default:
				for (Player p : _players) {
					if (p.getKeepHand() == Player.KEEP_HAND_NO) {
						p.takeMulligan();
						bMulligansOK = false;
					}
				}
				break;
			}
			break;
		}

		if (bMulligansOK) {
			_activePlayer.setState(State.Ready);
			return Response.OK;			
		}
		return Response.Mulliganing_B;
	}
	
	/**
	 * Start the game.
	 * @return
	 */
	public Response startGame() {
		_activePlayer = _players.get(0);
		_activePlayer.setTurn(1);
		_playerWithPriority = _activePlayer;
		setOpponents();
		
		for (Player p : _players) {
			_zones.add(p.getLibrary());
			_zones.add(p.getHand());
			_zones.add(p.getGraveyard());
			_zones.add(p.getExile());
			_zones.add(p.getCommand());
			p.shuffle();
			drawCards(p, DEFAULT_STARTING_HAND_SIZE);
		}
		doMulligans();
		return Response.OK;
	}

	public void assignMonarch(Player p) {
		// Remove the previous monarch, if any
		if (_monarch != null) {
			Card c = (Card) _monarch.getCommand().findCardByName("The Monarch");
			_monarch.getCommand().removeObject(c);
		}
		
		_monarch = p;
		Card monarchToken = CardFactory.create("The Monarch");
		monarchToken.setController(this, p);
		p.getCommand().addObject(monarchToken);
		
		// fire callbacks associated with p becoming the monarch
		ArrayList<EffectCallback> effectCallbacks = p.getEffectCallbacks();
		EffectCallback ecb;
		
		while (!effectCallbacks.isEmpty()) {
			ecb = effectCallbacks.get(0);
			String methodName = ecb.getMethodName();
			Object parameter = ecb.getParameter();
			try {
				Method m = Effect.class.getMethod(methodName, Game.class, Object.class);
				m.invoke(null, this, parameter);
			} catch (NoSuchMethodException e) {
				System.err.println("No such method : " + methodName);
			} catch (Exception e) {
				System.err.println("Other exception.");
			}
			p.removeEffectCallback(ecb);			
		}
	}
	
	public Player getTheMonarch() {
		return _monarch;
	}
	
//	private Response drawCard(Player p) {
//		if (p.getLibrary().isEmpty()) {
//			attemptToDrawEmptyLib();
//			return Response.ErrorEmptyLibrary;
//		}
//
//		// Look for effects that replace drawing a card (like Dredge and Abundance)
//		int nbReplacementEffects = 0;
//		_replaceDrawEffects.clear();
//		
//		// Any dredge cards ?
//		if (p.getGraveyard().containsDredgeCards())
//			nbReplacementEffects++;
//		
//		// Any other replacement effects ?
//		for (ContinuousEffect ce : _continuousEffects)
//			if (ce.replacesDraw(this, p)) {
//				_replaceDrawEffects.add(ce);
//				nbReplacementEffects++;
//			}
//		
//		// No replacement effect was found, do normal draw
//		if (nbReplacementEffects == 0)
//			return doNormalDraw(p);
//
//		// Exactly one replacement effect was found, apply it
//		else if (nbReplacementEffects == 1) { 
//			if (!_replaceDrawEffects.isEmpty()) {
//				_replaceDrawEffect = _replaceDrawEffects.get(0);
//				p.setState(State.PromptApplyReplaceDraw);
//			}
//			else
//				p.setState(State.PromptDredge);
//			return Response.MoreStep;
//		}
//
//		// More than one replacement effect was found. Prompt player to choose one.
//		else { 
//			p.setState(State.WaitChooseReplaceDraw);
//			return Response.MoreStep;
//		}
//	}
//	
	/**
	 * 
	 * @param p
	 * @return
	 */
	private Response doDredge(Player p) {
//		Card c;
//		int dredgeNumber;
//		
//		c = (Card) getChoices().get(0);
//		move_GYD_to_HND(c);
//		dredgeNumber = Integer.parseInt(c.getStaticAbility("dredge").getParameter());
//		for (int i = 0; (i < dredgeNumber) && !p.getLibrary().isEmpty(); i++)
//			move_LIB_to_GYD(p.getLibrary().getTopCard());
//		_nbCardsToDraw--;
//		if (_nbCardsToDraw > 0) {
//			return drawCard(p);
//		}
//		else {
//			if (_stack.isEmpty())
//				return givePriority(_activePlayer);				
//			else
//				return resolveTopObject();	
//		}
		return Response.OK;
	}
	
	public Response drawCards(Player p, int nb) {
		for (int i = 0; i < nb; i++)
			move_LIB_to_HND(p.getLibrary().getTopCard());
		return Response.OK;
	}
	
//	private Response doNormalDraw(Player p) {
//		if (_nbCardsToDraw == 0)
//			return Response.OK;
//
//		move_LIB_to_HND(p.getLibrary().getTopCard());
//		
//		_nbCardsToDraw--;
//		if (_nbCardsToDraw > 0)
//			return drawCard(p);
//		else
//		{
//			if (_stack.isEmpty()) {
//				return givePriority(_activePlayer);				
//			}
//			else
//				return resolveTopObject();	
//		}
//	}
	
	public Response assignDredgeResponse(int playerId, Vector<Integer> choices) {
//		Player p = findPlayerByID(playerId);
//		_effectChoice = new Choice(p, choices);
//		
//		if (validateChoices()) {
//			// Player chose to dredge a card
//			if (getChoices().size() == 1)
//				return doDredge(p);
//			else // Player chose not to dredge : normal draw
//				return doNormalDraw(p);
//		}
//		return Response.MoreStep;
		return Response.OK;
	}

	public Response assignReplaceDraw(int playerId, Vector<Integer> choices) {
		Player p = findPlayerByID(playerId);
		_effectChoice = new Choice(p, choices);

		if (validateChoices()) {
			Vector<MtgObject> ch = getChoices();
			MtgObject choice = ch.get(0);

			if (((Card) choice).hasStaticAbility("dredge"))
				p.setState(State.PromptDredge);
			else
			{
				for (ContinuousEffect ce : _replaceDrawEffects) {
					if (ce.getSource() == choice) {
						_replaceDrawEffect = ce;
						break;
					}
				}
				p.setState(State.PromptApplyReplaceDraw);
			}	
		}
		
		return Response.MoreStep;
	}
	
	public Response assignShocklandResponse(Answer answer) {
		// Player chose to pay 2 life : the Shockand enters TB untapped
		if (answer == Answer.Yes)
			_shockland.getController(this).loseLife(2);
		else // Player chose not to pay 2 life : the Shockand enters TB tapped
			_shockland.tap(this);
		enterBattlefield(_shockland);
		finishSwitchZone(null, _battlefield, _shockland);
		finishResolveTopObject(_topStackObject, Response.OK);
		return Response.OKholdPriority;
	}
	
	public Response assignRevealLandResponse(int playerId, Vector<Integer> choices) {
		Player p = findPlayerByID(playerId);
		_effectChoice = new Choice(p, choices);
		
		if (validateChoices()) {
			// Player chose not to reveal a basic land : the RevealLand enters TB tapped
			if (choices.size() != 1)
				_revealLand.tap(this);
			enterBattlefield(_revealLand);
			finishSwitchZone(null, _battlefield, _revealLand);
		}
		else
			return Response.MoreStep;
		givePriority(_activePlayer);
		return Response.OKholdPriority;
	}
	
//	public Response drawCards(Player p, int nbCards) {
//		_nbCardsToDraw = nbCards;
//		return drawCard(p);
//	}
	
	public Response addPlayer(String name, String deck) {
		if (_players.size() == 2)
			return Response.ErrorMaxNbPlayerReached;
		
		_players.add(new Player(this, name, deck));
		return Response.OK;
	}
	
	private void initZones() {
		// instantiate zones
		_stack = new Stack(this);
		_battlefield = new Battlefield(this);
		_special = new Zone(this, null, null);
		
		//add them to _zones container
		_zones = new Vector<Zone>();
		_zones.add(_stack);
		_zones.add(_battlefield);
		_zones.add(_special);
		
		// instantiate combat containers
		_attackers = new Vector<Card>();
		_blockers = new Vector<Card>();
	}
	
	/**
	 * Computes if object target is protected from object source
	 * @param source
	 * @param target
	 * @return true if target is protected, false otherwise
	 */
	public boolean computeIsProtectedFrom(MtgObject source, MtgObject target) {
		Card sourceCard;
		Card targetCard;
		
		// 1. check source color (ex: protection from red)
		for (Object protection : target.getProtections(this)) {
			if (protection instanceof Color) {
				if (source instanceof Card) {
					sourceCard = (Card) source;
					if (sourceCard.hasColor((Color) protection)) {
						if (target instanceof Card) {
							targetCard = (Card) target;
							// if target object is a card, make sure it's on the battlefield (protection only works while card is OTB)
							if (targetCard.isOTB(this))
								return true;
						}
						else // target is another object like an ability or a player
							return true;
					}
				}
				
			}
		}
		// 1bis. Protection from colored spells (Emrakul)
		for (Object protection : target.getProtections(this)) {
			if ((protection instanceof Protection) && (protection == Protection.COLOREDSPELLS)) 			{
				if (source instanceof Card) {
					sourceCard = (Card) source;
					if ((sourceCard.isInstantCard() || sourceCard.isSorceryCard()) && (sourceCard.isColored()))
						return true;
				}
			}
		}
		
		// 2. check source type (ex: protection from creatures)
		// 3. check converted mana cost (ex: protection from converted mana cost 2 or more)
		// 4. check player (ex: True Name Nemesis)
		// 5. check protection from everything (just return true)
		return false;
	}
	
	public int getMaxNbLands(Player player, int maxNbLands) {
		for (ContinuousEffect ce : _continuousEffects) {
			maxNbLands = ce.modifyMaxNbLands(this, player, maxNbLands);
		}
		return maxNbLands;
	}
	
	/******************************************************/
	
	public Phase getPhase() {
		return _phase;
	}
	
	public Step getStep() {
		return _step;
	}
	
	public State getState(int idPlayer) {
		return findPlayerByID(idPlayer).getState();
	}
	
	public Response takeMulligan(int idPlayer) {
		findPlayerByID(idPlayer).takeMulligan();
		return Response.OK;
	}
	
	private boolean needFirstStrikeStep() {
		for (Card atk : _attackers) {
			if (atk.hasEvergreenGlobal(Evergreen.FIRSTSTRIKE, this) || atk.hasEvergreenGlobal(Evergreen.DOUBLESTRIKE, this))
				return true;
		}
		for (Card blk : _blockers) {
			if (blk.hasEvergreenGlobal(Evergreen.FIRSTSTRIKE, this) || blk.hasEvergreenGlobal(Evergreen.DOUBLESTRIKE, this))
				return true;
		}
		return false;
	}
	
	/* advancing to the next step */
	private Response advanceStep() {
		Response ret;
		
		switch (_step) {
		
		/* Untap Step*/
		case Untap:
			_step = Step.Upkeep;
			ret = upkeepStep();
			if (ret == Response.MoreStep)
				return Response.MoreStep;
			givePriority(_activePlayer);
			break;
			
		/* Upkeep Step */
		case Upkeep:
			boolean bSkipDrawStep = false;
			
			// Look for continuous effects that make a player skip his draw step
			for (ContinuousEffect ce : _continuousEffects) {
				if (ce.skipDrawStep(this, _activePlayer))
					bSkipDrawStep = true;
			}
			
			// Do the draw step (normal behavior)
			if (!bSkipDrawStep) {
				_step = Step.Draw;
				ret = drawStep();
				if (ret == Response.MoreStep)
					return Response.MoreStep;
				givePriority(_activePlayer);
			}
			else {// Skip the draw step
				_step = Step.Main;
				ret = advancePhase();
				if (ret == Response.MoreStep)
					return Response.MoreStep;
				givePriority(_activePlayer);
			}
			break;
			
		/* Draw Step */
		case Draw:
			_step = Step.Main;
			ret = advancePhase();
			if (ret == Response.MoreStep)
				return Response.MoreStep;
			givePriority(_activePlayer);
			break;

		/* Main Phases (no step) */
		case Main:
			advancePhase();
			if (_phase == Phase.Combat) {
				_step = Step.BegCombat;
				queueBOCeffectsZone(_battlefield, Origin.BTLFLD);
			}
			else {
				_step = Step.End;
				endStep();
			}
			givePriority(_activePlayer);
			break;
			
		/* Beginning of combat Step */
		case BegCombat:
			_bBlockersValidated = false;
			_bAttackersValidated = false;
			_step = Step.DeclAttackers;
			declareAttackersStep();
			break;
			
		/* Declare Attackers Step */
		case DeclAttackers:
			if (_attackers.size() > 0)
			{
				_step = Step.DeclBlockers;
				declareBlockersStep();
			}
			else
			{
				_step = Step.EndCombat;
			}
			break;
		
		/* Declare Blockers Step */
		case DeclBlockers:
			if (needFirstStrikeStep()) {
				_step = Step.FirstStrikeCombatDamage;
				firstStrikeCombatDamageStep();
			}
			else {
				_step = Step.CombatDamage;
				combatDamageStep();
			}
			givePriority(_activePlayer);
			break;
		
		/* First strike combat Damage Step */
		case FirstStrikeCombatDamage:
			_step = Step.CombatDamage;
			combatDamageStep();
			givePriority(_activePlayer);
			break;
			
		/* Combat Damage Step */
		case CombatDamage:
			_step = Step.EndCombat;
			endOfCombatStep();
			givePriority(_activePlayer);
			break;
		
		/* End of Combat Step */
		case EndCombat:
			_step = Step.Main;
			removeAllCreaturesFromCombat();
			ret = advancePhase();
			if (ret == Response.MoreStep)
				return Response.MoreStep;
			givePriority(_activePlayer);
			break;
		
		/* End Step */
		case End:
			_step = Step.Cleanup;
			cleanupStep();
			break;
		
		/* Cleanup Step */
		case Cleanup:
			advancePhase();
			_step = Step.Untap;
			untapStep();
			break;
		
		default:
			break;
		}
		this.emptyManaPools();
		return Response.OK;
	}

	public void emptyManaPools() {
		for (Player p : _players)
			p.emptyManaPool();
	}
	
	public void removeCreatureFromCombat(Card creature) {
		if (creature.isAttacking(this)) {
			creature.stopAttacking();
			_attackers.remove(creature);
		}
			
		if (creature.isBlocking(this)) {
			creature.stopBlocking();
			_blockers.remove(creature);
		}
	}
	
	private void removeAllCreaturesFromCombat() {
		for (Card atk : _attackers) {
			atk.stopAttacking();
		}
		_attackers.clear();
		for (Card blk : _blockers) {
			blk.stopBlocking();
		}
		_blockers.clear();
	}
	
	private void endOfCombatStep() {
		queueAndRemoveTriggersFromContinuousEffect(Event.EndOfCombat, true);
		if (_queuedTriggeredAbilities.size() > 0) {
			checkStateBasedActions();
			putTriggeredAbilitiesOnStack(_queuedTriggeredAbilities);
		}
	}
	
	private void firstStrikeCombatDamageStep() {
		_assignedCombatDamage.clear();

		for (Card atk : _attackers) {
			if (!atk.isCreature(this))
				continue;

			int power = atk.getPower(this);
			Damageable recipient = atk.getAttackRecipient();
			
			// if the attacking creature is blocked... 
			if (atk.isBlocked(this))
			{
				if (!atk.doesIgnoreBlocker()) {
					// deal damage to the 1st blocking creature
					// TODO: implement gang blocking !
					Card firstBlocker = (Card) atk.getBlockers().get(0);
					if (atk.hasEvergreenGlobal(Evergreen.FIRSTSTRIKE, this) || atk.hasEvergreenGlobal(Evergreen.DOUBLESTRIKE, this))
						_assignedCombatDamage.add(new CombatDamage(atk, firstBlocker, power));	
				}
				else
				{
					if (atk.hasEvergreenGlobal(Evergreen.FIRSTSTRIKE, this) || atk.hasEvergreenGlobal(Evergreen.DOUBLESTRIKE, this))
						_assignedCombatDamage.add(new CombatDamage(atk, recipient, power));
				}
				
				// then each blocking creature deals damage to the blocked creature
				for (Card blocker : atk.getBlockers()) {
					if (blocker.hasEvergreenGlobal(Evergreen.FIRSTSTRIKE, this) || blocker.hasEvergreenGlobal(Evergreen.DOUBLESTRIKE, this))
						_assignedCombatDamage.add(new CombatDamage(blocker, atk, blocker.getPower(this)));
				}
			}
			else // it is unblocked, deal damage to the recipient (player or planeswalker)
			{
				if (atk.hasEvergreenGlobal(Evergreen.FIRSTSTRIKE, this) || atk.hasEvergreenGlobal(Evergreen.DOUBLESTRIKE, this))
					_assignedCombatDamage.add(new CombatDamage(atk, recipient, power));
			}
		}

		for (CombatDamage cd : _assignedCombatDamage)
			cd.compute(this);
	}
	
	private void combatDamageStep() {
		_assignedCombatDamage.clear();

		for (Card atk : _attackers) {
			if (!atk.isCreature(this))
				continue;
			
			int power = atk.getPower(this);
			Damageable recipient = atk.getAttackRecipient();
			
			// if the attacking creature is blocked... 
			if (atk.isBlocked(this))
			{
				if (!atk.doesIgnoreBlocker()) {
					// deal damage to the 1st blocking creature
					// TODO: implement gang blocking !
					Card firstBlocker = (Card) atk.getBlockers().get(0);
					if (!atk.hasEvergreenGlobal(Evergreen.FIRSTSTRIKE, this) || atk.hasEvergreenGlobal(Evergreen.DOUBLESTRIKE, this))
						_assignedCombatDamage.add(new CombatDamage(atk, firstBlocker, power));
				}
				else
				{
					if (!atk.hasEvergreenGlobal(Evergreen.FIRSTSTRIKE, this) || atk.hasEvergreenGlobal(Evergreen.DOUBLESTRIKE, this))
						_assignedCombatDamage.add(new CombatDamage(atk, recipient, power));
				}
				
				// then each blocking creature deals damage to the blocked creature
				for (Card blocker : atk.getBlockers()) {
					if (!blocker.hasEvergreenGlobal(Evergreen.FIRSTSTRIKE, this) || blocker.hasEvergreenGlobal(Evergreen.DOUBLESTRIKE, this))
						_assignedCombatDamage.add(new CombatDamage(blocker, atk, blocker.getPower(this)));
				}
			}
			else // it is unblocked, deal damage to the recipient (player or planeswalker)
			{
				if (!atk.hasEvergreenGlobal(Evergreen.FIRSTSTRIKE, this) || atk.hasEvergreenGlobal(Evergreen.DOUBLESTRIKE, this))
					_assignedCombatDamage.add(new CombatDamage(atk, recipient, power));
			}
		}

		for (CombatDamage cd : _assignedCombatDamage)
			cd.compute(this);
	}

	public void switchCards(Card out, Card in) {
		if (_attackers.contains(out)) {
			in.attacks(out.getAttackRecipient());
			_attackers.remove(out);
			_attackers.add(in);
		}
		if (_attackingCreature == out)
			_attackingCreature = in;
		
		if (_blockers.contains(out)) {
			_blockers.remove(out);
			_blockers.add(in);
		}
		if (_blockingCreature == out)
			_blockingCreature = in;
		
		for (MtgObject obj : _stack.getObjects()) {
			StackObject so = (StackObject) obj;
			if (so.hasTargetRequirements()) {
				int nbTargets = so.getNbTarget();
				for (int i = 0; i < nbTargets; i++) {
					if (so.getTargetObject(i) == out)
						so.addTarget(i, in);
				}
			}
		}
	}
	
	public void resetAttackersStep() {
		declareAttackersStep();
		_attackers.clear();
		_attackingCreature = null;
	}
	
	public void declareAttackersStep() {
		_activePlayer.setState(State.WaitDeclareAttackers);
	}
	
	public void resetBlockersStep() {
		declareBlockersStep();
		_blockers.clear();
		_blockingCreature = null;
	}
	
	public void declareBlockersStep() {
		_activePlayer.getOpponent().setState(State.WaitDeclareBlockers);
	}
	
	public void giveExtraTurn(Player p) {
		_extraTurns.add(p);
	}
	
	public void promptUntap(Card c) {
		_activePlayer.setState(State.PromptDoYouWantToUntap);
		_untapOptional_card = c;
	}

	public void setOptionalUntap(UntapOptional.State answer) {
		_untapOptional_card.setUntapOptionalState(answer);
		if (_activePlayer.newTurn() == Response.MoreStep)
			return;
		finishUntapPermanents();
	}
	
	private void untapStep() {
		if (_extraTurns.size() > 0) {
			_activePlayer = _extraTurns.get(0);
			_extraTurns.remove(0);
		}
		else
			_activePlayer = _activePlayer.getOpponent();
		endUntilYNTEffects();
		_activePlayer.incrementTurn();
		if (_activePlayer.newTurn() == Response.MoreStep)
			return;
		finishUntapPermanents();
	}
	
	/**
	 * This method will stop the effects that last until the end of the untap step of the targeted player.
	 * i.e. the effect generated by Exhaustion
	 */
	private void endExhaustionEffects() {
		Vector<ContinuousEffect> toBeRemoved = new Vector<ContinuousEffect>();
		for (ContinuousEffect ce : _continuousEffects) {
			if (ce.getAdditionalData() instanceof Player) {
				Player targetedPlayer = (Player) ce.getAdditionalData();
				if (ce.doesStop(StopWhen.TARGET_UNTAP) && (_activePlayer == targetedPlayer))
					toBeRemoved.add(ce);	
			}
		}

		for (ContinuousEffect ce : toBeRemoved)
			_continuousEffects.remove(ce);
	}
	
	private void finishUntapPermanents() {
		_activePlayer.getOpponent().newTurn();
		_playerWithPriority = _activePlayer;
		endExhaustionEffects();
		advanceStep();
	}
	
	public int getNbSpellsCastLastTurn() {
		int ret = 0;
		for (Player p : _players) {
			ret += p.getNbSpellsCastLastTurn();
		}
		return ret;
	}
	
	private PerformableAction findActionById(int idAction) {
		for (PerformableAction action : _performableActions) {
			if (action.getID() == idAction)
				return action;
		}
		return null;
	}
	
	public MtgObject findObjectById(int idCard) {
		MtgObject ret = null;
		
		// Search among permanents
		if ((ret = _battlefield.getObjectByID(idCard)) != null)
			return ret;
		
		// Search among objects on the stack
		MtgObject obj = _stack.getObjectByID(idCard);
		if (obj != null) {
			if (obj instanceof Card)
				return (Card) obj;	
			else if (obj instanceof ActivatedAbility)
				return ((ActivatedAbility) obj).getSource();
			else if (obj instanceof TriggeredAbility)
				return ((TriggeredAbility) obj).getSource();
		}
		
		// Search among player's private zones (library, graveyard, exile and command)
		for (Player p : _players) {
			ret = p.findObjectById(idCard);
			if (ret != null)
				return ret;
		}
		return null;
	}
	
	private Damageable findAttackRecipientById(int idObject) {
		Damageable attackRecipient;
		
		// Check if recipient is a permanent
		attackRecipient = (Damageable) _battlefield.getObjectByID(idObject);
		if (attackRecipient != null)
		{
			// Recipient is a permanent
			Card permanent = (Card) attackRecipient;
			
			// Check if recipient is a planeswalker
			if (permanent.isPlaneswalkerCard())
				return permanent;
			
			// Recipient is a non-planeswalker permanent : return null
			return null;
		}
		else 
		{
			// Recipient is not a permanent, check if recipient is a player
			for (Player player : _players) {
				if (player.getID() == idObject)
					return player;
			}
			
			// Recipient is neither a planeswalker nor a player : return null
			return null;
		}
	}
	
	/**
	 * Get continuous effects
	 */
	public Vector<ContinuousEffect> getContinuousEffects() {
		return _continuousEffects;
	}
	
	private Response advancePhase() {
		switch (_phase) {
		case Beginning:
			_phase = Phase.PreMain;
			return mainPhase();
			
		case PreMain:
			_phase = Phase.Combat;
			break;
			
		case Combat:
			_phase = Phase.PostMain;
			return mainPhase();
			
		case PostMain:
			_phase = Phase.Ending;
			break;
			
		case Ending:
			_phase = Phase.Beginning;
			break;
			
		default:
			break;
		}
		
		return Response.OK;
	}
	
	private Response cleanupStep() {
		Response ret;
		
		queueAndRemoveTriggersFromContinuousEffect(Event.BegOfNextCleanupStep, true);
		if (!_queuedTriggeredAbilities.isEmpty()) {
			givePriority(_activePlayer);
			return Response.OK;
		}
		else
		{
			ret = checkHandSize();
			if (ret == Response.OK) {
				endUntilEOTEffects();
				ret = cleanDamage();
				advanceStep();
			}	
		}
		return ret;
	}
	
	// End "until your next turn" effects
	private void endUntilYNTEffects() {
		_battlefield.endUntilYNTEffects(_activePlayer);
		for (Player p : _players)
			p.endUntilYNTEffects(_activePlayer);
	}
	
	// End "until end of turn" effects
	private void endUntilEOTEffects() {
		_battlefield.endUntilEOTEffects();
		for (Player p : _players) {
			p.getGraveyard().endUntilEOTEffects();
		}
		
		// remove triggered abilities that last only this turn (i.e. Narset Transcendant -2 ability)
		if (_continuousEffects != null) {
			Iterator<ContinuousEffect> it = _continuousEffects.iterator();
			while (it.hasNext()) {
				ContinuousEffect ce = (ContinuousEffect) it.next();

				if (ce.doesStop(StopWhen.END_OF_TURN))
					it.remove();
				
				if (ce.hasTriggeredAbilities(this, Event.YouCastYourNextInstantOrSorcerySpellThisTurn))
					it.remove();
			}
		}
	}

	private Response checkHandSize() {
		if (_activePlayer.getHandSize() > DEFAULT_MAX_HAND_SIZE)
		{
			_activePlayer.setState(State.WaitDiscardEOT);
			return Response.ErrorTooManyCards;
		}
		return Response.OK;
	}

	private Response cleanDamage() {
		_battlefield.cleanDamage();
		return Response.OK;
	}

	private void queueBOMPeffectsZone(Zone zone, Origin origin) {
		Vector<TriggeredAbility> effects = new Vector<TriggeredAbility>();
		for (MtgObject obj : zone.getObjects()) {
			effects.clear();
			if (obj instanceof Card) {
				Card c = (Card) obj;
				if (c.getController(this) == _activePlayer)
					effects.addAll(c.getTriggeredAbilities(this, Event.BegOfYourMainPhase, origin));
				queueTriggeredAbilities(effects, c);	
			}
		}
	}
	
	private void queueBOUeffectsZone(Zone zone, Origin origin) {
		Vector<TriggeredAbility> effects = new Vector<TriggeredAbility>();
		for (MtgObject obj : zone.getObjects())
		{
			effects.clear();
			if (obj instanceof Card) {
				Card c = (Card) obj;
				if (c.getController(this) == _activePlayer)
					effects.addAll(c.getTriggeredAbilities(this, Event.BegOfYourUpkeep, origin));
				effects.addAll(c.getTriggeredAbilities(this, Event.BegOfEachUpkeep, origin));
				
				// Auras
				if ((zone.getName() == Zone.Name.Battlefield) && c.hasSubtypePrinted(Subtype.AURA) && (c.getHost().getController(this) == _activePlayer))
					effects.addAll(c.getTriggeredAbilities(this, Event.BegOfEnchantedCreatureControllerUpkeep, origin));
				
				queueTriggeredAbilities(effects, c);	
			}
			else if (obj instanceof Emblem) {
				Emblem e = (Emblem) obj;
				if (e.getController(this) == _activePlayer) {
					Vector<TriggeredAbility> ta = e.getContinuousEffet().getTriggeredAbilities(this, Event.BegOfYourUpkeep);
					if (ta != null)
						effects.addAll(ta);
				}
				Vector<TriggeredAbility> ta = e.getContinuousEffet().getTriggeredAbilities(this, Event.BegOfEachUpkeep);
				if (ta != null)
					effects.addAll(ta);
				queueTriggeredAbilities(effects, e.getSource());
			}
		}
	}
	
	private void queueBODeffectsZone(Zone zone, Origin origin) {
		Vector<TriggeredAbility> effects = new Vector<TriggeredAbility>();
		for (MtgObject obj : zone.getObjects())
		{
			effects.clear();
			if (obj instanceof Card) {
				Card c = (Card) obj;
				if (c.getController(this) == _activePlayer)
					effects.addAll(c.getTriggeredAbilities(this, Event.BegOfYourDrawStep, origin));
				effects.addAll(c.getTriggeredAbilities(this, Event.BegOfEachDrawStep, origin));
				queueTriggeredAbilities(effects, c);	
			}
			else if (obj instanceof Emblem) {
				Emblem e = (Emblem) obj;
				if (e.getController(this) == _activePlayer)
					effects.addAll(e.getContinuousEffet().getTriggeredAbilities(this, Event.BegOfYourDrawStep));
				effects.addAll(e.getContinuousEffet().getTriggeredAbilities(this, Event.BegOfEachDrawStep));
				queueTriggeredAbilities(effects, e.getSource());
			}
		}
	}
	
	private void queueBOCeffectsZone(Zone zone, Origin origin) {
		Vector<TriggeredAbility> effects = new Vector<TriggeredAbility>();
		for (MtgObject obj : zone.getObjects()) {
			effects.clear();
			if (obj instanceof Card) {
				Card c = (Card) obj;
				if (c.getController(this) == _activePlayer)
					effects.addAll(c.getTriggeredAbilities(this, Event.BegOfYourCombatStep, origin));
				queueTriggeredAbilities(effects, c);	
			}
		}
	}
	
	private void queueEOTeffectsZone(Zone zone, Origin origin) {
		Vector<TriggeredAbility> effects = new Vector<TriggeredAbility>();
		for (MtgObject obj : zone.getObjects())
		{
			effects.clear();
			Card c = (Card) obj;
			if (c.getController(this) == _activePlayer)
				effects.addAll(c.getTriggeredAbilities(this, Event.BegOfYourEndStep, origin));
			effects.addAll(c.getTriggeredAbilities(this, Event.BegOfEachEndStep, origin)); 
			queueTriggeredAbilities(effects, c);
		}
	}
	
	public void queuePlayLandEffects(Player p, Card land) {
		Vector<TriggeredAbility> effects = new Vector<TriggeredAbility>();
		
		// From permanents
		for (Card c : _battlefield.getPermanents()) {
			if (c.getController(this) != p) {
				if (!land.hasSupertype(Supertype.BASIC))
					effects.addAll(c.getTriggeredAbilities(this, Event.AnOpponentPlaysNonbasicLand, Origin.BTLFLD)); // nonbasic land	
				effects.addAll(c.getTriggeredAbilities(this, Event.AnOpponentPlaysLand, Origin.BTLFLD)); //any land
			}
		}
		queueTriggeredAbilities(effects, land);
	}
	
	public void queueSagaEffects(Card source) {
		Vector<TriggeredAbility> effects = new Vector<TriggeredAbility>();
		int nbLoreCounters = source.getNbCountersOfType(this, CounterType.LORE);
		for (TriggeredAbility ta : source.getTriggeredAbilities(this)) {
			if (Integer.parseInt(ta.getParameter()) == nbLoreCounters)
				effects.add(ta);
		}
		queueTriggeredAbilities(effects, source);
	}
	
	public void queueExploresEffects(Card source) {
		Vector<TriggeredAbility> effects = new Vector<TriggeredAbility>();

		// From permanents
		for (MtgObject obj : _battlefield.getObjects()) {
			Card c = (Card) obj;
			if (c.getController(this) == source.getController(this)) {
				effects.addAll(c.getTriggeredAbilities(this, Event.ACreatureYouControlExplores, Origin.BTLFLD));
			}
		}
		queueTriggeredAbilities(effects, source);
	}
	
	public void queueGainLifeEffects(Player p, int amount) {
		Event event;
		Vector<TriggeredAbility> effects = new Vector<TriggeredAbility>();

		// From permanents
		for (MtgObject obj : _battlefield.getObjects()) {
			Card c = (Card) obj;
			if (c.getController(this) == p)
				event = Event.YouGainLife;
			else
				event = Event.AnOpponentGainsLife;
			effects.addAll(c.getTriggeredAbilities(this, event, Origin.BTLFLD));
		}
		
		// From cards in graveyards
		for (Player player : _players) {
			for (MtgObject obj : player.getGraveyard().getObjects()) {
				Card c = (Card) obj;
				if (c.getOwner() == p)
					event = Event.YouGainLife;
				else
					event = Event.AnOpponentGainsLife;
				effects.addAll(c.getTriggeredAbilities(this, event, Origin.GRVYRD));
			}	
		}
		queueTriggeredAbilities(effects, amount);
	}
	
	public void queueSacrificeClue(Player p) {
		Vector<TriggeredAbility> effects;
		for (MtgObject obj : _battlefield.getObjects()) {
			Card c = (Card) obj;
			if (c.getController(this) != p)
				continue;
			effects = c.getTriggeredAbilities(this, Event.SacrificeClue, Origin.BTLFLD); 
			queueTriggeredAbilities(effects, c);
		}
	}
	
	/**
	 * This will queue triggered abilities that are generated from continuous effects. Then, it will remove the continuous effect.
	 * @param event
	 * @param additionalData
	 */
	private void queueAndRemoveTriggersFromContinuousEffect(Event event, /*Object additionalData,*/ boolean bBothPlayers) {
		Object additionalData;
		Vector<TriggeredAbility> effects;
		int i = 0;
		ContinuousEffect ce;
		while (i < _continuousEffects.size())
		{
			ce = _continuousEffects.get(i);
			
			if (bBothPlayers || (ce.getController(this) == _activePlayer))
			{
				if ((ce.getActiveZone() == null))
				{
					effects = ce.getTriggeredAbilities(this, event);
					if ((effects != null) && (effects.size() > 0))
					{
						additionalData = ce.getAdditionalData();
						queueTriggeredAbilities(effects, additionalData);
						// Remove the continuous effect after the ability has been triggered
						_continuousEffects.remove(ce);
						i--;
					}
				}	
			}
			i++;
		}
	}
	
	private void queueAndRemoveTriggersFromContinuousEffect_2(Event event, Object additionalData, boolean bBothPlayers) {
		Vector<TriggeredAbility> effects;
		int i = 0;
		ContinuousEffect ce;
		while (i < _continuousEffects.size())
		{
			ce = _continuousEffects.get(i);
			
			if (bBothPlayers || (ce.getController(this) == _activePlayer))
			{
				if ((ce.getActiveZone() == null))
				{
					effects = ce.getTriggeredAbilities(this, event);
					if ((effects != null) && (effects.size() > 0))
					{
						queueTriggeredAbilities(effects, additionalData);
						// Remove the continuous effect after the ability has been triggered
						_continuousEffects.remove(ce);
						i--;
					}
				}	
			}
			i++;
		}
	}
	
	private void advanceSagas() {
		for (Card permanent : _battlefield.getEnchantmentsControlledBy(_activePlayer)) {
			if (permanent.hasSubtypePrinted(Subtype.SAGA))
				permanent.addCounter(this, CounterType.LORE, 1);
		}
	}
	
	private Response mainPhase() {
		// Put a lore counter on sagas if it's the precombat main phase
		if (_phase == Phase.PreMain)
			advanceSagas();
		
		/* Queue triggered effects from permanents */
		queueBOMPeffectsZone(_battlefield, Origin.BTLFLD);
		
		if (_queuedTriggeredAbilities.size() > 0) {
			checkStateBasedActions();
			Response ret = putTriggeredAbilitiesOnStack(_queuedTriggeredAbilities);
			if (ret == Response.MoreStep)
				return Response.MoreStep;
		}
		return Response.OK;
	}
	
	private Response upkeepStep() {
		/* Queue triggered effects from permanents */
		queueBOUeffectsZone(_battlefield, Origin.BTLFLD);
		
		/* Queue triggered effects from cards in graveyards */
		queueBOUeffectsZone(_activePlayer.getGraveyard(), Origin.GRVYRD);
		
		/* Queue triggered effects from cards in exiles */
		queueBOUeffectsZone(_activePlayer.getExile(), Origin.EXILE);
		
		/* Queue triggered effects from objects in command zones */
		queueBOUeffectsZone(_activePlayer.getCommand(), Origin.COMMAND);
		
		/* Queue triggered effects from continuous effects (i.e. delayed triggered abilities)*/
		queueAndRemoveTriggersFromContinuousEffect(Event.BegOfEachUpkeep, true);
		queueAndRemoveTriggersFromContinuousEffect(Event.BegOfYourUpkeep, false);
		
		if (_queuedTriggeredAbilities.size() > 0) {
			checkStateBasedActions();
			Response ret = putTriggeredAbilitiesOnStack(_queuedTriggeredAbilities);
			if (ret == Response.MoreStep)
				return Response.MoreStep;
		}
		return Response.OK;
	}
	
	private void endStep() {
		/* Queue triggered abilities from permanents */
		queueEOTeffectsZone(_battlefield, Origin.BTLFLD);
		
		/* Trigger monarch extra draw */
		if (_monarch != null)
			queueEOTeffectsZone(_monarch.getCommand(), Origin.COMMAND);
		
		/* Queue triggered abilities from continuous effects */
		queueAndRemoveTriggersFromContinuousEffect(Event.BegOfEachEndStep, true);
		queueAndRemoveTriggersFromContinuousEffect(Event.BegOfYourEndStep, false);

		if (_queuedTriggeredAbilities.size() > 0) {
			checkStateBasedActions();
			putTriggeredAbilitiesOnStack(_queuedTriggeredAbilities);
		}
	}
	
	private Response drawStep() {
		/* Queue triggered effects from permanents */
		queueBODeffectsZone(_battlefield, Origin.BTLFLD);
		return drawCards(_activePlayer, 1);
	}

	public boolean isMainPhase() {
		return (_phase == Phase.PreMain) || (_phase == Phase.PostMain);
	}
	
	private Response suspend(Card c) {
		int nbTimeCounters =  Integer.parseInt(c.getStaticAbility(StaticAbility.SUSPEND).getParameter());
		
		this.move_HND_to_EXL(c);
		c.addCounter(this, CounterType.TIME, nbTimeCounters);
		return Response.OKholdPriority;
	}
	
	/**
	 * Handle special actions here
	 * @param idCard
	 * @param action
	 * @return
	 */
	public Response takeSpecialAction(int idCard, SpecialAction action) {
		Card card;

		switch(action.getOption()) {
		case PLAY_LAND:
			card = (Card) _playerWithPriority.getHand().getObjectByID(idCard);
			if (card == null)
				return Response.ErrorInvalidCardNumber;
			return playLand(card, Zone.Name.Hand);
			
		case SUSPEND:
			card = (Card) _playerWithPriority.getHand().getObjectByID(idCard);
			if (card == null)
				return Response.ErrorInvalidCardNumber;
			return suspend(card);
			
		case TURN_FACEUP:
			card = (Card) _battlefield.getObjectByID(idCard);
			if ((card == null) || (card.getController(this) != _playerWithPriority) || (card.getFaceUp() == null))
				return Response.ErrorInvalidCardNumber;
			
			boolean bMega = card.getFaceUp().hasStaticAbility(StaticAbility.MEGAMORPH);
			StaticAbilityEffect.morph(this, card, bMega);
			return Response.OKholdPriority;
			
		default:
			return Response.OK;
		}
	}
	
	/**
	 * Cast a spell
	 * @param i
	 * @return
	 */
	public Response castSpell(int cardID, SpellCast playMode) {
		Response ret;

		Card card = (Card) _playerWithPriority.getHand().getObjectByID(cardID);
		if (card == null)
		{
			// card is not in hand. Check if it is in graveyard (for flashback spells)
			card = (Card) _playerWithPriority.getGraveyard().getObjectByID(cardID);
			if (card == null)
			{
				// card is not in graveyard either. Check if it is in exile (for rebound spells)
				card = (Card) _playerWithPriority.getExile().getObjectByID(cardID);
				if (card == null)
				{
					return Response.ErrorInvalidCardNumber;
				}
			}
		}
		
		if (!card.getSpellCastOptions().contains(playMode))
			return Response.ErrorInvalidPlayMode;
		
		if (!card.checkRestrictions(this))
			return Response.ErrorCannotPayCost;
		
		// Check that caster is allowed to cast this spell
		if ((playMode.getOption() != Option.CAST_FACEDOWN) && !_playerWithPriority.canCastSpell(card))
			return Response.ErrorForbiddenSpell;	
		card.setSpellCastOptionUsed(playMode);
		_topStackObject = card;
		// TODO : deduce the mana cost of the spell from the mana pool
		ret = resolvableCastCheckTiming();
		return ret;
	}
	
	// Step 1 : check timing
	public Response resolvableCastCheckTiming() {
		Response ret;
		
		if (_bDebugMode) {
			ret = Response.OK;
		}
		// ignore timimg restrictions in case of cast a spell as part of the resolution of an ability (i.e. Spell Queller)
		else if ((_topStackObject instanceof Card) && (((Card) _topStackObject).getSpellCastUsed().getWithoutPayingManaCost() == true)) {
			ret = Response.OK;
		}
		// ignore timing restrictions in case of a spell cast with Rebound
		else if ((_topStackObject instanceof Card) && (((Card)_topStackObject).getSpellCastUsed().getOption() == Option.CAST_FROM_EXILE)) { 
			ret = Response.OK;
		}
		// ignore timing restrictions in case of the copy of a spell
		else if ((_topStackObject instanceof Card) && ((Card)_topStackObject).isCopy()) {
			ret = Response.OK;
		}
		else
		{
			if (!_topStackObject.isSorcerySpeed(this)) {
				ret = Response.OK;
			}
			else {
				if (_topStackObject.getController(this) != _activePlayer)
					ret = Response.ErrorNotMyTurn;
				else if (_step != Step.Main)
					ret = Response.ErrorNotMainPhase;
				else if (!_stack.isEmpty())
					ret = Response.ErrorStackNotEmpty;
				else
					ret = Response.OK;
			}
		}
		
		// All timing checks are OK
		if (ret == Response.OK)
		{
			if (_topStackObject instanceof Card) {
				Card card = (Card) _topStackObject;
				SpellCast playMode = card.getSpellCastUsed();
				
				// Playing the card face-down
				if (playMode.getOption() == Option.CAST_FACEDOWN) {
					// 1. exile card from hand and remove it completely
					card.getOwner().getHand().removeObject(card);
					
					// 2 .create facedown version
					_topStackObject = card.createFaceDownVersion(this);
					_topStackObject.getController(this).getHand().addObject(_topStackObject);
				}
				// playing the card using an alternate cost (i.e. Force of Will)
				else if (playMode.getOption() == Option.CAST_WITH_ALTERNATE_COST) {
					// check that the cost can be paid
					_currentAC = playMode.getAlternateCost();
					if (!_currentAC.canBePaid(this, card.getController(this)))
						return Response.ErrorCannotPayCost;
				}
			}
			ret = resolvableCastXValue();
		}
		else {
			_topStackObject.getController(this).setState(State.Ready);
			if (!_stack.isEmpty())
				_topStackObject = _stack.getTopObject();
		}
		return ret;
	}
	
	// Step 2a : prompt for X value
	private Response resolvableCastXValue() {
		if (!_topStackObject.hasXValue())
			return resolvableCastModes();
		
		if (_topStackObject instanceof Card)
		{
			// In case of a spell card, only prompt X if the spell was cast from hand (i.e. not rebound)
			if (((Card) _topStackObject).getSpellCastUsed().getOption() == Option.CAST)
				_topStackObject.getController(this).setState(State.PromptXValue);
			else
				return resolvableCastModes();
		}
		else
		{
			_topStackObject.getController(this).setState(State.PromptXValue);
		}
		return Response.OK;
	}
	
	// Step 2b : prompt for mode or modes
	private Response resolvableCastModes() {
		if (_topStackObject.isModal())
			_topStackObject.getController(this).setState(State.PromptMode);
		else
			return resolvableCastTargeting();
		return Response.OK;
	}
	
	// Step 3 : prompt for target(s)
	private Response resolvableCastTargeting() {
		Player controller = _topStackObject.getController(this);
		Card spell;
		
		// If a spell was cast with Awaken, add a target requirement for the land
		if (_topStackObject instanceof Card) {
			spell = (Card) _topStackObject;
			if (spell.getSpellCastUsed().getOption() == SpellCast.Option.CAST_WITH_AWAKEN)
				spell.addTargetRequirement(Category.LandYouControl);
		}
		
		if (_topStackObject.hasTargetRequirements())
		{
			if (_topStackObject.hasLegalTargets(this) == false) // there are no legal targets
			{
				// If a spell was cast with Awaken, remove a target requirement for the land
				if (_topStackObject instanceof Card) {
					spell = (Card) _topStackObject;
					if (spell.getSpellCastUsed().getOption() == SpellCast.Option.CAST_WITH_AWAKEN) {
						spell.removeTargetRequirements(Category.LandYouControl);
					}
				}
				return Response.ErrorNoLegalTarget;
			}
			else {
				Cardinality cardinality = _topStackObject.getTargetRequirements().get(_iTargetReq).getCardinality();
				
				switch (cardinality) {
				case UP_TO_ONE:
				case UP_TO_TWO:
				case UP_TO_THREE:
				case UP_TO_FOUR:
				case UP_TO_X:
					controller.setState(State.PromptTargetsOrDone);
					break;
					
				case X:
					if (_topStackObject.getXValue() == 0)
						return resolvableCastPayCosts();
					
				default:
					controller.setState(State.PromptTargets);
					return Response.MoreStep;
					//break;
				}
			}
		}
		else
			return resolvableCastPayCosts();
		return Response.OK;
	}

	// Step 4 : pay costs
	private Response resolvableCastPayCosts() {
		// Deal with alternate cost payments like Force of Will
		if (_topStackObject instanceof Card) {
			Card card = (Card) _topStackObject;
			SpellCast playMode = card.getSpellCastUsed();
			if (playMode.getOption() == Option.CAST_WITH_ALTERNATE_COST) {
				// check that the hasn't already been paid
				_currentAC = playMode.getAlternateCost();
				if (!_currentAC.wasPaid())
					if (_currentAC.pay(this, card.getController(this)) == Response.MoreStep)
						return Response.MoreStep;
			}
		}
		
		
		if ((_topStackObject instanceof ActivatedAbility) || (_topStackObject instanceof Card))
		{
			Card source = _topStackObject.getSource();
			Cost cost = _topStackObject.getCost();
			Player controller = _topStackObject.getController(this);
			
			// tap the permanent source if applicable
			if (cost.requiresAdditionalCost(Requirement.TAP_THIS)) {
				if (source.hasSubtypeGlobal(this, Subtype.ISLAND)) {
					for (ContinuousEffect ce : _continuousEffects) {
						Vector<TriggeredAbility> effects;
						effects = ce.getTriggeredAbilities(this, Event.APlayerTapsIslandForMana); 
						queueTriggeredAbilities(effects, source);
					}
				}
				else if (source.hasSubtypeGlobal(this, Subtype.FOREST)) {
					for (ContinuousEffect ce : _continuousEffects) {
						Vector<TriggeredAbility> effects;
						effects = ce.getTriggeredAbilities(this, Event.APlayerTapsForestForMana); 
						queueTriggeredAbilities(effects, source);
					}
				}
				source.tap(this);
			}

			// sacrifice the permanent source if applicable
			if (cost.requiresAdditionalCost(Requirement.SACRIFICE_THIS))
				sacrifice(controller, source);
			
			// return the permanent source to its owner's hand if applicable
			if (cost.requiresAdditionalCost(Requirement.RETURN_THIS_TO_HAND))
				move_BFD_to_HND(source);
			
			// discard the card (i.e. Cycling)
			if (cost.requiresAdditionalCost(Requirement.DISCARD_THIS))
				discard(source);
			
			// tap an additional creature you control if applicable (i.e. Loam Dryad)
			if (cost.requiresAdditionalCost(Requirement.TAP_AN_UNTAPPED_CREATURE_YOU_CONTROL)) {
				controller.setState(State.WaitPayCostTapUntappedCreature);
				return Response.MoreStep;
			}
			
			// Crew
			if (cost.requiresAdditionalCost(Requirement.CREW)) {
				controller.setState(State.WaitPayCostCrew);
				return Response.MoreStep;
			}
			
			// sacrifice creature if applicable (i.e. Nantuko Husk)
			if (cost.requiresAdditionalCost(Requirement.SACRIFICE_A_CREATURE)) {
				controller.setState(State.WaitPayCostSacrificeCreature);
				return Response.MoreStep;
			}

			// sacrifice 5creatures if applicable (Westvale Abbey)
			if (cost.requiresAdditionalCost(Requirement.SACRIFICE_FIVE_CREATURES)) {
				controller.setState(State.WaitPayCostSacrificeCreature); // TODO : 5 creatures, not one
				return Response.MoreStep;
			}
		
			// sacrifice a Goblin if applicable (i.e. Siege-Gang Commander)
			if (cost.requiresAdditionalCost(Requirement.SACRIFICE_A_GOBLIN)) {
				controller.setState(State.WaitPayCostSacrificeGoblin);
				return Response.MoreStep;
			}

			// sacrifice another Vampire or Zombie (Kalitas)
			if (cost.requiresAdditionalCost(Requirement.SACRIFICE_ANOTHER_VAMPIRE_OR_ZOMBIE)) {
				controller.setState(State.WaitPayCostSacrificeAnotherVampireOrZombie);
				return Response.MoreStep;
			}
			
			// sacrifice artifact if applicable (i.e. Arcbound Ravager)
			if (cost.requiresAdditionalCost(Requirement.SACRIFICE_AN_ARTIFACT)) {
				controller.setState(State.WaitPayCostSacrificeArtifact);
				return Response.MoreStep;
			}
			
			// sacrifice enchantment if applicable (i.e. Faith Healer)
			if (cost.requiresAdditionalCost(Requirement.SACRIFICE_AN_ENCHANTMENT)) {
				controller.setState(State.WaitPayCostSacrificeEnchantment);
				return Response.MoreStep;
			}
			
			// sacrifice land if permanent (Barrin)
			if (cost.requiresAdditionalCost(Requirement.SACRIFICE_A_PERMANENT)) {
				controller.setState(State.WaitPayCostSacrificePermanent);
				return Response.MoreStep;
			}
			
			// Exile another creature card from your graveyard
			if (cost.requiresAdditionalCost(Requirement.EXILE_ANOTHER_CREATURE_CARD_FROM_GYD)) {
				controller.setState(State.WaitPayCostExileAnotherCreatureCardFromGyd);
				return Response.MoreStep;
			}
			
			// discard a creature card if applicable (i.e. Fauna Shaman)
			if (cost.requiresAdditionalCost(Requirement.DISCARD_A_CREATURE_CARD)) {
				controller.setState(State.WaitPayCostDiscardACreatureCard);
				return Response.MoreStep;
			}

			// discard a card if applicable
			if (cost.requiresAdditionalCost(Requirement.DISCARD_A_CARD)) {
				controller.setState(State.WaitPayCostDiscardACard);
				return Response.MoreStep;
			}

			// sacrifice land if applicable (i.e. Dust Bowl)
			if (cost.requiresAdditionalCost(Requirement.SACRIFICE_A_LAND)) {
				controller.setState(State.WaitPayCostSacrificeLand);
				return Response.MoreStep;
			}
			
			// sacrifice a Forest or Plains if applicable
			if (cost.requiresAdditionalCost(Requirement.SACRIFICE_A_FOREST_OR_PLAINS)) {
				controller.setState(State.WaitPayCostSacrificeForestOrPlains);
				return Response.MoreStep;
			}
			
			// sacrifice a Forest
			if (cost.requiresAdditionalCost(Requirement.SACRIFICE_A_FOREST)) {
				controller.setState(State.WaitPayCostSacrificeForest);
				return Response.MoreStep;
			}
			
			// return land if applicable (i.e. Meloku)
			if (cost.requiresAdditionalCost(Requirement.RETURN_A_LAND_YOU_CONTROL)) {
				controller.setState(State.WaitPayCostReturnLand);
				return Response.MoreStep;
			}

			// return Elf
			if (cost.requiresAdditionalCost(Requirement.RETURN_AN_ELF_YOU_CONTROL)) {
				controller.setState(State.WaitPayCostReturnElf);
				return Response.MoreStep;
			}
			
			// Remove a loyalty counter from a Planeswalker you control (i.e. Heart of Kiran)
			if (cost.requiresAdditionalCost(Requirement.REMOVE_A_LOYALTY_COUNTER)) {
				controller.setState(State.WaitHeartOfKiran);
				return Response.MoreStep;
			}
			
			// Remove a fade counter (i.e. Saproling Burst)
			if (cost.requiresAdditionalCost(Requirement.REMOVE_A_FADE_COUNTER))
				source.removeCounter(this, CounterType.FADE, 1);

			// Remove a +1/+1 counter (i.e. Spike Feeder)
			if (cost.requiresAdditionalCost(Requirement.REMOVE_A_PLUS1_COUNTER))
				source.removeCounter(this, CounterType.PLUS_ONE, 1);
			
			// Remove a charge counter (i.e. Umezawa's Jitte)
			if (cost.requiresAdditionalCost(Requirement.REMOVE_A_CHARGE_COUNTER))
				source.removeCounter(this, CounterType.CHARGE, 1);

			// Remove a mining counter (Gemstone Mine)
			if (cost.requiresAdditionalCost(Requirement.REMOVE_A_MINING_COUNTER))
				source.removeCounter(this, CounterType.MINING, 1);
			
			// Pay 1 Energy
			if (cost.requiresAdditionalCost(Requirement.PAY_1_ENERGY))
				controller.payEnergy(1);
			
			// Pay 2 Energy
			if (cost.requiresAdditionalCost(Requirement.PAY_2_ENERGY))
				controller.payEnergy(2);
			
			// Pay 3 Energy
			if (cost.requiresAdditionalCost(Requirement.PAY_3_ENERGY))
				controller.payEnergy(3);
			
			// Pay 5 Energy
			if (cost.requiresAdditionalCost(Requirement.PAY_5_ENERGY))
				controller.payEnergy(5);
			
			// Pay 1 life
			if (cost.requiresAdditionalCost(Requirement.PAY_1_LIFE))
				controller.loseLife(1);
			
			// Pay 2 life
			if (cost.requiresAdditionalCost(Requirement.PAY_2_LIFE))
				controller.loseLife(2);

			// Pay 7 life
			if (cost.requiresAdditionalCost(Requirement.PAY_7_LIFE))
				controller.loseLife(7);
			
			// Pay 8 life
			if (cost.requiresAdditionalCost(Requirement.PAY_8_LIFE))
				controller.loseLife(8);
			
			// Pay X life
			if (cost.requiresAdditionalCost(Requirement.PAY_X_LIFE))
				controller.loseLife(_topStackObject.getXValue());
			
			// Pay half your life rounded up
			if (cost.requiresAdditionalCost(Requirement.PAY_HALF_LIFE)) {
				int life = controller.getLife();
				int payAmount;
				
				if (life <= 0)
					payAmount = 0;
				else {
					if (life % 2 == 0) // even
						payAmount = life / 2;
					else // odd
						payAmount = (life + 1) / 2;
				}
				controller.loseLife(payAmount);
			}
			
			// Put a -0/-1 counter
			if (cost.requiresAdditionalCost(Requirement.PUT_A_MINUS0_MINUS1_COUNTER)) {
				source.addCounter(this, CounterType.MINUS_ZERO_MINUS_ONE, 1);
			}
			
			// if it's a loyalty ability, remove corresponding number of counters
			if ((_topStackObject instanceof ActivatedAbility) && ((ActivatedAbility) _topStackObject).isLoyalty()) {
				ActivatedAbility aa = (ActivatedAbility) _topStackObject;
				Card planeswalker = aa.getSource();
				LoyaltyCost lc = aa.getCost().getLoyaltyCost();
				
				// if it's a "Put" cost
				if (lc.getType() == Type.Put)
				{
					planeswalker.increaseLoyalty(this, lc.getNumber());
				}
				else // it's a "Remove" cost
				{
					// if it's a -X
					if (lc.isXvalue())
						planeswalker.decreaseLoyalty(this, aa.getXValue());
					else // it's a number
						planeswalker.decreaseLoyalty(this, lc.getNumber());
				}
			}
		}
		return resolvableFinalize();
	}
	
	/**
	 * 
	 * @return
	 */
	private Response resolvableFinalize() {
		Response ret;

		// Only mana abilities (activated and triggered) bypass the stack
		if ((_topStackObject instanceof TriggeredAbility || _topStackObject instanceof ActivatedAbility) && (((ManaAbility) _topStackObject).isManaAbility())) {
			ret = _topStackObject.doEffect(this);
			if (ret == Response.OK)
				ret = Response.OKholdPriority;	
		}
		else {
			ret = resolvableCastPutOnStack();
		}
		return ret;
	}
	
	/* Trigger abilities that trigger whenever a spell is cast  */
	private void queueCastAspellEffects() {
		queueCastAspelleffectsFromZone(_battlefield);
		queueCastAspelleffectsFromZone(_stack);
		for (Player p : _players) {
			queueCastAspelleffectsFromZone(p.getGraveyard());
			
			// Emblems (in command zone)
			for (MtgObject obj : p.getCommand().getObjects()) {
				if (obj instanceof Emblem) {
					Emblem e = (Emblem) obj;
					if (e.getController(this) == _topStackObject.getController(this)) {
						Vector<TriggeredAbility> ta = e.getContinuousEffet().getTriggeredAbilities(this, Event.YouCastASpell);
						if (ta != null)
							queueTriggeredAbilities(ta, e);
					}
				}
			}
		}
		
		if ((((Card) _topStackObject).isSorceryCard() || ((Card) _topStackObject).isInstantCard()) &&
			(_topStackObject.getController(this) == _activePlayer))
			queueAndRemoveTriggersFromContinuousEffect_2(Event.YouCastYourNextInstantOrSorcerySpellThisTurn, _topStackObject, false);
	}
	
	// Step 5 : put on stack
	private Response resolvableCastPutOnStack() {
		Zone fromZone;
		Card card;
		Player controller = _topStackObject.getController(this);
		
		// in case of a spell
		if (_topStackObject instanceof Card) {
			card = (Card) _topStackObject;
			
			if (card.isCopy())
			{
				addStackObject(card);
			}
			else
			{
				fromZone = card.getZone(this);
				switchZone(fromZone, card, _stack);
				controller.incrementNbSpellsCastThisTurn();
				if (card.isCreatureCard())
					controller.incrementNbCreatureSpellsCastThisTurn();  // This is for Vengevine
				queueCastAspellEffects();
			}
		}
		else // in case of an activated or triggered ability
		{
			addStackObject(_topStackObject);
		}
	
		_nbPlayersWhoPassed = 0;
		if ((_topStackObject instanceof Card) || (_topStackObject instanceof ActivatedAbility))
			return givePriority(controller);
		else
			return givePriority(_activePlayer);
	}
	
	/**
	 * Play a land
	 * @param fromZone TODO
	 * @param i
	 * @return
	 */
	public Response playLand(Card card, Name fromZone) {
		Response ret;

		if (!card.isLandCard())
			return Response.ErrorNotALand;
		
		// Disable land play restrictions when in debug mode
		if (!_bDebugMode) {
			if (_playerWithPriority != _activePlayer)
				return Response.ErrorNotMyTurn;
			if (!isMainPhase())
				return Response.ErrorNotMainPhase;
			if (!_stack.isEmpty())
				return Response.ErrorStackNotEmpty;
			if (_activePlayer.getNbLandsPlayed() >= getMaxNbLands(_activePlayer, DEFAULT_MAX_NB_LANDS))
				return Response.ErrorTooManyLandDrops;
		}

		_activePlayer.setNbLandsPlayed(_activePlayer.getNbLandsPlayed() + 1);
		Zone oldZone;
		if (fromZone == Name.Hand)
			oldZone = _activePlayer.getHand();
		else if (fromZone == Name.Library)
			oldZone = _activePlayer.getLibrary();
		else
			return Response.ErrorIncorrectZone;
		
		// queue effects that trigger when playing a land
		queuePlayLandEffects(card.getController(this), card);
		
		ret = switchZone(oldZone, card, _battlefield);
		if (ret == Response.OK)
			ret = Response.OKholdPriority;
		if (ret != Response.MoreStep)
			givePriority(_activePlayer);
		return ret;
	}
	
	public void queueDamageDealtTriggeredAbilities(Vector<TriggeredAbility> abs, Object additionalData) {
		queueTriggeredAbilities(abs, additionalData);
	}
	
	private void queueCastAspelleffectsFromZone(Zone zone) {
		Vector<TriggeredAbility> CastAspellEffects = new Vector<TriggeredAbility>();
		Card c;
		Card triggeringSpell = (Card) _topStackObject;
		Origin origin = Origin.BTLFLD;
		
		if (zone.getName() == Name.Graveyard)
			origin = Origin.GRVYRD;
		
		if (zone.getName() == Name.Stack)
			origin = Origin.STACK;
		
		for (MtgObject obj : zone.getObjects()) {
			if (!(obj instanceof Card))
				continue;
			c = (Card) obj;
			CastAspellEffects.clear();
			
			// Triggers whenever any player casts a spell
			CastAspellEffects.addAll(c.getTriggeredAbilities(this, Event.AnyPlayerCastASpell, origin));
			if (triggeringSpell.hasColor(Color.GREEN))
				CastAspellEffects.addAll(c.getTriggeredAbilities(this, Event.AnyPlayerCastAGreenSpell, origin));
			if (triggeringSpell.isCreatureCard()) {
				CastAspellEffects.addAll(c.getTriggeredAbilities(this, Event.AnyPlayerCastACreatureSpell, origin));
				endSoulSculptedEffects();
			}
			if (triggeringSpell.isEnchantmentCard())
				CastAspellEffects.addAll(c.getTriggeredAbilities(this, Event.AnyPlayerCastAnEnchantSpell, origin));
			
			// Triggers whenever the controller casts a spell
			if (c.getController(this) == triggeringSpell.getController(this)) {
				// Cast this spell (Cascade mostly)
				if (triggeringSpell == c)
					CastAspellEffects.addAll(c.getTriggeredAbilities(this, Event.YouCastThisSpell, origin));
				
				// Cast a spell
				CastAspellEffects.addAll(c.getTriggeredAbilities(this, Event.YouCastASpell, origin));
				
				// Cast a creature/non-creature trigger (Primordial Sage / Prowess)
				if (triggeringSpell.isCreatureCard())
					CastAspellEffects.addAll(c.getTriggeredAbilities(this, Event.YouCastCreatureSpell, origin));
				else {
					CastAspellEffects.addAll(c.getTriggeredAbilities(this, Event.YouCastNonCreatureSpell, origin));
				}
				
				// Cast an enchantment triggers (i.e. Argothian Enchantress)
				if (triggeringSpell.isEnchantmentCard())
					CastAspellEffects.addAll(c.getTriggeredAbilities(this, Event.YouCastEnchantmentSpell, origin));
				
				// Cast an instant or sorcery trigger (i.e. Young Pyromancer)
				if (triggeringSpell.isInstantCard() || triggeringSpell.isSorceryCard()) {
					CastAspellEffects.addAll(c.getTriggeredAbilities(this, Event.YouCastInstantOrSorcerySpell, origin));
					CastAspellEffects.addAll(c.getTriggeredAbilities(this, Event.YouCastYourNextInstantOrSorcerySpellThisTurn, origin));
				}
			}
			// Triggers when the opponent casts a spell (i.e. Urza's Saga "Sleeping enchantments")
			else {
				CastAspellEffects.addAll(c.getTriggeredAbilities(this, Event.AnOpponentCastSpell, origin));
				
				// Opponent cast a white spell
				if (triggeringSpell.hasColor(Color.WHITE))
					CastAspellEffects.addAll(c.getTriggeredAbilities(this, Event.AnOpponentCastWhiteSpell, origin));
				
				// Opponent cast an enchantment
				if (triggeringSpell.isEnchantmentCard())
					CastAspellEffects.addAll(c.getTriggeredAbilities(this, Event.AnOpponentCastEnchantSpell, origin));

				// Opponent cast an artifact
				if (triggeringSpell.isArtifactCard())
					CastAspellEffects.addAll(c.getTriggeredAbilities(this, Event.AnOpponentCastArtifactSpell, origin));

				// Opponent cast a creature
				if (triggeringSpell.isCreatureCard()) {
					CastAspellEffects.addAll(c.getTriggeredAbilities(this, Event.AnOpponentCastCreatureSpell, origin));

					// Opponent cast a creature with flying
					if (triggeringSpell.hasEvergreenGlobal(Evergreen.FLYING, this))
						CastAspellEffects.addAll(c.getTriggeredAbilities(this, Event.AnOpponentCastCreatureSpellFlying, origin));
				}
			}
			queueTriggeredAbilities(CastAspellEffects, triggeringSpell);	
		}
	}

	/**
	 * This method will stop the effects generated by Soul Sculptor. It is not a triggered ability.
	 */
	private void endSoulSculptedEffects() {
		ContinuousEffect toBeRemoved;
		Vector<Card> permanents = _battlefield.getPermanents();
		for (Card permanent : permanents)
		{
			toBeRemoved = null;
			Vector<ContinuousEffect> effects = permanent.getContinuousEffects(Name.Battlefield);
			for (ContinuousEffect ce : effects) {
				if (ce.getAbilityName().equals("soulSculpted"))
					toBeRemoved = ce;
			}
			if (toBeRemoved != null)
				permanent.removeContinuousEffects(toBeRemoved);
		}
	}

	/**
	 * Perform the requested action on the specified card
	 * @param idCard
	 * @param idAction
	 * @return
	 */
	public Response performAction(int idCard, int idAction) {
		PerformableAction action = findActionById(idAction);
		if (action == null)
			return Response.ErrorInvalidCardNumber;
		
		PerformableAction.Type actionType = action.getActionType();
		Response ret;
		
		switch (actionType) {
		case ACTIVATE_ABILITY:
			ret = activateAbility(idCard, (ActivatedAbility) action);
			if (ret == Response.MoreStep)
				return Response.MoreStep;
			break;
			
		case CAST_SPELL:
			ret = castSpell(idCard, (SpellCast) action);
			break;

		case TAKE_SPECIAL_ACTION:
			ret = takeSpecialAction(idCard, (SpecialAction) action);
			break;

		default:
			return Response.ErrorInvalidCommand;  // should never get here
		}
		if (!_queuedTriggeredAbilities.isEmpty())
			givePriority(_playerWithPriority);
		return ret;
	}
	
	/**
	 * 
	 * @param idAbility
	 * @return
	 */
	private Response activateAbility(int idCard, ActivatedAbility aa) {
		Response ret;
		
		if ((aa == null) || (aa.getSource().getID() != idCard)) // error with the source card
			return Response.ErrorInvalidCardNumber;
		aa.setController(this, aa.getSource().getController(this));
		aa.setOwner(aa.getSource().getController(this));
		ret = proposeActivatedAbility(aa);
		return ret;
	}
	
	public StackObject getTopStackObject() {
		return _topStackObject;
	}
	
	private Response proposeActivatedAbility(ActivatedAbility aa) {
		Response ret;
		
		_topStackObject = aa.clone();
		aa.increaseNbActivation();
		ret = resolvableCastCheckTiming();

//		if (ret == Response.OK) {
			if (aa.isLoyalty())
				aa.getSource().setAlreadyActivatedLoyaltyAbility();
	//	}
		return ret;
	}
	
	public Response assignTarget(Vector<Integer> choices) {
		Target.Category targetCategory;
		int chosenCardID;
		Response ret = Response.OK;
		Cardinality cardinality = _topStackObject.getTargetRequirements().get(_iTargetReq).getCardinality();
		
		if ((cardinality == Cardinality.UP_TO_ONE) || (cardinality == Cardinality.UP_TO_X)) {
			if (choices.get(0) == Game.COMMAND_DONE)
				return resolvableCastPayCosts();
		}
		
		for (int i = 0; i < choices.size(); i++)
		{
			targetCategory = _topStackObject.getTarget(i).getType();
			chosenCardID = choices.get(i);
			if (chosenCardID == Game.COMMAND_DONE) {
				if ((cardinality == Cardinality.UP_TO_THREE) || (cardinality == Cardinality.UP_TO_FOUR))
					return resolvableCastPayCosts();
				else
					break;
			}
			
			MtgObject chosenTarget = null;
			
			switch (targetCategory) {

			/* Graveyard */
			case CardInYourGraveyard:
			case LandCardInYourGraveyard:
			case EnchantmentCardInYourGraveyard:
			case CreatureCardInYourGraveyard:
			case InstantCardInYourGraveyard:
			case NonLgdryCreaCardInYourGydWithCMCX:
				chosenTarget = _topStackObject.getController(this).getGraveyard().getObjectByID(chosenCardID);
				break;
				
			case CardInAnyGraveyard:
			case LandCardInAnyGraveyard:
			case CreatureCardInAnyGraveyard:
			case PermanentCardWithCdMC3OrLessInYourGraveyard:
			case PermanentCardInYourGraveyard:
			case SorceryOrInstantCardInAnyGraveyard:
			case CreatureOrPlaneswalkerCardinAnyGraveyard:
				for (Player p : _players) {
					chosenTarget = p.getGraveyard().getObjectByID(chosenCardID);
					if (chosenTarget != null)
						break;
				}
				break;

			/* Permanents */
			case Permanent:
			case AnotherPermanent:
			case NonlandPermanent:
			case NonlandPermanentWithCMC3OrLess:
			case Land:
			case Forest:
			case Swamp:
			case LandYouControl:
			case NonbasicLand:
			case Artifact:
			case NonCreatureArtifact:
			case Enchantment:
			case AuraAttachedToAcreatureOrLand:
			case ArtifactOrEnchantment:
			case ArtifactOrEnchantmentOrLand:
			case ArtifactOrCreatureOrLand:
			case Creature:
			case NontokenCreature:
			case TokenCreature:
			case CreatureWithPower4orGreater:
			case GreenCreature:
			case WhiteCreature:
			case AttackingCreature:
			case BlockedCreature:
			case AttackingCreatureWithFlying:
			case AttackingCreatureWithoutFlying:
			case AttackingOrBlockingCreature:
			case ArtifactCreature:
			case LegendaryCreature:
			case BlinkmothCreature:
			case AnotherCreature:
			case CreatureAnOpponentControls:
			case CreatureOrPlaneswalker:
			case CreatureYouControl:
			case NonAngelCreatureYouControl:
			case AnotherPermanentYouControl:
			case PermanentYouControl:
			case NonArtifactNonBlackCreature:
			case NonBlackCreature:
			case LandOrNonBlackCreature:
			case MonocoloredCreature:
			case Planeswalker:
			case EnchantmentTappedArtifactOrTappedCreature:
				chosenTarget = _battlefield.getObjectByID(chosenCardID);
				break;

			/* Stack */
			case ActivatedAbility:
			case ActivatedOrTriggeredAbility:
			case SpellOrActivatedOrTriggeredAbility:
			case Spell:
			case SpellWithCMC4orLess:
			case CreatureSpell:
			case NonCreatureSpell:
			case RedSpell:
			case ArtifactOrEnchantmentSpell:
			case InstantOrSorcery:
				chosenTarget = _stack.getObjectByID(chosenCardID);
				break;
				
			/* Venser */
			case SpellOrPermanent:
				chosenTarget = _stack.getObjectByID(chosenCardID); // look for spell
				if (chosenTarget == null) // if not found, select permanent
					chosenTarget = _battlefield.getObjectByID(chosenCardID);
				break;

			/* Damageables */
			case PlayerOrPlaneswalker:
			case AnyTarget:
				chosenTarget = findPlayerByID(chosenCardID);
				if (chosenTarget == null)
					chosenTarget = _battlefield.getObjectByID(chosenCardID);
				break;
				
			case Opponent:
			case Player:
				chosenTarget = findPlayerByID(chosenCardID);
				break;
				
			default:
				chosenTarget = null;
				break;
			}
			
			if (Target.validate(this, targetCategory, chosenTarget, _topStackObject, Timing.ASSIGNMENT) == false)
				return Response.ErrorInvalidTarget;
			
			if (computeIsProtectedFrom(_topStackObject.getSource(), chosenTarget) == true)
				return Response.ErrorInvalidTarget;
			
			_topStackObject.addTarget(i, chosenTarget);
			if (chosenTarget instanceof Card) {
				Card targetCard = (Card) chosenTarget;
				queueTriggeredAbilities(targetCard.getTriggeredAbilities(this, Event.BecomesTheTargetOfSpellOrAb, Origin.BTLFLD), _topStackObject);
			}
		}
		
		_iTargetReq++;
		
		if (_topStackObject.allTargetsAssigned()) {
			_iTargetReq = 0;
			ret = resolvableCastPayCosts();
		}
		return ret;
	}
	
	/**
	 * This method is used when needing to assign a host to an Aura in the resolution of an effect (i.e. Zur)
	 * @param choices
	 * @return
	 */
	public Response assignHost(int chosenCardID) {
		Target.Category targetCategory;
		Response ret = Response.OK;
		
		targetCategory = _currentAura.getTarget(0).getType();
		MtgObject chosenTarget = null;
		
		switch (targetCategory) {

		/* Permanents */
		case Forest:
		case Swamp:
		case Creature:
			chosenTarget = _battlefield.getObjectByID(chosenCardID);
			break;

		case Opponent:
		case Player:
			chosenTarget = findPlayerByID(chosenCardID);
			break;
			
		default:
			chosenTarget = null;
			break;
		}
		
		if (Target.validate(this, targetCategory, chosenTarget, _currentAura, Timing.ASSIGNMENT) == false)
			return Response.ErrorInvalidTarget;
		
		_currentAura.addTarget(0, chosenTarget);
		resolveTopObject();
		return ret;
	}
	
	public Response addStackObject(StackObject so) {
		_stack.addObject(so);
		so.resetStep();
		return Response.OK;
	}
	
	public int getNbQueuedTriggeredManaAbilities() {
		return _queuedTriggeredManaAbilities.size();
	}
	
	public void queueTriggeredManaAbilities(Vector<TriggeredAbility> triggeredAbilities) {
		for (TriggeredAbility ta : triggeredAbilities)
		{
			_queuedTriggeredManaAbilities.add(ta);
		}
	}
	
	public Response doTriggeredManaAbilities() {
		Response ret;
		
		if (_queuedTriggeredManaAbilities.isEmpty())
			return Response.OKholdPriority;
		else
		{
			TriggeredAbility ta = _queuedTriggeredManaAbilities.get(0);
			ret = ta.doEffect(this);
			if (ret == Response.MoreStep)
				return Response.MoreStep;
			else if (ret == Response.OK) {
				_queuedTriggeredManaAbilities.remove(ta);
			}
			return doTriggeredManaAbilities();
		}
	}
	
	public void queueTriggeredAbilities(Vector<TriggeredAbility> triggeredAbilities, Object additionalData) {
		if ((triggeredAbilities == null) || (triggeredAbilities.size() == 0))
			return;

		for (TriggeredAbility ta : triggeredAbilities) {
				ta.setAdditionalData(additionalData);
			if (!ta.hasInterveningIfClause() || ta.validateInterveningIfClause(this)) {
				_queuedTriggeredAbilities.add(ta);
			}
		}
	}
	
	public Card getRevealLand() {
		return _revealLand;
	}
	
	/* Switch zone */
	public Response switchZone(Zone oldZone, Card card, Zone newZone) {
		Response ret;
		if (card == null)
			return Response.ErrorInvalidCardNumber;

		if (card.getZone(this) != oldZone)
			return Response.ErrorIncorrectZone;
		
		// If a spell cast with Flashback would leave the stack, put it in exile instead of anywhere else
		if ((oldZone == _stack) && (card.getSpellCastUsed().getOption() == Option.CAST_WITH_FLASHBACK)) {
			newZone = card.getOwner().getExile();
		}

		// If the card is a token, it cannot enter the battlefield (this is used for "Blink" effects)
		if (card.isToken() && (newZone == _battlefield))
			return Response.ErrorCardIsToken;
		
		boolean bReplacement = false;
		
		// Before looking at triggered abilities, check for replacement effects
		if (newZone.getName() == Zone.Name.Graveyard) {
			for (ContinuousEffect ce : _continuousEffects) {
				if (ce.replace_Dies(this, card) || ce.replace_PutIntoGraveyardFromAnywhere(this, card)) {
					bReplacement = true;
					ce.processReplacement(this, card);
				}
			}
		}
		
		// Replacement effect was applied
		if (bReplacement)
			return Response.OK;
		
		oldZone.removeObject(card);
		if (newZone == _stack) {
			card.zoneSwitched();
			return addStackObject(card);
		}

		/* Enter the battlefield */
		if (newZone == _battlefield)
		{
			if (card.isPlaneswalkerCard())
				card.getController(this).deployedPlaneswalker();
			
			// Look for static abilities that modify how a card enters the battlefield
			for (StaticAbility sa : card.getStaticAbilities()) {
				if (sa.isEntersBattlefield()) {
					_currentSA = sa;
					ret = sa.doEffect(this);
					if (ret == Response.MoreStep)
						return ret;
				}
			}
		}
		finishSwitchZone(oldZone, newZone, card);
		if (newZone == _battlefield)
			enterBattlefield(card);
		return Response.OK;
	}
	
	private void finishSwitchZone(Zone oldZone, Zone newZone, Card card) {
		newZone.addObject(card);
		/* Leaves the battlefield */
		if (oldZone == _battlefield)
			leaveBattlefield(newZone, card);

		/* Card was put into the graveyard from anywhere */
		if (newZone.getName() == Name.Graveyard) {
			queueTriggeredAbilities(card.getTriggeredAbilities(this, Event.PutIntoAGraveyardFromAnywhere, null), null);
			
			/* Check for triggered abilities on other cards on the battlefield (i.e. Energy Field) */
			for (ContinuousEffect ce : _continuousEffects) {
				if (card.getController(this) == ce.getController(this))
					queueTriggeredAbilities(ce.getTriggeredAbilities(this, Event.ACardPutInYourGydFromAnywhere), card);
				queueTriggeredAbilities(ce.getTriggeredAbilities(this, Event.ACardPutInAnyGydFromAnywhere), card);
			}
		}
		card.zoneSwitched();
	}
	
	private void enterBattlefield(Card card) {
		card.setInternalObjectId(generateInternalObjectId());
		// Does this card enter the battlefield tapped ?
		if (card.doesEnterTheBattlefieldTapped(this))
			card.tap(this, false);
		
		/* Check for triggered abilities on the card itself */
		Vector<TriggeredAbility> ETBeffects = card.getTriggeredAbilities(this, Event.EntersTheBattlefield, Origin.BTLFLD); 
		ETBeffects.addAll(card.getTriggeredAbilities(this, Event.EntersTheBattleFieldOrDies, Origin.BTLFLD));
		ETBeffects.addAll(card.getTriggeredAbilities(this, Event.EntersTheBattleFieldOrAttacks, Origin.BTLFLD));
		ETBeffects.addAll(card.getTriggeredAbilities(this, Event.ThisOrAnotherEnchantETB, Origin.BTLFLD));
		
		// Look for constellation triggers
		if (card.isEnchantmentCard()) {
			Player controller = card.getController(this);
			Vector<Card> permanents = _battlefield.getPermanentsControlledBy(controller);
			for (Card permanent : permanents) {
				if (permanent != card)
					ETBeffects.addAll(permanent.getTriggeredAbilities(this, Event.ThisOrAnotherEnchantETB, Origin.BTLFLD));
			}
		}
		
		queueTriggeredAbilities(ETBeffects, card);
		
		//////////////////////
		/* Check for triggered abilities on other cards on the battlefield (i.e. Nissa Vastwood Seer) */
		for (ContinuousEffect ce : _continuousEffects) {
			if (card.isLandCard() && (card.getController(this) == ce.getSource().getController(this))) {
				Vector<TriggeredAbility> LANDFALLeffects;
				LANDFALLeffects = ce.getTriggeredAbilities(this, Event.ALandEntersBattlefieldUnderYourControl); 
				queueTriggeredAbilities(LANDFALLeffects, card);
			}
			
			if (card.isCreature(this)) {
				if (card.getController(this) == ce.getSource().getController(this)) {
					Vector<TriggeredAbility> effects = ce.getTriggeredAbilities(this, Event.ACreatureEntersBattlefieldUnderYourControl);
					queueTriggeredAbilities(effects, card);
				}
				Vector<TriggeredAbility> effects = ce.getTriggeredAbilities(this, Event.ACreatureEntersBattlefield);
				queueTriggeredAbilities(effects, card);
			}
		}
		//////////////////////
		
		/* check for continuous effects to be activated */
		if (card.hasContinuousEffects()) {
			Vector<ContinuousEffect> effects = card.getContinuousEffects(Zone.Name.Battlefield);
			for (ContinuousEffect effect : effects) {
				addContinuousEffect(effect);
			}
		}
		card.putBaseCounters(this);
		
		if (card.hasSubtypeGlobal(this, Subtype.AURA)) {
			Card host = (Card) card.getTargetObject(0);
			if (host != null)
				host.attachLocalPermanent(card);
		}
		
		// if the card is a creature that was cast using suspend, give it haste
		if (card.isCreature(this) && (card.getSpellCastUsed() != null) &&(card.getSpellCastUsed().getOption() == Option.CAST_USING_SUSPEND))
			card.addModifiers(new EvergreenModifier(card, Duration.PERMANENTLY, Evergreen.HASTE));
	}
	
	private void leaveBattlefield(Zone newZone, Card card) {
		card.untap(this);
		card.clearSpellCastOptionUsed();
		card.removeReferences();
		
		card.getController(this).setRevolt();
		
		// If card was an attacking creature, remove it from attackers
		if (card.isCreature(this) && _attackers.contains(card))
			_attackers.remove(card);

		/* Check for triggered abilities on the card that just left the battlefield */
		Vector<TriggeredAbility> LEAVESeffects = card.getTriggeredAbilities(this, Event.LeavesTheBattlefield, Origin.BTLFLD); 
		queueTriggeredAbilities(LEAVESeffects, card);

		// If it's a double faced card, turn it to it's recto face
		if (card.getDayFaceCard() != null) {
			Card recto = CardFactory.create(card.getDayFaceCard());
			recto.setOwner(card.getOwner());
			recto.setController(this, card.getController(this));
			newZone.removeObject(card);
			newZone.addObject(recto);
		}
		
		// If it's a face-down card (morph), turn it to it's face-up version
		if (card.getFaceUp() != null) {
			Card faceUp = card.getFaceUp();
			faceUp.setOwner(card.getOwner());
			faceUp.setController(this, card.getController(this));
			newZone.removeObject(card);
			newZone.addObject(faceUp);
		}			
		
		// If it's a copy of another card, turn it to it's original form
		if (card.isCopy()) {
			Card original = card.getOriginal();
			original.setOwner(card.getOwner());
			original.setController(this, card.getController(this));
			newZone.removeObject(card);
			newZone.addObject(original);
		}

		if (newZone.getName() == Name.Graveyard) {
			/* Check for triggered abilities on the card that just died */
			queueTriggeredAbilities(card.getTriggeredAbilities(this, Event.Dies, Origin.BTLFLD), card);
			queueTriggeredAbilities(card.getTriggeredAbilities(this, Event.EntersTheBattleFieldOrDies, Origin.BTLFLD), card);
			
			/* Check for triggered abilities on other cards on the battlefield (i.e. Fecundity) */
			for (ContinuousEffect ce : _continuousEffects) {
				if (card.isCreature(this)) {
					queueTriggeredAbilities(ce.getTriggeredAbilities(this, Event.ACreatureDies), card);
					if (card.hasColor(Color.GREEN))
						queueTriggeredAbilities(ce.getTriggeredAbilities(this, Event.AGreenCreatureDies), card);
					
					if (card.hasCounters(CounterType.FUNGUS))
						queueTriggeredAbilities(ce.getTriggeredAbilities(this, Event.ACreatureWithFungusDies), card);
				}
				
				if (!card.isToken())
					queueTriggeredAbilities(ce.getTriggeredAbilities(this, Event.ANonTokenPermanentDies), card);
				
				if ((card != ce.getSource()) && card.isCreature(this) && (card.getController(this) == ce.getController(this))) {
					queueTriggeredAbilities(ce.getTriggeredAbilities(this, Event.AnotherCreatureYouControlDies), card);
					if ((card.isToken() == false))
						queueTriggeredAbilities(ce.getTriggeredAbilities(this, Event.AnotherNonTokenCreatureYouControlDies), card);
				}
				
				// Creature you control
				if (card.isCreature(this) && (card.getController(this) == ce.getController(this))) {
					// Any
					queueTriggeredAbilities(ce.getTriggeredAbilities(this, Event.ACreatureYouControlDies), card);
					
					// Non token
					if ((card.isToken() == false))
						queueTriggeredAbilities(ce.getTriggeredAbilities(this, Event.ANonTokenCreatureYouControlDies), card);
					
					// Non angel
					if (!card.hasCreatureType(this, CreatureType.Angel))
						queueTriggeredAbilities(ce.getTriggeredAbilities(this, Event.AnonAngelCreatureYouControlDies), card);
				}
			}
			card.setPutIntoGraveyardThisTurn(true);
		}
		card.removeAllCounters(this);
	}
	
	public void addContinuousEffect(ContinuousEffect effect) {
		_continuousEffects.add(effect);
	}
	
	/**
	 * Check for continuous effects to be deactivated.
	 */
	private void removeObsoleteContinuousEffects() {
		int i = 0;
		StackObject source;
		Zone zone;
		
		while (i < _continuousEffects.size()) {
			ContinuousEffect effect = _continuousEffects.get(i);
			
			// if the effect does not come from an Emblem or a spell's effect rather than a permanent on the battlefield
			if (!((effect.getSource() instanceof Emblem) || (effect.getActiveZone() == null))) { 
				source = (StackObject) effect.getSource();
				zone = source.getZone(this);
				if ((zone == null) || (zone.getName() != effect.getActiveZone())) {
					_continuousEffects.remove(effect);
					i--;
				}
			}			
			i++;
		}		
	}
	
	/**
	 * 
	 */
	public Response givePriority(Player player) {
		Response ret = Response.OK;
		
		// checkStateBasedActions();
		removeObsoleteContinuousEffects();
		checkStateBasedActions();
		
		if (!_queuedTriggeredAbilities.isEmpty())
			ret = putTriggeredAbilitiesOnStack(_queuedTriggeredAbilities);
		else
		{
			_playerWithPriority = player;
			_playerWithPriority.setState(State.Ready);
		}
		return ret;
	}
	
	

	/**
	 * Put triggered abilities onto the stack
	 */
	private Response putTriggeredAbilitiesOnStack(Vector<TriggeredAbility> abilities) {
		TriggeredAbility ta = abilities.remove(0);
		_topStackObject = ta;
		Card source = ta.getSource();
		Player controller;
		if (source != null)
			controller = source.getController(this);
		else
			controller = ta.getController(this);
		ta.setController(this, controller);
		return resolvableCastModes();
	}

	/**
	 * Mox Diamond is a little special as when it comes to its resolution.
	 * @param card
	 * @return
	 */
	private Response doMoxDiamond(Card card) {
		Player controller = card.getController(this);
		State state = controller.getState();
		if (state == State.Ready) {
			controller.setState(State.WaitChoiceMoxDiamond);
			return Response.MoreStep;
		}
		else if (state == State.WaitChoiceMoxDiamond) {
			if (validateChoices()) {
				if (getChoices().size() == 1) {
					discard((Card) getChoices().get(0));
					switchZone(_stack, card, _battlefield);
				}
				else
					switchZone(_stack, card, controller.getGraveyard());
				return givePriority(_activePlayer);
			}
			else
				return Response.MoreStep;
		}
		else
			return Response.Error;
	}
	
	/**
	 * Start resolving the object on top of the stack
	 * @return
	 */
	private Response resolveTopObject() {
		StackObject so = _stack.getTopObject();
		boolean bOK = true;
		Response ret;

		// upon resolution, check target(s) is(are) still legal
		if (so.hasTargetRequirements())
			bOK = so.checkTargetLegality(this);
		if (so instanceof Card) {
			Card card = (Card) so;
			if (card.isPermanentCard()) { // a permanent spell is about to resolve
				
				// Mox Diamond
				if (card.hasStaticAbility("moxDiamond"))
					return doMoxDiamond(card);
				
				// switchZone(_stack, card, _battlefield);
				ret = switchZone(_stack, card, _battlefield);
				if (ret == Response.OK)
					ret = Response.OKholdPriority;
				if (ret != Response.MoreStep)
					givePriority(_activePlayer);
				//return givePriority(_activePlayer);
					return ret;
			}
		}
		else if (so instanceof TriggeredAbility) {// a triggered ability is about to resolve
			TriggeredAbility ta = (TriggeredAbility) so;
			if (ta.hasInterveningIfClause() && !ta.validateInterveningIfClause(this))
				bOK = false;
			ta.setController(this, ta.getSource().getController(this));
		}
		
		// All checks are OK
		if (bOK)
			ret = so.doEffect(this);
		else
			ret = Response.ErrorTargetBecameIllegal;
		if (ret != Response.MoreStep)
			finishResolveTopObject(so, ret);
		return ret;
	}
	
	/**
	 * Finish resolving the object on top of the stack after all choices (upon resolution)
	 * have been made.
	 * @return
	 */
	private void finishResolveTopObject(StackObject sobj, Response spellReturnCode) {
		if (!_stack.isEmpty()) {
			StackObject so = _stack.getTopObject();
			if (so == sobj) { // this test is necessary in case the stackobject is no longer on the stack (i.e. has been
				              // exiled during the resolution of its effect like Rally the Ancestors)
				so.purgeTargets();
				so.setXValue(-1);
				if (so instanceof Card) {
					Card card = (Card) so;
					
					// if the spell was cast with Awaken, we need to remove the additional target land requirement
					if (card.getSpellCastUsed().getOption() == Option.CAST_WITH_AWAKEN)
						card.removeTargetRequirements(Category.LandYouControl);
					
					// check if spell was cast with buyback and was did resolve
					if ((card.getSpellCastUsed().getOption() == Option.CAST_WITH_BUYBACK) && (spellReturnCode == Response.OK))
						switchZone(_stack, card, card.getOwner().getHand());

					// check if spell has rebound and was cast from hand and did resolve
					if (card.hasStaticAbility("rebound") && (spellReturnCode == Response.OK))
					{
						// Case A : Card was cast from hand
						if (card.getSpellCastUsed().getOption() != SpellCast.Option.CAST_FROM_EXILE) {
							// 1. exile the card instead of putting it in its owner's graveyard
							switchZone(_stack, card, card.getOwner().getExile());
							
							// 2. create a delayed triggered ability than will trigger next upkeep to allow casting the card from exile
							ContinuousEffect delayedTrigger = ContinuousEffectFactory.create("rebound", card);
							this.addContinuousEffect(delayedTrigger);	
						}
						// Case B : Card was cast from Exile
						else if (card.getSpellCastUsed().getOption() == SpellCast.Option.CAST_FROM_EXILE)
						{
							// Put card in its owner's graveyard
							switchZone(_stack, card, card.getOwner().getGraveyard());
						}
					}
					// default : put card in graveyard after resolution unless it was a copy
					else
					{
						if (card.isCopy())
							_stack.removeObject(card);
						else
							switchZone(_stack, card, card.getOwner().getGraveyard());
					}					
				}
				else if (so instanceof TriggeredAbility)
				{
					TriggeredAbility ta = (TriggeredAbility) _stack.removeObject(0);
					_queuedTriggeredAbilities.remove(ta);
				}
				else // activated ability
				{
					_stack.removeObject(0);
				}	
			}
		}
		
		if (!_stack.isEmpty())
			_topStackObject = _stack.getTopObject();
		_playerWithPriority.setLibrarySearchable(false);
		givePriority(_activePlayer);
		
		/* Queue rebound spells */
		if (_reboundSpell != null) {
			castSpell(_reboundSpell.getID(), _reboundSpell.getPlayModeWithOption(Option.CAST_FROM_EXILE));
			_reboundSpell = null;
		}
		
		/* Queue spell copies */
		if (_spellCopy != null) {
			playCardCopy(_spellCopy);
			_spellCopy = null;
		}
	}
	
	void playCardCopy(Card card) {
		_topStackObject = card;
		resolvableCastCheckTiming();
	}

	public Response allPlayersPassedPriority() {
		if (_stack.isEmpty())
			return advanceStep();
		return resolveTopObject();
	}
	
	public Response destroy(Card card, boolean bRegenerationPossible) {
		if (card.hasEvergreenGlobal(Evergreen.INDESTRUCTIBLE, this))
			return Response.ErrorIndestructible;
		
		Response ret;
		if (bRegenerationPossible && card.hasRegenShield()) {
			card.consumeRegenShield();
			card.tap(this);
			card.cleanDamage();
			removeCreatureFromCombat(card);
			ret = Response.OK;
		}
		else
			ret = move_BFD_to_GYD(card);
		return ret;
	}
	
	public Response sacrifice(Player controller, Card card) {
		if (!card.isOTB(this))
			return Response.ErrorIncorrectZone;
		
		if ((card.getController(this) != controller))
			return Response.ErrorIncorrectController;

		move_BFD_to_GYD(card);
		if (card.getDisplayName().equals("Clue"))
			queueSacrificeClue(controller);
		return Response.OK;
	}

	public void discardRandom(Player controller) {
		int handSize = controller.getHandSize();
		if (handSize == 0)
			return;
		Random rnd = new Random();
		Card c = (Card) controller.getHand().getObjectAt(rnd.nextInt(handSize));
		discard(c);
	}

	
	public Response discard(Card c) {
		if ((_topStackObject != null) && (_topStackObject.getController(this) != c.getController(this))) {
			/* Check for triggered abilities on the card when it is being discarded by an opponent spell or ability */
			Vector<TriggeredAbility> effects = c.getTriggeredAbilities(this, Event.DiscardedByOpponent, Origin.HAND); 
			queueTriggeredAbilities(effects, c);	
		}
		
		// TODO : do things like Madness here
		
		/* Check for triggered abilities on other cards on the battlefield (i.e. Necropotence) */
		
		for (ContinuousEffect ce : _continuousEffects) {
			if ((_topStackObject != null) && (c.getOwner() == ce.getSource().getController(this))) {
				queueTriggeredAbilities(ce.getTriggeredAbilities(this, Event.YouDiscard), c);
			}
		}
		
		return move_HND_to_GYD(c);
	}
	
	private Response move_HND_to_GYD(Card c) {
		return switchZone(c.getOwner().getHand(), c, c.getOwner().getGraveyard());
	}
	
	public Response move_HND_to_EXL(Card c) {
		return switchZone(c.getOwner().getHand(), c, c.getOwner().getExile());
	}
	
	public Response move_EXL_to_BFD(Card c) {
		return switchZone(c.getOwner().getExile(), c, _battlefield);
	}
	
	public Card createCopyToken(Card source, String cardname, Player controller) {
		Card token = CardFactory.create(cardname);
		
		token.setController(this, controller);
		token.setOwner(controller);
		token.setTokenSource(source.getSource());
		controller.getExile().addObject(token);
		move_EXL_to_BFD(token);
		token.setTokenName(cardname);
		return token;
	}
	
	/**
	 * Returns the card name of a creature card chosen at random with converted mana cost = X.
	 * - Momir Vig
	 * @param convertedManaCost
	 * @return
	 */
	public String getRandomCreatureCardWithCCM(int convertedManaCost) {
		String cardname = null;
		Card card;
		
		int i = 0;

		do {
			cardname = CardFactory.getRandomCard();
			card = CardFactory.create(cardname);
			i++;
			if (i == 1500)
				return null;
		} while (!(card.isCreatureCard() && (card.getConvertedManaCost(this) == convertedManaCost)));
		
		return cardname;
	}
	
	public Card blink(Card c) {
		boolean bVersoFace = (c.getDayFaceCard() != null);
		Card newInstance = c;
		
		if (move_BFD_to_EXL(c) == Response.OK)
		{
			removeCreatureFromCombat(c);
			removeObsoleteContinuousEffects();
			if (bVersoFace)
				newInstance = (Card) c.getOwner().getExile().findCardByName(c.getDayFaceCard());
			if (c.getName().equals(Card.COLORLESS_MORPH_22))
				newInstance = (Card) c.getOwner().getExile().findCardByName(c.getFaceUp().getName());
			move_EXL_to_BFD(newInstance);
		}
		return newInstance;
	}
	
	// i.e. Swords to plowshares
	public Response move_BFD_to_EXL(Card c) {
		return switchZone(_battlefield, c, c.getOwner().getExile());
	}
	
	public Response move_HND_to_TOPLIB(Card c) {
		return switchZone(c.getOwner().getHand(), c, c.getOwner().getLibrary());
	}
	
	public Response move_BFD_to_TOPLIB(Card c) {
		return switchZone(_battlefield, c, c.getOwner().getLibrary());
	}
	
	public Response move_BFD_to_BOTTOMLIB(Card c) {
		Response ret = switchZone(_battlefield, c, c.getOwner().getLibrary());
		if (ret == Response.OK) {
			Library lib = c.getOwner().getLibrary();
			lib.removeObject(c);
			lib.addObject(c, -1);	
		}
		return Response.OK;
	}
	
	public Response move_EXL_to_BOTTOMLIB(Card c) {
		Exile exl = c.getOwner().getExile();
		Library lib = c.getOwner().getLibrary();
		exl.removeObject(c);
		lib.addObject(c, -1);
		return Response.OK;
	}
	
	public Response move_LIB_to_BOTTOMLIB(Card c) {
		Library lib = c.getOwner().getLibrary();
		lib.removeObject(c);
		lib.addObject(c, -1);
		return Response.OK;
	}

	// i.e. Reclaim
	public Response move_GYD_to_TOPLIB(Card c) {
		return switchZone(c.getOwner().getGraveyard(), c, c.getOwner().getLibrary());
	}
	
	// i.e. Cremate
	public Response move_GYD_to_EXL(Card card) {
		return switchZone(card.getOwner().getGraveyard(), card, card.getOwner().getExile());
	}
	
	// i.e. Casting a card with Rebound from exile
	public Response move_EXL_to_STK(Card card) {
		return switchZone(card.getOwner().getExile(), card, _stack);
	}
	
	// i.e. Necropotence
	public Response move_EXL_to_HND(Card card) {
		return switchZone(card.getOwner().getExile(), card, card.getOwner().getHand());
	}
	
	public Response move_STK_to_EXL(Card card) {
		return switchZone(_stack, card, card.getOwner().getExile());
	}
	
	private Response move_BFD_to_GYD(Card c) {
		Player owner = c.getOwner();
		return switchZone(_battlefield, c, owner.getGraveyard());
	}
	
	public Response move_GYD_to_BFD(Card c) {
		return switchZone(c.getOwner().getGraveyard(), c, _battlefield);
	}
	
	public Response move_GYD_to_HND(Card c) {
		return switchZone(c.getOwner().getGraveyard(), c, c.getOwner().getHand());
	}
	
	public Response move_BFD_to_HND(Card c) {
		return switchZone(_battlefield, c, c.getOwner().getHand());
	}
	
	/**
	 * Attemtps to counter a spell or ability
	 * @param target The target spell to be countered..
	 * @return true if the spell was countered, false otherwise.
	 */
	public boolean counter(StackObject target) {
		// in case of a spell
		if (target instanceof Card) {
			Card spell = (Card) target;
			if (spell.hasEvergreenGlobal(Evergreen.UNCOUNTERABLE, this))
				return false;
			switchZone(_stack, spell, spell.getController(this).getGraveyard());
		}
		else // in case of an activated or triggered ability
		{
			_stack.removeObject(target);
		}
		return true;
	}
	
	/**
	 * Attemtps to counter a spell and move it to zone other than it's owner's graveyard.
	 * @param target The target spell to be countered..
	 * @param newZone The zone where the spell card must be placed into.
	 * @return
	 */
	public boolean counterAndMove(Card target, Zone newZone) {
		// Spell cannot be countered
		if (target.hasEvergreenGlobal(Evergreen.UNCOUNTERABLE, this))
			return false;

		switchZone(_stack, target, newZone);
		return true;
	}
	
	// i.e. Green Sun Zenith's reshuffling effect
	public Response move_STK_to_LIB(Card c) {
		return switchZone(_stack, c, c.getOwner().getLibrary());
	}
	
	// i.e. Remand or Venser's effect
	public Response move_STK_to_HND(Card c) {
		return switchZone(_stack, c, c.getOwner().getHand());
	}
	
	// i.e. Demonic Tutor
	public Response move_LIB_to_HND(Card c) {
		return switchZone(c.getOwner().getLibrary(), c, c.getOwner().getHand());
	}
	
	// i.e. Entomb
	public Response move_LIB_to_GYD(Card c) {
		if (c.getOwner().getLibrary().size() == 0)
			return Response.OK;
		return switchZone(c.getOwner().getLibrary(), c, c.getOwner().getGraveyard());
	}
	
	// i.e. Cascade
	public Response move_LIB_to_EXL(Card c) {
		return switchZone(c.getOwner().getLibrary(), c, c.getOwner().getExile());		
	}

	
	public Response mill(Player p, int nbCards) {
		Response ret = Response.OK;
		for (int i = 0; i < nbCards; i++) {
			Card c = p.getLibrary().getTopCard();
			ret = switchZone(c.getOwner().getLibrary(), c, c.getOwner().getGraveyard());
			if (ret != Response.OK)
				return ret;
		}
		return ret;
	}
	
	public Response libToLibTop(Card c) {
		Library lib = c.getOwner().getLibrary();
		lib.removeObject(c);
		lib.addObject(c, 0);
		return Response.OK;
	}
	
	public Response libToLibBottom(Card c) {
		Library lib = c.getOwner().getLibrary();
		lib.removeObject(c);
		lib.addObject(c, -1);
		return Response.OK;
	}
	
	// i.e. Fetch lands
	public Response move_LIB_to_BFD(Card c) {
		return switchZone(c.getOwner().getLibrary(), c, _battlefield);
	}
	
	// i.e. Stoneforge Mystic 2nd ability
	public Response move_HND_to_BFD(Card c) {
		return switchZone(c.getOwner().getHand(), c, _battlefield);
	}
	
	/* ! end Zone-switching methods !*/
	
	public Response discardEOT(int iCommand) {
		if (_activePlayer.getHand().getObjectByID(iCommand) == null)
			return Response.ErrorInvalidCardNumber;
		discard((Card) _activePlayer.getHand().getObjectByID(iCommand));
		_step = Step.End;
		return advanceStep();
	}
	
	public Answer getAnswer() {
		Answer a = _answer;
		_answer = null;
		return a;
	}
	
	public Response setAnswer(Answer answer) {
		_answer = answer;
		return resolveTopObject();
	}
	
	public Card createSingleToken(Token tokenName, StackObject source, Player controller) {
		Card token;
		
		token = CardFactory.create(tokenName.toString());
		token.setController(this, controller);
		token.setOwner(controller);
		if (source instanceof Card)
			token.setTokenSource((Card)source);
		else
			token.setTokenSource(source.getSource());
		enterBattlefield(token);
		_battlefield.addObject(token);
		return token;
	}
	
	/**
	 * Creates one token and returns it
	 * @param name Token name
	 * @param source Spell or ability that created the token
	 * @return The created token
	 */
	public Card createSingleToken(Token tokenName, StackObject source) {
		return createSingleToken(tokenName, source, source.getController(this));
	}
	
	/**
	 * Creates more than one token
	 * @param tokenName Tokens name
	 * @param nbToken number of tokens to be created
	 * @param source Spell or ability that created the token
	 * @return A Vector<Card> with the tokens
	 */
	public Vector<Card> createTokens(Token tokenName, int nbToken, StackObject source) {
		Vector<Card> tokens = new Vector<Card>();
		for (int i = 0; i < nbToken; i++)
			tokens.add(createSingleToken(tokenName, source));
		return tokens;
	}

	public int countCardsInAllGraveyardsWithName(String cardname) {
		int nb = 0;
		
		for (Player p : _players) {
			Vector<MtgObject> cards = p.getGraveyard().getObjects();
			for (MtgObject card : cards) {
				if (((Card) card).getName().equals(cardname))
					nb++;
			}
		}
		return nb;
	}
	
	public void createEmblem(Card source, String effectName, Player controller) {
		Emblem e = new Emblem(source, controller);
		ContinuousEffect effect = ContinuousEffectFactory.create(effectName, e);
		e.setContinuousEffect(effect);
		controller.addEmblem(e);
		addContinuousEffect(effect);
	}
	
	public void createEmblem(Card source, String effectName) {
		createEmblem(source, effectName, source.getController(this));
	}
	
	public Response setChoice(int playerId, Vector<Integer> choices) {
		Player p = findPlayerByID(playerId);
		_effectChoice = new Choice(p, choices);
		return resolveTopObject();
	}
	
	private int _currentCrewPaymentRequired = 0;
	
	public void setCrewRequirement(int crewParameter) {
		_currentCrewPaymentRequired = crewParameter;
	}
	
	public Response setChoicePayCosts(int playerId, Vector<Integer> choices) {
		_effectChoice = new Choice(findPlayerByID(playerId), choices);
		if (validateChoices()) {
			Vector<MtgObject> cardChoices = getChoices();
			
			Card card = (Card) cardChoices.get(0);
			
			State state = _playerWithPriority.getState(); 
			switch (state) {
			
			case WaitPayCostExileAnotherCreatureCardFromGyd:
				move_GYD_to_EXL(card);
				break;
			
			case WaitPayCostTapUntappedCreature:
				card.tap(this);
				break;
				
			case WaitPayCostCrew:
				card.tap(this);
				Vector<TriggeredAbility> trigs = card.getTriggeredAbilities(this, Event.CrewsAVehicle, Origin.BTLFLD);
				queueTriggeredAbilities(trigs, _topStackObject.getSource()); // additional data = the crewed vehicule
				
				_currentCrewPaymentRequired -= card.getPower(this);
				if (_currentCrewPaymentRequired > 0)
					return Response.MoreStep;
				else
					_currentCrewPaymentRequired = 0;
				break;
			
			case WaitPayCostDiscardACreatureCard:
			case WaitPayCostDiscardACard:
				discard(card);
				break;
			
			case WaitHeartOfKiran:
				card.removeCounter(this, CounterType.LOYALTY, 1);
				break;
				
			case WaitPayCostReturnElf:
			case WaitPayCostReturnLand:
				move_BFD_to_HND(card);
				break;
				
			case WaitPayCostSacrificeCreature:
			case WaitPayCostSacrificeGoblin:
			case WaitPayCostSacrificeAnotherVampireOrZombie:
			case WaitPayCostSacrificeArtifact:
			case WaitPayCostSacrificeLand:
			case WaitPayCostSacrificePermanent:
			case WaitPayCostSacrificeEnchantment:
			case WaitPayCostSacrificeForestOrPlains:
			case WaitPayCostSacrificeForest:
				Card sacrificedPermanent = card;
				_topStackObject.setAdditionalData(sacrificedPermanent);
				sacrifice(_playerWithPriority, sacrificedPermanent);
				_playerWithPriority.setState(State.Ready);
				break;
				
			default:
				break;
			}
			
			return resolvableFinalize();
		}
		else
			return Response.MoreStep;
	}

	private boolean isAttackDeclarationLegal() {
		if (_attackers.isEmpty())
			return true;
		
		for (Card attacker : _attackers) {
			if (!attacker.isAttackDeclarationLegal(this))
				return false;
		}
		return true;
	}
	
	public Response validateDeclareAttackers() {
		if (!isAttackDeclarationLegal())
			return Response.ErrorIllegalAttackDeclaration;
		
		_activePlayer.setState(State.Ready);
		_bAttackersValidated = true;
		for (Card attackingCreature : _attackers) {
			// Queue triggered abilities on the attacking creature
			queueTriggeredAbilities(attackingCreature.getTriggeredAbilities(this, Event.Attacks, Origin.BTLFLD), null);
			queueTriggeredAbilities(attackingCreature.getTriggeredAbilities(this, Event.AttacksOrBlocks, Origin.BTLFLD), null);
			queueTriggeredAbilities(attackingCreature.getTriggeredAbilities(this, Event.EntersTheBattleFieldOrAttacks, Origin.BTLFLD), null);
			
			// Queue triggered abilities on other permanents (i.e. Hellrider)
			for (Card permanent : _battlefield.getPermanents())
			{
				if (permanent.getController(this) == _activePlayer)
				{
					queueTriggeredAbilities(permanent.getTriggeredAbilities(this, Event.ACreatureYouControlAttacks, Origin.BTLFLD), null);
				
					// Triggered abilities when a creature attacks alone (i.e. Exalted)
					if (_attackers.size() == 1)
						queueTriggeredAbilities(permanent.getTriggeredAbilities(this, Event.ACreatureYouControlAttacksAlone, Origin.BTLFLD), attackingCreature);
				}
			}
			
			// Tap the creature unless it has vigilance
			if (!attackingCreature.hasEvergreenGlobal(Evergreen.VIGILANCE, this))
				attackingCreature.tap(this);
		}
		checkStateBasedActions();
		if (!_queuedTriggeredAbilities.isEmpty())
			putTriggeredAbilitiesOnStack(_queuedTriggeredAbilities);
		return Response.OK;
	}
	
	private boolean isBlockDeclarationLegal() {
		if (_blockers.isEmpty())
			return true;
		
		// For each blocker : check that it can legally block (i.e. Okk)
		for (Card blocker : _blockers) {
			if (!blocker.isBlockerDeclarationLegal(this))
				return false;
		}
		
		// For each attacker : check that its block declaration is legal (i.e. Phyrexian Colossus)
		for (Card attacker : _attackers) {
			if (!attacker.isBlockedDeclarationLegal(this))
				return false;
		}
		
		return true;
	}
	
	public Response validateDeclareBlockers() {
		if (!isBlockDeclarationLegal())
			return Response.ErrorIllegalBlockDeclaration;
		boolean bGangBlock = false;
		for (Card attacker : _attackers) {
			if (attacker.isBlocked(this)) {
				// Triggers upon being blocked
				queueTriggeredAbilities(attacker.getTriggeredAbilities(this, Event.BlocksOrBecomesBlocked, Origin.BTLFLD), null);
				
				// Triggers upon being blocked by a creature once per blocker
				for (int i = 0; i < attacker.getBlockers().size(); i++) {
					queueTriggeredAbilities(attacker.getTriggeredAbilities(this, Event.BecomesBlockedByACreature, Origin.BTLFLD), null);
					queueTriggeredAbilities(attacker.getBlockers().get(i).getTriggeredAbilities(this, Event.BlocksOrBecomesBlocked, Origin.BTLFLD), null);
					queueTriggeredAbilities(attacker.getBlockers().get(i).getTriggeredAbilities(this, Event.AttacksOrBlocks, Origin.BTLFLD), null);
					queueTriggeredAbilities(attacker.getBlockers().get(i).getTriggeredAbilities(this, Event.Blocks, Origin.BTLFLD), null);
				}
				
				// in case of gang block, attacking player must announce the damage assignment order
				if (attacker.getBlockers().size() > 1)
					bGangBlock = true;
			}
		}
		
		if (bGangBlock)
			_activePlayer.setState(State.WaitReorderBlockers);
		else {
			_activePlayer.setState(State.Ready);
			_bBlockersValidated = true;
			checkStateBasedActions();
			if (!_queuedTriggeredAbilities.isEmpty())
				putTriggeredAbilitiesOnStack(_queuedTriggeredAbilities);
		}
		return Response.OK;
	}
	
	public boolean isBlockerDeclarationValidated() {
		return _bBlockersValidated;
	}
	
	public boolean validateChoices() {
		return _effectChoice.validate(this);
	}
	
	public Vector<MtgObject> getChoices() {
		return _effectChoice.get();
	}

	public void searchLibrary(Player p, boolean value) {
		p.setLibrarySearchable(value);
	}
	
	public int getNbTarget() {
		return _topStackObject.getNbTarget();
	}

	public Response attach(Card equipment, Card creature) {
		if (creature == null)
			return Response.ErrorInvalidTarget;
		if (!creature.isOTB(this))
			return Response.ErrorIncorrectZone;

		creature.attachLocalPermanent(equipment);
		return Response.OK;
	}

	/**
	 * 
	 * @param c
	 */
	private void buildSpellCastOptions(Card c) {
		for (SpellCast mode : c.getSpellCastOptions())
		{
			// do not add flashback to play modes if card is not in graveyard
			if ((mode.getOption() == SpellCast.Option.CAST_WITH_FLASHBACK) && !c.isIGY(this))
				continue;
			
			// do not add spectacle to play modes if no opponent was damaged this turn
			if ((mode.getOption() == Option.CAST_WITH_SPECTACLE) && !c.getController(this).getOpponent().hasLostLifeThisTurn())
				continue;
			
			// do not add "normal play" to play modes if card is in graveyard.
			if (mode.getOption() == SpellCast.Option.CAST) {
				if (c.isIGY(this) && !c.getOwner().isUnderYawgmothsWill()) // Unless the player is under some kind of Yawgmoth's Will effect
					continue;
			}
			
			// do not add rebound (Play from Exile) to play modes if card is not in exile
			if ((mode.getOption() == SpellCast.Option.CAST_FROM_EXILE) && !c.isIEX(this))
				continue;
			
			_performableActions.add(mode);
		}
	}
	
	/**
	 * 
	 * @param c
	 */
	private void buildSpecialActions(Card c) {
		for (SpecialAction action : c.getSpecialActions()) {
			// do not add "play a land" if the card is not in hand
			if ((action.getOption() == SpecialAction.Option.PLAY_LAND) && !c.isIH(this))
				continue;

			// do not add "suspend" if the card is not in hand
			if ((action.getOption() == SpecialAction.Option.SUSPEND) && !c.isIH(this))
				continue;
			
			// do not add "un-morph" if card is not OTB
			if ((action.getOption() == SpecialAction.Option.TURN_FACEUP) && !c.isOTB(this))
				continue;
			
			_performableActions.add(action);
		}
	}
	
	/**
	 * Build the list of all performable actions on the specified card.
	 * @param idCard
	 * @return
	 */
	public Response requestActions(int idCard) {
		MtgObject obj = findObjectById(idCard);
		
		// check that an object with this ID exists
		if (obj == null)
			return Response.ErrorInvalidCardNumber;
		
		// check that the object with this ID is a card (not a player or an ability, etc ....)
		if (!(obj instanceof Card))
			return Response.ErrorInvalidCardNumber;
		
		Card c = (Card) obj;
		
		// check that the player who is trying to perform this action has priority
		if ((c.isIH(this) && (c.getOwner() != _playerWithPriority)) || (c.isOTB(this) && (c.getController(this) != _playerWithPriority)))
			return Response.ErrorIncorrectController;
		
		// All checks OK, start building actions
		_performableActions.clear();

		// Build special actions
		buildSpecialActions(c);
		
		// Build cast a spell modes
		if (c.isIH(this) || (c.isIGY(this) && (c.hasStaticAbility("flashback") || c.getOwner().isUnderYawgmothsWill())))
			buildSpellCastOptions(c);
		
		// Build activated abilities
		_performableActions.addAll(c.getActivatedAbilities(this, true));
		return Response.PrintActions;
	}
	
	public Vector<MtgObject> getTargetablePlayers(StackObject so) {
		Vector<MtgObject> ret = new Vector<MtgObject>(); 
		for (Player p : _players) {
			if (p.isTargetableBy(this, so))
				ret.addElement(p);
		}
		return ret;
	}
	
	public Player findPlayerByID(int id) {
		for (Player p : _players)
			if (p.getID() == id)
				return p;
		return null;
	}

	public Vector<Player> getPlayers() {
		return _players;
	}

	public Stack getStack() {
		return _stack;
	}

	public Battlefield getBattlefield() {
		return _battlefield;
	}

	public Player getActivePlayer() {
		return _activePlayer;
	}

	public Response passPriority(int idPlayer) {
		Response ret = Response.OK;
		if (findPlayerByID(idPlayer) != _playerWithPriority)
			return Response.Error;
		
		_playerWithPriority = _playerWithPriority.getOpponent();
		_playerWithPriority.setState(State.Ready);
		_nbPlayersWhoPassed++;
		if (_nbPlayersWhoPassed == _players.size())
		{
			_nbPlayersWhoPassed = 0;
			ret = allPlayersPassedPriority();
		}
		return ret;
	}

	public Player getPlayerWhoHasPriority() {
		return _playerWithPriority;
	}

	public Response setAttackingCreature(Card attacker) {
		Response ret = Response.OK;
		
		// By default, the attack recipient is defending player
		Player defendingPlayer = _activePlayer.getOpponent();
		
		// Check if defending player controls a planeswalker
		if (defendingPlayer.controlsAPlaneswalker() == true)
		{
			_activePlayer.setState(State.WaitChooseRecipientToAttack);
			_attackingCreature = attacker;
			ret = Response.MoreStep;
		}
		else
		{
			attacker.attacks(defendingPlayer);
		}
		_attackers.add(attacker);
		return ret;
	}
	
	public Response toggleDeclareAttackers(int idCard) {
		Card attacker = (Card) findObjectById(idCard);
		Response ret = Response.OK;
		
		if ((attacker == null) || (!attacker.canAttack(this)))
			return Response.ErrorInvalidCardNumber;
				
		if (_attackers.contains(attacker))
		{
			_attackers.remove(attacker);
			attacker.clearAttackRecipient();
		}
		else
			ret = setAttackingCreature(attacker);
		return ret;
	}
	
	public Response toggleDeclareBlockers(int idCard) {
		Card blocker = (Card) findObjectById(idCard);
		
		if ((blocker == null) || (!blocker.canBlock(this)) || (countBlockableCreatures(blocker) == 0))
			return Response.ErrorInvalidCardNumber;
				
		if (_blockers.contains(blocker))
		{
			_blockers.remove(blocker);
			blocker.clearBlockedCreatures();
		}
		else
		{
			_blockers.add(blocker);
			_blockingCreature = blocker;
			_activePlayer.getOpponent().setState(State.WaitChooseCreatureToBlock);
		}
		return Response.OK;
	}
	
	public Response reorderBlocker(int idCard) {
		Card blocker = (Card) findObjectById(idCard);

		for (Card attacker : _attackers) {
			if (attacker.getBlockers() != null) {
				if (attacker.getBlockers().contains(blocker)) {
					Vector<Card> blockers = attacker.getBlockers();
					
					// shift the selected blocker to the top
					blockers.remove(blocker);
					blockers.add(0, blocker);
				}	
			}
		}
		return Response.OK;
	}

	
	public Response assignCreatureToBlock(int idCard) {
		Card attacker = (Card) findObjectById(idCard);
		if (attacker == null)
			return Response.ErrorInvalidCardNumber;
		
		if (!_blockingCreature.canBlockAttacker(this, attacker))
			return Response.ErrorIllegalBlockDeclaration;
		
		_blockingCreature.blocks(attacker);
		declareBlockersStep();
		return Response.OK;
	}
	
	public boolean flipCoin() {
		Random rnd = new Random();
		int result = rnd.nextInt(100);
		if (result % 2 == 0)
			return true;
		else
			return false;
	}
	
	public Response assignRecipientToAttack(int idObject) {
		Damageable recipient = findAttackRecipientById(idObject);
		if (recipient == null)
			return Response.ErrorInvalidCardNumber;
		
		// Check if the recipient is the attacking player
		if ((recipient instanceof Player) && (_attackingCreature.getController(this) == recipient))
			return Response.ErrorInvalidCardNumber;
		
		// Check if the recipient is a planeswalker controlled by the attacking player
		if ((recipient instanceof Card) && (_attackingCreature.getController(this) == ((Card) recipient).getController(this)))
			return Response.ErrorInvalidCardNumber;
		
		if (!_attackingCreature.canAttackRecipient(this, recipient))
			return Response.ErrorIllegalAttackDeclaration;
		
		_attackingCreature.attacks(recipient);
		if (!_bAttackersValidated)
			declareAttackersStep();
		else
		{
			_activePlayer.setState(State.Ready);
			finishResolveTopObject(_topStackObject, Response.OK);
		}
		return Response.OK;
	}

	private int countBlockableCreatures(Card blocker) {
		int nb = 0;
		for (Card attacker : _attackers)
			if (blocker.canBlockAttacker(this, attacker))
				nb++;
		return nb;
	}
	
	/* dump functions used by Packet and Interpreter to transmit data */

	/**
	 * Status for idPlayer
	 * Contains : state, turn, phase, step 
	 * @param idPlayer
	 */
	public Vector<String> dumpStatus(int idPlayer) {
		Vector<String> ret = new Vector<String>();
		State state = findPlayerByID(idPlayer).getState();
		String st = state.name();

		ret.add(Boolean.toString(_bDebugMode)); // debug mode 0
		ret.add(Integer.toString(_activePlayer.getTurn())); // turn number of the active player 1
		ret.add(_phase.toString()); // phase 2
		ret.add(_step.toString()); // step 3
		if ((state == State.PromptTargets) || (state == State.PromptTargetsOrDone))
			st += ":" + _topStackObject.getName();
		ret.add(st); // state 4
		for (Player p : _players) // players 5-n
			ret.add(p.dumpStatus());
		return ret;
	}
	
	/**
	 * Information on players 
	 */
	public Vector<String> dumpPlayers() {
		Vector<String> ret = new Vector<String>();
		for (Player p : _players) {
			ret.add(p.getSystemName());
		}
		return ret;
	}
	
	/**
	 * Card info (printed text + computed abilities)
	 * 
	 */
	public Vector<String> dumpCardInfo() {
		Vector<String> ret = null;
		MtgObject object = findObjectById(_requestCardInfo_IdCard);
		if (object == null)
			return null;
		if (object instanceof Card) {
			Card c = (Card) object;
			ret = c.getCardInfo(this, findPlayerByID(_requestCardInfo_IdPlayer));
			_requestCardInfo_IdCard = -1;
			_requestCardInfo_IdPlayer = -1;	
		}
		else if (object instanceof Emblem) {
			Emblem e = (Emblem) object;
			ret = e.getSource().getCardInfo(this, findPlayerByID(_requestCardInfo_IdPlayer));
		}
		return ret;
	}
	
	/**
	 * Print continuous effects to the server console
	 */
	public void printContinuousEffects() {
		if (_continuousEffects.size() == 0)
			return;

		StackObject source;
		String text;
		int maxLen = 150;
		
		System.out.println("[********** CONTINUOUS EFFECTS **********]");
		for (ContinuousEffect ce : _continuousEffects) {
			if (ce.getSource() instanceof StackObject)
				source = (StackObject) ce.getSource();
			else
				source = ((Emblem) ce.getSource()).getSource();
			text = ce.getDescription();
			if (text.length() > maxLen)
				text = text.substring(0, maxLen-3) + "...";
			System.out.println("[" + source + " (" + ce.getController(this).getName() + ") : " + text + "]");
		}
	}
	
	/**
	 * idPlayer's hand 
	 * @param idPlayer
	 */
	public Vector<String> dumpHandforPlayer(int idPlayer) {
		return findPlayerByID(idPlayer).getHand().toStringVector();
	}	
	
	/**
	 * idPlayer's opponent's hand 
	 * @param idPlayer
	 */
	public Vector<String> dumpOpponentHandforPlayer(int idPlayer) {
		return findPlayerByID(idPlayer).getOpponent().getHand().toStringVector();
	}
	
	/**
	 * idPlayer's library
	 * @param idPlayer
	 */
	public Vector<String> dumpLibraryForPlayer(int idPlayer) {
		return findPlayerByID(idPlayer).getLibrary().toStringVector();
	}
	
	/**
	 * idPlayer's library's top X cards
	 * @param idPlayer
	 * @param nbCards
	 */
	public Vector<String> dumpLibraryManipulation(int idPlayer) {
		Vector<String> ret = new Vector<String>();
		for (Card c : _topStackObject.getLibManip())
			ret.add(c.getSystemName());
		return ret;
	}
	
	/**
	 * Permanents controlled by idPlayer from category cat
	 * @param idPlayer
	 * @param cat Categories are : Creatures, Lands and Other
	 */
	public Vector<String> dumpPermanentsforPlayer(int idPlayer, PermanentCategory cat) {
		Vector<String> ret = new Vector<String>();
		ret.addAll(findPlayerByID(idPlayer).dumpPermanentsAttached(cat));
		ret.addAll(findPlayerByID(idPlayer).dumpPermanents(cat));
		return ret;
	}
	
	/**
	 * Cards in idPlayer's graveyard 
	 * @param idPlayer
	 */
	public Vector<String> dumpGraveyardforPlayer(int idPlayer) {
		return findPlayerByID(idPlayer).getGraveyard().toStringVector();
	}
	
	/**
	 * Cards in idPlayer's exile zone 
	 * @param idPlayer
	 */
	public Vector<String> dumpExileforPlayer(int idPlayer) {
		return findPlayerByID(idPlayer).getExile().toStringVector();
	}
	
	/**
	 * Objects on the stack (spells and abilities) 
	 */
	public Vector<String> dumpStack() {
		return _stack.toStringVector();
	}
	
	/**
	 * Objects in the command zone (Emblems ...) 
	 */
	public Vector<String> dumpCommandForPlayer(int idPlayer) {
		return findPlayerByID(idPlayer).getCommand().toStringVector();
	}
	
	/**
	 * Counters on permanents 
	 */
	public Vector<String> dumpCounters() {
		Vector<String> ret = new Vector<String>();
		// For permanents
		for (MtgObject o : _battlefield.getObjects()) {
			Card c = (Card)o;
			if (c.hasCounters())
				ret.add("#" + c.getID() + "|" + c.printCounters());
		}
		// For cards in exile (like suspended cards)
		for (Player p : _players) {
			for (MtgObject o : p.getExile().getObjects()) {
				Card c = (Card)o;
				if (c.hasCounters())
					ret.add("#" + c.getID() + "|" + c.printCounters());
			}	
		}
		return ret;
	}
	
	/**
	 * Creatures that can be declared as attackers
	 */
	public Vector<String> dumpAvailableAttackers() {
		Vector<String> ret = new Vector<String>();
		String bDeclared;
		for (MtgObject obj : _battlefield.getObjects()) {
			Card card = (Card) obj;
			if (card.canAttack(this))
			{
				if (_attackers.contains(card))
					bDeclared = "!";
				else
					bDeclared = "?";
				ret.add(String.format("%d", card.getID()) + bDeclared);
			}
			else
				continue;
		}
		return ret;
	}
	
	/**
	 * Creatures that can be declared as blockers
	 */
	public Vector<String> dumpAvailableBlockers() {
		Vector<String> ret = new Vector<String>();
		String bDeclared;
		for (MtgObject obj : _battlefield.getObjects()) {
			Card card = (Card) obj;
			if (card.canBlock(this) && (countBlockableCreatures(card) > 0))
			{
				if (_blockers.contains(card))
					bDeclared = "!";
				else
					bDeclared = "?";
				ret.add(String.format("%d", card.getID()) + bDeclared);
			}
			else
				continue;
		}
		return ret;
	}
	
	/**
	 * The order in which the attacking creature will assign combat damage to creatures blocking it
	 * @return
	 */
	public Vector<String> dumpBlockersOrder() {
		Vector<String> ret = new Vector<String>();
		String blockEntry;
		int nbBlockers;
		
		for (Card attacker : _attackers) {
			nbBlockers = 0;
			blockEntry = "";
			if (attacker.getBlockers() != null) {
				for (Card blocker : attacker.getBlockers()) {
					if (nbBlockers > 0)
						blockEntry += ",";
					blockEntry += String.format("%d", blocker.getID());
					nbBlockers++;
				}	
			}
			ret.add(blockEntry);
		}
		return ret;
	}

	
	/**
	 * Actions that can be performed
	 */
	public Vector<String> dumpActions() {
		Vector<String> ret = new Vector<String>();
		for (PerformableAction action : _performableActions)
			ret.add(action.getSystemName());
		return ret;
	}
	
	
	/**
	 * When an activated ability that is a mana ability needs a choice (i.e. Adarkar Wastes),
	 * we send the list of choices.
	 * @return
	 */
	public Vector<String> dumpManaChoices() {
		Vector<String> ret = new Vector<String>();
		if (_currentManaAbility != null) {
			if (_currentManaAbility.getManaChoices() != null) {
				Vector<Vector<ManaType>> choices = _currentManaAbility.getManaChoices();
				if (choices.size() > 1) {
					int nbChoices = choices.size();
					for (int iChoice = 0; iChoice < nbChoices; iChoice++) {
						ret.add(String.format("%s", choices.get(iChoice)));
					}
				}
			}
		}
		return ret;
	}
 
	
	/**
	 * Available targets for target number iTarget for the object on top of the stack
	 */
	public Vector<String> dumpAvailableTargets(int iTarget) {
		Vector<String> ret = new Vector<String>();
		Vector<MtgObject> targets = _topStackObject.getAvailableTargets(this, iTarget);
		for (MtgObject target : targets)
			ret.add(String.format("%d", target.getID()));
		return ret;
	}
	
	/**
	 * Available hosts for an Aura
	 */
	public Vector<String> dumpAvailableHosts() {
		Vector<String> ret = new Vector<String>();
		Vector<MtgObject> targets = _currentAura.getAvailableTargets(this, 0);
		for (MtgObject target : targets)
			ret.add(String.format("%d", target.getID()));
		return ret;
	}
	
	/**
	 * Available attacking creatures that current* blocking creature can block
	 * (*defined by _blockingCreature)
	 * 
	 * @return
	 */
	public Vector<String> dumpAvailableCreaturesToBlock() {
		Vector<String> ret = new Vector<String>();
		
		for (Card attacker : _attackers) {
			if (_blockingCreature.canBlockAttacker(this, attacker)) {
				ret.add(String.format("%d", attacker.getID()));
			}
			else
				continue;
		}		
		return ret;
	}

	/**
	 * Available recipients (players and planeswalkers) that current* attacking creature can attack
	 * (*defined by _attackingCreature)
	 * 
	 * @return
	 */
	public Vector<String> dumpAvailableRecipientsToAttack() {
		Vector<String> ret = new Vector<String>();
		
		// adding defending player
		ret.add(String.format("%d", _activePlayer.getOpponent().getID()));
		
		// adding planeswalkers controlled by defending player
		for (Card permanent : _battlefield.getObjectsControlledBy(_activePlayer.getOpponent().getID())) {
			if (permanent.isPlaneswalkerCard())
				ret.add(Integer.toString(permanent.getID()));
		}
		return ret;
	}

	/**
	 * Attacking creatures
 	 * each entry is fomatted like this : id_attacker->id_recipient
	 * ex: 77->158  means creature with idCard 77 is attacking recipient (player or planeswalker) with ID 158
	 */
	public Vector<String> dumpDeclaredAttackers() {
		Vector<String> ret = new Vector<String>();
		Damageable recipient;
		
		for (Card attacker : _attackers)
		{
			if (attacker.isCreature(this) && attacker.isOTB(this))
			{
				recipient = attacker.getAttackRecipient();
				if (recipient != null)
					ret.add(String.format("%d->%d", attacker.getID(), attacker.getAttackRecipient().getID()));
				else
					ret.add(String.format("%d", attacker.getID()));
			}
		}
		return ret;
	}
	
	/**
	 * each entry is fomatted like this : id_blocker->id_attacker
	 * ex: 25->42  means creature with idCard 25 blocks creature with idCard 42
	 * 
	 * 
	 * @return
	 */
	public Vector<String> dumpDeclaredBlocks() {
		Vector<String> ret = new Vector<String>();
		Vector<Card> blockedCreatures;
		
		for (Card blockingCreature : _blockers)
		{
			if (blockingCreature.isOTB(this))
			{
				blockedCreatures = blockingCreature.getBlockedCreatures();
				if (blockedCreatures != null)
				{
					for (Card blockedCreature : blockingCreature.getBlockedCreatures())
					{
						if (blockedCreature.isOTB(this))
							ret.add(String.format("%d->%d", blockingCreature.getID(), blockedCreature.getID()));
					}	
				}
				else
					ret.add(String.format("%d", blockingCreature.getID()));
			}
		}
		return ret;
	}

	public int getTarmoBonusCount() {
		int nbTypes = 0;
		boolean bArti = false;
		boolean bCrea = false;
		boolean bEnch = false;
		boolean bInst = false;
		boolean bLand = false;
		boolean bPlan = false;
		boolean bSorc = false;
		boolean bTrib = false;
		
		Vector<MtgObject> allGraveyards = new Vector<MtgObject>();
		
		for (Player p : _players)
			allGraveyards.addAll(p.getGraveyard().getObjects());

		for (MtgObject o : allGraveyards) {
			Card card = (Card) o;
			
			if (card.isArtifact(this) && !bArti) {
				nbTypes++;
				bArti = true;
			}
			if (card.isCreatureCard() && !bCrea) {
				nbTypes++;
				bCrea = true;
			}
			if (card.isEnchantmentCard() && !bEnch) {
				nbTypes++;
				bEnch = true;
			}
			if (card.isInstantCard() && !bInst) {
				nbTypes++;
				bInst = true;
			}
			if (card.isLandCard() && !bLand) {
				nbTypes++;
				bLand = true;
			}
			if (card.isPlaneswalkerCard() && !bPlan) {
				nbTypes++;
				bPlan = true;
			}
			if (card.isSorceryCard() && !bSorc) {
				nbTypes++;
				bSorc = true;
			}

			if (card.isTribalCard() && !bTrib) {
				nbTypes++;
				bTrib = true;
			}

		}
		
		return nbTypes;
	}

	public void setKeepHand(int _idPlayer, boolean value) {
		findPlayerByID(_idPlayer).setKeepHand(value);
	}

	public void transformCreatureToPlaneswaler(Card creatureFace) {
		if (!creatureFace.isOTB(this))
			return;
		Card planeswalkerFace = CardFactory.create(creatureFace.getNightFaceCard());
		planeswalkerFace.setController(this, creatureFace.getOwner());
		planeswalkerFace.setOwner(creatureFace.getOwner());
		
		// 1. exile creature face and remove it completely
		move_BFD_to_EXL(creatureFace);
		creatureFace.getOwner().getExile().removeObject(creatureFace);
		
		// 2 .create PW face and put it onto the Battlefield
		creatureFace.getOwner().getExile().addObject(planeswalkerFace);
		move_EXL_to_BFD(planeswalkerFace);
	}
	
	public boolean getDebugMode() {
		return _bDebugMode;
	}

	public Response toggleDebugMode() {
		if (_bDebugMode)
			_bDebugMode = false;
		else
			_bDebugMode = true;
		return Response.OK;
	}
	
	public Response requestCardInfo(int idPlayer, int iCard) {
		_requestCardInfo_IdPlayer = idPlayer;
		_requestCardInfo_IdCard = iCard;
		return Response.OK;
	}

	public int getRequestCardInfo_player() {
		return _requestCardInfo_IdPlayer;
	}

	private boolean checkXvaluePlaneswalker(int xValue) {
		if (_topStackObject instanceof ActivatedAbility) {
			ActivatedAbility ability = (ActivatedAbility) _topStackObject;
			Card source = ability.getSource();
			if (ability.isLoyalty() &&   // it is a loyalty ability
					ability.hasXValue() &&  // the loyalty cost is -X
					(xValue > source.getNbCountersOfType(this, CounterType.LOYALTY)))  // X is > the number of counters
				return false;
		}
		return true;
	}
	
	public Zone findZone(StackObject so) {
		for (Zone zone : _zones) {
			if (zone.contains(so))
				return zone;
		}
		return null; // should never get here
	}
	
	private boolean checkXvalue(int xValue) {
		if (xValue < 0)
			return false;

		/* Mishra's Helix : X cannot be more than the number of targetable lands on the battlefield */
		if (_topStackObject.getName().equals("Mishra's Helix")) {
			int nbLands = _battlefield.getTargetableObjectsOfType(CardType.LAND, _topStackObject).size();
			if (xValue > nbLands)
				return false;
		}
		
		/* Recantation, etc : X cannot be more than the number of counters */
		if (_topStackObject.getName().equals("Recantation") || _topStackObject.getName().equals("Rumbling Crescendo") || _topStackObject.getName().equals("Vile Requiem")) {
			if (xValue > _topStackObject.getSource().getNbCountersOfType(this, CounterType.VERSE))
				return false;
		}
		
		if (_topStackObject.getName().equals("Arc Lightning")) {
			if (!((xValue == 1) || (xValue == 2) || (xValue == 3)))
				return false;
		}
		
		// A loyalty ability with X is not valid if the planeswalker has less than X loyalty counters on it
		if (!checkXvaluePlaneswalker(xValue))
			return false;
		
		// In the case of Liliana, Defiant Necromancer 2nd ability (-X : Zombify a creature with CMC = X), make sure there are
		// legal targets for the specified X value.
		if (_topStackObject.getName().startsWith("Liliana, Defiant Necromancer")) {
			Vector<Card> cards = _topStackObject.getController(this).getGraveyard().getCreatureCards();
			boolean bFound = false;
			for (Card card : cards) {
				if ((card.getConvertedManaCost(this) == xValue) && !card.hasSupertype(Supertype.LEGENDARY)) {
					bFound = true;
					break;
				}
			}
			if (!bFound)
				return false;
		}
		return true;
	}
	
	public Response assignIntegerStaticETB(int number) { // Phyrexian Processor
		Response ret;
		_currentSA.getSource().setXValue(number);
		ret = _currentSA.doEffect(this);
		if (ret == Response.MoreStep)
			return ret;
		else {
			finishSwitchZone(_stack, _battlefield, _currentSA.getSource());
			enterBattlefield(_currentSA.getSource());
			return givePriority(_activePlayer);
		}
	}

	
	public Response assignColorStaticETB(Color color) { // Utopia Sprawl
		Response ret;
		_currentSA.getSource().setChosenColor(color);
		ret = _currentSA.doEffect(this);
		if (ret == Response.MoreStep)
			return ret;
		else {
			finishSwitchZone(_stack, _battlefield, _currentSA.getSource());
			enterBattlefield(_currentSA.getSource());
			return givePriority(_activePlayer);
		}
	}

	
	public void setCurrentManaAbility(StackObject so) {
		_currentManaAbility = so;
	}
	
	public Response assignManaChoice(int number) {
		_currentManaAbility.setAdditionalData(number);
		Effect.addMana(this, _currentManaAbility);
		return givePriority(_playerWithPriority);
	}
	
	public Response assignTriggeredManaAbilityColor(int number) {
		_currentManaAbility.setAdditionalData(number);
		return doTriggeredManaAbilities();
	}
	
	public Response assignIntegerResolution(int number) {
		_topStackObject.setXValue(number);
		return resolveTopObject();
	}
	
	public Response assignXValue(int iCommand) {
		if (checkXvalue(iCommand) == false)
			return Response.ErrorInvalidCommand;
		_topStackObject.setXValue(iCommand);
		
		// In the case where X is a number of life, check that the player has enough life to pay for
		if (_topStackObject.getCost().requiresAdditionalCost(Requirement.PAY_X_LIFE) && (_topStackObject.getController(this).getLife() < _topStackObject.getXValue()))
			return Response.ErrorCannotPayCost;
		
		return resolvableCastModes();
	}
	
	public Response assignMode(int iCommand) {
		if (!_topStackObject.isModal() || (iCommand <= 0) || (iCommand > _topStackObject.getNbModes()))
			return Response.ErrorInvalidCommand;
		_topStackObject.setChosenMode(iCommand);
		return resolvableCastTargeting();
	}
	
	public boolean isOver() {
		return _bGameOver;
	}

	public void attemptToDrawEmptyLib() {
		_bDrawEmptyLib = true;
	}

	/**
	 * Create a Clue token
	 * @param source The effect that created the token.
	 * @param controller The controller of the token.
	 */
	public void investigate(Card source, Player controller) {
		createSingleToken(Token.ARTIFACT_CLUE, source, controller);
	}

	public void setReboundSpell(Card source) {
		_reboundSpell = source;
	}

	public void copySpell(Card target, StackObject source) {
		_spellCopy = target.copy(this, source);
	}
	
	public int getHighestLifeTotal() {
		int highestLifeTotal = 0;
		for (Player p : _players) {
			if (p.getLife() >= highestLifeTotal)
				highestLifeTotal = p.getLife();
		}
		return highestLifeTotal;
	}

	public Zone getSpecialZone() {
		return _special;
	}

	public Response validateBlockersOrder() {
		_activePlayer.setState(State.Ready);
		_bBlockersValidated = true;
		checkStateBasedActions();
		if (!_queuedTriggeredAbilities.isEmpty())
			putTriggeredAbilitiesOnStack(_queuedTriggeredAbilities);
		return Response.OK;
	}

	public Response castWithoutPayingManaCost(Card spell) {
		_topStackObject = spell;
		SpellCast sc = new SpellCast(spell, Option.CAST, null, null);
		sc.setWithoutPayingManaCost();
		spell.setSpellCastOptionUsed(sc);
		return resolvableCastCheckTiming();
	}

	public Response castUsingSuspend(Card spell) {
		_topStackObject = spell;
		SpellCast sc = new SpellCast(spell, Option.CAST_USING_SUSPEND, null, null);
		sc.setWithoutPayingManaCost();
		spell.setSpellCastOptionUsed(sc);
		return resolvableCastCheckTiming();
	}

	
	public Vector<Card> getAttackers() {
		return _attackers;
	}
	
	public Vector<Card> getBlockers() {
		return _blockers;
	}

	
	
	/**
	 * Yes = land, No = nonland
	 * @param playerId
	 * @param answer
	 * @return
	 */
	public Response assignAbundanceResponse(int playerId, Answer answer) {
//		Player p = findPlayerByID(playerId);
//		Library lib = p.getLibrary();
//		Vector<Card> revealed = new Vector<Card>();
//		Card c = null;
//		int i = 0;
//		
//		do {
//			c = lib.getObjectAt(i);
//			revealed.add(c);
//			if (((answer == Answer.Yes) && c.isLand(this)) || ((answer == Answer.No) && !c.isLand(this)))
//				break;
//			i++;
//		} while (i < p.getLibrary().size());
//
//		System.out.println(p.getName() + " reveals " + revealed + ".");
//		
//		// If a card was found, put it in hand
//		if (c != null)
//			move_LIB_to_HND(c);
//		
//		// Put revealed cards in the bottom
//		for (Card re : revealed) {
//			if (re != c)
//				move_LIB_to_BOTTOMLIB(re);
//		}
//		
//		_nbCardsToDraw--;
//		if (_nbCardsToDraw > 0)
//			return drawCard(p);
//		else {
//			if (_stack.isEmpty())
//				return givePriority(_activePlayer);				
//			else
//				return resolveTopObject();	
//		}
		return Response.OK;
	}
	
	public Response assignReplaceDrawResponse(int playerId, Answer answer) {
//		Player p = findPlayerByID(playerId);
//		if (answer == Answer.Yes) {
//			if (_replaceDrawEffect.getAbilityName().equals("abundance"))
//				p.setState(State.PromptAbundance_CardType);
//			return Response.MoreStep;
//		}
//		else
//			return doNormalDraw(p);
		return Response.OK;
	}

	public Response setAlternateCostChoice(int playerId, Vector<Integer> choices) {
		Player p = findPlayerByID(playerId);
		_effectChoice = new Choice(p, choices);
		if (this.validateChoices()) {
			if (_currentAC.pay(this, _playerWithPriority) == Response.OK)
				return resolvableCastPayCosts();	
		}

		return Response.MoreStep;
	}

	public void checkStateBasedActions() {
		// 704.5a If a player has 0 or less life, he or she loses the game.
		checkPlayerLife();
		
		// 704.5b If a player attempted to draw a card from a library with no cards in it since the last time state-based actions were checked, he or she loses the game.
		checkDrawEmptyLibrary();
		
		// 704.5c If a player has ten or more poison counters, he or she loses the game.
		checkPoisonCounters();
		
		// 704.5e If a copy of a spell is in a zone other than the stack, it ceases to exist. If a copy of a card is in any zone other than the stack or the battlefield, it ceases to exist.
		// TODO
		
		// 704.5f If a creature has toughness 0 or less, it’s put into its owner’s graveyard. Regeneration can’t replace this event.
		checkZeroToughness();
		
		// 704.5g If a creature has toughness greater than 0, and the total damage marked on it is greater than or equal to its toughness, that creature has been dealt lethal damage and is destroyed. Regeneration can replace this event
		checkLethalDamage();				
		
		// 704.5h If a creature has toughness greater than 0, and it’s been dealt damage by a source with deathtouch since the last time state-based actions were checked, that creature is destroyed. Regeneration can replace this event.
		checkDeathtouchDamage();
		
		// 704.5d If a token is phased out, or is in a zone other than the battlefield, it ceases to exist.
		checkTokenExistence();
		
		// 704.5i If a planeswalker has loyalty 0, it’s put into its owner’s graveyard.
		checkLoyalty();
		
		// 704.5j If a player controls two or more legendary permanents with the same name, that player chooses one of them, and the rest are put into their owners’ graveyards. This is called the “legend rule.”
		checkLegendRule();
		
		// 704.5k If two or more permanents have the supertype world, all except the one that has had the world supertype for the shortest amount of time are put into their owners’ graveyards. In the event of a tie for the shortest amount of time, all are put into their owners’ graveyards. This is called the “world rule.”
		// TODO
		
		// 704.5m If an Aura is attached to an illegal object or player, or is not attached to an object or player, that Aura is put into its owner’s graveyard.
		checkAurasLegality();
		
		// 704.5n If an Equipment or Fortification is attached to an illegal permanent, it becomes unattached from that permanent. It remains on the battlefield.
		checkEquipIllegalPermanent();
		
		// 704.5p If a creature is attached to an object or player, it becomes unattached and remains on the battlefield. Similarly, if a permanent that’s neither an Aura, an Equipment, nor a Fortification is attached to an object or player, it becomes unattached and remains on the battlefield.
		// TODO
		
		// 704.5q If a permanent has both a +1/+1 counter and a -1/-1 counter on it, N +1/+1 and N -1/-1 counters are removed from it, where N is the smaller of the number of +1/+1 and -1/-1 counters on it.
		checkPlusOneMinusOneCounters();
		
		// 704.5r If a permanent with an ability that says it can’t have more than N counters of a certain kind on it has more than N counters of that kind on it, all but N of those counters are removed from it.
		// TODO
		
		// 704.5s If the number of lore counters on a Saga permanent is greater than or equal to its final chapter number and
		//        it isn’t the source of a chapter ability that has triggered but not yet left the stack, that Saga’s controller sacrifices it.
		checkSacrificeSagas();
		
		// special one for Dark Depths (couldn't find another way than using a state based action)
		checkStateTriggerEffects();
	}
	
	private void checkDrawEmptyLibrary() {
		if (_bDrawEmptyLib)
			_bGameOver = true;
	}
	
	private void checkPlayerLife() {
		for (Player p : _players) {
			if (p.getLife() < 1)
				_bGameOver = true;
		}
	}
	
	private void checkPoisonCounters() {
		for (Player p : _players) {
			if (p.getNbCounters(CounterType.POISON) >= 10)
				_bGameOver = true;
		}
	}
	
	
	
	private boolean isSagaBeingResolved(Card saga) {
		for (TriggeredAbility ta : _queuedTriggeredAbilities) {
			if (ta.getSource() == saga)
				return true;
		}
		for (StackObject so : _stack.getStackObjects()) {
			if (so.getSource() == saga)
				return true;
		}
		return false;
	}
	
	private void checkSacrificeSagas() {
		for (Card saga : _battlefield.getSagas())
		{
			if (saga.getNbCountersOfType(this, CounterType.LORE) >= 3)
			{
				if (!isSagaBeingResolved(saga))
				{
					sacrifice(saga.getController(this), saga);
				}
			}
		}
	}

	private void checkLegendRule() {
		for (Player p : _players) {
			if (p.controls2LegendaryPermanentsWithSameName())
				System.out.println("LEGEND RUUULLLLEE !!!!");
		}
	}

	/**
	 * 
	 * @param source
	 * @return
	 */
	private boolean didStateEffectTrigger(Card source, String effectName) {
		for (TriggeredAbility trig : _queuedTriggeredAbilities) {
			if (trig.getAbilityName().equals(effectName) && (trig.getSource() == source))
				return true;
		}
		
		Vector<MtgObject> abilities = _stack.getAbilities();
		for (MtgObject ability : abilities) {
			if (ability instanceof TriggeredAbility) {
				TriggeredAbility trig = (TriggeredAbility) ability;
				if (trig.getAbilityName().equals(effectName) && (trig.getSource() == source))
					return true;	
			}
		}
		return false;
	}
	
	private void checkStateTriggerEffects() {
		Vector<Card> permanents = _battlefield.getPermanents();
		Vector<TriggeredAbility> trigs;
		boolean bTrigger;
		
		for (Card permanent : permanents) {
			/* darkDepths_putToken */
			if (permanent.hasTriggeredAbility("darkDepths_putToken")) {
				if (!permanent.hasCounters(CounterType.ICE)) {
					trigs = permanent.getTriggeredAbilities(this, Event.NoIceCounters, Origin.BTLFLD);
					if (!didStateEffectTrigger(permanent, "darkDepths_putToken"))
						queueTriggeredAbilities(trigs, permanent);
				}
			}
			
			/* veiledCrocodile */
			else if (permanent.hasTriggeredAbility("veiledCrocodile")) {
				bTrigger = false;
				for (Player p : _players) {
					if (p.getHandSize() == 0)
						bTrigger = true;
				}
				
				if (bTrigger) {
					trigs = permanent.getTriggeredAbilities(this, Event.APlayerHasNoCardInHand, Origin.BTLFLD);
					if (!didStateEffectTrigger(permanent, "veiledCrocodile"))
						queueTriggeredAbilities(trigs, permanent);
				}
			}
			
			/* hiddenPredators */
			else if (permanent.hasTriggeredAbility("hiddenPredators")) {
				Player controller = permanent.getController(this);
				Player opponent = controller.getOpponent();
				bTrigger = false;
				
				for (Card creature : _battlefield.getCreaturesControlledBy(opponent)) {
					if (creature.getPower(this) >= 4) {
						bTrigger = true;
						break;
					}
				}
				
				if (bTrigger) {
					trigs = permanent.getTriggeredAbilities(this, Event.AnOpponentControlsCreaturePower4, Origin.BTLFLD);
					if (!didStateEffectTrigger(permanent, "hiddenPredators"))
						queueTriggeredAbilities(trigs, permanent);
				}
			}
		}
	}

	// Check that Auras are legally attached to their host 
	private void checkAurasLegality() {
		Card permanent;

		for (int i = 0; i < _battlefield.getObjects().size(); i++) {
			permanent = (Card) _battlefield.getObjectAt(i);
			if (permanent.hasSubtypeGlobal(this, Subtype.AURA)) {
				Card aura = permanent;
				Card host = aura.getHost();
				
				// If an Aura is not attached it is moved from the battlefield into its owner's graveyard
				if (host == null) {
					move_BFD_to_GYD(aura);
					i--;
					continue;
				}
				
				// If an Aura is attached to a wrong host it is moved from the battlefield into its owner's graveyard
				if ((aura.hasStaticAbility("enchantCreature") && !host.isCreature(this)) || 
						(aura.hasStaticAbility("enchantForest") && !host.hasSubtypeGlobal(this, Subtype.FOREST)) || 
						(aura.hasStaticAbility("enchantSwamp") && !host.hasSubtypeGlobal(this, Subtype.SWAMP)))
				{
					move_BFD_to_GYD(aura);
					i--;
					continue;
				}
			}
		}
	}
	
	// If an Equipment equips a permanent that is not a creature, unattach it
	private void checkEquipIllegalPermanent() {
		for (MtgObject obj : _battlefield.getObjects()) {
			Card permanent = (Card) obj;
			if (permanent.isArtifact(this) && permanent.hasSubtypeGlobal(this, Subtype.EQUIPMENT) && (permanent.getHost() != null)) {
				Card host = permanent.getHost();
				if (!host.isCreature(this))
					host.unattach(permanent);
			}
		}
	}
	
	/* If a permanent has both a +1/+1 counter and a -1/-1 counter on it, N +1/+1 and N -1/-1 counters are removed from it,
	** where N is the smaller of the number of +1/+1 and -1/-1 counters on it.
	*/
	private void checkPlusOneMinusOneCounters() {
		for (MtgObject obj : _battlefield.getObjects()) {
			Card permanent = (Card) obj;
			if (permanent.hasCounters(CounterType.PLUS_ONE) && permanent.hasCounters(CounterType.MINUS_ONE)) {
				int nbCountersToRemove = Math.min(permanent.getNbCountersOfType(this, CounterType.PLUS_ONE), permanent.getNbCountersOfType(this, CounterType.MINUS_ONE));
				permanent.removeCounter(this, CounterType.PLUS_ONE, nbCountersToRemove);
				permanent.removeCounter(this, CounterType.MINUS_ONE, nbCountersToRemove);
			}
		}
	}
	
	private void checkLoyalty() {
		Card permanent;

		for (int i = 0; i < _battlefield.getObjects().size(); i++) {
			permanent = (Card) _battlefield.getObjectAt(i);
			if (permanent.isPlaneswalkerCard() && (permanent.getLoyalty(this) <= 0))
			{
				move_BFD_to_GYD(permanent);
				i--;
			}
		}
	}
	
	private boolean checkTokenExistenceInZone(Zone zone) {
		Iterator<MtgObject> it = zone.getObjects().iterator();
		Card card;
		boolean ret = false;
		
		while (it.hasNext())
		{
			card = (Card) it.next();
			if (card.isToken()) {
				it.remove();
				ret = true;
			}
		}
		return ret;
	}
	
	private void checkTokenExistenceForPlayer(Player player) {
		checkTokenExistenceInZone(player.getLibrary());
		checkTokenExistenceInZone(player.getGraveyard());
		checkTokenExistenceInZone(player.getExile());
		checkTokenExistenceInZone(player.getHand());
	}
	
	/**
	 * Check tokens in a zone other than the battlefield cease to exist
	 * 
	 */
	private void checkTokenExistence() {
		for (Player p : _players)
			checkTokenExistenceForPlayer(p);
	}
	
	/**
	 *  Check for lethal damage on permanents and destroy them if necessary (regeneration may apply)
	 **/
	private void checkLethalDamage() {
		Card p;
		
		for (int i = 0; i < _battlefield.getObjects().size(); i++) {
			p = (Card) _battlefield.getObjectAt(i);
			if ((p.isCreature(this) && (p.getDamage() > 0) && (p.getDamage() >= p.getToughness(this)))) {
				if (destroy(p, true) == Response.OK)
					i--;
			}
		}
	}
	
	/**
	 *  Check for damage from a source with deattouched on permanents and destroy them if necessary (regeneration may apply)
	 **/
	private void checkDeathtouchDamage() {
		Card p;
		
		for (int i = 0; i < _battlefield.getObjects().size(); i++) {
			p = (Card) _battlefield.getObjectAt(i);
			if ((p.isCreature(this) && p.isDeathtouched())) {
				if (destroy(p, true) == Response.OK)
					i--;
			}
		}
	}
	
	/**
	 *  Check for creatures with toughness <= 0 and put them in their owner's yard
	 **/
	private void checkZeroToughness() {
		Card permanent;

		for (int i = 0; i < _battlefield.getObjects().size(); i++) {
			permanent = (Card) _battlefield.getObjectAt(i);
			if (permanent.isCreature(this) && (permanent.getToughness(this) <= 0)) {
				move_BFD_to_GYD(permanent);
				i--;
			}
		}
	}

	
};
