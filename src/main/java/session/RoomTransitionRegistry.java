package session;

import java.util.concurrent.ConcurrentHashMap;

public class RoomTransitionRegistry {
	private static final RoomTransitionRegistry inst = new RoomTransitionRegistry();

	public static RoomTransitionRegistry getInstance() {
		return inst;
	}

	private final ConcurrentHashMap<String, Long> moving = new ConcurrentHashMap<>();

	private String key(String roomId, String userId) {
		return roomId + ":" + userId;
	}

	public void markMoving(String roomId, String userId, long ttlMs) {
		moving.put(key(roomId, userId), System.currentTimeMillis() + ttlMs);
	}

	public boolean isMoving(String roomId, String userId) {
		Long exp = moving.get(key(roomId, userId));
		if (exp == null)
			return false;
		if (System.currentTimeMillis() > exp) {
			moving.remove(key(roomId, userId));
			return false;
		}
		return true;
	}

	public void clear(String roomId, String userId) {
		moving.remove(key(roomId, userId));
	}
}
