package game.multi.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.websocket.Session;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class MultiGameService {

	// 방 ID -> 방 객체 맵
    private static Map<String, GameRoom> rooms = new ConcurrentHashMap<>();
    
    // 세션 ID -> 방 ID 맵 (어떤 유저가 어느 방에 있는지 추적용)
    private static Map<String, String> sessionRoomMap = new ConcurrentHashMap<>();

	// Dispatcher에게 전달할 작업 단위
	public static record SendJob(Session target, String text) {}
	
	// 입장 처리
    public List<SendJob> handleOpen(Session session, String roomId, String userId) {
        // 1. 방이 없으면 생성
        GameRoom room = rooms.computeIfAbsent(roomId, id -> new GameRoom(id));
        
        // 2. 세션이 어느 방에 들어갔는지 기록
        sessionRoomMap.put(session.getId(), roomId);
        
        // 3. 해당 방의 입장 로직 호출
        return room.handleOpen(session, userId);
    }
    
    // 메시지 처리
    public List<SendJob> handleMessage(Session session, String msg) {
        String roomId = sessionRoomMap.get(session.getId());
        if (roomId == null) return new ArrayList<>();

        GameRoom room = rooms.get(roomId);
        if (room != null) {
            return room.handleMessage(session, msg);
        }
        return new ArrayList<>();
    }

    // 퇴장 처리
    public List<SendJob> handleClose(Session session) {
        String roomId = sessionRoomMap.remove(session.getId()); // 매핑 삭제
        if (roomId == null) return new ArrayList<>();

        GameRoom room = rooms.get(roomId);
        if (room != null) {
            List<SendJob> jobs = room.handleClose(session);
            
            // 방에 사람이 아무도 없으면 방 삭제 (메모리 관리)
            if (room.isEmpty()) {
            	room.destroy();
                rooms.remove(roomId);
                System.out.println("방 삭제됨: " + roomId);
            }
            return jobs;
        }
        return new ArrayList<>();
    }
}