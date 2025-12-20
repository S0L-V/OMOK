package lobby.ws;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

public class LobbyWebSocketConfig extends ServerEndpointConfig.Configurator {

	@Override
	public void modifyHandshake(ServerEndpointConfig sec,
		HandshakeRequest request,
		HandshakeResponse response) {

		Object httpSession = request.getHttpSession();
		if (httpSession instanceof HttpSession) {
			sec.getUserProperties().put(HttpSession.class.getName(), httpSession);
		}
	}
}
