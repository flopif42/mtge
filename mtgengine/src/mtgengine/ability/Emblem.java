package mtgengine.ability;

import java.util.Vector;

import mtgengine.Game;
import mtgengine.Game.Response;
import mtgengine.MtgObject;
import mtgengine.StackObject;
import mtgengine.card.Card;
import mtgengine.damage.DamageSource;
import mtgengine.damage.Damageable;
import mtgengine.effect.ContinuousEffect;
import mtgengine.effect.ContinuousEffectSource;
import mtgengine.player.Player;

public class Emblem extends MtgObject implements ContinuousEffectSource, DamageSource{
	private Card _source; // The card that created the emblem
	private Player _controller;
	private ContinuousEffect _continuousEffect;
	
	public Emblem(Card source, Player controller) {
		super(null);
		_source = source;
		_controller = controller;
	}
	
	public Emblem(Game g, Card source) {
		super(null);
		_source = source;
		_controller = source.getController(g);
	}

	@Override
	public  String getSystemName() {
		return getSource().getSystemName() + "#" + _id + "~" + _continuousEffect.getDescription();
	}

	public Player getController(Game g) {
		return _controller;
	}
	
	public void setContinuousEffect (ContinuousEffect ce) {
		_continuousEffect = ce;
	}

	public ContinuousEffect getContinuousEffet() {
		return _continuousEffect;
	}
	
	public Card getSource() {
		return _source;
	}

	@Override
	public boolean isTargetableBy(Game g, StackObject so) {
		// Emblems cannot be targeted
		return false;
	}

	@Override
	public Response dealDamageTo(Game g, Damageable recipient, int amount, boolean bCombat) {
		// Do nothing if damage is less than 1
		if (amount < 1)
			return Response.OK;
		
		// Do nothing if recipient has protection from the source
		if (g.computeIsProtectedFrom(this, (MtgObject) recipient) == true)
			return Response.OK;
		
		// Continuous effects that prevent damage from being dealt (i.e. Glacial Chasm)
		Vector<ContinuousEffect> effects = g.getContinuousEffects();
		for (ContinuousEffect effect : effects) {
			if (effect.isDamagePrevented(g, this, recipient, false))
				return Response.OK;
		}
		
		// If the card is Undamageable (Cho-Manno style), do nothing.
		if (recipient.isUndamageable(g))
			return Response.OK;
		
		int dmgDealt = recipient.isDealtDamage(this, amount, g);
		if (dmgDealt == 0)
			return Response.OK;

		return Response.OK; 
	}

}
