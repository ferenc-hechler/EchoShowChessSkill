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
package de.hechler.aigames.api.move;

import de.hechler.aigames.api.Move;

public class ChessMove extends Move {
	public String move;
	public boolean check;
	public ChessMove(String move) {
		this(move, false);
	}
	public ChessMove(String move, boolean check) {
		this.move = move;
		this.check = check;
	}

	public boolean isValid() {
		return (move != null) && move.matches("[a-h][1-8][a-h][1-8][rbnq]?");
	}
	
	public String getMove() {
		return move;
	}
	
	public String getFrom() {
		return move.substring(0, 2);
	}
	
	public String getTo() {
		return move.substring(2, 2);
	}
	
	public int getFromCol() {
		return move.charAt(0) - 'a';
	}
	
	public int getFromRow() {
		return move.charAt(1) - '1';
	}
	
	public int getToCol() {
		return move.charAt(2) - 'a';
	}
	
	public int getToRow() {
		return move.charAt(3) - '1';
	}

	public boolean isCheck() {
		return check;
	}
	
	public String getPromotion() {
		String result = (move.length() < 5) ? "" : move.substring(4, 5);
		if (getToRow() == 7) {
			result = result.toUpperCase();
		}
		return result;
	}
	
	@Override
	public String toString() {
		return "move="+move + (check?"#":"");
	}
}
