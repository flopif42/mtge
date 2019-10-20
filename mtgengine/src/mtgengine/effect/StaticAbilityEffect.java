package mtgengine.effect;

import mtgengine.CounterType;
import mtgengine.Game;
import mtgengine.Game.Response;
import mtgengine.Game.State;
import mtgengine.ability.StaticAbility;
import mtgengine.card.Card;
import mtgengine.player.Player;
import mtgengine.type.Subtype;

public class StaticAbilityEffect {
	
	/**
	 * 
	 * @param g
	 * @param ability
	 * @return
	 */
	public static Response shockland(Game g, StaticAbility ability) {
		Card source = ability.getSource();

		// Check if controller has enough life to pay
		if (source.getController(g).getLife() >= 2) {
			source.getController(g).setState(State.PromptPayForShockland);
			g._shockland = source;
			return Response.MoreStep;
		}
		else // If player life < 2, land enters tapped
			source.tap(g);
		return Response.OK;
	}
	
	
	/**
	 * 
	 * @param g
	 * @param ability
	 * @return
	 */
	public static Response revealland(Game g, StaticAbility ability) {
		Card source = ability.getSource();
		Subtype subtype1 = ability.getAssociatedBasicLandType(0);
		Subtype subtype2 = ability.getAssociatedBasicLandType(1);
		State state;
		
		if ((subtype1 == Subtype.PLAINS) && (subtype2 == Subtype.ISLAND))
			state = State.PromptRevealPlainsOrIsland;
		else if ((subtype1 == Subtype.ISLAND) && (subtype2 == Subtype.SWAMP))
			state = State.PromptRevealIslandOrSwamp;
		else if ((subtype1 == Subtype.SWAMP) && (subtype2 == Subtype.MOUNTAIN))
			state = State.PromptRevealSwampOrMountain;
		else if ((subtype1 == Subtype.MOUNTAIN) && (subtype2 == Subtype.FOREST))
			state = State.PromptRevealMountainOrForest;
		else // if ((subtype1 == Subtype.FOREST) && (subtype2 == Subtype.PLAINS))
			state = State.PromptRevealForestOrPlains;
		source.getController(g).setState(state);
		g._revealLand = source;
		return Response.MoreStep;
	}
	
	
	/* utopiaSprawl_chooseColor */
	public static Response utopiaSprawl_chooseColor(Game g, StaticAbility ability) {
		Player controller = ability.getSource().getController(g);
		
		if (ability.getStep() == 1) {
			ability.advanceStep();
			controller.setState(State.PromptChooseColorStaticETB);
			return Response.MoreStep;
		}
		return Response.OK;
	}
	
	/* ionaShieldEmeria */
	public static Response ionaShieldEmeria(Game g, StaticAbility ability) {
		Player controller = ability.getSource().getController(g);
		
		if (ability.getStep() == 1) {
			ability.advanceStep();
			controller.setState(State.PromptChooseColorStaticETB);
			return Response.MoreStep;
		}
		else if (ability.getStep() == 2) {
			ability.advanceStep();
			g.addContinuousEffect(ContinuousEffectFactory.create("ionaShieldEmeria", ability.getSource()));;
		}
		return Response.OK;
	}
	
	/* voiceOfAll */
	public static Response voiceOfAll(Game g, StaticAbility ability) {
		Card angel = ability.getSource();
		Player controller = angel.getController(g);
		
		if (ability.getStep() == 1) {
			ability.advanceStep();
			controller.setState(State.PromptChooseColorStaticETB);
			return Response.MoreStep;
		}
		else if (ability.getStep() == 2) {
			ability.advanceStep();
			angel.setProtectionFrom(angel.getChosenColor());
		}
		return Response.OK;
	}
	
	/* morph / megamorph*/
	public static Response morph(Game g, Card source, boolean bMegamorph) {
		if (source.getFaceUp() == null)
			return Response.ErrorInvalidCommand;
		if (bMegamorph)
			source.addCounter(g, CounterType.PLUS_ONE, 1);
		return source.turnFace(g, source.getFaceUp());
	}

	/**
	 * 
	 * @param g
	 * @param ability
	 * @return
	 */
	public static Response phyrexianProcessor_payLife(Game g, StaticAbility ability) {
		int step = ability.getStep();
		Card processor = ability.getSource();
		Player controller = processor.getController(g);
		
		if (step == 1) {
			ability.advanceStep();;
			controller.setState(State.PromptPayLifeStaticETB);
			return Response.MoreStep;
		}
		else {
			if (processor.getXValue() > controller.getLife())
				return Response.MoreStep;
			else {
				controller.loseLife(processor.getXValue());
				return Response.OK;
			}
		}
	}
}
