package hoop.g1;

public class Player {
		public final int playerId;
		public int positionId;
		public String team;
		public double preShootingWeight = -1;
		public double prePassingWeight = -1;
		public double preBlockingWeight = -1;
		
		// Players ability
		// 1. Shooting ability
		public double shotsMade;
		public double shotsAttempted;
		// 2 .Blocking ability
		public double blocksMade;
		public double blocksAttempted;
		// 3. Passing ability
		public double passesMade;
		public double passesAttempted;
		// 4. Intercepting ability
		public double interceptsMade; 
		public double interceptsAttempted; 
		
		public Player(int id) {
			this.playerId = id;
			shotsMade=0;
			shotsAttempted=0;
			blocksMade=0;
			blocksAttempted=0;
			passesMade=0;
			passesAttempted=0;
			interceptsMade=0;
			interceptsAttempted=0;
			this.team="unknown";
		}
		public Player(int id, String team) {
			this.playerId = id;
			shotsMade=0;
			shotsAttempted=0;
			blocksMade=0;
			blocksAttempted=0;
			passesMade=0;
			passesAttempted=0;
			interceptsMade=0;
			interceptsAttempted=0;
			this.team=team;
		}

		public String toString(){
			// return "Player #: " + Integer.toString(playerId) + " - [" + team +"]";
			return "&_" + Integer.toString(playerId);
			
		}

		//the player has taken shot
		public void shotMade(){shotsMade++; }
		public void shotAttempted(){shotsAttempted++; } 
		public void blockMade(){blocksMade++;}
		public void blockAttempted(){blocksAttempted++;}
		public void passMade(){passesMade++;}
		public void passAttempted(){passesAttempted++;}
		public void interceptMade(){interceptsMade++;}
		public void interceptAttempted(){interceptsAttempted++;}

		//VERY CRUDE. NEED TO UPDATE THIS...
		
		public boolean ourShooter() {
			return (preShootingWeight >= 0);
		}
		
		private static double PRE_RATIO = 0.75;
		public double getShootingWeight() {
			
			double dynShootingWeight = -1;
			if(shotsAttempted != 0) {
				dynShootingWeight = shotsMade/shotsAttempted;
			} else if(ourShooter()) {
				dynShootingWeight = preShootingWeight; 
			}
			
			
			return (ourShooter()) // Then this an opponent Player
					? ((preShootingWeight * PRE_RATIO) + (dynShootingWeight * (1 - PRE_RATIO))) / 2 //  
					: dynShootingWeight ;
					 // Return dynamic weight.
		}
		
		public double getBlockingWeight() {
			
			double dynBlockingWeight = -1;
			if(blocksAttempted != 0) {
				dynBlockingWeight = blocksMade/blocksAttempted;
			} else if(ourShooter()) {
				dynBlockingWeight = preBlockingWeight;
			}
			
			return (ourShooter())
					? ((preBlockingWeight * PRE_RATIO) + (dynBlockingWeight * (1 - PRE_RATIO))) / 2
					: dynBlockingWeight ;
		}
		
		public double getPassingWeight() {
			
			double dynPassingWeight = -1;
			if(passesAttempted != 0) {
				dynPassingWeight = passesMade / passesAttempted;
			} else if(ourShooter()) {
				dynPassingWeight = prePassingWeight;
			}
			
			return (ourShooter())
					? ((prePassingWeight * PRE_RATIO) + (dynPassingWeight * (1 - PRE_RATIO))) / 2
					: dynPassingWeight ;
		}
		
		public double getInterceptionWeight() {
			double interceptRatio = -1;
			if(interceptsAttempted != 0) {
				interceptRatio = interceptsMade / interceptsAttempted;
			}
			
			return interceptRatio;
		}
		
		public double getTotalWeight() {
			return getShootingWeight() + getBlockingWeight();
		}

		public int hashCode(){
			return (playerId * 123456789) ^
		      		 (team.hashCode() * 987654321);
		}

		public boolean equals(Object obj)
		{
			if (!(obj instanceof Player))
				return false;
			Player p = (Player) obj;
			return playerId == p.playerId && team.equals(p.team);
		}
	}

