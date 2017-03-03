package com.base.engine.game.entities;

import java.util.Random;

import com.base.engine.Game;
import com.base.engine.game.Player;
import com.base.engine.renderEngine.Material;
import com.base.engine.renderEngine.Mesh;
import com.base.engine.renderEngine.Texture;
import com.base.engine.renderEngine.shaders.Shader;
import com.base.engine.utils.Time;
import com.base.engine.utils.Transform;
import com.base.engine.utils.Vector2f;
import com.base.engine.utils.Vector3f;
import com.base.engine.utils.Vertex;

public class Enemy {
	
	public static final float SCALE = 0.8f;
	public static final float SIZEY = SCALE;
	public static final float SIZEX = (float)((double) SIZEY / (1.75 * 2.0));
	public static final float START = 0;
	
	public static final float OFFSET_X = 0.05f;
	public static final float OFFSET_Y = 0.01f;
	
	public static final float TEX_MIN_X = -OFFSET_X;
	public static final float TEX_MAX_X = -1 - OFFSET_X;
	public static final float TEX_MIN_Y = -OFFSET_Y;
	public static final float TEX_MAX_Y = 1 - OFFSET_Y;
	
	public static final int STATE_IDLE = 0;
	public static final int STATE_CHASING = 1;
	public static final int STATE_ATTACKING = 2;
	public static final int STATE_DYING = 3;
	public static final int STATE_DEAD = 4;
	
	public static final float OFFSET_FROM_GROUND = -0.00f;
	
	private static final float SHOOT_ERROR = 10.0f;
	private static final float SHOOT_DISTANCE = 1000;
	private static final float MOVE_SPEED = 1.5f;
	private static final float WIDTH = 0.2f;
	private static final float HEIGHT = 0.2f;
	
	private static Mesh mesh;
	private Material material;
	private Transform transform;
	private Random random;
	private int state;
	
	public Enemy(Transform transform) {
		this.state = STATE_ATTACKING;
		this.random = new Random();
		
		this.transform = transform;
		this.transform.setTranslation(new Vector3f(23, 0.4375f, 19));
		this.material = new Material(new Texture("ss_fstanding_1.png"));
		
		if(mesh == null) {
			Vertex[] vertices = new Vertex[]{new Vertex(new Vector3f(-SIZEX,START,START), new Vector2f(TEX_MAX_X, TEX_MAX_Y)),
											 new Vertex(new Vector3f(-SIZEX,SIZEY,START), new Vector2f(TEX_MAX_X, TEX_MIN_Y)),
											 new Vertex(new Vector3f(SIZEX,SIZEY,START), new Vector2f(TEX_MIN_X, TEX_MIN_Y)),
											 new Vertex(new Vector3f(SIZEX,START,START), new Vector2f(TEX_MIN_X, TEX_MAX_Y))
											};
											 
			
			int[] indices = new int[]{0,1,2,
									  0,2,3
									 };
			
			mesh = new Mesh(vertices, indices);
		}
	}
	
	private void idleUpdate(Vector3f orientation, float distance) {
	}
	
	private void chasingUpdate(Vector3f orientation, float distance) {
		if(distance > 1) {
			float movAmt = -MOVE_SPEED * (float) Time.getDelta();
			
			Vector3f oldPos = transform.getTranslation();
			Vector3f newPos = transform.getTranslation().add(orientation.mul(-MOVE_SPEED * (float)Time.getDelta()));
			
			Vector3f collisionVector = Game.getLevel().checkCollision(oldPos, newPos, WIDTH, HEIGHT);
			
			Vector3f movementVector = collisionVector.mul(orientation);
			
			if(movementVector.length() > 0)
				transform.setTranslation(transform.getTranslation().add(movementVector.mul(movAmt)));
		}
	}

	private void attackingUpdate(Vector3f orientation, float distance) {
		Vector2f lineStart = new Vector2f(transform.getTranslation().getX(), transform.getTranslation().getZ());
		Vector2f castDirection = new Vector2f(-orientation.getX(), -orientation.getZ()).rotate((random.nextFloat() - 0.5f) * SHOOT_ERROR);
		Vector2f lineEnd = lineStart.add(castDirection.mul(SHOOT_DISTANCE));
		
		Vector2f collisionVector = Game.getLevel().checkIntersections(lineStart, lineEnd);
		Vector2f playerIntersectVector = Game.getLevel().lineIntersectRect(lineStart, lineEnd, 
					new Vector2f(Transform.getCamera().getPos().getX(), Transform.getCamera().getPos().getZ()), 
					new Vector2f(Player.PLAYER_SIZE, Player.PLAYER_SIZE));
		
		if(playerIntersectVector != null && (collisionVector == null || 
				playerIntersectVector.sub(lineStart).length() < collisionVector.sub(lineStart).length())) {
			System.out.println("Player Shot");
			state = STATE_CHASING;
		}
		
		if(collisionVector == null)
			System.out.println("Shooting missed everything");
		else
			System.out.println("We've hit something!");
	}
	
	private void dyingUpdate(Vector3f orientation, float distance) {
		
	}
	
	private void deadUpdate(Vector3f orientation, float distance) {
		
	}
	
	private void facePlayer(Vector3f directionToPlayer) {
		float angleToFacePlayer = (float)Math.toDegrees(Math.atan(directionToPlayer.getZ() / directionToPlayer.getX()));
		
		if(directionToPlayer.getX() > 0)
			angleToFacePlayer += 180;
			
		transform.getRotation().setY(angleToFacePlayer + 90);
	}
	
	public void update() {
		Vector3f directionToPlayer = transform.getTranslation().sub(Transform.getCamera().getPos());
		
		float distance = directionToPlayer.length();
		Vector3f orientation = directionToPlayer.div(distance);
		
		facePlayer(orientation);
		transform.getTranslation().setY(OFFSET_FROM_GROUND);
		
		switch(state) {
			case STATE_IDLE: 
				idleUpdate(orientation, distance); 
				break;
			case STATE_CHASING:
				chasingUpdate(orientation, distance); 
				break;
			case STATE_ATTACKING:
				attackingUpdate(orientation, distance); 
				break;
			case STATE_DYING:
				dyingUpdate(orientation, distance); 
				break;
			case STATE_DEAD:
				deadUpdate(orientation, distance); 
				break;
		}
	}
	
	public void render() {
		Shader shader = Game.getLevel().getShader();
		shader.updateUniforms(transform.getTransformation(), transform.getProjectedTransformation(), material);
		mesh.draw();
	}
}
