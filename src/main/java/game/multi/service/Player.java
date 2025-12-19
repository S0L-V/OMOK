package game.multi.service;

import javax.websocket.Session;

public class Player {

	private Session session;
	private final int slot; // 0~3 (턴 순서)
	private final int team; // 0 or 1
	private final int color; // 1=black, 2=white
	private boolean alive = true;

	public Player(Session session, int slot, int team, int color) {
		this.session = session;
		this.slot = slot;
		this.team = team;
		this.color = color;
	}

	public Session getSession() {
		return session;
	}

	public int getSlot() {
		return slot;
	}

	public int getTeam() {
		return team;
	}

	public int getColor() {
		return color;
	}

	public boolean isAlive() {
		return alive;
	}

	public void disconnect() {
		this.alive = false;
		this.session = null;
	}
}
