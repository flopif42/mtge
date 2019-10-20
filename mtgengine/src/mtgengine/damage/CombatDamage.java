package mtgengine.damage;

import mtgengine.Game;
import mtgengine.card.Card;

public class CombatDamage {
	Card _source;
	Damageable _recipient;
	int _amount;
	
	public CombatDamage(Card source, Damageable recipient, int amount) {
		_source = source;
		_recipient = recipient;
		_amount = amount;
	}
	
	public void compute(Game g) {
		_source.dealCombatDamageTo(g, _recipient, _amount);
	}
};
