package game.single.ws;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import game.single.service.SingleGameServiceImpl;

@ServerEndpoint("/omok")
public class SingleWebSocket {

	private static SingleGameServiceImpl service = SingleGameServiceImpl.getInstance();

	@OnOpen
	public void onOpen(Session session) throws Exception {
		service.onOpen(session);
	}

	@OnMessage
	public void onMessage(String msg, Session session) throws Exception {
		service.onMessage(msg, session);
	}

	@OnClose
	public void onClose(Session session) {
		service.onClose();
	}
}
