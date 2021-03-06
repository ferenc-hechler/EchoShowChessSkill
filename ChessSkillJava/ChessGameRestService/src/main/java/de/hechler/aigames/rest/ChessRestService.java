/**
 * Diese Datei ist Teil des Alexa Skills 'Carballo Chess'.
 * Copyright (C) 2016-2017 Ferenc Hechler (github@fh.anderemails.de)
 *
 * Der Alexa Skills 'Carballo Chess' ist Freie Software: 
 * Sie koennen es unter den Bedingungen
 * der GNU General Public License, wie von der Free Software Foundation,
 * Version 3 der Lizenz oder (nach Ihrer Wahl) jeder spaeteren
 * veroeffentlichten Version, weiterverbreiten und/oder modifizieren.
 *
 * Der Alexa Skills 'Carballo Chess' wird in der Hoffnung, 
 * dass es nuetzlich sein wird, aber
 * OHNE JEDE GEWAEHRLEISTUNG, bereitgestellt; sogar ohne die implizite
 * Gewaehrleistung der MARKTFAEHIGKEIT oder EIGNUNG FUER EINEN BESTIMMTEN ZWECK.
 * Siehe die GNU General Public License fuer weitere Details.
 * 
 * Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
 * Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.
 */
package de.hechler.aigames.rest;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import de.hechler.aigames.ai.AIGame;
import de.hechler.aigames.ai.GameRepository;
import de.hechler.aigames.ai.connectfour.ChessField;
import de.hechler.aigames.ai.connectfour.ChessGame;
import de.hechler.aigames.ai.connectfour.ChessImpl;
import de.hechler.aigames.api.GenericResult;
import de.hechler.aigames.api.GetGameDataResult;
import de.hechler.aigames.api.GetGameParameterResult;
import de.hechler.aigames.api.NewGameResult;
import de.hechler.aigames.api.ResultCodeEnum;
import de.hechler.aigames.api.fieldview.ChessFieldView;
import de.hechler.aigames.api.move.ChessMove;
import de.hechler.aigames.rest.ImageRegistry.ImageEnum;
import de.hechler.aigames.rest.ImageRegistry.SessionEntry;
import de.hechler.utils.RandUtils;
import de.hechler.utils.TemporaryStore;

//@WebServlet(urlPatterns = "/main", loadOnStartup = 1) 
public class ChessRestService extends HttpServlet {
	
	private static final String RX_MOVE = "^([^-]*)-([^:]*):([^-]*)-(.*)$";

	/** the svuid */ private static final long serialVersionUID = -3679002890645814953L;

	private final static Logger logger = Logger.getLogger(ChessRestService.class.getName());

	private static final int DEFAULT_AI_LEVEL = 2;

	private static boolean debugloggingEnabled = Boolean.getBoolean("chessrest.debugging");
	
	public static ChessImpl chessImpl = new ChessImpl();

	public static TemporaryStore<String, String> tempGameResult = new TemporaryStore<>(10000); 
	
	public Gson gson;
	
	@Override
	public void init() throws ServletException {
		super.init();
		gson = new Gson();
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {
	    doPost(request, response);
	}
	 
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {
		try {
			String responseString;
			
		    PrintWriter writer = response.getWriter();

		    if (!checkAuth(request)) {
				responseString = unauthResponse(response);
				writer.println(responseString);
				return;
			}
			
		    HttpSession session = request.getSession(true);
		    String sessionId = session.getId();
		    
			String gameId = normalizeGameId(request.getParameter("gameId"));
			String cmd = request.getParameter("cmd");
			String param1 = request.getParameter("param1");
			String param2 = request.getParameter("param2");
			
			switch(cmd) {
			case "enableDebugLogging": {
				responseString = enableDebugLogging(param1);
				break;
			}
			case "connect": {
				responseString = connect(param1, param2);
				break;
			}
			case "doMove": {
				responseString = doMove(gameId, param1);
				break;
			}
			case "doAIMove": {
				responseString = doAIMove(gameId);
				break;
			}
			case "rollback": {
				responseString = rollback(gameId, param1);
				break;
			}
			case "getGameData": {
				responseString = getGameData(gameId);
				break;
			}
			case "restartGame": {
				responseString = restartGame(gameId);
				break;
			}
			case "closeGame": {
				responseString = closeGame(gameId);
				break;
			}
			case "initTests": {
				responseString = initTests(param1);
				break;
			}
			case "setPlayerNames": {
				responseString = setPlayerNames(gameId, param1, param2);
				break;
			}
			case "setAILevel": {
				responseString = setAILevel(gameId, param1);
				break;
			}
			case "connectImage": {
				responseString = connectImage(gameId, param1);
				break;
			}

			// CLIENT
			case "clearSession": {
				responseString = clearSession(request.getSession());
				break;
			}
			case "getImage": {
				responseString = getImage(session, param1, false);
				break;
			}
			case "getImageEN": {
				responseString = getImage(session, param1, true);
				break;
			}
			case "hasChanges": {
				responseString = hasChanges(request.getSession(), param1);
				break;
			}
			case "clientGetGameData": {
				responseString = clientGetGameData(request.getSession());
				break;
			}
			
			default: {
				responseString = gson.toJson(GenericResult.genericUnknownCommandResult);
				response.setStatus(500);
				break;
			}
			}

			response.setCharacterEncoding(StandardCharsets.UTF_8.name());
			response.setContentType("application/json");
			
			response.setHeader("Expires", "Sat, 6 May 1995 12:00:00 GMT");
			response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
			response.addHeader("Cache-Control", "post-check=0, pre-check=0");
			response.setHeader("Pragma", "no-cache");
			
		    responseString = encode(responseString); 
	    	writer.println(responseString);
	    	if (debugloggingEnabled) {
	    		if (!(responseString.contains("S_NO_CHANGES"))) {
	    			logger.info("RQ[cmd="+cmd+",gid="+gameId+",p1="+param1+",p2="+param2+",S#"+sessionId+"] -> "+responseString);
	    		}
	    	}
	    }
		catch (RuntimeException | IOException e) {
			logger.log(Level.SEVERE, e.toString(), e);
			throw e;
		}
	}

	
	private String normalizeGameId(String gameId) {
		if (gameId == null) {
			return null;
		}
		return gameId.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]*", "");
	}

	/* ======== */
	/* COMMANDS */
	/* ======== */

	
	private String enableDebugLogging(String value) {
		debugloggingEnabled = Boolean.parseBoolean(value);
		return gson.toJson(GenericResult.genericOkResult);
	}

	
	private String initTests(String initParams) {
		if (initParams.matches("SEED[(]([0-9]+)[)]")) {
			long seed = Long.parseLong(initParams.replaceFirst("SEED[(]([0-9]+)[)]", "$1"));
			System.out.println("setPRNG("+seed+")");
			chessImpl.shutdown();
			RandUtils.setPRNG(seed);
			ChessField.testRandom = RandUtils.createPRNG(seed);
			ChessGame.testRandom = RandUtils.createPRNG(seed);
			AIGame.testRandom = RandUtils.createPRNG(seed);
			GameRepository.testRandom = RandUtils.createPRNG(seed);
			String rand9999 = Integer.toString(RandUtils.randomInt(10000));
			return gson.toJson(new GetGameParameterResult(ResultCodeEnum.S_OK, rand9999));
		}
		return gson.toJson(GenericResult.genericInvalidParameterResult);
	}
	
	private String restartGame(String gameId) {
		return gson.toJson(chessImpl.restart(gameId)); 
	}

	private String closeGame(String gameId) {
		String lastGameData = gson.toJson(chessImpl.getGameData(gameId));
		tempGameResult.put(gameId, lastGameData);
		return gson.toJson(chessImpl.closeGame(gameId));
	}

	private String clearSession(HttpSession session) {
		if (session != null) {
			session.invalidate();
			logger.info("session cleared");
		}
		return gson.toJson(GenericResult.genericOkResult);
	}

	private String connectImage(String gameId, String imageName) {
		ImageEnum image;
		try {
			image = ImageEnum.valueOf(imageName.toUpperCase());
		} catch (Exception e) {
			return gson.toJson(GenericResult.genericInvalidParameterResult);
		}
		SessionEntry entry = ImageRegistry.getInstance().getSessionEntry(image);
//		logger.info("connectImage(" + gameId + ", " + imageName + " - " + entry + ")");
		if (entry == null) {
			return gson.toJson(new GenericResult(ResultCodeEnum.E_IMAGE_NOT_FOUND));
		}
		if (!entry.checkLastQueryTime(3500)) {
			return gson.toJson(new GenericResult(ResultCodeEnum.E_IMAGE_NOT_FOUND));
		}
		entry.gameId = gameId;
//		logger.info("connectImage(" + entry.image.name() + ": " + entry.sessionId + " -> " + entry.gameId + ")");
		return gson.toJson(GenericResult.genericOkResult);
	}
	
	private String getImage(HttpSession session, String cntStr, boolean english) {
		int cnt;
		try {
			cnt = Integer.parseInt(cntStr);
		} catch (NumberFormatException | NullPointerException e) {
			return gson.toJson(GenericResult.genericInvalidParameterResult);
		}
		String sessionId = session.getId();
//		logger.info("SESSIONID: " + sessionId);
		if (cnt >= 30) {
			ImageRegistry.getInstance().freeSession(sessionId);
			return gson.toJson(GenericResult.genericTimeoutResult);
		}
		SessionEntry entry = ImageRegistry.getInstance().getSessionEntry(sessionId);
		if (entry.gameId != null) {
			return gson.toJson(new NewGameResult(ResultCodeEnum.S_ACTIVATED, entry.gameId));
		}
		entry.updateLastQueryTime();
		Object sessionImageName = session.getAttribute("IMAGE");
		if (entry.image.name().equals(sessionImageName)) {
			return gson.toJson(GenericResult.genericNoChangeResult);
		}
		session.setAttribute("IMAGE", entry.image.name());
		return gson.toJson(new GetImageResult(ResultCodeEnum.S_OK, entry.image, english));
	}

	private String setPlayerNames(String gameId, String player1Name, String player2Name) {
		return gson.toJson(chessImpl.setPlayerNames(gameId, player1Name, player2Name));
	}

	private String setAILevel(String gameId, String aiLevelName) {
		try { 
			int aiLevel = Integer.parseInt(aiLevelName);
			return gson.toJson(chessImpl.setAILevel(gameId, aiLevel));
		}
		catch (NumberFormatException e) {
			return gson.toJson(GenericResult.genericInvalidParameterResult);
		} 
	}

	private String doMove(String gameId, String moveText) {
		try { 
			String move = parseMoveText(moveText);
			return gson.toJson(chessImpl.doMove(gameId, new ChessMove(move)));
		}
		catch (RuntimeException e) {
			return gson.toJson(GenericResult.genericInvalidParameterResult);
		}
	}

	private String parseMoveText(String moveText) {
		if (!moveText.matches(RX_MOVE)) {
			return null;
		}
		String from_col = moveText.replaceFirst(RX_MOVE, "$1?").toLowerCase();
		String from_row = moveText.replaceFirst(RX_MOVE, "$2?");
		String to_col = moveText.replaceFirst(RX_MOVE, "$3?").toLowerCase();
		String to_row = moveText.replaceFirst(RX_MOVE, "$4?");
		String result = from_col.substring(0, 1) + from_row.substring(0, 1)+to_col.substring(0, 1) + to_row.substring(0, 1); 
		return result;
	}

	private String doAIMove(String gameId) {
		return gson.toJson(chessImpl.doAIMove(gameId));
	}

	private String rollback(String gameId, String numberOfHalfMoves) {
		try { 
			int numHalfMoves = Integer.parseInt(numberOfHalfMoves);
			return gson.toJson(chessImpl.rollback(gameId, numHalfMoves));
		}
		catch (NumberFormatException e) {
			return gson.toJson(GenericResult.genericInvalidParameterResult);
		} 
	}


	private String hasChanges(HttpSession session, String versionStr) {
		if (session == null) {
			return gson.toJson(GenericResult.genericTimeoutResult);
		}
		int version;
		try {
			version = Integer.parseInt(versionStr);
		} catch (NumberFormatException | NullPointerException e) {
			return gson.toJson(GenericResult.genericInvalidParameterResult);
		}
		String sessionId = session.getId();
		SessionEntry entry = ImageRegistry.getInstance().getSessionEntry(sessionId);
//		logger.info("hasChanges(" + sessionId + ", " + versionStr + " - " + entry + ")");
		if (entry == null) {
			return gson.toJson(GenericResult.genericTimeoutResult);
		}
		if (entry.gameId == null) {
			return gson.toJson(GenericResult.genericUnknownGameId);
		}
		if (tempGameResult.containsKey(entry.gameId)) {
			return gson.toJson(GenericResult.genericChangesExistResult);
		}
		return gson.toJson(chessImpl.hasChanges(entry.gameId, version));

	}
	
	private String clientGetGameData(HttpSession session) {
		if (session == null) {
			return gson.toJson(GenericResult.genericTimeoutResult);
		}
		String sessionId = session.getId();
		SessionEntry entry = ImageRegistry.getInstance().getSessionEntry(sessionId);
		if (entry == null) {
			return gson.toJson(GenericResult.genericTimeoutResult);
		}
		if (entry.gameId == null) {
			return gson.toJson(GenericResult.genericUnknownGameId);
		}
		String result = tempGameResult.get(entry.gameId);
		if (result != null) {
			tempGameResult.remove(entry.gameId);
			return result;
		}
		return gson.toJson(chessImpl.getGameData(entry.gameId));
	}

	private String getGameData(String gameId) {
		return gson.toJson(chessImpl.getGameData(gameId));
	}

	private String connect(String userId, String aiLevelStr) {
		int aiLevel = DEFAULT_AI_LEVEL;
		if ((aiLevelStr!=null) && aiLevelStr.matches("[1-7]")) {
			aiLevel = Integer.parseInt(aiLevelStr);
		}
		GetGameDataResult<ChessFieldView> getGameDataResult = chessImpl.getGameDataByUserId(userId);
		if (getGameDataResult.code != ResultCodeEnum.S_OK) {
			NewGameResult newGameResult = chessImpl.createNewGame(userId, aiLevel, true);
			String gameId = newGameResult.gameId;
			getGameDataResult = chessImpl.getGameData(gameId);
		}
		else {
			String gameId = getGameDataResult.gameId;
			if (aiLevel != getGameDataResult.aiLevel) {
				chessImpl.setAILevel(gameId, aiLevel);
				getGameDataResult.aiLevel = aiLevel;
			}
		}
		return gson.toJson(getGameDataResult);
	}

	
	/* ==== */
	/* AUTH */
	/* ==== */


	private final static String DEFAULT_AUTH = System.getProperty("chess.rest.auth", "rest:geheim");

	private boolean checkAuth(HttpServletRequest request) throws IOException {
		String cmd = request.getParameter("cmd");
		if ("clearSession".equals(cmd) || "getImage".equals(cmd)  || "getImageEN".equals(cmd) || "hasChanges".equals(cmd) || "clientGetGameData".equals(cmd)) {
			// allow client queries without auth
			return true;
		}

		String auth = request.getHeader("Authorization");
		if ((auth == null) || !auth.startsWith("Basic ")) {
			return false;
		}
		String userpass;
		try {
			userpass = new String(java.util.Base64.getDecoder().decode(auth.substring(6)), StandardCharsets.ISO_8859_1);   // ISO-8859-1 is the standard for basic auth.
		}
		catch (Exception e) {
			System.out.println("basic auth decode failed: "+e);
			return false;
		}
		boolean result = userpass.equals(DEFAULT_AUTH);
		return result;
	}

	private String unauthResponse(HttpServletResponse response) {
		response.setStatus(401);
		response.setHeader("WWW-Authenticate", "Basic realm=\"c4-rest-service\"");
		return "";
	}


	/* ======================= */
	/* INTERNAL HELPER METHODS */
	/* ======================= */
	

	private String encode(String text) {
		// TODO: look for encoding in REST Service.
		return text;
	}


}
