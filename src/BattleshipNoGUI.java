/*
Project: Battleship
File: BattleshipNoGUI.java
Description: Non-GUI Battleship Game Program (Easy or Hard Difficulty)
Date: May. 29, 2024 - Jun. 17, 2024
Author: Emily Au
*/

package src;

import java.util.*;
import java.io.*;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class BattleshipNoGUI {
	
	// global variables
	
	static boolean gameOver = false;
	static XYCoordinate lastHit = new XYCoordinate();
	static int numGuesses = 0;
	static boolean targetMode = false;
	static boolean computerWin = false;
	static boolean userWin = false;
	
	/**
	 * Initializes and returns the board of Blocks the user will refer to while guessing
	 * 
	 * @param boardLength - board dimensions
	 * @return 2D Block array (board) that user will guess from
	 */
	public static Block[][] initializeUserBoard(int boardLength) {
		Block[][] userBoard = new Block[boardLength][boardLength];
		for (int r = 0; r < boardLength; r++) {
			for (int c = 0; c < boardLength; c++) {
				userBoard[r][c] = new Block(c + 1, intToLetter(r), '?');
			}
		}
		return userBoard;
	}
	
	/**
	 * Converts a letter (A - J) to an integer (0 - 9)
	 * 
	 * @param letter - character to be converted into an integer
	 * @return corresponding integer value of letter
	 */
	public static int letterToInt(char letter) {
		return ((int) (letter)) - 65;
	}
	
	/**
	 * Converts an integer (0 - 9) to a letter (A - J)
	 * 
	 * @param integer - integer to be converted into a letter
	 * @return corresponding letter value of the integer
	 */
	public static char intToLetter(int integer) {
		return (char) (integer + 65);
	}
	
	/**
	 * Returns a random integer with value weighting implemented
	 * 
	 * @param board - 2D double array of values to be selected from
	 * @return index of chosen random weighted value
	 */
	public static int weightedRandom(double[][] board) {
		double totalWeight = 0;
		for (int r = 0; r < board.length; r++) {
			totalWeight += board[r][0];
		}
		
		int index = 0;
		for (double r = Math.random() * totalWeight; index < board.length - 1; ++index) {
			r -= board[index][0];
			if (r <= 0) {
				break;
			}
		}
		return index;
	}
	
	/**
	 * Expert ship placement algorithm: uses random weighted placement to determine locations for placement
	 * 
	 * @param board - 2D Block array (board) to place ships upon
	 * @param ships - array of ships that will be placed
	 * @param water - character to represent empty spaces
	 */
	public static void placeShips(Block[][] board, Ship[] ships, char water) {
		// initializes all blocks of board to water
		for (int r = 0; r < board.length; r++) {
			for (int c = 0; c < board.length; c++) {
				board[r][c] = new Block(r, intToLetter(c), water);
			}
		}
		
		int[] temp = new int[5];
		
		// generates inverted heat map for empty board
		double[][] heatMap = generateOverallHeatMap(board, ships, temp, water);
		for (int r = 0; r < heatMap.length; r++) {
			for (int c = 0; c < heatMap.length; c++) {
				heatMap[r][c] = 1 / heatMap[r][c];
			}
		}
		
		// placing ships
		int shipsPlaced = 0;
		while (shipsPlaced < 5) {
			boolean validPlacement = false;
			String orientation;
			
			int zeroOrOne = (int)(Math.random()*2);
			
			// determines orientation of ship to be placed
			if (zeroOrOne == 0) {
				orientation = "horizontal";
			} else {
				orientation = "vertical";
			}
			
			ships[shipsPlaced].setOrientation(orientation);
			
			do {
				XYCoordinate initialCoordinate;
				validPlacement = true;
				int xIndex, yIndex;
				
				if (orientation.equals("horizontal")) {
					while (true) {
						// generates weighted random "starting coordinate"
						xIndex = weightedRandom(heatMap);
						yIndex = weightedRandom(heatMap);
						
						// checks if ship placement is valid in board boundaries
						if (yIndex < board.length - ships[shipsPlaced].getLength()) {
							break;
						}
					}
					
					initialCoordinate = new XYCoordinate(yIndex, xIndex);
					
					// checks if ship placement does not overlap other ships
					for (int c = initialCoordinate.getX(); c < initialCoordinate.getX() + ships[shipsPlaced].getLength(); c++) {
						if (board[initialCoordinate.getY()][c].getOccupation() != water) {
							validPlacement = false;
						}
					}
					
					// if ship placement is valid, place ship on board
					if (validPlacement) {
						for (int c = initialCoordinate.getX(); c < initialCoordinate.getX() + ships[shipsPlaced].getLength(); c++) {
							board[initialCoordinate.getY()][c].setOccupation(intToLetter(shipsPlaced));							
						}
					}
					
				} else {
					while (true) {
						// generates weighted random "starting coordinate"
						xIndex = weightedRandom(heatMap);
						yIndex = weightedRandom(heatMap);
						
						// checks if ship placement is valid in board boundaries
						if (xIndex < board.length - ships[shipsPlaced].getLength()) {
							break;
						}
					}
					
					initialCoordinate = new XYCoordinate(yIndex, xIndex);
					
					// checks if ship placement does not overlap other ships
					for (int r = initialCoordinate.getY(); r < initialCoordinate.getY() + ships[shipsPlaced].getLength(); r++) {
						if (board[r][initialCoordinate.getX()].getOccupation() != water) {
							validPlacement = false;
						}
					}
					
					// if ship placement is valid, place ship on board
					if (validPlacement) {
						for (int r = initialCoordinate.getY(); r < initialCoordinate.getY() + ships[shipsPlaced].getLength(); r++) {
								board[r][initialCoordinate.getX()].setOccupation(intToLetter(shipsPlaced));
						}
					}
				}
			} while(validPlacement != true);
			
			shipsPlaced++;
		}
	}
	
	/**
	 * Simple ship placement algorithm: uses random placement to determine locations for placement
	 * 
	 * @param board - 2D Block array (board) to place ships upon
	 * @param ships - array of ships that will be placed
	 * @param water - character to represent empty spaces
	 */
	public static void placeShipsSimple(Block[][] board, Ship[] ships, char water) {
		// initializes all blocks of board to water
		for (int r = 0; r < board.length; r++) {
			for (int c = 0; c < board.length; c++) {
				board[r][c] = new Block(r, intToLetter(c), water);
			}
		}
		
		int shipsPlaced = 0;
		
		// placing ships
		while (shipsPlaced < 5) {
			boolean validPlacement = false;
			String orientation;
			
			int zeroOrOne = (int)(Math.random()*2);
			
			// determines orientation of ship to be placed
			if (zeroOrOne == 0) {
				orientation = "horizontal";
			} else {
				orientation = "vertical";
			}
			
			ships[shipsPlaced].setOrientation(orientation);
			
			do {
				XYCoordinate initialCoordinate;
				validPlacement = true;
				
				if (orientation.equals("horizontal")) {
					// generates random "starting coordinate"
					initialCoordinate = new XYCoordinate((int)(Math.random()*(board.length - ships[shipsPlaced].getLength())), (int)(Math.random()*board.length));
					
					// checks if ship placement does not overlap other ships
					for (int c = initialCoordinate.getX(); c < initialCoordinate.getX() + ships[shipsPlaced].getLength(); c++) {
						if (board[initialCoordinate.getY()][c].getOccupation() != water) {
							validPlacement = false;
						}
					}
					
					// if ship placement is valid, place ship on board
					if (validPlacement) {
						for (int c = initialCoordinate.getX(); c < initialCoordinate.getX() + ships[shipsPlaced].getLength(); c++) {
							board[initialCoordinate.getY()][c].setOccupation(intToLetter(shipsPlaced));							
						}
					}
					
				} else {
					// generates random "starting coordinate"
					initialCoordinate = new XYCoordinate((int)(Math.random()*board.length), (int)(Math.random()*(board.length - ships[shipsPlaced].getLength())));
					
					// checks if ship placement does not overlap other ships
					for (int r = initialCoordinate.getY(); r < initialCoordinate.getY() + ships[shipsPlaced].getLength(); r++) {
						if (board[r][initialCoordinate.getX()].getOccupation() != water) {
							validPlacement = false;
						}
					}

					// if ship placement is valid, place ship on board
					if (validPlacement) {
						for (int r = initialCoordinate.getY(); r < initialCoordinate.getY() + ships[shipsPlaced].getLength(); r++) {
								board[r][initialCoordinate.getX()].setOccupation(intToLetter(shipsPlaced));
						}
					}
				}				
			} while(validPlacement != true);
			
			shipsPlaced++;
		}
	}
		
	/**
	 * Generates heat map/probability map for a ship of certain length
	 * 
	 * @param board - 2D Block array (board) that will be used assess probability
	 * @param water - character to represent empty space
	 * @param shipLength - length of ship to be assessed
	 * @return 2D double array of probability values
	 */
	public static double[][] generateHeatMapPerShip(Block[][] board, char water, int shipLength) {
		double[][] heatMap = new double[board.length][board.length];
		int sum = 0;
		
		for (int r = 0; r < board.length; r++) {
			for (int c = 0; c < board.length; c++) {
				
				// horizontal placement
				boolean validPlacement = true;
				
				for (int h = r; h < (r + shipLength); h++) {
					try {
						// if the block of the board being checked is not water
						if (board[h][c].getOccupation() != water) {
							validPlacement = false;
						}
					} catch (ArrayIndexOutOfBoundsException e) {
						validPlacement = false;
					}
				}
				
				// if ship placement is valid, increment heat map coordinates of ship placement
				if (validPlacement) {
					for (int h = r; h < (r + shipLength); h++) {
						heatMap[h][c]++;
						sum++;
					}
				}
				
				// vertical placement
				validPlacement = true;
				
				for (int v = c; v < (c + shipLength); v++) {
					try {
						// if the block of the board being checked is not water
						if (board[r][v].getOccupation() != water) {
							validPlacement = false;
						}
					} catch (ArrayIndexOutOfBoundsException e) {
						validPlacement = false;
					}	
				}
				
				// if ship placement is valid, increment heat map coordinates of ship placement
				if (validPlacement) {
					for (int v = c; v < (c + shipLength); v++) {
						heatMap[r][v]++;
						sum++;
					}
				}
			}
		}
		
		// changes each value of all heat map coordinates to the probability of ship occupation 
		for (int r = 0; r < board.length; r++) {
			for (int c = 0; c < board.length; c++) {
				heatMap[r][c] = heatMap[r][c] / sum;
			}
		}
		
		return heatMap;
	}
	
	/**
	 * Generates heat map/probability map for all ships that have not been sunk
	 * 
	 * @param board - 2D Block array (board) that will be used assess probability 
	 * @param ships - array of ships that will be checked for probability
	 * @param shipHitCount - integer array of number of shots that have hit each ship
	 * @param water - character to represent empty space
	 * @return 2D double array of total probability values
	 */
	public static double[][] generateOverallHeatMap(Block[][] board, Ship[] ships, int[] shipHitCount, char water) {
		double[][] heatMap = new double[board.length][board.length];
		
		for (int s = 0; s < ships.length; s++) {
			// generate individual heat maps for ships that are not sunk
			if (ships[s].getLength() != shipHitCount[s]) {
				double[][] shipHeatMap = generateHeatMapPerShip(board, water, ships[s].getLength());
				
				// add ship heat map to total overall heat map
				for (int r = 0; r < board.length; r++) {
					for (int c = 0; c < board.length; c++) {
						heatMap[r][c] += shipHeatMap[r][c];
					}
				}
			}
		}
		
		return heatMap;
	}
	
	/**
	 * Displays 2D double array (heat map)
	 * 
	 * @param board - 2D double array (heat map) to be displayed
	 */
	public static void displayHeatMap(double[][] board) {
		for (int r = 0; r < board.length; r++) {
			for (int c = 0; c < board.length; c++ ) {
				System.out.print(String.format( "%.3f", board[r][c]) + " ");
			}
			System.out.println("");
		}
	}
	
	/**
	 * Displays 2D Block array (board)
	 * 
	 * @param board - 2D Block array (board) to be displayed
	 */
	public static void displayBoard(Block[][] board) {
		for (int r = 0; r < board.length; r++) {
			for (int c = 0; c < board.length; c++ ) {
				System.out.print(board[r][c].getOccupation() + " ");
			}
			System.out.println("");
		}
	}
	
	public static void writeBoardToFile(Block[][] board) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter("AIShipPlacement.txt"))){
			for (int r = 0; r < board.length; r++) {
				for (int c = 0; c < board.length; c++) {
					writer.write(board[r][c].getOccupation() + " ");
				}
				writer.newLine();
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
 	 * Simple guess algorithm: guesses a random coordinate that has not been previously fired on before
	 * 
	 * @param board - 2D Block array (board) to be assessed for guessing
	 * @return XY coordinate to be guessed
	 */
	public static XYCoordinate simpleGuess(Block[][] board) {
		XYCoordinate guessCoordinate = new XYCoordinate();
		
		while (true) {
			// generates random coordinate values
			int randomX = (int) (Math.random()*10);
			int randomY = (int) (Math.random()*10);
			
			// checks if coordinate is valid to guess
			if (board[randomY][randomX].getIsShot() == false) {
				guessCoordinate.setX(randomX);
				guessCoordinate.setY(randomY);
				board[randomY][randomX].setIsShot(true);
				break;
			}
		}
		
		return guessCoordinate;
	}
	
	/**
	 * Hunts a coordinate using the parity method: targets only certain coordinates in which ship of certain length must reside upon
	 * 
	 * @param board - 2D Block array (board) to be assessed for guessing
	 * @param longestShipLength - target ship length of ship
	 * @return XY coordinate to be guessed
	 */
	public static XYCoordinate huntParity(Block[][] board, int longestShipLength) {
		XYCoordinate guessCoordinate = new XYCoordinate();
		
		while (true) {
			// generates random coordinate values
			int randomX = (int) (Math.random()*10), randomYInt = (int) (Math.random()*10);
			char randomY = intToLetter(randomYInt);
			
			// checks if coordinate falls under parity coordinates and is valid to guess
			if (((randomX + randomYInt + 1) % longestShipLength == 0) && (board[randomX][randomYInt].getIsShot() == false)) {
				guessCoordinate.setX(randomX);
				guessCoordinate.setY(randomYInt);
				board[randomX][randomYInt].setIsShot(true);
				break;
			}
		}
		
		return guessCoordinate;
	}
	
	/**
	 * Hunts a coordinate using the heat map method: targets the coordinate with the highest probability of containing a ship (based of number of possible combinations able to be made on coordinate)
	 * 
	 * @param board - 2D Block array (board) to be assessed for guessing
	 * @param ships - array of ships to be hunted
	 * @param shipHitCount - integer array of number of shots that have hit each ship 
	 * @param unknown - character to represent empty, guessed coordinates
	 * @return XY coordinate to be guessed
	 */
	public static XYCoordinate huntHeatMap(Block[][] board, Ship[] ships, int[] shipHitCount, char unknown) {
		XYCoordinate guessCoordinate = new XYCoordinate();
		
		// generates total overall heat map to be referenced
		double[][] heatMap = generateOverallHeatMap(board, ships, shipHitCount, unknown);
		displayHeatMap(heatMap);
		int guessX = 0;
		int guessY = 0;
		
		// searches through heat map for coordinate with highest probability of ship occupation
		for (int r = 0; r < board.length; r++) {
			for (int c = 0; c < board.length; c++) {
				if (heatMap[r][c] > heatMap[guessY][guessX]) {
					guessX = c;
					guessY = r;
				} else if (heatMap[r][c] == heatMap[guessY][guessX]) {
					if (((int) (Math.random())*2) == 0) {
						guessX = c;
						guessY = r;
					}
				}
			}
		}
		
		guessCoordinate.setX(guessX);
		guessCoordinate.setY(guessY);
		board[guessX][guessY].setIsShot(true);
		
		return guessCoordinate;
	}
	
	/**
	 * Assuming a ship has been hit but not sunk, hunts a coordinate based on the previously "hit" coordinate(s)
	 * - if only one segment of a ship has been hit, search in directions surrounding ship
	 * - if multiple segments of a ship has been hit, search in orientation of ship segments (if segments share the same X coordinate, search vertically; if segments share the same Y Coordinate, search horizontally)
	 * 
	 * @param board - 2D Block array (board) to be assessed for guessing
	 * @param ships - array of ships to be hunted
	 * @param shipHitCount - integer array of number of shots that have hit each ship
	 * @param unknown - character to represent empty, guessed coordinates
	 * @return XY coordinate to be guessed
	 */
	public static XYCoordinate target(Block[][] board, Ship[] ships, int[] shipHitCount, char unknown) {	
		XYCoordinate guessCoordinate = new XYCoordinate();		
		
		int lastHitX = lastHit.getX();
		int lastHitY = lastHit.getY();
		
		XYCoordinate firstShip = new XYCoordinate();
		XYCoordinate lastShip = new XYCoordinate();
		
		boolean validFirstShip = false;
		boolean targetShip = false;
		int targetShipIndex = 0;
		
		// searches for the first and last ship segment of the shortest ship shot but not sunk
		for (int s = 0; s < ships.length; s++) {
			if (targetShip == false) {
				// if ship is shot but not sunk
				if ((shipHitCount[s] > 0) && (shipHitCount[s] < ships[s].getLength())) {
					for (int r = 0; r < board.length; r++) {
						for (int c = 0; c < board.length; c++) {
							if (board[r][c].getOccupation() == intToLetter(s)) {
								targetShip = true;
								targetShipIndex = s;
								if (validFirstShip == false) {
									firstShip.setX(c);
									firstShip.setY(r);
									validFirstShip = true;
								}
								lastShip.setX(c);
								lastShip.setY(r);
							}
						}
					}
				}
			}
		}
				
		// if one segment of the targeted ship has been hit
		
		if ((firstShip.getX() == lastShip.getX()) && (firstShip.getY() == lastShip.getY())) {
			
			boolean canShootVertical = false;
			boolean canShootHorizontal = false;
			
			// determines if ship has the possibility to be placed vertically
			for (int r = firstShip.getY() - ships[targetShipIndex].getLength() + 1; r <= firstShip.getY(); r++) {
				boolean validVertical = true;
				for (int i = r; i < r + ships[targetShipIndex].getLength() - 1; i++) {
					try {
						if ((board[i][firstShip.getX()].getOccupation() != unknown) && (board[i][firstShip.getX()].getOccupation() != intToLetter(targetShipIndex))) {
							validVertical = false;
						}
					}
					catch (ArrayIndexOutOfBoundsException e) {
						validVertical = false;
					}
				}
				if (validVertical) {
					canShootVertical = true;
				}
			}
			
			// determines if ship has the possibility to be placed horizontally
			for (int c = firstShip.getX() - ships[targetShipIndex].getLength() + 1; c <= firstShip.getX(); c++) {
				boolean validHorizontal = true;
				for (int i = c; i < c + ships[targetShipIndex].getLength() - 1; i++) {
					try {
						if ((board[firstShip.getY()][i].getOccupation() != unknown) && (board[firstShip.getY()][i].getOccupation() != intToLetter(targetShipIndex))) {
							validHorizontal = false;
						}
					}
					catch (ArrayIndexOutOfBoundsException e) {
						validHorizontal = false;
					}
				}
				if (validHorizontal) {
					canShootHorizontal = true;
				}
			}
						
			// if circumstances allow, shoot up
			if ((lastHitY != 0) && (board[lastHitY - 1][lastHitX].getOccupation() == unknown) && canShootVertical) {
				guessCoordinate.setX(lastHitX);
				guessCoordinate.setY(lastHitY - 1);
			}
			// if circumstances allow, shoot left
			else if ((lastHitX != 0) && (board[lastHitY][lastHitX - 1].getOccupation() == unknown) && canShootHorizontal) {
				guessCoordinate.setX(lastHitX - 1);
				guessCoordinate.setY(lastHitY);
			}
			// if circumstances allow, shoot down
			else if ((lastHitY != board.length - 1) && (board[lastHitY + 1][lastHitX].getOccupation() == unknown) && canShootVertical) {
				guessCoordinate.setX(lastHitX);
				guessCoordinate.setY(lastHitY + 1);
			}
			// if circumstances allow, shoot right
			else if ((lastHitX != board.length - 1) && (board[lastHitY][lastHitX + 1].getOccupation() == unknown) && canShootHorizontal) {
				guessCoordinate.setX(lastHitX + 1);
				guessCoordinate.setY(lastHitY);
			}
		}
		
		// if multiple segments of the targeted ship has been hit
		
		// if circumstances allow, shoot vertical - shoot above first ship segment
		if ((firstShip.getY() > 0) && firstShip.getX() == lastShip.getX() && (board[firstShip.getY() - 1][firstShip.getX()].getOccupation() == unknown)) {
			guessCoordinate.setX(firstShip.getX());
			guessCoordinate.setY(firstShip.getY() - 1);
		}
		// if circumstances allow, shoot horizontal - shoot to the left of first ship segment
		else if ((firstShip.getX() > 0) && (firstShip.getY() == lastShip.getY()) && (board[firstShip.getY()][firstShip.getX() - 1].getOccupation() == unknown)) {
			guessCoordinate.setX(firstShip.getX() - 1);
			guessCoordinate.setY(firstShip.getY());
		}
		// if circumstances allow, shoot vertical - shoot below last ship segment
		else if ((lastShip.getY() < board.length - 1) && (firstShip.getX() == lastShip.getX()) && (board[lastShip.getY() + 1][lastShip.getX()].getOccupation() == unknown)) {
			guessCoordinate.setX(lastShip.getX());
			guessCoordinate.setY(lastShip.getY() + 1);
		}
		// if circumstances allow, shoot horizontal - shoot to the right of last ship segment
		else if ((lastShip.getX() < board.length - 1) && (firstShip.getY() == lastShip.getY()) && (board[lastShip.getY()][lastShip.getX() + 1].getOccupation() == unknown)) {
			guessCoordinate.setX(lastShip.getX() + 1);
			guessCoordinate.setY(lastShip.getY());
		}
		
		board[guessCoordinate.getX()][guessCoordinate.getY()].setIsShot(true);
		
		return guessCoordinate;
	}
	
	/**
	 * Expert guess algorithm: guesses a coordinate depending on game circumstances using a variety of strategic methods
	 * 
	 * @param board - 2D Block array (board) to be assessed for guessing
	 * @param ships - array of ships to be hunted
	 * @param shotsHit - boolean array to track whether AI's guesses are hits or misses
	 * @param shipHitCount - integer array of number of shots that have hit each ship
	 * @param lastHit - last known coordinate to be a hit
	 * @param numGuesses - number of guesses AI has taken
	 * @param unknown - character to represent empty, guessed coordinates
	 * @return XY coordinate to be guessed
	 */
	public static XYCoordinate expertGuess(Block[][] board, Ship[] ships, ArrayList<Boolean> shotsHit, int[] shipHitCount, XYCoordinate lastHit, int numGuesses, char unknown) {
		XYCoordinate guessCoordinate = new XYCoordinate();
		
		// if a ship(s) has been shot but not sunk
		if (targetMode == true) {
			guessCoordinate = target(board, ships, shipHitCount, unknown);
		}
		else if (numGuesses < 7) {
			guessCoordinate = huntParity(board, 4);
		}
		else {
			guessCoordinate = huntHeatMap(board, ships, shipHitCount, unknown);
		}
		
		return guessCoordinate;
	}
	
	// self-play statistics updater
	public static void check(Block[][] unknownBoard, Block[][] answerBoard, Ship[] ships, ArrayList<Boolean> shotsHit, int[] shipHitCount, XYCoordinate coordinateShot) {
		int shipIndex = -1;
		int hitX = 0;
		int hitY = 0;
		for (int r = 0; r < unknownBoard.length; r++) {
			for (int c = 0; c < unknownBoard.length; c++) {
				for (int s = 0; s < ships.length; s++) {
					if (c == coordinateShot.getX() && r == coordinateShot.getY()) {
						if (answerBoard[r][c].getOccupation() == intToLetter(s)) {
							shipIndex = s;
							hitX = c;
							hitY = r;
							unknownBoard[r][c].setOccupation(intToLetter(s));
						}
					}
				}
				
			}
		}
		
		
		if (shipIndex != -1) {
			shotsHit.add(true);
			shipHitCount[shipIndex]++;
			lastHit.setX(hitX);
			lastHit.setY(hitY);
			if (shipHitCount[shipIndex] == ships[shipIndex].getLength()) {
				System.out.println("Sunk, " + ships[shipIndex].getName());
				targetMode = false;
								
				for (int s = 0; s < ships.length; s++) {
					if (shipHitCount[s] > 0 && shipHitCount[s] < ships[s].getLength()) {
						targetMode = true;
					}
				}
				
				gameOver = true;
				for (int i = 0; i < ships.length; i++) {
					if (shipHitCount[i] != ships[i].getLength()) {
						gameOver = false;
					}
				}
			}
			else {
				System.out.println("Hit, " + ships[shipIndex].getName());
				targetMode = true;
			}
		}
		else {
			System.out.println("Miss");
			shotsHit.add(false);
			unknownBoard[coordinateShot.getY()][coordinateShot.getX()].setOccupation('-');
		}
		
		displayBoard(unknownBoard);
				
		numGuesses++;
	}
	
	/**
	 * Updates statistics based on whether the AI's guess was a hit or a miss
	 * 
	 * @param unknownBoard - 2D Block array (board) that computer guesses from
	 * @param ships - array of ships to be hunted
	 * @param shotsHit - boolean array to track whether AI's guesses are hits or misses
	 * @param computerShipHitCount - integer array of AI's number of shots that have hit each ship
	 * @param coordinateShot - last coordinate that AI guessed to shoot at
	 * @param shot - whether AI's shot was a hit or miss
	 */
	public static void checkComputerGuess(Block[][] unknownBoard, Ship[] ships, ArrayList<Boolean> shotsHit, int[] computerShipHitCount, XYCoordinate coordinateShot, String shot) {
		int hitX = coordinateShot.getX();
		int hitY = coordinateShot.getY();
		
		// if computer's guess hit ship
		if (!(shot.equals("-"))) {
			shotsHit.add(true);
			lastHit.setX(hitX);
			lastHit.setY(hitY);
			int shipIndex = 0;
			
			// determine ship that computer shot
			for (int s = 0; s < ships.length; s++) {
				if (shot.equals(ships[s].getName())) {
					unknownBoard[hitY][hitX].setOccupation(intToLetter(s));
					computerShipHitCount[s]++;
					shipIndex = s;
				}
			}
			
			// if shot destroyed ship
			if (computerShipHitCount[shipIndex] == ships[shipIndex].getLength()) {
				targetMode = false;
				
				for (int s = 0; s < ships.length; s++) {
					// if any other ships are shot but not sunk
					if (computerShipHitCount[s] > 0 && computerShipHitCount[s] < ships[s].getLength()) {
						targetMode = true;
					}
				}
			}
			// if any ship has been shot but not sunk
			else {
				targetMode = true;
			}
		}
		// if computer's guess is a miss
		else {
			shotsHit.add(false);
			unknownBoard[coordinateShot.getY()][coordinateShot.getX()].setOccupation('-');
		}
		
		gameOver = true;
		
		for (int s = 0; s < ships.length; s++) {
			if (computerShipHitCount[s] != ships[s].getLength()) {
				gameOver = false;
			}
		}
		
		// if computer wins
		if (gameOver) {
			computerWin = true;
		}
		
		numGuesses++;
	}
	
	/**
	 * Updates statistics based on whether the user's guess was a hit or a miss 
	 * 
	 * @param computerBoard - 2D Block array (board) that user guesses from
	 * @param ships - array of ships to be hunted
	 * @param userShipHitCount - integer array of user's number of shots that have hit each ship 
	 * @param coordinateGuessed - last coordinate that user guessed to shoot at
	 */
	public static void checkUserGuess(Block[][] computerBoard, Ship[] ships, int[] userShipHitCount, XYCoordinate coordinateGuessed) {
		int shipIndex = 0;
		boolean hit = false;
		
		// checks if user's guess hit ship
		for (int r = 0; r < computerBoard.length; r++) {
			for (int c = 0; c < computerBoard.length; c++) {
				for (int s = 0; s < ships.length; s++) {
					if (c == coordinateGuessed.getX() && r == coordinateGuessed.getY()) {
						if (computerBoard[r][c].getOccupation() == intToLetter(s)) {
							shipIndex = s;
							hit = true;
						}
					}
				}
			}
		}
		
		// if user's guess hit ship
		if (hit) {
			userShipHitCount[shipIndex]++;
			
			// if shot destroyed ship
			if (userShipHitCount[shipIndex] == ships[shipIndex].getLength()) {
				System.out.println("Sunk, " + ships[shipIndex].getName());
			}
			// if ship is shot but not sunk
			else {
				System.out.println("Hit, " + ships[shipIndex].getName());
			}
		}
		// if user's guess is a miss
		else {
			System.out.println("Miss");
		}
		
		gameOver = true;
		
		for (int s = 0; s < ships.length; s++) {
			// if any ships are not sunk yet
			if (userShipHitCount[s] != ships[s].getLength()) {
				gameOver = false;
			}
		}
		
		if (gameOver) {
			userWin = true;
		}
	}
	
	public static void main(String[] args) {
		
		// declaring variables
		
		ArrayList<Boolean> shotsHit = new ArrayList<Boolean>();
		int boardLength = 10;
		int numShips = 5;
		int longestShipLength = 5;
		int[] shipHitCount = new int[numShips];
		int[] computerShipHitCount = new int[numShips];
		int[] userShipHitCount = new int[numShips];
		
		Block[][] computerBoard = new Block[boardLength][boardLength];
		Ship[] ships = new Ship[numShips];
		char water = '-';
		char unknown = '?';
		
		ships[0] = new Ship("Carrier", 5);
		ships[1] = new Ship("Battleship", 4);
		ships[2] = new Ship("Cruiser", 3);
		ships[3] = new Ship("Submarine", 3);
		ships[4] = new Ship("Destroyer", 2);
		
		// place ships on board
		placeShips(computerBoard, ships, water);
		writeBoardToFile(computerBoard);
		
		Block[][] userBoard = initializeUserBoard(boardLength);
				
		Scanner scan = new Scanner(System.in);
		Block[][] userGuessesBoard = initializeUserBoard(boardLength);
		
		System.out.println("Welcome to Battleship!\n");
		
		int coinToss = (int) (Math.random()*2);
		
		if (coinToss == 0) {
			System.out.println("Coin toss has determined that the AI guesses first.");
		}
		else {
			System.out.println("Coin toss has determined that the user guesses first.");
		}
		
		// game in session
		while (!gameOver) {
			// computer's turn
			
			if (coinToss == 0) {
				XYCoordinate guess = expertGuess(userBoard, ships, shotsHit, computerShipHitCount, lastHit, numGuesses, unknown);
				System.out.println("Computer guessed: " + intToLetter(guess.getY()) + "" + (guess.getX() + 1));
				
				String shot;
				boolean validInput;
				
				// gather user input for ship hit/miss confirmation
				do {
					validInput = false;
					System.out.println("Please input name of ship (\"-\" for miss):");
					shot = scan.nextLine();
					
					if (shot.equals("-")) {
						validInput = true;
					}
					
					for (int s = 0; s < ships.length; s++) {
						if (shot.equals(ships[s].getName())) {
							validInput = true;
						}
						if (shot.equals(Character.toString(intToLetter(s)))) {
							validInput = true;
							shot = ships[s].getName();
						}
					}
				} while (validInput == false);
				
				// check computer's guess for ship hit/sunk/miss on user's board
				checkComputerGuess(userBoard, ships, shotsHit, computerShipHitCount, guess, shot);
				displayBoard(userBoard);
			}
			
			if (computerWin) {
				break;
			}
			
			// user's turn
			
			boolean validGuess;
			char userGuessY;
			int userGuessX = 0;
			
			// gather user input for user's guess
			do {
				validGuess = false;
				System.out.println("Please input your guess:");
				String userGuessXY = scan.nextLine();
				userGuessY = userGuessXY.charAt(0);
				for (int i = 0; i < 10; i++) {
					if (userGuessY == intToLetter(i)) {
						validGuess = true;
					}
				}
				if (userGuessXY.length() == 2) {
					userGuessX = Integer.parseInt(userGuessXY.substring(1));
				}
				else if (userGuessXY.length() == 3) {
					userGuessX = Integer.parseInt(userGuessXY.substring(1, 3));
					if (userGuessX > 10) {
						validGuess = false;
					}
				} else {
					validGuess = false;
				}
			} while (validGuess == false);
			
			XYCoordinate userGuess = new XYCoordinate();
			userGuess.setX(userGuessX - 1);
			userGuess.setY(letterToInt(userGuessY));
			
			// check user's guess for ship hit/sunk/miss on computer's board
			checkUserGuess(computerBoard, ships, userShipHitCount, userGuess);
			
			coinToss = 0;
			
			if (userWin) {
				break;
			}
		}
		
		// game over
		if (computerWin) {
			System.out.println("\nGAME OVER, you lose. Better luck next time!");
		}
		else if (userWin) {
			System.out.println("\nGAME OVER, you win! Congratulations!");
		}
		
	}

}