package mtgengine.effect;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mtgengine.CounterType;
import mtgengine.Game;
import mtgengine.Game.Response;
import mtgengine.MtgObject;
import mtgengine.StackObject;
import mtgengine.ability.ActivatedAbility;
import mtgengine.ability.ActivatedAbilityFactory;
import mtgengine.ability.Evergreen;
import mtgengine.ability.TriggeredAbility;
import mtgengine.ability.TriggeredAbilityFactory;
import mtgengine.ability.TriggeredAbility.Event;
import mtgengine.ability.TriggeredAbility.Origin;
import mtgengine.action.SpellCast.Option;
import mtgengine.card.Card;
import mtgengine.card.Color;
import mtgengine.card.Token;
import mtgengine.damage.DamageSource;
import mtgengine.damage.Damageable;
import mtgengine.player.Player;
import mtgengine.player.PlayerBuff;
import mtgengine.type.CardType;
import mtgengine.type.CreatureType;
import mtgengine.type.Subtype;
import mtgengine.type.Supertype;
import mtgengine.zone.Zone;

public class ContinuousEffect {
	public enum StopWhen { END_OF_TURN, SOURCE_UNTAPS, TARGET_UNTAP }
	
	private ContinuousEffectSource _source;
	private String _effectText;
	private String _effectName;
	private Zone.Name _effectZone;
	private Object _additionalData = null;
	private Vector<TriggeredAbility> _triggeredAbilities = null;
	private StopWhen _bStopWhen;
		
	public ContinuousEffect(String name, ContinuousEffectSource source) {
		_effectName = name;
		_source = source;
		_effectZone = Zone.Name.Battlefield;
	}
	
	public String getAbilityName() {
		return _effectName;
	}
	
	/* Effect description (rules text) */
	public void setDescription(String desc) {
		_effectText = desc;
	}

	public String getDescription() {
		return _effectText;
	}
	
	/* Effect active zone : zone in which the effect will be enabled */
	public void setActiveZone(Zone.Name zone) {
		_effectZone = zone;
	}
	
	public Zone.Name getActiveZone() {
		return _effectZone;
	}

	/* Additional data related to the trigger of the ability (i.e. for Fecundity, the creature that died) */
	public void setAdditionalData(Object data) {
		_additionalData = data;
	}
	
	public Object getAdditionalData() {
		return _additionalData;
	}

	/* The source card that generates the continuous effect */
	public ContinuousEffectSource getSource() {
		return _source;
	}
	
	/* The effect's controller */
	public Player getController(Game g) {
		return _source.getController(g);
	}
	
	public String toString() {
		return _source.getSystemName() + " : " + _effectText;
	}

	/* Effects that make a player skip his draw step */
	public boolean skipDrawStep(Game g, Player player) {
		/* skipDrawStep */
		if (_effectName.equals("skipDrawStep") && (player == _source.getController(g)))
			return true;
		return false;
	}
	
	/* Effects that modify the number of counters to be put on a permanent or player */
	public int modifyNbCounters(Game g, MtgObject recipient, CounterType ct, int originalNumber) {
		/* windingConstrictor_1 : put one more counter than originalNumber on creature and/or artifacts you control*/
		if (_effectName.equals("windingConstrictor_1") && (recipient instanceof Card)) {
			Card permanent = (Card) recipient;
			if ((permanent.isCreature(g) || permanent.isArtifact(g)) && (permanent.getController(g) == _source.getController(g)))
				return originalNumber + 1;
		}
		
		/* windingConstrictor_2 : put one more counter than originalNumber on player */
		else if (_effectName.equals("windingConstrictor_2") && (recipient instanceof Player)) {
			Player player = (Player) recipient;
			if (player == _source.getController(g))
				return originalNumber + 1;
		}
		
		/* Melira : prevents poison counters to be put on player */
		else if (_effectName.equals("melira_1") && (recipient instanceof Player)) {
			Player player = (Player) recipient;
			if ((ct == CounterType.POISON) && (player == _source.getController(g)))
				return 0;
		}
		
		/* Melira prevents -1/-1 counters on creatures */
		else if (_effectName.equals("melira_2") && (recipient instanceof Card)) {
			Card permanent = (Card) recipient;
			if ((ct == CounterType.MINUS_ONE) && (permanent.getController(g) == _source.getController(g)))
				return 0;
		}
		
		return originalNumber;
	}
	
	/* Effects that modify the max number of lands playable per turn */
	public int modifyMaxNbLands(Game g, Player player, int maxNbLands) {
		/* Exploration */
		if ((_effectName.equals("exploration")) && (player == _source.getController(g))) {
			maxNbLands = maxNbLands + 1;
		}
		return maxNbLands;
	}
	
	/**
	 * Effects that prevent certain players to cast certain spells (i.e. Meddling Mage or Narset ulti)
	 * @param p The player trying to cast the spell.
	 * @param spell The spell that is tried to be cast.
	 * @param g 
	 * @return True if the spell cannot be cast, false otherwise.
	 */
	public boolean isSpellForbidden(Player p, Card spell, Game g) {
		/* Narset Transcendent 3rd ability (oppponents cannot cast noncreature spells) */
		if (_effectName.equals("narsetT_emblem")) {
			if (p != _source.getController(g)) {
				if (!spell.isCreatureCard())
					return true;
			}
		}
		
		/* ionaShieldEmeria */
		else if (_effectName.equalsIgnoreCase("ionaShieldEmeria")) {
			Card iona = (Card) _source;
			Color chosenColor = iona.getChosenColor();
			if (p != _source.getController(g)) {
				if (spell.hasColor(chosenColor))
					return true;
			}
		}
		
		/* Meddling Mage */
		else if (_effectName.equals("meddlingMage")) {
			if ((_additionalData != null) && spell.getName().equals(_additionalData)) 
				return true;
		}
		
		/* Gaddock Teeg */
		else if (_effectName.equals("gaddockTeeg_1")) {
			if (!spell.isCreatureCard() && (spell.getConvertedManaCost(g) >= 4))
				return true;
		}
		else if (_effectName.equals("gaddockTeeg_2")) {
			if (!spell.isCreatureCard() && spell.hasXValue())
				return true;
		}
		
		/* arcaneLaboratory */
		else if (_effectName.equals("arcaneLaboratory")) {
			if (spell.getController(g).getNbSpellsCastThisTurn() > 0)
				return true;
		}
		
		return false;
	}
	
	/* Effects that grant Evergreen abilities */
	public boolean grantsEvergreen(Evergreen ability, MtgObject object, Game g) {
		Card card = null;
		if (object instanceof Card)
			card = (Card) object;
		
		switch (ability) {
		case CANTBLOCK:
			break;
		
		case CHANGELING:
			break;
			
		case DEATHTOUCH:
			// Basilisk Collar
			if (_effectName.equals("basiliskCollar") && (card == ((Card) _source).getHost()))
				 return true;
			break;
			
		case DEFENDER:
			break;
			
		case DOUBLESTRIKE:
			break;
			
		case FIRSTSTRIKE:
			/* reflexes */
			if (_effectName.equals("reflexes")) {
				if (card == ((Card) _source).getHost())
					return true;
			}

			break;
			
		case FLASH:
			break;
			
		case FLYING:
			//galeriderSliver (affects Slivers you control)
			if (_effectName.equals("galeriderSliver")) {
				if (card.isOTB(g) && card.hasCreatureType(g, CreatureType.Sliver) && (card.getController(g) == _source.getController(g)))
					return true;
			}

			/* zephidsEmbrace */
			else if (_effectName.equals("zephidsEmbrace")) {
				if (card == ((Card) _source).getHost())
					return true;
			}
			
			/* serrasEmbrace */
			else if (_effectName.equals("serrasEmbrace")) {
				if (card == ((Card) _source).getHost())
					return true;
			}

			/* shivsEmbrace */
			else if (_effectName.equals("shivsEmbrace")) {
				if (card == ((Card) _source).getHost())
					return true;
			}

			/* vampiricEmbrace */
			else if (_effectName.equals("vampiricEmbrace")) {
				if (card == ((Card) _source).getHost())
					return true;
			}
			
			/* launch_boost */
			else if (_effectName.equals("launch_boost")) {
				if (card == ((Card) _source).getHost())
					return true;
			}
			
			break;
			
		case HASTE:
			// Arlinn, Embraced by the Moon ultimate
			if (_effectName.equals("arlinnEBTM_emblem")) {
				if (card.isCreature(g) && (card.getController(g) == _source.getController(g)) && card.isOTB(g))
					return true;
			}
			
			// Fires of Yavimaya
			if (_effectName.equals("firesOfYavimaya")) {
				if (card.isCreature(g) && (card.getController(g) == _source.getController(g)) && card.isOTB(g))
					return true;
			}
			break;
			
		case HEXPROOF:
			break;
			
		case INDESTRUCTIBLE:
			// Elspeth Knight Errand, Ultimate
			if (_effectName.equals("elspethKE_3")) {
				if (card.isArtifact(g) || card.isCreature(g) || card.isEnchantmentCard() || card.isLand(g)) {
					if ((card.getController(g) == _source.getController(g)) && card.isOTB(g))
						return true;
				}	
			}
			break;
			
		case INFECT:
			break;
			
		case LIFELINK:
			// Basilisk Collar
			if (_effectName.equals("basiliskCollar") && (card == ((Card) _source).getHost()))
				 return true;
			
			// Batterskull
			if (_effectName.equals("batterskull") && (card == ((Card) _source).getHost()))
				 return true;
			break;
			
		case REACH:
			break;
			
		case SHADOW:
			break;
			
		case SHROUD:
			// For shroud, we need to differentiate Player and Card
			if (object instanceof Player) {

				// solitaryConfinement_shroud
				if (_effectName.equals("solitaryConfinement_shroud")) {
					Player player = (Player) object;
					if (player == _source.getController(g))
						return true;
				}
			}
			else  // (object instanceof Card)
			{ 
				//crystallineSliver (affects all Slivers)
				if (_effectName.equals("crystallineSliver"))				{
					if (card.isOTB(g) && card.hasCreatureType(g, CreatureType.Sliver))
						return true;
				}
				
				// sterlingGrove_shroud
				if (_effectName.equals("sterlingGrove_shroud")) {
					if ((card.isEnchantmentCard() && card.isOTB(g) && card != _source) && card.getController(g) == _source.getController(g))
						return true;
				}
				
				/* zephidsEmbrace */
				if (_effectName.equals("zephidsEmbrace")) {
					if (card == ((Card) _source).getHost())
						return true;
				}
			}
			
			break;
			
		case TRAMPLE:
			// Kavu Titan (when kicked)
			if (_effectName.equals("kavuTitan") && (_source == card)) {
				if (card.getSpellCastUsed().getOption() == Option.CAST_WITH_KICKER)
					return true;	
			}
			
			/* gaeasEmbrace_pump */
			else if (_effectName.equals("gaeasEmbrace_pump")) {
				if (((Card) _source).getHost() == card)
					return true;
			}
			break;
			
		case UNBLOCKABLE:
			/* cloakOfMists */
			if (_effectName.equals("cloakOfMists")) {
				if (card == ((Card) _source).getHost())
					return true;
			}
			break;
			
		case UNCOUNTERABLE:
			break;
			
		case UNDAMAGEABLE:
			// solitaryConfinement_undamageable
			if (_effectName.equals("solitaryConfinement_undamageable")) {
				if (!(object instanceof Player))
					break;
				else {
					Player player = (Player) object;
					if (player == _source.getController(g))
						return true;
				}
			}
			break;
			
		case VIGILANCE:
			// Batterskull
			if (_effectName.equals("batterskull") && (card == ((Card) _source).getHost()))
				 return true;
			
			/* serrasEmbrace */
			else if (_effectName.equals("serrasEmbrace")) {
				if (card == ((Card) _source).getHost())
					return true;
			}

			break;
			
		default:
			break;
		
		}
		return false;
	}

	/* Effects that make a permanent enter the battlefield tapped : i.e. Thalia */
	public boolean makesCardETBtapped(Game g, Card permanent) {
		/* thaliaHereticCathar */
		if (_effectName.equals("thaliaHereticCathar")) {
			if (_source.getController(g) != permanent.getController(g)) { // if the permanent is not controlled by the controller of Thalia
				if (permanent.isCreatureCard() || (permanent.isLandCard() && !permanent.hasSupertype(Supertype.BASIC)))
					return true;
			}
		}
		
		return false;
	}
	
	/* Effects that prevent a permanent from untapping during it's controller's untap step */
	public boolean doesUntapDuringUntapStep(Game g, Card permanent) {
		/* maintainTapped (used by cards like Mana Leech) */
		if (_effectName.equals("maintainTapped")) {
			if ((_source instanceof Card) && ((Card) _source).getLinkedCards().contains(permanent)) {
				return false;
			}
		}
		
		/* exhaustion_noUntap */
		else if (_effectName.equals("exhaustion_noUntap")) {
			Player target = (Player) _additionalData;
			if ((permanent.getController(g) == target) && (permanent.isLand(g) || permanent.isCreature(g)))
				return false;
		}
		
		/* backtoBasics */
		else if (_effectName.equals("backtoBasics"))
			if (!permanent.hasSupertype(Supertype.BASIC))
				return false;
		
		return true;
	}
	
	public boolean isAttackLegal(Card attacker, Game g) {
		/* okk_noAttack : check if a creature with power greater than Okk's was also declared attacking */
		if (_effectName.equals("okk_noAttack") && (attacker == _source)) {
			boolean bFound = false;
			Card Okk = attacker;
			Vector<Card> attackers = g.getAttackers();
			for (Card attackingCreature : attackers)
				if (attackingCreature.getPower(g) > Okk.getPower(g))
					bFound = true;
			if (!bFound)
				return false;
		}
		return true;
	}
	
	public boolean isBlockLegal_blocker(Card blocker, Game g) {
		/* okk_noAttack : check if a creature with power greater than Okk's was also declared blocking */
		if (_effectName.equals("okk_noBlock") && (blocker == _source)) {
			boolean bFound = false;
			Card Okk = blocker;
			Vector<Card> blockers = g.getBlockers();
			for (Card blockingCreature : blockers)
				if (blockingCreature.getPower(g) > Okk.getPower(g))
					bFound = true;
			if (!bFound)
				return false;
		}
		
		return true;
	}
	
	public boolean isBlockLegal_attacker(Card attacker, Game g) {
		/* phyrexianColossus_evasion : check that at least 3 creatures were declared as blockers */
		if (_effectName.equals("phyrexianColossus_evasion") && (attacker == _source)) {
			Card colossus = attacker;
			int nbBlockers = 0;
			Vector<Card> blockers = g.getBlockers();
			for (Card blockingCreature : blockers) {
				if (blockingCreature.getBlockedCreatures().contains(colossus))
					nbBlockers++;
			}
			if ((nbBlockers > 0) && (nbBlockers < 3))
				return false;
		}
		
		return true;
	}
	
	/* Effects that prevent attacking */
	public boolean canAttack(Card attacker, Game g) {
		/* glacialChasm_cantAttack */
		if (_effectName.equals("glacialChasm_cantAttack") && (attacker.getController(g) == this.getController(g)))
			return false;
		
		/* wirecat : can't attack if an enchantment is on the battlefield */
		else if (_effectName.equals("wirecat") && (attacker == _source)) {
			if (g.getBattlefield().getEnchantments().size() > 0)
				return false;
		}
		
		/* veiledSerpent_cannotAttack : can't attack unless defending player controls an island */
		else if (_effectName.equals("veiledSerpent_cannotAttack") && (attacker == _source)) {
			Player defending = attacker.getController(g).getOpponent();
			if (!defending.controlsABasicLandType(Subtype.ISLAND))
				return false;
		}
		
		/* pacifism */
		else if (_effectName.equals("pacifism")) {
			Card aura = (Card) _source;
			if (attacker == aura.getHost())
				return false;
		}
		return true;
	}
	
	/* Effects that prevent blocking */
	public boolean canBlock(Card blocker, Card attacker, Game g) {
		/* bedlam */
		if (_effectName.equals("bedlam"))
			return false;
		
		if (attacker.isCreature(g) && (attacker == _source) && attacker.isOTB(g)) {
			/* denProtector_evasion */
			if (_effectName.equals("denProtector_evasion")) {
				if (blocker.getPower(g) < attacker.getPower(g))
					return false;
			}
			
			/* treetopRangers_evasion */
			else if (_effectName.equals("treetopRangers_evasion")) {
				if (!blocker.hasEvergreenGlobal(Evergreen.FLYING, g))
					return false;
			}
			
			/* signalPest_evasion */
			else if (_effectName.equals("signalPest_evasion")) {
				if (!(blocker.hasEvergreenGlobal(Evergreen.FLYING, g) || blocker.hasEvergreenGlobal(Evergreen.REACH, g)))
					return false;
			}	
		}
		
		/* falter_noBlock : creatures without flying can't block */
		if (_effectName.equals("falter_noBlock") && !blocker.hasEvergreenGlobal(Evergreen.FLYING, g))
			return false;
		
		/* wirecat : can't block if an enchantment is on the battlefield */
		else if (_effectName.equals("wirecat") && (blocker == _source)) {
			if (g.getBattlefield().getEnchantments().size() > 0)
				return false;
		}
		
		/* pacifism */
		else if (_effectName.equals("pacifism")) {
			Card aura = (Card) _source;
			if (blocker == aura.getHost())
				return false;
		}
		
		return true;
	}
	
	/* Effects that modify protections */
	public Vector<Object> getProtections(Game g, MtgObject object) {
		Vector<Object> prot = new Vector<Object>();
		
		// Sword of Fire and Ice
		if (_effectName.equals("swordFireAndIce") && (object == ((Card) _source).getHost())) {
			prot.add(Color.RED);
			prot.add(Color.BLUE);
		}
		
		// Sword of Feast and Famine
		else if (_effectName.equals("swordFeastAndFamine") && (object == ((Card) _source).getHost())) {
			prot.add(Color.BLACK);
			prot.add(Color.GREEN);
		}
		
		// Sword of Body and Mind
		else if (_effectName.equals("swordBodyAndMind") && (object == ((Card) _source).getHost())) {
			prot.add(Color.GREEN);
			prot.add(Color.BLUE);
		}
		
		// Etched Champion
		else if (_effectName.equals("etchedChampion") && (object == _source)) {
			if (_source.getController(g).hasBuff(PlayerBuff.Metalcraft)) {
				prot.add(Color.WHITE);
				prot.add(Color.BLUE);
				prot.add(Color.BLACK);
				prot.add(Color.RED);
				prot.add(Color.GREEN);
			}
		}
		
		/* absoluteGrace */
		else if (_effectName.equals("absoluteGrace")) {
			if (object instanceof Card && ((Card)object).isCreature(g))
				prot.add(Color.BLACK);
		}

		/* absoluteLaw */
		else if (_effectName.equals("absoluteLaw")) {
			if (object instanceof Card && ((Card)object).isCreature(g))
				prot.add(Color.RED);
		}
		
		return prot;
	}
	
	public boolean replacesDraw(Game g, Player p) {
		/* abundance */
		if (_effectName.equals("abundance") && (_source.getController(g) == p))
			return true;
		
		return false;
	}
	
//	public boolean isColor(Game g, Card c, Color col) {
//		/* darkestHour */
//		//TODO
//	}

	public String setPT(Game g, Card c, String pt) {
		/* Opalescence */
		if (_effectName.equals("opalescence")) {
			if (c.isEnchantment(g) && c.isCreature(g) && c.isOTB(g)) {
				int cmc = c.getConvertedManaCost(g);
				return String.format("%d/%d", cmc, cmc);
			}
		}
		
		// Else, return unchanged PT
		return pt;
	}
	
	public String addPT(Game g, Card c, String pt) {
		Player sourceController = _source.getController(g);
		Player creatureController = c.getController(g);
		
		// Equipment and Auras
		Pattern p = Pattern.compile("^.*[equipped|enchanted] creature gets ([+-]{1}[0-9]+/[+-]{1}[0-9]+)(.*).$");
		Matcher m = p.matcher(_effectText.toLowerCase());
		
		if (m.matches()) {
			int number = 1;

			if ((_source instanceof Card) && (c == ((Card) _source).getHost())) {
				String definition = m.group(1);
				String moreText = m.group(2);
				if (!moreText.isEmpty()) {
					p = Pattern.compile("^ for each (.*) you control$");
					m = p.matcher(moreText);
					
					if (m.matches()) {
						String criteria = m.group(1);
						System.out.println("match " + criteria);
						
						// card types (artifact, enchantments, etc)
						if (CardType.parse(criteria) != null) {
							number = sourceController.getNbPermanentsControlledOfType(CardType.parse(criteria));
						}
						// card subtypes (goblin, forest, etc)
						else if (Subtype.parse(criteria) != null) {
							number = sourceController.getNbPermanentsControlledOfSubtype(Subtype.parse(criteria));
						}
						else {
							System.out.println("Unable to understand this criteria : " + criteria);
						}
							
					}
					else {
						System.out.println("no match");
					}
				}
				return Card.addPT(pt, definition, number);
			}
		}
		
		/* Glorious Anthem */
		if (_effectName.equals("gloriousAnthem")) {
			if (creatureController == sourceController)
				return Card.addPT(pt, "+1/+1");
		}

		/* crosswinds */
		else if (_effectName.equals("crosswinds")) {
			if (c.hasEvergreenGlobal(Evergreen.FLYING, g))
				return Card.addPT(pt, "-2/-0");
		}

		/* masterOfEtherium_pump */
		else if (_effectName.equals("masterOfEtherium_pump")) {
			if ((creatureController == sourceController) && c.hasCardTypeGlobal(g, CardType.ARTIFACT) && (c != _source))
				return Card.addPT(pt, "+1/+1");
		}
		
		/* Glorious Anthem */
		else if (_effectName.equals("gloriousAnthem")) {
			if (creatureController == sourceController)
				return Card.addPT(pt, "+1/+1");
		}
		
		/* endoskeleton_pump */
		else if (_effectName.equals("endoskeleton_pump")) {
			if (c == (Card) _additionalData) // targeted creature is in additional data
				return Card.addPT(pt, "+0/+3");
		}
		
		/* wildNacatl_mountain */
		else if (_effectName.equals("wildNacatl_mountain")) {
			if ((c == _source) && (creatureController.controlsABasicLandType(Subtype.MOUNTAIN)))
				return Card.addPT(pt, "+1/+1");
		}

		/* wildNacatl_plains */
		else if (_effectName.equals("wildNacatl_plains")) {
			if ((c == _source) && (creatureController.controlsABasicLandType(Subtype.PLAINS)))
				return Card.addPT(pt, "+1/+1");
		}
		
		/* werebear */
		else if (_effectName.equals("werebear")) {
			if ((c == _source) && (creatureController.hasBuff(PlayerBuff.Threshold)))
				return Card.addPT(pt, "+3/+3");
		}
		
		/* inventorsApprentice */
		else if (_effectName.equals("inventorsApprentice")) {
			if ((c == _source) && creatureController.controlsAnArtifact())
				return Card.addPT(pt, "+1/+1");
		}
		
		/* knightOfTheReliquary_pump */
		else if (_effectName.equals("knightOfTheReliquary_pump")) {
			if (c == _source) {
				int nbLandsInGraveyard = c.getController(g).getGraveyard().getNbLands();
				return Card.addPT(pt, "+" + nbLandsInGraveyard + "/+" + nbLandsInGraveyard);
			}
		}
		
		/* sylvanAdvocate */
		else if (_effectName.equals("sylvanAdvocate")) {
			if (c.getController(g).getNbLandsControlled() >= 6) {
				// pump itself
				if (c == _source)
					return Card.addPT(pt, "+2/+2");
				
				// pump land creatures you control
				if ((creatureController == sourceController) && c.isLand(g))
					return Card.addPT(pt, "+2/+2");	
			}
		}

		/* Deranged Hermit */
		else if (_effectName.equals("derangedHermit_boost")) {
			if (c.hasCreatureType(g, CreatureType.Squirrel))
				return Card.addPT(pt, "+1/+1");
		}
		
		/* eleshNorn_bonus */
		else if (_effectName.equals("eleshNorn_bonus")) {
			if ((creatureController == sourceController) && (c != _source))
				return Card.addPT(pt, "+2/+2");
		}
		
		/* eleshNorn_malus */
		else if (_effectName.equals("eleshNorn_malus")) {
			if (creatureController != sourceController)
				return Card.addPT(pt, "-2/-2");
		}
		
		/* deathsShadow */
		else if (_effectName.equals("deathsShadow")) {
			int life = sourceController.getLife();
			if (c == _source)
				return Card.addPT(pt, "-" + life + "/-" + life);
		}
		
		/* muscleSliver */
		else if (_effectName.equals("muscleSliver")) {
			if (c.hasCreatureType(g, CreatureType.Sliver))
				return Card.addPT(pt, "+1/+1");
		}
		
		/* predatorySliver */
		else if (_effectName.equals("predatorySliver")) {
			if (c.hasCreatureType(g, CreatureType.Sliver) && (creatureController == sourceController))
				return Card.addPT(pt, "+1/+1");
		}
		
		// Else, return unchanged PT
		return pt;
	}

	public boolean doesWorshipEffect(Game g, Player playerDealtDamage) {
		Player worshipController = _source.getController(g);
		
		/* worship */
		if (_effectName.equals("worship")) {
			if ((worshipController == playerDealtDamage) && (worshipController.getNbCreaturesControlled() >= 1))
				return true;
		}
		return false;
	}
	
	public int modifyDamageAmount(Game g, DamageSource source, Damageable recipient, int amount) {
		/* sulfuricVapors */
		if (_effectName.equals("sulfuricVapors")) {
			if (source instanceof Card) {
				Card card = (Card) source;
				if (card.isOTS(g) && card.hasColor(Color.RED))
					return amount + 1;
			}
		}
		return amount;
	}
	
	public Vector<TriggeredAbility> getGrantedTriggeredAbilities(Card c, Game g, Event event, boolean bCardinfo) {
		Vector<TriggeredAbility> ret = new Vector<TriggeredAbility>();
		
		// Tabernacle at Pendrell machin
		if (_effectName.equals("tabernacle")) {
			if (bCardinfo || (event == Event.BegOfYourUpkeep)) {
				if (c.isCreature(g) && c.isOTB(g))
					ret.add(TriggeredAbilityFactory.create("tabernacle_upkeep", c));
			}
		}
		
		/* pendrellFlux */
		else if (_effectName.equals("pendrellFlux")) {
			if (bCardinfo || (event == Event.BegOfYourUpkeep)) {
				if (_source instanceof Card && (((Card)_source).getHost() == c))
					ret.add(TriggeredAbilityFactory.create("pendrellFlux_upkeep", c));
			}
		}
		
		/* retaliation */
		else if (_effectName.equals("retaliation")) {
			if (bCardinfo || (event == Event.BecomesBlockedByACreature)) {
				if (c.isCreature(g) && c.isOTB(g) && (c.getController(g) == _source.getController(g)))
					ret.add(TriggeredAbilityFactory.create("retaliation_pump", c));
			}
		}
		return ret;
	}
	
	public Vector<TriggeredAbility> getGrantedTriggeredAbilitiesForCardinfo(Card c, Game g) {
		Vector<TriggeredAbility> ret = new Vector<TriggeredAbility>();
		
		// Tabernacle at Pendrell machin
		if (_effectName.equals("tabernacle")) {
			if (c.isCreature(g) && c.isOTB(g)) {
				ret.add(TriggeredAbilityFactory.create("tabernacle_upkeep", c));
			}
		}
		return ret;
	}
	
	public Vector<ActivatedAbility> getGrantedActivatedAbilities(Card c, Game g) {
		Vector<ActivatedAbility> ret = new Vector<ActivatedAbility>();
		//Cost cost;
		
		/* cryptolithRite */
		if (_effectName.equals("cryptolithRite")) {
			if ((c.getController(g) == _source.getController(g)) && c.isOTB(g) && c.isCreature(g)) {
				ret.add(ActivatedAbilityFactory.create("addAnyMana", c));
			}
		}
		
		/* rishkarPeemaRenegade_mana */
		else if (_effectName.equals("rishkarPeemaRenegade_mana")) {
			if ((c.getController(g) == _source.getController(g)) && c.isOTB(g) && c.isCreature(g) && c.hasCounters()) {
				ret.add(ActivatedAbilityFactory.create("addGreenMana", c));
			}
		}
		
		/* citanulHierophants */
		else if (_effectName.equals("citanulHierophants")) {
			if ((c.getController(g) == _source.getController(g)) && c.isOTB(g) && c.isCreature(g)) {
				ret.add(ActivatedAbilityFactory.create("addGreenMana", c));
			}
		}
		
		/* hermeticStudy */
		else if (_effectName.equals("hermeticStudy")) {
			Card aura = (Card) _source;
			Card host = aura.getHost();
			if (c == host) {
				ret.add(ActivatedAbilityFactory.create("ping", c));
			}
		}
		
		/* hibernationSliver */
		else if (_effectName.equals("hibernationSliver")) {
			if (c.isOTB(g) && c.isCreature(g) && c.hasCreatureType(g, CreatureType.Sliver)) {
				ret.add(ActivatedAbilityFactory.create("hibernationSliver_return", c));
			}
		}

		/* Arlinn, Embraced by the Moon */
		else if (_effectName.equals("arlinnEBTM_emblem")) {
			if (c.isOTB(g) && c.isCreature(g) && (c.getController(g) == _source.getController(g))) {
				ret.add(ActivatedAbilityFactory.create("arlinn_tapToDamage", c));
			}
		}
		
		return ret;
	}
	
	/* Triggered Abilities */
	public void addTriggeredEffect(TriggeredAbility ta) {
		if (_triggeredAbilities == null)
			_triggeredAbilities = new Vector<TriggeredAbility>();
		_triggeredAbilities.add(ta);
	}
	
	public Vector<TriggeredAbility> getTriggeredAbilities(Game g, Event event) {
		TriggeredAbility t;
		TriggeredAbility t2;
		Vector<TriggeredAbility> ta = new Vector<TriggeredAbility>();
		if (_triggeredAbilities == null)
			return null;
		for (int i = 0; i < _triggeredAbilities.size(); i++) {
			t = _triggeredAbilities.get(i);
			if ((t.getEvent() == event) && (t.getOrigin() == Origin.CONTINUOUS_EFFECT)) {
				t2 = _triggeredAbilities.get(i).clone();
				t2.setController(g, getSource().getController(g));
				ta.add(t2);
			}
		}
		return ta;
	}

	public boolean hasTriggeredAbilities(Game g, Event event) {
		if (_triggeredAbilities == null)
			return false;
		
		if (this.getTriggeredAbilities(g, event).size() > 0)
			return true;
		
		return false;
	}

	public void computeDamagePrevention(Game g, DamageSource damageSource, Damageable recipient) {
		/* urzasArmor */
		if (_effectName.equals("urzasArmor") && (recipient == this.getController(g)))
			this.getController(g).addDamagePrevention(1);
	}
	
	public boolean isDamagePrevented(Game g, DamageSource damageSource, Damageable recipient, boolean bCombatDamage) {
		/* glacialChasm_preventDamage */
		if (_effectName.equals("glacialChasm_preventDamage") && (recipient == this.getController(g)))
			return true;
		
		/* energyField_prevent */
		else if (_effectName.equals("energyField_prevent") && (recipient == this.getController(g)) && (damageSource.getController(g) != this.getController(g)))
			return true;

		/* fogBank */
		else if (_effectName.equals("fogBank") && bCombatDamage) {
			if ((damageSource == _source) || (recipient == _source))
				return true;
		}
		
		/* fog */
		else if (_effectName.equals("fog") && bCombatDamage)
			return true;
		
		/* runeOfProtection_prevent */
		else if (_effectName.equals("runeOfProtection_prevent")) {
			DamageSource dmgSource = (DamageSource) _additionalData;
			if ((recipient == this.getController(g)) && (damageSource == dmgSource)) {
				// The shield effect has been consumed. Remove the continuous effect.
				g.getContinuousEffects().remove(this);
				return true;
			}
		}
		
		/* sanctumGuardian_prevent */
		else if (_effectName.equals("sanctumGuardian_prevent")) {
			/*
			 * The additional data is a vector with 2 objects :
			 * object [0] is the target.
			 * object [1] is the chosen source which damage will be prevented.
			 */
			@SuppressWarnings("unchecked")
			Vector<Object> data = (Vector<Object>) _additionalData;
			Damageable target = (Damageable) data.get(0);
			Card dmgSource = (Card) data.get(1);
			if ((recipient == target) && (damageSource == dmgSource)) {
				// The shield effect has been consumed. Remove the continuous effect.
				g.getContinuousEffects().remove(this);
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Effects that replace a creature dying.
	 * @param card
	 * @return
	 */
	public boolean replace_Dies(Game g, Card card) {
		// Kalitas (instead of putting the card in the graveyard, exile it and put a 2/2 Zombie token)
		if (_effectName.equals("kalitas_putToken")) {
			if (card.isCreature(g) && !card.isToken() && (card.getController(g) != _source.getController(g)))
				return true;
		}
		
		else if (_effectName.equals("exileUponDeath")) {
			if (card == _additionalData)
				return true;
		}
		
		return false;
	}

	/**
	 * Effects that replace a card being put into a graveyard from anywhere (i.e. Yawgmoth's Will)
	 * @param card
	 * @return
	 */
	public boolean replace_PutIntoGraveyardFromAnywhere(Game g, Card card) {
		// Yawgmoth's Will (instead of putting the card in the graveyard, exile it)
		if (_effectName.equals("yawgmothsWill_exile")) {
			if (card.getOwner() == _source.getController(g))
				return true;
		}
		
		// Torrential Gearhulk
		else if (_effectName.equals("torrentialGearhulk_exile")) {
			if (card == _additionalData)
				return true;
		}
			
		return false;
	}
	
	public void processReplacement(Game g, Card card) {
		// Kalitas (instead of putting the card in the graveyard, exile it and put a 2/2 Zombie token)
		if (_effectName.equals("kalitas_putToken")) {
			if (g.move_BFD_to_EXL(card) == Response.OK) {
				g.createSingleToken(Token.BLACK_ZOMBIE_22, (StackObject) _source);
			}
		}
		
		// Yawgmoth's Will (instead of putting the card in the graveyard, exile it)
		else if (_effectName.equals("yawgmothsWill_exile") || _effectName.equals("torrentialGearhulk_exile"))
			g.switchZone(card.getZone(g), card, card.getOwner().getExile());
		
		// Effects that exile a creature that would die
		else if (_effectName.equals("exileUponDeath"))
			g.move_BFD_to_EXL(card);
	}

	public boolean grantsCardType(Game g, Card card, CardType t) {
		// Effects that grant the creature type
		if (t == CardType.CREATURE) {
			// Opalescence
			if (_effectName.equals("opalescence")) {
				if ((_source != card) && card.isEnchantmentCard() && !card.hasSubtypeGlobal(g, Subtype.AURA) && card.isOTB(g))
					return true;
			}
		}
		
		// Effects that grant the artifact type
		else if (t == CardType.ARTIFACT) {
			
		}
		return false;
	}
	
	// Some effects stop existing at the end of turn
	public void setStop(StopWhen when) {
		_bStopWhen = when;
	}
	
	public boolean doesStop(StopWhen when) {
		return (_bStopWhen == when);
	}

	public boolean canPlayCardFromGraveyard(Game g, Player controller) {
		/* yawgmothsWill_play */
		if (_effectName.equals("yawgmothsWill_play")) {
			if (controller == _source.getController(g))
				return true;
		}
		return false;
	}

	public boolean makesIsland(Card land) {
		/* lingeringMirage */
		if (_effectName.equals("lingeringMirage")) {
			if (land == ((Card)_source).getHost())
				return true;
		}
		return false;
	}
	
	public boolean modifiesController(StackObject obj) {
		/* confiscate */
		if (_effectName.equals("confiscate")) {
			if (obj == ((Card)_source).getHost())
				return true;
		}
		return false;
	}

	public Player getNewController(Game g, StackObject obj) {
		/* confiscate */
		if (_effectName.equals("confiscate")) {
			if (((Card)_source).getTargetObject(0) == obj)
				return _source.getController(g);
		}
		return obj.getOwner();
	}

	public boolean grantsContinuousEffect(Game g, String effectName, Card card) {
		/* lingeringMirage */
		if (_effectName.equals("lingeringMirage")) {
			if ((effectName == "lingeringMirage_strip") && (card == ((Card)_source).getHost()))
				return true;
		}
		return false;
	}
	
	public Damageable computeDamageRedirection(Game g, DamageSource source, Damageable recipient) {
		/* pariah */
		if (_effectName.equals("pariah")) {
			Card aura = (Card) _source;
			if (recipient instanceof Player) {
				if (recipient == aura.getController(g))
					return aura.getHost();
			}
		}
		return recipient;
	}
}
