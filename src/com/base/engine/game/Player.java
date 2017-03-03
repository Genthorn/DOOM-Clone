package com.base.engine.game;

import com.base.engine.Game;
import com.base.engine.renderEngine.Camera;
import com.base.engine.renderEngine.Window;
import com.base.engine.utils.Input;
import com.base.engine.utils.Time;
import com.base.engine.utils.Transform;
import com.base.engine.utils.Vector2f;
import com.base.engine.utils.Vector3f;

public class Player {
	
	public static final float PLAYER_SIZE = 0.15f;
	private static final float MOUSE_SENSITIVITY = 0.15f;
	private static final float MOVE_SPEED = 2.0f;
	private static final Vector3f zeroVector = new Vector3f(0,0,0);
	private float sprint = 0;
	
	private Camera camera;
	private boolean mouseLocked = false;
	private Vector2f centerPosition = new Vector2f(Window.getWidth()/2, Window.getHeight()/2);
	private Vector3f movementVector;
	
	public Player(Vector3f position) {
		movementVector = zeroVector;
		camera = new Camera(position, new Vector3f(0,0,1), new Vector3f(0,1,0));
		Transform.setCamera(camera);
		Transform.setProjection(70, Window.getWidth(), Window.getHeight(), 0.01f, 1000f);
	}
	
	public void input() {
		sprint = 0;
		
		if(Input.getKey(Input.KEY_LSHIFT))
			sprint = 0.0002f;
		
		if(Input.getKey(Input.KEY_ESCAPE)) {
			Input.setCursor(true);
			mouseLocked = false;
		}
		
		if(Input.getMouseDown(0)) {
			Input.setMousePosition(centerPosition);
			Input.setCursor(false);
			mouseLocked = true;
		}
		
		movementVector = zeroVector;
		
		if(Input.getKey(Input.KEY_W))
			movementVector = movementVector.add(camera.getForward());
		if(Input.getKey(Input.KEY_S))
			movementVector = movementVector.sub(camera.getForward());
		if(Input.getKey(Input.KEY_A))
			movementVector = movementVector.add(camera.getLeft());
		if(Input.getKey(Input.KEY_D))
			movementVector = movementVector.add(camera.getRight());
		
		if(Input.getKey(Input.KEY_LEFT))
			camera.rotateY(-0.3f * MOUSE_SENSITIVITY);
		if(Input.getKey(Input.KEY_RIGHT))
			camera.rotateY(0.3f * MOUSE_SENSITIVITY);
		
		if(mouseLocked) {
			Vector2f deltaPos = Input.getMousePosition().sub(centerPosition);
			
			boolean rotX = deltaPos.getX() != 0;
			
			if(rotX)
				camera.rotateY(deltaPos.getX() * MOUSE_SENSITIVITY);
				
			if(rotX)
				Input.setMousePosition(centerPosition);
		}
	}
	
	public void update() {
		float movAmt = (float)(MOVE_SPEED * Time.getDelta()) + sprint;
		
		movementVector.setY(0);
		if(movementVector.length() > 0) 
			movementVector = movementVector.normalized();
		
		Vector3f oldPos = camera.getPos();
		Vector3f newPos = oldPos.add(movementVector.mul(movAmt));
		
		Vector3f collisionVector = Game.getLevel().checkCollision(oldPos, newPos, PLAYER_SIZE, PLAYER_SIZE);
		movementVector = movementVector.mul(collisionVector);
		
		if(movementVector.length() > 0)
			camera.move(movementVector, movAmt);
	}

	public void render() {
		
	}
	
	public Camera getCamera() {
		return camera;
	}
	
	public Vector3f getPosition() {
		return camera.getPos();
	}
}
