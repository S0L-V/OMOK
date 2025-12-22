package game.single.dao;

public interface SinglePlayerDAO {
	boolean isMember(String roomId, String userId) throws Exception;
}
