package game.multi.ws;

import java.util.List;

import javax.servlet.http.HttpSession;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import config.WebSocketConfig;
import game.multi.service.MultiGameService;
import game.multi.service.MultiGameService.SendJob;

@ServerEndpoint(value = "/game/multi", configurator = WebSocketConfig.class)
public class MultiWebSocket {

	private static final MultiGameService service = new MultiGameService();

	@OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        try {
        	// HttpSession에서 userId 가져오기
        	HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
        	String userId = null;
        	if (httpSession != null) {
        		userId = (String) httpSession.getAttribute("loginUserId");
        	}
        	
            // URL 쿼리 스트링에서 roomId 파싱
            String query = session.getRequestURI().getQuery();
            String roomId = getParameterValue(query, "roomId");

            if (roomId == null || roomId.trim().isEmpty()) {
                // 방 ID가 없으면 에러 처리 혹은 기본방("default")
                roomId = "default"; 
            }

            // Service에 roomId와 userId 함께 전달
            List<SendJob> jobs = service.handleOpen(session, roomId, userId);
            dispatch(session, jobs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	// 쿼리 스트링 파싱 헬퍼 메서드
    private String getParameterValue(String query, String name) {
        if (query == null) return null;
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length > 1 && pair[0].equals(name)) {
                return pair[1];
            }
        }
        return null;
    }

	@OnMessage
	public void onMessage(String msg, Session session) {
		try {
			List<SendJob> jobs = service.handleMessage(session, msg);
			dispatch(session, jobs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@OnClose
	public void onClose(Session session) {
		try {
			List<SendJob> jobs = service.handleClose(session);
			dispatch(session, jobs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@OnError
	public void onError(Session session, Throwable t) {
		System.out.println("소켓 에러 발생: " + t.getMessage());
	}

	private void dispatch(Session fallback, List<SendJob> jobs) {
		// 전체에게 보내야 할 경우를 대비해 현재 열려있는 모든 세션을 가져옴
		// Service가 target=null을 주면 "모두에게" 보냄

		for (SendJob job : jobs) {
			try {
				// 1. 전체 전송 (Broadcast)
				if (job.target() == null) {
					// 타겟이 없으면(null) => 브로드캐스트 (전체 전송)
					for (Session s : fallback.getOpenSessions()) {
						if (s.isOpen()) {
							try {
								// 동기화 처리로 충돌 방지
								synchronized (s) {
									s.getBasicRemote().sendText(job.text());
								}
							} catch (Exception e) { }
						}
					}
				} 
				// 2. 개별 전송 (Unicast)
				else {
					if (job.target().isOpen()) {
						try {
							synchronized (job.target()) {
								job.target().getBasicRemote().sendText(job.text());
							}
						} catch (Exception e) { }
					}
				}
			} catch (Exception ignore) { }
		}
	}
}