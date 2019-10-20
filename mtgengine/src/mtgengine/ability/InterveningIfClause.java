package mtgengine.ability;

import java.util.Vector;

import mtgengine.CounterType;
import mtgengine.Game;
import mtgengine.action.SpellCast;
import mtgengine.action.SpellCast.Option;
import mtgengine.card.Card;
import mtgengine.player.Player;
import mtgengine.type.CardType;
import mtgengine.type.CreatureType;

public class InterveningIfClause {
	private TriggeredAbility _ta;
	
	public InterveningIfClause(TriggeredAbility triggeredAbility) {
		_ta = triggeredAbility;
	}

	/**
	 * Checks if the clause is valid.
	 * @param g
	 * @return
	 */
	public boolean validate(Game g) {
		Card source = _ta.getSource();
		String triggeredAbilityName = _ta.getAbilityName();
		Object additionalData = _ta.getAdditionalData();
		Player controller = source.getController(g);
		
		/* Nether Spirit : check it is the only creature in the grveyard */
		if (triggeredAbilityName.equals("netherSpirit")) {
			if (source.isIGY(g) && (controller.getGraveyard().getNbCreatures() == 1))
				return true;
		}

		/* sleeping enchantmenys : check if it's still an enchantment */
		else if (triggeredAbilityName.equals("hiddenAncients") ||
				 triggeredAbilityName.equals("hiddenGuerrillas") ||
				 triggeredAbilityName.equals("hiddenHerd") || 
				 triggeredAbilityName.equals("hiddenPredators") ||
				 triggeredAbilityName.equals("hiddenSpider") ||
				 triggeredAbilityName.equals("hiddenStag") ||
				 triggeredAbilityName.equals("opalAcrolith_animate") ||
				 triggeredAbilityName.equals("opalArchangel") ||
				 triggeredAbilityName.equals("opalCaryatid") ||
				 triggeredAbilityName.equals("opalGargoyle") ||
				 triggeredAbilityName.equals("opalTitan") ||
				 triggeredAbilityName.equals("veilOfBirds") ||
				 triggeredAbilityName.equals("veiledApparition") ||
				 triggeredAbilityName.equals("veiledCrocodile") ||
				 triggeredAbilityName.equals("veiledSentry") ||
				 triggeredAbilityName.equals("veiledSerpent")
				 ) {
			if (source.hasCardTypeGlobal(g, CardType.ENCHANTMENT))
				return true;
		}

		/* pestilence_sac : check that there are no creatures in play */
		else if (triggeredAbilityName.equals("pestilence_sac")) {
			if (g.getBattlefield().getCreatures().size() == 0)
				return true;
		}
		
		/* greenerPastures : check that ability controller controls more lands than his opponent */
		else if (triggeredAbilityName.equals("greenerPastures")) {
			Player activePlayer = g.getActivePlayer();
			Player hisOpponent = activePlayer.getOpponent();
			if (activePlayer.getNbLandsControlled() > hisOpponent.getNbLandsControlled())
				return true;
		}
		
		/* diabolicServitude_return : check that the creature that died is linked to Diabolic Servitude */
		else if (triggeredAbilityName.equals("diabolicServitude_return")) {
			Card creature = (Card) _ta.getAdditionalData();
			if (!source.getLinkedCards().isEmpty()) {
				if (creature == source.getLinkedCards().get(0)) {
					if (creature.getInternalObjectId() == source.getLinkedCardInternalId(creature))
						return true;
				}
			}
		}
		
		/* reclusiveWight : check that controller controls another nonland permanent */
		else if (triggeredAbilityName.equals("reclusiveWight")) {
			Vector<Card> permanents = g.getBattlefield().getPermanentsControlledBy(controller);
			for (Card permanent : permanents) {
				if ((permanent != source) && !permanent.isLand(g))
					return true;
			}
		}
		
		/* wildDogs : check that a player has more life than each other */
		else if (triggeredAbilityName.equals("wildDogs")) {
			Vector<Player> players = g.getPlayers();
			if (players.get(0).getLife() != players.get(1).getLife())
				return true;
		}
		
		/* vampiricEmbrace_counter : check that the creature that dies was dealt damage by enchanted creature this turn */
		else if (triggeredAbilityName.equals("vampiricEmbrace_addCounter")) {
			Card dyingCreature = (Card) additionalData;
			Card aura = source;
			Card host = aura.getHost();
			
			if (dyingCreature.wasDamagedThisTurnBy(host))
				return true;
		}
		
		/* chaliceVoid_counterSpell : check the spell converted mana cost = X */
		else if (triggeredAbilityName.equals("chaliceVoid_counterSpell")) {
			Card triggeringSpell = (Card) additionalData;
			if (triggeringSpell.getConvertedManaCost(g) == source.getNbCountersOfType(g, CounterType.CHARGE))
				return true;
		}
		
		/* persist : check that the creature didn't have any -1/-1 counter on it */
		else if (triggeredAbilityName.equals("persist")) {
			if (source.getNbCountersOfType(g, CounterType.MINUS_ONE) == 0)
				return true;
		}
		
		/* linvalaPreserver_gainLife : check that an opponent has more life */
		else if (triggeredAbilityName.equals("linvalaPreserver_gainLife")) {
			Player opponent = controller.getOpponent();
			if (opponent.getLife() > controller.getLife())
				return true;
		}
		
		/* linvalaPreserver_putToken : check that an opponent controls more creatures */
		else if (triggeredAbilityName.equals("linvalaPreserver_putToken")) {
			Player opponent = controller.getOpponent();
			if (opponent.getNbCreaturesControlled() > controller.getNbCreaturesControlled())
				return true;
		}
		
		/* thranQuarry_sacrifice : check that controller has no creatures in play */
		else if (triggeredAbilityName.equals("thranQuarry_sacrifice")) {
			if (g.getBattlefield().getCreaturesControlledBy(controller).size() == 0)
				return true;
		}
		
		/* glimmervoid_sacrifice : check that controller has no artifacts in play */
		else if (triggeredAbilityName.equals("glimmervoid_sacrifice")) {
			if (g.getBattlefield().getArtifactsControlledBy(controller).size() == 0)
				return true;
		}
		
		/* Skizzik (sacrifice) : check that kicker cost was not paid */
		else if (triggeredAbilityName.equals("skizzik_sacrifice")) {
			SpellCast mode = source.getSpellCastUsed();
			if (mode == null)
				return true;
//			String name = mode.getCardinfoText();
//			if ((name != null) && name.equals("Kicker {R}"))
//				return false;
			return true;
		}
		
		/* oathOfLiliana_putToken : check that a planeswalker entered the battlefield under your control this turn */
		else if (triggeredAbilityName.equals("oathOfLiliana_putToken")) {
			if (controller.getNbPlaneswalkerDeployedThisTurn() > 0)
				return true;
		}
		
		/* Thornscape Battlemage 1 (shock) : check if kicker cost was paid */
		else if (triggeredAbilityName.equals("thornscapeBattlemage_1")) {
			SpellCast sc = source.getSpellCastUsed();
			Option option = sc.getOption();
			if (option == Option.CAST_WITH_KICKER) {
				if (sc.getParameter().equals("Kicker {R}") || sc.getParameter().equals("Kicker {R}{W}"))
					return true;
			}
		}
		
		/* Thornscape Battlemage 2 (destroy artifact) : check if kicker cost was paid */
		else if (triggeredAbilityName.equals("thornscapeBattlemage_2")) {
			SpellCast sc = source.getSpellCastUsed();
			Option option = sc.getOption();
			if (option == Option.CAST_WITH_KICKER) {
				if (sc.getParameter().equals("Kicker {W}") || sc.getParameter().equals("Kicker {R}{W}"))
					return true;
			}
		}
		
		/* Vengevine : check if the spell that triggered the ability was the second creature spell of the turn */
		else if (triggeredAbilityName.equals("vengevine")) {
			if (controller.getNbCreatureSpellsCastThisTurn() == 2)
				return true;
		}
		
		/* Ichorid : check Ichorid is in the graveyard */
		else if (triggeredAbilityName.equals("ichorid_reanimate")) {
			if (source.isIGY(g))
				return true;
		}
		
		/* Check echo has to be paid */
		else if (triggeredAbilityName.equals("echo")) {
			if (source.mustPayEcho() == true)
				return true;
		}
		
		/* Werewolf standard transformation DAY to NIGHT */
		else if (triggeredAbilityName.equals("werewolf_transform_night")) {
			if (g.getNbSpellsCastLastTurn() == 0)
				return true;
		}
		
		/* Werewolf standard transformation NIGHT to DAY */
		else if (triggeredAbilityName.equals("werewolf_transform_day")) {
			for (Player p : g.getPlayers()) {
				if (p.getNbSpellsCastLastTurn() >= 2)
					return true;	
			}
		}
		
		/* Sarcomancy : check for presence of Zombie on the battlefield */
		else if (triggeredAbilityName.equals("sarcomancy_damage"))
		{
			Vector<Card> permanents = g.getBattlefield().getPermanents();
			boolean bZombiesInPlay = false;
			for (Card permanent : permanents) {
				if (permanent.hasCreatureType(g, CreatureType.Zombie)) {
					bZombiesInPlay = true;
					break;
				}
			}
			if (!bZombiesInPlay)
				return true;
		}
		
		/* Nissa Vastwood Seer, landfall transform thingy */
		else if (triggeredAbilityName.equals("nissaVS_transform")) {
			if (g.getBattlefield().getLandsControlledBy(controller).size() >= 7)
				return true;
		}
		
		/* imaginaryPet : check that controller has a card in hand */
		else if (triggeredAbilityName.equals("imaginaryPet")) {
			if (controller.getHandSize() > 0)
				return true;
		}
		
		/* lifeline_trigger : check that another creature is on the battlefield at the time the creature dies */
		else if (triggeredAbilityName.equals("lifeline_trigger")) {
			if (g.getBattlefield().getCreatures().size() > 0)
				return true;
		}
		
		/* manaVault_damage : check that mana vault is tapped*/
		else if (triggeredAbilityName.equals("manaVault_damage")) {
			if (source.isTapped())
				return true;
		}
		
		/* toolcraftExemplar : check player controls at least one artifact */
		else if (triggeredAbilityName.equals("toolcraftExemplar")) {
			if (controller.controlsAnArtifact())
				return true;
		}

		/* throneWarden */
		else if (triggeredAbilityName.equals("throneWarden")) {
			if (g.getTheMonarch() == controller)
				return true;
		}
		
		/* suspend_remove_counter : check card is suspended (= is in exile, has suspend, has at least one time counter on it)*/
		else if (triggeredAbilityName.equals("suspend_remove_counter")) {
			if (source.isIEX(g) && source.hasStaticAbility(StaticAbility.SUSPEND) && source.hasCounters(CounterType.TIME))
				return true;
		}

		/* suspend_cast_from_exile : check card is exiled */
		else if (triggeredAbilityName.equals("suspend_cast_from_exile")) {
			if (source.isIEX(g))
				return true;
		}
		
		/* Should never get there */
		else {
			System.err.println("Invalid triggered ability intervening if clause : " + triggeredAbilityName);
		}
		return false;
	}

	public InterveningIfClause clone(TriggeredAbility ta) {
		InterveningIfClause clone = new InterveningIfClause(ta);
		return clone;
	}
}
