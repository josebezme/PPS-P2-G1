package hoop.g1;

import hoop.sim.Game.Round;
import hoop.sim.Hoop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import java.util.Random;

public class Team implements hoop.sim.Team, Logger {
	
	private static int versions;
	
	private static final Random gen = new Random();
	private static final boolean DEBUG = true;
	private static final int TEAM_SIZE = 5;
	
	private boolean startedTournament;

	private static double[][] ourStats;
	
	public enum Status {
		DEFENDING,
		START,
		PASSING,
		SHOOTING
	}
	
	public void log(String message) {
		if(DEBUG) {
			System.err.println(name() + ": "  + message);			
		}
	}

	public String name()
	{
		return "G1-" + version;
	}

	private final int version = ++versions;

	private int holder = 0; //this variable will store our first passer

	// private Set<OtherTeam> otherTeam = new HashSet<OtherTeam>();
	private int[] currentPlayingTeam;
	private int[] currentOpponentTeam;
	private OtherTeam otherTeamPointer;

	private TeamPicker picker;
	private Game game;
	private int currentPerspective;
	private Game[] games = new Game[2];

	private Game attackingGame;
	
	private Map<String, StatWrapper> allTeamStats = new HashMap<String, StatWrapper>();
	private Map<String, OtherTeam> name2OtherTeam = new HashMap<String, OtherTeam>();
	private List<OtherTeam> otherTeamList = new ArrayList<OtherTeam>();
	private StatWrapper teamStats;
	private static List<Player> ourTeamStat;

	public static class StatWrapper {
		String teamName = new String();
		List<Player> playerObjects = new ArrayList<Player>();
		PriorityQueue<Player> favoriteShooters = new PriorityQueue<Player>();
		PriorityQueue<Player> favoritePassers = new PriorityQueue<Player>();

	}
	//This array changes based on what team we play against
	private Player[] opponents = new Player[TEAM_SIZE]; 

	private List<Integer> bestShooters;

	private List<Integer> bestPassers;

	private static boolean in(int[] a, int n, int x)
	{
		for (int i = 0 ; i != n ; ++i)
			if (a[i] == x) return true;
		return false;
	}

	private static void shuffle(int[] a, Random gen)
	{
		for (int i = 0 ; i != a.length ; ++i) {
			int r = gen.nextInt(a.length - i) + i;
			int t = a[i];
			a[i] = a[r];
			a[r] = t;
		}
	}


	@Override
	public void opponentTeam(int[] opponentPlayers) {
		currentOpponentTeam=opponentPlayers;
		log("Called opponentTeam()");
		// We're told what players were picked to play us. 123
		// Keep track of these players for the game Jose & Jiang & Albert
		if(game.selfGame) {
			return;
		}
		

		
		for(int i = 0; i < TEAM_SIZE; i++) {
			//intilize the opponents(array of Players bojects)
			opponents[i] = teamStats.playerObjects.get(opponentPlayers[i] - 1);
			log("OPPOSITE TEAM: " + opponentPlayers[i]);
			log(opponents[i].toString());
			
		}
		log("----------------- opponentTeam()ENDS----------------");
	}

	@Override
	public int[] pickTeam(String opponent, int totalPlayers, hoop.sim.Game[] history) {

	
		log("------------ pickTeam() call STARTS HERE---------");
		log("Called pickTeam() with opponent: " + opponent + " with tP: " + totalPlayers);
		// Here we find out how many players we have for the game.
		
		// allTeamStats = hoop.sim.tweakedHoop.stats;
		// ourStats = allTeamStats[hoop.sim.tweakedHoop.name2Index.get(name())];

		// if (DEBUG) {	
		// 	System.out.println("player\tshoot\tdefense\tpass\tintercept");
		// 	for (int p=0; p < ourStats.length; p++ ) {
		// 		System.out.print("--> Player [" + p + "] : ");
		// 		for (int s=0; s < ourStats[0].length; s++ ) {
		// 				System.out.printf("%.3f\t", ourStats[p][s]);
		// 		}
		// 		System.out.println();
		// 	}
		// }

		game = new Game();
		attackingGame = game;
		if(name().equals(opponent)) {
			// We're playing a self game.
			log("Setting game to self game.");
			game.selfGame = true;
			games[currentPerspective] = game;
			currentPerspective = (currentPerspective + 1) % 2;
		} else {
			if(!startedTournament) {
				// Everything in here happens once per tournament.
				startedTournament = true;
				log("STARTED_TOURN");
				bestShooters = picker.getBestShooters();
				bestPassers = picker.getBestPassers();
			}
			//Load up the teamsat for a particular team
			teamStats = allTeamStats.get(opponent);
			// teamStats.teamName = opponent;
			log(opponent);

			
			//if the stat is not found, make one for that team
			if(teamStats == null) {
				teamStats = new StatWrapper();
				
				Player p;
				//intilize the teams stat for that team
				for(int i = 0; i < totalPlayers; i++) {
					p = new Player(i + 1);
					teamStats.playerObjects.add(p); 
					teamStats.favoritePassers.add(p);
					teamStats.favoriteShooters.add(p);
				}
			}
			
			log("ESTIMATION BASED ON TWO PIVOTS: ");
			// picker.printExtraInfo();
			log("Shooters: "  + bestShooters);
			log("------------Passer INFO STARTS HERE-----------");
			log("Passers: " +  bestPassers);
			log("-----------PASSER INFO ENDS HERE------------");
		}
		
		// First initialize the scores.
		game.ourScore = -1;
		game.theirScore = 0;
		if(game.selfGame) {
			if(picker == null) {
				picker = new PivotTeamPicker();
				picker.initialize(totalPlayers, 10, Hoop.gameTurns());
				picker.setLogger(this);
			}
			
			return picker.pickTeam();
			
		} else {
			
			ourTeamStat = picker.getPlayerStat();
			// for (Player p : ourTeamStat){
			// 	log("Player Number : " + p.playerId);
			// }

			int[] team = new int[5];
			currentPlayingTeam = team;
			// Pick 2 shooters.
			team[0] = bestShooters.get(0);
			team[1] = bestShooters.get(1);
			
			List<Integer> already = new ArrayList<Integer>(4);
			already.add(team[0]);
			already.add(team[1]);
			
			// Pick 3 passers;.
			int pos = 0;
			int offset = 0;
			for(int i = 2; i < 5; i++) {
				
				// select random int (+1)
				// if already contains (already assigned to position) that pos
				// then randomly get again.
				while(already.contains(pos = bestPassers.get(offset++))){
				}
				
				team[i] = pos;
				already.add(pos);
			}
			
			log("Choosing team: " + Arrays.toString(team));
			if(!game.selfGame){

				//newly seen team
				if(!name2OtherTeam.containsKey(opponent))
				{
					otherTeamPointer = new OtherTeam(opponent,totalPlayers);
					otherTeamList.add(otherTeamPointer);

				}
				else{
					otherTeamPointer = name2OtherTeam.get(opponent);
				}
				log("WE ARE COMPETING AGAINST: " + otherTeamPointer.getName());

			}

			log("------------ pickTeam() call ENDS HERE---------");
			return team;

		}
	}

	@Override
	public int pickAttack(int yourScore, int opponentScore, Round previousRound) {


		if(game.selfGame) {
			picker.reportLastRound(previousRound);
		}
		
		log("Called pickAttack()");
		log("yourScore: " + opponentScore+ " ourScore: " + yourScore);
		if(game.selfGame) {
			game = games[currentPerspective];
			attackingGame = game;
			currentPerspective = (currentPerspective + 1) % 2;
		}
		
		if(game.ourScore == -1) {
			// This is the first turn.
			game.ourScore = 0;
		} else if (game.theirScore == opponentScore) {
			// They failed to make their shot.
			// Or their pass was blocked.
			// case 1: their shot is miseed
			// Based on the previous round, store this knowledge about the player whose shot is missed
			// player.shotAttempted();
			// Case 2: their pass was blocked
			// If so, get the whoever tried to pass, and 
			// player.passAttempted();
		} else {
			game.theirScore = opponentScore;
			// They made their shot.
			//player.shotMade();
		}
		
		if(previousRound != null && !game.selfGame) {
			int[] holders = previousRound.holders();
			int lastPasser = holders.length - 1;
			int position = 0;
			Player lastBallHolder = opponents[holders.length - 1]; // it's their index + 1
			
			// Assume the last ballholder was a passer and penalize in the case
			lastBallHolder.passAttempted();
			lastBallHolder.passMade();

			log("currentPlayingTeam : " + Arrays.toString(currentPlayingTeam));
			log("Previous Rounds Defenders:" + Arrays.toString(previousRound.defenders()));
			log("current Opponent PlayerID: " + Arrays.toString(currentOpponentTeam));
			switch(previousRound.lastAction()) {
				case MISSED:
					//the last ball holder was a shooter
					lastBallHolder.passAttemptedNullify();
					lastBallHolder.passFailed();
					lastBallHolder.shotAttempted();
					log("shot was missed by: " + lastBallHolder + " from team: " + teamStats);

					// blocked by what player in our team?

					break;
				case SCORED:
					//th least ball holder was a shooter
					lastBallHolder.passAttemptedNullify();
					lastBallHolder.passFailed();
					lastBallHolder.shotAttempted();
					lastBallHolder.shotMade();

					lastBallHolder.weight++;
					teamStats.favoriteShooters.remove(lastBallHolder);
					teamStats.favoriteShooters.add(lastBallHolder);
				
					lastPasser--;
					log("shot was made by: " + lastBallHolder + " from team: " + teamStats);
				// Continue to next case to do passers.
					break;
			case STOLEN:
				lastBallHolder.passFailed();
				log("pass is stolen from " + lastBallHolder);
				for(int i = 0; i < lastPasser; i++) {
					position = holders[i];
					lastBallHolder = opponents[position - 1]; // it's their index + 1
					lastBallHolder.weight++;
					teamStats.favoritePassers.remove(lastBallHolder);
					teamStats.favoritePassers.add(lastBallHolder);
				}
				break;
			default:
				log("START OF THE GAME?");
				break;
				
			}
		}
		
		if(game.selfGame) {
			holder =  picker.getBallHolder();
		} else {
			// Pick pos 3-5
			holder = gen.nextInt(3) + 3;
		}
		
		// Set status to holding until action.
		game.lastMove = new Move(holder, 0, Status.START);
		
		log("holder in pickAttack() is : " + holder);
		return holder;
	}

	/**
	 * Return
	 *  0 - For shoot
	 *  # - Of player to pass to
	 */
	@Override
	public int action(int[] defenders) {
		
		if(game.selfGame) {
			Move m = picker.action(defenders, attackingGame.lastMove);
			attackingGame.lastMove = m;
			return (m.action == Status.SHOOTING) ? 0 : m.toPlayer;
		}
		
		switch(attackingGame.lastMove.action) {
			case START:  
				// then we pass
				
				// Then we should be shooting
				
				// whether or not to pass again ?
				// decide
				// Also who to pass to??
				
				// last move is holder is passing to the new holder.
				
				int oldHolder = holder;
				
				holder = gen.nextInt(2) + 1;
				
				attackingGame.lastMove = new Move(oldHolder, holder, Status.PASSING);
				return holder;
				
				
			case PASSING:
				// We want to logs the success of a pass
				// from player x on defending player y
				
				// then we shoot.
				
				//lastMove = shooting move.
				
				attackingGame.lastMove = new Move(holder, 0, Status.SHOOTING);
				return 0;// return 0 cause we're shooting.
				
			case DEFENDING:
			case SHOOTING:
				// This should never happen.
				throw new IllegalArgumentException("Illegal status on action: " + attackingGame.lastMove.action);
		}
		
		return 0;
	}

	// Pick defend.
	@Override
	public int[] pickDefend(int yourScore, int opponentScore, int ballHolder, Round previousRound) {
		log("Called pickDefend()");
	

		// log("yourScore: " + yourScore + " ourScore: " + game.ourScore);
		if(game.selfGame) {
			log("Current perspective: " + currentPerspective);
			game = games[currentPerspective];
		}
		else{	
		log("our score: " + yourScore + " opponent score:" + opponentScore);
		}
		if(game.ourScore == -1) {
			// This is the first turn.
			game.ourScore = 0;
		} else if (game.ourScore == yourScore) {
			// so either pass is blocked
			// or shot failed.
			
	// //TRY TO GET THE SENSE OF previous Round
		// 	log("lastRound: " + previousRound);

		// 	int[] prevRoundDef=previousRound.defenders();
		// 	int[] prevRoundHol=previousRound.holders();
		// 	for (int p : prevRoundDef) {
		// 		log("Previous Round Defender:" + p);
		// 	}

	//TRY TO GET THE SENSE OF previous Round ENDS
	
		//Maybe depreciated
		// switch(game.lastMove.action) {
		// 		case PASSING:
		// 			// pass was blocked.

		// 			break;
		// 		case SHOOTING:
		// 			// shot was blocked.
		// 			// shot as blocked by:
		// 			// lastMove.theirPlayer;
		// 			// log that their player has the ability
		// 			// to block our shooter.
		// 			break;
		// 		default:
		// 			throw new IllegalArgumentException("Illegal status for defend:" + game.lastMove.action);
		// 	}
		}
		else {
			// supposedly can only go up.
			// Maybe we made a shot?
			game.ourScore = yourScore;
		}
		if(previousRound != null && !game.selfGame) //we only do this when playing with others
		{
			int lastPlayerNumber = currentPlayingTeam[holder-1];
			Player lastBallHolder= ourTeamStat.get(lastPlayerNumber-1);
			log("holder: " + holder);
			log("currentPlayingTeam[holder-1]: " + currentPlayingTeam[holder-1]);
			log("last Shooter Player Id: " + lastBallHolder);
			log("our current player ID:" + Arrays.toString(currentPlayingTeam));
			//Assume it's a passer and reject that if shooter in the case
			lastBallHolder.passAttempted();
			lastBallHolder.passMade();

			switch(previousRound.lastAction()) {
				case MISSED:
					//was shooter
					lastBallHolder.passAttemptedNullify();
					lastBallHolder.passFailed();

					lastBallHolder.shotAttempted();
					break;
				case SCORED:
					lastBallHolder.passAttemptedNullify();
					lastBallHolder.passFailed();
					
					lastBallHolder.shotAttempted();
					lastBallHolder.shotMade();
					break;
				case STOLEN:
					lastBallHolder.passFailed();
					break;
				default:
					log("START OF THE GAME?");
					break;
					
			}
		}

			
	
		
		// We're on defense so our last move is not defending.
		game.lastMove = new Move(0, 0, Status.DEFENDING);
		
		if(game.selfGame) {
			return picker.getDefenseMatch();
		}
		
		int[] defenders = new int [] {1,2,3,4,5};
		shuffle(defenders, gen);
		return defenders;
	}
	

	public interface TeamPicker {
		//intitalize the total # of players, # of games, and # of turns per game
		void initialize(int players, int games, int turns);

		//Returns the array of teams
		int[] pickTeam();

		//Returns the Move object based on the defenders & lastMove
		Move action(int[] defenders, Move lastMove);

		//Reports previous Rounds
		void reportLastRound(Round previousRound);
		
		//Returns the array of defense matchup
		int[] getDefenseMatch();

		//Returns who will be the starting player of the game
		int getBallHolder();
		
		//Returns the list of best shooters
		List<Integer> getBestShooters();

		//Returns the list of best passers
		List<Integer> getBestPassers();

		//Returns the list of best Blockers
		List<Integer> getBestBlocker();

		//Returns the list of best Interceptor
		List<Integer> getBestInterceptor();

		//Returns the list of player stats
		public List<Player> getPlayerStat();

		//Returns the extra Infomariton
		void printExtraInfo();
		
		//logger
		void setLogger(Logger logger);
	}
	
	public static class PivotTeamPicker implements TeamPicker {
		
		private int totalPlayers;
		private int games;
		private int turns;
		private int firstPivot;
		private int secondPivot;
		
		private double[][] shotsMade;
		private double[][] shotsTaken;
		
		private int[] totalShotsTaken = new int[2];
		private int[] totalShotsMade = new int[2];
		
		private double[][] passMade;
		private double[][] passTaken;


		private int[] teamA = new int[TEAM_SIZE];
		private int[] teamB = new int[TEAM_SIZE];
		
		private int shooter = -1;
		private int changeShooter = 0;
		
		private int pickingTeam; // Changes every game twice.
		private int currentPlayer; //
		
		private int pickingDefense; // Changes every turn.
		
		private Logger logger = DEFAULT_LOGGER;
		
		public double[][] getShotsMade(){return shotsMade; }
		public double[][] getShotsTaken(){return shotsTaken; }
		public double[][] getPassMade(){return passMade; }
		public double[][] getPassTaken(){return passTaken; }
		public int[] getTotalShotsTaken(){return totalShotsTaken; }
		public int[] getTotalShotsMade(){return totalShotsMade; }

		@Override
		public void printExtraInfo(){
			logger.log("FIRST PIVOT: " + firstPivot);
			logger.log("SECOND PIVOT: " + secondPivot);
		}
		
		@Override
		public void initialize(int players, int games, int turns) {
			this.totalPlayers = players;
			this.games = games;
			this.turns = turns;

			shotsMade = new double[2][players];
			shotsTaken = new double[2][players];
			
			passMade = new double[2][players];
			passTaken = new double[2][players];
			
			firstPivot = gen.nextInt(players) + 1;
			secondPivot = firstPivot;
			
			while(secondPivot == firstPivot) {
				secondPivot = gen.nextInt(players) + 1;
			}
			
			logger.log("First pivot is: " + firstPivot);
			logger.log("Second pivot is: " + secondPivot);
			currentPlayer = 1;
			while(currentPlayer == firstPivot || currentPlayer == secondPivot) {
				currentPlayer++;
			}


		}

		@Override
		public int[] pickTeam() {
	


			int curPos = currentPlayer;
			
			int[] team = null;
			if(pickingTeam++ % 2 == 0) { //this 
				team = teamA;
				team[0] = firstPivot;
				logger.log("Team A...");
			} else {
				team = teamB;
				team[0] = secondPivot;
				logger.log("Team B...");
			}
			
			for(int i = 1; i < TEAM_SIZE;) {
				if(curPos != firstPivot && curPos != secondPivot) {
					team[i] = curPos;
					i++;
				}
				
				curPos = ++curPos % totalPlayers;
				if(curPos == 0) {
					curPos = totalPlayers;
				}
			}
			
			logger.log("Team: " + Arrays.toString(team));
			
			currentPlayer = curPos;
			
			return team;
		}

		@Override
		public int getBallHolder() {
			if(changeShooter == 0) {
				shooter = ++shooter % TEAM_SIZE; //shooter variabl is an index !!!!
			}
			changeShooter = ++changeShooter % 2;
			
			int[] players = null;
			if(pickingDefense == 0) {
				players = teamA;
			} else {
				players = teamB;
			}
			pickingDefense = ++pickingDefense % 2;


			int ballHolder = ((shooter + 1) % TEAM_SIZE) + 1;
			logger.log(whatTeam("attack") + ": Picker: ballHolder: [playerID]: "+ players[ballHolder-1] + " | [sim#]: " + ballHolder);
			return ballHolder; //we return the position of ball holder here [simulation #]
		}
		
		@Override
		public Move action(int[] defenders, Move lastMove) {
			Move move = null;
			
			int[] players = null;
			if(pickingDefense == 0) {
				players = teamB;
			} else {
				players = teamA;
			}
				
			int pivot=0;
			if(whatTeam("attack").equals("Team A"))
				pivot=1;
			else
				pivot=0;
			
			switch(lastMove.action) {
				case START:
					// do the pass.
					int nextHolder = shooter + 1;
					logger.log(whatTeam("attack") + ": Passing to --> playerID " + players[shooter] + " ( [Sim#]: " + nextHolder + ") ");
					move = new Move(lastMove.ourPlayer, nextHolder, Status.PASSING);
					//Log the pass -Jiang
					passTaken[pivot][players[lastMove.ourPlayer-1]-1]++;
					break;
				case PASSING:
					// Shoot
					logger.log("Shooting..." + " from " + whatTeam("attack"));
					move = new Move(lastMove.ourPlayer, 0, Status.SHOOTING);
					//Log the pass -Jiang
					passMade[pivot][players[lastMove.ourPlayer-1]-1]++;

					break;
				default:
					throw new IllegalArgumentException("Invalid status: " + lastMove.action);
				
			}
			
			return move;
		}

		@Override
		public void reportLastRound(Round previousRound) {
			// Because the first turn doesn't have previous round.
			// previousRound.attacksA = A is attacking
			if(previousRound != null) {
				int pivot = 0;
				int[] offTeam = null;
				int[] defTeam = null;
				
				logger.log("AttakcsA: " + previousRound.attacksA);
				if(!previousRound.attacksA) {
					
					// B is attacking A.
					pivot = 0;
					offTeam = teamB;
					defTeam = teamA;
				} else {
					// A is attacking B.
					pivot = 1;
					offTeam = teamA;
					defTeam = teamB;
				}
				
				// crunch da numbers.
				
				// 1 passes to 2
				// and 2 shoots
				// ballholder[] = {1, 2};
				// last action?
				
				int holders[] = previousRound.holders();
				logger.log("holders array: " + Arrays.toString(holders));
				
				int shooter = holders[holders.length - 1];
				int playerId = offTeam[shooter -1];
				int passer=holders[0];
				int passerPlayerId=offTeam[passer - 1];
				int playerIndex = playerId - 1;

				
				switch(previousRound.lastAction()) {
				
					case SCORED:
						shotsMade[pivot][playerIndex]++;
						totalShotsMade[pivot]++;
					case MISSED:
						shotsTaken[pivot][playerIndex]++;
						totalShotsTaken[pivot]++;
						break;
					default:
						// dont care.
				}
				
				logger.log("Pivot 1: " + Arrays.toString(shotsMade[0]));
				logger.log("Pivot 1: " + Arrays.toString(shotsTaken[0]));
				logger.log("Pivot 2: " + Arrays.toString(shotsMade[1]));
				logger.log("Pivot 2: " + Arrays.toString(shotsTaken[1]));
				
				logger.log("Passing Succeed Team A: " + Arrays.toString(passMade[1]));
				logger.log("Passing Attempt Team A: " + Arrays.toString(passTaken[1]));
				logger.log("Passing Succeed Team B: " + Arrays.toString(passMade[0]));
				logger.log("Passing Attempt Team B: " + Arrays.toString(passTaken[0]));

			}
		}

		@Override
		public int[] getDefenseMatch() {
			
			int[] match = new int[TEAM_SIZE];
			
			for(int i = 0; i < TEAM_SIZE; i++) {
				int offPos = ((i + shooter) % TEAM_SIZE);
				match[offPos] = i + 1;
			}

			logger.log("Picker: DefMatch: " + Arrays.toString(match) + " of " + whatTeam("defend"));
			return match;
		}
		
		public void setLogger(Logger logger) {
			this.logger = logger;
		}

		public String whatTeam(String attackOrDefend){
			//depending on the state of the game, returns the attacking team 

				if(attackOrDefend.equals("attack"))
					return ( changeShooter == 0 )? "Team A" : "Team B" ;
				else
					return ( changeShooter == 0 )? "Team B" : "Team A" ;

		}

		@Override
		public List<Integer> getBestShooters() {
			
			// logger.log("Pivot 1: " + Arrays.toString(shotsMade[0]));
			// logger.log("Pivot 1: " + Arrays.toString(shotsTaken[0]));
			// logger.log("Pivot 2: " + Arrays.toString(shotsMade[1]));
			// logger.log("Pivot 2: " + Arrays.toString(shotsTaken[1]));
			// logger.log("Def1: " + ( 1.0 / (totalShotsMade[0] * 1.0 / totalShotsTaken[0])));
			// logger.log("Def2: " + ( 1.0 / (totalShotsMade[1] * 1.0 / totalShotsTaken[1])));
			
			PriorityQueue<Player> shooterQueue = new PriorityQueue<Player>(12);
			
			Player player = null;
			
			double def1Weight= totalShotsTaken[0]/ totalShotsMade[0];
			double def2Weight= totalShotsTaken[1]/ totalShotsMade[1];
			logger.log("Def1: " + def1Weight);
			logger.log("Def2: " + def2Weight);


			double averageWeight1 = 0;
			double averageWeight2 = 0;
			
			double weight1;
			double weight2;
			for(int i = 0; i < totalPlayers; i++) {
				int playerId = i + 1;
				if(playerId == firstPivot || playerId == secondPivot) {
					continue;
				}
				
				player = new Player(playerId);
				weight1 = 0;
				weight2 = 0;
				
				if(shotsTaken[0][i] != 0) {
					weight1 = shotsMade[0][i] / shotsTaken[0][i];
				}
				if(shotsTaken[1][i] != 0) {
					weight2 = shotsMade[1][i] / shotsTaken[1][i];
				}
				
				averageWeight1 += weight1;
				averageWeight2 += weight2;
				
				// player.weight = weight1 + weight2;
				player.weight = def1Weight * weight1 + def2Weight*weight2;
				
				logger.log("Player " + playerId + ": " + player.weight);
				
				shooterQueue.add(player);
			}
			
			averageWeight1 /= 10;
			averageWeight2 /= 10;
			
			// first pivot weight.
			player = new Player(firstPivot);
			weight1 = (shotsMade[1][firstPivot-1] / shotsTaken[1][firstPivot-1]);
			weight2 = averageWeight2 * (weight1 / averageWeight1 );
			
			// logger.log("---FIRST PIVOT ----");
			// logger.log("weight1: " + weight1);
			// logger.log("weight2: " + weight2);
			// logger.log("weight: " + (weight1 + weight2));
			player.weight = weight1 + weight2;
			shooterQueue.add(player);
			
			player = new Player(secondPivot);
			weight1 = (shotsMade[0][secondPivot-1] / shotsTaken[0][secondPivot-1]);
			weight2 = averageWeight1 * (weight1 / averageWeight2 );
			
			// logger.log("---SECOND PIVOT ----");
			// logger.log("weight1: " + weight1);
			// logger.log("weight2: " + weight2);
			// logger.log("weight: " + (weight1 + weight2));
			player.weight = weight1 + weight2;
			shooterQueue.add(player);
			
			// logger.log("average1: " + averageWeight1);
			// logger.log("average2: " + averageWeight2);
			
			List<Integer> shooters = new ArrayList<Integer>(12);
			while(shooterQueue.size() > 0) {
				shooters.add(shooterQueue.poll().playerId);
			}
			
			return shooters;
		}

		@Override
		public List<Integer> getBestPassers(){
			// logger.log("Pivot 1: " + Arrays.toString(passMade[0]));
			// logger.log("Pivot 1: " + Arrays.toString(passTaken[0]));
			// logger.log("Pivot 2: " + Arrays.toString(passMade[1]));
			// logger.log("Pivot 2: " + Arrays.toString(passTaken[1]));

			int totalPassMade1=0;
			int totalPassMade2=0;
			int totalPassTaken1=0;
			int totalPassTaken2=0;


			for(int i=0; i < passMade[0].length; i++){
				totalPassMade1 += passMade[0][i];
				totalPassMade2 += passMade[1][i];

				totalPassTaken1 += passTaken[0][i];
				totalPassTaken2 += passTaken[1][i];

			}

			// logger.log("totalPassMade1: " + totalPassMade1);
			// logger.log("totalPassTaken1: " + totalPassTaken1);

			// logger.log("totalPassMade2: " + totalPassMade2);
			// logger.log("totalPassTaken2: " + totalPassTaken2);
			
			double block1Factor = ( (double) totalPassTaken1 ) / ((double) totalPassMade1) ;
			double block2Factor = ( (double) totalPassTaken2 ) / ((double) totalPassMade2) ;
			// logger.log("blocker1: " + block1Factor);
			// logger.log("blocker2: " + block2Factor);
			
			PriorityQueue<Player> passerQueue = new PriorityQueue<Player>(totalPlayers);
			
			Player player = null;
			
			double averageWeight1 = 0;
			double averageWeight2 = 0;
			
			double weight1;
			double weight2;
			for(int i = 0; i < totalPlayers; i++) {
				int playerId = i + 1;
				if(playerId == firstPivot || playerId == secondPivot) {
					continue;
				}
				
				player = new Player(playerId);
				weight1 = 0;
				weight2 = 0;
				
				if(shotsTaken[0][i] != 0) {
					weight1 = passMade[0][i] / passTaken[0][i];
				}
				if(shotsTaken[1][i] != 0) {
					weight2 = passMade[1][i] / passTaken[1][i];
				}
				
				averageWeight1 += weight1;
				averageWeight2 += weight2;
				
				// player.weight = weight1 + weight2;
				player.weight = block1Factor * weight1 + block2Factor*weight2;
				
				logger.log("Player " + playerId + ": " + player.weight);
				
				passerQueue.add(player);
			}
			
			//WEIGHT FOR PIVOTS..cuz they are one data points short of others
			averageWeight1 /= 10;
			averageWeight2 /= 10;
			
			// first pivot weight.
			// PIVOT1 and PIVOT 2 have to be 
			player = new Player(firstPivot);
			// logger.log("" + passMade[0][firstPivot-1]);
			// logger.log("" + passTaken[0][firstPivot-1]);
			// logger.log("" + Arrays.toString(passMade[0]));
			// logger.log("" + Arrays.toString(passMade[1]));
			
			
			weight1 = (passMade[0][firstPivot-1] / passTaken[0][firstPivot-1]);
			weight2 = averageWeight2 * (weight1 / averageWeight1 );
			
			// logger.log("---FIRST PIVOT ----");
			// logger.log("weight1: " + weight1);
			// logger.log("weight2: " + weight2);
			// logger.log("weight: " + (weight1 + weight2));
			player.weight = weight1 + weight2;
			passerQueue.add(player);

			//FOR SECOND PLAYER
			player = new Player(secondPivot);
			weight1 = (passMade[1][secondPivot-1] / passTaken[1][secondPivot-1]);
			weight2 = averageWeight1 * (weight1 / averageWeight2 );
			
			// logger.log("---SECOND PIVOT ----");
			// logger.log("weight1: " + weight1);
			// logger.log("weight2: " + weight2);
			// logger.log("weight: " + (weight1 + weight2));
			player.weight = weight1 + weight2;
			passerQueue.add(player);
			
			// logger.log("average1: " + averageWeight1);
			// logger.log("average2: " + averageWeight2);
			
			List<Integer> passers = new ArrayList<Integer>(12);
			while(passerQueue.size() > 0) {
				passers.add(passerQueue.poll().playerId);
			}
			
			return passers;
		}


		//Returns the list of best Blockers
		@Override
		public List<Integer> getBestBlocker(){
			List<Integer> blockerList = new ArrayList<Integer>();
			return blockerList;
		}

		//Returns the list of best Interceptor
		@Override
		public List<Integer> getBestInterceptor(){
			List<Integer> interceptor = new ArrayList<Integer>();
			// int[] interceptros = new int[5];
			// Arrays.fill(interceptros,0);
			return interceptor; 

		}

		@Override
		public List<Player> getPlayerStat(){
			//initialize our team stat 
			ArrayList<Player> theTeamStat = new ArrayList<Player>();
			for(int p=0; p < totalPlayers; p++){
				Player player = new Player(p+1,"our team");
				
				for(int numPivot=0; numPivot<2; numPivot++){
					player.numShotAttempted+=shotsTaken[numPivot][p];
					player.numShotMade+=shotsMade[numPivot][p];
					player.numPassAttempted+=passTaken[numPivot][p];
					player.numPassMade+=passMade[numPivot][p];
				}
				theTeamStat.add(p,player);
			}
			return theTeamStat;
		}


	}
	
	// public static class Player implements Comparable<Player>{
	// 	public int playerId;
	// 	public String team;
	// 	public double weight;
		
	// 	// Players ability
	// 	// 1. Shooting ability
	// 	public double numShotMade;
	// 	public double numShotAttempted;
	// 	// 2 .Blocking ability
	// 	public double numBlockMade;
	// 	public double numBlockAttempted;
	// 	// 3. Passing ability
	// 	public double numPassMade;
	// 	public double numPassAttempted;
	// 	// 4. Intercepting ability
	// 	public double numInterceptMade; 
	// 	public double numInterceptAttempted; 
		
	// 	public Player(int id) {
	// 		this.playerId = id;
	// 		numShotMade=0;
	// 		numShotAttempted=0;
	// 		numBlockMade=0;
	// 		numBlockAttempted=0;
	// 		numPassMade=0;
	// 		numPassAttempted=0;
	// 		numInterceptMade=0;
	// 		numInterceptAttempted=0;
	// 		this.team="unknown";
	// 	}
	// 	public Player(int id, String team) {
	// 		this.playerId = id;
	// 		numShotMade=0;
	// 		numShotAttempted=0;
	// 		numBlockMade=0;
	// 		numBlockAttempted=0;
	// 		numPassMade=0;
	// 		numPassAttempted=0;
	// 		numInterceptMade=0;
	// 		numInterceptAttempted=0;
	// 		this.team=team;
	// 	}

	// 	@Override
	// 	public int compareTo(Player other) {
	// 		return 	(weight > other.weight) ? -1 :
	// 				(weight < other.weight) ?  1 : 0 ;
	// 	}

	// 	public String toString(){
	// 		return "Player #: " + Integer.toString(playerId) + " from team [" + team +"]";
			
	// 	}

	// 	//the player has taken shot
	// 	public void shotMade(){numShotMade++; }
	// 	public void shotAttempted(){numShotAttempted++; } 
	// 	public void shotFailed(){numShotMade--;}
	// 	public void blockMade(){numBlockMade++;}
	// 	public void blockAttempted(){numBlockAttempted++;}
	// 	public void blockFailed(){numBlockMade--;}
	// 	public void passMade(){numPassMade++;}
	// 	public void passAttempted(){numPassAttempted++;}
	// 	public void passFailed(){numPassMade--;}		
	// 	public void interceptMade(){numInterceptMade++;}
	// 	public void interceptAttempted(){numInterceptAttempted++;}
	// 	public void interceptFailed(){numInterceptMade--;}
	// 	public void passAttemptedNullify(){numPassAttempted--;}
	// }


	public static final Logger DEFAULT_LOGGER = new Logger() {
		@Override
		public void log(String message) {
			System.out.println(message);
		}
	};
	
	
}
