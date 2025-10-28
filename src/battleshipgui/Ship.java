/*
Project: Battleship
File: Ship.java
Description: Represents a vessel occupying multiple connected blocks on the game grid, encapsulates the position, size, and hit state of a battleship. Compatible with GUI.
Date: May. 29, 2024 - Jun. 17, 2024
Author: Emily Au, Xuanyao Li
*/

package battleshipgui;

public class Ship {
	
	private String name;
	private int length;
	private String orientation;
	private XYCoordinate[] coordinates;
	private boolean isDestroyed;
	
	public Ship(String initialName, int initialLength) {
		name = initialName;
		length = initialLength;
		isDestroyed = false;
	}
	public Ship() {
		name = "";
		length = 0;
		isDestroyed = false;
	}
	
	public String getName() {
		return name;
	}
	
	public int getLength() {
		return length;
	}
	
	public String getOrientation() {
		return orientation;
	}
	
	public void setOrientation(String newOrientation) {
		orientation = newOrientation;
	}
	
	public XYCoordinate[] getCoordinates() {
		return coordinates;
	}
	
	public void setCoordinates(XYCoordinate[] newCoordinates) {
		coordinates = newCoordinates;
	}
	
	public boolean getIsDestroyed() {
		return isDestroyed;
	}
	
	public void setIsDestroyed(boolean newIsDestroyed) {
		isDestroyed = newIsDestroyed;
	}

	@Override
	public String toString() {
		return name;
	}
}