package room.ws;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.Session;

public class RoomSessionRegistry {

	private static final RoomSessionRegistry INSTANCE = new RoomSessionRegistry();

	public static RoomSessionRegistry getInstance() {
		return INSTANCE;
	}

	private RoomSessionRegistry() {}

	private final Map<String, Set<Session>> roomSessions = new ConcurrentHashMap<>();

	public void join(String roomId, Session s) {
		roomSessions.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(s);
	}

	public void leave(String roomId, Session s) {
		if (roomId == null)
			return;
		Set<Session> set = roomSessions.get(roomId);
		if (set == null)
			return;

		set.remove(s);
		if (set.isEmpty())
			roomSessions.remove(roomId);
	}

	public Set<Session> getSessions(String roomId) {
		Set<Session> set = roomSessions.get(roomId);
		return set == null ? Collections.emptySet() : set;
	}

	public void removeFromAnyRoom(Session s) {
		for (Map.Entry<String, Set<Session>> e : roomSessions.entrySet()) {
			Set<Session> set = e.getValue();
			if (set.remove(s) && set.isEmpty()) {
				roomSessions.remove(e.getKey());
			}
		}
	}
}
