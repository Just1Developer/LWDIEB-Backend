/* (C)2025 */
package net.justonedev.lwdiebbackend.controller;

import net.justonedev.lwdiebbackend.Signature;
import net.justonedev.lwdiebbackend.websockets.SocketSessionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@EnableScheduling
public class WSPostController {
	private static final Logger logger = LoggerFactory.getLogger(WSPostController.class);
	@Autowired
	private SocketSessionHandler socketSessionHandler;
	private Signature signature;

	private static final int REQUEST_VALID_TIME = 120000; // 2 minutes

	@Value("${server.comms.password}")
	private String SERVER_PASSWORD;
	@Value("${server.signature.signature-secret}")
	private String SIGNATURE_SECRET;

	private final ConcurrentHashMap<String, Long> uuidRequestExpirationTime;

	public WSPostController() {
		uuidRequestExpirationTime = new ConcurrentHashMap<>();
	}

	@PostMapping("/ws-post")
	public ResponseEntity<Map<String, Object>> post(@RequestBody Map<String, String> requestBody) {
		if (signature == null)
			signature = new Signature(SIGNATURE_SECRET);

		String userId = requestBody.get("userId");
		String command = requestBody.get("command");
		String serverPassword = requestBody.get("serverPassword");
		String checksum = requestBody.get("checksum");
		String timestampStr = requestBody.get("timestamp");
		long timestamp;
		try {
			timestamp = Long.parseLong(Objects.requireNonNull(timestampStr));
		} catch (NumberFormatException e) {
			logger.info("Invalid timestamp from ws-post request. Timestamp: {}", timestampStr);
			return ResponseEntity.internalServerError().build();
		}
		String signedTime = requestBody.get("signedTime");
		String requestUUID = requestBody.get("requestUUID");
		String signedUUID = requestBody.get("signedUUID");

		if (userId == null || command == null || serverPassword == null || checksum == null || timestamp == 0 || signedTime == null || requestUUID == null
				|| signedUUID == null) {
			logger.info("Invalid or missing parameters from ws-post request. UserId: {}, Command: {}", userId, command);
			return ResponseEntity.internalServerError().build();
		}

		String signature = this.signature.signAsString("%s#%s#%s".formatted(userId, command, serverPassword));
		String timeSignature = this.signature.signAsString(timestampStr);
		String uuidSignature = this.signature.signAsString(requestUUID);

		boolean invalidTime = System.currentTimeMillis() - timestamp > REQUEST_VALID_TIME;
		boolean invalidCommand = userId.replaceAll("[0\\-]", "").isEmpty() && !command.endsWith("-global");
		boolean uuidAlreadyProcessed = uuidRequestExpirationTime.containsKey(requestUUID);

		if (!serverPassword.equals(SERVER_PASSWORD) || !signature.equals(checksum) || !timeSignature.equals(signedTime) || !uuidSignature.equals(signedUUID)
				|| invalidCommand || invalidTime || uuidAlreadyProcessed) {
			String reason = !serverPassword.equals(SERVER_PASSWORD) ? "Incorrect server password."
					: invalidCommand ? "Invalid command: %s with userId: %s".formatted(command, userId)
							: invalidTime ? "Request too old." : uuidAlreadyProcessed ? "This request has already been processed." : "Invalid signature.";
			logger.warn("Denied request to send ws command {} to user {}: {}", command, userId, reason);
			return ResponseEntity.status(403).build();
		}

		uuidRequestExpirationTime.put(requestUUID, System.currentTimeMillis() + REQUEST_VALID_TIME);

		logger.info("Sending ws command {} to user {}", command, userId);
		switch (command) {
		case "light":
		case "dark":
			socketSessionHandler.sendThemeSelected(userId, command);
			break;
		default:
			socketSessionHandler.sendCommand(userId, command);
		}
		return ResponseEntity.ok().build();
	}

	@Scheduled(fixedRate = 20000)
	public void cleanUp() {
		long currentTime = System.currentTimeMillis();
		uuidRequestExpirationTime.entrySet().removeIf(entry -> currentTime > entry.getValue());
	}
}
