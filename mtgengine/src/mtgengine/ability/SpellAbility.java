package mtgengine.ability;

import mtgengine.effect.Effect;

public class SpellAbility {
	private Effect _effect;
	private String _name;
	
	public SpellAbility(String name) {
		_name = name;
	}

	public Effect getEffect() {
		return _effect;
	}

	public String getAbilityName() {
		return _name;
	}

	public void setEffect(String methodName, String text) {
		_effect = new Effect(methodName, text);
	}

	public String getDescription() {
		return _effect.getRulesText();
	}
}
