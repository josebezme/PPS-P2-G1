package hoop.g1;

import java.util.Comparator;

public class Player {
	public final int playerId;
	public int positionId;
	public String team;

	// Players ability
	// 1. Shooting ability
	private double shotsMade;
	private double shotsAttempted;
	// 2 .Blocking ability
	private double blocksMade;
	private double blocksAttempted;
	// 3. Passing ability
	private double passesMade;
	private double passesAttempted;
	// 4. Intercepting ability
	private double interceptsMade;
	private double interceptsAttempted;

	public Player(int id) {
		this.playerId = id;
		shotsMade = 0;
		shotsAttempted = 0;
		blocksMade = 0;
		blocksAttempted = 0;
		passesMade = 0;
		passesAttempted = 0;
		interceptsMade = 0;
		interceptsAttempted = 0;
		this.team = "unknown";
	}

	public Player(int id, String team) {
		this.playerId = id;
		shotsMade = 0;
		shotsAttempted = 0;
		blocksMade = 0;
		blocksAttempted = 0;
		passesMade = 0;
		passesAttempted = 0;
		interceptsMade = 0;
		interceptsAttempted = 0;
		this.team = team;
	}

	public String toString() {
		// return "Player #: " + Integer.toString(playerId) + " - [" + team
		// +"]";
		return "&_" + Integer.toString(playerId);

	}

	// the player has taken shot
	public void shotMade() {
		shotsMade++;
	}

	public void shotAttempted() {
		shotsAttempted++;
	}

	public void blockMade() {
		blocksMade++;
	}

	public void blockAttempted() {
		blocksAttempted++;
	}

	public void passMade() {
		passesMade++;
	}

	public void passAttempted() {
		passesAttempted++;
	}

	public void interceptMade() {
		interceptsMade++;
	}

	public void interceptAttempted() {
		interceptsAttempted++;
	}

	public double getShootingWeight() {
		if (shotsAttempted != 0) {
			return shotsMade / shotsAttempted;
		} else {
			return 0;
		}
	}

	public double getBlockingWeight() {
		if (blocksAttempted != 0) {
			return blocksMade / blocksAttempted;
		} else {
			return 0;
		}

	}

	public double getPassingWeight() {
		if (passesAttempted != 0) {
			return passesMade / passesAttempted;
		} else {
			return 0;
		}

	}

	public double getInterceptionWeight() {
		if (interceptsAttempted != 0) {
			return interceptsMade / interceptsAttempted;
		} else {
			return 0;
		}
	}

	public double getTotalWeight() {
		return getShootingWeight() + getBlockingWeight();
	}

	public int hashCode() {
		return (playerId * 123456789) ^ (team.hashCode() * 987654321);
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Player))
			return false;
		Player p = (Player) obj;
		return playerId == p.playerId && team.equals(p.team);
	}

	public double getShotsMade() {
		return shotsMade;
	}

	public double getShotsAttempted() {
		return shotsAttempted;
	}

	public double getBlocksMade() {
		return blocksMade;
	}

	public double getBlocksAttempted() {
		return blocksAttempted;
	}

	public double getPassesMade() {
		return passesMade;
	}

	public double getPassesAttempted() {
		return passesAttempted;
	}

	public double getInterceptsMade() {
		return interceptsMade;
	}

	public double getInterceptsAttempted() {
		return interceptsAttempted;
	}
	
	
	public static Comparator<Player> SORT_BY_SHOOTING = new Comparator<Player>() {
		@Override
		public int compare(Player p1, Player p2) {
			return 	(p1.getShootingWeight() > p2.getShootingWeight()) ? -1 :
					(p1.getShootingWeight() < p2.getShootingWeight()) ?  1 : 0 ;
		}
	};
	
	public static Comparator<Player> SORT_BY_PASSING = new Comparator<Player>() {
		@Override
		public int compare(Player p1, Player p2) {
			return 	(p1.getPassingWeight() > p2.getPassingWeight()) ? -1 :
					(p1.getPassingWeight() < p2.getPassingWeight()) ?  1 : 0 ;
		}
	};
	
	public static Comparator<Player> SORT_BY_BLOCKING = new Comparator<Player>() {
		@Override
		public int compare(Player p1, Player p2) {
			return 	(p1.getBlockingWeight() > p2.getBlockingWeight()) ? -1 :
					(p1.getBlockingWeight() < p2.getBlockingWeight()) ?  1 : 0 ;
		}
	};
	
	public static Comparator<Player> SORT_BY_OVERALL = new Comparator<Player>() {
		@Override
		public int compare(Player p1, Player p2) {
			return 	(p1.getTotalWeight() > p2.getTotalWeight()) ? -1 :
					(p1.getTotalWeight() < p2.getTotalWeight()) ?  1 : 0 ;
		}
	};
	
	
}
