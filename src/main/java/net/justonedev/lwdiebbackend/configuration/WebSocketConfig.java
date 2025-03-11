/* (C)2025 */
package net.justonedev.lwdiebbackend.configuration;

import net.justonedev.lwdiebbackend.websockets.SocketSessionHandler;
import net.justonedev.lwdiebbackend.websockets.WebSocketInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class configures WebSocket support for the application. Registers a
 * WebSocket handler and manages a socket session handler.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
	@Value("dashboard.variable.frontend-url")
	private String frontendUrl;

	private final Map<String, Set<WebSocketSession>> userSockets = new ConcurrentHashMap<>();

	/**
	 * Creates a handler to manage WebSocket sessions.
	 *
	 * @return the {@link SocketSessionHandler} instance
	 */
	@Bean
	public SocketSessionHandler socketSessionHandler() {
		return new SocketSessionHandler(userSockets);
	}

	/**
	 * Registers WebSocket handlers and defines allowed origins:
	 * <ul>
	 * <li><a href="http://localhost:3000">...</a></li>
	 * <li><a href="http://localhost>...</a></li>
	 * <li><a href="https://localhost:3000">...</a></li>
	 * </ul>
	 *
	 * @param registry the {@link WebSocketHandlerRegistry} to configure
	 */
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(socketSessionHandler(), "/ws").addInterceptors(new WebSocketInterceptor()).setAllowedOrigins(frontendUrl);
	}
}
