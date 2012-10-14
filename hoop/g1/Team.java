package hoop.g1;

import hoop.sim.Game.Round;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Random;

public class Team implements hoop.sim.Team, Logger {
	
	private static int versions;
	
	private static final Random gen = new Random();
	private static final boolean DEBUG = true;
	private static final int TEAM_SIZE = 5;
	
	protected Map<String, OtherTeam> name2OtherTeam = new HashMap<String, OtherTeam>();
	private HistoryAnalyzer historyAnalyzer = new HistoryAnalyzer(this);
	{
		historyAnalyzer.setLogger(this);
	}
	
	private boolean startedTournament;

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
				picker.initialize(totalPlayers);
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
		//intitalize the total # of players.
		void initialize(int players);

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
		private int firstPivot;
		private int secondPivot;
		
		private List<Player> players;
		
		private int[] teamA = new int[TEAM_SIZE];
		private int[] teamB = new int[TEAM_SIZE];
		
		private int testingIndex = -1;
		private int changeTester = 0;
		
		private int pickingTeam; // Changes every game twice.
		private int currentPlayer; //
		
		private int pickingDefense; // Changes every turn.
		
		private Logger logger = DEFAULT_LOGGER;
		
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
		public void initialize(int totalPlayers) {
			this.totalPlayers = totalPlayers;

			
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
			
			int[] positions = null;
			if(pickingDefense == 0) {
				positions = teamB;
			} else {
				positions = teamA;
			}
				
			switch(lastMove.action) {
				case START:
					// do the pass.
					int nextHolder = 0;
					if(changeTester < 2) {
						nextHolder = testingIndex + 1;
					} else {
						nextHolder = 1;
					}
					
					logger.log(whatTeam("attack") + ": Passing to --> playerID " + positions[testingIndex] + " ( [Sim#]: " + nextHolder + ") ");
					move = new Move(lastMove.ourPlayer, nextHolder, Status.PASSING);
					//Log the pass -Jiang
					players.get(positions[lastMove.ourPlayer-1]-1).passAttempted();
					break;
				case PASSING:
					// Shoot
					logger.log("Shooting..." + " from " + whatTeam("attack"));
					move = new Move(lastMove.ourPlayer, 0, Status.SHOOTING);
					//Log the pass -Jiang
					players.get(positions[lastMove.ourPlayer - 1] - 1).passMade();

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
				int[] offTeam = null;
				int[] defTeam = null;
				
				logger.log("AttakcsA: " + previousRound.attacksA);
				if(!previousRound.attacksA) {
					
					// B is attacking A.
					offTeam = teamB;
					defTeam = teamA;
				} else {
					// A is attacking B.
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
				int shooterIndex = playerId - 1;
				int blockerPosition = testingIndex;
				int blockerId = defTeam[blockerPosition];
				int blockerIndex = blockerId - 1;

				
				if(changeTester < 2) {
					switch(previousRound.lastAction()) {
						case SCORED:
							players.get(shooterIndex).shotMade();
						case MISSED:
							players.get(shooterIndex).shotAttempted();
							break;
						default:
							// dont care.
					}
				}
				
				if(changeTester >= 2 || shooter == 1){
					switch(previousRound.lastAction()) {
						case MISSED:
							players.get(blockerIndex).blockMade();
						case SCORED:
							players.get(blockerIndex).blockAttempted();
							break;
						default:
						// dont care.
					}
				}
				
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


	public static final Logger DEFAULT_LOGGER = new Logger() {
		@Override
		public void log(String message) {
			System.out.println(message);
		}
	};
	
	
}
