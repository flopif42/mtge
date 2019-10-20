package mtgengine.card;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Vector;

import mtgengine.card.UntapOptional;
import mtgengine.CounterType;
import mtgengine.Game;
import mtgengine.MtgObject;
import mtgengine.StackObject;
import mtgengine.Game.Response;
import mtgengine.ability.ActivatedAbility;
import mtgengine.ability.ActivatedAbilityFactory;
import mtgengine.ability.Evergreen;
import mtgengine.ability.SpellAbility;
import mtgengine.ability.StaticAbility;
import mtgengine.ability.TriggeredAbility;
import mtgengine.ability.TriggeredAbility.Origin;
import mtgengine.ability.TriggeredAbility.Event;
import mtgengine.action.SpecialAction.Option;
import mtgengine.action.SpellCast;
import mtgengine.action.SpecialAction;
import mtgengine.cost.AlternateCost;
import mtgengine.cost.Cost;
import mtgengine.cost.ManaCost;
import mtgengine.cost.ManaCost.Symbol;
import mtgengine.damage.DamageSource;
import mtgengine.damage.Damageable;
import mtgengine.effect.ContinuousEffect;
import mtgengine.effect.ContinuousEffect.StopWhen;
import mtgengine.effect.Effect;
import mtgengine.modifier.AbilityModifier;
import mtgengine.modifier.CardTypeModifier;
import mtgengine.modifier.CreatureTypeModifier;
import mtgengine.modifier.EvergreenModifier;
import mtgengine.modifier.Modifier;
import mtgengine.modifier.Modifier.Operation;
import mtgengine.player.Player;
import mtgengine.modifier.PTModifier;
import mtgengine.type.CardType;
import mtgengine.type.CreatureType;
import mtgengine.type.Subtype;
import mtgengine.type.Supertype;
import mtgengine.zone.Zone;

public class Card extends StackObject implements Damageable, DamageSource {
	public static String COLORLESS_MORPH_22 = "MorphCreature";
	private Vector<TriggeredAbility> _triggeredAbilities = new Vector<TriggeredAbility>();
	private Vector<ActivatedAbility> _activatedAbilities = new Vector<ActivatedAbility>();
	private Vector<ContinuousEffect> _continuousEffects = new Vector<ContinuousEffect>();
	private Vector<StaticAbility> _staticAbilities = new Vector<StaticAbility>();
	private Vector<Vector<Card>> _references = new Vector<Vector<Card>>();
	private Vector<DamageSource> _whoDamagedMeThisTurn = new Vector<DamageSource>();

	private int _internalObjectId;
	private int _imageID = -1;
	private boolean _bTapped = false;
	private String _tokenName = null;
	private boolean _bDeathtouched = false;
	private boolean _bSummoningSick = true;
	private boolean _bPutIntoGraveyardFromBattlefieldThisTurn = false;
	private int _damage;
	private int _nbRegenShields = 0;
	private boolean _bMustPayEcho = true;
	private String _printedPT;
	private HashMap<CounterType, Integer> _counters = null;
	private HashMap<CounterType, Integer> _lastKnownCounters = null;
	private Card _source;
	private Color _chosenColor; // For cards like Utopia Sprawl, Iona
	
	private UntapOptional _untapOptional;
	
	// Spell copies (Twincast)
	private boolean _bCopy = false;

	// Double faced cards
	private String _dayFaceCardName = null;
	private String _nightFaceCardName = null;
	
	private Card _faceUp = null;
	
	// spell casting variables
	private boolean _bXSpell = false;
	
	// card spell cast options
	private Vector<SpellCast> _spellCastOptions = new Vector<SpellCast>();
	private SpellCast _spellCastOptionUsed = null;
	
	// card special actions
	private Vector<SpecialAction> _specialActions = new Vector<SpecialAction>();
	
	// card types
	private Vector<CardType> _types = new Vector<CardType>();
	
	// card subtypes
	private Vector<Subtype> _subtypes = new Vector<Subtype>();

	// Creature subtypes
	private Vector<CreatureType> _creatureTypes = new Vector<CreatureType>();
		
	// card supertypes
	private Vector<Supertype> _supertypes = new Vector<Supertype>();
	
	// Color indicators (tokens and cards like Pact of negation or back faces of double faced cards)
	private Vector<Color> _colorIndicators = new Vector<Color>();;
	
	// a creature may attack a player or a planeswalker
	private Damageable _attackRecipient = null;
	
	// a creature may be able to block several creatures, however most of the time it will block only one
	// vector containing all creatures blocked by this
	private Vector<Card> _blockRecipients; 
	
	// vector containing all creatures blocking this
	private Vector<Card> _blockers;
	
	/* Attached permanents */
	private Card _permanentIamAttachedTo;
	private Vector<Card> _permanentsAttachedToMe = new Vector<Card>();

	/* Loyalty */
	private int _loyalty = -1;
	private boolean _bHasAlreadyActivatedLoyaltyAbility;
	
	/* Combat */
	private boolean _bAttacking = false;
	private boolean _bIgnoreBlocker = false;
	private int _damagePrevention = 0;
	
	// Handling copies
	private Card _originalCard = null;

	private boolean _b_preventCombatDamageDealtTo = false;
	private boolean _b_preventCombatDamageDealtBy = false;

	// For effects like Fiend Hunter
	// the key is the card, the value is the internal object ID (that number changes for the same physical card when it changes zone)
	private HashMap<Card, Integer> _linkedCards = new HashMap<Card, Integer>();
	
	// For instant and sorcery
	private SpellAbility _spellEffect;
	
	public Card(String name, int imageID, ManaCost manaCost) {
		super(name);
		_source = this; // used by tokens to know what card created them
		_imageID = imageID;
		_cost = new Cost(manaCost);
		initialize();
	}

	public void generatePerformableActions() {
		if (this.isLandCard()) {
			addSpecialAction(Option.PLAY_LAND/*, "Play a land"*/);
		}
		else {
			addSpellCastOption(SpellCast.Option.CAST);
		}
	}
	
	public void setCounters(HashMap<CounterType, Integer> counters) {
		_counters = counters;
	}
	
	// This method is used not only at object construction but also when the card switches zone
	private void initialize() {
		_bDeathtouched = false;
		_bAttacking = false;
		_damage = 0;
		_damagePrevention = 0;
		_nbRegenShields = 0;
		_bMustPayEcho = true;
		_bSummoningSick = true;
		_bHasAlreadyActivatedLoyaltyAbility = false;
	}
	
	/* Card types */
	public void setCardTypes(Vector<CardType> types) {
		_types.addAll(types);
	}
	
	public Vector<CardType> getCardTypes() {
		return _types;
	}
	
	public Vector<Subtype> getSubtypes() {
		return _subtypes;
	}
	
	public Vector<Supertype> getSupertypes() {
		return _supertypes;
	}
	
	/* Card supertypes */
	public void setSuperType(Supertype... supertypes) {
		for (Supertype supertype : supertypes)
			_supertypes.addElement(supertype);
	}
	
	public void setSuperTypes(Vector<Supertype> supertypes) {
		_supertypes.addAll(supertypes);
	}
	
	public boolean hasSupertype(Supertype supertype) {
		if (_supertypes.contains(supertype))
			return true;
		return false;
	}
	
	/* Card subtypes */
	public void setSubtype(Subtype... subtypes) {
		for (Subtype subtype : subtypes)
			_subtypes.add(subtype);
	}

	public void setSubtypes(Vector<Subtype> subtypes) {
		_subtypes.addAll(subtypes);
	}
	
	/* Creature types */
	public void setCreatureTypes(CreatureType... types) {
		for (CreatureType type : types)
			_creatureTypes.add(type);
	}
	
	public void setCreatureTypes(Vector<CreatureType> types) {
		_creatureTypes.addAll(types);
	}
	
	public boolean hasCreatureType(Game g, CreatureType type) {
		if (hasEvergreenGlobal(Evergreen.CHANGELING, g))
			return true;
		
		for (Modifier mod : _modifiers) {
			if (mod instanceof CreatureTypeModifier)
			{
				if (((CreatureTypeModifier) mod).getCreatureTypes().contains(type))
					return true;
			}
		}
		
		return _creatureTypes.contains(type);
	}
	
	/***************** Power and toughness **************************/
	public static String addPT(String oldValue, String definition, int multiple) {
		String newValue;
		int power;
		int toughness;
		
		// Decompose old value
		String[] ptArray = oldValue.split("/");
		power = Integer.parseInt(ptArray[0]);
		toughness = Integer.parseInt(ptArray[1]);
		
		// Decompose definition
		ptArray = definition.split("/");
		power += (Integer.parseInt(ptArray[0]) * multiple);
		toughness += (Integer.parseInt(ptArray[1]) * multiple);
		
		// Compose and return new value
		newValue = String.format("%d/%d", power, toughness);
		return newValue;	}
	
	public static String addPT(String oldValue, String definition) {
		return addPT(oldValue, definition, 1);
	}
	
	public void setPrintedPT(String strPT) {
		_printedPT = strPT;
	}
	
	public String getPrintedPT() {
		return _printedPT;
	}
	
	private String computePT(Game g) {
		String pt = "0/0";

		// Layer 7a: Effects from characteristic-defining abilities that define power and/or toughness are applied.
		if (_printedPT != null) {
			if (_printedPT.contains("*")) {
				StaticAbility ab = getStaticAbility("pt_star");
				if (ab == null) {
					System.err.println("Error on [" + _name + "] : star (*) in  power/toughness but no ability declared.");
					System.exit(1);
				}
				pt = ab.computeBasePT(g);
			}
			else
				pt = _printedPT;			
		}
		
		
		// The following effects apply only when the creature is OTB
		if (this.isOTB(g)) {
			// Layer 7b: Effects that set power and/or toughness to a specific number or value are applied.
			for (Modifier mod : _modifiers) {
				if (mod instanceof PTModifier) {
					PTModifier ptmod = (PTModifier) mod;
					if (ptmod.getOperation() == Operation.SET)
						pt = ptmod.set();
				}
			}
			for (ContinuousEffect ce : g.getContinuousEffects())
				pt = ce.setPT(g, this, pt);
			
			//Layer 7c: Effects that modify power and/or toughness (but don’t set power and/or toughness to a specific number or value) are applied.
			for (Modifier mod : _modifiers) {
				if (mod instanceof PTModifier) {
					PTModifier ptmod = (PTModifier) mod;
					if (ptmod.getOperation() == Operation.ADD)
						pt = ptmod.add(pt);
				}
			}
			
			for (ContinuousEffect ce : g.getContinuousEffects())
				pt = ce.addPT(g, this, pt);
			
			// Layer 7d: Power and/or toughness changes from counters are applied.
			if (_counters != null) {
				for (CounterType ct : _counters.keySet()) {
					switch (ct) {
					case MINUS_ONE:
						for (int i = 0; i < _counters.get(ct); i++)
							pt = Card.addPT(pt, "-1/-1");
						break;

					case MINUS_ZERO_MINUS_ONE:
						for (int i = 0; i < _counters.get(ct); i++)
							pt = Card.addPT(pt, "-0/-1");
						break;

					case PLUS_ONE:
						for (int i = 0; i < _counters.get(ct); i++)
							pt = Card.addPT(pt, "+1/+1");
						break;

					default:
						break;
					}
				}
			}
			
			// Layer 7e: Effects that switch a creature’s power and toughness are applied.
			for (Modifier mod : _modifiers) {
				if (mod instanceof PTModifier) {
					PTModifier ptmod = (PTModifier) mod;
					if (ptmod.getOperation() == Operation.SWITCH)
						pt = ptmod.switchPT(pt);
				}
			}
		}
		return pt;
	}
	
	public int getPower(Game g) {
		String[] ptArray = computePT(g).split("/");
		return Integer.parseInt(ptArray[0]);
	}
	
	public int getToughness(Game g) {
		String pt = computePT(g);
		String[] ptArray = pt.split("/");
		return Integer.parseInt(ptArray[1]);

	}
	
	public void endUntilEOTEffects() {
		// Remove Modifiers that last "until end of turn"
		Iterator<Modifier> itMod = _modifiers.iterator();
		while (itMod.hasNext()) {
			Modifier mod = itMod.next();
			if (mod.getDuration() == Modifier.Duration.UNTIL_END_OF_TURN)
				itMod.remove();
		}

		// Remove continuous effects that last "until end of turn"
		Iterator<ContinuousEffect> itCe = _continuousEffects.iterator();
		while (itCe.hasNext()) {
			ContinuousEffect ce = itCe.next();
			if (ce.doesStop(StopWhen.END_OF_TURN))
				itCe.remove();
		}
	}
	
	public void endUntilYNTEffects(Game g, Player p) {
		Iterator<Modifier> it = _modifiers.iterator();
		while (it.hasNext()) {
			Modifier mod = (Modifier)it.next();
			if ((mod.getDuration() == Modifier.Duration.UNTIL_YOUR_NEXT_TURN) && (mod.getSource().getController(g) == p))
				it.remove();
		}
	}
	
	public Response dealCombatDamageTo(Game g, Damageable recipient, int amount) {
		// A creature not on the Battlefield cannot deal damage
		if (!this.isOTB(g))
			return Response.ErrorIncorrectZone;
		
		// Effects like Maze of Ith that prevent combat damage dealt by this creature
		if (_b_preventCombatDamageDealtBy)
			return Response.OK;
		
		return dealDamageTo(g, recipient, amount, true);
	}
	
	/**
	 * Deals damage to a recipient. The recipient can be a player, planeswalker, creature, etc...
	 * @param g
	 * @param recipient
	 * @param amount
	 * @param bCombatDamage
	 * @return
	 */
	public Response dealDamageTo(Game g, Damageable recipient, int amount, boolean bCombatDamage) {
		// Do nothing if damage is less than 1
		if (amount < 1)
			return Response.OK;
		
		// Continuous effects that change the damage recipient (i.e. Pariah)
		Vector<ContinuousEffect> effects = g.getContinuousEffects();
		for (ContinuousEffect effect : effects)
			recipient = effect.computeDamageRedirection(g, this, recipient);
		
		// Do nothing if recipient has protection from the source
		if (g.computeIsProtectedFrom(this, (MtgObject) recipient) == true)
			return Response.OK;
		
		// Effects like Maze of Ith that prevent combat damage dealt to this creature
		if (recipient instanceof Card) {
			Card rec = (Card) recipient;
			if (rec.isCreature(g)) {
				if (rec._b_preventCombatDamageDealtTo == true)
					return Response.OK;		
			}
		}
		
		// Continuous effects that prevent damage from being dealt (i.e. Glacial Chasm)
		effects = g.getContinuousEffects();
		for (ContinuousEffect effect : effects) {
			if (effect.isDamagePrevented(g, this, recipient, bCombatDamage))
				return Response.OK;
			effect.computeDamagePrevention(g, this, recipient);   // effects like Urza's Armor
		}
		
		// If the card is Undamageable (Cho-Manno style), do nothing.
		if (recipient.isUndamageable(g))
			return Response.OK;
		
		// Effects that modify how much damage is dealt
		for (ContinuousEffect effect : effects)
			amount = effect.modifyDamageAmount(g, this, recipient, amount);
		
		int dmgDealt = recipient.isDealtDamage(this, amount, g);
		g._lastDamageDealt = dmgDealt;
		if (dmgDealt == 0)
			return Response.OK;

		// check for lifelink
		if (this.hasEvergreenGlobal(Evergreen.LIFELINK, g))
			_controller.gainLife(amount);
		
		// Triggered abilities that trigger upon dealing damage
		if (bCombatDamage) { // Combat damage is dealt
			if (recipient instanceof Player) { // to a player
				g.queueTriggeredAbilities(getTriggeredAbilities(g, Event.ThisDealsCbtDmgToAPlayer, Origin.BTLFLD), (Player) recipient); // to player (Swords of A and B)

				// If the damaged player is the Monarch, assign new Monarch
				if (g.getTheMonarch() != null) {
					Player monarch = g.getTheMonarch();
					if (monarch == recipient) {
						Card theMonarch = (Card) monarch.getCommand().findCardByName("The Monarch");
						g.queueTriggeredAbilities(theMonarch.getTriggeredAbilities(g, Event.MonarchWasDamaged, Origin.COMMAND), this); // the addional data is the damaging creature
					}
				}
			}
			else if ((recipient instanceof Card) && (((Card) recipient).isCreature(g))) {
				Card damagedCreature = (Card) recipient;
				Vector<TriggeredAbility> abs = getTriggeredAbilities(g, Event.ThisDealsCbtDmgToACreature, Origin.BTLFLD);
				g.queueTriggeredAbilities(abs, damagedCreature); // to creature (Basilisk-like effects)
			}
			g.queueTriggeredAbilities(getTriggeredAbilities(g, Event.ThisDealsCbtDmg, Origin.BTLFLD), null);
			
			// Look for equipment and aura triggers COMBAT DAMAGE
			for (Card local : _permanentsAttachedToMe) {
				if (local.hasSubtypeGlobal(g, Subtype.EQUIPMENT)) {
					g.queueTriggeredAbilities(local.getTriggeredAbilities(g, Event.EquippedCreatureDealsCbtDmg, Origin.BTLFLD), null);
					if (recipient instanceof Player) // to a player
						g.queueTriggeredAbilities(local.getTriggeredAbilities(g, Event.EquippedCreatureDealsCbtDmgToAPlayer, Origin.BTLFLD), recipient);
				}
				
				if (local.hasSubtypeGlobal(g, Subtype.AURA)) {
					if (recipient instanceof Player) // to a player
						g.queueTriggeredAbilities(local.getTriggeredAbilities(g, Event.EnchantedCreatureDealsCbtDmgToAPlayer, Origin.BTLFLD), recipient);
				}
			}
		}
		
		// Look for equipment and aura triggers COMBAT and NONCOMBAT DAMAGE
		for (Card local : _permanentsAttachedToMe) {
			if (local.hasSubtypeGlobal(g, Subtype.AURA)) {
				if ((recipient instanceof Card) && ((Card) recipient).isCreature(g))
					g.queueTriggeredAbilities(local.getTriggeredAbilities(g, Event.EnchantedCreatureDealsDmgToACreature, Origin.BTLFLD), recipient);
			}
		}
		
		// Any damage, not only combat
		g.queueTriggeredAbilities(getTriggeredAbilities(g, Event.ThisDealsDamage, Origin.BTLFLD), amount);

		// Damage (combat or non combat) dealt to a player 
		if (recipient instanceof Player)
			g.queueTriggeredAbilities(getTriggeredAbilities(g, Event.ThisDealsDmgToAPlayer, Origin.BTLFLD), (Player) recipient);
		
		// If the damage was dealt to a creature or an opponent (Flesh Reaver)
		if ((recipient instanceof Card) && (((Card) recipient).isCreature(g)) || ((recipient instanceof Player) && ((Player) recipient != _controller)))
			g.queueTriggeredAbilities(getTriggeredAbilities(g, Event.ThisDealsDamageToCreatureOrOpponent, Origin.BTLFLD), amount);
		return Response.OK; 
	}
	
	public boolean isDeathtouched() {
		return _bDeathtouched;
	}
	
	private void markDeathtouch() {
		_bDeathtouched = true;
	}

	public void setTokenName(String tokenName) {
		_tokenName = tokenName;
	}
	
	public boolean isToken() {
		return (_tokenName != null);
	}
	
	public void setTokenSource(Card source) {
		_source = source;
	}
	
	public void addDamage(int damage) {
		_damage += damage;
	}
	
	public void cleanDamage() {
		_damage = 0;
		_damagePrevention = 0;
	}
	
	public int getDamage() {
		return _damage;
	}
	
	/**
	 * Returns the computed converted mana cost of a spell in the given context.
	 * @param g
	 * @return
	 */
	public int getConvertedManaCost(Game g) {
		int ccm = 0;
		
		ManaCost mc = _cost.getManaCost();
		
		if (mc != null)
			ccm += mc.computeCMC();
		else if (_dayFaceCardName != null)
			ccm += CardFactory.create(_dayFaceCardName).getConvertedManaCost(g);

		/*
		 * 203.3b When calculating the converted mana cost of an object with an {X} in its mana cost,
		 * X is treated as 0 while the object is not on the stack, and X is treated as the number
		 * chosen for it while the object is on the stack.
		 */
		if (_bXSpell && this.isOTS(g)) {
			// count the number of occuremces of {X} in the mana cost and multiply by the X value chose when cast
			ccm += (mc.getNumberOfX() * this.getXValue());
		}
		return ccm;
	}
	
	// Functions that test if the card has a certain card type as printed on it
	private boolean hasCardTypePrinted(CardType type) {
		if (_types.contains(type))
			return true;
		return false;
	}

	public boolean hasSubtypePrinted(Subtype st) {
		if (_subtypes.contains(st))
			return true;
		return false;
	}
	
	public boolean isPermanentCard() {
		if (_types.contains(CardType.ARTIFACT) ||
			_types.contains(CardType.CREATURE) ||
			_types.contains(CardType.ENCHANTMENT) ||
			_types.contains(CardType.LAND) ||
			_types.contains(CardType.PLANESWALKER))
			return true;
		return false;
	}

	
	public boolean isCreatureCard() {
		return hasCardTypePrinted(CardType.CREATURE);
	}
	
	public boolean isArtifactCard() {
		return hasCardTypePrinted(CardType.ARTIFACT);
	}

	public boolean isEnchantmentCard() {
		return hasCardTypePrinted(CardType.ENCHANTMENT);
	}

	public boolean isSorceryCard() {
		return hasCardTypePrinted(CardType.SORCERY);
	}
	
	public boolean isTribalCard() {
		return hasCardTypePrinted(CardType.TRIBAL);
	}
	
	public boolean isInstantCard() {
		return hasCardTypePrinted(CardType.INSTANT);
	}
	
	public boolean isPlaneswalkerCard() {
		return hasCardTypePrinted(CardType.PLANESWALKER);
	}
	
	public boolean isLandCard() {
		return hasCardTypePrinted(CardType.LAND);
	}
	
	// Functions that test if the card a certain card subtype in the context of the game
	public boolean hasSubtypeGlobal(Game g, Subtype st) {
		/* Effects that force the land to be an Island and nothing else */
		for (ContinuousEffect ce : g.getContinuousEffects()) {
			if (ce.makesIsland(this)) {
				if (st == Subtype.ISLAND)
					return true;
				return false;
			}
				
		}
		
		// Check for values printed on cards
		if (hasSubtypePrinted(st)) 
			return true;
		
		return false;
	}

	// Functions that test if the card a certain card type in the context of the game
	public boolean hasCardTypeGlobal(Game g, CardType t) {
		boolean bSet = false;
		
		if (t != CardType.ENCHANTMENT) {
			for (ContinuousEffect ce : _continuousEffects) {
				if (ce.getAbilityName().equals("soulSculpted"))
					return false;
			}
		}
		
		if (t == CardType.ENCHANTMENT) {
			for (ContinuousEffect ce : _continuousEffects) {
				if (ce.getAbilityName().equals("soulSculpted"))
					return true;
			}
		}
		
		// check Modifiers
		for (Modifier mod : _modifiers) {
			if (mod instanceof CardTypeModifier) {
				CardTypeModifier ctmod = (CardTypeModifier) mod;
				if (ctmod.getSetTypes().isEmpty()) {
					if (ctmod.getAddedTypes().contains(t))
						return true;	
				}
				else {
					bSet = true;
					if (ctmod.getSetTypes().contains(t))
						return true;	
				}
			}
		}
		
		// Check for values printed on cards
		if (!bSet && hasCardTypePrinted(t)) 
			return true;
		
		// check continuous effects from other cards
		for (ContinuousEffect ce : g.getContinuousEffects()) {
			if (ce.grantsCardType(g, this, t))
				return true;
		}
		return false;
	}
	
	public boolean isCreature(Game g) {
		return hasCardTypeGlobal(g, CardType.CREATURE);
	}
	
	public boolean isEnchantment(Game g) {
		return hasCardTypeGlobal(g, CardType.ENCHANTMENT);
	}
	
	public boolean isArtifact(Game g) {
		return hasCardTypeGlobal(g, CardType.ARTIFACT);
	}
	
	public boolean isLand(Game g) {
		return hasCardTypeGlobal(g, CardType.LAND);
	}
	
	/**
	 * returns true if the card is on the battlefield
	 * false otherwise
	 * @return
	 */
	public boolean isOTB(Game game) {
		if (game.getBattlefield().contains(this))
			return true;
		return false;
	}
	
	/**
	 * Card is in hand ?
	 * @param g
	 * @return
	 */
	public boolean isIH(Game g) {
		if (_owner.getHand().contains(this))
			return true;
		return false;
	}
	
	/**
	 * Card is on the stack ?
	 * @param g
	 * @return
	 */
	public boolean isOTS(Game game) {
		if (game.getStack().contains(this))
			return true;
		return false;
	}

	/**
	 * Card is in the graveyard ?
	 * @param g
	 * @return
	 */
	public boolean isIGY(Game game) {
		if (_owner.getGraveyard().contains(this))
			return true;
		return false;
	}
	
	/**
	 * Card is in Exile ?
	 * @param g
	 * @return
	 */
	public boolean isIEX(Game game) {
		if (_owner.getExile().contains(this))
			return true;
		return false;
	}

	public void tap(Game g) {
		tap(g, true);
	}
	
	public void tap(Game g, boolean trigger) {
		// do not do anything if card is already tapped
		if (_bTapped)
			return;
		
		_bTapped = true;
		// if trigger parameter == false, do not trigger abilities "whenever X becomes tapped" (i.e. lands that come into play tapped)
		if (!trigger)
			return;

		// Queue triggered abilites triggering on cards becoming tapped (like city of brass)
		g.queueTriggeredAbilities(getTriggeredAbilities(g, Event.BecomesTapped, Origin.BTLFLD), null);
		
		for (Card local : _permanentsAttachedToMe) {
			if (local.hasSubtypeGlobal(g, Subtype.AURA))
				g.queueTriggeredAbilities(local.getTriggeredAbilities(g, Event.EnchantedLandBecomesTapped, Origin.BTLFLD), null);
		}
	}
	
	public void untap(Game g) {
		_bTapped = false;
		
		// Disable continuous effects that only work while the card is tapped (i.e. Mana Leech)
		Vector<ContinuousEffect> effectsToRemove = new Vector<ContinuousEffect>();
		for (ContinuousEffect ce : g.getContinuousEffects()) {
			if (ce.getSource() == this) {
				// flag effect for removal
				if (ce.doesStop(StopWhen.SOURCE_UNTAPS))
					effectsToRemove.add(ce);
			}
		}
		
		// remove flagged effects
		for (ContinuousEffect ce : effectsToRemove)
			g.getContinuousEffects().remove(ce);
	}
	
	public boolean isTapped() {
		return _bTapped;
	}

	public void setPutIntoGraveyardThisTurn(boolean val) {
		_bPutIntoGraveyardFromBattlefieldThisTurn = val;
	}
	
	public boolean wasPutIntoGraveyardThisTurn() {
		return _bPutIntoGraveyardFromBattlefieldThisTurn;
	}
	
	public void newTurn(Game game) {
		_whoDamagedMeThisTurn.clear();
		_b_preventCombatDamageDealtBy = false;
		_b_preventCombatDamageDealtTo = false;
		_bHasAlreadyActivatedLoyaltyAbility = false;
		_nbRegenShields = 0;
		if (isOTB(game) && (_controller == game.getActivePlayer()))
			_bSummoningSick = false;
		for (ActivatedAbility aa : _activatedAbilities)
			aa.resetNbActivation();
	}
	
	private boolean hasAnySubtypeGlobal(Game g) {
		for (Subtype subtype : Subtype.values()) {
			if (hasSubtypeGlobal(g, subtype))
				return true;
		}
		return false;
	}
	
	public Vector<String> getCardInfo(Game g, Player requester) {
		String displayName = this.getDisplayName();
		Vector<String> ret = new Vector<String>();

		// Mana cost (if any)
		if (_cost.getManaCost() != null)
			ret.add(_cost.getManaCost().toString());
		else
			ret.add("");

		// Card name
		if (displayName != null)
			ret.add(displayName);
		
//		// CMC
//		ret.add("[" + getConvertedManaCost(g) + "]");
//		
//		// Card color(s) if any
//		String colorLine = "";
//		if (this.getColors().size() > 0) {
//			for (Color col : this.getColors())
//				colorLine += col.toString() + " ";
//		}
//		ret.add(colorLine);
//		ret.add(" ");
		
		// Card supertype(s) Basic, Legendary, ...
		String typeLine = "";
		if (_supertypes.size() > 0) {
			for (Supertype spt : _supertypes)
				typeLine += spt.toString() + " ";
		}
		
		if (this.isToken())
			typeLine += "Token ";
		
		// Card type(s) Artifact, Creature, Land, ...
		for (CardType type : CardType.values()) {
			if (hasCardTypeGlobal(g, type))
				typeLine += type.toString() + " ";
		}
		
		// noncreature subtype(s) Forest, Equipment
		if (this.hasAnySubtypeGlobal(g)) {
			typeLine += "- ";
			for (Subtype subtype : Subtype.values()) {
				if (hasSubtypeGlobal(g, subtype))
					typeLine += subtype.toString() + " ";
			}
		}
			
		// creature type(s) Soldier, Goblin, Angel, ...
		Vector<CreatureType> ct = getCreatureTypes();
		if (ct.size() > 0) {
			typeLine += "- ";
			for (CreatureType st : ct)
				typeLine += st.toString() + " ";
		}
		ret.add(typeLine);
		ret.add(" ");

		ManaCost mc = _cost.getManaCost();
		
		// if the card mana cost contains Phyrexian mana, write the reminder here.
		if ((mc != null) && (mc.hasPhyrexianMana())) {
			String phyrexianManaReminder = "";
			
			if (mc.contains(ManaCost.Symbol.Phyrexian_W))
				phyrexianManaReminder = "<i>({w/p} can be paid with either {W} or 2 life.)</i>§";
			if (mc.contains(ManaCost.Symbol.Phyrexian_U))
				phyrexianManaReminder = "<i>({u/p} can be paid with either {U} or 2 life.)</i>§";
			if (mc.contains(ManaCost.Symbol.Phyrexian_B))
				phyrexianManaReminder = "<i>({b/p} can be paid with either {B} or 2 life.)</i>§";
			if (mc.contains(ManaCost.Symbol.Phyrexian_R))
				phyrexianManaReminder = "<i>({r/p} can be paid with either {R} or 2 life.)</i>§";
			if (mc.contains(ManaCost.Symbol.Phyrexian_G))
				phyrexianManaReminder = "<i>({g/p} can be paid with either {G} or 2 life.)</i>§";
			ret.add(phyrexianManaReminder);
		}
		
		// if the card mana cost contains monocolor hybrid mana, write the reminder here.
		//"{2/u} can be paid with any two mana or with {U}. This card's converted mana cost is %."
		if ((mc != null) && (mc.hasMonocolorHybridMana())) {
			String hybridSymbol = "";
			String normalSymbol = "";
			
			if (mc.contains(ManaCost.Symbol.Hybrid_W)) {
				hybridSymbol = "{2/w}";
				normalSymbol = "{W}";
			}
			if (mc.contains(ManaCost.Symbol.Hybrid_U)) {
				hybridSymbol = "{2/u}";
				normalSymbol = "{U}";
			}
			if (mc.contains(ManaCost.Symbol.Hybrid_B)) {
				hybridSymbol = "{2/b}";
				normalSymbol = "{B}";
			}
			if (mc.contains(ManaCost.Symbol.Hybrid_R)) {
				hybridSymbol = "{2/r}";
				normalSymbol = "{R}";
			}
			if (mc.contains(ManaCost.Symbol.Hybrid_G)) {
				hybridSymbol = "{2/g}";
				normalSymbol = "{G}";
			}
			
			ret.add(String.format("<i>(%s can be paid with any two mana or with %s. This card's converted mana cost is %d.)</i>§", hybridSymbol, normalSymbol, this.getConvertedManaCost(g)));
		}
		
		// uncounterability
		if (hasEvergreenGlobal(Evergreen.UNCOUNTERABLE, g)) {
			String uncounterableText = _name + " can't be countered";
			if (this.hasTargetRequirements())
				uncounterableText += " by spells or abilities";
			uncounterableText += ".";
			ret.add(uncounterableText);
		}
		
		// inability to block
		if (hasEvergreenGlobal(Evergreen.CANTBLOCK, g))
			ret.add(displayName + " can't block.");
		
		// No untapping during the untap step
		if (hasEvergreenGlobal(Evergreen.NOUNTAP, g))
			ret.add(displayName + " doesn't untap during your untap step.");
		
		// unblockability
		if (hasEvergreenGlobal(Evergreen.UNBLOCKABLE, g))
			ret.add(displayName + " can't be blocked.");
		
		// undamageability
		if (hasEvergreenGlobal(Evergreen.UNDAMAGEABLE, g))
			ret.add("Prevent all damage that would be dealt to " + displayName + ".");
		
		// Evergreen abilities
		String evergreenLine = "";
		for (Evergreen ev : Evergreen.values())
		{
			// The following abilities have customized text
			if ((ev == Evergreen.UNBLOCKABLE) ||
					(ev == Evergreen.CANTBLOCK) ||
					(ev == Evergreen.UNCOUNTERABLE) ||
					(ev == Evergreen.UNDAMAGEABLE) ||
					(ev == Evergreen.IGNORE_DEFENDER) ||
					(ev == Evergreen.NOUNTAP))
				continue;
			
			if (hasEvergreenGlobal(ev, g))
			{
				if (evergreenLine.length() > 0)
					evergreenLine += ", ";
				evergreenLine += ev.getFullName();
			}
		}
		if (evergreenLine.length() > 0)
			ret.add(evergreenLine);
		
		// Protections
		String protectionLine = "";
		for (Object prot : getProtections(g)) {
			if (protectionLine.length() == 0)
				protectionLine = "Protection from ";
			else
				protectionLine += " and from ";
			
			protectionLine += prot.toString();
		}
		if (protectionLine.length() > 0)
			ret.add(protectionLine);
		
		// Static abilities 
		for (StaticAbility sa : _staticAbilities)
			ret.add(sa.getDescription());
		
		// Continuous effects
		for (ContinuousEffect ce : _continuousEffects)
			ret.add(ce.getDescription());
		
		// Triggered abilities
		for (TriggeredAbility ta : getTriggeredAbilities(g)) {
			if (!ta.isSubAbility())
				ret.add(ta.getDescription());
		}
		
		// Activated abilities
		for (ActivatedAbility aa : getActivatedAbilities(g, false))
			ret.add(aa.getDescription());
		
		// Power and toughness if it's a creature or vehicle
		if (hasCardTypeGlobal(g, CardType.CREATURE) || hasSubtypePrinted(Subtype.VEHICLE)) {
			String ptLine = String.format("%d/%d", getPower(g), getToughness(g));
			if (_damage > 0)
				ptLine += " " + _damage + "!";
			ret.add(ptLine);
		}
		
		// Loyalty if it's a Planeswalker
		if (isPlaneswalkerCard()) {
			ret.add(String.format("[%d]", getLoyalty(g)));
		}
		
		// Effect text if it's an instant or sorcery
		if (isInstantCard() || isSorceryCard())
			ret.add(_spellEffect.getDescription());
		
		// Face-up card if it's a creature with morph or megamorph
		if ((_faceUp != null) && (requester == _controller)) {
			ret.add("--- Other face ---");
			ret.addAll(_faceUp.getCardInfo(g, _controller));
		}
		
		// If it's a copy, show the original card
		if (_bCopy)
			ret.addAll(_originalCard.getCardInfo(g, _controller));
		
		// If another card is linked to this card (i.e. Fiend Hunter's ability), write it
		if (!_linkedCards.isEmpty()) {
			ret.add("--- Linked card(s) ---");
			for (Card c : _linkedCards.keySet())
				ret.add(c.getName());
		}
		
		// Chosen color (if any)
		if (_chosenColor != null)
			ret.add("The chosen color is : " + _chosenColor.toString());
		return ret;
	}
	
	public boolean hasAbilities() {
		if (_staticAbilities.isEmpty() && _activatedAbilities.isEmpty() && _triggeredAbilities.isEmpty() && _continuousEffects.isEmpty() && _evergreenAbilities.isEmpty())
			return false;
		return true;
	}
	
	/**
	 * Called after a card has been switched from a zone to another
	 */
	public void zoneSwitched() {
		initialize();
		_modifiers.clear();

		// Unattach me
		if (_permanentIamAttachedTo != null)
			_permanentIamAttachedTo.unattach(this);
		
		// Unattach permanents from me
		while (_permanentsAttachedToMe.size() > 0)
			unattach(_permanentsAttachedToMe.get(0));
	}

	/* Triggered and Activated Abilities */
	public void addTriggeredAbility(TriggeredAbility... abilities) {
		for (TriggeredAbility ta : abilities)
			_triggeredAbilities.add(ta);
	}

	public boolean hasTriggeredAbility(String abilityName) {
		for (TriggeredAbility ta : _triggeredAbilities) {
			if (ta.getAbilityName().equals(abilityName))
				return true;
		}
		return false;
	}
	
	/**
	 * This is used by the XML generation.
	 * @return
	 */
	public Vector<TriggeredAbility> getPrintedTriggeredAbilities() {
		return _triggeredAbilities;
	}
	
	/**
	 * This is used by the game engine to find the abilities to queue depending on the event and the origin
	 * @param g
	 * @param event
	 * @param origin
	 * @return
	 */
	public Vector<TriggeredAbility> getTriggeredAbilities(Game g, Event event, Origin origin) {
		return getTriggeredAbilities(g, event, origin, false);
	}
	
	/**
	 * This is used only by cardinfo
	 * @param g
	 * @return
	 */
	public Vector<TriggeredAbility> getTriggeredAbilities(Game g) {
		return getTriggeredAbilities(g, null, null, true);
	}
	
	private Vector<TriggeredAbility> getTriggeredAbilities(Game g, Event event, Origin origin, boolean bCardinfo) {
		TriggeredAbility templateAbility;
		TriggeredAbility clonedAbility;
		Vector<TriggeredAbility> tmp = new Vector<TriggeredAbility>();
		Vector<TriggeredAbility> ret = new Vector<TriggeredAbility>();

		// Effects that strip all abilities (i.e. Soul Sculptor and Humble)
		if (hasContinuousEffect(g, "soulSculpted") || hasContinuousEffect(g, Effect.LOSE_ALL_ABILITIES) || hasContinuousEffect(g, "lingeringMirage_strip"))
			return ret;
		
		// Add triggered abilities on the card itself
		for (int i = 0; i < _triggeredAbilities.size(); i++) {
			templateAbility = _triggeredAbilities.get(i);
			if (bCardinfo || (templateAbility.getEvent() == event) && ((templateAbility.getOrigin() == origin) || (templateAbility.getOrigin() == Origin.ANYWHERE)))
				tmp.add(templateAbility);
		}
			
		// Add triggered abilities granted by Modifiers (i.e. Raging Ravine gets the ability "When this attacks...")
		for (Modifier mod : _modifiers) {
			if (mod instanceof AbilityModifier) {
				StackObject so = ((AbilityModifier) mod).getAbility();
				if (so instanceof TriggeredAbility) {
					templateAbility = (TriggeredAbility) so;
					if (bCardinfo || (templateAbility.getEvent() == event) && ((templateAbility.getOrigin() == origin) || (templateAbility.getOrigin() == Origin.ANYWHERE)))
						tmp.add(templateAbility);
				}
			}
		}
		
		// Clone the abilities
		for (TriggeredAbility t : tmp) {
			clonedAbility = t.clone();
			clonedAbility.setController(g, _controller);
			clonedAbility.setOwner(_owner);
			ret.add(clonedAbility);
		}
		
		// Add triggered abilities granted by other cards (i.e. Tabernacle)
		Vector<ContinuousEffect> effects = g.getContinuousEffects();
		for (ContinuousEffect effect : effects) {
			ret.addAll(effect.getGrantedTriggeredAbilities(this, g, event, bCardinfo));
		}
		
		return ret;
	}

	/**
	 * SpellEffect class is used only by instant and sorceries.
	 * @param create
	 */
	public void setSpellEffect(SpellAbility effect) {
		_spellEffect = effect;
	}
	
	// Static abilities
	public void addStaticAbility(StaticAbility... abilities) {
		for (StaticAbility ability : abilities)
			_staticAbilities.add(ability);
	}
	
	/**
	 * Returns a vector with all static abilities on the card as printed
	 * @return
	 */
	public Vector<StaticAbility> getPrintedStaticAbilities() {
		return _staticAbilities;
	}

	
	// Activated abilities
	/**
	 * Add an activated ability to the card
	 * @param abilities
	 */
	public void addActivatedAbility(ActivatedAbility... abilities) {
		for (ActivatedAbility ability : abilities)
			_activatedAbilities.add(ability);
	}
	
	/**
	 * Returns a vector with all activated abilities on the card as printed
	 * @return
	 */
	public Vector<ActivatedAbility> getPrintedActivatedAbilities() {
		return _activatedAbilities;
	}
	
	/**
	 * Returns a vector with all activated abilities as in the specified game state
	 * @param g
	 * @param bOnlyActivable true : returns only the abilities that can be activated at this state
	 * @return
	 */
	public Vector<ActivatedAbility> getActivatedAbilities(Game g, boolean bOnlyActivable) {
		Vector<ActivatedAbility> temp = new Vector<ActivatedAbility>();
		Vector<ActivatedAbility> availableAbs = new Vector<ActivatedAbility>();
		
		// Soul Sculptor special
		// effects that strip all abilities (i.e. Soul Sculptor and Humble)
		if (hasContinuousEffect(g, "soulSculpted") || hasContinuousEffect(g, Effect.LOSE_ALL_ABILITIES))
			return availableAbs;
		
		if (hasContinuousEffect(g, "lingeringMirage_strip")) {
			ActivatedAbility bluemana = ActivatedAbilityFactory.create("island", this);
			availableAbs.add(bluemana);
			return availableAbs;
		}
		
		// add activated abilities printed on the card
		temp.addAll(_activatedAbilities);
		
		// add activated abilities granted from modifiers
		for (Modifier mod : _modifiers) {
			if (mod instanceof AbilityModifier) {
				StackObject so = (StackObject) ((AbilityModifier) mod).getAbility();
				if (so instanceof ActivatedAbility)
					temp.add((ActivatedAbility) so);
			}
		}
		
		// Add activated abilities granted by continuous effects from other sources than the card itself (i.e. Cryptolith Rite)
		for (ContinuousEffect ce : g.getContinuousEffects())
			temp.addAll(ce.getGrantedActivatedAbilities(this, g));
		
		for (ActivatedAbility aa : temp) {
			aa.setController(g, _controller);
			
			if (bOnlyActivable) {
				// Check activability zone
				if (aa.getAvailabilityZone() != getZone(g).getName())
					continue;
				
				// Check if the ability can be activated now
				if (aa.checkRestrictions(g) == false)
					continue;
				
				// Check targeting availability
				if (aa.hasTargetRequirements() && !aa.hasLegalTargets(g))
					continue;
			}
			availableAbs.add(aa);
		}
		return availableAbs;
	}	
	
	public boolean hasContinuousEffects() {
		return (!_continuousEffects.isEmpty());
	}

	public boolean hasStaticAbilities() {
		return (!_staticAbilities.isEmpty());
	}
	
	public void removeContinuousEffects(ContinuousEffect ce) {
		if (_continuousEffects.contains(ce))
			_continuousEffects.remove(ce);
	}
	
	public Vector<ContinuousEffect> getContinuousEffects(Zone.Name activeZone) {
		Vector<ContinuousEffect> effects = new Vector<ContinuousEffect>();
		ContinuousEffect effect;
		
		for (int i = 0; i < _continuousEffects.size(); i++) {
			effect = _continuousEffects.get(i);
			if ((effect.getActiveZone() == activeZone)) {
				effects.add(_continuousEffects.get(i));
			}
		}
		return effects;
	}

	public void addContinuousEffect(ContinuousEffect... effects) {
		for (ContinuousEffect ce : effects)
			_continuousEffects.add(ce);
	}
	
	public void addContinuousEffects(Vector<ContinuousEffect> effects) {
		_continuousEffects.addAll(effects);
	}
	
	public boolean mustPayEcho() {
		return _bMustPayEcho ;
	}

	public void setEchoPaid() {
		_bMustPayEcho = false;
	}

	public void attachLocalPermanent(Card auraOrEquipment) {
		// 1. card previously equipped must update its '_permanentsAttachedToMe' object (remove)
		Card previousHost = auraOrEquipment.getHost();
		if (previousHost != null)
			previousHost.unattach(auraOrEquipment);
		
		// 2. *this (newly equipped) must update its '_permanentsAttachedToMe' object (add)
		if (!_permanentsAttachedToMe.contains(auraOrEquipment))
			_permanentsAttachedToMe.add(auraOrEquipment);
		
		// 3. equipment must update its '_permanentsIamAttachedTo' object (add *this)
		auraOrEquipment.setHost(this);
	}

	private void setHost(Card host) {
		_permanentIamAttachedTo = host;
	}
	
	public Card getHost() {
		return _permanentIamAttachedTo;
	}

	private void unattachFromHost() {
		_permanentIamAttachedTo = null;
	}
	
	public void unattach(Card equipment) {
		_permanentsAttachedToMe.remove(equipment);
		equipment.unattachFromHost();
	}

	public boolean hasAuras() {
		if (_permanentsAttachedToMe.size() == 0)
			return false;
		return true;
	}
	
	public Vector<Card> getAuras(Game g) {
		Vector<Card> auras = new Vector<Card>();
		for (Card aura : _permanentsAttachedToMe) {
			if (aura.isEnchantment(g) && aura.hasSubtypePrinted(Subtype.AURA))
				auras.add(aura);
		}
		return auras;
	}

	public boolean hasHost() {
		return _permanentIamAttachedTo != null;
	}
	
	public boolean hasCounters() {
		return (_counters != null) && (_counters.size() > 0);
	}
	
	public boolean hasCounters(CounterType t) {
		if (_counters == null)
			return false;
		if (_counters.containsKey(t) == false)
			return false; 
		return true;
	}
	
	public int getNbCountersOfType(Game g, CounterType t) {
		int ret = 0;

		if (isOTB(g)) {
			if ((_counters == null) || (_counters.containsKey(t) == false))
				ret = 0;
			else
				ret = _counters.get(t);
		}
		else // the permanent is no longer on the battlefield, its counters have been reset
			 // we must use the last known information
		{
			if (t == CounterType.LOYALTY)
				ret = _loyalty;
			else
			{
				if ((_counters != null) && _counters.containsKey(t))
					ret = _counters.get(t);
				else if ((_lastKnownCounters != null) && _lastKnownCounters.containsKey(t))
					ret = _lastKnownCounters.get(t);
				else
					ret = 0;
			}
		}
		return ret;
	}
	
	public String printCounters() {
		String ret = "";
		
		Iterator<Entry<CounterType, Integer>> it = _counters.entrySet().iterator();
		while (it.hasNext()) {
			Entry<CounterType, Integer> pair = (Entry<CounterType, Integer>) it.next();
			ret += pair.getKey().toString() + "*" + pair.getValue().toString() + "|";
		}
		return ret;
	}
	
	public int getImageID() {
		return _imageID;
	}
	
	public int getID() {
		return _id;
	}
	
	public boolean isNamed(String cardName) {
		return ((_name != null) && getDisplayName().equals(cardName));
	}
	
	public String getDisplayName() {
		if (_tokenName != null)
			return _tokenName;
		return _name;
	}
	
	public String getName() {
		return _name;
	}
	
	public String getSystemName() {
		return _name + "@" + _imageID + "#" + _id;
	}

	public Effect getEffect() {
		return _spellEffect.getEffect();
	}
	
	public void addCounter(Game g, CounterType ct, int nb) {
		if (nb <= 0 )
			return;
		
		// check for effects that modify the number of counters received
		Vector<ContinuousEffect> effects = g.getContinuousEffects();
		for (ContinuousEffect effect : effects)
			nb = effect.modifyNbCounters(g, this, ct, nb);
		
		if (nb <= 0 )
			return;
		
		if (_counters == null)
			_counters = new HashMap<CounterType, Integer>();
		
		if (_counters.containsKey(ct))
			_counters.put(ct, _counters.get(ct) + nb);
		else
			_counters.put(ct, nb);
		
		/* Trigger saga */
		if (ct == CounterType.LORE) {
			g.queueSagaEffects(this);
		}
	}
	
	public void removeCounter(Game g, CounterType ct, int nb) {
		if ((_counters == null) || !_counters.containsKey(ct))
			return;
		if (_counters.get(ct) <= nb)
			_counters.put(ct, 0);
		else
			_counters.put(ct, _counters.get(ct) - nb);
		if (_counters.get(ct) == 0)
			_counters.remove(ct);
		
		// In case of Vanishing, trigger the sacrifice ability if the last counter was removed
		if ((hasStaticAbility("vanishing")) && (ct == CounterType.TIME)) {
			if ((nb > 0) && !_counters.containsKey(CounterType.TIME))
				g.queueTriggeredAbilities(getTriggeredAbilities(g, Event.LastTimeCounterRemoved, Origin.BTLFLD), null);	
		}

		// In case of Suspend, trigger the "cast without paying mana cost" effect if the last counter was removed
		if ((hasStaticAbility(StaticAbility.SUSPEND)) && (ct == CounterType.TIME)) {
			if ((nb > 0) && !_counters.containsKey(CounterType.TIME))
				g.queueTriggeredAbilities(getTriggeredAbilities(g, Event.LastTimeCounterRemoved, Origin.EXILE), null);	
		}

	}
	
	public void increaseLoyalty(Game g, int nb) {
		addCounter(g, CounterType.LOYALTY, nb);
	}
	
	public void decreaseLoyalty(Game g, int nb) {
		removeCounter(g, CounterType.LOYALTY, nb);
	}
	
	public void setLoyalty(int nbCounters) {
		_loyalty = nbCounters;
	}
	
	public int getPrintedLoyalty() {
		return _loyalty;
	}
	
	/**
	 * Put counter(s) on permanents as they enter the battlefield
	 * @param g
	 */
	public void putBaseCounters(Game g) {
		// Loyalty
		if (_loyalty != -1)
			addCounter(g, CounterType.LOYALTY, _loyalty);

		// Counters that are added as a result of a static ability (i.e. Spike Feeder)
		for (StaticAbility ability : _staticAbilities)
			ability.putCounters(g, this);
	}

	public int getLoyalty(Game g) {
		return getNbCountersOfType(g, CounterType.LOYALTY);
	}

	public boolean hasAlreadyActivatedLoyaltyAbility() {
		return _bHasAlreadyActivatedLoyaltyAbility;
	}

	public void setAlreadyActivatedLoyaltyAbility() {
		_bHasAlreadyActivatedLoyaltyAbility = true;
	}

	public void attacks(Damageable recipient) {
		_bAttacking = true;
		_attackRecipient = recipient;
	}
	
	public void clearAttackRecipient() {
		_attackRecipient = null;
	}
	
	public Vector<Card> getBlockedCreatures() {
		return _blockRecipients;
	}
	
	public void blocks(Card attacker) {
		if (_blockRecipients == null)
			_blockRecipients = new Vector<Card>();
		_blockRecipients.add(attacker);
		attacker.addBlocker(this);
	}

	private void addBlocker(Card blocker) {
		if (_blockers == null)
			_blockers = new Vector<Card>();
		_blockers.add(blocker);
	}

	public Vector<Card> getBlockers() {
		return _blockers;
	}
	
	public void stopAttacking() {
		_bAttacking = false;
		_attackRecipient = null;
		if (_blockers != null) {
			for (Card blocker : _blockers) {
				blocker.getBlockedCreatures().remove(this);
			}
			_blockers.clear();
		}
	}
	
	public void stopBlocking() {
		for (Card blockedCreature : _blockRecipients)
			blockedCreature.getBlockers().remove(this);
		_blockRecipients.clear();
		
	}
	
	public Damageable getAttackRecipient() {
		return _attackRecipient;
	}

	public boolean canAttack(Game g) {
		// Check it's a creature
		if (!isCreature(g))
			return false;
		
		// Check it's controlled by the active player
		if (getController(g) != g.getActivePlayer())
			return false;

		// Check it's untapped and does not have summoning sickness
		if (!canTap(g))
			return false;
		
		// Check it doesn't have Defender
		if (hasEvergreenGlobal(Evergreen.DEFENDER, g) && !hasEvergreenGlobal(Evergreen.IGNORE_DEFENDER, g))
			return false;
		
		// Check for continuous effects preventing a creature from attacking
		Vector<ContinuousEffect> effects = g.getContinuousEffects();
		for (ContinuousEffect effect : effects) {
			if (effect.canAttack(this, g) == false)
				return false;
		}
		
		return true;
	}

	public boolean canTap(Game g) {
		// Creatures have haste when in debugging mode
		if (g.getDebugMode())
			return true;
		
		// Card is not on the battlefield
		if (!isOTB(g))
			return false;
		
		// Permanent is tapped
		if (isTapped())
			return false;

		// Permanent is affected by summoning sickness
		if (isCreature(g) && _bSummoningSick && !hasEvergreenGlobal(Evergreen.HASTE, g))
			return false;
		
		return true;
	}
	
	/**
	 * Checks if this creature can be declared as a blocker (is a creature, is untapped, etc)
	 * @param g
	 * @return
	 */
	public boolean canBlock(Game g) {
		if (!isOTB(g))
			return false;
		
		if (!isCreature(g))
			return false;
		
		if (_controller == g.getActivePlayer())
			return false;
		
		if (isTapped())
			return false;
		
		if (hasEvergreenGlobal(Evergreen.CANTBLOCK, g))
			return false;
		
		return true;
	}
	
	/**
	 * Checks if this creature can block a specific attacking creature (evasion, protection, etc)
	 * @param g
	 * @param attacker
	 * @return
	 */
	public boolean canBlockAttacker(Game g, Card attacker) {
		Player defendingPlayer = this.getController(g);
		
		// check if creature is actually attacking
		if (!attacker.isAttacking(g))
			return false;
		
		// check if creature can block (is a creature, is untapped)
		if (!canBlock(g))
			return false;
		
		// check unblockable
		if (attacker.hasEvergreenGlobal(Evergreen.UNBLOCKABLE, g))
			return false;
		
		// check landwalk
		if (attacker.hasEvergreenGlobal(Evergreen.PLAINSWALK, g) && defendingPlayer.controlsABasicLandType(Subtype.PLAINS))
			return false;
		if (attacker.hasEvergreenGlobal(Evergreen.ISLANDWALK, g) && defendingPlayer.controlsABasicLandType(Subtype.ISLAND))
			return false;
		if (attacker.hasEvergreenGlobal(Evergreen.SWAMPWALK, g) && defendingPlayer.controlsABasicLandType(Subtype.SWAMP))
			return false;
		if (attacker.hasEvergreenGlobal(Evergreen.MOUNTAINWALK, g) && defendingPlayer.controlsABasicLandType(Subtype.MOUNTAIN))
			return false;
		if (attacker.hasEvergreenGlobal(Evergreen.FORESTWALK, g) && defendingPlayer.controlsABasicLandType(Subtype.FOREST))
			return false;
		
		// check flying
		if (attacker.hasEvergreenGlobal(Evergreen.FLYING, g)) { // attacker has flying
			if (!this.hasEvergreenGlobal(Evergreen.FLYING, g) && !this.hasEvergreenGlobal(Evergreen.REACH, g)) // blocker doesnt't have flying AND blocker doesn't have reach
				return false;
		}

		// check fear
		if (attacker.hasEvergreenGlobal(Evergreen.FEAR, g)) { // attacker has fear
			if (!(this.hasColor(Color.BLACK) || this.isArtifact(g)))
				return false;
		}
		
		// check shadow step 1/2
		if (attacker.hasEvergreenGlobal(Evergreen.SHADOW, g)) { // attacker has shadow
			if (!this.hasEvergreenGlobal(Evergreen.SHADOW, g)) // blocker doesnt't have shadow
				return false;
		}
		
		// check shadow step 2/2
		if (this.hasEvergreenGlobal(Evergreen.SHADOW, g)) { // blocker has shadow
			if (!attacker.hasEvergreenGlobal(Evergreen.SHADOW, g)) // attacker doesnt't have shadow
				return false;
		}
		
		// check protection
		if (g.computeIsProtectedFrom(this, attacker))
			return false;
		
		// check continuous effects
		for (ContinuousEffect ce : g.getContinuousEffects()) {
			if (ce.canBlock(this, attacker, g) == false)
				return false;
		}
		
		return true;
	}

//	// Check if the card is the color with its printed values
//	private boolean isColorPrinted(Color color) {
//		return _colors.contains(color);
//	}

//	// Check if the card is the color with the card's own Modifiers (i.e. manlands)
//	private boolean isColorWithModifiers(Color color) {
//	
//		return false;
//	}
//	
//	// Check if the card is the color with effects coming from other cards (i.e. Darkest Hour)
//	public boolean isColorGlobal(Game g, Color color) {
//		for (Modifier mod : _modifiers) {
//			if ((mod instanceof ColorModifier) && (((ColorModifier) mod).getColors().contains(color)))
//				return true;
//		}
//		
//		// Look for continuous effects that change the color
//		for (ContinuousEffect ce : g.getContinuousEffects()) {
//			ce.isColor(g, this, color, ret);
//		}
//
//		return false;
//	}

	/**
	 * Returns the number of colors from the printed values of the card. 
	 * @return
	 */
//	private int getNbPrintedColors() {
//		return _colors.size();
//	}
	
//	public Vector<Color> getPrintedColors() {
//		return _colors;
//	}
	
//	public boolean hasColor(Color col) {
//		return _colors.contains(col);
//	}
	
//	/**
//	 * Returns the number of colors from the card's modifiers
//	 * @return
//	 */
//	private int getNbColorsWithModifiers() {
//		Vector<Color> tmpColors = new Vector<Color>();
//		tmpColors.addAll(_colors);
//		for (Modifier mod : _modifiers) {
//			if (mod instanceof ColorModifier) {
//				for (Color c : ((ColorModifier) mod).getColors()) {
//					if (!tmpColors.contains(c))
//						tmpColors.add(c);
//				}
//			}
//		}
//		return tmpColors.size();
//	}
	
//	// Check if the card is the monocolored (as opposed to multicolor)
//	public boolean isMonocolored() {
//		if ((this.getNbPrintedColors() == 1) && (this.getNbColorsWithModifiers() == 1))
//			return true;
//		return false;
//	}
	
	/**
	 * Checks for the card has Evergreen ability from its own Modifiers
	 * @param ability Evergreen ability to be checked
	 * @return true if the card has the ability, false otherwise
	 */
	private boolean hasEvergreenWithModifiers(Evergreen ability) {
		Vector<Evergreen> abilities;
		
		for (Modifier mod : _modifiers) {
			if (mod instanceof EvergreenModifier) {
				abilities = ((EvergreenModifier) mod).getEvergreens();
				for (Evergreen ab : abilities)
					if (ab == ability)
						return true;
			}
		}
		return false;
	}
	
	public Vector<Evergreen> getPrintedEvergreen() {
		return _evergreenAbilities;
	}
	
	/**
	 * Checks for the card's printed values if it has certain Evergreen ability
	 * @param ability Evergreen ability to be checked
	 * @return true if the card has the ability, false otherwise
	 */
	private boolean hasEvergreenPrinted(Evergreen ability) {
		return _evergreenAbilities.contains(ability);
	}
	
	
	
	/**
	 * Checks if the card has a certain Evergreen ability
	 * @param ability Evergreen ability to be checked
	 * @param g Game instance
	 * @return true if the card has the ability, false otherwise
	 */
	public boolean hasEvergreenGlobal(Evergreen ability, Game g) {
		// effects that strip all abilities (i.e. Soul Sculptor and Humble)
		if (hasContinuousEffect(g, "soulSculpted") || hasContinuousEffect(g, Effect.LOSE_ALL_ABILITIES))
			return false;

		// compute card's printed values
		if (hasEvergreenPrinted(ability))
			return true;

		// compute card's own modifiers
		if (hasEvergreenWithModifiers(ability))
			return true;
		
		// check evergreen granted from continuous effects coming from other cards
		for (ContinuousEffect ce : g.getContinuousEffects()) {
			if (ce.grantsEvergreen(ability, this, g))
				return true;
		}
		return false;
	}

	public void setEvergreen(Evergreen ability) {
		if (!_evergreenAbilities.contains(ability))
			_evergreenAbilities.add(ability);
	}
	
	public void setEvergreen(Vector<Evergreen> abilities) {
		for (Evergreen ability : abilities) {
			if (!_evergreenAbilities.contains(ability))
				_evergreenAbilities.add(ability);
		}
	}
	
	public boolean isBlocked(Game g) {
		if (!isAttacking(g))
			return false;
		if ((_blockers != null) && !_blockers.isEmpty())
			return true;
		return false;
	}
	
	public boolean isBlocking(Game g) {
		if (!isCreature(g))
			return false;
		if ((_blockRecipients != null) && !_blockRecipients.isEmpty())
			return true;
		return false;
	}
	
	public boolean isAttacking(Game g) {
		if (!isCreature(g))
			return false;
		return _bAttacking;
	}
	
	/**
	 * Returns a vector with creature types of this card
	 * @return
	 */
	public Vector<CreatureType> getCreatureTypes() {
		Vector<CreatureType> ct;
		Vector<CreatureType> ret = new Vector<CreatureType>();
		
		// Add printed creature types
		ret.addAll(_creatureTypes);

		// Add creature types granted by Modifiers
		for (Modifier mod : _modifiers) {
			if (mod instanceof CreatureTypeModifier)
			{
				ct = ((CreatureTypeModifier) mod).getCreatureTypes();
				
				// For each creature type contained in this Modifier...
				for (CreatureType t : ct) {
					// add only if creature doesn't have it yet to avoid redundancy
					if (!ret.contains(t))
						ret.add(t);
				}
			}
		}
		return ret;
	}
	
	@Override
	public Card getSource() {
		return _source;
	}

	public boolean canAttackRecipient(Game game, Damageable recipient) {
		// TODO: check if this can attack the recipient (ie. Blazing Archon)
		return true;
	}

	public boolean doesEnterTheBattlefieldTapped(Game g) {
		// Check if card enters the battlefield tapped
		if (hasStaticAbility("ETB_tapped"))
			return true;

		if (hasStaticAbility("tangoland") && (_controller.getNbBasicLandsControlled() < 2))
			return true;

		if (hasStaticAbility("mirroland") && (_controller.getNbOtherLands(this) > 2))
			return true;
		
		if (hasStaticAbility("checkland")) {
			StaticAbility checkland = getStaticAbility("checkland");
			Subtype subtype1 = checkland.getAssociatedBasicLandType(0);
			Subtype subtype2 = checkland.getAssociatedBasicLandType(1);
			
			if (! (_controller.controlsABasicLandType(subtype1) || _controller.controlsABasicLandType(subtype2)))
				return true;
		}
		
		// Look for continuous effects that would make the card enter tapped (i.e. Thalia)
		Vector<ContinuousEffect> ces = g.getContinuousEffects();
		for (ContinuousEffect ce : ces) {
			if (ce.makesCardETBtapped(g, this))
				return true;
		}
		
		return false;
	}

	public Response regenerate() {
		_nbRegenShields++;
		return Response.OK;
	}

	public void requiresXValue() {
		_bXSpell = true;
	}
	
	/* Used for double faced card (such as Innistrad Werewolves) */
	public void setDayFaceCard(String cardname) {
		_dayFaceCardName = cardname;
	}
	
	public String getDayFaceCard() {
		return _dayFaceCardName;
	}
	
	public void setNightFaceCard(String cardname) {
		_nightFaceCardName = cardname;
	}
	
	public String getNightFaceCard() {
		return _nightFaceCardName;
	}

	public boolean hasXValue() {
		return _bXSpell;
	}

	@Override
	public boolean isSorcerySpeed(Game g) {
		if (isInstantCard() || hasEvergreenGlobal(Evergreen.FLASH, g))
			return false;
		return true;
	}

	@Override
	public boolean isTargetableBy(Game g, StackObject targetingObject) {
		// Return false if the target is a permanent with Shroud
		if (this.isOTB(g) && this.hasEvergreenGlobal(Evergreen.SHROUD, g))
			return false;

		// Return false if the target is a permanent with Hexproof and controlled by an opponent
		if (isOTB(g) && hasEvergreenGlobal(Evergreen.HEXPROOF, g) && (targetingObject.getController(g) != _controller))
			return false;
		
		// Return true otherwise (so far cards can be targeted in the graveyard, exile and on the stack)
		return true;
	}
	
	/**
	 * Add a spellcast option that has a parameter and/or an alternate cost 
	 * @param option
	 * @param parameter
	 * @param ac
	 */
	public void addSpellCastOption(SpellCast.Option option, String parameter, AlternateCost ac) {
		_spellCastOptions.add(new SpellCast(this, option, parameter, ac));
	}

	/**
	 * Add a spellcast option that has a parameter but no alternate cost
	 * @param option
	 * @param parameter
	 */
	public void addSpellCastOption(SpellCast.Option option, String parameter) {
		_spellCastOptions.add(new SpellCast(this, option, parameter, null));
	}

	/**
	 * Add a spellcast option that has no parameter and no alternate cost (most common)
	 * @param option
	 */
	public void addSpellCastOption(SpellCast.Option option) {
		_spellCastOptions.add(new SpellCast(this, option, null, null));
	}
 
	
	public void addSpecialAction(SpecialAction.Option option/*, String name*/) {
		_specialActions.add(new SpecialAction(this, option/*, name*/));
	}
	
	public Vector<SpellCast> getSpellCastOptions() {
		return _spellCastOptions;
	}

	public void setSpellCastOptionUsed(SpellCast option) {
		_spellCastOptionUsed = option;
	}
	
	public SpellCast getSpellCastUsed() {
		return _spellCastOptionUsed;
	}

	public SpellCast getPlayModeWithOption(SpellCast.Option option) {
		for (SpellCast pm : _spellCastOptions) {
			if (pm.getOption() == option)
				return pm;
		}
		return null;
	}
	
	/**
	 * Used by cards with MORPH or MEGAMORPH. Creates a 2/2 colorless creature and give it the ability
	 * to turn face up. This ability is a special action (does not use the stack);
	 * @param g
	 * @return
	 */
	public Card createFaceDownVersion(Game g) {
		Card faceDown = CardFactory.create(Card.COLORLESS_MORPH_22 );
		faceDown.setFaceUpCard(this);
		faceDown.setOwner(_owner);
		faceDown.setController(g, _controller);
		faceDown.addSpecialAction(Option.TURN_FACEUP);
		faceDown.setSpellCastOptionUsed(_spellCastOptionUsed);
		return faceDown;
	}
	
	private void setFaceUpCard(Card faceUp) {
		_faceUp = faceUp;
	}
	
	public Card getFaceUp() {
		return _faceUp;
	}

	/**
	 * Returns the other face of the card (the new face that is up)
	 * @param g
	 * @return
	 */
	public Card transform(Game g) {
		// Make sure the card is on the battlefield
		if (!this.isOTB(g))
			return null;
		
		// Make sure the card is a double faced card
		if ((_nightFaceCardName == null) && (_dayFaceCardName == null))
			return null;
		
		Card otherFace;
		
		// Case 1 : Switch from recto to verso
		if (_nightFaceCardName != null) {
			otherFace = CardFactory.create(_nightFaceCardName);
			//otherFace._printedConvertedManaCost = _printedConvertedManaCost;
		}
		else  // Case 2 : Switch from verso to recto
			otherFace = CardFactory.create(_dayFaceCardName);

		turnFace(g, otherFace);
		
		/* Remove all continuous effect that were generated by previous face */
		Vector<ContinuousEffect> effects = g.getContinuousEffects();
		int i = 0;
		while (i < effects.size()) {
			if (effects.get(i).getSource() == this) {
				effects.remove(i);
				i--;
			}
			i++;
		}
		
		/* Queue triggered abilities that trigger when the Card transforms to the new face (i.e. Avacyn, the Purifier) */
		g.queueTriggeredAbilities(otherFace.getTriggeredAbilities(g, Event.TransformsInto, Origin.BTLFLD), null);
		return otherFace;
	}
	
	@SuppressWarnings("unchecked")
	public Response turnFace(Game g, Card otherFace) {
		otherFace.setOwner(_owner);
		otherFace.setController(g, _controller);
		otherFace._bTapped = _bTapped;
		otherFace._damage = _damage;
		otherFace._bSummoningSick = _bSummoningSick;
		otherFace._bHasAlreadyActivatedLoyaltyAbility = _bHasAlreadyActivatedLoyaltyAbility;
		otherFace._permanentIamAttachedTo = _permanentIamAttachedTo;
		otherFace._permanentsAttachedToMe = _permanentsAttachedToMe;
		for (Card aura : _permanentsAttachedToMe)
			aura.setHost(otherFace);
		
		if (hasCounters())
			otherFace.setCounters((HashMap<CounterType, Integer>) _counters.clone());

		Zone zone = getZone(g);
		
		zone.addObject(otherFace, zone.indexOf(this));
		zone.removeObject(this);

		if (isBlocked(g)) {
			for (Card blocker : this.getBlockers())
				blocker.blocks(otherFace);
		}
		if (isBlocking(g)) {
			for (Card attacker : this.getBlockedCreatures()) {
				attacker.getBlockers().remove(this);
				otherFace.blocks(attacker);
			}
		}
		g.switchCards(this, otherFace);
		
		// Add continuous effects generated by the face up
		if (otherFace.hasContinuousEffects()) {
			ContinuousEffect ce;
			for (int i = 0; i < otherFace.getContinuousEffects(Zone.Name.Battlefield).size(); i++) {
				ce = otherFace.getContinuousEffects(Zone.Name.Battlefield).get(i);
				g.addContinuousEffect(ce);
			}
		}
		
		// queue triggered abilities of type "When this is turned face up ..."
		g.queueTriggeredAbilities(otherFace.getTriggeredAbilities(g, Event.IsTurnedFaceUp, Origin.BTLFLD), otherFace);
		return Response.OK;
	}

	public void clearSpellCastOptionUsed() {
		_spellCastOptionUsed = null;
	}
	
	public void addReference(Vector<Card> obj) {
		if (!_references.contains(obj))
			_references.add(obj);
	}
	
	public void removeReferences() {
		while (_references.size() > 0) {
			_references.get(0).remove(this);
			_references.remove(0);
		}
	}
	
	public void removeReference(Vector<Card> obj) {
		_references.remove(obj);
	}
	
	public String toString() {
		return _name;
	}
	
	public boolean hasRegenShield() {
		return _nbRegenShields > 0;
	}
	
	public void consumeRegenShield() {
		if (_nbRegenShields > 0)
			_nbRegenShields--;
	}

	@Override
	public int isDealtDamage(DamageSource source, int damageDealt, Game g) {
		Card sourceCard;
		
		// check card is on the battlefield
		if (!this.isOTB(g))
			return 0;
	
		// subtract damage prevented
		int actualDamage = Math.max(damageDealt - _damagePrevention, 0);

		// subtract the amount of damage to the current prevention
		_damagePrevention = Math.max(_damagePrevention - damageDealt, 0);
		
		// 1.a. recipient is a creature
		if (this.isCreature(g)) {
			if (source instanceof Card) {
				sourceCard = (Card) source;
				if (sourceCard.hasEvergreenGlobal(Evergreen.INFECT, g))
					this.addCounter(g, CounterType.MINUS_ONE, actualDamage);
				else
					this.addDamage(actualDamage);
				
				// mark deathtouch damage
				if ((actualDamage > 0) && sourceCard.hasEvergreenGlobal(Evergreen.DEATHTOUCH, g)) {
					this.markDeathtouch();
				}
			}
			
		}
		// 1.b. recipient is a planeswalker
		if (this.isPlaneswalkerCard())
			this.removeCounter(g, CounterType.LOYALTY, actualDamage);
		
		// trigger effects that trigger when the permanent is dealt damage (i.e. Jackal Pup)
		if (actualDamage > 0) {
			// the additional data is a Vector with 2 elements :
			//   element 1 is the amount of damage
			//   element 2 is the damage source
			Vector<Object> additionalData = new Vector<Object>();
			additionalData.add(actualDamage);
			additionalData.add(source);
			g.queueDamageDealtTriggeredAbilities(this.getTriggeredAbilities(g, Event.IsDealtDamage, Origin.BTLFLD), additionalData);
		}
		if (!_whoDamagedMeThisTurn.contains(source))
			_whoDamagedMeThisTurn.addElement(source);
		return actualDamage;
	}
	
	public boolean wasDamagedThisTurnBy(Card c) {
		return _whoDamagedMeThisTurn.contains(c);
	}
	
	public void addDamagePrevention(int nb) {
		_damagePrevention  += nb;
	}
	
	public ManaCost getManaCost() {
		return _cost.getManaCost();
	}
	
	public boolean hasContinuousEffect(Game g, String effectName) {
		String abilityName;
		// Game continuous effects	
		for (ContinuousEffect ce : g.getContinuousEffects()) {
			if (ce.grantsContinuousEffect(g, effectName, this))
				return true;
		}
		
		// Card (printed) continuous effects	
		for (ContinuousEffect ce : _continuousEffects) {
			abilityName = ce.getAbilityName();
			if (abilityName.equals(effectName))
				return true;
		}
		return false;
	}
	
	/**
	 * Returns true if the card has the specified Static Ability, false otherwise
	 * @param abilityName
	 * @return
	 */
	public boolean hasStaticAbility(String abilityName) {
		for (StaticAbility sa : _staticAbilities) {
			if (sa.getAbilityName().equals(abilityName))
				return true;
		}
		return false;
	}
	
	/**
	 * Copies an instant or sorcery spell.
	 * @param source The source effect that created the copy.
	 * @return
	 */
	public Card copy(Game g, StackObject source) {
		Card copy = CardFactory.create(_name);
		
		copy._spellCastOptionUsed = this._spellCastOptionUsed;
		copy._bCopy = true;
		copy._controller = source.getController(g);
		copy._owner = copy._controller;
		copy._originalCard = this;
		return copy;
	}
	
	public boolean isCopy() {
		return _bCopy;
	}

	public boolean hasName() {
		return _name != null;
	}

	public void clearBlockedCreatures() {
		if (_blockRecipients != null) {
			for (Card blockedCreature : _blockRecipients) {
				blockedCreature.getBlockers().remove(this);
			}
			_blockRecipients = null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public void removeAllCounters(Game g) {
		if (_counters != null) {
			_lastKnownCounters = (HashMap<CounterType, Integer>) _counters.clone();
			for (CounterType key : _counters.keySet()) {
				removeCounter(g, key, _counters.get(key));
			}	
		}
	}

	public void becomesCopyOf(Game g, Card target) {
		Card copy = CardFactory.create(target.getName());
		copy._owner = _owner;
		copy._controller = _controller;
		copy._originalCard = this;
		copy._bCopy = true;
		g.getBattlefield().addObject(copy);
		g.getBattlefield().removeObject(this);
	}

	public Card getOriginal() {
		return _originalCard;
	}
	
	public void preventCombatDamageDealtTo() {
		_b_preventCombatDamageDealtTo = true;
	}
	
	public void preventCombatDamageDealtBy() {
		_b_preventCombatDamageDealtBy = true;
	}

	public int getLinkedCardInternalId(Card linkedCard) {
		if (_linkedCards.containsKey(linkedCard))
			return _linkedCards.get(linkedCard);
		return -1;
	}
	
	public Vector<Card> getLinkedCards() {
		Vector<Card> ret = new Vector<Card>();
		for (Card card : _linkedCards.keySet())
			ret.add(card);
		return ret;
	}
	
	public void addLinkedCard(Card card) {
		_linkedCards.put(card, card.getInternalObjectId());
	}

	public SpellAbility getSpellEffect() {
		return _spellEffect;
	}

	public Vector<ContinuousEffect> getPrintedContinuousEffects() {
		return _continuousEffects;
	}

	public Vector<Object> getPrintedProtections() {
		return _protections;
	}

	public void setInternalObjectId(int internalObjectId) {
		_internalObjectId = internalObjectId;
	}

	public int getInternalObjectId() {
		return _internalObjectId;
	}
	
	public StaticAbility getStaticAbility(String abilityName) {
		for (StaticAbility sa : _staticAbilities) {
			if (sa.getAbilityName().equals(abilityName))
				return sa;
		}
		return null;
	}

	public boolean hasNightFace() {
		if (_nightFaceCardName != null)
			return true;
		return false;
	}
	
	public boolean hasDayFace() {
		if (_dayFaceCardName != null)
			return true;
		return false;
	}

	@Override
	public boolean isUndamageable(Game g) {
		return this.hasEvergreenGlobal(Evergreen.UNDAMAGEABLE, g);
	}
	
	@Override
	public void setController(Game g, Player controller) {
		if (this.isCreature(g)) {
			if (controller != _controller) {
				_bSummoningSick = true;
				_bMustPayEcho = true;				
			}
		}
		super.setController(g, controller);
	}

	public boolean isUntapOptional() {
		return (_untapOptional != null);
	}
	
	public void setUntapOptional() {
		_untapOptional = new UntapOptional();
	}
	
	public boolean isUntapOptionalAnswered() {
		return _untapOptional.isAnswered();
	}
	
	public UntapOptional.State getUntapOptionalChoice() {
		return _untapOptional.getChosenState();
	}
	
	public void setUntapOptionalState(UntapOptional.State answer) {
		_untapOptional.setOptionalUntapState(answer);
	}

	/**
	 * Checks if declaring this creature as an attacker is legal in the given context.
	 * @param game
	 * @return
	 */
	public boolean isAttackDeclarationLegal(Game g) {
		// Look for continuous effects like Okk
		Vector<ContinuousEffect> effects = g.getContinuousEffects();
		
		for (ContinuousEffect ce : effects) {
			if (!ce.isAttackLegal(this, g))
				return false;
		}
		return true;
	}
	
	/**
	 * Checks if declaring this creature as a blocker is legal in the given context.
	 * @param game
	 * @return
	 */
	public boolean isBlockerDeclarationLegal(Game g) {
		// Look for continuous effects like Okk
		Vector<ContinuousEffect> effects = g.getContinuousEffects();
		
		for (ContinuousEffect ce : effects) {
			if (!ce.isBlockLegal_blocker(this, g))
				return false;
		}
		return true;
	}
	
	/**
	 * This creature is attacking. Check that blocks declared for it are legal. (i.e. Phyrexian Colossus)
	 * @param game
	 * @return
	 */
	public boolean isBlockedDeclarationLegal(Game g) {
		// Menace
		if (hasEvergreenGlobal(Evergreen.MENACE, g) && (_blockers != null) && (_blockers.size() == 1))
			return false;
		
		// Look for continuous effects
		Vector<ContinuousEffect> effects = g.getContinuousEffects();
		
		for (ContinuousEffect ce : effects) {
			if (!ce.isBlockLegal_attacker(this, g))
				return false;
		}
		return true;
	}

	public void clearLinkedCards() {
		_linkedCards.clear();
	}
	
	@Override
	public Vector<Object> getProtections(Game g) {
		if (hasContinuousEffect(g, "soulSculpted") || hasContinuousEffect(g, Effect.LOSE_ALL_ABILITIES))
			return new Vector<Object>();
		return super.getProtections(g);
	}

	/**
	 * Used for cards that make a blocked creature deal damage to its attacking recipient as though it weren't blocked.
	 */
	public void ignoreBlocker() {
		_bIgnoreBlocker  = true;
	}

	public boolean doesIgnoreBlocker() {
		return _bIgnoreBlocker;
	}

	public Vector<StaticAbility> getStaticAbilities() {
		return _staticAbilities;
	}

	public void setChosenColor(Color color) {
		this._chosenColor = color;
	}
	
	public Color getChosenColor() {
		return _chosenColor;
	}

	public Response dealNonCombatDamageTo(Game g, Damageable targetObject, int i) {
		return this.dealDamageTo(g, targetObject, i, false);
	}

	public Vector<SpecialAction> getSpecialActions() {
		return _specialActions;
	}

	private Vector<Color> getColors() {
		Vector<Color> ret = new Vector<>();
		
		// Cards with Devoid have no color.
		if (hasStaticAbility(StaticAbility.DEVOID))
			return ret;
		
		ManaCost myManaCost = this.getManaCost();
		
		if (myManaCost != null) {
			if (myManaCost.contains(Symbol.White) ||
					myManaCost.contains(Symbol.Hybrid_W) || myManaCost.contains(Symbol.Phyrexian_W) ||
					myManaCost.contains(Symbol.Hybrid_gw) || myManaCost.contains(Symbol.Hybrid_rw) || myManaCost.contains(Symbol.Hybrid_wu) || myManaCost.contains(Symbol.Hybrid_wb))
				ret.add(Color.WHITE);

			if (myManaCost.contains(Symbol.Blue) ||
					myManaCost.contains(Symbol.Hybrid_U) || myManaCost.contains(Symbol.Phyrexian_U) ||
					myManaCost.contains(Symbol.Hybrid_wu) || myManaCost.contains(Symbol.Hybrid_ub) || myManaCost.contains(Symbol.Hybrid_ur) || myManaCost.contains(Symbol.Hybrid_gu))
				ret.add(Color.BLUE);
			
			if (myManaCost.contains(Symbol.Black) ||
					myManaCost.contains(Symbol.Hybrid_B) || myManaCost.contains(Symbol.Phyrexian_B) ||
					myManaCost.contains(Symbol.Hybrid_bg) || myManaCost.contains(Symbol.Hybrid_br) || myManaCost.contains(Symbol.Hybrid_ub) || myManaCost.contains(Symbol.Hybrid_wb))
				ret.add(Color.BLACK);
			
			if (myManaCost.contains(Symbol.Red) ||
					myManaCost.contains(Symbol.Hybrid_R) || myManaCost.contains(Symbol.Phyrexian_R) ||
					myManaCost.contains(Symbol.Hybrid_rw) || myManaCost.contains(Symbol.Hybrid_ur) || myManaCost.contains(Symbol.Hybrid_br) || myManaCost.contains(Symbol.Hybrid_rg))
				ret.add(Color.RED);

			if (myManaCost.contains(Symbol.Green) ||
					myManaCost.contains(Symbol.Hybrid_G) || myManaCost.contains(Symbol.Phyrexian_G) ||
					myManaCost.contains(Symbol.Hybrid_gw) || myManaCost.contains(Symbol.Hybrid_gu) || myManaCost.contains(Symbol.Hybrid_bg) || myManaCost.contains(Symbol.Hybrid_rg))
				ret.add(Color.GREEN);
		}
		else {
			ret.addAll(_colorIndicators);
		}
		return ret;
	}
	
	public boolean hasColor(Color color) {
		return this.getColors().contains(color);
	}
	
	public boolean isColored() {
		return (this.getColors().size() > 0);
	}

	public boolean isMonocolored() {
		return (this.getColors().size() == 1);
	}

	public void setColorIndicators(Vector<Color> colorIndicators) {
		_colorIndicators = colorIndicators;
	}
	
	public boolean hasColorIndicator() {
		return !_colorIndicators.isEmpty();
	}
};
