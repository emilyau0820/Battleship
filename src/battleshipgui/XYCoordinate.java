/*
Project: Battleship
File: XYCoordinate.java
Description: Defines a position on the game grid using x and y values, stores grid coordinates to identify a specific block or ship segment. Compatible with GUI.
Date: May. 29, 2024 - Jun. 17, 2024
Author: Emily Au, Xuanyao Li
*/

package battleshipgui;

import java.util.Objects;

public class XYCoordinate {
	
	protected int x;
	protected int y;
	
	public XYCoordinate() {
		x = 1;
		y = 1;
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		XYCoordinate that = (XYCoordinate) o;
		return x == that.x && y == that.y;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	@Override
	public String toString() {
		return (String)((char)(y + 'A') + "" + x);
	}
}