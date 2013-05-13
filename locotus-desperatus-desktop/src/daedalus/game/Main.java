package daedalus.game;

import daedalus.graphics.Cutscene;
import daedalus.graphics.HUD;
import daedalus.main.GameComponent;

public class Main {
	public static LocotusDesperatus game;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GameComponent.create("Locotus Desperatus", 1280, 704, true, true);
		game = new LocotusDesperatus();
		GameComponent.getGame().pushContext(game);
		GameComponent.getGame().pushContext(new HUD(game.getHero()));
		Cutscene player = new Cutscene("logo_anim");
		player.preroll();
		GameComponent.getGame().pushContext(player);
		GameComponent.getGame().setPaused(false);
	}

}
