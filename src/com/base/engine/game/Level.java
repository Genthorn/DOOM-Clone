package com.base.engine.game;

import java.util.ArrayList;

import com.base.engine.renderEngine.Bitmap;
import com.base.engine.renderEngine.Material;
import com.base.engine.renderEngine.Mesh;
import com.base.engine.renderEngine.Texture;
import com.base.engine.renderEngine.shaders.BasicShader;
import com.base.engine.renderEngine.shaders.Shader;
import com.base.engine.utils.Transform;
import com.base.engine.utils.Util;
import com.base.engine.utils.Vector2f;
import com.base.engine.utils.Vector3f;
import com.base.engine.utils.Vertex;

public class Level {
	
	private Mesh mesh;
	private Bitmap level;
	private Shader shader;
	private Material material;
	private Transform transform;
	
	private ArrayList<Vector2f> collisionPosStart;
	private ArrayList<Vector2f> collisionPosEnd;
	
	private static final float SPOT_WIDTH = 1;
	private static final float SPOT_LENGTH = 1;
	private static final float SPOT_HEIGHT = 1;
	
	private static final int NUM_TEX_EXP = 4;
	private static final int NUM_TEXTURES = (int) Math.pow(2, NUM_TEX_EXP);
	
	public Level(String levelname, String texturename) {
		level = new Bitmap(levelname).flipY();
		material = new Material(new Texture(texturename));
		transform = new Transform();
		
		shader = BasicShader.getInstance();
		
		collisionPosStart = new ArrayList<Vector2f>();
		collisionPosEnd = new ArrayList<Vector2f>();
		
		generateLevel();
	}
	
	public void input() {}
	
	public void update() {}
	
	public void render() {
		shader.bind();
		shader.updateUniforms(transform.getTransformation(), 
				transform.getProjectedTransformation(), material);
		mesh.draw();
	}
	
	public Vector3f checkCollision(Vector3f oldPos, Vector3f newPos, float obectWidth, float objectHeight) {
		Vector2f collisionVector = new Vector2f(1,1);
		Vector3f movementVector = newPos.sub(oldPos);
		
		if(movementVector.length() > 0) {
			Vector2f blockSize = new Vector2f(SPOT_WIDTH, SPOT_LENGTH);
			Vector2f objectSize = new Vector2f(obectWidth, objectHeight);
			
			Vector2f oldPos2 = new Vector2f(oldPos.getX(), oldPos.getZ());
			Vector2f newPos2 = new Vector2f(newPos.getX(), newPos.getZ());
			
			for(int i = 0; i < level.getWidth(); i++)
				for(int j = 0; j < level.getHeight(); j++)
					if((level.getPixel(i, j) & 0xFFFFFF) == 0)
						collisionVector = collisionVector.mul(rectCollide(oldPos2, newPos2, objectSize, blockSize.mul(new Vector2f(i, j)), blockSize));
		}
		
		return new Vector3f(collisionVector.getX(), 0, collisionVector.getY());
	}
	
	public Vector2f checkIntersections(Vector2f lineStart, Vector2f lineEnd) {
		Vector2f nearestIntersection = null;
		
		for(int i = 0; i < collisionPosStart.size(); i++) {
			Vector2f collisionVector = lineIntersect(lineStart, lineEnd, collisionPosStart.get(i), collisionPosEnd.get(i));
		
			if (collisionVector != null && (nearestIntersection == null ||
					nearestIntersection.sub(lineStart).length() > collisionVector.sub(lineStart).length())) {
				nearestIntersection = collisionVector;
			}
		}
		
		return nearestIntersection;
	}
	
	private Vector2f findNearestVector2f(Vector2f a, Vector2f b, Vector2f positionRelativeTo) {
		if (b != null && (a == null ||
				a.sub(positionRelativeTo).length() > b.sub(positionRelativeTo).length())) {
			return b;
		}
		
		return a;
	}
	
	public Vector2f lineIntersectRect(Vector2f lineStart, Vector2f lineEnd, Vector2f rectPos, Vector2f rectSize) {
		Vector2f result = null;

		Vector2f collisionVector = lineIntersect(lineStart, lineEnd, rectPos, new Vector2f(rectPos.getX() + rectSize.getX(), rectPos.getY()));
		result = findNearestVector2f(result, collisionVector, lineStart);

		collisionVector = lineIntersect(lineStart, lineEnd, rectPos, new Vector2f(rectPos.getX(), rectPos.getY() + rectSize.getY()));
		result = findNearestVector2f(result, collisionVector, lineStart);

		collisionVector = lineIntersect(lineStart, lineEnd, new Vector2f(rectPos.getX(), rectPos.getY() + rectSize.getY()), rectPos.add(rectSize));
		result = findNearestVector2f(result, collisionVector, lineStart);

		collisionVector = lineIntersect(lineStart, lineEnd, new Vector2f(rectPos.getX() + rectSize.getX(), rectPos.getY()), rectPos.add(rectSize));
		result = findNearestVector2f(result, collisionVector, lineStart);

		return result;
	}
	
	private float crossProd(Vector2f a, Vector2f b) {
		return a.getX() * b.getY() - a.getY() * b.getX();
	}
	
	private Vector2f lineIntersect(Vector2f lineStart1, Vector2f lineEnd1, Vector2f lineStart2, Vector2f lineEnd2) {
		
		Vector2f line1 = lineEnd1.sub(lineStart1);
		Vector2f line2 = lineEnd2.sub(lineStart2);

		//lineStart1 + line1 * a == lineStart2 + line2 * b

		float cross = crossProd(line1, line2);

		if(cross == 0)
			return null;

		Vector2f distanceBetweenLineStarts = lineStart2.sub(lineStart1);

		float a = crossProd(distanceBetweenLineStarts, line2) / cross;
		float b = crossProd(distanceBetweenLineStarts, line1) / cross;

		if(0.0f < a && a < 1.0f && 0.0f < b && b < 1.0f)
			return lineStart1.add(line1.mul(a));

		return null;
	}
	
	public Vector2f rectCollide(Vector2f oldPos, Vector2f newPos, Vector2f size1, Vector2f pos2, Vector2f size2) {
		Vector2f result = new Vector2f(0,0);
		
		if(newPos.getX() + size1.getX() < pos2.getX() ||
		   newPos.getX() - size1.getX() > pos2.getX() + size2.getX() * size2.getX() ||
		   oldPos.getY() + size1.getY() < pos2.getY() || 
		   oldPos.getY() - size1.getY() > pos2.getY() + size2.getY() * size2.getY()) {
			
			result.setX(1);
		}
		
		if(oldPos.getX() + size1.getX() < pos2.getX() ||
		   oldPos.getX() - size1.getX() > pos2.getX() + size2.getX() * size2.getX() ||
		   newPos.getY() + size1.getY() < pos2.getY() || 
		   newPos.getY() - size1.getY() > pos2.getY() + size2.getY() * size2.getY()) {
					
			result.setY(1);
		}
		
		return result;
	}
	
	private void addFace(ArrayList<Integer> indices, int startLocation, boolean direction) {
		if(direction) {
			indices.add(startLocation + 2);
			indices.add(startLocation + 1);
			indices.add(startLocation + 0);
			indices.add(startLocation + 3);
			indices.add(startLocation + 2);
			indices.add(startLocation + 0);
		} else {
			indices.add(startLocation + 0);
			indices.add(startLocation + 1);
			indices.add(startLocation + 2);
			indices.add(startLocation + 0);
			indices.add(startLocation + 2);
			indices.add(startLocation + 3);
		}
	}
	
	private float[] calcTexCoords(int value) {
		int texX = value / NUM_TEXTURES;
		int texY = texX % NUM_TEX_EXP;
		texX /= NUM_TEX_EXP;
		
		float[] result = new float[4];
		
		result[0] = 1f - (float) texX / (float) NUM_TEX_EXP;
		result[1] = result[0] - 1f / (float) NUM_TEX_EXP;
		result[3] = 1f - (float) texY / (float) NUM_TEX_EXP;
		result[2] = result[3] - 1f / (float) NUM_TEX_EXP;
		
		return result;
	}
	
	private void addVertices(ArrayList<Vertex> vertices, int i, int j, boolean x, boolean y, boolean z, float offset, float[] texCoords) {
		if(x && z) {
			vertices.add(new Vertex(new Vector3f(i * SPOT_WIDTH, offset * SPOT_HEIGHT, j * SPOT_LENGTH), new Vector2f(texCoords[1],texCoords[3])));
			vertices.add(new Vertex(new Vector3f((i + 1) * SPOT_WIDTH, offset * SPOT_HEIGHT, j * SPOT_LENGTH), new Vector2f(texCoords[0],texCoords[3])));
			vertices.add(new Vertex(new Vector3f((i + 1) * SPOT_WIDTH, offset * SPOT_HEIGHT, (j + 1) * SPOT_LENGTH), new Vector2f(texCoords[0],texCoords[2])));
			vertices.add(new Vertex(new Vector3f(i * SPOT_WIDTH, offset * SPOT_HEIGHT, (j + 1) * SPOT_LENGTH), new Vector2f(texCoords[1],texCoords[2])));
		} else if(x && y) {
			vertices.add(new Vertex(new Vector3f(i * SPOT_WIDTH, j * SPOT_HEIGHT, offset * SPOT_LENGTH), new Vector2f(texCoords[1],texCoords[3])));
			vertices.add(new Vertex(new Vector3f((i + 1) * SPOT_WIDTH, j * SPOT_HEIGHT, offset * SPOT_LENGTH), new Vector2f(texCoords[0],texCoords[3])));
			vertices.add(new Vertex(new Vector3f((i + 1) * SPOT_WIDTH, (j + 1) * SPOT_HEIGHT, offset * SPOT_LENGTH), new Vector2f(texCoords[0],texCoords[2])));
			vertices.add(new Vertex(new Vector3f(i * SPOT_WIDTH, (j + 1) * SPOT_HEIGHT, offset * SPOT_LENGTH), new Vector2f(texCoords[1],texCoords[2])));
		} else if(y && z) {
			vertices.add(new Vertex(new Vector3f(offset * SPOT_WIDTH, i * SPOT_HEIGHT, j * SPOT_LENGTH), new Vector2f(texCoords[1],texCoords[3])));
			vertices.add(new Vertex(new Vector3f(offset * SPOT_WIDTH, i * SPOT_HEIGHT, (j + 1) * SPOT_LENGTH), new Vector2f(texCoords[0],texCoords[3])));
			vertices.add(new Vertex(new Vector3f(offset * SPOT_WIDTH, (i + 1) * SPOT_HEIGHT, (j + 1) * SPOT_LENGTH), new Vector2f(texCoords[0],texCoords[2])));
			vertices.add(new Vertex(new Vector3f(offset * SPOT_WIDTH, (i + 1) * SPOT_HEIGHT, j * SPOT_LENGTH), new Vector2f(texCoords[1],texCoords[2])));
		} else {
			System.err.println("Invalid plane used in level generator");
			new Exception().printStackTrace();
			System.exit(1);
		}
	}
	
	private void generateLevel() {
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		ArrayList<Integer> indices = new ArrayList<Integer>();
		
		//level loading
		for(int i = 0; i < level.getWidth(); i++) {
			for(int j = 0; j < level.getHeight(); j++) {
				
				if((level.getPixel(i, j) & 0xFFFFFF) == 0) 
					continue;
				
				float[] texCoords = calcTexCoords((level.getPixel(i, j) & 0x00FF00) >> 8);
				
				//Generate Floor
				addFace(indices, vertices.size(), true);
				addVertices(vertices, i, j, true, false, true, 0, texCoords);
			
				//Generate Ceiling
//				addFace(indices, vertices.size(), false);
//				addVertices(vertices, i, j, true, false, true, 1, texCoords);
				
				texCoords = calcTexCoords((level.getPixel(i, j) & 0xFF0000) >> 16);
				
				//Generate Walls
				if((level.getPixel(i, j - 1) & 0xFFFFFF) == 0) {
					collisionPosStart.add(new Vector2f(i * SPOT_WIDTH, j * SPOT_LENGTH));
					collisionPosEnd.add(new Vector2f((i + 1) * SPOT_WIDTH, j * SPOT_LENGTH));
					addFace(indices, vertices.size(), false);
					addVertices(vertices, i, 0, true, true, false, j, texCoords);
				}
				
				if((level.getPixel(i, j + 1) & 0xFFFFFF) == 0) {
					collisionPosStart.add(new Vector2f(i * SPOT_WIDTH, (j + 1) * SPOT_LENGTH));
					collisionPosEnd.add(new Vector2f((i + 1) * SPOT_WIDTH, (j + 1) * SPOT_LENGTH));
					addFace(indices, vertices.size(), true);
					addVertices(vertices, i, 0, true, true, false, (j + 1), texCoords);
				}
				
				if((level.getPixel(i - 1, j) & 0xFFFFFF) == 0) {
					collisionPosStart.add(new Vector2f(i * SPOT_WIDTH, j * SPOT_LENGTH));
					collisionPosEnd.add(new Vector2f(i * SPOT_WIDTH, (j + 1) * SPOT_LENGTH));
					addFace(indices, vertices.size(), true);
					addVertices(vertices, 0, j, false, true, true, i, texCoords);
				}
				
				if((level.getPixel(i + 1, j) & 0xFFFFFF) == 0) {
					collisionPosStart.add(new Vector2f((i + 1) * SPOT_WIDTH, j * SPOT_LENGTH));
					collisionPosEnd.add(new Vector2f((i + 1) * SPOT_WIDTH, j * SPOT_LENGTH));
					addFace(indices, vertices.size(), false);
					addVertices(vertices, 0, j, false, true, true, (i + 1), texCoords);
				}
			}
		}
		
		Vertex[] vertArray = new Vertex[vertices.size()];
		Integer[] intArray = new Integer[indices.size()];
		
		vertices.toArray(vertArray);
		indices.toArray(intArray);
		
		mesh = new Mesh(vertArray, Util.toIntArray(intArray));
	}
	
	public Shader getShader() {
		return shader;
	}
}
