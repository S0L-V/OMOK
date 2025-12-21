package game.single.service;

import javax.websocket.Session;

public interface SingleGameService {
	void onOpen(Session session, String userId) throws Exception;

	void onMessage(String msg, Session session) throws Exception;

	void onClose(Session session);
}
