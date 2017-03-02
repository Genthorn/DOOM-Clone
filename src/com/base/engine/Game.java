package com.base.engine;

import com.base.engine.game.Level;
import com.base.engine.game.Player;
import com.base.engine.renderEngine.Camera;
import com.base.engine.renderEngine.Window;
import com.base.engine.utils.Transform;
import com.base.engine.utils.Vector3f;

public class Game {
	
	private Level level;
	private Player player;
	
	public Game() {
		level = new Level("levels/level1.png", "WolfCollection.png");
		player = new Player(new Vector3f(0, 0.4375f, 0));
	}
	
	public void input() {
		level.input();
		player.input();
	}
	
	public void update() {
		level.update();
		player.update();
	}
	
	public void render() {
		level.render();
		player.render();
	}
}
