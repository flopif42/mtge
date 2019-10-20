package mtgengine.effect;

import mtgengine.Game;
import mtgengine.player.Player;

public interface ContinuousEffectSource {
	public Player getController(Game g);
	public String getSystemName();
}
