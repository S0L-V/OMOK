package chat.webSocket;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/ws/room/{roomId}")
public class ChatServer {

    // roomId -> sessions
    private static final ConcurrentHashMap<String, CopyOnWriteArraySet<Session>> rooms = new ConcurrentHashMap<>();

    // sessionId -> playerNo (1,2,3...)
    private static final ConcurrentHashMap<String, Integer> playerNoMap = new ConcurrentHashMap<>();

    // roomId -> state ("LOBBY" or "IN_GAME")
    private static final ConcurrentHashMap<String, String> roomState = new ConcurrentHashMap<>();

    private static final int GAME_START_PLAYERS = 2; // 개인전: 2명 되면 시작 (팀전이면 4로 바꾸면 됨)

    @OnOpen
    public void onOpen(Session session, @PathParam("roomId") String roomId) throws IOException {
        CopyOnWriteArraySet<Session> set =
                rooms.computeIfAbsent(roomId, k -> new CopyOnWriteArraySet<>());
        set.add(session);

        // 플레이어 번호 부여(테스트용 하드코딩)
        int playerNo = set.size(); // 들어온 순서대로 1,2,3...
        playerNoMap.put(session.getId(), playerNo);

        // 나중에 카카오 로그인 닉네임을 쓰려면
        // 1) HttpSession을 EndpointConfig로 가져오는 Configurator가 필요하고
        // 2) 여기서 nickname을 session.getUserProperties()에 저장하는 방식으로 씀
        // String nickname = (String) httpSession.getAttribute("nickname");
        // session.getUserProperties().put("nickname", nickname);

        // 접속 확인 메시지
        send(session, "SYSTEM:connected room=" + roomId);

        // 현재 상태 계산/전파
        updateAndBroadcastState(roomId);

        // 참가 메시지
        broadcast(roomId, "SYSTEM:Player" + playerNo + " joined (players=" + set.size() + ")");
    }

    @OnMessage
    public void onMessage(Session session, String message, @PathParam("roomId") String roomId) {
        String state = roomState.getOrDefault(roomId, "LOBBY");
        int playerNo = playerNoMap.getOrDefault(session.getId(), 0);
        String nick = "Player" + playerNo;

        // message 
        if (message == null) return;

        // 게임 시작 후에는 이모지만 허용(채팅 막기)
        if ("IN_GAME".equals(state)) {
            if (message.startsWith("EMOJI:")) {
                String code = message.substring("EMOJI:".length());
                broadcast(roomId, "EMOJI:" + nick + ":" + code);
            } else {
                // 게임 중 채팅은 무시(또는 경고 SYSTEM 메시지로 보내도 됨)
                send(session, "SYSTEM:IN_GAME mode - only EMOJI allowed");
            }
            return;
        }

        // LOBBY에서는 채팅만(이모지도 보내고 싶으면 허용해도 됨)
        if (message.startsWith("CHAT:")) {
            String text = message.substring("CHAT:".length());
            broadcast(roomId, "CHAT:" + nick + ":" + text);
        } else if (message.startsWith("EMOJI:")) {
            // 로비에서도 이모지 허용하고 싶으면 살려두기
            String code = message.substring("EMOJI:".length());
            broadcast(roomId, "EMOJI:" + nick + ":" + code);
        } else {
            // 알 수 없는 프로토콜
            send(session, "SYSTEM:Unknown message format");
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("roomId") String roomId) {
        CopyOnWriteArraySet<Session> set = rooms.get(roomId);
        if (set != null) {
            set.remove(session);
        }

        Integer playerNo = playerNoMap.remove(session.getId());
        String nick = (playerNo == null) ? "Unknown" : ("Player" + playerNo);

        // 나간 메시지
        int players = (set == null) ? 0 : set.size();
        broadcast(roomId, "SYSTEM:" + nick + " left (players=" + players + ")");

        // 상태 갱신/전파
        updateAndBroadcastState(roomId);

        // 방 비면 정리
        if (set != null && set.isEmpty()) {
            rooms.remove(roomId);
            roomState.remove(roomId);
        }
    }

    private static void updateAndBroadcastState(String roomId) {
        CopyOnWriteArraySet<Session> set = rooms.get(roomId);
        int players = (set == null) ? 0 : set.size();

        String newState = (players >= GAME_START_PLAYERS) ? "IN_GAME" : "LOBBY";
        String oldState = roomState.getOrDefault(roomId, "LOBBY");

        roomState.put(roomId, newState);

        // 상태가 바뀐 경우만 브로드캐스트
        if (!newState.equals(oldState)) {
            broadcast(roomId, "STATE:" + newState);
        } else {
            // 디버깅용으로 처음 접속자에게도 상태를 알리고 싶으면 아래처럼 유지해도 됨
            // 지금은 gameChat에서 LOBBY 받으면 방으로 돌아가니까, 상태 바뀔 때만이 안정적
            // broadcast(roomId, "STATE:" + newState);
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
        if (s == null || !s.isOpen()) return;
        try {
            s.getBasicRemote().sendText(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
