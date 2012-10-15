package hoop.g1;
import hoop.g1.Player;
import hoop.g1.OtherTeam;
import hoop.sim.Game;
import hoop.sim.Game.Round;
import java.util.Arrays;
import java.util.Map;
import java.util.List;

public class HistoryAnalyzer{
	Map<String, OtherTeam>  name2TeamObj; 
	private int lastLength;
	private Team team;
	private Logger logger;

	public HistoryAnalyzer(Team team) {
		this.name2TeamObj = team.name2OtherTeam;
		this.team = team;
	}
	
	public void setLogger(Logger logger) {
		this.logger = logger;
	}
	
	public void analyzeGameHistory(Game[] games, int totalPlayers) {
	
		if(games == null || games.length ==  0) {
			return; //no need to take history
		} 

		Game game = games[0];
		
		OtherTeam teamAObject = null;
		OtherTeam teamBObject = null;
		for(int i = lastLength; i < games.length; i++) {
			game = games[i];
			
			if(!name2TeamObj.containsKey(game.teamA)){
				teamAObject = new OtherTeam(game.teamA, totalPlayers);
				name2TeamObj.put(game.teamA, teamAObject);
				logger.log("adding new team : " + game.teamA);
			} else {
				teamAObject = name2TeamObj.get(game.teamA);
			}
			
			if(!name2TeamObj.containsKey(game.teamB)){
				teamBObject= new  OtherTeam(game.teamB,totalPlayers);
				name2TeamObj.put(game.teamB, teamBObject);
				logger.log("adding new team : " + game.teamB);
			} else {
				teamBObject = name2TeamObj.get(game.teamB);
			}
			
			//  Dont anaylze our games a second time.
			if(team.ourTeamPointer == teamBObject || team.ourTeamPointer == teamAObject) {
				return;
			}
			
			logger.log("teamA " + Arrays.toString(game.playersA()));
			logger.log("teamB " + Arrays.toString(game.playersB()));
			
			//do the update
			printGame(game, i);
			analyzeGame(game, teamAObject, teamBObject);
		}
		
		//print stats
		printStat();
	}

	private void analyzeGame(Game game, OtherTeam teamA, OtherTeam teamB) {
		for (int roundIdx=0; roundIdx < game.rounds() ; roundIdx++ ) {
			Round r = game.round(roundIdx);
			
			Player[] playersA = new Player[5];
			for(int i = 0; i < 5; i++) {
				playersA[i] = teamA.getPlayer(game.playersA()[i]);
			}
			
			Player[] playersB = new Player[5];
			for(int i = 0; i < 5; i++) {
				playersB[i] = teamB.getPlayer(game.playersA()[i]);
			}
			
			analyzeRound(r, playersA, playersB);
		}
		
	}

	public void analyzeRound(Round r, Player[] playersA, Player[] playersB) {
		int[] holders = r.holders();
		int[] defenders = r.defenders();

		Player attackBH;
		Player defendBH;

		//Passing Update
		int lastHolder = holders.length - 1;

		if(r.attacksA) {
			//team A is attacking
			attackBH = playersA[holders[lastHolder] - 1];
			defendBH = playersB[defenders[holders[lastHolder]-1] - 1];
			
		} else {
			//team B is attacking
			attackBH = playersB[holders[lastHolder] - 1];
			defendBH = playersA[defenders[holders[lastHolder]-1] - 1];
		}

		switch(r.lastAction()) {
			case MISSED: // implies that pass was successful
				// Shooter Point of view
				attackBH.shotAttempted();
				defendBH.blockAttempted();
				defendBH.blockMade();

				lastHolder--;
				
				break;
			case SCORED: // implies that pass was successful
			//th least ball holder was a shooter
				
				// Shooter Point of view
				attackBH.shotAttempted();
				attackBH.shotMade();
				
				defendBH.blockAttempted();
				lastHolder--;
				
				break;
			case STOLEN:
				attackBH.passAttempted();
				defendBH.interceptAttempted();
				defendBH.interceptMade();
				
				lastHolder--;
				break;
			default:
				break;
		}
		
		for(int i = 0; i <= lastHolder; i++) {
			if(r.attacksA) {
				//team A is attacking
				attackBH = playersA[holders[lastHolder] - 1];
				defendBH = playersB[defenders[holders[lastHolder]-1] - 1];
				
			} else {
				//team B is attacking
				attackBH = playersB[holders[lastHolder] - 1];
				defendBH = playersA[defenders[holders[lastHolder]-1] - 1];
			}
			
			attackBH.passAttempted();
			attackBH.passMade();
			
			defendBH.interceptAttempted();
		}
		
		printStat();
		printRawCount();
	}

	public void printGame(Game g, int i){
		logger.log("----------------Game History-------------------");
		
		String output = "Game: " + i + "\t" +
				g.teamA + " vs. " + g.teamB +
				" | score: " + g.scoreA + " : " + g.scoreB +
				" | teamA: " + Arrays.toString(g.playersA()) + " vs. teamB:" + Arrays.toString(g.playersB());
		
		logger.log(output);
		logger.log("| rounds: " + g.rounds());
		for(int j=0; j< g.rounds();j++){
			Round r = g.round(j);
			output = "--> Round [" + j + "] ";
			if(r.attacksA)
				output += "attack A [" + g.teamA + "]";
			else
				output += "attack B [" + g.teamB + "]";

			logger.log(output + " defenders: " + Arrays.toString(r.defenders())
								+ " | holders:  " + Arrays.toString(r.holders())
								+ " | lastAction: " + r.lastAction()

								);
			
		}
								
		logger.log("");
			
	}

	public void printStat(){
		OtherTeam teamPointer;
		for(String key : name2TeamObj.keySet()) {
			logger.log("--------------------print Stat-----------------");
			teamPointer = name2TeamObj.get(key);
			
			List<Player> pList = teamPointer.getPlayerList();
			logger.log("player     \tSw\tBw\tPw\tIw");
			
			for (Player p : pList ) {
				String output = "Player:" + p.playerId +
						"\t"+ String.format("%1$.2f", p.getShootingRatio()) +
						"\t"+ String.format("%1$.2f", p.getBlockingRatio()) +
						"\t"+ String.format("%1$.2f", p.getPassingRatio()) +
						"\t"+ String.format("%1$.2f", p.getInterceptionRatio());
				logger.log(output);
				
			}

			logger.log("pairs : " + key + " | value: " + teamPointer);
			logger.log("--------------------print Stat ENDS-----------------");
		}
	}

	public void printRawCount(){
		OtherTeam teamPointer;
		for(String key : name2TeamObj.keySet()) {
			logger.log("--------------------print RAW COUNT-----------------");
			teamPointer = name2TeamObj.get(key);
			
			List<Player> pList = teamPointer.getPlayerList();
			logger.log("player     \tSM\tSA\tPM\tPA\tBM\tBA\tIM\tIA");
			
			for (Player p : pList ) {
				String output = "Player:" + p.playerId +
						"\t"+ String.format("%1$.2f", p.getShotsMade()) +
						"\t"+ String.format("%1$.2f", p.getShotsAttempted()) +
						"\t"+ String.format("%1$.2f", p.getPassesMade()) +
						"\t"+ String.format("%1$.2f", p.getPassesAttempted()) + 
						"\t"+ String.format("%1$.2f", p.getBlocksMade()) +
						"\t"+ String.format("%1$.2f", p.getBlocksAttempted()) +
						"\t"+ String.format("%1$.2f", p.getInterceptsMade()) +
						"\t"+ String.format("%1$.2f", p.getInterceptsAttempted());
				logger.log(output);
				
			}

			logger.log("pairs : " + key + " | value: " + teamPointer);
			logger.log("--------------------print RAW COUNT ENDS-----------------");
		}
	}

}