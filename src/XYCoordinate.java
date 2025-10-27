/*
Project: Battleship
File: XYCoordinate.java
Description: Defines a position on the game grid using x and y values, stores grid coordinates to identify a specific block or ship segment.
Date: May. 29, 2024 - Jun. 17, 2024
Author: Emily Au
*/

package src;

public class XYCoordinate {
	
	protected int x;
	protected int y;
	
	public XYCoordinate() {
		x = 0;
		y = 0;
	}
	
	public XYCoordinate(int initialX, int initialY) {
		x = initialX;
		y = initialY;
	}
	
	public int getX() {
		return x;
	}
	
	public void setX(int newX) {
		x = newX;
	}
	
	public int getY() {
		return y;
	}
	
	public void setY(int newY) {
		y = newY;
	}

}