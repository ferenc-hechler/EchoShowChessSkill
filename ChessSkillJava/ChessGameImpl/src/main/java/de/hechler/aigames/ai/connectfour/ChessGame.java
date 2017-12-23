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

import java.util.ArrayList;
import java.util.List;

import com.alonsoruibal.chess.Config;
import com.alonsoruibal.chess.Move;
import com.alonsoruibal.chess.book.FileBook;
import com.alonsoruibal.chess.search.SearchEngineThreaded;
import com.alonsoruibal.chess.search.SearchObserver;
import com.alonsoruibal.chess.search.SearchParameters;
import com.alonsoruibal.chess.search.SearchStatusInfo;

import de.hechler.aigames.ai.AIGame;
import de.hechler.aigames.api.DoMoveResult;
import de.hechler.aigames.api.ResultCodeEnum;
import de.hechler.aigames.api.fieldview.ChessFieldView;
import de.hechler.aigames.api.move.ChessMove;


public class ChessGame extends AIGame<ChessFieldView, ChessMove> {
	
	private static final int DEFAULT_MOVETIME_MS = 1000;
	private static final int DEFAULT_ELO = 1000;
	protected ChessField field;
	private List<ChessMove> chessMoves;
	
	
	private static class BestMoveReceiver implements SearchObserver {
		private String ponderStr;
		private String bestMoveStr;
		
		@Override public void info(SearchStatusInfo arg0) {}
		
		@Override
		public void bestMove(int bestMove, int ponder) {
			bestMoveStr = Move.toString(bestMove);
			ponderStr = Move.toString(ponder);
			synchronized (this) {
				this.notify();
			}
		}

		public String getBestMoveStr() {
			return bestMoveStr;
		}
		public String getPonderStr() {
			return ponderStr;
		}
		
	}
	
	@Override
	public DoMoveResult<ChessMove> doMove(ChessMove move) {
		if (!move.isValid()) {
			return new DoMoveResult<ChessMove>(ResultCodeEnum.E_INVALID_RANGE);
		}
		if (hasWinner()) {
			return new DoMoveResult<ChessMove>(ResultCodeEnum.E_GAME_FINISHED);
		}
		boolean moveOK = field.setFieldArray(move, currentPlayer);
		if (!moveOK) {
			return new DoMoveResult<ChessMove>(ResultCodeEnum.E_INVALID_MOVE);
		}
		chessMoves.add(move);
		winner = field.checkForEndGame();
		if (winner == -1) {
			return new DoMoveResult<ChessMove>(ResultCodeEnum.S_DRAW);
		} else  if (winner != 0) {
			return new DoMoveResult<ChessMove>(ResultCodeEnum.S_PLAYER_WINS);
		}
		changePlayer();
		return new DoMoveResult<ChessMove>(ResultCodeEnum.S_OK);
	}
	
	@Override
	public DoMoveResult<ChessMove> doAIMove() {
		if (hasWinner()) {
			return new DoMoveResult<ChessMove>(ResultCodeEnum.E_GAME_FINISHED);
		}
		ChessMove aiMove = calcAIMove();
		field.setFieldArray(aiMove, currentPlayer);
		winner = field.checkForEndGame();
		if (winner == -1) {
			return new DoMoveResult<ChessMove>(ResultCodeEnum.S_AI_DRAW, aiMove);
		} else  if (winner != 0) {
			return new DoMoveResult<ChessMove>(ResultCodeEnum.S_AI_PLAYER_WINS, aiMove);
		}
		changePlayer();
		return new DoMoveResult<ChessMove>(ResultCodeEnum.S_OK, aiMove);
	}
	
	@Override
	public ChessMove calcAIMove() {
//		BestMoveReceiver bestMoveRcv = new BestMoveReceiver();
		Config config = new Config();
		config.setBook(new FileBook("/book_small.bin"));
		config.setElo(DEFAULT_ELO);
		SearchEngineThreaded engine = new SearchEngineThreaded(config);
//		engine.setObserver(bestMoveRcv);
		SearchParameters searchParameters = new SearchParameters();
		searchParameters.setMoveTime(DEFAULT_MOVETIME_MS);
		
		// new game
		engine.getBoard().startPosition();
		engine.clear();
		
		// replay
		for (ChessMove chessMove:chessMoves) {
			int move = Move.getFromString(engine.getBoard(), chessMove.getMove(), true);
			engine.getBoard().doMove(move);
		}

		// search
		engine.go(searchParameters);
		
		// wait for answer
		try {
			long timeout = System.currentTimeMillis() + DEFAULT_MOVETIME_MS;
			while (engine.isSearching() && timeout > System.currentTimeMillis()) {
				Thread.sleep(100);
			}
		} catch (InterruptedException e) {
			return null;
		}
		
		// stop
		engine.stop();
		
		String bestMove = Move.toString(engine.getBestMove());
		engine.destroy();
		ChessMove result = new ChessMove(bestMove);
		chessMoves.add(result);
		return result;
	} 
	
	
	@Override
	public ChessFieldView getField() {
		return field.createExportField();
	}

	@Override
	protected void createField() {
		field = new ChessField();
		chessMoves = new ArrayList<>();
	}


}
