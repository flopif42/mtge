package mtgengine.modifier;

import java.util.Vector;

import mtgengine.card.Card;

/**
 *	This modifier is applied to a Player and specifies when he/she can't play certain spells
 *	Examples : Meddling Mage, Reflector Mage, Iona, Shield of Emeria, Silence ...
 * 
 * @author nguyenf
 *
 */
public class SpellCastingModifier extends Modifier {
	
	Vector<String> _forbiddenSpellNames = null;
	
	public SpellCastingModifier(Card source, Modifier.Duration duration) {
		_source = source;
		_duration = duration;
	}
	
	public void addForbiddenSpellName(String spellName) {
		if (_forbiddenSpellNames == null)
			_forbiddenSpellNames = new Vector<String>();
		_forbiddenSpellNames.add(spellName);
	}
	
	public boolean isForbidden(Card spell) {
		if (_forbiddenSpellNames.contains(spell.getName()))
			return true;
		return false;
	}
}
