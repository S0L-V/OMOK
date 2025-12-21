package game.multi.ws;

import java.util.List;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import game.multi.service.MultiGameService;
import game.multi.service.MultiGameService.SendJob;

@ServerEndpoint("/game/multi")
public class MultiWebSocket {

	private static final MultiGameService service = new MultiGameService();

	@OnOpen
	public void onOpen(Session session) {
		try {
			List<SendJob> jobs = service.handleOpen(session);
			dispatch(session, jobs);
		} catch (Exception e) {
			e.printStackTrace();
		}
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

	// [중요] Service가 시킨 배달 심부름을 수행하는 곳
	private void dispatch(Session fallback, List<SendJob> jobs) {
		// 전체에게 보내야 할 경우를 대비해 현재 열려있는 모든 세션을 가져옴
		// (주의: service가 관리하는 sessionList와 다를 수 있으나, 보통 session.getOpenSessions()를 씀)
		// 하지만 여기서는 Service가 target=null을 주면 "모두에게" 보내는 약속을 함.

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