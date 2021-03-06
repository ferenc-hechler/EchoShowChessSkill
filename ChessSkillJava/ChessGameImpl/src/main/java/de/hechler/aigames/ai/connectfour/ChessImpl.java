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



import de.hechler.aigames.ai.GameRepository;
import de.hechler.aigames.ai.GameRepository.GameState;
import de.hechler.aigames.api.DoMoveResult;
import de.hechler.aigames.api.GameAPI;
import de.hechler.aigames.api.GenericResult;
import de.hechler.aigames.api.GetGameDataResult;
import de.hechler.aigames.api.GetGameParameterResult;
import de.hechler.aigames.api.NewGameResult;
import de.hechler.aigames.api.ResultCodeEnum;
import de.hechler.aigames.api.fieldview.ChessFieldView;
import de.hechler.aigames.api.move.ChessMove;


public class ChessImpl implements GameAPI<ChessFieldView, ChessMove> {

	private static GameRepository<ChessGame> gameRepository = new GameRepository<ChessGame>(ChessGame.class);
	
	@Override
	public NewGameResult createNewGame(String userId, int aiLevel, boolean weak) {
		GameState<ChessGame> newGameState = gameRepository.createNewGame();
		String gameId = newGameState.getGameId();
		gameRepository.connectUser(gameId, userId);
		newGameState.getGame().setAILevel(aiLevel);
		newGameState.getGame().setWeak(weak);
		NewGameResult result = new NewGameResult(ResultCodeEnum.S_OK, gameId);
		return result;
	}

	@Override
	public GenericResult setPlayerNames(String gameId, String player1Name, String player2Name) {
		GameState<ChessGame> gameState = gameRepository.getGameStateByGameId(gameId);
		if (gameState == null) {
			return new GenericResult(ResultCodeEnum.E_UNKNOWN_GAMEID);
		}
		gameState.getGame().setPlayerNames(player1Name, player2Name);
		gameState.update();
		return new GenericResult(ResultCodeEnum.S_OK);
	}

	@Override
	public GenericResult setAILevel(String gameId, int aiLevel) {
		GameState<ChessGame> gameState = gameRepository.getGameStateByGameId(gameId);
		if (gameState == null) {
			return new GenericResult(ResultCodeEnum.E_UNKNOWN_GAMEID);
		}
		if ((aiLevel < 1) || (aiLevel > 7)) {
			return GenericResult.genericInvalidRangeResult;
		}
		gameState.getGame().setAILevel(aiLevel);
		gameState.update();
		return new GenericResult(ResultCodeEnum.S_OK);
	}

	@Override
	public GenericResult hasChanges(String gameId, int lastChange) {
		GameState<ChessGame> gameState = gameRepository.getGameStateByGameId(gameId);
		if (gameState == null) {
			return new GenericResult(ResultCodeEnum.E_UNKNOWN_GAMEID);
		}
		if (gameState.getVersion() == lastChange) {
			return new GenericResult(ResultCodeEnum.S_NO_CHANGES);
		}
		return new GenericResult(ResultCodeEnum.S_CHANGES_EXIST);
	}

	@Override
	public GetGameDataResult<ChessFieldView> getGameData(String gameId) {
		GameState<ChessGame> gameState = gameRepository.getGameStateByGameId(gameId);
		if (gameState == null) {
			return new GetGameDataResult<ChessFieldView>(ResultCodeEnum.E_UNKNOWN_GAMEID);
		}
		ChessGame game = gameState.getGame(); 
		GetGameDataResult<ChessFieldView> result = new GetGameDataResult<ChessFieldView>(ResultCodeEnum.S_OK);
		result.aiLevel = game.getAILevel();
		result.player1Name = game.getPlayer1Name();
		result.player2Name = game.getPlayer2Name();
		result.currentPlayer = game.getCurrentPlayer();
		result.gamePhase = game.getGamePhase();
		result.fieldView = game.getField();
		result.version = gameState.getVersion();
		result.gameId = gameState.getGameId();
		result.movesCount = game.getMovesCount();
		result.winner = game.getWinner();
		return result;
	}
	
	@Override
	public GetGameDataResult<ChessFieldView> getGameDataByUserId(String userId) {
		GameState<ChessGame> gameState = gameRepository.getGameStateByUserId(userId);
		if (gameState == null) {
			return new GetGameDataResult<ChessFieldView>(ResultCodeEnum.E_UNKNOWN_GAMEID);
		}
		ChessGame game = gameState.getGame(); 
		GetGameDataResult<ChessFieldView> result = new GetGameDataResult<ChessFieldView>(ResultCodeEnum.S_OK);
		result.aiLevel = game.getAILevel();
		result.player1Name = game.getPlayer1Name();
		result.player2Name = game.getPlayer2Name();
		result.currentPlayer = game.getCurrentPlayer();
		result.gamePhase = game.getGamePhase();
		result.fieldView = game.getField();
		result.version = gameState.getVersion();
		result.gameId = gameState.getGameId();
		result.movesCount = game.getMovesCount();
		result.winner = game.getWinner();
		return result;
	}



	@Override
	public DoMoveResult<ChessMove> doMove(String gameId, ChessMove move) {
		GameState<ChessGame> gameState = gameRepository.getGameStateByGameId(gameId);
		if (gameState == null) {
			return new DoMoveResult<ChessMove>(ResultCodeEnum.E_UNKNOWN_GAMEID);
		}
		DoMoveResult<ChessMove> result = gameState.getGame().doMove(move);
		gameState.update();
		return result;
	}

	@Override
	public DoMoveResult<ChessMove> doAIMove(String gameId) {
		GameState<ChessGame> gameState = gameRepository.getGameStateByGameId(gameId);
		if (gameState == null) {
			return new DoMoveResult<ChessMove>(ResultCodeEnum.E_UNKNOWN_GAMEID);
		}
		DoMoveResult<ChessMove> result = gameState.getGame().doAIMove();
		gameState.update();
		return result;
	}

	public GenericResult rollback(String gameId, int numHalfMoves) {
		GameState<ChessGame> gameState = gameRepository.getGameStateByGameId(gameId);
		if (gameState == null) {
			return new DoMoveResult<ChessMove>(ResultCodeEnum.E_UNKNOWN_GAMEID);
		}
		GenericResult result = gameState.getGame().rollback(numHalfMoves);
		gameState.update();
		return result;
	}

	@Override
	public GenericResult switchPlayers(String gameId) {
		GameState<ChessGame> gameState = gameRepository.getGameStateByGameId(gameId);
		if (gameState == null) {
			return new DoMoveResult<ChessMove>(ResultCodeEnum.E_UNKNOWN_GAMEID);
		}
		boolean ok = gameState.getGame().switchPlayers();
		if (!ok) {
			return new GenericResult(ResultCodeEnum.E_UNKNOWN_COMMAND);
		}
		gameState.update();
		return new GenericResult(ResultCodeEnum.S_OK);
	}

//	// initial moves to mate
//	private static String[] initialMoves = {
//			"e2e4",
//			"e7e5",
//			"f1c4",
//			"a7a6",
//			"d1f3",
//			"a6a5"
//	};
	
//	// initial moves to en-passant 
//	private static String[] initialMoves = {
//			"h2h4",
//			"h7h6",
//			"h4h5",
//			"f7f6",
//			"h1h2",
//			"g7g5",
//			// "h5g5"
//	};
	
//	// initial moves to rochade
//	private static String[] initialMoves = {
//			"d2d4",
//			"g8h6",
//			"d4d5",
//			"e7e5",
//			"d5e6",
//			"f8a3",
//			"b2a3",
//			// "e8g8"
//	};

//	// initial moves to pawn promotion 
//	private static String[] initialMoves = {
//			"a2a4",
//			"b7b5",
//			"a4b5",
//			"a7a5",
//			"b5a6",
//			"c8b7",
//			"a6b7",
//			"c7c6",
//			// "b7a8q"
//	};
	
	// no initial moves 
	private static String[] initialMoves = {
	};

	
	
	@Override
	public GenericResult restart(String gameId) {
		GameState<ChessGame> gameState = gameRepository.getGameStateByGameId(gameId);
		if (gameState == null) {
			return new GenericResult(ResultCodeEnum.E_UNKNOWN_GAMEID);
		}
		gameState.getGame().restartGame();
		for (String move:initialMoves) {
			doMove(gameId, new ChessMove(move));
		}
		gameState.update();
		return new GenericResult(ResultCodeEnum.S_OK);
	}

	@Override
	public GenericResult closeGame(String gameId) {
		GameState<ChessGame> gameState = gameRepository.getGameStateByGameId(gameId);
		if (gameState == null) {
			return new GenericResult(ResultCodeEnum.E_UNKNOWN_GAMEID);
		}
		gameRepository.removeGame(gameId);
		return new GenericResult(ResultCodeEnum.S_OK);
	}

	public void startup() {
	}
	public void shutdown() {
		gameRepository.close();
	}


	public GameState<ChessGame> findGameId(String gameId) {
		return gameRepository.getGameStateByGameId(gameId);
	}

	@Override
	public GenericResult changeGameParameter(String gameId, String gpName, String gpValue) {
		return GenericResult.genericInvalidParameterResult;
	}

	@Override
	public GetGameParameterResult getGameParameter(String gameId, String paramName) {
		return new GetGameParameterResult(ResultCodeEnum.E_INVALID_PARAMETER);
	}



}
