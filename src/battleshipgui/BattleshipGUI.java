/*
Project: Battleship
File: BattleshipGUI.java
Description: GUI Battleship Game Program
Date: May. 29, 2024 - Jun. 17, 2024
Author: Emily Au, Xuanyao Li
*/

package battleshipgui;

import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.util.Scanner;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class MainClass extends JFrame{
	static String filename = "BattleshipBoard.txt";
	static boolean gameOver = false;
	static XYCoordinate lastHit = new XYCoordinate();
	static int numGuesses = 0;
	static boolean targetMode = true;
	static boolean playerFirst = true;
	static String level = "";
	static String sequence = "";
	static String type = "";

	static ArrayList<Boolean> shotsHit = new ArrayList<Boolean>();
	static ArrayList<Integer> XGuesses = new ArrayList<Integer>();
	static ArrayList<Character> YGuesses = new ArrayList<Character>();
	ArrayList<XYCoordinate> coordinatesGuessed = new ArrayList<XYCoordinate>();
	static int[] shipHitCount = new int[5];
	static char unknown = '?';
	static int boardLength = 10;
	static int numShips = 5;
	static int longestShipLength = 5;
	static char water = '-';
	static Ship[] ships = new Ship[numShips];



	static ArrayList<JButton> AIButton = new ArrayList<JButton>();
	static ArrayList<JButton> playerButton = new ArrayList<JButton>();
	//	char[][] board = new char[boardLength][boardLength];
	static Block[][] computerBoard = new Block[boardLength][boardLength];
	static Block[][] userBoard = new Block[boardLength][boardLength];

	static double[][] aiLearningBoard = new double[boardLength][boardLength];
	public XYCoordinate computerGuess;
	
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
	 * Generates heat map/probability map for all ships that have not been sunk
	 *
	 * @param board - 2D Block array (board) that will be used assess probability
	 * @param ships - array of ships that will be checked for probability
	 * @param shipHitCount - integer array of number of shots that have hit each ship
	 * @param water - character to represent empty space
	 * @return 2D double array of total probability values
	 */
	public static double[][] generateOverallHeatMap(Block[][] board, char water, Ship[] ships) {
		double[][] heatMap = new double[board.length][board.length];
		for (int s = 0; s < ships.length; s++) {
			double[][] shipHeatMap = generateHeatMapPerShip(board, water, ships[s].getLength());
			for (int r = 0; r < board.length; r++) {
				for (int c = 0; c < board.length; c++) {
					heatMap[r][c] += shipHeatMap[r][c];
				}
			}
		}
		return heatMap;
	}
	
	/**
	 * Expert ship placement algorithm: uses random weighted placement to determine locations for placement
	 *
	 * @param board - 2D Block array (board) to place ships upon
	 * @param ships - array of ships that will be placed
	 * @param water - character to represent empty spaces
	 */
	public static void expertPlaceShips(Block[][] board, Ship[] ships, char water) {
		for (int r = 0; r < board.length; r++) {
			for (int c = 0; c < board.length; c++) {
				board[r][c] = new Block(r, intToLetter(c), water);
			}
		}

		double[][] heatMap = generateOverallHeatMap(board, water, ships);
		for (int r = 0; r < heatMap.length; r++) {
			for (int c = 0; c < heatMap.length; c++) {
				heatMap[r][c] = 1 / heatMap[r][c];
			}
		}

		int shipsPlaced = 0;
		while (shipsPlaced < 5) {
			boolean validPlacement = false;
			String orientation;
			int zeroOrOne = (int)(Math.random()*2);
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
						xIndex = weightedRandom(heatMap);
						yIndex = weightedRandom(heatMap);
						if (yIndex < board.length - ships[shipsPlaced].getLength()) {
							break;
						}
					}
					initialCoordinate = new XYCoordinate(yIndex, xIndex);

					for (int c = initialCoordinate.getX(); c < initialCoordinate.getX() + ships[shipsPlaced].getLength(); c++) {
						if (board[initialCoordinate.getY()][c].getOccupation() != water) {
							validPlacement = false;
						}
					}

					if (validPlacement) {
						for (int c = initialCoordinate.getX(); c < initialCoordinate.getX() + ships[shipsPlaced].getLength(); c++) {
							board[initialCoordinate.getY()][c].setOccupation(intToLetter(shipsPlaced));
						}
					}

				} else {
					while (true) {
						xIndex = weightedRandom(heatMap);
						yIndex = weightedRandom(heatMap);
						if (xIndex < board.length - ships[shipsPlaced].getLength()) {
							break;
						}
					}
					initialCoordinate = new XYCoordinate(yIndex, xIndex);

					for (int r = initialCoordinate.getY(); r < initialCoordinate.getY() + ships[shipsPlaced].getLength(); r++) {
						if (board[r][initialCoordinate.getX()].getOccupation() != water) {
							validPlacement = false;
						}
					}

					if (validPlacement) {
						for (int r = initialCoordinate.getY(); r < initialCoordinate.getY() + ships[shipsPlaced].getLength(); r++) {
							board[r][initialCoordinate.getX()].setOccupation(intToLetter(shipsPlaced));
						}
					}
				}

//					System.out.println(intToLetter(initialCoordinate.getY()) + "" + (initialCoordinate.getX() + 1) + " " + ships[shipsPlaced].getOrientation());

			} while(validPlacement != true);
			shipsPlaced++;
		}

		try(BufferedWriter writer = new BufferedWriter(new FileWriter(filename))){
			for(int i = 0; i < boardLength;i++) {
				for(int j = 0; j < boardLength; j++) {
					writer.write(board[i][j]+"");
				}
				writer.newLine();
			}
			System.out.println("The board written to the file");
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * simple guess: randomly generate a xy-coordinate which is not generated before
	 * @param board
	 * @param coordinatesGuessed
	 * @return the xy coordinate of guess
	 */
	public static XYCoordinate simpleGuess(Block[][] board, ArrayList<XYCoordinate> coordinatesGuessed) {
		int randomX = 0;
		int randomYIdx =0;
		char randomY = 0;
		XYCoordinate xyCoordinate;
		while (true) {
			randomX = (int) (Math.random()*10) + 1;
			randomYIdx = (int) (Math.random()*10);
			randomY = intToLetter(randomYIdx);
			xyCoordinate = new XYCoordinate(randomX, randomYIdx);
			if (!coordinatesGuessed.contains(xyCoordinate)){
				break;
			}
		}

		coordinatesGuessed.add(xyCoordinate);
		return xyCoordinate;
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

				// horizontal
				boolean validPlacement = true;

				for (int h = r; h < (r + shipLength); h++) {

					try {
						if (board[h][c].getOccupation() != water) {
							validPlacement = false;
						}
					} catch (ArrayIndexOutOfBoundsException e) {
						validPlacement = false;
					}

				}
				if (validPlacement) {
					for (int h = r; h < (r + shipLength); h++) {
						heatMap[h][c]++;
						sum++;
					}
				}

				// vertical
				validPlacement = true;

				for (int v = c; v < (c + shipLength); v++) {
					try {
						if (board[r][v].getOccupation() != water) {
							validPlacement = false;
						}
					} catch (ArrayIndexOutOfBoundsException e) {
						validPlacement = false;
					}

				}
				if (validPlacement) {
					for (int v = c; v < (c + shipLength); v++) {
						heatMap[r][v]++;
						sum++;
					}
				}
			}
		}

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
			if (ships[s].getLength() != shipHitCount[s]) {
				double[][] shipHeatMap = generateHeatMapPerShip(board, water, ships[s].getLength());
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
	 * Hunts a coordinate using the parity method: targets only certain coordinates in which ship of certain length must reside upon
	 *
	 * @param board - 2D Block array (board) to be assessed for guessing
	 * @param longestShipLength - target ship length of ship
	 * @return XY coordinate to be guessed
	 */
	public static XYCoordinate huntParity(Block[][] board, int longestShipLength) {
		XYCoordinate guessCoordinate = new XYCoordinate();

		while (true) {
			int randomX = (int) (Math.random()*10), randomYInt = (int) (Math.random()*10);
			char randomY = intToLetter(randomYInt);
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

		double[][] heatMap = generateOverallHeatMap(board, ships, shipHitCount, unknown);
		int guessX = 0;
		int guessY = 0;

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
		for (int s = 0; s < ships.length; s++) {
			if (targetShip == false) {
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
		
		// if only one shot of ship has been hit

		if ((firstShip.getX() == lastShip.getX()) && (firstShip.getY() == lastShip.getY())) {

			boolean canShootVertical = false;
			boolean canShootHorizontal = false;

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

			// shoot up
			if ((lastHitY != 0) && (board[lastHitY - 1][lastHitX].getOccupation() == unknown) && canShootVertical) {
				guessCoordinate.setX(lastHitX);
				guessCoordinate.setY(lastHitY - 1);
			}
			// shoot left
			else if ((lastHitX != 0) && (board[lastHitY][lastHitX - 1].getOccupation() == unknown) && canShootHorizontal) {
				guessCoordinate.setX(lastHitX - 1);
				guessCoordinate.setY(lastHitY);
			}
			// shoot down
			else if ((lastHitY != board.length) && (board[lastHitY + 1][lastHitX].getOccupation() == unknown) && canShootVertical) {
				guessCoordinate.setX(lastHitX);
				guessCoordinate.setY(lastHitY + 1);
			}
			// shoot right
			else if ((lastHitX != board.length) && (board[lastHitY][lastHitX + 1].getOccupation() == unknown) && canShootHorizontal) {
				guessCoordinate.setX(lastHitX + 1);
				guessCoordinate.setY(lastHitY);
			}
		}

		// if multiple segments of a ship has been hit

		// vertical - shoot above first ship segment
		if ((firstShip.getY() > 0) && firstShip.getX() == lastShip.getX() && (board[firstShip.getY() - 1][firstShip.getX()].getOccupation() == unknown)) {
			guessCoordinate.setX(firstShip.getX());
			guessCoordinate.setY(firstShip.getY() - 1);
		}
		// horizontal - shoot to the left of first ship segment
		else if ((firstShip.getX() > 0) && (firstShip.getY() == lastShip.getY()) && (board[firstShip.getY()][firstShip.getX() - 1].getOccupation() == unknown)) {
			guessCoordinate.setX(firstShip.getX() - 1);
			guessCoordinate.setY(firstShip.getY());
		}
		// vertical - shoot below last ship segment
		else if ((lastShip.getY() < board.length) && (firstShip.getX() == lastShip.getX()) && (board[lastShip.getY() + 1][lastShip.getX()].getOccupation() == unknown)) {
			guessCoordinate.setX(lastShip.getX());
			guessCoordinate.setY(lastShip.getY() + 1);
		}
		// horizontal - shoot to the right of last ship segment
		else if ((lastShip.getX() < board.length) && (firstShip.getY() == lastShip.getY()) && (board[lastShip.getY()][lastShip.getX() + 1].getOccupation() == unknown)) {
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
		if (targetMode) {
			int final_y = -1;
			int final_x = -1;
			double maxProbablity = -99999.9999;
			for(int y = 0; y < boardLength; y++){
				for(int x = 0; x < boardLength; x++){
					if (aiLearningBoard[y][x] > maxProbablity){
						maxProbablity = aiLearningBoard[y][x];
						final_y = y;
						final_x = x;
					}
				}
			}
	
			if (final_x == -1 || final_y == -1 || maxProbablity <= 0){
				final_y = (int) (Math.random()*10);
				final_x = (int) (Math.random()*10);
			}
			guessCoordinate.setX(final_x+1);
			guessCoordinate.setY(final_y);
		}
		else if (numGuesses < 10) {
			guessCoordinate = huntParity(board, 4);
		}
		else {
			guessCoordinate = huntHeatMap(board, ships, shipHitCount, unknown);
		}
		return guessCoordinate;
	}
	
	/**
	 * Simple ship placement algorithm: uses random placement to determine locations for placement
	 *
	 * @param board - 2D Block array (board) to place ships upon
	 * @param ships - array of ships that will be placed
	 * @param water - character to represent empty spaces
	 */
	public static void placeShipsSimple(Block[][] board, Ship[] ships, char water) {
		for (int r = 0; r < board.length; r++) {
			for (int c = 0; c < board.length; c++) {
				board[r][c] = new Block(r, intToLetter(c), water);
			}
		}

		int shipsPlaced = 0;
		while (shipsPlaced < 5) {
			boolean validPlacement = false;
			String orientation;
			int zeroOrOne = (int)(Math.random()*2);
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
					initialCoordinate = new XYCoordinate((int)(Math.random()*(board.length - ships[shipsPlaced].getLength())), (int)(Math.random()*board.length));

					for (int c = initialCoordinate.getX(); c < initialCoordinate.getX() + ships[shipsPlaced].getLength(); c++) {
						if (board[initialCoordinate.getY()][c].getOccupation() != water) {
							validPlacement = false;
						}
					}

					if (validPlacement) {
						for (int c = initialCoordinate.getX(); c < initialCoordinate.getX() + ships[shipsPlaced].getLength(); c++) {
							board[initialCoordinate.getY()][c].setOccupation(intToLetter(shipsPlaced));
						}
					}

				} else {
					initialCoordinate = new XYCoordinate((int)(Math.random()*board.length), (int)(Math.random()*(board.length - ships[shipsPlaced].getLength())));

					for (int r = initialCoordinate.getY(); r < initialCoordinate.getY() + ships[shipsPlaced].getLength(); r++) {
						if (board[r][initialCoordinate.getX()].getOccupation() != water) {
							validPlacement = false;
						}
					}

					if (validPlacement) {
						for (int r = initialCoordinate.getY(); r < initialCoordinate.getY() + ships[shipsPlaced].getLength(); r++) {
							board[r][initialCoordinate.getX()].setOccupation(intToLetter(shipsPlaced));
						}
					}
				}

				System.out.println(intToLetter(initialCoordinate.getY()) + "" + (initialCoordinate.getX() + 1) + " " + ships[shipsPlaced].getOrientation());

			} while(validPlacement != true);
			shipsPlaced++;
		}
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(filename))){
			for(int i = 0; i < boardLength;i++) {
				for(int j = 0; j < boardLength; j++) {
					writer.write(board[i][j].getOccupation()+" ");
				}
				writer.newLine();
			}
			System.out.println("The board written to the file");
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * To change the probability of every grid according to a miss
	 * @param x
	 * @param y
	 */
	public static void aiMissLearn(int x, int y){
		aiLearningBoard[y][x] = -9999.9999;
		int minShipLength = 100;
		for(Ship s : ships){
			if (s.getLength() < minShipLength){
				minShipLength = s.getLength();
			}
		}
		if (x + minShipLength < boardLength){
			for(int i = x; i < boardLength; i++){
				aiLearningBoard[y][i] -= 0.5;
			}
		}

		if (x - minShipLength < -1){
			for(int i = x; i >= 0; i--){
				aiLearningBoard[y][i] -= -0.5;
			}
		}

		if (y + minShipLength < boardLength){
			for(int i = y; i < boardLength; i++){
				aiLearningBoard[i][x] -= 0.5;
			}
		}

		if (y - minShipLength < -1){
			for(int i = y; i >= 0; i--){
				aiLearningBoard[i][x] -= -0.5;
			}
		}
	}

	/**
	 * To change the probability of every grid according to a hit
	 * @param ship
	 * @param x
	 * @param y
	 */
	public static void aiLearn(Ship ship, int x, int y){
		aiLearningBoard[y][x] = -9999.9999;
		int shipLength = ship.getLength();

		if (x + shipLength > boardLength){
			// do nothing
		} else {
			for(int i = 1; i < shipLength; i++){
				aiLearningBoard[y][x+i] += (1 / i);
			}
		}

		if (x - shipLength < 0){
			// do nothing
		} else {
			for(int i = 1; i < shipLength; i++){
				aiLearningBoard[y][x-i] += (1 / i);
			}
		}

		if (y + shipLength > boardLength){
			// do nothing
		} else {
			for(int i = 1; i < shipLength; i++){
				aiLearningBoard[y+i][x] += (1 / i);
			}
		}

		if (y - shipLength < 0 ){
			// do nothing
		} else {
			for(int i = 1; i < shipLength; i++){
				aiLearningBoard[y-i][x] += (1 / i);
			}
		}
	}
	/**
	 * Updates statistics based on whether the AI's guess was a hit or a miss
	 *
	 * @param unknownBoard - 2D Block array (board) that computer guesses from
	 * @param ships - array of ships to be hunted
	 * @param shotsHit - boolean array to track whether AI's guesses are hits or misses
	 * @param shipHitCount - integer array of number of shots that have hit each ship
	 * @param coordinateShot - coordinate that AI guessed to shot at
	 * @param shot - whether AI's shot was a hit or miss
	 */

	public static void checkComputerGuess(Block[][] unknownBoard, Ship[] ships, ArrayList<Boolean> shotsHit, int[] shipHitCount, XYCoordinate coordinateShot, String shot) {
		int hitX = coordinateShot.getX();
		int hitY = coordinateShot.getY();
		String type = "---";

		if (!(shot.equals("Miss"))) {
			shotsHit.add(true);
			lastHit.setX(hitX);
			lastHit.setY(hitY);

			int shipIndex = 0;
	
			for (int s = 0; s < ships.length; s++) {
				if (shot.equals(ships[s].getName())) {
					//unknownBoard[hitY][hitX].setOccupation(intToLetter(s));

					type = String.valueOf(intToLetter(s));
					shipHitCount[s]++;
					shipIndex = s;
					aiLearn(ships[s], hitX-1, hitY);
				}
			}

			if (shipHitCount[shipIndex] == ships[shipIndex].getLength()) {
				targetMode = false;

				for (int s = 0; s < ships.length; s++) {
					if (shipHitCount[s] > 0 && shipHitCount[s] < ships[s].getLength()) {
						targetMode = false;
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
				targetMode = true;
			}
			playerButton.get(hitY * boardLength + hitX - 1 ).setText(type);
		}
		else {
			aiMissLearn(hitX-1, hitY);
			lastHit.setX(hitX);
			lastHit.setY(hitY);
			shotsHit.add(false);
			unknownBoard[coordinateShot.getY()][coordinateShot.getX()].setOccupation('-');
			//playerButton.get(coordinateShot.getY() * boardLength + coordinateShot.getX() - 1 ).setText("---");
			playerButton.get(hitY * boardLength + hitX - 1 ).setText(type);
		}
		
		numGuesses++;
	}




/**
 * The method checks whether the guess hits a ship and returns the index of the hit ship if successful
 * @param computerBoard
 * @param ships
 * @param coordinateGuessed
 * @return The index of the ship in the ship[]
 */
	public static int checkUserGuess(Block[][] computerBoard, Ship[] ships, XYCoordinate coordinateGuessed) {
		int shipIndex = -1;
		boolean hit = false;

		int userX = coordinateGuessed.getX() - 1;
		int userY = coordinateGuessed.getY();

		String computerItem = String.valueOf(computerBoard[userY][userX].getOccupation());

		if (computerItem.equals("-")){
			return shipIndex;
		} else {
			char c = computerBoard[userY][userX].getOccupation();
			computerBoard[userY][userX].setIsShot(true);
			shipIndex = c - 'A';
		}
		return shipIndex;
	}


	char[] yAxisList = {'A','B','C','D','E','F','G','H','I','J'};

	JPanel mainPanel;

	JPanel titlePanel;
	JLabel titleLabel;
	JPanel levelPanel;
	JLabel levelLabel;
	JComboBox levelBox;
	JPanel sequencePanel;
	JLabel sequenceLabel;
	JComboBox sequenceBox;

	JPanel boardPanel;
	JPanel AIBoardPanel;
	JPanel playerBoardPanel;


	JPanel informationPanel;
	JLabel AILabel;
	JLabel playerLabel;
	JLabel AIGuessLabel;
	JLabel playerGuessLabel;

	JPanel inputPanel;
	JPanel inputPanel1;
	JLabel xLabel;
	static JTextField xInput;
	JLabel yLabel;
	static JTextField yInput;
	JButton enterButton;
	JLabel typeLabel;
	JComboBox typeBox;
	JLabel AIGuess;

	JPanel resultPanel;
	static JLabel resultLabel;

	public JPanel getAIBoardPanel() {
		return this.AIBoardPanel;
	}
	public JPanel getPlayerBoardPanel() {
		return this.playerBoardPanel;
	}
	/**
	 * Setup the GUI
	 */
	public void setUp() {
		ships[0] = new Ship("Carrier", 5);
		ships[1] = new Ship("Battleship", 4);
		ships[2] = new Ship("Cruiser", 3);
		ships[3] = new Ship("Submarine", 3);
		ships[4] = new Ship("Destroyer", 2);
		AIBoardPanel.setLayout(new GridLayout(boardLength+1,boardLength+1));
		playerBoardPanel.setLayout(new GridLayout(boardLength+1,boardLength+1));
		for(int i = 0; i < boardLength+1; i++) {
			for(int j = 0; j < boardLength+1; j++) {
				if(i == 0 && j == 0) {

					AIBoardPanel.add(new JLabel(""));
					playerBoardPanel.add(new JLabel(""));
				}
				else if(i == 0) {
					AIBoardPanel.add(new JLabel(Integer.toString(j)));
					playerBoardPanel.add(new JLabel(Integer.toString(j)));
				}
				else if(j == 0){
					JLabel label1 = new JLabel(Character.toString(yAxisList[i-1]));
					label1.setHorizontalAlignment(SwingConstants.RIGHT);
					AIBoardPanel.add(label1);
					JLabel label2 = new JLabel(Character.toString(yAxisList[i-1]));
					label2.setHorizontalAlignment(SwingConstants.RIGHT);
					playerBoardPanel.add(label2);
				}
				else {


					JButton AIBlockButton = new JButton();
					JButton playerBlockButton = new JButton();
					AIButton.add(AIBlockButton);
					playerButton.add(playerBlockButton);
					AIBoardPanel.add(AIBlockButton);
					playerBoardPanel.add(playerBlockButton);

				}
			}
		}

	}
/**
 * Initiate the GUI
 */
	public void initGui(){
		//initialize button
		enterButton = new JButton("Enter");
		//initialize textfield
		xInput = new JTextField(5);
		xInput.setText("0");
		yInput = new JTextField(5);
		yInput.setText("0");
		//initialize combo
		levelBox = new JComboBox<>(new String[] {"","Easy","Hard"});
		sequenceBox = new JComboBox<>(new String[] {"","AI","Player"});
		typeBox = new JComboBox<>(new String[] {"","Miss","Carrier","Battleship","Cruiser","Submarine","Destroyer"});
		//initialize labels
		titleLabel = new JLabel("Battleship");
		levelLabel = new JLabel("AI Difficulty:");
		sequenceLabel = new JLabel("Who plays first:");
		AILabel = new JLabel("AI Board");
		playerLabel = new JLabel("Your Board");
		playerGuessLabel = new JLabel("Round: ");
		xLabel = new JLabel("");
		yLabel = new JLabel("");
		typeLabel = new JLabel("Ship:");
		AIGuess = new JLabel("AI guesses ");
		resultLabel = new JLabel("Result:");

		//panels layout
		titlePanel = new JPanel();
		titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.PAGE_AXIS));
		levelPanel = new JPanel();
		levelPanel.setLayout(new FlowLayout());
		sequencePanel = new JPanel();
		sequencePanel.setLayout(new FlowLayout());
		boardPanel = new JPanel();
		boardPanel.setLayout(new GridLayout(1,2));
		AIBoardPanel = new JPanel();
		playerBoardPanel = new JPanel();
		informationPanel = new JPanel();
		informationPanel.setLayout(new GridLayout(2,2));
		inputPanel = new JPanel();
		inputPanel.setLayout(new FlowLayout());
		inputPanel1 = new JPanel();
		inputPanel1.setLayout(new FlowLayout());
		resultPanel = new JPanel();
		resultPanel.setLayout(new FlowLayout());
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.PAGE_AXIS));

		//add labels,textfield and buttons to panels
		titlePanel.add(titleLabel);
		titlePanel.add(levelPanel);
		titlePanel.add(sequencePanel);
		levelPanel.add(levelLabel);
		levelPanel.add(levelBox);




		typeBox.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				type =  (String)typeBox.getSelectedItem();
				checkComputerGuess(userBoard, ships, shotsHit, shipHitCount, computerGuess, type);
			}

		});
		levelBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				int num = 0;
				XYCoordinate guess;
				Block[][] userBoard = initializeUserBoard(boardLength);
				String level = (String)levelBox.getSelectedItem();




			}

		});

		sequencePanel.add(sequenceLabel);
		sequencePanel.add(sequenceBox);

		sequenceBox.addActionListener(new ActionListener() {

			@Override
			
			 
			public void actionPerformed(ActionEvent e) {
				String level = (String)levelBox.getSelectedItem();
				System.out.println(level);
				String selected = (String) sequenceBox.getSelectedItem();

				if (selected.equals("AI")){
					//System.out.println("run yes");
					playerFirst = false;
					if (level.equals("Difficult")){
						//System.out.println("run yes");
						computerGuess = expertGuess(userBoard, ships, shotsHit, shipHitCount, lastHit, numGuesses, unknown);
						System.out.println("diff run");
					} else {
						//System.out.println("run yes"); output
						System.out.println(level); //level is ""
						computerGuess = simpleGuess(userBoard, coordinatesGuessed);
					}
					System.out.println(level+1);
					System.out.println(computerGuess);
					AIGuess.setText("AI guesses " + computerGuess.toString());
					playerGuessLabel.setText("ROUND: " + String.valueOf(numGuesses));
				}
			}
		});


		boardPanel.add(AIBoardPanel);
		boardPanel.add(playerBoardPanel);

		informationPanel.add(AILabel);
		informationPanel.add(playerLabel);
		informationPanel.add(playerGuessLabel);

		inputPanel.add(xLabel);
		inputPanel.add(yLabel);
		inputPanel.add(yInput);
		inputPanel.add(xInput);
		inputPanel.add(enterButton);
		
		resultPanel.add(resultLabel);
		
		inputPanel1.add(AIGuess);
		inputPanel1.add(typeLabel);
		inputPanel1.add(typeBox);

		mainPanel.add(titlePanel);
		mainPanel.add(boardPanel);
		mainPanel.add(informationPanel);
		mainPanel.add(inputPanel);
		mainPanel.add(inputPanel1);
		mainPanel.add(resultPanel);
		enterButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int shipIndex;
				String xValue = xInput.getText();
				String yValue = yInput.getText();
				//int xInt = Integer.parseInt(xValue);
				//int yInt = Integer.parseInt(yValue);
				XYCoordinate userGuess = new XYCoordinate();

				userGuess.setX(Integer.parseInt(xValue));
				userGuess.setY(letterToInt(yValue.charAt(0)));

				shipIndex = checkUserGuess(computerBoard, ships, userGuess);

				if(shipIndex!=-1) {
					resultLabel.setText("Result: HIT, "+ships[shipIndex]);
					gameOver = checkUserFinish(computerBoard);
				}
				else {
					resultLabel.setText("Result: MISS");
					//playerButton.get(Integer.parseInt(yValue) * boardLength + Integer.parseInt(xValue) - 1 ).setText("---");
					//playerButton.get(xInt*userBoard.length+yInt).setText("-");
				}

				if (gameOver){
					resultLabel.setText("!GAME OVER!");
				}
				level = (String)levelBox.getSelectedItem();
				if (level.equals("Difficult")){
					computerGuess = expertGuess(userBoard, ships, shotsHit, shipHitCount, lastHit, numGuesses, unknown);
				} else {
					computerGuess = simpleGuess(userBoard, coordinatesGuessed);
				}
				System.out.println(computerGuess);
				AIGuess.setText("AI guess:" + computerGuess.toString());
				playerGuessLabel.setText("ROUND: " + String.valueOf(numGuesses));
			}
		});
		setUp();
		add(mainPanel);
		setSize(1400,1200);
		setTitle("Battleship Game");
		setVisible(true);


	}
/**
 * If the game is over
 * @param computerBoard
 * @return gameOver(boolean)
 */
	public boolean checkUserFinish(Block[][] computerBoard){
		for(Block[] blocks : computerBoard){
			for(Block block : blocks){
				if(block.getOccupation()!='-' && block.getIsShot()==false){
					return false;
				}
			}
		}
		return true;
	}
	public MainClass()  {
		ArrayList<Boolean> shotsHit = new ArrayList<Boolean>();
		ArrayList<Integer> XGuesses = new ArrayList<Integer>();
		ArrayList<Character> YGuesses = new ArrayList<Character>();
		int[] shipHitCount = new int[5];
		char unknown = '?';

		int boardLength = 10;
		int numShips = 5;
		int longestShipLength = 5;
		char water = '-';
//	char[][] board = new char[boardLength][boardLength];

		Ship[] ships = new Ship[numShips];
		ships[0] = new Ship("Carrier", 5);
		ships[1] = new Ship("Battleship", 4);
		ships[2] = new Ship("Cruiser", 3);
		ships[3] = new Ship("Submarine", 3);
		ships[4] = new Ship("Destroyer", 2);

		// setUp();

		computerBoard = initializeUserBoard(boardLength);
		placeShipsSimple(computerBoard, ships, water);


		userBoard = initializeUserBoard(boardLength);

//
		for(int a = 0; a < boardLength; a++){
			aiLearningBoard[a] = new double[boardLength];
			for(int b = 0; b < boardLength; b++){
				aiLearningBoard[a][b] = 0.0;
			}
		}



	}




	public static void main(String[] args) {

		MainClass main = new MainClass();
		main.initGui();




	}
	
	
}