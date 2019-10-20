package mtgengine.ability;

import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mtgengine.CounterType;
import mtgengine.Game;
import mtgengine.action.PerformableAction;
import mtgengine.card.Card;
import mtgengine.cost.AdditionalCost.Requirement;
import mtgengine.cost.Cost;
import mtgengine.cost.LoyaltyCost;
import mtgengine.cost.ManaCost;
import mtgengine.cost.AdditionalCost;
import mtgengine.effect.Effect;
import mtgengine.player.PlayerBuff;
import mtgengine.Game.Step;
import mtgengine.StackObject;
import mtgengine.TargetRequirement;
import mtgengine.zone.Zone;
import mtgengine.type.CreatureType;
import mtgengine.type.Subtype;

public class ActivatedAbility extends StackObject implements PerformableAction, ManaAbility {
	private Zone.Name _availability;
	private boolean _bSorcerySpeed = false;
	private boolean _bOncePerTurn = false;
	private boolean _bOnlyActivableDuringUpkeep = false;
	private boolean _bOnlyActivableWithMetalcraft = false;
	private int _nbActivationsThisTurn = 0;
	private Card _source;
	private boolean _bXValue = false;
	private String _abilityName;
	private Effect _effect;

	// Mana producing abilities granted by basic land types are implied : they do not explicitly appear in the card definition
	private boolean _bImplied = false;
	
	/**
	 * Default constructor
	 * @param name 
	 * @param name Name of the activated ability
	 * @param methodName Method of the Effect class that will be called 
	 * @param desc Ability rules text
	 * @param source The source of the ability
	 * @param avail Zone which the ability can be casted from
	 */
	public ActivatedAbility(String name, Card source, String parameter) {
		super(source.getSystemName());
		_source = source;
		_abilityName = name;
		_parameter = parameter;
	}

	/**
	 * 
	 * @param methodName
	 * @param rulesText
	 * @param avail
	 */
	public void initialize(String methodName, String rulesText, Zone.Name avail) {
		_effect = new Effect(methodName, rulesText);
		_availability = avail;
		initializeCost();
	}
	
	public void initialize(String methodName, String desc) {
		initialize(methodName, desc, Zone.Name.Battlefield);
	}
	
	// Loyalty ability
	public boolean isLoyalty() {
		return (_cost.getLoyaltyCost() != null);
	}
	
	public void setSorcerySpeed() {
		_bSorcerySpeed = true;
	}
	
	public void setOncePerTurn() {
		_bOncePerTurn = true;
	}
	
	public boolean isOncePerTurn() {
		return _bOncePerTurn;
	}
	
	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public ActivatedAbility clone() {
		ActivatedAbility aa = new ActivatedAbility(_abilityName, _source, _parameter);
		aa.initialize(_effect.getMethodName(), _effect.getRulesText(), _availability);
		if (_targets != null)
			aa._targets = cloneTargets();
		aa._targetRequirements =  (HashMap<Integer, Vector<TargetRequirement>>) _targetRequirements.clone();
		aa._bYouMay = _bYouMay;
		aa._bAnswered = false;
//		aa._bBypassStack = _bBypassStack;
		aa._bSorcerySpeed = _bSorcerySpeed;
		aa._bXValue = _bXValue;
		aa._cost = _cost.clone();
		aa._parameter = _parameter;
		aa._nbModes = _nbModes;
		aa._controller = _controller;
		aa._owner = _owner;
		aa._bOncePerTurn = _bOncePerTurn;
		aa._bOnlyActivableDuringUpkeep = _bOnlyActivableDuringUpkeep;
		aa._bOnlyActivableWithMetalcraft = _bOnlyActivableWithMetalcraft;
		aa._nbActivationsThisTurn = _nbActivationsThisTurn;
		return aa;
	}
	public Zone.Name getAvailabilityZone() {
		return _availability;
	}

	public String getSystemName() {
		return _source.getSystemName() + "#" + _id + "~" + _effect.getRulesText();
	}
	
	@Override
	protected int getImageID() {
		return _source.getImageID();
	}

	public String getAbilityName() {
		return _abilityName;
	}
	
	public ManaCost getManaCost() {
		return _cost.getManaCost();
	}
	
	@Override
	public String getName() {
		return _source.getName();
	}

	@Override
	public boolean checkRestrictions(Game g) {
		if (super.checkRestrictions(g) == false)
			return false;
		
		// loyalty abilites ...
		if (isLoyalty()) {
			LoyaltyCost lc = _cost.getLoyaltyCost();
			
			// cannot be activated if one already has been activated for this planeswalker this turn
			if (getSource().hasAlreadyActivatedLoyaltyAbility())
				return false;
			
			// cannot be activated if the number of loyalty counters on the permanent is lower than the loyalty cost
			if (lc.getType() == LoyaltyCost.Type.Remove) {
				int nbCounters = getSource().getNbCountersOfType(g, CounterType.LOYALTY);
				int loyaltyCost = lc.getNumber();
				if (nbCounters < loyaltyCost)
					return false;
			}
		}
		
		// Abilities that can be activated only once per turn ...
		if (isOncePerTurn() && (_nbActivationsThisTurn > 0)) {
			return false;
		}
		
		// abilities that require tap cannot be activated if the source is tapped or the source is a creature with summoning sickness
		if (_cost.requiresAdditionalCost(Requirement.TAP_THIS)) {
			Card source = getSource();
			if (source.isTapped() || !source.canTap(g))
				return false;
		}

		// Crew : this ability requires to tap a number of target creatures with total force >= crew parameter
		if (_cost.requiresAdditionalCost(Requirement.CREW)) {
			try {
				int crewParameter = Integer.parseInt(_parameter);
				g.setCrewRequirement(crewParameter);
				Vector<Card> untappedCreatures = g.getBattlefield().getUntappedCreaturesControlledBy(_controller);
				int totalPower = 0;
				for (Card untappedCreature : untappedCreatures)
					totalPower += untappedCreature.getPower(g);
				if (totalPower < crewParameter)
					return false;
			} catch (NumberFormatException e) {
				System.err.println("Invalid crew parameter : " + _parameter);
				System.exit(1);
			}
		}
		
		// abilities that require to tap an untapped creature
		if (_cost.requiresAdditionalCost(Requirement.TAP_AN_UNTAPPED_CREATURE_YOU_CONTROL)) {
			int nbUntappedCreatures = g.getBattlefield().getUntappedCreaturesControlledBy(_controller).size();
			if (nbUntappedCreatures == 0)
				return false;

			// if there is only one untapped creature and that creature also requires to tap, return false
			// example : When Loam Dryad is the only untapped creature, its ability cannot be activated
			if ((nbUntappedCreatures == 1) && (_cost.requiresAdditionalCost(Requirement.TAP_THIS) && getSource().isCreature(g)))
				return false;
		}
		
		// Heart of Kiran alternate Crew cost (Remove a loyalty counter from a Planeswalker you control)
		if (_cost.requiresAdditionalCost(Requirement.REMOVE_A_LOYALTY_COUNTER)) {
			Vector<Card> planeswalkers = g.getBattlefield().getPlaneswalkersControlledBy(_controller);
			if (planeswalkers.size() == 0)
				return false;
		}
		
		// Abilities that require to remove a +1/+1 counter (like Spike Feeder), can't be activated if the permanent has less than one counter
		if (_cost.requiresAdditionalCost(Requirement.REMOVE_A_PLUS1_COUNTER)) {
			if (getSource().getNbCountersOfType(g, CounterType.PLUS_ONE) < 1)
				return false;
		}

		// Abilities that require to remove a mining counter (Gemstone Mine), can't be activated if the permanent has less than one counter
		if (_cost.requiresAdditionalCost(Requirement.REMOVE_A_MINING_COUNTER)) {
			if (getSource().getNbCountersOfType(g, CounterType.MINING) < 1)
				return false;
		}
		
		// Abilities that require to remove a charge counter (like Umezawa's Jitte), can't be activated if the permanent has less than one counter
		if (_cost.requiresAdditionalCost(Requirement.REMOVE_A_CHARGE_COUNTER)) {
			if (getSource().getNbCountersOfType(g, CounterType.CHARGE) < 1)
				return false;
		}
		
		// Abilities that require to remove a Fade counter (like Saproling Burst), can't be activated if the permanent has less than one counter
		if (_cost.requiresAdditionalCost(Requirement.REMOVE_A_FADE_COUNTER)) {
			if (getSource().getNbCountersOfType(g, CounterType.FADE) < 1)
				return false;
		}
		
		// Abilities that require the controller to discard a creature cannot be activated if controller does not have at least
		// one creature card in hand.
		if (_cost.requiresAdditionalCost(Requirement.DISCARD_A_CREATURE_CARD)) {
			if (_controller.getHand().getCreatureCards().size() < 1)
				return false;
		}

		// Abilities that require the controller to discard a card cannot be activated if controller does not have at least
		// one card in hand.
		if (_cost.requiresAdditionalCost(Requirement.DISCARD_A_CARD)) {
			if (_controller.getHandSize() == 0)
				return false;
		}
		
		// Sacrifice another Vampire or Zombie (Kalitas)
		if (_cost.requiresAdditionalCost(Requirement.SACRIFICE_ANOTHER_VAMPIRE_OR_ZOMBIE)) {
			Vector<Card> permanents = g.getBattlefield().getPermanentsControlledBy(_controller);
			boolean bFound = false;
			for (Card permanent : permanents) {
				if ((permanent != _source) && (permanent.hasCreatureType(g, CreatureType.Vampire) || permanent.hasCreatureType(g, CreatureType.Zombie)))
					bFound = true;
			}
			if (!bFound)
				return false;
		}

		// Sacrifice an enchantment
		if (_cost.requiresAdditionalCost(Requirement.SACRIFICE_AN_ENCHANTMENT)) {
			if (_controller.getNbEnchantmentsControlled() == 0)
				return false;
		}
		
		// Sacrifice a creature
		if (_cost.requiresAdditionalCost(Requirement.SACRIFICE_A_CREATURE)) {
			if (_controller.getNbCreaturesControlled() == 0)
				return false;
		}

		// sacrifice 5 creatures
		if (_cost.requiresAdditionalCost(Requirement.SACRIFICE_FIVE_CREATURES)) {
			if (g.getBattlefield().getCreaturesControlledBy(_controller).size() < 5)
				return false;
		}
		
		// Sacrifice an artifact
		if (_cost.requiresAdditionalCost(Requirement.SACRIFICE_AN_ARTIFACT)) {
			if (_controller.getNbArtifactsControlled() == 0)
				return false;
		}

		// Sacrifice a Goblin
		if (_cost.requiresAdditionalCost(Requirement.SACRIFICE_A_GOBLIN)) {
			Vector<Card> permanents = g.getBattlefield().getPermanentsControlledBy(_controller);
			boolean bFound = false;
			for (Card permanent : permanents) {
				if (permanent.hasCreatureType(g, CreatureType.Goblin))
					bFound = true;
			}
			if (!bFound)
				return false;
		}
		
		// Sacrifice a Forest or Plains (Knight of the Reliquary)
		if (_cost.requiresAdditionalCost(Requirement.SACRIFICE_A_FOREST_OR_PLAINS)) {
			Vector<Card> lands = g.getBattlefield().getLandsControlledBy(_controller);
			boolean bFound = false;
			for (Card land : lands) {
				if (land.hasSubtypeGlobal(g,Subtype.FOREST) || land.hasSubtypeGlobal(g,Subtype.PLAINS))
					bFound = true;
			}
			if (!bFound)
				return false;
		}
		
		// Sacrifice a Forest
		if (_cost.requiresAdditionalCost(Requirement.SACRIFICE_A_FOREST)) {
			Vector<Card> lands = g.getBattlefield().getLandsControlledBy(_controller);
			boolean bFound = false;
			for (Card land : lands) {
				if (land.hasSubtypeGlobal(g, Subtype.FOREST)) {
					bFound = true;
					break;
				}
			}
			if (!bFound)
				return false;
		}
		
		// Scrapheap Scrounger : Exile another creature card from your graveyard
		if (_cost.requiresAdditionalCost(Requirement.EXILE_ANOTHER_CREATURE_CARD_FROM_GYD)) {
			if (_controller.getGraveyard().getCreatureCards().size() < 2)
				return false;
		}
		
		// Griselbrand ability cannot be activated if player's life is less than 7
		if (_cost.requiresAdditionalCost(Requirement.PAY_7_LIFE)) {
			if (_controller.getLife() < 7)
				return false;
		}
		
		// Phyrexian Colossus ability cannot be activated if player's life is less than 8
		if (_cost.requiresAdditionalCost(Requirement.PAY_8_LIFE)) {
			if (_controller.getLife() < 8)
				return false;
		}
		
		// abilities that require one Energy counter
		if (_cost.requiresAdditionalCost(Requirement.PAY_1_ENERGY)) {
			if (_controller.getNbCounters(CounterType.ENERGY) < 1)
				return false;
		}
		
		// abilities that require two Energy counter
		if (_cost.requiresAdditionalCost(Requirement.PAY_2_ENERGY)) {
			if (_controller.getNbCounters(CounterType.ENERGY) < 2)
				return false;
		}

		// abilities that require three Energy counter
		if (_cost.requiresAdditionalCost(Requirement.PAY_3_ENERGY)) {
			if (_controller.getNbCounters(CounterType.ENERGY) < 3)
				return false;
		}
		
		// abilities that require five Energy counter
		if (_cost.requiresAdditionalCost(Requirement.PAY_5_ENERGY)) {
			if (_controller.getNbCounters(CounterType.ENERGY) < 5)
				return false;
		}
		
		// Abilities that require to pay one life
		if (_cost.requiresAdditionalCost(Requirement.PAY_1_LIFE)) {
			if (_controller.getLife() < 1)
				return false;
		}
		
		// Abilities that require to pay two life
		if (_cost.requiresAdditionalCost(Requirement.PAY_2_LIFE)) {
			if (_controller.getLife() < 2)
				return false;
		}
		
		// Ability activable only during the player's upkeep (i.e. Hammer of Bogardan and Shard Phoenix)
		if (_bOnlyActivableDuringUpkeep) {
			if ((g.getActivePlayer() != _controller) || (g.getStep() != Step.Upkeep))
				return false;
		}

		// Ability only activable when the player has Metalcraft (controls at least 3 artifacts. i.e. Mox Opal)
		if (_bOnlyActivableWithMetalcraft) {
			if (!_controller.hasBuff(PlayerBuff.Metalcraft))
				return false;
		}
		
		// Ability only actibale when the player controls an artifact : i.e. Spire of Industry
		if (_abilityName.equals("spireOfIndustry")) {
			if (!_controller.controlsAnArtifact())
				return false;
		}
		
		// Library of Alexandria : ability activable only if controller has 7 cards in hand
		if (_abilityName.equals("libraryOfAlexandria_draw")) {
			if (_controller.getHandSize() != 7)
				return false;
		}
		
		// Abilities that require the controller to return an Elf he controls to its owner's hand (i.e. Wirewood Symbiote)
		if (_cost.requiresAdditionalCost(Requirement.RETURN_AN_ELF_YOU_CONTROL))
		{
			boolean bControlElf = false;
			Vector<Card> creatures = g.getBattlefield().getCreaturesControlledBy(_controller);
			for (Card creature : creatures) {
				if (creature.hasCreatureType(g, CreatureType.Elf)) {
					bControlElf = true;
					break;
				}
			}
			return bControlElf;
		}

		
		return true;
	}

	@Override
	public Card getSource() {
		return _source;
	}

	@Override
	public boolean isSorcerySpeed(Game g) {
		if (isLoyalty())
			return true;
		return _bSorcerySpeed;
	}

	@Override
	public boolean hasXValue() {
		return _bXValue;
	}

	@Override
	public void requiresXValue() {
		_bXValue  = true;
	}

	@Override
	public boolean isTargetableBy(Game g, StackObject so) {
		// All activated abilities can be targeted
		return true;
	}

	public boolean isManaAbility() {
		Pattern p = Pattern.compile(".*add (\\{[wubrgc]\\}|one).*");
		Matcher m = p.matcher(_effect.getRulesText().toLowerCase());
		return m.matches() && _targetRequirements.isEmpty() && !this.isLoyalty();
	}

	@Override
	public Effect getEffect() {
		return _effect;
	}

	public void setImplied() {
		_bImplied = true;
	}
	
	public boolean isImplied() {
		return _bImplied;
	}

	public void increaseNbActivation() {
		_nbActivationsThisTurn++;
	}

	public void resetNbActivation() {
		_nbActivationsThisTurn = 0;
	}
	
	public String toString() {
		return _source.toString();
	}

	// For abilities that can only be activated during upkeep
	public void setOnlyActivableDuringUpkeep() {
		_bOnlyActivableDuringUpkeep = true;
	}
	
	public boolean isbOnlyActivableDuringUpkeep() {
		return _bOnlyActivableDuringUpkeep;
	}

	// For abilities that can only be activated when the player has Metalcraft (controls
	// at least 3 artifacts)
	public void setOnlyActivableWithMetalcraft() {
		_bOnlyActivableWithMetalcraft = true;
	}
	
	public boolean isbOnlyActivableWithMetalcraft() {
		return _bOnlyActivableWithMetalcraft;
	}

	@Override
	public Type getActionType() {
		return Type.ACTIVATE_ABILITY;
	}

	/**
	 * This method creates the cost of the activated ability based on its rules text.
	 */
	private void initializeCost() {
		String rulesText = _effect.getRulesText();
		String cost = "";
		_cost = new Cost(null);
		Requirement ac = null;
		
		// cost : effect line is inside parenthesis
		if (rulesText.contains("(")) {
			int open = rulesText.indexOf("(")+1;
			int close = rulesText.indexOf(")");
			rulesText = rulesText.substring(open, close);
		}
		
		// cost : effect line is preceded by some text that must be ignored
		if (rulesText.startsWith("<i>Metalcraft</i> — "))
			rulesText = rulesText.substring(20);
		
		Pattern p = Pattern.compile("^(.*?):.*$");
		Matcher m = p.matcher(rulesText);
		if (m.matches()) {
			cost = m.group(1);
			
			String[] elements = cost.split(", ");
			for (String e : elements) {
				// Loyalty cost
				if (LoyaltyCost.isValid(e)) {
					LoyaltyCost lc = new LoyaltyCost(e);
					_cost.setLoyaltyCost(lc);
					continue;
				}
				
				// Mana cost
				if (ManaCost.isValid(e)) {
					ManaCost mc = new ManaCost(e);
					_cost.setManaCost(mc);
					continue;
				}
				
				// Additional cost (if any)
				ac = AdditionalCost.parse(e, _source.getName());
				if (ac != null)
					_cost.addAdditionalCost(ac);
			}
		}
		else {
			System.err.println("No match for : " + rulesText);
			System.exit(1);
		}
	}
}

