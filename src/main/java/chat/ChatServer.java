package chat;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

@ServerEndpoint("/ws/room/{roomId}")
public class ChatServer {

    private static final ConcurrentHashMap<String, CopyOnWriteArraySet<Session>> rooms = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Integer> playerNoMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, AtomicInteger> playerCounters = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> roomState = new ConcurrentHashMap<>();

    private static final int GAME_START_PLAYERS = 2; // 개인전 기준

    @OnOpen
    public void onOpen(Session session, @PathParam("roomId") String roomId) throws IOException {
        rooms.computeIfAbsent(roomId, k -> new CopyOnWriteArraySet<>()).add(session);

        int playerNo = playerCounters
                .computeIfAbsent(roomId, k -> new AtomicInteger(0))
                .incrementAndGet();
        playerNoMap.put(session.getId(), playerNo);

        // 접속자에게 현재 상태 알려줌
        send(session, "STATE:" + roomState.getOrDefault(roomId, "ROOM_CHAT"));

        broadcast(roomId, "SYSTEM:Player" + playerNo + " joined");
        updateAndBroadcastState(roomId);
    }

    @OnMessage
    public void onMessage(Session session, String message, @PathParam("roomId") String roomId) {
        if (message == null) return;

        int playerNo = playerNoMap.getOrDefault(session.getId(), 0);
        String nick = "Player" + playerNo;

        // 채팅
        if (message.startsWith("ROOM_CHAT:")) {
            String text = message.substring("ROOM_CHAT:".length());
            broadcast(roomId, "ROOM_CHAT:" + nick + ":" + text);
            return;
        }

        // 나머지 메시지 (오목 담당이 처리)
        broadcast(roomId, message);
    }

    @OnClose
    public void onClose(Session session, @PathParam("roomId") String roomId) {
        CopyOnWriteArraySet<Session> set = rooms.get(roomId);
        if (set != null) set.remove(session);

        playerNoMap.remove(session.getId());

        broadcast(roomId, "SYSTEM:Player left");
        updateAndBroadcastState(roomId);

        if (set != null && set.isEmpty()) {
            rooms.remove(roomId);
            roomState.remove(roomId);
            playerCounters.remove(roomId);
        }
    }

    private static void updateAndBroadcastState(String roomId) {
        CopyOnWriteArraySet<Session> set = rooms.get(roomId);
        int players = (set == null) ? 0 : set.size();

        String newState = (players >= GAME_START_PLAYERS)
                ? "GAME_START"
                : "ROOM_CHAT";

        String oldState = roomState.getOrDefault(roomId, "ROOM_CHAT");
        roomState.put(roomId, newState);

        if (!newState.equals(oldState)) {
            broadcast(roomId, "STATE:" + newState);
        }
    }

    private static void broadcast(String roomId, String msg) {
        CopyOnWriteArraySet<Session> set = rooms.get(roomId);
        if (set == null) return;

        for (Session s : set) {
            if (!s.isOpen()) continue;
            try {
                s.getBasicRemote().sendText(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void send(Session s, String msg) {
        try {
            s.getBasicRemote().sendText(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
