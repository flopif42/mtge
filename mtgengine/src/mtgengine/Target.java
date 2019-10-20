package mtgengine;

import mtgengine.ability.ActivatedAbility;
import mtgengine.ability.Evergreen;
import mtgengine.ability.TriggeredAbility;
import mtgengine.card.Card;
import mtgengine.card.Color;
import mtgengine.player.Player;
import mtgengine.type.CreatureType;
import mtgengine.type.Subtype;
import mtgengine.type.Supertype;

public class Target {
	
	public enum Timing { ASSIGNMENT, RESOLUTION }
	
	public enum Category {
		// Players
		Player,
		Opponent,

		// Lands
		Land,
		LandYouControl,
		NonbasicLand,
		Forest,
		Swamp,
		
		// Creatures
		Creature, 
		AnotherCreature,
		LegendaryCreature,
		ArtifactCreature,
		CreatureYouControl,
		CreatureWithPower4orGreater,
		NonAngelCreatureYouControl,        // Restoration Angel
		CreatureAnOpponentControls,
		WhiteCreature,
		GreenCreature,
		NonBlackCreature,
		NonArtifactNonBlackCreature,
		NontokenCreature,
		TokenCreature,
		MonocoloredCreature,
		BlinkmothCreature,
		AttackingCreature,
		BlockedCreature,
		AttackingOrBlockingCreature,
		AttackingCreatureWithFlying,
		AttackingCreatureWithoutFlying,
		
		// Other permanents
		Permanent,
		PermanentYouControl,               // Donate
		Artifact,
		Enchantment,
		Planeswalker,
		AnotherPermanent,
		AnotherPermanentYouControl,			// Felidar Guardian
		NonlandPermanent,
		NonCreatureArtifact,
		ArtifactOrEnchantment,
		ArtifactOrEnchantmentOrLand,        // Hoodwink
		AuraAttachedToAcreatureOrLand,      // Enchantment Alteration 
		NonlandPermanentWithCMC3OrLess, 	// Abrupt Decay
		EnchantmentTappedArtifactOrTappedCreature,			// Nahiri, the Harbinger	
		ArtifactOrCreatureOrLand,
        CreatureOrPlaneswalker,
        LandOrNonBlackCreature,           // Befoul
		
		// Stack
		Spell,
		CreatureSpell, NonCreatureSpell,
		SpellWithCMC4orLess,
		ArtifactOrEnchantmentSpell,
		RedSpell,
		ActivatedOrTriggeredAbility,
        ActivatedAbility,
        SpellOrActivatedOrTriggeredAbility,
        InstantOrSorcery,
        
        // Cards in your graveyard
		CardInYourGraveyard,
		LandCardInYourGraveyard,
		EnchantmentCardInYourGraveyard,
		CreatureCardInYourGraveyard,
		InstantCardInYourGraveyard,							// Torrential Gearhulk
		NonLgdryCreaCardInYourGydWithCMCX,					// Liliana, Defiant Necromancer
		PermanentCardWithCdMC3OrLessInYourGraveyard,		// Sun Titan
		PermanentCardInYourGraveyard,		                // Nissa, Vital Force
		
		// Cards in any graveyard
		CardInAnyGraveyard,
		LandCardInAnyGraveyard,
		CreatureCardInAnyGraveyard,
		SorceryOrInstantCardInAnyGraveyard,
		CreatureOrPlaneswalkerCardinAnyGraveyard,
		
		// Other
		AnyTarget,   // This is the same as : target creature, player or planeswalker
		SpellOrPermanent,
		PlayerOrPlaneswalker
    };
	
	private Category _category;
	private MtgObject _object = null;
	private int _internalObjectId;

	public Target(Category targetType) {
		_category = targetType;
	}
	
	public Category getType() {
		return _category;
	}

	public void setObject(MtgObject obj) {
		_object = obj;
		if ((obj != null) && (obj.getClass() == Card.class)) {
			Card card = (Card) obj;
			_internalObjectId = card.getInternalObjectId();
		}
	}
	
	public MtgObject getObject() {
		return _object;
	}

	public void reset() {
		_object = null;
	}
	
	public boolean isAssigned() {
		return (_object != null);
	}
	
	public boolean isValid(Game g, StackObject targetingObject) {
		if ((_object != null) && (_object.getClass() == Card.class)) {
			Card card = (Card) _object;
			if (card.getInternalObjectId() != _internalObjectId)
				return false;
		}
		return Target.validate(g, _category, _object, targetingObject, Timing.RESOLUTION);
	}
	
	/**
	 * 
	 * @param g
	 * @param category
	 * @param object
	 * @return
	 */
	public static boolean validate(Game g, Category category, MtgObject object, StackObject targetingObject, Timing timing) {
		if (object == null)
			return false;

		if (!object.isTargetableBy(g, targetingObject))
			return false;
		
		if (g.computeIsProtectedFrom(g.getTopStackObject().getSource(), object))
			return false;
		
		Card card;
		
		switch (category) {
		case Artifact:
			if (!((Card)object).isArtifact(g) || !((Card)object).isOTB(g))
				return false;
			break;
			
		case NonCreatureArtifact:
			card = (Card) object;
			if (!(card.isArtifact(g) && !card.isCreature(g)))
				return false;
			break;

		case CreatureCardInYourGraveyard:
			card = (Card) object;
			if (!card.isCreatureCard() || !card.isIGY(g) || (card.getController(g) != g.getTopStackObject().getController(g))) 
				return false;
			break;
			
		case InstantCardInYourGraveyard:
			card = (Card) object;
			if (!card.isInstantCard() || !card.isIGY(g) || (card.getController(g) != g.getTopStackObject().getController(g))) 
				return false;
			break;
			
		case LandCardInYourGraveyard:
			card = (Card) object;
			if (!card.isLandCard() || !card.isIGY(g) || (card.getController(g) != g.getTopStackObject().getController(g))) 
				return false;
			break;

		case EnchantmentCardInYourGraveyard:
			card = (Card) object;
			if (!card.isEnchantmentCard() || !card.isIGY(g) || (card.getController(g) != g.getTopStackObject().getController(g))) 
				return false;
			break;
			
		case NonLgdryCreaCardInYourGydWithCMCX:
			int xValue = g.getTopStackObject().getXValue();
			card = (Card) object;
			if ((card.getConvertedManaCost(g) != xValue) || card.hasSupertype(Supertype.LEGENDARY) || !card.isCreatureCard() || !card.isIGY(g) || (card.getController(g) != g.getTopStackObject().getController(g)))
				return false;
			break;
			
		case CardInYourGraveyard:
			if (!((Card) object).isIGY(g) || (((Card) object).getOwner() != g.getTopStackObject().getController(g))) 
				return false;
			break;
			
		case CardInAnyGraveyard:
			if (((Card)object).isIGY(g) == false)
				return false;
			break;
			
		case CreatureYouControl:
			if (!((Card) object).isCreature(g) || !((Card) object).isOTB(g) || (((Card) object).getController(g) != g.getTopStackObject().getController(g)))
				return false;
			break;

		case NonAngelCreatureYouControl:
			card = (Card) object;
			if (!card.isCreature(g) ||                                                 // creature
					card.hasCreatureType(g, CreatureType.Angel) ||                    // non-Angel
					!card.isOTB(g) ||                                                 // on the battlefield
					(card.getController(g) != g.getTopStackObject().getController(g)))  // you control
				return false;
			break;

		case AnotherPermanentYouControl:
			card = (Card) object;
			if (!card.isPermanentCard() ||                                                 // permanent
				!card.isOTB(g) ||                                                 // on the battlefield
				(card.getController(g) != g.getTopStackObject().getController(g)))  // you control
				return false;
		
			if (object == targetingObject.getSource()) // another
				return false;
			
			break;
			
		case PermanentYouControl:
				card = (Card) object;
				if (!card.isPermanentCard() ||                                                 // permanent
					!card.isOTB(g) ||                                                 // on the battlefield
					(card.getController(g) != g.getTopStackObject().getController(g)))  // you control
					return false;
				
				break;
			
		case CreatureAnOpponentControls:
			if (!((Card) object).isCreature(g) || !((Card) object).isOTB(g) || (((Card) object).getController(g) == g.getTopStackObject().getController(g)))
				return false;
			break;
			
		case Creature:
			if (!((Card)object).isCreature(g) || !((Card)object).isOTB(g))
				return false;
			break;

		case CreatureWithPower4orGreater:
			card = (Card) object;
			if (!(card.isOTB(g) && card.isCreature(g) && (card.getPower(g) >= 4)))
				return false;
			break;
			
		case NontokenCreature:
			card = (Card) object;
			if (!(card.isOTB(g) && card.isCreature(g) && !card.isToken()))
				return false;
			break;
			
		case TokenCreature:
			card = (Card) object;
			if (!(card.isCreature(g) && card.isOTB(g) && card.isToken()))
				return false;
			break;
			
		case GreenCreature:
			card = (Card) object;
			if (!(card.isOTB(g) && card.isCreature(g) && card.hasColor(Color.GREEN)))
				return false;
			break;

		case WhiteCreature:
			card = (Card) object;
			if (!(card.isOTB(g) && card.isCreature(g) && card.hasColor(Color.WHITE)))
				return false;
			break;
			
		case AttackingCreature:
			card = (Card) object;
			if (!(card.isCreature(g) && card.isOTB(g) && card.isAttacking(g)))			
				return false;
			break;
			
		case AttackingCreatureWithFlying:
			card = (Card) object;
			if (!(card.isCreature(g) && card.isOTB(g) && card.isAttacking(g) && card.hasEvergreenGlobal(Evergreen.FLYING, g)))			
				return false;
			break;
			
		case AttackingCreatureWithoutFlying:
			card = (Card) object;
			if (!(card.isCreature(g) && card.isOTB(g) && card.isAttacking(g) && !card.hasEvergreenGlobal(Evergreen.FLYING, g)))			
				return false;
			break;
			
		case LegendaryCreature:
			card = (Card) object;
			if (!(card.isCreature(g) && card.isOTB(g) && card.hasSupertype(Supertype.LEGENDARY)))
				return false;
			break;

		case ArtifactCreature:
			card = (Card) object;
			if (!(card.isCreature(g) && card.isOTB(g) && card.isArtifact(g)))
				return false;
			break;
			
		case AnotherCreature:
			if (!((Card)object).isCreature(g) || !((Card)object).isOTB(g))
				return false;
			
			if (object == targetingObject.getSource())
				return false;
			break;
			
		case NonArtifactNonBlackCreature:
			card = (Card)object;
			if (!card.isCreature(g) || !card.isOTB(g) || card.isArtifact(g) || card.hasColor(Color.BLACK))
				return false;
			break;
			
		case LandOrNonBlackCreature:
			card = (Card)object;
			if (!card.isOTB(g))
				return false;
			if (!(card.isCreature(g) || card.isLand(g)))
				return false;
			if (card.isCreature(g) && card.hasColor(Color.BLACK))
				return false;
			break;
			
		case NonBlackCreature:
			card = (Card)object;
			if (!card.isCreature(g) || !card.isOTB(g) || card.hasColor(Color.BLACK))
				return false;
			break;
			
		case MonocoloredCreature:
			card = (Card)object;
			if (!card.isCreature(g) || !card.isOTB(g) || !card.isMonocolored())
				return false;
			break;
			
		case Planeswalker:
			if (!((Card)object).isPlaneswalkerCard() || !((Card)object).isOTB(g))
				return false;
			break;
			
		case CreatureCardInAnyGraveyard:
			if (!((Card)object).isCreatureCard() || !((Card)object).isIGY(g))
				return false;
			break;
			
		case AnyTarget:
			// Is it a player ?
			if (object instanceof Player)
				return true;
			
			// In case of a card
			if (object instanceof Card) {
				Card target = (Card) object;
				
				// Is it on the battlefield ? // Is it a creature or a PW ?
				if (target.isOTB(g) && (target.isCreature(g) || target.isPlaneswalkerCard()))
					return true;
				else
					return false;
			}
			return false;
		
		case CreatureOrPlaneswalker:
			if (object.getClass() != Card.class)
				return false;
			card = (Card) object;
			
			// Not correct type
			if ((card.isCreature(g) || card.isPlaneswalkerCard()) == false)
				return false;
			
			// Not on the battle field
			if (!card.isOTB(g))
				return false;
			break;
			
		case SpellOrPermanent:
			break;
			
		case ArtifactOrEnchantmentSpell:
			card = (Card) object;
			if (!(card.isArtifactCard() || card.isEnchantmentCard()))
				return false;
			break;
			
		case CreatureSpell:
			if (!((Card)object).isCreatureCard() || !((Card)object).isOTS(g))
				return false;
			break;
			
		case NonCreatureSpell:
			card = (Card) object;
			
			if (!(card.isOTS(g) && !card.isCreature(g)))
				return false;
			break;

		case RedSpell:
			card = (Card) object;
			if (!(card.isOTS(g) && card.hasColor(Color.RED)))
				return false;
			break;
			
		case Enchantment:
			if (!((Card)object).isEnchantmentCard() || !((Card)object).isOTB(g))
				return false;
			break;
			
		case AuraAttachedToAcreatureOrLand:
			card = (Card) object;
			if (!card.hasSubtypeGlobal(g, Subtype.AURA))
				return false;
			
			Card host = card.getHost();
			if (!(host.isCreature(g) || host.isLand(g)))
				return false;
			break;
			
		case EnchantmentTappedArtifactOrTappedCreature:
			Card target = (Card)object;
			if (!target.isOTB(g))
				return false;
			
			if (!target.isEnchantmentCard() &&
					!(target.isArtifact(g) && target.isTapped()) &&
					!(target.isCreature(g) && target.isTapped()))
				return false;
			break;
			
		case ArtifactOrCreatureOrLand:
			target = (Card)object;
			if (!target.isOTB(g) || !(target.isArtifact(g) || target.isCreature(g) || target.isLand(g)))
				return false;
			break;
			
		case ArtifactOrEnchantmentOrLand:
			target = (Card)object;
			if (!target.isOTB(g) || !(target.isArtifact(g) || target.isEnchantment(g) || target.isLand(g)))
				return false;
			break;
			
		case ArtifactOrEnchantment:
			target = (Card)object;
			if (!(target.isEnchantmentCard() || target.isArtifact(g)) || !target.isOTB(g))
				return false;
			break;
			
		case Land:
			if (!((Card)object).isLand(g) || !((Card)object).isOTB(g))
				return false;
			break;
			
		case Forest:
		case Swamp:
		{
			Subtype st;
			
			switch (category) {
			case Forest:
				st = Subtype.FOREST;
				break;
				
			case Swamp:
				st = Subtype.SWAMP;
				break;
				
			default:
				return false;
			}
			
			Card c = (Card) object;
			if (!(c.isLand(g) && c.isOTB(g) && c.hasSubtypeGlobal(g, st)))
				return false;
			break;
		}
		
		case NonbasicLand:
			card = (Card) object; 
			if (!card.isLand(g) || !card.isOTB(g) || card.hasSupertype(Supertype.BASIC))
				return false;
			break;
			
		case LandCardInAnyGraveyard:
			if (!((Card)object).isLandCard() || !((Card)object).isIGY(g))
				return false;
			break;
			
		case NonlandPermanent:
			if (!((Card)object).isOTB(g))
				return false;
			
			if (((Card)object).isLand(g))
				return false;
			break;
			
		case NonlandPermanentWithCMC3OrLess:
			if (!((Card)object).isOTB(g))
				return false;
			
			if (((Card)object).isLand(g))
				return false;
			
			if (((Card)object).getConvertedManaCost(g) > 3)
				return false;
			break;
			
		case Permanent:
			if (!((Card)object).isOTB(g))
				return false;
			break;

			
			
		case AnotherPermanent:
			if (!((Card)object).isOTB(g))
				return false;
			
			if (object == targetingObject.getSource())
				return false;
			break;
			
		case Opponent:
			if (!(object.getClass() == Player.class))
				return false;
			if (targetingObject.getController(g) == object)
				return false;
			break;
			
		case Player:
			if (!(object.getClass() == Player.class))
				return false;
			break;
			
		case PlayerOrPlaneswalker:
			if (object instanceof Player)
				return true;
			if (object instanceof Card) {
				target = (Card) object;
				if (target.isOTB(g) && target.isPlaneswalkerCard())
					return true;
			}
			return false;
			
		case SorceryOrInstantCardInAnyGraveyard:
			if (!((Card)object).isIGY(g))
				return false;
			if (!((Card)object).isSorceryCard() && !((Card)object).isInstantCard())
				return false;
			break;
			
		case CreatureOrPlaneswalkerCardinAnyGraveyard:
			if (!(object instanceof Card))
				return false;
			Card c = (Card) object;
			if (!c.isIGY(g))
				return false;
			if (!(c.isCreatureCard() || c.isPlaneswalkerCard()))
				return false;
			break;
		
		case InstantOrSorcery:
			if (object.getClass() != Card.class) // object is not a Card
				return false;
			
			card = (Card)object;
			if (!card.isOTS(g)) // object is not on the stack
				return false;
			
			if (!(card.isSorceryCard() || card.isInstantCard()))
				return false;
			break;
		
		case Spell:
			if (object.getClass() != Card.class) // object is not a Card
				return false;

			if (timing == Timing.ASSIGNMENT) {
				if (((Card)object).isOTS(g) == false) // object is not on the stack
					return false;	
			}
			break;

		case SpellWithCMC4orLess:
			if (object.getClass() != Card.class) // object is not a Card
				return false;

			card = (Card)object;

			if (!card.isOTS(g)) // object is not on the stack
				return false;

			if (card.getConvertedManaCost(g) > 4) // Spell has a converted mana cost > 4
				return false;
			break;

		case ActivatedAbility:
			if (!(object instanceof ActivatedAbility)) // object is not an activated ability
				return false;
			break;
			
		case ActivatedOrTriggeredAbility:
			if (!((object instanceof ActivatedAbility) || (object instanceof TriggeredAbility))) // object is not an activated or triggered ability
				return false;
			break;

		case SpellOrActivatedOrTriggeredAbility:
			if (!((object instanceof ActivatedAbility) || (object instanceof TriggeredAbility) || ((object instanceof Card) && (((Card)object).isOTS(g) == true))))// object is not an activated or triggered ability
				return false;
			break;
			
		case PermanentCardInYourGraveyard:
			if (object.getClass() != Card.class) // object is not a Card
				return false;
			card = (Card)object;
			
			if (!(card.isPermanentCard() && card.isIGY(g)))
				return false;
			break;
			
		default:
			break;
		}
		return true;
	}
}
