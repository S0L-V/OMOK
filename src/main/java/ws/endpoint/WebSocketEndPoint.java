package ws.endpoint;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.websocket.EndpointConfig;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.google.gson.Gson;

import ws.session.SessionContext;

@ServerEndpoint(value = "/ws", configurator = WebSocketConfigurator.class)
public class WebSocketEndPoint {

	private static final Gson gson = new Gson();
	private static final SessionContext sessionContext = SessionContext.getInstance();

	@OnOpen
	public void onOpen(Session session, EndpointConfig config) throws IOException {

		HttpSession httpSession = (HttpSession)config.getUserProperties().get(HttpSession.class.getName());

		sessionContext.connectSession(session, httpSession);

		Map<String, Object> payload = new HashMap<>();
		payload.put("type", "CONNECTED");

		session.getAsyncRemote().sendText(gson.toJson(payload));
	}
}
