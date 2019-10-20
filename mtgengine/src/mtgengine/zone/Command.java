package mtgengine.zone;

import mtgengine.Game;
import mtgengine.player.Player;

public class Command extends Zone {

	public Command(Game g, Player owner) {
		super(g, Name.Command, owner);
	}

}
