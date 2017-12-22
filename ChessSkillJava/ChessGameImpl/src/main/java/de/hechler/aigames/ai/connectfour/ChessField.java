/**
 * Diese Datei ist Teil des Alexa Skills Rollenspiel Soloabenteuer.
 * Copyright (C) 2016-2017 Ferenc Hechler (github@fh.anderemails.de)
 *
 * Der Alexa Skills Rollenspiel Soloabenteuer ist Freie Software: 
 * Sie koennen es unter den Bedingungen
 * der GNU General Public License, wie von der Free Software Foundation,
 * Version 3 der Lizenz oder (nach Ihrer Wahl) jeder spaeteren
 * veroeffentlichten Version, weiterverbreiten und/oder modifizieren.
 *
 * Der Alexa Skills Rollenspiel Soloabenteuer wird in der Hoffnung, 
 * dass es nuetzlich sein wird, aber
 * OHNE JEDE GEWAEHRLEISTUNG, bereitgestellt; sogar ohne die implizite
 * Gewaehrleistung der MARKTFAEHIGKEIT oder EIGNUNG FUER EINEN BESTIMMTEN ZWECK.
 * Siehe die GNU General Public License fuer weitere Details.
 * 
 * Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
 * Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.
 */
package de.hechler.aigames.ai.connectfour;

import java.util.Random;

import de.hechler.aigames.api.fieldview.ChessFieldView;
import de.hechler.aigames.api.move.ChessMove;
import de.hechler.utils.RandUtils;

public class ChessField {

	public final static int PLAYER_1 =  1;
	public final static int PLAYER_2 = -1;
	
	public static Random testRandom = null;

	private final static int rowSize = 8;
	private final static int colSize = 8;
	private ChessMove lastMove = null;
	private int lastRow = -1;
	private int lastCol = -1;

	private char[][] fieldArray;
	
	private final static String startPosition = "rnbqkbnr/pppppppp/11111111/11111111/11111111/11111111/PPPPPPPP/RNBQKBNR";
	
	
	public ChessField() {
		fieldArray = new char[rowSize][colSize];
		setField(startPosition);
	}
	
	private void setField(String fen) {
		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				fieldArray[row][col] = fen.charAt((7-row)*9+col); 
			}
		}
	}

	private ChessField(char[][] copyField) {
		this();
		for (int row=0; row<rowSize; row++) {
			for (int col=0; col<colSize; col++) {
				fieldArray[row][col] = copyField[row][col];
			}	
		}
	}
	
	
	public ChessField copy() {
		ChessField result = new ChessField(fieldArray);
		return result;
	}
	
	/**
	 * @param slot
	 * @param player
	 * @return false if slot is already filled
	 */
	public boolean setFieldArray(ChessMove move, int player) {
		// HOT-FIX
		if (player == -1) {
			player = 2;
		}
		if (!move.isValid()) {
			return false;
		}
		char cFrom = fieldArray[move.getFromRow()][move.getFromCol()];
		if (cFrom == '1') {
			return false;
		}
		fieldArray[move.getToRow()][move.getToCol()] = cFrom;
		fieldArray[move.getFromRow()][move.getFromCol()] = '1';
		lastMove = move;
		lastRow = move.getToRow();
		lastCol = move.getToCol();
		return true;
	}
	
	public int checkForWinner () {
		int result = 0;
//		result = IField.PLAYER_2;
//		result = IField.PLAYER_1;
    	return result;
	}	
	
	
	public int checkForEndGame() {
		int result = 0;
//		int value = calcPosition();
//		if (value < -50000)
//			result = 2;
//		else if (value > 50000)
//			result = 1;
//        else if (hasFreeSlot())
//        	result = 0;
//        else 
//        	result = -1;
    	return result;
	}	
	

	
	public String toString() {
		return toFEN();
	}
	
	public ChessMove getLastMove() {
		return lastMove;
	}

	public ChessFieldView createExportField() {
		String lastMoveText = lastMove == null ? null : lastMove.getMove();  
		ChessFieldView result = new ChessFieldView(toFEN(), lastMoveText);
		return result;
	}

	public String toFEN() {
		StringBuffer result = new StringBuffer();
		for (int row = 0; row < rowSize; row++) {
			if (row != 0) {
				result.append("/");
			}
			for (int col = 0; col < colSize; col++) {
				char element = fieldArray[7-row][col];
				result.append(element);
			}
		}
		return result.toString();
	}


//	public static void main(String[] args) {
//		int x;
//		Field first = new Field();
//		for (int k=1; k<=4; k++)
//		{
//			x = first.compiMove();
//			first.setFieldArray(x, 1);
//			x = first.compiMove();
//			first.setFieldArray(x, 2);
//		}
//		System.out.println("current field, rating=" + first.calc(Field.));
//		System.out.println(first.toString());
//		Field[] next = first.nextMoves(2);
//		for (int i = 0; i < next.length; i++) {
//			Field field = next[i];
//			System.out.println("move " + Integer.toString(i+1) + ", rating=" + field.calc());
//			System.out.println(field.toString());
//		}
//	}
}
	
		

