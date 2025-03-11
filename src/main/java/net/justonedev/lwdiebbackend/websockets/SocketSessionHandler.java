/* (C)2025 */
package net.justonedev.lwdiebbackend.websockets;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class handles Websocket Connections to the main dashboard and
 * handles refresh messages.
 */
@Slf4j
@EnableScheduling
public class SocketSessionHandler extends TextWebSocketHandler {
	private static final Logger logger = LoggerFactory.getLogger(SocketSessionHandler.class);

	private static final String DEFAULT_UUID = "00000000-0000-0000-0000-000000000000";

	public static boolean isDefaultString(String uuid) {
		return uuid != null && uuid.replace("0", "").equals("----");
	}

	/**
	 * The pairs of:<br/>
	 * - key: The id of the user that owns the currently viewed dashboard. Either
	 * own when logged in, or the one where the public dashboard is stored when
	 * viewing that.<br/>
	 * - value: Set of sessions that would need to be updated when the dashboard for
	 * that user is updated.
	 * <p>
	 * When the dashboard page is entered, the socket is entered here. When the site
	 * is exited (reloading would also count), the session is deleted. If the user's
	 * key cannot be found in the hashmap, iterate over all sets and try to remove
	 * the session. This should not worsen time complexity, since both extreme cases
	 * of 1 entry with n sockets and n entries with 1 socket would result in O(log
	 * n) time complexity for finding and removing.
	 * </p>
	 * <p>
	 * Excluded here are only users that have a dashboard saved locally, since this
	 * is for pushing changes when a dashboard in the database is modified. Users
	 * viewing the default dashboard, regardless of if they are logged in or not,
	 * will be in the default entry, since that is the dashboard they are viewing,
	 * and it is stored in the database for the default user.
	 * </p>
	 */
	private final Map<String, Set<WebSocketSession>> userSockets;

	/**
	 * A separate set for sockets from users who are logged in but do not yet have
	 * their own dashboard. They should still be grouped with their regular String
	 * for updates in theme and dashboard, but they should also receive update
	 * messages for the default String when showing the default dashboard.
	 */
	private final Set<WebSocketSession> defaultViewerUserSockets;

	/**
	 * Constructs a new SocketSessionHandler with a reference to the user sockets.
	 * This way, it is possible to track the data structure outside after creation,
	 * or have multiple handlers with the same storage.
	 *
	 * @param userSockets The user socket map. Stored as a reference.
	 */
	public SocketSessionHandler(Map<String, Set<WebSocketSession>> userSockets) {
		this.userSockets = userSockets;
		this.defaultViewerUserSockets = new HashSet<>();
		log.info("SocketSessionHandler initialized");
	}

	/**
	 * When a websocket is connected, this registers it into the internal maps to
	 * keep track when update commands are to be sent out.
	 *
	 * @param session The session.
	 */
	@Override
	public void afterConnectionEstablished(@NonNull WebSocketSession session) {
		String uuid = getUuid(session);
		boolean connectDefault = Boolean.parseBoolean(session.getAttributes().getOrDefault("connectDefault", "false").toString());

		// Store session
		userSockets.computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet()).add(session);
		if (!uuid.equals(DEFAULT_UUID) && connectDefault) {
			defaultViewerUserSockets.add(session);
		}

		if (userSockets.containsKey(uuid)) {
			log.info("User {} has {} sessions", uuid, userSockets.get(uuid).size());
		} else {
			log.info("User {} has no sessions", uuid);
		}

		log.info("User {} connected", uuid);
	}

	/**
	 * Closes a given session and removes it from the internal session handler
	 * storage.
	 *
	 * @param session The session to close.
	 * @param status  The CloseStatus to close the connection with internally.
	 * @throws IOException If an IOException is thrown while closing the session.
	 */
	@Override
	public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws IOException {
		String userUuid = getUuid(session);
		defaultViewerUserSockets.remove(session);
		if (!userSockets.containsKey(userUuid)) {
			for (Set<WebSocketSession> sessions : userSockets.values()) {
				sessions.remove(session);
				if (userSockets.get(userUuid) == null)
					continue;
				if (userSockets.get(userUuid).isEmpty()) {
					userSockets.remove(userUuid);
				}
			}
			return;
		}

		userSockets.get(userUuid).remove(session);
		session.close(status);
		log.info("User {} disconnected", userUuid);
	}

	/**
	 * Sends the command to refresh the page to all Sessions connected under the
	 * given String.
	 *
	 * @param userUuid The String.
	 */
	public void sendRefreshMessage(String userUuid) {
		sendSocketMessage(userUuid, formatCommand("refreshDashboard"));
	}

	private final HashMap<String, Timer> messageBuffer = new HashMap<>();

	/**
	 * Sends a message to all Sessions connected under the given String after a
	 * specified timeout and stores it using the combination of String and key. If a
	 * new message comes for the same combination of String and key within the
	 * specified time, the old message will not be sent and a new timer will start,
	 * which will send the message after the specified time.
	 *
	 * @param userUuid The String to send the message to.
	 * @param key      The buffer key.
	 * @param message  The message to send.
	 * @param timeout  The timeout.
	 */
	public void sendSocketMessageBuffered(String userUuid, String key, String message, int timeout) {
		String pair = "%s.%s".formatted(userUuid, key);
		Timer oldTimer = messageBuffer.get(pair);
		if (oldTimer != null) {
			oldTimer.cancel();
		}
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				sendSocketMessage(userUuid, message);
				messageBuffer.remove(pair);
			}
		}, timeout);
		messageBuffer.put(pair, timer);
	}

	/**
	 * Sends a message to all Sessions connected under the given String.
	 *
	 * @param userUuid The String.
	 * @param message  The message to send.
	 */
	private void sendSocketMessage(String userUuid, String message) {
		Set<WebSocketSession> sessions = userSockets.get(userUuid);

		logger.info("Sending message {} to user {} ({} sessions)", message, userUuid, sessions == null ? "null" : "" + sessions.size());

		if (userUuid.equals(DEFAULT_UUID)) {
			for (WebSocketSession s : defaultViewerUserSockets) {
				sendSingleSocketMessage(s, message);
			}
		}

		if (sessions != null) {
			for (WebSocketSession s : sessions) {
				sendSingleSocketMessage(s, message);
			}
			return;
		}
		sessions = userSockets.get(DEFAULT_UUID);
		if (sessions == null)
			return;
		if (sessions.isEmpty()) {
			userSockets.remove(DEFAULT_UUID);
			return;
		}
		Set<WebSocketSession> remove = new HashSet<>();
		for (WebSocketSession s : sessions) {
			if (!s.isOpen()) {
				remove.add(s);
				continue;
			}
			String uuid = getUuid(s);
			if (!uuid.equals(userUuid))
				continue;
			sendSingleSocketMessage(s, message);
		}
		sessions.removeAll(remove);
	}

	/**
	 * Sends a message to a given Websocket. Catches and logs IOExceptions.
	 *
	 * @param session The WebSocketSession.
	 * @param message The message to send.
	 */
	private void sendSingleSocketMessage(WebSocketSession session, String message) {
		if (!session.isOpen())
			return;
		try {
			session.sendMessage(new TextMessage(message));
		} catch (IOException e) {
			log.error("Failed to send message to {}: {}", session.getPrincipal(), e.getMessage());
		}
	}

	private String formatCommand(String command) {
		return formatPair("command", command);
	}

	private String formatPair(String key, String value) {
		return "{\"%s\": \"%s\"}".formatted(key, value);
	}

	@Scheduled(fixedRate = 29000)
	public void sendKeepAliveMessages() {
		// Iterate through all user sessions and send a keep-alive message
		userSockets.values().forEach((sessions) -> sessions.forEach(session -> sendSingleSocketMessage(session, formatPair("status", "alive"))));
	}

	/**
	 * Extracts the String from the given Session. If there is no identification,
	 * returns the default String.
	 *
	 * @param session The WebSocketSession
	 * @return The String of the User of this session.
	 */
	@NonNull private static String getUuid(@NonNull WebSocketSession session) {
		return session.getAttributes().getOrDefault("uuid", DEFAULT_UUID).toString();
	}

	/**
	 * Sends a theme selected message to the given user. Will send the message with
	 * a buffer time of 2500ms.
	 *
	 * @param userUuid The user to send the message to.
	 * @param theme    The selected theme.
	 */
	public void sendThemeSelected(String userUuid, String theme) {
		sendSocketMessageBuffered(userUuid, "theme", formatPair("selectedTheme", theme), 2500);
	}

	/**
	 * Sends a command properly formatted (JSON) to the given user.
	 *
	 * @param userUuid The user to send the command to.
	 * @param command  The command to send.
	 */
	public void sendCommand(String userUuid, String command) {
		sendSocketMessage(userUuid, formatCommand(command));
	}
}
