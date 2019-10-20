package mtgengine.damage;

import mtgengine.Game;
import mtgengine.Game.Response;
import mtgengine.player.Player;

public interface DamageSource {
	Response dealDamageTo(Game g, Damageable recipient, int amount, boolean bCombat);
	Player getController(Game g);
}
