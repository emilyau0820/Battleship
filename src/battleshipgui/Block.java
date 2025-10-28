/*
Project: Battleship
File: Block.java
Description: Represents a single cell on the grid, optionally holding a ship segment and tracking hit status. Compatible with and without GUI.
Date: May. 29, 2024 - Jun. 17, 2024
Author: Emily Au
*/

package src;

public class Block {
	
	private int xCoordinate;
	private char yCoordinate;
	private char shipOccupation;
	private boolean isShot;
	
	public Block(int x, char y, char initialOccupation) {
		xCoordinate = x;
		yCoordinate = y;
		shipOccupation = initialOccupation;
		isShot = false;
	}
	
	public String getXY() {
		String xy = yCoordinate + "" + xCoordinate;
		return xy;
	}
	
	public char getOccupation() {
		return shipOccupation;
	}
	
	public void setOccupation(char newOccupation) {
		shipOccupation = newOccupation;
	}
	
	public boolean getIsShot() {
		return isShot;
	}
	
	public void setIsShot(boolean newIsShot) {
		isShot = newIsShot;
	}
	
}