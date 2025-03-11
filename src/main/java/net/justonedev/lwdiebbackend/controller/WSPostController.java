/* (C)2025 */
package net.justonedev.lwdiebbackend.controller;

import net.justonedev.lwdiebbackend.Signature;
import net.justonedev.lwdiebbackend.websockets.SocketSessionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

@RestController
public class WSPostController {
	private static final Logger logger = LoggerFactory.getLogger(WSPostController.class);
	@Autowired
	private SocketSessionHandler socketSessionHandler;
	private Signature signature;

	@Value("${server.comms.password}")
	private String SERVER_PASSWORD;
	@Value("${server.signature.signature-secret}")
	private String SIGNATURE_SECRET;

	@PostMapping("/ws-post")
	public ResponseEntity<Map<String, Object>> post(@RequestBody Map<String, String> requestBody) {
		if (signature == null)
			signature = new Signature(SIGNATURE_SECRET);

		String userId = requestBody.get("userId");
		String command = requestBody.get("command");
		String serverPassword = requestBody.get("serverPassword");
		String checksum = requestBody.get("checksum");

		if (userId == null || command == null || serverPassword == null || checksum == null) {
			logger.info("Invalid or missing parameters from ws-post request. UserId: {}, Command: {}", userId, command);
			return ResponseEntity.internalServerError().build();
		}

		String signature = this.signature.signAsString(command + serverPassword);
		if (!serverPassword.equals(SERVER_PASSWORD) || !signature.equals(checksum)
				|| userId.replaceAll("[0\\-]", "").isEmpty() && !command.endsWith("-global")) {
			String reason = !serverPassword.equals(SERVER_PASSWORD) ? "Incorrect server password"
					: !signature.equals(checksum) ? "Invalid signature" : "Invalid command: %s with userId: %s".formatted(command, userId);
			logger.warn("Denied request to send ws command {} to user {}: {}", command, userId, reason);
			return ResponseEntity.status(403).build();
		}

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
}
