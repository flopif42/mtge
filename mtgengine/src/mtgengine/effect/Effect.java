package mtgengine.effect;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mtgengine.CounterType;
import mtgengine.Game;
import mtgengine.MtgObject;
import mtgengine.StackObject;
import mtgengine.Game.Answer;
import mtgengine.Game.Response;
import mtgengine.Game.State;
import mtgengine.ability.ActivatedAbility;
import mtgengine.ability.ActivatedAbilityFactory;
import mtgengine.ability.Emblem;
import mtgengine.ability.Evergreen;
import mtgengine.ability.StaticAbilityFactory;
import mtgengine.ability.TriggeredAbility;
import mtgengine.ability.TriggeredAbility.Event;
import mtgengine.ability.TriggeredAbility.Origin;
import mtgengine.ability.TriggeredAbilityFactory;
import mtgengine.action.SpellCast.Option;
import mtgengine.card.Card;
import mtgengine.card.Color;
import mtgengine.card.Token;
import mtgengine.damage.DamageSource;
import mtgengine.damage.Damageable;
import mtgengine.effect.ContinuousEffect.StopWhen;
import mtgengine.type.CreatureType;
import mtgengine.type.Subtype;
import mtgengine.type.Supertype;
import mtgengine.mana.Mana;
import mtgengine.mana.Mana.ManaType;
import mtgengine.modifier.AbilityModifier;
import mtgengine.modifier.CardTypeModifier;
import mtgengine.modifier.ColorModifier;
import mtgengine.modifier.CreatureTypeModifier;
import mtgengine.modifier.EvergreenModifier;
import mtgengine.modifier.Modifier;
import mtgengine.modifier.PTModifier;
import mtgengine.modifier.Modifier.Operation;
import mtgengine.player.Player;
import mtgengine.player.PlayerBuff;
import mtgengine.modifier.ProtectionModifier;
import mtgengine.modifier.SpellCastingModifier;
import mtgengine.type.CardType;
import mtgengine.zone.Battlefield;
import mtgengine.zone.Graveyard;
import mtgengine.zone.Library;
import mtgengine.zone.Zone;
import mtgengine.zone.Zone.Name;

public class Effect {
	private String _effectMethodName;
	private String _rulesText;
	private static int nbTimes;
	
	// Simple generic effects that use the same function
	public static final String CREW = "crew";
	public static final String TAP_TARGET = "tapTarget";
	public static final String UNTAP_THIS = "untapThis";
	public static final String DESTROY_TARGET_CANNOT_REGEN = "destroyTargetCannotRegen";
	public static final String DESTROY_TARGET_CAN_REGEN = "destroyTargetCanRegen";
	public static final String DRAW_A_CARD = "drawACard";
	public static final String DEAL_X_DAMAGE_TO_TARGET = "dealXdamageToTarget";
	public static final String DEAL_1_DAMAGE_TO_TARGET = "deal1damageToTarget";
	public static final String DEAL_2_DAMAGE_TO_TARGET = "deal2damageToTarget";
	public static final String DEAL_3_DAMAGE_TO_TARGET = "deal3damageToTarget";
	public static final String COUNTER_TARGET_STACKOBJECT = "counterTargetSpell";
	public static final String RETURN_TO_HAND = "returnToHand";      // Effects like Rancor
	public static final String LOSE_ALL_ABILITIES = "loseAllAbilities";
	public static final String PESTILENCE = "pestilence";
	public static final String PUMP = "pump";
	public static final String ADD_VERSE_COUNTER = "addVerseCounter";
	public static final String GAIN_1_ENERGY = "gain1energy";
	public static final String GAIN_2_ENERGY = "gain2energy";
	public static final String REGROWTH = "regrowth";
	public static final String PUT_PLUS_ONE_COUNTER_ON_THIS = "plusOneCounterOnThis";
	public static final String ADD_MANA = "addMana";
	
	public Effect(String methodName, String text) {
		_effectMethodName = methodName;
		_rulesText = text;
	}
	
	public String getMethodName() {
		return _effectMethodName;
	}
	
	public String getRulesText() {
		return _rulesText;
	}
	
	/**
	 * 
	 * @param g
	 * @param aa
	 * @param manaString
	 * @return
	 */
	private static int doManaAftermath(Game g, StackObject aa, String manaString) {
		Player controller = aa.getController(g);
		Card land = aa.getSource();
		String cardname = aa.getSource().getName().toLowerCase();
		String pat;
		Pattern p;
		Matcher m;
		
		// Pain lands
		pat = String.format(".*%s deals ([0-9]+) damage to you$", cardname);
		p = Pattern.compile(pat);
		m = p.matcher(manaString);
		if (m.matches()) {
			land.dealNonCombatDamageTo(g, controller, Integer.parseInt(m.group(1)));
			return 1;
		}
		
		// Gemstone Mine
		p = Pattern.compile(".*gemstone mine.*");
		m = p.matcher(manaString);
		if (m.matches()) {
			if (land.getNbCountersOfType(g, CounterType.MINING) == 0)
				g.sacrifice(controller, land);
			return 1;
		}
		
		// Grove of the Burnwillows
		p = Pattern.compile(".*each opponent gains 1 life");
		m = p.matcher(manaString);
		if (m.matches()) {
			Player opponent = controller.getOpponent();
			if (opponent != null)
				opponent.gainLife(1);
			return 1;
		}
		
		// For each... (i.e. Gaea's Cradle)
		p = Pattern.compile(" for each (creature|elf|enchantment|artifact) (you control|on the battlefield)");
		m = p.matcher(manaString);
		if (m.matches()) {
			int number;
			Player pl = null;
			String element = m.group(1);
			String where = m.group(2);

			if (element.equals("elf")) {
				number = g.getBattlefield().getNumberOfCreatureTypesControlledBy(CreatureType.Elf, pl);
			}
			else {
				if (where.equals("you control"))
					pl = controller;
				number = g.getBattlefield().getNumberOfControlledBy(CardType.parse(element), pl);
			}
			return number;
		}
		
		// Witch Engine
		p = Pattern.compile(".*gains control of witch engine.*");
		m = p.matcher(manaString);
		if (m.matches()) {
			aa.getSource().setController(g, controller.getOpponent());
			return 1;
		}
		
		// Activate only bla bla bla ... (just ignore)
		p = Pattern.compile(".*activate this ability only.*");
		m = p.matcher(manaString);
		if (m.matches())
			return 1;
		
		System.err.println("DEAL WITH THIS : <" + manaString + ">");
		return 1;
	}
	
	/******** Generic methods used by many card spells and abilities ************/
	/* Add mana */
	public static Response addMana(Game g, StackObject so) {
		String manaString;
		String effectText = so.getDescription().toLowerCase();
		Pattern p = Pattern.compile("^.*add (\\{[wubrgc]\\}.*).$");
		Matcher m = p.matcher(effectText);
		boolean bContinue = true;
		Vector<ManaType> manaCombination;
		
		if (so.getStep() == 1) {
			nbTimes = 1;
			if (m.matches()) {
				manaString = m.group(1);
				
				do {
					manaCombination = new Vector<ManaType>();
					p = Pattern.compile("(\\{[wubrgc]\\})(.*)");
					m = p.matcher(manaString);
					while (m.matches()) {
						manaCombination.add(Mana.parse(m.group(1))); 
						manaString = m.group(2);
						m = p.matcher(manaString);
					}
					
					// there is text after mana symbol(s)
					if (manaString.length() > 0) {
						p = Pattern.compile("(, |or | or )(.*)");
						m = p.matcher(manaString);
						if (m.matches()) {
							manaString = m.group(2);
						}
						else {
							nbTimes = doManaAftermath(g, so, manaString); // this is used to get the number of times mana must be calcuted
							bContinue = false;                                 // for example for Gaea's Cradle
						}
					}
					else {
						bContinue = false;
					}
					if (!manaCombination.isEmpty())
						so.addManaCombination(manaCombination);
				} while (bContinue);
				
			}
			else { // Pentacolor lands
				p = Pattern.compile("^.*add one mana of any color(.*).$");
				m = p.matcher(effectText);
				if (m.matches()) {
					so.setPentaChoice();
					String otherText = m.group(1);
					if (otherText.length() > 0) {
						doManaAftermath(g, so, otherText);
					}
				}
				else {
					System.err.println("Error : " + so.getName() + " : no match for ["+effectText+"]"); // should never get here
				}
			}

			int nbChoices = so.getManaChoices().size();
			
			if (nbChoices == 0) {
				System.err.println("Error in addmana : choices cannot be empty");
				return Response.Error;
			}
			else if (nbChoices == 1) {  // Only one choice : execute it
				so.advanceStep();
			}
			else { // more than one choice available
				so.getController(g).setState(State.WaitChooseManaCombination);
				g.setCurrentManaAbility(so);
				so.advanceStep();
				return Response.MoreStep; // ask for choice
			}
		}
		
		if (so.getStep() == 2) {
			int iChoice = 0;
			
			if (so.getAdditionalData() != null) {
				int number = (int) so.getAdditionalData();
				if ((number < 1) || (number > so.getManaChoices().size()))
					return Response.MoreStep;
				iChoice = number - 1;
			}
			addManaCombination(g, so.getController(g), so.getManaChoice(iChoice), nbTimes);
			
			// If the mana was added by tapping a land, trigger all "Fertile Ground"-like abilities.			
			if (so.getSource().isLand(g)) {
				Card land = so.getSource();
				Vector<TriggeredAbility> abilities;
				for (Card aura : land.getAuras(g)) {
					abilities = aura.getTriggeredAbilities(g, Event.EnchantedLandIsTappedForMana, Origin.BTLFLD);
					g.queueTriggeredManaAbilities(abilities);
				}
			}
		}
		if (g.getNbQueuedTriggeredManaAbilities() == 0) {
			so.getController(g).setState(State.Ready);
			return Response.OKholdPriority;
		}
		else
			return g.doTriggeredManaAbilities();
	}
	
	/**
	 * Adds a mana combination to the player mana pool.
	 * A mana combination is for example what Skycloud Expanse produces : {W}{U}
	 * @param g
	 * @param p
	 * @param combi
	 * @param nbTimes : the number of times the combination must be added. For example Gaea's Cradle, nbTimes = number of
	 * creatures controlled by the player.
	 */
	private static void addManaCombination(Game g, Player p, Vector<ManaType> combi, int nbTimes) {
		for (ManaType mt : combi)
			p.addMana(mt, nbTimes);
	}
	
	/* untapThis */
	public static Response untapThis(Game g, StackObject ta) {
		ta.getSource().untap(g);
		return Response.OK;
	}
	
	/* plusOneCounterOnThis : Put a +1/+1 counter on ~this~ */
	public static Response plusOneCounterOnThis(Game g, StackObject aa) {
		aa.getSource().addCounter(g, CounterType.PLUS_ONE, 1);
		return Response.OK;
	}
	
	/* pump (this gets +1/+1 until end of turn) */
	public static Response pump(Game g, StackObject so) {
		Card card = so.getSource();
		card.addModifiers(new PTModifier(so, "+1/+1", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
		return Response.OK;
	}
	
	/* regrowth */
	public static Response regrowth(Game g, StackObject c) {
		return g.move_GYD_to_HND((Card) c.getTargetObject(0));
	}
	
	/* crew */
	public static Response crew(Game g, StackObject aa) {
		Card vehicle = aa.getSource();
		vehicle.addModifiers(new CardTypeModifier(aa, Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN, CardType.CREATURE));
		return Response.OK;
	}
	
	/* gain2energy */
	public static Response gain2energy(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		controller.addCounter(CounterType.ENERGY, 2);
		return Response.OK;
	}
	
	/* gain1energy */
	public static Response gain1energy(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		controller.addCounter(CounterType.ENERGY, 1);
		return Response.OK;
	}
	
	/* dealXdamageToTarget */
	public static Response dealXdamageToTarget(Game g, StackObject so) {
		Card spell = (Card) so;
		return (spell.dealNonCombatDamageTo(g, (Damageable) so.getTargetObject(0), spell.getXValue()));
	}

	/* deal1damageToTarget */
	public static Response deal1damageToTarget(Game g, StackObject so) {
		return so.getSource().dealNonCombatDamageTo(g, (Damageable) so.getTargetObject(0), 1);
	}
	
	/* deal2damageToTarget */
	public static Response deal2damageToTarget(Game g, StackObject so) {
		return so.getSource().dealNonCombatDamageTo(g, (Damageable) so.getTargetObject(0), 2);
	}
	
	/* deal3damageToTarget */
	public static Response deal3damageToTarget(Game g, StackObject so) {
		return so.getSource().dealNonCombatDamageTo(g, (Damageable) so.getTargetObject(0), 3);
	}
	
	/* pestilence (source deals 1 damage to each creature and player) */
	public static Response pestilence(Game g, StackObject aa) {
		Card source = aa.getSource();
		Player controller = aa.getController(g);
		Player opponent = controller.getOpponent();

		if (aa.getStep() == 1) {
			aa.advanceStep();
			
			// deal damage to creatures
			for (Card creature : g.getBattlefield().getCreatures())
				source.dealDamageTo(g, creature, 1, false);
			
			// deal damage to controller
			source.dealDamageTo(g, controller, 1, false);
			
			// deal damage to opponent
			return source.dealDamageTo(g, opponent, 1, false);
		}
		
		return Response.OK;
	}
	
	/* addVerseCounter (add a VERSE counter to the source (usually an enchantment)) */
	public static Response addVerseCounter(Game g, StackObject ta) {
		ta.getSource().addCounter(g, CounterType.VERSE, 1);
		return Response.OK;
	}
	
	/* returnToHand */
	public static Response returnToHand(Game g, StackObject ta) {
		g.move_GYD_to_HND(ta.getSource());
		return Response.OK;
	}
	
	/* counterTargetSpell */
	public static Response counterTargetSpell(Game g, StackObject spell) {
		g.counter((StackObject) spell.getTargetObject(0));
		return Response.OK;
	}
	
	/* drawACard */
	public static Response drawACard(Game g, StackObject c) {
		Response ret = Response.OK;
		if (c.getStep() == 1) {
			c.advanceStep();
			ret = g.drawCards(c.getController(g), 1);
		}
		return ret;
	}
	
	/* destroyTargetCanRegen */
	public static Response destroyTargetCanRegen(Game g, StackObject c) {
		return g.destroy((Card) c.getTargetObject(0), true);
	}
	
	/* destroyTargetCannotRegen */
	public static Response destroyTargetCannotRegen(Game g, StackObject c) {
		return g.destroy((Card) c.getTargetObject(0), false);
	}
	
	/* tapTarget */
	public static Response tapTarget(Game g, StackObject c) {
		((Card) c.getTargetObject(0)).tap(g);
		return Response.OK;
	}
	
	/******** ! END Generic methods ************/
	
	/* necropotence_exile */
	public static Response necropotence_exile(Game g, StackObject so) {
		Card discardedCard = (Card) so.getAdditionalData();
		g.move_GYD_to_EXL(discardedCard);
		return Response.OK;
	}
	
	/* rishkarPeemaRenegade_counters */
	public static Response rishkarPeemaRenegade_counters(Game g, StackObject ta) {
		Card target;
		
		for (int i = 0; i < 2; i++) {
			if (ta.getTargetObject(i) != null) {
				target = (Card) ta.getTargetObject(i);
				target.addCounter(g, CounterType.PLUS_ONE, 1);
			}
		}
		return Response.OK;
	}
	
	/* verdurousGearhulk */
	public static Response verdurousGearhulk(Game g, StackObject ta) {
		Card target;
		
		for (int i = 0; i < 4; i++) {
			if (ta.getTargetObject(i) != null) {
				target = (Card) ta.getTargetObject(i);
				target.addCounter(g, CounterType.PLUS_ONE, 1);
			}
		}
		return Response.OK;
	}
	
	/* bulwark */
	public static Response bulwark(Game g, StackObject ta) {
		Card bullwark = ta.getSource();
		Player controller = bullwark.getController(g);
		Player opponent = controller.getOpponent();
		int x = controller.getHandSize() - opponent.getHandSize();
		return bullwark.dealNonCombatDamageTo(g, opponent, x);
	}

	/* pestilence_sac */
	public static Response pestilence_sac(Game g, StackObject ta) {
		return g.sacrifice(ta.getController(g), ta.getSource());
	}
	
	/* torchSong_sac */
	public static Response torchSong_sac(Game g, StackObject aa, int unused) {
		Card enchant = aa.getSource();
		int X = enchant.getNbCountersOfType(g, CounterType.VERSE);
		return enchant.dealNonCombatDamageTo(g, (Damageable) aa.getTargetObject(0), X);
	}
	
	/* waylay */
	public static Response waylay(Game g, StackObject aa) {
		Vector<Card> tokens = g.createTokens(Token.WHITE_KNIGHT_22, 3, aa.getSource());
		// Delayed triggered effect that will exile the tokens at the beginning of the next cleanup step.
		ContinuousEffect delayedTrigger = ContinuousEffectFactory.create("waylay_delayed", aa.getSource());
		delayedTrigger.setAdditionalData(tokens);
		g.addContinuousEffect(delayedTrigger);
		return Response.OK;
	}
	
	/* historyOfBenalia_1_2 */
	public static Response historyOfBenalia_1_2(Game g, StackObject ta) {
		g.createSingleToken(Token.WHITE_KNIGHT_22_VIGILANCE, ta.getSource());
		return Response.OK;
	}
	
	/* historyOfBenalia_3 */
	public static Response historyOfBenalia_3(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		Vector<Card> creatures = g.getBattlefield().getCreaturesControlledBy(controller);
		for (Card creature : creatures) {
			if (creature.hasCreatureType(g, CreatureType.Knight))
				creature.addModifiers(new PTModifier(ta, "+2/+1", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
		}
		return Response.OK;
	}
	
	/* theEldestReborn_1 */
	public static Response theEldestReborn_1(Game g, StackObject ta) {
		Player opponent = ta.getController(g).getOpponent();
		int step = ta.getStep();
		
		if (step == 1) {
			// Do nothing if opponent doesn't control any creature or planeswalker
			Vector<Card> creatures = g.getBattlefield().getCreaturesControlledBy(opponent);
			Vector<Card> planeswalkers = g.getBattlefield().getPlaneswalkersControlledBy(opponent);
			if ((creatures.size() + planeswalkers.size()) == 0)
				return Response.OK;
			
			// Prompt opponent to choose a creature to sac
			opponent.setState(Game.State.WaitSacrificeCreatureOrPlaneswalker);
			ta.advanceStep();
			return Response.MoreStep;
		}
		else if (step == 2) {
			if (g.validateChoices())
				g.sacrifice(opponent, (Card) g.getChoices().get(0));
			else
				return Response.MoreStep;
		}
		return Response.OK;
	}
	
	
	/* figureOfDestiny_1 */
	public static Response figureOfDestiny_1(Game g, StackObject aa) {
		Card figure = aa.getSource();
		figure.addModifiers(new CreatureTypeModifier(aa, Modifier.Duration.PERMANENTLY, CreatureType.Kithkin, CreatureType.Spirit),
						    new PTModifier(aa, "2/2", Operation.SET, Modifier.Duration.PERMANENTLY));
		return Response.OK;
	}
	
	/* figureOfDestiny_2 */
	public static Response figureOfDestiny_2(Game g, StackObject aa) {
		Card figure = aa.getSource();
		if (figure.hasCreatureType(g, CreatureType.Spirit))
			figure.addModifiers(new CreatureTypeModifier(aa, Modifier.Duration.PERMANENTLY, CreatureType.Kithkin, CreatureType.Spirit, CreatureType.Warrior),
					            new PTModifier(aa, "4/4", Operation.SET, Modifier.Duration.PERMANENTLY));
		return Response.OK;
	}

	/* figureOfDestiny_3 */
	public static Response figureOfDestiny_3(Game g, StackObject aa) {
		Card figure = aa.getSource();
		if (figure.hasCreatureType(g, CreatureType.Warrior))
			figure.addModifiers(new CreatureTypeModifier(aa, Modifier.Duration.PERMANENTLY, CreatureType.Kithkin, CreatureType.Spirit, CreatureType.Warrior, CreatureType.Avatar),
					            new PTModifier(aa, "8/8", Operation.SET, Modifier.Duration.PERMANENTLY),
					            new EvergreenModifier(aa, Modifier.Duration.PERMANENTLY, Evergreen.FLYING, Evergreen.FIRSTSTRIKE));
		return Response.OK;
	}
	
	/* skitteringSkirge */
	public static Response skitteringSkirge(Game g, StackObject ta) {
		return g.sacrifice(ta.getSource().getController(g), ta.getSource());
	}
	
	/* thespiansStage */
	public static Response thespiansStage(Game g, StackObject aa) {
		Card source = aa.getSource();
		Card target = (Card) aa.getTargetObject(0);
		source.becomesCopyOf(g, target);
		return Response.OK;
	}
	
	/* templeScry */
	public static Response templeScry(Game g, StackObject aa) {
		return scry(g, aa, 1); 
	}
	
	/* veteranMotorist_scry */
	public static Response veteranMotorist_scry(Game g, StackObject aa) {
		return scry(g, aa, 2); 
	}
	
	/* veteranMotorist_pump */
	public static Response veteranMotorist_pump(Game g, StackObject ta) {
		Card vehicle = (Card) ta.getAdditionalData();
		vehicle.addModifiers(new PTModifier(ta, "+1/+1", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
		return Response.OK;
	}
	
	/* toolcraftExemplar */
	public static Response toolcraftExemplar(Game g, StackObject ta) {
		Card creature = ta.getSource();
		creature.addModifiers(new PTModifier(ta, "+2/+1", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
		if (g.getBattlefield().getArtifactsControlledBy(ta.getController(g)).size() >= 3)
			creature.addModifiers(new EvergreenModifier(ta, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.FIRSTSTRIKE));
		return Response.OK;
	}
	
	/* darkDepths_removeCounter */
	public static Response darkDepths_removeCounter(Game g, StackObject aa) {
		Card source = aa.getSource();
		if (source.getNbCountersOfType(g, CounterType.ICE) > 0)
			source.removeCounter(g, CounterType.ICE, 1);
		return Response.OK;
	}
	
	/* darkDepths_putToken */
	public static Response darkDepths_putToken(Game g, StackObject aa) {
		Card source = aa.getSource();
		Response ret = g.sacrifice(source.getController(g), source);
		if (ret == Response.OK)
			g.createSingleToken(Token.BLACK_AVATAR_2020_FLYING_INDES_MARIT, source);
		return Response.OK;
	}
	
	/* sliverQueen */
	public static Response sliverQueen(Game g, StackObject aa) {
		Card source = aa.getSource();
		g.createSingleToken(Token.COLORLESS_SLIVER_11, source);
		return Response.OK;
	}
	
	/* dromosaur */
	public static Response dromosaur(Game g, StackObject ta) {
		ta.getSource().addModifiers(new PTModifier(ta.getSource(), "+2/-2", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
		return Response.OK;
	}
	
	/* necropotence_pseudoDraw */
	public static Response necropotence_pseudoDraw(Game g, StackObject aa) {
		Player controller = aa.getController(g);
		Library lib = controller.getLibrary();
		
		/* Do nothing if library is empty */
		if (lib.size() == 0)
			return Response.OK;
		
		Card topCard = lib.getTopCard();
		g.move_LIB_to_EXL(topCard);
		
		// Delayed triggered effect that will put the card in the hand at the player's next end step
		ContinuousEffect delayedTrigger = ContinuousEffectFactory.create("necropotence_delayedTrigger", aa.getSource());
		delayedTrigger.setAdditionalData(topCard);
		g.addContinuousEffect(delayedTrigger);
		
		return Response.OK;
	}
	
	/* necropotence_putCardInHand */
	public static Response necropotence_putCardInHand(Game g, StackObject ta) {
		Card exiledCard = (Card) ta.getAdditionalData();
		g.move_EXL_to_HND(exiledCard);
		return Response.OK;
	}
	
	/* illusions_gainLife */
	public static Response illusions_gainLife(Game g, StackObject ta) {
		ta.getController(g).gainLife(20);
		return Response.OK;
	}
	
	/* illusions_loseLife */
	public static Response illusions_loseLife(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		controller.loseLife(20);
		return Response.OK;
	}
	
	/* donate */
	public static Response donate(Game g, StackObject spell) {
		Card permanent = (Card) spell.getTargetObject(0);
		permanent.setController(g, (Player) spell.getTargetObject(1));
		return Response.OK;
	}
	
	/* imaginaryPet */
	public static Response imaginaryPet(Game g, StackObject ta) {
		g.move_BFD_to_HND(ta.getSource());
		return Response.OK;
	}
	
	/* retromancer */
	public static Response retromancer(Game g, StackObject so) {
		Card retromancer = so.getSource();
		TriggeredAbility ta = (TriggeredAbility) so;
		StackObject triggeringObject = (StackObject) ta.getAdditionalData();
		return retromancer.dealNonCombatDamageTo(g, triggeringObject.getController(g), 3);
	}
	
	/* reclusiveWight */
	public static Response reclusiveWight(Game g, StackObject ta) {
		return g.sacrifice(ta.getController(g), ta.getSource());
	}
	
	/* hollowDogs */
	public static Response hollowDogs(Game g, StackObject ta) {
		ta.getSource().addModifiers(new PTModifier(ta.getSource(), "+2/+0", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
		return Response.OK;
	}
	
	/* mobileFort */
	public static Response mobileFort(Game g, StackObject ta) {
		Card automaton = ta.getSource();
		automaton.addModifiers(new PTModifier(automaton, "+3/-1", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN),
				               new EvergreenModifier(automaton, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.IGNORE_DEFENDER));
		return Response.OK;
	}
	
	/* vraskasContempt */
	public static Response vraskasContempt(Game g, StackObject spell) {
		Player controller = spell.getController(g);
		Card target = (Card) spell.getTargetObject(0);
		
		g.move_BFD_to_EXL(target);
		controller.gainLife(2);
		return Response.OK;
	}
	
	/* hoppingAutomaton */
	public static Response hoppingAutomaton(Game g, StackObject ta) {
		Card automaton = ta.getSource();
		automaton.addModifiers(new PTModifier(automaton, "-1/-1", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN),
				               new EvergreenModifier(automaton, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.FLYING));
		return Response.OK;
	}
	
	/* karn_pump */
	public static Response karn_pump(Game g, StackObject ta) {
		ta.getSource().addModifiers(new PTModifier(ta.getSource(), "-4/+4", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
		return Response.OK;
	}
	
	/* karn_animate */
	public static Response karn_animate(Game g, StackObject aa) {
		Card target = (Card) aa.getTargetObject(0);
		int ccm = target.getConvertedManaCost(g);
		String pt = String.format("%d/%d", ccm, ccm);
		target.addModifiers(new CardTypeModifier(aa.getSource(), Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN, CardType.CREATURE),
							new PTModifier(aa.getSource(), pt, Operation.SET, Modifier.Duration.UNTIL_END_OF_TURN));
		return Response.OK;
	}

	/* scald_damage */
	public static Response scald_damage(Game g, StackObject ta) {
		Card scald = ta.getSource();
		Card island = (Card) ta.getAdditionalData();
		return scald.dealNonCombatDamageTo(g, island.getController(g), 1);
	}
	
	/* viashinoWeaponsmith */
	public static Response viashinoWeaponsmith(Game g, StackObject ta) {
		ta.getSource().addModifiers(new PTModifier(ta.getSource(), "+2/+2", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
		return Response.OK;
	}
	
	/* manabond */
	public static Response manabond(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		Vector<Card> lands = controller.getHand().getLandCards();
		for (Card land : lands)
			g.move_HND_to_BFD(land);
		controller.discardAllCardsInHand();
		return Response.OK;
	}
	
	/* darkRitual */
	public static Response darkRitual(Game g, StackObject spell) {
		spell.getController(g).addMana(ManaType.BLACK, 3);
		return Response.OK;
	}
	
	/* shivanGorge */
	public static Response shivanGorge(Game g, StackObject aa) {
		Card gorge = aa.getSource();
		Player opponent = gorge.getController(g).getOpponent();
		
		if (aa.getStep() == 1) {
			aa.advanceStep();
			return gorge.dealNonCombatDamageTo(g, opponent, 1);
		}
		
		return Response.OK;
	}
	
	/* rebound_cast */
	public static Response rebound_cast(Game g, StackObject ta) {
		g.setReboundSpell(ta.getSource());
		return Response.OK;
	}
	
	/* suspend_cast_from_exile */
	public static Response suspend_cast_from_exile(Game g, StackObject ta) {
		Card c = ta.getSource();
		Response ret = g.castUsingSuspend(c);
		return ret;
	}
	
	/* orzhovPontiff */
	public static Response orzhovPontiff(Game g, StackObject ta) {
		int mode = ta.getChosenMode();
		Player controller = ta.getController(g);
		Vector<Card> affectedCreatures;
		
		switch(mode) {
		case 1:
			affectedCreatures = g.getBattlefield().getCreaturesControlledBy(controller);
			for (Card crea : affectedCreatures) {
				crea.addModifiers(new PTModifier(ta, "+1/+1", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
			}
			break;
			
		case 2:
			affectedCreatures = g.getBattlefield().getCreaturesControlledBy(controller.getOpponent());
			for (Card crea : affectedCreatures) {
				crea.addModifiers(new PTModifier(ta, "-1/-1", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
			}
			break;
		}
		return Response.OK;
	}
	
	/* tormentorExarch */
	public static Response tormentorExarch(Game g, StackObject ta) {
		int mode = ta.getChosenMode();
		Card targetCreature = (Card) ta.getTargetObject(0);
		
		switch(mode) {
		case 1:
			targetCreature.addModifiers(new PTModifier(ta.getSource(), "+2/+0", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
			break;
			
		case 2:
			targetCreature.addModifiers(new PTModifier(ta.getSource(), "-0/-2", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
			break;
		}
		return Response.OK;
	}
	
	/* healingSalve */
	public static Response healingSalve(Game g, StackObject spell) {
		switch (spell.getChosenMode()) {
		case 1:
			Player target = (Player) spell.getTargetObject(0);
			target.gainLife(3);
			break;
			
		case 2:
			((Damageable) spell.getTargetObject(0)).addDamagePrevention(3);
			break;
		}
		return Response.OK;
	}
	
	/* dromarsCharm */
	public static Response dromarsCharm(Game g, StackObject spell) {
		int mode = spell.getChosenMode();
		switch(mode) {
		case 1:
			spell.getController(g).gainLife(5);
			break;
			
		case 2:
			Card targetSpell = (Card) spell.getTargetObject(0);
			g.counter(targetSpell);
			
		case 3:
			Card targetCreature = (Card) spell.getTargetObject(0);
			targetCreature.addModifiers(new PTModifier(spell.getSource(), "-2/-2", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
			break;
		}
		return Response.OK;
	}
	
	/* blinkmoth_pump */
	public static Response blinkmoth_pump(Game g, StackObject aa) {
		Card target = (Card) aa.getTargetObject(0);
		target.addModifiers(new PTModifier(aa, "+1/+1", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
		return Response.OK;
	}

	/* piaNalaar_pump */
	public static Response piaNalaar_pump(Game g, StackObject aa) {
		Card target = (Card) aa.getTargetObject(0);
		target.addModifiers(new PTModifier(aa, "+1/+0", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
		return Response.OK;
	}
	
	/* piaNalaar_cantBlock */
	public static Response piaNalaar_cantBlock(Game g, StackObject aa) {
		Card target = (Card) aa.getTargetObject(0);
		target.addModifiers(new EvergreenModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.CANTBLOCK));
		return Response.OK;
	}
	
	/* savageKnuckleblade_pump */
	public static Response savageKnuckleblade_pump(Game g, StackObject aa) {
		Card card = aa.getSource();
		card.addModifiers(new PTModifier(aa, "+2/+2", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
		return Response.OK;
	}
	/* savageKnuckleblade_bounce */
	public static Response savageKnuckleblade_bounce(Game g, StackObject aa) {
		Card card = aa.getSource();
		return g.move_BFD_to_HND(card);
	}
	/* savageKnuckleblade_haste */
	public static Response savageKnuckleblade_haste(Game g, StackObject aa) {
		Card card = aa.getSource();
		card.addModifiers(new EvergreenModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.HASTE));
		return Response.OK;
	}
	
	/* werewolf_transform */
	public static Response werewolf_transform(Game g, StackObject aa) {
		Card source = (Card) aa.getSource();
		if (source.transform(g) == null)
			return Response.ErrorNotDoubleFace;
		return Response.OK;
	}
	
	/* emrakulTAT_timeWalk */
	public static Response emrakulTAT_timeWalk(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		g.giveExtraTurn(controller);
		return Response.OK;
	}
	
	/* silentAttendant */
	public static Response silentAttendant(Game g, StackObject ta) {
		(ta.getController(g)).gainLife(1);
		return Response.OK;
	}
	
	
	/* serraAvatar_shuffle */
	public static Response serraAvatar_shuffle(Game g, StackObject ta) {
		Card avatar = ta.getSource();
		if (avatar.isIGY(g)) {
			g.move_GYD_to_TOPLIB(ta.getSource());
			(ta.getSource().getOwner()).shuffle();	
		}
		return Response.OK;
	}
	
	/* emrakulTAT_shuffle */
	public static Response emrakulTAT_shuffle(Game g, StackObject ta) {
		ta.getSource().getOwner().getGraveyard().shuffleIntoLibrary();
		return Response.OK;
	}
	
	/* haunt */
	public static Response haunt(Game g, StackObject ta) {
		Card haunter = ta.getSource();
		Card haunted = (Card) ta.getTargetObject(0);
		
		if (haunter.isIGY(g))
			g.move_GYD_to_EXL(haunter);
		haunter.addLinkedCard(haunted);
		return Response.OK;
	}
	
	/* persist */
	public static Response persist(Game g, StackObject ta) {
		Card creature = ta.getSource();
		if (creature.isIGY(g))
			g.move_GYD_to_BFD(creature);
		creature.addCounter(g, CounterType.MINUS_ONE, 1);
		return Response.OK;
	}
	
	/* modular_moveCounters */
	public static Response modular_moveCounters(Game g, StackObject ta) {
		Card source = ta.getSource();
		Card target = (Card) ta.getTargetObject(0);
		int nbCounters = source.getNbCountersOfType(g, CounterType.PLUS_ONE);
		target.addCounter(g, CounterType.PLUS_ONE, nbCounters);
		return Response.OK;
	}
	
	/* steelOverseer */
	public static Response steelOverseer(Game g, StackObject aa) {
		Player controller = aa.getController(g);
		Vector<Card> creatures = g.getBattlefield().getCreaturesControlledBy(controller);
		for (Card creature : creatures) {
			if (creature.isArtifact(g))
				creature.addCounter(g, CounterType.PLUS_ONE, 1);
		}
		return Response.OK;
	}
	
	/* voltaicBrawler_pump */
	public static Response voltaicBrawler_pump(Game g, StackObject ta) {
		Card brawler = ta.getSource();
		Player controller = brawler.getController(g);
		
		// immediately return if the player does not have at least one energy.
		if (controller.getNbCounters(CounterType.ENERGY) < 1)
			return Response.OK;
		
		controller.payEnergy(1);
		brawler.addModifiers(new PTModifier(ta, "+1/+1", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN),
				        new EvergreenModifier(ta, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.TRAMPLE));
		return Response.OK;
	}
	
	/* spikes_transfer */
	public static Response spikes_transfer(Game g, StackObject aa) {
		Card target = (Card) aa.getTargetObject(0);
		target.addCounter(g, CounterType.PLUS_ONE, 1);
		return Response.OK;
	}
	
	/* spikeFeeder */
	public static Response spikeFeeder(Game g, StackObject aa) {
		Card spike = (Card) aa.getSource();
		spike.getController(g).gainLife(2);
		return Response.OK;
	}
	
	/* gavonyTownship */
	public static Response gavonyTownship(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		Vector<Card> myCreatures = g.getBattlefield().getCreaturesControlledBy(controller);
		for (Card myCreature : myCreatures)
			myCreature.addCounter(g, CounterType.PLUS_ONE, 1);
		return Response.OK;
	}
	
	/* kalitas_sac */
	public static Response kalitas_sac(Game g, StackObject aa) {
		Card kalitas = aa.getSource();
		kalitas.addCounter(g, CounterType.PLUS_ONE, 2);
		return Response.OK;
	}
	
	/* archangelOfThune */
	public static Response archangelOfThune(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		Vector<Card> myCreatures = g.getBattlefield().getCreaturesControlledBy(controller);
		for (Card myCreature : myCreatures)
			myCreature.addCounter(g, CounterType.PLUS_ONE, 1);
		return Response.OK;
	}
	
	/* illGottenGains */
	public static Response illGottenGains(Game g, StackObject spell) {
		Player controller = spell.getController(g);
		Player opponent = controller.getOpponent();
		
		if (spell.getStep() == 1) {
			spell.advanceStep();
			controller.discardAllCardsInHand();
			opponent.discardAllCardsInHand();
			controller.setState(State.WaitchoiceCardInGraveyard);
			return Response.MoreStep;
		}
		
		if (spell.getStep() == 2 || spell.getStep() == 3 || spell.getStep() == 4) {
			if (g.validateChoices())
			{
				if (g.getChoices().size() == 0)  // player 1 clicked Done. prompt player 2
					spell.goToStep(10);
				else {
					g.move_GYD_to_HND((Card) g.getChoices().get(0));
					if (spell.getStep() < 4) {
						spell.advanceStep();
						return Response.MoreStep;
					}
					else
						spell.goToStep(10);
				}
			}
			else
				return Response.MoreStep;
		}
		
		if (spell.getStep() == 10) {
			spell.advanceStep();
			opponent.setState(State.WaitchoiceCardInGraveyard);
			return Response.MoreStep;
		}
		
		if (spell.getStep() == 11 || spell.getStep() == 12 || spell.getStep() == 13) {
			if (g.validateChoices()) {
				if (g.getChoices().size() == 0)  // player 2 clicked Done.
					spell.goToStep(20);
				else {
					g.move_GYD_to_HND((Card) g.getChoices().get(0));
					if (spell.getStep() < 13) {
						spell.advanceStep();
						return Response.MoreStep;
					}
					else
						spell.goToStep(20);
				}
			}
			else
				return Response.MoreStep;
		}
		
		if (spell.getStep() == 20)
			if (spell instanceof Card && !((Card) spell).isCopy())
				g.move_STK_to_EXL((Card) spell);
		return Response.OK;
	}
	
	/* meltdown */
	public static Response meltdown(Game g, StackObject spell) {
		Card card;
		int i = 0;
		int xValue = spell.getXValue();
		Zone bf = g.getBattlefield();
		Vector<Card> list = new Vector<Card>();
		
		// Mark permanents that need to be destroyed
		while (i < bf.size()) {
			card = (Card) bf.getObjectAt(i);
			if (card.isArtifact(g) && (card.getConvertedManaCost(g) <= xValue))
				list.add(card);
			i++;
		}
		
		// Destroy the marked permanents
		for (Card item : list)
			g.destroy(item, true);	
		return Response.OK;
	}
	
	/* hush */
	public static Response hush(Game g, StackObject spell) {
		Card card;
		int i = 0;
		Zone bf = g.getBattlefield();
		Vector<Card> list = new Vector<Card>();
		
		// Mark permanents that need to be destroyed
		while (i < bf.size()) {
			card = (Card) bf.getObjectAt(i);
			if (card.isEnchantment(g))
				list.add(card);
			i++;
		}
		
		// Destroy the marked permanents
		for (Card item : list)
			g.destroy(item, true);	
		return Response.OK;
	}
	
	/* armageddon */
	public static Response armageddon(Game g, StackObject spell) {
		Card card;
		int i = 0;
		Zone bf = g.getBattlefield();
		Vector<Card> list = new Vector<Card>();
		
		// Mark permanents that need to be destroyed
		while (i < bf.size()) {
			card = (Card) bf.getObjectAt(i);
			if (card.isLand(g))
				list.add(card);
			i++;
		}
		
		// Destroy the marked permanents
		for (Card item : list)
			g.destroy(item, true);	
		return Response.OK;
	}
	
	/* stinkweedImp */
	public static Response stinkweedImp(Game g, StackObject ta) {
		Card damagedCreature = (Card) ((TriggeredAbility) ta).getAdditionalData();
		return g.destroy(damagedCreature, true);
	}
	
	/* unlicensedDisintegration */
	public static Response unlicensedDisintegration(Game g, StackObject spell) {
		Card target = (Card) spell.getTargetObject(0);
		Player controller = spell.getController(g);
		
		if (spell.getStep() == 1) {
			spell.advanceStep();
			g.destroy(target, true);
			if (controller.controlsAnArtifact())
				return spell.getSource().dealNonCombatDamageTo(g, target.getController(g), 3);
		}
		return Response.OK;
	}
	
	/* whirlwind */
	public static Response whirlwind(Game g, StackObject c) {
		Card card;
		int i = 0;
		Zone bf = g.getBattlefield();
		Vector<Card> list = new Vector<Card>();
		
		// Mark cards that will be destroyed
		while (i < bf.size()) {
			card = (Card) bf.getObjectAt(i);
			if (card.isCreature(g) && card.hasEvergreenGlobal(Evergreen.FLYING, g))
				list.add(card);
			i++;
		}
		
		// Destroy marked cards
		for (Card item : list)
			g.destroy(item, true);
		return Response.OK;
	}

	/* wrathOfGod */
	public static Response wrathOfGod(Game g, StackObject c) {
		Card card;
		int i = 0;
		Zone bf = g.getBattlefield();
		Vector<Card> list = new Vector<Card>();
		
		// Mark cards that will be destroyed
		while (i < bf.size()) {
			card = (Card) bf.getObjectAt(i);
			if (card.isCreature(g))
				list.add(card);
			i++;
		}
		
		// Destroy marked cards
		for (Card item : list)
			g.destroy(item, false);
		return Response.OK;
	}
	
	/* ritualOfSoot */
	public static Response ritualOfSoot(Game g, StackObject c) {
		Card card;
		int i = 0;
		Zone bf = g.getBattlefield();
		Vector<Card> list = new Vector<Card>();
		
		// Mark cards that will be destroyed
		while (i < bf.size()) {
			card = (Card) bf.getObjectAt(i);
			if (card.isCreature(g) && (card.getConvertedManaCost(g) <= 3))
				list.add(card);
			i++;
		}
		
		// Destroy marked cards
		for (Card item : list)
			g.destroy(item, true);
		return Response.OK;
	}
	
	/* languish */
	public static Response languish(Game g, StackObject c) {
		for (Card creature : g.getBattlefield().getCreatures()) {
			creature.addModifiers(new PTModifier(c, "-4/-4", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
		}
		return Response.OK;
	}	
	
	/* perniciousDeed */
	public static Response perniciousDeed(Game g, StackObject aa) {
		int xValue = aa.getXValue();
		Vector<Card> permanents = g.getBattlefield().getPermanents();
		for (Card permanent : permanents) {
			if ((permanent.isArtifact(g) || permanent.isCreature(g) || permanent.isEnchantment(g)) && (permanent.getConvertedManaCost(g) <= xValue))
				g.destroy(permanent, true);
		}
		return Response.OK;
	}
	
	/* cinderElemental */
	public static Response cinderElemental(Game g, StackObject aa) {
		Response ret;
		Card source = aa.getSource();
		MtgObject target = aa.getTargetObject(0);
		int Xvalue = aa.getXValue();
		ret = source.dealNonCombatDamageTo(g, (Damageable) target, Xvalue);
		return ret;
	}
	
	/* jitte_dealsDamage (triggered ability) */
	public static Response jitte_dealsDamage(Game g, StackObject ta) {
		Card jitte = ta.getSource();
		jitte.addCounter(g, CounterType.CHARGE, 2);
		return Response.OK;
	}
	
	/* jitte_activation (activated ability) */
	public static Response jitte_activation(Game g, StackObject aa) {
		Card jitte = aa.getSource();
		
		switch(aa.getChosenMode()) {
		case 1:
			Card equippedCreature = jitte.getHost();
			if (equippedCreature != null)
				equippedCreature.addModifiers(new PTModifier(jitte, "+2/+2", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
			break;
			
		case 2:
			Card target = (Card) aa.getTargetObject(0);
			target.addModifiers(new PTModifier(jitte, "-1/-1", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
			break;
			
		case 3:
			jitte.getController(g).gainLife(2);
			break;
		}
		return Response.OK;
	}
	
	/* swordBodyAndMind */
	public static Response swordBodyAndMind(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		Player opponent = controller.getOpponent();

		g.createSingleToken(Token.GREEN_WOLF_22, ta);
		g.mill(opponent, 10);
		return Response.OK;
	}
	
	/* thoughtScour */
	public static Response thoughtScour(Game g, StackObject spell) {
		Response ret = Response.OK;

		if (spell.getStep() == 1) {
			g.mill((Player) spell.getTargetObject(0), 2);
			spell.advanceStep();
			ret = g.drawCards(spell.getController(g), 1);
		}
		return ret;
	}
	
	/* urzasRage */
	public static Response urzasRage(Game g, StackObject spell) {
		Card rage = (Card) spell;
		MtgObject target = rage.getTargetObject(0);
		boolean bKicked = (rage.getSpellCastUsed().getOption() == Option.CAST_WITH_KICKER);
		int damage = 3;
		if (bKicked)
			damage = 10;
		return rage.dealNonCombatDamageTo(g, (Damageable) target, damage);
	}
	
	/* propheticBolt */
	public static Response propheticBolt(Game g, StackObject spell) {
		Response ret = Response.OK;
		MtgObject target = spell.getTargetObject(0);
		Card source = (Card) spell;

		if (spell.getStep() == 1) {
			spell.advanceStep();
			ret = source.dealNonCombatDamageTo(g, (Damageable) target, 4);
			if (ret != Response.MoreStep)
				ret = impulse(g, spell);
			else // damage has been redirected to a PW
				spell.goToStep(2);
		}
		else 
			ret = impulse(g, spell);
		return ret;
	}
	
	/* zap */
	public static Response zap(Game g, StackObject spell) {
		Response ret = Response.OK;
		MtgObject target = spell.getTargetObject(0);
		Card source = (Card) spell;
		Player controller = spell.getController(g);

		if (spell.getStep() == 1) {
			spell.advanceStep();
			ret = source.dealNonCombatDamageTo(g, (Damageable) target, 1);
			if (ret != Response.MoreStep) {
				spell.advanceStep();
				ret = g.drawCards(controller, 1);
			}
		}
		else if (spell.getStep() == 2) {
			spell.advanceStep();
			ret = g.drawCards(controller, 1);
		}
		return ret;
	}
	
	/* swordFireAndIce */
	public static Response swordFireAndIce(Game g, StackObject ta) {
		Response ret = Response.OK;;
		MtgObject target = ta.getTargetObject(0);
		Card source = ta.getSource();
		Player controller = ta.getController(g);
		
		if (ta.getStep() == 1) {
			ta.advanceStep();
			ret = source.dealNonCombatDamageTo(g, (Damageable) target, 2);
			if (ret == Response.MoreStep) {
				return ret;
			}
		}
		
		if  (ta.getStep() == 2) {
			ta.advanceStep();
			ret = g.drawCards(controller, 1);
		}
		return ret;
	}
	
	/* swordFeastAndFamine */
	public static Response swordFeastAndFamine(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		Player defendingPlayer = controller.getOpponent();
		
		if (ta.getStep() == 1) {
			defendingPlayer.setState(State.WaitDiscard);
			ta.advanceStep();
			return Response.MoreStep;
		}
		else { // if (step == 2) {
			if (g.validateChoices()) {
				Vector<MtgObject> choices = g.getChoices();
				if (choices.size() > 0)
					g.discard((Card) choices.get(0));	
				controller.setState(State.Ready);
				for (Card land : g.getBattlefield().getLandsControlledBy(controller)) {
					land.untap(g);
				}
				
				return Response.OK;
			}
			else
				return Response.MoreStep;
		}
	}
	
	/* energyField_sac */
	public static Response energyField_sac(Game g, StackObject so) {
		return g.sacrifice(so.getController(g), so.getSource());
	}
	
	/* planarVoid_exile */
	public static Response planarVoid_exile(Game g, StackObject ta) {
		Card triggeringCard = (Card) ta.getAdditionalData();
		g.move_GYD_to_EXL(triggeringCard);
		return Response.OK;
	}
	
	/* liability_loseLife */
	public static Response liability(Game g, StackObject so) {
		TriggeredAbility ta = (TriggeredAbility) so;
		Card triggeringPermanent = (Card) ta.getAdditionalData();
		triggeringPermanent.getController(g).loseLife(1);
		return Response.OK;
	}
	
	/* lifeline_trigger */
	public static Response lifeline_trigger(Game g, StackObject ta) {
		Card dyingCreature = (Card) ta.getAdditionalData();
		ContinuousEffect delayedTrigger = ContinuousEffectFactory.create("lifeline_delayed", ta.getSource(), dyingCreature);
		g.addContinuousEffect(delayedTrigger);
		return Response.OK;
	}
	
	/* lifeline_reanimate */
	public static Response lifeline_reanimate(Game g, StackObject ta) {
		Card dyingCreature = (Card) ta.getAdditionalData();
		g.move_GYD_to_BFD(dyingCreature);
		return Response.OK;
	}
	
	/* antagonism */
	public static Response antagonism(Game g, StackObject ta) {
		Card antagonism = ta.getSource();
		Player activePlayer = g.getActivePlayer();
		Player controller = ta.getController(g);
		Player opponent = controller.getOpponent();
		
		if (activePlayer == controller) {
			if (!opponent.wasDealtDamageThisTurn())
				antagonism.dealNonCombatDamageTo(g, controller, 2);
			return Response.OK;
		}
		
		if (activePlayer == opponent) {
			if (!controller.wasDealtDamageThisTurn())
				return antagonism.dealNonCombatDamageTo(g, opponent, 2);
			else
				return Response.OK;
		}
		
		// Should never get here
		return Response.Error;
	}
	
	/* angelicChorus_gainLife */
	public static Response angelicChorus_gainLife(Game g, StackObject so) {
		TriggeredAbility ta = (TriggeredAbility) so;
		Card triggeringCreature = (Card) ta.getAdditionalData();
		ta.getController(g).gainLife(triggeringCreature.getToughness(g));
		return Response.OK;
	}
	
	/* bereavement_discard */
	public static Response bereavement_discard(Game g, StackObject so) {
		TriggeredAbility ta = (TriggeredAbility) so;
		Card triggeringCreature = (Card) ta.getAdditionalData();
		Player controller = triggeringCreature.getController(g);
		
		if (controller.getHandSize() == 0)
			return Response.OK;
		
		if (so.getStep() == 1) {
			so.advanceStep();
			controller.setState(State.WaitDiscard);
			return Response.MoreStep;
		}
		
		if (so.getStep() == 2) {
			if (g.validateChoices())
				g.discard((Card) g.getChoices().get(0));
			else
				return Response.MoreStep;
		}
		
		return Response.OK;
	}
	
	/* fecundity_draw */
	public static Response fecundity_draw(Game g, StackObject so) {
		Response ret = Response.OK;
		TriggeredAbility ta = (TriggeredAbility) so;
		Card triggeringCreature = (Card) ta.getAdditionalData();
		
		if (so.getStep() == 1) {
			so.advanceStep();
			ret = g.drawCards(triggeringCreature.getController(g), 1);
		}
		return ret;
	}
	
	/* sporogenesis_removeCounters */
	public static Response sporogenesis_removeCounters(Game g, StackObject ta) {
		Vector<Card> creatures = g.getBattlefield().getCreatures();
		for (Card creature : creatures)
			creature.removeCounter(g, CounterType.FUNGUS, creature.getNbCountersOfType(g, CounterType.FUNGUS));
		return Response.OK;
	}
	
	/* sporogenesis_makeToken */
	public static Response sporogenesis_makeToken(Game g, StackObject so) {
		Response ret = Response.OK;
		TriggeredAbility ta = (TriggeredAbility) so;
		Card triggeringCreature = (Card) ta.getAdditionalData();
		
		if (so.getStep() == 1) {
			so.advanceStep();
			int nbFungi = triggeringCreature.getNbCountersOfType(g, CounterType.FUNGUS);
			g.createTokens(Token.GREEN_SAPROLING_11, nbFungi, so);
		}
		return ret;
	}
	
	/* tolarianWinds */
	public static Game.Response tolarianWinds(Game g, StackObject spell) {
		Player controller = spell.getController(g);
		int nbCards = controller.getHandSize();
		
		if (spell.getStep() == 1) {
			controller.discardAllCardsInHand();
			spell.advanceStep();
			return g.drawCards(controller, nbCards);
		}
		return Response.OK;
	}
	
	/* corrupt */
	public static Response corrupt(Game g, StackObject spell) {
		Response ret;
		Player controller = spell.getController(g);
		
		if (spell.getStep() == 1) {
			Card source = (Card) spell;
			int nbSwamps = 0;
			Vector<Card> permanents = g.getBattlefield().getPermanentsControlledBy(controller);
			for (Card permanent : permanents)
				if (permanent.hasSubtypeGlobal(g, Subtype.SWAMP))
					nbSwamps++;
			ret = source.dealNonCombatDamageTo(g, (Damageable) spell.getTargetObject(0), nbSwamps);
			spell.advanceStep();
			if (ret == Response.MoreStep) {
				return Response.MoreStep;
			}
		}
		
		if (spell.getStep() == 2)
			controller.gainLife(g._lastDamageDealt);
		return Response.OK;
	}
	
	/* catastrophe */
	public static Response catastrophe(Game g, StackObject spell) {
		Player controller = spell.getController(g);
		int choice = spell.getXValue();
		
		// Prompt for the type of permanent to destroy (creature or land)
		if (spell.getStep() == 1) {
			controller.setState(State.PromptCatastrophe_PermanentType);
			spell.advanceStep();
			return Response.MoreStep;
		}
		
		// Controller chose a permanent type. Destroy accordingly
		if (spell.getStep() == 2) {
			if ((choice == 1) || (choice == 2)) {
				switch (choice) {
				case 1: // destroy lands
					armageddon(g, spell);
					break;
					
				case 2: // destroy creatures, cannot regen
					wrathOfGod(g, spell);
					break;
				}
				return Response.OK;
			}
			else
				return Response.MoreStep;
		}
		return Response.OK;
	}
	
	/* turnabout */
	public static Response turnabout(Game g, StackObject spell) {
		Player controller = spell.getController(g);
		Player target = (Player) spell.getTargetObject(0);
		int choice = spell.getXValue();
		
		// Prompt for the type of permanent (artifact, creature or land)
		if (spell.getStep() == 1) {
			controller.setState(State.WaitChoiceTurnabout);
			spell.advanceStep();
			return Response.MoreStep;
		}
		
		// Controller chose a permanent type. Check answer and prompt for Tap/Untap
		if (spell.getStep() == 2) {
			if ((choice == 1) || (choice == 2) || (choice == 3)) {
				controller.setState(State.PromptTurnabout_doYouWantToTap);
				spell.advanceStep();	
			}
			return Response.MoreStep;
		}
		
		// Controller chose Tap or Untap
		if (spell.getStep() == 3) {
			Vector<Card> affectedPermanents = null;
			switch(choice) {
			case 1: // artifacts
				affectedPermanents = g.getBattlefield().getArtifactsControlledBy(target);
				break;
				
			case 2: // creatures
				affectedPermanents = g.getBattlefield().getCreaturesControlledBy(target);
				break;
				
			case 3: // lands
				affectedPermanents = g.getBattlefield().getLandsControlledBy(target);
				break;
			}
			
			if (g.getAnswer() == Answer.Yes) {
				// tap
				for (Card permanent : affectedPermanents) {
					if (!permanent.isTapped())
						permanent.tap(g);
				}
			}
			else {
				// untap
				for (Card permanent : affectedPermanents) {
					if (permanent.isTapped())
						permanent.untap(g);
				}
			}
		}
		return Response.OK;
	}
	
	/* catalog */
	public static Response catalog(Game g, StackObject spell) {
		Response ret = Response.OK;
		Player controller = spell.getController(g);
		
		if (spell.getStep() == 1) {
			spell.advanceStep();
			ret = g.drawCards(controller, 2);
		}
		
		if (spell.getStep() == 2) {
			spell.advanceStep();
			controller.setState(State.WaitDiscard);
			return Response.MoreStep;
		}
		
		if (spell.getStep() == 3) {
			if (g.validateChoices()) {
				Card chosenCard = (Card) g.getChoices().get(0);
				g.discard(chosenCard);
				ret = Response.OK;
			}
			else
				return Response.MoreStep;
		}
		return ret;
	}
	
	
	/* carefulStudy */
	public static Game.Response carefulStudy(Game g, StackObject spell) {
		Response ret = Response.OK;
		Player controller = spell.getController(g);
		
		if (spell.getStep() == 1) {
			spell.advanceStep();
			ret = g.drawCards(controller, 2);
		}
		
		if (spell.getStep() == 2) {
			spell.advanceStep();
			controller.setState(State.WaitDiscard);
			return Response.MoreStep;
		}
		
		if ((spell.getStep() == 3) || (spell.getStep() == 4)) {
			if (g.validateChoices()) {
				Card chosenCard = (Card) g.getChoices().get(0);
				g.discard(chosenCard);

				if (spell.getStep() == 3) {
					spell.advanceStep();
					ret = Response.MoreStep;
				}
				else
					ret = Response.OK;
			}
			else
				return Response.MoreStep;
		}
		return ret;
	}
	
	/* congregate */
	public static Response congregate(Game g, StackObject spell) {
		((Player) spell.getTargetObject(0)).gainLife(g.getBattlefield().getCreatures().size() * 2);;
		return Response.OK;
	}
	
	/* brainstorm */
	public static Response brainstorm(Game g, StackObject spell) {
		Response ret = Response.OK;
		Player controller = spell.getController(g);
		int step = spell.getStep();
		
		if (step == 1) {
			spell.advanceStep();
			ret = g.drawCards(controller, 3);
		}
		
		if (step == 2) {
			spell.advanceStep();
			controller.setState(State.WaitBrainstorm);
			return Response.MoreStep;
		}
		
		if ((step == 3) || (step == 4)) {
			if (g.validateChoices()) {
				Card chosenCard = (Card) g.getChoices().get(0);
				g.move_HND_to_TOPLIB(chosenCard);

				if (step == 3) {
					spell.advanceStep();
					ret = Response.MoreStep;
				}
				else
					ret = Response.OK;
			}
			else
				return Response.MoreStep;
		}
		return ret;
	}
	
	/* ancestralRecall */
	public static Game.Response ancestralRecall(Game g, StackObject c) {
		Response ret = Response.OK;
		Player target = (Player) c.getTarget(0).getObject();
		if (c.getStep() == 1) {
			c.advanceStep();
			ret = g.drawCards(target, 3);
		}
		return ret;
	}
	
	/* inspiration */
	public static Game.Response inspiration(Game g, StackObject c) {
		Response ret = Response.OK;
		Player target = (Player) c.getTarget(0).getObject();
		if (c.getStep() == 1) {
			c.advanceStep();
			ret = g.drawCards(target, 2);
		}
		return ret;
	}
	
	/* opportunity */
	public static Game.Response opportunity(Game g, StackObject c) {
		Response ret = Response.OK;
		Player target = (Player) c.getTarget(0).getObject();
		if (c.getStep() == 1) {
			c.advanceStep();
			ret = g.drawCards(target, 4);
		}
		return ret;
	}
	
	/* meditate */
	public static Game.Response meditate(Game g, StackObject spell) {
		Response ret = Response.OK;
		Player controller = spell.getController(g);
		if (spell.getStep() == 1) {
			spell.advanceStep();
			ret = g.drawCards(controller, 4);
		}
		
		if (spell.getStep() == 2) {
			g.giveExtraTurn(controller.getOpponent());
			return Response.OK;
		}
		return ret;
	}
	
	/* accumulatedKnowledge */
	public static Game.Response accumulatedKnowledge(Game g, StackObject c) {
		Response ret = Response.OK;
		int nbAccu = g.countCardsInAllGraveyardsWithName("Accumulated Knowledge");
		if (c.getStep() == 1) {
			c.advanceStep();
			ret = g.drawCards(c.getController(g), 1 + nbAccu);
		}
		return ret;
	}
	
	/* vampireHexmage */
	public static Response vampireHexmage(Game g, StackObject aa) {
		Card target = (Card) aa.getTargetObject(0);
		target.removeAllCounters(g);
		return Response.OK;
	}
	
	/* darkConfidant */
	public static Response darkConfidant(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		Library lib = controller.getLibrary();
		
		// Library is empty
		if (lib.isEmpty())
			return Response.OK;
		
		Card topCard = controller.getLibrary().getTopCard();
		g.move_LIB_to_HND(topCard);
		controller.loseLife(topCard.getConvertedManaCost(g));
		return Response.OK;
	}
	
	/* Bitterblossom */
	public static Response bitterblossom(Game g, StackObject c) {
		c.getController(g).loseLife(1);
		g.createSingleToken(Token.BLACK_FAERIE_ROGUE_11_FLYING, c);
		return Response.OK;
	}
		
	/* Twister */
//	public static Response timeTwister(Game g, StackObject c) {
//		for (Player p : g.getPlayers()) {
//			Vector<MtgObject> objects = p.getHand().getObjects();
//			
//			// Put all cards in hand on top of library
//			while (objects.size() > 0)
//				g.move_HND_to_TOPLIB((Card) objects.get(0));
//			
//			// Put all cards in graveyard on top of library
//			objects = p.getGraveyard().getObjects();
//			while (objects.size() > 0)
//				g.move_GYD_to_TOPLIB((Card) objects.get(0));
//		
//			// Shuffle and draw 7
//			p.shuffle();
//			p.draw(7);
//		}
//		return Response.OK;
//	}
	
	/* geistOfSaintTraft */
	public static Response geistOfSaintTraft(Game g, StackObject ta) {
		Card angel = g.createSingleToken(Token.WHITE_ANGEL_44_FLYING, ta.getSource());
		
		// Delayed triggered effect that will exile the token at end of combat
		ContinuousEffect delayedTrigger = ContinuousEffectFactory.create("geistOfSaintTraft", ta.getSource());
		delayedTrigger.setAdditionalData(angel);
		g.addContinuousEffect(delayedTrigger);
		
		angel.tap(g);
		return g.setAttackingCreature(angel);
	}
	
	/* acidicSoil */
	public static Response acidicSoil(Game g, StackObject spell) {
		Player controller = spell.getController(g);
		Player opponent = controller.getOpponent();
		Card c = (Card) spell;
		c.dealNonCombatDamageTo(g, controller, controller.getNbLandsControlled());
		return c.dealNonCombatDamageTo(g, opponent, opponent.getNbLandsControlled());
	}
	
	/* noeticScales */
	public static Response noeticScales(Game g, StackObject ta) {
		Player activePlayer = g.getActivePlayer();
		int handSize = activePlayer.getHandSize();
		Vector<Card> flag = new Vector<Card>();
		
		// 1. Flag creatures to be returned to hand
		for (Card creature : g.getBattlefield().getCreaturesControlledBy(activePlayer)) {
			if (creature.getPower(g) > handSize)
				flag.add(creature);
		}
		
		// 2. Return the flagged creatures
		for (Card flagged : flag)
			g.move_BFD_to_HND(flagged);
		
		return Response.OK;
	}
	
	/* arcLightning */
	public static Response arcLightning(Game g, StackObject spell) {
		Card source = (Card) spell;
		int nbTargets = spell.getNbTarget();
		Response ret;
		
		switch (nbTargets) {
		// 3 damage to a single target
		case 1:
			return source.dealNonCombatDamageTo(g, (Damageable) spell.getTargetObject(0), 3);

		// 2 targets : 1 damage to the first target and 2 damage to the second target 
		case 2:
			if (spell.getStep() == 1) {
				ret = source.dealNonCombatDamageTo(g, (Damageable) spell.getTargetObject(0), 1);
				if (ret != Response.MoreStep)
					return source.dealNonCombatDamageTo(g, (Damageable) spell.getTargetObject(1), 2);
				else {
					spell.advanceStep();
					return Response.MoreStep;
				}
			}
			else if (spell.getStep() == 2) // this step necessary if the first target was a player controlling a PW
				return source.dealNonCombatDamageTo(g, (Damageable) spell.getTargetObject(1), 2);
		
		// 3 targets : 1 damage to each of the three targets
		case 3:
			if (spell.getStep() == 1) {
				ret = source.dealNonCombatDamageTo(g, (Damageable) spell.getTargetObject(0), 1);
				spell.advanceStep();
				if (ret == Response.MoreStep) // target = player with a PW
					return Response.MoreStep;
			}
			
			if (spell.getStep() == 2) {
				ret = source.dealNonCombatDamageTo(g, (Damageable) spell.getTargetObject(1), 1);
				spell.advanceStep();
				if (ret == Response.MoreStep) // target = player with a PW
					return Response.MoreStep;
			}
			
			if (spell.getStep() == 3) {
				spell.advanceStep();
				return source.dealNonCombatDamageTo(g, (Damageable) spell.getTargetObject(2), 1);
			}
		}
		return Response.OK;
	}
	
	/* geistOfSaintTraft_exile */
	public static Response geistOfSaintTraft_exile(Game g, StackObject so) {
		TriggeredAbility ta = (TriggeredAbility) so;
		Card angel = (Card) ta.getAdditionalData();
		return g.move_BFD_to_EXL(angel);
	}
	
	/* flickerwisp_return */
	public static Response flickerwisp_return(Game g, StackObject so) {
		TriggeredAbility ta = (TriggeredAbility) so;
		Card exiled = (Card) ta.getAdditionalData();
		return g.move_EXL_to_BFD(exiled);
	}
	
	/* waylay_exile */
	public static Response waylay_exile(Game g, StackObject so) {
		TriggeredAbility ta = (TriggeredAbility) so;
		@SuppressWarnings("unchecked")
		Vector<Card> elemTokens = (Vector<Card>) ta.getAdditionalData();
		for (Card token : elemTokens)
			g.move_BFD_to_EXL(token);
		return Response.OK;
	}
	
	/* sneakAttack_sacCreature */
	public static Response sneakAttack_sacCreature(Game g, StackObject so) {
		TriggeredAbility ta = (TriggeredAbility) so;
		Card creature = (Card) ta.getAdditionalData();
		g.sacrifice(so.getController(g), creature);
		return Response.OK;
	}
	
	/* chandraF_exileTokens */
	public static Response chandraF_exileTokens(Game g, StackObject so) {
		TriggeredAbility ta = (TriggeredAbility) so;
		@SuppressWarnings("unchecked")
		Vector<Card> elemTokens = (Vector<Card>) ta.getAdditionalData();
		for (Card token : elemTokens)
			g.move_BFD_to_EXL(token);
		return Response.OK;
	}
	
	/* nahiriUltimate_bounce */
	public static Response nahiriUltimate_bounce(Game g, StackObject so) {
		TriggeredAbility ta = (TriggeredAbility) so;
		Card chosenArtifactOrCreature = (Card) ta.getAdditionalData();
		return g.move_BFD_to_HND(chosenArtifactOrCreature);
	}

	/* sunder */
	public static Response sunder(Game g, StackObject c) {
		Vector<Card> lands = new Vector<Card>();
		lands.addAll(g.getBattlefield().getLands());
		for (Card land : lands)
			g.move_BFD_to_HND(land);
		return Response.OK;
	}
	
	/* Upheaval */
	public static Response upheaval(Game g, StackObject c) {
		Vector<MtgObject> battlefield = g.getBattlefield().getObjects();
		while (battlefield.size() > 0)
			g.move_BFD_to_HND((Card) battlefield.get(0));
		return Response.OK;
	}
	
	/* metrognome */
	public static Response metrognome(Game g, StackObject so) {
		g.createSingleToken(Token.ARTIFACT_GNOME_11, so);
		return Response.OK;
	}

	/* metrognome_discarded */
	public static Response metrognome_discarded(Game g, StackObject so) {
		g.createTokens(Token.ARTIFACT_GNOME_11, 4, so);
		return Response.OK;
	}

	/* hangarbackWalker_dies */
	public static Response hangarbackWalker_dies(Game g, StackObject so) {
		Card source = so.getSource();
		int nbCounters = source.getNbCountersOfType(g, CounterType.PLUS_ONE);
		g.createTokens(Token.ARTIFACT_THOPTER_11_FLYING, nbCounters, so);
		return Response.OK;
	}

	/* piaNalaar_makeToken */
	public static Response piaNalaar_makeToken(Game g, StackObject ta) {
		Card pia = ta.getSource();
		g.createSingleToken(Token.ARTIFACT_THOPTER_11_FLYING, pia);
		return Response.OK;
	}
	
	/* momirVig */
	public static Response momirVig(Game g, StackObject aa) {
		Player controller = aa.getController(g);
		int xValue = aa.getXValue();
		String cardname = g.getRandomCreatureCardWithCCM(xValue);
		if (cardname == null) {
			System.out.println("Momir Vig activation X=" + xValue + " -> Nothing");
			return Response.OK;
		}
		
		g.createCopyToken(aa.getSource(), cardname, controller);
		System.out.println("Momir Vig activation X=" + xValue + " -> " + cardname);
		return Response.OK;
	}
	
	/* riteOfReplication */
	public static Response riteOfReplication(Game g, StackObject spell) {
		Card target = (Card) spell.getTargetObject(0);
		Player controller = spell.getController(g);
		int nbTokens = 1;
		
		if (((Card) spell).getSpellCastUsed().getOption() == Option.CAST_WITH_KICKER)
			nbTokens = 5;
		
		for (int i = 0; i < nbTokens; i++)
			g.createCopyToken(spell.getSource(), target.getName(), controller);
		return Response.OK;
	}
	
	/* thrabenInspector */
	public static Response thrabenInspector(Game g, StackObject ta) {
		g.investigate(ta.getSource(), ta.getController(g));
		return Response.OK;
	}
	
	/* tirelessTracker_investigate */
	public static Response tirelessTracker_investigate(Game g, StackObject ta) {
		g.investigate(ta.getSource(), ta.getController(g));
		return Response.OK;
	}
	/* tirelessTracker_counter */
	public static Response tirelessTracker_counter(Game g, StackObject ta) {
		ta.getSource().addCounter(g, CounterType.PLUS_ONE, 1);
		return Response.OK;
	}
	
	/* monarchCombatDamaged */
	public static Response monarchCombatDamaged(Game g, StackObject ta) {
		Card damagingCreature = (Card) ta.getAdditionalData();
		Player newMonarch = damagingCreature.getController(g);
		if (newMonarch != g.getTheMonarch())
			g.assignMonarch(newMonarch);
		return Response.OK;
	}
	
	/* monasteryMentor */
	public static Response monasteryMentor(Game g, StackObject so) {
		g.createSingleToken(Token.WHITE_MONK_11_PROWESS, so);
		return Response.OK;
	}
	
	/* lavaclawReaches_pump */
	public static Response lavaclawReaches_pump(Game g, StackObject aa) {
		Card card = aa.getSource();
		int Xvalue = aa.getXValue();
		card.addModifiers(new PTModifier(aa, "+" + Xvalue + "/+0", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
		return Response.OK;
	}	
	
	/* fieryMantle_pump */
	public static Response fieryMantle_pump(Game g, StackObject aa) {
		Card aura = aa.getSource();
		aura.getTargetObject(0).addModifiers(new PTModifier(aa, "+1/+0", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
		return Response.OK;
	}
	
	/* firebreathing */
	public static Response firebreathing(Game g, StackObject aa) {
		aa.getSource().addModifiers(new PTModifier(aa, "+1/+0", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
		return Response.OK;
	}
	
	/* wanderingFumarole_switch */
	public static Response wanderingFumarole_switch(Game g, StackObject aa) {
		Card card = aa.getSource();
		card.addModifiers(new PTModifier(card, null, Operation.SWITCH, Modifier.Duration.UNTIL_END_OF_TURN));
		return Response.OK;
	}	
	
	/* Questing Phelddagrif */
	public static Response questingPheldda_1(Game g, StackObject aa) {
		Player target = (Player) aa.getTargetObject(0);
		Card pheldda = aa.getSource();
		pheldda.addModifiers(new PTModifier(aa, "+1/+1", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
		g.createSingleToken(Token.GREEN_HIPPO_11, aa, target);
		return Response.OK;
	}
	public static Response questingPheldda_2(Game g, StackObject aa) {
		Player target = (Player) aa.getTargetObject(0);
		Card pheldda = aa.getSource();
		pheldda.addModifiers(new ProtectionModifier(aa, Color.BLACK, Modifier.Duration.UNTIL_END_OF_TURN),
				             new ProtectionModifier(aa, Color.RED, Modifier.Duration.UNTIL_END_OF_TURN));
		target.gainLife(2);
		return Response.OK;
	}
	public static Response questingPheldda_3(Game g, StackObject aa) {
		Player target = (Player) aa.getTargetObject(0);
		Card pheldda = aa.getSource();
		
		if (aa.getStep() == 1) {
			aa.advanceStep();
			pheldda.addModifiers(new EvergreenModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.FLYING));
			target.setState(State.PromptDoYouWantToDrawACard);
			return Response.MoreStep;
		}
		else if (aa.getStep() == 2) {
			if (g.getAnswer() == Answer.Yes) {
				aa.advanceStep();
				return g.drawCards(target, 1);
			}
			else
				return Response.OK;
		}
		
		return Response.OK;
	}
	
	/* Morphling */
	public static Response morphling_2(Game g, StackObject so) {
		Card card = so.getSource();
		card.addModifiers(new EvergreenModifier(so, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.FLYING));
		return Response.OK;
	}
	public static Response morphling_3(Game g, StackObject so) {
		Card card = so.getSource();
		card.addModifiers(new EvergreenModifier(so, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.SHROUD));
		return Response.OK;
	}
	public static Response morphling_4(Game g, StackObject so) {
		Card card = so.getSource();
		card.addModifiers(new PTModifier(so, "+1/-1", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
		return Response.OK;
	}
	public static Response morphling_5(Game g, StackObject so) {
		Card card = so.getSource();
		card.addModifiers(new PTModifier(so, "-1/+1", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
		return Response.OK;
	}
	
	/* jeskaiAscendancy_pump */
	public static Response jeskaiAscendancy_pump(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		Vector<Card> permanents = g.getBattlefield().getCreaturesControlledBy(controller);
		for (Card p : permanents) {
			p.addModifiers(new PTModifier(ta, "+1/+1", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
			p.untap(g);
		}
		return Response.OK;
	}
	
	/* jeskaiAscendancy_draw */
	public static Response jeskaiAscendancy_draw(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		int step = ta.getStep();
		
		if (step == 1)
		{
			ta.advanceStep();
			g.drawCards(controller, 1);
			return Response.MoreStep;
		}
		else if (step == 2) {
			controller.setState(State.WaitDiscard);
			ta.advanceStep();
			return Response.MoreStep;
		}
		else if (step == 3)
		{
			if (g.validateChoices())
			{
				Vector<MtgObject> choices = g.getChoices();
				g.discard((Card) choices.get(0));
			}
		}
		return Response.OK;
	}
	
	/* lifeFromTheLoam */
	public static Response lifeFromTheLoam(Game g, StackObject spell) {
		for (int i = 0; i < 3; i++) {
			if (spell.getTargetObject(i) != null)
				g.move_GYD_to_HND((Card) spell.getTargetObject(i));
		}
		return Response.OK;
	}

	/* wildDogs */
	public static Response wildDogs(Game g, StackObject ta) {
		Player newController;
		Vector<Player> players = g.getPlayers();
		
		if (players.get(0).getLife() > players.get(1).getLife())
			newController = players.get(0);
		else
			newController = players.get(1);
		
		ta.getSource().setController(g, newController);
		return Response.OK;
	}
	
	/* wallOfJunk_blocks */
	public static Response wallOfJunk_blocks(Game g, StackObject ta) {
		// Delayed triggered effect that will return the wall at end of combat
		ContinuousEffect delayedTrigger = ContinuousEffectFactory.create("wallOfJunk_return", ta.getSource());
		delayedTrigger.setAdditionalData(ta.getSource());
		g.addContinuousEffect(delayedTrigger);
		return Response.OK;
	}
	
	/* wallOfJunk_return */
	public static Response wallOfJunk_return(Game g, StackObject so) {
		TriggeredAbility ta = (TriggeredAbility) so;
		Card wall = (Card) ta.getAdditionalData();
		return g.move_BFD_to_HND(wall);
	}
	
	/* vebulid_upkeep */
	public static Response vebulid_upkeep(Game g, StackObject ta) {
		ta.getSource().addCounter(g, CounterType.PLUS_ONE, 1);
		return Response.OK;
	}
	
	/* vebulid_attaksOrBlocks */
	public static Response vebulid_attaksOrBlocks(Game g, StackObject ta) {
		// Delayed triggered effect that will destroy Vebulid at end of combat
		ContinuousEffect delayedTrigger = ContinuousEffectFactory.create("vebulid_destroy", ta.getSource());
		delayedTrigger.setAdditionalData(ta.getSource());
		g.addContinuousEffect(delayedTrigger);
		return Response.OK;
	}
	
	/* vebulid_destroy */
	public static Response vebulid_destroy(Game g, StackObject so) {
		TriggeredAbility ta = (TriggeredAbility) so;
		Card vebulid = (Card) ta.getAdditionalData();
		return g.destroy(vebulid, true);
	}
	
	/* titaniasChosen */
	public static Response titaniasChosen(Game g, StackObject ta) {
		ta.getSource().addCounter(g, CounterType.PLUS_ONE, 1);
		return Response.OK;
	}
	
	/* Boomerang */
	public static Response boomerang(Game g, StackObject c) {
		Response ret = g.move_BFD_to_HND((Card) c.getTargetObject(0));
		return ret;
	}
	
	/* hoodwink */
	public static Response hoodwink(Game g, StackObject c) {
		Response ret = g.move_BFD_to_HND((Card) c.getTargetObject(0));
		return ret;
	}
	
	
	
	/* declarationInStone */
	public static Response declarationInStone(Game g, StackObject spell) {
		Card target = (Card) spell.getTargetObject(0);
		g.move_BFD_to_EXL(target);
		if (!target.isToken())
			g.investigate(spell.getSource(), target.getController(g));
		
		Vector<Card> creatures = g.getBattlefield().getCreaturesControlledBy(target.getController(g));
		Card creature;
		String name;
		int i = 0;
		
		while (i < creatures.size())
		{
			creature = (Card) creatures.get(i);
			name = creature.getDisplayName();
			if ((name != null) && name.equals(target.getDisplayName()))
			{
				g.move_BFD_to_EXL(creature);
				if (!creature.isToken())
					g.investigate(spell.getSource(), creature.getController(g));
			}
			i++;
		}
		return Response.OK;
	}
	
	/* maelstromPulse */
	public static Response maelstromPulse(Game g, StackObject spell) {
		Card target = (Card) spell.getTargetObject(0);
		g.destroy(target, true);
		
		Vector<MtgObject> permanents = g.getBattlefield().getObjects();
		Card permanent;
		String name;
		int i = 0;
		
		while (i < permanents.size())
		{
			permanent = (Card) permanents.get(i);
			name = permanent.getDisplayName();
			if ((name != null) && name.equals(target.getDisplayName()))
			{
				g.destroy(permanent, true);
				i--;
			}
			i++;
		}
		return Response.OK;
	}
	
	/* ashenRider */
	public static Response ashenRider(Game g, StackObject c) {
		return g.move_BFD_to_EXL((Card) c.getTargetObject(0));
	}
	
	/* beastWithin */
	public static Response beastWithin(Game g, StackObject spell) {
		Response ret;
		Card target = (Card) spell.getTargetObject(0);
		ret = g.destroy(target, true);
		Card beast = g.createSingleToken(Token.GREEN_BEAST_33, spell);
		beast.setController(g, target.getController(g));
		return ret;
	}
	
	/* anguishedUnmaking */
	public static Response anguishedUnmaking(Game g, StackObject spell) {
		g.move_BFD_to_EXL((Card) spell.getTargetObject(0));
		spell.getController(g).loseLife(3);
		return Response.OK;
	}
	
	/* venserSS */
	public static Response venserSS(Game g, StackObject ta) {
		Card target = (Card) ta.getTargetObject(0);
		if (target.isOTB(g))
			return g.move_BFD_to_HND(target);
		if (target.isOTS(g))
			return g.move_STK_to_HND(target);
		return Response.ErrorInvalidTarget;
	}
	
	/* chaliceVoid_counterSpell */
	public static Response chaliceVoid_counterSpell(Game g, StackObject so) {
		TriggeredAbility ta = (TriggeredAbility)so;
		Card spell = (Card) ta.getAdditionalData();
		g.counter(spell);
		return Response.OK;
	}
	
	/* brand */
	public static Response brand(Game g, StackObject spell) {
		Player controller = spell.getController(g);
		Vector<Card> permanents = g.getBattlefield().getPermanents();
		for (Card permanent : permanents) {
			if (permanent.getOwner() == controller)
				permanent.setController(g, controller);
		}
		return Response.OK;
	}
	
	/* remand */
	public static Response remand(Game g, StackObject c) {
		Response ret = Response.OK;
		Card target = (Card) c.getTargetObject(0);
		g.counterAndMove(target, target.getController(g).getHand());
		if (c.getStep() == 1) {
			c.advanceStep();
			ret = g.drawCards(c.getController(g), 1);
		}
		return ret;
	}
	
	/* voidShatter */
	public static Response voidShatter(Game g, StackObject c) {
		Response ret = Response.OK;
		Card target = (Card) c.getTargetObject(0);
		g.counterAndMove(target, target.getController(g).getExile());
		return ret;
	}
	
	/* disruptiveStudent */
	public static Response disruptiveStudent(Game g, StackObject aa) {
		Card target = (Card) aa.getTargetObject(0);
		Player targetController = target.getController(g);

		if (aa.getStep() == 1) {
			targetController.setState(State.PromptPay_1mana);
			aa.advanceStep();
			return Response.MoreStep;
		}
		else {
			if (g.getAnswer() == Answer.No) // Spell controller chose not to pay (1) : counter spell.
				g.counter(target);
			return Response.OK;
		}
	}
	
	/* powerTaint */
	public static Response powerTaint(Game g, StackObject ta) {
		Card aura = ta.getSource();
		Card host = aura.getHost();
		
		if (ta.getStep() == 1) {
			ta.advanceStep();
			host.getController(g).setState(State.PromptPay_2mana);
			return Response.MoreStep;
		}
		
		if (ta.getStep() == 2) {
			if (g.getAnswer() == Answer.No)
				host.getController(g).loseLife(2);
			return Response.OK;
		}
		return Response.OK;
	}
	
	/* presenceOfTheMaster */
	public static Response presenceOfTheMaster(Game g, StackObject ta) {
		Card triggeringSpell = (Card) ta.getAdditionalData();
		g.counter(triggeringSpell);
		return Response.OK;
	}
	
	/* palaceJailer_monarch */
	public static Response palaceJailer_monarch(Game g, StackObject ta) {
		g.assignMonarch(ta.getController(g));
		return Response.OK;
	}
	
	/* ponder */
	public static Response ponder(Game g, StackObject spell) {
		Player controller = spell.getController(g);
		int nbCardsInLibrary = controller.getLibrary().size();
		
		// immediately return OK if the library is empty
		if (nbCardsInLibrary == 0)
			return Response.OK;
		
		// the number of cards looked at is the MIN between 3 and the library size
		int nbCardsPeeked = Math.min(nbCardsInLibrary, 3);
		
		// Show top X cards of library and wait for player to put them back in any order
		if (spell.getStep() == 1) {
			spell.setLibManip(controller.getLibrary().getXTopCards(nbCardsPeeked));
			controller.setState(State.WaitChoicePutTopLib);
			spell.advanceStep();
			return Response.MoreStep;
		}
		
		// Checking card clicked
		if (spell.getStep() == 2) {
			if (g.validateChoices()) {
				Card c = (Card) g.getChoices().get(0);
				g.libToLibTop(c);
				spell.getLibManip().remove(c);
			}
			else
				return Response.MoreStep;
			if (spell.getLibManip().size() > 0)
				return Response.MoreStep;
			else
				spell.advanceStep();
		}
		
		// Ask if player wants to shuffle library
		if (spell.getStep() == 3) {
			spell.advanceStep();
			controller.setState(State.PromptDoYouWantToShuffle);
			return Response.MoreStep;
		}
		
		// Checking answer
		if (spell.getStep() == 4) {
			spell.advanceStep();
			if (g.getAnswer() == Answer.Yes)
				controller.shuffle();
			return g.drawCards(controller, 1);
		}
		
		return Response.Error; // should never get here
	}
	
	/* palaceJailer_exile */
	public static Response palaceJailer_exile(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		Player opponent = controller.getOpponent();
		Card target = (Card) ta.getTargetObject(0);
		g.move_BFD_to_EXL(target);
		
		if (!target.isToken())
			opponent.addEffectCallback("palaceJailer_return", target);
		
		return Response.OK;
	}
	
	public static void palaceJailer_return(Game g, Object exiledCreature) {
		g.move_EXL_to_BFD((Card) exiledCreature);
	}

	/* mindOverMatter */
	public static Response mindOverMatter(Game g, StackObject aa) {
		Card target = (Card) aa.getTargetObject(0);
		
		if (target.isTapped())
			target.untap(g);
		else
			target.tap(g);
		
		return Response.OK;
	}
	
	/* manaVault_untap */
	public static Response manaVault_untap(Game g, StackObject ta) {
		Card manaVault = ta.getSource();
		
		if (ta.getStep() == 1) {
			ta.advanceStep();
			manaVault.getController(g).setState(State.PromptPay_4mana);
			return Response.MoreStep;
		}
		
		if (ta.getStep() == 2) {
			if (g.getAnswer() == Answer.Yes) {
				manaVault.untap(g);
				return Response.OK;
			}
			else
				return Response.OK;
		}
		
		return Response.OK;
	}
	
	/* forceSpike */
	public static Response forceSpike(Game g, StackObject spell) {
		Card target = (Card) spell.getTargetObject(0);
		Player targetController = target.getController(g);

		if (spell.getStep() == 1) {
			targetController.setState(State.PromptPay_1mana);
			spell.advanceStep();
			return Response.MoreStep;
		}
		
		if (spell.getStep() == 2) {
			if (g.getAnswer() == Answer.No) // Spell controller chose not to pay (1) : counter spell.
				g.counter(target);
		}
		return Response.OK;	
	}
	
	/* miscalculation */
	public static Response miscalculation(Game g, StackObject spell) {
		Card target = (Card) spell.getTargetObject(0);
		Player targetController = target.getController(g);

		if (spell.getStep() == 1) {
			targetController.setState(State.PromptPay_2mana);
			spell.advanceStep();
			return Response.MoreStep;
		}
		else {
			if (g.getAnswer() == Answer.No) // Spell controller chose not to pay (2) : counter spell.
				g.counter(target);
			return Response.OK;
		}
	}
	
	/* redeem */
	public static Response redeem(Game g, StackObject spell) {
		for (int i = 0; i < spell.getNbTarget(); i++) {
			if (spell.getTargetObject(i) != null) {
				((Card) spell.getTargetObject(i)).addModifiers(new EvergreenModifier(spell, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.UNDAMAGEABLE));
			}
				
		}
		return Response.OK;
	}
	
	/* rainOfFilth */
	public static Response rainOfFilth(Game g, StackObject spell) {
		Vector<Card> lands = g.getBattlefield().getLandsControlledBy(spell.getController(g));
		for (Card land : lands) {
			ActivatedAbility ab = ActivatedAbilityFactory.create("rainOfFilth_sac", land);
			land.addModifiers(new AbilityModifier(spell, ab, Modifier.Duration.UNTIL_END_OF_TURN));
		}
		return Response.OK;
	}
	
	/* liltingRefrain_sac */
	public static Response liltingRefrain_sac(Game g, StackObject aa) {
		Card target = (Card) aa.getTargetObject(0);
		Player targetController = target.getController(g);
		if (aa.getStep() == 1) {
			targetController.setState(State.PromptPay_Xmana);
			aa.advanceStep();
			return Response.MoreStep;
		}
		
		if (aa.getStep() == 2) {
			if (g.getAnswer() == Answer.No) // Spell controller chose not to pay (X) : counter spell
				g.counter(target);
		}
		return Response.OK;
	}
	
	/* powerSink */
	public static Response powerSink(Game g, StackObject spell) {
		Card target = (Card) spell.getTargetObject(0);
		Player targetController = target.getController(g);
		
		switch(spell.getStep()) {
		case 1:
			targetController.setState(State.PromptPay_Xmana);
			spell.advanceStep();
			return Response.MoreStep;
			
		default:
			if (g.getAnswer() == Answer.No) // Spell controller chose not to pay (X) : counter spell and tap his lands
			{ 
				g.counter(target);
				Vector<Card> lands = g.getBattlefield().getLandsControlledBy(targetController);
				for (Card land : lands)
				{
					for (ActivatedAbility ab : land.getActivatedAbilities(g, true))
					{
						if (ab.isManaAbility())
							land.tap(g);
					}
				}
			}
			return Response.OK;
		}
	}

	/* manaLeak */
	public static Response manaLeak(Game g, StackObject spell) {
		Card target = (Card) spell.getTargetObject(0);
		Player targetController = target.getController(g);
		if (spell.getStep() == 1) {
			targetController.setState(State.PromptPay_3mana);
			spell.advanceStep();
			return Response.MoreStep;
		}
		else {
			if (g.getAnswer() == Answer.No) // Spell controller chose not to pay (3) : counter spell.
				g.counter(target);
			return Response.OK;
		}
	}
	
	/* spellShrivel */
	public static Response spellShrivel(Game g, StackObject spell) {
		Card target = (Card) spell.getTargetObject(0);
		Player targetController = target.getController(g);
		if (spell.getStep() == 1) {
			targetController.setState(State.PromptPay_4mana);
			spell.advanceStep();
			return Response.MoreStep;
		}
		else {
			if (g.getAnswer() == Answer.No) // Spell controller chose not to pay (4) : counter spell.
				g.counterAndMove(target, targetController.getExile());
			return Response.OK;
		}
	}
	
	/* absorb */
	public static Response absorb(Game g, StackObject c) {
		Card target = (Card) c.getTargetObject(0);
		if (target == null)
			return Response.ErrorInvalidTarget;
		
		g.counter(target);
		Player controller = c.getController(g);
		controller.gainLife(3);
		return Response.OK;
	}
	
	/* firesOfYavimaya_pump */
	public static Response firesOfYavimaya_pump(Game g, StackObject aa) {
		Card target = (Card) aa.getTargetObject(0);
		target.addModifiers(new PTModifier(aa, "+2/+2", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
		return Response.OK;
	}
	
	/* Exalted */
	public static Response exalted(Game g, StackObject so) {
		TriggeredAbility ta = (TriggeredAbility) so;
		Card creatureWithExalted = ta.getSource();
		Card attackingCreature = (Card) ta.getAdditionalData();
		attackingCreature.addModifiers(new PTModifier(creatureWithExalted, "+1/+1", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
		return Response.OK;
	}
	
	/* breach */
	public static Response breach(Game g, StackObject c) {
		Card target = (Card) c.getTargetObject(0);
		target.addModifiers(new PTModifier(c, "+2/+0", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN),
							new EvergreenModifier(c, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.FEAR));
		return Response.OK;
	}
	
	/* rejuvenate */
	public static Response rejuvenate(Game g, StackObject spell) {
		spell.getController(g).gainLife(6);
		return Response.OK;
	}
	
	/* showAndTell */
	public static Response showAndTell(Game g, StackObject spell) {
		Player controller = spell.getController(g);
		Player opponent = controller.getOpponent();
		
		if (spell.getStep() == 1) {
			spell.advanceStep();
			controller.setState(State.WaitChoiceShowAndTell);
			return Response.MoreStep;
		}
		
		if (spell.getStep() == 2) {
			if (g.validateChoices()) {
				if (g.getChoices().size() == 1)
					g.move_HND_to_BFD(((Card) g.getChoices().get(0)));
				spell.goToStep(10);
			}
			else
				return Response.MoreStep;
		}
		
		if (spell.getStep() == 10) {
			spell.advanceStep();
			opponent.setState(State.WaitChoiceShowAndTell);
			return Response.MoreStep;
		}
		
		if (spell.getStep() == 11) {
			if (g.validateChoices()) {
				if (g.getChoices().size() == 1)
					g.move_HND_to_BFD(((Card) g.getChoices().get(0)));
				return Response.OK;
			}
			else
				return Response.MoreStep;
		}
		
		return Response.OK;
	}
	
	/* carpetOfFlowers */
	public static Response carpetOfFlowers(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		Player opponent = controller.getOpponent();
		int X = 0;
		
		for (Card land : g.getBattlefield().getLandsControlledBy(opponent))
			if (land.hasSubtypeGlobal(g, Subtype.ISLAND))
				X++;
		
		System.out.println(controller + " adds up to " + X + " mana of any single color to his mana pool.");
		return Response.OK;
	}
	
	/* reprocess */
	public static Response reprocess(Game g, StackObject spell) {
		Player controller = spell.getController(g);
		if (spell.getStep() == 1) {
			spell.advanceStep();
			controller.setState(State.WaitChoiceReprocess);
			return Response.MoreStep;
		}
		if (spell.getStep() == 2) {
			if (g.validateChoices()) {
				if (g.getChoices().size() == 0) // player clicked Done
					spell.goToStep(10);
				else {
					g.sacrifice(controller, (Card) g.getChoices().get(0));
					spell.setXValue(spell.getXValue() + 1);
					return Response.MoreStep;
				}
			}
			else
				return Response.MoreStep;
		}
		if (spell.getStep() == 10) {
			spell.advanceStep();
			return g.drawCards(controller, spell.getXValue());
		}
		return Response.OK;
	}

	/* motherOfRunes */
	public static Response motherOfRunes(Game g, StackObject aa) {
		Player controller = aa.getController(g);
		
		if (aa.getStep() == 1) {
			aa.advanceStep();
			controller.setState(State.WaitChooseColor);
			return Response.MoreStep;
		}
		
		if (aa.getStep() == 2) {
			int choice = aa.getXValue();
			Color chosenColor;
			Card target = (Card) aa.getTargetObject(0);
			
			if ((choice < 1) || (choice > 5))
			{
				return Response.MoreStep;
			}
			else
			{
				chosenColor = Color.intToColor(choice);
				target.addModifiers(new ProtectionModifier(aa, chosenColor, Modifier.Duration.UNTIL_END_OF_TURN));
			}
		}
		return Response.OK;
	}
	
	/* smokestack_charge */
	public static Response smokestack_charge(Game g, StackObject ta) {
		ta.getSource().addCounter(g, CounterType.SOOT, 1);
		return Response.OK;
	}
	
	/* umbilicus */
	public static Response umbilicus(Game g, StackObject ta) {
		Player activePlayer = g.getActivePlayer();
		
		if (ta.getStep() == 1) {
			ta.advanceStep();
			
			if (activePlayer.getLife() >= 2) {
				activePlayer.setState(State.PromptPay_2life);
				return Response.MoreStep;	
			}
			else
				ta.goToStep(3);
		}
		
		if (ta.getStep() == 2) {
			if (g.getAnswer() == Answer.Yes) {
				activePlayer.loseLife(2);
				return Response.OK;
			}
			else
				ta.goToStep(3);
		}
		
		if (ta.getStep() == 3) {
			// If the player controls 0 permanent, immediately return
			if (activePlayer.getNbPermanentsControlled() == 0)
				return Response.OK;
			
			ta.advanceStep();
			activePlayer.setState(State.WaitReturnPermanent);
			return Response.MoreStep;
		}
		
		if (ta.getStep() == 4) {
			if (g.validateChoices())
				g.move_BFD_to_HND((Card) g.getChoices().get(0));
			else
				return Response.MoreStep;
		}
		
		return Response.OK;
	}
	
	/* spreadingAlgae_destroy */
	public static Response spreadingAlgae_destroy(Game g, StackObject ta) {
		Card aura = ta.getSource();
		Card host = aura.getHost();
		g.destroy(host, true);
		return Response.OK;
	}
	
	/* taintedAEther_sac */
	public static Response taintedAEther_sac(Game g, StackObject ta) {
		Card triggeringCreature = (Card) ta.getAdditionalData();
		Player creaController = triggeringCreature.getController(g);
		
		if ((creaController.getNbCreaturesControlled() == 0) && (creaController.getNbLandsControlled() == 0))
			return Response.OK;
		
		if (ta.getStep() == 1) {
			ta.advanceStep();
			creaController.setState(State.WaitSacrificeCreatureOrLand);
			return Response.MoreStep;
		}
		
		if (ta.getStep() == 2) {
			if (g.validateChoices()) {
				Card choice = (Card) g.getChoices().get(0);
				g.sacrifice(creaController, choice);
			}
			else
				return Response.MoreStep;
		}
		return Response.OK;
	}
	
	/* venomousFangs */
	public static Response venomousFangs(Game g, StackObject ta) {
		Card damagedCreature = (Card) ta.getAdditionalData();
		g.destroy(damagedCreature, true);
		return Response.OK;
	}

	/* destructiveUrge */
	public static Response destructiveUrge(Game g, StackObject ta) {
		Player affectedPlayer = (Player) ta.getAdditionalData();
		
		if (affectedPlayer.getNbLandsControlled() == 0)
			return Response.OK;
		
		if (ta.getStep() == 1) {
			ta.advanceStep();
			affectedPlayer.setState(State.WaitSacrificeLand);
			return Response.MoreStep;
		}
		
		if (ta.getStep() == 2) {
			if (g.validateChoices())
				g.sacrifice(affectedPlayer, (Card) g.getChoices().get(0));
			else
				return Response.MoreStep;
		}
		return Response.OK;
	}
	
	/* smokestack_sac */
	public static Response smokestack_sac(Game g, StackObject ta) {
		Player activePlayer = g.getActivePlayer();
		int nbPermanents = activePlayer.getNbPermanentsControlled();
		int nbCounters = ta.getSource().getNbCountersOfType(g, CounterType.SOOT);

		if ((nbPermanents == 0) || (nbCounters == 0))
			return Response.OK;
		
		if (ta.getStep() == 1) {
			ta.advanceStep();
			ta.setXValue(nbCounters);
			activePlayer.setState(State.WaitSacrificePermanent);
			return Response.MoreStep;
		}
		
		if (ta.getStep() == 2) {
			if (g.validateChoices()) {
				g.sacrifice(activePlayer, (Card) g.getChoices().get(0));
				if (activePlayer.getNbPermanentsControlled() == 0)
					return Response.OK;
				ta.setXValue(ta.getXValue() - 1);
				if (ta.getXValue() > 0) {
					activePlayer.setState(State.WaitSacrificePermanent);
					return Response.MoreStep;
				}
				else
					return Response.OK;
			}
			else
				return Response.MoreStep;
		}
		
		return Response.OK;
	}
	
	/* noRestfortheWicked */
	public static Response noRestfortheWicked(Game g, StackObject aa) {
		Player controller = aa.getController(g);
		Vector<Card> markedCards = new Vector<Card>();
		
		// mark cards
		for (Card dead : controller.getGraveyard().getCreatureCards()) {
			if (dead.wasPutIntoGraveyardThisTurn())
				markedCards.add(dead);
		}
		
		// reutrn marked cards
		for (Card c : markedCards)
			g.move_GYD_to_HND(c);
		
		return Response.OK;
	}
	
	/* midsummerRevel_sac */
	public static Response midsummerRevel_sac(Game g, StackObject aa) {
		int X = aa.getSource().getNbCountersOfType(g, CounterType.VERSE);
		g.createTokens(Token.GREEN_BEAST_33, X, aa);
		return Response.OK;
	}
	
	/* vileRequiem_sac */
	public static Response vileRequiem_sac(Game g, StackObject aa) {
		int X = aa.getXValue();
		Card target;
		
		for (int i = 0; i < X; i++) {
			target = (Card) aa.getTargetObject(i);
			if (target != null) {
				g.destroy(target, false);
			}
			else
				return Response.OK;
		}
		
		return Response.OK;
	}
	
	/* recantation_sac */
	public static Response recantation_sac(Game g, StackObject aa) {
		int X = aa.getXValue();
		Card target;
		
		for (int i = 0; i < X; i++) {
			target = (Card) aa.getTargetObject(i);
			if (target != null)
				g.move_BFD_to_HND(target);
			else
				return Response.OK;
		}
		
		return Response.OK;
	}
	
	/* serrasLiturgy_sac */
	public static Response serrasLiturgy_sac(Game g, StackObject aa) {
		int X = aa.getXValue();
		Card target;
		
		for (int i = 0; i < X; i++) {
			target = (Card) aa.getTargetObject(i);
			if (target != null)
				g.destroy(target, true);
			else
				return Response.OK;
		}
		
		return Response.OK;
	}
	
	/* yawgmothsEdict */
	public static Response yawgmothsEdict(Game g, StackObject ta) {
		Card triggeringSpell = (Card) ta.getAdditionalData();
		Player opponent = triggeringSpell.getController(g);
		Player player = ta.getController(g);
		
		opponent.loseLife(1);
		player.gainLife(1);
		return Response.OK;
	}
	
	/* rumblingCrescendo_sac */
	public static Response rumblingCrescendo_sac(Game g, StackObject aa) {
		int X = aa.getXValue();
		Card target;
		
		for (int i = 0; i < X; i++) {
			target = (Card) aa.getTargetObject(i);
			if (target != null)
				g.destroy(target, true);
			else
				return Response.OK;
		}
		
		return Response.OK;
	}
	
	/* discordantDirge_sac */
	public static Response discordantDirge_sac(Game g, StackObject aa) {
		Player controller = aa.getController(g);
		Player opponent = (Player) aa.getTargetObject(0);
		int X = aa.getSource().getNbCountersOfType(g, CounterType.VERSE);
		
		// if the opponent has no cards in hand or there are 0 counters, immediately return
		if ((opponent.getHandSize() == 0) || (X == 0))
			return Response.OK;
		
		// look at hand and wait for player to choose a card
		if (aa.getStep() == 1) {
			aa.setXValue(X);
			aa.advanceStep();
		}
		
		if (aa.getStep() == 2) {
			aa.advanceStep();
			controller.setState(State.WaitChoiceCoercion);
			return Response.MoreStep;
		}
		
		if (aa.getStep() == 3) {
			X = aa.getXValue();
			if (!g.validateChoices())
				return Response.MoreStep;
			else {
				if (g.getChoices().size() == 0)
					return Response.OK;
				else {
					g.discard((Card) g.getChoices().get(0));
					X--;
					if ((opponent.getHandSize() == 0) || (X == 0))
						return Response.OK;
					else {
						aa.setXValue(X);
						return Response.MoreStep;
					}
				}
			}
		}
		return Response.OK;
	}
	
	/* barrinsCodex_charge */
	public static Response barrinsCodex_charge(Game g, StackObject ta) {
		ta.getSource().addCounter(g, CounterType.PAGE, 1);
		return Response.OK;
	}
	
	/* lotusBlossom_charge */
	public static Response lotusBlossom_charge(Game g, StackObject ta) {
		ta.getSource().addCounter(g, CounterType.PETAL, 1);
		return Response.OK;
	}

	/* barrinsCodex_sac */
	public static Response barrinsCodex_sac(Game g, StackObject aa) {
		if (aa.getStep() == 1) {
			aa.advanceStep();
			int nbCounters = aa.getSource().getNbCountersOfType(g, CounterType.PAGE);
			return g.drawCards(aa.getController(g), nbCounters);
		}
		return Response.OK;
	}
	
	/* warDance_sac */
	public static Response warDance_sac(Game g, StackObject aa) {
		int nbCounters = aa.getSource().getNbCountersOfType(g, CounterType.VERSE);
		Card target = (Card) aa.getTargetObject(0);
		String bonus = String.format("+%d/+%d", nbCounters, nbCounters);
		target.addModifiers(new PTModifier(aa, bonus, Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
		return Response.OK;
	}
	
	/* serrasHymn_sac */
	public static Response serrasHymn_sac(Game g, StackObject aa) {
		if (aa.getNbTarget() == 0)
			return Response.OK;
		
		int X = aa.getSource().getNbCountersOfType(g, CounterType.VERSE);
		Damageable target = (Damageable) aa.getTargetObject(0);
		target.addDamagePrevention(X);
		return Response.OK;
	}
	
	/* rainOfSalt */
	public static Response rainOfSalt(Game g, StackObject spell) {
		g.destroy((Card) spell.getTargetObject(0), true);
		g.destroy((Card) spell.getTargetObject(1), true);
		return Response.OK;
	}
	
	/* jaggedLightning */
	public static Response jaggedLightning(Game g, StackObject so) {
		Card spell = (Card) so;
		
		spell.dealNonCombatDamageTo(g, (Card) spell.getTargetObject(0), 3);
		spell.dealNonCombatDamageTo(g, (Card) spell.getTargetObject(1), 3);
		return Response.OK;
	}
	
	/* symbiosis */
	public static Response symbiosis(Game g, StackObject c) {
		for (int i = 0; i < 2; i++) {
			((Card) c.getTargetObject(i)).addModifiers(new PTModifier(c, "+2/+2", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
		}
		return Response.OK;
	}
	
	/* Giant Growth */
	public static Response giantGrowth(Game g, StackObject c) {
		Card target = (Card) c.getTargetObject(0);
		target.addModifiers(new PTModifier(c, "+3/+3", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
		return Response.OK;
	}
	
	/* animateLand */
	public static Response animateLand(Game g, StackObject c) {
		Card target = (Card) c.getTargetObject(0);
		target.addModifiers(new CardTypeModifier(c, Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN, CardType.CREATURE),
						   new PTModifier(c, "3/3", Operation.SET, Modifier.Duration.UNTIL_END_OF_TURN));
		return Response.OK;
	}
	
	/* forbiddingWatchtower */
	public static Response forbiddingWatchtower(Game g, StackObject c) {
		Card land = c.getSource();
		land.addModifiers(new CardTypeModifier(c, Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN, CardType.CREATURE),
						 new PTModifier(c, "1/5", Operation.SET, Modifier.Duration.UNTIL_END_OF_TURN),
						 new ColorModifier(c, Modifier.Duration.UNTIL_END_OF_TURN, Color.WHITE),
						 new CreatureTypeModifier(c, Modifier.Duration.UNTIL_END_OF_TURN, CreatureType.Soldier));
		return Response.OK;
	}
	
	/* faerieConclave */
	public static Response faerieConclave(Game g, StackObject c) {
		Card land = c.getSource();
		land.addModifiers(new CardTypeModifier(c, Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN, CardType.CREATURE),
						 new PTModifier(c, "2/1", Operation.SET, Modifier.Duration.UNTIL_END_OF_TURN),
						 new ColorModifier(c, Modifier.Duration.UNTIL_END_OF_TURN, Color.BLUE),
						 new CreatureTypeModifier(c, Modifier.Duration.UNTIL_END_OF_TURN, CreatureType.Faerie),
						 new EvergreenModifier(c, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.FLYING));
		return Response.OK;
	}
	
	/* spawningPool */
	public static Response spawningPool(Game g, StackObject c) {
		Card land = c.getSource();
		land.addModifiers(new CardTypeModifier(c, Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN, CardType.CREATURE),
						 new PTModifier(c, "1/1", Operation.SET, Modifier.Duration.UNTIL_END_OF_TURN),
						 new ColorModifier(c, Modifier.Duration.UNTIL_END_OF_TURN, Color.BLACK),
						 new CreatureTypeModifier(c, Modifier.Duration.UNTIL_END_OF_TURN, CreatureType.Skeleton),
						 new AbilityModifier(c, ActivatedAbilityFactory.create("spawningPool_regen", land), Modifier.Duration.UNTIL_END_OF_TURN));
		return Response.OK;
	}
	
	/* ghituEncampment */
	public static Response ghituEncampment(Game g, StackObject c) {
		Card land = c.getSource();
		land.addModifiers(new CardTypeModifier(c, Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN, CardType.CREATURE),
						 new PTModifier(c, "2/1", Operation.SET, Modifier.Duration.UNTIL_END_OF_TURN),
						 new ColorModifier(c, Modifier.Duration.UNTIL_END_OF_TURN, Color.RED),
						 new CreatureTypeModifier(c, Modifier.Duration.UNTIL_END_OF_TURN, CreatureType.Warrior),
						 new EvergreenModifier(c, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.FIRSTSTRIKE));
		return Response.OK;
	}
	
	/* aethersphereHarvester_lifelink */
	public static Response aethersphereHarvester_lifelink(Game g, StackObject aa) {
		Card vehicle = aa.getSource();
		vehicle.addModifiers(new EvergreenModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.LIFELINK));
		return Response.OK;
	}
	
	/* treetopVillage */
	public static Response treetopVillage(Game g, StackObject c) {
		Card land = c.getSource();
		land.addModifiers(new CardTypeModifier(c, Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN, CardType.CREATURE),
						 new PTModifier(c, "3/3", Operation.SET, Modifier.Duration.UNTIL_END_OF_TURN),
						 new ColorModifier(c, Modifier.Duration.UNTIL_END_OF_TURN, Color.GREEN),
						 new CreatureTypeModifier(c, Modifier.Duration.UNTIL_END_OF_TURN, CreatureType.Ape),
						 new EvergreenModifier(c, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.TRAMPLE));
		return Response.OK;
	}
	
	/* aetherHub_energy */
	public static Response aetherHub_energy(Game g, StackObject ta) {
		ta.getController(g).addCounter(CounterType.ENERGY, 1);
		return Response.OK;
	}
	
	/* shamblingVent */
	public static Response shamblingVent(Game g, StackObject aa) {
		Card land = aa.getSource();
		land.addModifiers(new CardTypeModifier(aa, Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN, CardType.CREATURE),
						 new ColorModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Color.BLACK, Color.WHITE),
						 new PTModifier(aa, "2/3", Operation.SET, Modifier.Duration.UNTIL_END_OF_TURN),
						 new EvergreenModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.LIFELINK));
		return Response.OK;
	}
	
	/* lumberingFalls */
	public static Response lumberingFalls(Game g, StackObject aa) {
		Card land = aa.getSource();
		land.addModifiers(new CardTypeModifier(aa, Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN, CardType.CREATURE),
						 new ColorModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Color.BLUE, Color.GREEN),
						 new PTModifier(aa, "3/3", Operation.SET, Modifier.Duration.UNTIL_END_OF_TURN),
						 new EvergreenModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.HEXPROOF));
		return Response.OK;
	}
	
	/* wanderingFumarole */
	public static Response wanderingFumarole(Game g, StackObject aa) {
		Card land = aa.getSource();
		land.addModifiers(new CardTypeModifier(aa, Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN, CardType.CREATURE),
						 new ColorModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Color.BLUE, Color.RED),
						 new PTModifier(aa, "1/4", Operation.SET, Modifier.Duration.UNTIL_END_OF_TURN),
						 new AbilityModifier(aa, ActivatedAbilityFactory.create("wanderingFumarole_switch", land), Modifier.Duration.UNTIL_END_OF_TURN));
		return Response.OK;
	}
	
	/* needleSpires */
	public static Response needleSpires(Game g, StackObject aa) {
		Card land = aa.getSource();
		land.addModifiers(new CardTypeModifier(aa, Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN, CardType.CREATURE),
						 new ColorModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Color.RED, Color.WHITE),
						 new PTModifier(aa, "2/1", Operation.SET, Modifier.Duration.UNTIL_END_OF_TURN),
						 new EvergreenModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.DOUBLESTRIKE));
		return Response.OK;
	}
	
	/* hissingQuagmire */
	public static Response hissingQuagmire(Game g, StackObject aa) {
		Card land = aa.getSource();
		land.addModifiers(new CardTypeModifier(aa, Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN, CardType.CREATURE),
						 new ColorModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Color.BLACK, Color.GREEN),
						 new PTModifier(aa, "2/2", Operation.SET, Modifier.Duration.UNTIL_END_OF_TURN),
						 new EvergreenModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.DEATHTOUCH));
		return Response.OK;
	}
	
	/* noblePanther */
	public static Response noblePanther(Game g, StackObject aa) {
		aa.getSource().addModifiers(new EvergreenModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.FIRSTSTRIKE));
		return Response.OK;
	}
	
	/* chimericStaff */
	public static Response chimericStaff(Game g, StackObject aa) {
		Card land = aa.getSource();
		int X = aa.getXValue();
		land.addModifiers(new CardTypeModifier(aa, Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN, CardType.CREATURE, CardType.ARTIFACT),
						 new PTModifier(aa, X+"/"+X, Operation.SET, Modifier.Duration.UNTIL_END_OF_TURN),
						 new CreatureTypeModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, CreatureType.Construct));
		return Response.OK;
	}
	
	/* blinkmoth_animate */
	public static Response blinkmoth_animate(Game g, StackObject aa) {
		Card land = aa.getSource();
		land.addModifiers(new CardTypeModifier(aa, Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN, CardType.CREATURE, CardType.ARTIFACT),
						 new PTModifier(aa, "1/1", Operation.SET, Modifier.Duration.UNTIL_END_OF_TURN),
						 new EvergreenModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.FLYING),
						 new CreatureTypeModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, CreatureType.Blinkmoth));
		return Response.OK;
	}
	
	/* inkmoth_animate */
	public static Response inkmoth_animate(Game g, StackObject aa) {
		Card land = aa.getSource();
		land.addModifiers(new CardTypeModifier(aa, Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN, CardType.CREATURE, CardType.ARTIFACT),
						 new PTModifier(aa, "1/1", Operation.SET, Modifier.Duration.UNTIL_END_OF_TURN),
						 new EvergreenModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.FLYING, Evergreen.INFECT),
						 new CreatureTypeModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, CreatureType.Blinkmoth));
		return Response.OK;
	}
	
	/* mutavault */
	public static Response mutavault(Game g, StackObject aa) {
		Card land = aa.getSource();
		land.addModifiers(new CardTypeModifier(aa, Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN, CardType.CREATURE),
						 new PTModifier(aa, "2/2", Operation.SET, Modifier.Duration.UNTIL_END_OF_TURN),
						 new EvergreenModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.CHANGELING));
		return Response.OK;
	}
	
	/* celestialColonnade */
	public static Response celestialColonnade(Game g, StackObject aa) {
		Card land = aa.getSource();
		land.addModifiers(new CardTypeModifier(aa, Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN, CardType.CREATURE),
						 new PTModifier(aa, "4/4", Operation.SET, Modifier.Duration.UNTIL_END_OF_TURN),
						 new ColorModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Color.WHITE, Color.BLUE),
						 new CreatureTypeModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, CreatureType.Elemental),
						 new EvergreenModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.FLYING, Evergreen.VIGILANCE));
		return Response.OK;
	}
	
	/* creepingTarPit */
	public static Response creepingTarPit(Game g, StackObject aa) {
		Card land = aa.getSource();
		land.addModifiers(new CardTypeModifier(aa, Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN, CardType.CREATURE),
						 new ColorModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Color.BLUE, Color.BLACK),
						 new PTModifier(aa, "3/2", Operation.SET, Modifier.Duration.UNTIL_END_OF_TURN),
						 new CreatureTypeModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, CreatureType.Elemental),
						 new EvergreenModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.UNBLOCKABLE));
		return Response.OK;
	}
	
	/* shyvanna_2 */
	public static Response shyvanna_2(Game g, StackObject aa) {
		Card shyv = aa.getSource();
		shyv.addModifiers(new PTModifier(aa, "3/3", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN),
						 new CreatureTypeModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, CreatureType.Dragon),
						 new EvergreenModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.FLYING),
						 new AbilityModifier(aa, ActivatedAbilityFactory.create("firebreathing", shyv), Modifier.Duration.UNTIL_END_OF_TURN));
		return Response.OK;
	}
	
	/* lavaclawReaches */
	public static Response lavaclawReaches(Game g, StackObject aa) {
		Card land = aa.getSource();
		land.addModifiers(new CardTypeModifier(aa, Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN, CardType.CREATURE),
						 new ColorModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Color.BLACK, Color.RED),
						 new PTModifier(aa, "2/2", Operation.SET, Modifier.Duration.UNTIL_END_OF_TURN),
						 new CreatureTypeModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, CreatureType.Elemental),
						 new AbilityModifier(aa, ActivatedAbilityFactory.create("lavaclawReaches_pump", land), Modifier.Duration.UNTIL_END_OF_TURN));
		return Response.OK;
	}	
	
	/* ragingRavine */
	public static Response ragingRavine(Game g, StackObject aa) {
		Card land = aa.getSource();
		land.addModifiers(new CardTypeModifier(aa, Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN, CardType.CREATURE),
						 new ColorModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Color.RED, Color.GREEN),
						 new PTModifier(aa, "3/3", Operation.SET, Modifier.Duration.UNTIL_END_OF_TURN),
						 new CreatureTypeModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, CreatureType.Elemental),
						 new AbilityModifier(aa, TriggeredAbilityFactory.create("ragingRavine_pump", land), Modifier.Duration.UNTIL_END_OF_TURN));
		return Response.OK;
	}	
	
	/* stirringWildwood */
	public static Response stirringWildwood(Game g, StackObject aa) {
		Card land = aa.getSource();
		land.addModifiers(new CardTypeModifier(aa, Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN, CardType.CREATURE),
						 new ColorModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Color.GREEN, Color.WHITE),
						 new PTModifier(aa, "3/4", Operation.SET, Modifier.Duration.UNTIL_END_OF_TURN),
						 new CreatureTypeModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, CreatureType.Elemental),
						 new EvergreenModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.REACH));
		return Response.OK;
	}
	
	/* fireAnts */
	public static Response fireAnts(Game g, StackObject aa) {
		Card ants = aa.getSource();
		Vector<Card> creatures = g.getBattlefield().getCreatures();
		for (Card creature : creatures) {
			if ((creature != ants) && !creature.hasEvergreenGlobal(Evergreen.FLYING, g))
				ants.dealNonCombatDamageTo(g, creature, 1);
		}
		return Response.OK;
	}
	
	/* viashinoSandswimmer */
	public static Response viashinoSandswimmer(Game g, StackObject aa) {
		if (g.flipCoin()) {
			System.out.println(aa.getController(g).getName() + " loses the flip.");
			g.sacrifice(aa.getController(g), aa.getSource());
		}
		else {
			System.out.println(aa.getController(g).getName() + " wins the flip.");
			g.move_BFD_to_HND(aa.getSource());
		}
		return Response.OK;
	}

	/* scoriaWurm */
	public static Response scoriaWurm(Game g, StackObject aa) {
		if (g.flipCoin()) {
			System.out.println(aa.getController(g).getName() + " loses the flip.");
			g.move_BFD_to_HND(aa.getSource());
		}
		else
			System.out.println(aa.getController(g).getName() + " wins the flip.");
		return Response.OK;
	}
	
	/* craterHellion */
	public static Response craterHellion(Game g, StackObject ta) {
		Card hellion = ta.getSource();
		for (Card creature : g.getBattlefield().getCreatures())
			if (creature != hellion)
				hellion.dealNonCombatDamageTo(g, creature, 4);
		return Response.OK;
	}
	
	/* avacynPurifier_damage */
	public static Response avacynPurifier_damage(Game g, StackObject ta) {
		Player opponent = ta.getController(g).getOpponent();
		Card avacyn = ta.getSource();
		for (Card creature : g.getBattlefield().getCreatures())
			if (creature != avacyn)
				avacyn.dealNonCombatDamageTo(g, creature, 3);
		return avacyn.dealNonCombatDamageTo(g, opponent, 3);
	}
	
	/* disorder */
	public static Response disorder(Game g, StackObject spell) {
		boolean bControllerHasWhiteCreature = false;
		boolean bOpponentHasWhiteCreature = false;

		if (spell.getStep() == 1 ){
			for (Card creature : g.getBattlefield().getCreatures()) {
				if (creature.hasColor(Color.WHITE)) {
					if (creature.getController(g) == spell.getController(g))
						bControllerHasWhiteCreature = true;
					else
						bOpponentHasWhiteCreature = true;
					((Card) spell).dealNonCombatDamageTo(g, creature, 2);
				}
			}
			
			if (bControllerHasWhiteCreature)
				((Card) spell).dealNonCombatDamageTo(g, spell.getController(g), 2);
			
			if (bOpponentHasWhiteCreature)
				return ((Card) spell).dealNonCombatDamageTo(g, spell.getController(g).getOpponent(), 2);	
		}
		
		return Response.OK;
	}
	
	/* Pyroclasm */
	public static Response pyroclasm(Game g, StackObject c) {
		for (Card creature : g.getBattlefield().getCreatures())
			((Card) c).dealNonCombatDamageTo(g, creature, 2);
		return Response.OK;
	}
	
	/* living_weapon */
	public static Response living_weapon(Game g, StackObject ta) {
		Card equipment = ta.getSource();
		Card germ = g.createSingleToken(Token.BLACK_GERM_00, equipment);
		g.attach(equipment, germ);
		return Response.OK;
	}
	
	/* shardPhoenix_sac */
	public static Response shardPhoenix_sac(Game g, StackObject aa) {
		Card phoenix = aa.getSource();
		
		for (Card creature : g.getBattlefield().getCreatures()) {
			if (!creature.hasEvergreenGlobal(Evergreen.FLYING, g))
				phoenix.dealNonCombatDamageTo(g, creature, 2);
		}
		return Response.OK;
	}

	/* Disfigure */
	public static Response disfigure(Game g, StackObject c) {
		Card target = (Card) c.getTargetObject(0);
		target.addModifiers(new PTModifier(c, "-2/-2", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
		return Response.OK;
	}
	
	/* graspOfDarkness */
	public static Response graspOfDarkness(Game g, StackObject c) {
		Card target = (Card) c.getTargetObject(0);
		target.addModifiers(new PTModifier(c, "-4/-4", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
		return Response.OK;
	}
	
	/* phyrexianPlaguelord_1 */
	public static Response phyrexianPlaguelord_1(Game g, StackObject c) {
		Card target = (Card) c.getTargetObject(0);
		target.addModifiers(new PTModifier(c, "-4/-4", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
		return Response.OK;
	}
	
	/* Exclude */
	public static Response exclude(Game g, StackObject c) {
		Response ret = Response.OK;
		Card target = (Card) c.getTargetObject(0);
		g.counter(target);
		
		if (c.getStep() == 1) {
			c.advanceStep();
			ret = g.drawCards(c.getController(g), 1);
		}
		return ret;
	}

	/* vampiricEmbrace_addCounter */
	public static Response vampiricEmbrace_addCounter(Game g, StackObject ta) {
		Card aura = ta.getSource();
		Card host = aura.getHost();
		host.addCounter(g, CounterType.PLUS_ONE, 1);
		return Response.OK;
	}
	
	/* spellQueller_enters */
	public static Response spellQueller_enters(Game g, StackObject ta) {
		Card target = (Card) ta.getTargetObject(0);
		Card queller = ta.getSource();
		if (!target.isCopy()) {
			queller.addLinkedCard(target);
			return g.move_STK_to_EXL(target);
		}
		return Response.OK;
	}
	
	/* spellQueller_leaves */
	public static Response spellQueller_leaves(Game g, StackObject ta) {
		TriggeredAbility ability = (TriggeredAbility) ta;
		int step = ta.getStep();
		
		// Retrieve exiled card if any
		Card exiledCard = ability.getSource().getLinkedCards().get(0);

		// Do nothing if no card was exiled
		if (exiledCard == null)
			return Response.OK;

		Player owner = exiledCard.getOwner();
		
		if (step == 1) {
			owner.setState(State.PromptCastWithoutPayingManaCost);
			ta.advanceStep();
			return Response.MoreStep;
		}
		else if (step == 2) {
			ability.getSource().addLinkedCard(null);
			if (g.getAnswer() == Answer.Yes) {
				ta.advanceStep();
				g.getStack().removeObject(ta);
				if (g.castWithoutPayingManaCost(exiledCard) == Response.OK)
					return Response.MoreStep;
				else
					return Response.OK;
			}
		}
		return Response.OK;
	}
	
	/* fiendHunter_enters */
	public static Response fiendHunter_enters(Game g, StackObject ta) {
		Card target = (Card) ta.getTargetObject(0);
		if (!target.isToken())
			ta.getSource().addLinkedCard(target);	
		return g.move_BFD_to_EXL(target);
	}
	
	/* fiendHunter_leaves */
	public static Response fiendHunter_leaves(Game g, StackObject ta) {
		Card hunter = ta.getSource();
		if (hunter.getLinkedCards().isEmpty())
			return Response.OK;
		
		Card exiledcard = hunter.getLinkedCards().get(0);
		if (exiledcard != null) {
			g.move_EXL_to_BFD(exiledcard);
			hunter.addLinkedCard(null);
		}	
		return Response.OK;
	}
	
	/* Put a +1/+1 counter on target creature */
	public static Response bondBeetle(Game g, StackObject c) {
		Card target = (Card) c.getTargetObject(0);
		target.addCounter(g, CounterType.PLUS_ONE, 1);
		return Response.OK;
	}
	
	/* eldraziDisplacer */
	public static Response eldraziDisplacer(Game g, StackObject aa) {
		Card target = (Card) aa.getTargetObject(0);
		target = g.blink(target);
		target.tap(g);
		return Response.OK;
	}
	
	/* felidarGuardian */
	public static Response felidarGuardian(Game g, StackObject ta) {
		g.blink((Card) ta.getTargetObject(0));
		return Response.OK;
	}
	
	/* restorationAngel */
	public static Response restorationAngel(Game g, StackObject ta) {
		g.blink((Card) ta.getTargetObject(0));
		return Response.OK;
	}
	
	/* Plague Spores */
	public static Response plagueSpores(Game g, StackObject c) {
		g.destroy((Card) c.getTargetObject(0), false);
		g.destroy((Card) c.getTargetObject(1), false);
		return Response.OK;
	}
	
	/* hibernationSliver_return */
	public static Response hibernationSliver_return(Game g, StackObject c) {
		return g.move_BFD_to_HND(c.getSource());
	}
	
	/* Unsummon */
	public static Response unsummon(Game g, StackObject c) {
		return g.move_BFD_to_HND((Card) c.getTargetObject(0));
	}
	
	/*  karakas */
	public static Response karakas(Game g, StackObject c) {
		return g.move_BFD_to_HND((Card) c.getTargetObject(0));
	}
	
	/* wizardMentor */
	public static Response wizardMentor(Game g, StackObject aa) {
		g.move_BFD_to_HND((Card) aa.getTargetObject(0));
		g.move_BFD_to_HND(aa.getSource());
		return Response.OK;
	}
		
	/* Repulse */
	public static Response repulse(Game g, StackObject c) {
		Response ret = Response.OK;
		Card target = (Card) c.getTargetObject(0);
		g.move_BFD_to_HND(target);
		
		if (c.getStep() == 1) {
			c.advanceStep();
			ret = g.drawCards(c.getController(g), 1);
		}
		return ret;
	}

	/* explores */
	public static Response explores(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		Card creature = ta.getSource();
		Card topCard = controller.getLibrary().getTopCard();
		int step = ta.getStep();
		
		if (step == 1) {
			g.queueExploresEffects(creature);
			ta.setLibManip(controller.getLibrary().getXTopCards(1));
			ta.advanceStep();
			// 1. reveal top card of library
			System.out.println(controller + " reveals " + topCard);
			
			// 2. test if it's a land
			if (topCard.isLandCard()) {
				// 2.a Yes it's land, put it in hand
				g.move_LIB_to_HND(topCard);
				return Response.OK;
			}
			else {
				// 2.b1 No it's not a land, put a +1/+1 counter on the creature
				creature.addCounter(g, CounterType.PLUS_ONE, 1);
				
				// 2.b2 prompt if the player wants to leave the card on top or put it in the graveyard
				controller.setState(State.PromptDoYouWantPutInGraveyard);
				return Response.MoreStep;
			}
		}
		else { // if (step == 2)
			ta.advanceStep();
			if (g.getAnswer() == Answer.Yes) {
				g.move_LIB_to_GYD(topCard);
			}
			return Response.OK;
		}
	}
	
	/* exploresAgain */
	public static Response exploresAgain(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		Card creature = ta.getSource();
		Card topCard = controller.getLibrary().getTopCard();
		int step = ta.getStep();
		
		if (step == 3) {
			g.queueExploresEffects(creature);
			ta.setLibManip(controller.getLibrary().getXTopCards(1));
			ta.advanceStep();
			// 1. reveal top card of library
			System.out.println(controller + " reveals " + topCard);
			
			// 2. test if it's a land
			if (topCard.isLandCard()) {
				// 2.a Yes it's land, put it in hand
				g.move_LIB_to_HND(topCard);
				return Response.OK;
			}
			else {
				// 2.b1 No it's not a land, put a +1/+1 counter on the creature
				creature.addCounter(g, CounterType.PLUS_ONE, 1);
				
				// 2.b2 prompt if the player wants to leave the card on top or put it in the graveyard
				controller.setState(State.PromptDoYouWantPutInGraveyard);
				return Response.MoreStep;
			}
		}
		else { // if (step == 4)
			ta.advanceStep();
			if (g.getAnswer() == Answer.Yes) {
				g.move_LIB_to_GYD(topCard);
			}
			return Response.OK;
		}
	}
	
	/* jadelightRanger */
	public static Response jadelightRanger(Game g, StackObject ta) {
		Response ret = Response.Error;
		
		if (ta.getStep() == 1) {
			ret = explores(g, ta);
			if (ret == Response.OK) // first explore is a land, skip to second explore
				ta.goToStep(3);
			else // first explore is a nonland
				return Response.MoreStep;
		}
		
		if (ta.getStep() == 2) {
			explores(g, ta);
		}
		
		if (ta.getStep() == 3) {
			ret = exploresAgain(g, ta);
			if (ret == Response.OK) // second explore is a land
				return Response.OK;
			else // second explore is a nonland
				return Response.MoreStep;
		}
		
		if (ta.getStep() == 4) {
			exploresAgain(g, ta);
			ret = Response.OK;
		}
		return ret;
	}
	
	/* wildgrowthWalker */
	public static Response wildgrowthWalker(Game g, StackObject ta) {
		Card walker = ta.getSource();
		Player controller = ta.getController(g);
		
		walker.addCounter(g, CounterType.PLUS_ONE, 1);
		controller.gainLife(3);
		return Response.OK;
	}
	
	/* reflectorMage */
	public static Response reflectorMage(Game g, StackObject ta) {
		Card target = (Card) ta.getTargetObject(0);
		Player opponent = target.getController(g);
		
		// 1. bounce creature
		g.move_BFD_to_HND(target);
		
		// 2. Prevent owner to cast it again until next turn
		String cardName = target.getName();
		if (cardName != null) {
			SpellCastingModifier mod = new SpellCastingModifier(ta.getSource(), Modifier.Duration.UNTIL_YOUR_NEXT_TURN);
			mod.addForbiddenSpellName(cardName);
			opponent.addModifiers(mod);
		}
		return Response.OK;
	}
	
	/* batterskull_return */
	public static Response batterskull_return(Game g, StackObject aa) {
		Card batterskull = aa.getSource();
		return g.move_BFD_to_HND(batterskull);
	}
	
	/* Erase */
	public static Response erase(Game g, StackObject c) {
		return g.move_BFD_to_EXL((Card) c.getTargetObject(0));
	}
	
	/* electryte */
	public static Response electryte(Game g, StackObject ta) {
		Card electryte = ta.getSource();
		Player defendingPlayer = electryte.getController(g).getOpponent();
		int power = electryte.getPower(g);
		
		for (Card creature : g.getBattlefield().getCreaturesControlledBy(defendingPlayer))
			if (creature.isBlocking(g))
				electryte.dealNonCombatDamageTo(g, creature, power);
		return Response.OK;
	}
	
	/* awaken */
	private static void doAwaken(Game g, Card spell, int nbTarget) {
		if (spell.getSpellCastUsed().getOption() == Option.CAST_WITH_AWAKEN)
		{
			Card targetLand = (Card) spell.getTargetObject(nbTarget);
			targetLand.addCounter(g, CounterType.PLUS_ONE, Integer.parseInt(spell.getStaticAbility("awaken").getParameter()));
			targetLand.addModifiers(new CardTypeModifier(spell, Operation.ADD, Modifier.Duration.PERMANENTLY, CardType.CREATURE),
					                new PTModifier(spell, "0/0", Operation.SET, Modifier.Duration.PERMANENTLY),
					                new CreatureTypeModifier(spell, Modifier.Duration.PERMANENTLY, CreatureType.Elemental),
					                new EvergreenModifier(spell, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.HASTE));
		}
	}
	
	/* planarOutburst */
	public static Response planarOutburst(Game g, StackObject so) {
		Card spell = (Card) so;
		int i = 0;
		Vector<Card> creatures = g.getBattlefield().getCreatures();
		while (i < creatures.size()) {
			Card creature = creatures.get(i);
			if (!creature.isLand(g)) {
				if (g.destroy(creature, true) == Response.OK)
					i--;
			}
			i++;
		}
		doAwaken(g, spell, 0);
		return Response.OK;
	}

	/* ruinousPath */
	public static Response ruinousPath(Game g, StackObject so) {
		Card spell = (Card) so;
		Card target_creatureOrPlaneswalker = (Card) spell.getTargetObject(0);
		g.destroy(target_creatureOrPlaneswalker, true);
		doAwaken(g, spell, 1);
		return Response.OK;
	}

	/* scatterToTheWinds */
	public static Response scatterToTheWinds(Game g, StackObject so) {
		Card spell = (Card) so;
		Card target_spell = (Card) spell.getTargetObject(0);
		g.counter(target_spell);
		doAwaken(g, spell, 1);
		return Response.OK;
	}
	
	/* phyrexianPlaguelord_2 */
	public static Response phyrexianPlaguelord_2(Game g, StackObject aa) {
		Card target = (Card) aa.getTargetObject(0);
		target.addModifiers(new PTModifier(aa, "-1/-1", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
		return Response.OK;
	}
	
	/* arcboundRavager_sac */
	public static Response arcboundRavager_sac(Game g, StackObject aa) {
		aa.getSource().addCounter(g, CounterType.PLUS_ONE, 1);
		return Response.OK;
	}
	
	/* falkenrathAristocrat */
	public static Response falkenrathAristocrat(Game g, StackObject aa) {
		Card sacrificedCreature, aristocrat;

		aristocrat = aa.getSource();
		aristocrat.addModifiers(new EvergreenModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.INDESTRUCTIBLE));
		sacrificedCreature = (Card) aa.getAdditionalData();
		if (sacrificedCreature.hasCreatureType(g, CreatureType.Human))
			aristocrat.addCounter(g, CounterType.PLUS_ONE, 1);
		return Response.OK;
	}

	/* nantukoHusk */
	public static Response nantukoHusk(Game g, StackObject aa) {
		aa.getSource().addModifiers(new PTModifier(aa, "+2/+2", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
		return Response.OK;
	}
	
	/* misguidedRage */
	public static Response misguidedRage(Game g, StackObject spell) {
		Player target = (Player) spell.getTargetObject(0);
		int step = spell.getStep();
		
		if (step == 1) {
			// Check if target controls at least one permanent he can sacrifice
			Vector<Card> permanents = g.getBattlefield().getPermanentsControlledBy(target);
			if (permanents.size() == 0)
				return Response.OK;
			
			target.setState(Game.State.WaitSacrificePermanent);
			spell.advanceStep();
			return Response.MoreStep;
		}
		else if (step == 2)
		{
			if (g.validateChoices())
				return g.sacrifice(target, (Card) g.getChoices().get(0));
			return Response.MoreStep;
		}
		return Response.OK;
	}
	
	/* hatred */
	public static Response hatred(Game g, StackObject spell) {
		Card target = (Card) spell.getTargetObject(0);
		int X = spell.getXValue();
		target.addModifiers(new PTModifier(spell, String.format("+%d/+0", X), Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
		return Response.OK;
	}
	
	/* angelicPage */
	public static Response angelicPage(Game g, StackObject aa) {
		Card target = (Card) aa.getTargetObject(0);
		target.addModifiers(new PTModifier(aa.getSource(), "+1/+1", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
		return Response.OK;
	}
	
	/* academyResearchers */
	public static Response academyResearchers(Game g, StackObject ta) {
		int step = ta.getStep();
		
		if (step == 1)
		{
			ta.getController(g).setState(State.WaitChoiceAcademyResearchers);
			ta.advanceStep();
			return Response.MoreStep;
		}
		else if (step == 2)
		{
			if (g.validateChoices() == true)
			{
				if ((g.getChoices() != null) && (g.getChoices().size() == 1)) {
					Card aura = (Card) g.getChoices().get(0);
					g.move_HND_to_BFD(aura);
					Card host = ta.getSource(); 
					host.attachLocalPermanent(aura);
				}
				return Response.OK;
			}
			else
				return Response.MoreStep;
		}
		return Response.OK;
	}
	
	/* elvishHerder */
	public static Response elvishHerder(Game g, StackObject aa) {
		Card target = (Card) aa.getTargetObject(0);
		target.addModifiers(new EvergreenModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.TRAMPLE));
		return Response.OK;
	}
	
	/* unnerve */
	public static Response unnerve(Game g, StackObject spell) {
		int step = spell.getStep();
		Player opponent = spell.getController(g).getOpponent();
		if (opponent.getHandSize() == 0)
			return Response.OK;
		
		if (step == 1) {
			opponent.setState(Game.State.WaitDiscard);
			spell.advanceStep();
			return Response.MoreStep;
		}
		else if (step == 2)
		{
			if (g.validateChoices()) {
				spell.advanceStep();
				g.discard((Card) g.getChoices().get(0));
				if (opponent.getHandSize() == 0)
					return Response.OK;
			}
			return Response.MoreStep;
		}
		else // if (step == 3)
		{
			if (g.validateChoices())
				return g.discard((Card) g.getChoices().get(0));
			else
				return Response.MoreStep;
		}
	}
	
	/* mishrasHelix */
	public static Response mishrasHelix(Game g, StackObject aa) {
		int X = aa.getXValue();
		for (int i = 0; i < X; i++)
			((Card) aa.getTargetObject(i)).tap(g);
		return Response.OK;
	}
	
	/* abyssalHorror */
	public static Response abyssalHorror(Game g, StackObject ta) {
		int step = ta.getStep();
		Player target = (Player) ta.getTargetObject(0);
		if (target.getHandSize() == 0)
			return Response.OK;
		
		if (step == 1) {
			target.setState(Game.State.WaitDiscard);
			ta.advanceStep();
			return Response.MoreStep;
		}
		else if (step == 2)
		{
			if (g.validateChoices()) {
				ta.advanceStep();
				g.discard((Card) g.getChoices().get(0));
				if (target.getHandSize() == 0)
					return Response.OK;
			}
			return Response.MoreStep;
		}
		else // if (step == 3)
		{
			if (g.validateChoices())
				return g.discard((Card) g.getChoices().get(0));
			else
				return Response.MoreStep;
		}
	}
	
	/* carrionBeetles */
	public static Response carrionBeetles(Game g, StackObject aa) {
		for (int i = 0; i < 3; i++) {
			if (aa.getTargetObject(i) != null)
				g.move_GYD_to_EXL((Card) aa.getTargetObject(i));
		}
		return Response.OK;
	}
	
	/* cacklingFiend */
	public static Response cacklingFiend(Game g, StackObject ta) {
		Player opponent = ta.getController(g).getOpponent();
		if (ta.getStep() == 1) {
			// Check if opponent has at least one card in hand
			int handSize = opponent.getHandSize();
			if (handSize == 0)
				return Response.OK;
			
			opponent.setState(Game.State.WaitDiscard);
			ta.advanceStep();
			return Response.MoreStep;
		}
		else // if (step == 2)
		{
			if (g.validateChoices())
				return g.discard((Card) g.getChoices().get(0));
			return Response.MoreStep;
		}
	}
	
	/* orderOfYawgmoth */
	public static Response orderOfYawgmoth(Game g, StackObject ta) {
		Player victim = (Player) ((TriggeredAbility) ta).getAdditionalData();
				
		if (ta.getStep() == 1) {
			// Check if target has at least one card in hand
			int handSize = victim.getHandSize();
			if (handSize == 0)
				return Response.OK;
			
			victim.setState(Game.State.WaitDiscard);
			ta.advanceStep();
			return Response.MoreStep;
		}
		else // if (step == 2)
		{
			if (g.validateChoices())
				return g.discard((Card) g.getChoices().get(0));
			return Response.MoreStep;
		}
	}
	
	/* mindPeel */
	public static Response mindPeel(Game g, StackObject spell) {
		Player target = (Player) spell.getTargetObject(0);
		
		if (spell.getStep() == 1) {
			// Check if target has at least one card in hand
			int handSize = target.getHandSize();
			if (handSize == 0)
				return Response.OK;
			
			target.setState(Game.State.WaitDiscard);
			spell.advanceStep();
			return Response.MoreStep;
		}
		else // if (step == 2)
		{
			if (g.validateChoices())
				return g.discard((Card) g.getChoices().get(0));
			return Response.MoreStep;
		}
	}
	
	/*  glacialChasm_sacAland */
	public static Response glacialChasm_sacAland(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		int step = ta.getStep();
				
		if (step == 1) {
			// Check that, at resolution, there is still at least one land
			Vector<Card> lands = g.getBattlefield().getLandsControlledBy(controller);
			if (lands.size() == 0) {
				return Response.OK;
			}
			
			controller.setState(Game.State.WaitSacrificeLand);
			ta.advanceStep();
			return Response.MoreStep;
		}
		else if (step == 2)
		{
			if (g.validateChoices())
				g.sacrifice(controller, (Card) g.getChoices().get(0));
			else
				return Response.MoreStep;
		}
		return Response.OK;
	}
	
	/* diabolicEdict */
	public static Response diabolicEdict(Game g, StackObject spell) {
		Player target = (Player) spell.getTargetObject(0);
		int step = spell.getStep();
		
		if (step == 1) {
			// Check if target controls at least one creature he can sacrifice
			Vector<Card> creatures = g.getBattlefield().getCreaturesControlledBy(target);
			if (creatures.size() == 0) {
				return Response.OK;
			}
			
			target.setState(Game.State.WaitSacrificeCreature);
			spell.advanceStep();
			return Response.MoreStep;
		}
		else if (step == 2)
		{
			if (g.validateChoices())
				g.sacrifice(target, (Card) g.getChoices().get(0));
			else
				return Response.MoreStep;
		}
		return Response.OK;
	}
	
	/* parallaxWave_exile */
	public static Response parallaxWave_exile(Game g, StackObject aa) {
		Card target = (Card) aa.getTargetObject(0);
		Response ret = g.move_BFD_to_EXL(target);
		if (ret == Response.OK)
			target.addLinkedCard(aa.getSource());;
		return Response.OK;
	}
	
	/* parallaxTide_exile */
	public static Response parallaxTide_exile(Game g, StackObject aa) {
		return parallaxWave_exile(g, aa);
	}
	
	/* parallaxWave_leaves */
	public static Response parallaxWave_leaves(Game g, StackObject aa) {
		Vector<Player> players = g.getPlayers();
		Vector<MtgObject> exiledObjects = new Vector<MtgObject>();
		Card exiledCard;
		
		for (Player p : players) {
			exiledObjects.addAll(p.getExile().getObjects());
			for (MtgObject obj : exiledObjects) {
				if (obj.getClass() == Card.class) {
					exiledCard = (Card) obj;
					if (exiledCard.getLinkedCards().get(0) == aa.getSource()) {
						exiledCard.clearLinkedCards();
						g.move_EXL_to_BFD(exiledCard);
					}
				}
			}
		}
		return Response.OK;
	}
	
	/* parallaxTide_leaves */
	public static Response parallaxTide_leaves(Game g, StackObject aa) {
		return parallaxWave_leaves(g, aa);
	}
	
	/* scourFromExistence */
	public static Response scourFromExistence(Game g, StackObject c) {
		return g.move_BFD_to_EXL((Card) c.getTargetObject(0));
	}
	
	/* persecute */
	public static Response persecute(Game g, StackObject spell) {
		Player controller = spell.getController(g);
		Player target = (Player) spell.getTargetObject(0);
		int choice = spell.getXValue();
		Color chosenColor;
		
		if (spell.getStep() == 1) {
			spell.advanceStep();
			controller.setState(State.WaitChooseColor);
			return Response.MoreStep;
		}
		
		if (spell.getStep() == 2) {
			if ((choice >= 1) && (choice <= 5)) {
				switch (choice) {
				case 1:
					chosenColor = Color.WHITE;
					break;
					
				case 2:
					chosenColor = Color.BLUE;
					break;
					
				case 3:
					chosenColor = Color.BLACK;
					break;
					
				case 4:
					chosenColor = Color.RED;
					break;
					
				case 5:
					chosenColor = Color.GREEN;
					break;
					
				default: // should never get here
					return Response.Error;
				}
				int handSize = target.getHandSize();
				if (handSize > 0) {
					String text = target.getName() + " reveals "; 
					Vector<Card> discarded = new Vector<Card>();
					Vector<Card> hand = target.getHand().getCards();
					Card c;
					for (int i = 0; i < handSize; i++) {
						c = hand.get(i);
						text += c.getName();
						if (i < handSize-1)
							text += ", ";	
						if (c.hasColor(chosenColor))
							discarded.add(c);
					}
					System.out.println(text + ".");
					for (Card d : discarded)
						g.discard(d);
					return Response.OK;
				}
			}
			return Response.MoreStep;
		}
		return Response.OK;
	}
	
	/* pathOfPeace */
	public static Response pathOfPeace(Game g, StackObject c) {
		Card target = (Card) c.getTargetObject(0);
		Player owner = target.getOwner(); 
		g.destroy(target, true);
		owner.gainLife(4);
		return Response.OK;
	}

	/* swordsToPlowshares */
	public static Response swordsToPlowshares(Game g, StackObject c) {
		Card target = (Card) c.getTargetObject(0);
		int targetPower = target.getPower(g);
		Player controller = target.getController(g);
		Response r = g.move_BFD_to_EXL(target);
		if (r == Response.OK)
			controller.gainLife(targetPower);
		return r;
	}
	
	/* wildfire */
	public static Response wildfire(Game g, StackObject spell) {
		Player controller = spell.getController(g);
		Player opponent = controller.getOpponent();
		Card wildfire = (Card) spell;

		if (spell.getStep() == 1) {
			if (controller.getNbLandsControlled() == 0)
				spell.goToStep(5);
			else {
				spell.advanceStep();
				controller.setState(State.WaitSacrificeLand);
				return Response.MoreStep;
			}
		}
		
		if ((spell.getStep() >= 2) && (spell.getStep() < 5)) {
			if (g.validateChoices()) {
				g.sacrifice(controller, (Card) g.getChoices().get(0));
				if (controller.getNbLandsControlled() == 0)
					spell.goToStep(5);
				else {
					spell.advanceStep();
					return Response.MoreStep;
				}	
			}
			else
				return Response.MoreStep;
		}
		
		if (spell.getStep() == 5) {
			if (opponent.getNbLandsControlled() == 0)
				spell.goToStep(9);
			else {
				spell.advanceStep();
				opponent.setState(State.WaitSacrificeLand);
				return Response.MoreStep;
			}
		}
		
		if ((spell.getStep() >= 6) && (spell.getStep() < 9)) {
			if (g.validateChoices()) {
				g.sacrifice(opponent, (Card) g.getChoices().get(0));
				if (opponent.getNbLandsControlled() == 0)
					spell.goToStep(9);
				else {
					spell.advanceStep();
					return Response.MoreStep;
				}	
			}
			else
				return Response.MoreStep;
		}
		
		if (spell.getStep() == 9) {
			for (Card creature : g.getBattlefield().getCreatures())
				wildfire.dealNonCombatDamageTo(g, creature, 4);
		}
		return Response.OK;
	}
	
	/* desolationAngel */
	public static Response desolationAngel(Game g, StackObject ta) {
		Vector<Card> lands;
		
		if (ta.getSource().getSpellCastUsed().getOption() == Option.CAST_WITH_KICKER)
			lands = g.getBattlefield().getLands();
		else
			lands = g.getBattlefield().getLandsControlledBy(ta.getController(g));
		
		for (Card land : lands)
			g.destroy(land, true);
		return Response.OK;
	}
	
	/* graveTitan */
	public static Response graveTitan(Game g, StackObject ta) {
		g.createTokens(Token.BLACK_ZOMBIE_22, 2, ta.getSource());
		return Response.OK;
	}
	
	/* sunTitan */
	public static Response sunTitan(Game g, StackObject ta) {
		return g.move_GYD_to_BFD((Card) ta.getTargetObject(0));
	}
	
	/* attunement */
	public static Response attunement(Game g, StackObject spell) {
		int step = spell.getStep();
		Response ret = Response.OK;
		Player controller = spell.getController(g);
		int nbCardsToDraw = 3;
		int nbCardsToDiscard = 4;
		
		if (step == 1) {
			spell.advanceStep();
			ret = g.drawCards(controller, nbCardsToDraw);
		}
		
		if (step == 2) {
			spell.advanceStep();
			controller.setState(State.WaitDiscard);
			return Response.MoreStep;
		}
		
		if ((step >= 3) && (step < 3 + nbCardsToDiscard)) {
			if (g.validateChoices()) 			{
				Card chosenCard = (Card) g.getChoices().get(0);
				g.discard(chosenCard);

				if (step < 2 + nbCardsToDiscard) {
					spell.advanceStep();
					ret = Response.MoreStep;
				}
				else
					ret = Response.OK;
			}
			else
				return Response.MoreStep;
		}
		return ret;
	}
	
	/* rewind */
	public static Response rewind(Game g, StackObject spell) {
		int step = spell.getStep();
		Player controller = spell.getController(g);
		int nbLandsToUntap = 4;
		
		if (step == 1) {
			g.counter((StackObject) spell.getTargetObject(0));
			spell.advanceStep();
			controller.setState(State.WaitUntapLand);
			return Response.MoreStep;
		}
		
		if ((step >= 2) && (step < 2 + nbLandsToUntap)) {
			if (g.validateChoices()) {
				Vector<MtgObject> choices = g.getChoices();
				if (choices.size() == 0)
					return Response.OK;
				Card chosenLand = (Card) g.getChoices().get(0);
				chosenLand.untap(g);
				
				if (step < 1 + nbLandsToUntap) {
					spell.advanceStep();
					return Response.MoreStep;
				}
				else
					return Response.OK;
			}
			else
				return Response.MoreStep;
		}
		return Response.OK;
	}
	
	/* peregrineDrake */
	public static Response peregrineDrake(Game g, StackObject spell) {
		Player controller = spell.getController(g);
		int nbLandsToUntap = 5;
		int step = spell.getStep();
		
		if (step == 1) {
			spell.advanceStep();
			controller.setState(State.WaitUntapLand);
			return Response.MoreStep;
		}
		
		if ((step >= 2) && (step < 2 + nbLandsToUntap)) {
			if (g.validateChoices()) {
				Vector<MtgObject> choices = g.getChoices();
				if (choices.size() == 0)
					return Response.OK;
				Card chosenLand = (Card) g.getChoices().get(0);
				chosenLand.untap(g);
				
				if (step < 1 + nbLandsToUntap) {
					spell.advanceStep();
					return Response.MoreStep;
				}
				else
					return Response.OK;
			}
			else
				return Response.MoreStep;
		}
		return Response.OK;
	}
	
	/* greatWhale */
	public static Response greatWhale(Game g, StackObject ta) {
		int step = ta.getStep();
		Player controller = ta.getController(g);
		int nbLandsToUntap = 7;
		
		if (step == 1) {
			ta.advanceStep();
			controller.setState(State.WaitUntapLand);
			return Response.MoreStep;
		}
		
		if ((step >= 2) && (step < 2 + nbLandsToUntap)) {
			if (g.validateChoices()) {
				Vector<MtgObject> choices = g.getChoices();
				if (choices.size() == 0)
					return Response.OK;
				Card chosenLand = (Card) g.getChoices().get(0);
				chosenLand.untap(g);
				
				if (step < 1 + nbLandsToUntap) {
					ta.advanceStep();
					return Response.MoreStep;
				}
				else
					return Response.OK;
			}
			else
				return Response.MoreStep;
				
		}
		return Response.OK;
	}
	
	/* franticSearch */
	public static Response franticSearch(Game g, StackObject spell) {
		Player controller = spell.getController(g);
		int nbCardsToDraw = 2;
		int nbCardsToDiscard = 2;
		int nbLandsToUntap = 3;
		int step = spell.getStep();
		
		if (step == 1) {
			spell.advanceStep();
			return g.drawCards(controller, nbCardsToDraw);
		}
		
		if (step == 2) {
			spell.advanceStep();
			controller.setState(State.WaitDiscard);
			return Response.MoreStep;
		}
		
		if ((step >= 3) && (step < 3 + nbCardsToDiscard)) {
			if (g.validateChoices()) {
				g.discard((Card) g.getChoices().get(0));

				if (step < 2 + nbCardsToDiscard)
					spell.advanceStep();
				else {
					spell.goToStep(10);
					controller.setState(State.WaitUntapLand);
				}
				return Response.MoreStep;
			}
			else
				return Response.MoreStep;
		}
		
		if ((step >= 10) && (step < 10 + nbLandsToUntap)) {
			if (g.validateChoices()) {
				Vector<MtgObject> choices = g.getChoices();
				if (choices.size() == 0)
					return Response.OK;
				Card chosenLand = (Card) g.getChoices().get(0);
				chosenLand.untap(g);
				
				if (step < 9 + nbLandsToUntap) {
					spell.advanceStep();
					return Response.MoreStep;
				}
				else
					return Response.OK;
			}
			else
				return Response.MoreStep;
				
		}
		return Response.OK;
	}
	
	/* argothianWurm */
	public static Response argothianWurm(Game g, StackObject ta, int unused) {
		Player activePlayer = g.getActivePlayer();
		Player nonActivePlayer = activePlayer.getOpponent();
		Card wurm = ta.getSource();
				
		// Return immediately if no lands are on the battlefield
		if (g.getBattlefield().getLands().size() == 0)
			return Response.OK;
		
		if (ta.getStep() == 1) {
			//check active player number of lands
			int nbLands = g.getBattlefield().getLandsControlledBy(activePlayer).size();
			
			// prompt active player to sac
			if (nbLands > 0) {
				activePlayer.setState(State.PromptDoYouWantToSacrificeALand);
				ta.advanceStep();
				return Response.MoreStep;
			}
			else
				ta.goToStep(4);
		}
		
		if (ta.getStep() == 2) {
			// active player decided to sac
			if (g.getAnswer() == Answer.Yes) {
				activePlayer.setState(State.WaitSacrificeLand);
				ta.advanceStep();
				return Response.MoreStep;
			}
			else
				ta.goToStep(4);

		}
		
		// sacrifice the chosen land
		if (ta.getStep() == 3) {
			if (g.validateChoices()) {
				g.sacrifice(activePlayer, (Card) g.getChoices().get(0));
				g.move_BFD_to_TOPLIB(wurm);
				ta.advanceStep();
			}
			else
				return Response.MoreStep;
		}
		
		// prompt non active player to sac
		if (ta.getStep() == 4) {
			//check non active player number of lands
			int nbLands = g.getBattlefield().getLandsControlledBy(nonActivePlayer).size();
			
			// prompt non active player to sac
			if (nbLands > 0) {
				nonActivePlayer.setState(State.PromptDoYouWantToSacrificeALand);
				ta.advanceStep();
				return Response.MoreStep;
			}
			else
				return Response.OK;
		}
		
		if (ta.getStep() == 5) {
			// non active player decided to sac
			if (g.getAnswer() == Answer.Yes) {
				nonActivePlayer.setState(State.WaitSacrificeLand);
				ta.advanceStep();
				return Response.MoreStep;
			}
			else
				return Response.OK;
		}
		
		// sacrifice the chosen land
		if (ta.getStep() == 6) {
			if (g.validateChoices()) {
				g.sacrifice(nonActivePlayer, (Card )g.getChoices().get(0));
				g.move_BFD_to_TOPLIB(wurm);
				return Response.OK;
			}
			else
				return Response.MoreStep;
		}
		return Response.OK;
	}
	
	/* argothianElder */
	public static Response argothianElder(Game g, StackObject aa) {
		Card land1 = (Card) aa.getTargetObject(0);
		Card land2 = (Card) aa.getTargetObject(1);
		land1.untap(g);
		land2.untap(g);
		return Response.OK;
	}
	
	/* victimize */
	public static Response victimize(Game g, StackObject spell) {
		Player controller = spell.getController(g);
		
		// if the controller has no creature to sacrifice, immediately return
		if (controller.getNbCreaturesControlled() == 0)
			return Response.OK;
		
		if (spell.getStep() == 1) {
			spell.advanceStep();
			controller.setState(State.WaitSacrificeCreature);
			return Response.MoreStep;
		}
		
		if (spell.getStep() == 2) {
			if (g.validateChoices()) {
				g.sacrifice(controller, (Card) g.getChoices().get(0));
				Card target1 = (Card) spell.getTargetObject(0);
				Card target2 = (Card) spell.getTargetObject(1);
				g.move_GYD_to_BFD(target1);
				g.move_GYD_to_BFD(target2);
				target1.tap(g);
				target2.tap(g);
			}
			else
				return Response.MoreStep;
		}
		return Response.OK;
	}
	
	/* recurringNightmare */
	public static Response recurringNightmare(Game g, StackObject aa) {
		return g.move_GYD_to_BFD((Card) aa.getTargetObject(0));
	}
	
	/* diabolicServitude_reanimate : Servitude enters the battlefield, reanimate target */
	public static Response diabolicServitude_reanimate(Game g, StackObject ta) {
		Response ret;
		
		Card target = (Card) ta.getTargetObject(0);
		Card diabServ = ta.getSource();
		ret = g.move_GYD_to_BFD(target);
		diabServ.clearLinkedCards();
		diabServ.addLinkedCard(target);
		return ret; 
	}
	
	/* diabolicServitude_return : reanimated creature dies -> exile it from grave and return Servitude to owner's hand */
	public static Response diabolicServitude_return(Game g, StackObject ta) {
		Card diabServ = ta.getSource();
		if (!diabServ.getLinkedCards().isEmpty())
			g.move_GYD_to_EXL(diabServ.getLinkedCards().get(0));
		g.move_BFD_to_HND(diabServ);
		return Response.OK;
	}
	
	/* diabolicServitude_exile : Serv leaves the battlefield : exile reanimated creature (if applicable) */
	public static Response diabolicServitude_exile(Game g, StackObject ta) {
		g.move_BFD_to_EXL(ta.getSource().getLinkedCards().get(0));
		return Response.OK;
	}
	
	/* Zombify */
	public static Response zombify(Game g, StackObject c) {
		Player controller = c.getController(g);
		Card target = (Card) c.getTargetObject(0);
		g.move_GYD_to_BFD(target);
		target.setController(g, controller);
		return Response.OK;
	}
	
	/* lilianaDN_2 */
	public static Response lilianaDN_2(Game g, StackObject aa) {
		return g.move_GYD_to_BFD((Card) aa.getTargetObject(0));
	}

	/* curfew */
	public static Response curfew(Game g, StackObject spell) {
		Player controller = spell.getController(g);
		Player opponent = controller.getOpponent();
		int step = spell.getStep();
		
		// prompt player 1
		if (step == 1) {
			if (controller.getNbCreaturesControlled() > 0) {
				controller.setState(State.WaitChoiceCreature);
				spell.advanceStep();
				return Response.MoreStep;
			}
			else
				step = 3;
		}
		
		// validate player 1
		if (step == 2) {
			if (g.validateChoices()) {
				g.move_BFD_to_HND((Card) g.getChoices().get(0));
				step = 3;
			}
			else
				return Response.MoreStep;
		}
		
		// prompt player 2
		if (step == 3) {
			if (opponent.getNbCreaturesControlled() > 0) {
				opponent.setState(State.WaitChoiceCreature);
				spell.advanceStep();
				return Response.MoreStep;
			}
			else
				return Response.OK;
		}
		
		// validate player 2
		if (step == 4) {
			if (g.validateChoices()) {
				g.move_BFD_to_HND((Card) g.getChoices().get(0));
				return Response.OK;
			}
			return Response.MoreStep;
		}
		return Response.OK;
	}
	
	/* delverOfSecrets */
	public static Response delverOfSecrets(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		Card delver = ta.getSource();
		
		if (controller.getLibrary().isEmpty())
			return Response.OK;
		
		Card topCard = controller.getLibrary().getTopCard();
		if (topCard.isSorceryCard() || topCard.isInstantCard())
			delver.transform(g);
		
		return Response.OK;
	}
	
	/* exhume */
	public static Response exhume(Game g, StackObject spell) {
		Player controller = spell.getController(g);
		Player opponent = controller.getOpponent();
		int step = spell.getStep();
		
		// prompt player 1
		if (step == 1) {
			if (controller.getGraveyard().getNbCreatures() > 0) {
				controller.setState(State.WaitChoiceExhume);
				spell.advanceStep();
				return Response.MoreStep;
			}
			else
				step = 3;
		}
		
		// validate player 1
		if (step == 2) {
			if (g.validateChoices()) {
				g.move_GYD_to_BFD((Card) g.getChoices().get(0));
				step = 3;
			}
			else
				return Response.MoreStep;
		}
		
		// prompt player 2
		if (step == 3) {
			if (opponent.getGraveyard().getNbCreatures() > 0) {
				opponent.setState(State.WaitChoiceExhume);
				spell.advanceStep();
				return Response.MoreStep;
			}
			else
				return Response.OK;
		}
		
		// validate player 2
		if (step == 4) {
			if (g.validateChoices()) {
				g.move_GYD_to_BFD((Card) g.getChoices().get(0));
				return Response.OK;
			}
			return Response.MoreStep;
		}
		return Response.OK;
	}
	
	/* volrathsStronghold */
	public static Response volrathsStronghold(Game g, StackObject aa) {
		Card target = (Card) aa.getTargetObject(0);
		return g.move_GYD_to_TOPLIB(target);
	}
	
	/* reanimate */
	public static Response reanimate(Game g, StackObject spell) {
		Player controller = spell.getController(g);
		Card target = (Card) spell.getTargetObject(0);
		Response ret = g.move_GYD_to_BFD(target);
		if (ret == Response.OK) {
			target.setController(g, controller);
			controller.loseLife(target.getConvertedManaCost(g));
		}
		return ret;
	}
	
	/* karmicGuide */
	public static Response karmicGuide(Game g, StackObject ta) {
		Card target = (Card) ta.getTargetObject(0);
		return g.move_GYD_to_BFD(target);
	}
	
	/* phyrexianArena */
	public static Response phyrexianArena(Game g, StackObject c) {
		Response ret = Response.OK;

		if (c.getStep() == 1) {
			c.advanceStep();
			c.getController(g).loseLife(1);
			ret = g.drawCards(c.getController(g), 1);
		}
		return ret;
	}
	
	/* coilingOracle */
	public static Response coilingOracle(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		Card topCard = controller.getLibrary().getTopCard();
		System.out.println(controller + " reveals " + topCard);
		if (topCard.isLandCard())
			g.move_LIB_to_BFD(topCard);
		else
			g.move_LIB_to_HND(topCard);
		return Response.OK;
	}
	
	/* harnessedLightning */
	public static Response harnessedLightning(Game g, StackObject spell) {
		Player controller = spell.getController(g);
		Card target = (Card) spell.getTargetObject(0);
		
		if (spell.getStep() == 1) {
			spell.advanceStep();
			controller.addCounter(CounterType.ENERGY, 3);
			controller.setState(State.WaitChoiceHarnessedLightning);
			return Response.MoreStep;
		}
		else {
			int number = spell.getXValue();
			if (number > controller.getNbCounters(CounterType.ENERGY))
				return Response.MoreStep;
			
			else {
				controller.payEnergy(number);
				((Card)spell).dealNonCombatDamageTo(g, target, number);
				return Response.OK;
			}
		}
	}
	
	/* rogueRefiner */
	public static Response rogueRefiner(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		
		if (ta.getStep() == 1) {
			ta.advanceStep();
			return g.drawCards(controller, 1);
		}
		
		controller.addCounter(CounterType.ENERGY, 2);
		return Response.OK;
	}
	
	/* traversetheUlvenwald */
	public static Response traversetheUlvenwald(Game g, StackObject spell) {
		Player controller = spell.getController(g);
		State state = State.WaitChoiceBasicLand;
		
		if (controller.hasBuff(PlayerBuff.Delirium))
			state = State.WaitChoiceLandOrCreatureCard;
		
		if (spell.getStep() == 1) {
			spell.advanceStep();
			controller.setState(state);
			return Response.MoreStep;
		}
		else {
			if (!g.validateChoices())
				return Response.MoreStep;
			if (g.getChoices().size() == 1)
				g.move_LIB_to_HND((Card) g.getChoices().get(0));
			controller.shuffle();
			controller.addCounter(CounterType.ENERGY, 2);
			return Response.OK;
		}
	}
	
	/* attuneWithAether */
	public static Response attuneWithAether(Game g, StackObject spell) {
		Player controller = spell.getController(g);
		
		if (spell.getStep() == 1) {
			spell.advanceStep();
			controller.setState(State.WaitChoiceBasicLand);
			return Response.MoreStep;
		}
		else {
			if (!g.validateChoices())
				return Response.MoreStep;
			if (g.getChoices().size() == 1)
				g.move_LIB_to_HND((Card) g.getChoices().get(0));
			controller.shuffle();
			controller.addCounter(CounterType.ENERGY, 2);
			return Response.OK;
		}
	}
	
	/* yavimayaElder_fetch */ 
	public static Response yavimayaElder_fetch(Game g, StackObject ta) {
		int step = ta.getStep();
		Response ret;
		
		
		switch (step) {
		case 1:
			ret = libSearchHelperFunction(g, ta, Game.State.WaitChoiceBasicLand);
			ta.advanceStep();
			break;
		
		case 2:
			if (g.validateChoices()) {
				if ((g.getChoices() != null) && (g.getChoices().size() == 1))
					g.move_LIB_to_HND((Card) g.getChoices().get(0));
				ta.advanceStep();
			}
			ret = Response.MoreStep;
			break;
			
		default:
			if (g.validateChoices()) {
				if ((g.getChoices() != null) && (g.getChoices().size() == 1))
					g.move_LIB_to_HND((Card) g.getChoices().get(0));
				ta.getController(g).shuffle();
				ret = Response.OK;
			}
			else
				ret = Response.MoreStep;
			break;
		}
		return ret;
	}
	
	/* purgingScythe */
	public static Response purgingScythe(Game g, StackObject ta) {
		// Immediately return if no creatures are on the battlefield
		if (g.getBattlefield().getCreatures().size() == 0)
			return Response.OK;
		
		Card scythe = ta.getSource();
		
		if (ta.getStep() == 1) {
			ta.advanceStep();
			Vector<Card> flagged = g.getBattlefield().getCreaturesWithLeastToughness();
			
			// Exactly one creature has the smallest toughness
			if (flagged.size() == 1) {
				scythe.dealNonCombatDamageTo(g, flagged.get(0), 2);
				return Response.OK;
			}
			else  {  // If more than one creature are tied for smallest toughness...
				scythe.getController(g).setState(State.WaitChoicePurgingScythe);
				return Response.MoreStep;
			}
		}
		
		if (ta.getStep() == 2) {
			if (g.validateChoices()) {
				scythe.dealNonCombatDamageTo(g, (Damageable) g.getChoices().get(0), 2);
			}
			else
				return Response.MoreStep;
		}
		return Response.OK;
	}
	
	/* thundermawHellkite */
	public static Response thundermawHellkite(Game g, StackObject ta) {
		Card dragon = ta.getSource();
		Player controller = ta.getController(g);
		Player opponent = controller.getOpponent();
		Vector<Card> oppCreatures = g.getBattlefield().getCreaturesControlledBy(opponent);
		for (Card oppCreature : oppCreatures) {
			if (oppCreature.hasEvergreenGlobal(Evergreen.FLYING, g)) {
				dragon.dealNonCombatDamageTo(g, oppCreature, 1);
				oppCreature.tap(g);
			}
		}
		return Response.OK;
	}
	
	/* outmaneuver */
	public static Response outmaneuver(Game g, StackObject spell) {
		Card attacker;
		for (int i = 0; i < spell.getXValue(); i++) {
			attacker = (Card) spell.getTargetObject(i);
			attacker.ignoreBlocker();
		}
		return Response.OK;
	}
	
	/* lull */
	public static Response lull(Game g, StackObject spell) {
		ContinuousEffect fog = ContinuousEffectFactory.create("fog", spell);
		g.addContinuousEffect(fog);
		return Response.OK;
	}
	
	/* strokeOfGenius */
	public static Response strokeOfGenius(Game g, StackObject spell) {
		Response ret = Response.OK;
		Player target = (Player) spell.getTarget(0).getObject();
		if (spell.getStep() == 1) {
			spell.advanceStep();
			ret = g.drawCards(target, spell.getXValue());
		}
		return ret;
	}
	
	/* sphinxsRevelation */
	public static Response sphinxsRevelation(Game g, StackObject spell) {
		Response ret = Response.OK;
		int xValue = spell.getXValue();
		Player controller = spell.getController(g);
		
		if (spell.getStep() == 1) {
			controller.gainLife(xValue);
			spell.advanceStep();
			ret = g.drawCards(controller, xValue);	
		}
		return ret;
	}
	
	/* loxodonHierarch_gainlife */
	public static Response loxodonHierarch_gainlife(Game g, StackObject ta) {
		ta.getController(g).gainLife(4);
		return Response.OK;
	}
	
	/* kitchenFinks_gainLife */
	public static Response kitchenFinks_gainLife(Game g, StackObject ta) {
		ta.getController(g).gainLife(2);
		return Response.OK;
	}
	
	/* selflessSpirit */
	public static Response selflessSpirit(Game g, StackObject aa) {
		Player controller = aa.getController(g);
		Vector<Card> creatures = g.getBattlefield().getCreaturesControlledBy(controller);
		for (Card creature : creatures)
			creature.addModifiers(new EvergreenModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.INDESTRUCTIBLE));
		return Response.OK;
	}
	
	/* loxodonHierarch_regen */
	public static Response loxodonHierarch_regen(Game g, StackObject aa) {
		Player controller = aa.getController(g);
		Vector<Card> creatures = g.getBattlefield().getCreaturesControlledBy(controller);
		for (Card creature : creatures) {
			creature.regenerate();
		}
		return Response.OK;
	}
	
	/* siegeRhino */
	public static Response siegeRhino(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		Player opponent = controller.getOpponent();
		controller.gainLife(3);
		opponent.loseLife(3);
		return Response.OK;
	}
	
	/* zulaportCutthroat_drain */
	public static Response zulaportCutthroat_drain(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		controller.getOpponent().loseLife(1);
		controller.gainLife(1);
		return Response.OK;
	}
	
	/* nightsWhisper */
	public static Response nightsWhisper(Game g, StackObject spell) {
		Response ret = Response.OK;
		Player controller = spell.getController(g);
		
		if (spell.getStep() == 1) {
			spell.advanceStep();
			controller.loseLife(2);	
			ret = g.drawCards(controller, 2);
		}
		return ret;
	}
	
	/* tidespoutTyrant */
	public static Response tidespoutTyrant(Game g, StackObject ta) {
		Card target = (Card) ta.getTargetObject(0);
		return g.move_BFD_to_HND(target);
	}
	
	/* griselbrand */
	public static Response griselbrand(Game g, StackObject aa) {
		Response ret = Response.OK;
		Player controller = aa.getController(g);
		
		if (aa.getStep() == 1) {
			aa.advanceStep();
			ret = g.drawCards(controller, 7);
		}
		return ret;
	}
	
	/* twincast */
	public static Response twincast(Game g, StackObject spell) {
		Card target = (Card) spell.getTargetObject(0);
		g.copySpell(target, spell);
		return Response.OK;
	}
	
	/* phyrexianRager */
	public static Response phyrexianRager(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		controller.loseLife(1);
		if (ta.getStep() == 1) {
			ta.advanceStep();
			return g.drawCards(controller, 1);
		}
		return Response.OK;
	}
	
	/* aetherVial_charge */
	public static Response aetherVial_charge(Game g, StackObject ta) {
		Card vial = ta.getSource();

		// add a counter only if the permanent is still on the battlefield
		if (vial.isOTB(g)) 
			vial.addCounter(g, CounterType.CHARGE, 1);
		return Response.OK;
	}
	
	/* phyrexianProcessor_createToken */
	public static Response phyrexianProcessor_createToken(Game g, StackObject aa) {
		Card token = g.createSingleToken(Token.BLACK_MINION_XX, aa.getSource());
		int x = aa.getSource().getXValue();
		token.setPrintedPT(String.format("%d/%d", x, x));
		return Response.OK;
	}
	
	/* goblinTrenches */
	public static Response goblinTrenches(Game g, StackObject aa) {
		g.createTokens(Token.RED_WHITE_GOBLIN_SOLDIER_11, 2, aa.getSource());
		return Response.OK;
	}
	
	/* assembleTheLegion */
	public static Response assembleTheLegion(Game g, StackObject ta) {
		int nbCounters;
		Card enchant = ta.getSource();

		// add a counter only if the enchantment is still on the battlefield
		if (enchant.isOTB(g)) 
			enchant.addCounter(g, CounterType.MUSTER, 1);
		nbCounters = enchant.getNbCountersOfType(g, CounterType.MUSTER);
		g.createTokens(Token.RED_WHITE_SOLDIER_11_HASTE, nbCounters, enchant);
		return Response.OK;
	}
	
	/* Sarcomancy, damage efect */
	public static Response sarcomancy_damage(Game g, StackObject c) {
		return c.getSource().dealNonCombatDamageTo(g, c.getController(g), 1);
	}
	
	/* linvalaPreserver_gainLife */
	public static Response linvalaPreserver_gainLife(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		controller.gainLife(5);
		return Response.OK;
	}
	
	/* linvalaPreserver_putToken */
	public static Response linvalaPreserver_putToken(Game g, StackObject ta) {
		g.createSingleToken(Token.WHITE_ANGEL_33_FLYING, ta.getSource());
		return Response.OK;
	}
	
	/* Thragtusk, gain life (enters the BF) */
	public static Response thragtusk_gainLife(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		controller.gainLife(5);
		return Response.OK;
	}
	
	/* Thragtusk, put a 3/3 token (leaves the BF) */
	public static Response thragtusk_putToken(Game g, StackObject ta) {
		g.createSingleToken(Token.GREEN_BEAST_33, ta.getSource());
		return Response.OK;
	}
	
	/* callOfTheHerd */
	public static Response callOfTheHerd(Game g, StackObject ta) {
		g.createSingleToken(Token.GREEN_ELEPHANT_33, ta.getSource());
		return Response.OK;
	}
	
	/* scavengingOoze */
	public static Response scavengingOoze(Game g, StackObject aa) {
		Card target = (Card) aa.getTargetObject(0);
		Player controller = aa.getController(g);
		Card source = aa.getSource();
		
		if (target == null)
			return Response.ErrorInvalidTarget;
		
		g.move_GYD_to_EXL(target);
		if (target.isCreatureCard()) {
			source.addCounter(g, CounterType.PLUS_ONE, 1);
			controller.gainLife(1);
		}
		return Response.OK;
	}
	
	/* Deathrite Shaman */
	/* DRS - Add mana */
	public static Response drs_1(Game g, StackObject aa) {
		Game.Response r;
		r = g.move_GYD_to_EXL((Card) aa.getTargetObject(0));
		Effect.addMana(g, aa);
		return r;
	}
	
	/* DRS - Make opponents lose life */
	public static Response drs_2(Game g, StackObject c) {
		Game.Response r;
		r = g.move_GYD_to_EXL((Card) c.getTargetObject(0));
		if (r == Response.OK)
			c.getController(g).getOpponent().loseLife(2);
		return r;
	}
	
	/* DRS - Gain life */
	public static Response drs_3(Game g, StackObject c) {
		Game.Response r;
		r = g.move_GYD_to_EXL((Card) c.getTargetObject(0));
		if (r == Response.OK)
			c.getController(g).gainLife(2);
		return r;
	}
	
	/* darkPetition */
	public static Response darkPetition(Game g, StackObject spell) {
		Response ret = Response.OK;
		Player controller = spell.getController(g);
		int step = spell.getStep();
		
		if (step == 1)
		{
			spell.getController(g).setState(Game.State.WaitChoiceTutor);
			controller.setLibrarySearchable(true);
			spell.advanceStep();
			ret = Response.MoreStep;
		}
		else if (step == 2)
		{
			if (g.validateChoices()) {
				Card c = (Card) g.getChoices().get(0);
				g.move_LIB_to_HND(c);
				controller.shuffle();
				
				// test spell mastery
				if (controller.hasBuff(PlayerBuff.SpellMastery))
					controller.addMana(ManaType.BLACK, 3);
				ret = Response.OK;
			}
			else
				ret = libSearchHelperFunction(g, spell, Game.State.WaitChoiceTutor);
		}
		return ret;	}
	
	/* tutor */
	public static Response demonicTutor(Game g, StackObject so) {
		Response ret = Response.OK;
		int step = so.getStep();
		
		if (step == 1) {
			so.getController(g).setState(Game.State.WaitChoiceTutor);
			so.getController(g).setLibrarySearchable(true);
			so.advanceStep();
			ret = Response.MoreStep;
		}
		else if (step == 2)
		{
			if (g.validateChoices()) {
				Card c = (Card) g.getChoices().get(0);
				g.move_LIB_to_HND(c);
				so.getController(g).shuffle();
				ret = Response.OK;
			}
			else
				ret = libSearchHelperFunction(g, so, Game.State.WaitChoiceTutor);
		}
		return ret;
	}
	
	/* songstitcher */
	public static Response songstitcher(Game g, StackObject aa) {
		Card target = (Card) aa.getTargetObject(0);
		target.preventCombatDamageDealtBy();
		return Response.OK;
	}
	
	/* mazeOfIth */
	public static Response mazeOfIth(Game g, StackObject aa) {
		Card target = (Card) aa.getTargetObject(0);
		target.untap(g);
		target.preventCombatDamageDealtTo();
		target.preventCombatDamageDealtBy();
		return Response.OK;
	}
	
	/* soulSculptor */
	public static Response soulSculptor(Game g, StackObject aa) {
		Card target = (Card) aa.getTargetObject(0);
		ContinuousEffect ce = ContinuousEffectFactory.create("soulSculpted", target);
		target.addContinuousEffect(ce);
		return Response.OK;
	}
	
	/* priestGix */
	public static Response priestGix(Game g, StackObject ta) {
		ta.getController(g).addMana(ManaType.BLACK, 3);
		return Response.OK;
	}
	
	/* gamble */
	public static Response gamble(Game g, StackObject so) {
		Response ret = Response.OK;
		int step = so.getStep();
		
		if (step == 1)
		{
			so.getController(g).setState(Game.State.WaitChoiceTutor);
			so.getController(g).setLibrarySearchable(true);
			so.advanceStep();
			ret = Response.MoreStep;
		}
		else if (step == 2)
		{
			if (g.validateChoices()) {
				Card c = (Card) g.getChoices().get(0);
				g.move_LIB_to_HND(c);
				g.discardRandom(so.getController(g));
				so.getController(g).shuffle();
				ret = Response.OK;
			}
			else
				ret = libSearchHelperFunction(g, so, Game.State.WaitChoiceTutor);
		}
		return ret;
	}
	
	/* vampiric tutor */
	public static Response vampiricTutor(Game g, StackObject spell) {
		Response ret = Response.OK;
		Player controller = spell.getController(g);
		
		if (spell.getStep() == 1) {
			controller.setState(Game.State.WaitChoiceTutor);
			controller.setLibrarySearchable(true);
			spell.advanceStep();
			ret = Response.MoreStep;
		}
		else {
			if (g.validateChoices()) {
				Card c = (Card) g.getChoices().get(0);
				g.move_LIB_to_HND(c);
				controller.shuffle();
				g.move_HND_to_TOPLIB(c);
				controller.loseLife(2);
				ret = Response.OK;
			}
			else
				ret = libSearchHelperFunction(g, spell, Game.State.WaitChoiceTutor);
		}
		return ret;
	}

	/* sterlingGrove_tutor */
	public static Response sterlingGrove_tutor(Game g, StackObject spell) {
		Response ret = Response.OK;
		Player controller = spell.getController(g);
		
		if (spell.getStep() == 1) {
			controller.setState(Game.State.WaitChoiceEnchantment);
			controller.setLibrarySearchable(true);
			spell.advanceStep();
			ret = Response.MoreStep;
		}
		else {
			if (g.validateChoices()) {
				Card c = (Card) g.getChoices().get(0);
				g.move_LIB_to_HND(c);
				controller.shuffle();
				g.move_HND_to_TOPLIB(c);
				ret = Response.OK;
			}
			else {
				ret = libSearchHelperFunction(g, spell, Game.State.WaitChoiceEnchantment);
			}
		}
		return ret;
	}
	

	/* zurTheEnchanter */ 
	public static Response zurTheEnchanter(Game g, StackObject ta) {
		Response ret;
		Player controller = ta.getController(g);
		int step = ta.getStep();
		
		switch (step) {
		// Step 1 : Wait for player to choose an enchantment card or click Done
		case 1:
			controller.setState(Game.State.WaitChoiceEnchantmentCardWithCCM3orLess);
			controller.setLibrarySearchable(true);
			ta.advanceStep();
			ret = Response.MoreStep;
			break;
			
		// Step 2 : Handle player choice
		case 2:	
			if (g.validateChoices() == true)
			{
				if (g.getChoices().isEmpty()) // player clicked DONE button, shuffle and return OK
				{
					controller.shuffle();
					return Response.OK;
				}
				else // player chose a card
				{
					Card c = (Card) g.getChoices().get(0);
					boolean bOK = true;

					// Additional verifications must be done for Auras
					if (c.hasSubtypeGlobal(g, Subtype.AURA))
					{
						// Check that there is a permanent that the Aura can be attached to
						Vector<MtgObject> availTargets = c.getAvailableTargets(g, 0);
						if (availTargets.size() > 0) {
							g._currentAura = c;
							controller.setState(Game.State.PromptHost);
							ta.advanceStep();
							return Response.MoreStep;
						}
						else // No permanent can be attached with this aura
						{ 
							ret = Response.ErrorNoLegalTarget;
							bOK = false;
						}
					}
					else
						ret = Response.OK;

					if (bOK)
						g.move_LIB_to_BFD(c);
					controller.shuffle();
				}
			}
			else
				ret = libSearchHelperFunction(g, ta, Game.State.WaitChoiceEnchantmentCardWithCCM3orLess);
			break;
			
		case 3:
			g.move_LIB_to_BFD(g._currentAura);
			ret = Response.OK;
			break;
			
		// Should never get here
		default:
			ret = Response.Error;
			break;
		}
		return ret;
	}

	/* recruiterOfTheGuard */
	public static Response recruiterOfTheGuard(Game g, StackObject ta) {
		Player controller = ta.getController(g);
				
		if  (ta.getStep() == 1) {
			ta.advanceStep();
			controller.setState(Game.State.WaitChoiceCreatureCardWithToughness2orLess);
			return Response.MoreStep;
		}

		if (ta.getStep() == 2) {
			if (!g.validateChoices()) {
				return Response.MoreStep;
			}
			else {
				// if player chose not to pick a card, just shuffle
				if (g.getChoices().size() == 0) {
					controller.shuffle();
				}
				else {
					Card c = (Card) g.getChoices().get(0);
					System.out.println(controller.getName() + " reveals " + c.getName() + ".");
					g.move_LIB_to_HND(c);
					controller.shuffle();
				}
				return Response.OK;
			}
		}
		return Response.OK;
	}
	
	/* goblinMatron */
	public static Response goblinMatron(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		
		if  (ta.getStep() == 1) {
			ta.advanceStep();
			controller.setState(Game.State.WaitChoiceGoblinCard);
			return Response.MoreStep;
		}

		if (ta.getStep() == 2) {
			if (!g.validateChoices()) {
				return Response.MoreStep;
			}
			else {
				// if player chose not to pick a card, just shuffle
				if (g.getChoices().size() == 0) {
					controller.shuffle();
				}
				else {
					Card c = (Card) g.getChoices().get(0);
					System.out.println(controller.getName() + " reveals " + c.getName() + ".");
					g.move_LIB_to_HND(c);
					controller.shuffle();
				}
				return Response.OK;
			}
		}
		return Response.OK;
	}
	
	/* enlightenedTutor */
	public static Response enlightenedTutor(Game g, StackObject spell) {
		Response ret = Response.OK;
		Player controller = spell.getController(g);
		
		if (spell.getStep() == 1) {
			controller.setState(Game.State.WaitChoiceArtifactOrEnchantmentCard);
			controller.setLibrarySearchable(true);
			spell.advanceStep();
			ret = Response.MoreStep;
		}
		else {
			if (g.validateChoices()) {
				if (g.getChoices().size() == 0) {
					controller.shuffle();
					return Response.OK;
				}
				Card c = (Card) g.getChoices().get(0);
				g.move_LIB_to_HND(c);
				controller.shuffle();
				g.move_HND_to_TOPLIB(c);
				ret = Response.OK;
			}
			else {
				ret = libSearchHelperFunction(g, spell, Game.State.WaitChoiceArtifactOrEnchantmentCard);
			}
		}
		return ret;
	}
	
	/* mysticalTutor */
	public static Response mysticalTutor(Game g, StackObject spell) {
		Response ret = Response.OK;
		Player controller = spell.getController(g);
		
		if (spell.getStep() == 1) {
			controller.setState(Game.State.WaitChoiceInstantOrSorceryCard);
			controller.setLibrarySearchable(true);
			spell.advanceStep();
			ret = Response.MoreStep;
		}
		else {
			if (g.validateChoices()) {
				Card c = (Card) g.getChoices().get(0);
				g.move_LIB_to_HND(c);
				controller.shuffle();
				g.move_HND_to_TOPLIB(c);
				ret = Response.OK;
			}
			else {
				ret = libSearchHelperFunction(g, spell, Game.State.WaitChoiceInstantOrSorceryCard);
			}
		}
		return ret;
	}
	
	/* greenSunZenith */
	public static Response greenSunZenith(Game g, StackObject spell) {
		if (spell.getStep() == 1) {
			libSearchHelperFunction(g, spell, Game.State.WaitChoiceGreenSunZenith);
			spell.advanceStep();
			return Response.MoreStep;
		}
		else {
			if (g.validateChoices()) {
				if (g.getChoices().size() == 1)
					g.move_LIB_to_BFD((Card) g.getChoices().get(0));
				g.move_STK_to_LIB((Card) spell);
				spell.getController(g).shuffle();
			}
			else
				return Response.MoreStep;
		}
		return Response.OK;
	}
	
	/* clawsOfGix */
	public static Response clawsOfGix(Game g, StackObject so) {
		so.getController(g).gainLife(1);
		return Response.OK;
	}
	
	/* citanulFlute */
	public static Response citanulFlute(Game g, StackObject aa) {
		int step = aa.getStep();
		
		if (step == 1) {
			aa.advanceStep();
			libSearchHelperFunction(g, aa, Game.State.WaitChoiceCitanulFlute);
			return Response.MoreStep;
		}
		
		if (step == 2) {
			if (g.validateChoices()) {
				if (g.getChoices().size() == 1)
					g.move_LIB_to_HND((Card) g.getChoices().get(0));
				aa.getController(g).shuffle();
				return Response.OK;
			}
			else
				return Response.MoreStep;
		}
		
		return Response.OK;
	}

	/* chordOfCalling */
	public static Response chordOfCalling(Game g, StackObject so) {
		Response ret = Response.OK;
		
		if (so.getStep() == 1) {
			ret = libSearchHelperFunction(g, so, Game.State.WaitChoiceChordOfCalling);
			so.advanceStep();
		}
		else
		{
			if (g.validateChoices()) {
				if ((g.getChoices() != null) && (g.getChoices().size() == 1))
					g.move_LIB_to_BFD((Card) g.getChoices().get(0));
				so.getController(g).shuffle();
				ret = Response.OK;
			}
			else
				ret = libSearchHelperFunction(g, so, Game.State.WaitChoiceChordOfCalling);
		}
		return ret;
	}
	
	/* entomb */
	public static Response entomb(Game g, StackObject so) {
		Response ret = Response.OK;
		
		if (so.getStep() == 1) {
			so.getController(g).setState(Game.State.WaitChoiceTutor);
			so.getController(g).setLibrarySearchable(true);
			so.advanceStep();
			ret = Response.MoreStep;
		}
		else {
			if (g.validateChoices()) {
				Card c = (Card) g.getChoices().get(0);
				g.move_LIB_to_GYD(c);
				so.getController(g).shuffle();
				ret = Response.OK;
			}
			else
				ret = libSearchHelperFunction(g, so, Game.State.WaitChoiceTutor);
		}
		return ret;
	}

	private static Response libSearchHelperFunction(Game g, StackObject so, State state) {
		so.getController(g).setState(state);
		so.getController(g).setLibrarySearchable(true);
		return Response.MoreStep;
	}
	
	/* nissaVS_tutorForest */
	public static Response nissaVS_tutorForest(Game g, StackObject ta) {
		Response ret;
		if (ta.getStep() == 1) {
			ret = libSearchHelperFunction(g, ta, Game.State.WaitChoiceBasicForestCard);
			ta.advanceStep();
		}
		else {
			if (g.validateChoices()) {
				if ((g.getChoices() != null) && (g.getChoices().size() == 1))
					g.move_LIB_to_HND((Card) g.getChoices().get(0));
				ta.getController(g).shuffle();
				ret = Response.OK;
			}
			else
				ret = Response.MoreStep;
		}
		return ret;
	}
	
	/* nissaVS_transform */
	public static Response nissaVS_transform(Game g, StackObject ta) {
		g.transformCreatureToPlaneswaler(ta.getSource());
		return Response.OK;
	}
	
	/* knightOfTheReliquary_fetch */
	public static Response knightOfTheReliquary_fetch(Game g, StackObject aa) {
		if (aa.getStep() == 1) {
			libSearchHelperFunction(g, aa, Game.State.WaitChoiceSylvanScrying);
			aa.advanceStep();
			return Response.MoreStep;
		}
		else {
			if (g.validateChoices()) {
				if (g.getChoices().size() == 1)
					g.move_LIB_to_BFD((Card) g.getChoices().get(0));
				aa.getController(g).shuffle();
			}
			else
				return Response.MoreStep;
		}
		return Response.OK;
	}
	
	/* cropRotation */
	public static Response cropRotation(Game g, StackObject so) {
		Response ret = Response.OK;
		
		if (so.getStep() == 1) {
			ret = libSearchHelperFunction(g, so, Game.State.WaitChoiceSylvanScrying);
			so.advanceStep();
		}
		else {
			if (g.validateChoices()) {
				if ((g.getChoices() != null) && (g.getChoices().size() == 1))
					g.move_LIB_to_BFD((Card) g.getChoices().get(0));
				so.getController(g).shuffle();
				ret = Response.OK;
			}
			else
			{
				ret = libSearchHelperFunction(g, so, Game.State.WaitChoiceSylvanScrying);
				so.resetStep();
			}
		}
		return ret;
	}
	
	/* gaeasBounty */
	public static Response gaeasBounty(Game g, StackObject spell) {
		
		if (spell.getStep() == 1) {
			libSearchHelperFunction(g, spell, Game.State.WaitChoiceForestCard);
			spell.advanceStep();
			return Response.MoreStep;
		}
		
		else if (spell.getStep() == 2) {
			if (g.validateChoices()) {
				spell.advanceStep();
				if (g.getChoices().size() == 0) {
					spell.getController(g).shuffle();
					return Response.OK;
				}
				else
				{
					g.move_LIB_to_HND((Card) g.getChoices().get(0));
					libSearchHelperFunction(g, spell, Game.State.WaitChoiceForestCard);
					return Response.MoreStep;
				}
			}
			else
				return Response.MoreStep;
		}
		
		else if (spell.getStep() == 3) {
			if (g.validateChoices()) {
				spell.advanceStep();
				if (g.getChoices().size() == 1)
					g.move_LIB_to_HND((Card) g.getChoices().get(0));
				spell.getController(g).shuffle();
				return Response.OK;
			}
			else
				return Response.MoreStep;
		}
		return Response.OK;
	}
	
	/* sylvanScrying */
	public static Response sylvanScrying(Game g, StackObject so) {
		Response ret = Response.OK;
		
		if (so.getStep() == 1) {
			ret = libSearchHelperFunction(g, so, Game.State.WaitChoiceSylvanScrying);
			so.advanceStep();
		}
		else  {
			if (g.validateChoices()) {
				if ((g.getChoices() != null) && (g.getChoices().size() == 1))
					g.move_LIB_to_HND((Card) g.getChoices().get(0));
				so.getController(g).shuffle();
				ret = Response.OK;
			}
			else
			{
				ret = libSearchHelperFunction(g, so, Game.State.WaitChoiceSylvanScrying);
				so.resetStep();
			}
		}
		return ret;
	}
	
	/* stoneforgeTutor */
	public static Response stoneforgeMystic_tutor(Game g, StackObject so) {
		Response ret = Response.OK;

		if (so.getStep() == 1) {
			ret = libSearchHelperFunction(g, so, Game.State.WaitChoiceStoneforgeMystic_search);
			so.advanceStep();
		}
		else {
			if (g.validateChoices()) {
				if ((g.getChoices() != null) && (g.getChoices().size() == 1))
					g.move_LIB_to_HND((Card) g.getChoices().get(0));
				so.getController(g).shuffle();
				ret = Response.OK;
			}
			else
				ret = libSearchHelperFunction(g, so, Game.State.WaitChoiceStoneforgeMystic_search);
		}
		return ret;
	}
	
	/* flickerwisp */
	public static Response flickerwisp(Game g, StackObject ta) {
		Card target = (Card) ta.getTargetObject(0);
		g.move_BFD_to_EXL(target);
		
		// Delayed triggered effect that will return the creature at end of turn
		ContinuousEffect delayedTrigger = ContinuousEffectFactory.create("flickerwisp_delayedTrigger", ta.getSource());
		delayedTrigger.setAdditionalData(target);
		g.addContinuousEffect(delayedTrigger);
		
		return Response.OK;
	}
	
	/* aetherVial_put */
	public static Response aetherVial_put(Game g, StackObject aa) {
		if (aa.getStep() == 1) {
			aa.getController(g).setState(State.WaitChoiceAetherVial_put);
			aa.advanceStep();
			return Response.MoreStep;
		}
		else
		{
			if (g.validateChoices()) {
				if ((g.getChoices() != null) && (g.getChoices().size() == 1))
					g.move_HND_to_BFD((Card) g.getChoices().get(0));
			}
			else
				return Response.MoreStep;
		}
		return Response.OK;
	}
	
	/* copperGnomes */
	public static Response copperGnomes(Game g, StackObject aa) {
		if (aa.getStep() == 1) {
			aa.getController(g).setState(State.WaitChoiceCopperGnomes);
			aa.advanceStep();
			return Response.MoreStep;
		}
		else
		{
			if (g.validateChoices())
			{
				if ((g.getChoices() != null) && (g.getChoices().size() == 1))
					g.move_HND_to_BFD((Card) g.getChoices().get(0));
				return Response.OK;
			}
			else
			{
				return Response.MoreStep;
			}
		}
	}
	
	/* goblinLackey */
	public static Response goblinLackey(Game g, StackObject aa) {
		if (aa.getStep() == 1) {
			aa.getController(g).setState(State.WaitChoiceGoblinLackey);
			aa.advanceStep();
			return Response.MoreStep;
		}
		else {
			if (g.validateChoices()) {
				if ((g.getChoices() != null) && (g.getChoices().size() == 1))
					g.move_HND_to_BFD((Card) g.getChoices().get(0));
				return Response.OK;
			}
			else
				return Response.MoreStep;
		}
	}

	/* sneakAttack */
	public static Response sneakAttack(Game g, StackObject aa) {
		Player controller = aa.getController(g);
		Card creature;
		
		// if the player has no creature cards in hand, immediately return
		if (controller.getHand().getCreatureCards().size() == 0)
			return Response.OK;
		
		if (aa.getStep() == 1) {
			aa.advanceStep();
			controller.setState(State.WaitChoiceSneakAttack);
			return Response.MoreStep;
		}
		
		if (aa.getStep() == 2) {
			if (g.validateChoices()) {
				if ((g.getChoices() != null) && (g.getChoices().size() == 1)) {
					creature = (Card) g.getChoices().get(0);
					g.move_HND_to_BFD(creature);
					creature.addModifiers(new EvergreenModifier(aa, Modifier.Duration.PERMANENTLY, Evergreen.HASTE));
					// Delayed triggered effect that will exile the tokens at the beginning of the next end step.
					ContinuousEffect delayedTrigger = ContinuousEffectFactory.create("sneakAttack_delayed", aa.getSource());
					delayedTrigger.setAdditionalData(creature);
					g.addContinuousEffect(delayedTrigger);

				}
				return Response.OK;
			}
			else
				return Response.MoreStep;
		}
		return Response.OK;
	}
	
	/* stoneforgeMystic_put */
	public static Response stoneforgeMystic_put(Game g, StackObject aa) {
		if (aa.getStep() == 1)
		{
			aa.getController(g).setState(State.WaitChoiceStoneforgeMystic_put);
			aa.advanceStep();
			return Response.MoreStep;
		}
		else
		{
			if (g.validateChoices()) {
				if ((g.getChoices() != null) && (g.getChoices().size() == 1))
					g.move_HND_to_BFD((Card) g.getChoices().get(0));
				return Response.OK;
			}
			else
				return Response.MoreStep;
		}
	}
	
	/* flameSlash */
	public static Response flameSlash(Game g, StackObject c) {
		return ((Card) c).dealNonCombatDamageTo(g, (Card) c.getTargetObject(0), 4);
	}
	
	/* whetstone */
	public static Response whetstone(Game g, StackObject aa) {
		Player controller = aa.getController(g);
		Player opponent = controller.getOpponent();
		
		for (int i = 0; i < 2; i++) {
			g.move_LIB_to_GYD(controller.getLibrary().getTopCard());
			g.move_LIB_to_GYD(opponent.getLibrary().getTopCard());
		}
		return Response.OK;
		
	}
	
	/* voltaicKey */
	public static Response voltaicKey(Game g, StackObject aa) {
		Card target = (Card) aa.getTargetObject(0);
		target.untap(g);
		return Response.OK;
	}
	
	/* annihilator */
	public static Response annihilator(Game g, StackObject ta) {
		Card eldrazi = ta.getSource();
		Player defendingPlayer = eldrazi.getController(g).getOpponent();
		int nbAnnihilator = Integer.parseInt(((TriggeredAbility) ta).getParameter());
		
		// Do nothing if the player doesn't control any permanent
		if (g.getBattlefield().getPermanentsControlledBy(defendingPlayer).size() == 0) {
			g.getActivePlayer().setState(State.Ready);
			return Response.OK;
		}
		
		if (ta.getStep() == 1) {
			ta.setTmpVar(nbAnnihilator);
			defendingPlayer.setState(State.WaitSacrificePermanent);
			ta.advanceStep();
			return Response.MoreStep;
		}
		else {
			if (!g.validateChoices())
				return Response.MoreStep;
			
			g.sacrifice(defendingPlayer, (Card) g.getChoices().get(0));
			int remainingPermanentsToSac = ta.getTmpVar() - 1;
			if ((remainingPermanentsToSac > 0) && (g.getBattlefield().getPermanentsControlledBy(defendingPlayer).size() > 0)) {
				ta.setTmpVar(remainingPermanentsToSac);
				ta.advanceStep();
				return Response.MoreStep;
			}
			else {
				g.getActivePlayer().setState(State.Ready);
				return Response.OK;
			}
		}
	}
	
	/* battlecry */
	public static Response battlecry(Game g, StackObject ta) {
		Vector<Card> creatures = g.getBattlefield().getCreatures();
		for (Card creature : creatures) {
			if (creature.isAttacking(g) && (creature != ta.getSource()))
				creature.addModifiers(new PTModifier(ta.getSource(), "+1/+0", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
		}
		return Response.OK;
	}
	
	/* thornscapeBattlemage_1 */
	public static Response thornscapeBattlemage_1(Game g, StackObject ta) {
		Card source = ta.getSource();
		MtgObject target = ta.getTargetObject(0);
		Response ret = source.dealNonCombatDamageTo(g, (Damageable) target, 2);
		return ret;
	}
	
	/* murderousRedcap_damage */
	public static Response murderousRedcap_damage(Game g, StackObject ta) {
		Card redcap = ta.getSource();
		Damageable target = (Damageable) ta.getTargetObject(0);
		return redcap.dealNonCombatDamageTo(g, target, redcap.getPower(g));
	}	
	
	/* Flametongue flametongueKavu */
	public static Response flametongueKavu(Game g, StackObject c) {
		return c.getSource().dealNonCombatDamageTo(g, (Card) c.getTargetObject(0), 4);
	}
	
	/* headlongRush */
	public static Response headlongRush(Game g, StackObject so) {
		for (Card creature : g.getBattlefield().getCreatures()) {
			if (creature.isAttacking(g))
				creature.addModifiers(new EvergreenModifier(so, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.FIRSTSTRIKE));
		}
		return Response.OK;
	}
	
	/* titaniasBoon */
	public static Response titaniasBoon(Game g, StackObject spell) {
		Player controller = spell.getController(g);
		for (Card creature : g.getBattlefield().getCreaturesControlledBy(controller))
			creature.addCounter(g, CounterType.PLUS_ONE, 1);
		return Response.OK;
	}
	
	/* windfall */
	public static Response windfall(Game g, StackObject spell) {
		Player controller = spell.getController(g);
		Player opponent = controller.getOpponent();
		int nbCardsToDraw = Math.max(controller.getHandSize(), opponent.getHandSize());
		Response ret;
		
		if (spell.getStep() == 1) {
			spell.advanceStep();
			controller.discardAllCardsInHand();
			opponent.discardAllCardsInHand();
			ret = g.drawCards(controller, nbCardsToDraw);
			if (ret == Response.MoreStep)
				return ret;
		}
		else if (spell.getStep() == 2) {
			spell.advanceStep();
			ret = g.drawCards(opponent, nbCardsToDraw);
			if (ret == Response.MoreStep)
				return ret;
		}
		
		return Response.OK;
	}
	
	/* yawgmothsWill */
	public static Response yawgmothsWill(Game g, StackObject spell) {
		ContinuousEffect ce = ContinuousEffectFactory.create("yawgmothsWill_play", spell);
		ContinuousEffect ce2 = ContinuousEffectFactory.create("yawgmothsWill_exile", spell);
		g.addContinuousEffect(ce);
		g.addContinuousEffect(ce2);
		return Response.OK;
	}
	
	/* timeSpiral */
	public static Response timeSpiral(Game g, StackObject spell) {
		Player controller = spell.getController(g);
		Player opponent = controller.getOpponent();
		Response ret;
		int nbLandsToUntap = 6;
		int step = spell.getStep();
		
		if (spell.getStep() == 1) {
			spell.advanceStep();
			controller.getHand().shuffleIntoLibrary();
			controller.getGraveyard().shuffleIntoLibrary();
			opponent.getHand().shuffleIntoLibrary();
			opponent.getGraveyard().shuffleIntoLibrary();
			ret = g.drawCards(controller, 7);
			if (ret == Response.MoreStep)
				return ret;
		}
		else if (spell.getStep() == 2) {
			spell.advanceStep();
			ret = g.drawCards(opponent, 7);
			if (ret == Response.MoreStep)
				return ret;
		}
		else if (spell.getStep() == 3) {
			spell.advanceStep();
			controller.setState(State.WaitUntapLand);
			return Response.MoreStep;
		}
		
		if ((step >= 4) && (step < 4 + nbLandsToUntap)) {
			if (g.validateChoices()) {
				Vector<MtgObject> choices = g.getChoices();
				if (choices.size() == 0)
					spell.goToStep(20);
				else {
					Card chosenLand = (Card) g.getChoices().get(0);
					chosenLand.untap(g);
					if (step < 3 + nbLandsToUntap) {
						spell.advanceStep();
						return Response.MoreStep;
					}
					else
						spell.goToStep(20);
				}
			}
			else
				return Response.MoreStep;
				
		}
		
		// Exile the spell if it's not a copy
		if (spell.getStep() == 20)
			if (spell instanceof Card && !((Card) spell).isCopy())
				g.move_STK_to_EXL((Card) spell);
		return Response.OK;
	}
	
	/* steamBlast */
	public static Response steamBlast(Game g, StackObject so) {
		Card spell = (Card) so;
		
		for (Card creature : g.getBattlefield().getCreatures())
			spell.dealNonCombatDamageTo(g, creature, 2);
		spell.dealNonCombatDamageTo(g, so.getController(g), 2);
		return spell.dealNonCombatDamageTo(g, so.getController(g).getOpponent(), 2);
	}
	
	/* faultLine */
	public static Response faultLine(Game g, StackObject so) {
		Card spell = (Card) so;
		int xValue = so.getXValue();
		
		for (Card creature : g.getBattlefield().getCreatures()) {
			if (!creature.hasEvergreenGlobal(Evergreen.FLYING, g))
				spell.dealNonCombatDamageTo(g, creature, xValue);
		}
		spell.dealNonCombatDamageTo(g, so.getController(g), xValue);
		return spell.dealNonCombatDamageTo(g, so.getController(g).getOpponent(), xValue);
	}
	
	/* humble */
	public static Response humble(Game g, StackObject spell) {
		Card target = (Card) spell.getTargetObject(0);
		
		target.addModifiers(new PTModifier(spell, "0/1", Operation.SET, Modifier.Duration.UNTIL_END_OF_TURN));
		ContinuousEffect ce = ContinuousEffectFactory.create(Effect.LOSE_ALL_ABILITIES, target);
		ce.setStop(StopWhen.END_OF_TURN);
		target.addContinuousEffect(ce);
		return Response.OK;
	}
	
	/* hibernation */
	public static Response hibernation(Game g, StackObject so) {
		Vector<Card> greenCreatures = new Vector<Card>();
		for (Card creature : g.getBattlefield().getPermanents()) {
			if (creature.hasColor(Color.GREEN))
				greenCreatures.add(creature);
		}
		
		for (Card creature : greenCreatures)
			g.move_BFD_to_HND(creature);
		return Response.OK;
	}
	
	/* orderedMigration */
	public static Response orderedMigration(Game g, StackObject spell) {
		Player controller = spell.getController(g);
		int nbTokens = controller.getDomainCount();
		g.createTokens(Token.BLUE_BIRD_11_FLYING, nbTokens, spell);
		return Response.OK;
	}
	
	/* tribalFlames */
	public static Response tribalFlames(Game g, StackObject spell) {
		Player controller = spell.getController(g);
		int amount = controller.getDomainCount();
		Response ret = ((Card) spell).dealNonCombatDamageTo(g, (Damageable) spell.getTargetObject(0), amount);
		return ret;
	}
	
	/* mirari */
	public static Response mirari(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		
		if (ta.getStep() == 1) {
			controller.setState(State.PromptPay_3mana);
			ta.advanceStep();
			return Response.MoreStep;
		}
		else {
			if (g.getAnswer() == Answer.Yes) {
				Card triggeringSpell = (Card) ((TriggeredAbility) ta).getAdditionalData();
				g.copySpell(triggeringSpell, ta.getSource());
			}
			return Response.OK;
		}
	}
	
	/* punishingFire_return */
	public static Response punishingFire_return(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		
		if (ta.getStep() == 1) {
			controller.setState(State.PromptPayPunishingFire);
			ta.advanceStep();
			return Response.MoreStep;
		}
		else {
			if (g.getAnswer() == Answer.Yes)
				g.move_GYD_to_HND(ta.getSource());
			return Response.OK;
		}
	}
	
	/* galvanicBlast */
	public static Response galvanicBlast(Game g, StackObject c) {
		int nbDamage = 2;
		if (c.getController(g).hasBuff(PlayerBuff.Metalcraft))
			nbDamage = 4;
		return ((Card) c).dealNonCombatDamageTo(g, (Damageable) c.getTargetObject(0), nbDamage);
	}
	
	/* lightningHelix */
	public static Response lightningHelix(Game g, StackObject spell) {
		Card helix = (Card) spell;
		Player controller = spell.getController(g);
		controller.gainLife(3);
		return helix.dealNonCombatDamageTo(g, (Damageable) helix.getTargetObject(0), 3);
	}
	
	/* eliteArchers */
	public static Response eliteArchers(Game g, StackObject c) {
		Response ret = c.getSource().dealNonCombatDamageTo(g, (Damageable) c.getTargetObject(0), 3);
		return ret;
	}
	
	/* showerOfSparks */
	public static Response showerOfSparks(Game g, StackObject c) {
		((Card) c).dealNonCombatDamageTo(g, (Damageable) c.getTargetObject(0), 1);
		Response ret = ((Card) c).dealNonCombatDamageTo(g, (Damageable) c.getTargetObject(1), 1);
		return ret;
	}
	
	/* incendiaryFlow */
	public static Response incendiaryFlow(Game g, StackObject spell) {
		Card source = spell.getSource();
		MtgObject target = spell.getTargetObject(0);
		
		if (target instanceof Card) {
			Card targetedCreature = (Card) target;
			g.addContinuousEffect(ContinuousEffectFactory.create("exileUponDeath", source, targetedCreature));
		}
		return source.dealNonCombatDamageTo(g, (Damageable) target, 3);
	}
	
	/* siegeGang_sac */
	public static Response siegeGang_sac(Game g, StackObject aa) {
		Card source = aa.getSource();
		Response ret = source.dealNonCombatDamageTo(g, (Damageable) aa.getTargetObject(0), 2);
		return ret;
	}
	
	/* moggFanatic */
	public static Response moggFanatic(Game g, StackObject aa) {
		Card source = aa.getSource();
		Response ret = source.dealNonCombatDamageTo(g, (Damageable) aa.getTargetObject(0), 1);
		return ret;
	}

	/* rop (Rune of Protection effects) */
	public static Response rop(Game g, StackObject aa, Object quality) {
		Player controller = aa.getController(g);
		DamageSource choice;
		State state;
		
		if (quality instanceof Color) {
			Color col = (Color) quality;
			
			switch (col) {
			case BLACK:
				state = State.WaitChoiceDamageBlackSource;
				break;
				
			case BLUE:
				state = State.WaitChoiceDamageBlueSource;
				break;
				
			case GREEN:
				state = State.WaitChoiceDamageGreenSource;
				break;
				
			case RED:
				state = State.WaitChoiceDamageRedSource;
				break;
				
			case WHITE:
				state = State.WaitChoiceDamageWhiteSource;
				break;
				
			// should never get here
			default:
				return Response.Error;				
			}
		}
		
		else if (quality instanceof CardType) {
			CardType ct = (CardType) quality;
			
			switch (ct) {
			case ARTIFACT:
				state = State.WaitChoiceDamageArtifactSource;
				break;
				
			case LAND:
				state = State.WaitChoiceDamageLandSource;
				break;

			// should never get here
			default:
				return Response.Error;
			}
		}
		else // should never get here
			return Response.Error;
			
		// chose the damage source
		if (aa.getStep() == 1) {
			controller.setState(state);
			aa.advanceStep();
			return Response.MoreStep;
		}
		
		if (aa.getStep() == 2) {
			if (g.validateChoices()) {
				choice = (DamageSource) g.getChoices().get(0);
				ContinuousEffect ce = ContinuousEffectFactory.create("runeOfProtection_prevent", aa, choice);
				g.addContinuousEffect(ce);				
			}
			else
				return Response.MoreStep;
		}
		return Response.OK;
	}

	/* RoPArtifacts */
	public static Response RoPArtifacts(Game g, StackObject aa) {
		return rop(g, aa, CardType.ARTIFACT);
	}

	/* RoPLands */
	public static Response RoPLands(Game g, StackObject aa) {
		return rop(g, aa, CardType.LAND);
	}
	
	/* RoPWhite */
	public static Response RoPWhite(Game g, StackObject aa) {
		return rop(g, aa, Color.WHITE);
	}
	
	/* RoPBlue */
	public static Response RoPBlue(Game g, StackObject aa) {
		return rop(g, aa, Color.BLUE);
	}
	
	/* RoPBlack */
	public static Response RoPBlack(Game g, StackObject aa) {
		return rop(g, aa, Color.BLACK);
	}
	
	/* RoPRed */
	public static Response RoPRed(Game g, StackObject aa) {
		return rop(g, aa, Color.RED);
	}
	
	/* RoPGreen */
	public static Response RoPGreen(Game g, StackObject aa) {
		return rop(g, aa, Color.GREEN);
	}
	
	/* sanctumGuardian */
	public static Response sanctumGuardian(Game g, StackObject aa) {
		Player controller = aa.getController(g);
		DamageSource choice;
		
		// chose the damage source
		if (aa.getStep() == 1) {
			controller.setState(State.WaitChoiceDamageSource);
			aa.advanceStep();
			return Response.MoreStep;
		}
		
		if (aa.getStep() == 2) {
			if (g.validateChoices()) {
				choice = (DamageSource) g.getChoices().get(0);
				/*
				 * The additional data is a vector with 2 objects :
				 * object [0] is the target.
				 * object [1] is the chosen source which damage will be prevented.
				 */
				Vector<Object> additionalData = new Vector<Object>();
				additionalData.add(0, aa.getTargetObject(0));
				additionalData.add(1, choice);
				ContinuousEffect ce = ContinuousEffectFactory.create("sanctumGuardian_prevent", aa, additionalData);
				g.addContinuousEffect(ce);				
			}
			else
				return Response.MoreStep;
		}
		return Response.OK;
	}
	
	/* goblinLegionnaire_prevent */
	public static Response goblinLegionnaire_prevent(Game g, StackObject aa) {
		Damageable target = (Damageable) aa.getTargetObject(0);
		target.addDamagePrevention(2);
		return Response.OK;
	}
	
	/* youngPyromancer */
	public static Response youngPyromancer(Game g, StackObject c) {
		g.createSingleToken(Token.RED_ELEMENTAL_11, c);
		return Response.OK;
	}
	
	/* goblinOffensive */
	public static Response goblinOffensive(Game g, StackObject spell) {
		g.createTokens(Token.RED_GOBLIN_11, spell.getXValue(), spell);
		return Response.OK;
	}
	
	/* secureTheWastes */
	public static Response secureTheWastes(Game g, StackObject spell) {
		g.createTokens(Token.WHITE_WARRIOR_11, spell.getXValue(), spell);
		return Response.OK;
	}

	/* dragonFodder */
	public static Response dragonFodder(Game g, StackObject c) {
		g.createTokens(Token.RED_GOBLIN_11, 2, c);
		return Response.OK;
	}
	
	/* lingeringSouls */
	public static Response lingeringSouls(Game g, StackObject c) {
		g.createTokens(Token.WHITE_SPIRIT_11_FLYING, 2, c);
		return Response.OK;
	}
	
	/* spectralProcession */
	public static Response spectralProcession(Game g, StackObject c) {
		g.createTokens(Token.WHITE_SPIRIT_11_FLYING, 3, c);
		return Response.OK;
	}
	
	/* siegeGang_putTokens */
	public static Response siegeGang_putTokens(Game g, StackObject c) {
		g.createTokens(Token.RED_GOBLIN_11, 3, c);
		return Response.OK;
	}
	
	/* goblinMarshal */
	public static Response goblinMarshal(Game g, StackObject c) {
		g.createTokens(Token.RED_GOBLIN_11, 2, c);
		return Response.OK;
	}
	
	/* meloku */
	public static Response meloku(Game g, StackObject aa) {
		g.createSingleToken(Token.BLUE_ILLUSION_11_FLYING, aa.getSource());
		return Response.OK;
	}
	
	/* eldraziSkyspawner */
	public static Response eldraziSkyspawner(Game g, StackObject c) {
		g.createTokens(Token.COLORLESS_ELDRAZI_SCION_11, 1, c);
		return Response.OK;
	}
	
	/* broodMonitor */
	public static Response broodMonitor(Game g, StackObject c) {
		g.createTokens(Token.COLORLESS_ELDRAZI_SCION_11, 3, c);
		return Response.OK;
	}
	
	/* derangedHermit_tokens */
	public static Response derangedHermit_tokens(Game g, StackObject c) {
		g.createTokens(Token.GREEN_SQUIRREL_11, 4, c);
		return Response.OK;
	}
	
	/* sarcomancy_token */
	public static Response sarcomancy_token(Game g, StackObject c) {
		g.createSingleToken(Token.BLACK_ZOMBIE_22, c);
		return Response.OK;
	}
	
	/* netherSpirit */
	public static Response netherSpirit(Game g, StackObject so) {
		return g.move_GYD_to_BFD(so.getSource());
	}
	
	/* fatalPush */
	public static Response fatalPush(Game g, StackObject spell) {
		Player controller = spell.getController(g);
		Card target = (Card) spell.getTargetObject(0);
		
		if ((target.getConvertedManaCost(g) <= 2) || (controller.hasBuff(PlayerBuff.Revolt) && (target.getConvertedManaCost(g) <= 4)))
			g.destroy(target, true);
		return Response.OK;
	}
	
	/* mishrasBauble */
	public static Response mishrasBauble(Game g, StackObject ability, int unused) {
		Player controller = ability.getController(g);
		Player target = (Player) ability.getTargetObject(0);
		
		// immediately return OK if the library is empty
		if (target.getLibrary().size() == 0)
			return Response.OK;
		
		if (ability.getStep() == 1) {
			ability.setLibManip(target.getLibrary().getXTopCards(1));
			controller.setState(State.WaitChoicePutTopLib);
			ability.advanceStep();
			return Response.MoreStep;
		}
		else {
			if (g.validateChoices()) {
				Card c = (Card) g.getChoices().get(0);
				g.libToLibTop(c);
				ability.getLibManip().remove(c);
			}
			
			// Delayed triggered effect to draw a card at beg of next upkeep
			ContinuousEffect delayedTrigger = ContinuousEffectFactory.create("mishrasBauble", ability.getSource());
			g.addContinuousEffect(delayedTrigger);
			
			return Response.OK;
		}
	}
	
	/* greenbeltRampager */
	public static Response greenbeltRampager(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		Card elephant = ta.getSource();
		
		if (controller.getNbCounters(CounterType.ENERGY) >= 2) {
			controller.payEnergy(2);
		}
		else {
			g.move_BFD_to_HND(elephant);
			controller.addCounter(CounterType.ENERGY, 1);
		}
		return Response.OK;
	}
	
	/* cityOfBrass_Damage */
	public static Response cityOfBrass_Damage(Game g, StackObject ta) {
		Card source = ta.getSource();
		Player controller = source.getController(g);
		source.dealNonCombatDamageTo(g, controller, 1);
		return Response.OK;
	}
	
	/* exhaustion */
	public static Response exhaustion(Game g, StackObject spell) {
		ContinuousEffect ce = ContinuousEffectFactory.create("exhaustion_noUntap", spell, spell.getTargetObject(0));
		g.addContinuousEffect(ce);
		return Response.OK;
	}
	
	/* graftedSkullcap_discard */
	public static Response graftedSkullcap_discard(Game g, StackObject ta) {
		ta.getController(g).discardAllCardsInHand();
		return Response.OK;
	}
		
	/* endoskeleton */
	public static Response endoskeleton(Game g, StackObject c) {
		ContinuousEffect ce = ContinuousEffectFactory.create("endoskeleton_pump", c.getSource(), c.getTargetObject(0));
		ce.setStop(StopWhen.SOURCE_UNTAPS);
		g.addContinuousEffect(ce);
		return Response.OK;
	}
	
	/* manaLeech */
	public static Response manaLeech(Game g, StackObject c) {
		Card target = (Card) c.getTargetObject(0);
		target.tap(g);
		c.getSource().clearLinkedCards();
		c.getSource().addLinkedCard(target);
		ContinuousEffect ce = ContinuousEffectFactory.create("maintainTapped", c.getSource());
		ce.setStop(StopWhen.SOURCE_UNTAPS);
		g.addContinuousEffect(ce);
		return Response.OK;
	}
	
	/* somnophore */
	public static Response somnophore(Game g, StackObject c) {
		Card target = (Card) c.getTargetObject(0);
		target.tap(g);
		c.getSource().addLinkedCard(target);
		ContinuousEffect ce = ContinuousEffectFactory.create("maintainTapped", c.getSource());
		g.addContinuousEffect(ce);
		return Response.OK;
	}
	
	/* sandSquid */
	public static Response sandSquid(Game g, StackObject c) {
		Card target = (Card) c.getTargetObject(0);
		target.tap(g);
		c.getSource().addLinkedCard(target);
		g.addContinuousEffect(ContinuousEffectFactory.create("maintainTapped", c.getSource()));
		return Response.OK;
	}
	
	/* falter */
	public static Response falter(Game g, StackObject c) {
		ContinuousEffect ce = ContinuousEffectFactory.create("falter_noBlock", c);
		g.addContinuousEffect(ce);
		return Response.OK;
	}
	
	/* sporogenesis_putCounter */
	public static Response sporogenesis_putCounter(Game g, StackObject ta) {
		Card target = (Card) ta.getTargetObject(0);
		target.addCounter(g, CounterType.FUNGUS, 1);
		return Response.OK;
	}
	
	/* enchantmentAlteration */
	public static Response enchantmentAlteration(Game g, StackObject spell) {
		Player controller = spell.getController(g);
		Card targetAura = (Card) spell.getTargetObject(0);
		Card currentHost = (Card) targetAura.getTargetObject(0);
		
		// If there is no valid host other than initial host, return immediately
		boolean bAnotherTargetAvailable = false;
		
		switch (targetAura.getTargetRequirements().get(0).getCategory()) {
		case Creature:
			for (Card permanent : g.getBattlefield().getCreatures()) {
				if (permanent != currentHost)
					bAnotherTargetAvailable = true;
			}
			break;
			
		case Land:
			for (Card permanent : g.getBattlefield().getLands()) {
				if (permanent != currentHost)
					bAnotherTargetAvailable = true;
			}
			break;

		case Permanent:
			for (Card permanent : g.getBattlefield().getPermanents()) {
				if (permanent != currentHost)
					bAnotherTargetAvailable = true;
			}
			break;
			
		default:
			break;
		}
		
		if (!bAnotherTargetAvailable)
			return Response.OK;
		
		if (spell.getStep() == 1) {
			controller.setState(State.WaitChoiceEnchantmentAlteration);
			spell.advanceStep();
			return Response.MoreStep;
		}
		
		else if (spell.getStep() == 2) {
			if (g.validateChoices()) {
				Card newHost = (Card) g.getChoices().get(0);
				newHost.attachLocalPermanent(targetAura);
				return Response.OK;
			}
			else
				return Response.MoreStep;
		}
		return Response.OK;
	}
	
	/* parasiticBond */
	public static Response parasiticBond(Game g, StackObject ta) {
		Card aura = ta.getSource();
		return aura.dealNonCombatDamageTo(g, aura.getHost().getController(g), 2);
	}	
	
	/* regenerate */
	public static Response regenerate(Game g, StackObject c) {
		return c.getSource().regenerate();
	}	
	
	/* lurkingEvil */
	public static Response lurkingEvil(Game g, StackObject ta) {
		Card enchant = ta.getSource();
		enchant.addModifiers(new CardTypeModifier(ta, Operation.SET, Modifier.Duration.PERMANENTLY, CardType.CREATURE),
							 new PTModifier(ta, "4/4", Operation.SET, Modifier.Duration.PERMANENTLY),
							 new CreatureTypeModifier(ta, Modifier.Duration.PERMANENTLY, CreatureType.Horror),
							 new EvergreenModifier(ta, Modifier.Duration.PERMANENTLY, Evergreen.FLYING));
		return Response.OK;
	}
	
	/* opalArchangel */
	public static Response opalArchangel(Game g, StackObject ta) {
		Card enchant = ta.getSource();
		enchant.addModifiers(new CardTypeModifier(ta, Operation.SET, Modifier.Duration.PERMANENTLY, CardType.CREATURE),
							 new PTModifier(ta, "5/5", Operation.SET, Modifier.Duration.PERMANENTLY),
							 new CreatureTypeModifier(ta, Modifier.Duration.PERMANENTLY, CreatureType.Angel),
							 new EvergreenModifier(ta, Modifier.Duration.PERMANENTLY, Evergreen.FLYING, Evergreen.VIGILANCE));
		return Response.OK;
	}
	
	/* veilOfBirds */
	public static Response veilOfBirds(Game g, StackObject ta) {
		Card enchant = ta.getSource();
		enchant.addModifiers(new CardTypeModifier(ta, Operation.SET, Modifier.Duration.PERMANENTLY, CardType.CREATURE),
							 new PTModifier(ta, "1/1", Operation.SET, Modifier.Duration.PERMANENTLY),
							 new CreatureTypeModifier(ta, Modifier.Duration.PERMANENTLY, CreatureType.Bird),
							 new EvergreenModifier(ta, Modifier.Duration.PERMANENTLY, Evergreen.FLYING));
		return Response.OK;
	}
	
	/* veiledSentry */
	public static Response veiledSentry(Game g, StackObject ta) {
		Card triggeringSpell = (Card) ta.getAdditionalData();
		int X = triggeringSpell.getConvertedManaCost(g);
		String ptDef = String.format("%d/%d", X, X);
		Card enchant = ta.getSource();
		enchant.addModifiers(new CardTypeModifier(ta, Operation.SET, Modifier.Duration.PERMANENTLY, CardType.CREATURE),
							 new PTModifier(ta, ptDef, Operation.SET, Modifier.Duration.PERMANENTLY),
							 new CreatureTypeModifier(ta, Modifier.Duration.PERMANENTLY, CreatureType.Illusion));
		return Response.OK;
	}
	
	/* veiledSerpent */
	public static Response veiledSerpent(Game g, StackObject ta) {
		Card enchant = ta.getSource();
		enchant.addModifiers(new CardTypeModifier(ta, Operation.SET, Modifier.Duration.PERMANENTLY, CardType.CREATURE),
							 new PTModifier(ta, "4/4", Operation.SET, Modifier.Duration.PERMANENTLY),
							 new CreatureTypeModifier(ta, Modifier.Duration.PERMANENTLY, CreatureType.Serpent));
		enchant.addContinuousEffect(ContinuousEffectFactory.create("veiledSerpent_cannotAttack", enchant));
		Vector<ContinuousEffect> effects = enchant.getContinuousEffects(Zone.Name.Battlefield);
		for (ContinuousEffect effect : effects)
			g.addContinuousEffect(effect);
		return Response.OK;
	}
	
	/* veiledApparition */
	public static Response veiledApparition(Game g, StackObject ta) {
		Card enchant = ta.getSource();
		enchant.addModifiers(new CardTypeModifier(ta, Operation.SET, Modifier.Duration.PERMANENTLY, CardType.CREATURE),
							 new PTModifier(ta, "3/3", Operation.SET, Modifier.Duration.PERMANENTLY),
							 new CreatureTypeModifier(ta, Modifier.Duration.PERMANENTLY, CreatureType.Illusion),
							 new EvergreenModifier(ta, Modifier.Duration.PERMANENTLY, Evergreen.FLYING));
		enchant.addTriggeredAbility(TriggeredAbilityFactory.create("veiledApparition_upkeep", enchant));
		return Response.OK;
	}
	
	/* veiledCrocodile */
	public static Response veiledCrocodile(Game g, StackObject ta) {
		Card enchant = ta.getSource();
		enchant.addModifiers(new CardTypeModifier(ta, Operation.SET, Modifier.Duration.PERMANENTLY, CardType.CREATURE),
							 new PTModifier(ta, "4/4", Operation.SET, Modifier.Duration.PERMANENTLY),
							 new CreatureTypeModifier(ta, Modifier.Duration.PERMANENTLY, CreatureType.Crocodile));
		return Response.OK;
	}
	
	/* opalCaryatid */
	public static Response opalCaryatid(Game g, StackObject ta) {
		Card enchant = ta.getSource();
		enchant.addModifiers(new CardTypeModifier(ta, Operation.SET, Modifier.Duration.PERMANENTLY, CardType.CREATURE),
							 new PTModifier(ta, "2/2", Operation.SET, Modifier.Duration.PERMANENTLY),
							 new CreatureTypeModifier(ta, Modifier.Duration.PERMANENTLY, CreatureType.Soldier));
		return Response.OK;
	}
	
	/* opalGargoyle */
	public static Response opalGargoyle(Game g, StackObject ta) {
		Card enchant = ta.getSource();
		enchant.addModifiers(new CardTypeModifier(ta, Operation.SET, Modifier.Duration.PERMANENTLY, CardType.CREATURE),
							 new PTModifier(ta, "2/2", Operation.SET, Modifier.Duration.PERMANENTLY),
							 new CreatureTypeModifier(ta, Modifier.Duration.PERMANENTLY, CreatureType.Gargoyle),
							 new EvergreenModifier(ta, Modifier.Duration.PERMANENTLY, Evergreen.FLYING));
		return Response.OK;
	}
	
	/* opalTitan */
	public static Response opalTitan(Game g, StackObject ta) {
		Card enchant = ta.getSource();
		enchant.addModifiers(new CardTypeModifier(ta, Operation.SET, Modifier.Duration.PERMANENTLY, CardType.CREATURE),
							 new PTModifier(ta, "4/4", Operation.SET, Modifier.Duration.PERMANENTLY),
							 new CreatureTypeModifier(ta, Modifier.Duration.PERMANENTLY, CreatureType.Giant));
		Card triggeringSpell = (Card) ta.getAdditionalData();
		
		for (Color col : Color.values()) {
			if (triggeringSpell.hasColor(col))
				enchant.addModifiers(new ProtectionModifier(ta, col, Modifier.Duration.PERMANENTLY));
		}
		
		return Response.OK;
	}
	
	/* oppression */
	public static Response oppression(Game g, StackObject ta) {
		Card triggeringSpell = (Card) ((TriggeredAbility) ta).getAdditionalData();
		Player caster = triggeringSpell.getController(g);
		
		if (caster.getHandSize() == 0)
			return Response.OK;
		
		if (ta.getStep() == 1) {
			ta.advanceStep();
			caster.setState(State.WaitDiscard);
			return Response.MoreStep;
		}
		
		if (ta.getStep() == 2) {
			if (g.validateChoices()) {
				g.discard((Card) g.getChoices().get(0));
				return Response.OK;
			}
			else
				return Response.MoreStep;
		}
		
		return Response.OK;
	}
	
	
	/* opalAcrolith_animate */
	public static Response opalAcrolith_animate(Game g, StackObject ta) {
		Card enchant = ta.getSource();
		Vector<Modifier> mods = enchant.getModifiers();
		CardTypeModifier ctmod = null;
		
		for (Modifier mod : mods) {
			if (mod instanceof CardTypeModifier) {
				ctmod = (CardTypeModifier) mod;
				if (ctmod.getSetTypes().contains(CardType.ENCHANTMENT))
					break;
			}
		}
		if (ctmod != null)
			mods.remove(ctmod);
		
		enchant.addModifiers(new CardTypeModifier(ta, Operation.SET, Modifier.Duration.PERMANENTLY, CardType.CREATURE),
							 new PTModifier(ta, "2/4", Operation.SET, Modifier.Duration.PERMANENTLY),
							 new CreatureTypeModifier(ta, Modifier.Duration.PERMANENTLY, CreatureType.Soldier));
		return Response.OK;
	}
	
	/* opalAcrolith_petrify */
	public static Response opalAcrolith_petrify(Game g, StackObject ta) {
		Card creature = ta.getSource();
		Vector<Modifier> mods = creature.getModifiers();
		CardTypeModifier ctmod = null;
		
		for (Modifier mod : mods) {
			if (mod instanceof CardTypeModifier) {
				ctmod = (CardTypeModifier) mod;
				if (ctmod.getSetTypes().contains(CardType.CREATURE))
					break;
			}
		}
		if (ctmod != null)
			mods.remove(ctmod);
		creature.addModifiers(new CardTypeModifier(ta, Operation.SET, Modifier.Duration.PERMANENTLY, CardType.ENCHANTMENT));
		return Response.OK;
	}
	
	/* hiddenAncients */
	public static Response hiddenAncients(Game g, StackObject ta) {
		Card enchant = ta.getSource();
		enchant.addModifiers(new CardTypeModifier(ta, Operation.SET, Modifier.Duration.PERMANENTLY, CardType.CREATURE),
							 new PTModifier(ta, "5/5", Operation.SET, Modifier.Duration.PERMANENTLY),
							 new CreatureTypeModifier(ta, Modifier.Duration.PERMANENTLY, CreatureType.Treefolk));
		return Response.OK;
	}

	/* hiddenGuerrillas */
	public static Response hiddenGuerrillas(Game g, StackObject ta) {
		Card enchant = ta.getSource();
		enchant.addModifiers(new CardTypeModifier(ta, Operation.SET, Modifier.Duration.PERMANENTLY, CardType.CREATURE),
							 new PTModifier(ta, "5/3", Operation.SET, Modifier.Duration.PERMANENTLY),
							 new CreatureTypeModifier(ta, Modifier.Duration.PERMANENTLY, CreatureType.Soldier),
							 new EvergreenModifier(ta, Modifier.Duration.PERMANENTLY, Evergreen.TRAMPLE));
		return Response.OK;
	}
	
	/* hiddenSpider */
	public static Response hiddenSpider(Game g, StackObject ta) {
		Card enchant = ta.getSource();
		enchant.addModifiers(new CardTypeModifier(ta, Operation.SET, Modifier.Duration.PERMANENTLY, CardType.CREATURE),
							 new PTModifier(ta, "3/5", Operation.SET, Modifier.Duration.PERMANENTLY),
							 new CreatureTypeModifier(ta, Modifier.Duration.PERMANENTLY, CreatureType.Spider),
							 new EvergreenModifier(ta, Modifier.Duration.PERMANENTLY, Evergreen.REACH));
		return Response.OK;
	}

	/* hiddenHerd */
	public static Response hiddenHerd(Game g, StackObject ta) {
		Card enchant = ta.getSource();
		enchant.addModifiers(new CardTypeModifier(ta, Operation.SET, Modifier.Duration.PERMANENTLY, CardType.CREATURE),
							 new PTModifier(ta, "3/3", Operation.SET, Modifier.Duration.PERMANENTLY),
							 new CreatureTypeModifier(ta, Modifier.Duration.PERMANENTLY, CreatureType.Beast));
		return Response.OK;
	}

	/* hiddenStag */
	public static Response hiddenStag(Game g, StackObject ta) {
		Card enchant = ta.getSource();
		enchant.addModifiers(new CardTypeModifier(ta, Operation.SET, Modifier.Duration.PERMANENTLY, CardType.CREATURE),
							 new PTModifier(ta, "3/2", Operation.SET, Modifier.Duration.PERMANENTLY),
							 new CreatureTypeModifier(ta, Modifier.Duration.PERMANENTLY, CreatureType.Elk, CreatureType.Beast));
		return Response.OK;
	}

	/* hiddenPredators */
	public static Response hiddenPredators(Game g, StackObject ta) {
		Card enchant = ta.getSource();
		enchant.addModifiers(new CardTypeModifier(ta, Operation.SET, Modifier.Duration.PERMANENTLY, CardType.CREATURE),
							 new PTModifier(ta, "4/4", Operation.SET, Modifier.Duration.PERMANENTLY),
							 new CreatureTypeModifier(ta, Modifier.Duration.PERMANENTLY, CreatureType.Beast));
		return Response.OK;
	}
	
	/* fortitude_regen */
	public static Response fortitude_regen(Game g, StackObject aa) {
		Card aura = aa.getSource();
		Card host = aura.getHost();
		return host.regenerate();
	}	
	
	/* gaeasEmbrace_regen */
	public static Response gaeasEmbrace_regen(Game g, StackObject aa) {
		Card aura = aa.getSource();
		Card host = aura.getHost();
		return host.regenerate();
	}	
	
	/* greaterGood */
	public static Response greaterGood(Game g, StackObject aa) {
		Player controller = aa.getController(g);
		Card sacrificedCreature = (Card) aa.getAdditionalData();
		int power = sacrificedCreature.getPower(g);
		int nbCardsToDiscard = 3;
		
		if (aa.getStep() == 1) {
			aa.advanceStep();
			g.drawCards(controller, power);
			return Response.MoreStep;
		}
		
		if (aa.getStep() == 2) {
			aa.advanceStep();
			controller.setState(State.WaitDiscard);
			return Response.MoreStep;
		}
		
		if ((aa.getStep() >= 3) && (aa.getStep() < 3 + nbCardsToDiscard)) {
			if (g.validateChoices()) 			{
				Card chosenCard = (Card) g.getChoices().get(0);
				g.discard(chosenCard);
				if (controller.getHandSize() == 0)
					return Response.OK;
				
				if (aa.getStep() < 2 + nbCardsToDiscard) {
					aa.advanceStep();
					return Response.MoreStep;
				}
				else
					return Response.OK;
			}
			else
				return Response.MoreStep;
		}
		
		return Response.OK;
	}
	
	/* boundingKrasis */
	public static Response boundingKrasis(Game g, StackObject ta) {
		Card target = (Card) ta.getTargetObject(0);
		if (target.isTapped())
			target.untap(g);
		else
			target.tap(g);
		return Response.OK;
	}
	
	/* seasonedMarshal */
	public static Response seasonedMarshal(Game g, StackObject c) {
		((Card) c.getTargetObject(0)).tap(g);
		return Response.OK;
	}
	
	/* stormscapeApprentice_loselife */
	public static Response stormscapeApprentice_loselife(Game g, StackObject c) {
		Player target = (Player) c.getTargetObject(0);
		if (target == null)
			return Response.ErrorInvalidTarget;
		target.loseLife(1);
		return Response.OK;
	}
	
	/* archangelAvacyn_protect */
	public static Response archangelAvacyn_protect(Game g, StackObject ta) {
		Card avacyn = ta.getSource();
		Vector<Card> creatures = g.getBattlefield().getCreaturesControlledBy(avacyn.getController(g));
		for (Card creature : creatures) {
			creature.addModifiers(new EvergreenModifier(ta, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.INDESTRUCTIBLE));
		}
		return Response.OK;
	}
	
	/* archangelAvacyn_creatureDies */
	public static Response archangelAvacyn_creatureDies(Game g, StackObject ta) {
		Card avacyn = ta.getSource();
		
		ContinuousEffect delayedTrigger = ContinuousEffectFactory.create("archangelAvacyn_transform", avacyn);
		g.addContinuousEffect(delayedTrigger);
		return Response.OK;
	}

	/* archangelAvacyn_transform */
	public static Response archangelAvacyn_transform(Game g, StackObject ta) {
		Card avacyn = ta.getSource();
		if (g.getBattlefield().contains(avacyn))
			avacyn.transform(g);
		return Response.OK;
	}

	
	/* evolvingWilds */
	public static Response evolvingWilds(Game g, StackObject aa) {
		Response ret;
		Player controller = aa.getController(g);
		State state = State.WaitChoiceBasicLand;
		
		if (aa.getStep() == 1) {
			ret = libSearchHelperFunction(g, aa, state);
			aa.advanceStep();
		}
		else { // (step == 2)
			if (g.validateChoices()) {
				if ((g.getChoices() != null) && (g.getChoices().size() == 1)) {
					Card land = (Card) g.getChoices().get(0);
					g.move_LIB_to_BFD(land);
					land.tap(g);
				}
				controller.shuffle();
				ret = Response.OK;
			}
			else
				ret = libSearchHelperFunction(g, aa, state);
		}
		return ret;
	}
	
	// fetchlands helper function
	private static Response fetchLandsHelperFunction(Game g, StackObject aa, State state) {
		Response ret;
		Player controller = aa.getController(g);
		
		if (aa.getStep() == 1) {
			ret = libSearchHelperFunction(g, aa, state);
			aa.advanceStep();
		}
		else { // (step == 2)
			if (g.validateChoices() == true) {
				if ((g.getChoices() != null) && (g.getChoices().size() == 1))
					ret = g.move_LIB_to_BFD((Card) g.getChoices().get(0));
				else
					ret = Response.OK;
				controller.shuffle();
			}
			else
				ret = libSearchHelperFunction(g, aa, state);
		}
		return ret;
	}

	/* fetchLand */
	public static Response fetchLand(Game g, StackObject aa) {
		State state;
		if (aa.getName().equals("Flooded Strand"))
			state = Game.State.WaitChoicePlainsIsland;
		else if (aa.getName().equals("Polluted Delta"))
			state = Game.State.WaitChoiceIslandSwamp;
		else if (aa.getName().equals("Bloodstained Mire"))
			state = Game.State.WaitChoiceSwampMountain;
		else if (aa.getName().equals("Wooded Foothills"))
			state = Game.State.WaitChoiceMountainForest;
		else if (aa.getName().equals("Windswept Heath"))
			state = Game.State.WaitChoiceForestPlains;
		
		else if (aa.getName().equals("Marsh Flats"))
			state = Game.State.WaitChoicePlainsSwamp;
		else if (aa.getName().equals("Verdant Catacombs"))
			state = Game.State.WaitChoiceSwampForest;
		else if (aa.getName().equals("Misty Rainforest"))
			state = Game.State.WaitChoiceForestIsland;
		else if (aa.getName().equals("Scalding Tarn"))
			state = Game.State.WaitChoiceIslandMountain;
		else // if (aa.getName().equals("Arid Mesa"))
			state = Game.State.WaitChoiceMountainPlains;
		return fetchLandsHelperFunction(g, aa, state);
	}

	/* glitterfang */
	public static Response glitterfang(Game g, StackObject ta) {
		Card source = ta.getSource();
		return g.move_BFD_to_HND(source);
	}
	
	/* skizzik_sacrifice */
	public static Response skizzik_sacrifice(Game g, StackObject ta) {
		Card source = ta.getSource();
		if (source.getSpellCastUsed().getOption() != Option.CAST_WITH_KICKER)
			return g.sacrifice(ta.getController(g), source);
		return Response.OK;
	}
	
	/* phyrexianNegator */
	public static Response phyrexianNegator(Game g, StackObject ta) {
		Card negator = ta.getSource();
		Player controller = negator.getController(g);
		// Do nothing if the player doesn't control any permanent
		if (g.getBattlefield().getPermanentsControlledBy(controller).size() == 0)
			return Response.OK;
		
		@SuppressWarnings("unchecked")
		Vector<Object> additionalData = (Vector<Object>) ((TriggeredAbility)ta).getAdditionalData();
		Integer amount = (Integer) additionalData.get(0);
		if (ta.getStep() == 1) {
			ta.setTmpVar(amount);
			controller.setState(State.WaitSacrificePermanent);
			ta.advanceStep();
			return Response.MoreStep;
		}
		else {
			if (!g.validateChoices())
				return Response.MoreStep;
			
			g.sacrifice(controller, (Card) g.getChoices().get(0));
			int remainingPermanentsToSac = ta.getTmpVar() - 1;
			if (remainingPermanentsToSac > 0) {
				ta.setTmpVar(remainingPermanentsToSac);
				ta.advanceStep();
				return Response.MoreStep;
			}
			else
				return Response.OK;
		}
	}
	
	/* goblinCadets */
	public static Response goblinCadets(Game g, StackObject ta) {
		Card gob = ta.getSource();
		g.removeCreatureFromCombat(gob);
		Player opponent = (Player) ta.getTargetObject(0);
		gob.setController(g, opponent);
		return Response.OK;
	}
	
	/* sleeperAgent_give */
	public static Response sleeperAgent_give(Game g, StackObject ta) {
		Card agent = ta.getSource();
		Player opponent = (Player) ta.getTargetObject(0);
		agent.setController(g, opponent);
		return Response.OK;
	}
	
	/* sleeperAgent_damage */
	public static Response sleeperAgent_damage(Game g, StackObject ta) {
		return ta.getSource().dealNonCombatDamageTo(g, ta.getController(g), 2);
	}
	
	/* gildedDrake */
	public static Response gildedDrake(Game g, StackObject ta) {
		Card drake = ta.getSource();
		Player controller = drake.getController(g);
		Player opponent = controller.getOpponent();
		Card target = (Card) ta.getTargetObject(0);
		
		if ((target == null) || !target.isTargetableBy(g, drake))
			g.sacrifice(controller, drake);
		else {
			drake.setController(g, opponent);
			target.setController(g, controller);
		}
		return Response.OK;
	}
	
	/* fleshReaver */
	public static Response fleshReaver(Game g, StackObject ta) {
		Card reaver = ta.getSource();
		Player controller = reaver.getController(g);
		Integer amount = (Integer) ((TriggeredAbility)ta).getAdditionalData();
		return reaver.dealNonCombatDamageTo(g, controller, amount);
	}

	/* jackalPup */
	public static Response jackalPup(Game g, StackObject ta) {
		Card pup = ta.getSource();
		Player controller = pup.getController(g);
		@SuppressWarnings("unchecked")
		Vector<Object> additionalData = (Vector<Object>) ((TriggeredAbility)ta).getAdditionalData();
		Integer amount = (Integer) additionalData.get(0);
		return pup.dealNonCombatDamageTo(g, controller, amount);
	}
	
	/* thranQuarry_sacrifice */
	public static Response thranQuarry_sacrifice(Game g, StackObject ta) {
		return g.sacrifice(ta.getController(g), ta.getSource());
	}
	
	/* glimmervoid_sacrifice */
	public static Response glimmervoid_sacrifice(Game g, StackObject ta) {
		return g.sacrifice(ta.getController(g), ta.getSource());
	}
	
	/* ballLightning_sacrifice */
	public static Response ballLightning_sacrifice(Game g, StackObject ta) {
		return g.sacrifice(ta.getController(g), ta.getSource());
	}
	
	/* ichorid_sacrifice */
	public static Response ichorid_sacrifice(Game g, StackObject ta) {
		return g.sacrifice(ta.getController(g), ta.getSource());
	}
	
	/* vengevine */
	public static Response vengevine(Game g, StackObject ta) {
		Response ret;
		
		Card source = ta.getSource();
		ret = g.move_GYD_to_BFD(source);
		return ret;
	}
	
	/* ichorid_reanimate */
	public static Response ichorid_reanimate(Game g, StackObject ta) {
		Card source = ta.getSource();
		Player controller = ta.getController(g);
		Response ret;
		
		// Step 1 : prompt controller to exile another black creature card from his graveyard
		if (ta.getStep() == 1) {
			controller.setState(State.WaitExileForIchorid);
			ta.advanceStep();
			ret = Response.MoreStep;
		}
		else { // Step 2 : check if chosen creature can be exiled, and if so : reanimate Ichorid
			if (g.validateChoices()) {
				if ((g.getChoices() != null) && (g.getChoices().size() == 1)) {
					Card exiledCreature = (Card) g.getChoices().get(0);
					// make sure chosen card is not Ichorid
					if (exiledCreature == source)
						ret = Response.MoreStep;
					else {
						ret = g.move_GYD_to_EXL(exiledCreature);
						if (ret == Response.OK)
							ret = g.move_GYD_to_BFD(source);
					}
				}
				else // player clicked "DONE"
				{
					ret = Response.OK;
				}
			}
			else
				ret = Response.MoreStep;
		}
		return ret;
	}
	
	/* remembrance_tutor */
	public static Response remembrance_tutor(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		
		if (ta.getStep() == 1) {
			ta.advanceStep();
			controller.setState(State.WaitChoiceRemembrance);
			return Response.MoreStep;
		}
		
		if (ta.getStep() == 2) {
			// player clicked incorrect card
			if (!g.validateChoices()) 
				return Response.MoreStep;
			
			// player clicked "Done" (chose not to pick a card)
			if (g.getChoices().size() == 0) { 
				controller.shuffle();
				return Response.OK;
			}
			
			// player chose a valid card
			g.move_LIB_to_HND((Card) g.getChoices().get(0));
			controller.shuffle();
			return Response.OK;
		}
		
		// Should never get here
		return Response.Error;
	}
	
	/* faunaShaman */
	public static Response faunaShaman(Game g, StackObject aa) {
		Response ret;

		if (aa.getStep() == 1) {
			ret = libSearchHelperFunction(g, aa, Game.State.WaitChoiceCreatureCard);
			aa.advanceStep();
		}
		else
		{
			if (g.validateChoices() == true) {
				if ((g.getChoices() != null) && (g.getChoices().size() == 1))
					g.move_LIB_to_HND((Card) g.getChoices().get(0));
				aa.getController(g).shuffle();
				ret = Response.OK;
			}
			else {
				ret = libSearchHelperFunction(g, aa, Game.State.WaitChoiceCreatureCard);
			}
		}
		return ret;
	}
	
	/* hellrider */
	public static Response hellrider(Game g, StackObject aa) {
		Card hellrider = aa.getSource();
		Player controller = aa.getController(g);
		Player defending = controller.getOpponent();
		return hellrider.dealNonCombatDamageTo(g, defending, 1);
	}
	
	/* equip */
	public static Response equip(Game g, StackObject c) {
		Response r;
		ActivatedAbility aa = (ActivatedAbility)c;
		
		Card target = (Card)aa.getTargetObject(0);
		if (target == null)
			return Response.ErrorInvalidTarget;
		
		if (target.isCreature(g) == false)
			return Response.ErrorInvalidTarget;
		
		if (target.isOTB(g) == false)
			return Response.ErrorIncorrectZone;
		
		r = g.attach(aa.getSource(), target);
		return r;
	}

	/* cranialPlating_attach */
	public static Response cranialPlating_attach(Game g, StackObject aa) {
		return g.attach(aa.getSource(), (Card)aa.getTargetObject(0));
	}
	
	/* Fading */
	public static Response fadingRemoveCounter(Game g, StackObject ta) {
		Card permanent = ta.getSource();
		int nbFadeCounters;

		// 1. count number of remaining Fade counters
	    nbFadeCounters = permanent.getNbCountersOfType(g, CounterType.FADE);
		
	    // 2.a if there's at least one counter, remove it
	    if (nbFadeCounters > 0) {
	    	permanent.removeCounter(g, CounterType.FADE, 1);
	    }
	    else { // 2.b else, sacrifice the permanent
	    	g.sacrifice(ta.getController(g), permanent);
	    }
	    return Response.OK;
	}
	
	/* greenerPastures */
	public static Response greenerPastures(Game g, StackObject ta) {
		Card token = g.createSingleToken(Token.GREEN_SAPROLING_11, ta.getSource());
		token.setController(g, g.getActivePlayer());
		return Response.OK;
	}
	
	/* saprolingBurst_makeToken */
	public static Response saprolingBurst_makeToken(Game g, StackObject ta) {
		Card source = ta.getSource();
		g.createSingleToken(Token.GREEN_SAPROLING_XX, source);
		return Response.OK;
	}
	
	/* westvaleAbbey_make_token */
	public static Response westvaleAbbey_make_token(Game g, StackObject aa) {
		g.createSingleToken(Token.WHITE_BLACK_HUMAN_CLERIC_11, aa.getSource());
		return Response.OK;
	}
	
	/* westvaleAbbey_transform */
	public static Response westvaleAbbey_transform(Game g, StackObject aa) {
		Card source = aa.getSource();
		int step = aa.getStep();
		
		if (aa.getStep() == 1) {
			aa.getController(g).setState(Game.State.WaitSacrificeCreature);
			aa.advanceStep();
			return Response.MoreStep;
		}
		else
		{
			if (g.validateChoices()) {
				g.sacrifice(aa.getController(g), (Card) g.getChoices().get(0));
				if (step == 6) {
					Card demon = source.transform(g);
					demon.untap(g);
					return Response.OK;		
				}
				else
					aa.advanceStep();
			}
			return Response.MoreStep;
		}
	}
	
	/* Jace, Vryn's Prodigy activated ability (loot)*/
	public static Response jaceVrynProdigy(Game g, StackObject aa) {
		Player controller = aa.getController(g);
		Card source = aa.getSource();
		int step = aa.getStep();
		
		if (step == 1) {
			aa.advanceStep();
			g.drawCards(controller, 1);
			return Response.MoreStep;
		}
		else if (step == 2) {
			controller.setState(State.WaitDiscard);
			aa.advanceStep();
			return Response.MoreStep;
		}
		else if (step == 3) {
			if (g.validateChoices()) {
				Card choice = (Card) g.getChoices().get(0);
				g.discard(choice);
				if (controller.getGraveyard().size() >= 5) {
					// Transform
					g.transformCreatureToPlaneswaler(source);
				}
			}
		}
		return Response.OK;
	}

	/* lilianaHeretical_transform */
	public static Response lilianaHeretical_transform(Game g, StackObject aa) {
		Card source = aa.getSource();
		
		// Check that the card has not already been transformed
		if (source.isOTB(g)) {
			// Transform
			g.transformCreatureToPlaneswaler(source);
			
			// put the zombie token onto the battlefield
			g.createSingleToken(Token.BLACK_ZOMBIE_22, source);
		}
		return Response.OK;
	}
	
	/* Vanishing */
	public static Response removeTimeCounter(Game g, StackObject ta) {
		Card source = ta.getSource();
		int nbTimeCounters;

		// Check for number of Time counters
		nbTimeCounters = source.getNbCountersOfType(g, CounterType.TIME);
		
		// If there are no counters, we cannot remove one, so do nothing
		if (nbTimeCounters == 0)
			return Response.OK;
		
		// Remove a Time counter
		source.removeCounter(g, CounterType.TIME, 1);
		return Response.OK;
	}
	
	public static Response vanishingSacrifice(Game g, StackObject ta) {
		Card source = ta.getSource();
		return g.sacrifice(ta.getController(g), source);
	}
	
	/* glacialChasm_upkeep */
	public static Response glacialChasm_upkeep(Game g, StackObject ta) {
		Response ret = Response.OK;
		int step = ta.getStep();
		
		if (step == 1)
		{
			ta.getController(g).setState(State.PromptPayUpkeepCost);
			ta.advanceStep();
			ret = Response.MoreStep;
		}
		else if (step == 2)
		{
			Card source = ta.getSource();
			source.addCounter(g, CounterType.AGE, 1);
			
			if (g.getAnswer() == Answer.No)
			{
				g.sacrifice(ta.getController(g), ta.getSource());
				ret = Response.OK;
			}
			else {
				ta.getController(g).loseLife(source.getNbCountersOfType(g, CounterType.AGE) * 2);
			}
		}
		return ret;
	}
	
	/* torrentialGearhulk */
	public static Response torrentialGearhulk(Game g, StackObject ta) {
		Response ret;
		Player controller = ta.getController(g);
		Card target = (Card) ta.getTargetObject(0);

		if (ta.getStep() == 1) {
			ta.advanceStep();
			controller.shuffle();
			controller.setState(State.PromptCastWithoutPayingManaCost);
			return Response.MoreStep;
		}
		else { //if (step == 2) {
			g.getStack().removeObject(ta);
			if (g.getAnswer() == Answer.Yes) {
				g.addContinuousEffect(ContinuousEffectFactory.create("torrentialGearhulk_exile", ta.getSource(), target));
				ret = g.castWithoutPayingManaCost(target);
			}
			else // player chose not to play the card
				ret = Response.OK;
		}
		return ret;
	}
	
	/* naturalObsolescence */
	public static Response naturalObsolescence(Game g, StackObject spell) {
		Card target = (Card) spell.getTargetObject(0);
		g.move_BFD_to_BOTTOMLIB(target);
		return Response.OK;
	}
	
	/* temporalAperture */
	public static Response temporalAperture(Game g, StackObject aa) {
		Response ret;
		Player controller = aa.getController(g);
		Library lib = controller.getLibrary();

		Card topCard;
		
		if (aa.getStep() == 1) {
			aa.advanceStep();
			controller.shuffle();
			topCard = lib.getTopCard();
			System.out.println(controller + " reveals " + topCard);
			controller.setState(State.PromptCastWithoutPayingManaCost);
			return Response.MoreStep;
		}
		else { //if (step == 2) {
			g.getStack().removeObject(aa);
			topCard = lib.getTopCard();
			if (g.getAnswer() == Answer.Yes) {
				if (topCard.isLandCard()) {
					ret = g.playLand(topCard, Name.Library);
				}
				else {
					if (g.castWithoutPayingManaCost(topCard) == Response.OK)
						ret = Response.MoreStep;
					else
						ret = Response.OK;	
				}
			}
			else // player chose not to play the card
				ret = Response.OK;
		}
		return ret;
	}
	
	/* Cascade */
	@SuppressWarnings("unchecked")
	public static Response cascade(Game g, StackObject ta) {
		Response ret;
		Player controller = ta.getController(g);
		Library lib = controller.getLibrary();
		Card spell = ta.getSource();
		int cmc = spell.getConvertedManaCost(g);
		Vector<Card> exiledCards = new Vector<Card>();
		Card freeSpell = null;
		Card topCard;
		
		if (ta.getStep() == 1) {
			ta.advanceStep();
			do {
				topCard = lib.getTopCard();
				g.move_LIB_to_EXL(topCard);
				exiledCards.add(0, topCard);
				if (!topCard.isLandCard() && (topCard.getConvertedManaCost(g) < cmc)) {
					freeSpell = topCard;
					ta.setAdditionalData(exiledCards);
				}
				
			} while ((freeSpell == null) && !lib.isEmpty());
			System.out.println(controller + " exiles " + exiledCards);
			controller.setState(State.PromptCastWithoutPayingManaCost);
			return Response.MoreStep;
		}
		else { //if (step == 2) {
			exiledCards = (Vector<Card>) ta.getAdditionalData();
			g.getStack().removeObject(ta);
			
			if (g.getAnswer() == Answer.Yes)
			{
				if (exiledCards != null)
				{
					freeSpell = exiledCards.get(0);
					if (g.castWithoutPayingManaCost(freeSpell) == Response.OK)
					{
						exiledCards.remove(freeSpell);
						ret = Response.MoreStep;
					}
					else
						ret = Response.OK;
				}
				else
					ret = Response.OK;
			}
			else // player chose to not play the card
			{
				ret = Response.OK;
			}
			
			// shuffle the exiled cards and put them on the bottom of the library
			if (exiledCards.size() > 0)
			{
				exiledCards = Zone.shuffle(exiledCards);
				for (int i = 0; i < exiledCards.size(); i++)
					g.move_EXL_to_BOTTOMLIB(exiledCards.get(i));
			}
		}
		
		return ret;
	}
	
	/* driftingDjinn_upkeep */
	public static Response driftingDjinn_upkeep(Game g, StackObject ta) {
		Response ret = Response.OK;
		
		if (ta.getStep() == 1) {
			ta.getController(g).setState(State.PromptPayDriftingDjinn);
			ta.advanceStep();
			ret = Response.MoreStep;
		}
		else {
			if (g.getAnswer() == Answer.No) {
				g.sacrifice(ta.getController(g), ta.getSource());
				ret = Response.OK;
			}
		}
		return ret;
	}
	
	/* faithHealer */
	public static Response faithHealer(Game g, StackObject aa) {
		Card sacrificedEnchantment = (Card) aa.getAdditionalData();
		Player controller = aa.getController(g);
		controller.gainLife(sacrificedEnchantment.getConvertedManaCost(g));
		return Response.OK;
	}
	
	/* endlessWurm */
	public static Response endlessWurm(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		int step = ta.getStep();

		// if controller controls no enchantments : sacrifice wurm
		if (controller.getNbEnchantmentsControlled() == 0) {
			g.sacrifice(controller, ta.getSource());
			return Response.OK;
		}
		
		if (step == 1) {
			ta.getController(g).setState(State.PromptSacrificeEnchantment);
			ta.advanceStep();
			return Response.MoreStep;
		}
		else if (step == 2)
		{
			if (g.getAnswer() == Answer.No) {
				// controller chose not to pay : sacrifice wurm
				g.sacrifice(controller, ta.getSource());
				return Response.OK;
			}
			else {
				controller.setState(State.WaitSacrificeEnchantment);
				ta.advanceStep();
				return Response.MoreStep;
			}
		}
		else { // step == 3
			if (g.validateChoices()) {
				Card enchant = (Card) g.getChoices().get(0);
				g.sacrifice(controller, enchant);
				return Response.OK;
			}
			else
				return Response.MoreStep;
		}
	}
	
	/* childOfGaea_upkeep */
	public static Response childOfGaea_upkeep(Game g, StackObject ta) {
		Response ret = Response.OK;
		
		if (ta.getStep() == 1) {
			ta.getController(g).setState(State.PromptPayChildOfGaea);
			ta.advanceStep();
			ret = Response.MoreStep;
		}
		else {
			if (g.getAnswer() == Answer.No) {
				g.sacrifice(ta.getController(g), ta.getSource());
				ret = Response.OK;
			}
		}
		return ret;
	}
	
	/* Echo */
	public static Response echo(Game g, StackObject ta) {
		Response ret = Response.OK;

		if (ta.getStep() == 1) {
			ta.getController(g).setState(State.PromptPayEchoCost);
			ta.advanceStep();
			ret = Response.MoreStep;
		}
		else
		{
			if (g.getAnswer() == Answer.No)
			{
				g.sacrifice(ta.getController(g), ta.getSource());
				ret = Response.OK;
			}
			else
				ta.getSource().setEchoPaid();
		}
		return ret;
	}
	
	/* cumulativeUpkeep */
	public static Response cumulativeUpkeep(Game g, StackObject ta) {
		Response ret = Response.OK;
		
		if (ta.getStep() == 1) {
			ta.getSource().addCounter(g, CounterType.AGE, 1);
			ta.getController(g).setState(State.PromptPayCumulativeUpkeep);
			ta.advanceStep();
			ret = Response.MoreStep;
		}
		else  {
			if (g.getAnswer() == Answer.No) {
				g.sacrifice(ta.getController(g), ta.getSource());
				ret = Response.OK;
			}
			else
				ta.getSource().setEchoPaid();
		}
		return ret;
	}
	
	/* tabernacle_upkeep */
	public static Response tabernacle_upkeep(Game g, StackObject ta) {
		Response ret;
		Card creature = ta.getSource();
		
		if (ta.getStep() == 1)
		{
			ta.getController(g).setState(State.PromptPay_1mana);
			ta.advanceStep();
			ret = Response.MoreStep;
		}
		else 
		{
			if (g.getAnswer() == Answer.No) // controller chooses not to pay -> destroy creature
				g.destroy(creature, true);
			ret = Response.OK;
		}
		return ret;
	}
	
	/* carnophage */
	public static Response carnophage(Game g, StackObject ta) {
		Response ret;
		Card carno = ta.getSource();
		Player controller = carno.getController(g);
		
		if (ta.getStep() == 1)
		{
			ta.getController(g).setState(State.PromptPay_1life);
			ta.advanceStep();
			ret = Response.MoreStep;
		}
		else // if (step == 2)
		{
			if (g.getAnswer() == Answer.No) // controller chooses not to pay -> tap Carnophage
				carno.tap(g);
			else
				controller.loseLife(1);
			ret = Response.OK;
		}
		return ret;
	}
	
	/* pendrellFlux_upkeep */
	public static Response pendrellFlux_upkeep(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		Card host = ta.getSource();
		
		if (ta.getStep() == 1) {
			ta.advanceStep();
			// Ask if player wants to pay the upkeep cost
			controller.setState(State.PromptPayUpkeepCost);
			return Response.MoreStep;	
		}

		if (ta.getStep() == 2) {
			if (g.getAnswer() == Answer.No)
				g.sacrifice(controller, host);
		}

		return Response.OK;
	}
	
	/* masticore_discard */
	public static Response masticore_discard(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		int step = ta.getStep();

		// if controller has fewer than one card in hand : sacrifice masticore
		if (controller.getHandSize() < 1) {
			g.sacrifice(controller, ta.getSource());
			return Response.OK;
		}
		
		if (step == 1) {
			ta.getController(g).setState(State.PromptDiscardToPay);
			ta.advanceStep();
			return Response.MoreStep;
		}
		else if (step == 2)
		{
			if (g.getAnswer() == Answer.No) {
				// controller chose not to pay : sacrifice masticore
				g.sacrifice(controller, ta.getSource());
				return Response.OK;
			}
			else {
				controller.setState(State.WaitDiscard);
				ta.advanceStep();
				return Response.MoreStep;
			}
		}
		else { // step == 3
			if (g.validateChoices()) {
				Card choice = (Card) g.getChoices().get(0);
				g.discard(choice);
				return Response.OK;
			}
			else
				return Response.MoreStep;
		}
	}

	/* exaltedAngel */
	public static Response exaltedAngel(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		int dmgAmount = (Integer) ta.getAdditionalData();
		controller.gainLife(dmgAmount);
		return Response.OK;
	}
	
	/* wirewoodSymbiote */
	public static Response wirewoodSymbiote(Game g, StackObject aa) {
		Card target = (Card) aa.getTargetObject(0);
		target.untap(g);
		return Response.OK;
	}
	
	/* craterhoofBehemoth */
	public static Response craterhoofBehemoth(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		Vector<Card> creatures = g.getBattlefield().getCreaturesControlledBy(controller);
		int x = creatures.size();
		for (Card creature : creatures) {
			creature.addModifiers(new PTModifier(ta, "+" + x + "/+" + x +"", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN),
					              new EvergreenModifier(ta, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.TRAMPLE));
		}
		return Response.OK;
	}
	
	/* arlinn_tapToDamage */
	public static Response arlinn_tapToDamage(Game g, StackObject aa) {
		Card creature = aa.getSource();
		return creature.dealNonCombatDamageTo(g, (Damageable) aa.getTargetObject(0), creature.getPower(g));
	}
		
	/* saprolingBurst_destroy */
	public static Response saprolingBurst_destroy(Game g, StackObject aa) {
		Vector<MtgObject> bf = g.getBattlefield().getObjects();
		int i = 0;
		
		while (i < bf.size()) {
			Card c = (Card) bf.get(i); 
			if (c.getSource() == aa.getSource()) {
				g.destroy(c, false);
				i--;
			}
			i++;
		}
		return Response.OK;
	}
	
	/* narset_rebound */
	public static Response narset_rebound(Game g, StackObject so) {
		Object additionalData = so.getAdditionalData();
		if (additionalData instanceof Card) {
			Card triggeringSpell = (Card) additionalData;
			triggeringSpell.addStaticAbility(StaticAbilityFactory.create("rebound", triggeringSpell));
		}
		return Response.OK;
	}
	
	/**** Garruk Wildspeaker **************************************************************/
	/* garrukW_1 (untap 2 target lands) */
	public static Response garrukW_1(Game g, StackObject aa) {
		for (int iTarget = 0; iTarget < 2; iTarget++)
			((Card) aa.getTargetObject(iTarget)).untap(g);
		return Response.OK;
	}
	/* garrukW_2 (create 3/3 beast token) */
	public static Response garrukW_2(Game g, StackObject aa) {
		g.createSingleToken(Token.GREEN_BEAST_33, aa);
		return Response.OK;
	}
	/* garrukW_3 (overrun) */
	public static Response garrukW_3(Game g, StackObject aa) {
		Player controller = aa.getController(g);
		Vector<Card> creatures = g.getBattlefield().getCreaturesControlledBy(controller);
		for (Card creature : creatures) {
			creature.addModifiers(new PTModifier(aa, "+3/+3", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN),
								  new EvergreenModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.TRAMPLE));
		}
		return Response.OK;
	}
	/**************************************************************************************/
	
	/**** Jace, Telepath Unbound **********************************************************/
	/* jaceTU_1 (-2/-0 to target creature) */
	public static Response jaceTU_1(Game g, StackObject aa) {
		Card target = (Card) aa.getTargetObject(0);
		if (target != null)
			target.addModifiers(new PTModifier(aa, "-2/-0", Operation.ADD, Modifier.Duration.UNTIL_YOUR_NEXT_TURN));
		return Response.OK;
	}
	/**************************************************************************************/
	
	/**** Narset Transcendent **********************************************************/
	/* narsetT_1 (look at top card and put it in hand if noncreature nonland) */
	public static Response narsetT_1(Game g, StackObject aa) {
		Player controller = aa.getController(g);
		Library lib = controller.getLibrary();
		int step = aa.getStep();

		// if the library is empty, immediately return
		if (lib.size() == 0)
			return Response.OK;
		
		Card topCard = lib.getTopCard();		
		if (aa.getStep() == 1) {
			aa.setLibManip(lib.getXTopCards(1));

			// get the top card and check if it's noncreature nonland
			if (!topCard.isCreatureCard() && !topCard.isLandCard()) {
				controller.setState(State.PromptDoYouWantPutTheCardInYourHand);  // prompt player if he wants to put it in his hand
				aa.goToStep(2);
				return Response.MoreStep;
			}
			else
			{
				controller.setState(State.WaitChoiceLookTop);
				aa.goToStep(3);
				return Response.MoreStep;
			}
		}
		else if (step == 2) {
			if (g.getAnswer() == Answer.Yes) {
				// TODO : Reveal card to all players
				g.move_LIB_to_HND(topCard);
			}
		}
		else if (step == 3) {
			if (!g.validateChoices())
				return Response.MoreStep;
		}
		return Response.OK;
	}
	/* narsetT_2 (next instant or sorcery gains rebound) */
	public static Response narsetT_2(Game g, StackObject aa) {
		Card narset = aa.getSource();
		ContinuousEffect ce = ContinuousEffectFactory.create("narset_rebound", narset);
		g.addContinuousEffect(ce);
		return Response.OK;
	}
	/* narsetT_3 (super emblem : opponents cant cast noncreature spells) */
	public static Response narsetT_3(Game g, StackObject aa) {
		Card narset = aa.getSource();
		g.createEmblem(narset, "narsetT_emblem");
		return Response.OK;
	}
	/***********************************************************************/
	
	/********* Nahiri, the Harbinger ***************************************/
	/* nahiriTH_1 (discard and draw) */
	public static Response nahiriTH_1(Game g, StackObject aa) {
		Response ret = Response.OK;
		Player controller = aa.getController(g);
		int step = aa.getStep();
		
		if (aa.getStep() == 1) {
			// immediately return if controller has no card in hand
			if (controller.getHandSize() < 1)
				return Response.OK;
			
			controller.setState(State.WaitDiscard);
			aa.advanceStep();
			return Response.MoreStep;
		}
		else if (step == 2)
		{
			if (!g.validateChoices())
				return Response.MoreStep;
			else
			{
				g.discard((Card) g.getChoices().get(0));
				aa.advanceStep();
				ret = g.drawCards(controller, 1);
			}
		}
		return ret;
	}
	/* nahiriTH_2 (exile a permanent) */
	public static Response nahiriTH_2(Game g, StackObject aa) {
		return g.move_BFD_to_EXL((Card) aa.getTargetObject(0));
	}
	/* nahiriTH_3 (sneak attack, kinda) */
	public static Response nahiriTH_3(Game g, StackObject aa) {
		if (aa.getStep() == 1)
		{
			libSearchHelperFunction(g, aa, Game.State.WaitChoiceArtifactOrCreatureCard);
			aa.advanceStep();
			return Response.MoreStep;
		}
		else
		{
			if (g.validateChoices() == true)
			{
				if ((g.getChoices() != null) && (g.getChoices().size() == 1)) {
					Card chosenCreatureOrArtifact = (Card) g.getChoices().get(0);
					g.move_LIB_to_BFD(chosenCreatureOrArtifact);  // put in onto the battlefield
					chosenCreatureOrArtifact.addModifiers(new EvergreenModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.HASTE)); // it gains haste
					aa.getController(g).shuffle();
					
					// Delayed triggered effect that will return the card at the beginning of the next end step.
					ContinuousEffect delayedTrigger = ContinuousEffectFactory.create("nahiriUltimate", aa.getSource());
					delayedTrigger.setAdditionalData(chosenCreatureOrArtifact);
					g.addContinuousEffect(delayedTrigger);

				}
				aa.getController(g).shuffle();
			}
			else {
				return libSearchHelperFunction(g, aa, Game.State.WaitChoiceArtifactOrCreatureCard);
			}
		}
		return Response.OK;
	}
	/***********************************************************************/
	
	/****** Chandra, Flamecaller *******************************************/
	/* chandraF_1 (put tokens with haste) */
	public static Response chandraF_1(Game g, StackObject aa) {
		Vector<Card> tokens = g.createTokens(Token.RED_ELEMENTAL_31_HASTE, 2, aa.getSource());
		// Delayed triggered effect that will exile the tokens at the beginning of the next end step.
		ContinuousEffect delayedTrigger = ContinuousEffectFactory.create("chandraF_1_delayed", aa.getSource());
		delayedTrigger.setAdditionalData(tokens);
		g.addContinuousEffect(delayedTrigger);
		return Response.OK;
	}
	/* chandraF_2 (discard hand and draw) */
	public static Response chandraF_2(Game g, StackObject aa) {
		Response ret = Response.OK;
		Player controller = aa.getController(g);
		int handSize = controller.getHandSize();
		
		if (aa.getStep() == 1) {
			aa.advanceStep();
			controller.discardAllCardsInHand();
			ret = g.drawCards(controller, handSize + 1);
		}
		return ret;
	}
	/* chandraF_3 (X damage to each creature) */
	public static Response chandraF_3(Game g, StackObject aa) {
		Card chandra = aa.getSource();
		for (Card creature : g.getBattlefield().getCreatures())
			chandra.dealNonCombatDamageTo(g, creature, aa.getXValue());
		return Response.OK;
	}
	/***********************************************************************/
	
	
	/******** Ob Nixilis Reignited *****************************************/
	/* nixilisR_1 (draw and lose 1 life) */
	public static Response nixilisR_1(Game g, StackObject aa) {
		Response ret = Response.OK;
		Player controller = aa.getController(g);
		
		if (aa.getStep() == 1) {
			aa.advanceStep();
			controller.loseLife(1);
			ret = g.drawCards(controller, 1);
		}
		return ret;
	}
	/***********************************************************************/
	
	/********* Nissa, Voice of Zendikar ************************************/
	/* nissaVOZ_1 (put a plant token) */
	public static Response nissaVOZ_1(Game g, StackObject aa) {
		g.createSingleToken(Token.GREEN_PLANT_01, aa.getSource());
		return Response.OK;
	}
	/* nissaVOZ_2 (+1/+1 counter on all creatures) */
	public static Response nissaVOZ_2(Game g, StackObject aa) {
		Player controller = aa.getController(g);
		
		Vector<Card> creatures = g.getBattlefield().getCreaturesControlledBy(controller);
		for (Card creature : creatures)
			creature.addCounter(g, CounterType.PLUS_ONE, 1);
		return Response.OK;
	}
	/* nissaVOZ_3 (gain life and draw) */
	public static Response nissaVOZ_3(Game g, StackObject aa) {
		Response ret = Response.OK;
		Player controller = aa.getController(g);
		int X = controller.getNbLandsControlled();
		
		if (aa.getStep() == 1) {
			aa.advanceStep();
			controller.gainLife(X);
			g.drawCards(controller, X);
		}
		return ret;
	}
	/***********************************************************************/
	
	/**** Arlinn Kord *******************************************************************/
	/* arlinnKord_1 (+2/+2 vigilance haste) */
	public static Response arlinnKord_1(Game g, StackObject aa) {
		Card target = (Card) aa.getTargetObject(0);
		if (target != null)
			target.addModifiers(new PTModifier(aa, "+2/+2", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN),
								new EvergreenModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.VIGILANCE,Evergreen.HASTE));
		return Response.OK;
	}
	/* arlinnKord_2 (put Wolf token and transform) */
	public static Response arlinnKord_2(Game g, StackObject aa) {
		g.createSingleToken(Token.GREEN_WOLF_22, aa);
		aa.getSource().transform(g);
		return Response.OK;
	}
	/************************************************************************************/
	
	/**** Arlinn, Embraced by the Moon *******************************************************************/
	/* arlinnEBTM_1 (creatures +1/+1 trample) */
	public static Response arlinnEBTM_1(Game g, StackObject aa) {
		Player controller = aa.getController(g);
		Vector<Card> creatures = g.getBattlefield().getCreaturesControlledBy(controller);
		for (Card creature : creatures) {
			creature.addModifiers(new PTModifier(aa, "+1/+1", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN),
								  new EvergreenModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.TRAMPLE));
		}
		return Response.OK;
	}
	/* arlinnEBTM_2 (3 damage and transform) */
	public static Response arlinnEBTM_2(Game g, StackObject aa) {
		Response ret;
		Card arlinn = aa.getSource();
		MtgObject target = aa.getTargetObject(0);
		ret = arlinn.dealNonCombatDamageTo(g, (Damageable) target, 3);
		arlinn.transform(g);
		return ret;
	}
	/* arlinnEBTM_3 (emblem) */
	public static Response arlinnEBTM_3(Game g, StackObject aa) {
		g.createEmblem(aa.getSource(), "arlinnEBTM_emblem");	
		return Response.OK;
	}
	/***********************************************************************/
	
	
	/**** Sorin, Grim Nemesis *********************************************************/
	/* sorinGN_1 (reveal top card, opp loses life = CMC) */
	public static Response sorinGN_1(Game g, StackObject aa) {
		Player controller = aa.getController(g);
		Library lib = controller.getLibrary();
		if (lib.isEmpty())
			return Response.OK;
		Card topCard = lib.getTopCard();
		g.move_LIB_to_HND(topCard);
		controller.getOpponent().loseLife(topCard.getConvertedManaCost(g));
		return Response.OK;
	}
	/* sorinGN_2 (Death Grasp kinda) */
	public static Response sorinGN_2(Game g, StackObject aa) {
		Card sorin = aa.getSource();
		Card target = (Card) aa.getTargetObject(0);
		Player controller = sorin.getController(g);
		
		sorin.dealNonCombatDamageTo(g, target, aa.getXValue());
		controller.gainLife(aa.getXValue());
		return Response.OK;
	}
	/* sorinGN_3 (put tokens) */
	public static Response sorinGN_3(Game g, StackObject aa) {
		g.createTokens(Token.BLACK_VAMPIRE_KNIGHT_11_LIFELINK, g.getHighestLifeTotal(), aa.getSource());
		return Response.OK;
	}
	/**********************************************************************************/
	
	/******** Jace, Unraveler of Secrets **********************************/
	/* jaceUOS_1 (scry 1 and draw) */
	public static Response jaceUOS_1(Game g, StackObject aa) {
		Response ret = Response.OK;
		if (aa.getStep() != 11)
		{
			ret = scry(g, aa, 1);
			if (ret == Response.OK)
				aa.goToStep(10);	
		}
		if (aa.getStep() == 10)
		{
			aa.advanceStep();
			ret = g.drawCards(aa.getController(g), 1);
		}
		return ret;
	}
	/* jaceUOS_2 (bounce a creature) */
	public static Response jaceUOS_2(Game g, StackObject aa) {
		return g.move_BFD_to_HND((Card) aa.getTargetObject(0));
	}	
	/* jaceUOS_3 (create super emblem) */
	public static Response jaceUOS_3(Game g, StackObject aa) {
		System.out.println("Ability not coded yet.");
		return Response.OK;
	}
	/***********************************************************************/
	
	/********************************* Nissa, Vital Force **************************************/
	/* nissaVF_1 (animate a land) */
	public static Response nissaVF_1(Game g, StackObject aa) {
		Card target = (Card) aa.getTargetObject(0);
		
		target.untap(g);
		target.addModifiers(new CardTypeModifier(aa, Operation.ADD, Modifier.Duration.UNTIL_YOUR_NEXT_TURN, CardType.CREATURE),
				            new PTModifier(aa, "5/5", Operation.SET, Modifier.Duration.UNTIL_YOUR_NEXT_TURN),
				            new CreatureTypeModifier(aa, Modifier.Duration.UNTIL_YOUR_NEXT_TURN, CreatureType.Elemental),
				            new EvergreenModifier(aa, Modifier.Duration.UNTIL_YOUR_NEXT_TURN, Evergreen.HASTE));
		return Response.OK;
	}
	/* nissaVF_1 (animate a land) */
	public static Response nissaVF_3(Game g, StackObject aa) {
		g.createEmblem(aa.getSource(), "nissaVF_emblem");
		return Response.OK;
	}
	/***********************************************************************/
	
	/********************************* Chandra, Torch of Defiance **************************************/
	/* chandraTOD_1 (reveal top card and play it or two damage do each opponent) */
	public static Response chandraTOD_1(Game g, StackObject aa) {
		Player controller = aa.getController(g);
		Card chandra = aa.getSource();
		
		if (aa.getStep() == 1) {
			aa.advanceStep();
			if (!controller.getLibrary().isEmpty()) {
				Card topCard = controller.getLibrary().getTopCard();
				g.move_LIB_to_EXL(topCard);
				if (!topCard.isLandCard()) {
					aa.setAdditionalData(topCard);
					controller.setState(State.PromptCastWithoutPayingManaCost);
					return Response.MoreStep;
				}
			}
		}
		
		if (aa.getStep() == 2) {
			aa.advanceStep();
			Answer a = g.getAnswer();
			if (a == Answer.Yes) {
				Card freeSpell = (Card) aa.getAdditionalData();
				if (g.castWithoutPayingManaCost(freeSpell) == Response.OK) {
					return Response.MoreStep;
				}
				else
					return Response.OK;
			}
			else
				return chandra.dealNonCombatDamageTo(g, controller.getOpponent(), 2);
		}
		return Response.OK;
	}
	/* chandraTOD_2 (add RR to mana pool) */
	public static Response chandraTOD_2(Game g, StackObject aa) {
		aa.getController(g).addMana(ManaType.RED, 2);
		return Response.OK;
	}
	/* chandraTOD_3 (4 damage to target creature) */
	public static Response chandraTOD_3(Game g, StackObject aa) {
		Card chandra = aa.getSource();
		Card target = (Card) aa.getTargetObject(0);
		chandra.dealNonCombatDamageTo(g, target, 4);
		return Response.OK;
	}
	/* chandraTOD_4 (create emblem) */
	public static Response chandraTOD_4(Game g, StackObject aa) {
		g.createEmblem(aa.getSource(), "chandraTOD_ultimate");
		return Response.OK;
	}
	/* chandraTOD_ultimate_trigger */
	public static Response chandraTOD_ultimate_trigger(Game g, StackObject ta) {
		Emblem e = (Emblem) ta.getAdditionalData();
		Damageable target = (Damageable) ta.getTargetObject(0);
		return e.dealDamageTo(g, target, 5, false);
	}
	
	/***********************************************************************/
	
	/********************************* Gideon, Ally of Zendikar **************************************/
	/* gideonAOZ_1 (animate into a 5/5) */
	public static Response gideonAOZ_1(Game g, StackObject aa) {
		Card gideon = aa.getSource();
		gideon.addModifiers(new CardTypeModifier(aa, Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN, CardType.CREATURE),
							new CreatureTypeModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, CreatureType.Human, CreatureType.Soldier, CreatureType.Ally), 
							new PTModifier(aa, "5/5", Operation.SET, Modifier.Duration.UNTIL_END_OF_TURN),
							new EvergreenModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.INDESTRUCTIBLE, Evergreen.UNDAMAGEABLE));
		return Response.OK;
	}
	/* gideonAOZ_2 (create a 2/2 token) */
	public static Response gideonAOZ_2(Game g, StackObject aa) {
		g.createSingleToken(Token.WHITE_KNIGHT_ALLY_22, aa.getSource());
		return Response.OK;
	}
	/* gideonAOZ_3 (create emblem) */
	public static Response gideonAOZ_3(Game g, StackObject aa) {
		Card gideon = aa.getSource();
		g.createEmblem(gideon, "gloriousAnthem");
		return Response.OK;
	}
	/**************************************************************************************************/
	
	/******** Liliana of the Veil **********************************/
	/* lilianaOTV_1 (each player discards a card) */
	public static Response lilianaOTV_1(Game g, StackObject aa) {
		Player playerToDiscard;
		Vector<MtgObject> choices;
		int step = aa.getStep();
		
		if (step == 1) {
			playerToDiscard = g.getActivePlayer();
			playerToDiscard.setState(State.WaitDiscard);
			aa.advanceStep();
			return Response.MoreStep;
		}
		else if (step == 2)
		{
			playerToDiscard = g.getActivePlayer();
			if (g.validateChoices()) {
				choices = g.getChoices();
				if (choices.size() != 0)
					g.discard((Card) choices.get(0));
				playerToDiscard = g.getActivePlayer().getOpponent();
				playerToDiscard.setState(State.WaitDiscard);
				aa.advanceStep();
			}
			return Response.MoreStep;
		}	
		else if (step == 3) {
			playerToDiscard = g.getActivePlayer().getOpponent();
			if (playerToDiscard.getHandSize() == 0)
				return Response.OK;
			
			if (g.validateChoices()) {
				choices = g.getChoices();
				g.discard((Card) choices.get(0));
			}
			else
				return Response.MoreStep;
		}
		return Response.OK;
	}
	/* lilianaOTV_2 (Diabolic Edict) */
	public static Response lilianaOTV_2(Game g, StackObject aa) {
		return diabolicEdict(g, aa);
	}
	/* lilianaOTV_3 not coded yet (nobody really knows what this does anyway) */
	public static Response lilianaOTV_3(Game g, StackObject aa) {
		System.out.println("Ability not coded yet.");
		return Response.OK;
	}
	/***********************************************************************/
	
	
	/* Nissa, Sage Animist */
	public static Response nissaSA_1(Game g, StackObject aa) {
		Card nissa = aa.getSource();
		Player controller = nissa.getController(g);

		// library is empty -> do nothing
		if (controller.getLibrary().size() == 0)
			return Response.OK;
		Card topCard = controller.getLibrary().getTopCard();
		// TODO : reveal card to all players
		System.out.println(controller.getName() + " reveals " + topCard);
		if (topCard.isLandCard())
			g.move_LIB_to_BFD(topCard);
		else
			g.move_LIB_to_HND(topCard);
		return Response.OK;
	}
	public static Response nissaSA_2(Game g, StackObject aa) {
		g.createSingleToken(Token.GREEN_ELEMENTAL_44_ASHAYA, aa.getSource());
		return Response.OK;
	}
	
	/* Sorin, Solemn Visitor */
	public static Response sorinSV_1(Game g, StackObject aa) {
		Player controller = aa.getController(g);
		Vector<Card> creatures = g.getBattlefield().getCreaturesControlledBy(controller);
		for (Card creature : creatures) {
			creature.addModifiers(new PTModifier(aa, "+1/+0", Operation.ADD, Modifier.Duration.UNTIL_YOUR_NEXT_TURN));
			creature.addModifiers(new EvergreenModifier(aa, Modifier.Duration.UNTIL_YOUR_NEXT_TURN, Evergreen.LIFELINK));
		}
		return Response.OK;
	}
	public static Response sorinSV_2(Game g, StackObject aa) {
		g.createSingleToken(Token.BLACK_VAMPIRE_22_FLYING, aa);
		return Response.OK;
	}
	
	/* Elspeth, Knight-Errant */
	public static Response elspethKE_1(Game g, StackObject aa) {
		g.createSingleToken(Token.WHITE_SOLDIER_11, aa);
		return Response.OK;
	}
	public static Response elspethKE_2(Game g, StackObject aa) {
		Card target = (Card)aa.getTargetObject(0);
		if (target == null)
			return Response.ErrorInvalidTarget;
		target.addModifiers(new PTModifier(aa, "+3/+3", Operation.ADD, Modifier.Duration.UNTIL_END_OF_TURN));
		target.addModifiers(new EvergreenModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.FLYING));
		return Response.OK;
	}
	public static Response elspethKE_3(Game g, StackObject aa) {
		Card elspeth = aa.getSource();
		g.createEmblem(elspeth, "elspethKE_3");
		return Response.OK;
	}
	
	public static Response lilianaDN_3(Game g, StackObject aa) {
		Card liliana = aa.getSource();
		g.createEmblem(liliana, "lilianaDN_3");
		return Response.OK;
	}
	
	/* manaVault_damage */
	public static Response manaVault_damage(Game g, StackObject ta) {
		Card manaVault = ta.getSource();
		Player controller = manaVault.getController(g);
		return manaVault.dealNonCombatDamageTo(g, controller, 1);
	}
	
	/* catacombSifter_ETB */
	public static Response catacombSifter_ETB(Game g, StackObject ta) {
		g.createSingleToken(Token.COLORLESS_ELDRAZI_SCION_11, ta);
		return Response.OK;
	}
	
	/* blisterpod */
	public static Response blisterpod(Game g, StackObject ta) {
		g.createSingleToken(Token.COLORLESS_ELDRAZI_SCION_11, ta);
		return Response.OK;
	}

	/* visceraSeer */
	public static Response visceraSeer(Game g, StackObject ta) {
		Response ret;
		ret = scry(g, ta, 1);
		return ret;
	}
	
	/* catacombSifter_scry */
	public static Response catacombSifter_scry(Game g, StackObject ta) {
		Response ret;
		ret = scry(g, ta, 1);
		return ret;
	}
	
	/* preordain */
	public static Response preordain(Game g, StackObject spell) {
		Response ret = Response.OK;
		Player controller = spell.getController(g);
		
		if (spell.getStep() == 1) {
			ret = scry(g, spell, 2);
			if (ret == Response.OK)
				spell.advanceStep();;
		}
		
		if (spell.getStep() == 2) {
			spell.advanceStep();
			ret = g.drawCards(controller, 1);
		}
		return ret;
	}
	
	/* glimmerOfGenius */
	public static Response glimmerOfGenius(Game g, StackObject spell) {
		Response ret = Response.OK;
		Player controller = spell.getController(g);
		
		if (spell.getStep() == 1) {
			ret = scry(g, spell, 2);
			if (ret == Response.OK)
				spell.advanceStep();
		}
		
		if (spell.getStep() == 2) {
			spell.advanceStep();
			ret = g.drawCards(controller, 2);
		}
		
		if (spell.getStep() == 3) {
			spell.advanceStep();
			controller.addCounter(CounterType.ENERGY, 2);
		}
		return ret;
	}
	
	/* magmaJet */
	public static Response magmaJet(Game g, StackObject so) {
		Response ret = Response.OK;
		Card spell = so.getSource();
		MtgObject target = spell.getTargetObject(0);
		
		if (so.getStep() == 1) {
			so.advanceStep();
			ret = spell.dealNonCombatDamageTo(g, (Damageable) target, 2);
		}
		
		if (spell.getStep() == 2) {
			ret = scry(g, spell, 2);
			if (ret == Response.OK)
				spell.advanceStep();;
		}
		
		return ret;
	}
	
	
	
	/* scry X */
	private static Response scry(Game g, StackObject so, int nbCards) {
		Response ret;
		Player controller = so.getController(g);
		int nbCardsInLibrary = controller.getLibrary().size();
		
		// immediately return OK if the library is empty
		if (nbCardsInLibrary == 0)
			return Response.OK;
		
		// the number of cards looked at is the MIN between X and the library size
		int nbCardsPeeked = Math.min(nbCardsInLibrary, nbCards);
		
		// Step 1
		if (so.getScryStep() == 1) {
			so.setLibManip(controller.getLibrary().getXTopCards(nbCardsPeeked));
			controller.setState(State.WaitChoiceScryPutBottom);
			so.advanceScryStep();
			ret = Response.MoreStep;
		}
		
		// Step 2 : prompt player to put any number of cards on the bottom
		else if (so.getScryStep() == 2)
		{
			if (g.validateChoices()) {
				if (g.getChoices().isEmpty()) { // player clicked DONE button = he wants to put all cards back on top
					if (so.getLibManip().size() > 0) {
						so.advanceScryStep();
						controller.setState(State.WaitChoicePutTopLib);
						ret = Response.MoreStep;
					}
					else
						return Response.OK;
				}
				else {
					Card c = (Card) g.getChoices().get(0);
					g.libToLibBottom(c);
					so.getLibManip().remove(c);
					if (so.getLibManip().size() == 0)
						return Response.OK;
				}
			}
			ret = Response.MoreStep;
		}
		// Step 3 : prompt player to put any number of cards back on the top
		else {
			if (g.validateChoices()) {
				Card c = (Card) g.getChoices().get(0);
				g.libToLibTop(c);
				so.getLibManip().remove(c);
			}
			if (so.getLibManip().size() > 0)
				ret = Response.MoreStep;
			else
				return Response.OK;
		}
		
		return ret;
	}
	
	/* collectedCompany */
	public static Response collectedCompany(Game g, StackObject spell) {
		int step = spell.getStep();
		Response ret;
		Player controller = spell.getController(g);
		int nbCardsInLibrary = controller.getLibrary().size();
		
		// immediately return OK if the library is empty
		if (nbCardsInLibrary == 0)
			return Response.OK;
		
		// the number of cards looked at is the MIN between 6 and the library size
		int nbCardsPeeked = Math.min(nbCardsInLibrary, 6);
		
		if (step == 1) // prompt player to pick a creature card with  CMC <= 3 to put onto the battlefield or click DONE
		{
			controller.setState(State.WaitChoiceCollectedCompany);
			spell.setLibManip(controller.getLibrary().getXTopCards(nbCardsPeeked));
			spell.advanceStep();
			ret = Response.MoreStep;
		}
		else if (step == 2) // put 1st the chosen card onto the battlefield
		{
			if (g.validateChoices()) {
				if (g.getChoices().isEmpty()) { // player clicked DONE button
					spell.goToStep(6);
					controller.setState(State.WaitChoicePutBottomLib);
				}
				else {
					Card c = (Card) g.getChoices().get(0);
					g.move_LIB_to_BFD(c);
					spell.getLibManip().remove(c);
					if (spell.getLibManip().size() > 0)
						spell.advanceStep();
					else
						return Response.OK;
				}
			}
			ret = Response.MoreStep;				
		}
		else if (step == 3) // put 2nd the chosen card onto the battlefield
		{
			if (g.validateChoices()) {
				if (!g.getChoices().isEmpty()) {
					Card c = (Card) g.getChoices().get(0);
					g.move_LIB_to_BFD(c);
					spell.getLibManip().remove(c);
				}
			}
			else
				return Response.MoreStep;

			if (spell.getLibManip().size() > 0) {
				spell.goToStep(6);
				controller.setState(State.WaitChoicePutBottomLib);
				ret = Response.MoreStep;
			}
			else
				return Response.OK;
		}
		else if (step == 6) { // start putting cards on bottom lib
			if (g.validateChoices()) {
				Card c = (Card) g.getChoices().get(0);
				g.libToLibBottom(c);
				spell.getLibManip().remove(c);
			}
			if (spell.getLibManip().size() > 0)
				ret = Response.MoreStep;
			else
				return Response.OK;
		}
		else
			ret = Response.OK;
		return ret;
	}
	
	/* coralhelmGuide */
	public static Response coralhelmGuide(Game g, StackObject aa) {
		Card target = (Card) aa.getTargetObject(0);
		target.addModifiers(new EvergreenModifier(aa, Modifier.Duration.UNTIL_END_OF_TURN, Evergreen.UNBLOCKABLE));
		return Response.OK;
	}
	
	/* mirrisGuile */
	public static Response mirrisGuile(Game g, StackObject ta) {
		Response ret;
		Player controller = ta.getController(g);
		int nbCardsInLibrary = controller.getLibrary().size();
		
		// immediately return OK if the library is empty
		if (nbCardsInLibrary == 0)
			return Response.OK;
		
		// the number of cards looked at is the MIN between 5 and the library size
		int nbCardsPeeked = Math.min(nbCardsInLibrary, 3);
		
		if (ta.getStep() == 1) {
			ta.setLibManip(controller.getLibrary().getXTopCards(nbCardsPeeked));
			controller.setState(State.WaitChoicePutTopLib);
			ta.advanceStep();
			ret = Response.MoreStep;
		}
		else {
			if (g.validateChoices()) {
				Card c = (Card) g.getChoices().get(0);
				g.libToLibTop(c);
				ta.getLibManip().remove(c);
			}
			if (ta.getLibManip().size() > 0)
				ret = Response.MoreStep;
			else
				return Response.OK;
		}
		return ret;
	}
	
	/* spireOwl */
	public static Response spireOwl(Game g, StackObject ta) {
		Response ret;
		Player controller = ta.getController(g);
		int nbCardsInLibrary = controller.getLibrary().size();
		
		// immediately return OK if the library is empty
		if (nbCardsInLibrary == 0)
			return Response.OK;
		
		// the number of cards looked at is the MIN between 4 and the library size
		int nbCardsPeeked = Math.min(nbCardsInLibrary, 4);
		
		if (ta.getStep() == 1) {
			ta.setLibManip(controller.getLibrary().getXTopCards(nbCardsPeeked));
			controller.setState(State.WaitChoicePutTopLib);
			ta.advanceStep();
			ret = Response.MoreStep;
		}
		else {
			if (g.validateChoices()) {
				Card c = (Card) g.getChoices().get(0);
				g.libToLibTop(c);
				ta.getLibManip().remove(c);
			}
			if (ta.getLibManip().size() > 0)
				ret = Response.MoreStep;
			else
				return Response.OK;
		}
		return ret;
	}
	
	/* index */
	public static Response index(Game g, StackObject spell) {
		Response ret;
		Player controller = spell.getController(g);
		int nbCardsInLibrary = controller.getLibrary().size();
		
		// immediately return OK if the library is empty
		if (nbCardsInLibrary == 0)
			return Response.OK;
		
		// the number of cards looked at is the MIN between 5 and the library size
		int nbCardsPeeked = Math.min(nbCardsInLibrary, 5);
		
		if (spell.getStep() == 1) {
			spell.setLibManip(controller.getLibrary().getXTopCards(nbCardsPeeked));
			controller.setState(State.WaitChoicePutTopLib);
			spell.advanceStep();
			ret = Response.MoreStep;
		}
		else {
			if (g.validateChoices()) {
				Card c = (Card) g.getChoices().get(0);
				g.libToLibTop(c);
				spell.getLibManip().remove(c);
			}
			if (spell.getLibManip().size() > 0)
				ret = Response.MoreStep;
			else
				return Response.OK;
		}
		return ret;
	}
	
	/* coercion */
	public static Response coercion(Game g, StackObject spell) {
		Player controller = spell.getController(g);
		Player opponent = (Player) spell.getTargetObject(0);
		
		// if the opponent has no cards in hand, immediately return
		if (opponent.getHandSize() == 0)
			return Response.OK;
		
		// look at hand and wait for player to choose a card
		if (spell.getStep() == 1) {
			spell.advanceStep();
			controller.setState(State.WaitChoiceCoercion);
			return Response.MoreStep;
		}
		
		if (spell.getStep() == 2) {
			if (g.validateChoices()) {
				spell.advanceStep();
				g.discard((Card) g.getChoices().get(0));
			}
			else
				return Response.MoreStep;
		}
		
		return Response.OK;
	}
	
	/* thoughtseize */
	public static Response thoughtseize(Game g, StackObject spell) {
		Player controller = spell.getController(g);
		Player target = (Player) spell.getTargetObject(0);
		
		// if the target has no cards in hand, immediately return
		if (target.getHandSize() == 0)
			return Response.OK;
		
		// look at hand and wait for player to choose a card
		if (spell.getStep() == 1) {
			spell.advanceStep();
			controller.setState(State.WaitChoiceThoughtseize);
			return Response.MoreStep;
		}
		
		if (spell.getStep() == 2) {
			if (g.validateChoices()) {
				spell.advanceStep();
				if (g.getChoices().size() == 1)
					g.discard((Card) g.getChoices().get(0));
				controller.loseLife(2);
			}
			else
				return Response.MoreStep;
		}
		
		return Response.OK;
	}
	/* duress */
	public static Response duress(Game g, StackObject spell) {
		Player controller = spell.getController(g);
		Player opponent = (Player) spell.getTargetObject(0);
		
		// if the opponent has no cards in hand, immediately return
		if (opponent.getHandSize() == 0)
			return Response.OK;
		
		// look at hand and wait for player to choose a card
		if (spell.getStep() == 1) {
			spell.advanceStep();
			controller.setState(State.WaitChoiceDuress);
			return Response.MoreStep;
		}
		
		if (spell.getStep() == 2) {
			if (g.validateChoices()) {
				spell.advanceStep();
				if (g.getChoices().size() == 1)
					g.discard((Card) g.getChoices().get(0));
			}
			else
				return Response.MoreStep;
		}
		
		return Response.OK;
	}
	
	/* peek */
	public static Response peek(Game g, StackObject spell) {
		Player controller = spell.getController(g);
		Player target = (Player) spell.getTargetObject(0);
		
		// look at hand
		if (spell.getStep() == 1) {
			spell.advanceStep();
			if (target != controller) 			{
				controller.setState(State.LookPlayersHand);
				return Response.MoreStep;
			}	
		}
		
		// draw a card
		if (spell.getStep() == 2) {
			spell.advanceStep();
			return g.drawCards(controller, 1);
		}
		return Response.OK;
	}
	
	/* duskwatchRecruiter */
	public static Response duskwatchRecruiter(Game g, StackObject spell) {
		int step = spell.getStep();
		Response ret;
		Player controller = spell.getController(g);
		int nbCardsInLibrary = controller.getLibrary().size();
		
		// immediately return OK if the library is empty
		if (nbCardsInLibrary == 0)
			return Response.OK;
		
		// the number of cards looked at is the MIN between 3 and the library size
		int nbCardsPeeked = Math.min(nbCardsInLibrary, 3);

		if (step == 1) // prompt player to pick a card to put in their hand
		{
			controller.setState(State.WaitChoicePutCreatureCardInHand);
			spell.setLibManip(controller.getLibrary().getXTopCards(nbCardsPeeked));
			spell.advanceStep();
			ret = Response.MoreStep;
		}
		else if (step == 2) // put the chosen card in hand
		{
			if (g.validateChoices()) {
				if (g.getChoices().isEmpty()) { // player clicked DONE button
					spell.goToStep(3);
					controller.setState(State.WaitChoicePutBottomLib);
				}
				else
				{
					Card c = (Card) g.getChoices().get(0);
					g.move_LIB_to_HND(c);
					spell.getLibManip().remove(c);
					spell.advanceStep();	
				}
			}
			else
			{
				return Response.MoreStep;
			}
			if (spell.getLibManip().size() > 0) {
				controller.setState(State.WaitChoicePutBottomLib);
				return Response.MoreStep;
			}
			else
				return Response.OK;
		}
		else
		{
			if (g.validateChoices()) {
				Card c = (Card) g.getChoices().get(0);
				g.libToLibBottom(c);
				spell.getLibManip().remove(c);
			}
			if (spell.getLibManip().size() > 0)
				ret = Response.MoreStep;
			else
				ret = Response.OK;
		}
		return ret;
	}
	
	private static Response impulseLike_functionHelper(Game g, StackObject spell, int nbCards, State state) {
		int step = spell.getStep();
		Response ret;
		Player controller = spell.getController(g);
		int nbCardsInLibrary = controller.getLibrary().size();
		
		// immediately return OK if the library is empty
		if (nbCardsInLibrary == 0)
			return Response.OK;
		
		// the number of cards looked at is the MIN between nbCards and the library size
		int nbCardsPeeked = Math.min(nbCardsInLibrary, nbCards);

		if (step == 1) // prompt player to pick a card to put in their hand
		{
			controller.setState(state);
			spell.setLibManip(controller.getLibrary().getXTopCards(nbCardsPeeked));
			spell.advanceStep();
			ret = Response.MoreStep;
		}
		else if (step == 2) // put the chosen card in hand
		{
			if (g.validateChoices()) {
				if (g.getChoices().isEmpty()) { // player clicked DONE button
					spell.goToStep(3);
					controller.setState(State.WaitChoicePutBottomLib);
					return Response.MoreStep;
				}
				else
				{
					Card c = (Card) g.getChoices().get(0);
					g.move_LIB_to_HND(c);
					spell.getLibManip().remove(c);
					spell.advanceStep();
				}
			}
			else
				return Response.MoreStep;
			if (spell.getLibManip().size() > 0) {
				controller.setState(State.WaitChoicePutBottomLib);
				ret = Response.MoreStep;
			}
			else
				ret = Response.OK;
		}
		else
		{
			if (g.validateChoices()) {
				Card c = (Card) g.getChoices().get(0);
				g.libToLibBottom(c);
				spell.getLibManip().remove(c);
			}
			if (spell.getLibManip().size() > 0)
				ret = Response.MoreStep;
			else
				ret = Response.OK;
		}
		return ret;
	}
	
	/* contamination_sac */
	public static Response contamination_sac(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		Card contamination = ta.getSource();
		
		if (ta.getStep() == 1) {
			ta.advanceStep();
			Vector<Card> creatures = g.getBattlefield().getCreaturesControlledBy(controller);
			// Prompt player to choose a creature to sac
			if (creatures.size() > 0) {
				controller.setState(State.PromptPayUpkeepCost);
				return Response.MoreStep;	
			}
			else { // no creatures can be sacrificed, sac Contamination
				g.sacrifice(controller, contamination);
				return Response.OK;
			}
		}

		if (ta.getStep() == 2) {
			if (g.getAnswer() == Answer.Yes) { // player chose to sac a creature
				controller.setState(State.WaitSacrificeCreature);
				ta.advanceStep();
				return Response.MoreStep;
			}
			else // player chose not to sac a creature -> sac contamination
			{
				g.sacrifice(controller, contamination);
				return Response.OK;
			}
		}
		
		if (ta.getStep() == 3) {
			if (g.validateChoices())
				g.sacrifice(controller, (Card) g.getChoices().get(0));
			else
				return Response.MoreStep;
		}
		return Response.OK;
	}
	
	/* spinedFluke_sac */
	public static Response spinedFluke_sac(Game g, StackObject ta) {
		Player controller = ta.getController(g);
		int step = ta.getStep();
		
		if (step == 1) {
			// Do nothing if controller doesn't control any creature
			Vector<Card> creatures = g.getBattlefield().getCreaturesControlledBy(controller);
			if (creatures.size() == 0)
				return Response.OK;
			
			// Prompt player to choose a creature to sac
			controller.setState(State.WaitSacrificeCreature);
			ta.advanceStep();
			return Response.MoreStep;
		}
		else if (step == 2) {
			if (g.validateChoices())
				g.sacrifice(controller, (Card) g.getChoices().get(0));
			else
				return Response.MoreStep;
		}
		return Response.OK;
	}
	
	/* oathOfLiliana_sac */
	public static Response oathOfLiliana_sac(Game g, StackObject spell) {
		Player opponent = spell.getController(g).getOpponent();
		int step = spell.getStep();
		
		if (step == 1) {
			// Do nothing if opponent doesn't control any creature
			Vector<Card> creatures = g.getBattlefield().getCreaturesControlledBy(opponent);
			if (creatures.size() == 0)
				return Response.OK;
			
			// Prompt opponent to choose a creature to sac
			opponent.setState(Game.State.WaitSacrificeCreature);
			spell.advanceStep();
			return Response.MoreStep;
		}
		else if (step == 2) {
			if (g.validateChoices())
				g.sacrifice(opponent, (Card) g.getChoices().get(0));
			else
				return Response.MoreStep;
		}
		return Response.OK;
	}

	/* oathOfLiliana_putToken */
	public static Response oathOfLiliana_putToken(Game g, StackObject c) {
		g.createSingleToken(Token.BLACK_ZOMBIE_22, c);
		return Response.OK;
	}
	
	/* oathOfNissa */
	public static Response oathOfNissa(Game g, StackObject spell) {
		return impulseLike_functionHelper(g, spell, 3, State.WaitChoiceOathOfNissa);
	}
	
	/* anticipate */
	public static Response anticipate(Game g, StackObject spell) {
		return impulseLike_functionHelper(g, spell, 3, State.WaitChoicePutInHand);
	}
	
	/* impulse */
	public static Response impulse(Game g, StackObject spell) {
		return impulseLike_functionHelper(g, spell, 4, State.WaitChoicePutInHand);
	}
	
	/* rallyTheAncestors */
	public static Response rallyTheAncestors(Game g, StackObject spell) {
		int xValue = spell.getXValue();
		Player controller = spell.getController(g);
		Card c;
		int i = 0;
		Vector<Card> allCreatures = controller.getGraveyard().getCreatureCards();
		Vector<Card> reanimatedCreatures = new Vector<Card>();
		
		while (i < allCreatures.size()) {
			c = (Card) allCreatures.get(i);
			if (c.getConvertedManaCost(g) <= xValue) {
				g.move_GYD_to_BFD(c);
				reanimatedCreatures.add(c);
				c.addReference(reanimatedCreatures);
				i--;
			}
			i++;
			allCreatures = controller.getGraveyard().getCreatureCards();
		}
		
		ContinuousEffect delayedTrigger = ContinuousEffectFactory.create("rallyTheAncestors", (Card) spell);
		delayedTrigger.setAdditionalData(reanimatedCreatures);
		g.addContinuousEffect(delayedTrigger);
		
		// exile the spell
		g.move_STK_to_EXL(spell.getSource());
		return Response.OK;
	}
	/* rallyTheAncestors_exile */
	@SuppressWarnings("unchecked")
	public static Response rallyTheAncestors_exile(Game g, StackObject ta) {
		Object additionalData = ((TriggeredAbility) ta).getAdditionalData();
		Vector<Card> reanimatedCreatures = (Vector<Card>) additionalData;
		while (reanimatedCreatures.size() > 0) {
			Card ancestor = reanimatedCreatures.get(0);
			g.move_BFD_to_EXL(ancestor);
			ancestor.removeReference(reanimatedCreatures);
		}
		return Response.OK;
	}
	
	/* planarBirth */
	public static Response planarBirth(Game g, StackObject spell) {
		Vector<Card> basicLandCards = new Vector<Card>();
		// Mark cards that need to be returned to the battlefield
		for (Player p : g.getPlayers())
			for (Card landCard : p.getGraveyard().getLandCards())
				if (landCard.hasSupertype(Supertype.BASIC))
					basicLandCards.add(landCard);
		// Return the marked cards and tap them 
		for (Card basic : basicLandCards) {
			g.move_GYD_to_BFD(basic);
			basic.tap(g);
		}
		return Response.OK;
	}
	
	/* crystalChimes */
	public static Response crystalChimes(Game g, StackObject spell) {
		for (Card enchantment : spell.getController(g).getGraveyard().getEnchantments())
			g.move_GYD_to_HND(enchantment);
		return Response.OK;
	}
	
	/* replenish */
	public static Response replenish(Game g, StackObject spell) {
		Player controller = spell.getController(g);
		
		Vector<Card> enchantments = controller.getGraveyard().getEnchantments();
		for (Card enchantment : enchantments) {
			if (!enchantment.hasSubtypeGlobal(g, Subtype.AURA))  // TODO : handle bringing back auras
				g.move_GYD_to_BFD(enchantment);
		}
		return Response.OK;
	}
	
	/* living Death */
	public static Response livingDeath(Game g, StackObject c) {
		Card card;
		int i;
		Battlefield bf = (Battlefield) g.getBattlefield();
		Zone special = g.getSpecialZone();
		
		//1. exile in a special exile zone creature cards from all graveyards
		for (Player p : g.getPlayers()) {
			Graveyard yard = p.getGraveyard();
		    
			i = 0;
			while (i < yard.size()) {
				card = (Card) yard.getObjectAt(i);
				if (card.isCreatureCard()) {
					g.switchZone(yard, card, special);
					i--;
				}
				i++;
			}			
		}
		
		//2. sac all creatures
		i = 0;
		while (i < bf.size()) {
			card = (Card) bf.getObjectAt(i);
			if (card.isCreature(g)) {
				g.sacrifice(card.getController(g), card);
				i--;
			}
			i++;
		}
		
		//3. put all exiled cards this way on the battlefield
		while (special.size() > 0) {
			g.switchZone(special, (Card) special.getObjectAt(0), bf);
		}
		return Response.OK;
	}
	
	/* wildGrowth */
	public static Response wildGrowth(Game g, StackObject ta) {
		Card aura = ta.getSource();
		Card enchantedLand = aura.getHost();
		Player controller = enchantedLand.getController(g);
		controller.addMana(ManaType.GREEN, 1);
		return Response.OK;
	}
	
	/* utopiaSprawl_additionalMana */
	public static Response utopiaSprawl_additionalMana(Game g, StackObject ta) {
		Card aura = ta.getSource();
		Color chosenColor = aura.getChosenColor();
		Card enchantedLand = aura.getHost();
		Player controller = enchantedLand.getController(g);
		controller.addMana(Mana.colorToMana(chosenColor), 1);
		return Response.OK;
	}
	
	/* overgrowth */
	public static Response overgrowth(Game g, StackObject ta) {
		Card aura = ta.getSource();
		Card enchantedLand = aura.getHost();
		Player controller = enchantedLand.getController(g);
		controller.addMana(ManaType.GREEN, 2);
		return Response.OK;
	}

	/* fertileGround */
	public static Response fertileGround(Game g, StackObject ta) {
		g.setCurrentManaAbility(ta);
		Card aura = ta.getSource();
		Card enchantedLand = aura.getHost();
		Player controller = enchantedLand.getController(g);
		
		if (ta.getStep() == 1) {
			ta.advanceStep();
			controller.setState(State.WaitChooseTriggeredManaAbilityColor);
			return Response.MoreStep;
		}
		
		if (ta.getStep() == 2) {
			int choice = (int) ta.getAdditionalData();
			
			if ((choice < 1) || (choice > 5))
				return Response.MoreStep;
			else
				controller.addMana(Mana.intToManaType(choice), 1);
		}
		return Response.OK;
	}
};
