package game.single.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class SingleGameServiceManager {
	
	private static final SingleGameServiceManager INSTANCE = new SingleGameServiceManager();
    public static SingleGameServiceManager getInstance() {
        return INSTANCE;
    }
    
    private final ConcurrentMap<String, SingleGameServiceImpl> games = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    private SingleGameServiceManager() {}
    
    public SingleGameServiceImpl getOrCreate(String roomId) {
        return games.computeIfAbsent(roomId, id -> new SingleGameServiceImpl(id, scheduler));
    }
    
    public void remove(String roomId) {
    	SingleGameServiceImpl svc = games.remove(roomId);
        if (svc != null) {
            svc.destroy(); // (선택) turnTask cancel 같은 정리
        }
    }
    
 // ✅ 컨테이너 종료 시 호출될 정리 메서드
    public void destroy() {
        // 각 게임 정리(타이머 취소 등)
        for (SingleGameServiceImpl svc : games.values()) {
            try { svc.destroy(); } catch (Exception ignore) {}
        }
        games.clear();

        // 스레드풀 정리
        scheduler.shutdownNow();
    }
}
