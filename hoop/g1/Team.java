package hoop.g1;

import hoop.sim.Game.Round;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Team implements hoop.sim.Team, Logger {
	
	private static int versions;
	
	private static final boolean DEBUG = true;
	public static final int TEAM_SIZE = 5;
	
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
			
			Collections.sort(bestOverall, Player.SORT_BY_OVERALL);
			
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
			if(next.getPassingRatio() > bestPasser.getPassingRatio()) {
				bestPasser = next;
			}
		}
		
		return bestPasser.positionId;
	}
	
//	public int pickBestShooterOverall() {
//		Player bestShooter = ourPlayers.get(0);
//		
//		
//	}
//	
//	public double getCorrectedRatio(double ratio, double total) {
//		
//	}

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
				holder = getMistmatch(defenders, oldHolder);
				
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
	
	private static final double z = 1.96;
	public int getMistmatch(int[] defenders, int ballHolder) {
		double highestConfidentDiff = -10000000;
		
		List<Player> bestPlayers = new ArrayList<Player>();
		
		for(int i = 0; i < TEAM_SIZE; i++) {
			if(i + 1 == ballHolder) {
				continue;
			}
			
			Player offPlayer = currentPlayingTeam[i];
			Player defPlayer = currentOpponentTeam[defenders[i] - 1];
			
			double confidentDiff = -1000000;
			if(offPlayer.getShotsAttempted() != 0 && defPlayer.getBlocksAttempted() != 0) {
				confidentDiff = getConfidence(offPlayer, defPlayer);
			}
			
			if(confidentDiff > highestConfidentDiff) {
				bestPlayers.clear();
				bestPlayers.add(offPlayer);
				highestConfidentDiff = confidentDiff;
			} else if(confidentDiff == highestConfidentDiff) {
				bestPlayers.add(offPlayer);
			} else {
				log("Doing nothing");
			}
		}
		
		Collections.sort(bestPlayers, Player.SORT_BY_SHOOTING);
		
		return bestPlayers.get(0).positionId;
	}
	
	public int[] getBlockingMistmatch() {
		int[] team = new int[TEAM_SIZE];
		
		double bestConfidence = 10000;
		int[] bestMatch = new int[TEAM_SIZE];
		
		for(int a = 0; a < TEAM_SIZE; a++) { // All possible orderings
			for(int b = 0; b < TEAM_SIZE; b++) { // New position
				if(b == a) {continue;}
				
				for(int c = 0; c < TEAM_SIZE; c++) { // Shift
					if(c == b || c == a) {continue;}
					
					for(int d = 0; d < TEAM_SIZE; d++) {
						if(d == c || d == b || d == a) {continue;}
						
						for(int e = 0; e < TEAM_SIZE; e++) {
							if(e == d || e == c || e == b || e == a) {continue;}
							
							team[0] = a + 1;
							team[1] = b + 1;
							team[2] = c + 1;
							team[3] = d + 1;
							team[4] = e + 1;
							
							double confidence = getConfidenceOfMatch(team);
						
							if(confidence < bestConfidence) {
								bestConfidence = confidence;
								bestMatch = team;
							}
						}
					}
				}
			}
		}
		
		return bestMatch;
	}
	
	private double getConfidenceOfMatch(int[] match) {
		double sum = 0;
		
		for(int i = 0; i < TEAM_SIZE; i++) {
			Player defPlayer = currentPlayingTeam[match[i] - 1];
			Player offPlayer = currentOpponentTeam[i];
			
			if(offPlayer.getShotsAttempted() != 0 && defPlayer.getBlocksAttempted() != 0) {
				sum += getConfidence(offPlayer, defPlayer, true);
			} else {
				sum += 2;
			}
			
		}
		
		return sum;
	}
	
	public static double getConfidence(Player offPlayer, Player defPlayer) {
		return getConfidence(offPlayer, defPlayer, false);
	}
	
	public static double getConfidence(Player offPlayer, Player defPlayer, boolean upper) {
		double offShooting = offPlayer.getShootingRatio();
		double defBlocking = defPlayer.getBlockingRatio();
		
		double offStdErr = stdErr(offShooting, offPlayer.getShotsAttempted());
		double defStdErr = stdErr(defBlocking, defPlayer.getBlocksAttempted());
		
		double interval = Math.sqrt(offStdErr + defStdErr)  * z;
		
		if(upper) {
			return (offShooting - defBlocking) + interval;
		} else {
			return (offShooting - defBlocking) - interval;
		}
	}
	
	public static double stdErr(double ratio, double denom) {
		return (ratio * (1 - ratio)) / denom;
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
		
		Collections.sort(players, Player.SORT_BY_BLOCKING);
		
		List<Player> opponentPlayers = new ArrayList<Player>(TEAM_SIZE);
		opponentPlayers.addAll(Arrays.asList(currentOpponentTeam));
		
		Collections.sort(opponentPlayers, Player.SORT_BY_SHOOTING);
		
		int[] defenders = new int [5];
		for(int i = 0; i < TEAM_SIZE; i++) {
			defenders[opponentPlayers.get(i).positionId - 1] = players.get(i).positionId;  
		}
		return defenders;
		
//		return getBlockingMistmatch();
	}
}
