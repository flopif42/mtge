package mtgengine.ability;

import java.util.HashMap;
import java.util.Vector;

import mtgengine.Game;
import mtgengine.StackObject;
import mtgengine.TargetRequirement;
import mtgengine.card.Card;
import mtgengine.cost.Cost;
import mtgengine.cost.ManaCost;
import mtgengine.effect.Effect;

public class TriggeredAbility extends StackObject implements ManaAbility {
	public enum Event {
		// Stuff entering the battlefield
		EntersTheBattlefield,
		EntersTheBattleFieldOrDies,
		EntersTheBattleFieldOrAttacks,
		ALandEntersBattlefieldUnderYourControl,
		ACreatureEntersBattlefieldUnderYourControl,
		ACreatureEntersBattlefield,
		ThisOrAnotherEnchantETB,  // Journey into Nyx "Constellation" creatures
		
		// Stuff leaving the battlefield
		LeavesTheBattlefield,
		
		// Stuff dying
		Dies, ACreatureDies, ANonTokenPermanentDies, AGreenCreatureDies,
		ACreatureYouControlDies,
		ANonTokenCreatureYouControlDies,
		AnotherCreatureYouControlDies,
		AnotherNonTokenCreatureYouControlDies,
		AnonAngelCreatureYouControlDies,
		
		// Stuff being put into the graveyard from anywhere (i.e. Emrakul)
		PutIntoAGraveyardFromAnywhere,  // Emrakul
		ACardPutInYourGydFromAnywhere,  // Energy Field
		ACardPutInAnyGydFromAnywhere,   // Planar Void
		
		// Stuff attacking or blocking
		Attacks,
		Blocks,
		AttacksOrBlocks,
		ACreatureYouControlAttacks,
		ACreatureYouControlAttacksAlone,
		BecomesBlockedByACreature,
		BlocksOrBecomesBlocked,
		
		// Stuff being discarded
		DiscardedByOpponent,   // i.e. Metrognome, Dodecapod, etc...
		YouDiscard,            // Necropotence
		
		// Beginning/End of phases / steps
		BegOfEachUpkeep,   // i.e. Werewolves transforming
		BegOfYourUpkeep,       // i.e. Nether Spirit
		BegOfEachDrawStep,     // no cards yet
		BegOfYourDrawStep,	   // i.e. Grafted Skullcap
		BegOfYourMainPhase,    // i.e. Carpet of Flowers
		BegOfEachEndStep,      // i.e. Creatures like Ball Lightning
		BegOfYourEndStep,      // i.e. Manabond
		BegOfYourCombatStep,   // i.e. Toolcraft Exemplar
		EndOfCombat,		   // i.e. Geist of Saint Traft
		BegOfNextCleanupStep,  // i.e. Waylay
		BegOfEnchantedCreatureControllerUpkeep,
		
		// Spell casting
		// You
		YouCastASpell,
		YouCastThisSpell,   // Cascade
		YouCastCreatureSpell,
		YouCastNonCreatureSpell,
		YouCastEnchantmentSpell,
		YouCastInstantOrSorcerySpell,
		YouCastYourNextInstantOrSorcerySpellThisTurn,
		// An opponent
		AnOpponentCastSpell,
		AnOpponentCastEnchantSpell,
		AnOpponentCastArtifactSpell,
		AnOpponentCastCreatureSpell,
		AnOpponentCastCreatureSpellFlying,
		AnOpponentCastWhiteSpell,
		
		// Any player
		AnyPlayerCastASpell,
		AnyPlayerCastAGreenSpell,
		AnyPlayerCastACreatureSpell,
		AnyPlayerCastAnEnchantSpell,


		// Damage related stuff
		// any damage
		ThisDealsDamage,
		ThisDealsDmgToAPlayer,
		ThisDealsDamageToCreatureOrOpponent,
		EnchantedCreatureDealsDmgToACreature,
		// combat damage
		ThisDealsCbtDmg,
		ThisDealsCbtDmgToAPlayer,
		ThisDealsCbtDmgToACreature,
		EquippedCreatureDealsCbtDmg,				// Jitte
		EquippedCreatureDealsCbtDmgToAPlayer,		// Swords of A and B
		EnchantedCreatureDealsCbtDmgToAPlayer,
		// damage receiving
		IsDealtDamage,
		
		// Life gaining
		YouGainLife, AnOpponentGainsLife,
	    
	    // Misc
		LoreCountersAdded, // Sagas (From the Dominaria set)
		MonarchWasDamaged,
		IsTurnedFaceUp, TransformsInto,
		LastTimeCounterRemoved, 
	    BecomesTapped, EnchantedLandBecomesTapped,
	    SacrificeClue,
	    EnchantedLandIsTappedForMana,
	    BecomesTheTargetOfSpellOrAb,
	    AnOpponentPlaysLand,
	    AnOpponentPlaysNonbasicLand,
	    APlayerTapsIslandForMana, APlayerTapsForestForMana, 
	    ACreatureWithFungusDies,
	    CrewsAVehicle,
	    ACreatureYouControlExplores,
	    
	    // State trigger
	    AnOpponentControlsCreaturePower4,
	    NoIceCounters,
	    APlayerHasNoCardInHand
	};
		               
	public enum Origin { BTLFLD, GRVYRD, EXILE, HAND, COMMAND, CONTINUOUS_EFFECT, STACK, ANYWHERE };

	public enum YouMayChoiceMaker { AbilityController, AdditionalDataController };
	private boolean _bSubAbility = false;  // If this is true, it means the ability is part of a main ability (i.e. for Vanishing wich
									       // which consists of a set of 2 triggered abilities.)
	private Event _event = null;
	private InterveningIfClause _interveningIf = null;
	private Card _source;
	private String _parameter;
	private Effect _effect;
	private Origin _origin = Origin.BTLFLD; // where the permanent must be for the ability to trigger (for example, do not
											// trigger Squee's ability if it's on the battlefield, or do not trigger
											// Carnophage's ability if the card is in the graveyard.)
	/**
	 * 
	 * @param name
	 * @param source
	 */
	public TriggeredAbility(String name, Card source, Cost cost, String parameter) {
		super(name);
		_source = source;
		_cost = cost;
		_parameter = parameter;
	}
	
	public String getParameter() {
		return _parameter;
	}
	
	public boolean isSubAbility() {
		return _bSubAbility;
	}
	
	public void initialize(String methodName, String desc, Event event) {
		initialize(methodName, desc, event, false);
	}
	
	/**
	 * 
	 * @param methodName
	 * @param text
	 * @param event
	 */
	public void initialize(String methodName, String text, Event event, boolean bSubAbility) {
		_effect = new Effect(methodName, text);
		_event = event;
		_bSubAbility = bSubAbility;
	}
	
	public void setOrigin(Origin origin) {
		_origin = origin;
	}
	
	public Origin getOrigin() {
		return _origin;
	}

	public ManaCost getManaCost() {
		if (_cost != null)
			return _cost.getManaCost();
		return null;
	}
	
	/**
	 * Use this method to specify that the ability has an 'intervening if' clause.
	 */
	public void setInterveningIfClause() {
		_interveningIf = new InterveningIfClause(this);
	}
	
	/**
	 * Returns true if the ability has an 'intervening if' clause, false otherwise;
	 * @return
	 */
	public boolean hasInterveningIfClause() {
		return (_interveningIf != null);
	}
	
	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public TriggeredAbility clone() {
		TriggeredAbility ta = new TriggeredAbility(_name, _source, _cost, _parameter);
		ta.initialize(_effect.getMethodName(), _effect.getRulesText(), _event);
		if (_targets != null)
			ta._targets = cloneTargets();
		ta._targetRequirements = (HashMap<Integer, Vector<TargetRequirement>>) _targetRequirements.clone();
		ta._bYouMay = _bYouMay;
		ta._bSubAbility = _bSubAbility;
		ta._nbModes = _nbModes;
		ta._bAnswered = false;
		ta._youMayChoiceMaker = _youMayChoiceMaker; 
		ta._additionalData = _additionalData;
		if (_interveningIf != null)
			ta._interveningIf = _interveningIf.clone(ta);
		return ta;
	}
	
	public boolean validateInterveningIfClause(Game g) {
		if (_interveningIf != null)
			return _interveningIf.validate(g);
		return false;
	}
	
	public  String getSystemName() {
		Card source = _source;
		if (source != null)
			return _source.getSystemName() + "#" + _id + "~" + _effect.getRulesText();
		else
			return "#" + _id + "~" + _effect.getRulesText();
		
	}

	protected int getImageID() {
		return _source.getImageID();
	}

	public String getAbilityName() {
		return _name;
	}
	
	@Override
	public String getName() {
		return _source.getName();
	}
	
	public Event getEvent() {
		return _event;
	}

	@Override
	public boolean isSorcerySpeed(Game g) {
		return false;
	}

	@Override
	public void requiresXValue() {
	}

	@Override
	public boolean hasXValue() {
		return false;
	}

	@Override
	public boolean isTargetableBy(Game g, StackObject so) {
		// All triggered abilities can be targeted
		return true;
	}

	@Override
	public Card getSource() {
		return _source;
	}

	@Override
	public Effect getEffect() {
		return _effect;
	}
	
	public boolean isManaAbility() {
		if (_event == Event.EnchantedLandIsTappedForMana)
			return true;
		return false;
	}
}
