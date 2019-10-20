package mtgengine;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Vector;

import mtgengine.Game.Answer;
import mtgengine.Game.Response;
import mtgengine.Game.State;
import mtgengine.Target.Category;
import mtgengine.TargetRequirement.Cardinality;
import mtgengine.ability.Evergreen;
import mtgengine.ability.TriggeredAbility.YouMayChoiceMaker;
import mtgengine.card.Card;
import mtgengine.card.Color;
import mtgengine.cost.Cost;
import mtgengine.cost.AdditionalCost.Requirement;
import mtgengine.effect.ContinuousEffect;
import mtgengine.effect.ContinuousEffectSource;
import mtgengine.effect.Effect;
import mtgengine.mana.Mana;
import mtgengine.mana.Mana.ManaType;
import mtgengine.player.Player;
import mtgengine.type.CardType;
import mtgengine.type.CreatureType;
import mtgengine.type.Subtype;
import mtgengine.type.Supertype;
import mtgengine.zone.*;

public abstract class StackObject extends MtgObject implements ContinuousEffectSource {
	public abstract String getSystemName();
	public abstract Card getSource();
	protected abstract int getImageID();
	public abstract String getName();
	public abstract boolean isSorcerySpeed(Game g);
	public abstract void requiresXValue();
	public abstract boolean hasXValue();
	public abstract Effect getEffect();
	
	protected HashMap<Integer, Vector<Target>> _targets = new HashMap<Integer, Vector<Target>>();
	protected HashMap<Integer, Vector<TargetRequirement>> _targetRequirements = new HashMap<Integer, Vector<TargetRequirement>>();
	private int _currentStep = 1;
	private int _currentScryStep = 1;
	protected Object _additionalData = null;
	protected int _xValue = -1;
	protected boolean _bYouMay = false;
	protected int _nbModes = 1;
	private int _chosenMode = 1;
	private Vector<Card> _libManip;
	private int _tmpVar;
	protected YouMayChoiceMaker _youMayChoiceMaker = YouMayChoiceMaker.AbilityController;
	protected Cost _cost;
	protected String _parameter;
	protected boolean _bAnswered = false;

	/* owner and controller */
	protected Player _owner;
	protected Player _controller = null;
	
	public void setYouMayChoiceMaker(YouMayChoiceMaker parameter) {
		_youMayChoiceMaker = parameter;
	}
	
	public Player getYouMayChoiceMaker(Game g) {
		switch (_youMayChoiceMaker) {

		case AbilityController:
			return _controller;
			
		case AdditionalDataController:
			return ((StackObject) _additionalData).getController(g);
		
		default:
			break;
		}
		return null;
	}

	public StackObject(String name) {
		super(name);
	}
	
	public void setOptional() {
		_bYouMay  = true;
	}
	
	public void setOwner(Player owner) {
		_owner = owner;
	}
	
	public Player getOwner() {
		return _owner;
	}

	public void setController(Game g, Player controller) {
		_controller = controller;
	}
	
	public Player getController(Game g) {
		// Look for effects that change the controller
		for (ContinuousEffect ce : g.getContinuousEffects()) {
			if (ce.modifiesController(this))
				return ce.getNewController(g, this);
		}
		return _controller;
	}
	
	public int getStep() {
		return _currentStep;
	}
	
	public int getScryStep() {
		return _currentScryStep;
	}
	
	public void advanceStep() {
		_currentStep++;
	}
	
	public void advanceScryStep() {
		_currentScryStep++;
	}
	
	public void goToStep(int step) {
		_currentStep = step;
	}
	
	public void resetStep() {
		_currentStep = 1;
	}
	
	public void removeTargetRequirements(Target.Category cat) {
		Vector<TargetRequirement> requirements = _targetRequirements.get(_chosenMode);
		int i = 0;
		
		while (i < requirements.size()) {
			if (requirements.get(i).getCategory() == cat) {
				requirements.remove(i);
				i--;
			}
			i++;
		}
		
		if (requirements.size() == 0)
			_targetRequirements.remove(_chosenMode);

		Vector<Target> ts = _targets.get(1);
		i = 0;
		
		while (i < ts.size()) {
			if (ts.get(i).getType() == cat) {
				ts.remove(i);
				i--;
			}
			i++;
		}
	}
	
	public void addTargetRequirement(Category cat) {
		addTargetRequirementMode(1, cat, Cardinality.ONE);
	}
	
	public void addTargetRequirement(Category cat, Cardinality cardinality) {
		addTargetRequirementMode(1, cat, cardinality);
	}
	
	public void addTargetRequirementMode(int mode, Category cat, Cardinality cardinality) {
		Vector<TargetRequirement> requirements;
		TargetRequirement req;
		Vector<Target> targets;
		if (_targetRequirements.get(mode) == null)
			requirements = new Vector<TargetRequirement>();
		else
			requirements = _targetRequirements.get(mode);
		req = new TargetRequirement(cat, cardinality);
		requirements.add(req);
		_targetRequirements.put(mode, requirements);

		if (_targets.get(mode) == null)
			targets = new Vector<Target>();
		else
			targets = _targets.get(mode);
		
		switch(req.getCardinality()) {
		case ONE_OR_TWO:
			break;
			
		case THREE:
			break;
			
		case TWO:
			for (int i = 0; i < 2; i++)
				targets.add(new Target(req.getCategory()));
			_targets.put(mode, targets);
			break;
			
		case ONE:
		case UP_TO_ONE:
			targets.add(new Target(req.getCategory()));
			_targets.put(mode, targets);
			break;
			
		case UP_TO_THREE:
			for (int i = 0; i < 3; i++){
				targets.add(new Target(req.getCategory()));
				_targets.put(mode, targets);
			}
			break;
			
		case UP_TO_FOUR:
			for (int i = 0; i < 4; i++){
				targets.add(new Target(req.getCategory()));
				_targets.put(mode, targets);
			}
			break;
			
		case UP_TO_TWO:
			for (int i = 0; i < 2; i++){
				targets.add(new Target(req.getCategory()));
				_targets.put(mode, targets);
			}
			break;

		default:
			break;
		}
	}
	
	public boolean hasTargetRequirements() {
		return _targetRequirements.containsKey(_chosenMode);
	}
	
	public boolean hasLegalTargets(Game g) {
		Vector<TargetRequirement> requirements = _targetRequirements.get(_chosenMode);
		Vector<Target> targets;
		Vector<MtgObject> availableTargets;
		
		for (TargetRequirement req : requirements)
		{
			switch (req.getCardinality())
			{
			case UP_TO_ONE:
			case UP_TO_TWO:
			case UP_TO_THREE:
			case UP_TO_FOUR:
			case UP_TO_X:
			case X:
				continue;
				
			default:
				targets = _targets.get(_chosenMode);
				for (int i = 0; i < targets.size(); i++)
				{
					availableTargets = getAvailableTargets(g, i);
					if ((req.getCardinality() == Cardinality.TWO) && (availableTargets.size() < 2))
						return false;
					if (availableTargets.size() == 0)
						return false;
				}
				break;
			}
		}
		return true;
	}
	
	public Vector<MtgObject> getAvailableTargets(Game g, int iTarget) {
		Stack _stack = (Stack) g.getStack();
		Vector<Target> targets = _targets.get(_chosenMode);
		if (targets.isEmpty())
			return new Vector<MtgObject>();
		Target t = targets.get(iTarget);
		Target.Category key = t.getType();
		Vector<MtgObject> ret = new Vector<MtgObject>();
		Vector<MtgObject> tempTargets;
		Card card;
		int cnt;
		int iPermanent;
		
		// Step 1: based on the category of target, fill a vector with available, targetable objects
		switch (key) {
		case CardInYourGraveyard:
			ret = getController(g).getGraveyard().getTargetableObjects(this);
			break;
		
		case LandCardInYourGraveyard:
			ret.addAll(getController(g).getGraveyard().getLandCards());
			break;

		case EnchantmentCardInYourGraveyard:
			ret = getController(g).getGraveyard().getEnchantmentCards();
			break;
			
		case PermanentCardWithCdMC3OrLessInYourGraveyard:
			cnt = 0;
			
			// 1. Get all cards in effect controller's graveyard
			ret = getController(g).getGraveyard().getTargetableObjects(this);
			
			// 2. Remove all cards that do not match criteria
			while (cnt < ret.size()) {
				card = (Card) ret.get(cnt);
				if (!card.isPermanentCard() || (card.getConvertedManaCost(g) > 3)) {
					ret.remove(cnt);
					cnt--;
				}
				cnt++;
			}
			break;
			
		case PermanentCardInYourGraveyard:
			cnt = 0;
			
			// 1. Get all cards in effect controller's graveyard
			ret = getController(g).getGraveyard().getTargetableObjects(this);
			
			// 2. Remove all cards that do not match criteria
			while (cnt < ret.size()) {
				card = (Card) ret.get(cnt);
				if (!card.isPermanentCard()) {
					ret.remove(cnt);
					cnt--;
				}
				cnt++;
			}
			break;
			
		case NonLgdryCreaCardInYourGydWithCMCX:
			cnt = 0;
			
			// 1. Get all creature cards in effect controller's graveyard
			ret.addAll(getController(g).getGraveyard().getCreatureCards());
			
			// 2. Remove all cards that do not match criteria
			while (cnt < ret.size()) {
				card = (Card) ret.get(cnt);
				
				if (_xValue == -1) {
					if (card.hasSupertype(Supertype.LEGENDARY)) {
						ret.remove(cnt);
						cnt--;
					}
				}
				else {
					if ((card.getConvertedManaCost(g)!= _xValue) || card.hasSupertype(Supertype.LEGENDARY)) {
						ret.remove(cnt);
						cnt--;
					}					
				}
				cnt++;
			}
			break;
			
		case CreatureCardInYourGraveyard:
			ret.addAll(getController(g).getGraveyard().getCreatureCards());
			break;
			
		case InstantCardInYourGraveyard:
			ret.addAll(getController(g).getGraveyard().getInstantCards());
			break;
			
		case CardInAnyGraveyard:
			for (Player player : g.getPlayers())
				ret.addAll(player.getGraveyard().getTargetableObjects(this));
			break;
			
		case LandCardInAnyGraveyard:
			for (Player player : g.getPlayers())
				ret.addAll(player.getGraveyard().getLandCards());
			break;
			
		case SorceryOrInstantCardInAnyGraveyard:
			for (Player player : g.getPlayers()) {
				ret.addAll(player.getGraveyard().getSorceryCards());
				ret.addAll(player.getGraveyard().getInstantCards());
			}
			break;
			
		case CreatureOrPlaneswalkerCardinAnyGraveyard:
			for (Player player : g.getPlayers()) {
				ret.addAll(player.getGraveyard().getCreatureCards());
				ret.addAll(player.getGraveyard().getPlaneswalkerCards());
			}
			break;
			
		case CreatureCardInAnyGraveyard:
			for (Player player : g.getPlayers())
				ret.addAll(player.getGraveyard().getCreatureCards());
			break;

		case AnyTarget:
			tempTargets = g.getBattlefield().getTargetableObjectsOfType(CardType.CREATURE, this);
			tempTargets.addAll(g.getBattlefield().getTargetableObjectsOfType(CardType.PLANESWALKER, this));
			tempTargets.addAll(g.getTargetablePlayers(this));
			ret = tempTargets;
			break;
			
		case CreatureOrPlaneswalker:
			tempTargets = g.getBattlefield().getTargetableObjectsOfType(CardType.CREATURE, this);
			tempTargets.addAll(g.getBattlefield().getTargetableObjectsOfType(CardType.PLANESWALKER, this));
			ret = tempTargets;
			break;
			
		case SpellOrPermanent:
			tempTargets = g.getBattlefield().getTargetableObjects(this); // first add targetable permanents
			tempTargets.addAll(_stack.getSpells()); //then add spells on the stack
			ret = tempTargets;
			break;
			
		case NonlandPermanent:
			ret = g.getBattlefield().getTargetableObjects(this);
			iPermanent = 0;
			while (iPermanent < ret.size()) {
				Card c = (Card) ret.get(iPermanent);
				if (c.isLand(g)) {
					ret.remove(iPermanent);
					iPermanent--;
				}
				iPermanent++;
			}
			break;
			
		case NonlandPermanentWithCMC3OrLess:
			ret = g.getBattlefield().getTargetableObjects(this);
			iPermanent = 0;
			while (iPermanent < ret.size()) {
				Card c = (Card) ret.get(iPermanent);
				if (c.isLand(g) || (c.getConvertedManaCost(g) > 3)) {
					ret.remove(iPermanent);
					iPermanent--;
				}
				iPermanent++;
			}
			break;
			
		case Permanent:
			ret = g.getBattlefield().getTargetableObjects(this);
			break;
			
		case SpellOrActivatedOrTriggeredAbility:
			ret = _stack.getObjects();
			break;
			
		case ActivatedOrTriggeredAbility:
			ret = _stack.getAbilities();
			break;
			
		case ActivatedAbility:
			ret = _stack.getActivatedAbilities();
			break;
			
		case Spell:
			ret = _stack.getSpells();
			break;

		case SpellWithCMC4orLess:
			ret = _stack.getSpells();
			cnt = 0;
			while (cnt < ret.size()) {
				if (((Card) ret.get(cnt)).getConvertedManaCost(g) > 4) {
					ret.remove(cnt);
					cnt--;
				}
				cnt++;
			}
			break;
			
		case InstantOrSorcery:
			ret = _stack.getInstantAndSorcerySpells();
			break;
			
		case ArtifactOrEnchantmentSpell:
			ret = _stack.getArtifactAndEnchantmentSpells();
			break;
			
		case CreatureSpell:
			ret = _stack.getCreatureSpells();
			break;
			
		case NonCreatureSpell:
			ret = _stack.getNonCreatureSpells();
			break;
			
		case RedSpell:
			ret = _stack.getSpells();
			cnt = 0;
			while (cnt < ret.size()) {
				if (!((Card) ret.get(cnt)).hasColor(Color.RED)) {
					ret.remove(cnt);
					cnt--;
				}
				cnt++;
			}
			break;
		
		case Land:
			ret = g.getBattlefield().getTargetableObjectsOfType(CardType.LAND, this);
			break;
			
		case Forest:
		case Swamp:
		{
			Subtype st;
			
			switch (key) {
			case Forest:
				st = Subtype.FOREST;
				break;
				
			case Swamp:
				st = Subtype.SWAMP;
				break;
				
			default:
				return null;
			}
			
			ret = g.getBattlefield().getTargetableObjectsOfType(CardType.LAND, this);
			cnt = 0;
			while (cnt < ret.size()) {
				if (!((Card) ret.get(cnt)).hasSubtypeGlobal(g, st)) {
					ret.remove(cnt);
					cnt--;
				}
				cnt++;
			}
			break;			
		}
			
		case NonbasicLand:
			ret = g.getBattlefield().getTargetableObjectsOfType(CardType.LAND, this);
			cnt = 0;
			while (cnt < ret.size()) {
				if (((Card) ret.get(cnt)).hasSupertype(Supertype.BASIC)) {
					ret.remove(cnt);
					cnt--;
				}
				cnt++;
			}
			break;
			
		case Artifact:
			ret = g.getBattlefield().getTargetableObjectsOfType(CardType.ARTIFACT, this);
			break;
			
		case NonCreatureArtifact:
			ret = g.getBattlefield().getTargetableObjectsOfType(CardType.ARTIFACT, this);
			cnt = 0;
			while (cnt < ret.size()) {
				if (((Card) ret.get(cnt)).isCreature(g)) {
					ret.remove(cnt);
					cnt--;
				}
				cnt++;
			}
			break;
			
		case Enchantment:
			ret = g.getBattlefield().getTargetableObjectsOfType(CardType.ENCHANTMENT, this);
			break;
			
		case AuraAttachedToAcreatureOrLand:
			ret = g.getBattlefield().getTargetableObjectsOfType(CardType.ENCHANTMENT, this);
			cnt = 0;
			while (cnt < ret.size()) {
				card = (Card) ret.get(cnt);
				
				// enchantment is an aura
				if (card.hasSubtypeGlobal(g, Subtype.AURA)) {
					Card host = card.getHost();
					
					if (! (host.isCreature(g) || host.isLand(g))) {
						ret.remove(cnt);
						cnt--;
					}
				}
				else // enchantement is not an aura
				{
					ret.remove(cnt);
					cnt--;
				}
				cnt++;
			}
			break;
			
		case EnchantmentTappedArtifactOrTappedCreature:
			Vector<MtgObject> objs = g.getBattlefield().getTargetableObjects(this);
			
			for (MtgObject obj : objs) {
				Card permanent = (Card) obj;
				
				if (permanent.isEnchantmentCard() || // enchantment
						(permanent.isArtifact(g) && permanent.isTapped()) || //tapped artifact
						(permanent.isCreature(g) && permanent.isTapped()))   //tapped creature
					ret.add(permanent);
			}
			break;
			
		case ArtifactOrEnchantment:
			ret = g.getBattlefield().getTargetableObjectsOfType(CardType.ARTIFACT, this);
			ret.addAll(g.getBattlefield().getTargetableObjectsOfType(CardType.ENCHANTMENT, this));
			break;
			
		case ArtifactOrCreatureOrLand:
			ret = g.getBattlefield().getTargetableObjectsOfType(CardType.ARTIFACT, this);
			ret.addAll(g.getBattlefield().getTargetableObjectsOfType(CardType.CREATURE, this));
			ret.addAll(g.getBattlefield().getTargetableObjectsOfType(CardType.LAND, this));
			break;
			
		case ArtifactOrEnchantmentOrLand:
			ret = g.getBattlefield().getTargetableObjectsOfType(CardType.ARTIFACT, this);
			ret.addAll(g.getBattlefield().getTargetableObjectsOfType(CardType.ENCHANTMENT, this));
			ret.addAll(g.getBattlefield().getTargetableObjectsOfType(CardType.LAND, this));
			break;
			
		case CreatureYouControl:
			ret = g.getBattlefield().getTargetableCreaturesControlledBy(_controller, this);
			break;

		case NonAngelCreatureYouControl:
			ret = g.getBattlefield().getTargetableCreaturesControlledBy(_controller, this);
			cnt = 0;
			while (cnt < ret.size()) {
				card = (Card) ret.get(cnt);
				if (card.hasCreatureType(g, CreatureType.Angel)) {
					ret.remove(cnt);
					cnt--;
				}
				cnt++;
			}
			break;
			
		case CreatureAnOpponentControls:
			ret = g.getBattlefield().getTargetableCreaturesControlledBy(_controller.getOpponent(), this);
			break;

		case LandYouControl:
			ret = g.getBattlefield().getTargetableLandsControlledBy(_controller, this);
			break;
			
		case AttackingCreature:
			ret = g.getBattlefield().getTargetableObjectsOfType(CardType.CREATURE, this);
			cnt = 0;
			while (cnt < ret.size()) {
				card = (Card) ret.get(cnt);
				if (!card.isAttacking(g)) {
					ret.remove(cnt);
					cnt--;
				}
				cnt++;
			}
			break;

		case AttackingCreatureWithFlying:
			ret = g.getBattlefield().getTargetableObjectsOfType(CardType.CREATURE, this);
			cnt = 0;
			while (cnt < ret.size()) {
				card = (Card) ret.get(cnt);
				if (!(card.isAttacking(g) && card.hasEvergreenGlobal(Evergreen.FLYING, g))) {
					ret.remove(cnt);
					cnt--;
				}
				cnt++;
			}
			break;
			
		case AttackingCreatureWithoutFlying:
			ret = g.getBattlefield().getTargetableObjectsOfType(CardType.CREATURE, this);
			cnt = 0;
			while (cnt < ret.size()) {
				card = (Card) ret.get(cnt);
				if (!(card.isAttacking(g) && !card.hasEvergreenGlobal(Evergreen.FLYING, g))) {
					ret.remove(cnt);
					cnt--;
				}
				cnt++;
			}
			break;

		case BlockedCreature:
			ret = g.getBattlefield().getTargetableObjectsOfType(CardType.CREATURE, this);
			cnt = 0;
			while (cnt < ret.size()) {
				card = (Card) ret.get(cnt);
				if (!card.isBlocked(g)) {
					ret.remove(cnt);
					cnt--;
				}
				cnt++;
			}
			break;
			
		case AttackingOrBlockingCreature:
			ret = g.getBattlefield().getTargetableObjectsOfType(CardType.CREATURE, this);
			cnt = 0;
			while (cnt < ret.size()) {
				card = (Card) ret.get(cnt);
				if (!(card.isAttacking(g) || card.isBlocking(g))) {
					ret.remove(cnt);
					cnt--;
				}
				cnt++;
			}
			break;
			
		case Creature:
			ret = g.getBattlefield().getTargetableObjectsOfType(CardType.CREATURE, this);
			break;

		case NontokenCreature:
			ret = g.getBattlefield().getTargetableObjectsOfType(CardType.CREATURE, this);
			cnt = 0;
			while (cnt < ret.size()) {
				card = (Card) ret.get(cnt);
				if (card.isToken()) {
					ret.remove(cnt);
					cnt--;
				}
				cnt++;
			}
			break;
			
		case TokenCreature:
			ret = g.getBattlefield().getTargetableObjectsOfType(CardType.CREATURE, this);
			cnt = 0;
			while (cnt < ret.size()) {
				card = (Card) ret.get(cnt);
				if (!card.isToken()) {
					ret.remove(cnt);
					cnt--;
				}
				cnt++;
			}
			break;
			
		case CreatureWithPower4orGreater:
			ret = g.getBattlefield().getTargetableObjectsOfType(CardType.CREATURE, this);
			cnt = 0;
			while (cnt < ret.size()) {
				card = (Card) ret.get(cnt);
				if (card.getPower(g) < 4) {
					ret.remove(cnt);
					cnt--;
				}
				cnt++;
			}
			break;
			
		case GreenCreature:
			ret = g.getBattlefield().getTargetableObjectsOfType(CardType.CREATURE, this);
			cnt = 0;
			while (cnt < ret.size()) {
				card = (Card) ret.get(cnt);
				if (!card.hasColor(Color.GREEN)) {
					ret.remove(cnt);
					cnt--;
				}
				cnt++;
			}
			break;
			
		case WhiteCreature:
			ret = g.getBattlefield().getTargetableObjectsOfType(CardType.CREATURE, this);
			cnt = 0;
			while (cnt < ret.size()) {
				card = (Card) ret.get(cnt);
				if (!card.hasColor(Color.WHITE)) {
					ret.remove(cnt);
					cnt--;
				}
				cnt++;
			}
			break;
			
		case LegendaryCreature:
			ret = g.getBattlefield().getTargetableObjectsOfType(CardType.CREATURE, this);
			cnt = 0;
			while (cnt < ret.size()) {
				card = (Card) ret.get(cnt);
				if (!card.hasSupertype(Supertype.LEGENDARY)) {
					ret.remove(cnt);
					cnt--;
				}
				cnt++;
			}
			break;
			
		case BlinkmothCreature:
			ret = g.getBattlefield().getTargetableObjectsOfType(CardType.CREATURE, this);
			cnt = 0;
			while (cnt < ret.size()) {
				card = (Card) ret.get(cnt);
				if (!card.hasCreatureType(g, CreatureType.Blinkmoth)) {
					ret.remove(cnt);
					cnt--;
				}
				cnt++;
			}
			break;
			
		case ArtifactCreature:
			ret = g.getBattlefield().getTargetableObjectsOfType(CardType.CREATURE, this);
			cnt = 0;
			while (cnt < ret.size()) {
				card = (Card) ret.get(cnt);
				if (!card.isArtifact(g)) {
					ret.remove(cnt);
					cnt--;
				}
				cnt++;
			}
			break;
			
		case AnotherCreature:
			ret = g.getBattlefield().getTargetableObjectsOfType(CardType.CREATURE, this);
			cnt = 0;
			while (cnt < ret.size()) {
				card = (Card) ret.get(cnt);
				if (card == this.getSource()) {
					ret.remove(cnt);
					cnt--;
				}
				cnt++;
			}
			break;
			
		case AnotherPermanent:
			ret = g.getBattlefield().getTargetableObjects(this);
			cnt = 0;
			while (cnt < ret.size()) {
				card = (Card) ret.get(cnt);
				if (card == this.getSource()) {
					ret.remove(cnt);
					cnt--;
				}
				cnt++;
			}
			break;
			
		case AnotherPermanentYouControl:
			ret = g.getBattlefield().getTargetablePermanentsControlledBy(_controller, this);
			cnt = 0;
			while (cnt < ret.size()) {
				card = (Card) ret.get(cnt);
				if (card == this.getSource()) {
					ret.remove(cnt);
					cnt--;
				}
				cnt++;
			}
			break;
			
		case PermanentYouControl:
				ret = g.getBattlefield().getTargetablePermanentsControlledBy(_controller, this);
				cnt = 0;
				while (cnt < ret.size()) {
					card = (Card) ret.get(cnt);
					cnt++;
				}
				break;
			
		case NonArtifactNonBlackCreature:
			ret = g.getBattlefield().getTargetableObjectsOfType(CardType.CREATURE, this);
			cnt = 0;
			
			while (cnt < ret.size()) {
				card = (Card) ret.get(cnt);
				if (card.isArtifact(g) || card.hasColor(Color.BLACK)) {
					ret.remove(cnt);
					cnt--;
				}
				cnt++;
			}
			break;
			
		case NonBlackCreature:
			ret = g.getBattlefield().getTargetableObjectsOfType(CardType.CREATURE, this);
			cnt = 0;
			
			while (cnt < ret.size()) {
				card = (Card) ret.get(cnt);
				if (card.hasColor(Color.BLACK)) {
					ret.remove(cnt);
					cnt--;
				}
				cnt++;
			}
			break;
			
		case LandOrNonBlackCreature:
			ret = g.getBattlefield().getTargetableObjectsOfType(CardType.CREATURE, this);
			cnt = 0;
			
			while (cnt < ret.size()) {
				card = (Card) ret.get(cnt);
				if (card.hasColor(Color.BLACK)) {
					ret.remove(cnt);
					cnt--;
				}
				cnt++;
			}
			
			ret.addAll(g.getBattlefield().getTargetableObjectsOfType(CardType.LAND, this));
			break;
			
		case MonocoloredCreature:
			ret = g.getBattlefield().getTargetableObjectsOfType(CardType.CREATURE, this);
			cnt = 0;
			
			while (cnt < ret.size()) {
				card = (Card) ret.get(cnt);
				if (!card.isMonocolored()) {
					ret.remove(cnt);
					cnt--;
				}
				cnt++;
			}
			break;
		
		case Planeswalker:
			ret = g.getBattlefield().getTargetableObjectsOfType(CardType.PLANESWALKER, this);
			break;
			
		case Opponent:
			ret = g.getTargetablePlayers(this);
			ret.remove(_controller);
			break;
			
		case Player:
			ret = g.getTargetablePlayers(this);
			break;
			
		case PlayerOrPlaneswalker:
			ret = g.getTargetablePlayers(this);
			ret.addAll(g.getBattlefield().getTargetableObjectsOfType(CardType.PLANESWALKER, this));
			break;
			
		default:
			ret = null;
			break;
		}
		
		// Step 2: remove from ret all targets illegal due to protection
		int j = 0;
		while (j < ret.size()) {
			MtgObject target = ret.get(j);
			if (g.computeIsProtectedFrom(getSource(), target)) {
				ret.remove(j);
				j--;
			}
			j++;
		}
		return ret;
	}
	
	/**
	 * Returns true if all targets have been assigned
	 * false otherwise
	 * @return
	 */
	public boolean allTargetsAssigned() {
		Vector<TargetRequirement> requirements = _targetRequirements.get(_chosenMode);
		
		for (TargetRequirement req : requirements) {
			
			switch(req.getCardinality()) {

			case UP_TO_ONE:
			case UP_TO_TWO:
			case UP_TO_THREE:
			case UP_TO_FOUR:
			case UP_TO_X:
				return true;
				
			default:
				break;
			}
			
			for (Target t : _targets.get(_chosenMode)) {
				if (t.getObject() == null)
					return false;
			}			
		}
		return true;
	}
	
	public Target getTarget(int iTarget) {
		return _targets.get(_chosenMode).get(iTarget);
	}
	
	public void addTarget(int i, MtgObject obj) {
		_targets.get(_chosenMode).get(i).setObject(obj);
	}

	public MtgObject getTargetObject(int i) {
		if (_targets.get(_chosenMode) == null)
			return null;
		if (_targets.get(_chosenMode).get(i) == null)
			return null;
		return _targets.get(_chosenMode).get(i).getObject();
	}
	
	public void purgeTargets() {
		if (_targets.get(_chosenMode) != null)
		{
			for (Target t : _targets.get(_chosenMode)) {
				t.reset();
			}	
		}
	}

	protected HashMap<Integer, Vector<Target>> cloneTargets() {
		HashMap<Integer, Vector<Target>> ret = new HashMap<Integer, Vector<Target>>();
		
		// for each mode
		for (Integer mode : _targets.keySet()) {
			Vector<Target> t = new Vector<Target>();
			
			// for each target
			for (Target originalTarget : _targets.get(mode)) {
				Target clonedTarget = new Target(originalTarget.getType());
				clonedTarget.setObject(originalTarget.getObject());
				t.add(clonedTarget);
			}
			ret.put(mode, t);
		}
		return ret;
	}
	
	public Game.Response doEffect(Game g) {
		Effect effect = this.getEffect();
		
		try {
			Method m = Effect.class.getMethod(effect.getMethodName(), Game.class, StackObject.class);
			
			// check if it's a "you may" effect before doing anything
			if (_bYouMay && !_bAnswered)
			{
				if (_currentStep == 1)
				{
					getYouMayChoiceMaker(g).setState(State.PromptDoYouWantToUseTheAbility);
					advanceStep();
					return Response.MoreStep;
				}
				else if (_currentStep == 2)
				{
					_bAnswered = true;
					Answer choice = g.getAnswer();
					if (choice == Answer.No)
					{
						getYouMayChoiceMaker(g).setState(State.Ready);
						return Response.OK;
					}
					_currentStep = 1;
				}
			}

			Response ret = (Game.Response) m.invoke(null, g, this);

			if (ret != Response.MoreStep)
				getYouMayChoiceMaker(g).setState(State.Ready);
			return ret; 
		} catch(NullPointerException e) {
			System.err.println("null pointer");
			return Game.Response.Error;
		} catch(NoSuchMethodException e) {
			System.err.println("no such method : " + effect.getMethodName() + "\n");
			return Game.Response.Error;
		} catch(IllegalAccessException e) {
			System.err.println("illegal access");
			return Game.Response.Error;
		} catch(InvocationTargetException e) {
			System.err.println("invocation target exception : " + e.getCause().getMessage());
			return Game.Response.Error;
		}
	}

	public String getDescription() {
		Effect e = this.getEffect();
		return e.getRulesText();
	}
	
	public Zone getZone(Game g) {
		return g.findZone(this);
	}
	
	public boolean checkTargetLegality(Game g) {
		if (_targets.isEmpty())
			return true;
		
		if (_name.equals("gildedDrake"))
			return true;
		
		for (Target t : _targets.get(_chosenMode))
		{
			Vector<TargetRequirement> tr = _targetRequirements.get(_chosenMode);
			Cardinality car = tr.get(0).getCardinality();
			if (!t.isAssigned()) {
				if (! ((car == Cardinality.UP_TO_FOUR) || (car == Cardinality.UP_TO_THREE) || (car == Cardinality.UP_TO_TWO) || (car == Cardinality.UP_TO_X)))
					return false;
			}
			
			if (!t.isValid(g, this)) {
				if (! ((car == Cardinality.UP_TO_FOUR) || (car == Cardinality.UP_TO_THREE) || (car == Cardinality.UP_TO_TWO) || (car == Cardinality.UP_TO_X)))
					return false;
			}
		}	
		return true;
	}

	public int getNbTarget() {
		if (_targets.get(_chosenMode) != null)
			return _targets.get(_chosenMode).size();
		return 0;
	}
	
	public void setXValue(int val) {
		Vector<TargetRequirement> reqs;
		Vector<Target> targets = new Vector<Target>();
		
		_xValue = val;
		for (int mode : _targetRequirements.keySet()) {
			reqs = _targetRequirements.get(mode);
			for (TargetRequirement req : reqs)
			{
				if ((req.getCardinality() == Cardinality.X) || (req.getCardinality() == Cardinality.UP_TO_X)) {
					for (int i = 0; i < _xValue; i++)
						targets.add(new Target(req.getCategory()));
					_targets.put(mode, targets);
					break;
				}
			}
			
		}
	}
	
	public int getXValue() {
		if (_xValue == -1)
			return 0;
		return _xValue;
	}
	
	// modal spells and abilities
	public boolean isModal() {
		return _nbModes > 1;
	}
	
	public void setModal(int nbModes) {
		_nbModes = nbModes;
	}
	
	public void setChosenMode(int mode) {
		_chosenMode = mode;
	}
	
	public int getChosenMode() {
		return _chosenMode;
	}
	
	public int getNbModes() {
		return _nbModes;
	}
	
	public void setLibManip(Vector<Card> cards) {
		_libManip = cards;
	}
	
	public Vector<Card> getLibManip() {
		return _libManip;
	}
	public Vector<TargetRequirement> getTargetRequirements() {
		return _targetRequirements.get(_chosenMode);
	}
	public int getTmpVar() {
		return _tmpVar;
	}
	public void setTmpVar(int _tmpVar) {
		this._tmpVar = _tmpVar;
	}

	public void setCost(Cost cost) {
		_cost = cost;
	}
	
	public Cost getCost() {
		return _cost;
	}

	public void setAdditionalData(Object obj) {
		_additionalData = obj;
	}

	public Object getAdditionalData() {
		return _additionalData;
	}
	
	public boolean checkRestrictions(Game g) {
		// Abilities and spells that require to pay X life
		if (_cost.requiresAdditionalCost(Requirement.PAY_X_LIFE)) {
			if (_controller.getLife() < getXValue())
				return false;
		}
		
		// Abilities and spells that require the controller to sacrifice or return a land he controls to its owner's hand cannot be activated
		// if controller does not control at least a land.
		if (_cost.requiresAdditionalCost(Requirement.RETURN_A_LAND_YOU_CONTROL) ||
				_cost.requiresAdditionalCost(Requirement.SACRIFICE_A_LAND)) {
			if (_controller.getNbLandsControlled() < 1)
				return false;
		}
		
		// Abilities and spells that require the controller to sacrifice a permanent
		// if controller does not control at least a permanent.
		if (_cost.requiresAdditionalCost(Requirement.SACRIFICE_A_PERMANENT)) {
			if (g.getBattlefield().getPermanentsControlledBy(_controller).size() == 0)
				return false;
		}
		
		return true;
	}

	public String getParameter() {
		return _parameter;
	}

	private Vector<Vector<ManaType>> _manaChoices;
	public void addManaCombination(Vector<ManaType> combi) {
		if (_manaChoices == null)
			_manaChoices = new Vector<Vector<ManaType>>();
		_manaChoices.add(combi);
	}
	
	public void setPentaChoice() {
		if (_manaChoices == null)
			_manaChoices = new Vector<Vector<ManaType>>();
		Vector<ManaType> combi;
		for (ManaType mt : ManaType.values()) {
			if (mt != ManaType.COLORLESS) {
				combi = new Vector<Mana.ManaType>();
				combi.add(mt);
				_manaChoices.add(combi);
			}
		}		
	}
	
	public Vector<Vector<ManaType>> getManaChoices() {
		return _manaChoices;
	}
	
	public Vector<ManaType> getManaChoice(int nb) {
		return _manaChoices.get(nb);
	}
}
