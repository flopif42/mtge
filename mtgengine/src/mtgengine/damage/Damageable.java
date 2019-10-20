package mtgengine.damage;

import mtgengine.Game;

public interface Damageable {
	public int isDealtDamage(DamageSource source, int damageDealt, Game g);
	public boolean isUndamageable(Game g);
	public void addDamagePrevention(int nb);
	public int getID();
}
