package hoop.g1;

public class Player {
		public final int playerId;
		public int positionId;
		public String team;
		public double preShootingWeight;
		public double prePassingWeight;
		public double preBlockingWeight;
		
		// Players ability
		// 1. Shooting ability
		public double numShotMade;
		public double numShotAttempted;
		// 2 .Blocking ability
		public double numBlockMade;
		public double numBlockAttempted;
		// 3. Passing ability
		public double numPassMade;
		public double numPassAttempted;
		// 4. Intercepting ability
		public double numInterceptMade; 
		public double numInterceptAttempted; 
		
		public Player(int id) {
			this.playerId = id;
			numShotMade=0;
			numShotAttempted=0;
			numBlockMade=0;
			numBlockAttempted=0;
			numPassMade=0;
			numPassAttempted=0;
			numInterceptMade=0;
			numInterceptAttempted=0;
			this.team="unknown";
		}
		public Player(int id, String team) {
			this.playerId = id;
			numShotMade=0;
			numShotAttempted=0;
			numBlockMade=0;
			numBlockAttempted=0;
			numPassMade=0;
			numPassAttempted=0;
			numInterceptMade=0;
			numInterceptAttempted=0;
			this.team=team;
		}

		public String toString(){
			// return "Player #: " + Integer.toString(playerId) + " - [" + team +"]";
			return "&_" + Integer.toString(playerId);
			
		}

		//the player has taken shot
		public void shotMade(){numShotMade++; }
		public void shotAttempted(){numShotAttempted++; } 
		public void shotFailed(){numShotMade--;}
		public void blockMade(){numBlockMade++;}
		public void blockAttempted(){numBlockAttempted++;}
		public void blockFailed(){numBlockMade--;}
		public void passMade(){numPassMade++;}
		public void passAttempted(){numPassAttempted++;}
		public void passFailed(){numPassMade--;}		
		public void interceptMade(){numInterceptMade++;}
		public void interceptAttempted(){numInterceptAttempted++;}
		public void interceptFailed(){numInterceptMade--;}
		public void interceptNullify(){numInterceptAttempted--; numInterceptMade--;};
		public void passNullify(){numPassAttempted--;numPassMade--;}
		public void shotNullify(){numShotMade--;numShotAttempted--;}
		public void blockNullify(){numBlockMade--;numBlockAttempted--;}

		//VERY CRUDE. NEED TO UPDATE THIS...
		
		private static double PRE_RATIO = 0.5;
		public double getShootingWeight() {
			
			double dynShootingWeight = 0.5;
			if(numShotAttempted != 0) {
				dynShootingWeight = numShotMade/numShotAttempted;
			}
			
			
			return (preShootingWeight == 0) // Then this an opponent Player
					? dynShootingWeight : // Return dynamic weight.
					((preShootingWeight * PRE_RATIO) + (dynShootingWeight * (1 - PRE_RATIO))) / 2 //  
					;
		}
		
		public double getBlockingWeight() {
			return 0.0;
		}
		
		public double getPassingWeight() {
			return 0.0;
		}
		
		public double getInterceptionWeight() {
			return 0.0;
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

