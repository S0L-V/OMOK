package game.single.ws;

import java.util.List;

import javax.servlet.http.HttpSession;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import config.WebSocketConfig;
import game.single.dao.SinglePlayerDAO;
import game.single.dao.SinglePlayerDAOImpl;
import game.single.service.SingleGameServiceImpl;
import game.single.service.SingleGameServiceManager;
import session.SessionContext;

@ServerEndpoint(value = "/single", configurator = WebSocketConfig.class)
public class SingleWebSocket {

//	private static SingleGameServiceImpl service = SingleGameServiceImpl.getInstance()
	
	private static final SingleGameServiceManager manager =SingleGameServiceManager.getInstance();

	private static final SinglePlayerDAO singlePlayerDao = new SinglePlayerDAOImpl();

    // ✅ 너 프로젝트에서 로비에서 쓰던 sessionContext를 그대로 사용(전역/싱글톤/DI 방식에 맞춰)
    private static final SessionContext sessionContext = SessionContext.getInstance();
	
    private String getRoomId(Session session) {
    	List<String> ids = session.getRequestParameterMap().get("roomId");
        return (ids == null || ids.isEmpty()) ? "DEFAULT" : ids.get(0);
    }

	@OnOpen
	public void onOpen(Session session, EndpointConfig config) throws Exception {
		String roomId = getRoomId(session);
		
		if (roomId == null) {
            session.close();
            return;
        }
		
		HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());

        if (httpSession == null) {
            session.close();
            return;
        }

        sessionContext.connectSession(session, httpSession);

        String userId = sessionContext.getUserId(session);
        if (userId == null) {
            session.close();
            return;
        }

        // ✅ DB로 방 멤버 검증
        if (!singlePlayerDao.isMember(roomId, userId)) {
            session.close();
            return;
        }
        
        // onClose에서 쓰기 위해 저장
        session.getUserProperties().put("roomId", roomId);
        session.getUserProperties().put("userId", userId);

        System.out.println("[SingleWS] CONNECT session=" + session.getId()
            + " userId=" + userId + " roomId=" + roomId);

        SingleGameServiceImpl service = manager.getOrCreate(roomId);
        service.onOpen(session, userId);
	}

	@OnMessage
	public void onMessage(String msg, Session session) throws Exception {
//		service.onMessage(msg, session);
		String roomId = (String) session.getUserProperties().get("roomId");
        SingleGameServiceImpl service = manager.getOrCreate(roomId);
        service.onMessage(msg, session);
	}

	@OnClose
	public void onClose(Session session) {
//		service.onClose(session);
		
		String roomId = (String) session.getUserProperties().get("roomId");
        SingleGameServiceImpl service = manager.getOrCreate(roomId);
        service.onClose(session);

        // (선택) 게임이 완전히 끝났으면 remove하도록 개선 가능
        // manager.remove(gameId);
	}
}
