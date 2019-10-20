package mtgengine.zone;

import mtgengine.Game;
import mtgengine.player.Player;

public class Exile extends Zone {

	public Exile(Game g, Player owner) {
		super(g, Name.Exile, owner);
	}

}
