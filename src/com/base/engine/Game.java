package com.base.engine;

import com.base.engine.game.Level;
import com.base.engine.game.Player;
import com.base.engine.game.entities.Enemy;
import com.base.engine.renderEngine.Camera;
import com.base.engine.renderEngine.Window;
import com.base.engine.utils.Transform;
import com.base.engine.utils.Vector3f;

public class Game {
	
	private static Level level;
	private static Player player;
	
	private Enemy enemy;
	
	public Game() {
		level = new Level("levels/level1.png", "tileset.png");
		player = new Player(new Vector3f(20, 0.4375f, 18));
		
		Transform transform = new Transform();
		transform.setTranslation(new Vector3f(8,0,8));
		
		enemy = new Enemy(transform);
	}
	
	public void input() {
		level.input();
		player.input();
	}
	
	public void update() {
		level.update();
		player.update();
		
		enemy.update();
	}
	
	public void render() {
		level.render();
		player.render();
		
		enemy.render();
	}
	
	public static Level getLevel() {
		return level;
	}
}
