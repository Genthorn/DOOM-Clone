package com.base.engine.renderEngine;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Bitmap {
	private int width;
	private int height;
	private int[] pixels;
	
	public Bitmap(String filename) {
		try {
			BufferedImage image = ImageIO.read(new File("./res/" + filename));
			width = image.getWidth();
			height = image.getHeight();
			pixels = new int[width * height];
			image.getRGB(0, 0, width, height, pixels, 0, width);
		} catch (IOException e) {
			System.out.println("Could not find bitmap: ./res/" + filename);
			e.printStackTrace();
		}
	}
	
	public Bitmap(int width, int height) {
		this.width = width;
		this.height = height;
		this.pixels = new int[width * height];
	}
	
	//NEEDS REWRITE
	public Bitmap flipX() {
		int[] temp = new int[pixels.length];
		
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				temp[i + j * width] = pixels[(width - i - 1) + j * width];
			}
		}
		
		pixels = temp;
		return this;
	}
	
	public Bitmap flipY() {
		int[] temp = new int[pixels.length];
		
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				temp[i + j * width] = pixels[i + (height - j - 1) * width];
			}
		}
		
		pixels = temp;
		return this;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int[] getPixels() {
		return pixels;
	}
	
	public int getPixel(int x, int y) {
		return pixels[x + y * width];
	}
	
	public void setPixel(int x, int y, int value) {
		pixels[x + y * width] = value;
	}
	
}
