package hoop.g1;

import hoop.sim.Game.Round;
import hoop.sim.Hoop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import java.util.Random;

public class Team implements hoop.sim.Team, Logger {
	
	private static int versions;
	
	private static final Random gen = new Random();
	private static final boolean DEBUG = false;
	private static final int TEAM_SIZE = 5;
	
	protected Map<String, OtherTeam> name2OtherTeam = new HashMap<String, OtherTeam>();
	private HistoryAnalyzer historyAnalyzer = new HistoryAnalyzer(this);
	{
		historyAnalyzer.setLogger(this);
	}
	
	private boolean startedTournament;

	private static int totalPlayers;
	
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

	private Player[] currentPlayingTeam = new Player[TEAM_SIZE];
	private Player[] currentOpponentTeam = new Player[TEAM_SIZE];
	
	public OtherTeam ourTeamPointer;
	private OtherTeam otherTeamPointer;

	private TeamPicker picker;
	private Game game;
	private int currentPerspective; // TODO: Check if needed.
	private Game[] games = new Game[2];

	private Game attackingGame; /// TODO: Check if needed
	
	

	//This array changes based on what team we play against

	private List<Player> ourPlayers;
	
	private List<Integer> bestShooters; // TODO; Thsi should be removed and
	private List<Integer> bestPassers;  // TODO: replaced with history analyzer.

	@Override
	public void opponentTeam(int[] opponentPlayers) {
		log("Called opponentTeam()");
		// We're told what players were picked to play us. 123
		// Keep track of these players for the game Jose & Jiang & Albert
		if(game.selfGame) {
			return;
		}
		//need to store that info to other team
		
		for(int i = 0; i < TEAM_SIZE; i++) {
			//intilize the opponents(array of Players bojects)
			currentOpponentTeam[i] = otherTeamPointer.getPlayer(opponentPlayers[i]);
			currentOpponentTeam[i].positionId = i + 1;
			log("OPPOSITE TEAM: " + opponentPlayers[i]);
			log(currentOpponentTeam[i].toString());
			
		}
		log("----------------- opponentTeam()ENDS----------------");
	}

	@Override
	public int[] pickTeam(String opponent, int totalPlayers, hoop.sim.Game[] history) {

		log("------------ pickTeam() call STARTS HERE---------");
		log("Called pickTeam() with opponent: " + opponent + " with tP: " + totalPlayers);
		// Here we find out how many players we have for the game.
		
		Team.totalPlayers = totalPlayers;
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
				
				ourPlayers = picker.getPlayers();
				
				ourTeamPointer = new OtherTeam(name(), totalPlayers);
				name2OtherTeam.put(name(), ourTeamPointer);
				ourTeamPointer.setPlayerList(ourPlayers);
				
				// TODO: Remove this and pull from history analyzer.
				bestShooters = picker.calculateShooters();
				bestPassers = picker.calculatePassers();
				picker.calculateBlockers();
				
				log("ESTIMATION BASED ON TWO PIVOTS: ");
				// picker.printExtraInfo();
				log("Shooters: "  + bestShooters);
				log("------------Passer INFO STARTS HERE-----------");
				log("Passers: " +  bestPassers);
				log("-----------PASSER INFO ENDS HERE------------");
			}
			
			historyAnalyzer.analyzeGameHistory(history,totalPlayers);
		}
		
		// First initialize the scores.
		game.ourScore = -1;
		game.theirScore = 0;
		if(game.selfGame) {
			if(picker == null) {
				picker = new PivotTeamPicker();
				picker.setLogger(this);
				picker.initialize(totalPlayers, 10, Hoop.gameTurns());
			}
			
			return picker.pickTeam();
			
		} else {
			
			int[] team = new int[5];
			// Pick 2 shooters.
			List<Player> bestOverall = new ArrayList<Player>(ourPlayers);
			
			Collections.sort(bestOverall, SORT_BY_OVERALL);
			
			for(int i = 0; i < TEAM_SIZE; i++) 
				team[i] = bestOverall.get(i).playerId;
			
			log("Choosing team: " + Arrays.toString(team));
			
			// This is needed since there's no game to instantiate
			// first game teams off of.
			if((otherTeamPointer = name2OtherTeam.get(opponent)) == null) {
				otherTeamPointer = new OtherTeam(opponent, totalPlayers);
				name2OtherTeam.put(opponent, otherTeamPointer);
			}
			
			log("WE ARE COMPETING AGAINST: " + otherTeamPointer.getName());

			log("Choosing team: " + Arrays.toString(currentPlayingTeam));
			
			int playerId;
			for(int i = 0; i < TEAM_SIZE; i++) {
				playerId = team[i];
				
				currentPlayingTeam[i] = ourPlayers.get(playerId - 1);
				currentPlayingTeam[i].positionId = i + 1;
			}
			
			log("------------ pickTeam() call ENDS HERE---------");
			return team;
			
		}
	}

	@Override
	public int pickAttack(int yourScore, int opponentScore, Round previousRound) {
		if(game.selfGame) {
			picker.reportLastRound(previousRound);
		} else if(previousRound != null) {
			if(previousRound.attacksB) {
				historyAnalyzer.analyzeRound(previousRound, currentPlayingTeam, currentOpponentTeam);
			} else {
				historyAnalyzer.analyzeRound(previousRound, currentOpponentTeam, currentPlayingTeam);
			}
		}
		
		log("Called pickAttack()");
		log("yourScore: " + opponentScore+ " ourScore: " + yourScore);
		if(game.selfGame) {
			game = games[currentPerspective];
			attackingGame = game;
			currentPerspective = (currentPerspective + 1) % 2;
			holder =  picker.getBallHolder();
		} else {
			holder = pickBestPasser();
		}
		
		// Set status to holding until action.
		game.lastMove = new Move(holder, 0, Status.START);
		
		log("holder in pickAttack() is : " + holder);
		return holder;
	}
	
	public int pickBestPasser() {
		Player bestPasser = currentPlayingTeam[0];
		for(Player next : currentPlayingTeam) {
			if(next.getPassingWeight() > bestPasser.getPassingWeight()) {
				bestPasser = next;
			}
		}
		
		return bestPasser.positionId;
	}

	/**
	 * Return
	 *  0 - For shoot
	 *  # - Of player to pass to
	 */
	@Override
	public int action(int[] defenders) {
		// defenders indexed by off position
		
		log("Defenders: " + Arrays.toString(defenders));
		
		if(game.selfGame) {
			Move m = picker.action(defenders, attackingGame.lastMove);
			attackingGame.lastMove = m;
			return (m.action == Status.SHOOTING) ? 0 : m.toPlayer;
		}
		
		switch(attackingGame.lastMove.action) {
			case START:  
				int oldHolder = holder;
				holder = searchShotMismatch(defenders, oldHolder);
				
				attackingGame.lastMove = new Move(oldHolder, holder, Status.PASSING);
				return holder;
				
				
			case PASSING:
				
				attackingGame.lastMove = new Move(holder, 0, Status.SHOOTING);
				return 0;// return 0 cause we're shooting.
				
			case DEFENDING:
			case SHOOTING:
				// This should never happen.
				throw new IllegalArgumentException("Illegal status on action: " + attackingGame.lastMove.action);
		}
		
		return 0;
	}
	
	public int searchShotMismatch(int[] defenders, int ballHolder) {
		int bestPositionId = 0;
		double bestShootingWeight = 0;
		
		for(int i = 0; i < TEAM_SIZE; i++) {
			if(i + 1 == ballHolder) {
				continue;
			}
			
			Player p = ourPlayers.get(getPlayerForPosition(i + 1) - 1);
			
			if(p.getShootingWeight() > bestShootingWeight) {
				bestPositionId = p.positionId;
				bestShootingWeight = p.getShootingWeight();
			}
		}
		
		return bestPositionId;
	}
	
	public int getPlayerForPosition(int position) {
		return currentPlayingTeam[position - 1].playerId;
	}
	
	public int getOpponentForPosition(int position) {
		return currentOpponentTeam[position - 1].playerId;
	}

	// Pick defend.
	@Override
	public int[] pickDefend(int yourScore, int opponentScore, int ballHolder, Round previousRound) {
		log("Called pickDefend()");

		// log("yourScore: " + yourScore + " ourScore: " + game.ourScore);
		if(game.selfGame) {
			log("Current perspective: " + currentPerspective);
			game = games[currentPerspective];
		} else if(previousRound != null) {
			log("our score: " + yourScore + " opponent score:" + opponentScore);
			if(previousRound.attacksA) {
				historyAnalyzer.analyzeRound(previousRound, currentPlayingTeam, currentOpponentTeam);
			} else {
				historyAnalyzer.analyzeRound(previousRound, currentOpponentTeam, currentPlayingTeam);
			}
		}
		
		// We're on defense so our last move is not defending.
		game.lastMove = new Move(0, 0, Status.DEFENDING);
		
		if(game.selfGame) {
			return picker.getDefenseMatch();
		}
		
		List<Player> players = new ArrayList<Player>(TEAM_SIZE);
		players.addAll(Arrays.asList(currentPlayingTeam));
		
		Collections.sort(players, SORT_BY_BLOCKING);
		
		List<Player> opponentPlayers = new ArrayList<Player>(TEAM_SIZE);
		opponentPlayers.addAll(Arrays.asList(currentOpponentTeam));
		
		Collections.sort(opponentPlayers, SORT_BY_SHOOTING);
		
		int[] defenders = new int [5];
		for(int i = 0; i < TEAM_SIZE; i++) {
			defenders[opponentPlayers.get(i).positionId - 1] = players.get(i).positionId;  
		}
		
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
		
		List<Player> getPlayers();
		
		//Returns the list of best shooters
		List<Integer> calculateShooters();

		//Returns the list of best passers
		List<Integer> calculatePassers();

		//Returns the list of best Blockers
		List<Integer> calculateBlockers();

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
		
		enum TrainStatus{
			Shooting, Blocking
		}
		private int totalPlayers;
		private int games;
		private int turns;
		private int firstPivot;
		private int secondPivot;
		
		private List<Player> players;
		
		private double[][] shotsMade;
		private double[][] shotsTaken;
		
		private double[][] blocksMade;
		private double[][] blocksTaken;
		
		private int[] totalShotsTaken = new int[2];
		private int[] totalShotsMade = new int[2];
		
		private int[] totalBlocksTaken = new  int[2];
		private int[] totalBlocksMade = new int[2];
		
		private double[][] passMade;
		private double[][] passTaken;


		private int[] teamA = new int[TEAM_SIZE];
		private int[] teamB = new int[TEAM_SIZE];
		
		private int testingIndex = -1;
		private int changeTester = 0;
		
		private int pickingTeam; // Changes every game twice.
		private int currentPlayer; //
		
		private int pickingDefense; // Changes every turn.
		
		private Logger logger = DEFAULT_LOGGER;
		
		public double[][] getShotsMade(){return shotsMade; }
		public double[][] getShotsTaken(){return shotsTaken; }
		public double[][] getPassMade(){return passMade; }
		public double[][] getPassTaken(){return passTaken; }
		public double[][] getBlocksMade(){return blocksMade; }
		public double[][] getBlocksTaken(){return blocksTaken; }
		public int[] getTotalShotsTaken(){return totalShotsTaken; }
		public int[] getTotalShotsMade(){return totalShotsMade; }
		public int[] getTotalBlocksTaken(){return totalBlocksTaken; }
		public int[] getTotalBlockssMade(){return totalBlocksMade; }

		@Override
		public void printExtraInfo(){
			logger.log("FIRST PIVOT: " + firstPivot);
			logger.log("SECOND PIVOT: " + secondPivot);
		}
		
		@Override
		public List<Player> getPlayers() {
			return players;
		}
		
		@Override
		public void initialize(int totalPlayers, int games, int turns) {
			this.totalPlayers = totalPlayers;
			this.games = games;
			this.turns = turns;

			shotsMade = new double[2][totalPlayers];
			shotsTaken = new double[2][totalPlayers];
			
			blocksMade = new double[2][totalPlayers];
			blocksTaken = new double[2][totalPlayers];
			
			passMade = new double[2][totalPlayers];
			passTaken = new double[2][totalPlayers];
			
			firstPivot = gen.nextInt(totalPlayers) + 1;
			secondPivot = firstPivot;
			
			while(secondPivot == firstPivot) {
				secondPivot = gen.nextInt(totalPlayers) + 1;
			}
			
			logger.log("First pivot is: " + firstPivot);
			logger.log("Second pivot is: " + secondPivot);
			currentPlayer = 1;
			while(currentPlayer == firstPivot || currentPlayer == secondPivot) {
				currentPlayer++;
			}
			
			players = new ArrayList<Player>(totalPlayers);
			for(int i = 0; i < totalPlayers; i++) {
				players.add(new Player(i + 1));
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
			
			if(changeTester == 0) {
				testingIndex = ++testingIndex % TEAM_SIZE; //shooter variable is an index !!!!
			}
			
			if(testingIndex == 0) {
				changeTester = ++changeTester % 2;
			} else {
				changeTester = ++changeTester % 4;
			}
			
			int[] players = null;
			if(pickingDefense == 0) {
				players = teamA;
			} else {
				players = teamB;
			}
			pickingDefense = ++pickingDefense % 2;

			int ballHolder = ((testingIndex + 1) % TEAM_SIZE) + 1;
			
			if(ballHolder == 1) {
				ballHolder++;
			}
			
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
					int nextHolder = 0;
					if(changeTester < 2) {
						nextHolder = testingIndex + 1;
					} else {
						nextHolder = 1;
					}
					
					logger.log(whatTeam("attack") + ": Passing to --> playerID " + players[testingIndex] + " ( [Sim#]: " + nextHolder + ") ");
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
				int shooterIndex = playerId - 1;
				int blockerPosition = testingIndex;
				int blockerId = defTeam[blockerPosition];
				int blockerIndex = blockerId - 1;

				
				if(changeTester < 2) {
					switch(previousRound.lastAction()) {
						case SCORED:
							shotsMade[pivot][shooterIndex]++;
							totalShotsMade[pivot]++;
						case MISSED:
							shotsTaken[pivot][shooterIndex]++;
							totalShotsTaken[pivot]++;
							break;
						default:
							// dont care.
					}
				}
				
				if(changeTester >= 2 || shooter == 1){
					int shootingPivot = (pivot + 1) % 2;
					switch(previousRound.lastAction()) {
						case MISSED:
							blocksMade[shootingPivot][blockerIndex]++;
							totalBlocksMade[shootingPivot]++;
						case SCORED:
							blocksTaken[shootingPivot][blockerIndex]++;
							totalBlocksTaken[shootingPivot]++;
							break;
						default:
						// dont care.
					}
				}
				
				logger.log("Shots Made  P1: " + Arrays.toString(shotsMade[0]));
				logger.log("Shots Taken P1: " + Arrays.toString(shotsTaken[0]));
				logger.log("Shots Made  P2: " + Arrays.toString(shotsMade[1]));
				logger.log("Shots Taken P2: " + Arrays.toString(shotsTaken[1]));
				
				logger.log("blocks Made  P1: " + Arrays.toString(blocksMade[0]));
				logger.log("blocks Taken P1: " + Arrays.toString(blocksTaken[0]));
				logger.log("blocks Made  P2: " + Arrays.toString(blocksMade[1]));
				logger.log("blocks Taken P2: " + Arrays.toString(blocksTaken[1]));
				
				logger.log("Passing Succeed Team A: " + Arrays.toString(passMade[1]));
				logger.log("Passing Attempt Team A: " + Arrays.toString(passTaken[1]));
				logger.log("Passing Succeed Team B: " + Arrays.toString(passMade[0]));
				logger.log("Passing Attempt Team B: " + Arrays.toString(passTaken[0]));

			}
		}

		@Override
		public int[] getDefenseMatch() {
			
			int[] match = new int[TEAM_SIZE];
			
			if(changeTester < 2) {
				for(int i = 0; i < TEAM_SIZE; i++) {
					int offPos = ((i + testingIndex) % TEAM_SIZE);
					match[offPos] = i + 1;
				}
			} else {
				for(int i = 0; i < TEAM_SIZE; i++) {
					int offPos = ((i + testingIndex) % TEAM_SIZE);
					match[i] = offPos + 1;
				}
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
					return ( changeTester == 0 )? "Team A" : "Team B" ;
				else
					return ( changeTester == 0 )? "Team B" : "Team A" ;

		}

		@Override
		public List<Integer> calculateShooters() {
			
			// logger.log("Pivot 1: " + Arrays.toString(shotsMade[0]));
			// logger.log("Pivot 1: " + Arrays.toString(shotsTaken[0]));
			// logger.log("Pivot 2: " + Arrays.toString(shotsMade[1]));
			// logger.log("Pivot 2: " + Arrays.toString(shotsTaken[1]));
			// logger.log("Def1: " + ( 1.0 / (totalShotsMade[0] * 1.0 / totalShotsTaken[0])));
			// logger.log("Def2: " + ( 1.0 / (totalShotsMade[1] * 1.0 / totalShotsTaken[1])));
			
			PriorityQueue<Player> shooterQueue = 
					new PriorityQueue<Player>(totalPlayers, SORT_BY_SHOOTING);
			
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
				
				player = players.get(i);
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
				player.preShootingWeight = def1Weight * weight1 + def2Weight*weight2;
				
				logger.log("Player " + playerId + ": " + player.preShootingWeight);
				
				shooterQueue.add(player);
			}
			
			averageWeight1 /= 10;
			averageWeight2 /= 10;
			
			// first pivot weight.
			player = players.get(firstPivot - 1);
			if(shotsTaken[1][firstPivot-1] == 0) {
				weight1 = 0;
			} else {
				weight1 = (shotsMade[1][firstPivot-1] / shotsTaken[1][firstPivot-1]);
			}
			weight2 = averageWeight2 * (weight1 / averageWeight1 );
			
			// logger.log("---FIRST PIVOT ----");
			// logger.log("weight1: " + weight1);
			// logger.log("weight2: " + weight2);
			// logger.log("weight: " + (weight1 + weight2));
			player.preShootingWeight = weight1 + weight2;
			shooterQueue.add(player);
			
			player = players.get(secondPivot - 1);
			if(shotsTaken[0][secondPivot-1] == 0) {
				weight1 = 0;
			} else {
				weight1 = (shotsMade[0][secondPivot-1] / shotsTaken[0][secondPivot-1]);
			}
			weight2 = averageWeight1 * (weight1 / averageWeight2 );
			
			// logger.log("---SECOND PIVOT ----");
			// logger.log("weight1: " + weight1);
			// logger.log("weight2: " + weight2);
			// logger.log("weight: " + (weight1 + weight2));
			player.preShootingWeight = weight1 + weight2;
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
		public List<Integer> calculatePassers(){
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
			
			PriorityQueue<Player> passerQueue = 
					new PriorityQueue<Player>(totalPlayers, SORT_BY_PASSING);
			
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
				
				player = players.get(i);
				weight1 = 0;
				weight2 = 0;
				
				if(passTaken[0][i] != 0) {
					weight1 = passMade[0][i] / passTaken[0][i];
				}
				if(passTaken[1][i] != 0) {
					weight2 = passMade[1][i] / passTaken[1][i];
				}
				
				averageWeight1 += weight1;
				averageWeight2 += weight2;
				
				// player.weight = weight1 + weight2;
				player.prePassingWeight = block1Factor * weight1 + block2Factor*weight2;
				
				logger.log("Player " + playerId + ": " + player.prePassingWeight);
				
				passerQueue.add(player);
			}
			
			//WEIGHT FOR PIVOTS..cuz they are one data points short of others
			averageWeight1 /= 10;
			averageWeight2 /= 10;
			
			// first pivot weight.
			// PIVOT1 and PIVOT 2 have to be
			
			player = players.get(firstPivot - 1);
			// logger.log("" + passMade[0][firstPivot-1]);
			// logger.log("" + passTaken[0][firstPivot-1]);
			// logger.log("" + Arrays.toString(passMade[0]));
			// logger.log("" + Arrays.toString(passMade[1]));
			
			if(passTaken[0][firstPivot-1] == 0) {
				weight1 = 0;
			} else {
				weight1 = (passMade[0][firstPivot-1] / passTaken[0][firstPivot-1]);
			}
			weight2 = averageWeight2 * (weight1 / averageWeight1 );
			
			// logger.log("---FIRST PIVOT ----");
			// logger.log("weight1: " + weight1);
			// logger.log("weight2: " + weight2);
			// logger.log("weight: " + (weight1 + weight2));
			player.prePassingWeight = weight1 + weight2;
			passerQueue.add(player);

			//FOR SECOND PLAYER
			player = players.get(secondPivot - 1);
			
			if(passTaken[1][secondPivot-1] == 0) {
				weight1 = 0;
			} else {
				weight1 = (passMade[1][secondPivot-1] / passTaken[1][secondPivot-1]);
			}
			weight2 = averageWeight1 * (weight1 / averageWeight2 );
			
			// logger.log("---SECOND PIVOT ----");
			// logger.log("weight1: " + weight1);
			// logger.log("weight2: " + weight2);
			// logger.log("weight: " + (weight1 + weight2));
			player.prePassingWeight = weight1 + weight2;
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
		public List<Integer> calculateBlockers(){
			PriorityQueue<Player> blockingQueue = 
					new PriorityQueue<Player>(totalPlayers, SORT_BY_BLOCKING);
			
			Player player = null;
			
			double def1Weight = totalBlocksTaken[0] / totalBlocksMade[0];
			double def2Weight = totalBlocksTaken[1] / totalBlocksMade[1];
			
			logger.log("Blocking def1: " + def1Weight);
			logger.log("Blocking def2: " + def2Weight);
			
			double averageWeight1 = 0;
			double averageWeight2 = 0;
			
			double weight1;
			double weight2;
			
			int playerId = 0;
			for(int i = 0; i < totalPlayers; i++) {
				playerId = i + 1;
				if(playerId == firstPivot || playerId == secondPivot) {
					continue;
				}
				
				player = players.get(i);
				weight1 = 0;
				weight2 = 0;
				
				if(blocksTaken[0][i] != 0) {
					weight1 = blocksMade[0][i] / blocksTaken[0][i];
				}
				
				if(blocksTaken[1][i] != 0) {
					weight2 = blocksMade[1][i] / blocksTaken[1][i];
				}
				
				averageWeight1 += weight1;
				averageWeight2 += weight2;
				player.preBlockingWeight = def1Weight * weight1 + def2Weight * weight2;
				
				logger.log("Player " + playerId + ": " + player.preBlockingWeight);
				blockingQueue.add(player);
			}
			
			averageWeight1 /= totalPlayers - 2;
			averageWeight2 /= totalPlayers - 2;
			
			// first pivot weight.
			player = players.get(firstPivot - 1);
			if(blocksTaken[1][firstPivot - 1] == 0) {
				weight1 = 0;
			} else {
				weight1 = (blocksMade[1][firstPivot - 1] / blocksTaken[1][firstPivot - 1]);
			}
			weight2 = averageWeight2 * (weight1 / averageWeight1 );
			
			// logger.log("---FIRST PIVOT ----");
			// logger.log("weight1: " + weight1);
			// logger.log("weight2: " + weight2);
			// logger.log("weight: " + (weight1 + weight2));
			player.preBlockingWeight = weight1 + weight2;
			blockingQueue.add(player);
			
			player = players.get(secondPivot - 1);
			if(blocksTaken[1][secondPivot - 1] == 0) {
				weight1 = 0;
			} else {
				weight1 = (blocksMade[1][secondPivot - 1] / blocksTaken[1][secondPivot - 1]);
			}
			weight2 = averageWeight1 * (weight1 / averageWeight2 );
			
			// logger.log("---SECOND PIVOT ----");
			// logger.log("weight1: " + weight1);
			// logger.log("weight2: " + weight2);
			// logger.log("weight: " + (weight1 + weight2));
			player.preBlockingWeight = weight1 + weight2;
			blockingQueue.add(player);
			
			// logger.log("average1: " + averageWeight1);
			// logger.log("average2: " + averageWeight2);
			
			List<Integer> blockers = new ArrayList<Integer>(12);
			while(blockingQueue.size() > 0) {
				blockers.add(blockingQueue.poll().playerId);
			}
			
			return blockers;
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
				
			}
			return theTeamStat;
		}


	}
	
	private static Comparator<Player> SORT_BY_SHOOTING = new Comparator<Player>() {
		@Override
		public int compare(Player p1, Player p2) {
			return 	(p1.preShootingWeight > p2.preShootingWeight) ? -1 :
					(p1.preShootingWeight < p2.preShootingWeight) ?  1 : 0 ;
		}
	};
	
	private static Comparator<Player> SORT_BY_PASSING = new Comparator<Player>() {
		@Override
		public int compare(Player p1, Player p2) {
			return 	(p1.preShootingWeight > p2.preShootingWeight) ? -1 :
					(p1.preShootingWeight < p2.preShootingWeight) ?  1 : 0 ;
		}
	};
	
	private static Comparator<Player> SORT_BY_BLOCKING = new Comparator<Player>() {
		@Override
		public int compare(Player p1, Player p2) {
			return 	(p1.preBlockingWeight > p2.preBlockingWeight) ? -1 :
					(p1.preBlockingWeight < p2.preBlockingWeight) ?  1 : 0 ;
		}
	};
	
	private static Comparator<Player> SORT_BY_OVERALL = new Comparator<Player>() {
		@Override
		public int compare(Player p1, Player p2) {
			return 	(p1.getTotalWeight() > p2.getTotalWeight()) ? -1 :
					(p1.getTotalWeight() < p2.getTotalWeight()) ?  1 : 0 ;
		}
	};


	public static final Logger DEFAULT_LOGGER = new Logger() {
		@Override
		public void log(String message) {
			System.out.println(message);
		}
	};
	
	
}
